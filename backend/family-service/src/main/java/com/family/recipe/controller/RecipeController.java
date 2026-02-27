package com.family.recipe.controller;

import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recipe")
public class RecipeController {

    @GetMapping("/search")
    public Map<String, Object> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long familyId) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", new ArrayList<>());
        return result;
    }
    
    @PostMapping("/create")
    public Map<String, Object> create(@RequestBody Map<String, Object> recipe) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", recipe);
        return result;
    }
    
    @GetMapping("/{id}")
    public Map<String, Object> getById(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("name", "示例菜谱");
        result.put("data", data);
        return result;
    }
    
    @GetMapping("/categories")
    public Map<String, Object> categories() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        List<String> cats = new ArrayList<>();
        cats.add("中餐");
        cats.add("西餐");
        result.put("data", cats);
        return result;
    }
}
