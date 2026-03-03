package com.family.family.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.family.family.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    // 根据手机号查询用户
    @Select("SELECT * FROM sys_user WHERE phone = #{phone} LIMIT 1")
    User selectByPhone(String phone);
    
    // 根据微信OpenID查询用户
    @Select("SELECT * FROM sys_user WHERE wx_openid = #{wxOpenid} LIMIT 1")
    User selectByWxOpenid(String wxOpenid);
}
