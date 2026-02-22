package com.family.file.controller;

import com.family.common.core.Result;
import com.family.file.dto.*;
import com.family.file.service.FileFolderService;
import com.family.file.vo.FolderContentVO;
import com.family.file.vo.FolderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文件夹控制器
 */
@RestController
@RequestMapping("/folder")
@RequiredArgsConstructor
public class FileFolderController {
    
    private final FileFolderService folderService;
    
    /**
     * 创建文件夹
     */
    @PostMapping("/create")
    public Result<FolderVO> createFolder(@RequestBody FolderCreateDTO dto) {
        return Result.success(folderService.createFolder(dto));
    }
    
    /**
     * 重命名文件夹
     */
    @PostMapping("/rename")
    public Result<FolderVO> renameFolder(@RequestBody FolderRenameDTO dto) {
        return Result.success(folderService.renameFolder(dto));
    }
    
    /**
     * 删除文件夹
     */
    @DeleteMapping("/{folderId}")
    public Result<Void> deleteFolder(@PathVariable Long folderId) {
        folderService.deleteFolder(folderId);
        return Result.success();
    }
    
    /**
     * 移动文件夹
     */
    @PostMapping("/move")
    public Result<FolderVO> moveFolder(@RequestBody FolderMoveDTO dto) {
        return Result.success(folderService.moveFolder(dto));
    }
    
    /**
     * 获取文件夹内容
     */
    @GetMapping("/content/{folderId}")
    public Result<FolderContentVO> getFolderContent(@PathVariable(required = false) Long folderId) {
        return Result.success(folderService.getFolderContent(folderId));
    }
    
    /**
     * 获取文件夹树
     */
    @GetMapping("/tree")
    public Result<List<FolderVO>> getFolderTree() {
        return Result.success(folderService.getFolderTree());
    }
    
    /**
     * 更新文件夹权限
     */
    @PostMapping("/permission/{folderId}")
    public Result<FolderVO> updatePermission(@PathVariable Long folderId, @RequestParam Integer permission) {
        return Result.success(folderService.updatePermission(folderId, permission));
    }
}
