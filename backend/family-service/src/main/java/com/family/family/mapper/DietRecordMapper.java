package com.family.family.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.family.family.entity.DietRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;

/**
 * 饮食记录Mapper
 */
@Mapper
public interface DietRecordMapper extends BaseMapper<DietRecord> {
    
    /**
     * 统计某日热量
     */
    @Select("SELECT SUM(calories) FROM diet_record WHERE user_id = #{userId} AND record_date = #{date}")
    Integer sumCaloriesByUserAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    /**
     * 统计日期范围内热量
     */
    @Select("SELECT SUM(calories) FROM diet_record WHERE user_id = #{userId} AND record_date BETWEEN #{startDate} AND #{endDate}")
    Integer sumCaloriesByUserAndDateRange(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    /**
     * 统计日期范围内记录天数
     */
    @Select("SELECT COUNT(DISTINCT record_date) FROM diet_record WHERE user_id = #{userId} AND record_date BETWEEN #{startDate} AND #{endDate}")
    Long countDistinctDatesByUserAndRange(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
