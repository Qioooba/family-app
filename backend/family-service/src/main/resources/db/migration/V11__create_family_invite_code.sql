-- 创建家庭邀请码表
CREATE TABLE IF NOT EXISTS family_invite_code (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '邀请码ID',
    family_id BIGINT NOT NULL COMMENT '家庭ID',
    code VARCHAR(20) NOT NULL COMMENT '邀请码',
    creator_id BIGINT NOT NULL COMMENT '创建者ID',
    max_uses INT DEFAULT 1 COMMENT '最大使用次数',
    used_count INT DEFAULT 0 COMMENT '已使用次数',
    expires_at DATETIME COMMENT '过期时间',
    status TINYINT DEFAULT 1 COMMENT '状态: 1-有效, 0-无效',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_code (code),
    INDEX idx_family_id (family_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家庭邀请码表';
