-- 家庭相册服务数据库表结构
-- 创建时间: 2026-02-22

-- 1. 家庭相册表
CREATE TABLE IF NOT EXISTS `family_album` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `family_id` BIGINT NOT NULL COMMENT '家庭ID',
    `name` VARCHAR(100) NOT NULL COMMENT '相册名称',
    `description` VARCHAR(500) COMMENT '相册描述',
    `cover_url` VARCHAR(500) COMMENT '封面图片URL',
    `type` TINYINT DEFAULT 1 COMMENT '相册类型: 1-普通相册 2-时光轴 3-共享相册',
    `creator_id` BIGINT COMMENT '创建者ID',
    `photo_count` INT DEFAULT 0 COMMENT '照片数量',
    `sort_order` INT DEFAULT 0 COMMENT '排序顺序',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-删除 1-正常',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_family_id` (`family_id`),
    INDEX `idx_type` (`type`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家庭相册表';

-- 2. 相册照片表
CREATE TABLE IF NOT EXISTS `album_photo` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `album_id` BIGINT NOT NULL COMMENT '相册ID',
    `family_id` BIGINT NOT NULL COMMENT '家庭ID',
    `uploader_id` BIGINT COMMENT '上传者ID',
    `url` VARCHAR(500) NOT NULL COMMENT '图片URL(压缩图)',
    `thumb_url` VARCHAR(500) COMMENT '缩略图URL',
    `original_url` VARCHAR(500) COMMENT '原图URL',
    `description` VARCHAR(500) COMMENT '照片描述',
    `photo_date` DATE COMMENT '拍摄日期',
    `location` VARCHAR(200) COMMENT '拍摄地点',
    `latitude` DECIMAL(10, 8) COMMENT '纬度',
    `longitude` DECIMAL(11, 8) COMMENT '经度',
    `width` INT COMMENT '图片宽度',
    `height` INT COMMENT '图片高度',
    `file_size` BIGINT COMMENT '文件大小(字节)',
    `tags` VARCHAR(500) COMMENT '标签(逗号分隔)',
    `is_favorite` TINYINT DEFAULT 0 COMMENT '是否收藏: 0-否 1-是',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-删除 1-正常',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_album_id` (`album_id`),
    INDEX `idx_family_id` (`family_id`),
    INDEX `idx_photo_date` (`photo_date`),
    INDEX `idx_status` (`status`),
    INDEX `idx_is_favorite` (`is_favorite`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='相册照片表';

-- 3. 相册分享表
CREATE TABLE IF NOT EXISTS `album_share` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `album_id` BIGINT NOT NULL COMMENT '相册ID',
    `family_id` BIGINT NOT NULL COMMENT '家庭ID',
    `shared_by` BIGINT COMMENT '分享者ID',
    `share_type` TINYINT DEFAULT 1 COMMENT '分享类型: 1-家庭内分享 2-外部链接',
    `share_code` VARCHAR(32) COMMENT '分享码',
    `share_url` VARCHAR(500) COMMENT '分享链接',
    `password` VARCHAR(32) COMMENT '访问密码',
    `expire_time` DATETIME COMMENT '过期时间',
    `view_count` INT DEFAULT 0 COMMENT '查看次数',
    `download_count` INT DEFAULT 0 COMMENT '下载次数',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-失效 1-有效',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_album_id` (`album_id`),
    INDEX `idx_family_id` (`family_id`),
    INDEX `idx_share_code` (`share_code`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='相册分享表';

-- 4. 照片标签表
CREATE TABLE IF NOT EXISTS `photo_tag` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `photo_id` BIGINT NOT NULL COMMENT '照片ID',
    `family_id` BIGINT NOT NULL COMMENT '家庭ID',
    `tag_name` VARCHAR(50) NOT NULL COMMENT '标签名称',
    `tag_type` TINYINT DEFAULT 4 COMMENT '标签类型: 1-人物 2-地点 3-事件 4-自定义',
    `user_id` BIGINT COMMENT '创建者ID',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-删除 1-正常',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_photo_id` (`photo_id`),
    INDEX `idx_family_id` (`family_id`),
    INDEX `idx_tag_name` (`tag_name`),
    INDEX `idx_tag_type` (`tag_type`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='照片标签表';
