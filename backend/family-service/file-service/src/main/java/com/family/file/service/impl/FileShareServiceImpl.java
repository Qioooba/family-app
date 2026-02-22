package com.family.file.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.family.common.core.ErrorCode;
import com.family.common.core.exception.BusinessException;
import com.family.file.dto.FileShareAccessDTO;
import com.family.file.dto.FileShareCreateDTO;
import com.family.file.entity.FileRecord;
import com.family.file.entity.FileShare;
import com.family.file.enums.ShareTypeEnum;
import com.family.file.mapper.FileShareMapper;
import com.family.file.service.FileRecordService;
import com.family.file.service.FileShareService;
import com.family.file.vo.FileShareVO;
import com.family.file.vo.FileVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 文件分享服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileShareServiceImpl extends ServiceImpl<FileShareMapper, FileShare> implements FileShareService {
    
    private final FileRecordService fileRecordService;
    private final StringRedisTemplate redisTemplate;
    
    private static final String SHARE_ACCESS_KEY = "file:share:access:";
    
    @Override
    @Transactional
    public FileShareVO createShare(FileShareCreateDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        // 检查文件
        FileRecord fileRecord = fileRecordService.getById(dto.getFileId());
        if (fileRecord == null) {
            throw new BusinessException("文件不存在");
        }
        
        // 检查权限
        if (!fileRecord.getUploaderId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        
        FileShare share = new FileShare();
        share.setFileId(dto.getFileId());
        share.setShareType(dto.getShareType());
        share.setSharerId(userId);
        share.setShareUserId(dto.getShareUserId());
        share.setAccessPassword(dto.getAccessPassword());
        share.setPermission(dto.getPermission());
        share.setExpireTime(dto.getExpireTime());
        share.setAccessCount(0);
        
        // 链接分享生成唯一链接
        if (dto.getShareType().equals(ShareTypeEnum.LINK.getCode())) {
            share.setShareLink(IdUtil.simpleUUID());
        }
        
        save(share);
        
        return convertToVO(share, fileRecord);
    }
    
    @Override
    @Transactional
    public void cancelShare(Long shareId) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        FileShare share = getById(shareId);
        if (share == null) {
            throw new BusinessException("分享不存在");
        }
        
        if (!share.getSharerId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        
        removeById(shareId);
    }
    
    @Override
    public FileVO accessShare(FileShareAccessDTO dto) {
        // 验证分享
        FileShare share = baseMapper.selectByShareLink(dto.getShareLink());
        if (share == null) {
            throw new BusinessException("分享不存在或已取消");
        }
        
        // 检查有效期
        if (share.getExpireTime() != null && share.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("分享已过期");
        }
        
        // 验证密码
        if (StrUtil.isNotBlank(share.getAccessPassword())) {
            if (!share.getAccessPassword().equals(dto.getAccessPassword())) {
                throw new BusinessException("访问密码错误");
            }
        }
        
        // 更新访问次数
        baseMapper.incrementAccessCount(share.getId());
        
        // 返回文件信息
        return fileRecordService.getFileDetail(share.getFileId());
    }
    
    @Override
    public void downloadShareFile(String shareLink, HttpServletResponse response) throws IOException {
        // 验证分享
        FileShare share = baseMapper.selectByShareLink(shareLink);
        if (share == null) {
            throw new BusinessException("分享不存在或已取消");
        }
        
        // 检查有效期
        if (share.getExpireTime() != null && share.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("分享已过期");
        }
        
        // 下载文件
        fileRecordService.downloadFile(share.getFileId(), response);
    }
    
    @Override
    public List<FileShareVO> getMyShares() {
        Long userId = StpUtil.getLoginIdAsLong();
        
        List<FileShare> shares = baseMapper.selectBySharerId(userId);
        return shares.stream().map(share -> {
            FileRecord file = fileRecordService.getById(share.getFileId());
            return convertToVO(share, file);
        }).collect(Collectors.toList());
    }
    
    @Override
    public List<FileShareVO> getSharesToMe() {
        Long userId = StpUtil.getLoginIdAsLong();
        
        List<FileShare> shares = baseMapper.selectByShareUserId(userId);
        return shares.stream().map(share -> {
            FileRecord file = fileRecordService.getById(share.getFileId());
            return convertToVO(share, file);
        }).collect(Collectors.toList());
    }
    
    @Override
    public Boolean verifySharePassword(String shareLink, String password) {
        FileShare share = baseMapper.selectByShareLink(shareLink);
        if (share == null) {
            return false;
        }
        
        return share.getAccessPassword().equals(password);
    }
    
    private FileShareVO convertToVO(FileShare share, FileRecord file) {
        FileShareVO vo = new FileShareVO();
        BeanUtil.copyProperties(share, vo);
        
        if (file != null) {
            vo.setFileName(file.getFileName());
        }
        
        // 设置分享类型描述
        for (ShareTypeEnum type : ShareTypeEnum.values()) {
            if (type.getCode().equals(share.getShareType())) {
                vo.setShareTypeDesc(type.getDesc());
                break;
            }
        }
        
        // 是否需要密码
        vo.setNeedPassword(StrUtil.isNotBlank(share.getAccessPassword()));
        
        // 是否过期
        vo.setExpired(share.getExpireTime() != null && share.getExpireTime().isBefore(LocalDateTime.now()));
        
        return vo;
    }
}
