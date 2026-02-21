package com.family.family.vo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 价格趋势VO
 */
public class PriceTrendVO {
    
    private String itemName;        // 商品名称
    private String barcode;         // 条码
    private BigDecimal currentPrice; // 当前价格
    private BigDecimal lowestPrice;  // 最低价格
    private BigDecimal highestPrice; // 最高价格
    private BigDecimal averagePrice; // 平均价格
    private List<PricePoint> prices; // 价格点列表
    
    // Getters and Setters
    public String getItemName() {
        return itemName;
    }
    
    public void setItemName(String itemName) {
        this.itemName = itemName;
    }
    
    public String getBarcode() {
        return barcode;
    }
    
    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }
    
    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }
    
    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }
    
    public BigDecimal getLowestPrice() {
        return lowestPrice;
    }
    
    public void setLowestPrice(BigDecimal lowestPrice) {
        this.lowestPrice = lowestPrice;
    }
    
    public BigDecimal getHighestPrice() {
        return highestPrice;
    }
    
    public void setHighestPrice(BigDecimal highestPrice) {
        this.highestPrice = highestPrice;
    }
    
    public BigDecimal getAveragePrice() {
        return averagePrice;
    }
    
    public void setAveragePrice(BigDecimal averagePrice) {
        this.averagePrice = averagePrice;
    }
    
    public List<PricePoint> getPrices() {
        return prices;
    }
    
    public void setPrices(List<PricePoint> prices) {
        this.prices = prices;
    }
    
    /**
     * 价格点
     */
    public static class PricePoint {
        private LocalDate date;     // 日期
        private BigDecimal price;   // 价格
        private String storeName;   // 商店名称
        
        public LocalDate getDate() {
            return date;
        }
        
        public void setDate(LocalDate date) {
            this.date = date;
        }
        
        public BigDecimal getPrice() {
            return price;
        }
        
        public void setPrice(BigDecimal price) {
            this.price = price;
        }
        
        public String getStoreName() {
            return storeName;
        }
        
        public void setStoreName(String storeName) {
            this.storeName = storeName;
        }
    }
}