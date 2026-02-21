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
}
