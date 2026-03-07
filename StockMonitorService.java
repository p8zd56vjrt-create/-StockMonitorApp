package com.example.stockmonitorapp.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.stockmonitorapp.MainActivity;
import com.example.stockmonitorapp.model.Stock;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StockMonitorService {
    private static final String CHANNEL_ID = "stock_monitor_channel";
    private static final int NOTIFICATION_ID = 1;

    private Context context;
    private List<Stock> stocks;
    private Handler handler;
    private Runnable monitorRunnable;
    private boolean isMonitoring;
    // 存储股票历史数据用于计算ATR
    private List<List<Double>> stockHistoryPrices; // 存储每个股票的历史价格
    private List<List<Double>> stockHistoryHighs; // 存储每个股票的历史最高价
    private List<List<Double>> stockHistoryLows; // 存储每个股票的历史最低价
    // Supertrend参数
    private int atrPeriod = 10; // ATR周期
    private double atrMultiplier = 3.0; // ATR乘数
    private boolean changeAtrCalculation = true; // 是否使用标准ATR计算
    // 网络请求相关
    private retrofit2.Retrofit retrofit;
    private StockService stockService;
    // 通知设置
    private boolean notificationEnabled;
    private int notificationFrequency;
    private String notificationType;
    // 数据缓存
    private StockCacheManager cacheManager;

    public StockMonitorService(Context context) {
        this.context = context;
        this.stocks = new ArrayList<>();
        this.stockHistoryPrices = new ArrayList<>();
        this.stockHistoryHighs = new ArrayList<>();
        this.stockHistoryLows = new ArrayList<>();
        this.handler = new Handler(Looper.getMainLooper());
        this.isMonitoring = false;
        this.cacheManager = new StockCacheManager(context);
        createNotificationChannel();
        // 初始化通知设置
        loadNotificationSettings();
        // 初始化Retrofit
        initRetrofit();
    }

    // 加载通知设置
    private void loadNotificationSettings() {
        android.content.SharedPreferences preferences = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        notificationEnabled = preferences.getBoolean("notification_enabled", true);
        String frequencyStr = preferences.getString("notification_frequency", "5");
        notificationFrequency = Integer.parseInt(frequencyStr);
        notificationType = preferences.getString("notification_type", "all");
    }

    // 初始化Retrofit
    private void initRetrofit() {
        retrofit = new retrofit2.Retrofit.Builder()
            .baseUrl("http://hq.sinajs.cn") // 新浪财经API
            .addConverterFactory(retrofit2.converter.scalars.ScalarsConverterFactory.create()) // 直接返回字符串
            .build();
        
        stockService = retrofit.create(StockService.class);
    }

    public void addStock(Stock stock) {
        stocks.add(stock);
        // 为新股票初始化历史数据列表
        List<Double> prices = new ArrayList<>();
        List<Double> highs = new ArrayList<>();
        List<Double> lows = new ArrayList<>();
        // 初始化历史数据，使用当前价格作为初始值
        double initialPrice = stock.getPrice();
        for (int i = 0; i < atrPeriod + 1; i++) {
            prices.add(initialPrice);
            highs.add(initialPrice * 1.01); // 假设最高价为当前价格的1.01倍
            lows.add(initialPrice * 0.99); // 假设最低价为当前价格的0.99倍
        }
        stockHistoryPrices.add(prices);
        stockHistoryHighs.add(highs);
        stockHistoryLows.add(lows);
    }

    public void startMonitoring() {
        if (!isMonitoring) {
            isMonitoring = true;
            monitorRunnable = new Runnable() {
                @Override
                public void run() {
                    monitorStocks();
                    // 根据通知设置中的频率来设置监控间隔
                    handler.postDelayed(this, notificationFrequency * 1000);
                }
            };
            handler.post(monitorRunnable);
        }
    }

    public void stopMonitoring() {
        if (isMonitoring) {
            isMonitoring = false;
            handler.removeCallbacks(monitorRunnable);
        }
    }

    private void monitorStocks() {
        for (Stock stock : stocks) {
            // 模拟股票价格变化
            updateStockPrice(stock);
            // 检查是否达到买入或卖出阈值
            checkThresholds(stock);
        }
    }

    private void updateStockPrice(Stock stock) {
        // 首先检查缓存
        Stock cachedStock = cacheManager.getCachedStockData(stock.getCode());
        if (cachedStock != null) {
            // 使用缓存数据更新股票信息
            stock.setName(cachedStock.getName());
            stock.setPrice(cachedStock.getPrice());
            stock.setChange(cachedStock.getChange());
            stock.setChangePercent(cachedStock.getChangePercent());
            stock.setTrend(cachedStock.getTrend());
            stock.setAtr(cachedStock.getAtr());
            stock.setSupertrendUp(cachedStock.getSupertrendUp());
            stock.setSupertrendDn(cachedStock.getSupertrendDn());
            stock.setMacd(cachedStock.getMacd());
            stock.setMacdSignal(cachedStock.getMacdSignal());
            stock.setMacdHistogram(cachedStock.getMacdHistogram());
            stock.setRsi(cachedStock.getRsi());
            stock.setBollingerUpper(cachedStock.getBollingerUpper());
            stock.setBollingerMiddle(cachedStock.getBollingerMiddle());
            stock.setBollingerLower(cachedStock.getBollingerLower());
            return;
        }

        // 转换股票代码为新浪财经格式
        String stockCode = stock.getCode();
        if (!stockCode.startsWith("sh") && !stockCode.startsWith("sz")) {
            // 根据股票代码判断是沪市还是深市
            if (stockCode.startsWith("6")) {
                stockCode = "sh" + stockCode;
            } else {
                stockCode = "sz" + stockCode;
            }
        }

        // 使用新浪财经API获取股票数据
        if (stockService != null) {
            retrofit2.Call<String> call = stockService.getStockData(stockCode);
            call.enqueue(new retrofit2.Callback<String>() {
                @Override
                public void onResponse(retrofit2.Call<String> call, retrofit2.Response<String> response) {
                    if (response.isSuccessful()) {
                        String data = response.body();
                        if (data != null) {
                            // 解析新浪财经返回的数据
                            parseSinaStockData(stock, data);
                            // 缓存股票数据
                            cacheManager.cacheStockData(stock);
                        }
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<String> call, Throwable t) {
                    t.printStackTrace();
                    // 如果API调用失败，使用模拟数据
                    updateWithMockData(stock);
                    // 缓存模拟数据
                    cacheManager.cacheStockData(stock);
                }
            });
        } else {
            // 如果Retrofit未初始化，使用模拟数据
            updateWithMockData(stock);
            // 缓存模拟数据
            cacheManager.cacheStockData(stock);
        }
    }

    // 解析新浪财经股票数据
    private void parseSinaStockData(Stock stock, String data) {
        // 新浪财经API返回格式: var hq_str_sh600000="浦发银行,10.00,10.10,9.90,10.20,9.80,9.95,9.96,1000000,9950000,100,9.95,200,9.94,300,9.93,400,9.92,500,9.91,100,9.96,200,9.97,300,9.98,400,9.99,500,10.00,2023-03-07,15:00:00,00";
        try {
            // 提取股票数据部分
            int startIndex = data.indexOf("=");
            int endIndex = data.lastIndexOf(";");
            if (startIndex != -1 && endIndex != -1) {
                String stockData = data.substring(startIndex + 1, endIndex).replace("\"", "");
                String[] parts = stockData.split(",");
                
                if (parts.length > 3) {
                    // 解析股票名称
                    String name = parts[0];
                    // 解析当前价格
                    double newPrice = Double.parseDouble(parts[1]);
                    // 解析开盘价
                    double openPrice = Double.parseDouble(parts[2]);
                    // 解析昨收价
                    double prevClose = Double.parseDouble(parts[3]);
                    // 解析最高价
                    double newHigh = Double.parseDouble(parts[4]);
                    // 解析最低价
                    double newLow = Double.parseDouble(parts[5]);
                    
                    // 计算涨跌幅
                    double change = newPrice - prevClose;
                    double changePercent = (change / prevClose) * 100;

                    // 更新股票信息
                    stock.setName(name);
                    stock.setPrice(newPrice);
                    stock.setChange(change);
                    stock.setChangePercent(changePercent);

                    // 更新历史数据
                    updateStockHistory(stock, newPrice, newHigh, newLow);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 解析失败，使用模拟数据
            updateWithMockData(stock);
        }
    }

    // 使用模拟数据更新股票价格
    private void updateWithMockData(Stock stock) {
        // 模拟股票价格变化
        Random random = new Random();
        double priceChange = (random.nextDouble() - 0.5) * 2; // -1 到 1 之间的随机变化
        double newPrice = stock.getPrice() + priceChange;
        double newHigh = newPrice * (1 + random.nextDouble() * 0.02); // 随机生成最高价
        double newLow = newPrice * (1 - random.nextDouble() * 0.02); // 随机生成最低价
        double change = newPrice - stock.getPrice();
        double changePercent = (change / stock.getPrice()) * 100;

        // 更新股票价格
        stock.setPrice(newPrice);
        stock.setChange(change);
        stock.setChangePercent(changePercent);

        // 更新历史数据
        updateStockHistory(stock, newPrice, newHigh, newLow);
    }

    // 更新股票历史数据
    private void updateStockHistory(Stock stock, double newPrice, double newHigh, double newLow) {
        int stockIndex = stocks.indexOf(stock);
        if (stockIndex >= 0) {
            List<Double> prices = stockHistoryPrices.get(stockIndex);
            List<Double> highs = stockHistoryHighs.get(stockIndex);
            List<Double> lows = stockHistoryLows.get(stockIndex);

            // 添加新数据并移除最旧的数据
            prices.add(newPrice);
            highs.add(newHigh);
            lows.add(newLow);
            if (prices.size() > atrPeriod + 1) {
                prices.remove(0);
                highs.remove(0);
                lows.remove(0);
            }

            // 计算ATR
            double atr = calculateATR(prices, highs, lows);
            stock.setAtr(atr);

            // 计算Supertrend
            calculateSupertrend(stock, prices, highs, lows, atr);
            
            // 计算MACD
            calculateMACD(stock, prices);
            
            // 计算RSI
            calculateRSI(stock, prices);
            
            // 计算布林带
            calculateBollingerBands(stock, prices);
        }
    }

    // 计算ATR (平均真实范围)
    private double calculateATR(List<Double> prices, List<Double> highs, List<Double> lows) {
        if (prices.size() < 2) {
            return 0.0;
        }

        double[] tr = new double[prices.size() - 1];
        for (int i = 1; i < prices.size(); i++) {
            double highLow = highs.get(i) - lows.get(i);
            double highPrevClose = Math.abs(highs.get(i) - prices.get(i - 1));
            double lowPrevClose = Math.abs(lows.get(i) - prices.get(i - 1));
            tr[i - 1] = Math.max(highLow, Math.max(highPrevClose, lowPrevClose));
        }

        if (changeAtrCalculation) {
            // 标准ATR计算（使用移动平均）
            double sum = 0;
            for (double value : tr) {
                sum += value;
            }
            return sum / tr.length;
        } else {
            // 使用SMA计算ATR
            double sum = 0;
            int count = Math.min(tr.length, atrPeriod);
            for (int i = tr.length - count; i < tr.length; i++) {
                sum += tr[i];
            }
            return sum / count;
        }
    }

    // 计算Supertrend指标
    private void calculateSupertrend(Stock stock, List<Double> prices, List<Double> highs, List<Double> lows, double atr) {
        double src = (highs.get(highs.size() - 1) + lows.get(lows.size() - 1)) / 2;
        double up = src - (atrMultiplier * atr);
        double dn = src + (atrMultiplier * atr);

        // 更新Supertrend上轨和下轨
        if (stock.getSupertrendUp() > 0) {
            up = Math.max(up, stock.getSupertrendUp());
        }
        if (stock.getSupertrendDn() > 0) {
            dn = Math.min(dn, stock.getSupertrendDn());
        }

        stock.setSupertrendUp(up);
        stock.setSupertrendDn(dn);

        // 判断趋势
        int previousTrend = stock.getTrend();
        int newTrend = previousTrend;

        if (previousTrend == 0) {
            // 初始趋势判断
            newTrend = stock.getPrice() > dn ? 1 : -1;
        } else if (previousTrend == 1 && stock.getPrice() < up) {
            // 从上涨趋势转为下跌趋势
            newTrend = -1;
            // 生成卖出信号
            sendNotification(stock, "卖出信号", stock.getName() + " Supertrend卖出信号");
        } else if (previousTrend == -1 && stock.getPrice() > dn) {
            // 从下跌趋势转为上涨趋势
            newTrend = 1;
            // 生成买入信号
            sendNotification(stock, "买入信号", stock.getName() + " Supertrend买入信号");
        }

        stock.setTrend(newTrend);
    }

    // 计算MACD指标
    private void calculateMACD(Stock stock, List<Double> prices) {
        int fastPeriod = 12;
        int slowPeriod = 26;
        int signalPeriod = 9;

        if (prices.size() < slowPeriod + signalPeriod) {
            return;
        }

        // 计算EMA
        double fastEMA = calculateEMA(prices, fastPeriod);
        double slowEMA = calculateEMA(prices, slowPeriod);
        double macdLine = fastEMA - slowEMA;

        // 计算信号线
        List<Double> macdLineList = new ArrayList<>();
        for (int i = prices.size() - slowPeriod - signalPeriod; i < prices.size(); i++) {
            double tempFastEMA = calculateEMA(prices.subList(i - fastPeriod + 1, i + 1), fastPeriod);
            double tempSlowEMA = calculateEMA(prices.subList(i - slowPeriod + 1, i + 1), slowPeriod);
            macdLineList.add(tempFastEMA - tempSlowEMA);
        }

        double signalLine = calculateEMA(macdLineList, signalPeriod);
        double histogram = macdLine - signalLine;

        stock.setMacd(macdLine);
        stock.setMacdSignal(signalLine);
        stock.setMacdHistogram(histogram);
    }

    // 计算EMA
    private double calculateEMA(List<Double> prices, int period) {
        double multiplier = 2.0 / (period + 1);
        double ema = prices.get(0);

        for (int i = 1; i < prices.size(); i++) {
            ema = (prices.get(i) - ema) * multiplier + ema;
        }

        return ema;
    }

    // 计算RSI指标
    private void calculateRSI(Stock stock, List<Double> prices) {
        int period = 14;

        if (prices.size() < period + 1) {
            return;
        }

        double[] changes = new double[prices.size() - 1];
        for (int i = 1; i < prices.size(); i++) {
            changes[i - 1] = prices.get(i) - prices.get(i - 1);
        }

        double sumGain = 0;
        double sumLoss = 0;

        for (int i = 0; i < period; i++) {
            if (changes[i] > 0) {
                sumGain += changes[i];
            } else {
                sumLoss += Math.abs(changes[i]);
            }
        }

        double avgGain = sumGain / period;
        double avgLoss = sumLoss / period;

        for (int i = period; i < changes.length; i++) {
            if (changes[i] > 0) {
                avgGain = (avgGain * (period - 1) + changes[i]) / period;
                avgLoss = (avgLoss * (period - 1)) / period;
            } else {
                avgGain = (avgGain * (period - 1)) / period;
                avgLoss = (avgLoss * (period - 1) + Math.abs(changes[i])) / period;
            }
        }

        double rs = avgLoss == 0 ? 100 : avgGain / avgLoss;
        double rsi = 100 - (100 / (1 + rs));

        stock.setRsi(rsi);
    }

    // 计算布林带指标
    private void calculateBollingerBands(Stock stock, List<Double> prices) {
        int period = 20;
        double stdDev = 2.0;

        if (prices.size() < period) {
            return;
        }

        // 计算中轨（MA）
        double sum = 0;
        for (int i = prices.size() - period; i < prices.size(); i++) {
            sum += prices.get(i);
        }
        double middleBand = sum / period;

        // 计算标准差
        double variance = 0;
        for (int i = prices.size() - period; i < prices.size(); i++) {
            variance += Math.pow(prices.get(i) - middleBand, 2);
        }
        double standardDeviation = Math.sqrt(variance / period);

        // 计算上轨和下轨
        double upperBand = middleBand + (standardDeviation * stdDev);
        double lowerBand = middleBand - (standardDeviation * stdDev);

        stock.setBollingerUpper(upperBand);
        stock.setBollingerMiddle(middleBand);
        stock.setBollingerLower(lowerBand);
    }

    private void checkThresholds(Stock stock) {
        // 这里可以根据实际需求设置阈值检查逻辑
        // 示例：当股票价格上涨超过5%时发送买入提示，下跌超过5%时发送卖出提示
        if (stock.getChangePercent() > 5) {
            sendNotification(stock, "买入提示", stock.getName() + " 上涨超过5%，建议买入");
        } else if (stock.getChangePercent() < -5) {
            sendNotification(stock, "卖出提示", stock.getName() + " 下跌超过5%，建议卖出");
        }
    }

    private void sendNotification(Stock stock, String title, String message) {
        // 检查通知是否开启
        if (!notificationEnabled) {
            return;
        }

        // 检查通知类型
        boolean shouldSend = false;
        if (notificationType.equals("all")) {
            shouldSend = true;
        } else if (notificationType.equals("technical") && (title.contains("买入信号") || title.contains("卖出信号"))) {
            shouldSend = true;
        } else if (notificationType.equals("price") && (title.contains("买入提示") || title.contains("卖出提示"))) {
            shouldSend = true;
        }

        if (!shouldSend) {
            return;
        }

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // 为每个股票使用不同的通知ID，避免通知被覆盖
        int notificationId = stock.getCode().hashCode();

        // 构建更详细的通知内容
        String detailedMessage = message + "\n当前价格: " + String.format("%.2f", stock.getPrice())
                + "\nSupertrend上轨: " + String.format("%.2f", stock.getSupertrendUp())
                + "\nSupertrend下轨: " + String.format("%.2f", stock.getSupertrendDn());

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(detailedMessage))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setVibrate(new long[]{1000, 1000}); // 添加振动提示

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId, builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "股票监控";
            String description = "股票价格变动通知";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
    }
}