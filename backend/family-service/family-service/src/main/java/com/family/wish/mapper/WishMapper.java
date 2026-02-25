package com.family.wish.mapper;

import com.family.family.entity.Wish;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface WishMapper extends BaseMapper<Wish> {
    
    @Select("SELECT id, family_id, user_id, type, title, description, budget_min, budget_max, expect_date, visibility, priority, difficulty, status, claimant_id, progress, images, finish_time, create_time, update_time, status FROM wish WHERE family_id = #{familyId} AND status != 3 ORDER BY priority DESC, create_time DESC")
    List<Wish> selectByFamilyId(Long familyId);
    
    @Select("SELECT id, family_id, user_id, type, title, description, budget_min, budget_max, expect_date, visibility, priority, difficulty, status, claimant_id, progress, images, finish_time, create_time, update_time, status FROM wish WHERE user_id = #{userId} AND status = 2 ORDER BY finish_time DESC LIMIT 10")
    List<Wish> selectCompletedByUserId(Long userId);
}
