package com.family.notify.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.family.common.core.PageResult;
import com.family.notify.dto.NotificationQueryDTO;
import com.family.notify.dto.SendNotificationDTO;
import com.family.family.entity.Notification;
import com.family.notify.vo.NotificationStatsVO;
import com.family.notify.vo.NotificationVO;

import java.util.List;

/**
 * 通知服务接口
 */
public interface NotificationService extends IService<Notification> {
    
    /**
     * 发送通知
     */
    void sendNotification(SendNotificationDTO dto);
    
    /**
     * 发送系统通知
     */
    void sendSystemNotification(Long userId, String title, String content);
    
    /**
     * 发送家庭公告
     */
    void sendFamilyAnnouncement(Long familyId, Long senderId, String title, String content);
    
    /**
     * 获取通知列表
     */
    PageResult<NotificationVO> getNotificationList(NotificationQueryDTO queryDTO);
    
    /**
     * 标记通知已读
     */
    void markAsRead(Long notificationId);
    
    /**
     * 标记所有通知已读
     */
    void markAllAsRead(Long userId);
    
    /**
     * 删除通知
     */
    void deleteNotification(Long notificationId);
    
    /**
     * 获取通知详情
     */
    NotificationVO getNotificationDetail(Long notificationId);
    
    /**
     * 获取通知统计
     */
    NotificationStatsVO getNotificationStats(Long userId);
    
    /**
     * 获取未读通知列表
     */
    List<NotificationVO> getUnreadNotifications(Long userId, Integer limit);
    
    /**
     * 处理定时发送任务
     */
    void processScheduledNotifications();
}
