package com.example.stockmonitorapp;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.stockmonitorapp.model.Stock;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.List;

public class StockDetailActivity extends AppCompatActivity {

    private TextView tvStockCode;
    private TextView tvStockName;
    private TextView tvStockPrice;
    private TextView tvStockChange;
    private TextView tvMacd;
    private TextView tvRsi;
    private TextView tvBollinger;
    private TextView tvSupertrend;
    private LineChart lineChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_detail);

        // 初始化UI组件
        tvStockCode = findViewById(R.id.tv_stock_code_detail);
        tvStockName = findViewById(R.id.tv_stock_name_detail);
        tvStockPrice = findViewById(R.id.tv_stock_price_detail);
        tvStockChange = findViewById(R.id.tv_stock_change_detail);
        tvMacd = findViewById(R.id.tv_macd);
        tvRsi = findViewById(R.id.tv_rsi);
        tvBollinger = findViewById(R.id.tv_bollinger);
        tvSupertrend = findViewById(R.id.tv_supertrend);
        lineChart = findViewById(R.id.line_chart);

        // 获取股票数据
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            Stock stock = (Stock) extras.getSerializable("stock");
            if (stock != null) {
                // 显示股票基本信息
                tvStockCode.setText(stock.getCode());
                tvStockName.setText(stock.getName());
                tvStockPrice.setText(String.format("%.2f", stock.getPrice()));
                tvStockChange.setText(String.format("%.2f (%.2f%%)", stock.getChange(), stock.getChangePercent()));

                // 设置涨跌颜色
                if (stock.getChange() > 0) {
                    tvStockChange.setTextColor(getResources().getColor(R.color.colorUp));
                } else if (stock.getChange() < 0) {
                    tvStockChange.setTextColor(getResources().getColor(R.color.colorDown));
                }

                // 显示技术指标
                tvMacd.setText(String.format("%.2f, %.2f, %.2f", stock.getMacd(), stock.getMacdSignal(), stock.getMacdHistogram()));
                tvRsi.setText(String.format("%.2f", stock.getRsi()));
                tvBollinger.setText(String.format("%.2f, %.2f, %.2f", stock.getBollingerUpper(), stock.getBollingerMiddle(), stock.getBollingerLower()));
                tvSupertrend.setText(String.format("上轨: %.2f, 下轨: %.2f", stock.getSupertrendUp(), stock.getSupertrendDn()));

                // 初始化图表
                initChart(stock);
            }
        }
    }

    private void initChart(Stock stock) {
        // 模拟股票历史数据
        List<Entry> entries = new ArrayList<>();
        double basePrice = stock.getPrice();
        for (int i = 0; i < 30; i++) {
            double price = basePrice + (Math.random() - 0.5) * 10;
            entries.add(new Entry(i, (float) price));
        }

        // 创建数据集
        LineDataSet dataSet = new LineDataSet(entries, "股价走势");
        dataSet.setColor(getResources().getColor(R.color.colorPrimary));
        dataSet.setLineWidth(2f);
        dataSet.setCircleColor(getResources().getColor(R.color.colorPrimary));
        dataSet.setCircleRadius(3f);
        dataSet.setDrawCircleHole(false);
        dataSet.setValueTextSize(10f);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        // 创建数据
        List<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(dataSet);
        LineData lineData = new LineData(dataSets);

        // 设置图表
        lineChart.setData(lineData);
        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.setScaleEnabled(true);
        lineChart.setPinchZoom(true);
        lineChart.invalidate();
    }
}