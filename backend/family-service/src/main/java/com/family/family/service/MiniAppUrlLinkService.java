package com.family.family.service;

import com.family.family.util.TempTokenUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信小程序URL Link服务
 * 用于生成可以直接打开小程序的链接
 */
@Slf4j
@Service
public class MiniAppUrlLinkService {

    @Autowired
    private SystemConfigService configService;

    @Autowired(required = false)
    private TempTokenUtil tempTokenUtil;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private String accessToken;
    private long tokenExpireTime;

    /**
     * 获取小程序access_token
     */
    private synchronized String getAccessToken() {
        if (accessToken != null && System.currentTimeMillis() < tokenExpireTime) {
            return accessToken;
        }

        String appId = configService.getValue("wechat.miniapp.appid", "wxbdc70536c5e52b82");
        String secret = configService.getValue("wechat.miniapp.secret", "");

        if (secret.isEmpty()) {
            log.warn("小程序secret未配置");
            return null;
        }

        try {
            String url = String.format(
                "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s",
                appId, secret
            );
            String response = restTemplate.getForObject(url, String.class);
            Map<String, Object> result = objectMapper.readValue(response, Map.class);

            if (result.get("access_token") != null) {
                accessToken = (String) result.get("access_token");
                tokenExpireTime = System.currentTimeMillis() + (7200 - 300) * 1000;
                return accessToken;
            }
        } catch (Exception e) {
            log.error("获取小程序access_token失败", e);
        }
        return null;
    }

    /**
     * 生成小程序URL Link
     * 该链接可以在微信内直接打开小程序
     * 
     * @param path 小程序页面路径
     * @param query 查询参数
     * @param expireTime 过期时间（秒），最大30天
     * @return URL Link
     */
    public String generateUrlLink(String path, String query, Integer expireTime) {
        String token = getAccessToken();
        if (token == null) {
            log.warn("无法获取小程序access_token，返回H5链接");
            return generateH5Link(path, query);
        }

        try {
            String url = "https://api.weixin.qq.com/wxa/generate_urllink?access_token=" + token;

            Map<String, Object> payload = new HashMap<>();
            payload.put("path", path);
            if (query != null && !query.isEmpty()) {
                payload.put("query", query);
            }
            // 云开发环境（如果有）
            payload.put("env_version", "release");
            // 过期时间
            if (expireTime != null) {
                payload.put("expire_type", 1); // 1: 指定过期时间
                payload.put("expire_interval", expireTime);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            String response = restTemplate.postForObject(url, request, String.class);
            Map<String, Object> result = objectMapper.readValue(response, Map.class);

            if (result.get("errcode") != null && (Integer) result.get("errcode") == 0) {
                String urlLink = (String) result.get("url_link");
                log.info("生成小程序URL Link成功: {}", urlLink);
                return urlLink;
            } else {
                log.warn("生成URL Link失败: {}", result.get("errmsg"));
            }
        } catch (Exception e) {
            log.error("生成小程序URL Link异常", e);
        }

        // 失败时返回H5链接
        return generateH5Link(path, query);
    }

    /**
     * 生成短链接（Scheme）
     */
    public String generateShortLink(String path, String query) {
        String token = getAccessToken();
        if (token == null) {
            return generateH5Link(path, query);
        }

        try {
            String url = "https://api.weixin.qq.com/wxa/genwxashortlink?access_token=" + token;

            Map<String, Object> payload = new HashMap<>();
            // 页面路径
            String fullPath = path;
            if (query != null && !query.isEmpty()) {
                fullPath += "?" + query;
            }
            payload.put("page_url", fullPath);
            payload.put("page_title", "家庭小程序");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            String response = restTemplate.postForObject(url, request, String.class);
            Map<String, Object> result = objectMapper.readValue(response, Map.class);

            if (result.get("errcode") != null && (Integer) result.get("errcode") == 0) {
                String shortLink = (String) result.get("link");
                log.info("生成小程序短链接成功: {}", shortLink);
                return shortLink;
            }
        } catch (Exception e) {
            log.error("生成小程序短链接异常", e);
        }

        return generateH5Link(path, query);
    }

    /**
     * 生成H5备用链接
     */
    private String generateH5Link(String path, String query) {
        String baseUrl = "https://qioba.cn:8443";
        String fullQuery = query != null && !query.isEmpty() ? "?" + query : "";
        return baseUrl + "/h5/" + fullQuery;
    }

    /**
     * 获取任务详情页的跳转链接
     *
     * @param taskId 任务ID
     * @param userId 用户ID（用于免密登录）
     * @return 可直接点击的链接
     */
    public String getTaskDetailLink(Long taskId, Long userId) {
        String path = "pages/task/detail";
        String query = "id=" + taskId + "&from=wechat";

        // 尝试生成小程序URL Link
        String urlLink = generateUrlLink(path, query, 7 * 24 * 3600); // 7天过期

        // 如果成功生成了URL Link，直接返回
        if (urlLink != null && urlLink.startsWith("http")) {
            return urlLink;
        }

        // 否则返回免密登录链接
        if (tempTokenUtil != null) {
            String tempToken = tempTokenUtil.generateTempToken(userId);
            return String.format("https://qioba.cn:8443/auto-login.html?token=%s&taskId=%s", tempToken, taskId);
        }

        return generateH5Link(path, query);
    }
}
