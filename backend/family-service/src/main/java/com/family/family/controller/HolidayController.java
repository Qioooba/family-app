package com.family.family.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.family.common.core.Result;
import com.family.family.entity.HolidayConfig;
import com.family.family.mapper.HolidayConfigMapper;
import com.family.family.service.HolidayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 节假日控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/calendar")
public class HolidayController {

    @Autowired
    private HolidayService holidayService;

    @Autowired
    private HolidayConfigMapper holidayConfigMapper;

    private final RestTemplate restTemplate = new RestTemplate();

    // 节假日API - 使用 apihubs.cn (免费，无需API Key)
    private static final String HOLIDAY_API = "https://api.apihubs.cn/holiday/get";

    /**
     * 获取指定年份的节假日信息
     * GET /api/calendar/holidays?year=2024
     */
    @GetMapping("/holidays")
    public Result<List<HolidayConfig>> getHolidays(@RequestParam(required = false) Integer year) {
        if (year == null) {
            year = LocalDate.now().getYear();
        }

        List<HolidayConfig> holidays = holidayConfigMapper.selectList(
            new LambdaQueryWrapper<HolidayConfig>()
                .eq(HolidayConfig::getYear, year)
                .orderByAsc(HolidayConfig::getHolidayDate)
        );

        return Result.success(holidays);
    }

    /**
     * 判断指定日期是否是工作日
     * GET /api/calendar/is-workday?date=2024-01-01
     */
    @GetMapping("/is-workday")
    public Result<Map<String, Object>> isWorkday(@RequestParam String date) {
        LocalDate localDate = LocalDate.parse(date);
        boolean isWorkDay = holidayService.isWorkDay(localDate);

        Map<String, Object> result = new HashMap<>();
        result.put("date", date);
        result.put("isWorkDay", isWorkDay);
        result.put("isHoliday", !isWorkDay);

        return Result.success(result);
    }

    /**
     * 从API同步指定年份的节假日数据
     * POST /api/calendar/sync?year=2024
     */
    @PostMapping("/sync")
    public Result<Map<String, Object>> syncHolidays(@RequestParam Integer year) {
        try {
            log.info("开始同步 {} 年的节假日数据", year);

            int holidayCount = 0;
            int workdayCount = 0;
            int weekendCount = 0;
            int page = 1;
            int pageSize = 31;
            int total = 365;

            // 分页获取所有数据
            while (true) {
                String url = HOLIDAY_API + "?year=" + year + "&page=" + page;
                log.info("请求URL: {}", url);
                Map<String, Object> response = restTemplate.getForObject(url, Map.class);
                log.info("API响应: code={}, data={}", response != null ? response.get("code") : "null", response != null && response.get("data") != null ? "exists" : "null");

                if (response == null || !"0".equals(String.valueOf(response.get("code")))) {
                    log.warn("获取节假日数据失败或已达上限: {}, 尝试处理已有数据", response);
                    // 即使失败也尝试处理已获取的数据
                    break;
                }

                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) response.get("data");
                if (data == null) {
                    break;
                }

                @SuppressWarnings("unchecked")
                List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("list");
                if (list == null || list.isEmpty()) {
                    log.info("没有更多数据");
                    break;
                }

                for (Map<String, Object> dayInfo : list) {
                // 解析日期 (格式: 20241231 -> LocalDate)
                Integer dateInt = (Integer) dayInfo.get("date");
                if (dateInt == null) {
                    continue;
                }
                LocalDate holidayDate = LocalDate.parse(dateInt.toString(), DateTimeFormatter.ofPattern("yyyyMMdd"));

                // week: 1=周一, 7=周日
                Integer week = (Integer) dayInfo.get("week");
                // workday: 1=工作日, 2=非工作日
                Integer workday = (Integer) dayInfo.get("workday");
                // weekend: 1=周末, 2=非周末
                Integer weekend = (Integer) dayInfo.get("weekend");
                // holiday_today: 1=节假日, 2=非节假日
                Integer holidayToday = (Integer) dayInfo.get("holiday_today");

                // 判断是否是节假日
                boolean isHoliday = (weekend != null && weekend == 1) ||
                                   (holidayToday != null && holidayToday == 1);
                // 判断是否是调休工作日 (是周末但却是工作日)
                boolean isCompensatory = (weekend != null && weekend == 1) &&
                                        (workday != null && workday == 1);

                // 判断是否已存在
                HolidayConfig existing = holidayConfigMapper.selectOne(
                    new LambdaQueryWrapper<HolidayConfig>()
                        .eq(HolidayConfig::getHolidayDate, holidayDate)
                );

                String holidayType;
                String holidayName;

                if (isCompensatory) {
                    // 调休工作日 (周末需要上班)
                    holidayType = "WORKDAY";
                    holidayName = "调休工作日";
                    workdayCount++;
                } else if (isHoliday) {
                    // 法定节假日
                    holidayType = "HOLIDAY";
                    holidayName = getHolidayName(dateInt, holidayDate);
                    holidayCount++;
                } else if (week != null && week <= 5) {
                    // 正常工作日 (周一到周五)
                    holidayType = "WORKDAY";
                    holidayName = "工作日";
                    workdayCount++;
                } else {
                    // 正常周末 (周六周日)
                    holidayType = "HOLIDAY";
                    holidayName = "周末";
                    weekendCount++;
                    continue; // 周末不需要存入数据库
                }

                if (existing != null) {
                    // 更新
                    existing.setHolidayName(holidayName);
                    existing.setHolidayType(holidayType);
                    existing.setYear(year);
                    holidayConfigMapper.updateById(existing);
                } else {
                    // 新增
                    HolidayConfig config = new HolidayConfig();
                    config.setHolidayDate(holidayDate);
                    config.setHolidayName(holidayName);
                    config.setHolidayType(holidayType);
                    config.setYear(year);
                    holidayConfigMapper.insert(config);
                }
            }

            // 检查是否还有更多数据
            Integer currentTotal = (Integer) data.get("total");
            Integer currentSize = (Integer) data.get("size");
            log.info("处理第 {} 页: list.size()={}, currentSize={}, total={}", page, list.size(), currentSize, currentTotal);
            if (currentTotal != null && currentSize != null && list.size() >= currentSize) {
                page++;
                log.info("继续获取下一页, page={}, 等待2000ms", page);
                try {
                    Thread.sleep(2000); // 避免请求过于频繁
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                continue; // 获取下一页
            }
            break; // 没有更多数据
        }

            log.info("同步节假日数据完成: {} 年, 节假日 {} 天, 工作日 {} 天, 周末 {} 天", year, holidayCount, workdayCount, weekendCount);

            Map<String, Object> result = new HashMap<>();
            result.put("year", year);
            result.put("holidayCount", holidayCount);
            result.put("workdayCount", workdayCount);
            result.put("weekendCount", weekendCount);

            return Result.success("同步成功", result);

        } catch (Exception e) {
            log.error("同步节假日数据失败", e);
            return Result.error("同步失败: " + e.getMessage());
        }
    }

    /**
     * 根据日期判断节假日名称
     */
    private String getHolidayName(Integer dateInt, LocalDate date) {
        int month = date.getMonthValue();
        int day = date.getDayOfMonth();

        // 中国法定节假日
        Map<Integer, String> holidays = new HashMap<>();
        // 元旦
        holidays.put(101, "元旦");
        // 春节 (大致日期，年年不同，需要精确数据)
        // 清明节
        holidays.put(404, "清明节");
        // 劳动节
        holidays.put(501, "劳动节");
        // 端午节
        holidays.put(605, "端午节");
        // 中秋节
        holidays.put(815, "中秋节");
        // 国庆节
        holidays.put(1001, "国庆节");

        // 简单判断（更精确的判断需要API提供）
        int key = month * 100 + day;
        if (holidays.containsKey(key)) {
            return holidays.get(key);
        }

        // 春节期间的大致判断 (正月初一到初七)
        if (month == 2 && day >= 1 && day <= 15) {
            return "春节";
        }

        return "法定节假日";
    }

    /**
     * 同步当年及次年的节假日数据
     * POST /api/calendar/sync-current
     */
    @PostMapping("/sync-current")
    public Result<Map<String, Object>> syncCurrentYear() {
        int currentYear = LocalDate.now().getYear();
        int nextYear = currentYear + 1;

        Result<Map<String, Object>> currentResult = syncHolidays(currentYear);
        Result<Map<String, Object>> nextResult = syncHolidays(nextYear);

        Map<String, Object> result = new HashMap<>();
        result.put("currentYear", currentYear);
        result.put("nextYear", nextYear);
        result.put("currentYearSync", currentResult.getData());
        result.put("nextYearSync", nextResult.getData());

        return Result.success(result);
    }
}
