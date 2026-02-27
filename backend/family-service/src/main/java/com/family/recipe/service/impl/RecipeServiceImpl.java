package com.family.recipe.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.family.recipe.entity.Recipe;
import com.family.recipe.mapper.RecipeMapper;
import com.family.recipe.service.RecipeService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecipeServiceImpl extends ServiceImpl<RecipeMapper, Recipe> implements RecipeService {
    
    @Override
    public List<Recipe> searchRecipes(Long familyId, String keyword, String category) {
        LambdaQueryWrapper<Recipe> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Recipe::getFamilyId, familyId);
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Recipe::getName, keyword)
                    .or().like(Recipe::getIngredients, keyword)
                    .or().like(Recipe::getDescription, keyword));
        }
        
        if (category != null && !category.isEmpty()) {
            wrapper.eq(Recipe::getCategory, category);
        }
        
        wrapper.orderByDesc(Recipe::getCreateTime);
        return list(wrapper);
    }
    
    @Override
    public Recipe getRecipeById(Long id) {
        return getById(id);
    }
    
    @Override
    public Recipe createRecipe(Recipe recipe) {
        recipe.setCreateTime(LocalDateTime.now());
        recipe.setUpdateTime(LocalDateTime.now());
        recipe.setFavoriteCount(0);
        save(recipe);
        return recipe;
    }
    
    @Override
    public List<String> getAllCategories() {
        return Arrays.asList(
            "早餐", "午餐", "晚餐", "夜宵",
            "家常菜", "面食", "凉菜", "热菜",
            "汤类", "甜点", "饮品", "小吃"
        );
    }
}
