package com.family.family.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.family.family.entity.Reminder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 提醒 Mapper
 */
@Mapper
public interface ReminderMapper extends BaseMapper<Reminder> {
    
    /**
     * 查询需要执行的提醒（下次执行时间<=当前时间）
     */
    @Select("SELECT * FROM reminder_config WHERE status = 1 AND next_execute_time <= #{now} ORDER BY priority DESC")
    List<Reminder> selectDueReminders(@Param("now") LocalDateTime now);
    
    /**
     * 查询用户的提醒列表
     */
    @Select("SELECT * FROM reminder_config WHERE create_by = #{userId} ORDER BY create_time DESC")
    List<Reminder> selectByUserId(@Param("userId") Long userId);
}
