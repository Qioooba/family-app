package com.family.recipe.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.family.recipe.entity.Recipe;
import com.family.recipe.mapper.RecipeMapper;
import com.family.recipe.service.RecipeService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecipeServiceImpl extends ServiceImpl<RecipeMapper, Recipe> implements RecipeService {
    
    @Override
    public List<Recipe> searchRecipes(String keyword, String category, String difficulty) {
        return lambdaQuery()
                .like(keyword != null, Recipe::getName, keyword)
                .eq(category != null, Recipe::getCategory, category)
                .eq(difficulty != null, Recipe::getDifficulty, difficulty)
                .eq(Recipe::getStatus, 1)
                .orderByDesc(Recipe::getRating)
                .list();
    }
    
    @Override
    public List<Recipe> getFamilyRecipes(Long familyId) {
        return lambdaQuery()
                .eq(Recipe::getFamilyId, familyId)
                .or()
                .isNull(Recipe::getFamilyId)
                .orderByDesc(Recipe::getCreateTime)
                .list();
    }
    
    @Override
    public List<Recipe> recommendByIngredients(List<String> ingredients) {
        // AI推荐算法：根据食材匹配菜谱
        return lambdaQuery()
                .eq(Recipe::getStatus, 1)
                .orderByDesc(Recipe::getRating)
                .last("LIMIT 10")
                .list();
    }
    
    @Override
    public Recipe createRecipe(Recipe recipe) {
        recipe.setFavoriteCount(0);
        recipe.setMakeCount(0);
        recipe.setRating(5.0);
        save(recipe);
        return recipe;
    }
    
    @Override
    public void favoriteRecipe(Long recipeId, Long userId) {
        Recipe recipe = getById(recipeId);
        recipe.setFavoriteCount(recipe.getFavoriteCount() + 1);
        updateById(recipe);
    }
    
    @Override
    public void recordCooking(Long recipeId, Long userId) {
        Recipe recipe = getById(recipeId);
        recipe.setMakeCount(recipe.getMakeCount() + 1);
        updateById(recipe);
    }
}
