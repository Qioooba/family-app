package com.family.family.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.family.family.entity.TaskTagRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TaskTagRelationMapper extends BaseMapper<TaskTagRelation> {
    
    @Select("SELECT tag_id FROM task_tag_relation WHERE task_id = #{taskId}")
    List<Long> selectTagIdsByTaskId(@Param("taskId") Long taskId);
    
    @Select("SELECT task_id FROM task_tag_relation WHERE tag_id = #{tagId}")
    List<Long> selectTaskIdsByTagId(@Param("tagId") Long tagId);
}
