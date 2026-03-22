package com.family.family.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 新增饮水记录请求。
 */
public class WaterRecordCreateRequest {

    @NotNull(message = "饮水量不能为空")
    @Min(value = 10, message = "单次饮水量不能小于10ml")
    @Max(value = 3000, message = "单次饮水量不能大于3000ml")
    private Integer amount;

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }
}
