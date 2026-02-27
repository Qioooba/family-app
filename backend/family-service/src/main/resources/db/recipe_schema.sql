-- 菜谱服务数据库表结构
-- 创建时间: 2026-02-27

-- 菜谱表
CREATE TABLE IF NOT EXISTS `recipe` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `family_id` BIGINT NOT NULL COMMENT '家庭ID',
    `name` VARCHAR(100) NOT NULL COMMENT '菜谱名称',
    `category` VARCHAR(50) COMMENT '分类',
    `ingredients` TEXT COMMENT '食材(JSON数组格式)',
    `steps` TEXT COMMENT '烹饪步骤(JSON数组格式)',
    `difficulty` VARCHAR(20) COMMENT '难度: 简单/中等/困难',
    `cooking_time` INT COMMENT '烹饪时间(分钟)',
    `image_url` VARCHAR(500) COMMENT '菜谱图片URL',
    `description` VARCHAR(500) COMMENT '描述',
    `creator_id` BIGINT COMMENT '创建者ID',
    `creator_name` VARCHAR(50) COMMENT '创建者名称',
    `favorite_count` INT DEFAULT 0 COMMENT '收藏数',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-删除 1-正常',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_family_id` (`family_id`),
    INDEX `idx_category` (`category`),
    INDEX `idx_name` (`name`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜谱表';
