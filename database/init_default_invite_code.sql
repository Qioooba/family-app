-- =============================================
-- 家庭小程序 - 邀请码强制验证功能
-- 初始化默认邀请码脚本
-- =============================================

-- 创建邀请码表（如果不存在）
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

-- =============================================
-- 创建默认家庭（如果不存在）
-- =============================================
INSERT IGNORE INTO family (id, name, creator_id, invite_code, member_count, status, create_time, update_time)
VALUES (1, '默认家庭', 1, 'DEFAULT001', 1, 1, NOW(), NOW());

-- =============================================
-- 创建默认邀请码
-- 默认邀请码：FAMILY01，有效期10年，最多使用10000次
-- =============================================
INSERT INTO family_invite_code (code, family_id, creator_id, max_uses, used_count, status, expires_at, create_time)
SELECT 'FAMILY01', 1, 1, 10000, 0, 1, DATE_ADD(NOW(), INTERVAL 10 YEAR), NOW()
WHERE NOT EXISTS (SELECT 1 FROM family_invite_code WHERE code = 'FAMILY01');

-- =============================================
-- 查看所有有效邀请码
-- =============================================
SELECT 
    id, 
    code, 
    family_id, 
    max_uses, 
    used_count,
    (max_uses - used_count) AS remaining_uses,
    status,
    expires_at,
    create_time 
FROM family_invite_code 
WHERE status = 1 
ORDER BY create_time DESC;
