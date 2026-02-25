package com.family.family.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 动态评论
 */
@TableName("moment_comment")
public class MomentComment {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long momentId;
    private Long userId;
    private String content;
    private Long replyTo;
    private Long replyUserId;
    private Integer likeCount;
    private LocalDateTime createTime;
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getMomentId() {
        return momentId;
    }
    
    public void setMomentId(Long momentId) {
        this.momentId = momentId;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
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
    
    public Long getReplyUserId() {
        return replyUserId;
    }
    
    public void setReplyUserId(Long replyUserId) {
        this.replyUserId = replyUserId;
    }
    
    public Integer getLikeCount() {
        return likeCount;
    }
    
    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
