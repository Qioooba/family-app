package com.family.file.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.family.common.core.ErrorCode;
import com.family.common.core.exception.BusinessException;
import com.family.file.config.FileStorageProperties;
import com.family.file.config.FileUploadProperties;
import com.family.file.dto.*;
import com.family.family.entity.FileRecord;
import com.family.file.enums.FilePermissionEnum;
import com.family.file.enums.FileTypeEnum;
import com.family.file.mapper.FileRecordMapper;
import com.family.file.service.FileRecordService;
import com.family.file.service.FileVersionService;
import com.family.file.vo.FileUploadInitVO;
import com.family.file.vo.FileVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 文件服务实现
 */
@Slf4j
@Service
public class FileRecordServiceImpl extends ServiceImpl<FileRecordMapper, FileRecord> implements FileRecordService {
    
    private final FileStorageProperties storageProperties;
    private final FileUploadProperties uploadProperties;
    private FileVersionService fileVersionService;
    private final StringRedisTemplate redisTemplate;
    
    @Autowired
    public FileRecordServiceImpl(FileStorageProperties storageProperties, 
                                  FileUploadProperties uploadProperties,
                                  StringRedisTemplate redisTemplate) {
        this.storageProperties = storageProperties;
        this.uploadProperties = uploadProperties;
        this.redisTemplate = redisTemplate;
    }
    
    @Autowired
    @Lazy
    public void setFileVersionService(FileVersionService fileVersionService) {
        this.fileVersionService = fileVersionService;
    }
    
    private static final String UPLOAD_TASK_KEY = "file:upload:";
    
    @PostConstruct
    public void init() {
        // 确保存储目录存在
        FileUtil.mkdir(storageProperties.getLocal().getPath());
        FileUtil.mkdir(storageProperties.getLocal().getChunkPath());
    }
    
    @Override
    public FileUploadInitVO initUpload(FileUploadInitDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        // 检查秒传
        FileRecord existingFile = baseMapper.selectByMd5(dto.getFileMd5());
        if (existingFile != null) {
            FileUploadInitVO vo = new FileUploadInitVO();
            vo.setExisting(true);
            vo.setExistingFileId(existingFile.getId());
            return vo;
        }
        
        // 生成上传任务ID
        String uploadId = IdUtil.simpleUUID();
        
        // 计算分片数
        int totalChunks = (int) Math.ceil((double) dto.getFileSize() / uploadProperties.getChunkSize());
        
        // 保存上传任务信息到Redis
        Map<String, String> taskInfo = new HashMap<>();
        taskInfo.put("fileName", dto.getFileName());
        taskInfo.put("fileMd5", dto.getFileMd5());
        taskInfo.put("fileSize", String.valueOf(dto.getFileSize()));
        taskInfo.put("mimeType", dto.getMimeType());
        taskInfo.put("folderId", String.valueOf(dto.getFolderId() != null ? dto.getFolderId() : 0));
        taskInfo.put("permission", String.valueOf(dto.getPermission() != null ? dto.getPermission() : FilePermissionEnum.PRIVATE.getCode()));
        taskInfo.put("uploaderId", String.valueOf(userId));
        taskInfo.put("totalChunks", String.valueOf(totalChunks));
        
        String taskKey = UPLOAD_TASK_KEY + uploadId;
        redisTemplate.opsForHash().putAll(taskKey, taskInfo);
        redisTemplate.expire(taskKey, uploadProperties.getTempExpireHours(), TimeUnit.HOURS);
        
        // 获取已上传的分片
        List<Integer> uploadedChunks = getUploadedChunks(uploadId);
        
        FileUploadInitVO vo = new FileUploadInitVO();
        vo.setUploadId(uploadId);
        vo.setTotalChunks(totalChunks);
        vo.setChunkSize(uploadProperties.getChunkSize());
        vo.setUploadedChunks(uploadedChunks);
        vo.setExisting(false);
        
        return vo;
    }
    
    @Override
    public void uploadChunk(String uploadId, Integer chunkIndex, MultipartFile file) throws IOException {
        String taskKey = UPLOAD_TASK_KEY + uploadId;
        
        // 检查任务是否存在
        if (!Boolean.TRUE.equals(redisTemplate.hasKey(taskKey))) {
            throw new BusinessException("上传任务不存在或已过期");
        }
        
        // 保存分片
        String chunkDir = storageProperties.getLocal().getChunkPath() + File.separator + uploadId;
        FileUtil.mkdir(chunkDir);
        
        String chunkFileName = chunkDir + File.separator + chunkIndex;
        file.transferTo(new File(chunkFileName));
        
        // 记录已上传的分片
        redisTemplate.opsForSet().add(taskKey + ":chunks", String.valueOf(chunkIndex));
    }
    
    @Override
    @Transactional
    public FileVO mergeChunks(FileMergeDTO dto) throws IOException {
        String uploadId = dto.getUploadId();
        String taskKey = UPLOAD_TASK_KEY + uploadId;
        
        // 获取任务信息
        Map<Object, Object> taskInfo = redisTemplate.opsForHash().entries(taskKey);
        if (taskInfo.isEmpty()) {
            throw new BusinessException("上传任务不存在或已过期");
        }
        
        String fileName = (String) taskInfo.get("fileName");
        String fileMd5 = (String) taskInfo.get("fileMd5");
        Long fileSize = Long.valueOf((String) taskInfo.get("fileSize"));
        String mimeType = (String) taskInfo.get("mimeType");
        Long folderId = Long.valueOf((String) taskInfo.get("folderId"));
        Integer permission = Integer.valueOf((String) taskInfo.get("permission"));
        Long uploaderId = Long.valueOf((String) taskInfo.get("uploaderId"));
        Integer totalChunks = Integer.valueOf((String) taskInfo.get("totalChunks"));
        
        // 检查所有分片是否上传完成
        Long uploadedCount = redisTemplate.opsForSet().size(taskKey + ":chunks");
        if (uploadedCount == null || uploadedCount.intValue() != totalChunks) {
            throw new BusinessException("分片未全部上传完成");
        }
        
        // 合并文件
        String relativePath = generateRelativePath(fileName);
        String storagePath = storageProperties.getLocal().getPath() + File.separator + relativePath;
        FileUtil.mkdir(FileUtil.getParent(storagePath, 1));
        
        String chunkDir = storageProperties.getLocal().getChunkPath() + File.separator + uploadId;
        try (FileOutputStream fos = new FileOutputStream(storagePath)) {
            for (int i = 0; i < totalChunks; i++) {
                File chunkFile = new File(chunkDir + File.separator + i);
                if (!chunkFile.exists()) {
                    throw new BusinessException("分片文件丢失: " + i);
                }
                byte[] bytes = FileUtil.readBytes(chunkFile);
                fos.write(bytes);
            }
        }
        
        // 验证MD5
        String finalMd5 = cn.hutool.crypto.digest.DigestUtil.md5Hex(new File(storagePath));
        if (!finalMd5.equalsIgnoreCase(dto.getFileMd5())) {
            FileUtil.del(storagePath);
            throw new BusinessException("文件校验失败，请重新上传");
        }
        
        // 保存文件记录
        FileRecord fileRecord = new FileRecord();
        fileRecord.setFileName(fileName);
        fileRecord.setOriginalName(fileName);
        fileRecord.setFileType(FileTypeEnum.getByFileName(fileName).getCode());
        fileRecord.setMimeType(mimeType);
        fileRecord.setFileSize(fileSize);
        fileRecord.setStoragePath(storagePath);
        fileRecord.setUrl(generateFileUrl(relativePath));
        fileRecord.setFolderId(folderId);
        fileRecord.setUploaderId(uploaderId);
        fileRecord.setVersion(1);
        fileRecord.setIsLatest(1);
        fileRecord.setFileMd5(fileMd5);
        fileRecord.setPermission(permission);
        fileRecord.setDownloadCount(0);
        
        save(fileRecord);
        
        // 创建版本记录
        fileVersionService.createVersion(fileRecord.getId(), storagePath, fileSize, fileMd5, "初始版本");
        
        // 清理临时文件
        cleanupUploadTask(uploadId);
        
        return convertToVO(fileRecord);
    }
    
    @Override
    @Transactional
    public FileVO simpleUpload(MultipartFile file, Long folderId, Integer permission) throws IOException {
        Long userId = StpUtil.getLoginIdAsLong();
        
        String originalName = file.getOriginalFilename();
        if (StrUtil.isBlank(originalName)) {
            originalName = "unknown";
        }
        
        // 检查文件大小
        if (file.getSize() > uploadProperties.getMaxFileSize()) {
            throw new BusinessException("文件大小超过限制");
        }
        
        // 计算MD5
        String fileMd5 = cn.hutool.crypto.digest.DigestUtil.md5Hex(file.getInputStream());
        
        // 检查秒传
        FileRecord existingFile = baseMapper.selectByMd5(fileMd5);
        if (existingFile != null) {
            // 创建新的文件记录引用
            FileRecord newRecord = new FileRecord();
            BeanUtil.copyProperties(existingFile, newRecord, "id", "folderId", "uploaderId", "permission", "createTime", "updateTime");
            newRecord.setFolderId(folderId != null ? folderId : 0);
            newRecord.setUploaderId(userId);
            newRecord.setPermission(permission != null ? permission : FilePermissionEnum.PRIVATE.getCode());
            save(newRecord);
            return convertToVO(newRecord);
        }
        
        // 保存文件
        String relativePath = generateRelativePath(originalName);
        String storagePath = storageProperties.getLocal().getPath() + File.separator + relativePath;
        FileUtil.mkdir(FileUtil.getParent(storagePath, 1));
        file.transferTo(new File(storagePath));
        
        // 保存记录
        FileRecord fileRecord = new FileRecord();
        fileRecord.setFileName(originalName);
        fileRecord.setOriginalName(originalName);
        fileRecord.setFileType(FileTypeEnum.getByFileName(originalName).getCode());
        fileRecord.setMimeType(file.getContentType());
        fileRecord.setFileSize(file.getSize());
        fileRecord.setStoragePath(storagePath);
        fileRecord.setUrl(generateFileUrl(relativePath));
        fileRecord.setFolderId(folderId != null ? folderId : 0);
        fileRecord.setUploaderId(userId);
        fileRecord.setVersion(1);
        fileRecord.setIsLatest(1);
        fileRecord.setFileMd5(fileMd5);
        fileRecord.setPermission(permission != null ? permission : FilePermissionEnum.PRIVATE.getCode());
        fileRecord.setDownloadCount(0);
        
        save(fileRecord);
        
        // 创建版本记录
        fileVersionService.createVersion(fileRecord.getId(), storagePath, file.getSize(), fileMd5, "初始版本");
        
        return convertToVO(fileRecord);
    }
    
    @Override
    public void downloadFile(Long fileId, HttpServletResponse response) throws IOException {
        FileRecord fileRecord = getById(fileId);
        if (fileRecord == null) {
            throw new BusinessException("文件不存在");
        }
        
        // 检查权限
        checkFilePermission(fileRecord);
        
        File file = new File(fileRecord.getStoragePath());
        if (!file.exists()) {
            throw new BusinessException("文件不存在或已删除");
        }
        
        // 更新下载次数
        baseMapper.incrementDownloadCount(fileId);
        
        // 设置响应头
        response.setContentType(fileRecord.getMimeType());
        response.setHeader("Content-Disposition", "attachment; filename=" + 
                new String(fileRecord.getOriginalName().getBytes("UTF-8"), "ISO-8859-1"));
        response.setContentLengthLong(fileRecord.getFileSize());
        
        // 写入响应
        try (InputStream is = Files.newInputStream(file.toPath());
             OutputStream os = response.getOutputStream()) {
            IoUtil.copy(is, os);
        }
    }
    
    @Override
    @Transactional
    public void deleteFile(Long fileId) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        FileRecord fileRecord = getById(fileId);
        if (fileRecord == null) {
            throw new BusinessException("文件不存在");
        }
        
        // 检查权限
        if (!fileRecord.getUploaderId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        
        // 逻辑删除
        removeById(fileId);
    }
    
    @Override
    @Transactional
    public FileVO moveFile(FileMoveDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        FileRecord fileRecord = getById(dto.getFileId());
        if (fileRecord == null) {
            throw new BusinessException("文件不存在");
        }
        
        // 检查权限
        if (!fileRecord.getUploaderId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        
        fileRecord.setFolderId(dto.getTargetFolderId());
        updateById(fileRecord);
        
        return convertToVO(fileRecord);
    }
    
    @Override
    @Transactional
    public FileVO updatePermission(FilePermissionDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        FileRecord fileRecord = getById(dto.getFileId());
        if (fileRecord == null) {
            throw new BusinessException("文件不存在");
        }
        
        // 检查权限
        if (!fileRecord.getUploaderId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        
        fileRecord.setPermission(dto.getPermission());
        updateById(fileRecord);
        
        return convertToVO(fileRecord);
    }
    
    @Override
    public List<FileVO> getFileList(Long folderId) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        List<FileRecord> records = lambdaQuery()
                .eq(FileRecord::getFolderId, folderId != null ? folderId : 0L)
                .eq(FileRecord::getUploaderId, userId)
                .eq(FileRecord::getStatus, 1)
                .eq(FileRecord::getIsLatest, 1)
                .orderByDesc(FileRecord::getCreateTime)
                .list();
        
        return records.stream().map(this::convertToVO).collect(Collectors.toList());
    }
    
    @Override
    public FileVO getFileDetail(Long fileId) {
        FileRecord fileRecord = getById(fileId);
        if (fileRecord == null) {
            throw new BusinessException("文件不存在");
        }
        
        checkFilePermission(fileRecord);
        
        return convertToVO(fileRecord);
    }
    
    @Override
    @Transactional
    public FileVO renameFile(Long fileId, String newName) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        FileRecord fileRecord = getById(fileId);
        if (fileRecord == null) {
            throw new BusinessException("文件不存在");
        }
        
        // 检查权限
        if (!fileRecord.getUploaderId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        
        fileRecord.setFileName(newName);
        fileRecord.setOriginalName(newName);
        updateById(fileRecord);
        
        return convertToVO(fileRecord);
    }
    
    private List<Integer> getUploadedChunks(String uploadId) {
        String chunkKey = UPLOAD_TASK_KEY + uploadId + ":chunks";
        Set<String> members = redisTemplate.opsForSet().members(chunkKey);
        if (members == null) {
            return new ArrayList<>();
        }
        return members.stream()
                .map(Integer::parseInt)
                .sorted()
                .collect(Collectors.toList());
    }
    
    private void cleanupUploadTask(String uploadId) {
        String taskKey = UPLOAD_TASK_KEY + uploadId;
        redisTemplate.delete(taskKey);
        redisTemplate.delete(taskKey + ":chunks");
        
        // 删除分片文件
        String chunkDir = storageProperties.getLocal().getChunkPath() + File.separator + uploadId;
        FileUtil.del(chunkDir);
    }
    
    private String generateRelativePath(String fileName) {
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String uuid = IdUtil.simpleUUID();
        String ext = FileUtil.extName(fileName);
        if (StrUtil.isNotBlank(ext)) {
            return datePath + "/" + uuid + "." + ext;
        }
        return datePath + "/" + uuid;
    }
    
    private String generateFileUrl(String relativePath) {
        return storageProperties.getUrlPrefix() + "/" + relativePath.replace("\\", "/");
    }
    
    private void checkFilePermission(FileRecord fileRecord) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        // 自己上传的文件总是有权限
        if (fileRecord.getUploaderId().equals(userId)) {
            return;
        }
        
        // 检查权限
        Integer permission = fileRecord.getPermission();
        if (permission.equals(FilePermissionEnum.PRIVATE.getCode())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        
        // TODO: 家庭权限检查，需要获取用户的家庭ID进行判断
    }
    
    private FileVO convertToVO(FileRecord fileRecord) {
        FileVO vo = new FileVO();
        BeanUtil.copyProperties(fileRecord, vo);
        vo.setFileSizeStr(FileUtil.readableFileSize(fileRecord.getFileSize()));
        return vo;
    }
}
