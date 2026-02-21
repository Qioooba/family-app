package com.family.family.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 心愿实体
 */
@Data
@TableName("wish")
public class Wish {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long familyId;
    private Long userId;
    private String title;
    private String description;
    private String type;
    private String visibility;
    private Integer status;
    private Integer progress;
    private BigDecimal budget;
    private Long claimerId;
    private LocalDateTime targetDate;
    private LocalDateTime fulfillDate;
    private String imageUrl;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
