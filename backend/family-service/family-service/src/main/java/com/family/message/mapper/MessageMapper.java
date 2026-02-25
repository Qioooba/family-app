package com.family.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.family.family.entity.Message;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 消息Mapper
 */
public interface MessageMapper extends BaseMapper<Message> {
    
    /**
     * 获取用户的未读消息数量
     */
    @Select("SELECT COUNT(*) FROM message WHERE (receiver_id = #{userId} OR receiver_id = 0) " +
            "AND id NOT IN (SELECT message_id FROM message_read_status WHERE user_id = #{userId}) " +
            "AND (expire_time IS NULL OR expire_time > NOW())")
    Long selectUnreadCount(@Param("userId") Long userId);
    
    /**
     * 获取用户指定类型的未读消息数量
     */
    @Select("SELECT COUNT(*) FROM message WHERE type = #{type} AND (receiver_id = #{userId} OR receiver_id = 0) " +
            "AND id NOT IN (SELECT message_id FROM message_read_status WHERE user_id = #{userId}) " +
            "AND (expire_time IS NULL OR expire_time > NOW())")
    Long selectUnreadCountByType(@Param("userId") Long userId, @Param("type") Integer type);
}
