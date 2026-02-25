package com.family.food.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.family.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 价格提醒
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("price_alert")
public class PriceAlert extends BaseEntity {
    private static final long serialVersionUID = 1L;
    
    private Long userId;
    private String barcode;
    private String productName;
    private BigDecimal targetPrice;
    private String alertType;
    private Integer status;
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public BigDecimal getTargetPrice() { return targetPrice; }
    public void setTargetPrice(BigDecimal targetPrice) { this.targetPrice = targetPrice; }
    public String getAlertType() { return alertType; }
    public void setAlertType(String alertType) { this.alertType = alertType; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
