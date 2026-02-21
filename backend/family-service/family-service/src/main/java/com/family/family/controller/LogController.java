package com.family.family.controller;

import com.family.common.core.Result;
import com.family.family.entity.OperationLog;
import com.family.family.mapper.OperationLogMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 日志管理控制器
 */
@RestController
@RequestMapping("/api/log")
@RequiredArgsConstructor
@Tag(name = "日志管理", description = "操作日志查询接口")
public class LogController {
    
    private final OperationLogMapper logMapper;
    
    /**
     * 查询操作日志列表
     */
    @GetMapping("/list")
    @Operation(summary = "查询操作日志", description = "分页查询操作日志列表")
    public Result<List<OperationLog>> list(
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "模块") @RequestParam(required = false) String module,
            @Parameter(description = "状态：0失败 1成功") @RequestParam(required = false) Integer status) {
        
        List<OperationLog> logs = logMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<OperationLog>()
                .eq(userId != null, OperationLog::getUserId, userId)
                .eq(module != null, OperationLog::getModule, module)
                .eq(status != null, OperationLog::getStatus, status)
                .orderByDesc(OperationLog::getCreateTime)
                .last("LIMIT 100")
        );
        return Result.success(logs);
    }
    
    /**
     * 获取日志详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取日志详情", description = "根据ID获取日志详情")
    public Result<OperationLog> getById(@Parameter(description = "日志ID") @PathVariable Long id) {
        return Result.success(logMapper.selectById(id));
    }
}