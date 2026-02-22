package com.family.file.vo;

import lombok.Data;

import java.util.List;

/**
 * 文件夹内容VO(包含子文件夹和文件)
 */
@Data
public class FolderContentVO {
    
    /**
     * 当前文件夹ID
     */
    private Long folderId;
    
    /**
     * 当前文件夹名称
     */
    private String folderName;
    
    /**
     * 父文件夹ID
     */
    private Long parentId;
    
    /**
     * 子文件夹列表
     */
    private List<FolderVO> folders;
    
    /**
     * 文件列表
     */
    private List<FileVO> files;
}
