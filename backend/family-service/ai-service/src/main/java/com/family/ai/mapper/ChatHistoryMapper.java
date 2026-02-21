package com.family.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.family.ai.entity.ChatHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 对话历史Mapper
 */
@Mapper
public interface ChatHistoryMapper extends BaseMapper<ChatHistory> {
    
    /**
     * 获取会话历史
     */
    @Select("SELECT * FROM ai_chat_history WHERE session_id = #{sessionId} ORDER BY create_time ASC")
    List<ChatHistory> selectBySessionId(@Param("sessionId") String sessionId);
    
    /**
     * 获取用户最近的历史
     */
    @Select("SELECT * FROM ai_chat_history WHERE user_id = #{userId} ORDER BY create_time DESC LIMIT #{limit}")
    List<ChatHistory> selectRecentByUserId(@Param("userId") Long userId, @Param("limit") Integer limit);
}
