package com.family.message.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.family.message.dto.MessageListDTO;
import com.family.message.dto.MessageSendDTO;
import com.family.family.entity.Message;
import com.family.message.vo.MessageVO;
import com.family.common.core.PageResult;

import java.util.Map;

/**
 * 消息服务接口
 */
public interface MessageService extends IService<Message> {
    
    /**
     * 发送消息
     */
    MessageVO sendMessage(MessageSendDTO dto);
    
    /**
     * 使用模板发送消息
     */
    MessageVO sendMessageWithTemplate(String templateCode, Long receiverId, Map<String, Object> params);
    
    /**
     * 获取消息列表
     */
    PageResult<MessageVO> getMessageList(MessageListDTO dto);
    
    /**
     * 标记消息已读
     */
    void markAsRead(Long messageId);
    
    /**
     * 批量标记已读
     */
    void markAsReadBatch(Long[] messageIds);
    
    /**
     * 标记所有消息已读
     */
    void markAllAsRead();
    
    /**
     * 获取未读消息数
     */
    Long getUnreadCount();
    
    /**
     * 获取指定类型的未读消息数
     */
    Long getUnreadCountByType(Integer type);
    
    /**
     * 获取未读消息统计(按类型)
     */
    Map<String, Long> getUnreadStats();
    
    /**
     * 删除消息
     */
    void deleteMessage(Long messageId);
    
    /**
     * 获取消息详情
     */
    MessageVO getMessageDetail(Long messageId);
}
