package com.example.stockmonitorapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.content.Intent;

import com.example.stockmonitorapp.adapter.StockAdapter;
import com.example.stockmonitorapp.model.Stock;
import com.example.stockmonitorapp.model.StockGroup;
import com.example.stockmonitorapp.service.StockService;
import com.example.stockmonitorapp.service.StockMonitorService;
import com.example.stockmonitorapp.service.StockGroupManager;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText etStockCode;
    private Button btnAddStock;
    private EditText etSearch;
    private Button btnSearch;
    private Button btnSettings;
    private Button btnManageGroups;
    private Spinner spinnerGroups;
    private RecyclerView rvStocks;
    private StockAdapter stockAdapter;
    private List<Stock> stockList;
    private StockMonitorService stockMonitorService;
    private StockGroupManager groupManager;
    private List<StockGroup> stockGroups;
    private StockGroup currentGroup;
    private ArrayAdapter<String> groupAdapter;
    // 模拟股票数据库
    private List<Stock> stockDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 应用主题设置
        applyTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etStockCode = findViewById(R.id.et_stock_code);
        btnAddStock = findViewById(R.id.btn_add_stock);
        etSearch = findViewById(R.id.et_search);
        btnSearch = findViewById(R.id.btn_search);
        btnSettings = findViewById(R.id.btn_settings);
        btnManageGroups = findViewById(R.id.btn_manage_groups);
        spinnerGroups = findViewById(R.id.spinner_groups);
        rvStocks = findViewById(R.id.rv_stocks);

        stockList = new ArrayList<>();
        stockAdapter = new StockAdapter(this, stockList);
        rvStocks.setLayoutManager(new LinearLayoutManager(this));
        rvStocks.setAdapter(stockAdapter);

        // 初始化模拟股票数据库
        initStockDatabase();

        // 初始化股票监控服务
        stockMonitorService = new StockMonitorService(this);

        // 初始化分组管理器
        groupManager = new StockGroupManager(this);
        stockGroups = groupManager.loadGroups();

        // 初始化分组选择器
        initGroupSpinner();

        // 添加股票按钮点击事件
        btnAddStock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String stockCode = etStockCode.getText().toString().trim();
                if (!stockCode.isEmpty()) {
                    addStock(stockCode);
                    etStockCode.setText("");
                } else {
                    Toast.makeText(MainActivity.this, "请输入股票代码", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 搜索按钮点击事件
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchText = etSearch.getText().toString().trim();
                if (!searchText.isEmpty()) {
                    searchStock(searchText);
                } else {
                    Toast.makeText(MainActivity.this, "请输入搜索内容", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 设置按钮点击事件
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        // 分组管理按钮点击事件
        btnManageGroups.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 这里可以打开分组管理界面
                Toast.makeText(MainActivity.this, "分组管理功能开发中", Toast.LENGTH_SHORT).show();
            }
        });

        // 分组选择器选择事件
        spinnerGroups.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (position < stockGroups.size()) {
                    currentGroup = stockGroups.get(position);
                    loadStocksFromCurrentGroup();
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });

        // 启动监控服务
        stockMonitorService.startMonitoring();
    }

    // 初始化分组选择器
    private void initGroupSpinner() {
        List<String> groupNames = new ArrayList<>();
        for (StockGroup group : stockGroups) {
            groupNames.add(group.getName());
        }

        groupAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, groupNames);
        groupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGroups.setAdapter(groupAdapter);

        // 默认选择第一个分组
        if (!stockGroups.isEmpty()) {
            currentGroup = stockGroups.get(0);
            loadStocksFromCurrentGroup();
        }
    }

    // 从当前分组加载股票
    private void loadStocksFromCurrentGroup() {
        if (currentGroup != null) {
            stockList.clear();
            stockList.addAll(currentGroup.getStocks());
            stockAdapter.notifyDataSetChanged();
        }
    }

    // 初始化模拟股票数据库
    private void initStockDatabase() {
        stockDatabase = new ArrayList<>();
        // 添加一些常见的股票
        stockDatabase.add(new Stock("600000", "浦发银行", 10.00, 0.0, 0.0));
        stockDatabase.add(new Stock("600519", "贵州茅台", 1800.00, 0.0, 0.0));
        stockDatabase.add(new Stock("000001", "平安银行", 15.00, 0.0, 0.0));
        stockDatabase.add(new Stock("000858", "五粮液", 160.00, 0.0, 0.0));
        stockDatabase.add(new Stock("601318", "中国平安", 50.00, 0.0, 0.0));
        stockDatabase.add(new Stock("600036", "招商银行", 35.00, 0.0, 0.0));
        stockDatabase.add(new Stock("601888", "中国中免", 180.00, 0.0, 0.0));
        stockDatabase.add(new Stock("600276", "恒瑞医药", 30.00, 0.0, 0.0));
        stockDatabase.add(new Stock("601398", "工商银行", 5.00, 0.0, 0.0));
        stockDatabase.add(new Stock("601288", "农业银行", 3.00, 0.0, 0.0));
    }

    // 搜索股票
    private void searchStock(String searchText) {
        List<Stock> searchResults = new ArrayList<>();
        for (Stock stock : stockDatabase) {
            if (stock.getCode().contains(searchText) || stock.getName().contains(searchText)) {
                searchResults.add(stock);
            }
        }

        if (searchResults.isEmpty()) {
            Toast.makeText(this, "未找到匹配的股票", Toast.LENGTH_SHORT).show();
        } else {
            // 显示搜索结果
            StringBuilder result = new StringBuilder("搜索结果:\n");
            for (Stock stock : searchResults) {
                result.append(stock.getCode()).append(" - " ).append(stock.getName()).append("\n");
            }
            Toast.makeText(this, result.toString(), Toast.LENGTH_LONG).show();
        }
    }

    private void addStock(String stockCode) {
        // 从模拟股票数据库中查找股票
        Stock stock = null;
        for (Stock s : stockDatabase) {
            if (s.getCode().equals(stockCode)) {
                stock = s;
                break;
            }
        }

        // 如果找到股票，使用数据库中的股票名称和价格
        if (stock != null) {
            // 添加到当前分组
            if (currentGroup != null) {
                groupManager.addStockToGroup(stock, currentGroup, stockGroups);
                loadStocksFromCurrentGroup();
            }

            // 添加到监控列表
            stockMonitorService.addStock(stock);

            Toast.makeText(this, "股票添加成功: " + stock.getName(), Toast.LENGTH_SHORT).show();
        } else {
            // 如果没找到，使用默认值
            stock = new Stock(stockCode, "股票名称", 100.0, 0.0, 0.0);
            
            // 添加到当前分组
            if (currentGroup != null) {
                groupManager.addStockToGroup(stock, currentGroup, stockGroups);
                loadStocksFromCurrentGroup();
            }

            // 添加到监控列表
            stockMonitorService.addStock(stock);

            Toast.makeText(this, "股票添加成功", Toast.LENGTH_SHORT).show();
        }
    }

    // 应用主题设置
    private void applyTheme() {
        android.content.SharedPreferences preferences = android.preference.PreferenceManager.getDefaultSharedPreferences(this);
        boolean darkModeEnabled = preferences.getBoolean("dark_mode", false);
        if (darkModeEnabled) {
            setTheme(R.style.AppTheme_Dark);
        } else {
            setTheme(R.style.AppTheme);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 停止监控服务
        stockMonitorService.stopMonitoring();
    }
}