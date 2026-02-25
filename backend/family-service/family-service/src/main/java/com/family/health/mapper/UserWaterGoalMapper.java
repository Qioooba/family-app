package com.family.health.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.family.family.entity.UserWeight;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户饮水目标Mapper
 */
@Mapper
public interface UserWaterGoalMapper extends BaseMapper<UserWaterGoal> {
    
    /**
     * 根据用户ID查询饮水目标
     */
    @Select("SELECT * FROM user_water_goal WHERE user_id = #{userId} LIMIT 1")
    UserWaterGoal selectByUserId(@Param("userId") Long userId);
}