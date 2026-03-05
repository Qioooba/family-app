package com.family.family.controller;

import com.family.family.entity.User;
import com.family.family.entity.Wish;
import com.family.family.mapper.UserMapper;
import com.family.family.mapper.WishMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 心愿控制器 - 情侣专属功能
 */
@RestController
@RequestMapping("/api/wish")
public class WishController {

    @Autowired
    private WishMapper wishMapper;
    
    @Autowired
    private UserMapper userMapper;

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        try {
            // 从Sa-Token获取当前用户ID
            Object userId = com.dev33.satoken.StpUtil.getLoginId();
            if (userId != null) {
                return Long.parseLong(userId.toString());
            }
        } catch (Exception e) {
            // 如果获取不到，返回默认用户ID
        }
        return 1L;
    }

    /**
     * 获取当前用户信息
     */
    private User getCurrentUser() {
        Long userId = getCurrentUserId();
        return userMapper.selectById(userId);
    }

    /**
     * 检查用户是否是情侣关系
     */
    private boolean isCouple(Long userId1, Long userId2) {
        if (userId1 == null || userId2 == null) return false;
        
        User user1 = userMapper.selectById(userId1);
        User user2 = userMapper.selectById(userId2);
        
        if (user1 == null || user2 == null) return false;
        
        // 如果两个用户ID相同，认为是情侣（自己也可以看到）
        // 实际业务中可以通过家庭成员关系判断
        return user1.getFamilyId() != null && 
               user1.getFamilyId().equals(user2.getFamilyId());
    }

    /**
     * 获取心愿列表 - 只显示情侣可见的心愿
     */
    @GetMapping("/list")
    public Map<String, Object> list(@RequestParam Long familyId) {
        Map<String, Object> result = new HashMap<>();
        Long currentUserId = getCurrentUserId();
        
        try {
            // 查询该家庭的心愿，只返回情侣可见的
            List<Wish> allWishes = wishMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Wish>()
                    .eq(Wish::getFamilyId, familyId)
                    .eq(Wish::getIsDeleted, 0)
                    .orderByDesc(Wish::getCreateTime)
            );
            
            // 过滤只显示情侣可见的心愿
            List<Map<String, Object>> wishList = new ArrayList<>();
            for (Wish wish : allWishes) {
                // visibility: "couple" 只情侣可见, "family" 全家可见
                if ("couple".equals(wish.getVisibility()) || wish.getVisibility() == null) {
                    Map<String, Object> wishMap = convertToMap(wish);
                    
                    // 获取创建者信息
                    if (wish.getUserId() != null) {
                        User creator = userMapper.selectById(wish.getUserId());
                        if (creator != null) {
                            wishMap.put("creatorName", creator.getNickname() || creator.getUsername());
                            wishMap.put("creatorAvatar", creator.getAvatar());
                        }
                    }
                    
                    // 获取认领人信息
                    if (wish.getClaimantId() != null) {
                        User claimant = userMapper.selectById(wish.getClaimantId());
                        if (claimant != null) {
                            wishMap.put("claimantName", claimant.getNickname() || claimant.getUsername());
                        }
                    }
                    
                    wishList.add(wishMap);
                }
            }
            
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", wishList);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("message", "查询失败：" + e.getMessage());
        }
        return result;
    }
    
    /**
     * 获取我的心愿（我创建的和认领的）
     */
    @GetMapping("/my")
    public Map<String, Object> myList(@RequestParam Long familyId) {
        Map<String, Object> result = new HashMap<>();
        Long currentUserId = getCurrentUserId();
        
        try {
            List<Wish> myWishes = wishMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Wish>()
                    .eq(Wish::getFamilyId, familyId)
                    .eq(Wish::getIsDeleted, 0)
                    .and(w -> w.eq(Wish::getUserId, currentUserId).or().eq(Wish::getClaimantId, currentUserId))
                    .orderByDesc(Wish::getCreateTime)
            );
            
            List<Map<String, Object>> wishList = new ArrayList<>();
            for (Wish wish : myWishes) {
                wishList.add(convertToMap(wish));
            }
            
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", wishList);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("message", "查询失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 获取心愿详情
     */
    @GetMapping("/detail")
    public Map<String, Object> detail(@RequestParam Long id) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Wish wish = wishMapper.selectById(id);
            if (wish == null) {
                result.put("code", 404);
                result.put("message", "心愿不存在");
                return result;
            }
            
            // 检查权限 - 情侣或创建者/认领者可见
            Long currentUserId = getCurrentUserId();
            boolean hasPermission = "family".equals(wish.getVisibility()) ||
                    wish.getUserId().equals(currentUserId) ||
                    (wish.getClaimantId() != null && wish.getClaimantId().equals(currentUserId));
            
            if (!hasPermission) {
                result.put("code", 403);
                result.put("message", "无权查看此心愿");
                return result;
            }
            
            Map<String, Object> wishMap = convertToMap(wish);
            
            // 获取创建者信息
            if (wish.getUserId() != null) {
                User creator = userMapper.selectById(wish.getUserId());
                if (creator != null) {
                    wishMap.put("creatorName", creator.getNickname() || creator.getUsername());
                    wishMap.put("creatorAvatar", creator.getAvatar());
                }
            }
            
            // 获取认领人信息
            if (wish.getClaimantId() != null) {
                User claimant = userMapper.selectById(wish.getClaimantId());
                if (claimant != null) {
                    wishMap.put("claimantName", claimant.getNickname() || claimant.getUsername());
                }
            }
            
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", wishMap);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("message", "查询失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 创建心愿
     */
    @PostMapping("/create")
    public Map<String, Object> create(@RequestBody Map<String, Object> data) {
        Map<String, Object> result = new HashMap<>();
        Long currentUserId = getCurrentUserId();
        
        try {
            Wish wish = new Wish();
            wish.setFamilyId(Long.parseLong(data.get("familyId").toString()));
            wish.setUserId(currentUserId);
            wish.setTitle((String) data.get("title"));
            wish.setDescription((String) data.get("description"));
            wish.setType((String) data.get("type"));
            wish.setVisibility((String) data.getOrDefault("visibility", "couple")); // 默认情侣可见
            
            // 处理预算
            if (data.get("budgetMin") != null) {
                wish.setBudgetMin(new java.math.BigDecimal(data.get("budgetMin").toString()));
            }
            if (data.get("budgetMax") != null) {
                wish.setBudgetMax(new java.math.BigDecimal(data.get("budgetMax").toString()));
            }
            
            // 处理期望日期
            if (data.get("expectDate") != null) {
                wish.setExpectDate(java.time.LocalDate.parse(data.get("expectDate").toString()));
            }
            
            // 处理图片
            if (data.get("images") != null) {
                wish.setImages((String) data.get("images"));
            }
            
            wish.setPriority(data.get("priority") != null ? 
                Integer.parseInt(data.get("priority").toString()) : 0);
            wish.setDifficulty(data.get("difficulty") != null ? 
                Integer.parseInt(data.get("difficulty").toString()) : 1);
            wish.setStatus(0); // 待认领
            wish.setProgress(0);
            wish.setIsDeleted(0);
            wish.setCreateTime(LocalDateTime.now());
            wish.setUpdateTime(LocalDateTime.now());
            
            wishMapper.insert(wish);
            
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", convertToMap(wish));
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("message", "创建失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 更新心愿
     */
    @PutMapping("/update")
    public Map<String, Object> update(@RequestBody Map<String, Object> data) {
        Map<String, Object> result = new HashMap<>();
        Long currentUserId = getCurrentUserId();
        
        try {
            Long id = Long.parseLong(data.get("id").toString());
            Wish wish = wishMapper.selectById(id);
            
            if (wish == null) {
                result.put("code", 404);
                result.put("message", "心愿不存在");
                return result;
            }
            
            // 只有创建者可以修改
            if (!wish.getUserId().equals(currentUserId)) {
                result.put("code", 403);
                result.put("message", "无权修改此心愿");
                return result;
            }
            
            // 更新字段
            if (data.get("title") != null) {
                wish.setTitle((String) data.get("title"));
            }
            if (data.get("description") != null) {
                wish.setDescription((String) data.get("description"));
            }
            if (data.get("type") != null) {
                wish.setType((String) data.get("type"));
            }
            if (data.get("visibility") != null) {
                wish.setVisibility((String) data.get("visibility"));
            }
            if (data.get("budgetMin") != null) {
                wish.setBudgetMin(new java.math.BigDecimal(data.get("budgetMin").toString()));
            }
            if (data.get("budgetMax") != null) {
                wish.setBudgetMax(new java.math.BigDecimal(data.get("budgetMax").toString()));
            }
            if (data.get("expectDate") != null) {
                wish.setExpectDate(java.time.LocalDate.parse(data.get("expectDate").toString()));
            }
            if (data.get("images") != null) {
                wish.setImages((String) data.get("images"));
            }
            if (data.get("priority") != null) {
                wish.setPriority(Integer.parseInt(data.get("priority").toString()));
            }
            if (data.get("difficulty") != null) {
                wish.setDifficulty(Integer.parseInt(data.get("difficulty").toString()));
            }
            
            wish.setUpdateTime(LocalDateTime.now());
            wishMapper.updateById(wish);
            
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", convertToMap(wish));
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("message", "更新失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 删除心愿 - 只有创建者可以删除
     */
    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        Long currentUserId = getCurrentUserId();
        
        try {
            Wish wish = wishMapper.selectById(id);
            
            if (wish == null) {
                result.put("code", 404);
                result.put("message", "心愿不存在");
                return result;
            }
            
            // 只有创建者可以删除
            if (!wish.getUserId().equals(currentUserId)) {
                result.put("code", 403);
                result.put("message", "无权删除此心愿");
                return result;
            }
            
            // 软删除
            wish.setIsDeleted(1);
            wish.setDeleteTime(LocalDateTime.now());
            wishMapper.updateById(wish);
            
            result.put("code", 200);
            result.put("message", "success");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("message", "删除失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 认领心愿
     */
    @PostMapping("/claim/{wishId}")
    public Map<String, Object> claim(@PathVariable Long wishId, @RequestBody Map<String, Object> data) {
        Map<String, Object> result = new HashMap<>();
        Long currentUserId = getCurrentUserId();
        
        try {
            Wish wish = wishMapper.selectById(wishId);
            
            if (wish == null) {
                result.put("code", 404);
                result.put("message", "心愿不存在");
                return result;
            }
            
            if (wish.getStatus() != 0) {
                result.put("code", 400);
                result.put("message", "该心愿已被认领或已完成");
                return result;
            }
            
            // 情侣才能认领
            if (!isCouple(wish.getUserId(), currentUserId)) {
                result.put("code", 403);
                result.put("message", "只有情侣才能认领心愿");
                return result;
            }
            
            wish.setClaimantId(currentUserId);
            wish.setStatus(1); // 进行中
            wish.setUpdateTime(LocalDateTime.now());
            wishMapper.updateById(wish);
            
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", convertToMap(wish));
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("message", "认领失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 更新进度
     */
    @PostMapping("/progress/{wishId}")
    public Map<String, Object> updateProgress(@PathVariable Long wishId, @RequestBody Map<String, Object> data) {
        Map<String, Object> result = new HashMap<>();
        Long currentUserId = getCurrentUserId();
        
        try {
            Wish wish = wishMapper.selectById(wishId);
            
            if (wish == null) {
                result.put("code", 404);
                result.put("message", "心愿不存在");
                return result;
            }
            
            // 只有创建者或认领者可以更新进度
            if (!wish.getUserId().equals(currentUserId) && 
                !wish.getClaimantId().equals(currentUserId)) {
                result.put("code", 403);
                result.put("message", "无权更新此心愿");
                return result;
            }
            
            if (data.get("progress") != null) {
                wish.setProgress(Integer.parseInt(data.get("progress").toString()));
            }
            
            wish.setUpdateTime(LocalDateTime.now());
            wishMapper.updateById(wish);
            
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", convertToMap(wish));
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("message", "更新失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 完成心愿
     */
    @PostMapping("/complete/{wishId}")
    public Map<String, Object> complete(@PathVariable Long wishId) {
        Map<String, Object> result = new HashMap<>();
        Long currentUserId = getCurrentUserId();
        
        try {
            Wish wish = wishMapper.selectById(wishId);
            
            if (wish == null) {
                result.put("code", 404);
                result.put("message", "心愿不存在");
                return result;
            }
            
            // 只有创建者或认领者可以标记完成
            if (!wish.getUserId().equals(currentUserId) && 
                !wish.getClaimantId().equals(currentUserId)) {
                result.put("code", 403);
                result.put("message", "无权操作此心愿");
                return result;
            }
            
            wish.setStatus(2); // 已完成
            wish.setProgress(100);
            wish.setFinishTime(java.time.LocalDate.now());
            wish.setUpdateTime(LocalDateTime.now());
            wishMapper.updateById(wish);
            
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", convertToMap(wish));
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("message", "操作失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 放弃认领
     */
    @PostMapping("/abandon/{wishId}")
    public Map<String, Object> abandon(@PathVariable Long wishId) {
        Map<String, Object> result = new HashMap<>();
        Long currentUserId = getCurrentUserId();
        
        try {
            Wish wish = wishMapper.selectById(wishId);
            
            if (wish == null) {
                result.put("code", 404);
                result.put("message", "心愿不存在");
                return result;
            }
            
            // 只有认领者可以放弃
            if (!wish.getClaimantId().equals(currentUserId)) {
                result.put("code", 403);
                result.put("message", "只有认领者可以放弃");
                return result;
            }
            
            wish.setClaimantId(null);
            wish.setStatus(0); // 恢复待认领
            wish.setUpdateTime(LocalDateTime.now());
            wishMapper.updateById(wish);
            
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", convertToMap(wish));
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("message", "操作失败：" + e.getMessage());
        }
        return result;
    }
    
    /**
     * 取消认领（退还）
     */
    @PostMapping("/cancel/{wishId}")
    public Map<String, Object> cancel(@PathVariable Long wishId) {
        Map<String, Object> result = new HashMap<>();
        Long currentUserId = getCurrentUserId();
        
        try {
            Wish wish = wishMapper.selectById(wishId);
            
            if (wish == null) {
                result.put("code", 404);
                result.put("message", "心愿不存在");
                return result;
            }
            
            // 创建者可以取消认领
            if (!wish.getUserId().equals(currentUserId)) {
                result.put("code", 403);
                result.put("message", "只有创建者可以取消认领");
                return result;
            }
            
            wish.setClaimantId(null);
            wish.setStatus(0); // 恢复待认领
            wish.setProgress(0);
            wish.setUpdateTime(LocalDateTime.now());
            wishMapper.updateById(wish);
            
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", convertToMap(wish));
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("message", "操作失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 转换Wish为Map
     */
    private Map<String, Object> convertToMap(Wish wish) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", wish.getId());
        map.put("familyId", wish.getFamilyId());
        map.put("userId", wish.getUserId());
        map.put("type", wish.getType());
        map.put("title", wish.getTitle());
        map.put("description", wish.getDescription());
        map.put("budgetMin", wish.getBudgetMin());
        map.put("budgetMax", wish.getBudgetMax());
        map.put("expectDate", wish.getExpectDate());
        map.put("visibility", wish.getVisibility());
        map.put("priority", wish.getPriority());
        map.put("difficulty", wish.getDifficulty());
        map.put("status", wish.getStatus());
        map.put("claimantId", wish.getClaimantId());
        map.put("progress", wish.getProgress());
        map.put("images", wish.getImages());
        map.put("finishTime", wish.getFinishTime());
        map.put("createTime", wish.getCreateTime());
        map.put("updateTime", wish.getUpdateTime());
        
        // 状态文本
        String statusText = "待认领";
        if (wish.getStatus() == 1) statusText = "进行中";
        else if (wish.getStatus() == 2) statusText = "已完成";
        map.put("statusText", statusText);
        
        return map;
    }
}
