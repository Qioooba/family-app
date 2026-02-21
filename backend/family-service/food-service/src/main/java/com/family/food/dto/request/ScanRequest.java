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
}
