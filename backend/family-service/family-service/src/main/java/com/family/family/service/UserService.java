package com.family.family.service;

import com.family.family.dto.UserLoginDTO;
import com.family.family.dto.UserRegisterDTO;
import com.family.family.vo.UserVO;

/**
 * 用户服务
 */
public interface UserService {
    
    UserVO register(UserRegisterDTO dto);
    
    String login(UserLoginDTO dto);
    
    UserVO getCurrentUser();
    
    UserVO getUserById(Long id);
    
    UserVO updateUser(UserVO vo);
    
    void changePassword(String oldPassword, String newPassword);
    
    void sendSmsCode(String phone);
}
