package com.family.recipe.entity;

import com.family.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 菜谱实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Recipe extends BaseEntity {
    private static final long serialVersionUID = 1L;
    
    private Long familyId;
    private String name;
    private String description;
    private String category;
    private String cuisine;
    private Integer difficulty;
    private Integer time;
    private Integer servings;
    private String coverImage;
    private String images;
    private String ingredients;
    private String steps;
    private String nutrition;
    private String tags;
    private String source;
    private Long creatorId;
    private Integer favoriteCount;
    private Integer makeCount;
    private Double rating;
}
