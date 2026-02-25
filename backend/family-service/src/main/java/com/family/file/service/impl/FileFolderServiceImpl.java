package com.family.file.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.family.common.core.ErrorCode;
import com.family.common.core.exception.BusinessException;
import com.family.file.dto.FolderCreateDTO;
import com.family.file.dto.FolderMoveDTO;
import com.family.file.dto.FolderRenameDTO;
import com.family.file.entity.FileFolder;
import com.family.file.enums.FilePermissionEnum;
import com.family.file.mapper.FileFolderMapper;
import com.family.file.service.FileFolderService;
import com.family.file.vo.FolderContentVO;
import com.family.file.vo.FolderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 文件夹服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileFolderServiceImpl extends ServiceImpl<FileFolderMapper, FileFolder> implements FileFolderService {
    
    @Override
    @Transactional
    public FolderVO createFolder(FolderCreateDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        // 检查文件夹名是否重复
        if (lambdaQuery()
                .eq(FileFolder::getCreatorId, userId)
                .eq(FileFolder::getParentId, dto.getParentId() != null ? dto.getParentId() : 0L)
                .eq(FileFolder::getName, dto.getName())
                .count() > 0) {
            throw new BusinessException("文件夹名称已存在");
        }
        
        FileFolder folder = new FileFolder();
        folder.setName(dto.getName());
        folder.setParentId(dto.getParentId() != null ? dto.getParentId() : 0L);
        folder.setCreatorId(userId);
        folder.setPermission(dto.getPermission() != null ? dto.getPermission() : FilePermissionEnum.PRIVATE.getCode());
        
        // 构建路径
        String path = buildPath(folder.getParentId(), folder.getName());
        folder.setPath(path);
        
        save(folder);
        
        return convertToVO(folder);
    }
    
    @Override
    @Transactional
    public FolderVO renameFolder(FolderRenameDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        FileFolder folder = getById(dto.getFolderId());
        if (folder == null) {
            throw new BusinessException("文件夹不存在");
        }
        
        // 检查权限
        if (!folder.getCreatorId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        
        // 检查新名称是否重复
        if (lambdaQuery()
                .eq(FileFolder::getCreatorId, userId)
                .eq(FileFolder::getParentId, folder.getParentId())
                .eq(FileFolder::getName, dto.getNewName())
                .ne(FileFolder::getId, dto.getFolderId())
                .count() > 0) {
            throw new BusinessException("文件夹名称已存在");
        }
        
        folder.setName(dto.getNewName());
        folder.setPath(buildPath(folder.getParentId(), folder.getName()));
        updateById(folder);
        
        return convertToVO(folder);
    }
    
    @Override
    @Transactional
    public void deleteFolder(Long folderId) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        FileFolder folder = getById(folderId);
        if (folder == null) {
            throw new BusinessException("文件夹不存在");
        }
        
        // 检查权限
        if (!folder.getCreatorId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        
        // 检查是否有子文件夹
        if (lambdaQuery().eq(FileFolder::getParentId, folderId).count() > 0) {
            throw new BusinessException("请先删除子文件夹");
        }
        
        // 逻辑删除
        removeById(folderId);
    }
    
    @Override
    @Transactional
    public FolderVO moveFolder(FolderMoveDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        FileFolder folder = getById(dto.getFolderId());
        if (folder == null) {
            throw new BusinessException("文件夹不存在");
        }
        
        // 检查权限
        if (!folder.getCreatorId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        
        // 不能移动到自己
        if (dto.getFolderId().equals(dto.getTargetParentId())) {
            throw new BusinessException("不能移动到自己");
        }
        
        // 检查目标文件夹是否存在
        if (dto.getTargetParentId() != 0) {
            FileFolder targetFolder = getById(dto.getTargetParentId());
            if (targetFolder == null) {
                throw new BusinessException("目标文件夹不存在");
            }
        }
        
        folder.setParentId(dto.getTargetParentId());
        folder.setPath(buildPath(folder.getParentId(), folder.getName()));
        updateById(folder);
        
        return convertToVO(folder);
    }
    
    @Override
    public FolderContentVO getFolderContent(Long folderId) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        FolderContentVO vo = new FolderContentVO();
        
        if (folderId == null || folderId == 0) {
            // 根目录
            vo.setFolderId(0L);
            vo.setFolderName("根目录");
            vo.setParentId(null);
        } else {
            FileFolder folder = getById(folderId);
            if (folder == null) {
                throw new BusinessException("文件夹不存在");
            }
            vo.setFolderId(folder.getId());
            vo.setFolderName(folder.getName());
            vo.setParentId(folder.getParentId());
        }
        
        // 获取子文件夹
        List<FileFolder> folders = lambdaQuery()
                .eq(FileFolder::getParentId, folderId != null ? folderId : 0L)
                .eq(FileFolder::getCreatorId, userId)
                .eq(FileFolder::getStatus, 1)
                .list();
        vo.setFolders(folders.stream().map(this::convertToVO).collect(Collectors.toList()));
        
        return vo;
    }
    
    @Override
    public List<FolderVO> getFolderTree() {
        Long userId = StpUtil.getLoginIdAsLong();
        
        List<FileFolder> folders = lambdaQuery()
                .eq(FileFolder::getCreatorId, userId)
                .eq(FileFolder::getStatus, 1)
                .list();
        
        return folders.stream().map(this::convertToVO).collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public FolderVO updatePermission(Long folderId, Integer permission) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        FileFolder folder = getById(folderId);
        if (folder == null) {
            throw new BusinessException("文件夹不存在");
        }
        
        // 检查权限
        if (!folder.getCreatorId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        
        folder.setPermission(permission);
        updateById(folder);
        
        return convertToVO(folder);
    }
    
    private String buildPath(Long parentId, String name) {
        if (parentId == null || parentId == 0) {
            return "/" + name;
        }
        FileFolder parent = getById(parentId);
        if (parent == null) {
            return "/" + name;
        }
        return parent.getPath() + "/" + name;
    }
    
    private FolderVO convertToVO(FileFolder folder) {
        FolderVO vo = new FolderVO();
        BeanUtil.copyProperties(folder, vo);
        return vo;
    }
}
