package com.family.family.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.family.common.core.Result;
import com.family.family.entity.User;
import com.family.family.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户企业微信绑定管理
 */
@Slf4j
@RestController
@RequestMapping("/api/user/wechat-work")
public class WechatWorkBindController {

    @Autowired
    private UserMapper userMapper;

    /**
     * 获取当前用户的企业微信绑定状态
     */
    @GetMapping("/status")
    public Result getBindStatus() {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = userMapper.selectById(userId);
        
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("workUserId", user.getWorkUserId());
        data.put("externalUserId", user.getExternalUserId());
        data.put("hasWorkUserId", user.getWorkUserId() != null && !user.getWorkUserId().isEmpty());
        data.put("hasExternalUserId", user.getExternalUserId() != null && !user.getExternalUserId().isEmpty());
        data.put("canReceiveMessage", 
            (user.getWorkUserId() != null && !user.getWorkUserId().isEmpty()) ||
            (user.getExternalUserId() != null && !user.getExternalUserId().isEmpty()));
        
        return Result.success(data);
    }

    /**
     * 绑定企业微信外部联系人ID
     * 用户在企业微信中关注应用后，可以绑定自己的外部联系人ID
     */
    @PostMapping("/bind-external")
    public Result bindExternalUserId(@RequestBody Map<String, String> params) {
        Long userId = StpUtil.getLoginIdAsLong();
        String externalUserId = params.get("externalUserId");
        
        if (externalUserId == null || externalUserId.trim().isEmpty()) {
            return Result.error("外部联系人ID不能为空");
        }
        
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        // 更新外部联系人ID
        user.setExternalUserId(externalUserId.trim());
        userMapper.updateById(user);
        
        log.info("用户{}绑定了企业微信外部联系人ID: {}", userId, externalUserId);
        
        return Result.success("绑定成功");
    }

    /**
     * 绑定企业微信内部成员ID
     * 用于企业内部成员
     */
    @PostMapping("/bind-work")
    public Result bindWorkUserId(@RequestBody Map<String, String> params) {
        Long userId = StpUtil.getLoginIdAsLong();
        String workUserId = params.get("workUserId");
        
        if (workUserId == null || workUserId.trim().isEmpty()) {
            return Result.error("企业微信成员ID不能为空");
        }
        
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        // 更新内部成员ID
        user.setWorkUserId(workUserId.trim());
        userMapper.updateById(user);
        
        log.info("用户{}绑定了企业微信内部成员ID: {}", userId, workUserId);
        
        return Result.success("绑定成功");
    }

    /**
     * 解绑企业微信
     */
    @PostMapping("/unbind")
    public Result unbind(@RequestBody Map<String, String> params) {
        Long userId = StpUtil.getLoginIdAsLong();
        String type = params.get("type"); // "work" | "external" | "all"
        
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        if ("work".equals(type)) {
            user.setWorkUserId(null);
        } else if ("external".equals(type)) {
            user.setExternalUserId(null);
        } else {
            user.setWorkUserId(null);
            user.setExternalUserId(null);
        }
        
        userMapper.updateById(user);
        log.info("用户{}解绑了企业微信, type={}", userId, type);
        
        return Result.success("解绑成功");
    }
}
