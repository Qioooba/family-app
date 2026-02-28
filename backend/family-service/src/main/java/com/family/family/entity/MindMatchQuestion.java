package com.family.family.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 默契问答问题
 */
@Data
@TableName("mind_match_question")
public class MindMatchQuestion implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 问题内容 */
    private String question;
    
    /** 题目类型: spouse-夫妻, parent-亲子, family-家庭 */
    private String type;
    
    /** 指向的用户ID(被问关于谁) */
    private Long targetUserId;
    
    /** 期望的答案(被问者本人的答案) */
    private String targetAnswer;
    
    /** 难度: 1-简单, 2-中等, 3-困难 */
    private Integer difficulty;
    
    /** 状态: 0-禁用, 1-启用 */
    private Integer status;
    
    /** 使用次数 */
    private Integer playCount;
    
    /** 答对次数 */
    private Integer correctCount;
    
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDeleted;
}
