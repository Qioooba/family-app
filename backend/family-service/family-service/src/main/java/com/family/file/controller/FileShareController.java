
package com.family.file.controller;
import cn.dev33.satoken.annotation.SaCheckLogin;
import com.family.common.core.Result;
import com.family.file.dto.FileShareAccessDTO;
import com.family.file.dto.FileShareCreateDTO;
import com.family.file.service.FileShareService;
import com.family.file.vo.FileShareVO;
import com.family.file.vo.FileVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 文件分享控制器
 */
@RestController
@SaCheckLogin
@RequestMapping("/file-share")
@RequiredArgsConstructor
public class FileShareController {
    
    private final FileShareService fileShareService;
    
    /**
     * 创建分享
     */
    @PostMapping("/create")
    public Result<FileShareVO> createShare(@RequestBody FileShareCreateDTO dto) {
        return Result.success(fileShareService.createShare(dto));
    }
    
    /**
     * 取消分享
     */
    @DeleteMapping("/{shareId}")
    public Result<Void> cancelShare(@PathVariable Long shareId) {
        fileShareService.cancelShare(shareId);
        return Result.success();
    }
    
    /**
     * 访问分享
     */
    @PostMapping("/access")
    public Result<FileVO> accessShare(@RequestBody FileShareAccessDTO dto) {
        return Result.success(fileShareService.accessShare(dto));
    }
    
    /**
     * 下载分享的文件
     */
    @GetMapping("/download/{shareLink}")
    public void downloadShareFile(@PathVariable String shareLink, HttpServletResponse response) throws IOException {
        fileShareService.downloadShareFile(shareLink, response);
    }
    
    /**
     * 获取我的分享列表
     */
    @GetMapping("/my-shares")
    public Result<List<FileShareVO>> getMyShares() {
        return Result.success(fileShareService.getMyShares());
    }
    
    /**
     * 获取分享给我的列表
     */
    @GetMapping("/shares-to-me")
    public Result<List<FileShareVO>> getSharesToMe() {
        return Result.success(fileShareService.getSharesToMe());
    }
    
    /**
     * 验证分享密码
     */
    @PostMapping("/verify-password")
    public Result<Boolean> verifySharePassword(@RequestParam String shareLink, @RequestParam String password) {
        return Result.success(fileShareService.verifySharePassword(shareLink, password));
    }
}
