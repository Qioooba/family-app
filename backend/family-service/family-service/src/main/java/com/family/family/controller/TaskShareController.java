package com.family.family.controller;
import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;import com.family.common.core.Result;
import com.family.family.entity.Task;
import com.family.family.mapper.TaskMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 任务分享控制器
 */
@RestController
@RequestMapping("/api/task")
@SaCheckLogin
@RequiredArgsConstructor
public class TaskShareController {
    
    private final TaskMapper taskMapper;
    
    /**
     * 生成任务分享链接
     * POST /api/task/{id}/share
     */
    @PostMapping("/{id}/share")
    public Result<Map<String, String>> generateShareLink(@PathVariable Long id) {
        Task task = taskMapper.selectById(id);
        if (task == null) {
            return Result.error("任务不存在");
        }
        
        // 生成分享token
        String shareToken = UUID.randomUUID().toString().replace("-", "");
        
        Map<String, String> result = new HashMap<>();
        result.put("shareToken", shareToken);
        result.put("shareUrl", "/api/task/share/" + shareToken);
        result.put("expireTime", LocalDateTime.now().plusDays(7).toString());
        result.put("taskTitle", task.getTitle());
        
        return Result.success(result);
    }
    
    /**
     * 通过分享链接查看任务
     * GET /api/task/share/{token}
     */
    @GetMapping("/share/{token}")
    public Result<Map<String, Object>> getSharedTask(@PathVariable String token) {
        // 实际实现需要验证token并查询任务
        Map<String, Object> result = new HashMap<>();
        result.put("shareToken", token);
        result.put("isValid", true);
        result.put("task", null); // 实际查询任务
        result.put("expireTime", LocalDateTime.now().plusDays(7).toString());
        
        return Result.success(result);
    }
    
    /**
     * 取消分享
     * DELETE /api/task/{id}/share
     */
    @DeleteMapping("/{id}/share")
    public Result<Boolean> cancelShare(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        Task task = taskMapper.selectById(id);
        
        if (task == null) {
            return Result.error("任务不存在");
        }
        
        if (!task.getCreatorId().equals(userId)) {
            return Result.error(403, "无权取消分享");
        }
        
        // 实际实现需要删除分享记录
        return Result.success(true);
    }
}
