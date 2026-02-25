package com.family.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI配置
 */
@Component
@ConfigurationProperties(prefix = "ai")
public class AiConfig {
    
    /**
     * DeepSeek API配置
     */
    private DeepSeek deepseek = new DeepSeek();
    
    /**
     * 文心一言配置
     */
    private Wenxin wenxin = new Wenxin();
    
    /**
     * 通义千问配置
     */
    private Qwen qwen = new Qwen();
    
    public DeepSeek getDeepseek() {
        return deepseek;
    }
    
    public void setDeepseek(DeepSeek deepseek) {
        this.deepseek = deepseek;
    }
    
    public Wenxin getWenxin() {
        return wenxin;
    }
    
    public void setWenxin(Wenxin wenxin) {
        this.wenxin = wenxin;
    }
    
    public Qwen getQwen() {
        return qwen;
    }
    
    public void setQwen(Qwen qwen) {
        this.qwen = qwen;
    }
    
    public static class DeepSeek {
        private String apiKey;
        private String apiUrl = "https://api.deepseek.com/v1/chat/completions";
        private String model = "deepseek-chat";
        private Double temperature = 0.7;
        private Integer maxTokens = 2000;
        
        public String getApiKey() {
            return apiKey;
        }
        
        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }
        
        public String getApiUrl() {
            return apiUrl;
        }
        
        public void setApiUrl(String apiUrl) {
            this.apiUrl = apiUrl;
        }
        
        public String getModel() {
            return model;
        }
        
        public void setModel(String model) {
            this.model = model;
        }
        
        public Double getTemperature() {
            return temperature;
        }
        
        public void setTemperature(Double temperature) {
            this.temperature = temperature;
        }
        
        public Integer getMaxTokens() {
            return maxTokens;
        }
        
        public void setMaxTokens(Integer maxTokens) {
            this.maxTokens = maxTokens;
        }
    }
    
    public static class Wenxin {
        private String apiKey;
        private String secretKey;
        private String apiUrl = "https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/completions";
        private String model = "eb-instant";
        
        public String getApiKey() {
            return apiKey;
        }
        
        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }
        
        public String getSecretKey() {
            return secretKey;
        }
        
        public void setSecretKey(String secretKey) {
            this.secretKey = secretKey;
        }
        
        public String getApiUrl() {
            return apiUrl;
        }
        
        public void setApiUrl(String apiUrl) {
            this.apiUrl = apiUrl;
        }
        
        public String getModel() {
            return model;
        }
        
        public void setModel(String model) {
            this.model = model;
        }
    }
    
    public static class Qwen {
        private String apiKey;
        private String apiUrl = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";
        private String model = "qwen-turbo";
        
        public String getApiKey() {
            return apiKey;
        }
        
        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }
        
        public String getApiUrl() {
            return apiUrl;
        }
        
        public void setApiUrl(String apiUrl) {
            this.apiUrl = apiUrl;
        }
        
        public String getModel() {
            return model;
        }
        
        public void setModel(String model) {
            this.model = model;
        }
    }
}
