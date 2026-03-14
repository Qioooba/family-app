package com.family.family.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.family.family.entity.FamilyMember;
import com.family.family.entity.Task;
import com.family.family.entity.User;
import com.family.family.mapper.FamilyMemberMapper;
import com.family.family.mapper.TaskMapper;
import com.family.family.mapper.UserMapper;
import com.family.family.service.WechatWorkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WechatWorkService wechatWorkService;

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
    public Map<String, Object> list(
            @RequestParam Long familyId, 
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
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
            // 查询当前状态的任务列表（指派人和被指派人都能看到）
            LambdaQueryWrapper<Task> query = new LambdaQueryWrapper<Task>()
                .eq(Task::getFamilyId, familyId)
                .eq(Task::getIsDeleted, 0)
                .and(q -> q.eq(Task::getAssigneeId, userId)
                          .or()
                          .eq(Task::getCreatorId, userId));
            
            if (status != null) {
                query.eq(Task::getStatus, status);
            }
            
            // 不在这里排序，后面会自定义排序
            List<Task> tasks = taskMapper.selectList(query);
            
            // 智能排序：
            // 1. 已完成的待办排最后
            // 2. 已过期待办（截止日期 < 今天且未完成）排前面，按截止日期从近到远排序
            // 3. 今天及未来的待办（截止日期 >= 今天）排中间，按截止日期从近到远排序
            LocalDate today = LocalDate.now();
            tasks.sort((a, b) -> {
                // 已完成的任务排最后
                if (a.getStatus() == 2 && b.getStatus() != 2) return 1;
                if (a.getStatus() != 2 && b.getStatus() == 2) return -1;
                
                // 都是已完成的，按完成时间排序
                if (a.getStatus() == 2 && b.getStatus() == 2) {
                    if (a.getFinishTime() == null && b.getFinishTime() == null) return 0;
                    if (a.getFinishTime() == null) return 1;
                    if (b.getFinishTime() == null) return -1;
                    return b.getFinishTime().compareTo(a.getFinishTime());
                }
                
                // 获取截止日期（处理null的情况）
                LocalDateTime dueTimeA = a.getDueTime();
                LocalDateTime dueTimeB = b.getDueTime();
                if (dueTimeA == null && dueTimeB == null) return 0;
                if (dueTimeA == null) return 1;  // null值排后面
                if (dueTimeB == null) return -1;
                
                LocalDate dateA = dueTimeA.toLocalDate();
                LocalDate dateB = dueTimeB.toLocalDate();
                boolean isOverdueA = dateA.isBefore(today);
                boolean isOverdueB = dateB.isBefore(today);
                
                // 过期任务排前面
                if (isOverdueA && !isOverdueB) return -1;
                if (!isOverdueA && isOverdueB) return 1;
                
                // 都是过期或都是非过期，按日期排序（从近到远）
                return dateA.compareTo(dateB);
            });
            
            // 查询待办数量（指派人和被指派人都能看到）
            LambdaQueryWrapper<Task> todoQuery = new LambdaQueryWrapper<Task>()
                .eq(Task::getFamilyId, familyId)
                .eq(Task::getIsDeleted, 0)
                .eq(Task::getStatus, 0)
                .and(q -> q.eq(Task::getAssigneeId, userId)
                          .or()
                          .eq(Task::getCreatorId, userId));
            Long todoCount = taskMapper.selectCount(todoQuery);
            
            // 查询已完成数量（指派人和被指派人都能看到）
            LambdaQueryWrapper<Task> doneQuery = new LambdaQueryWrapper<Task>()
                .eq(Task::getFamilyId, familyId)
                .eq(Task::getIsDeleted, 0)
                .eq(Task::getStatus, 2)
                .and(q -> q.eq(Task::getAssigneeId, userId)
                          .or()
                          .eq(Task::getCreatorId, userId));
            Long doneCount = taskMapper.selectCount(doneQuery);

            Map<String, Object> data = new HashMap<>();
            
            // 为每个任务添加assigneeRealName
            List<Map<String, Object>> taskList = new ArrayList<>();
            for (Task task : tasks) {
                Map<String, Object> taskMap = new HashMap<>();
                taskMap.put("id", task.getId());
                taskMap.put("familyId", task.getFamilyId());
                taskMap.put("categoryId", task.getCategoryId());
                taskMap.put("title", task.getTitle());
                taskMap.put("content", task.getContent());
                taskMap.put("remark", task.getRemark());
                taskMap.put("priority", task.getPriority());
                taskMap.put("status", task.getStatus());
                taskMap.put("assigneeId", task.getAssigneeId());
                taskMap.put("creatorId", task.getCreatorId());
                taskMap.put("dueTime", task.getDueTime());
                taskMap.put("remindTime", task.getRemindTime());
                taskMap.put("location", task.getLocation());
                taskMap.put("parentId", task.getParentId());
                taskMap.put("attachments", task.getAttachments());
                taskMap.put("finishTime", task.getFinishTime());
                taskMap.put("createTime", task.getCreateTime());
                taskMap.put("updateTime", task.getUpdateTime());
                taskMap.put("isArchived", task.getIsArchived());
                taskMap.put("archiveTime", task.getArchiveTime());
                taskMap.put("isDeleted", task.getIsDeleted());
                taskMap.put("deleteTime", task.getDeleteTime());
                taskMap.put("sortOrder", task.getSortOrder());
                
                // 获取指派人的昵称
                if (task.getAssigneeId() != null) {
                    User assignee = userMapper.selectById(task.getAssigneeId());
                    if (assignee != null) {
                        taskMap.put("assigneeNickname", assignee.getNickname());
                    }
                }
                
                // 获取创建人的昵称
                if (task.getCreatorId() != null) {
                    User creator = userMapper.selectById(task.getCreatorId());
                    if (creator != null) {
                        taskMap.put("creatorNickname", creator.getNickname());
                    }
                }
                
                taskList.add(taskMap);
            }
            
            data.put("list", taskList);
            data.put("todoCount", todoCount);
            data.put("doneCount", doneCount);
            // 添加分页信息
            data.put("total", tasks.size());
            data.put("pages", 1);
            data.put("current", page);
            data.put("size", size);
            
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", data);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("message", "查询失败：" + e.getMessage());
            result.put("errorType", e.getClass().getName());
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
                .and(q -> q.eq(Task::getAssigneeId, userId)
                          .or()
                          .eq(Task::getCreatorId, userId))
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
            
            // ===== 企业微信推送通知 =====
            try {
                // 获取创建者信息
                User creator = userMapper.selectById(userId);
                String creatorName = creator != null ? creator.getNickname() : "家人";
                
                // 如果有被指派人，给被指派人发通知
                if (task.getAssigneeId() != null && !task.getAssigneeId().equals(userId)) {
                    User assignee = userMapper.selectById(task.getAssigneeId());
                    if (assignee != null) {
                        String assigneeName = assignee.getNickname() != null ? assignee.getNickname() : "家人";
                        String remark = task.getRemark() != null ? task.getRemark() : "";
                        String miniProgramPath = "/pages/task/detail?id=" + task.getId();
                        
                        // 通知执行人（新任务指派）
                        wechatWorkService.sendTaskNotification(
                            task.getAssigneeId(),
                            com.family.family.entity.MessageType.TASK_ASSIGNED,
                            task.getTitle(),
                            remark,
                            creatorName,
                            assigneeName,
                            creatorName,
                            miniProgramPath
                        );
                        
                        // 通知创建者（任务已指派确认）
                        wechatWorkService.sendTaskNotification(
                            userId,
                            com.family.family.entity.MessageType.TASK_ASSIGN_NOTIFY,
                            task.getTitle(),
                            remark,
                            creatorName,
                            assigneeName,
                            creatorName,
                            miniProgramPath
                        );
                    }
                }
            } catch (Exception e) {
                // 推送失败不影响主流程
                System.err.println("[WechatWork] 任务指派推送失败: " + e.getMessage());
            }
            // =============================
            
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
            
            // ===== 企业微信推送通知 =====
            try {
                // 获取完成者信息
                User completer = userMapper.selectById(userId);
                String completerName = completer != null ? completer.getNickname() : "家人";
                
                // 通知自己（完成者）
                wechatWorkService.notifyTaskCompletedToSelf(
                    userId,
                    task.getTitle()
                );
                
                // 如果创建者不是自己，通知创建者
                if (task.getCreatorId() != null && !task.getCreatorId().equals(userId)) {
                    wechatWorkService.notifyTaskCompletedToCreator(
                        task.getCreatorId(),
                        completerName,
                        task.getTitle()
                    );
                }
            } catch (Exception e) {
                // 推送失败不影响主流程
                System.err.println("[WechatWork] 任务完成推送失败: " + e.getMessage());
            }
            // =============================

            result.put("code", 200);
            result.put("message", "success");
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "操作失败：" + e.getMessage());
        }
        return result;
    }

    
    /**
     * 恢复任务（从已完成状态恢复为待办）
     */
    @PostMapping("/restore/{id}")
    public Map<String, Object> restore(@PathVariable Long id) {
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

            // 恢复任务：将状态从已完成(2)改为待办(0)
            task.setStatus(0);
            task.setFinishTime(null);
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