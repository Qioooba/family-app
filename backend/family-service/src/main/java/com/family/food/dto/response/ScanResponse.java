package com.family.food.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 扫码识别响应DTO
 */
@Data
public class ScanResponse {
    
    /**
     * 识别状态: success-成功, fail-失败
     */
    private String status;
    
    /**
     * 商品名称
     */
    private String productName;
    
    /**
     * 条形码
     */
    private String barcode;
    
    /**
     * 品牌
     */
    private String brand;
    
    /**
     * 规格
     */
    private String specification;
    
    /**
     * 类别
     */
    private String category;
    
    /**
     * 参考价格
     */
    private BigDecimal referencePrice;
    
    /**
     * 生产日期
     */
    private LocalDate productionDate;
    
    /**
     * 保质期(天)
     */
    private Integer shelfLifeDays;
    
    /**
     * 过期日期
     */
    private LocalDate expireDate;
    
    /**
     * 识别结果原始数据(JSON)
     */
    private String rawData;
    
    /**
     * 提示消息
     */
    private String message;
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getProductName() {
        return productName;
    }
    
    public void setProductName(String productName) {
        this.productName = productName;
    }
    
    public String getBarcode() {
        return barcode;
    }
    
    public void setBarcode(String barcode) {
        this.barcode = barcode;
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
    
    public LocalDate getProductionDate() {
        return productionDate;
    }
    
    public void setProductionDate(LocalDate productionDate) {
        this.productionDate = productionDate;
    }
    
    public Integer getShelfLifeDays() {
        return shelfLifeDays;
    }
    
    public void setShelfLifeDays(Integer shelfLifeDays) {
        this.shelfLifeDays = shelfLifeDays;
    }
    
    public LocalDate getExpireDate() {
        return expireDate;
    }
    
    public void setExpireDate(LocalDate expireDate) {
        this.expireDate = expireDate;
    }
    
    public String getRawData() {
        return rawData;
    }
    
    public void setRawData(String rawData) {
        this.rawData = rawData;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}
