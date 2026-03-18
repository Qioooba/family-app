package com.family.family.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.json.JSONUtil;
import com.family.family.dto.request.SceneCreateRequest;
import com.family.family.entity.FamilyMember;
import com.family.family.entity.Reminder;
import com.family.family.entity.User;
import com.family.family.mapper.FamilyMemberMapper;
import com.family.family.mapper.ReminderMapper;
import com.family.family.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.family.family.service.ReminderScheduleService;
import com.family.family.service.ReminderService;
import com.family.family.service.scene.SceneHandlerFactory;
import com.family.family.service.scene.SceneReminderHandler;
import com.family.family.service.scene.SceneTemplate;
import com.family.family.vo.SceneTemplateVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 提醒管理 Controller
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/reminder", produces = "application/json;charset=UTF-8")
@SaCheckLogin
public class FamilyReminderController {
    
    @Autowired
    private ReminderService reminderService;
    
    @Autowired
    private ReminderScheduleService reminderScheduleService;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private FamilyMemberMapper familyMemberMapper;
    
    @Autowired
    private SceneHandlerFactory sceneHandlerFactory;
    
    @Autowired
    private ReminderMapper reminderMapper;
    
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    /**
     * 获取今日提醒
     */
    @GetMapping("/today")
    public Map<String, Object> getTodayReminders() {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            List<Reminder> reminders = reminderService.getTodayReminders(userId);
            return success(reminders);
        } catch (Exception e) {
            log.error("获取今日提醒失败", e);
            return error("获取失败");
        }
    }
    
    /**
     * 获取我的提醒列表
     */
    @GetMapping("/list")
    public Map<String, Object> getMyReminders() {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            List<Reminder> reminders = reminderService.getUserReminders(userId);
            return success(reminders);
        } catch (Exception e) {
            log.error("获取提醒列表失败", e);
            return error("获取失败");
        }
    }
    
    /**
     * 获取提醒详情
     */
    @GetMapping("/detail/{id}")
    public Map<String, Object> getReminderDetail(@PathVariable Long id) {
        try {
            Reminder reminder = reminderService.getById(id);
            if (reminder == null) {
                return error("提醒不存在");
            }
            return success(reminder);
        } catch (Exception e) {
            log.error("获取提醒详情失败", e);
            return error("获取失败");
        }
    }
    
    /**
     * 创建提醒
     */
    @PostMapping("/add")
    public Map<String, Object> addReminder(@RequestBody Reminder reminder) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            reminder.setCreateBy(userId);
            
            // 设置默认值
            if (reminder.getStatus() == null) {
                reminder.setStatus(1);
            }
            if (reminder.getPriority() == null) {
                reminder.setPriority(5);
            }
            if (reminder.getPushScope() == null) {
                reminder.setPushScope("SELF");
            }
            if (reminder.getReminderType() == null || reminder.getReminderType().trim().isEmpty()) {
                reminder.setReminderType("SYSTEM");
            }
            
            boolean success = reminderService.createReminder(reminder);
            if (success) {
                // 初始化下次执行时间
                reminderScheduleService.initNextExecuteTime(reminder);
                return success(null, "创建成功");
            }
            return error("创建失败");
        } catch (Exception e) {
            log.error("创建提醒失败", e);
            return error("创建失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新提醒
     */
    @PostMapping("/update")
    public Map<String, Object> updateReminder(@RequestBody Reminder reminder) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            if (!reminderService.isOwner(reminder.getId(), userId)) {
                return error("无权限修改");
            }
            
            boolean success = reminderService.updateReminder(reminder);
            if (success) {
                // 重新计算下次执行时间
                Reminder updated = reminderService.getById(reminder.getId());
                reminderScheduleService.initNextExecuteTime(updated);
                return success(null, "更新成功");
            }
            return error("更新失败");
        } catch (Exception e) {
            log.error("更新提醒失败", e);
            return error("更新失败");
        }
    }
    
    /**
     * 删除提醒
     */
    @PostMapping("/delete")
    public Map<String, Object> deleteReminder(@RequestBody Map<String, Long> params) {
        try {
            Long id = params.get("id");
            Long userId = StpUtil.getLoginIdAsLong();
            boolean success = reminderService.deleteReminder(id, userId);
            if (success) {
                return success(null, "删除成功");
            }
            return error("删除失败");
        } catch (Exception e) {
            log.error("删除提醒失败", e);
            return error("删除失败");
        }
    }
    
    /**
     * 切换状态
     */
    @PostMapping("/toggle")
    public Map<String, Object> toggleStatus(@RequestBody Map<String, Long> params) {
        try {
            Long id = params.get("id");
            Long userId = StpUtil.getLoginIdAsLong();
            boolean success = reminderService.toggleStatus(id, userId);
            if (success) {
                return success(null, "状态已切换");
            }
            return error("切换失败");
        } catch (Exception e) {
            log.error("切换状态失败", e);
            return error("切换失败");
        }
    }
    
    /**
     * 立即执行（测试用）
     */
    @PostMapping("/execute")
    public Map<String, Object> executeNow(@RequestBody Map<String, Long> params) {
        try {
            Long id = params.get("id");
            Long userId = StpUtil.getLoginIdAsLong();
            Reminder reminder = reminderService.getById(id);
            
            if (reminder == null) {
                return error("提醒不存在");
            }
            
            if (!reminder.getCreateBy().equals(userId)) {
                return error("无权限");
            }
            
            // 立即执行
            reminderScheduleService.executeReminder(reminder);
            return success(null, "执行成功");
        } catch (Exception e) {
            log.error("立即执行失败", e);
            return error("执行失败");
        }
    }
    
    /**
     * 获取频率类型选项
     */
    @GetMapping("/frequency-types")
    public Map<String, Object> getFrequencyTypes() {
        Map<String, Object> types = new HashMap<>();
        types.put("ONCE", "一次性");
        types.put("DAILY", "每天");
        types.put("HOURLY", "每小时");
        types.put("WEEKLY", "每周");
        types.put("MONTHLY", "每月");
        types.put("YEARLY", "每年");
        types.put("INTERVAL", "间隔");
        return success(types);
    }
    
    /**
     * 获取提醒类型选项
     */
    @GetMapping("/reminder-types")
    public Map<String, Object> getReminderTypes() {
        Map<String, Object> types = new HashMap<>();
        types.put("WATER", "喝水提醒");
        types.put("MEDICINE", "用药提醒");
        types.put("EXPIRE", "保质期");
        types.put("BIRTHDAY", "生日提醒");
        types.put("FINANCE", "财务提醒");
        types.put("SYSTEM", "系统提醒");
        return success(types);
    }
    
    /**
     * 获取推送范围选项
     */
    @GetMapping("/push-scopes")
    public Map<String, Object> getPushScopes() {
        Map<String, Object> scopes = new HashMap<>();
        scopes.put("SELF", "仅自己");
        scopes.put("ALL", "全部用户");
        scopes.put("SPECIFIED", "指定用户");
        return success(scopes);
    }
    
    /**
     * 获取用户列表（用于指定用户推送）
     * 只返回当前家庭的成员，使用用户昵称
     */
    @GetMapping("/users")
    public Map<String, Object> getUserList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "100") Integer limit) {
        try {
            Long currentUserId = StpUtil.getLoginIdAsLong();
            
            // 获取当前用户所在的家庭ID
            User currentUser = userMapper.selectById(currentUserId);
            Long familyId = currentUser != null ? currentUser.getCurrentFamilyId() : null;
            if (familyId == null) {
                Map<String, Object> emptyResult = new HashMap<>();
                emptyResult.put("list", List.of());
                emptyResult.put("total", 0);
                return success(emptyResult);
            }
            
            // 查询家庭成员（包含自己，前端过滤）
            List<FamilyMember> members = familyMemberMapper.selectList(
                    new LambdaQueryWrapper<FamilyMember>()
                            .eq(FamilyMember::getFamilyId, familyId)
            );
            
            // 组装成员信息，和 /api/family/{id}/members 保持一致
            List<Map<String, Object>> userList = new ArrayList<>();
            for (FamilyMember member : members) {
                // 排除当前用户自己
                if (member.getUserId().equals(currentUserId)) {
                    continue;
                }
                
                // 关键词过滤
                if (keyword != null && !keyword.trim().isEmpty()) {
                    String searchKey = keyword.toLowerCase();
                    String memberNickname = member.getNickname() != null ? member.getNickname().toLowerCase() : "";
                    if (!memberNickname.contains(searchKey)) {
                        // 获取用户昵称再判断
                        User user = userMapper.selectById(member.getUserId());
                        String userNickname = user != null && user.getNickname() != null ? user.getNickname().toLowerCase() : "";
                        if (!userNickname.contains(searchKey)) {
                            continue;
                        }
                    }
                }
                
                Map<String, Object> memberInfo = new HashMap<>();
                memberInfo.put("id", member.getUserId());  // 使用 userId 作为 id
                memberInfo.put("userId", member.getUserId());
                
                // 获取用户最新信息（头像、昵称等）
                User user = userMapper.selectById(member.getUserId());
                if (user != null) {
                    // 优先使用 User 表的最新昵称
                    String displayNickname = user.getNickname();
                    if (displayNickname == null || displayNickname.trim().isEmpty()) {
                        displayNickname = member.getNickname();
                    }
                    memberInfo.put("nickname", displayNickname);
                    memberInfo.put("avatar", user.getAvatar());
                } else {
                    memberInfo.put("nickname", member.getNickname());
                }
                
                userList.add(memberInfo);
                
                if (userList.size() >= limit) {
                    break;
                }
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("list", userList);
            result.put("total", userList.size());
            
            return success(result);
        } catch (Exception e) {
            log.error("获取用户列表失败", e);
            return error("获取用户列表失败");
        }
    }
    
    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/current-user")
    public Map<String, Object> getCurrentUser() {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            User user = userMapper.selectById(userId);
            if (user == null) {
                return error("用户不存在");
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("id", user.getId());
            result.put("nickname", user.getNickname());
            result.put("avatar", user.getAvatar());
            
            return success(result);
        } catch (Exception e) {
            log.error("获取当前用户信息失败", e);
            return error("获取失败");
        }
    }
    
    /**
     * 手机号脱敏
     */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
    
    private Map<String, Object> success(Object data) {
        return success(data, "success");
    }
    
    private Map<String, Object> success(Object data, String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("message", message);
        result.put("data", data);
        return result;
    }
    
    private Map<String, Object> error(String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 500);
        result.put("message", message);
        return result;
    }
    
    // ==================== 场景化提醒 API ====================
    
    /**
     * 获取所有场景模板
     */
    @GetMapping("/scene-templates")
    public Map<String, Object> getSceneTemplates() {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            
            List<SceneTemplateVO> templates = new ArrayList<>();
            
            for (SceneReminderHandler handler : sceneHandlerFactory.getAllHandlers()) {
                // 检查用户是否已创建该场景提醒
                Reminder existing = reminderMapper.selectOne(
                    new LambdaQueryWrapper<Reminder>()
                        .eq(Reminder::getCreateBy, userId)
                        .eq(Reminder::getReminderType, handler.getSceneType())
                        .last("LIMIT 1")
                );
                
                SceneTemplateVO vo = SceneTemplateVO.builder()
                    .sceneType(handler.getSceneType())
                    .name(handler.getSceneName())
                    .description(handler.getDefaultTemplate().getDescription())
                    .icon(handler.getIcon())
                    .bgColor(handler.getBgColor())
                    .created(existing != null)
                    .reminderId(existing != null ? existing.getId() : null)
                    .enabled(existing != null && existing.getStatus() == 1)
                    .build();
                
                templates.add(vo);
            }
            
            return success(templates);
        } catch (Exception e) {
            log.error("获取场景模板失败", e);
            return error("获取失败");
        }
    }
    
    /**
     * 一键创建场景提醒
     */
    @PostMapping("/scene/create")
    public Map<String, Object> createSceneReminder(@RequestBody @Validated SceneCreateRequest request) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            
            // 获取场景处理器
            SceneReminderHandler handler = sceneHandlerFactory.getHandler(request.getSceneType());
            if (handler == null) {
                return error("不支持的提醒场景: " + request.getSceneType());
            }
            
            // 获取默认模板
            SceneTemplate template = handler.getDefaultTemplate();
            
            // 创建提醒
            Reminder reminder = new Reminder();
            reminder.setReminderName(request.getCustomName() != null ? 
                request.getCustomName() : template.getReminderName());
            reminder.setReminderType(template.getReminderType());
            reminder.setFrequencyType(template.getFrequencyType());
            reminder.setFrequencyConfig(template.getFrequencyConfig());
            reminder.setTitleTemplate(template.getTitleTemplate());
            reminder.setContentTemplate(template.getContentTemplate());
            
            // 合并业务数据
            String businessData = template.getBusinessData();
            if (request.getCustomConfig() != null && !request.getCustomConfig().isEmpty()) {
                Map<String, Object> defaultConfig = JSONUtil.parseObj(businessData);
                defaultConfig.putAll(request.getCustomConfig());
                businessData = JSONUtil.toJsonStr(defaultConfig);
            }
            reminder.setBusinessData(businessData);
            
            reminder.setPushScope(request.getPushScope() != null ? request.getPushScope() : "SELF");
            reminder.setTargetUserIds(request.getTargetUserIds() != null ? 
                JSONUtil.toJsonStr(request.getTargetUserIds()) : null);
            reminder.setCreateBy(userId);
            reminder.setStatus(1);
            reminder.setPriority(5);
            
            boolean success = reminderService.createReminder(reminder);
            if (success) {
                // 计算下次执行时间
                reminderScheduleService.initNextExecuteTime(reminder);
                
                Map<String, Object> result = new HashMap<>();
                result.put("id", reminder.getId());
                result.put("sceneType", handler.getSceneType());
                result.put("name", reminder.getReminderName());
                
                return success(result, "创建成功");
            }
            return error("创建失败");
        } catch (Exception e) {
            log.error("创建场景提醒失败", e);
            return error("创建失败: " + e.getMessage());
        }
    }
    
    /**
     * 切换场景提醒开关（快速启用/停用）
     * 如果不存在则自动创建
     */
    @PostMapping("/scene/toggle")
    public Map<String, Object> toggleSceneReminder(@RequestBody Map<String, String> params) {
        try {
            String sceneType = params.get("sceneType");
            if (sceneType == null || sceneType.isEmpty()) {
                return error("场景类型不能为空");
            }
            
            Long userId = StpUtil.getLoginIdAsLong();
            
            // 查询是否已存在该场景提醒
            Reminder existing = reminderMapper.selectOne(
                new LambdaQueryWrapper<Reminder>()
                    .eq(Reminder::getCreateBy, userId)
                    .eq(Reminder::getReminderType, sceneType)
                    .last("LIMIT 1")
            );
            
            if (existing != null) {
                // 切换状态
                existing.setStatus(existing.getStatus() == 1 ? 0 : 1);
                reminderMapper.updateById(existing);
                
                Map<String, Object> result = new HashMap<>();
                result.put("enabled", existing.getStatus() == 1);
                result.put("reminderId", existing.getId());
                
                return success(result, existing.getStatus() == 1 ? "已开启" : "已关闭");
            } else {
                // 自动创建默认场景提醒
                SceneReminderHandler handler = sceneHandlerFactory.getHandler(sceneType);
                if (handler == null) {
                    return error("不支持的提醒场景: " + sceneType);
                }
                
                SceneTemplate template = handler.getDefaultTemplate();
                
                Reminder reminder = new Reminder();
                reminder.setReminderName(template.getReminderName());
                reminder.setReminderType(template.getReminderType());
                reminder.setFrequencyType(template.getFrequencyType());
                reminder.setFrequencyConfig(template.getFrequencyConfig());
                reminder.setTitleTemplate(template.getTitleTemplate());
                reminder.setContentTemplate(template.getContentTemplate());
                reminder.setBusinessData(template.getBusinessData());
                reminder.setPushScope("SELF");
                reminder.setCreateBy(userId);
                reminder.setStatus(1);
                reminder.setPriority(5);
                
                reminderService.createReminder(reminder);
                reminderScheduleService.initNextExecuteTime(reminder);
                
                Map<String, Object> result = new HashMap<>();
                result.put("enabled", true);
                result.put("reminderId", reminder.getId());
                
                return success(result, "已开启");
            }
        } catch (Exception e) {
            log.error("切换场景提醒失败", e);
            return error("操作失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取场景提醒配置
     */
    @GetMapping("/scene/config/{sceneType}")
    public Map<String, Object> getSceneConfig(@PathVariable String sceneType) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            
            SceneReminderHandler handler = sceneHandlerFactory.getHandler(sceneType);
            if (handler == null) {
                return error("不支持的提醒场景");
            }
            
            // 查询用户是否已配置
            Reminder existing = reminderMapper.selectOne(
                new LambdaQueryWrapper<Reminder>()
                    .eq(Reminder::getCreateBy, userId)
                    .eq(Reminder::getReminderType, sceneType)
                    .last("LIMIT 1")
            );
            
            Map<String, Object> result = new HashMap<>();
            result.put("sceneType", sceneType);
            result.put("sceneName", handler.getSceneName());
            result.put("icon", handler.getIcon());
            result.put("bgColor", handler.getBgColor());
            result.put("defaultTemplate", handler.getDefaultTemplate());
            result.put("exists", existing != null);
            
            if (existing != null) {
                result.put("reminder", existing);
                result.put("enabled", existing.getStatus() == 1);
            }
            
            return success(result);
        } catch (Exception e) {
            log.error("获取场景配置失败", e);
            return error("获取失败");
        }
    }
    
    /**
     * 更新场景提醒配置
     */
    @PostMapping("/scene/update")
    public Map<String, Object> updateSceneReminder(@RequestBody Map<String, Object> params) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            String sceneType = (String) params.get("sceneType");
            
            if (sceneType == null || sceneType.isEmpty()) {
                return error("场景类型不能为空");
            }
            
            // 查询现有提醒
            Reminder existing = reminderMapper.selectOne(
                new LambdaQueryWrapper<Reminder>()
                    .eq(Reminder::getCreateBy, userId)
                    .eq(Reminder::getReminderType, sceneType)
                    .last("LIMIT 1")
            );
            
            if (existing == null) {
                return error("该场景提醒不存在");
            }
            
            // 更新配置
            if (params.containsKey("businessData")) {
                existing.setBusinessData(JSONUtil.toJsonStr(params.get("businessData")));
            }
            if (params.containsKey("frequencyConfig")) {
                existing.setFrequencyConfig(JSONUtil.toJsonStr(params.get("frequencyConfig")));
            }
            if (params.containsKey("pushScope")) {
                existing.setPushScope((String) params.get("pushScope"));
            }
            
            reminderMapper.updateById(existing);
            
            // 重新计算下次执行时间
            reminderScheduleService.initNextExecuteTime(existing);
            
            return success(null, "更新成功");
        } catch (Exception e) {
            log.error("更新场景配置失败", e);
            return error("更新失败");
        }
    }
    
    /**
     * 保存用户定位信息（用于天气提醒）
     */
    @PostMapping("/scene/location")
    public Map<String, Object> saveUserLocation(@RequestBody Map<String, String> params) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            String location = params.get("location");
            
            if (location == null || location.isEmpty()) {
                return error("位置不能为空");
            }
            
            // 保存到Redis，有效期7天
            String locationKey = String.format("user:location:%d", userId);
            redisTemplate.opsForValue().set(locationKey, location, 7, java.util.concurrent.TimeUnit.DAYS);
            
            log.info("保存用户定位: userId={}, location={}", userId, location);
            
            return success(null, "定位已保存");
        } catch (Exception e) {
            log.error("保存用户定位失败", e);
            return error("保存失败");
        }
    }
    
    /**
     * 获取用户定位信息
     */
    @GetMapping("/scene/location")
    public Map<String, Object> getUserLocation() {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            String locationKey = String.format("user:location:%d", userId);
            String location = redisTemplate.opsForValue().get(locationKey);
            
            Map<String, Object> result = new HashMap<>();
            result.put("location", location);
            result.put("hasLocation", location != null);
            
            return success(result);
        } catch (Exception e) {
            log.error("获取用户定位失败", e);
            return error("获取失败");
        }
    }
}
