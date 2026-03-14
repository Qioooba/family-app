-- 提醒配置表（完整版）
CREATE TABLE IF NOT EXISTS reminder_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    
    -- 基础信息
    reminder_name VARCHAR(100) NOT NULL COMMENT '提醒名称：如"早上喝水"',
    reminder_type VARCHAR(30) NOT NULL COMMENT '类型：WATER-喝水 MEDICINE-用药 EXPIRE-保质期 BIRTHDAY-生日 FINANCE-财务 SYSTEM-系统 SCHEDULE-定时任务',
    
    -- 创建者信息
    create_type TINYINT DEFAULT 1 COMMENT '1-个人 2-系统',
    create_by BIGINT NOT NULL COMMENT '创建人ID',
    
    -- 推送范围
    push_scope VARCHAR(20) DEFAULT 'SELF' COMMENT 'SELF-仅自己 SPECIFIED-指定用户 ALL-全部',
    target_user_ids JSON COMMENT '目标用户ID列表',
    
    -- === 时间配置（核心）===
    
    -- 方式1：Cron表达式（最灵活）
    cron_expression VARCHAR(100) COMMENT 'Cron表达式：如 0 0 8 * * ?（每天8点）',
    
    -- 方式2：固定频率（更直观）
    frequency_type VARCHAR(20) COMMENT '频率类型：ONCE-一次性 DAILY-每天 HOURLY-每小时 WEEKLY-每周 MONTHLY-每月 YEARLY-每年 INTERVAL-间隔',
    frequency_config JSON COMMENT '频率配置：',
    /*
    {
        "fixedTime": "08:00",           -- 固定时间点
        "weekDays": [1,3,5],            -- 每周几（1=周一）
        "monthDay": 15,                 -- 每月几号
        "yearMonthDay": "03-15",        -- 每年几月几号
        "intervalMinutes": 60,          -- 每隔多少分钟
        "intervalHours": 1,             -- 每隔多少小时
        "intervalDays": 3,              -- 每隔多少天
        "startTime": "09:00",           -- 生效时间范围开始
        "endTime": "18:00",             -- 生效时间范围结束
        "workDaysOnly": true,           -- 仅工作日
        "excludeHoliday": true          -- 排除节假日
    }
    */
    
    -- === 内容配置 ===
    title_template VARCHAR(200) COMMENT '消息标题模板',
    -- 支持变量：{userName} {taskName} {time} {date} {weather} {todoCount}
    content_template TEXT COMMENT '消息内容模板',
    -- 支持HTML标签
    
    -- 业务数据（不同类型不同结构）
    business_data JSON COMMENT '业务数据：',
    /*
    【喝水】
    { "targetTimes": 8, "cupSize": 200, "workHours": ["09:00", "18:00"] }
    
    【用药】
    { "medicineName": "阿莫西林", "dosage": "2粒", "times": ["08:00", "12:00", "20:00"] }
    
    【保质期】
    { "itemName": "牛奶", "expiryDate": "2026-03-15", "remindBeforeDays": 3 }
    
    【定时任务-早安推送】
    { "includeWeather": true, "includeTodo": true, "includeGreeting": true }
    
    【定时任务-到期提醒】
    { "remindBeforeMinutes": 30, "remindTypes": ["ASSIGNED", "CREATED"] }
    */
    
    -- === 执行配置 ===
    next_execute_time DATETIME COMMENT '下次执行时间（系统自动计算）',
    last_execute_time DATETIME COMMENT '上次执行时间',
    last_execute_result VARCHAR(20) COMMENT 'SUCCESS/FAILED',
    execute_count INT DEFAULT 0 COMMENT '已执行次数',
    max_execute_count INT DEFAULT 0 COMMENT '最大执行次数（0无限）',
    
    -- === 状态控制 ===
    status TINYINT DEFAULT 1 COMMENT '0-停用 1-启用 2-暂停',
    priority TINYINT DEFAULT 5 COMMENT '优先级1-10',
    
    -- === 免打扰 ===
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

-- 提醒执行日志表
CREATE TABLE IF NOT EXISTS reminder_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    reminder_id BIGINT NOT NULL COMMENT '提醒ID',
    user_id BIGINT NOT NULL COMMENT '接收用户ID',
    
    execute_time DATETIME COMMENT '计划执行时间',
    actual_execute_time DATETIME COMMENT '实际执行时间',
    finish_time DATETIME COMMENT '完成时间',
    
    title VARCHAR(200) COMMENT '发送的标题',
    content TEXT COMMENT '发送的内容',
    
    push_status VARCHAR(20) COMMENT 'SUCCESS/FAILED/SKIPPED',
    push_result TEXT COMMENT '推送返回结果',
    error_msg TEXT COMMENT '错误信息',
    
    read_status TINYINT DEFAULT 0 COMMENT '0-未读 1-已读',
    read_time DATETIME COMMENT '阅读时间',
    
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_reminder_id (reminder_id),
    INDEX idx_user_id (user_id),
    INDEX idx_execute_time (execute_time),
    INDEX idx_status (push_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提醒执行日志表';

-- 节假日配置表
CREATE TABLE IF NOT EXISTS holiday_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    holiday_date DATE NOT NULL COMMENT '节假日日期',
    holiday_name VARCHAR(50) COMMENT '节假日名称',
    holiday_type VARCHAR(20) COMMENT 'HOLIDAY-节假日 WORKDAY-调休工作日',
    year INT COMMENT '年份',
    
    UNIQUE KEY uk_date (holiday_date),
    INDEX idx_year (year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='节假日配置表';

-- 插入示例数据

-- 1. 早安推送（系统定时任务）
INSERT INTO reminder_config (
    reminder_name, reminder_type, create_type, create_by, push_scope, target_user_ids,
    cron_expression, frequency_type, frequency_config,
    title_template, content_template, business_data, status
) VALUES (
    '🌅 早安推送',
    'SYSTEM',
    2,
    1,
    'ALL',
    NULL,
    '0 0 8 * * ?',
    'DAILY',
    '{"fixedTime": "08:00", "workDaysOnly": true}',
    '🌅 早安{userName}！今日{todoCount}项待办',
    '☀️ {weather}\n\n📋 今日待办：\n{todoList}\n\n💪 新的一天，加油！',
    '{"includeWeather": true, "includeTodo": true}',
    1
);

-- 2. 喝水提醒（个人定时任务）
INSERT INTO reminder_config (
    reminder_name, reminder_type, create_type, create_by, push_scope,
    frequency_type, frequency_config,
    title_template, business_data, status
) VALUES (
    '💧 早上喝水',
    'WATER',
    1,
    7,
    'SELF',
    'DAILY',
    '{"fixedTime": "08:00", "workHours": ["09:00", "18:00"]}',
    '💧 该喝水了！已喝{cupCount}杯',
    '{"targetTimes": 8, "cupSize": 200}',
    1
);

-- 3. 任务到期提醒（系统自动）
INSERT INTO reminder_config (
    reminder_name, reminder_type, create_type, create_by, push_scope,
    cron_expression, frequency_type, frequency_config,
    title_template, content_template, business_data, status
) VALUES (
    '⏰ 任务到期提醒',
    'SYSTEM',
    2,
    1,
    'SPECIFIED',
    '[7, 16]',
    '0 */5 * * * ?',
    'INTERVAL',
    '{"intervalMinutes": 5}',
    '⏰ 待办提醒：{taskName}',
    '任务：{taskName}\n截止时间：{dueTime}\n剩余时间：{remainTime}\n\n点击查看详情',
    '{"remindBeforeMinutes": 30}',
    1
);

-- 4. 生日提醒（每年）
INSERT INTO reminder_config (
    reminder_name, reminder_type, create_type, create_by, push_scope,
    frequency_type, frequency_config,
    title_template, business_data, status
) VALUES (
    '🎂 妈妈生日',
    'BIRTHDAY',
    1,
    7,
    'SELF',
    'YEARLY',
    '{"yearMonthDay": "05-20", "remindBeforeDays": 3}',
    '🎂 {personName}的生日快到了！',
    '{"personName": "妈妈", "eventDate": "1970-05-20"}',
    1
);

-- 5. 药品过期提醒（一次性）
INSERT INTO reminder_config (
    reminder_name, reminder_type, create_type, create_by, push_scope,
    frequency_type, frequency_config,
    title_template, business_data, status
) VALUES (
    '💊 阿莫西林到期',
    'MEDICINE',
    1,
    7,
    'SELF',
    'ONCE',
    '{"fixedDate": "2026-03-20"}',
    '💊 药品即将到期',
    '{"medicineName": "阿莫西林", "expiryDate": "2026-03-20", "remindBeforeDays": 7}',
    1
);

-- 6. 久坐提醒（每小时）
INSERT INTO reminder_config (
    reminder_name, reminder_type, create_type, create_by, push_scope,
    frequency_type, frequency_config,
    title_template, business_data, status
) VALUES (
    '🪑 久坐提醒',
    'WATER',
    1,
    7,
    'SELF',
    'INTERVAL',
    '{"intervalMinutes": 60, "workHours": ["09:00", "18:00"]}',
    '🪑 起来活动一下吧！',
    '{"intervalMinutes": 60}',
    1
);
