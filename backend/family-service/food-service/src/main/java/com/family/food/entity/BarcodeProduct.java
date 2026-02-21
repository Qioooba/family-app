package com.family.food.entity;

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
    
    /**
     * 条形码
     */
    private String barcode;
    
    /**
     * 商品名称
     */
    private String productName;
    
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
     * 保质期(天)
     */
    private Integer shelfLifeDays;
    
    /**
     * 商品图片URL
     */
    private String imageUrl;
    
    /**
     * 数据来源: local-本地库, api-第三方API
     */
    private String source;
    
    /**
     * 查询次数
     */
    private Integer queryCount;
}
