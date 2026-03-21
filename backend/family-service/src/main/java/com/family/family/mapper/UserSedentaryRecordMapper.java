package com.family.family.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.family.family.entity.UserSedentaryRecord;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;

/**
 * 用户久坐记录 Mapper
 */
@Mapper
public interface UserSedentaryRecordMapper extends BaseMapper<UserSedentaryRecord> {

    /**
     * 获取或创建今日记录
     */
    @Select("SELECT * FROM user_sedentary_record WHERE user_id = #{userId} AND record_date = #{date}")
    UserSedentaryRecord getTodayRecord(@Param("userId") Long userId, @Param("date") LocalDate date);

    /**
     * 插入新记录
     */
    @Insert("INSERT INTO user_sedentary_record (user_id, record_date, remind_count) VALUES (#{userId}, #{date}, 0)")
    int insertRecord(@Param("userId") Long userId, @Param("date") LocalDate date);

    /**
     * 更新最后久坐时间和提醒次数
     */
    @Update("UPDATE user_sedentary_record SET last_sit_time = #{lastSitTime}, remind_count = remind_count + 1 WHERE id = #{id}")
    int updateSitTime(@Param("id") Long id, @Param("lastSitTime") java.time.LocalDateTime lastSitTime);
}
