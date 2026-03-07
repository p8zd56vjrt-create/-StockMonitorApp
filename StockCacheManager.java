package com.example.stockmonitorapp.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.stockmonitorapp.model.Stock;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class StockCacheManager {
    private static final String TAG = "StockCacheManager";
    private static final String CACHE_PREFIX = "stock_cache_";
    private static final String CACHE_TIMESTAMP_PREFIX = "stock_cache_timestamp_";
    private static final long CACHE_DURATION = 5 * 60 * 1000; // 5分钟缓存

    private SharedPreferences preferences;

    public StockCacheManager(Context context) {
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    // 缓存股票数据
    public void cacheStockData(Stock stock) {
        if (stock == null) return;

        try {
            // 序列化股票对象
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(stock);
            oos.close();

            byte[] stockBytes = baos.toByteArray();
            String stockData = android.util.Base64.encodeToString(stockBytes, android.util.Base64.DEFAULT);

            // 存储股票数据和时间戳
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(CACHE_PREFIX + stock.getCode(), stockData);
            editor.putLong(CACHE_TIMESTAMP_PREFIX + stock.getCode(), System.currentTimeMillis());
            editor.apply();

            Log.d(TAG, "Cached stock data for: " + stock.getCode());
        } catch (IOException e) {
            Log.e(TAG, "Error caching stock data: " + e.getMessage());
        }
    }

    // 获取缓存的股票数据
    public Stock getCachedStockData(String stockCode) {
        long timestamp = preferences.getLong(CACHE_TIMESTAMP_PREFIX + stockCode, 0);
        long currentTime = System.currentTimeMillis();

        // 检查缓存是否过期
        if (currentTime - timestamp > CACHE_DURATION) {
            Log.d(TAG, "Cache expired for: " + stockCode);
            return null;
        }

        try {
            String stockData = preferences.getString(CACHE_PREFIX + stockCode, null);
            if (stockData == null) {
                Log.d(TAG, "No cache found for: " + stockCode);
                return null;
            }

            // 反序列化股票对象
            byte[] stockBytes = android.util.Base64.decode(stockData, android.util.Base64.DEFAULT);
            ByteArrayInputStream bais = new ByteArrayInputStream(stockBytes);
            ObjectInputStream ois = new ObjectInputStream(bais);
            Stock stock = (Stock) ois.readObject();
            ois.close();

            Log.d(TAG, "Retrieved cached stock data for: " + stockCode);
            return stock;
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving cached stock data: " + e.getMessage());
            return null;
        }
    }

    // 清除指定股票的缓存
    public void clearStockCache(String stockCode) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(CACHE_PREFIX + stockCode);
        editor.remove(CACHE_TIMESTAMP_PREFIX + stockCode);
        editor.apply();
        Log.d(TAG, "Cleared cache for: " + stockCode);
    }

    // 清除所有股票缓存
    public void clearAllCache() {
        SharedPreferences.Editor editor = preferences.edit();
        Map<String, ?> allEntries = preferences.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith(CACHE_PREFIX) || key.startsWith(CACHE_TIMESTAMP_PREFIX)) {
                editor.remove(key);
            }
        }
        editor.apply();
        Log.d(TAG, "Cleared all stock caches");
    }

    // 检查是否有缓存数据
    public boolean hasCachedData(String stockCode) {
        long timestamp = preferences.getLong(CACHE_TIMESTAMP_PREFIX + stockCode, 0);
        long currentTime = System.currentTimeMillis();
        return currentTime - timestamp <= CACHE_DURATION;
    }
}
