package com.family.food.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 价格对比响应DTO
 */
@Data
public class PriceResponse {
    
    private Long storeId;
    private String storeName;
    private String storeAddress;
    private Integer distance;
    private BigDecimal price;
    private BigDecimal promotionPrice;
    private String promotionInfo;
    private String stockStatus;
    private LocalDateTime updateTime;
    private Boolean isLowest;
    private BigDecimal priceDiff;
    
    public Long getStoreId() { return storeId; }
    public void setStoreId(Long storeId) { this.storeId = storeId; }
    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }
    public String getStoreAddress() { return storeAddress; }
    public void setStoreAddress(String storeAddress) { this.storeAddress = storeAddress; }
    public Integer getDistance() { return distance; }
    public void setDistance(Integer distance) { this.distance = distance; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public BigDecimal getPromotionPrice() { return promotionPrice; }
    public void setPromotionPrice(BigDecimal promotionPrice) { this.promotionPrice = promotionPrice; }
    public String getPromotionInfo() { return promotionInfo; }
    public void setPromotionInfo(String promotionInfo) { this.promotionInfo = promotionInfo; }
    public String getStockStatus() { return stockStatus; }
    public void setStockStatus(String stockStatus) { this.stockStatus = stockStatus; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    public Boolean getIsLowest() { return isLowest; }
    public void setIsLowest(Boolean isLowest) { this.isLowest = isLowest; }
    public BigDecimal getPriceDiff() { return priceDiff; }
    public void setPriceDiff(BigDecimal priceDiff) { this.priceDiff = priceDiff; }
}
