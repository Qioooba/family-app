package com.family.family.controller;
import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;import com.family.common.core.Result;
import com.family.family.entity.TaskComment;
import com.family.family.mapper.TaskCommentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务评论控制器
 */
@RestController
@RequestMapping("/api/task")
@SaCheckLogin
@RequiredArgsConstructor
public class TaskCommentController {
    
    private final TaskCommentMapper commentMapper;
    
    /**
     * 添加评论
     * POST /api/task/{taskId}/comments
     */
    @PostMapping("/{taskId}/comments")
    public Result<Long> addComment(@PathVariable Long taskId, @RequestBody CommentRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        TaskComment comment = new TaskComment();
        comment.setTaskId(taskId);
        comment.setUserId(userId);
        comment.setContent(request.getContent());
        comment.setParentId(request.getParentId());
        comment.setReplyCount(0);
        comment.setCreateTime(LocalDateTime.now());
        comment.setUpdateTime(LocalDateTime.now());
        
        commentMapper.insert(comment);
        
        // 更新父评论的回复数
        if (request.getParentId() != null) {
            TaskComment parent = commentMapper.selectById(request.getParentId());
            if (parent != null) {
                parent.setReplyCount(parent.getReplyCount() + 1);
                commentMapper.updateById(parent);
            }
        }
        
        return Result.success(comment.getId());
    }
    
    /**
     * 获取任务评论列表
     * GET /api/task/{taskId}/comments
     */
    @GetMapping("/{taskId}/comments")
    public Result<List<TaskComment>> getComments(@PathVariable Long taskId) {
        List<TaskComment> comments = commentMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TaskComment>()
                .eq(TaskComment::getTaskId, taskId)
                .isNull(TaskComment::getParentId) // 只查一级评论
                .orderByDesc(TaskComment::getCreateTime)
        );
        return Result.success(comments);
    }
    
    /**
     * 获取评论的回复列表
     * GET /api/task/comments/{commentId}/replies
     */
    @GetMapping("/comments/{commentId}/replies")
    public Result<List<TaskComment>> getReplies(@PathVariable Long commentId) {
        List<TaskComment> replies = commentMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TaskComment>()
                .eq(TaskComment::getParentId, commentId)
                .orderByAsc(TaskComment::getCreateTime)
        );
        return Result.success(replies);
    }
    
    /**
     * 删除评论
     * DELETE /api/task/comments/{id}
     */
    @DeleteMapping("/comments/{id}")
    public Result<Boolean> deleteComment(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        TaskComment comment = commentMapper.selectById(id);
        
        if (comment == null) {
            return Result.error("评论不存在");
        }
        
        if (!comment.getUserId().equals(userId)) {
            return Result.error(403, "无权删除此评论");
        }
        
        commentMapper.deleteById(id);
        return Result.success(true);
    }
    
    /**
     * 评论请求
     */
    public static class CommentRequest {
        private String content;
        private Long parentId;
        
        public String getContent() {
            return content;
        }
        
        public void setContent(String content) {
            this.content = content;
        }
        
        public Long getParentId() {
            return parentId;
        }
        
        public void setParentId(Long parentId) {
            this.parentId = parentId;
        }
    }
}
