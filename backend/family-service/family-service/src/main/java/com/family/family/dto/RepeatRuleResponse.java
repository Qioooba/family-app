package com.family.family.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 任务重复规则响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RepeatRuleResponse {
    private String repeatType;
    private String repeatRule;
}