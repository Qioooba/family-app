package com.family.notify.config;

import com.family.notify.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * 定时任务配置
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "family.notify", name = "enable-scheduled-task", havingValue = "true", matchIfMissing = true)
public class ScheduledTaskConfig {
    
    private final NotificationService notificationService;
    
    /**
     * 处理定时发送通知（每分钟执行一次）
     */
    @Scheduled(fixedRate = 60000)
    public void processScheduledNotifications() {
        try {
            notificationService.processScheduledNotifications();
        } catch (Exception e) {
            log.error("处理定时通知任务失败", e);
        }
    }
}
