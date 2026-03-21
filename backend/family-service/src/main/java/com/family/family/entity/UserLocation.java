package com.family.family.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户定位实体
 */
@Data
@TableName("user_location")
public class UserLocation {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String location;

    private Double latitude;

    private Double longitude;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
