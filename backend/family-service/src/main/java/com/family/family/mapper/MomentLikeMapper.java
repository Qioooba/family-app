package com.family.family.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.family.family.entity.MomentLike;
import org.apache.ibatis.annotations.Mapper;

/**
 * 动态点赞Mapper
 */
@Mapper
public interface MomentLikeMapper extends BaseMapper<MomentLike> {
}
