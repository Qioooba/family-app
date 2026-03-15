-- 提醒配置表结构增量修复脚本
-- 在保留现有数据的情况下添加缺失的字段

-- 添加频率相关字段
ALTER TABLE reminder_config 
ADD COLUMN IF NOT EXISTS frequency_type VARCHAR(20) COMMENT '频率类型：ONCE-一次性 DAILY-每天 HOURLY-每小时 WEEKLY-每周 MONTHLY-每月 YEARLY-每年 INTERVAL-间隔' AFTER cron_expression;

ALTER TABLE reminder_config 
ADD COLUMN IF NOT EXISTS frequency_config JSON COMMENT '频率配置(JSON格式)' AFTER frequency_type;

-- 添加执行统计字段（如果字段名不同则先添加再迁移数据）
ALTER TABLE reminder_config 
ADD COLUMN IF NOT EXISTS execute_count INT DEFAULT 0 COMMENT '已执行次数' AFTER last_remind_status;

ALTER TABLE reminder_config 
ADD COLUMN IF NOT EXISTS max_execute_count INT DEFAULT 0 COMMENT '最大执行次数（0无限）' AFTER execute_count;

-- 添加优先级字段
ALTER TABLE reminder_config 
ADD COLUMN IF NOT EXISTS priority TINYINT DEFAULT 5 COMMENT '优先级1-10' AFTER status;

-- 添加免打扰字段
ALTER TABLE reminder_config 
ADD COLUMN IF NOT EXISTS quiet_hours_start VARCHAR(10) COMMENT '免打扰开始时间：22:00' AFTER priority;

ALTER TABLE reminder_config 
ADD COLUMN IF NOT EXISTS quiet_hours_end VARCHAR(10) COMMENT '免打扰结束时间：08:00' AFTER quiet_hours_start;

-- 添加新命名的时间字段（如果旧字段存在则迁移数据）
ALTER TABLE reminder_config 
ADD COLUMN IF NOT EXISTS next_execute_time DATETIME COMMENT '下次执行时间' AFTER remind_count;

ALTER TABLE reminder_config 
ADD COLUMN IF NOT EXISTS last_execute_time DATETIME COMMENT '上次执行时间' AFTER next_execute_time;

ALTER TABLE reminder_config 
ADD COLUMN IF NOT EXISTS last_execute_result VARCHAR(20) COMMENT 'SUCCESS/FAILED/NO_TARGET' AFTER last_execute_time;

-- 数据迁移：从旧字段复制数据到新字段
UPDATE reminder_config SET 
    next_execute_time = next_remind_time 
WHERE next_execute_time IS NULL AND next_remind_time IS NOT NULL;

UPDATE reminder_config SET 
    last_execute_time = last_remind_time 
WHERE last_execute_time IS NULL AND last_remind_time IS NOT NULL;

UPDATE reminder_config SET 
    last_execute_result = last_remind_status 
WHERE last_execute_result IS NULL AND last_remind_status IS NOT NULL;

UPDATE reminder_config SET 
    execute_count = remind_count 
WHERE execute_count = 0 AND remind_count IS NOT NULL;

-- 修改target_user_ids为JSON类型（如果当前是TEXT）
-- 注意：MySQL 5.7+ 支持JSON类型，如果转换失败可以跳过
-- ALTER TABLE reminder_config MODIFY COLUMN target_user_ids JSON COMMENT '目标用户ID列表(JSON格式)';

-- 添加索引
ALTER TABLE reminder_config ADD INDEX IF NOT EXISTS idx_next_execute (next_execute_time);
ALTER TABLE reminder_config ADD INDEX IF NOT EXISTS idx_frequency_type (frequency_type);

-- 验证表结构
SHOW COLUMNS FROM reminder_config;
