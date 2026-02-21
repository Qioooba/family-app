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
