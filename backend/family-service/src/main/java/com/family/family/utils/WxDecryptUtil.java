package com.family.family.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 微信数据解密工具类
 */
@Slf4j
@Component
public class WxDecryptUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 通过 code 获取 openid 和 session_key
     */
    public static String getOpenidAndSessionKey(String appId, String appSecret, String code) {
        try {
            // 参数校验
            if (appId == null || appId.isEmpty()) {
                log.error("微信登录失败: appId 为空");
                return null;
            }
            if (appSecret == null || appSecret.isEmpty()) {
                log.error("微信登录失败: appSecret 为空");
                return null;
            }
            if (code == null || code.isEmpty()) {
                log.error("微信登录失败: code 为空");
                return null;
            }
            
            log.info("微信登录请求: appId={}, code={}", appId, code.substring(0, Math.min(code.length(), 10)) + "...");
            
            String url = "https://api.weixin.qq.com/sns/jscode2session" +
                    "?appid=" + appId +
                    "&secret=" + appSecret +
                    "&js_code=" + code +
                    "&grant_type=authorization_code";
            
            java.net.URL urlObj = new java.net.URL(url);
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) urlObj.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.connect();
            
            int responseCode = connection.getResponseCode();
            log.info("微信登录 HTTP 响应码: {}", responseCode);
            
            StringBuilder response = new StringBuilder();
            try (java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }
            connection.disconnect();
            
            String resp = response.toString();
            log.info("微信登录响应: {}", resp);
            
            JsonNode json = objectMapper.readTree(resp);
            
            if (json.has("openid")) {
                String openid = json.get("openid").asText();
                String sessionKey = json.has("session_key") ? json.get("session_key").asText() : "";
                log.info("微信登录成功: openid={}", openid);
                return openid + "," + sessionKey;
            } else if (json.has("errcode")) {
                int errCode = json.get("errcode").asInt();
                String errMsg = json.has("errmsg") ? json.get("errmsg").asText() : "未知错误";
                log.error("微信登录失败: errcode={}, errmsg={}", errCode, errMsg);
            } else {
                log.error("微信登录响应异常: {}", resp);
            }
            
            return null;
            
        } catch (Exception e) {
            log.error("获取微信 openid 失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 解密手机号
     */
    public static String decryptPhoneNumber(String encryptedData, String sessionKey, String iv) {
        try {
            byte[] dataByte = Base64.getDecoder().decode(encryptedData);
            byte[] sessionKeyByte = Base64.getDecoder().decode(sessionKey);
            byte[] ivByte = Base64.getDecoder().decode(iv);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec skeySpec = new SecretKeySpec(sessionKeyByte, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(ivByte);
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivSpec);
            byte[] result = cipher.doFinal(dataByte);

            if (null != result && result.length > 0) {
                String resultStr = new String(result, StandardCharsets.UTF_8);
                JsonNode json = objectMapper.readTree(resultStr);
                return json.get("phoneNumber").asText();
            }
            return null;
        } catch (Exception e) {
            log.error("解密手机号失败", e);
            return null;
        }
    }
}
