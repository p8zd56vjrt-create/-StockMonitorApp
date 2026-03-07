package com.example.stockmonitorapp.service;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface StockService {
    @GET("/list")
    Call<String> getStockData(@Query("list") String stockCode);
}