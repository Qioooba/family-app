package com.family.file.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.family.common.core.ErrorCode;
import com.family.common.core.exception.BusinessException;
import com.family.file.entity.FileRecord;
import com.family.file.entity.FileVersion;
import com.family.file.mapper.FileVersionMapper;
import com.family.file.service.FileRecordService;
import com.family.file.service.FileVersionService;
import com.family.file.vo.FileVersionVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文件版本服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileVersionServiceImpl extends ServiceImpl<FileVersionMapper, FileVersion> implements FileVersionService {
    
    private final FileRecordService fileRecordService;
    
    @Override
    public List<FileVersionVO> getFileVersions(Long fileId) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        // 检查文件权限
        FileRecord fileRecord = fileRecordService.getById(fileId);
        if (fileRecord == null) {
            throw new BusinessException("文件不存在");
        }
        
        if (!fileRecord.getUploaderId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        
        List<FileVersion> versions = baseMapper.selectByFileId(fileId);
        return versions.stream().map(this::convertToVO).collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public FileVersionVO rollbackVersion(Long fileId, Integer version) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        FileRecord fileRecord = fileRecordService.getById(fileId);
        if (fileRecord == null) {
            throw new BusinessException("文件不存在");
        }
        
        if (!fileRecord.getUploaderId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        
        FileVersion targetVersion = baseMapper.selectByFileIdAndVersion(fileId, version);
        if (targetVersion == null) {
            throw new BusinessException("版本不存在");
        }
        
        // 获取最新版本号
        Integer maxVersion = baseMapper.selectMaxVersion(fileId);
        if (maxVersion == null) {
            maxVersion = 0;
        }
        
        // 创建新版本（复制旧版本）
        FileVersion newVersion = new FileVersion();
        newVersion.setFileId(fileId);
        newVersion.setVersion(maxVersion + 1);
        newVersion.setStoragePath(targetVersion.getStoragePath());
        newVersion.setFileSize(targetVersion.getFileSize());
        newVersion.setFileMd5(targetVersion.getFileMd5());
        newVersion.setDescription("回滚到版本" + version);
        newVersion.setCreatorId(userId);
        newVersion.setCreateTime(LocalDateTime.now());
        save(newVersion);
        
        // 更新文件记录
        fileRecord.setVersion(newVersion.getVersion());
        fileRecord.setStoragePath(targetVersion.getStoragePath());
        fileRecord.setFileSize(targetVersion.getFileSize());
        fileRecord.setFileMd5(targetVersion.getFileMd5());
        fileRecordService.updateById(fileRecord);
        
        return convertToVO(newVersion);
    }
    
    @Override
    @Transactional
    public void deleteVersion(Long versionId) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        FileVersion version = getById(versionId);
        if (version == null) {
            throw new BusinessException("版本不存在");
        }
        
        FileRecord fileRecord = fileRecordService.getById(version.getFileId());
        if (fileRecord == null) {
            throw new BusinessException("文件不存在");
        }
        
        if (!fileRecord.getUploaderId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        
        // 不能删除当前正在使用的版本
        if (fileRecord.getVersion().equals(version.getVersion())) {
            throw new BusinessException("不能删除当前使用的版本");
        }
        
        removeById(versionId);
    }
    
    @Override
    @Transactional
    public FileVersion createVersion(Long fileId, String storagePath, Long fileSize, String fileMd5, String description) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        // 获取最新版本号
        Integer maxVersion = baseMapper.selectMaxVersion(fileId);
        if (maxVersion == null) {
            maxVersion = 0;
        }
        
        FileVersion version = new FileVersion();
        version.setFileId(fileId);
        version.setVersion(maxVersion + 1);
        version.setStoragePath(storagePath);
        version.setFileSize(fileSize);
        version.setFileMd5(fileMd5);
        version.setDescription(description);
        version.setCreatorId(userId);
        version.setCreateTime(LocalDateTime.now());
        save(version);
        
        return version;
    }
    
    private FileVersionVO convertToVO(FileVersion version) {
        FileVersionVO vo = new FileVersionVO();
        BeanUtil.copyProperties(version, vo);
        vo.setFileSizeStr(FileUtil.readableFileSize(version.getFileSize()));
        return vo;
    }
}
