-- 创建提醒配置表（如果不存在）
CREATE TABLE IF NOT EXISTS reminder_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    reminder_name VARCHAR(100) NOT NULL COMMENT '提醒名称',
    reminder_type VARCHAR(20) DEFAULT 'SYSTEM' COMMENT '提醒类型：WATER/MEDICINE/BIRTHDAY/FINANCE/EXPIRE/SYSTEM',
    create_type TINYINT DEFAULT 1 COMMENT '创建类型：1-用户创建 2-系统创建',
    create_by BIGINT COMMENT '创建人ID',
    
    -- 推送范围
    push_scope VARCHAR(20) DEFAULT 'SELF' COMMENT '推送范围：SELF-仅自己 ALL-全部用户 SPECIFIED-指定用户',
    target_user_ids TEXT COMMENT '目标用户ID列表(JSON格式)',
    
    -- 时间配置
    cron_expression VARCHAR(50) COMMENT 'Cron表达式（兼容旧版）',
    remind_time VARCHAR(10) COMMENT '提醒时间，如 08:00',
    frequency_type VARCHAR(20) COMMENT '频率类型：ONCE/DAILY/HOURLY/WEEKLY/MONTHLY/YEARLY/INTERVAL',
    frequency_config TEXT COMMENT '频率配置(JSON格式)',
    
    -- 内容配置
    title_template VARCHAR(200) COMMENT '标题模板',
    content_template TEXT COMMENT '内容模板',
    business_data TEXT COMMENT '业务数据(JSON格式)',
    
    -- 执行配置
    next_execute_time DATETIME COMMENT '下次执行时间',
    last_execute_time DATETIME COMMENT '上次执行时间',
    last_execute_result VARCHAR(20) COMMENT '执行结果：SUCCESS/FAILED/NO_TARGET',
    execute_count INT DEFAULT 0 COMMENT '已执行次数',
    max_execute_count INT DEFAULT 0 COMMENT '最大执行次数（0无限）',
    
    -- 状态控制
    status TINYINT DEFAULT 1 COMMENT '状态：0-停用 1-启用',
    priority TINYINT DEFAULT 5 COMMENT '优先级1-10',
    quiet_hours_start VARCHAR(10) COMMENT '免打扰开始时间：22:00',
    quiet_hours_end VARCHAR(10) COMMENT '免打扰结束时间：08:00',
    
    -- 时间戳
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_create_by (create_by),
    INDEX idx_status (status),
    INDEX idx_next_execute (next_execute_time),
    INDEX idx_frequency_type (frequency_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提醒配置表';
