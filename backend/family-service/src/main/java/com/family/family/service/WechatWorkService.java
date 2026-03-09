package com.family.family.service;

import com.family.family.entity.MessageType;
import com.family.family.entity.WechatMessage;
import com.family.family.mapper.UserMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 企业微信推送服务
 */
@Slf4j
@Service
public class WechatWorkService {

    @Value("${wechat.work.corpid:}")
    private String corpId;

    @Value("${wechat.work.agentid:}")
    private String agentId;

    @Value("${wechat.work.secret:}")
    private String secret;

    @Autowired
    private UserMapper userMapper;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private String accessToken;
    private long tokenExpireTime;

    /**
     * 获取企业微信access_token
     */
    private synchronized String getAccessToken() {
        // 如果token未过期，直接返回
        if (accessToken != null && System.currentTimeMillis() < tokenExpireTime) {
            return accessToken;
        }

        // 检查配置是否完整
        if (corpId == null || corpId.isEmpty() || secret == null || secret.isEmpty()) {
            log.warn("企业微信配置不完整，无法获取access_token");
            return null;
        }

        try {
            String url = String.format(
                "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=%s&corpsecret=%s",
                corpId, secret
            );

            String response = restTemplate.getForObject(url, String.class);
            Map<String, Object> result = objectMapper.readValue(response, Map.class);

            if (result.get("access_token") != null) {
                accessToken = (String) result.get("access_token");
                // token有效期2小时，提前5分钟刷新
                tokenExpireTime = System.currentTimeMillis() + (7200 - 300) * 1000;
                log.info("获取企业微信access_token成功");
                return accessToken;
            } else {
                log.error("获取access_token失败: {}", result);
            }
        } catch (Exception e) {
            log.error("获取企业微信access_token异常", e);
        }

        return null;
    }

    /**
     * 获取用户的企业微信ID
     */
    private String getWorkUserId(Long userId) {
        if (userId == null) return null;
        try {
            var user = userMapper.selectById(userId);
            return user != null ? user.getWorkUserId() : null;
        } catch (Exception e) {
            log.error("获取用户企业微信ID失败, userId={}", userId, e);
            return null;
        }
    }

    /**
     * 核心：异步发送消息（带3次重试）
     */
    @Async("wechatWorkExecutor")
    public void sendMessageAsync(WechatMessage message) {
        int maxRetry = 3;
        for (int i = 0; i < maxRetry; i++) {
            try {
                doSendMessage(message);
                return; // 发送成功，退出重试
            } catch (Exception e) {
                log.error("企业微信推送失败，第{}次尝试, userId={}", i + 1, message.getTargetUserId(), e);
                if (i < maxRetry - 1) {
                    try {
                        Thread.sleep(1000 * (i + 1)); // 递增延迟：1s, 2s, 3s
                    } catch (InterruptedException ignored) {}
                }
            }
        }
        log.error("企业微信推送最终失败，已重试{}次, userId={}", maxRetry, message.getTargetUserId());
    }

    /**
     * 实际发送消息
     */
    private void doSendMessage(WechatMessage message) throws Exception {
        // 获取用户企业微信ID
        String workUserId = getWorkUserId(message.getTargetUserId());
        if (workUserId == null || workUserId.isEmpty()) {
            log.debug("用户{}未配置企业微信ID，跳过推送", message.getTargetUserId());
            return;
        }

        // 获取access_token
        String token = getAccessToken();
        if (token == null) {
            throw new RuntimeException("无法获取access_token");
        }

        // 构建消息
        String url = "https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=" + token;

        Map<String, Object> payload = new HashMap<>();
        payload.put("touser", workUserId);
        payload.put("msgtype", "textcard");
        payload.put("agentid", agentId);

        // 文本卡片消息（美观）
        Map<String, String> textcard = new HashMap<>();
        textcard.put("title", message.getTitle());
        textcard.put("description", message.getDescription());
        textcard.put("url", message.getUrl() != null ? message.getUrl() : "https://qioba.cn");
        textcard.put("btntxt", "查看详情");

        payload.put("textcard", textcard);

        // 发送请求
        String response = restTemplate.postForObject(url, payload, String.class);
        log.info("企业微信消息发送成功, userId={}, workId={}, response={}", 
                 message.getTargetUserId(), workUserId, response);
    }

    // ==================== 便捷方法：任务相关 ====================

    /**
     * 任务被指派 - 通知执行人
     * 例：齐军收到"陶陶给你指派了任务：买菜"
     */
    public void notifyTaskAssignedToAssignee(Long assigneeId, String fromName, String taskTitle) {
        WechatMessage msg = new WechatMessage();
        msg.setType(MessageType.TASK_ASSIGNED);
        msg.setTargetUserId(assigneeId);
        msg.setTitle("🏠 新任务指派");
        msg.setDescription(String.format(
            "<div class='gray'>%s</div><div class='highlight'>%s给你指派了任务：%s</div>",
            java.time.LocalDateTime.now().toString().substring(0, 16),
            fromName, taskTitle
        ));
        sendMessageAsync(msg);
    }

    /**
     * 任务已指派 - 通知指派人
     * 例：陶陶收到"你已指派任务给齐军：买菜"
     */
    public void notifyTaskAssignedToCreator(Long creatorId, String toName, String taskTitle) {
        WechatMessage msg = new WechatMessage();
        msg.setType(MessageType.TASK_ASSIGN_NOTIFY);
        msg.setTargetUserId(creatorId);
        msg.setTitle("✅ 任务已指派");
        msg.setDescription(String.format(
            "<div class='gray'>%s</div><div class='highlight'>你已指派任务给%s：%s</div>",
            java.time.LocalDateTime.now().toString().substring(0, 16),
            toName, taskTitle
        ));
        sendMessageAsync(msg);
    }

    /**
     * 任务完成 - 通知指派人
     * 例：陶陶收到"齐军完成了任务：买菜"
     */
    public void notifyTaskCompletedToCreator(Long creatorId, String completeByName, String taskTitle) {
        WechatMessage msg = new WechatMessage();
        msg.setType(MessageType.TASK_COMPLETED);
        msg.setTargetUserId(creatorId);
        msg.setTitle("🎉 任务已完成");
        msg.setDescription(String.format(
            "<div class='gray'>%s</div><div class='highlight'>%s完成了你指派的任务：%s</div>",
            java.time.LocalDateTime.now().toString().substring(0, 16),
            completeByName, taskTitle
        ));
        sendMessageAsync(msg);
    }

    /**
     * 任务完成 - 通知自己
     * 例：齐军收到"你完成了任务：买菜"
     */
    public void notifyTaskCompletedToSelf(Long userId, String taskTitle) {
        WechatMessage msg = new WechatMessage();
        msg.setType(MessageType.TASK_COMPLETE_SELF);
        msg.setTargetUserId(userId);
        msg.setTitle("✨ 任务完成");
        msg.setDescription(String.format(
            "<div class='gray'>%s</div><div class='highlight'>你完成了任务：%s</div>继续保持！",
            java.time.LocalDateTime.now().toString().substring(0, 16),
            taskTitle
        ));
        sendMessageAsync(msg);
    }

    // ==================== 预留方法：心愿相关 ====================

    /**
     * 新心愿 - 通知家庭其他成员（预留）
     */
    public void notifyWishCreated(Long targetUserId, String creatorName, String wishTitle) {
        // 预留实现
        log.debug("心愿创建通知预留, target={}, creator={}, wish={}", targetUserId, creatorName, wishTitle);
    }

    /**
     * 心愿完成 - 通知创建者（预留）
     */
    public void notifyWishCompleted(Long targetUserId, String completeByName, String wishTitle) {
        // 预留实现
        log.debug("心愿完成通知预留, target={}, by={}, wish={}", targetUserId, completeByName, wishTitle);
    }

    // ==================== 预留方法：纪念日相关 ====================

    /**
     * 纪念日提醒（预留）
     */
    public void notifyAnniversaryRemind(Long targetUserId, String title, int daysLeft) {
        // 预留实现
        log.debug("纪念日提醒预留, target={}, title={}, days={}", targetUserId, title, daysLeft);
    }

    /**
     * 今日纪念日（预留）
     */
    public void notifyAnniversaryToday(Long targetUserId, String title) {
        // 预留实现
        log.debug("今日纪念日预留, target={}, title={}", targetUserId, title);
    }
}
