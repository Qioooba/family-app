package com.family.calendar.dto.response;

import lombok.Data;

import java.util.List;

/**
 * 农历信息响应DTO
 */
@Data
public class LunarResponse {
    
    /**
     * 公历日期
     */
    private String solarDate;
    
    /**
     * 农历日期
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
     * 日
     */
    private Integer day;
    
    /**
     * 农历年份(干支)
     */
    private String ganZhiYear;
    
    /**
     * 农历月份(干支)
     */
    private String ganZhiMonth;
    
    /**
     * 农历日(干支)
     */
    private String ganZhiDay;
    
    /**
     * 农历月份
     */
    private String lunarMonth;
    
    /**
     * 农历日
     */
    private String lunarDay;
    
    /**
     * 生肖
     */
    private String zodiac;
    
    /**
     * 星期
     */
    private String weekDay;
    
    /**
     * 是否闰月
     */
    private Boolean isLeapMonth;
    
    /**
     * 节气
     */
    private String solarTerm;
    
    /**
     * 节日
     */
    private String festival;
    
    /**
     * 宜
     */
    private List<String> yi;
    
    /**
     * 忌
     */
    private List<String> ji;
    
    /**
     * 节日列表
     */
    private List<String> festivals;
    
    /**
     * 宜（兼容旧字段）
     */
    private List<String> suitable;
    
    /**
     * 忌（兼容旧字段）
     */
    private List<String> avoid;
    
    /**
     * 时辰吉凶
     */
    private List<HourLuck> hourLuck;
    
    /**
     * 时辰吉凶
     */
    @Data
    public static class HourLuck {
        private String hour; // 子时、丑时等
        private String timeRange; // 23:00-01:00
        private String luck; // 吉、凶、平
    }
}
