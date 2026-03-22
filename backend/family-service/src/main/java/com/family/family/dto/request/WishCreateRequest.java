package com.family.family.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * 创建心愿请求。
 */
public class WishCreateRequest {

    @NotNull(message = "家庭ID不能为空")
    private Long familyId;

    @NotBlank(message = "心愿标题不能为空")
    @Size(max = 100, message = "心愿标题不能超过100个字符")
    private String title;

    @Size(max = 1000, message = "心愿描述不能超过1000个字符")
    private String description;

    @NotBlank(message = "心愿类型不能为空")
    private String type;

    private String visibility = "couple";
    private BigDecimal budgetMin;
    private BigDecimal budgetMax;
    private String expectDate;
    private String images;

    @Min(value = 0, message = "优先级不能小于0")
    @Max(value = 2, message = "优先级不能大于2")
    private Integer priority = 0;

    @Min(value = 1, message = "难度不能小于1")
    @Max(value = 5, message = "难度不能大于5")
    private Integer difficulty = 1;

    public Long getFamilyId() {
        return familyId;
    }

    public void setFamilyId(Long familyId) {
        this.familyId = familyId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public BigDecimal getBudgetMin() {
        return budgetMin;
    }

    public void setBudgetMin(BigDecimal budgetMin) {
        this.budgetMin = budgetMin;
    }

    public BigDecimal getBudgetMax() {
        return budgetMax;
    }

    public void setBudgetMax(BigDecimal budgetMax) {
        this.budgetMax = budgetMax;
    }

    public String getExpectDate() {
        return expectDate;
    }

    public void setExpectDate(String expectDate) {
        this.expectDate = expectDate;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Integer getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Integer difficulty) {
        this.difficulty = difficulty;
    }
}
