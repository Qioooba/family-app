package com.family.task.controller;

import com.family.common.core.Result;
import com.family.task.entity.Task;
import com.family.task.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/task")
@RequiredArgsConstructor
public class TaskController {
    
    private final TaskService taskService;
    
    @GetMapping("/list")
    public Result<List<Task>> list(@RequestParam Long familyId,
                                    @RequestParam(required = false) Long categoryId,
                                    @RequestParam(required = false) Integer status) {
        return Result.success(taskService.getFamilyTasks(familyId, categoryId, status));
    }
    
    @PostMapping("/create")
    public Result<Task> create(@RequestBody Task task) {
        return Result.success(taskService.createTask(task));
    }
    
    @PutMapping("/update")
    public Result<Task> update(@RequestBody Task task) {
        return Result.success(taskService.updateTask(task));
    }
    
    @PostMapping("/complete/{id}")
    public Result<Void> complete(@PathVariable Long id) {
        taskService.completeTask(id);
        return Result.success();
    }
    
    @PostMapping("/assign/{id}")
    public Result<Void> assign(@PathVariable Long id, @RequestParam Long assigneeId) {
        taskService.assignTask(id, assigneeId);
        return Result.success();
    }
    
    @GetMapping("/today/{familyId}")
    public Result<List<Task>> getTodayTasks(@PathVariable Long familyId) {
        return Result.success(taskService.getTodayTasks(familyId));
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        taskService.removeById(id);
        return Result.success();
    }
}
