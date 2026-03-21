package com.family.family.mapper;

import com.family.family.entity.TaskTagRelation;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TaskTagRelationMapper {

    @Insert("INSERT INTO task_tag_relation (task_id, tag_id) VALUES (#{taskId}, #{tagId})")
    int insertRelation(TaskTagRelation relation);

    @Delete("DELETE FROM task_tag_relation WHERE task_id = #{taskId}")
    int deleteByTaskId(@Param("taskId") Long taskId);

    @Delete("DELETE FROM task_tag_relation WHERE tag_id = #{tagId}")
    int deleteByTagId(@Param("tagId") Long tagId);

    @Delete("DELETE FROM task_tag_relation WHERE task_id = #{taskId} AND tag_id = #{tagId}")
    int deleteByTaskIdAndTagId(@Param("taskId") Long taskId, @Param("tagId") Long tagId);

    @Select("SELECT tag_id FROM task_tag_relation WHERE task_id = #{taskId}")
    List<Long> selectTagIdsByTaskId(@Param("taskId") Long taskId);

    @Select("SELECT task_id FROM task_tag_relation WHERE tag_id = #{tagId}")
    List<Long> selectTaskIdsByTagId(@Param("tagId") Long tagId);
}
