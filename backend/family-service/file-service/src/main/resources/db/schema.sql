-- 文件服务数据库表结构

-- 文件夹表
CREATE TABLE IF NOT EXISTS `file_folder` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `name` VARCHAR(255) NOT NULL COMMENT '文件夹名称',
    `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父文件夹ID(0为根目录)',
    `creator_id` BIGINT NOT NULL COMMENT '创建人ID',
    `path` VARCHAR(500) DEFAULT NULL COMMENT '文件夹路径',
    `permission` TINYINT NOT NULL DEFAULT 0 COMMENT '权限: 0-私有, 1-家庭可读, 2-家庭可读写',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-删除, 1-正常',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_creator_id` (`creator_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件夹表';

-- 文件记录表
CREATE TABLE IF NOT EXISTS `file_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `file_name` VARCHAR(255) NOT NULL COMMENT '文件名',
    `original_name` VARCHAR(255) NOT NULL COMMENT '原始文件名',
    `file_type` VARCHAR(50) DEFAULT 'other' COMMENT '文件类型: image/video/audio/document/archive/other',
    `mime_type` VARCHAR(100) DEFAULT NULL COMMENT 'MIME类型',
    `file_size` BIGINT NOT NULL DEFAULT 0 COMMENT '文件大小(字节)',
    `storage_path` VARCHAR(500) NOT NULL COMMENT '存储路径',
    `url` VARCHAR(500) DEFAULT NULL COMMENT '访问URL',
    `folder_id` BIGINT NOT NULL DEFAULT 0 COMMENT '文件夹ID',
    `uploader_id` BIGINT NOT NULL COMMENT '上传人ID',
    `version` INT NOT NULL DEFAULT 1 COMMENT '版本号',
    `is_latest` TINYINT NOT NULL DEFAULT 1 COMMENT '是否为最新版本: 0-否, 1-是',
    `file_md5` VARCHAR(64) DEFAULT NULL COMMENT '文件MD5值',
    `permission` TINYINT NOT NULL DEFAULT 0 COMMENT '权限: 0-私有, 1-家庭可读, 2-家庭可读写',
    `download_count` INT NOT NULL DEFAULT 0 COMMENT '下载次数',
    `last_download_time` DATETIME DEFAULT NULL COMMENT '最后下载时间',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-删除, 1-正常',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_folder_id` (`folder_id`),
    KEY `idx_uploader_id` (`uploader_id`),
    KEY `idx_file_md5` (`file_md5`),
    KEY `idx_file_type` (`file_type`),
    KEY `idx_status` (`status`),
    KEY `idx_is_latest` (`is_latest`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件记录表';

-- 文件版本表
CREATE TABLE IF NOT EXISTS `file_version` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `file_id` BIGINT NOT NULL COMMENT '文件ID',
    `version` INT NOT NULL COMMENT '版本号',
    `storage_path` VARCHAR(500) NOT NULL COMMENT '存储路径',
    `file_size` BIGINT NOT NULL DEFAULT 0 COMMENT '文件大小',
    `file_md5` VARCHAR(64) DEFAULT NULL COMMENT '文件MD5',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '版本描述',
    `creator_id` BIGINT NOT NULL COMMENT '创建人ID',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-删除, 1-正常',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_file_version` (`file_id`, `version`),
    KEY `idx_file_id` (`file_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件版本表';

-- 文件分享表
CREATE TABLE IF NOT EXISTS `file_share` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `file_id` BIGINT NOT NULL COMMENT '文件ID',
    `share_type` TINYINT NOT NULL DEFAULT 1 COMMENT '分享类型: 1-链接分享, 2-家庭分享, 3-指定用户分享',
    `sharer_id` BIGINT NOT NULL COMMENT '分享人ID',
    `share_user_id` BIGINT DEFAULT NULL COMMENT '被分享用户ID(share_type=3时有效)',
    `share_link` VARCHAR(100) DEFAULT NULL COMMENT '分享链接(share_type=1时有效)',
    `access_password` VARCHAR(64) DEFAULT NULL COMMENT '访问密码',
    `permission` TINYINT NOT NULL DEFAULT 1 COMMENT '权限: 1-只读, 2-读写',
    `expire_time` DATETIME DEFAULT NULL COMMENT '有效期至',
    `access_count` INT NOT NULL DEFAULT 0 COMMENT '访问次数',
    `last_access_time` DATETIME DEFAULT NULL COMMENT '最后访问时间',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-删除, 1-正常',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_share_link` (`share_link`),
    KEY `idx_file_id` (`file_id`),
    KEY `idx_sharer_id` (`sharer_id`),
    KEY `idx_share_user_id` (`share_user_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件分享表';
