package com.family.family.service;

import com.family.family.entity.MessageType;
import com.family.family.entity.WechatMessage;
import com.family.family.mapper.UserMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 企业微信推送服务 - 直接调用企业微信API
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

    @Value("${wechat.appid:wxbdc70536c5e52b82}")
    private String wechatAppId;

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
        if (accessToken != null && System.currentTimeMillis() < tokenExpireTime) {
            return accessToken;
        }

        if (corpId == null || corpId.isEmpty() || secret == null || secret.isEmpty()) {
            log.warn("企业微信配置不完整");
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
                tokenExpireTime = System.currentTimeMillis() + (7200 - 300) * 1000;
                log.info("获取企业微信access_token成功");
                return accessToken;
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
                return;
            } catch (Exception e) {
                log.error("企业微信推送失败，第{}次尝试, userId={}", i + 1, message.getTargetUserId(), e);
                if (i < maxRetry - 1) {
                    try { Thread.sleep(1000 * (i + 1)); } catch (InterruptedException ignored) {}
                }
            }
        }
        log.error("企业微信推送最终失败，已重试{}次, userId={}", maxRetry, message.getTargetUserId());
    }

    /**
     * 实际发送消息 - 直接调用企业微信API
     */
    private void doSendMessage(WechatMessage message) throws Exception {
        String token = getAccessToken();
        if (token == null) {
            throw new Exception("无法获取access_token");
        }

        String workUserId = getWorkUserId(message.getTargetUserId());
        if (workUserId == null || workUserId.isEmpty()) {
            log.debug("用户{}未配置企业微信ID，跳过推送", message.getTargetUserId());
            return;
        }

        String url = "https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=" + token;

        Map<String, Object> payload = new HashMap<>();
        payload.put("touser", workUserId);
        payload.put("msgtype", "miniprogram_notice");
        payload.put("agentid", agentId);
        
        // 小程序通知消息（点击跳转到小程序）
        Map<String, Object> miniprogramNotice = new HashMap<>();
        miniprogramNotice.put("appid", wechatAppId);
        miniprogramNotice.put("page", "pages/task/index");
        miniprogramNotice.put("title", message.getTitle());
        // 去除HTML标签
        String cleanDesc = message.getDescription().replaceAll("<[^>]*>", "");
        miniprogramNotice.put("description", cleanDesc);
        
        // 内容项
        java.util.List<Map<String, String>> contentItems = new java.util.ArrayList<>();
        Map<String, String> item1 = new HashMap<>();
        item1.put("key", "时间");
        item1.put("value", java.time.LocalDateTime.now().toString().substring(0, 16));
        contentItems.add(item1);
        
        Map<String, String> item2 = new HashMap<>();
        item2.put("key", "内容");
        item2.put("value", cleanDesc.length() > 100 ? cleanDesc.substring(0, 100) + "..." : cleanDesc);
        contentItems.add(item2);
        
        miniprogramNotice.put("content_item", contentItems);
        miniprogramNotice.put("emphasis_first_item", true);
        
        payload.put("miniprogram_notice", miniprogramNotice);

        String response = restTemplate.postForObject(url, payload, String.class);
        log.info("企业微信消息发送成功(小程序), userId={}, workId={}, response={}", 
                 message.getTargetUserId(), workUserId, response);
    }

    // ==================== 便捷方法：任务相关 ====================

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
}
