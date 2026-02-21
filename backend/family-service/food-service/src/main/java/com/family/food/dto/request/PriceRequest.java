package com.family.food.dto.request;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 价格查询请求DTO
 */
@Data
public class PriceRequest {
    
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
     * 当前价格
     */
    private BigDecimal currentPrice;
    
    /**
     * 商店名称
     */
    private String storeName;
    
    /**
     * 查询半径(公里)
     */
    private Integer radiusKm;
    
    /**
     * 排序方式: price_asc-价格升序, price_desc-价格降序, distance-距离
     */
    private String sortBy;
}
