package com.family.recipe.controller;

import com.family.common.core.Result;
import com.family.recipe.entity.Recipe;
import com.family.recipe.service.RecipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/recipe")
@RequiredArgsConstructor
public class RecipeController {
    
    private final RecipeService recipeService;
    
    @GetMapping("/search")
    public Result<List<Recipe>> search(@RequestParam(required = false) String keyword,
                                        @RequestParam(required = false) String category,
                                        @RequestParam(required = false) String difficulty) {
        return Result.success(recipeService.searchRecipes(keyword, category, difficulty));
    }
    
    @GetMapping("/family/{familyId}")
    public Result<List<Recipe>> getFamilyRecipes(@PathVariable Long familyId) {
        return Result.success(recipeService.getFamilyRecipes(familyId));
    }
    
    @PostMapping("/recommend")
    public Result<List<Recipe>> recommendByIngredients(@RequestBody List<String> ingredients) {
        return Result.success(recipeService.recommendByIngredients(ingredients));
    }
    
    @PostMapping("/create")
    public Result<Recipe> create(@RequestBody Recipe recipe) {
        return Result.success(recipeService.createRecipe(recipe));
    }
    
    @GetMapping("/{id}")
    public Result<Recipe> getById(@PathVariable Long id) {
        return Result.success(recipeService.getById(id));
    }
    
    @PostMapping("/favorite/{id}")
    public Result<Void> favorite(@PathVariable Long id, @RequestParam Long userId) {
        recipeService.favoriteRecipe(id, userId);
        return Result.success();
    }
    
    @PostMapping("/cook/{id}")
    public Result<Void> recordCooking(@PathVariable Long id, @RequestParam Long userId) {
        recipeService.recordCooking(id, userId);
        return Result.success();
    }
}
