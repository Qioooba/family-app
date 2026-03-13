package com.family.family.service;

import com.family.family.entity.MessageType;
import com.family.family.entity.User;
import com.family.family.entity.WechatMessage;
import com.family.family.mapper.UserMapper;
import com.family.family.util.TempTokenUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 企业微信推送服务
 * 配置从数据库读取，支持两种推送方式：
 * 1. 外部联系人(客户) - /externalcontact/add_msg_template (external_user_id) - 优先
 * 2. 企业内部成员 - /message/send (work_user_id) - 备用
 */
@Slf4j
@Service
public class WechatWorkService {

    @Autowired
    private SystemConfigService configService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TempTokenUtil tempTokenUtil;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private String accessToken;
    private long tokenExpireTime;

    // ==================== 小程序配置获取 ====================
    
    private String getMiniProgramAppId() {
        return configService.getValue("wechat.miniapp.appid", "wxbdc70536c5e52b82");
    }
    
    private String getMiniProgramPage() {
        return configService.getValue("wechat.miniapp.default_page", "pages/task/index");
    }
    
    private String getCorpId() {
        return configService.getWechatWorkCorpId();
    }
    
    private String getAgentId() {
        return configService.getWechatWorkAgentId();
    }
    
    private String getSecret() {
        return configService.getWechatWorkSecret();
    }
    
    private String getWorkUserId() {
        return configService.getWechatWorkUserId();
    }

    // ==================== Token管理 ====================

    /**
     * 获取企业微信access_token
     */
    private synchronized String getAccessToken() {
        if (accessToken != null && System.currentTimeMillis() < tokenExpireTime) {
            return accessToken;
        }

        String corpId = getCorpId();
        String secret = getSecret();
        
        if (corpId.isEmpty() || secret.isEmpty()) {
            log.warn("企业微信配置不完整，请在数据库sys_config表中配置");
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
            } else {
                log.error("获取access_token失败: {}", result.get("errmsg"));
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
     * 自动选择最优发送方式：外部联系人（优先）> 内部成员（备用）
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
     * 优先级：1.外部联系人推送（微信显示更好） 2.内部成员推送
     */
    private void doSendMessage(WechatMessage message) throws Exception {
        String token = getAccessToken();
        if (token == null) {
            throw new Exception("无法获取access_token，请检查企业微信配置");
        }

        WechatWorkIdentity identity = getWorkIdentity(message.getTargetUserId());
        if (!identity.canSend()) {
            log.debug("用户{}未配置任何企业微信身份，跳过推送", message.getTargetUserId());
            return;
        }

        // 优先使用外部联系人推送（微信里显示更明显）
        if (identity.hasExternalUserId()) {
            try {
                sendToExternalUser(token, identity.externalUserId, message);
                return;
            } catch (Exception e) {
                log.warn("外部联系人推送失败，尝试内部成员, userId={}, error={}", message.getTargetUserId(), e.getMessage());
                // 如果外部推送失败（如48小时限制），且有内部ID，尝试内部推送
                if (identity.hasWorkUserId()) {
                    sendToInternalUser(token, identity.workUserId, message);
                    return;
                }
                throw e;
            }
        }
        
        // 只有内部成员ID
        if (identity.hasWorkUserId()) {
            sendToInternalUser(token, identity.workUserId, message);
        }
    }

    /**
     * 发送给企业内部成员（使用miniprogram_notice或图文卡片）
     */
    private void sendToInternalUser(String token, String workUserId, WechatMessage message) throws Exception {
        String url = "https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=" + token;

        Map<String, Object> payload = new HashMap<>();
        payload.put("touser", workUserId);
        payload.put("agentid", getAgentId());
        
        // 使用news图文消息，支持小程序跳转
        Map<String, Object> news = new HashMap<>();
        List<Map<String, String>> articles = new ArrayList<>();
        
        Map<String, String> article = new HashMap<>();
        article.put("title", message.getTitle());
        article.put("description", buildRichMessageContent(message));
        article.put("url", "https://mp.weixin.qq.com/s?__biz=" + getMiniProgramAppId() + "&page=" + getMiniProgramPage());
        article.put("picurl", "https://qioba.cn/logo.png");
        
        articles.add(article);
        news.put("articles", articles);
        payload.put("news", news);
        payload.put("msgtype", "news");

        String response = restTemplate.postForObject(url, payload, String.class);
        Map<String, Object> result = objectMapper.readValue(response, Map.class);
        
        int errcode = (Integer) result.getOrDefault("errcode", -1);
        if (errcode != 0) {
            // 如果news失败，回退到text
            log.warn("图文消息发送失败，回退到文本消息: {}", result.get("errmsg"));
            sendTextMessage(token, workUserId, message);
            return;
        }
        
        log.info("企业微信图文消息发送成功, userId={}, workId={}", 
                 message.getTargetUserId(), workUserId);
    }
    
    /**
     * 发送纯文本消息（回退方案）
     */
    private void sendTextMessage(String token, String workUserId, WechatMessage message) throws Exception {
        String url = "https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=" + token;

        Map<String, Object> payload = new HashMap<>();
        payload.put("touser", workUserId);
        payload.put("msgtype", "text");
        payload.put("agentid", getAgentId());
        
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
        
        log.info("企业微信文本消息发送成功, userId={}, workId={}", 
                 message.getTargetUserId(), workUserId);
    }

    /**
     * 发送给外部联系人（客户）
     * 使用企业微信客户联系消息接口
     */
    private void sendToExternalUser(String token, String externalUserId, WechatMessage message) throws Exception {
        String url = "https://qyapi.weixin.qq.com/cgi-bin/externalcontact/add_msg_template?access_token=" + token;

        // 构建外部联系人消息
        Map<String, Object> payload = new HashMap<>();

        // 接收人列表
        List<String> externalUserIds = new ArrayList<>();
        externalUserIds.add(externalUserId);
        payload.put("external_userid", externalUserIds);

        // 发送者 - 使用企业微信小助手
        payload.put("sender", getWorkUserId());

        // 尝试使用小程序卡片消息
        try {
            Map<String, Object> miniprogram = new HashMap<>();
            miniprogram.put("title", message.getTitle());
            miniprogram.put("pic_media_id", ""); // 可以用默认图片或上传图片获取media_id
            miniprogram.put("appid", getMiniProgramAppId());
            miniprogram.put("page", message.getUrl() != null ? message.getUrl() : getMiniProgramPage());

            payload.put("msgtype", "miniprogram");
            payload.put("miniprogram", miniprogram);

            String response = restTemplate.postForObject(url, payload, String.class);
            Map<String, Object> result = objectMapper.readValue(response, Map.class);

            int errcode = (Integer) result.getOrDefault("errcode", -1);
            if (errcode == 0) {
                log.info("企业微信外部联系人小程序消息发送成功, userId={}, externalId={}, msgId={}",
                        message.getTargetUserId(), externalUserId, result.get("msgid"));
                return;
            } else {
                log.warn("小程序消息发送失败，回退到文本消息: {}", result.get("errmsg"));
            }
        } catch (Exception e) {
            log.warn("小程序消息发送异常，回退到文本消息", e);
        }

        // 回退到文本消息
        String content = buildRichMessageContent(message);
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
        @SuppressWarnings("unchecked")
        List<String> failList = (List<String>) result.get("fail_list");
        if (failList != null && !failList.isEmpty()) {
            log.warn("外部联系人消息部分发送失败: {}", failList);
        }

        log.info("企业微信外部联系人文本消息发送成功, userId={}, externalId={}, msgId={}",
                message.getTargetUserId(), externalUserId, result.get("msgid"));
    }

    /**
     * 构建丰富的任务消息内容（支持小程序跳转）
     */
    private String buildRichMessageContent(WechatMessage message) {
        StringBuilder sb = new StringBuilder();
        
        // 标题
        sb.append("🏠 ").append(message.getTitle()).append("\n\n");
        
        // 描述内容（去除HTML标签）
        String cleanDesc = message.getDescription().replaceAll("<[^>]*>", "");
        sb.append(cleanDesc).append("\n\n");
        
        // 分隔线
        sb.append("━━━━━━━━━━━━━━━\n\n");
        
        // 小程序跳转链接
        String miniAppId = getMiniProgramAppId();
        String path = message.getUrl() != null ? message.getUrl().replace("https://qioba.cn", "") : getMiniProgramPage();
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        
        // 使用小程序链接格式（企业微信支持）
        sb.append("<a href=\"http://mp.weixin.qq.com/mp/inappmsg?appid=")
          .append(miniAppId)
          .append("\">📱 点击查看详情</a>");
        
        return sb.toString();
    }

    /**
     * 构建简单的消息内容（向后兼容）
     */
    private String buildMessageContent(WechatMessage message) {
        String cleanDesc = message.getDescription().replaceAll("<[^>]*>", "");
        
        // 完成任务的消息不需要链接
        if (message.getType() == MessageType.TASK_COMPLETED || 
            message.getType() == MessageType.TASK_COMPLETE_SELF) {
            return message.getTitle() + "\n\n" + cleanDesc;
        }
        
        // 其他消息附加免密登录链接和小程序跳转
        String autoLoginUrl = tempTokenUtil.getAutoLoginUrl(message.getTargetUserId());
        StringBuilder sb = new StringBuilder();
        sb.append(message.getTitle()).append("\n\n")
          .append(cleanDesc).append("\n\n")
          .append("━━━━━━━━━━━━━━━\n\n")
          .append("<a href=\"http://mp.weixin.qq.com/mp/inappmsg?appid=")
          .append(getMiniProgramAppId())
          .append("\">📱 打开小程序查看</a>\n\n")
          .append("或点击免密登录：").append(autoLoginUrl);
        
        return sb.toString();
    }

    // ==================== 便捷方法：任务相关（增强版） ====================

    /**
     * 发送富文本任务通知（带小程序跳转）
     * 
     * @param userId 接收者用户ID
     * @param type 消息类型
     * @param taskTitle 任务标题
     * @param taskContent 任务内容/备注
     * @param creatorName 创建人
     * @param assigneeName 执行人
     * @param operatorName 操作人
     * @param miniProgramPath 小程序页面路径
     */
    public void sendTaskNotification(Long userId, MessageType type, String taskTitle, 
                                     String taskContent, String creatorName, 
                                     String assigneeName, String operatorName,
                                     String miniProgramPath) {
        WechatMessage msg = new WechatMessage();
        msg.setType(type);
        msg.setTargetUserId(userId);
        msg.setUrl(miniProgramPath);
        
        String title;
        StringBuilder desc = new StringBuilder();
        
        switch (type) {
            case TASK_ASSIGNED:
                title = "🏠 新任务指派";
                desc.append("📋 任务：").append(taskTitle).append("\n\n");
                if (taskContent != null && !taskContent.isEmpty()) {
                    desc.append("📝 备注：").append(taskContent).append("\n\n");
                }
                desc.append("👤 指派人：").append(operatorName).append("\n");
                desc.append("📅 时间：").append(java.time.LocalDateTime.now().toString().substring(0, 16));
                break;
                
            case TASK_ASSIGN_NOTIFY:
                title = "✅ 任务已指派";
                desc.append("📋 任务：").append(taskTitle).append("\n\n");
                desc.append("👤 执行人：").append(assigneeName).append("\n");
                desc.append("📅 时间：").append(java.time.LocalDateTime.now().toString().substring(0, 16));
                break;
                
            case TASK_COMPLETED:
                title = "🎉 任务已完成";
                desc.append("📋 任务：").append(taskTitle).append("\n\n");
                if (taskContent != null && !taskContent.isEmpty()) {
                    desc.append("📝 完成备注：").append(taskContent).append("\n\n");
                }
                desc.append("👤 完成人：").append(operatorName).append("\n");
                desc.append("📅 时间：").append(java.time.LocalDateTime.now().toString().substring(0, 16));
                break;
                
            case TASK_COMPLETE_SELF:
                title = "✨ 任务完成";
                desc.append("📋 任务：").append(taskTitle).append("\n\n");
                desc.append("👍 恭喜你完成了任务！\n");
                desc.append("📅 完成时间：").append(java.time.LocalDateTime.now().toString().substring(0, 16));
                break;
                
            default:
                title = "📱 任务通知";
                desc.append("📋 任务：").append(taskTitle);
        }
        
        msg.setTitle(title);
        msg.setDescription(desc.toString());
        
        sendMessageAsync(msg);
    }

    // ==================== 便捷方法：向后兼容 ====================

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
    
    // ==================== 配置管理API ====================
    
    /**
     * 同步企业微信外部联系人
     * 调用企业微信API获取客户列表，并更新到用户表
     */
    public void syncExternalUsers() {
        try {
            String token = getAccessToken();
            if (token == null) {
                log.error("无法获取access_token，跳过同步");
                return;
            }
            
            // 1. 获取客户列表
            String listUrl = String.format(
                "https://qyapi.weixin.qq.com/cgi-bin/externalcontact/list?access_token=%s&userid=%s",
                token, getWorkUserId()
            );
            String listResponse = restTemplate.getForObject(listUrl, String.class);
            Map<String, Object> listResult = objectMapper.readValue(listResponse, Map.class);
            
            if ((Integer) listResult.getOrDefault("errcode", -1) != 0) {
                log.error("获取客户列表失败: {}", listResult.get("errmsg"));
                return;
            }
            
            @SuppressWarnings("unchecked")
            List<String> externalUserIds = (List<String>) listResult.get("external_userid");
            if (externalUserIds == null || externalUserIds.isEmpty()) {
                log.info("小助手没有客户");
                return;
            }
            
            log.info("找到 {} 个客户，开始同步", externalUserIds.size());
            
            // 2. 获取每个客户的详情并匹配用户
            for (String externalUserId : externalUserIds) {
                try {
                    String detailUrl = String.format(
                        "https://qyapi.weixin.qq.com/cgi-bin/externalcontact/get?access_token=%s&external_userid=%s",
                        token, externalUserId
                    );
                    String detailResponse = restTemplate.getForObject(detailUrl, String.class);
                    Map<String, Object> detailResult = objectMapper.readValue(detailResponse, Map.class);
                    
                    if ((Integer) detailResult.getOrDefault("errcode", -1) != 0) {
                        continue;
                    }
                    
                    @SuppressWarnings("unchecked")
                    Map<String, Object> contact = (Map<String, Object>) detailResult.get("external_contact");
                    if (contact == null) continue;
                    
                    String name = (String) contact.get("name");
                    
                    // 3. 根据名称匹配用户并更新
                    matchAndUpdateUser(name, externalUserId);
                    
                } catch (Exception e) {
                    log.error("获取客户详情失败: {}", externalUserId, e);
                }
            }
            
        } catch (Exception e) {
            log.error("同步外部联系人失败", e);
        }
    }
    
    /**
     * 根据名称匹配用户并更新external_user_id
     */
    private void matchAndUpdateUser(String customerName, String externalUserId) {
        if (customerName == null || customerName.isEmpty()) return;
        
        // 查询所有用户，尝试匹配
        List<User> users = userMapper.selectList(null);
        
        for (User user : users) {
            String nickname = user.getNickname();
            String username = user.getUsername();
            
            boolean matched = false;
            
            if (nickname != null && nickname.equals(customerName)) {
                matched = true;
            } else if (username != null && username.equalsIgnoreCase(customerName)) {
                matched = true;
            } else if (customerName.contains("齐") && (nickname != null && nickname.contains("齐"))) {
                matched = true;
            } else if (customerName.contains("陶") && (nickname != null && nickname.contains("陶"))) {
                matched = true;
            }
            
            if (matched) {
                // 更新external_user_id
                if (user.getExternalUserId() == null || !user.getExternalUserId().equals(externalUserId)) {
                    user.setExternalUserId(externalUserId);
                    userMapper.updateById(user);
                    log.info("已更新用户 {} 的external_user_id: {}", user.getNickname() || user.getUsername(), externalUserId);
                }
                break;
            }
        }
    }
}
