package com.family.recipe.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.family.recipe.entity.Recipe;

import java.util.List;

public interface RecipeService extends IService<Recipe> {
    List<Recipe> searchRecipes(String keyword);
    Recipe createRecipe(Recipe recipe);
}
