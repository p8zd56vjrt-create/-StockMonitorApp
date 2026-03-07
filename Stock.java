package com.example.stockmonitorapp.model;

import java.io.Serializable;

public class Stock implements Serializable {
    private String code;
    private String name;
    private double price;
    private double change;
    private double changePercent;
    private double buyThreshold;
    private double sellThreshold;
    private int trend; // 1: 上涨趋势, -1: 下跌趋势, 0: 无趋势
    private double atr; // 平均真实范围
    private double supertrendUp; // Supertrend上轨
    private double supertrendDn; // Supertrend下轨
    // MACD指标
    private double macd; // MACD线
    private double macdSignal; // 信号线
    private double macdHistogram; // 柱状图
    // RSI指标
    private double rsi; // RSI值
    // 布林带指标
    private double bollingerUpper; // 布林带上轨
    private double bollingerMiddle; // 布林带中轨
    private double bollingerLower; // 布林带下轨

    public Stock(String code, String name, double price, double change, double changePercent) {
        this.code = code;
        this.name = name;
        this.price = price;
        this.change = change;
        this.changePercent = changePercent;
        this.buyThreshold = 0.0;
        this.sellThreshold = 0.0;
        this.trend = 0;
        this.atr = 0.0;
        this.supertrendUp = 0.0;
        this.supertrendDn = 0.0;
        this.macd = 0.0;
        this.macdSignal = 0.0;
        this.macdHistogram = 0.0;
        this.rsi = 0.0;
        this.bollingerUpper = 0.0;
        this.bollingerMiddle = 0.0;
        this.bollingerLower = 0.0;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getChange() {
        return change;
    }

    public void setChange(double change) {
        this.change = change;
    }

    public double getChangePercent() {
        return changePercent;
    }

    public void setChangePercent(double changePercent) {
        this.changePercent = changePercent;
    }

    public double getBuyThreshold() {
        return buyThreshold;
    }

    public void setBuyThreshold(double buyThreshold) {
        this.buyThreshold = buyThreshold;
    }

    public double getSellThreshold() {
        return sellThreshold;
    }

    public void setSellThreshold(double sellThreshold) {
        this.sellThreshold = sellThreshold;
    }

    public int getTrend() {
        return trend;
    }

    public void setTrend(int trend) {
        this.trend = trend;
    }

    public double getAtr() {
        return atr;
    }

    public void setAtr(double atr) {
        this.atr = atr;
    }

    public double getSupertrendUp() {
        return supertrendUp;
    }

    public void setSupertrendUp(double supertrendUp) {
        this.supertrendUp = supertrendUp;
    }

    public double getSupertrendDn() {
        return supertrendDn;
    }

    public void setSupertrendDn(double supertrendDn) {
        this.supertrendDn = supertrendDn;
    }

    // MACD指标
    public double getMacd() {
        return macd;
    }

    public void setMacd(double macd) {
        this.macd = macd;
    }

    public double getMacdSignal() {
        return macdSignal;
    }

    public void setMacdSignal(double macdSignal) {
        this.macdSignal = macdSignal;
    }

    public double getMacdHistogram() {
        return macdHistogram;
    }

    public void setMacdHistogram(double macdHistogram) {
        this.macdHistogram = macdHistogram;
    }

    // RSI指标
    public double getRsi() {
        return rsi;
    }

    public void setRsi(double rsi) {
        this.rsi = rsi;
    }

    // 布林带指标
    public double getBollingerUpper() {
        return bollingerUpper;
    }

    public void setBollingerUpper(double bollingerUpper) {
        this.bollingerUpper = bollingerUpper;
    }

    public double getBollingerMiddle() {
        return bollingerMiddle;
    }

    public void setBollingerMiddle(double bollingerMiddle) {
        this.bollingerMiddle = bollingerMiddle;
    }

    public double getBollingerLower() {
        return bollingerLower;
    }

    public void setBollingerLower(double bollingerLower) {
        this.bollingerLower = bollingerLower;
    }
}