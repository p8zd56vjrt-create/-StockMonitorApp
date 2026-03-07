package com.example.stockmonitorapp.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class StockGroup implements Serializable {
    private String name;
    private List<Stock> stocks;

    public StockGroup(String name) {
        this.name = name;
        this.stocks = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Stock> getStocks() {
        return stocks;
    }

    public void setStocks(List<Stock> stocks) {
        this.stocks = stocks;
    }

    public void addStock(Stock stock) {
        if (!stocks.contains(stock)) {
            stocks.add(stock);
        }
    }

    public void removeStock(Stock stock) {
        stocks.remove(stock);
    }

    public boolean containsStock(Stock stock) {
        return stocks.contains(stock);
    }

    public int getStockCount() {
        return stocks.size();
    }
}
