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
     * 农历年份(干支)
     */
    private String lunarYear;
    
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
     * 下一个节气
     */
    private String nextSolarTerm;
    
    /**
     * 距离下一个节气天数
     */
    private Integer daysToNextSolarTerm;
    
    /**
     * 节日列表
     */
    private List<String> festivals;
    
    /**
     * 宜
     */
    private List<String> suitable;
    
    /**
     * 忌
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
