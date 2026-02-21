package com.family.family.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.family.family.entity.UserPoints;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户积分Mapper
 */
@Mapper
public interface UserPointsMapper extends BaseMapper<UserPoints> {
}
