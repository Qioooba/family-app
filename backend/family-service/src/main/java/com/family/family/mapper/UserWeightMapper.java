package com.family.family.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.family.family.entity.UserWeight;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserWeightMapper extends BaseMapper<UserWeight> {
    
    @Select("SELECT id, user_id, weight, record_date, note, create_time FROM user_weight WHERE user_id = #{userId} ORDER BY record_date DESC")
    List<UserWeight> selectByUserId(@Param("userId") Long userId);
}
