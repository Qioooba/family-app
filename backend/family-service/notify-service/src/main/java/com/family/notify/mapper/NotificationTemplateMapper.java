package com.family.notify.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.family.notify.entity.NotificationTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 通知模板Mapper
 */
@Mapper
public interface NotificationTemplateMapper extends BaseMapper<NotificationTemplate> {
    
    /**
     * 根据模板编码查询
     */
    @Select("SELECT * FROM notification_template WHERE code = #{code} AND deleted = 0 LIMIT 1")
    NotificationTemplate selectByCode(@Param("code") String code);
}
