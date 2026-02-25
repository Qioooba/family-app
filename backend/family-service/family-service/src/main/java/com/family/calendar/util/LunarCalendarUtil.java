package com.family.calendar.util;

import java.time.LocalDate;
import java.util.Calendar;

/**
 * 农历转换工具类
 * 使用传统算法进行公历农历转换
 */
public class LunarCalendarUtil {
    
    // 农历1900-2100年的每月天数数据，每个元素表示一年，低12位表示12个月，闰月在高4位
    private static final long[] LUNAR_INFO = new long[] {
        0x04bd8, 0x04ae0, 0x0a570, 0x054d5, 0x0d260, 0x0d950, 0x16554, 0x056a0, 0x09ad0, 0x055d2,
        0x04ae0, 0x0a5b6, 0x0a4d0, 0x0d250, 0x1d255, 0x0b540, 0x0d6a0, 0x0ada2, 0x095b0, 0x14977,
        0x04970, 0x0a4b0, 0x0b4b5, 0x06a50, 0x06d40, 0x1ab54, 0x02b60, 0x09570, 0x052f2, 0x04970,
        0x06566, 0x0d4a0, 0x0ea50, 0x06e95, 0x05ad0, 0x02b60, 0x186e3, 0x092e0, 0x1c8d7, 0x0c950,
        0x0d4a0, 0x1d8a6, 0x0b550, 0x056a0, 0x1a5b4, 0x025d0, 0x092d0, 0x0d2b2, 0x0a950, 0x0b557,
        0x06ca0, 0x0b550, 0x15355, 0x04da0, 0x0a5d0, 0x14573, 0x052d0, 0x0a9a8, 0x0e950, 0x06aa0,
        0x0aea6, 0x0ab50, 0x04b60, 0x0aae4, 0x0a570, 0x05260, 0x0f263, 0x0d950, 0x05b57, 0x056a0,
        0x096d0, 0x04dd5, 0x04ad0, 0x0a4d0, 0x0d4d4, 0x0d250, 0x0d558, 0x0b540, 0x0b5a0, 0x195a6,
        0x095b0, 0x049b0, 0x0a974, 0x0a4b0, 0x0b27a, 0x06a50, 0x06d40, 0x0af46, 0x0ab60, 0x09570,
        0x04af5, 0x04970, 0x064b0, 0x074a3, 0x0ea50, 0x06b58, 0x055c0, 0x0ab60, 0x096d5, 0x092e0,
        0x0c960, 0x0d954, 0x0d4a0, 0x0da50, 0x07552, 0x056a0, 0x0abb7, 0x025d0, 0x092d0, 0x0cab5,
        0x0a950, 0x0b4a0, 0x0baa4, 0x0ad50, 0x055d9, 0x04ba0, 0x0a5b0, 0x15176, 0x052b0, 0x0a930,
        0x07954, 0x06aa0, 0x0ad50, 0x05b52, 0x04b60, 0x0a6e6, 0x0a4e0, 0x0d260, 0x0ea65, 0x0d530,
        0x05aa0, 0x076a3, 0x096d0, 0x04bd7, 0x04ad0, 0x0a4d0, 0x1d0b6, 0x0d250, 0x0d520, 0x0dd45,
        0x0b5a0, 0x056d0, 0x055b2, 0x049b0, 0x0a577, 0x0a4b0, 0x0aa50, 0x1b255, 0x06d20, 0x0ada0
    };
    
    private static final String[] TIAN_GAN = {"甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸"};
    private static final String[] DI_ZHI = {"子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥"};
    private static final String[] ZODIAC = {"鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪"};
    private static final String[] MONTH_NAMES = {"正", "二", "三", "四", "五", "六", "七", "八", "九", "十", "冬", "腊"};
    private static final String[] DAY_NAMES = {
        "初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十",
        "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十",
        "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十"
    };
    
    /**
     * 公历转农历
     * @param solarDate 公历日期
     * @return 农历日期字符串
     */
    public static String solarToLunar(LocalDate solarDate) {
        int year = solarDate.getYear();
        int month = solarDate.getMonthValue();
        int day = solarDate.getDayOfMonth();
        
        int offset = daysBetween(1900, 1, 31, year, month, day);
        int lunarYear = 1900;
        int daysInYear = 0;
        
        for (int i = 0; i < LUNAR_INFO.length && offset > 0; i++) {
            daysInYear = daysInLunarYear(lunarYear);
            offset -= daysInYear;
            lunarYear++;
        }
        
        if (offset < 0) {
            offset += daysInYear;
            lunarYear--;
        }
        
        int lunarMonth = 1;
        int lunarDay = 1;
        boolean isLeap = false;
        
        int leapMonth = leapMonth(lunarYear);
        int daysInMonth = 0;
        
        for (int i = 1; i <= 12 && offset > 0; i++) {
            daysInMonth = daysInLunarMonth(lunarYear, i);
            offset -= daysInMonth;
            lunarMonth = i;
        }
        
        if (offset < 0) {
            offset += daysInMonth;
        } else {
            lunarMonth++;
        }
        
        lunarDay = offset + 1;
        
        return MONTH_NAMES[lunarMonth - 1] + "月" + DAY_NAMES[lunarDay - 1];
    }
    
    /**
     * 农历转公历（简化版，返回当年对应的公历日期）
     * @param lunarMonth 农历月
     * @param lunarDay 农历日
     * @param year 公历年份
     * @return 公历日期
     */
    public static LocalDate lunarToSolar(int lunarMonth, int lunarDay, int year) {
        // 简化实现：从当年1月1日开始遍历找到对应农历日期
        LocalDate date = LocalDate.of(year, 1, 1);
        for (int i = 0; i < 400; i++) {
            String lunar = solarToLunar(date);
            String target = MONTH_NAMES[lunarMonth - 1] + "月" + DAY_NAMES[lunarDay - 1];
            if (lunar.equals(target)) {
                return date;
            }
            date = date.plusDays(1);
        }
        return null;
    }
    
    /**
     * 获取生肖
     * @param year 年份
     * @return 生肖
     */
    public static String getZodiac(int year) {
        return ZODIAC[(year - 4) % 12];
    }
    
    /**
     * 获取干支纪年
     * @param year 年份
     * @return 干支
     */
    public static String getGanZhi(int year) {
        return TIAN_GAN[(year - 4) % 10] + DI_ZHI[(year - 4) % 12];
    }
    
    private static int daysInLunarYear(int year) {
        int sum = 0;
        for (int i = 1; i <= 12; i++) {
            sum += daysInLunarMonth(year, i);
        }
        if (leapMonth(year) != 0) {
            sum += daysInLunarMonth(year, leapMonth(year));
        }
        return sum;
    }
    
    private static int daysInLunarMonth(int year, int month) {
        return ((LUNAR_INFO[year - 1900] & (0x10000 >> month)) != 0) ? 30 : 29;
    }
    
    private static int leapMonth(int year) {
        return (int) (LUNAR_INFO[year - 1900] & 0xf);
    }
    
    private static int daysBetween(int y1, int m1, int d1, int y2, int m2, int d2) {
        LocalDate date1 = LocalDate.of(y1, m1, d1);
        LocalDate date2 = LocalDate.of(y2, m2, d2);
        return (int) java.time.temporal.ChronoUnit.DAYS.between(date1, date2);
    }
}