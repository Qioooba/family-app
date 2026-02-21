package com.family.user.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.family.common.core.ErrorCode;
import com.family.common.core.exception.BusinessException;
import com.family.user.dto.UserLoginDTO;
import com.family.user.dto.UserRegisterDTO;
import com.family.user.entity.User;
import com.family.user.mapper.UserMapper;
import com.family.user.service.UserService;
import com.family.user.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 用户服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    
    private final StringRedisTemplate redisTemplate;
    
    @Override
    public UserVO register(UserRegisterDTO dto) {
        // 校验验证码
        String codeKey = "sms:code:" + dto.getPhone();
        String cacheCode = redisTemplate.opsForValue().get(codeKey);
        if (!dto.getCode().equals(cacheCode)) {
            throw new BusinessException("验证码错误或已过期");
        }
        
        // 检查用户名/手机号是否存在
        if (lambdaQuery().eq(User::getUsername, dto.getUsername()).count() > 0) {
            throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS);
        }
        if (lambdaQuery().eq(User::getPhone, dto.getPhone()).count() > 0) {
            throw new BusinessException("手机号已注册");
        }
        
        // 创建用户
        User user = new User();
        BeanUtil.copyProperties(dto, user);
        user.setPassword(DigestUtil.md5Hex(dto.getPassword()));
        user.setStatus(1);
        save(user);
        
        // 删除验证码
        redisTemplate.delete(codeKey);
        
        return convertToVO(user);
    }
    
    @Override
    public String login(UserLoginDTO dto) {
        User user = null;
        
        if (StrUtil.isNotBlank(dto.getUsername())) {
            // 用户名密码登录
            user = lambdaQuery()
                    .eq(User::getUsername, dto.getUsername())
                    .or()
                    .eq(User::getPhone, dto.getUsername())
                    .one();
            
            if (user == null || !DigestUtil.md5Hex(dto.getPassword()).equals(user.getPassword())) {
                throw new BusinessException(ErrorCode.USER_PASSWORD_ERROR);
            }
        } else if (StrUtil.isNotBlank(dto.getPhone())) {
            // 手机号验证码登录
            String codeKey = "sms:code:" + dto.getPhone();
            String cacheCode = redisTemplate.opsForValue().get(codeKey);
            if (!dto.getCode().equals(cacheCode)) {
                throw new BusinessException("验证码错误或已过期");
            }
            
            user = lambdaQuery().eq(User::getPhone, dto.getPhone()).one();
            if (user == null) {
                // 自动注册
                user = new User();
                user.setPhone(dto.getPhone());
                user.setNickname("用户" + dto.getPhone().substring(7));
                user.setStatus(1);
                save(user);
            }
            
            redisTemplate.delete(codeKey);
        }
        
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        
        // 登录并返回token
        StpUtil.login(user.getId());
        return StpUtil.getTokenValue();
    }
    
    @Override
    public UserVO getCurrentUser() {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return convertToVO(user);
    }
    
    @Override
    public UserVO getUserById(Long id) {
        User user = getById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return convertToVO(user);
    }
    
    @Override
    public UserVO updateUser(UserVO vo) {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        
        BeanUtil.copyProperties(vo, user, "id", "username", "password", "phone");
        updateById(user);
        
        return convertToVO(user);
    }
    
    @Override
    public void changePassword(String oldPassword, String newPassword) {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = getById(userId);
        
        if (!DigestUtil.md5Hex(oldPassword).equals(user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        
        user.setPassword(DigestUtil.md5Hex(newPassword));
        updateById(user);
    }
    
    @Override
    public void sendSmsCode(String phone) {
        // 生成6位验证码
        String code = String.valueOf((int) ((Math.random() * 9 + 1) * 100000));
        
        // 存入Redis，5分钟过期
        String codeKey = "sms:code:" + phone;
        redisTemplate.opsForValue().set(codeKey, code, 5, TimeUnit.MINUTES);
        
        log.info("发送验证码到 {}: {}", phone, code);
        // TODO: 接入短信服务商发送真实短信
    }
    
    private UserVO convertToVO(User user) {
        UserVO vo = new UserVO();
        BeanUtil.copyProperties(user, vo);
        return vo;
    }
}
