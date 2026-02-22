package com.family.notify.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.family.notify.entity.NotificationSetting;
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
    @Select("SELECT * FROM notification_setting WHERE user_id = #{userId} AND deleted = 0 LIMIT 1")
    NotificationSetting selectByUserId(@Param("userId") Long userId);
}
