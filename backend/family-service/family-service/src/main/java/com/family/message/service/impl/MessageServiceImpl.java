package com.family.message.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.family.common.core.PageResult;
import com.family.common.core.exception.BusinessException;
import com.family.message.dto.MessageListDTO;
import com.family.message.dto.MessageSendDTO;
import com.family.family.entity.Message;
import com.family.family.entity.MessageReadStatus;
import com.family.family.entity.MessageTemplate;
import com.family.message.mapper.MessageMapper;
import com.family.message.mapper.MessageReadStatusMapper;
import com.family.message.mapper.MessageTemplateMapper;
import com.family.message.vo.MessageVO;
import com.family.message.websocket.MessageWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 消息服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {
    
    private final MessageMapper messageMapper;
    private final MessageReadStatusMapper readStatusMapper;
    private final MessageTemplateMapper templateMapper;
    private final MessageWebSocketHandler webSocketHandler;
    private final ObjectMapper objectMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public MessageVO sendMessage(MessageSendDTO dto) {
        Long currentUserId = StpUtil.getLoginIdDefaultNull() != null ? StpUtil.getLoginIdAsLong() : 0L;
        
        Message message = new Message();
        BeanUtil.copyProperties(dto, message);
        
        // 设置发送者信息
        message.setSenderId(currentUserId);
        message.setIsRead(0);
        
        if (dto.getPriority() == null) {
            message.setPriority(2); // 默认中优先级
        }
        
        // 处理模板参数
        if (dto.getExtraData() != null) {
            try {
                message.setExtraData(objectMapper.writeValueAsString(dto.getExtraData()));
            } catch (Exception e) {
                log.error("转换extraData失败", e);
            }
        }
        if (dto.getTemplateParams() != null) {
            try {
                message.setTemplateParams(objectMapper.writeValueAsString(dto.getTemplateParams()));
            } catch (Exception e) {
                log.error("转换templateParams失败", e);
            }
        }
        
        save(message);
        
        // WebSocket推送
        if (dto.getReceiverId() != null && dto.getReceiverId() > 0) {
            // 单用户推送
            webSocketHandler.sendMessageToUser(dto.getReceiverId(), buildWebSocketMessage(message));
        } else {
            // 广播
            webSocketHandler.broadcast(buildWebSocketMessage(message));
        }
        
        return convertToVO(message);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public MessageVO sendMessageWithTemplate(String templateCode, Long receiverId, Map<String, Object> params) {
        MessageTemplate template = templateMapper.selectByCode(templateCode);
        if (template == null) {
            throw new BusinessException("消息模板不存在");
        }
        
        // 渲染模板
        String title = renderTemplate(template.getTitleTemplate(), params);
        String content = renderTemplate(template.getContentTemplate(), params);
        
        MessageSendDTO dto = new MessageSendDTO();
        dto.setType(template.getType());
        dto.setTitle(title);
        dto.setContent(content);
        dto.setReceiverId(receiverId);
        dto.setTemplateCode(templateCode);
        dto.setTemplateParams(params);
        
        return sendMessage(dto);
    }
    
    @Override
    public PageResult<MessageVO> getMessageList(MessageListDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        // 构建查询条件
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(Message::getReceiverId, userId).or().eq(Message::getReceiverId, 0L))
               .and(w -> w.isNull(Message::getExpireTime).or().gt(Message::getExpireTime, LocalDateTime.now()))
               .orderByDesc(Message::getCreateTime);
        
        if (dto.getType() != null) {
            wrapper.eq(Message::getType, dto.getType());
        }
        
        Page<Message> page = page(new Page<>(dto.getPageNum(), dto.getPageSize()), wrapper);
        
        // 转换为VO并设置已读状态
        List<MessageVO> records = page.getRecords().stream().map(msg -> {
            MessageVO vo = convertToVO(msg);
            // 查询已读状态
            if (msg.getReceiverId() == 0) {
                MessageReadStatus status = readStatusMapper.selectByMessageAndUser(msg.getId(), userId);
                vo.setIsRead(status != null ? 1 : 0);
            } else {
                vo.setIsRead(msg.getIsRead());
            }
            return vo;
        }).collect(Collectors.toList());
        
        // 根据isRead过滤
        if (dto.getIsRead() != null) {
            records = records.stream()
                    .filter(r -> r.getIsRead().equals(dto.getIsRead()))
                    .collect(Collectors.toList());
        }
        
        return PageResult.of(records, page.getTotal(), page.getCurrent(), page.getSize());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long messageId) {
        Long userId = StpUtil.getLoginIdAsLong();
        Message message = getById(messageId);
        
        if (message == null) {
            throw new BusinessException("消息不存在");
        }
        
        // 检查是否是发给该用户的消息
        if (!message.getReceiverId().equals(userId) && !message.getReceiverId().equals(0L)) {
            throw new BusinessException("无权操作该消息");
        }
        
        if (message.getReceiverId().equals(0L)) {
            // 广播消息，使用read_status表
            MessageReadStatus status = readStatusMapper.selectByMessageAndUser(messageId, userId);
            if (status == null) {
                status = new MessageReadStatus();
                status.setMessageId(messageId);
                status.setUserId(userId);
                status.setIsRead(1);
                status.setFirstReadTime(LocalDateTime.now());
                status.setLastReadTime(LocalDateTime.now());
                readStatusMapper.insert(status);
            } else {
                status.setIsRead(1);
                status.setLastReadTime(LocalDateTime.now());
                readStatusMapper.updateById(status);
            }
        } else {
            // 私信，直接更新消息表
            message.setIsRead(1);
            updateById(message);
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsReadBatch(Long[] messageIds) {
        for (Long messageId : messageIds) {
            markAsRead(messageId);
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllAsRead() {
        Long userId = StpUtil.getLoginIdAsLong();
        
        // 获取所有未读消息
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(Message::getReceiverId, userId).or().eq(Message::getReceiverId, 0L))
               .eq(Message::getIsRead, 0);
        
        List<Message> unreadMessages = list(wrapper);
        
        for (Message message : unreadMessages) {
            markAsRead(message.getId());
        }
    }
    
    @Override
    public Long getUnreadCount() {
        Long userId = StpUtil.getLoginIdAsLong();
        return messageMapper.selectUnreadCount(userId);
    }
    
    @Override
    public Long getUnreadCountByType(Integer type) {
        Long userId = StpUtil.getLoginIdAsLong();
        return messageMapper.selectUnreadCountByType(userId, type);
    }
    
    @Override
    public Map<String, Long> getUnreadStats() {
        Long userId = StpUtil.getLoginIdAsLong();
        Map<String, Long> stats = new HashMap<>();
        
        stats.put("total", messageMapper.selectUnreadCount(userId));
        stats.put("system", messageMapper.selectUnreadCountByType(userId, 1));
        stats.put("family", messageMapper.selectUnreadCountByType(userId, 2));
        stats.put("mention", messageMapper.selectUnreadCountByType(userId, 3));
        stats.put("private", messageMapper.selectUnreadCountByType(userId, 4));
        
        return stats;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMessage(Long messageId) {
        Long userId = StpUtil.getLoginIdAsLong();
        Message message = getById(messageId);
        
        if (message == null) {
            throw new BusinessException("消息不存在");
        }
        
        // 只能删除发给自己的消息
        if (!message.getReceiverId().equals(userId) && !message.getReceiverId().equals(0L)) {
            throw new BusinessException("无权删除该消息");
        }
        
        removeById(messageId);
    }
    
    @Override
    public MessageVO getMessageDetail(Long messageId) {
        Long userId = StpUtil.getLoginIdAsLong();
        Message message = getById(messageId);
        
        if (message == null) {
            throw new BusinessException("消息不存在");
        }
        
        // 自动标记已读
        if (message.getReceiverId().equals(userId) || message.getReceiverId().equals(0L)) {
            markAsRead(messageId);
        }
        
        return convertToVO(message);
    }
    
    /**
     * 转换为VO
     */
    private MessageVO convertToVO(Message message) {
        MessageVO vo = new MessageVO();
        BeanUtil.copyProperties(message, vo);
        
        // 设置类型名称
        vo.setTypeName(getTypeName(message.getType()));
        
        // 设置优先级名称
        vo.setPriorityName(getPriorityName(message.getPriority()));
        
        return vo;
    }
    
    /**
     * 获取类型名称
     */
    private String getTypeName(Integer type) {
        switch (type) {
            case 1: return "系统通知";
            case 2: return "家庭公告";
            case 3: return "@我的";
            case 4: return "私信";
            default: return "其他";
        }
    }
    
    /**
     * 获取优先级名称
     */
    private String getPriorityName(Integer priority) {
        switch (priority) {
            case 1: return "低";
            case 2: return "中";
            case 3: return "高";
            default: return "中";
        }
    }
    
    /**
     * 渲染模板
     */
    private String renderTemplate(String template, Map<String, Object> params) {
        if (StrUtil.isBlank(template) || params == null) {
            return template;
        }
        String result = template;
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", String.valueOf(entry.getValue()));
        }
        return result;
    }
    
    /**
     * 构建WebSocket消息
     */
    private String buildWebSocketMessage(Message message) {
        Map<String, Object> msg = new HashMap<>();
        msg.put("type", "message");
        msg.put("data", convertToVO(message));
        try {
            return objectMapper.writeValueAsString(msg);
        } catch (Exception e) {
            log.error("构建WebSocket消息失败", e);
            return "{}";
        }
    }
}
