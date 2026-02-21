package com.family.calendar.dto.request;

import lombok.Data;

/**
 * 农历查询请求DTO
 */
@Data
public class LunarRequest {
    
    /**
     * 公历日期: yyyy-MM-dd
     */
    private String solarDate;
    
    /**
     * 农历日期: yyyy-MM-dd
     */
    private String lunarDate;
    
    /**
     * 年份
     */
    private Integer year;
    
    /**
     * 月份
     */
    private Integer month;
    
    /**
     * 查询类型: convert-转换, festival-节日, almanac-黄历
     */
    private String queryType;
}
