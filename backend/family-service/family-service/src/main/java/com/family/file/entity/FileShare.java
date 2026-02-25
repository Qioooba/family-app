package com.family.family.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.family.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 文件分享实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("file_share")
public class FileShare extends BaseEntity {
    private static final long serialVersionUID = 1L;
    
    /**
     * 文件ID
     */
    private Long fileId;
    
    /**
     * 分享类型: 1-链接分享, 2-家庭分享, 3-指定用户分享
     */
    private Integer shareType;
    
    /**
     * 分享人ID
     */
    private Long sharerId;
    
    /**
     * 被分享用户ID(share_type=3时有效)
     */
    private Long shareUserId;
    
    /**
     * 分享链接(share_type=1时有效)
     */
    private String shareLink;
    
    /**
     * 访问密码
     */
    private String accessPassword;
    
    /**
     * 权限: 1-只读, 2-读写
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
     * 最后访问时间
     */
    private LocalDateTime lastAccessTime;
}
