package com.family.family.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.family.family.entity.Wish;
import com.family.family.mapper.WishMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 心愿控制器
 */
@RestController
@RequestMapping("/api/wish")
public class WishController {

    @Autowired
    private WishMapper wishMapper;

    @GetMapping("/list")
    public Map<String, Object> list(@RequestParam Long familyId) {
        Map<String, Object> result = new HashMap<>();
        try {
            QueryWrapper<Wish> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("family_id", familyId);
            queryWrapper.orderByDesc("create_time");
            List<Wish> wishes = wishMapper.selectList(queryWrapper);
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", wishes);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "查询失败: " + e.getMessage());
            result.put("data", new ArrayList<>());
        }
        return result;
    }
    
    @PostMapping("/create")
    public Map<String, Object> create(@RequestBody Map<String, Object> data) {
        Map<String, Object> result = new HashMap<>();
        try {
            Wish wish = new Wish();
            wish.setTitle((String) data.get("title"));
            wish.setFamilyId(((Number) data.get("familyId")).longValue());
            if (data.get("targetAmount") != null) {
                wish.setBudgetMax(new java.math.BigDecimal(data.get("targetAmount").toString()));
            }
            wish.setDescription((String) data.get("description"));
            wish.setStatus(0);
            wish.setProgress(0);
            wishMapper.insert(wish);
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", wish);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "创建失败: " + e.getMessage());
        }
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
