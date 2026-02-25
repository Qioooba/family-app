package com.family.family.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.family.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 商品价格记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("price_record")
public class PriceRecord extends BaseEntity {
    private static final long serialVersionUID = 1L;
    
    private String barcode;
    private String productName;
    private String brand;
    private String specification;
    private Long storeId;
    private String storeName;
    private BigDecimal price;
    private BigDecimal promotionPrice;
    private String promotionInfo;
    private String stockStatus;
    private LocalDateTime priceDate;
    private Double latitude;
    private Double longitude;
    private String submitterId;
    private Integer verifyStatus;
    private Integer source;
    
    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getSpecification() { return specification; }
    public void setSpecification(String specification) { this.specification = specification; }
    public Long getStoreId() { return storeId; }
    public void setStoreId(Long storeId) { this.storeId = storeId; }
    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public BigDecimal getPromotionPrice() { return promotionPrice; }
    public void setPromotionPrice(BigDecimal promotionPrice) { this.promotionPrice = promotionPrice; }
    public String getPromotionInfo() { return promotionInfo; }
    public void setPromotionInfo(String promotionInfo) { this.promotionInfo = promotionInfo; }
    public String getStockStatus() { return stockStatus; }
    public void setStockStatus(String stockStatus) { this.stockStatus = stockStatus; }
    public LocalDateTime getPriceDate() { return priceDate; }
    public void setPriceDate(LocalDateTime priceDate) { this.priceDate = priceDate; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public String getSubmitterId() { return submitterId; }
    public void setSubmitterId(String submitterId) { this.submitterId = submitterId; }
    public Integer getVerifyStatus() { return verifyStatus; }
    public void setVerifyStatus(Integer verifyStatus) { this.verifyStatus = verifyStatus; }
    public Integer getSource() { return source; }
    public void setSource(Integer source) { this.source = source; }
}
