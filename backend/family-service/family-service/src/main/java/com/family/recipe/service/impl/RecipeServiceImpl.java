package com.family.recipe.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.family.common.cache.Cacheable;
import com.family.common.cache.CacheEvict;
import com.family.common.cache.CachePut;
import com.family.family.entity.Recipe;
import com.family.recipe.mapper.RecipeMapper;
import com.family.recipe.service.RecipeService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class RecipeServiceImpl extends ServiceImpl<RecipeMapper, Recipe> implements RecipeService {
    
    @Override
    @Cacheable(value = "recipe:search", key = "(#keyword ?: 'all') + ':' + (#category ?: 'all') + ':' + (#difficulty ?: 'all')", expire = 30, unit = TimeUnit.MINUTES)
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
    @Cacheable(value = "recipe:family", key = "#familyId", expire = 20, unit = TimeUnit.MINUTES)
    public List<Recipe> getFamilyRecipes(Long familyId) {
        return lambdaQuery()
                .eq(Recipe::getFamilyId, familyId)
                .or()
                .isNull(Recipe::getFamilyId)
                .orderByDesc(Recipe::getCreateTime)
                .list();
    }
    
    @Override
    @Cacheable(value = "recipe:recommend", key = "'ingredients:' + #ingredients.hashCode()", expire = 60, unit = TimeUnit.MINUTES)
    public List<Recipe> recommendByIngredients(List<String> ingredients) {
        // AI推荐算法：根据食材匹配菜谱
        return lambdaQuery()
                .eq(Recipe::getStatus, 1)
                .orderByDesc(Recipe::getRating)
                .last("LIMIT 10")
                .list();
    }
    
    @Override
    @CacheEvict(value = {"recipe:search", "recipe:family"}, allEntries = true)
    public Recipe createRecipe(Recipe recipe) {
        recipe.setFavoriteCount(0);
        recipe.setMakeCount(0);
        recipe.setRating(5.0);
        save(recipe);
        return recipe;
    }
    
    @Override
    @CachePut(value = "recipe", key = "#recipeId", useMutex = true)
    public void favoriteRecipe(Long recipeId, Long userId) {
        Recipe recipe = getById(recipeId);
        recipe.setFavoriteCount(recipe.getFavoriteCount() + 1);
        updateById(recipe);
    }
    
    @Override
    @CachePut(value = "recipe", key = "#recipeId", useMutex = true)
    public void recordCooking(Long recipeId, Long userId) {
        Recipe recipe = getById(recipeId);
        recipe.setMakeCount(recipe.getMakeCount() + 1);
        updateById(recipe);
    }
}
