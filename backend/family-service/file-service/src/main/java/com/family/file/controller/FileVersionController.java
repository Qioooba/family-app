package com.family.file.controller;

import com.family.common.core.Result;
import com.family.file.service.FileVersionService;
import com.family.file.vo.FileVersionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文件版本控制器
 */
@RestController
@RequestMapping("/file-version")
@RequiredArgsConstructor
public class FileVersionController {
    
    private final FileVersionService fileVersionService;
    
    /**
     * 获取文件的所有版本
     */
    @GetMapping("/list/{fileId}")
    public Result<List<FileVersionVO>> getFileVersions(@PathVariable Long fileId) {
        return Result.success(fileVersionService.getFileVersions(fileId));
    }
    
    /**
     * 回滚到指定版本
     */
    @PostMapping("/rollback/{fileId}")
    public Result<FileVersionVO> rollbackVersion(@PathVariable Long fileId, @RequestParam Integer version) {
        return Result.success(fileVersionService.rollbackVersion(fileId, version));
    }
    
    /**
     * 删除版本
     */
    @DeleteMapping("/{versionId}")
    public Result<Void> deleteVersion(@PathVariable Long versionId) {
        fileVersionService.deleteVersion(versionId);
        return Result.success();
    }
}
