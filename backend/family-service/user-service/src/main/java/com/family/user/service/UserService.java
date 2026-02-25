package com.family.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.family.user.dto.UserLoginDTO;
import com.family.user.dto.UserRegisterDTO;
import com.family.user.entity.User;
import com.family.user.vo.UserVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {
    
    /**
     * 用户注册
     */
    UserVO register(UserRegisterDTO dto);
    
    /**
     * 用户登录
     */
    String login(UserLoginDTO dto);
    
    /**
     * 获取当前登录用户
     */
    UserVO getCurrentUser();
    
    /**
     * 根据ID获取用户信息
     */
    UserVO getUserById(Long id);
    
    /**
     * 更新用户信息
     */
    UserVO updateUser(UserVO vo);
    
    /**
     * 修改密码
     */
    void changePassword(String oldPassword, String newPassword);
    
    /**
     * 发送验证码
     */
    void sendSmsCode(String phone);
    
    /**
     * 上传头像
     */
    String uploadAvatar(MultipartFile file);
}
