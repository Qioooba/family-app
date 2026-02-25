package com.family.file.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.family.family.entity.FileVersion;
import com.family.file.vo.FileVersionVO;

import java.util.List;

/**
 * 文件版本服务接口
 */
public interface FileVersionService extends IService<FileVersion> {
    
    /**
     * 获取文件的所有版本
     */
    List<FileVersionVO> getFileVersions(Long fileId);
    
    /**
     * 回滚到指定版本
     */
    FileVersionVO rollbackVersion(Long fileId, Integer version);
    
    /**
     * 删除版本
     */
    void deleteVersion(Long versionId);
    
    /**
     * 创建新版本
     */
    FileVersion createVersion(Long fileId, String storagePath, Long fileSize, String fileMd5, String description);
}
