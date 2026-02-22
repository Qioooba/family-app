package com.family.file.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.family.file.entity.FileVersion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 文件版本Mapper
 */
@Mapper
public interface FileVersionMapper extends BaseMapper<FileVersion> {
    
    /**
     * 获取文件的所有版本
     */
    @Select("SELECT * FROM file_version WHERE file_id = #{fileId} AND status = 1 ORDER BY version DESC")
    List<FileVersion> selectByFileId(@Param("fileId") Long fileId);
    
    /**
     * 获取文件的最新版本号
     */
    @Select("SELECT MAX(version) FROM file_version WHERE file_id = #{fileId} AND status = 1")
    Integer selectMaxVersion(@Param("fileId") Long fileId);
    
    /**
     * 获取指定版本的文件
     */
    @Select("SELECT * FROM file_version WHERE file_id = #{fileId} AND version = #{version} AND status = 1")
    FileVersion selectByFileIdAndVersion(@Param("fileId") Long fileId, @Param("version") Integer version);
}
