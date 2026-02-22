package com.family.family.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.family.family.entity.FamilyMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface FamilyMemberMapper extends BaseMapper<FamilyMember> {
    
    /**
     * 根据用户ID和家庭ID查询成员信息
     */
    @Select("SELECT * FROM family_member WHERE user_id = #{userId} AND family_id = #{familyId}")
    FamilyMember selectByUserIdAndFamilyId(@Param("userId") Long userId, @Param("familyId") Long familyId);
}
