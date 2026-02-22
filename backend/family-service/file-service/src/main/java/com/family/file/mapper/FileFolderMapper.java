package com.family.file.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.family.file.entity.FileFolder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 文件夹Mapper
 */
@Mapper
public interface FileFolderMapper extends BaseMapper<FileFolder> {
    
    /**
     * 获取用户的所有文件夹
     */
    @Select("SELECT * FROM file_folder WHERE creator_id = #{userId} AND status = 1 ORDER BY create_time DESC")
    List<FileFolder> selectByCreatorId(@Param("userId") Long userId);
    
    /**
     * 获取父文件夹下的子文件夹
     */
    @Select("SELECT * FROM file_folder WHERE parent_id = #{parentId} AND status = 1 ORDER BY create_time DESC")
    List<FileFolder> selectByParentId(@Param("parentId") Long parentId);
}
