package com.family.recipe.controller;
import cn.dev33.satoken.annotation.SaCheckLogin;
import com.family.common.core.Result;
import com.family.recipe.entity.Recipe;
import com.family.recipe.service.RecipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜谱控制器
 */
@RestController
@RequestMapping("/api/recipe")
@RequiredArgsConstructor
@SaCheckLogin
public class RecipeController {
    
    private final RecipeService recipeService;
    
    /**
     * 获取菜谱列表
     */
    @GetMapping("/list")
    public Result<List<Recipe>> list(@RequestParam(required = false) Long familyId,
                                      @RequestParam(required = false) String category) {
        if (familyId != null) {
            return Result.success(recipeService.getFamilyRecipes(familyId));
        }
        return Result.success(recipeService.list());
    }
    
    /**
     * 搜索菜谱
     */
    @GetMapping("/search")
    public Result<List<Recipe>> search(@RequestParam(required = false) String keyword,
                                        @RequestParam(required = false) String category,
                                        @RequestParam(required = false) String difficulty) {
        return Result.success(recipeService.searchRecipes(keyword, category, difficulty));
    }
    
    /**
     * 获取家庭菜谱
     */
    @GetMapping("/family/{familyId}")
    public Result<List<Recipe>> getFamilyRecipes(@PathVariable Long familyId) {
        return Result.success(recipeService.getFamilyRecipes(familyId));
    }
    
    /**
     * 根据食材推荐菜谱
     */
    @PostMapping("/recommend")
    public Result<List<Recipe>> recommendByIngredients(@RequestBody List<String> ingredients) {
        return Result.success(recipeService.recommendByIngredients(ingredients));
    }
    
    /**
     * 创建菜谱
     */
    @PostMapping("/create")
    public Result<Recipe> create(@RequestBody Recipe recipe) {
        return Result.success(recipeService.createRecipe(recipe));
    }
    
    /**
     * 更新菜谱
     */
    @PutMapping("/update")
    public Result<Boolean> update(@RequestBody Recipe recipe) {
        return Result.success(recipeService.updateById(recipe));
    }
    
    /**
     * 删除菜谱
     */
    @DeleteMapping("/delete/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(recipeService.removeById(id));
    }
    
    /**
     * 获取菜谱详情
     */
    @GetMapping("/detail/{id}")
    public Result<Recipe> getById(@PathVariable Long id) {
        return Result.success(recipeService.getById(id));
    }
    
    /**
     * 收藏菜谱
     */
    @PostMapping("/favorite/{id}")
    public Result<Void> favorite(@PathVariable Long id, @RequestParam Long userId) {
        recipeService.favoriteRecipe(id, userId);
        return Result.success();
    }
    
    /**
     * 记录烹饪
     */
    @PostMapping("/cook/{id}")
    public Result<Void> recordCooking(@PathVariable Long id, @RequestParam Long userId) {
        recipeService.recordCooking(id, userId);
        return Result.success();
    }
}
