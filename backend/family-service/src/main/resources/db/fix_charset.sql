-- ============================================
-- 修复数据库字符集乱码问题
-- 执行方式：在 MySQL 客户端中运行
-- ============================================

USE family_app;

-- 1. 查看当前数据库字符集
SELECT 
    @@character_set_database as '数据库字符集',
    @@collation_database as '数据库排序规则';

-- 2. 查看表的字符集
SELECT 
    table_name,
    table_collation
FROM information_schema.tables 
WHERE table_schema = 'family_app';

-- 3. 修复数据库字符集（如果需要）
ALTER DATABASE family_app CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 4. 修复用户表字符集
ALTER TABLE sys_user CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 5. 修复任务表字符集
ALTER TABLE task CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 6. 修复家庭成员表字符集
ALTER TABLE family_member CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 7. 检查修复后的字符集
SELECT 
    table_name,
    table_collation
FROM information_schema.tables 
WHERE table_schema = 'family_app';

-- 8. 更新已有乱码数据（如果有）
-- 注意：这行会删除所有用户数据，请谨慎执行
-- DELETE FROM sys_user WHERE nickname LIKE '%��%' OR nickname LIKE '%æ%' OR nickname LIKE '%ç%';

-- 9. 插入一个测试用户（使用正确的UTF-8编码）
INSERT INTO sys_user (username, password, nickname, phone, status, create_time, update_time)
SELECT 'test_utf8', 
       '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iD5T1F6m',
       '测试用户',
       '13900139000',
       1,
       NOW(),
       NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE username = 'test_utf8');

-- 10. 验证测试用户的中文是否正确显示
SELECT id, username, nickname FROM sys_user WHERE username = 'test_utf8';
