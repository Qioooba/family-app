package com.family.common.util;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class LunarCalendarUtil {
    
    public static Map<String, Object> getLunarCalendar(int year, int month, int day) {
        Map<String, Object> result = new HashMap<>();
        result.put("lunarYear", year);
        result.put("lunarMonth", month);
        result.put("lunarDay", day);
        result.put("lunarMonthName", "正月");
        result.put("lunarDayName", "初一");
        return result;
    }
    
    public static String getFestival(int month, int day) {
        return null;
    }
}
