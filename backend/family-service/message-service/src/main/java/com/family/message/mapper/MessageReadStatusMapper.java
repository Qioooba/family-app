package com.family.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.family.message.entity.MessageReadStatus;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 消息已读状态Mapper
 */
public interface MessageReadStatusMapper extends BaseMapper<MessageReadStatus> {
    
    /**
     * 查询用户的消息已读状态
     */
    @Select("SELECT * FROM message_read_status WHERE message_id = #{messageId} AND user_id = #{userId}")
    MessageReadStatus selectByMessageAndUser(@Param("messageId") Long messageId, @Param("userId") Long userId);
}
