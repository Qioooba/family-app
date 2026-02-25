package com.family.file.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.family.file.dto.FileShareAccessDTO;
import com.family.file.dto.FileShareCreateDTO;
import com.family.family.entity.FileShare;
import com.family.file.vo.FileShareVO;
import com.family.file.vo.FileVO;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 文件分享服务接口
 */
public interface FileShareService extends IService<FileShare> {
    
    /**
     * 创建分享
     */
    FileShareVO createShare(FileShareCreateDTO dto);
    
    /**
     * 取消分享
     */
    void cancelShare(Long shareId);
    
    /**
     * 访问分享
     */
    FileVO accessShare(FileShareAccessDTO dto);
    
    /**
     * 下载分享的文件
     */
    void downloadShareFile(String shareLink, HttpServletResponse response) throws IOException;
    
    /**
     * 获取我的分享列表
     */
    List<FileShareVO> getMyShares();
    
    /**
     * 获取分享给我的列表
     */
    List<FileShareVO> getSharesToMe();
    
    /**
     * 验证分享密码
     */
    Boolean verifySharePassword(String shareLink, String password);
}
