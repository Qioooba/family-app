-- 创建用户积分表
CREATE TABLE IF NOT EXISTS `user_points` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `total_points` INT DEFAULT 0 COMMENT '总积分',
    `available_points` INT DEFAULT 0 COMMENT '可用积分',
    `spent_points` INT DEFAULT 0 COMMENT '已花费积分',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户积分表';

-- 创建积分流水表
CREATE TABLE IF NOT EXISTS `points_transaction` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `points` INT NOT NULL COMMENT '变动积分（正数为增加，负数为减少）',
    `type` VARCHAR(50) NOT NULL COMMENT '类型：earn/spend',
    `source` VARCHAR(100) COMMENT '来源',
    `description` VARCHAR(255) COMMENT '描述',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='积分流水表';

-- 初始化现有用户的积分记录
INSERT IGNORE INTO `user_points` (`user_id`, `total_points`, `available_points`, `spent_points`, `update_time`)
SELECT `id`, 0, 0, 0, NOW() FROM `sys_user` WHERE `status` = 1;
