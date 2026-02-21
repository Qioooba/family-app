package com.family.family.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.family.family.entity.StatsCache;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;

/**
 * 统计缓存Mapper
 */
@Mapper
public interface StatsCacheMapper extends BaseMapper<StatsCache> {
    
    /**
     * 获取缓存
     */
    @Select("SELECT * FROM stats_cache WHERE stat_type = #{statType} AND target_id = #{targetId} AND stat_date = #{statDate}")
    StatsCache selectByTypeTargetDate(@Param("statType") String statType, 
                                       @Param("targetId") Long targetId, 
                                       @Param("statDate") LocalDate statDate);
}
