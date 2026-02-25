package com.family.calendar.dto.response;

import lombok.Data;

import java.util.List;

/**
 * 节日信息响应DTO
 */
@Data
public class FestivalResponse {
    
    /**
     * 年份
     */
    private Integer year;
    
    /**
     * 月份
     */
    private Integer month;
    
    /**
     * 节日列表
     */
    private List<FestivalInfo> festivals;
    
    /**
     * 节日信息
     */
    @Data
    public static class FestivalInfo {
        private String name;
        private String date;
        private String type; // traditional-传统节日, public-公共假日, international-国际节日
        private Integer daysUntil; // 距离今天还有几天
        private Boolean isHoliday;
        private Integer holidayDays;
    }
}
