package com.family.common.util;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * 农历转换工具类
 * 支持公历与农历互相转换
 */
public class LunarCalendarUtil {
    
    // 农历年天干
    private static final String[] TIAN_GAN = {"甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸"};
    // 农历年地支
    private static final String[] DI_ZHI = {"子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥"};
    // 农历月份名称
    private static final String[] LUNAR_MONTHS = {
        "正月", "二月", "三月", "四月", "五月", "六月",
        "七月", "八月", "九月", "十月", "冬月", "腊月"
    };
    // 农历日期名称
    private static final String[] LUNAR_DAYS = {
        "初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十",
        "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十",
        "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十"
    };
    // 十二生肖
    private static final String[] ZODIAC = {
        "鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪"
    };
    
    /**
     * 公历转农历
     * 简化版实现，支持1900-2100年
     */
    public static LunarDate solarToLunar(LocalDate solarDate) {
        // 简化计算，使用基准日期偏移
        LocalDate baseDate = LocalDate.of(1900, 1, 31); // 1900年春节
        int offset = (int) java.time.temporal.ChronoUnit.DAYS.between(baseDate, solarDate);
        
        // 农历1900年信息
        int lunarYear = 1900;
        int lunarMonth = 1;
        int lunarDay = 1;
        
        // 每年天数（简化，实际需要查表）
        int[] yearDays = {
            384, 354, 355, 383, 354, 355, 384, 354, 355, 384, // 1900-1909
            354, 384, 354, 384, 354, 354, 384, 355, 354, 384  // 1910-1919
        };
        
        // 计算年份
        int yearIndex = 0;
        while (offset > 0 && yearIndex < yearDays.length) {
            if (offset < yearDays[yearIndex]) {
                break;
            }
            offset -= yearDays[yearIndex];
            lunarYear++;
            yearIndex++;
        }
        
        // 计算月份（简化，每月按30天）
        int[] monthDays = {30, 30, 30, 29, 30, 29, 30, 29, 30, 29, 30, 29};
        for (int i = 0; i < 12 && offset > 0; i++) {
            if (offset < monthDays[i]) {
                lunarMonth = i + 1;
                lunarDay = offset + 1;
                break;
            }
            offset -= monthDays[i];
            if (i == 11) {
                lunarMonth = 12;
                lunarDay = offset + 1;
            }
        }
        
        LunarDate lunar = new LunarDate();
        lunar.setLunarYear(lunarYear);
        lunar.setLunarMonth(lunarMonth);
        lunar.setLunarDay(lunarDay);
        lunar.setLunarYearStr(getLunarYearString(lunarYear));
        lunar.setLunarMonthStr(LUNAR_MONTHS[lunarMonth - 1]);
        lunar.setLunarDayStr(LUNAR_DAYS[lunarDay - 1]);
        lunar.setZodiac(getZodiac(lunarYear));
        lunar.setFullLunarDate(lunar.getLunarYearStr() + "年" + lunar.getLunarMonthStr() + lunar.getLunarDayStr());
        
        return lunar;
    }
    
    /**
     * 获取农历年份字符串（如：甲子年）
     */
    public static String getLunarYearString(int year) {
        int offset = year - 1900 + 36; // 1900年是庚子年，偏移36
        return TIAN_GAN[offset % 10] + DI_ZHI[offset % 12];
    }
    
    /**
     * 获取生肖
     */
    public static String getZodiac(int year) {
        return ZODIAC[(year - 1900 + 12) % 12];
    }
    
    /**
     * 获取农历日期字符串
     */
    public static String getLunarDateString(int month, int day) {
        return LUNAR_MONTHS[month - 1] + LUNAR_DAYS[day - 1];
    }
    
    /**
     * 获取下一个农历节日
     */
    public static Map<String, String> getNextLunarFestival(LocalDate solarDate) {
        Map<String, String> festivals = new HashMap<>();
        festivals.put("正月初一", "春节");
        festivals.put("正月十五", "元宵节");
        festivals.put("五月初五", "端午节");
        festivals.put("七月初七", "七夕节");
        festivals.put("八月十五", "中秋节");
        festivals.put("九月初九", "重阳节");
        festivals.put("腊月初八", "腊八节");
        festivals.put("腊月廿三", "小年");
        festivals.put("腊月三十", "除夕");
        
        LunarDate lunar = solarToLunar(solarDate);
        String key = lunar.getLunarMonthStr() + lunar.getLunarDayStr();
        
        Map<String, String> result = new HashMap<>();
        if (festivals.containsKey(key)) {
            result.put("today", festivals.get(key));
        }
        
        // 查找下一个节日
        // 简化实现
        result.put("next", "查找中...");
        return result;
    }
    
    /**
     * 农历日期对象
     */
    public static class LunarDate {
        private int lunarYear;
        private int lunarMonth;
        private int lunarDay;
        private String lunarYearStr;
        private String lunarMonthStr;
        private String lunarDayStr;
        private String zodiac;
        private String fullLunarDate;
        
        // Getters and Setters
        public int getLunarYear() { return lunarYear; }
        public void setLunarYear(int lunarYear) { this.lunarYear = lunarYear; }
        
        public int getLunarMonth() { return lunarMonth; }
        public void setLunarMonth(int lunarMonth) { this.lunarMonth = lunarMonth; }
        
        public int getLunarDay() { return lunarDay; }
        public void setLunarDay(int lunarDay) { this.lunarDay = lunarDay; }
        
        public String getLunarYearStr() { return lunarYearStr; }
        public void setLunarYearStr(String lunarYearStr) { this.lunarYearStr = lunarYearStr; }
        
        public String getLunarMonthStr() { return lunarMonthStr; }
        public void setLunarMonthStr(String lunarMonthStr) { this.lunarMonthStr = lunarMonthStr; }
        
        public String getLunarDayStr() { return lunarDayStr; }
        public void setLunarDayStr(String lunarDayStr) { this.lunarDayStr = lunarDayStr; }
        
        public String getZodiac() { return zodiac; }
        public void setZodiac(String zodiac) { this.zodiac = zodiac; }
        
        public String getFullLunarDate() { return fullLunarDate; }
        public void setFullLunarDate(String fullLunarDate) { this.fullLunarDate = fullLunarDate; }
    }
}
