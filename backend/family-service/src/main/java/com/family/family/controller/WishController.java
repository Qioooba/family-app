package com.family.family.controller;
import cn.dev33.satoken.annotation.SaCheckLogin;

import com.family.family.entity.User;
import com.family.family.entity.Wish;
import com.family.family.mapper.UserMapper;
import com.family.family.mapper.WishMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 心愿控制器
 */
@RestController
@RequestMapping("/api/wish")
@SaCheckLogin
public class WishController {

    @Autowired
    private WishMapper wishMapper;
    
    @Autowired
    private UserMapper userMapper;

    /**
     * 获取当前用户ID (简化版，默认返回1)
     */
    private Long getCurrentUserId() {
        // 尝试从请求头获取用户ID
        // 默认返回1，如果数据库中没有用户1，会自动创建
        return 1L;
    }

    /**
     * 获取心愿列表
     * @param familyId 家庭ID
     * @param status 状态筛选：0-待实现(待认领+进行中), 1-已实现
     */
    @GetMapping("/list")
    public Map<String, Object> list(@RequestParam Long familyId, 
                                    @RequestParam(required = false) Integer status) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Wish> wrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Wish>()
                    .eq(Wish::getFamilyId, familyId);
            
            // 根据状态筛选
            if (status != null) {
                if (status == 0) {
                    // 待实现：待认领(0) 或 进行中(1)
                    wrapper.in(Wish::getStatus, Arrays.asList(0, 1));
                } else if (status == 1) {
                    // 已实现：已完成(2)
                    wrapper.eq(Wish::getStatus, 2);
                }
            }
            
            wrapper.orderByDesc(Wish::getCreateTime);
            
            List<Wish> allWishes = wishMapper.selectList(wrapper);
            
            List<Map<String, Object>> wishList = new ArrayList<>();
            for (Wish wish : allWishes) {
                Map<String, Object> wishMap = convertToMap(wish);
                
                // 获取创建者信息
                if (wish.getUserId() != null) {
                    User creator = userMapper.selectById(wish.getUserId());
                    if (creator != null) {
                        wishMap.put("creatorName", creator.getNickname());
                        wishMap.put("creatorAvatar", creator.getAvatar());
                    }
                }
                
                // 获取认领人信息
                if (wish.getClaimantId() != null) {
                    User claimant = userMapper.selectById(wish.getClaimantId());
                    if (claimant != null) {
                        wishMap.put("claimantName", claimant.getNickname());
                    }
                }
                
                wishList.add(wishMap);
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
            wish.setVisibility((String) data.getOrDefault("visibility", "couple"));
            
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
            
            wish.setPriority(data.get("priority") != null ? Integer.parseInt(data.get("priority").toString()) : 0);
            wish.setDifficulty(data.get("difficulty") != null ? Integer.parseInt(data.get("difficulty").toString()) : 1);
            wish.setStatus(0);
            wish.setProgress(0);
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
        
        try {
            Long id = Long.parseLong(data.get("id").toString());
            Wish wish = wishMapper.selectById(id);
            
            if (wish == null) {
                result.put("code", 404);
                result.put("message", "心愿不存在");
                return result;
            }
            
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
     * 删除心愿
     */
    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            wishMapper.deleteById(id);
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
    public Map<String, Object> claim(@PathVariable Long wishId) {
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
            
            wish.setClaimantId(currentUserId);
            wish.setStatus(1);
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
        
        try {
            Wish wish = wishMapper.selectById(wishId);
            
            if (wish == null) {
                result.put("code", 404);
                result.put("message", "心愿不存在");
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
        
        try {
            Wish wish = wishMapper.selectById(wishId);
            
            if (wish == null) {
                result.put("code", 404);
                result.put("message", "心愿不存在");
                return result;
            }
            
            wish.setStatus(2);
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
        
        String statusText = "待认领";
        if (wish.getStatus() == 1) statusText = "进行中";
        else if (wish.getStatus() == 2) statusText = "已完成";
        map.put("statusText", statusText);
        
        return map;
    }
}
