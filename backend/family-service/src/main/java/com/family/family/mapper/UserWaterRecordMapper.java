package com.family.family.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.family.family.entity.UserWaterRecord;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;

/**
 * 用户喝水记录 Mapper
 */
@Mapper
public interface UserWaterRecordMapper extends BaseMapper<UserWaterRecord> {

    /**
     * 获取今日喝水记录
     */
    @Select("SELECT * FROM user_water_record WHERE user_id = #{userId} AND record_date = #{date}")
    UserWaterRecord getTodayRecord(@Param("userId") Long userId, @Param("date") LocalDate date);

    /**
     * 插入新记录
     */
    @Insert("INSERT INTO user_water_record (user_id, record_date, cup_count, last_drink_time) VALUES (#{userId}, #{date}, 0, NOW())")
    int insertRecord(@Param("userId") Long userId, @Param("date") LocalDate date);

    /**
     * 增加喝水量
     */
    @Update("UPDATE user_water_record SET cup_count = cup_count + 1, last_drink_time = NOW() WHERE id = #{id}")
    int incrementCupCount(@Param("id") Long id);
}
