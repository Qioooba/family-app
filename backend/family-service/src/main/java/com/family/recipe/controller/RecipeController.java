package com.family.recipe.controller;

import com.family.common.core.Result;
import com.family.recipe.entity.Recipe;
import com.family.recipe.service.RecipeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recipe")
public class RecipeController {
    
    private final RecipeService recipeService;
    
    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }
    
    @GetMapping("/search")
    public Result<List<Recipe>> search(@RequestParam(required = false) String keyword) {
        return Result.success(recipeService.searchRecipes(keyword));
    }
    
    @PostMapping("/create")
    public Result<Recipe> create(@RequestBody Recipe recipe) {
        return Result.success(recipeService.createRecipe(recipe));
    }
    
    @GetMapping("/{id}")
    public Result<Recipe> getById(@PathVariable Long id) {
        return Result.success(recipeService.getById(id));
    }
}
