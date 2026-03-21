USE family_app;

CREATE TABLE IF NOT EXISTS task_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    family_id BIGINT NOT NULL COMMENT '家庭ID',
    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    icon VARCHAR(50) DEFAULT NULL COMMENT '图标',
    color VARCHAR(20) DEFAULT NULL COMMENT '颜色',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务分类表';

INSERT INTO task_category (family_id, name, icon, color, sort_order, status)
SELECT 0, '购物清单', 'shopping-cart', '#FF6B6B', 1, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM task_category WHERE family_id = 0 AND name = '购物清单');
INSERT INTO task_category (family_id, name, icon, color, sort_order, status)
SELECT 0, '待办事项', 'file-text', '#4ECDC4', 2, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM task_category WHERE family_id = 0 AND name = '待办事项');
INSERT INTO task_category (family_id, name, icon, color, sort_order, status)
SELECT 0, '家务分工', 'home', '#45B7D1', 3, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM task_category WHERE family_id = 0 AND name = '家务分工');
INSERT INTO task_category (family_id, name, icon, color, sort_order, status)
SELECT 0, '财务提醒', 'account-book', '#96CEB4', 4, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM task_category WHERE family_id = 0 AND name = '财务提醒');
INSERT INTO task_category (family_id, name, icon, color, sort_order, status)
SELECT 0, '育儿相关', 'smile', '#FFEAA7', 5, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM task_category WHERE family_id = 0 AND name = '育儿相关');
INSERT INTO task_category (family_id, name, icon, color, sort_order, status)
SELECT 0, '健康医疗', 'medicine-box', '#DDA0DD', 6, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM task_category WHERE family_id = 0 AND name = '健康医疗');

CREATE TABLE IF NOT EXISTS family_album (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    family_id BIGINT NOT NULL COMMENT '家庭ID',
    name VARCHAR(100) NOT NULL COMMENT '相册名称',
    description VARCHAR(500) DEFAULT NULL COMMENT '相册描述',
    cover_url VARCHAR(500) DEFAULT NULL COMMENT '封面图片URL',
    type TINYINT DEFAULT 1 COMMENT '相册类型',
    creator_id BIGINT DEFAULT NULL COMMENT '创建者ID',
    photo_count INT DEFAULT 0 COMMENT '照片数量',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_family_id (family_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家庭相册表';

CREATE TABLE IF NOT EXISTS album_photo (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    album_id BIGINT NOT NULL COMMENT '相册ID',
    family_id BIGINT NOT NULL COMMENT '家庭ID',
    uploader_id BIGINT DEFAULT NULL COMMENT '上传者ID',
    url VARCHAR(500) NOT NULL COMMENT '压缩图URL',
    thumb_url VARCHAR(500) DEFAULT NULL COMMENT '缩略图URL',
    original_url VARCHAR(500) DEFAULT NULL COMMENT '原图URL',
    description VARCHAR(500) DEFAULT NULL COMMENT '照片描述',
    photo_date DATE DEFAULT NULL COMMENT '拍摄日期',
    location VARCHAR(200) DEFAULT NULL COMMENT '拍摄地点',
    latitude DECIMAL(10, 8) DEFAULT NULL COMMENT '纬度',
    longitude DECIMAL(11, 8) DEFAULT NULL COMMENT '经度',
    width INT DEFAULT NULL COMMENT '宽度',
    height INT DEFAULT NULL COMMENT '高度',
    file_size BIGINT DEFAULT NULL COMMENT '文件大小',
    tags VARCHAR(500) DEFAULT NULL COMMENT '标签',
    is_favorite TINYINT DEFAULT 0 COMMENT '是否收藏',
    status TINYINT DEFAULT 1 COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_album_id (album_id),
    INDEX idx_family_id (family_id),
    INDEX idx_photo_date (photo_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='相册照片表';

CREATE TABLE IF NOT EXISTS album_share (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    album_id BIGINT NOT NULL COMMENT '相册ID',
    family_id BIGINT NOT NULL COMMENT '家庭ID',
    shared_by BIGINT DEFAULT NULL COMMENT '分享者',
    share_type TINYINT DEFAULT 1 COMMENT '分享类型',
    share_code VARCHAR(32) DEFAULT NULL COMMENT '分享码',
    share_url VARCHAR(500) DEFAULT NULL COMMENT '分享链接',
    password VARCHAR(32) DEFAULT NULL COMMENT '访问密码',
    expire_time DATETIME DEFAULT NULL COMMENT '过期时间',
    view_count INT DEFAULT 0 COMMENT '查看次数',
    download_count INT DEFAULT 0 COMMENT '下载次数',
    status TINYINT DEFAULT 1 COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_album_id (album_id),
    INDEX idx_family_id (family_id),
    INDEX idx_share_code (share_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='相册分享表';

CREATE TABLE IF NOT EXISTS photo_tag (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    photo_id BIGINT NOT NULL COMMENT '照片ID',
    family_id BIGINT NOT NULL COMMENT '家庭ID',
    tag_name VARCHAR(50) NOT NULL COMMENT '标签名',
    tag_type TINYINT DEFAULT 4 COMMENT '标签类型',
    user_id BIGINT DEFAULT NULL COMMENT '创建者',
    status TINYINT DEFAULT 1 COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_photo_id (photo_id),
    INDEX idx_family_id (family_id),
    INDEX idx_tag_name (tag_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='照片标签表';

CREATE TABLE IF NOT EXISTS family_schedule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    family_id BIGINT NOT NULL COMMENT '家庭ID',
    user_id BIGINT NOT NULL COMMENT '执行人ID',
    creator_id BIGINT DEFAULT NULL COMMENT '创建者ID',
    task_name VARCHAR(100) NOT NULL COMMENT '任务名',
    day_of_week INT NOT NULL COMMENT '星期几',
    start_date DATE DEFAULT NULL COMMENT '开始日期',
    end_date DATE DEFAULT NULL COMMENT '结束日期',
    status TINYINT DEFAULT 1 COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_family_user (family_id, user_id),
    INDEX idx_date_range (start_date, end_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家庭排班表';

CREATE TABLE IF NOT EXISTS task_subtask (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL COMMENT '父任务ID',
    title VARCHAR(100) NOT NULL COMMENT '子任务标题',
    status TINYINT DEFAULT 0 COMMENT '状态',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_task_id (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='子任务表';

CREATE TABLE IF NOT EXISTS task_schedule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    family_id BIGINT NOT NULL COMMENT '家庭ID',
    task_name VARCHAR(100) NOT NULL COMMENT '任务名称',
    assignee_id BIGINT NOT NULL COMMENT '指派用户ID',
    schedule_type ENUM('daily', 'weekly', 'monthly') DEFAULT 'weekly' COMMENT '排班类型',
    schedule_day INT DEFAULT NULL COMMENT '排班日',
    status TINYINT DEFAULT 1 COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_family_id (family_id),
    INDEX idx_assignee_id (assignee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家务排班表';

CREATE TABLE IF NOT EXISTS task_attachment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL COMMENT '任务ID',
    uploader_id BIGINT DEFAULT NULL COMMENT '上传者ID',
    file_name VARCHAR(255) NOT NULL COMMENT '文件名',
    file_url VARCHAR(500) NOT NULL COMMENT '文件URL',
    file_size BIGINT DEFAULT NULL COMMENT '文件大小',
    file_type VARCHAR(100) DEFAULT NULL COMMENT '文件类型',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_task_id (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务附件表';

CREATE TABLE IF NOT EXISTS task_comment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL COMMENT '任务ID',
    user_id BIGINT NOT NULL COMMENT '评论用户ID',
    content TEXT NOT NULL COMMENT '评论内容',
    parent_id BIGINT DEFAULT 0 COMMENT '父评论ID',
    reply_count INT DEFAULT 0 COMMENT '回复数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_task_id (task_id),
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务评论表';

CREATE TABLE IF NOT EXISTS task_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL COMMENT '任务ID',
    user_id BIGINT DEFAULT NULL COMMENT '操作用户ID',
    action VARCHAR(50) NOT NULL COMMENT '动作',
    field_name VARCHAR(100) DEFAULT NULL COMMENT '变更字段',
    old_value TEXT DEFAULT NULL COMMENT '旧值',
    new_value TEXT DEFAULT NULL COMMENT '新值',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_task_id (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务历史表';

CREATE TABLE IF NOT EXISTS task_template (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    family_id BIGINT DEFAULT NULL COMMENT '家庭ID',
    creator_id BIGINT DEFAULT NULL COMMENT '创建者',
    name VARCHAR(100) NOT NULL COMMENT '模板名称',
    title VARCHAR(200) NOT NULL COMMENT '任务标题',
    content TEXT DEFAULT NULL COMMENT '任务内容',
    category_id BIGINT DEFAULT NULL COMMENT '分类ID',
    priority TINYINT DEFAULT 0 COMMENT '优先级',
    repeat_type VARCHAR(20) DEFAULT 'none' COMMENT '重复类型',
    repeat_rule VARCHAR(500) DEFAULT NULL COMMENT '重复规则',
    tags VARCHAR(500) DEFAULT NULL COMMENT '标签',
    estimated_minutes INT DEFAULT NULL COMMENT '预估分钟数',
    is_public TINYINT DEFAULT 0 COMMENT '是否公开',
    usage_count INT DEFAULT 0 COMMENT '使用次数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_family_id (family_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务模板表';

CREATE TABLE IF NOT EXISTS task_tag (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    family_id BIGINT NOT NULL COMMENT '家庭ID',
    name VARCHAR(50) NOT NULL COMMENT '标签名',
    color VARCHAR(20) DEFAULT NULL COMMENT '标签颜色',
    icon VARCHAR(50) DEFAULT NULL COMMENT '图标',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_family_name (family_id, name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务标签表';

CREATE TABLE IF NOT EXISTS task_tag_relation (
    task_id BIGINT NOT NULL COMMENT '任务ID',
    tag_id BIGINT NOT NULL COMMENT '标签ID',
    PRIMARY KEY (task_id, tag_id),
    INDEX idx_tag_id (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务标签关联表';

CREATE TABLE IF NOT EXISTS family_ledger (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    family_id BIGINT NOT NULL COMMENT '家庭ID',
    name VARCHAR(100) NOT NULL COMMENT '账本名',
    description VARCHAR(500) DEFAULT NULL COMMENT '描述',
    currency VARCHAR(10) DEFAULT 'CNY' COMMENT '货币',
    total_income DECIMAL(12,2) DEFAULT 0 COMMENT '总收入',
    total_expense DECIMAL(12,2) DEFAULT 0 COMMENT '总支出',
    balance DECIMAL(12,2) DEFAULT 0 COMMENT '余额',
    creator_id BIGINT DEFAULT NULL COMMENT '创建者',
    status TINYINT DEFAULT 1 COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_family_id (family_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家庭账本表';

CREATE TABLE IF NOT EXISTS ledger_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    family_id BIGINT NOT NULL COMMENT '家庭ID',
    name VARCHAR(100) NOT NULL COMMENT '分类名称',
    icon VARCHAR(50) DEFAULT NULL COMMENT '图标',
    color VARCHAR(20) DEFAULT NULL COMMENT '颜色',
    type TINYINT NOT NULL COMMENT '类型',
    sort_order INT DEFAULT 0 COMMENT '排序',
    parent_id BIGINT DEFAULT 0 COMMENT '父分类ID',
    is_default TINYINT DEFAULT 0 COMMENT '是否默认',
    status TINYINT DEFAULT 1 COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_family_type (family_id, type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账本分类表';

CREATE TABLE IF NOT EXISTS ledger_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ledger_id BIGINT NOT NULL COMMENT '账本ID',
    family_id BIGINT NOT NULL COMMENT '家庭ID',
    category_id BIGINT DEFAULT NULL COMMENT '分类ID',
    type TINYINT NOT NULL COMMENT '类型',
    amount DECIMAL(12,2) NOT NULL COMMENT '金额',
    description VARCHAR(500) DEFAULT NULL COMMENT '描述',
    record_date DATE NOT NULL COMMENT '记录日期',
    payer_id BIGINT DEFAULT NULL COMMENT '付款人',
    split_members VARCHAR(1000) DEFAULT NULL COMMENT '分摊成员',
    tags VARCHAR(500) DEFAULT NULL COMMENT '标签',
    attachment_url VARCHAR(500) DEFAULT NULL COMMENT '附件URL',
    location VARCHAR(200) DEFAULT NULL COMMENT '地点',
    status TINYINT DEFAULT 1 COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_ledger_date (ledger_id, record_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账本记录表';

CREATE TABLE IF NOT EXISTS ledger_budget (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ledger_id BIGINT NOT NULL COMMENT '账本ID',
    family_id BIGINT NOT NULL COMMENT '家庭ID',
    category_id BIGINT DEFAULT NULL COMMENT '分类ID',
    budget_month VARCHAR(7) NOT NULL COMMENT '预算月份',
    budget_amount DECIMAL(12,2) NOT NULL COMMENT '预算金额',
    used_amount DECIMAL(12,2) DEFAULT 0 COMMENT '已用金额',
    alert_threshold DECIMAL(5,2) DEFAULT 0.80 COMMENT '提醒阈值',
    alert_enabled TINYINT DEFAULT 1 COMMENT '是否提醒',
    status TINYINT DEFAULT 1 COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_budget_month (ledger_id, category_id, budget_month)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账本预算表';

CREATE TABLE IF NOT EXISTS ledger_share (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_id BIGINT NOT NULL COMMENT '账单记录ID',
    ledger_id BIGINT NOT NULL COMMENT '账本ID',
    family_id BIGINT NOT NULL COMMENT '家庭ID',
    member_id BIGINT NOT NULL COMMENT '成员ID',
    amount DECIMAL(12,2) NOT NULL COMMENT '金额',
    percentage DECIMAL(5,2) DEFAULT NULL COMMENT '比例',
    status TINYINT DEFAULT 1 COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_record_id (record_id),
    INDEX idx_member_id (member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账本分摊表';

CREATE TABLE IF NOT EXISTS health_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    family_id BIGINT DEFAULT NULL COMMENT '家庭ID',
    record_date DATE NOT NULL COMMENT '记录日期',
    weight DECIMAL(5,2) DEFAULT NULL COMMENT '体重',
    height DECIMAL(5,2) DEFAULT NULL COMMENT '身高',
    bmi DECIMAL(5,2) DEFAULT NULL COMMENT 'BMI',
    temperature DECIMAL(4,1) DEFAULT NULL COMMENT '体温',
    blood_pressure_systolic INT DEFAULT NULL COMMENT '收缩压',
    blood_pressure_diastolic INT DEFAULT NULL COMMENT '舒张压',
    heart_rate INT DEFAULT NULL COMMENT '心率',
    blood_sugar DECIMAL(6,2) DEFAULT NULL COMMENT '血糖',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    status TINYINT DEFAULT 1 COMMENT '状态',
    INDEX idx_user_date (user_id, record_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='健康记录表';

CREATE TABLE IF NOT EXISTS health_indicator (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    family_id BIGINT DEFAULT NULL COMMENT '家庭ID',
    indicator_type VARCHAR(50) NOT NULL COMMENT '指标类型',
    indicator_value DECIMAL(10,2) NOT NULL COMMENT '指标值',
    unit VARCHAR(20) DEFAULT NULL COMMENT '单位',
    record_date DATE NOT NULL COMMENT '记录日期',
    indicator_status VARCHAR(20) DEFAULT NULL COMMENT '状态',
    reference_min DECIMAL(10,2) DEFAULT NULL COMMENT '参考最小值',
    reference_max DECIMAL(10,2) DEFAULT NULL COMMENT '参考最大值',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    status TINYINT DEFAULT 1 COMMENT '状态',
    INDEX idx_user_type_date (user_id, indicator_type, record_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='健康指标表';

CREATE TABLE IF NOT EXISTS vote (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    family_id BIGINT NOT NULL COMMENT '家庭ID',
    creator_id BIGINT NOT NULL COMMENT '创建者',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    description TEXT DEFAULT NULL COMMENT '描述',
    vote_type ENUM('single', 'multiple', 'rating', 'rank', 'binary') DEFAULT 'single' COMMENT '投票类型',
    max_select INT DEFAULT 1 COMMENT '最大选择数',
    is_anonymous TINYINT DEFAULT 0 COMMENT '是否匿名',
    can_change TINYINT DEFAULT 0 COMMENT '是否允许改票',
    min_votes INT DEFAULT 1 COMMENT '最小有效票数',
    pass_threshold DECIMAL(3,2) DEFAULT 0.50 COMMENT '通过阈值',
    end_time DATETIME NOT NULL COMMENT '截止时间',
    status TINYINT DEFAULT 0 COMMENT '状态',
    options JSON DEFAULT NULL COMMENT '选项',
    result JSON DEFAULT NULL COMMENT '结果',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_family_id (family_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='投票表';

CREATE TABLE IF NOT EXISTS vote_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vote_id BIGINT NOT NULL COMMENT '投票ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    options JSON DEFAULT NULL COMMENT '投票项',
    vote_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '投票时间',
    UNIQUE KEY uk_vote_user (vote_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='投票记录表';

CREATE TABLE IF NOT EXISTS notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '接收者ID',
    type VARCHAR(50) DEFAULT NULL COMMENT '类型',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    content TEXT DEFAULT NULL COMMENT '内容',
    related_id BIGINT DEFAULT NULL COMMENT '关联ID',
    related_type VARCHAR(50) DEFAULT NULL COMMENT '关联类型',
    is_read TINYINT DEFAULT 0 COMMENT '是否已读',
    push_status TINYINT DEFAULT 0 COMMENT '推送状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_is_read (is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';

CREATE TABLE IF NOT EXISTS family_file (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    family_id BIGINT NOT NULL COMMENT '家庭ID',
    folder_id BIGINT DEFAULT 0 COMMENT '文件夹ID',
    name VARCHAR(200) NOT NULL COMMENT '文件名',
    original_name VARCHAR(200) DEFAULT NULL COMMENT '原始文件名',
    file_type VARCHAR(50) DEFAULT NULL COMMENT '文件类型',
    file_size BIGINT DEFAULT NULL COMMENT '文件大小',
    url VARCHAR(255) NOT NULL COMMENT '文件URL',
    uploader_id BIGINT DEFAULT NULL COMMENT '上传者',
    download_count INT DEFAULT 0 COMMENT '下载次数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_family_id (family_id),
    INDEX idx_folder_id (folder_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家庭文件表';

CREATE TABLE IF NOT EXISTS ai_chat_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    family_id BIGINT DEFAULT NULL COMMENT '家庭ID',
    session_id VARCHAR(64) DEFAULT NULL COMMENT '会话ID',
    role ENUM('user', 'assistant') DEFAULT NULL COMMENT '角色',
    content TEXT DEFAULT NULL COMMENT '内容',
    intent VARCHAR(50) DEFAULT NULL COMMENT '意图',
    entities JSON DEFAULT NULL COMMENT '实体',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_session (user_id, session_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI对话历史表';

CREATE TABLE IF NOT EXISTS ai_usage_stats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    date DATE NOT NULL COMMENT '日期',
    chat_count INT DEFAULT 0 COMMENT '对话次数',
    voice_count INT DEFAULT 0 COMMENT '语音次数',
    token_used INT DEFAULT 0 COMMENT 'Token使用量',
    UNIQUE KEY uk_user_date (user_id, date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI使用统计表';

CREATE TABLE IF NOT EXISTS stats_cache (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    stat_type VARCHAR(50) NOT NULL COMMENT '统计类型',
    target_id BIGINT NOT NULL COMMENT '目标ID',
    stat_date DATE NOT NULL COMMENT '统计日期',
    data JSON DEFAULT NULL COMMENT '缓存数据',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_type_target_date (stat_type, target_id, stat_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='统计缓存表';

CREATE TABLE IF NOT EXISTS user_activity (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    date DATE NOT NULL COMMENT '日期',
    login_count INT DEFAULT 0 COMMENT '登录次数',
    task_completed INT DEFAULT 0 COMMENT '任务完成数',
    diet_recorded INT DEFAULT 0 COMMENT '饮食记录数',
    active_minutes INT DEFAULT 0 COMMENT '活跃分钟数',
    UNIQUE KEY uk_user_date (user_id, date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户活跃度表';

CREATE TABLE IF NOT EXISTS moment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    family_id BIGINT NOT NULL COMMENT '家庭ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    content TEXT DEFAULT NULL COMMENT '内容',
    images JSON DEFAULT NULL COMMENT '图片',
    video VARCHAR(255) DEFAULT NULL COMMENT '视频',
    location VARCHAR(200) DEFAULT NULL COMMENT '地点',
    tags JSON DEFAULT NULL COMMENT '标签',
    mentions JSON DEFAULT NULL COMMENT '@用户',
    like_count INT DEFAULT 0 COMMENT '点赞数',
    comment_count INT DEFAULT 0 COMMENT '评论数',
    is_top TINYINT DEFAULT 0 COMMENT '是否置顶',
    status TINYINT DEFAULT 1 COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_family_time (family_id, create_time),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态表';

CREATE TABLE IF NOT EXISTS moment_like (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    moment_id BIGINT NOT NULL COMMENT '动态ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_moment_user (moment_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态点赞表';

CREATE TABLE IF NOT EXISTS moment_comment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    moment_id BIGINT NOT NULL COMMENT '动态ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    content TEXT NOT NULL COMMENT '内容',
    reply_to BIGINT DEFAULT NULL COMMENT '回复评论ID',
    reply_user_id BIGINT DEFAULT NULL COMMENT '回复用户ID',
    like_count INT DEFAULT 0 COMMENT '点赞数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_moment (moment_id),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态评论表';

CREATE TABLE IF NOT EXISTS shopping_list (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    family_id BIGINT NOT NULL COMMENT '家庭ID',
    name VARCHAR(100) DEFAULT NULL COMMENT '清单名称',
    type ENUM('auto', 'manual') DEFAULT 'manual' COMMENT '清单类型',
    status TINYINT DEFAULT 0 COMMENT '状态',
    total_amount DECIMAL(10,2) DEFAULT NULL COMMENT '总金额',
    creator_id BIGINT DEFAULT NULL COMMENT '创建者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    complete_time DATETIME DEFAULT NULL COMMENT '完成时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物清单表';

CREATE TABLE IF NOT EXISTS shopping_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    list_id BIGINT NOT NULL COMMENT '清单ID',
    name VARCHAR(100) NOT NULL COMMENT '商品名称',
    category VARCHAR(50) DEFAULT NULL COMMENT '分类',
    quantity DECIMAL(10,2) DEFAULT NULL COMMENT '数量',
    unit VARCHAR(20) DEFAULT NULL COMMENT '单位',
    estimated_price DECIMAL(10,2) DEFAULT NULL COMMENT '预计价格',
    actual_price DECIMAL(10,2) DEFAULT NULL COMMENT '实际价格',
    status TINYINT DEFAULT 0 COMMENT '状态',
    barcode VARCHAR(50) DEFAULT NULL COMMENT '条形码',
    assignee_id BIGINT DEFAULT NULL COMMENT '指派人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_list_id (list_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物项表';

CREATE TABLE IF NOT EXISTS inventory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    family_id BIGINT NOT NULL COMMENT '家庭ID',
    name VARCHAR(100) NOT NULL COMMENT '名称',
    category VARCHAR(50) DEFAULT NULL COMMENT '分类',
    quantity DECIMAL(10,2) DEFAULT NULL COMMENT '数量',
    unit VARCHAR(20) DEFAULT NULL COMMENT '单位',
    storage_location VARCHAR(50) DEFAULT NULL COMMENT '存放位置',
    purchase_date DATE DEFAULT NULL COMMENT '购买日期',
    expire_date DATE DEFAULT NULL COMMENT '过期日期',
    barcode VARCHAR(50) DEFAULT NULL COMMENT '条码',
    price DECIMAL(10,2) DEFAULT NULL COMMENT '价格',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存表';

CREATE TABLE IF NOT EXISTS wish_budget (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    wish_id BIGINT NOT NULL COMMENT '心愿ID',
    estimated_amount DECIMAL(10,2) DEFAULT NULL COMMENT '预算金额',
    actual_amount DECIMAL(10,2) DEFAULT NULL COMMENT '实际金额',
    currency VARCHAR(10) DEFAULT 'CNY' COMMENT '货币',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_wish_id (wish_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='心愿预算表';

CREATE TABLE IF NOT EXISTS wish_milestone (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    wish_id BIGINT NOT NULL COMMENT '心愿ID',
    title VARCHAR(100) NOT NULL COMMENT '标题',
    description TEXT DEFAULT NULL COMMENT '描述',
    target_date DATE DEFAULT NULL COMMENT '目标日期',
    is_completed TINYINT DEFAULT 0 COMMENT '是否完成',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_wish_id (wish_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='心愿里程碑表';

CREATE TABLE IF NOT EXISTS recipe_inventory_match (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    recipe_id BIGINT NOT NULL COMMENT '菜谱ID',
    inventory_name VARCHAR(100) NOT NULL COMMENT '库存名',
    required_quantity DECIMAL(10,2) DEFAULT NULL COMMENT '所需数量',
    unit VARCHAR(20) DEFAULT NULL COMMENT '单位',
    is_optional TINYINT DEFAULT 0 COMMENT '是否可选',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_recipe_id (recipe_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜谱库存匹配表';

CREATE TABLE IF NOT EXISTS user_weight (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    weight DECIMAL(5,2) NOT NULL COMMENT '体重',
    record_date DATE NOT NULL COMMENT '记录日期',
    note VARCHAR(200) DEFAULT NULL COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_date (user_id, record_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户体重表';

CREATE TABLE IF NOT EXISTS diet_nutrition_detail (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    diet_record_id BIGINT NOT NULL COMMENT '饮食记录ID',
    protein DECIMAL(10,2) DEFAULT NULL COMMENT '蛋白质',
    carbs DECIMAL(10,2) DEFAULT NULL COMMENT '碳水',
    fat DECIMAL(10,2) DEFAULT NULL COMMENT '脂肪',
    fiber DECIMAL(10,2) DEFAULT NULL COMMENT '纤维',
    sugar DECIMAL(10,2) DEFAULT NULL COMMENT '糖',
    sodium DECIMAL(10,2) DEFAULT NULL COMMENT '钠',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_diet_record_id (diet_record_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='营养详情表';

CREATE TABLE IF NOT EXISTS anniversary_reminder (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    anniversary_id BIGINT NOT NULL COMMENT '纪念日ID',
    reminder_days INT DEFAULT 7 COMMENT '提前天数',
    reminder_type ENUM('app', 'sms', 'email') DEFAULT 'app' COMMENT '提醒方式',
    is_enabled TINYINT DEFAULT 1 COMMENT '是否启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_anniversary_id (anniversary_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='纪念日提醒表';

CREATE TABLE IF NOT EXISTS price_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_name VARCHAR(100) NOT NULL COMMENT '商品名称',
    barcode VARCHAR(50) DEFAULT NULL COMMENT '条码',
    price DECIMAL(10,2) NOT NULL COMMENT '价格',
    store_name VARCHAR(100) DEFAULT NULL COMMENT '商店',
    purchase_date DATE DEFAULT NULL COMMENT '购买日期',
    family_id BIGINT DEFAULT NULL COMMENT '家庭ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_item_barcode (item_name, barcode)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='价格历史表';

CREATE TABLE IF NOT EXISTS coupon (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    family_id BIGINT NOT NULL COMMENT '家庭ID',
    title VARCHAR(100) NOT NULL COMMENT '标题',
    platform VARCHAR(50) DEFAULT NULL COMMENT '平台',
    coupon_code VARCHAR(100) DEFAULT NULL COMMENT '券码',
    discount_amount DECIMAL(10,2) DEFAULT NULL COMMENT '优惠金额',
    min_amount DECIMAL(10,2) DEFAULT NULL COMMENT '最低消费',
    expire_date DATE DEFAULT NULL COMMENT '过期时间',
    status TINYINT DEFAULT 0 COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券表';

CREATE TABLE IF NOT EXISTS points_coupon (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    family_id BIGINT NOT NULL COMMENT '家庭ID',
    title VARCHAR(100) NOT NULL COMMENT '标题',
    description TEXT DEFAULT NULL COMMENT '描述',
    points_cost INT NOT NULL COMMENT '所需积分',
    coupon_type ENUM('privilege', 'gift', 'task') DEFAULT 'privilege' COMMENT '券类型',
    status TINYINT DEFAULT 1 COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分兑换券表';

CREATE TABLE IF NOT EXISTS points_coupon_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    coupon_id BIGINT NOT NULL COMMENT '券ID',
    points_spent INT NOT NULL COMMENT '消耗积分',
    status TINYINT DEFAULT 0 COMMENT '状态',
    expire_date DATE DEFAULT NULL COMMENT '过期日期',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_coupon_id (coupon_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分兑换记录表';

CREATE TABLE IF NOT EXISTS family_report (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    family_id BIGINT NOT NULL COMMENT '家庭ID',
    report_type ENUM('weekly', 'monthly', 'yearly') NOT NULL COMMENT '报告类型',
    report_date DATE NOT NULL COMMENT '报告日期',
    title VARCHAR(100) NOT NULL COMMENT '标题',
    content JSON DEFAULT NULL COMMENT '内容',
    score INT DEFAULT NULL COMMENT '评分',
    is_read TINYINT DEFAULT 0 COMMENT '是否已读',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_family_id (family_id),
    INDEX idx_report_date (report_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家庭报告表';

CREATE TABLE IF NOT EXISTS game_wheel (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    family_id BIGINT NOT NULL COMMENT '家庭ID',
    name VARCHAR(100) NOT NULL COMMENT '名称',
    items JSON NOT NULL COMMENT '转盘项',
    creator_id BIGINT DEFAULT NULL COMMENT '创建者',
    use_count INT DEFAULT 0 COMMENT '使用次数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='转盘表';

CREATE TABLE IF NOT EXISTS family_challenge (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    family_id BIGINT NOT NULL COMMENT '家庭ID',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    description VARCHAR(1000) DEFAULT NULL COMMENT '描述',
    type INT DEFAULT 1 COMMENT '类型',
    target_type VARCHAR(50) DEFAULT NULL COMMENT '目标类型',
    target_value DECIMAL(10,2) DEFAULT NULL COMMENT '目标值',
    start_date DATE DEFAULT NULL COMMENT '开始日期',
    end_date DATE DEFAULT NULL COMMENT '结束日期',
    creator_id BIGINT DEFAULT NULL COMMENT '创建者ID',
    participant_count INT DEFAULT 0 COMMENT '参与人数',
    status TINYINT DEFAULT 1 COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    KEY idx_family_id (family_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家庭挑战表';

CREATE TABLE IF NOT EXISTS challenge_participant (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    challenge_id BIGINT NOT NULL COMMENT '挑战ID',
    family_id BIGINT NOT NULL COMMENT '家庭ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    current_value DECIMAL(10,2) DEFAULT 0 COMMENT '当前值',
    progress INT DEFAULT 0 COMMENT '进度',
    checkin_count INT DEFAULT 0 COMMENT '打卡次数',
    last_checkin_date DATE DEFAULT NULL COMMENT '最后打卡日期',
    is_completed TINYINT DEFAULT 0 COMMENT '是否完成',
    status TINYINT DEFAULT 1 COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_challenge_user (challenge_id, user_id),
    KEY idx_challenge_id (challenge_id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='挑战参与者表';

CREATE TABLE IF NOT EXISTS challenge_checkin (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    challenge_id BIGINT NOT NULL COMMENT '挑战ID',
    participant_id BIGINT NOT NULL COMMENT '参与者ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    checkin_date DATE DEFAULT NULL COMMENT '打卡日期',
    note VARCHAR(500) DEFAULT NULL COMMENT '备注',
    image VARCHAR(500) DEFAULT NULL COMMENT '图片',
    value DECIMAL(10,2) DEFAULT NULL COMMENT '打卡值',
    status TINYINT DEFAULT 1 COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    KEY idx_challenge_id (challenge_id),
    KEY idx_user_id (user_id),
    KEY idx_checkin_date (checkin_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='挑战打卡表';

CREATE TABLE IF NOT EXISTS mind_match_question (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    question VARCHAR(500) NOT NULL COMMENT '问题内容',
    type VARCHAR(20) NOT NULL COMMENT '题目类型',
    target_user_id BIGINT DEFAULT NULL COMMENT '目标用户ID',
    target_answer VARCHAR(500) DEFAULT NULL COMMENT '期望答案',
    difficulty INT DEFAULT 1 COMMENT '难度',
    status INT DEFAULT 1 COMMENT '状态',
    play_count INT DEFAULT 0 COMMENT '使用次数',
    correct_count INT DEFAULT 0 COMMENT '答对次数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted INT DEFAULT 0 COMMENT '逻辑删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='默契问答题库';

CREATE TABLE IF NOT EXISTS mind_match_session (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    family_id BIGINT NOT NULL COMMENT '家庭ID',
    round INT DEFAULT 1 COMMENT '轮次',
    question_id BIGINT DEFAULT NULL COMMENT '问题ID',
    player1_id BIGINT NOT NULL COMMENT '玩家1ID',
    player1_answer VARCHAR(500) DEFAULT NULL COMMENT '玩家1答案',
    player1_answered TINYINT(1) DEFAULT 0 COMMENT '玩家1是否已答',
    player2_id BIGINT NOT NULL COMMENT '玩家2ID',
    player2_answer VARCHAR(500) DEFAULT NULL COMMENT '玩家2答案',
    player2_answered TINYINT(1) DEFAULT 0 COMMENT '玩家2是否已答',
    is_match TINYINT(1) DEFAULT NULL COMMENT '是否匹配',
    round_points INT DEFAULT 0 COMMENT '本轮得分',
    status VARCHAR(20) DEFAULT 'waiting' COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted INT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_family_id (family_id),
    INDEX idx_player1_id (player1_id),
    INDEX idx_player2_id (player2_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='默契问答会话';

CREATE TABLE IF NOT EXISTS sys_operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT DEFAULT NULL COMMENT '用户ID',
    username VARCHAR(64) DEFAULT NULL COMMENT '用户名',
    module VARCHAR(100) DEFAULT NULL COMMENT '模块',
    operation VARCHAR(100) DEFAULT NULL COMMENT '操作',
    method VARCHAR(20) DEFAULT NULL COMMENT '请求方法',
    request_url VARCHAR(500) DEFAULT NULL COMMENT '请求地址',
    request_params LONGTEXT DEFAULT NULL COMMENT '请求参数',
    response_data LONGTEXT DEFAULT NULL COMMENT '响应数据',
    execution_time BIGINT DEFAULT NULL COMMENT '执行耗时',
    ip VARCHAR(64) DEFAULT NULL COMMENT 'IP',
    status TINYINT DEFAULT 1 COMMENT '状态',
    error_msg VARCHAR(1000) DEFAULT NULL COMMENT '错误信息',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统操作日志表';

INSERT INTO sys_config (config_key, config_value, description, category, create_time, update_time)
SELECT 'wechat.work.corpid', config_value, COALESCE(description, '企业微信CorpID'), 'wechat_work', NOW(), NOW()
FROM system_config
WHERE config_key = 'wechat_work_corpid'
  AND NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'wechat.work.corpid');

INSERT INTO sys_config (config_key, config_value, description, category, create_time, update_time)
SELECT 'wechat.work.agentid', config_value, COALESCE(description, '企业微信AgentID'), 'wechat_work', NOW(), NOW()
FROM system_config
WHERE config_key = 'wechat_work_agentid'
  AND NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'wechat.work.agentid');

INSERT INTO sys_config (config_key, config_value, description, category, create_time, update_time)
SELECT 'wechat.work.secret', config_value, COALESCE(description, '企业微信Secret'), 'wechat_work', NOW(), NOW()
FROM system_config
WHERE config_key = 'wechat_work_secret'
  AND NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'wechat.work.secret');

INSERT INTO sys_config (config_key, config_value, description, category, create_time, update_time)
SELECT 'wechat.work.userid', config_value, COALESCE(description, '企业微信成员ID'), 'wechat_work', NOW(), NOW()
FROM system_config
WHERE config_key = 'wechat_work_userid'
  AND NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'wechat.work.userid');

INSERT INTO sys_config (config_key, config_value, description, category, create_time, update_time)
SELECT 'wechat.work.token', config_value, COALESCE(description, '企业微信Token'), 'wechat_work', NOW(), NOW()
FROM system_config
WHERE config_key = 'wechat_work_token'
  AND NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'wechat.work.token');

INSERT INTO sys_config (config_key, config_value, description, category, create_time, update_time)
SELECT 'wechat.work.aeskey', config_value, COALESCE(description, '企业微信EncodingAESKey'), 'wechat_work', NOW(), NOW()
FROM system_config
WHERE config_key = 'wechat_work_aeskey'
  AND NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'wechat.work.aeskey');

INSERT INTO sys_config (config_key, config_value, description, category, create_time, update_time)
SELECT 'wechat.miniapp.appid', config_value, COALESCE(description, '微信小程序AppID'), 'wechat_miniapp', NOW(), NOW()
FROM system_config
WHERE config_key = 'weixin_appid'
  AND NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'wechat.miniapp.appid');

INSERT INTO sys_config (config_key, config_value, description, category, create_time, update_time)
SELECT 'wechat.miniapp.secret', config_value, COALESCE(description, '微信小程序Secret'), 'wechat_miniapp', NOW(), NOW()
FROM system_config
WHERE config_key = 'weixin_appsecret'
  AND NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'wechat.miniapp.secret');

INSERT INTO sys_config (config_key, config_value, description, category, create_time, update_time)
SELECT 'tencent.map.key', config_value, COALESCE(description, '腾讯地图Key'), 'map', NOW(), NOW()
FROM system_config
WHERE config_key = 'tencent_map_key'
  AND NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'tencent.map.key');

INSERT INTO sys_config (config_key, config_value, description, category, create_time, update_time)
SELECT 'ssl.keystore.password', config_value, COALESCE(description, 'SSL证书密码'), 'general', NOW(), NOW()
FROM system_config
WHERE config_key = 'ssl_keystore_password'
  AND NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'ssl.keystore.password');
