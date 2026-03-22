package com.family.family.service;

import com.family.family.entity.WechatMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 企业微信异步分发服务。
 * 单独拆分为独立 Bean，避免同类内部调用导致 @Async 失效。
 */
@Slf4j
@Service
public class WechatWorkAsyncService {

    private final WechatWorkService wechatWorkService;

    public WechatWorkAsyncService(WechatWorkService wechatWorkService) {
        this.wechatWorkService = wechatWorkService;
    }

    @Async("wechatWorkExecutor")
    public void sendMessageAsync(WechatMessage message) {
        int maxRetry = 3;
        for (int i = 0; i < maxRetry; i++) {
            try {
                wechatWorkService.doSendMessage(message);
                return;
            } catch (Exception e) {
                log.error("企业微信推送失败，第{}次尝试, userId={}", i + 1, message.getTargetUserId(), e);
                if (i < maxRetry - 1) {
                    try {
                        Thread.sleep(1000L * (i + 1));
                    } catch (InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
        log.error("企业微信推送最终失败，已重试{}次, userId={}", maxRetry, message.getTargetUserId());
    }

    @Async("wechatWorkExecutor")
    public void sendReminderWithQrCodeAsync(WechatMessage message) {
        int maxRetry = 3;
        for (int i = 0; i < maxRetry; i++) {
            try {
                wechatWorkService.doSendReminderWithQrCode(message);
                return;
            } catch (Exception e) {
                log.error("提醒推送失败，第{}次尝试, userId={}", i + 1, message.getTargetUserId(), e);
                if (i < maxRetry - 1) {
                    try {
                        Thread.sleep(1000L * (i + 1));
                    } catch (InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
        log.error("提醒推送最终失败，已重试{}次, userId={}", maxRetry, message.getTargetUserId());
    }

    @Async("wechatWorkExecutor")
    public void syncExternalUsersAsync() {
        wechatWorkService.syncExternalUsers();
    }
}
