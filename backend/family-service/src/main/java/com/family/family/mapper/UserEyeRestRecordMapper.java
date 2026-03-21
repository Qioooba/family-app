package com.family.family.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.family.family.entity.UserEyeRestRecord;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;

/**
 * 用户护眼记录 Mapper
 */
@Mapper
public interface UserEyeRestRecordMapper extends BaseMapper<UserEyeRestRecord> {

    /**
     * 获取或创建今日记录
     */
    @Select("SELECT * FROM user_eye_rest_record WHERE user_id = #{userId} AND record_date = #{date}")
    UserEyeRestRecord getTodayRecord(@Param("userId") Long userId, @Param("date") LocalDate date);

    /**
     * 插入新记录
     */
    @Insert("INSERT INTO user_eye_rest_record (user_id, record_date, remind_count) VALUES (#{userId}, #{date}, 0)")
    int insertRecord(@Param("userId") Long userId, @Param("date") LocalDate date);

    /**
     * 更新最后看屏幕时间和提醒次数
     */
    @Update("UPDATE user_eye_rest_record SET last_screen_time = #{lastScreenTime}, remind_count = remind_count + 1 WHERE id = #{id}")
    int updateScreenTime(@Param("id") Long id, @Param("lastScreenTime") java.time.LocalDateTime lastScreenTime);
}
