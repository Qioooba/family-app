package com.family.family.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 默契问答游戏会话
 */
@Data
@TableName("mind_match_session")
public class MindMatchSession implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 家庭ID */
    private Long familyId;
    
    /** 游戏轮次 */
    private Integer round;
    
    /** 题目ID */
    private Long questionId;
    
    /** 玩家1ID */
    private Long player1Id;
    
    /** 玩家1答案 */
    private String player1Answer;
    
    /** 玩家1是否已回答 */
    private Boolean player1Answered;
    
    /** 玩家2ID */
    private Long player2Id;
    
    /** 玩家2答案 */
    private String player2Answer;
    
    /** 玩家2是否已回答 */
    private Boolean player2Answered;
    
    /** 是否匹配(答案相同) */
    private Boolean isMatch;
    
    /** 本轮得分 */
    private Integer roundPoints;
    
    /** 会话状态: waiting-等待中, in_progress-进行中, completed-已完成 */
    private String status;
    
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDeleted;
}
