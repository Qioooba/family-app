package com.family.notify.controller;

import com.family.common.core.PageResult;
import com.family.common.core.Result;
import com.family.notify.dto.NotificationQueryDTO;
import com.family.notify.dto.SendNotificationDTO;
import com.family.notify.service.NotificationService;
import com.family.notify.vo.NotificationStatsVO;
import com.family.notify.vo.NotificationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 通知控制器
 */
@RestController
@RequestMapping("/notify")
@RequiredArgsConstructor
@SaCheckLogin
public class NotificationController {
    
    private final NotificationService notificationService;
    
    /**
     * 发送通知
     */
    @PostMapping("/send")
    public Result<Void> sendNotification(@RequestBody SendNotificationDTO dto) {
        notificationService.sendNotification(dto);
        return Result.success();
    }
    
    /**
     * 获取通知列表
     */
    @GetMapping("/list")
    public Result<PageResult<NotificationVO>> getNotificationList(
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer isRead,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        NotificationQueryDTO queryDTO = new NotificationQueryDTO();
        queryDTO.setUserId(StpUtil.getLoginIdAsLong());
        queryDTO.setType(type);
        queryDTO.setIsRead(isRead);
        queryDTO.setPageNum(pageNum);
        queryDTO.setPageSize(pageSize);
        
        return Result.success(notificationService.getNotificationList(queryDTO));
    }
    
    /**
     * 获取未读通知列表
     */
    @GetMapping("/unread")
    public Result<List<NotificationVO>> getUnreadNotifications(
            @RequestParam(defaultValue = "10") Integer limit) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(notificationService.getUnreadNotifications(userId, limit));
    }
    
    /**
     * 标记通知已读
     */
    @PostMapping("/read/{id}")
    public Result<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return Result.success();
    }
    
    /**
     * 标记所有通知已读
     */
    @PostMapping("/read/all")
    public Result<Void> markAllAsRead() {
        Long userId = StpUtil.getLoginIdAsLong();
        notificationService.markAllAsRead(userId);
        return Result.success();
    }
    
    /**
     * 获取通知详情
     */
    @GetMapping("/{id}")
    public Result<NotificationVO> getNotificationDetail(@PathVariable Long id) {
        return Result.success(notificationService.getNotificationDetail(id));
    }
    
    /**
     * 删除通知
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return Result.success();
    }
    
    /**
     * 获取通知统计
     */
    @GetMapping("/stats")
    public Result<NotificationStatsVO> getNotificationStats() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(notificationService.getNotificationStats(userId));
    }
}
