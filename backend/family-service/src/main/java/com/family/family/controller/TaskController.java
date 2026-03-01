package com.family.family.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.family.family.entity.FamilyMember;
import com.family.family.entity.Task;
import com.family.family.mapper.FamilyMemberMapper;
import com.family.family.mapper.TaskMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    @Autowired
    private FamilyMemberMapper familyMemberMapper;

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        try {
            return StpUtil.getLoginIdAsLong();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 检查用户是否为家庭成员
     */
    private boolean isFamilyMember(Long familyId, Long userId) {
        if (familyId == null || userId == null) return false;
        FamilyMember member = familyMemberMapper.selectOne(
            new LambdaQueryWrapper<FamilyMember>()
                .eq(FamilyMember::getFamilyId, familyId)
                .eq(FamilyMember::getUserId, userId)
        );
        return member != null;
    }

    @GetMapping("/list")
    public Map<String, Object> list(@RequestParam Long familyId, @RequestParam(required = false) Integer status) {
        Map<String, Object> result = new HashMap<>();
        Long userId = getCurrentUserId();

        if (userId == null) {
            result.put("code", 401);
            result.put("message", "请先登录");
            return result;
        }

        // 验证用户是否属于该家庭
        if (!isFamilyMember(familyId, userId)) {
            result.put("code", 403);
            result.put("message", "您不是该家庭的成员");
            return result;
        }

        try {
            // 查询当前状态的任务列表
            LambdaQueryWrapper<Task> query = new LambdaQueryWrapper<Task>()
                .eq(Task::getFamilyId, familyId)
                .eq(Task::getIsDeleted, 0);
            
            if (status != null) {
                query.eq(Task::getStatus, status);
            }
            
            // 按截止时间排序，没有截止时间的排最后
            query.orderByAsc(Task::getDueTime);
            List<Task> tasks = taskMapper.selectList(query);
            
            // 查询待办数量
            LambdaQueryWrapper<Task> todoQuery = new LambdaQueryWrapper<Task>()
                .eq(Task::getFamilyId, familyId)
                .eq(Task::getIsDeleted, 0)
                .eq(Task::getStatus, 0);
            Long todoCount = taskMapper.selectCount(todoQuery);
            
            // 查询已完成数量
            LambdaQueryWrapper<Task> doneQuery = new LambdaQueryWrapper<Task>()
                .eq(Task::getFamilyId, familyId)
                .eq(Task::getIsDeleted, 0)
                .eq(Task::getStatus, 2);
            Long doneCount = taskMapper.selectCount(doneQuery);

            Map<String, Object> data = new HashMap<>();
            data.put("list", tasks);
            data.put("todoCount", todoCount);
            data.put("doneCount", doneCount);
            
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", data);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "查询失败：" + e.getMessage());
        }
        return result;
    }

    @GetMapping("/today/{familyId}")
    public Map<String, Object> today(@PathVariable Long familyId) {
        Map<String, Object> result = new HashMap<>();
        Long userId = getCurrentUserId();

        if (userId == null) {
            result.put("code", 401);
            result.put("message", "请先登录");
            return result;
        }

        // 验证用户是否属于该家庭
        if (!isFamilyMember(familyId, userId)) {
            result.put("code", 403);
            result.put("message", "您不是该家庭的成员");
            return result;
        }

        try {
            LocalDate today = LocalDate.now();
            LocalDateTime startOfDay = today.atStartOfDay();
            LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

            LambdaQueryWrapper<Task> query = new LambdaQueryWrapper<Task>()
                .eq(Task::getFamilyId, familyId)
                .eq(Task::getIsDeleted, 0)
                .ge(Task::getDueTime, startOfDay)
                .lt(Task::getDueTime, endOfDay)
                .orderByAsc(Task::getDueTime);

            List<Task> tasks = taskMapper.selectList(query);

            result.put("code", 200);
            result.put("message", "success");
            result.put("data", tasks);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "查询失败：" + e.getMessage());
        }
        return result;
    }

    @PostMapping("/create")
    public Map<String, Object> create(@RequestBody Task task) {
        Map<String, Object> result = new HashMap<>();
        Long userId = getCurrentUserId();

        if (userId == null) {
            result.put("code", 401);
            result.put("message", "请先登录");
            return result;
        }

        if (task.getFamilyId() == null) {
            result.put("code", 400);
            result.put("message", "familyId不能为空");
            return result;
        }

        // 验证用户是否属于该家庭
        if (!isFamilyMember(task.getFamilyId(), userId)) {
            result.put("code", 403);
            result.put("message", "您不是该家庭的成员");
            return result;
        }

        try {
            // 设置创建者
            task.setCreatorId(userId);
            task.setCreateTime(LocalDateTime.now());
            task.setUpdateTime(LocalDateTime.now());
            task.setStatus(0); // 默认待完成
            task.setIsDeleted(0);

            taskMapper.insert(task);
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", task);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "创建失败：" + e.getMessage());
        }
        return result;
    }

    @GetMapping("/{id}")
    public Map<String, Object> getById(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        Long userId = getCurrentUserId();

        if (userId == null) {
            result.put("code", 401);
            result.put("message", "请先登录");
            return result;
        }

        try {
            Task task = taskMapper.selectById(id);
            if (task == null || task.getIsDeleted() == 1) {
                result.put("code", 404);
                result.put("message", "任务不存在");
                return result;
            }

            // 验证用户是否属于该家庭
            if (!isFamilyMember(task.getFamilyId(), userId)) {
                result.put("code", 403);
                result.put("message", "您无权查看此任务");
                return result;
            }

            result.put("code", 200);
            result.put("message", "success");
            result.put("data", task);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "查询失败：" + e.getMessage());
        }
        return result;
    }

    @PutMapping("/update")
    public Map<String, Object> update(@RequestBody Task task) {
        Map<String, Object> result = new HashMap<>();
        Long userId = getCurrentUserId();

        if (userId == null) {
            result.put("code", 401);
            result.put("message", "请先登录");
            return result;
        }

        try {
            Task existingTask = taskMapper.selectById(task.getId());
            if (existingTask == null || existingTask.getIsDeleted() == 1) {
                result.put("code", 404);
                result.put("message", "任务不存在");
                return result;
            }

            // 验证用户是否属于该家庭
            if (!isFamilyMember(existingTask.getFamilyId(), userId)) {
                result.put("code", 403);
                result.put("message", "您无权修改此任务");
                return result;
            }

            task.setUpdateTime(LocalDateTime.now());
            taskMapper.updateById(task);

            result.put("code", 200);
            result.put("message", "success");
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "更新失败：" + e.getMessage());
        }
        return result;
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        Long userId = getCurrentUserId();

        if (userId == null) {
            result.put("code", 401);
            result.put("message", "请先登录");
            return result;
        }

        try {
            Task task = taskMapper.selectById(id);
            if (task == null || task.getIsDeleted() == 1) {
                result.put("code", 404);
                result.put("message", "任务不存在");
                return result;
            }

            // 验证用户是否属于该家庭
            if (!isFamilyMember(task.getFamilyId(), userId)) {
                result.put("code", 403);
                result.put("message", "您无权删除此任务");
                return result;
            }

            // 逻辑删除
            task.setIsDeleted(1);
            task.setDeleteTime(LocalDateTime.now());
            task.setUpdateTime(LocalDateTime.now());
            taskMapper.updateById(task);

            result.put("code", 200);
            result.put("message", "success");
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "删除失败：" + e.getMessage());
        }
        return result;
    }

    @PostMapping("/complete/{id}")
    public Map<String, Object> complete(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        Long userId = getCurrentUserId();

        if (userId == null) {
            result.put("code", 401);
            result.put("message", "请先登录");
            return result;
        }

        try {
            Task task = taskMapper.selectById(id);
            if (task == null || task.getIsDeleted() == 1) {
                result.put("code", 404);
                result.put("message", "任务不存在");
                return result;
            }

            // 验证用户是否属于该家庭
            if (!isFamilyMember(task.getFamilyId(), userId)) {
                result.put("code", 403);
                result.put("message", "您无权操作此任务");
                return result;
            }

            task.setStatus(2); // 已完成
            task.setFinishTime(LocalDateTime.now());
            task.setUpdateTime(LocalDateTime.now());
            taskMapper.updateById(task);

            result.put("code", 200);
            result.put("message", "success");
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "操作失败：" + e.getMessage());
        }
        return result;
    }
}
