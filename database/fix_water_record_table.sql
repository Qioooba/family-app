-- 修复喝水记录功能 - 创建water_record表
-- 执行时间: 2026-02-23

CREATE TABLE IF NOT EXISTS water_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    amount INT NOT NULL COMMENT '喝水量(ml)',
    record_date DATE NOT NULL COMMENT '记录日期',
    record_time TIME COMMENT '记录时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_date (user_id, record_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='喝水记录表';

-- 验证表是否创建成功
-- DESCRIBE water_record;
