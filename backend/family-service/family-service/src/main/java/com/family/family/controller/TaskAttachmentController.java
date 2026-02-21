package com.family.family.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.family.common.core.Result;
import com.family.family.entity.Task;
import com.family.family.entity.TaskAttachment;
import com.family.family.mapper.TaskAttachmentMapper;
import com.family.family.mapper.TaskMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务附件控制器
 */
@RestController
@RequestMapping("/api/task")
@SaCheckLogin
@RequiredArgsConstructor
public class TaskAttachmentController {
    
    private final TaskAttachmentMapper attachmentMapper;
    private final TaskMapper taskMapper;
    
    /**
     * 添加附件
     * POST /api/task/{taskId}/attachments
     */
    @PostMapping("/{taskId}/attachments")
    public Result<Long> addAttachment(@PathVariable Long taskId, @RequestBody AttachmentRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        // 验证任务
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            return Result.error("任务不存在");
        }
        
        TaskAttachment attachment = new TaskAttachment();
        attachment.setTaskId(taskId);
        attachment.setUploaderId(userId);
        attachment.setFileName(request.getFileName());
        attachment.setFileUrl(request.getFileUrl());
        attachment.setFileSize(request.getFileSize());
        attachment.setFileType(request.getFileType());
        attachment.setCreateTime(LocalDateTime.now());
        
        attachmentMapper.insert(attachment);
        return Result.success(attachment.getId());
    }
    
    /**
     * 获取任务附件列表
     * GET /api/task/{taskId}/attachments
     */
    @GetMapping("/{taskId}/attachments")
    public Result<List<TaskAttachment>> getAttachments(@PathVariable Long taskId) {
        List<TaskAttachment> attachments = attachmentMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TaskAttachment>()
                .eq(TaskAttachment::getTaskId, taskId)
                .orderByDesc(TaskAttachment::getCreateTime)
        );
        return Result.success(attachments);
    }
    
    /**
     * 删除附件
     * DELETE /api/task/attachments/{id}
     */
    @DeleteMapping("/attachments/{id}")
    public Result<Boolean> deleteAttachment(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        TaskAttachment attachment = attachmentMapper.selectById(id);
        if (attachment == null) {
            return Result.error("附件不存在");
        }
        
        // 只有上传者可以删除
        if (!attachment.getUploaderId().equals(userId)) {
            return Result.error(403, "无权删除此附件");
        }
        
        attachmentMapper.deleteById(id);
        return Result.success(true);
    }
    
    /**
     * 附件请求
     */
    public static class AttachmentRequest {
        private String fileName;
        private String fileUrl;
        private Long fileSize;
        private String fileType;
        
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        
        public String getFileUrl() { return fileUrl; }
        public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
        
        public Long getFileSize() { return fileSize; }
        public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
        
        public String getFileType() { return fileType; }
        public void setFileType(String fileType) { this.fileType = fileType; }
    }
}
