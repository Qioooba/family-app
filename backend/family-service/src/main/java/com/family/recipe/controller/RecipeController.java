package com.family.recipe.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.family.common.core.Result;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recipe")
@SaCheckLogin
public class RecipeController {

    @GetMapping("/search")
    public Result<List<Map<String, Object>>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long familyId) {
        return Result.success(new ArrayList<>());
    }
    
    @PostMapping("/create")
    public Result<Map<String, Object>> create(@RequestBody Map<String, Object> recipe) {
        return Result.success(recipe);
    }
    
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getById(@PathVariable Long id) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("name", "示例菜谱");
        return Result.success(data);
    }
    
    @GetMapping("/categories")
    public Result<List<String>> categories() {
        List<String> cats = new ArrayList<>();
        cats.add("中餐");
        cats.add("西餐");
        return Result.success(cats);
    }
}
