package com.family.notify.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.family.family.entity.NotificationSetting;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 通知设置Mapper
 */
@Mapper
public interface NotificationSettingMapper extends BaseMapper<NotificationSetting> {
    
    /**
     * 根据用户ID查询设置
     */
    @Select("SELECT id, user_id, enable_in_app, enable_email, enable_sms, enable_wx, notify_system, notify_announcement, notify_reminder, notify_activity, notify_task, quiet_start_time, quiet_end_time, enable_quiet, create_time, update_time, status FROM notification_setting WHERE user_id = #{userId} AND deleted = 0 LIMIT 1")
    NotificationSetting selectByUserId(@Param("userId") Long userId);
}
