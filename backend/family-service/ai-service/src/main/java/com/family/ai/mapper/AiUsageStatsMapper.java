package com.family.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.family.ai.entity.AiUsageStats;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * AI使用统计Mapper
 */
@Mapper
public interface AiUsageStatsMapper extends BaseMapper<AiUsageStats> {
    
    /**
     * 获取用户某日的使用统计
     */
    @Select("SELECT id, user_id, date, chat_count, voice_count, token_used FROM ai_usage_stats WHERE user_id = #{userId} AND date = #{date}")
    AiUsageStats selectByUserAndDate(@Param("userId") Long userId, @Param("date") java.time.LocalDate date);
}
