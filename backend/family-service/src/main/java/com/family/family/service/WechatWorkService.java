package com.family.family.service;

import com.family.family.entity.MessageType;
import com.family.family.entity.User;
import com.family.family.entity.WechatMessage;
import com.family.family.mapper.UserMapper;
import com.family.family.util.TempTokenUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 企业微信推送服务
 * 支持两种推送方式：
 * 1. 企业内部成员 - /message/send (work_user_id)
 * 2. 外部联系人(客户) - /externalcontact/add_msg_template (external_user_id)
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

    @Value("${weixin.appSecret:}")
    private String wechatAppSecret;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TempTokenUtil tempTokenUtil;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private String accessToken;
    private long tokenExpireTime;

    // ==================== Token管理 ====================

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
                log.debug("获取企业微信access_token成功");
                return accessToken;
            }
        } catch (Exception e) {
            log.error("获取企业微信access_token异常", e);
        }
        return null;
    }

    // ==================== 用户信息获取 ====================

    /**
     * 用户的企业微信身份信息
     */
    public static class WechatWorkIdentity {
        public String workUserId;      // 内部成员ID
        public String externalUserId;  // 外部联系人ID
        
        public boolean hasWorkUserId() {
            return workUserId != null && !workUserId.isEmpty();
        }
        
        public boolean hasExternalUserId() {
            return externalUserId != null && !externalUserId.isEmpty();
        }
        
        public boolean canSend() {
            return hasWorkUserId() || hasExternalUserId();
        }
    }

    /**
     * 获取用户的企业微信身份信息
     */
    private WechatWorkIdentity getWorkIdentity(Long userId) {
        WechatWorkIdentity identity = new WechatWorkIdentity();
        if (userId == null) return identity;
        
        try {
            User user = userMapper.selectById(userId);
            if (user != null) {
                identity.workUserId = user.getWorkUserId();
                identity.externalUserId = user.getExternalUserId();
                log.debug("获取用户{}企业微信身份: workId={}, externalId={}", 
                    userId, identity.workUserId, identity.externalUserId);
            }
        } catch (Exception e) {
            log.error("获取用户企业微信身份失败, userId={}", userId, e);
        }
        return identity;
    }

    // ==================== 消息发送核心 ====================

    /**
     * 核心：异步发送消息（带3次重试）
     * 自动选择最优发送方式：内部成员 > 外部联系人
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
     * 实际发送消息
     * 优先级：1.内部成员推送 2.外部联系人推送
     */
    private void doSendMessage(WechatMessage message) throws Exception {
        String token = getAccessToken();
        if (token == null) {
            throw new Exception("无法获取access_token");
        }

        WechatWorkIdentity identity = getWorkIdentity(message.getTargetUserId());
        if (!identity.canSend()) {
            log.debug("用户{}未配置任何企业微信身份，跳过推送", message.getTargetUserId());
            return;
        }

        // 优先使用内部成员推送（更可靠）
        if (identity.hasWorkUserId()) {
            try {
                sendToInternalUser(token, identity.workUserId, message);
                return;
            } catch (Exception e) {
                log.warn("内部成员推送失败，尝试外部联系人, userId={}", message.getTargetUserId());
                // 如果内部推送失败，且有外部ID，尝试外部推送
                if (identity.hasExternalUserId()) {
                    sendToExternalUser(token, identity.externalUserId, message);
                    return;
                }
                throw e;
            }
        }
        
        // 只有外部联系人ID
        if (identity.hasExternalUserId()) {
            sendToExternalUser(token, identity.externalUserId, message);
        }
    }

    /**
     * 发送给企业内部成员
     */
    private void sendToInternalUser(String token, String workUserId, WechatMessage message) throws Exception {
        String url = "https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=" + token;

        Map<String, Object> payload = new HashMap<>();
        payload.put("touser", workUserId);
        payload.put("msgtype", "text");
        payload.put("agentid", agentId);
        
        String content = buildMessageContent(message);
        
        Map<String, String> text = new HashMap<>();
        text.put("content", content);
        payload.put("text", text);

        String response = restTemplate.postForObject(url, payload, String.class);
        Map<String, Object> result = objectMapper.readValue(response, Map.class);
        
        int errcode = (Integer) result.getOrDefault("errcode", -1);
        if (errcode != 0) {
            throw new Exception("发送失败: " + result.get("errmsg"));
        }
        
        log.info("企业微信内部成员消息发送成功, userId={}, workId={}", 
                 message.getTargetUserId(), workUserId);
    }

    /**
     * 发送给外部联系人（客户）
     * 使用企业微信客户联系消息接口
     */
    private void sendToExternalUser(String token, String externalUserId, WechatMessage message) throws Exception {
        String url = "https://qyapi.weixin.qq.com/cgi-bin/externalcontact/add_msg_template?access_token=" + token;

        String content = buildMessageContent(message);
        
        // 构建外部联系人消息
        Map<String, Object> payload = new HashMap<>();
        
        // 接收人列表
        List<String> externalUserIds = new ArrayList<>();
        externalUserIds.add(externalUserId);
        payload.put("external_userid", externalUserIds);
        
        // 发送者 - 使用企业微信小助手
        payload.put("sender", "XIAOXHUSHOU");
        
        // 消息类型 - 文本
        payload.put("msgtype", "text");
        
        Map<String, String> text = new HashMap<>();
        text.put("content", content);
        payload.put("text", text);

        String response = restTemplate.postForObject(url, payload, String.class);
        Map<String, Object> result = objectMapper.readValue(response, Map.class);
        
        int errcode = (Integer) result.getOrDefault("errcode", -1);
        if (errcode != 0) {
            // 特殊错误处理
            if (errcode == 48002) {
                throw new Exception("外部联系人48小时内无互动，无法发送消息");
            }
            throw new Exception("发送失败: " + result.get("errmsg"));
        }
        
        // 检查是否有失败的用户
        List<String> failList = (List<String>) result.get("fail_list");
        if (failList != null && !failList.isEmpty()) {
            log.warn("外部联系人消息部分发送失败: {}", failList);
        }
        
        log.info("企业微信外部联系人消息发送成功, userId={}, externalId={}, msgId={}", 
                 message.getTargetUserId(), externalUserId, result.get("msgid"));
    }

    /**
     * 构建消息内容
     */
    private String buildMessageContent(WechatMessage message) {
        String cleanDesc = message.getDescription().replaceAll("<[^>]*>", "");
        
        // 完成任务的消息不需要链接
        if (message.getType() == MessageType.TASK_COMPLETED || 
            message.getType() == MessageType.TASK_COMPLETE_SELF) {
            return message.getTitle() + "\n\n" + cleanDesc;
        }
        
        // 其他消息附加免密登录链接
        String autoLoginUrl = tempTokenUtil.getAutoLoginUrl(message.getTargetUserId());
        return message.getTitle() + "\n\n" + cleanDesc + "\n\n" + "【点击免密登录】" + autoLoginUrl;
    }

    // ==================== 便捷方法：任务相关 ====================

    public void notifyTaskAssignedToAssignee(Long assigneeId, String fromName, String taskTitle) {
        WechatMessage msg = new WechatMessage();
        msg.setType(MessageType.TASK_ASSIGNED);
        msg.setTargetUserId(assigneeId);
        msg.setTitle("🏠 新任务指派");
        msg.setDescription(String.format(
            "【%s】给你指派了新任务：%s  时间：%s",
            fromName, taskTitle,
            java.time.LocalDateTime.now().toString().substring(0, 16)
        ));
        sendMessageAsync(msg);
    }

    public void notifyTaskAssignedToCreator(Long creatorId, String toName, String taskTitle) {
        WechatMessage msg = new WechatMessage();
        msg.setType(MessageType.TASK_ASSIGN_NOTIFY);
        msg.setTargetUserId(creatorId);
        msg.setTitle("✅ 任务已指派");
        msg.setDescription(String.format(
            "你已指派任务给【%s】：%s  时间：%s",
            toName, taskTitle,
            java.time.LocalDateTime.now().toString().substring(0, 16)
        ));
        sendMessageAsync(msg);
    }

    public void notifyTaskCompletedToCreator(Long creatorId, String completeByName, String taskTitle) {
        WechatMessage msg = new WechatMessage();
        msg.setType(MessageType.TASK_COMPLETED);
        msg.setTargetUserId(creatorId);
        msg.setTitle("🎉 任务已完成");
        msg.setDescription(String.format(
            "【%s】完成了你指派的任务：%s  时间：%s",
            completeByName, taskTitle,
            java.time.LocalDateTime.now().toString().substring(0, 16)
        ));
        sendMessageAsync(msg);
    }

    public void notifyTaskCompletedToSelf(Long userId, String taskTitle) {
        WechatMessage msg = new WechatMessage();
        msg.setType(MessageType.TASK_COMPLETE_SELF);
        msg.setTargetUserId(userId);
        msg.setTitle("✨ 任务完成");
        msg.setDescription(String.format(
            "你完成了任务：%s  时间：%s  继续保持！",
            taskTitle,
            java.time.LocalDateTime.now().toString().substring(0, 16)
        ));
        sendMessageAsync(msg);
    }
}
