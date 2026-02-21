package com.family.family.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 菜谱记录实体
 */
@Data
@TableName("recipe_record")
public class RecipeRecord {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    private Long familyId;
    private Long recipeId;
    private String recipeName;
    private String imageUrl;
    private LocalDateTime cookTime;
    private String remark;
    private LocalDateTime createTime;
}
