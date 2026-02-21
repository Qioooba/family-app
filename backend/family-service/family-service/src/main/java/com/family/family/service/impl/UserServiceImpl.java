package com.family.family.service.impl;

import com.family.family.dto.UserLoginDTO;
import com.family.family.dto.UserRegisterDTO;
import com.family.family.entity.User;
import com.family.family.mapper.UserMapper;
import com.family.family.service.UserService;
import com.family.family.vo.UserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现
 */
@Service
public class UserServiceImpl implements UserService {
    
    private final UserMapper userMapper;
    
    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }
    
    @Override
    public UserVO register(UserRegisterDTO dto) {
        // 简单实现
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword());
        user.setNickname(dto.getNickname());
        user.setPhone(dto.getPhone());
        userMapper.insert(user);
        
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }
    
    @Override
    public String login(UserLoginDTO dto) {
        return "token";
    }
    
    @Override
    public UserVO getCurrentUser() {
        return new UserVO();
    }
    
    @Override
    public UserVO getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return null;
        }
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }
    
    @Override
    public UserVO updateUser(UserVO vo) {
        return vo;
    }
    
    @Override
    public void changePassword(String oldPassword, String newPassword) {
    }
    
    @Override
    public void sendSmsCode(String phone) {
    }
}
