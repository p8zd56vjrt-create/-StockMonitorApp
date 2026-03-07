package com.example.stockmonitorapp.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.stockmonitorapp.model.Stock;
import com.example.stockmonitorapp.model.StockGroup;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class StockGroupManager {
    private static final String TAG = "StockGroupManager";
    private static final String GROUPS_KEY = "stock_groups";

    private SharedPreferences preferences;

    public StockGroupManager(Context context) {
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    // 保存股票分组
    public void saveGroups(List<StockGroup> groups) {
        try {
            // 序列化分组列表
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(groups);
            oos.close();

            byte[] groupsBytes = baos.toByteArray();
            String groupsData = android.util.Base64.encodeToString(groupsBytes, android.util.Base64.DEFAULT);

            // 存储分组数据
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(GROUPS_KEY, groupsData);
            editor.apply();

            Log.d(TAG, "Saved " + groups.size() + " stock groups");
        } catch (IOException e) {
            Log.e(TAG, "Error saving stock groups: " + e.getMessage());
        }
    }

    // 加载股票分组
    public List<StockGroup> loadGroups() {
        try {
            String groupsData = preferences.getString(GROUPS_KEY, null);
            if (groupsData == null) {
                // 如果没有分组数据，返回默认分组
                return createDefaultGroups();
            }

            // 反序列化分组列表
            byte[] groupsBytes = android.util.Base64.decode(groupsData, android.util.Base64.DEFAULT);
            ByteArrayInputStream bais = new ByteArrayInputStream(groupsBytes);
            ObjectInputStream ois = new ObjectInputStream(bais);
            List<StockGroup> groups = (List<StockGroup>) ois.readObject();
            ois.close();

            Log.d(TAG, "Loaded " + groups.size() + " stock groups");
            return groups;
        } catch (Exception e) {
            Log.e(TAG, "Error loading stock groups: " + e.getMessage());
            // 加载失败，返回默认分组
            return createDefaultGroups();
        }
    }

    // 创建默认分组
    private List<StockGroup> createDefaultGroups() {
        List<StockGroup> groups = new ArrayList<>();
        groups.add(new StockGroup("我的股票"));
        groups.add(new StockGroup("关注列表"));
        Log.d(TAG, "Created default stock groups");
        return groups;
    }

    // 添加新分组
    public void addGroup(StockGroup group, List<StockGroup> groups) {
        if (!groups.contains(group)) {
            groups.add(group);
            saveGroups(groups);
            Log.d(TAG, "Added new group: " + group.getName());
        }
    }

    // 删除分组
    public void removeGroup(StockGroup group, List<StockGroup> groups) {
        if (groups.contains(group)) {
            groups.remove(group);
            saveGroups(groups);
            Log.d(TAG, "Removed group: " + group.getName());
        }
    }

    // 重命名分组
    public void renameGroup(StockGroup group, String newName, List<StockGroup> groups) {
        if (groups.contains(group)) {
            group.setName(newName);
            saveGroups(groups);
            Log.d(TAG, "Renamed group to: " + newName);
        }
    }

    // 将股票添加到分组
    public void addStockToGroup(Stock stock, StockGroup group, List<StockGroup> groups) {
        group.addStock(stock);
        saveGroups(groups);
        Log.d(TAG, "Added stock " + stock.getCode() + " to group " + group.getName());
    }

    // 从分组中移除股票
    public void removeStockFromGroup(Stock stock, StockGroup group, List<StockGroup> groups) {
        group.removeStock(stock);
        saveGroups(groups);
        Log.d(TAG, "Removed stock " + stock.getCode() + " from group " + group.getName());
    }
}
