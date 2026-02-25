package com.family.ai.service;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.family.ai.config.AiConfig;
import com.family.ai.entity.ChatHistory;
import com.family.ai.mapper.ChatHistoryMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * AI对话服务
 */
@Service
public class AiChatService {
    
    private static final Logger log = LoggerFactory.getLogger(AiChatService.class);
    
    private final AiConfig aiConfig;
    private final ChatHistoryMapper chatHistoryMapper;
    
    public AiChatService(AiConfig aiConfig, ChatHistoryMapper chatHistoryMapper) {
        this.aiConfig = aiConfig;
        this.chatHistoryMapper = chatHistoryMapper;
    }
    
    /**
     * 发送对话消息
     */
    public String chat(Long userId, Long familyId, String message, String sessionId) {
        if (sessionId == null) {
            sessionId = UUID.randomUUID().toString().replace("-", "");
        }
        
        // 保存用户消息
        saveChatHistory(userId, familyId, sessionId, "user", message, null, null);
        
        // 获取历史上下文
        List<ChatHistory> history = chatHistoryMapper.selectBySessionId(sessionId);
        
        // 调用DeepSeek API
        String reply = callDeepSeekApi(history, message);
        
        // 保存AI回复
        saveChatHistory(userId, familyId, sessionId, "assistant", reply, null, null);
        
        return reply;
    }
    
    /**
     * 调用DeepSeek API
     */
    private String callDeepSeekApi(List<ChatHistory> history, String currentMessage) {
        try {
            // 构建消息列表
            JSONArray messages = new JSONArray();
            
            // 添加系统提示词
            JSONObject systemMsg = new JSONObject();
            systemMsg.set("role", "system");
            systemMsg.set("content", getSystemPrompt());
            messages.add(systemMsg);
            
            // 添加历史对话
            for (ChatHistory h : history) {
                JSONObject msg = new JSONObject();
                msg.set("role", h.getRole());
                msg.set("content", h.getContent());
                messages.add(msg);
            }
            
            // 构建请求体
            JSONObject requestBody = new JSONObject();
            requestBody.set("model", aiConfig.getDeepseek().getModel());
            requestBody.set("messages", messages);
            requestBody.set("temperature", aiConfig.getDeepseek().getTemperature());
            requestBody.set("max_tokens", aiConfig.getDeepseek().getMaxTokens());
            
            // 发送请求
            HttpResponse response = HttpRequest.post(aiConfig.getDeepseek().getApiUrl())
                    .header("Authorization", "Bearer " + aiConfig.getDeepseek().getApiKey())
                    .header("Content-Type", "application/json")
                    .body(requestBody.toString())
                    .timeout(30000)
                    .execute();
            
            if (response.getStatus() == 200) {
                JSONObject jsonResponse = JSONUtil.parseObj(response.body());
                JSONArray choices = jsonResponse.getJSONArray("choices");
                if (choices != null && !choices.isEmpty()) {
                    JSONObject choice = choices.getJSONObject(0);
                    JSONObject message = choice.getJSONObject("message");
                    return message.getStr("content");
                }
            } else {
                log.error("DeepSeek API调用失败: {}", response.body());
            }
        } catch (Exception e) {
            log.error("AI对话异常", e);
        }
        
        return "抱歉，我暂时无法回答，请稍后再试。";
    }
    
    /**
     * 获取系统提示词
     */
    private String getSystemPrompt() {
        return """
                你是一个智能家庭助手，可以帮助家庭成员管理日常生活。
                你的能力包括：
                1. 菜谱推荐 - 根据食材、口味偏好推荐菜谱
                2. 营养顾问 - 分析饮食健康，给出建议
                3. 购物助手 - 生成购物清单，食材替代建议
                4. 生活管家 - 提醒待办事项、纪念日、天气提醒
                5. 家庭关系 - 亲子活动建议、节日礼物推荐
                
                回答要友好、简洁、实用。如果不确定，就坦诚说明。
                """;
    }
    
    /**
     * 保存对话历史
     */
    private void saveChatHistory(Long userId, Long familyId, String sessionId, 
                                  String role, String content, String intent, String entities) {
        ChatHistory history = new ChatHistory();
        history.setUserId(userId);
        history.setFamilyId(familyId);
        history.setSessionId(sessionId);
        history.setRole(role);
        history.setContent(content);
        history.setIntent(intent);
        history.setEntities(entities);
        history.setCreateTime(java.time.LocalDateTime.now());
        chatHistoryMapper.insert(history);
    }
    
    /**
     * 获取会话历史
     */
    public List<ChatHistory> getChatHistory(String sessionId) {
        return chatHistoryMapper.selectBySessionId(sessionId);
    }
    
    /**
     * 生成新的会话ID
     */
    public String generateSessionId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
