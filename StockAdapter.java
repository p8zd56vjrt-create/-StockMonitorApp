package com.example.stockmonitorapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;

import androidx.recyclerview.widget.RecyclerView;

import com.example.stockmonitorapp.R;
import com.example.stockmonitorapp.StockDetailActivity;
import com.example.stockmonitorapp.model.Stock;

import java.util.List;

public class StockAdapter extends RecyclerView.Adapter<StockAdapter.StockViewHolder> {

    private Context context;
    private List<Stock> stockList;

    public StockAdapter(Context context, List<Stock> stockList) {
        this.context = context;
        this.stockList = stockList;
    }

    @Override
    public StockViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_stock, parent, false);
        return new StockViewHolder(view);
    }

    @Override
    public void onBindViewHolder(StockViewHolder holder, int position) {
        Stock stock = stockList.get(position);
        holder.tvStockCode.setText(stock.getCode());
        holder.tvStockName.setText(stock.getName());
        holder.tvStockPrice.setText(String.format("%.2f", stock.getPrice()));
        holder.tvStockChange.setText(String.format("%.2f (%.2f%%)", stock.getChange(), stock.getChangePercent()));

        // 设置涨跌颜色
        if (stock.getChange() > 0) {
            holder.tvStockChange.setTextColor(context.getResources().getColor(R.color.colorUp));
        } else if (stock.getChange() < 0) {
            holder.tvStockChange.setTextColor(context.getResources().getColor(R.color.colorDown));
        } else {
            holder.tvStockChange.setTextColor(context.getResources().getColor(R.color.colorText));
        }

        // 显示Supertrend信息
        String trendStatus = ""; 
        int trend = stock.getTrend();
        if (trend == 1) {
            trendStatus = "上涨趋势"; 
            holder.tvTrendStatus.setTextColor(context.getResources().getColor(R.color.colorUp));
        } else if (trend == -1) {
            trendStatus = "下跌趋势"; 
            holder.tvTrendStatus.setTextColor(context.getResources().getColor(R.color.colorDown));
        } else {
            trendStatus = "无趋势"; 
            holder.tvTrendStatus.setTextColor(context.getResources().getColor(R.color.colorText));
        }
        holder.tvTrendStatus.setText(trendStatus);
        holder.tvSupertrend.setText(String.format("上轨: %.2f, 下轨: %.2f", stock.getSupertrendUp(), stock.getSupertrendDn()));

        // 设置阈值按钮点击事件
        holder.btnSetThresholds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 这里可以添加设置阈值的逻辑
                // 例如弹出对话框让用户输入买入和卖出阈值
                String buyThresholdStr = holder.etBuyThreshold.getText().toString();
                String sellThresholdStr = holder.etSellThreshold.getText().toString();

                if (!buyThresholdStr.isEmpty()) {
                    double buyThreshold = Double.parseDouble(buyThresholdStr);
                    stock.setBuyThreshold(buyThreshold);
                }

                if (!sellThresholdStr.isEmpty()) {
                    double sellThreshold = Double.parseDouble(sellThresholdStr);
                    stock.setSellThreshold(sellThreshold);
                }
            }
        });

        // 设置列表项点击事件，跳转到股票详情页
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, StockDetailActivity.class);
                intent.putExtra("stock", stock);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return stockList.size();
    }

    public class StockViewHolder extends RecyclerView.ViewHolder {
        TextView tvStockCode;
        TextView tvStockName;
        TextView tvStockPrice;
        TextView tvStockChange;
        TextView tvTrendStatus;
        TextView tvSupertrend;
        EditText etBuyThreshold;
        EditText etSellThreshold;
        Button btnSetThresholds;

        public StockViewHolder(View itemView) {
            super(itemView);
            tvStockCode = itemView.findViewById(R.id.tv_stock_code);
            tvStockName = itemView.findViewById(R.id.tv_stock_name);
            tvStockPrice = itemView.findViewById(R.id.tv_stock_price);
            tvStockChange = itemView.findViewById(R.id.tv_stock_change);
            tvTrendStatus = itemView.findViewById(R.id.tv_trend_status);
            tvSupertrend = itemView.findViewById(R.id.tv_supertrend);
            etBuyThreshold = itemView.findViewById(R.id.et_buy_threshold);
            etSellThreshold = itemView.findViewById(R.id.et_sell_threshold);
            btnSetThresholds = itemView.findViewById(R.id.btn_set_thresholds);
        }
    }
}