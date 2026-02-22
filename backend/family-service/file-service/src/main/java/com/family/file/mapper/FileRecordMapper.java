package com.family.file.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.family.file.entity.FileRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 文件记录Mapper
 */
@Mapper
public interface FileRecordMapper extends BaseMapper<FileRecord> {
    
    /**
     * 根据MD5获取文件
     */
    @Select("SELECT * FROM file_record WHERE file_md5 = #{md5} AND status = 1 LIMIT 1")
    FileRecord selectByMd5(@Param("md5") String md5);
    
    /**
     * 获取文件夹下的文件列表
     */
    @Select("SELECT * FROM file_record WHERE folder_id = #{folderId} AND status = 1 AND is_latest = 1 ORDER BY create_time DESC")
    List<FileRecord> selectByFolderId(@Param("folderId") Long folderId);
    
    /**
     * 获取用户的文件列表
     */
    @Select("SELECT * FROM file_record WHERE uploader_id = #{uploaderId} AND status = 1 AND is_latest = 1 ORDER BY create_time DESC")
    List<FileRecord> selectByUploaderId(@Param("uploaderId") Long uploaderId);
    
    /**
     * 更新文件为最新版本
     */
    @Update("UPDATE file_record SET is_latest = 1 WHERE id = #{fileId}")
    int updateToLatest(@Param("fileId") Long fileId);
    
    /**
     * 取消文件最新版本标记
     */
    @Update("UPDATE file_record SET is_latest = 0 WHERE id = #{fileId}")
    int cancelLatest(@Param("fileId") Long fileId);
    
    /**
     * 增加下载次数
     */
    @Update("UPDATE file_record SET download_count = download_count + 1 WHERE id = #{fileId}")
    int incrementDownloadCount(@Param("fileId") Long fileId);
}
