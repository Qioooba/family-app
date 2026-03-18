package com.family.family.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.family.family.entity.Schedule;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ScheduleMapper extends BaseMapper<Schedule> {
    
    @Select("SELECT * FROM family_schedule WHERE family_id = #{familyId} AND status = 1 ORDER BY day_of_week, user_id")
    List<Schedule> selectByFamilyId(@Param("familyId") Long familyId);
    
    @Select("SELECT * FROM family_schedule WHERE id = #{id}")
    Schedule selectById(@Param("id") Long id);
    
    @Insert("INSERT INTO family_schedule (family_id, user_id, creator_id, task_name, day_of_week, start_date, end_date, status) " +
            "VALUES (#{familyId}, #{userId}, #{creatorId}, #{taskName}, #{dayOfWeek}, #{startDate}, #{endDate}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Schedule schedule);
    
    @Update("UPDATE family_schedule SET user_id = #{userId}, task_name = #{taskName}, day_of_week = #{dayOfWeek}, " +
            "start_date = #{startDate}, end_date = #{endDate}, status = #{status}, update_time = NOW() WHERE id = #{id}")
    int updateById(Schedule schedule);
    
    @Delete("DELETE FROM family_schedule WHERE id = #{id}")
    int deleteById(@Param("id") Long id);
    
    @Select("SELECT * FROM family_schedule WHERE family_id = #{familyId} AND user_id = #{userId} AND status = 1 " +
            "AND (#{date} BETWEEN start_date AND end_date) ORDER BY day_of_week")
    List<Schedule> selectByFamilyAndUserAndDate(@Param("familyId") Long familyId, @Param("userId") Long userId, @Param("date") String date);
}
