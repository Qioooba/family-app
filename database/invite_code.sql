-- 家庭邀请码表
CREATE TABLE IF NOT EXISTS family_invite_code (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    family_id BIGINT NOT NULL COMMENT '家庭ID',
    code VARCHAR(20) NOT NULL COMMENT '邀请码',
    creator_id BIGINT NOT NULL COMMENT '创建者ID',
    max_uses INT DEFAULT 1 COMMENT '最大使用次数',
    used_count INT DEFAULT 0 COMMENT '已使用次数',
    expires_at DATETIME COMMENT '过期时间',
    status TINYINT DEFAULT 1 COMMENT '状态 0禁用 1启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_code (code),
    INDEX idx_family_id (family_id),
    INDEX idx_creator_id (creator_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家庭邀请码表';

-- 修改 sys_user 表，添加注册类型
ALTER TABLE sys_user ADD COLUMN register_type TINYINT DEFAULT 0 COMMENT '注册类型 0普通注册 1邀请注册' AFTER login_type;
