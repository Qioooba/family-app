package com.family.food.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.family.food.entity.PriceAlert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PriceAlertMapper extends BaseMapper<PriceAlert> {
    
    @Select("SELECT * FROM price_alert WHERE user_id = #{userId} AND status = 1")
    List<PriceAlert> selectActiveAlertsByUserId(@Param("userId") Long userId);
}
