package com.family.family.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.family.family.entity.Task;
import com.family.family.mapper.TaskMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 任务控制器
 */
@RestController
@RequestMapping("/api/task")
@SaCheckLogin
public class TaskController {

    @Autowired
    private TaskMapper taskMapper;

    @GetMapping("/list")
    public Map<String, Object> list(@RequestParam Long familyId) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", taskMapper.selectList(null));
        return result;
    }
    
    @GetMapping("/today/{familyId}")
    public Map<String, Object> today(@PathVariable Long familyId) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", List.of());
        return result;
    }
    
    @PostMapping("/create")
    public Map<String, Object> create(@RequestBody Task task) {
        Map<String, Object> result = new HashMap<>();
        try {
            taskMapper.insert(task);
            result.put("code", 200);
            result.put("message", "success");
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", e.getMessage());
        }
        return result;
    }
}
