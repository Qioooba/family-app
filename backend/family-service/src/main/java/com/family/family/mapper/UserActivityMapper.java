package com.family.family.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.family.family.entity.UserActivity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;

/**
 * 用户活跃度Mapper
 */
@Mapper
public interface UserActivityMapper extends BaseMapper<UserActivity> {
    
    /**
     * 获取用户某日的活跃度
     */
    @Select("SELECT id, user_id, date, login_count, task_completed, diet_recorded, active_minutes FROM user_activity WHERE user_id = #{userId} AND date = #{date}")
    UserActivity selectByUserAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
}
