-- 用户定位表（替代 Redis 存储用户定位）
CREATE TABLE IF NOT EXISTS user_location (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    location VARCHAR(100) COMMENT '位置名称，如：南京',
    latitude DOUBLE COMMENT '纬度',
    longitude DOUBLE COMMENT '经度',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户定位表';

-- 场景提醒日志表（防重复提醒）
CREATE TABLE IF NOT EXISTS scene_reminder_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    reminder_id BIGINT NOT NULL COMMENT '提醒配置ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    scene_type VARCHAR(30) NOT NULL COMMENT '场景类型',
    remind_date DATE NOT NULL COMMENT '提醒日期',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_reminder_date (reminder_id, remind_date),
    INDEX idx_user_date (user_id, remind_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='场景提醒日志表';

-- 喝水记录表
CREATE TABLE IF NOT EXISTS user_water_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    record_date DATE NOT NULL COMMENT '记录日期',
    cup_count INT DEFAULT 0 COMMENT '喝了多少杯',
    last_drink_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '最后一次喝水时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_date (user_id, record_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='喝水记录表';

-- 久坐记录表
CREATE TABLE IF NOT EXISTS user_sedentary_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    record_date DATE NOT NULL COMMENT '记录日期',
    last_sit_time DATETIME COMMENT '最后一次久坐开始时间',
    remind_count INT DEFAULT 0 COMMENT '今日提醒次数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_date (user_id, record_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='久坐记录表';

-- 护眼记录表
CREATE TABLE IF NOT EXISTS user_eye_rest_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    record_date DATE NOT NULL COMMENT '记录日期',
    last_screen_time DATETIME COMMENT '最后一次看屏幕开始时间',
    remind_count INT DEFAULT 0 COMMENT '今日提醒次数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_date (user_id, record_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='护眼记录表';
