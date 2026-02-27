-- 投票服务数据库表结构
-- 创建时间: 2026-02-27

-- 1. 投票表
CREATE TABLE IF NOT EXISTS `vote` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `family_id` BIGINT NOT NULL COMMENT '家庭ID',
    `title` VARCHAR(200) NOT NULL COMMENT '投票标题',
    `description` VARCHAR(500) COMMENT '投票描述',
    `options` TEXT COMMENT '投票选项(JSON数组格式)',
    `creator_id` BIGINT COMMENT '创建者ID',
    `creator_name` VARCHAR(50) COMMENT '创建者名称',
    `valid_days` INT DEFAULT 7 COMMENT '有效天数',
    `multiple_select` TINYINT DEFAULT 0 COMMENT '是否多选: 0-单选 1-多选',
    `deadline` DATETIME COMMENT '截止时间',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-删除 1-正常',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_family_id` (`family_id`),
    INDEX `idx_creator_id` (`creator_id`),
    INDEX `idx_deadline` (`deadline`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='投票表';

-- 2. 投票记录表
CREATE TABLE IF NOT EXISTS `vote_record` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `vote_id` BIGINT NOT NULL COMMENT '投票ID',
    `user_id` BIGINT NOT NULL COMMENT '投票用户ID',
    `user_name` VARCHAR(50) COMMENT '投票用户名称',
    `selected_options` TEXT COMMENT '选择的选项(JSON数组格式)',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-删除 1-正常',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '投票时间',
    INDEX `idx_vote_id` (`vote_id`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='投票记录表';
