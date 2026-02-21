package com.family.family.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 价格历史实体
 */
@Data
@TableName("price_history")
public class PriceHistory {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String itemName;
    private String barcode;
    private BigDecimal price;
    private String storeName;
    private LocalDate purchaseDate;
    private Long familyId;
    private LocalDateTime createTime;
}