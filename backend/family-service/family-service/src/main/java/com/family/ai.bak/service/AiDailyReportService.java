package com.family.ai.service;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.family.ai.config.AiConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * AI日报服务
 */
@Service
public class AiDailyReportService {
    
    private static final Logger log = LoggerFactory.getLogger(AiDailyReportService.class);
    
    private final AiConfig aiConfig;
    
    public AiDailyReportService(AiConfig aiConfig) {
        this.aiConfig = aiConfig;
    }
    
    /**
     * 生成早安日报
     */
    public String generateMorningReport(Long userId, String userName, LocalDate date) {
        String prompt = String.format("""
                请为 %s 生成一份早安日报，日期是 %s。
                
                日报应包含以下内容：
                1. 温馨问候语
                2. 今日待办提醒（如果有）
                3. 今日天气穿衣建议（假设晴天，15-22°C）
                4. 今日菜谱推荐（简单家常菜）
                5. 正能量祝福
                
                格式要简洁友好，适合在小程序中展示。
                """, userName, date.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")));
        
        return callAiApi(prompt);
    }
    
    /**
     * 生成晚安总结
     */
    public String generateEveningSummary(Long userId, String userName, LocalDate date) {
        String prompt = String.format("""
                请为 %s 生成一份晚安总结，回顾 %s 这一天。
                
                总结应包含：
                1. 温馨晚安问候
                2. 今日完成情况鼓励
                3. 明日待办预览
                4. 健康睡眠建议
                5. 晚安祝福
                
                语气要温暖鼓励。
                """, userName, date.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")));
        
        return callAiApi(prompt);
    }
    
    /**
     * 菜谱推荐
     */
    public String recommendRecipe(String ingredients, String preference) {
        String prompt = String.format("""
                请根据以下信息推荐一道家常菜：
                
                现有食材：%s
                口味偏好：%s
                
                请推荐一道菜，包含：
                1. 菜名
                2. 所需食材清单
                3. 简单烹饪步骤（3-5步）
                4. 预计烹饪时间
                5. 营养小贴士
                """, ingredients, preference);
        
        return callAiApi(prompt);
    }
    
    /**
     * 营养分析
     */
    public String analyzeNutrition(String todayDiet) {
        String prompt = String.format("""
                请分析以下今日饮食的营养情况：
                
                今日饮食：%s
                
                请给出：
                1. 营养摄入总体评价
                2. 优点
                3. 改进建议
                4. 明日饮食建议
                
                要专业但易懂。
                """, todayDiet);
        
        return callAiApi(prompt);
    }
    
    /**
     * 调用AI API
     */
    private String callAiApi(String prompt) {
        try {
            JSONArray messages = new JSONArray();
            
            JSONObject userMsg = new JSONObject();
            userMsg.set("role", "user");
            userMsg.set("content", prompt);
            messages.add(userMsg);
            
            JSONObject requestBody = new JSONObject();
            requestBody.set("model", aiConfig.getDeepseek().getModel());
            requestBody.set("messages", messages);
            requestBody.set("temperature", 0.7);
            requestBody.set("max_tokens", 1500);
            
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
                    return choices.getJSONObject(0).getJSONObject("message").getStr("content");
                }
            }
        } catch (Exception e) {
            log.error("AI API调用失败", e);
        }
        
        return "生成失败，请稍后重试";
    }
}
