package com.family.message.controller;
import cn.dev33.satoken.annotation.SaCheckLogin;
import com.family.common.core.PageResult;
import com.family.common.core.Result;
import com.family.message.dto.MessageListDTO;
import com.family.message.dto.MessageSendDTO;
import com.family.message.service.MessageService;
import com.family.message.vo.MessageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 消息控制器
 */
@RestController
@RequestMapping("/message")
@RequiredArgsConstructor
@SaCheckLogin
public class MessageController {
    
    private final MessageService messageService;
    
    /**
     * 发送消息
     */
    @PostMapping("/send")
    public Result<MessageVO> sendMessage(@RequestBody MessageSendDTO dto) {
        return Result.success(messageService.sendMessage(dto));
    }
    
    /**
     * 获取消息列表
     */
    @GetMapping("/list")
    public Result<PageResult<MessageVO>> getMessageList(MessageListDTO dto) {
        return Result.success(messageService.getMessageList(dto));
    }
    
    /**
     * 获取消息详情
     */
    @GetMapping("/{messageId}")
    public Result<MessageVO> getMessageDetail(@PathVariable Long messageId) {
        return Result.success(messageService.getMessageDetail(messageId));
    }
    
    /**
     * 标记消息已读
     */
    @PostMapping("/read/{messageId}")
    public Result<Void> markAsRead(@PathVariable Long messageId) {
        messageService.markAsRead(messageId);
        return Result.success();
    }
    
    /**
     * 批量标记已读
     */
    @PostMapping("/read/batch")
    public Result<Void> markAsReadBatch(@RequestBody Long[] messageIds) {
        messageService.markAsReadBatch(messageIds);
        return Result.success();
    }
    
    /**
     * 标记所有消息已读
     */
    @PostMapping("/read/all")
    public Result<Void> markAllAsRead() {
        messageService.markAllAsRead();
        return Result.success();
    }
    
    /**
     * 获取未读消息数
     */
    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount() {
        return Result.success(messageService.getUnreadCount());
    }
    
    /**
     * 获取未读消息统计(按类型)
     */
    @GetMapping("/unread-stats")
    public Result<Map<String, Long>> getUnreadStats() {
        return Result.success(messageService.getUnreadStats());
    }
    
    /**
     * 删除消息
     */
    @DeleteMapping("/{messageId}")
    public Result<Void> deleteMessage(@PathVariable Long messageId) {
        messageService.deleteMessage(messageId);
        return Result.success();
    }
}
