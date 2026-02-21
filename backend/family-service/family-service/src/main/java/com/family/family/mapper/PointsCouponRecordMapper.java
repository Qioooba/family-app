package com.family.family.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.family.family.entity.PointsCouponRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 积分兑换记录Mapper
 */
@Mapper
public interface PointsCouponRecordMapper extends BaseMapper<PointsCouponRecord> {
    
    /**
     * 查询用户的兑换记录
     */
    @Select("SELECT * FROM points_coupon_record WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<PointsCouponRecord> selectByUserId(@Param("userId") Long userId);
}