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

import jakarta.annotation.PostConstruct;

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

    @Autowired
    private MiniAppUrlLinkService miniAppUrlLinkService;

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

        // 优先使用内部成员推送（自动发送，无需确认）
        if (identity.hasWorkUserId()) {
            try {
                sendToInternalUser(token, identity.workUserId, message);
                return;
            } catch (Exception e) {
                log.warn("内部成员推送失败，尝试外部联系人, userId={}, error={}", message.getTargetUserId(), e.getMessage());
                // 如果内部推送失败，且有外部ID，尝试外部推送
                if (identity.hasExternalUserId()) {
                    sendToExternalUser(token, identity.externalUserId, message);
                    return;
                }
                throw e;
            }
        }
        
        // 只有外部联系人ID（需要管理员确认）
        if (identity.hasExternalUserId()) {
            sendToExternalUser(token, identity.externalUserId, message);
        }
    }

    /**
     * 发送给企业内部成员（使用mpnews图文消息，支持内嵌小程序码图片）
     */
    private void sendToInternalUser(String token, String workUserId, WechatMessage message) throws Exception {
        // 首先尝试发送mpnews图文消息（带小程序码）
        try {
            sendMpnewsMessage(token, workUserId, message);
            return;
        } catch (Exception e) {
            log.warn("mpnews图文消息发送失败，回退到news: {}", e.getMessage());
        }
        
        // 回退到普通news图文消息
        String url = "https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=" + token;

        Map<String, Object> payload = new HashMap<>();
        payload.put("touser", workUserId);
        payload.put("agentid", getAgentId());
        
        // 使用news图文消息
        Map<String, Object> news = new HashMap<>();
        List<Map<String, String>> articles = new ArrayList<>();
        
        Map<String, String> article = new HashMap<>();
        article.put("title", message.getTitle());
        article.put("description", buildRichMessageContent(message));
        article.put("url", "https://qioba.cn:8443");
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
     * 发送mpnews图文消息（富媒体，支持内嵌图片）
     */
    private void sendMpnewsMessage(String token, String workUserId, WechatMessage message) throws Exception {
        // 1. 上传小程序码图片获取media_id（用于封面）
        byte[] imageData = readMiniappQrImage();
        if (imageData == null) {
            throw new Exception("读取小程序码图片失败");
        }
        
        String mediaId = uploadMiniappQrImage(token, imageData);
        if (mediaId == null) {
            throw new Exception("上传小程序码图片失败");
        }
        
        String url = "https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=" + token;
        
        // 构建图文内容
        String timeStr = java.time.LocalDateTime.now().toString().substring(0, 16);
        String digest = buildMessageDigest(message, timeStr);
        // content中使用外部URL引用图片
        String content = buildMpnewsContentWithImageUrl(message, timeStr);
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("touser", workUserId);
        payload.put("agentid", getAgentId());
        payload.put("msgtype", "mpnews");
        
        Map<String, Object> mpnews = new HashMap<>();
        List<Map<String, Object>> articles = new ArrayList<>();
        
        Map<String, Object> article = new HashMap<>();
        article.put("title", message.getTitle());
        article.put("thumb_media_id", mediaId);
        article.put("author", "家庭小程序");
        article.put("content_source_url", "https://qioba.cn:8443");
        article.put("content", content);
        article.put("digest", digest);
        
        articles.add(article);
        mpnews.put("articles", articles);
        payload.put("mpnews", mpnews);
        
        String response = restTemplate.postForObject(url, payload, String.class);
        Map<String, Object> result = objectMapper.readValue(response, Map.class);
        
        int errcode = (Integer) result.getOrDefault("errcode", -1);
        if (errcode != 0) {
            throw new Exception("mpnews发送失败: " + result.get("errmsg"));
        }
        
        log.info("企业微信mpnews图文消息发送成功, userId={}, workId={}", 
                 message.getTargetUserId(), workUserId);
    }
    
    /**
     * 读取小程序码图片
     */
    private byte[] readMiniappQrImage() {
        String[] paths = {
            "/Volumes/document/Projects/family-app/backend/family-service/src/main/resources/static/miniapp-qr.png",
            "/app/static/miniapp-qr.png",
            "/static/miniapp-qr.png",
            "classpath:/static/miniapp-qr.png"
        };
        
        for (String path : paths) {
            try {
                if (path.startsWith("classpath:")) {
                    try (var is = getClass().getResourceAsStream("/static/miniapp-qr.png")) {
                        if (is != null) {
                            return is.readAllBytes();
                        }
                    }
                } else {
                    java.io.File file = new java.io.File(path);
                    if (file.exists()) {
                        return java.nio.file.Files.readAllBytes(file.toPath());
                    }
                }
            } catch (Exception e) {
                log.debug("尝试读取图片失败: {}", path);
            }
        }
        log.error("无法找到小程序码图片");
        return null;
    }
    
    /**
     * 上传小程序码图片到企业微信获取media_id
     */
    private String uploadMiniappQrImage(String token, byte[] imageData) {
        try {
            String uploadUrl = "https://qyapi.weixin.qq.com/cgi-bin/media/upload?access_token=" + token + "&type=image";
            
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.MULTIPART_FORM_DATA);
            
            org.springframework.core.io.ByteArrayResource resource = new org.springframework.core.io.ByteArrayResource(imageData) {
                @Override
                public String getFilename() {
                    return "miniapp-qr.png";
                }
            };
            
            org.springframework.util.MultiValueMap<String, Object> body = new org.springframework.util.LinkedMultiValueMap<>();
            body.add("media", resource);
            
            org.springframework.http.HttpEntity<org.springframework.util.MultiValueMap<String, Object>> requestEntity = 
                new org.springframework.http.HttpEntity<>(body, headers);
            
            org.springframework.http.ResponseEntity<String> response = restTemplate.postForEntity(
                uploadUrl, requestEntity, String.class);
            
            Map<String, Object> result = objectMapper.readValue(response.getBody(), Map.class);
            
            if (result.get("media_id") != null) {
                String mediaId = (String) result.get("media_id");
                log.debug("上传图片成功, media_id={}", mediaId);
                return mediaId;
            } else {
                log.error("上传图片失败: {}", result.get("errmsg"));
            }
        } catch (Exception e) {
            log.error("上传小程序码图片异常", e);
        }
        return null;
    }
    
    /**
     * 构建mpnews消息内容（简洁紧凑设计）
     */
    private String buildMpnewsContentWithImageUrl(WechatMessage message, String timeStr) {
        StringBuilder content = new StringBuilder();
        
        // 解析description获取任务信息
        String desc = message.getDescription();
        String[] lines = desc.split("\n");
        
        // 提取信息
        String taskName = extractFromLines(lines, "任务[:：]");
        if (taskName.isEmpty()) {
            taskName = message.getTitle().replaceAll("^[✅🏠🎉✨📱\\s]+", "").replaceAll(".*[：:]", "").trim();
        }
        String remark = extractFromLines(lines, "备注[:：]");
        String assigner = extractFromLines(lines, "指派[人]?[：:]");
        String executor = extractFromLines(lines, "执行[人]?[：:]");
        
        // 简洁白色卡片设计
        content.append("<div style='font-family: -apple-system, BlinkMacSystemFont, sans-serif; background: #fff; padding: 16px;'>")
               
               // 标题行
               .append("<div style='border-bottom: 1px solid #eee; padding-bottom: 12px; margin-bottom: 12px;'>")
               .append("<div style='font-size: 17px; font-weight: 600; color: #333; margin-bottom: 4px;'>📋 ").append(escapeHtml(taskName)).append("</div>")
               .append("<div style='font-size: 13px; color: #999;'>").append(timeStr).append("</div>")
               .append("</div>")
               
               // 备注（如果有）
               .append(!remark.isEmpty() ? 
                   "<div style='background: #f5f5f5; padding: 10px 12px; border-radius: 6px; margin-bottom: 12px; font-size: 14px; color: #666;'>" +
                   "📝 " + escapeHtml(remark) + "</div>" : "")
               
               // 人员信息一行显示
               .append("<div style='display: flex; gap: 20px; margin-bottom: 16px; font-size: 14px;'>")
               .append(!assigner.isEmpty() && !assigner.equals("系统") ? 
                   "<div><span style='color: #999;'>指派人：</span><span style='color: #333; font-weight: 500;'>" + escapeHtml(assigner) + "</span></div>" : "")
               .append(!executor.isEmpty() ? 
                   "<div><span style='color: #999;'>执行人：</span><span style='color: #333; font-weight: 500;'>" + escapeHtml(executor) + "</span></div>" : "")
               .append("</div>")
               
               // 小程序码（紧凑）
               .append("<div style='text-align: center; padding-top: 12px; border-top: 1px dashed #ddd;'>")
               .append("<img src=\"https://qioba.cn:8443/miniapp-qr.png\" style='width: 150px; height: 150px; border-radius: 8px;' alt='小程序码'/>")
               .append("<div style='margin-top: 8px; font-size: 12px; color: #999;'>长按识别小程序码</div>")
               .append("</div>")
               
               .append("</div>");
        
        return content.toString();
    }
    
    /**
     * 从行数组中提取指定前缀的内容
     */
    private String extractFromLines(String[] lines, String pattern) {
        for (String line : lines) {
            String trimmedLine = line.trim();
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(".*" + pattern + "\\s*(.+)", java.util.regex.Pattern.UNICODE_CHARACTER_CLASS);
            java.util.regex.Matcher m = p.matcher(trimmedLine);
            if (m.matches()) {
                return m.group(1).trim();
            }
        }
        return "";
    }
    
    /**
     * 提取操作人（执行人或指派人）
     */
    private String extractOperatorFromLines(String[] lines) {
        // 先尝试匹配"执行人"
        String operator = extractFromLines(lines, "执行[人]?[：:]");
        if (!operator.isEmpty()) {
            return operator;
        }
        // 再尝试匹配"指派人"
        operator = extractFromLines(lines, "指派[人]?[：:]");
        if (!operator.isEmpty()) {
            return operator;
        }
        return "";
    }
    
    /**
     * HTML转义
     */
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#x27;");
    }
    
    /**
     * 构建mpnews消息摘要
     */
    private String buildMessageDigest(WechatMessage message, String timeStr) {
        StringBuilder sb = new StringBuilder();
        
        // 解析description获取任务信息
        String[] lines = message.getDescription().split("\n");
        String taskName = extractFromLines(lines, "任务[:：]");
        if (taskName.isEmpty()) {
            taskName = message.getTitle().replaceAll(".*[：:]", "").trim();
        }
        String assigner = extractOperatorFromLines(lines);
        if (assigner.isEmpty()) {
            assigner = "系统";
        }
        String remark = extractFromLines(lines, "备注[:：]");
        
        switch (message.getType()) {
            case TASK_ASSIGNED:
                sb.append("📋 任务：").append(taskName).append("\n");
                if (!remark.isEmpty()) {
                    sb.append("📝 备注：").append(remark).append("\n");
                }
                sb.append("👤 指派人：").append(assigner).append("\n");
                sb.append("📅 时间：").append(timeStr).append("\n\n");
                sb.append("📱 请长按小程序码查看");
                break;
            case TASK_ASSIGN_NOTIFY:
                sb.append("📋 任务：").append(taskName).append("\n");
                sb.append("👤 执行人：").append(assigner).append("\n");
                sb.append("📅 时间：").append(timeStr);
                break;
            case TASK_COMPLETED:
                sb.append("📋 任务：").append(taskName).append("\n");
                sb.append("👤 完成人：").append(assigner).append("\n");
                sb.append("📅 时间：").append(timeStr);
                break;
            default:
                sb.append(message.getDescription().replaceAll("<[^>]*>", ""));
        }
        
        return sb.toString();
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

        // 生成小程序 URL Link
        String miniAppLink = generateMiniAppLink(message);
        sb.append("📱 点击打开小程序：").append(miniAppLink);

        return sb.toString();
    }

    /**
     * 生成小程序跳转链接
     */
    private String generateMiniAppLink(WechatMessage message) {
        // 从消息URL中提取路径
        String path = message.getUrl();
        String query = "";

        if (path != null && !path.isEmpty()) {
            // 去掉开头的 /
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            // 分离路径和参数
            int queryIndex = path.indexOf("?");
            if (queryIndex > 0) {
                query = path.substring(queryIndex + 1);
                path = path.substring(0, queryIndex);
            }
        } else {
            path = "pages/home/index";
        }

        // 默认返回免密登录链接（如果URL Link生成失败）
        String tempToken = tempTokenUtil.generateTempToken(message.getTargetUserId());
        String fallbackUrl = String.format("https://qioba.cn:8443/auto-login.html?token=%s", tempToken);

        try {
            // 尝试生成小程序 URL Link
            String urlLink = miniAppUrlLinkService.generateUrlLink(path, query, 7 * 24 * 3600);
            if (urlLink != null && !urlLink.equals(fallbackUrl)) {
                log.debug("生成小程序URL Link成功: {}", urlLink);
                return urlLink;
            }
        } catch (Exception e) {
            log.warn("生成小程序URL Link失败，使用备用链接", e);
        }

        return fallbackUrl;
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
                desc.append("👥 执行人：").append(assigneeName).append("\n");
                desc.append("📅 时间：").append(java.time.LocalDateTime.now().toString().substring(0, 16));
                break;
                
            case TASK_ASSIGN_NOTIFY:
                title = "✅ 任务已指派";
                desc.append("📋 任务：").append(taskTitle).append("\n\n");
                if (taskContent != null && !taskContent.isEmpty()) {
                    desc.append("📝 备注：").append(taskContent).append("\n\n");
                }
                desc.append("👤 指派人：").append(operatorName).append("\n");
                desc.append("👥 执行人：").append(assigneeName).append("\n");
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
        // 使用新的统一格式构造消息
        String timeStr = java.time.LocalDateTime.now().toString().substring(0, 16);
        StringBuilder desc = new StringBuilder();
        desc.append("📋 任务：").append(taskTitle).append("\n\n");
        desc.append("👤 执行人：").append(toName).append("\n");
        desc.append("📅 时间：").append(timeStr);
        
        WechatMessage msg = new WechatMessage();
        msg.setType(MessageType.TASK_ASSIGN_NOTIFY);
        msg.setTargetUserId(creatorId);
        msg.setTitle("✅ 任务已指派");
        msg.setDescription(desc.toString());
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
                    log.info("已更新用户 {} 的external_user_id: {}", user.getNickname() != null ? user.getNickname() : user.getUsername(), externalUserId);
                }
                break;
            }
        }
    }
}
