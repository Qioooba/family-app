package com.family.file.controller;

import com.family.common.core.Result;
import com.family.file.dto.*;
import com.family.file.service.FileRecordService;
import com.family.file.vo.FileUploadInitVO;
import com.family.file.vo.FileVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 文件控制器
 */
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileRecordController {
    
    private final FileRecordService fileService;
    
    /**
     * 初始化分片上传
     */
    @PostMapping("/upload/init")
    public Result<FileUploadInitVO> initUpload(@RequestBody FileUploadInitDTO dto) {
        return Result.success(fileService.initUpload(dto));
    }
    
    /**
     * 上传分片
     */
    @PostMapping("/upload/chunk")
    public Result<Void> uploadChunk(
            @RequestParam String uploadId,
            @RequestParam Integer chunkIndex,
            @RequestParam Integer totalChunks,
            @RequestParam MultipartFile file) throws IOException {
        FileChunkUploadDTO dto = new FileChunkUploadDTO();
        dto.setUploadId(uploadId);
        dto.setChunkIndex(chunkIndex);
        dto.setTotalChunks(totalChunks);
        fileService.uploadChunk(uploadId, chunkIndex, file);
        return Result.success();
    }
    
    /**
     * 合并分片
     */
    @PostMapping("/upload/merge")
    public Result<FileVO> mergeChunks(@RequestBody FileMergeDTO dto) throws IOException {
        return Result.success(fileService.mergeChunks(dto));
    }
    
    /**
     * 简单上传(小文件)
     */
    @PostMapping("/upload/simple")
    public Result<FileVO> simpleUpload(
            @RequestParam MultipartFile file,
            @RequestParam(required = false) Long folderId,
            @RequestParam(required = false) Integer permission) throws IOException {
        return Result.success(fileService.simpleUpload(file, folderId, permission));
    }
    
    /**
     * 下载文件
     */
    @GetMapping("/download/{fileId}")
    public void downloadFile(@PathVariable Long fileId, HttpServletResponse response) throws IOException {
        fileService.downloadFile(fileId, response);
    }
    
    /**
     * 删除文件
     */
    @DeleteMapping("/{fileId}")
    public Result<Void> deleteFile(@PathVariable Long fileId) {
        fileService.deleteFile(fileId);
        return Result.success();
    }
    
    /**
     * 移动文件
     */
    @PostMapping("/move")
    public Result<FileVO> moveFile(@RequestBody FileMoveDTO dto) {
        return Result.success(fileService.moveFile(dto));
    }
    
    /**
     * 更新文件权限
     */
    @PostMapping("/permission")
    public Result<FileVO> updatePermission(@RequestBody FilePermissionDTO dto) {
        return Result.success(fileService.updatePermission(dto));
    }
    
    /**
     * 获取文件列表
     */
    @GetMapping("/list")
    public Result<List<FileVO>> getFileList(@RequestParam(required = false) Long folderId) {
        return Result.success(fileService.getFileList(folderId));
    }
    
    /**
     * 获取文件详情
     */
    @GetMapping("/{fileId}")
    public Result<FileVO> getFileDetail(@PathVariable Long fileId) {
        return Result.success(fileService.getFileDetail(fileId));
    }
    
    /**
     * 重命名文件
     */
    @PostMapping("/rename/{fileId}")
    public Result<FileVO> renameFile(@PathVariable Long fileId, @RequestParam String newName) {
        return Result.success(fileService.renameFile(fileId, newName));
    }
}
