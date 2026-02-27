package com.family.recipe.service;

import com.family.recipe.entity.Recipe;

import java.util.List;

public interface RecipeService {
    
    List<Recipe> searchRecipes(Long familyId, String keyword, String category);
    
    Recipe getRecipeById(Long id);
    
    Recipe createRecipe(Recipe recipe);
    
    List<String> getAllCategories();
}
