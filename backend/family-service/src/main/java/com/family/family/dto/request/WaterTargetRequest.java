package com.family.family.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 饮水目标设置请求。
 */
public class WaterTargetRequest {

    @NotNull(message = "目标饮水量不能为空")
    @Min(value = 100, message = "目标饮水量不能小于100ml")
    @Max(value = 10000, message = "目标饮水量不能大于10000ml")
    private Integer targetAmount;

    public Integer getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(Integer targetAmount) {
        this.targetAmount = targetAmount;
    }
}
