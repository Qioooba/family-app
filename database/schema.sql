-- 家庭小程序数据库脚本
-- 数据库: family_app

-- 创建数据库
CREATE DATABASE IF NOT EXISTS family_app CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE family_app;

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    nickname VARCHAR(50) COMMENT '昵称',
    avatar VARCHAR(255) COMMENT '头像URL',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    gender TINYINT DEFAULT 0 COMMENT '性别 0未知 1男 2女',
    birthday DATE COMMENT '生日',
    height DECIMAL(5,2) COMMENT '身高cm',
    weight DECIMAL(5,2) COMMENT '体重kg',
    target_weight DECIMAL(5,2) COMMENT '目标体重',
    daily_calories INT COMMENT '每日热量目标',
    open_id VARCHAR(100) COMMENT '微信openId',
    union_id VARCHAR(100) COMMENT '微信unionId',
    current_family_id BIGINT COMMENT '当前家庭ID',
    login_type TINYINT DEFAULT 1 COMMENT '登录类型 1账号 2微信 3手机号',
    last_login_time DATETIME COMMENT '最后登录时间',
    last_login_ip VARCHAR(50) COMMENT '最后登录IP',
    status TINYINT DEFAULT 1 COMMENT '状态 0禁用 1正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 家庭表
CREATE TABLE IF NOT EXISTS family (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '家庭名称',
    avatar VARCHAR(255) COMMENT '家庭头像',
    creator_id BIGINT NOT NULL COMMENT '创建者ID',
    invite_code VARCHAR(20) COMMENT '邀请码',
    member_count INT DEFAULT 1 COMMENT '成员数',
    storage_used BIGINT DEFAULT 0 COMMENT '已用存储空间字节',
    storage_limit BIGINT DEFAULT 10737418240 COMMENT '存储限制 默认10G',
    status TINYINT DEFAULT 1 COMMENT '状态 0解散 1正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家庭表';

-- 家庭成员表
CREATE TABLE IF NOT EXISTS family_member (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    family_id BIGINT NOT NULL COMMENT '家庭ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role ENUM('owner', 'admin', 'member') DEFAULT 'member' COMMENT '角色 owner-家主 admin-管理员 member-成员',
    nickname VARCHAR(50) COMMENT '家庭内昵称',
    join_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    UNIQUE KEY uk_family_user (family_id, user_id),
    INDEX idx_family_id (family_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家庭成员表';

-- 任务/清单表
CREATE TABLE IF NOT EXISTS task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    family_id BIGINT NOT NULL COMMENT '家庭ID',
    category_id BIGINT COMMENT '分类ID',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    content TEXT COMMENT '内容描述',
    priority TINYINT DEFAULT 0 COMMENT '优先级 0普通 1重要 2紧急',
    status TINYINT DEFAULT 0 COMMENT '状态 0待办 1进行中 2已完成 3已取消',
    assignee_id BIGINT COMMENT '指派给',
    creator_id BIGINT NOT NULL COMMENT '创建者',
    due_time DATETIME COMMENT '截止时间',
    remind_time DATETIME COMMENT '提醒时间',
    repeat_type ENUM('none', 'daily', 'weekly', 'monthly', 'yearly', 'custom') DEFAULT 'none' COMMENT '重复类型',
    repeat_rule VARCHAR(200) COMMENT '重复规则JSON',
    location VARCHAR(200) COMMENT '地点',
    parent_id BIGINT DEFAULT 0 COMMENT '父任务ID 0为顶级任务',
    attachments JSON COMMENT '附件列表',
    finish_time DATETIME COMMENT '完成时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_family_id (family_id),
    INDEX idx_assignee (assignee_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务清单表';

-- 任务分类表
CREATE TABLE IF NOT EXISTS task_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    family_id BIGINT NOT NULL COMMENT '家庭ID',
    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    icon VARCHAR(50) COMMENT '图标',
    color VARCHAR(20) COMMENT '颜色',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务分类表';

-- 心愿表
CREATE TABLE IF NOT EXISTS wish (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    family_id BIGINT NOT NULL COMMENT '家庭ID',
    user_id BIGINT NOT NULL COMMENT '创建者ID',
    type ENUM('item', 'experience', 'learn', 'relation', 'charity', 'goal') NOT NULL COMMENT '类型',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    description TEXT COMMENT '描述',
    budget_min DECIMAL(10,2) COMMENT '预算下限',
    budget_max DECIMAL(10,2) COMMENT '预算上限',
    expect_date DATE COMMENT '期望实现日期',
    visibility ENUM('public', 'family', 'couple', 'private') DEFAULT 'public' COMMENT '可见性',
    priority TINYINT DEFAULT 3 COMMENT '优先级 1-5',
    difficulty TINYINT DEFAULT 2 COMMENT '难度 1-3',
    status TINYINT DEFAULT 0 COMMENT '状态 0待实现 1进行中 2已实现 3已放弃',
    claimant_id BIGINT COMMENT '认领者ID',
    progress INT DEFAULT 0 COMMENT '进度0-100',
    images JSON COMMENT '图片列表',
    finish_time DATETIME COMMENT '完成时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_family_id (family_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='心愿表';

-- 菜谱表
CREATE TABLE IF NOT EXISTS recipe (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    family_id BIGINT COMMENT '家庭ID NULL为系统菜谱',
    name VARCHAR(100) NOT NULL COMMENT '菜名',
    description TEXT COMMENT '简介',
    category VARCHAR(50) COMMENT '分类',
    cuisine VARCHAR(50) COMMENT '菜系',
    difficulty TINYINT DEFAULT 2 COMMENT '难度 1简单 2中等 3困难',
    time INT COMMENT '烹饪时间分钟',
    servings INT DEFAULT 2 COMMENT '份量',
    cover_image VARCHAR(255) COMMENT '封面图',
    images JSON COMMENT '步骤图列表',
    ingredients JSON COMMENT '食材清单',
    steps JSON COMMENT '步骤详情',
    nutrition JSON COMMENT '营养成分',
    tags JSON COMMENT '标签',
    source ENUM('system', 'custom', 'ai') DEFAULT 'custom' COMMENT '来源',
    creator_id BIGINT COMMENT '创建者',
    favorite_count INT DEFAULT 0 COMMENT '收藏数',
    make_count INT DEFAULT 0 COMMENT '做过次数',
    rating DECIMAL(2,1) DEFAULT 5.0 COMMENT '评分',
    status TINYINT DEFAULT 1 COMMENT '状态 0禁用 1正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_family_id (family_id),
    INDEX idx_category (category),
    FULLTEXT INDEX ft_name (name) WITH PARSER ngram
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜谱表';

-- 食材表
CREATE TABLE IF NOT EXISTS ingredient (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    family_id BIGINT NOT NULL COMMENT '家庭ID',
    name VARCHAR(100) NOT NULL COMMENT '食材名称',
    category VARCHAR(50) COMMENT '分类',
    quantity DECIMAL(10,2) COMMENT '数量',
    unit VARCHAR(20) COMMENT '单位',
    storage_location VARCHAR(50) COMMENT '存放位置',
    purchase_date DATE COMMENT '购买日期',
    expire_date DATE COMMENT '过期日期',
    reminder_days INT DEFAULT 3 COMMENT '提前提醒天数',
    status TINYINT DEFAULT 1 COMMENT '状态 1正常 2快过期 3已过期',
    image VARCHAR(255) COMMENT '图片',
    recognized_data JSON COMMENT 'AI识别原始数据',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_family_id (family_id),
    INDEX idx_expire (expire_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='食材表';

-- 饮食记录表
CREATE TABLE IF NOT EXISTS diet_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    family_id BIGINT NOT NULL COMMENT '家庭ID',
    meal_type ENUM('breakfast', 'lunch', 'dinner', 'snack') NOT NULL COMMENT '餐别',
    record_date DATE NOT NULL COMMENT '记录日期',
    record_time TIME COMMENT '记录时间',
    food_name VARCHAR(200) NOT NULL COMMENT '食物名称',
    quantity DECIMAL(10,2) COMMENT '数量',
    unit VARCHAR(20) COMMENT '单位',
    calories INT COMMENT '热量卡路里',
    protein DECIMAL(6,2) COMMENT '蛋白质g',
    carbs DECIMAL(6,2) COMMENT '碳水g',
    fat DECIMAL(6,2) COMMENT '脂肪g',
    fiber DECIMAL(6,2) COMMENT '纤维g',
    image VARCHAR(255) COMMENT '图片',
    recognition_source ENUM('manual', 'photo', 'voice', 'barcode') DEFAULT 'manual' COMMENT '录入方式',
    ai_analysis JSON COMMENT 'AI分析结果',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_date (user_id, record_date),
    INDEX idx_family_id (family_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='饮食记录表';

-- 纪念日表
CREATE TABLE IF NOT EXISTS anniversary (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    family_id BIGINT NOT NULL COMMENT '家庭ID',
    title VARCHAR(100) NOT NULL COMMENT '标题',
    type ENUM('birthday', 'love', 'wedding', 'family', 'traditional', 'custom') NOT NULL,
    date_type ENUM('solar', 'lunar') DEFAULT 'solar' COMMENT '阳历/农历',
    target_date DATE NOT NULL COMMENT '目标日期',
    is_recurring TINYINT DEFAULT 1 COMMENT '是否每年重复',
    reminder_days JSON COMMENT '提前提醒天数 [1,3,7]',
    related_user_id BIGINT COMMENT '关联用户ID',
    description TEXT COMMENT '描述',
    images JSON COMMENT '纪念相册',
    icon VARCHAR(20) COMMENT '图标',
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_family_id (family_id),
    INDEX idx_target_date (target_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='纪念日表';

-- 投票表
CREATE TABLE IF NOT EXISTS vote (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    family_id BIGINT NOT NULL COMMENT '家庭ID',
    creator_id BIGINT NOT NULL COMMENT '创建者',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    description TEXT COMMENT '描述',
    vote_type ENUM('single', 'multiple', 'rating', 'rank', 'binary') DEFAULT 'single',
    max_select INT DEFAULT 1 COMMENT '最多选几项',
    is_anonymous TINYINT DEFAULT 0 COMMENT '是否匿名',
    can_change TINYINT DEFAULT 0 COMMENT '是否可改票',
    min_votes INT DEFAULT 1 COMMENT '最低有效票数',
    pass_threshold DECIMAL(3,2) DEFAULT 0.50 COMMENT '通过阈值',
    end_time DATETIME NOT NULL COMMENT '截止时间',
    status TINYINT DEFAULT 0 COMMENT '状态 0进行中 1已结束',
    options JSON COMMENT '选项列表',
    result JSON COMMENT '结果',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_family_id (family_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='投票表';

-- 投票记录表
CREATE TABLE IF NOT EXISTS vote_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    vote_id BIGINT NOT NULL COMMENT '投票ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    options JSON COMMENT '选择的选项',
    vote_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_vote_user (vote_id, user_id),
    INDEX idx_vote_id (vote_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='投票记录表';

-- 消息通知表
CREATE TABLE IF NOT EXISTS notification (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '接收者ID',
    type VARCHAR(50) COMMENT '类型 task/wish/system等',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    content TEXT COMMENT '内容',
    related_id BIGINT COMMENT '关联业务ID',
    related_type VARCHAR(50) COMMENT '关联业务类型',
    is_read TINYINT DEFAULT 0 COMMENT '是否已读',
    push_status TINYINT DEFAULT 0 COMMENT '推送状态 0未推送 1已推送 2推送失败',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_is_read (is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息通知表';

-- 家庭相册表
CREATE TABLE IF NOT EXISTS family_album (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    family_id BIGINT NOT NULL COMMENT '家庭ID',
    name VARCHAR(100) NOT NULL COMMENT '相册名称',
    cover VARCHAR(255) COMMENT '封面图',
    photo_count INT DEFAULT 0 COMMENT '照片数量',
    creator_id BIGINT COMMENT '创建者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_family_id (family_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家庭相册表';

-- 照片表
CREATE TABLE IF NOT EXISTS photo (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    album_id BIGINT NOT NULL COMMENT '相册ID',
    family_id BIGINT NOT NULL COMMENT '家庭ID',
    url VARCHAR(255) NOT NULL COMMENT '图片URL',
    thumbnail VARCHAR(255) COMMENT '缩略图URL',
    description TEXT COMMENT '描述',
    taken_time DATETIME COMMENT '拍摄时间',
    location VARCHAR(200) COMMENT '拍摄地点',
    tags JSON COMMENT '标签',
    uploader_id BIGINT COMMENT '上传者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_album_id (album_id),
    INDEX idx_family_id (family_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='照片表';

-- 文件表
CREATE TABLE IF NOT EXISTS family_file (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    family_id BIGINT NOT NULL COMMENT '家庭ID',
    folder_id BIGINT DEFAULT 0 COMMENT '文件夹ID 0为根目录',
    name VARCHAR(200) NOT NULL COMMENT '文件名',
    original_name VARCHAR(200) COMMENT '原始文件名',
    file_type VARCHAR(50) COMMENT '文件类型',
    file_size BIGINT COMMENT '文件大小字节',
    url VARCHAR(255) NOT NULL COMMENT '文件URL',
    uploader_id BIGINT COMMENT '上传者',
    download_count INT DEFAULT 0 COMMENT '下载次数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_family_id (family_id),
    INDEX idx_folder_id (folder_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件表';

-- 初始化数据
INSERT INTO task_category (family_id, name, icon, color, sort_order) VALUES
(0, '购物清单', 'shopping-cart', '#FF6B6B', 1),
(0, '待办事项', 'file-text', '#4ECDC4', 2),
(0, '家务分工', 'home', '#45B7D1', 3),
(0, '财务提醒', 'account-book', '#96CEB4', 4),
(0, '育儿相关', 'smile', '#FFEAA7', 5),
(0, '健康医疗', 'medicine-box', '#DDA0DD', 6);

-- ============================================
-- AI助手功能表
-- ============================================

-- AI对话历史表
CREATE TABLE IF NOT EXISTS ai_chat_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    family_id BIGINT COMMENT '家庭ID',
    session_id VARCHAR(64) COMMENT '会话ID',
    role ENUM('user', 'assistant') COMMENT '角色',
    content TEXT COMMENT '对话内容',
    intent VARCHAR(50) COMMENT '意图识别',
    entities JSON COMMENT '实体提取',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_session (user_id, session_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI对话历史表';

-- AI使用统计表
CREATE TABLE IF NOT EXISTS ai_usage_stats (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    date DATE NOT NULL COMMENT '日期',
    chat_count INT DEFAULT 0 COMMENT '对话次数',
    voice_count INT DEFAULT 0 COMMENT '语音次数',
    token_used INT DEFAULT 0 COMMENT 'Token使用量',
    UNIQUE KEY uk_user_date (user_id, date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI使用统计表';

-- ============================================
-- 数据看板功能表
-- ============================================

-- 统计缓存表
CREATE TABLE IF NOT EXISTS stats_cache (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    stat_type VARCHAR(50) NOT NULL COMMENT '统计类型',
    target_id BIGINT NOT NULL COMMENT '目标ID（用户ID或家庭ID）',
    stat_date DATE NOT NULL COMMENT '统计日期',
    data JSON COMMENT '统计结果JSON',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_type_target_date (stat_type, target_id, stat_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='统计缓存表';

-- 用户活跃度表
CREATE TABLE IF NOT EXISTS user_activity (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    date DATE NOT NULL COMMENT '日期',
    login_count INT DEFAULT 0 COMMENT '登录次数',
    task_completed INT DEFAULT 0 COMMENT '完成任务数',
    diet_recorded INT DEFAULT 0 COMMENT '饮食记录数',
    active_minutes INT DEFAULT 0 COMMENT '活跃分钟数',
    UNIQUE KEY uk_user_date (user_id, date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户活跃度表';

-- 菜谱记录表（补充）
CREATE TABLE IF NOT EXISTS recipe_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    family_id BIGINT COMMENT '家庭ID',
    recipe_id BIGINT COMMENT '菜谱ID',
    recipe_name VARCHAR(100) COMMENT '菜谱名称',
    image_url VARCHAR(255) COMMENT '图片URL',
    cook_time DATETIME COMMENT '烹饪时间',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_family_id (family_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜谱记录表';

-- ============================================
-- 家庭圈功能表
-- ============================================

-- 动态表
CREATE TABLE IF NOT EXISTS moment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    family_id BIGINT NOT NULL COMMENT '家庭ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    content TEXT COMMENT '文字内容',
    images JSON COMMENT '图片列表',
    video VARCHAR(255) COMMENT '视频URL',
    location VARCHAR(200) COMMENT '位置',
    tags JSON COMMENT '标签',
    mentions JSON COMMENT '@的用户ID列表',
    like_count INT DEFAULT 0 COMMENT '点赞数',
    comment_count INT DEFAULT 0 COMMENT '评论数',
    is_top TINYINT DEFAULT 0 COMMENT '是否置顶',
    status TINYINT DEFAULT 1 COMMENT '0删除 1正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_family_time (family_id, create_time),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态表';

-- 点赞表
CREATE TABLE IF NOT EXISTS moment_like (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    moment_id BIGINT NOT NULL COMMENT '动态ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_moment_user (moment_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态点赞表';

-- 评论表
CREATE TABLE IF NOT EXISTS moment_comment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    moment_id BIGINT NOT NULL COMMENT '动态ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    content TEXT NOT NULL COMMENT '评论内容',
    reply_to BIGINT COMMENT '回复的评论ID',
    reply_user_id BIGINT COMMENT '回复的用户ID',
    like_count INT DEFAULT 0 COMMENT '点赞数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_moment (moment_id),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态评论表';

-- ============================================
-- 智能购物功能表
-- ============================================

-- 购物清单表
CREATE TABLE IF NOT EXISTS shopping_list (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    family_id BIGINT NOT NULL COMMENT '家庭ID',
    name VARCHAR(100) COMMENT '清单名称',
    type ENUM('auto', 'manual') DEFAULT 'manual' COMMENT '类型',
    status TINYINT DEFAULT 0 COMMENT '0进行中 1已完成',
    total_amount DECIMAL(10,2) COMMENT '总金额',
    creator_id BIGINT COMMENT '创建者ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    complete_time DATETIME COMMENT '完成时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物清单表';

-- 购物清单项表
CREATE TABLE IF NOT EXISTS shopping_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    list_id BIGINT NOT NULL COMMENT '清单ID',
    name VARCHAR(100) NOT NULL COMMENT '商品名称',
    category VARCHAR(50) COMMENT '分类',
    quantity DECIMAL(10,2) COMMENT '数量',
    unit VARCHAR(20) COMMENT '单位',
    estimated_price DECIMAL(10,2) COMMENT '预计价格',
    actual_price DECIMAL(10,2) COMMENT '实际价格',
    status TINYINT DEFAULT 0 COMMENT '0未买 1已买',
    barcode VARCHAR(50) COMMENT '条形码',
    assignee_id BIGINT COMMENT '指派给谁',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物清单项表';

-- 库存表
CREATE TABLE IF NOT EXISTS inventory (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    family_id BIGINT NOT NULL COMMENT '家庭ID',
    name VARCHAR(100) NOT NULL COMMENT '商品名称',
    category VARCHAR(50) COMMENT '分类',
    quantity DECIMAL(10,2) COMMENT '数量',
    unit VARCHAR(20) COMMENT '单位',
    storage_location VARCHAR(50) COMMENT '存放位置',
    purchase_date DATE COMMENT '购买日期',
    expire_date DATE COMMENT '过期日期',
    barcode VARCHAR(50) COMMENT '条形码',
    price DECIMAL(10,2) COMMENT '价格',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存表';

-- ============================================
-- 家庭游戏功能表
-- ============================================

-- 转盘表
CREATE TABLE IF NOT EXISTS game_wheel (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    family_id BIGINT NOT NULL COMMENT '家庭ID',
    name VARCHAR(100) NOT NULL COMMENT '转盘名称',
    items JSON NOT NULL COMMENT '转盘项 [{name, probability, color, icon}]',
    creator_id BIGINT COMMENT '创建者ID',
    use_count INT DEFAULT 0 COMMENT '使用次数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='转盘表';

-- 用户积分表
CREATE TABLE IF NOT EXISTS user_points (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE COMMENT '用户ID',
    total_points INT DEFAULT 0 COMMENT '总积分',
    available_points INT DEFAULT 0 COMMENT '可用积分',
    spent_points INT DEFAULT 0 COMMENT '已消费积分',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户积分表';

-- 积分流水表
CREATE TABLE IF NOT EXISTS points_transaction (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    type ENUM('earn', 'spend', 'transfer_in', 'transfer_out') NOT NULL COMMENT '类型',
    points INT NOT NULL COMMENT '积分变动',
    balance INT NOT NULL COMMENT '变动后余额',
    reason VARCHAR(200) COMMENT '变动原因',
    related_id BIGINT COMMENT '关联业务ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_time (user_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分流水表';

-- ============================================
-- 拓展功能表
-- ============================================

-- 任务提醒表
CREATE TABLE IF NOT EXISTS task_reminder (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id BIGINT NOT NULL COMMENT '任务ID',
    reminder_type ENUM('time', 'location') DEFAULT 'time' COMMENT '提醒类型',
    reminder_time DATETIME COMMENT '提醒时间',
    location_name VARCHAR(200) COMMENT '位置名称',
    location_lat DECIMAL(10,7) COMMENT '纬度',
    location_lng DECIMAL(10,7) COMMENT '经度',
    radius INT DEFAULT 500 COMMENT '地理围栏半径(米)',
    is_triggered TINYINT DEFAULT 0 COMMENT '是否已触发',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务提醒表';

-- 子任务表
CREATE TABLE IF NOT EXISTS task_subtask (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id BIGINT NOT NULL COMMENT '父任务ID',
    title VARCHAR(100) NOT NULL COMMENT '子任务标题',
    status TINYINT DEFAULT 0 COMMENT '0未完成 1已完成',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='子任务表';

-- 家务排班表
CREATE TABLE IF NOT EXISTS task_schedule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    family_id BIGINT NOT NULL COMMENT '家庭ID',
    task_name VARCHAR(100) NOT NULL COMMENT '任务名称',
    assignee_id BIGINT NOT NULL COMMENT '指派用户ID',
    schedule_type ENUM('daily', 'weekly', 'monthly') DEFAULT 'weekly' COMMENT '排班类型',
    schedule_day INT COMMENT '周几/几号',
    status TINYINT DEFAULT 1 COMMENT '0停用 1启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家务排班表';

-- 心愿预算表
CREATE TABLE IF NOT EXISTS wish_budget (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    wish_id BIGINT NOT NULL COMMENT '心愿ID',
    estimated_amount DECIMAL(10,2) COMMENT '预估金额',
    actual_amount DECIMAL(10,2) COMMENT '实际金额',
    currency VARCHAR(10) DEFAULT 'CNY' COMMENT '货币',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='心愿预算表';

-- 心愿里程碑表
CREATE TABLE IF NOT EXISTS wish_milestone (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    wish_id BIGINT NOT NULL COMMENT '心愿ID',
    title VARCHAR(100) NOT NULL COMMENT '里程碑标题',
    description TEXT COMMENT '描述',
    target_date DATE COMMENT '目标日期',
    is_completed TINYINT DEFAULT 0 COMMENT '是否完成',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='心愿里程碑表';

-- 菜谱库存匹配表
CREATE TABLE IF NOT EXISTS recipe_inventory_match (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    recipe_id BIGINT NOT NULL COMMENT '菜谱ID',
    inventory_name VARCHAR(100) NOT NULL COMMENT '所需食材名称',
    required_quantity DECIMAL(10,2) COMMENT '所需数量',
    unit VARCHAR(20) COMMENT '单位',
    is_optional TINYINT DEFAULT 0 COMMENT '是否可选',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜谱库存匹配表';

-- 体重记录表
CREATE TABLE IF NOT EXISTS user_weight (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    weight DECIMAL(5,2) NOT NULL COMMENT '体重(kg)',
    record_date DATE NOT NULL COMMENT '记录日期',
    note VARCHAR(200) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_date (user_id, record_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='体重记录表';

-- 详细营养记录表
CREATE TABLE IF NOT EXISTS diet_nutrition_detail (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    diet_record_id BIGINT NOT NULL COMMENT '饮食记录ID',
    protein DECIMAL(10,2) COMMENT '蛋白质(g)',
    carbs DECIMAL(10,2) COMMENT '碳水化合物(g)',
    fat DECIMAL(10,2) COMMENT '脂肪(g)',
    fiber DECIMAL(10,2) COMMENT '膳食纤维(g)',
    sugar DECIMAL(10,2) COMMENT '糖分(g)',
    sodium DECIMAL(10,2) COMMENT '钠(mg)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='详细营养记录表';

-- 纪念日提醒设置表
CREATE TABLE IF NOT EXISTS anniversary_reminder (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    anniversary_id BIGINT NOT NULL COMMENT '纪念日ID',
    reminder_days INT DEFAULT 7 COMMENT '提前几天提醒',
    reminder_type ENUM('app', 'sms', 'email') DEFAULT 'app' COMMENT '提醒方式',
    is_enabled TINYINT DEFAULT 1 COMMENT '是否启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='纪念日提醒设置表';

-- 价格历史表
CREATE TABLE IF NOT EXISTS price_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    item_name VARCHAR(100) NOT NULL COMMENT '商品名称',
    barcode VARCHAR(50) COMMENT '条形码',
    price DECIMAL(10,2) NOT NULL COMMENT '价格',
    store_name VARCHAR(100) COMMENT '商店名称',
    purchase_date DATE COMMENT '购买日期',
    family_id BIGINT COMMENT '家庭ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='价格历史表';

-- 优惠券表
CREATE TABLE IF NOT EXISTS coupon (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    family_id BIGINT NOT NULL COMMENT '家庭ID',
    title VARCHAR(100) NOT NULL COMMENT '优惠券名称',
    platform VARCHAR(50) COMMENT '平台（淘宝/京东/美团等）',
    coupon_code VARCHAR(100) COMMENT '优惠码',
    discount_amount DECIMAL(10,2) COMMENT '优惠金额',
    min_amount DECIMAL(10,2) COMMENT '最低消费',
    expire_date DATE COMMENT '过期日期',
    status TINYINT DEFAULT 0 COMMENT '0未使用 1已使用 2已过期',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券表';

-- 积分兑换券表
CREATE TABLE IF NOT EXISTS points_coupon (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    family_id BIGINT NOT NULL COMMENT '家庭ID',
    title VARCHAR(100) NOT NULL COMMENT '券名称',
    description TEXT COMMENT '描述',
    points_cost INT NOT NULL COMMENT '所需积分',
    coupon_type ENUM('privilege', 'gift', 'task') DEFAULT 'privilege' COMMENT '类型',
    status TINYINT DEFAULT 1 COMMENT '0停用 1启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分兑换券表';

-- 用户积分兑换记录表
CREATE TABLE IF NOT EXISTS points_coupon_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    coupon_id BIGINT NOT NULL COMMENT '券ID',
    points_spent INT NOT NULL COMMENT '花费积分',
    status TINYINT DEFAULT 0 COMMENT '0未使用 1已使用',
    expire_date DATE COMMENT '过期日期',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户积分兑换记录表';

-- 家庭报告表
CREATE TABLE IF NOT EXISTS family_report (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    family_id BIGINT NOT NULL COMMENT '家庭ID',
    report_type ENUM('weekly', 'monthly', 'yearly') NOT NULL COMMENT '报告类型',
    report_date DATE NOT NULL COMMENT '报告日期',
    title VARCHAR(100) NOT NULL COMMENT '报告标题',
    content JSON COMMENT '报告内容JSON',
    score INT COMMENT '综合评分',
    is_read TINYINT DEFAULT 0 COMMENT '是否已读',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家庭报告表';

-- 喝水记录表
CREATE TABLE IF NOT EXISTS water_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    amount INT NOT NULL COMMENT '喝水量(ml)',
    record_date DATE NOT NULL COMMENT '记录日期',
    record_time TIME COMMENT '记录时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_date (user_id, record_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='喝水记录表';
