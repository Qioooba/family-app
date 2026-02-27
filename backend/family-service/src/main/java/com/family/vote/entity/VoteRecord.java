package com.family.vote.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 投票记录实体
 */
@TableName("vote_record")
public class VoteRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long voteId;
    private Long userId;
    private String userName;
    private String selectedOptions; // JSON格式，如 ["选项1", "选项2"]
    private LocalDateTime createTime;
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getVoteId() { return voteId; }
    public void setVoteId(Long voteId) { this.voteId = voteId; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    
    public String getSelectedOptions() { return selectedOptions; }
    public void setSelectedOptions(String selectedOptions) { this.selectedOptions = selectedOptions; }
    
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
