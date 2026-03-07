-- ============================================
-- 修复邀请码问题 - 插入默认邀请码
-- ============================================

-- 插入默认邀请码 111222
-- 注意: 请确保 family_id=1 的家庭存在，且 creator_id=1 的用户存在

INSERT INTO family_invite_code (family_id, code, creator_id, max_uses, used_count, expires_at, status, create_time) 
VALUES (1, '111222', 1, 100, 0, '2026-12-31 23:59:59', 1, NOW())
ON DUPLICATE KEY UPDATE 
    expires_at = '2026-12-31 23:59:59',
    status = 1,
    max_uses = 100;

-- 查看插入结果
SELECT * FROM family_invite_code WHERE code = '111222';
