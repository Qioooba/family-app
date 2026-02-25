package com.family.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;

/**
 * AI使用统计
 */
@Data
@TableName("ai_usage_stats")
public class AiUsageStats {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 日期
     */
    private LocalDate date;
    
    /**
     * 对话次数
     */
    private Integer chatCount;
    
    /**
     * 语音次数
     */
    private Integer voiceCount;
    
    /**
     * Token使用量
     */
    private Integer tokenUsed;
}
