package com.family.family.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.family.family.entity.HolidayConfig;
import com.family.family.mapper.HolidayConfigMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 节假日服务
 */
@Slf4j
@Service
public class HolidayService extends ServiceImpl<HolidayConfigMapper, HolidayConfig> {

    // 中国法定节假日日期（月-日），用于自动判断
    // 注意：春节日期每年不同，这里列出大概范围的正月初一
    private static final Set<String> KNOWN_HOLIDAYS = new HashSet<>(Arrays.asList(
        // 元旦 (1月1日)
        "01-01",
        // 春节 (正月初一，大概在1月21日到2月20日之间)
        "01-28", "01-29", "01-30", "01-31", "02-01", "02-02", "02-03", "02-04", // 2026年春节
        // 清明节 (4月4日或4月5日)
        "04-04", "04-05", "04-06",
        // 劳动节 (5月1日)
        "05-01", "05-02", "05-03",
        // 端午节 (五月初五，大概在5月28日到6月20日之间)
        "05-31", "06-01", "06-02", // 2026年端午
        // 中秋节 (八月十五，大概在9月15日到10月5日之间)
        "09-25", "09-26", "09-27", // 2026年中秋
        // 国庆节 (10月1日)
        "10-01", "10-02", "10-03", "10-04", "10-05", "10-06", "10-07"
    ));

    /**
     * 判断是否是工作日
     */
    public boolean isWorkDay(LocalDate date) {
        // 1. 先查数据库中的节假日配置
        LambdaQueryWrapper<HolidayConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HolidayConfig::getHolidayDate, date);

        HolidayConfig config = this.getOne(wrapper);

        if (config != null) {
            // 数据库中有记录
            if ("WORKDAY".equals(config.getHolidayType())) {
                return true;  // 调休工作日
            } else {
                return false; // 节假日
            }
        }

        // 2. 数据库没有，检查是否是已知的节假日
        String monthDay = String.format("%02d-%02d", date.getMonthValue(), date.getDayOfMonth());
        if (KNOWN_HOLIDAYS.contains(monthDay)) {
            // 已知节假日，不是工作日
            return false;
        }

        // 3. 按正常周末判断
        int dayOfWeek = date.getDayOfWeek().getValue();  // 1=周一, 7=周日
        return dayOfWeek <= 5;  // 周一到周五是工作日
    }

    /**
     * 判断是否是节假日
     */
    public boolean isHoliday(LocalDate date) {
        return !isWorkDay(date);
    }
}
