package com.family.user.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaIgnore;
import com.family.common.core.Result;
import com.family.user.dto.UserLoginDTO;
import com.family.user.dto.UserRegisterDTO;
import com.family.user.service.UserService;
import com.family.user.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@SaCheckLogin
public class UserController {
    
    private final UserService userService;
    
    @PostMapping("/register")
    @SaIgnore
    public Result<UserVO> register(@Valid @RequestBody UserRegisterDTO dto) {
        return Result.success(userService.register(dto));
    }
    
    @PostMapping("/login")
    @SaIgnore
    public Result<String> login(@Valid @RequestBody UserLoginDTO dto) {
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
    
    @GetMapping("/detail")
    public Result<UserVO> getCurrentUserDetail() {
        return Result.success(userService.getCurrentUser());
    }
    
    @GetMapping("/stats")
    public Result<java.util.Map<String, Object>> getUserStats() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalPoints", 0);
        stats.put("completedTasks", 0);
        stats.put("loginDays", 1);
        stats.put("familyCount", 1);
        return Result.success(stats);
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
    
    @PostMapping("/change-password")
    public Result<Void> changePasswordAlias(@RequestParam String oldPassword, @RequestParam String newPassword) {
        userService.changePassword(oldPassword, newPassword);
        return Result.success();
    }
    
    @PostMapping("/sms-code")
    @SaIgnore
    public Result<Void> sendSmsCode(@RequestParam String phone) {
        userService.sendSmsCode(phone);
        return Result.success();
    }
}
