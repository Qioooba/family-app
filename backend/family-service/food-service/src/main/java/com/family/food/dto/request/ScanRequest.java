package com.family.food.dto.request;

import lombok.Data;

/**
 * 扫码识别请求DTO
 */
@Data
public class ScanRequest {
    
    /**
     * 图片Base64编码
     */
    private String imageBase64;
    
    /**
     * 扫码类型: barcode-条形码, qrcode-二维码, image-图片识别
     */
    private String scanType;
    
    /**
     * 家庭ID
     */
    private Long familyId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    public String getImageBase64() {
        return imageBase64;
    }
    
    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }
    
    public String getScanType() {
        return scanType;
    }
    
    public void setScanType(String scanType) {
        this.scanType = scanType;
    }
    
    public Long getFamilyId() {
        return familyId;
    }
    
    public void setFamilyId(Long familyId) {
        this.familyId = familyId;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
