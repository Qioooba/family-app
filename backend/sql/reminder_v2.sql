-- 提醒配置表（完整版）
CREATE TABLE IF NOT EXISTS reminder_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    
    -- 基础信息
    reminder_name VARCHAR(100) NOT NULL COMMENT '提醒名称',
    reminder_type VARCHAR(30) NOT NULL COMMENT '类型：WATER/MEDICINE/EXPIRE/BIRTHDAY/FINANCE/SYSTEM/SCHEDULE',
    
    -- 创建者
    create_type TINYINT DEFAULT 1 COMMENT '1-个人 2-系统',
    create_by BIGINT NOT NULL COMMENT '创建人ID',
    
    -- 推送范围
    push_scope VARCHAR(20) DEFAULT 'SELF' COMMENT 'SELF-仅自己 SPECIFIED-指定用户 ALL-全部',
    target_user_ids VARCHAR(500) COMMENT '目标用户ID列表',
    
    -- 时间配置
    cron_expression VARCHAR(100) COMMENT 'Cron表达式',
    frequency_type VARCHAR(20) COMMENT 'ONCE/DAILY/HOURLY/WEEKLY/MONTHLY/YEARLY/INTERVAL',
    frequency_config VARCHAR(500) COMMENT '频率配置JSON',
    
    -- 内容
    title_template VARCHAR(200) COMMENT '标题模板',
    content_template TEXT COMMENT '内容模板',
    business_data VARCHAR(1000) COMMENT '业务数据JSON',
    
    -- 执行记录
    next_execute_time DATETIME COMMENT '下次执行时间',
    last_execute_time DATETIME COMMENT '上次执行时间',
    last_execute_result VARCHAR(20) COMMENT '结果',
    execute_count INT DEFAULT 0 COMMENT '执行次数',
    max_execute_count INT DEFAULT 0 COMMENT '最大次数',
    
    -- 状态
    status TINYINT DEFAULT 1 COMMENT '0-停用 1-启用 2-暂停',
    priority TINYINT DEFAULT 5 COMMENT '优先级',
    
    -- 免打扰
    quiet_hours_start VARCHAR(10) COMMENT '免打扰开始',
    quiet_hours_end VARCHAR(10) COMMENT '免打扰结束',
    
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_create_by (create_by),
    INDEX idx_status (status),
    INDEX idx_next_execute (next_execute_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提醒配置表';

-- 提醒执行日志表
CREATE TABLE IF NOT EXISTS reminder_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    reminder_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    execute_time DATETIME,
    actual_execute_time DATETIME,
    finish_time DATETIME,
    title VARCHAR(200),
    content TEXT,
    push_status VARCHAR(20),
    push_result TEXT,
    error_msg TEXT,
    read_status TINYINT DEFAULT 0,
    read_time DATETIME,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_reminder_id (reminder_id),
    INDEX idx_user_id (user_id),
    INDEX idx_execute_time (execute_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提醒日志表';

-- 节假日配置表
CREATE TABLE IF NOT EXISTS holiday_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    holiday_date DATE NOT NULL,
    holiday_name VARCHAR(50),
    holiday_type VARCHAR(20),
    year INT,
    UNIQUE KEY uk_date (holiday_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='节假日配置表';

-- 插入示例数据
INSERT INTO reminder_config (reminder_name, reminder_type, create_type, create_by, push_scope, frequency_type, frequency_config, title_template, business_data, status, next_execute_time) VALUES
('🌅 早安推送', 'SYSTEM', 2, 1, 'ALL', 'DAILY', '{"fixedTime":"08:00","workDaysOnly":true}', '🌅 早安{userName}！', '{"includeWeather":true}', 1, NOW()),
('💧 喝水提醒', 'WATER', 1, 7, 'SELF', 'DAILY', '{"fixedTime":"09:00"}', '💧 该喝水了', '{"cupSize":200}', 1, NOW()),
('⏰ 任务到期提醒', 'SYSTEM', 2, 1, 'SPECIFIED', 'INTERVAL', '{"intervalMinutes":5}', '⏰ 待办提醒', '{}', 1, NOW());
