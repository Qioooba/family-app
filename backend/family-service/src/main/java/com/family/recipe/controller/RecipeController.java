package com.family.recipe.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.family.common.core.Result;
import com.family.recipe.entity.Recipe;
import com.family.recipe.service.RecipeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@SaCheckLogin
@RequestMapping("/api/recipe")
public class RecipeController {
    
    private final RecipeService recipeService;
    
    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }
    
    @GetMapping("/search")
    public Result<List<Recipe>> search(
            @RequestParam Long familyId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category) {
        return Result.success(recipeService.searchRecipes(familyId, keyword, category));
    }
    
    @GetMapping("/{id}")
    public Result<Recipe> getById(@PathVariable Long id) {
        return Result.success(recipeService.getRecipeById(id));
    }
    
    @PostMapping("/create")
    public Result<Recipe> create(@RequestBody Recipe recipe) {
        return Result.success(recipeService.createRecipe(recipe));
    }
    
    @GetMapping("/categories")
    public Result<List<String>> categories() {
        return Result.success(recipeService.getAllCategories());
    }
}
