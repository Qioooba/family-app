package com.family.common.core;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 基础实体类
 */
@Data
public class BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer status;
}
