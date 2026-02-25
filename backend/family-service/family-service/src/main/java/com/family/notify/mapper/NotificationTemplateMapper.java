package com.family.notify.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.family.family.entity.NotificationTemplate;
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
    @Select("SELECT id, code, name, type, channels, title, content, email_content, sms_content, wx_template_id, wx_template_data, variables, is_system, create_user_id, description, create_time, update_time, status FROM notification_template WHERE code = #{code} AND deleted = 0 LIMIT 1")
    NotificationTemplate selectByCode(@Param("code") String code);
}
