package com.family.family.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.family.family.entity.UserWaterGoal;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserWaterGoalMapper extends BaseMapper<UserWaterGoal> {
    
    /**
     * 查询用户的喝水目标
     */
    @Select("SELECT * FROM user_water_goal WHERE user_id = #{userId} LIMIT 1")
    UserWaterGoal selectByUserId(@Param("userId") Long userId);
}
