package com.family.family.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.family.family.entity.SystemConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 系统配置Mapper
 */
@Mapper
public interface SystemConfigMapper extends BaseMapper<SystemConfig> {

    /**
     * 根据key查询配置
     */
    @Select("SELECT * FROM sys_config WHERE config_key = #{key} LIMIT 1")
    SystemConfig selectByKey(String key);

    /**
     * 获取所有非系统配置（用户可编辑的配置）
     */
    @Select("SELECT * FROM sys_config ORDER BY category ASC, config_key ASC")
    List<SystemConfig> selectUserConfig();
}
