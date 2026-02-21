package com.family.family.controller;

import com.family.common.core.Result;
import com.family.family.dto.UserLoginDTO;
import com.family.family.dto.UserRegisterDTO;
import com.family.family.service.UserService;
import com.family.family.vo.UserVO;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/user")
public class UserController {
    
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    @PostMapping("/register")
    public Result<UserVO> register(@RequestBody UserRegisterDTO dto) {
        return Result.success(userService.register(dto));
    }
    
    @PostMapping("/login")
    public Result<String> login(@RequestBody UserLoginDTO dto) {
        return Result.success(userService.login(dto));
    }
    
    @PostMapping("/logout")
    public Result<Void> logout() {
        // Sa-Token会自动处理
        return Result.success();
    }
    
    @GetMapping("/info")
    public Result<UserVO> getCurrentUser() {
        return Result.success(userService.getCurrentUser());
    }
    
    @GetMapping("/{id}")
    public Result<UserVO> getUserById(@PathVariable Long id) {
        return Result.success(userService.getUserById(id));
    }
    
    @PutMapping("/info")
    public Result<UserVO> updateUser(@RequestBody UserVO vo) {
        return Result.success(userService.updateUser(vo));
    }
    
    @PostMapping("/password")
    public Result<Void> changePassword(@RequestParam String oldPassword, @RequestParam String newPassword) {
        userService.changePassword(oldPassword, newPassword);
        return Result.success();
    }
    
    @PostMapping("/sms-code")
    public Result<Void> sendSmsCode(@RequestParam String phone) {
        userService.sendSmsCode(phone);
        return Result.success();
    }
    
    /**
     * 切换当前家庭
     * POST /api/user/switch-family
     */
    @PostMapping("/switch-family")
    public Result<Void> switchFamily(@RequestBody SwitchFamilyRequest request) {
        // 从Sa-Token获取当前用户ID
        Long userId = 1L; // TODO: 使用Sa-Token获取
        userService.switchFamily(userId, request.getFamilyId());
        return Result.success();
    }
    
    public static class SwitchFamilyRequest {
        private Long familyId;
        
        public Long getFamilyId() { return familyId; }
        public void setFamilyId(Long familyId) { this.familyId = familyId; }
    }
}
}
