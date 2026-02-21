package com.family.wish.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.family.wish.entity.Wish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface WishMapper extends BaseMapper<Wish> {
    
    @Select("SELECT * FROM wish WHERE family_id = #{familyId} AND status != 3 ORDER BY priority DESC, create_time DESC")
    List<Wish> selectByFamilyId(Long familyId);
    
    @Select("SELECT * FROM wish WHERE user_id = #{userId} AND status = 2 ORDER BY finish_time DESC LIMIT 10")
    List<Wish> selectCompletedByUserId(Long userId);
}
