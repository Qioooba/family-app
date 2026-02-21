package com.family.food.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 价格趋势响应DTO
 */
public class PriceTrendResponse {
    
    private String productName;
    private String barcode;
    private BigDecimal currentAvgPrice;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String trend;
    private BigDecimal trendPercent;
    private List<PriceHistoryPoint> historyPoints;
    
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
    public BigDecimal getCurrentAvgPrice() { return currentAvgPrice; }
    public void setCurrentAvgPrice(BigDecimal currentAvgPrice) { this.currentAvgPrice = currentAvgPrice; }
    public BigDecimal getMinPrice() { return minPrice; }
    public void setMinPrice(BigDecimal minPrice) { this.minPrice = minPrice; }
    public BigDecimal getMaxPrice() { return maxPrice; }
    public void setMaxPrice(BigDecimal maxPrice) { this.maxPrice = maxPrice; }
    public String getTrend() { return trend; }
    public void setTrend(String trend) { this.trend = trend; }
    public BigDecimal getTrendPercent() { return trendPercent; }
    public void setTrendPercent(BigDecimal trendPercent) { this.trendPercent = trendPercent; }
    public List<PriceHistoryPoint> getHistoryPoints() { return historyPoints; }
    public void setHistoryPoints(List<PriceHistoryPoint> historyPoints) { this.historyPoints = historyPoints; }
    
    public static class PriceHistoryPoint {
        private String date;
        private BigDecimal avgPrice;
        private BigDecimal minPrice;
        private BigDecimal maxPrice;
        
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public BigDecimal getAvgPrice() { return avgPrice; }
        public void setAvgPrice(BigDecimal avgPrice) { this.avgPrice = avgPrice; }
        public BigDecimal getMinPrice() { return minPrice; }
        public void setMinPrice(BigDecimal minPrice) { this.minPrice = minPrice; }
        public BigDecimal getMaxPrice() { return maxPrice; }
        public void setMaxPrice(BigDecimal maxPrice) { this.maxPrice = maxPrice; }
    }
}
