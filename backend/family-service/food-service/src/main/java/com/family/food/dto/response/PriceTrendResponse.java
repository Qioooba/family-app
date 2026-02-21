package com.family.food.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 价格趋势响应DTO
 */
@Data
public class PriceTrendResponse {
    
    /**
     * 商品名称
     */
    private String productName;
    
    /**
     * 条形码
     */
    private String barcode;
    
    /**
     * 当前平均价格
     */
    private BigDecimal currentAvgPrice;
    
    /**
     * 最低价格
     */
    private BigDecimal minPrice;
    
    /**
     * 最高价格
     */
    private BigDecimal maxPrice;
    
    /**
     * 价格趋势: up-上涨, down-下降, stable-稳定
     */
    private String trend;
    
    /**
     * 趋势百分比
     */
    private BigDecimal trendPercent;
    
    /**
     * 价格历史数据点
     */
    private List<PriceHistoryPoint> historyPoints;
    
    /**
     * 价格历史数据点
     */
    @Data
    public static class PriceHistoryPoint {
        private String date;
        private BigDecimal avgPrice;
        private BigDecimal minPrice;
        private BigDecimal maxPrice;
    }
}
