package com.family.family.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.family.family.entity.WaterRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface WaterRecordMapper extends BaseMapper<WaterRecord> {
    
    /**
     * 查询用户指定日期的喝水记录
     */
    @Select("SELECT * FROM water_record WHERE user_id = #{userId} AND record_date = #{recordDate} ORDER BY record_time DESC")
    List<WaterRecord> selectByUserIdAndDate(@Param("userId") Long userId, @Param("recordDate") LocalDate recordDate);
    
    /**
     * 查询用户指定日期的喝水总量
     */
    @Select("SELECT COALESCE(SUM(amount), 0) FROM water_record WHERE user_id = #{userId} AND record_date = #{recordDate}")
    Integer sumAmountByUserIdAndDate(@Param("userId") Long userId, @Param("recordDate") LocalDate recordDate);
}
