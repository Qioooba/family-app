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
     * 限制最多100条，防止一次性处理过多任务
     */
    @Select("SELECT * FROM reminder_config WHERE status = 1 AND next_execute_time <= #{now} ORDER BY priority DESC, next_execute_time ASC LIMIT 100")
    List<Reminder> selectDueReminders(@Param("now") LocalDateTime now);
    
    /**
     * 查询用户的提醒列表
     * 限制最多100条
     */
    @Select("SELECT * FROM reminder_config WHERE create_by = #{userId} ORDER BY create_time DESC LIMIT 100")
    List<Reminder> selectByUserId(@Param("userId") Long userId);
    
    /**
     * 查询指定用户作为目标的提醒列表
     * 限制最多100条
     */
    @Select("SELECT * FROM reminder_config WHERE status = 1 AND push_scope = 'ALL' OR (push_scope = 'SPECIFIED' AND target_user_ids LIKE CONCAT('%', #{userId}, '%')) ORDER BY next_execute_time ASC LIMIT 100")
    List<Reminder> selectByTargetUserId(@Param("userId") Long userId);
    
    /**
     * 查询所有启用的提醒
     * 限制最多100条
     */
    @Select("SELECT * FROM reminder_config WHERE status = 1 ORDER BY priority DESC LIMIT 100")
    List<Reminder> selectAllActive();
}
