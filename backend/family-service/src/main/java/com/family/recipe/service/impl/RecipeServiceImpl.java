package com.family.recipe.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.family.recipe.entity.Recipe;
import com.family.recipe.mapper.RecipeMapper;
import com.family.recipe.service.RecipeService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecipeServiceImpl extends ServiceImpl<RecipeMapper, Recipe> implements RecipeService {
    
    @Override
    public List<Recipe> searchRecipes(String keyword) {
        LambdaQueryWrapper<Recipe> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Recipe::getName, keyword)
                   .or().like(Recipe::getDescription, keyword);
        }
        return list(wrapper);
    }
    
    @Override
    public Recipe createRecipe(Recipe recipe) {
        save(recipe);
        return recipe;
    }
}
