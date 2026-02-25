package com.family.file.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件分享VO
 */
@Data
public class FileShareVO {
    
    /**
     * 分享ID
     */
    private Long id;
    
    /**
     * 文件ID
     */
    private Long fileId;
    
    /**
     * 文件名
     */
    private String fileName;
    
    /**
     * 分享类型
     */
    private Integer shareType;
    
    /**
     * 分享类型描述
     */
    private String shareTypeDesc;
    
    /**
     * 分享人ID
     */
    private Long sharerId;
    
    /**
     * 分享人昵称
     */
    private String sharerName;
    
    /**
     * 被分享用户ID
     */
    private Long shareUserId;
    
    /**
     * 分享链接
     */
    private String shareLink;
    
    /**
     * 是否需要密码
     */
    private Boolean needPassword;
    
    /**
     * 权限
     */
    private Integer permission;
    
    /**
     * 有效期至
     */
    private LocalDateTime expireTime;
    
    /**
     * 访问次数
     */
    private Integer accessCount;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 是否过期
     */
    private Boolean expired;
}
