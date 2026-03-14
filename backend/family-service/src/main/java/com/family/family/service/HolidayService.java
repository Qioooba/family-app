package com.family.family.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.family.family.entity.HolidayConfig;
import com.family.family.mapper.HolidayConfigMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * 节假日服务
 */
@Slf4j
@Service
public class HolidayService extends ServiceImpl<HolidayConfigMapper, HolidayConfig> {
    
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
        
        // 2. 数据库没有，按正常周末判断
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
