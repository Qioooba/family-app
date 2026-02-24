package com.family.common.controller;

import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", Map.of(
            "status", "UP",
            "timestamp", System.currentTimeMillis()
        ));
        return result;
    }

    /**
     * 服务信息
     */
    @GetMapping("/info")
    public Map<String, Object> info() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", Map.of(
            "name", "Family Service",
            "version", "1.0.0",
            "description", "家庭服务后端API"
        ));
        return result;
    }
}
