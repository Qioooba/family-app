package com.family.file.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.family.file.entity.FileShare;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 文件分享Mapper
 */
@Mapper
public interface FileShareMapper extends BaseMapper<FileShare> {
    
    /**
     * 根据分享链接查询
     */
    @Select("SELECT id, file_id, share_type, sharer_id, share_user_id, share_link, access_password, permission, expire_time, access_count, last_access_time, create_time, update_time, status FROM file_share WHERE share_link = #{shareLink} AND status = 1")
    FileShare selectByShareLink(@Param("shareLink") String shareLink);
    
    /**
     * 获取用户分享的文件列表
     */
    @Select("SELECT id, file_id, share_type, sharer_id, share_user_id, share_link, access_password, permission, expire_time, access_count, last_access_time, create_time, update_time, status FROM file_share WHERE sharer_id = #{sharerId} AND status = 1 ORDER BY create_time DESC")
    List<FileShare> selectBySharerId(@Param("sharerId") Long sharerId);
    
    /**
     * 获取分享给用户的文件列表
     */
    @Select("SELECT id, file_id, share_type, sharer_id, share_user_id, share_link, access_password, permission, expire_time, access_count, last_access_time, create_time, update_time, status FROM file_share WHERE share_user_id = #{userId} AND status = 1 ORDER BY create_time DESC")
    List<FileShare> selectByShareUserId(@Param("userId") Long userId);
    
    /**
     * 获取文件的分享列表
     */
    @Select("SELECT id, file_id, share_type, sharer_id, share_user_id, share_link, access_password, permission, expire_time, access_count, last_access_time, create_time, update_time, status FROM file_share WHERE file_id = #{fileId} AND status = 1 ORDER BY create_time DESC")
    List<FileShare> selectByFileId(@Param("fileId") Long fileId);
    
    /**
     * 增加访问次数
     */
    @Update("UPDATE file_share SET access_count = access_count + 1 WHERE id = #{shareId}")
    int incrementAccessCount(@Param("shareId") Long shareId);
}
