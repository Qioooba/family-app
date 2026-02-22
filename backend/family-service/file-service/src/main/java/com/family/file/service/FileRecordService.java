package com.family.file.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.family.file.dto.*;
import com.family.file.entity.FileRecord;
import com.family.file.vo.FileUploadInitVO;
import com.family.file.vo.FileVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 文件服务接口
 */
public interface FileRecordService extends IService<FileRecord> {
    
    /**
     * 初始化分片上传
     */
    FileUploadInitVO initUpload(FileUploadInitDTO dto);
    
    /**
     * 上传分片
     */
    void uploadChunk(String uploadId, Integer chunkIndex, MultipartFile file) throws IOException;
    
    /**
     * 合并分片
     */
    FileVO mergeChunks(FileMergeDTO dto) throws IOException;
    
    /**
     * 简单上传(小文件)
     */
    FileVO simpleUpload(MultipartFile file, Long folderId, Integer permission) throws IOException;
    
    /**
     * 下载文件
     */
    void downloadFile(Long fileId, HttpServletResponse response) throws IOException;
    
    /**
     * 删除文件
     */
    void deleteFile(Long fileId);
    
    /**
     * 移动文件
     */
    FileVO moveFile(FileMoveDTO dto);
    
    /**
     * 更新文件权限
     */
    FileVO updatePermission(FilePermissionDTO dto);
    
    /**
     * 获取文件列表(按文件夹)
     */
    List<FileVO> getFileList(Long folderId);
    
    /**
     * 获取文件详情
     */
    FileVO getFileDetail(Long fileId);
    
    /**
     * 重命名文件
     */
    FileVO renameFile(Long fileId, String newName);
}
