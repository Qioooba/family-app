package com.family.recipe.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("recipe")
public class Recipe implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long familyId;
    private String name;
    private String category;
    private String ingredients;
    private String steps;
    private String description;
    private String difficulty;
    private Integer cookingTime;
    private String imageUrl;
    private Long creatorId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
