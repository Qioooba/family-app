package com.family.user.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    @Override
    public UserVO register(UserRegisterDTO dto) {
        String codeKey = null;
        // 如果提供了验证码，则校验
        if (StrUtil.isNotBlank(dto.getCode()) && StrUtil.isNotBlank(dto.getPhone())) {
            codeKey = "sms:code:" + dto.getPhone();
            String cacheCode = redisTemplate.opsForValue().get(codeKey);
            if (!dto.getCode().equals(cacheCode)) {
                throw new BusinessException("验证码错误或已过期");
            }
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
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setStatus(1);
        save(user);
        
        // 删除验证码
        if (codeKey != null) {
            redisTemplate.delete(codeKey);
        }
        
        return convertToVO(user);
    }
    
    @Override
    public String login(UserLoginDTO dto) {
        User user = null;
        
        // 根据登录类型进行不同的验证
        if ("password".equals(dto.getLoginType())) {
            // 用户名密码登录
            if (StrUtil.isBlank(dto.getUsername())) {
                throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "用户名不能为空");
            }
            if (StrUtil.isBlank(dto.getPassword())) {
                throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "密码不能为空");
            }
            
            user = lambdaQuery()
                    .eq(User::getUsername, dto.getUsername())
                    .or()
                    .eq(User::getPhone, dto.getUsername())
                    .one();
            
            if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
                throw new BusinessException(ErrorCode.USER_PASSWORD_ERROR.getCode(), "用户名或密码错误");
            }
        } else if ("sms".equals(dto.getLoginType())) {
            // 手机号验证码登录
            if (StrUtil.isBlank(dto.getPhone())) {
                throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "手机号不能为空");
            }
            if (StrUtil.isBlank(dto.getCode())) {
                throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "验证码不能为空");
            }
            
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
        } else {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "不支持的登录类型");
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
        
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
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
