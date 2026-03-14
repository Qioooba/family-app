package com.family.family.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;

/**
 * 节假日配置
 */
@Data
@TableName("holiday_config")
public class HolidayConfig {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private LocalDate holidayDate;
    private String holidayName;
    private String holidayType;  // HOLIDAY-节假日 WORKDAY-调休工作日
    private Integer year;
}
