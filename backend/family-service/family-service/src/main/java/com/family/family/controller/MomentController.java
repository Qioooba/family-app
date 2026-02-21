package com.family.family.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.family.common.core.Result;
import com.family.family.entity.MomentComment;
import com.family.family.service.MomentService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 家庭圈动态控制器
 */
@RestController
@RequestMapping("/api/moments")
public class MomentController {
    
    private final MomentService momentService;
    
    public MomentController(MomentService momentService) {
        this.momentService = momentService;
    }
    
    @PostMapping("/create")
    public Result<Long> create(@RequestBody MomentCreateRequest request) {
        if (request.getFamilyId() == null) {
            return Result.error("家庭ID不能为空");
        }
        if (!StringUtils.hasText(request.getContent())) {
            return Result.error("内容不能为空");
        }
        if (request.getContent().length() > 2000) {
            return Result.error("内容不能超过2000字");
        }
        
        request.setContent(filterXss(request.getContent()));
        
        Long userId = StpUtil.getLoginIdAsLong();
        Long momentId = momentService.create(userId, request);
        return Result.success(momentId);
    }
    
    @GetMapping("/feed/{familyId}")
    public Result<Map<String, Object>> getFeed(
            @PathVariable Long familyId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        if (familyId == null || familyId <= 0) {
            return Result.error("无效的家庭ID");
        }
        if (page < 1) page = 1;
        if (size < 1 || size > 50) size = 10;
        
        return Result.success(momentService.getFeed(familyId, page, size));
    }
    
    @PostMapping("/{id}/like")
    public Result<Boolean> like(@PathVariable Long id) {
        if (id == null || id <= 0) {
            return Result.error("无效的动态ID");
        }
        
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(momentService.like(id, userId));
    }
    
    @PostMapping("/{id}/comment")
    public Result<Long> comment(@PathVariable Long id, @RequestBody CommentRequest request) {
        if (id == null || id <= 0) {
            return Result.error("无效的动态ID");
        }
        if (!StringUtils.hasText(request.getContent())) {
            return Result.error("评论内容不能为空");
        }
        if (request.getContent().length() > 500) {
            return Result.error("评论内容不能超过500字");
        }
        
        request.setContent(filterXss(request.getContent()));
        
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(momentService.comment(id, userId, request.getContent(), request.getReplyTo()));
    }
    
    @GetMapping("/{id}/comments")
    public Result<List<MomentComment>> getComments(@PathVariable Long id) {
        if (id == null || id <= 0) {
            return Result.error("无效的动态ID");
        }
        return Result.success(momentService.getComments(id));
    }
    
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        if (id == null || id <= 0) {
            return Result.error("无效的动态ID");
        }
        
        Long userId = StpUtil.getLoginIdAsLong();
        Boolean result = momentService.delete(id, userId);
        if (!result) {
            return Result.error("删除失败，无权限或动态不存在");
        }
        return Result.success(true);
    }
    
    private String filterXss(String content) {
        if (content == null) return null;
        return content
                .replaceAll("<<", "&lt;")
                .replaceAll(">>", "&gt;")
                .replaceAll("\"", "&quot;")
                .replaceAll("'", "&#x27;")
                .replaceAll("\\(", "&#40;")
                .replaceAll("\\)", "&#41;");
    }
    
    public static class MomentCreateRequest {
        private Long familyId;
        private String content;
        private String images;
        private String location;
        private String tags;
        private String mentions;
        
        public Long getFamilyId() {
            return familyId;
        }
        
        public void setFamilyId(Long familyId) {
            this.familyId = familyId;
        }
        
        public String getContent() {
            return content;
        }
        
        public void setContent(String content) {
            this.content = content;
        }
        
        public String getImages() {
            return images;
        }
        
        public void setImages(String images) {
            this.images = images;
        }
        
        public String getLocation() {
            return location;
        }
        
        public void setLocation(String location) {
            this.location = location;
        }
        
        public String getTags() {
            return tags;
        }
        
        public void setTags(String tags) {
            this.tags = tags;
        }
        
        public String getMentions() {
            return mentions;
        }
        
        public void setMentions(String mentions) {
            this.mentions = mentions;
        }
    }
    
    public static class CommentRequest {
        private String content;
        private Long replyTo;
        
        public String getContent() {
            return content;
        }
        
        public void setContent(String content) {
            this.content = content;
        }
        
        public Long getReplyTo() {
            return replyTo;
        }
        
        public void setReplyTo(Long replyTo) {
            this.replyTo = replyTo;
        }
    }
}
