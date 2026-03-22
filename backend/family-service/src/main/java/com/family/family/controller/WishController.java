package com.family.family.controller;
import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;

import com.family.family.dto.request.WishCreateRequest;
import com.family.family.dto.request.WishProgressUpdateRequest;
import com.family.family.dto.request.WishUpdateRequest;
import com.family.family.entity.User;
import com.family.family.entity.Wish;
import com.family.family.mapper.UserMapper;
import com.family.family.mapper.WishMapper;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 心愿控制器
 */
@RestController
@RequestMapping("/api/wish")
@SaCheckLogin
public class WishController {

    private static final Logger log = LoggerFactory.getLogger(WishController.class);

    @Autowired
    private WishMapper wishMapper;
    
    @Autowired
    private UserMapper userMapper;

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        return StpUtil.getLoginIdAsLong();
    }

    private boolean canModifyWish(Wish wish, Long currentUserId) {
        if (wish == null || currentUserId == null) {
            return false;
        }
        return currentUserId.equals(wish.getUserId()) || currentUserId.equals(wish.getClaimantId());
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
            Map<Long, User> userMap = loadUsers(allWishes);
            
            List<Map<String, Object>> wishList = new ArrayList<>();
            for (Wish wish : allWishes) {
                Map<String, Object> wishMap = convertToMap(wish);
                
                // 获取创建者信息
                if (wish.getUserId() != null) {
                    User creator = userMap.get(wish.getUserId());
                    if (creator != null) {
                        wishMap.put("creatorName", creator.getNickname());
                        wishMap.put("creatorAvatar", creator.getAvatar());
                    }
                }
                
                // 获取认领人信息
                if (wish.getClaimantId() != null) {
                    User claimant = userMap.get(wish.getClaimantId());
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
            log.error("查询心愿列表失败: familyId={}, status={}", familyId, status, e);
            result.put("code", 500);
            result.put("message", "查询失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 创建心愿
     */
    @PostMapping("/create")
    public Map<String, Object> create(@Valid @RequestBody WishCreateRequest request) {
        Map<String, Object> result = new HashMap<>();
        Long currentUserId = getCurrentUserId();
        
        try {
            Wish wish = new Wish();
            validateBudgetRange(request.getBudgetMin(), request.getBudgetMax());

            wish.setFamilyId(request.getFamilyId());
            wish.setUserId(currentUserId);
            wish.setTitle(request.getTitle().trim());
            wish.setDescription(request.getDescription());
            wish.setType(request.getType());
            wish.setVisibility(request.getVisibility() == null || request.getVisibility().isBlank() ? "couple" : request.getVisibility());
            wish.setBudgetMin(request.getBudgetMin());
            wish.setBudgetMax(request.getBudgetMax());
            wish.setExpectDate(parseDate(request.getExpectDate()));
            wish.setImages(request.getImages());
            wish.setPriority(request.getPriority() == null ? 0 : request.getPriority());
            wish.setDifficulty(request.getDifficulty() == null ? 1 : request.getDifficulty());
            wish.setStatus(0);
            wish.setProgress(0);
            wish.setCreateTime(LocalDateTime.now());
            wish.setUpdateTime(LocalDateTime.now());
            
            wishMapper.insert(wish);
            
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", convertToMap(wish));
        } catch (Exception e) {
            log.error("创建心愿失败: currentUserId={}", currentUserId, e);
            result.put("code", 500);
            result.put("message", "创建失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 更新心愿
     */
    @PutMapping("/update")
    public Map<String, Object> update(@Valid @RequestBody WishUpdateRequest request) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Long id = request.getId();
            Wish wish = wishMapper.selectById(id);
            
            if (wish == null) {
                result.put("code", 404);
                result.put("message", "心愿不存在");
                return result;
            }
            Long currentUserId = getCurrentUserId();
            if (!currentUserId.equals(wish.getUserId())) {
                result.put("code", 403);
                result.put("message", "无权限修改该心愿");
                return result;
            }

            validateBudgetRange(request.getBudgetMin(), request.getBudgetMax());
            
            if (request.getTitle() != null) {
                wish.setTitle(request.getTitle().trim());
            }
            if (request.getDescription() != null) {
                wish.setDescription(request.getDescription());
            }
            if (request.getType() != null) {
                wish.setType(request.getType());
            }
            if (request.getVisibility() != null) {
                wish.setVisibility(request.getVisibility());
            }
            if (request.getBudgetMin() != null) {
                wish.setBudgetMin(request.getBudgetMin());
            }
            if (request.getBudgetMax() != null) {
                wish.setBudgetMax(request.getBudgetMax());
            }
            if (request.getExpectDate() != null) {
                wish.setExpectDate(parseDate(request.getExpectDate()));
            }
            if (request.getImages() != null) {
                wish.setImages(request.getImages());
            }
            if (request.getPriority() != null) {
                wish.setPriority(request.getPriority());
            }
            
            wish.setUpdateTime(LocalDateTime.now());
            wishMapper.updateById(wish);
            
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", convertToMap(wish));
        } catch (Exception e) {
            log.error("更新心愿失败", e);
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
            Wish wish = wishMapper.selectById(id);
            if (wish == null) {
                result.put("code", 404);
                result.put("message", "心愿不存在");
                return result;
            }
            Long currentUserId = getCurrentUserId();
            if (!currentUserId.equals(wish.getUserId())) {
                result.put("code", 403);
                result.put("message", "无权限删除该心愿");
                return result;
            }

            wishMapper.deleteById(id);
            result.put("code", 200);
            result.put("message", "success");
        } catch (Exception e) {
            log.error("删除心愿失败: id={}", id, e);
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
            log.error("认领心愿失败: wishId={}, currentUserId={}", wishId, currentUserId, e);
            result.put("code", 500);
            result.put("message", "认领失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 更新进度
     */
    @PostMapping("/progress/{wishId}")
    public Map<String, Object> updateProgress(@PathVariable Long wishId,
                                              @Valid @RequestBody WishProgressUpdateRequest request) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Wish wish = wishMapper.selectById(wishId);
            
            if (wish == null) {
                result.put("code", 404);
                result.put("message", "心愿不存在");
                return result;
            }
            
            Long currentUserId = getCurrentUserId();
            if (!canModifyWish(wish, currentUserId)) {
                result.put("code", 403);
                result.put("message", "无权限更新该心愿");
                return result;
            }

            wish.setProgress(request.getProgress());
            
            wish.setUpdateTime(LocalDateTime.now());
            wishMapper.updateById(wish);
            
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", convertToMap(wish));
        } catch (Exception e) {
            log.error("更新心愿进度失败: wishId={}", wishId, e);
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
            
            Long currentUserId = getCurrentUserId();
            if (!currentUserId.equals(wish.getUserId())) {
                result.put("code", 403);
                result.put("message", "无权限完成该心愿");
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
            log.error("完成心愿失败: wishId={}", wishId, e);
            result.put("code", 500);
            result.put("message", "操作失败：" + e.getMessage());
        }
        return result;
    }

    private Map<Long, User> loadUsers(List<Wish> wishes) {
        Set<Long> userIds = wishes.stream()
            .flatMap(wish -> Arrays.stream(new Long[]{wish.getUserId(), wish.getClaimantId()}))
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return userMapper.selectBatchIds(userIds).stream()
            .collect(Collectors.toMap(User::getId, Function.identity()));
    }

    private void validateBudgetRange(BigDecimal budgetMin, BigDecimal budgetMax) {
        if (budgetMin != null && budgetMax != null && budgetMin.compareTo(budgetMax) > 0) {
            throw new IllegalArgumentException("最低预算不能大于最高预算");
        }
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDate.parse(value);
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
