package com.family.family.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.family.family.entity.Task;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

/**
 * 任务Mapper
 */
@Mapper
public interface TaskMapper extends BaseMapper<Task> {
    
    /**
     * 查询用户今日待办任务数量
     */
    @Select("SELECT COUNT(*) FROM task WHERE creator_id = #{userId} AND DATE(create_time) = CURDATE() AND status != 2")
    int countTodayTodos(@Param("userId") Long userId);
}
