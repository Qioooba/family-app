package com.family.health.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.family.family.entity.WaterRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 喝水记录Mapper
 */
@Mapper
public interface WaterRecordMapper extends BaseMapper<WaterRecord> {
    
    /**
     * 查询用户某日的喝水总量
     */
    @Select("SELECT COALESCE(SUM(amount), 0) FROM water_record WHERE user_id = #{userId} AND record_date = #{date}")
    Integer selectTotalAmountByDate(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    /**
     * 查询用户某日的喝水记录
     * 使用别名确保字段正确映射到 Java 实体类属性（下划线转驼峰）
     */
    @Select("SELECT " +
            "  id, " +
            "  user_id as userId, " +
            "  amount, " +
            "  record_date as recordDate, " +
            "  record_time as recordTime, " +
            "  create_time as createTime " +
            "FROM water_record " +
            "WHERE user_id = #{userId} AND record_date = #{date} " +
            "ORDER BY record_time DESC")
    List<WaterRecord> selectByDate(@Param("userId") Long userId, @Param("date") LocalDate date);
}
