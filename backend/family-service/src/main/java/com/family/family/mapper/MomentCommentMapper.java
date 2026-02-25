package com.family.family.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.family.family.entity.MomentComment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 动态评论Mapper
 */
@Mapper
public interface MomentCommentMapper extends BaseMapper<MomentComment> {
}
