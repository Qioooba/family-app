package com.family.file.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.family.file.dto.*;
import com.family.file.entity.FileFolder;
import com.family.file.vo.FolderContentVO;
import com.family.file.vo.FolderVO;

import java.util.List;

/**
 * 文件夹服务接口
 */
public interface FileFolderService extends IService<FileFolder> {
    
    /**
     * 创建文件夹
     */
    FolderVO createFolder(FolderCreateDTO dto);
    
    /**
     * 重命名文件夹
     */
    FolderVO renameFolder(FolderRenameDTO dto);
    
    /**
     * 删除文件夹
     */
    void deleteFolder(Long folderId);
    
    /**
     * 移动文件夹
     */
    FolderVO moveFolder(FolderMoveDTO dto);
    
    /**
     * 获取文件夹内容
     */
    FolderContentVO getFolderContent(Long folderId);
    
    /**
     * 获取用户的文件夹树
     */
    List<FolderVO> getFolderTree();
    
    /**
     * 更新文件夹权限
     */
    FolderVO updatePermission(Long folderId, Integer permission);
}
