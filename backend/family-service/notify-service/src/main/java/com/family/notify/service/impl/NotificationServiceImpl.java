package com.family.notify.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.family.common.core.ErrorCode;
import com.family.common.core.PageResult;
import com.family.common.core.exception.BusinessException;
import com.family.notify.dto.NotificationQueryDTO;
import com.family.notify.dto.SendNotificationDTO;
import com.family.notify.entity.Notification;
import com.family.notify.entity.NotificationSetting;
import com.family.notify.entity.NotificationTemplate;
import com.family.notify.mapper.NotificationMapper;
import com.family.notify.service.NotificationService;
import com.family.notify.service.NotificationSettingService;
import com.family.notify.service.NotificationTemplateService;
import com.family.notify.vo.NotificationStatsVO;
import com.family.notify.vo.NotificationVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 通知服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements NotificationService {
    
    private final NotificationSettingService settingService;
    private final NotificationTemplateService templateService;
    private final StringRedisTemplate redisTemplate;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendNotification(SendNotificationDTO dto) {
        if (dto.getUserIds() == null || dto.getUserIds().isEmpty()) {
            throw new BusinessException("接收用户不能为空");
        }
        
        // 解析渠道
        List<Integer> channels = parseChannels(dto.getChannels());
        
        // 如果使用模板
        String title = dto.getTitle();
        String content = dto.getContent();
        if (StrUtil.isNotBlank(dto.getTemplateCode())) {
            NotificationTemplate template = templateService.getByCode(dto.getTemplateCode());
            if (template == null) {
                throw new BusinessException("模板不存在: " + dto.getTemplateCode());
            }
            title = templateService.renderTemplate(template.getTitle(), dto.getTemplateData());
            content = templateService.renderTemplate(template.getContent(), dto.getTemplateData());
        }
        
        // 是否为定时发送
        boolean isScheduled = dto.getScheduledTime() != null && dto.getScheduledTime().isAfter(LocalDateTime.now());
        
        // 为每个用户创建通知
        for (Long userId : dto.getUserIds()) {
            for (Integer channel : channels) {
                // 检查用户设置是否允许该渠道的通知
                if (!settingService.isNotificationAllowed(userId, dto.getType(), channel)) {
                    continue;
                }
                
                Notification notification = new Notification();
                notification.setUserId(userId);
                notification.setType(dto.getType());
                notification.setChannel(channel);
                notification.setTitle(title);
                notification.setContent(content);
                notification.setBizType(dto.getBizType());
                notification.setBizId(dto.getBizId());
                notification.setFamilyId(dto.getFamilyId());
                notification.setIsRead(0);
                notification.setSendStatus(isScheduled ? 0 : 1);
                notification.setScheduledTime(dto.getScheduledTime());
                if (dto.getExtraData() != null) {
                    notification.setExtraData(JSONUtil.toJsonStr(dto.getExtraData()));
                }
                
                if (!isScheduled) {
                    notification.setSendTime(LocalDateTime.now());
                }
                
                save(notification);
                
                // 如果不是定时发送，立即推送
                if (!isScheduled) {
                    pushNotification(notification);
                }
            }
        }
        
        // 更新用户未读数缓存
        for (Long userId : dto.getUserIds()) {
            updateUnreadCountCache(userId);
        }
    }
    
    @Override
    public void sendSystemNotification(Long userId, String title, String content) {
        SendNotificationDTO dto = new SendNotificationDTO();
        dto.setUserIds(Arrays.asList(userId));
        dto.setType(1);
        dto.setChannels("1");
        dto.setTitle(title);
        dto.setContent(content);
        dto.setBizType("system");
        sendNotification(dto);
    }
    
    @Override
    public void sendFamilyAnnouncement(Long familyId, Long senderId, String title, String content) {
        // TODO: 查询家庭成员列表并发送
        SendNotificationDTO dto = new SendNotificationDTO();
        dto.setType(2);
        dto.setChannels("1,4");
        dto.setTitle(title);
        dto.setContent(content);
        dto.setFamilyId(familyId);
        dto.setBizType("announcement");
        // dto.setUserIds(familyMemberIds); // 需要查询家庭成员
        sendNotification(dto);
    }
    
    @Override
    public PageResult<NotificationVO> getNotificationList(NotificationQueryDTO queryDTO) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        
        if (queryDTO.getUserId() != null) {
            wrapper.eq(Notification::getUserId, queryDTO.getUserId());
        }
        if (queryDTO.getType() != null) {
            wrapper.eq(Notification::getType, queryDTO.getType());
        }
        if (queryDTO.getIsRead() != null) {
            wrapper.eq(Notification::getIsRead, queryDTO.getIsRead());
        }
        if (queryDTO.getStartTime() != null) {
            wrapper.ge(Notification::getCreateTime, queryDTO.getStartTime());
        }
        if (queryDTO.getEndTime() != null) {
            wrapper.le(Notification::getCreateTime, queryDTO.getEndTime());
        }
        
        wrapper.orderByDesc(Notification::getCreateTime);
        
        Page<Notification> page = page(new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()), wrapper);
        
        List<NotificationVO> voList = page.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        return PageResult.of(voList, page.getTotal(), page.getCurrent(), page.getSize());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long notificationId) {
        Notification notification = getById(notificationId);
        if (notification == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        
        if (notification.getIsRead() == 0) {
            notification.setIsRead(1);
            notification.setReadTime(LocalDateTime.now());
            updateById(notification);
            updateUnreadCountCache(notification.getUserId());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllAsRead(Long userId) {
        baseMapper.markAllAsRead(userId);
        updateUnreadCountCache(userId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteNotification(Long notificationId) {
        removeById(notificationId);
        // 更新缓存
        Notification notification = getById(notificationId);
        if (notification != null) {
            updateUnreadCountCache(notification.getUserId());
        }
    }
    
    @Override
    public NotificationVO getNotificationDetail(Long notificationId) {
        Notification notification = getById(notificationId);
        if (notification == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        
        // 自动标记为已读
        if (notification.getIsRead() == 0) {
            markAsRead(notificationId);
        }
        
        return convertToVO(notification);
    }
    
    @Override
    public NotificationStatsVO getNotificationStats(Long userId) {
        NotificationStatsVO stats = new NotificationStatsVO();
        
        Long unreadCount = baseMapper.selectUnreadCount(userId);
        stats.setUnreadCount(unreadCount != null ? unreadCount : 0L);
        
        // 各类型未读数
        List<NotificationMapper.TypeCount> typeCounts = baseMapper.selectUnreadCountByType(userId);
        for (NotificationMapper.TypeCount tc : typeCounts) {
            switch (tc.getType()) {
                case 1:
                    stats.setSystemUnreadCount(tc.getCount());
                    break;
                case 2:
                    stats.setAnnouncementUnreadCount(tc.getCount());
                    break;
                case 3:
                    stats.setReminderUnreadCount(tc.getCount());
                    break;
                case 4:
                    stats.setActivityUnreadCount(tc.getCount());
                    break;
                case 5:
                    stats.setTaskUnreadCount(tc.getCount());
                    break;
            }
        }
        
        // 总通知数
        long total = lambdaQuery().eq(Notification::getUserId, userId).count();
        stats.setTotalCount(total);
        
        return stats;
    }
    
    @Override
    public List<NotificationVO> getUnreadNotifications(Long userId, Integer limit) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0)
                .orderByDesc(Notification::getCreateTime)
                .last("LIMIT " + limit);
        
        return list(wrapper).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processScheduledNotifications() {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getSendStatus, 0)
                .le(Notification::getScheduledTime, LocalDateTime.now());
        
        List<Notification> notifications = list(wrapper);
        
        for (Notification notification : notifications) {
            try {
                notification.setSendStatus(1);
                notification.setSendTime(LocalDateTime.now());
                updateById(notification);
                pushNotification(notification);
            } catch (Exception e) {
                log.error("发送定时通知失败: {}", notification.getId(), e);
                notification.setSendStatus(2);
                notification.setFailReason(e.getMessage());
                updateById(notification);
            }
        }
    }
    
    /**
     * 解析渠道
     */
    private List<Integer> parseChannels(String channels) {
        if (StrUtil.isBlank(channels)) {
            return Arrays.asList(1); // 默认站内信
        }
        return Arrays.stream(channels.split(","))
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }
    
    /**
     * 推送通知（异步）
     */
    @Async
    public void pushNotification(Notification notification) {
        // 根据渠道推送
        switch (notification.getChannel()) {
            case 1: // 站内信 - 已通过数据库保存，前端轮询或WebSocket推送
                // TODO: WebSocket推送
                break;
            case 2: // 邮件
                // TODO: 邮件服务
                break;
            case 3: // 短信
                // TODO: 短信服务
                break;
            case 4: // 微信模板消息
                // TODO: 微信服务
                break;
        }
    }
    
    /**
     * 更新未读数缓存
     */
    private void updateUnreadCountCache(Long userId) {
        String key = "notify:unread:" + userId;
        Long count = baseMapper.selectUnreadCount(userId);
        redisTemplate.opsForValue().set(key, String.valueOf(count != null ? count : 0));
    }
    
    /**
     * 转换为VO
     */
    private NotificationVO convertToVO(Notification notification) {
        NotificationVO vo = new NotificationVO();
        BeanUtil.copyProperties(notification, vo);
        
        // 设置类型名称
        String typeName = getTypeName(notification.getType());
        vo.setTypeName(typeName);
        
        return vo;
    }
    
    /**
     * 获取类型名称
     */
    private String getTypeName(Integer type) {
        if (type == null) return "未知";
        switch (type) {
            case 1: return "系统通知";
            case 2: return "家庭公告";
            case 3: return "提醒";
            case 4: return "活动通知";
            case 5: return "任务通知";
            default: return "未知";
        }
    }
}
