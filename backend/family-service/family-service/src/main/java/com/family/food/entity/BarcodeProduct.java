package com.family.family.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.family.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 条码商品库 - 存储常用商品条码信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("barcode_product")
public class BarcodeProduct extends BaseEntity {
    private static final long serialVersionUID = 1L;
    
    private String barcode;
    private String productName;
    private String brand;
    private String specification;
    private String category;
    private BigDecimal referencePrice;
    private Integer shelfLifeDays;
    private String imageUrl;
    private String source;
    private Integer queryCount;
    
    public String getBarcode() {
        return barcode;
    }
    
    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }
    
    public String getProductName() {
        return productName;
    }
    
    public void setProductName(String productName) {
        this.productName = productName;
    }
    
    public String getBrand() {
        return brand;
    }
    
    public void setBrand(String brand) {
        this.brand = brand;
    }
    
    public String getSpecification() {
        return specification;
    }
    
    public void setSpecification(String specification) {
        this.specification = specification;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public BigDecimal getReferencePrice() {
        return referencePrice;
    }
    
    public void setReferencePrice(BigDecimal referencePrice) {
        this.referencePrice = referencePrice;
    }
    
    public Integer getShelfLifeDays() {
        return shelfLifeDays;
    }
    
    public void setShelfLifeDays(Integer shelfLifeDays) {
        this.shelfLifeDays = shelfLifeDays;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public String getSource() {
        return source;
    }
    
    public void setSource(String source) {
        this.source = source;
    }
    
    public Integer getQueryCount() {
        return queryCount;
    }
    
    public void setQueryCount(Integer queryCount) {
        this.queryCount = queryCount;
    }
}
