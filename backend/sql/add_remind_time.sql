-- 添加 remind_time 字段到 reminder_config 表
-- 用于存储提醒的固定时间（如每天的08:00）

ALTER TABLE reminder_config 
ADD COLUMN IF NOT EXISTS remind_time VARCHAR(10) COMMENT '提醒时间，如08:00' AFTER frequency_config;

-- 添加索引
ALTER TABLE reminder_config ADD INDEX IF NOT EXISTS idx_remind_time (remind_time);

-- 查看表结构
SHOW COLUMNS FROM reminder_config;
