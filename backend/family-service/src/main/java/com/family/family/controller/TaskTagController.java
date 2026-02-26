package com.family.family.controller;
import cn.dev33.satoken.annotation.SaCheckLogin;
import com.family.common.core.Result;
import com.family.family.entity.Task;
import com.family.family.entity.TaskTag;
import com.family.family.entity.TaskTagRelation;
import com.family.family.mapper.TaskMapper;
import com.family.family.mapper.TaskTagMapper;
import com.family.family.mapper.TaskTagRelationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 任务标签控制器
 */
@RestController
@RequestMapping("/api/task")
@SaCheckLogin
@RequiredArgsConstructor
public class TaskTagController {
    
    private final TaskTagMapper tagMapper;
    private final TaskTagRelationMapper relationMapper;
    private final TaskMapper taskMapper;
    
    /**
     * 创建标签
     * POST /api/task/tags
     */
    @PostMapping("/tags")
    public Result<Long> createTag(@RequestBody TaskTag tag) {
        tag.setCreateTime(LocalDateTime.now());
        tag.setUpdateTime(LocalDateTime.now());
        tagMapper.insert(tag);
        return Result.success(tag.getId());
    }
    
    /**
     * 更新标签
     * PUT /api/task/tags/{id}
     */
    @PutMapping("/tags/{id}")
    public Result<Boolean> updateTag(@PathVariable Long id, @RequestBody TaskTag tag) {
        tag.setId(id);
        tag.setUpdateTime(LocalDateTime.now());
        tagMapper.updateById(tag);
        return Result.success(true);
    }
    
    /**
     * 删除标签
     * DELETE /api/task/tags/{id}
     */
    @DeleteMapping("/tags/{id}")
    @Transactional
    public Result<Boolean> deleteTag(@PathVariable Long id) {
        // 删除标签关联
        relationMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TaskTagRelation>()
            .eq(TaskTagRelation::getTagId, id));
        // 删除标签
        tagMapper.deleteById(id);
        return Result.success(true);
    }
    
    /**
     * 获取标签列表
     * GET /api/task/tags
     */
    @GetMapping("/tags")
    public Result<List<TaskTag>> getTags(@RequestParam Long familyId) {
        List<TaskTag> tags = tagMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TaskTag>()
                .eq(TaskTag::getFamilyId, familyId)
                .orderByAsc(TaskTag::getSortOrder)
        );
        return Result.success(tags);
    }
    
    /**
     * 获取标签详情
     * GET /api/task/tags/{id}
     */
    @GetMapping("/tags/{id}")
    public Result<TaskTag> getTag(@PathVariable Long id) {
        TaskTag tag = tagMapper.selectById(id);
        return Result.success(tag);
    }
    
    /**
     * 为任务添加标签
     * POST /api/task/{taskId}/tags/{tagId}
     */
    @PostMapping("/{taskId}/tags/{tagId}")
    public Result<Boolean> addTagToTask(@PathVariable Long taskId, @PathVariable Long tagId) {
        TaskTagRelation relation = new TaskTagRelation();
        relation.setTaskId(taskId);
        relation.setTagId(tagId);
        relationMapper.insert(relation);
        return Result.success(true);
    }
    
    /**
     * 为任务批量添加标签
     * POST /api/task/{taskId}/tags
     */
    @PostMapping("/{taskId}/tags")
    @Transactional
    public Result<Boolean> addTagsToTask(@PathVariable Long taskId, @RequestBody List<Long> tagIds) {
        // 先删除原有标签关联
        relationMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TaskTagRelation>()
            .eq(TaskTagRelation::getTaskId, taskId));
        
        // 添加新标签
        for (Long tagId : tagIds) {
            TaskTagRelation relation = new TaskTagRelation();
            relation.setTaskId(taskId);
            relation.setTagId(tagId);
            relationMapper.insert(relation);
        }
        return Result.success(true);
    }
    
    /**
     * 移除任务的标签
     * DELETE /api/task/{taskId}/tags/{tagId}
     */
    @DeleteMapping("/{taskId}/tags/{tagId}")
    public Result<Boolean> removeTagFromTask(@PathVariable Long taskId, @PathVariable Long tagId) {
        relationMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TaskTagRelation>()
            .eq(TaskTagRelation::getTaskId, taskId)
            .eq(TaskTagRelation::getTagId, tagId));
        return Result.success(true);
    }
    
    /**
     * 获取任务的标签列表
     * GET /api/task/{taskId}/tags
     */
    @GetMapping("/{taskId}/tags")
    public Result<List<TaskTag>> getTaskTags(@PathVariable Long taskId) {
        List<Long> tagIds = relationMapper.selectTagIdsByTaskId(taskId);
        if (tagIds.isEmpty()) {
            return Result.success(List.of());
        }
        
        List<TaskTag> tags = tagMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TaskTag>()
                .in(TaskTag::getId, tagIds)
        );
        return Result.success(tags);
    }
    
    /**
     * 按标签查询任务
     * GET /api/task/by-tag/{tagId}
     */
    @GetMapping("/by-tag/{tagId}")
    public Result<List<Task>> getTasksByTag(@PathVariable Long tagId) {
        List<Long> taskIds = relationMapper.selectTaskIdsByTagId(tagId);
        if (taskIds.isEmpty()) {
            return Result.success(List.of());
        }
        
        List<Task> tasks = taskMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Task>()
                .in(Task::getId, taskIds)
        );
        return Result.success(tasks);
    }
}
