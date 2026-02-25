package com.family.recipe.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.family.family.entity.Recipe;

import java.util.List;

public interface RecipeService extends IService<Recipe> {
    
    List<Recipe> searchRecipes(String keyword, String category, String difficulty);
    
    List<Recipe> getFamilyRecipes(Long familyId);
    
    List<Recipe> recommendByIngredients(List<String> ingredients);
    
    Recipe createRecipe(Recipe recipe);
    
    void favoriteRecipe(Long recipeId, Long userId);
    
    void recordCooking(Long recipeId, Long userId);
}
