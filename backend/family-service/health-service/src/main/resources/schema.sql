-- 喝水记录表
CREATE TABLE IF NOT EXISTS water_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    amount INT NOT NULL COMMENT '喝水量(ml)',
    record_date DATE NOT NULL COMMENT '记录日期',
    record_time TIME COMMENT '记录时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_date (user_id, record_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='喝水记录表';

-- 用户饮水目标设置表
CREATE TABLE IF NOT EXISTS user_water_goal (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    target_amount INT NOT NULL DEFAULT 2000 COMMENT '每日目标饮水量（毫升）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_id (user_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户饮水目标设置表';
