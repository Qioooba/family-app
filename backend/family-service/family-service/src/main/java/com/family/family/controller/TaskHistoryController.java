package com.family.family.controller;
import cn.dev33.satoken.annotation.SaCheckLogin;
import com.family.common.core.Result;
import com.family.family.entity.TaskHistory;
import com.family.family.mapper.TaskHistoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 任务历史控制器
 */
@RestController
@RequestMapping("/api/task")
@SaCheckLogin
@RequiredArgsConstructor
public class TaskHistoryController {
    
    private final TaskHistoryMapper historyMapper;
    
    /**
     * 获取任务操作历史
     * GET /api/task/{taskId}/history
     */
    @GetMapping("/{taskId}/history")
    public Result<List<TaskHistory>> getTaskHistory(@PathVariable Long taskId) {
        List<TaskHistory> history = historyMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TaskHistory>()
                .eq(TaskHistory::getTaskId, taskId)
                .orderByDesc(TaskHistory::getCreateTime)
        );
        return Result.success(history);
    }
    
    /**
     * 获取操作类型列表（用于筛选）
     * GET /api/task/history/actions
     */
    @GetMapping("/history/actions")
    public Result<List<String>> getActionTypes() {
        return Result.success(List.of("create", "update", "delete", "complete", "assign", "comment", "attach"));
    }
}
