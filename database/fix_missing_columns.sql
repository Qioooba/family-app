-- 修复数据库缺失的列
-- 执行时间: 2026-02-27

-- 1. 添加 request_url 列到 api_log 表
ALTER TABLE api_log ADD COLUMN request_url VARCHAR(500);

-- 2. 添加 reminder_type 列到 task 表  
ALTER TABLE task ADD COLUMN reminder_type VARCHAR(20) DEFAULT 'time';

-- 3. 添加 deleted 列到 coupon 表
ALTER TABLE coupon ADD COLUMN deleted TINYINT(1) DEFAULT 0;

-- 4. 添加缺失的其他列
ALTER TABLE task ADD COLUMN location VARCHAR(255);
ALTER TABLE task ADD COLUMN weather VARCHAR(50);

SELECT 'Database fixes applied' as result;
