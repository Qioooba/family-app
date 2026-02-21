package com.family.family.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.family.family.entity.FamilyReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 家庭报告Mapper
 */
@Mapper
public interface FamilyReportMapper extends BaseMapper<FamilyReport> {
    
    /**
     * 查询家庭的报告列表
     */
    @Select("SELECT * FROM family_report WHERE family_id = #{familyId} ORDER BY create_time DESC")
    List<FamilyReport> selectByFamilyId(@Param("familyId") Long familyId);
}