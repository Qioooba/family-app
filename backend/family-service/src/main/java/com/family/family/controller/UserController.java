package com.family.family.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.family.common.annotation.RateLimit;
import com.family.family.entity.User;
import com.family.family.service.InviteCodeService;
import com.family.family.entity.FamilyMember;
import com.family.family.mapper.UserMapper;
import com.family.family.mapper.FamilyMemberMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private FamilyMemberMapper familyMemberMapper;
    
    @Autowired
    private InviteCodeService inviteCodeService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    // 默认家庭ID - 所有用户自动加入这个家庭
    private static final Long DEFAULT_FAMILY_ID = 1L;
    
    // 文件上传配置
    @Value("${file.upload.path:./uploads}")
    private String uploadPath;
    
    @Value("${file.upload.base-url:http://qioba.cn:3000}")
    private String baseUrl;

    @GetMapping("/info")
    @SaCheckLogin
    public Map<String, Object> info() {
        Map<String, Object> result = new HashMap<>();
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            String tokenValue = StpUtil.getTokenValue();

            // 检查 token 是否有效
            if (!StpUtil.isLogin()) {
                result.put("code", 401);
                result.put("message", "登录已过期，请重新登录");
                return result;
            }

            // 从数据库查询用户信息
            User user = userMapper.selectById(userId);
            if (user == null) {
                // Token 有效但用户不存在，可能是用户被删除
                result.put("code", 404);
                result.put("message", "用户数据不存在，请联系管理员");
                result.put("errorType", "USER_NOT_FOUND");
                return result;
            }

            result.put("code", 200);
            result.put("message", "success");
            Map<String, Object> data = new HashMap<>();
            data.put("id", user.getId());
            data.put("userId", user.getId());
            data.put("username", user.getUsername());
            data.put("nickname", user.getNickname());
            data.put("phone", user.getPhone());
            data.put("avatar", user.getAvatar());
            data.put("currentFamilyId", user.getCurrentFamilyId());
            data.put("token", tokenValue);
            result.put("data", data);
        } catch (Exception e) {
            // 捕获 token 解析异常
            if (e.getMessage() != null && e.getMessage().contains("token")) {
                result.put("code", 401);
                result.put("message", "登录已过期，请重新登录");
            } else {
                result.put("code", 401);
                result.put("message", "未登录或登录已过期");
            }
        }
        return result;
    }
    
    @PutMapping("/info")
    @SaCheckLogin
    public Map<String, Object> updateInfo(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            
            // 查询当前用户
            User user = userMapper.selectById(userId);
            if (user == null) {
                result.put("code", 404);
                result.put("message", "用户不存在");
                return result;
            }
            
            // 更新可选字段
            if (params.containsKey("nickname")) {
                String newNickname = (String) params.get("nickname");
                user.setNickname(newNickname);
                
                // 同步更新所有家庭成员记录中的昵称
                List<FamilyMember> memberRecords = familyMemberMapper.selectByUserId(userId);
                for (FamilyMember member : memberRecords) {
                    member.setNickname(newNickname);
                    familyMemberMapper.updateById(member);
                }
            }
            if (params.containsKey("phone")) {
                user.setPhone((String) params.get("phone"));
            }
            if (params.containsKey("avatar")) {
                user.setAvatar((String) params.get("avatar"));
            }
            
            user.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(user);
            
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", user);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "更新失败: " + e.getMessage());
        }
        return result;
    }

    @PostMapping("/login")
    @SaIgnore
    @RateLimit(qps = 5.0, message = "登录请求过于频繁，请稍后再试")
    public Map<String, Object> login(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 获取登录凭证
            String phone = (String) params.get("phone");
            String username = (String) params.get("username");
            String password = (String) params.get("password");

            // 兼容处理：如果 phone 为空，尝试使用 username
            if (phone == null || phone.isEmpty()) {
                phone = username;
            }

            // 校验参数
            if ((phone == null || phone.isEmpty()) && (username == null || username.isEmpty())) {
                result.put("code", 400);
                result.put("message", "手机号/用户名不能为空");
                return result;
            }

            if (password == null || password.isEmpty()) {
                result.put("code", 400);
                result.put("message", "密码不能为空");
                return result;
            }

            // 根据 phone 或 username 查询用户
            User user = null;
            if (phone != null && !phone.isEmpty()) {
                LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(User::getPhone, phone);
                user = userMapper.selectOne(queryWrapper);
            }

            if (user == null && username != null && !username.isEmpty()) {
                LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(User::getUsername, username);
                user = userMapper.selectOne(queryWrapper);
            }

            // 用户不存在
            if (user == null) {
                result.put("code", 401);
                result.put("message", "用户名或密码错误");
                return result;
            }

            // 校验密码
            if (user.getPassword() == null || user.getPassword().isEmpty()) {
                result.put("code", 401);
                result.put("message", "用户密码未设置，请联系管理员");
                return result;
            }

            // 验证密码
            if (!passwordEncoder.matches(password, user.getPassword())) {
                result.put("code", 401);
                result.put("message", "用户名或密码错误");
                return result;
            }

            // 检查用户状态
            if (user.getStatus() != null && user.getStatus() == 0) {
                result.put("code", 403);
                result.put("message", "账号已被禁用");
                return result;
            }

            // 检查并设置默认家庭（如果没有current_family_id）
            if (user.getCurrentFamilyId() == null) {
                user.setCurrentFamilyId(DEFAULT_FAMILY_ID);
                // 检查是否已经是家庭成员，如果不是则添加
                FamilyMember existingMember = familyMemberMapper.selectByUserIdAndFamilyId(user.getId(), DEFAULT_FAMILY_ID);
                if (existingMember == null) {
                    FamilyMember familyMember = new FamilyMember();
                    familyMember.setFamilyId(DEFAULT_FAMILY_ID);
                    familyMember.setUserId(user.getId());
                    familyMember.setRole("member");
                    familyMember.setNickname(user.getNickname());
                    familyMember.setJoinTime(LocalDateTime.now());
                    familyMemberMapper.insert(familyMember);
                }
            }

            // 执行Sa-Token登录
            StpUtil.login(user.getId());
            String tokenValue = StpUtil.getTokenValue();

            // 更新最后登录时间和current_family_id（如果需要）
            user.setLastLoginTime(LocalDateTime.now());
            userMapper.updateById(user);

            result.put("code", 200);
            result.put("message", "success");
            Map<String, Object> data = new HashMap<>();
            data.put("token", tokenValue);
            data.put("userId", user.getId());
            data.put("nickname", user.getNickname());
            data.put("currentFamilyId", user.getCurrentFamilyId());
            result.put("data", data);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "登录失败：" + e.getMessage());
        }
        return result;
    }

    @PostMapping("/register")
    @SaIgnore
    @RateLimit(qps = 3.0, message = "注册请求过于频繁，请稍后再试")
    public Map<String, Object> register(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            String phone = (String) params.get("phone");
            String username = (String) params.get("username");
            String password = (String) params.get("password");
            String nickname = (String) params.get("nickname");
            String inviteCode = (String) params.get("inviteCode");

            // 校验邀请码（必填）
            if (inviteCode == null || inviteCode.trim().isEmpty()) {
                result.put("code", 400);
                result.put("message", "请输入邀请码");
                return result;
            }

            // 验证邀请码是否有效
            com.family.family.entity.InviteCode verifiedCode = inviteCodeService.verifyInviteCode(inviteCode.trim());
            if (verifiedCode == null) {
                result.put("code", 400);
                result.put("message", "邀请码无效或已过期");
                return result;
            }

            // 校验手机号或用户名
            if ((phone == null || phone.isEmpty()) && (username == null || username.isEmpty())) {
                result.put("code", 400);
                result.put("message", "手机号或用户名至少填写一个");
                return result;
            }

            // 校验昵称（必填）
            if (nickname == null || nickname.trim().isEmpty()) {
                result.put("code", 400);
                result.put("message", "请填写昵称");
                return result;
            }

            // 校验密码
            if (password == null || password.isEmpty()) {
                result.put("code", 400);
                result.put("message", "密码不能为空");
                return result;
            }

            // 检查手机号是否已存在
            if (phone != null && !phone.isEmpty()) {
                LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(User::getPhone, phone);
                User existingUser = userMapper.selectOne(queryWrapper);
                if (existingUser != null) {
                    result.put("code", 409);
                    result.put("message", "手机号已被注册");
                    return result;
                }
            }

            // 检查用户名是否已存在
            if (username != null && !username.isEmpty()) {
                LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(User::getUsername, username);
                User existingUser = userMapper.selectOne(queryWrapper);
                if (existingUser != null) {
                    result.put("code", 409);
                    result.put("message", "用户名已被注册");
                    return result;
                }
            }

            // 创建新用户
            User user = new User();
            user.setPhone(phone);
            user.setUsername(username != null ? username : phone);
            user.setNickname(nickname);
            user.setPassword(passwordEncoder.encode(password));
            user.setStatus(1); // 正常状态
            user.setCurrentFamilyId(verifiedCode.getFamilyId()); // 设置为邀请码对应的家庭
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());

            userMapper.insert(user);

            // 自动将用户加入邀请码对应的家庭
            FamilyMember familyMember = new FamilyMember();
            familyMember.setFamilyId(verifiedCode.getFamilyId());
            familyMember.setUserId(user.getId());
            familyMember.setRole("member");
            familyMember.setNickname(nickname);
            familyMember.setJoinTime(LocalDateTime.now());
            familyMemberMapper.insert(familyMember);

            // 更新邀请码使用次数
            verifiedCode.setUsedCount(verifiedCode.getUsedCount() + 1);
            inviteCodeService.updateById(verifiedCode);

            result.put("code", 200);
            result.put("message", "success");
            Map<String, Object> data = new HashMap<>();
            data.put("id", user.getId());
            data.put("phone", user.getPhone());
            data.put("username", user.getUsername());
            data.put("nickname", user.getNickname());
            data.put("currentFamilyId", user.getCurrentFamilyId());
            result.put("data", data);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "注册失败：" + e.getMessage());
        }
        return result;
    }

    @PostMapping("/logout")
    @SaCheckLogin
    public Map<String, Object> logout() {
        Map<String, Object> result = new HashMap<>();
        try {
            StpUtil.logout();
            result.put("code", 200);
            result.put("message", "退出成功");
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "退出失败：" + e.getMessage());
        }
        return result;
    }
    
    /**
     * 上传头像
     * POST /api/user/avatar
     */
    @PostMapping("/avatar")
    @SaCheckLogin
    public Map<String, Object> uploadAvatar(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        
        if (file == null || file.isEmpty()) {
            result.put("code", 400);
            result.put("message", "请选择要上传的文件");
            return result;
        }
        
        // 限制文件大小 (5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            result.put("code", 400);
            result.put("message", "文件大小不能超过5MB");
            return result;
        }
        
        // ========== 文件安全验证 ==========
        
        // 1. MIME Type 验证
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            result.put("code", 400);
            result.put("message", "只能上传图片文件");
            return result;
        }
        
        // 2. 扩展名白名单验证
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        }
        Set<String> allowedExtensions = Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp", ".bmp");
        if (!allowedExtensions.contains(extension)) {
            result.put("code", 400);
            result.put("message", "不支持的图片格式，仅支持：JPG、PNG、GIF、WebP、BMP");
            return result;
        }
        
        // 3. 文件魔数 (Magic Bytes) 验证 - 防止 polyglot 文件
        try {
            byte[] header = new byte[12];
            file.getInputStream().read(header);
            
            // 图片魔数定义
            boolean validMagic = false;
            // JPEG: FF D8 FF
            if (header[0] == (byte)0xFF && header[1] == (byte)0xD8 && header[2] == (byte)0xFF) {
                validMagic = true;
            }
            // PNG: 89 50 4E 47 0D 0A 1A 0A
            else if (header[0] == (byte)0x89 && header[1] == 0x50 && header[2] == 0x4E && header[3] == 0x47) {
                validMagic = true;
            }
            // GIF: 47 49 46 38 (GIF8)
            else if (header[0] == 0x47 && header[1] == 0x49 && header[2] == 0x46 && header[3] == 0x38) {
                validMagic = true;
            }
            // BMP: 42 4D (BM)
            else if (header[0] == 0x42 && header[1] == 0x4D) {
                validMagic = true;
            }
            // WebP: 52 49 46 46 ... 57 45 42 50 (RIFF....WEBP)
            else if (header[0] == 0x52 && header[1] == 0x49 && header[2] == 0x46 && header[3] == 0x46 
                     && header[8] == 0x57 && header[9] == 0x45 && header[10] == 0x42 && header[11] == 0x50) {
                validMagic = true;
            }
            
            if (!validMagic) {
                result.put("code", 400);
                result.put("message", "文件内容无效，不是合法的图片文件");
                return result;
            }
        } catch (IOException e) {
            result.put("code", 500);
            result.put("message", "文件读取失败");
            return result;
        }
        
        // 4. 生成安全的随机文件名（不使用用户提供的扩展名直接拼接）
        String newFileName = UUID.randomUUID().toString().replace("-", "") + extension;
        
        try {
            // 创建上传目录
            String avatarDir = uploadPath + "/avatars";
            File dir = new File(avatarDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            // 保存文件
            Path filePath = Paths.get(avatarDir, newFileName);
            Files.copy(file.getInputStream(), filePath);
            
            // 返回文件访问URL - 使用相对路径
            String fileUrl = "/api/avatars/" + newFileName;
            
            // 更新用户头像
            Long userId = StpUtil.getLoginIdAsLong();
            User user = userMapper.selectById(userId);
            if (user != null) {
                user.setAvatar(fileUrl);
                user.setUpdateTime(LocalDateTime.now());
                userMapper.updateById(user);
            }
            
            result.put("code", 200);
            result.put("message", "上传成功");
            Map<String, Object> data = new HashMap<>();
            data.put("url", fileUrl);
            result.put("data", data);
            
        } catch (IOException e) {
            result.put("code", 500);
            result.put("message", "文件保存失败: " + e.getMessage());
        }
        
        return result;
    }
}
