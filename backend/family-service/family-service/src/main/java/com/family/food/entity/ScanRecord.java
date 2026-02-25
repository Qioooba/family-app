package com.family.family.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.family.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 扫码识别记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("scan_record")
public class ScanRecord extends BaseEntity {
    private static final long serialVersionUID = 1L;
    
    private Long userId;
    private Long familyId;
    private String scanType;
    private String scanContent;
    private String resultJson;
    private Integer status;
    private String failReason;
    private String productName;
    private Boolean addedToInventory;
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public Long getFamilyId() {
        return familyId;
    }
    
    public void setFamilyId(Long familyId) {
        this.familyId = familyId;
    }
    
    public String getScanType() {
        return scanType;
    }
    
    public void setScanType(String scanType) {
        this.scanType = scanType;
    }
    
    public String getScanContent() {
        return scanContent;
    }
    
    public void setScanContent(String scanContent) {
        this.scanContent = scanContent;
    }
    
    public String getResultJson() {
        return resultJson;
    }
    
    public void setResultJson(String resultJson) {
        this.resultJson = resultJson;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
    
    public String getFailReason() {
        return failReason;
    }
    
    public void setFailReason(String failReason) {
        this.failReason = failReason;
    }
    
    public String getProductName() {
        return productName;
    }
    
    public void setProductName(String productName) {
        this.productName = productName;
    }
    
    public Boolean getAddedToInventory() {
        return addedToInventory;
    }
    
    public void setAddedToInventory(Boolean addedToInventory) {
        this.addedToInventory = addedToInventory;
    }
}
