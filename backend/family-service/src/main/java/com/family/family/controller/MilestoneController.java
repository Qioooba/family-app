package com.family.family.controller;
import cn.dev33.satoken.annotation.SaCheckLogin;

import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 里程碑控制器
 */
@RestController
@RequestMapping("/api/milestone")
@SaCheckLogin
public class MilestoneController {

    @PutMapping("/{milestoneId}/complete")
    public Map<String, Object> complete(@PathVariable Long milestoneId) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        // TODO: 完成里程碑
        return result;
    }
    
    @DeleteMapping("/{milestoneId}")
    public Map<String, Object> delete(@PathVariable Long milestoneId) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        // TODO: 删除里程碑
        return result;
    }
}
