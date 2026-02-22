package com.family.food.controller;
import cn.dev33.satoken.annotation.SaCheckLogin;
import com.family.common.core.Result;
import com.family.food.entity.Ingredient;
import com.family.food.service.IngredientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ingredient")
@SaCheckLogin
public class IngredientController {
    
    private final IngredientService ingredientService;
    
    @Autowired
    public IngredientController(IngredientService ingredientService) {
        this.ingredientService = ingredientService;
    }
    
    @GetMapping("/list/{familyId}")
    public Result<List<Ingredient>> list(@PathVariable Long familyId) {
        return Result.success(ingredientService.getFamilyIngredients(familyId));
    }
    
    @PostMapping("/add")
    public Result<Ingredient> add(@RequestBody Ingredient ingredient) {
        return Result.success(ingredientService.addIngredient(ingredient));
    }
    
    @PutMapping("/update")
    public Result<Ingredient> update(@RequestBody Ingredient ingredient) {
        return Result.success(ingredientService.updateIngredient(ingredient));
    }
    
    @PostMapping("/recognize")
    public Result<List<Ingredient>> recognize(@RequestParam String imageBase64) {
        return Result.success(ingredientService.recognizeFromImage(imageBase64));
    }
    
    @GetMapping("/expiring/{familyId}")
    public Result<List<Ingredient>> getExpiring(@PathVariable Long familyId) {
        return Result.success(ingredientService.getExpiringIngredients(familyId));
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        ingredientService.removeById(id);
        return Result.success();
    }
}
