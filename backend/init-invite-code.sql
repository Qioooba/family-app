-- 初始化默认邀请码（如果还没有的话）
-- 默认邀请码：111222，有效期10年，最多使用10000次

INSERT INTO family_invite_code (code, family_id, creator_id, max_uses, used_count, status, expires_at, create_time)
SELECT '111222', 1, 1, 10000, 0, 1, DATE_ADD(NOW(), INTERVAL 10 YEAR), NOW()
WHERE NOT EXISTS (SELECT 1 FROM family_invite_code WHERE code = '111222');

-- 查看所有邀请码
SELECT id, code, family_id, max_uses, used_count, status, expires_at, create_time 
FROM family_invite_code 
ORDER BY create_time DESC;
