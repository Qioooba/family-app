package com.family.family.controller;

import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 心愿控制器
 */
@RestController
@RequestMapping("/api/wish")
public class WishController {

    @GetMapping("/list")
    public Map<String, Object> list(@RequestParam Long familyId) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", new ArrayList<>());
        return result;
    }
    
    @PostMapping("/create")
    public Map<String, Object> create(@RequestBody Map<String, Object> data) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        return result;
    }
    
    @PutMapping("/update")
    public Map<String, Object> update(@RequestBody Map<String, Object> data) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        return result;
    }
    
    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        return result;
    }
    
    @PostMapping("/claim/{wishId}")
    public Map<String, Object> claim(@PathVariable Long wishId, @RequestBody Map<String, Object> data) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        return result;
    }
    
    @PostMapping("/progress/{wishId}")
    public Map<String, Object> updateProgress(@PathVariable Long wishId, @RequestBody Map<String, Object> data) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        return result;
    }
}
