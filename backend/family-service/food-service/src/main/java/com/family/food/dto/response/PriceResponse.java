package com.family.food.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 价格对比响应DTO
 */
@Data
public class PriceResponse {
    
    /**
     * 商店ID
     */
    private Long storeId;
    
    /**
     * 商店名称
     */
    private String storeName;
    
    /**
     * 商店地址
     */
    private String storeAddress;
    
    /**
     * 距离(米)
     */
    private Integer distance;
    
    /**
     * 商品价格
     */
    private BigDecimal price;
    
    /**
     * 促销价格
     */
    private BigDecimal promotionPrice;
    
    /**
     * 促销信息
     */
    private String promotionInfo;
    
    /**
     * 库存状态: in_stock-有货, out_of_stock-缺货, limited-限量
     */
    private String stockStatus;
    
    /**
     * 价格更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 是否是最低价
     */
    private Boolean isLowest;
    
    /**
     * 与当前价格差价
     */
    private BigDecimal priceDiff;
}
