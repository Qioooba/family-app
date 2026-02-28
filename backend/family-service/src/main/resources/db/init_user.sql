-- 初始化用户数据
-- 创建一个 admin 测试用户，密码为 123456

-- 首先检查表是否存在 password 字段
DESCRIBE family_app.sys_user;

-- 检查是否有用户存在
SELECT COUNT(*) FROM family_app.sys_user;

-- 创建 admin 用户（密码：123456，BCrypt加密）
-- 密码 123456 的 BCrypt 哈希值示例
INSERT INTO family_app.sys_user 
    (username, password, nickname, phone, status, create_time, update_time)
SELECT 'admin', 
       '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iD5T1F6m',  -- 密码：123456
       '管理员', 
       '13800138000', 
       1, 
       NOW(), 
       NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM family_app.sys_user WHERE username = 'admin'
);

-- 检查用户是否创建成功
SELECT id, username, nickname, phone, status, password IS NOT NULL as has_password 
FROM family_app.sys_user 
WHERE username = 'admin';
