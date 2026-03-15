-- 提醒配置表结构修复脚本
-- 将旧版 reminder_config 表更新为与实体类匹配的结构

-- 检查表是否存在
drop table if exists reminder_config_backup;

-- 备份原表数据
create table reminder_config_backup as select * from reminder_config;

-- 删除原表
drop table reminder_config;

-- 创建新版表结构（与实体类 Reminder.java 完全匹配）
CREATE TABLE IF NOT EXISTS reminder_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    
    -- 基础信息
    reminder_name VARCHAR(100) NOT NULL COMMENT '提醒名称',
    reminder_type VARCHAR(30) NOT NULL COMMENT '类型：WATER-喝水 MEDICINE-用药 EXPIRE-保质期 BIRTHDAY-生日 FINANCE-财务 SYSTEM-系统',
    
    -- 创建者信息
    create_type TINYINT DEFAULT 1 COMMENT '1-个人 2-系统',
    create_by BIGINT NOT NULL COMMENT '创建人ID',
    
    -- 推送范围
    push_scope VARCHAR(20) DEFAULT 'SELF' COMMENT 'SELF-仅自己 SPECIFIED-指定用户 ALL-全部',
    target_user_ids JSON COMMENT '目标用户ID列表(JSON格式)',
    
    -- 时间配置
    cron_expression VARCHAR(100) COMMENT 'Cron表达式',
    remind_time VARCHAR(20) COMMENT '提醒时间(如08:00)，前端使用',
    frequency_type VARCHAR(20) COMMENT '频率类型：ONCE-一次性 DAILY-每天 HOURLY-每小时 WEEKLY-每周 MONTHLY-每月 YEARLY-每年 INTERVAL-间隔',
    frequency_config JSON COMMENT '频率配置(JSON格式)',
    
    -- 内容配置
    title_template VARCHAR(200) COMMENT '消息标题模板',
    content_template TEXT COMMENT '消息内容模板',
    business_data JSON COMMENT '业务数据(JSON格式)',
    
    -- 执行配置
    next_execute_time DATETIME COMMENT '下次执行时间',
    last_execute_time DATETIME COMMENT '上次执行时间',
    last_execute_result VARCHAR(20) COMMENT 'SUCCESS/FAILED/NO_TARGET',
    execute_count INT DEFAULT 0 COMMENT '已执行次数',
    max_execute_count INT DEFAULT 0 COMMENT '最大执行次数（0无限）',
    
    -- 状态控制
    status TINYINT DEFAULT 1 COMMENT '0-停用 1-启用 2-暂停',
    priority TINYINT DEFAULT 5 COMMENT '优先级1-10',
    
    -- 免打扰
    quiet_hours_start VARCHAR(10) COMMENT '免打扰开始时间：22:00',
    quiet_hours_end VARCHAR(10) COMMENT '免打扰结束时间：08:00',
    
    -- 元数据
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_create_by (create_by),
    INDEX idx_status (status),
    INDEX idx_next_execute (next_execute_time),
    INDEX idx_reminder_type (reminder_type),
    INDEX idx_frequency_type (frequency_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提醒配置表';

-- 从备份恢复数据（兼容旧字段）
INSERT INTO reminder_config (
    id,
    reminder_name,
    reminder_type,
    create_type,
    create_by,
    push_scope,
    target_user_ids,
    cron_expression,
    remind_time,
    title_template,
    content_template,
    business_data,
    status,
    next_execute_time,
    last_execute_time,
    last_execute_result,
    remind_count,
    create_time,
    update_time
)
SELECT 
    id,
    reminder_name,
    reminder_type,
    create_type,
    create_by,
    push_scope,
    CASE 
        WHEN target_user_ids IS NOT NULL AND target_user_ids != '' 
        THEN CONCAT('[', target_user_ids, ']')
        ELSE NULL 
    END as target_user_ids,
    cron_expression,
    remind_time,
    title_template,
    content_template,
    business_data,
    status,
    next_remind_time as next_execute_time,
    last_remind_time as last_execute_time,
    last_remind_status as last_execute_result,
    remind_count as execute_count,
    create_time,
    update_time
FROM reminder_config_backup;

-- 删除备份表
drop table reminder_config_backup;

-- 验证表结构
SHOW COLUMNS FROM reminder_config;
