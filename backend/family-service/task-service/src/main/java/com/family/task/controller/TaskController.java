package com.family.task.controller;

import com.family.common.core.Result;
import com.family.task.entity.Task;
import com.family.task.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/task")
public class TaskController {

    @Autowired
    private TaskService taskService;

    /**
     * 获取任务列表
     */
    @GetMapping("/list")
    public Result<List<Task>> list(@RequestParam(required = false) Long familyId,
                                    @RequestParam(required = false) Long categoryId,
                                    @RequestParam(required = false) Integer status) {
        List<Task> tasks = taskService.getFamilyTasks(familyId, categoryId, status);
        return Result.success(tasks);
    }

    /**
     * 创建任务
     */
    @PostMapping("/create")
    public Result<Task> create(@RequestBody Task task) {
        Task created = taskService.createTask(task);
        return Result.success(created);
    }

    /**
     * 更新任务
     */
    @PutMapping("/update")
    public Result<Task> update(@RequestBody Task task) {
        Task updated = taskService.updateTask(task);
        return Result.success(updated);
    }

    /**
     * 完成任务
     */
    @PostMapping("/complete/{id}")
    public Result<Void> complete(@PathVariable Long id) {
        taskService.completeTask(id);
        return Result.success();
    }

    /**
     * 指派任务
     */
    @PostMapping("/assign/{id}")
    public Result<Void> assign(@PathVariable Long id, @RequestBody AssignRequest request) {
        taskService.assignTask(id, request.getAssigneeId());
        return Result.success();
    }

    /**
     * 获取今日任务
     */
    @GetMapping("/today/{familyId}")
    public Result<List<Task>> getTodayTasks(@PathVariable Long familyId) {
        List<Task> tasks = taskService.getTodayTasks(familyId);
        return Result.success(tasks);
    }

    /**
     * 删除任务
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        taskService.removeById(id);
        return Result.success();
    }

    /**
     * 获取任务详情
     */
    @GetMapping("/{id}")
    public Result<Task> getById(@PathVariable Long id) {
        Task task = taskService.getById(id);
        return Result.success(task);
    }

    /**
     * 获取子任务列表
     */
    @GetMapping("/subtask/list/{taskId}")
    public Result<List<Task>> getSubtasks(@PathVariable Long taskId) {
        List<Task> subtasks = taskService.lambdaQuery()
                .eq(Task::getParentId, taskId)
                .list();
        return Result.success(subtasks);
    }

    /**
     * 添加子任务
     */
    @PostMapping("/subtask/add")
    public Result<Long> addSubtask(@RequestBody Task task) {
        taskService.save(task);
        return Result.success(task.getId());
    }

    /**
     * 切换子任务状态
     */
    @PutMapping("/subtask/{id}/toggle")
    public Result<Boolean> toggleSubtask(@PathVariable Long id) {
        Task task = taskService.getById(id);
        if (task != null) {
            task.setStatus(task.getStatus() == 2 ? 0 : 2);
            taskService.updateById(task);
            return Result.success(true);
        }
        return Result.success(false);
    }

    /**
     * 删除子任务
     */
    @DeleteMapping("/subtask/{id}")
    public Result<Boolean> deleteSubtask(@PathVariable Long id) {
        boolean removed = taskService.removeById(id);
        return Result.success(removed);
    }

    // 内部类用于接收请求参数
    public static class AssignRequest {
        private Long assigneeId;

        public Long getAssigneeId() {
            return assigneeId;
        }

        public void setAssigneeId(Long assigneeId) {
            this.assigneeId = assigneeId;
        }
    }
}
