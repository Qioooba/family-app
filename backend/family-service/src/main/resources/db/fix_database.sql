-- ============================================
-- 家庭小程序数据库完整修复脚本
-- 执行方式：在 MySQL 客户端中运行
-- ============================================

-- 使用正确的数据库
USE family_app;

-- ============================================
-- 1. 修复已有表 - 添加缺失字段
-- ============================================

-- 食材表
ALTER TABLE ingredient
ADD COLUMN IF NOT EXISTS create_time datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
ADD COLUMN IF NOT EXISTS update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
ADD COLUMN IF NOT EXISTS is_deleted tinyint DEFAULT 0 COMMENT '是否删除(0-否 1-是)';

-- ============================================
-- 2. 创建缺失的表
-- ============================================

-- 谜语表
CREATE TABLE IF NOT EXISTS game_riddle (
    id bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
    question varchar(500) NOT NULL COMMENT '谜面',
    answer varchar(200) NOT NULL COMMENT '谜底',
    hint varchar(300) DEFAULT NULL COMMENT '提示',
    category varchar(50) DEFAULT NULL COMMENT '分类',
    difficulty int DEFAULT 1 COMMENT '难度 1-5',
    explanation varchar(500) DEFAULT NULL COMMENT '解释',
    play_count int DEFAULT 0 COMMENT '游玩次数',
    correct_count int DEFAULT 0 COMMENT '答对次数',
    status tinyint DEFAULT 1 COMMENT '状态 1-正常 0-禁用',
    create_time datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted tinyint DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (id),
    KEY idx_category (category),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='谜语表';

-- 答题表
CREATE TABLE IF NOT EXISTS game_quiz (
    id bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
    question varchar(500) NOT NULL COMMENT '问题',
    option_a varchar(200) DEFAULT NULL COMMENT '选项A',
    option_b varchar(200) DEFAULT NULL COMMENT '选项B',
    option_c varchar(200) DEFAULT NULL COMMENT '选项C',
    option_d varchar(200) DEFAULT NULL COMMENT '选项D',
    correct_answer varchar(10) NOT NULL COMMENT '正确答案 A/B/C/D',
    category varchar(50) DEFAULT NULL COMMENT '分类',
    difficulty int DEFAULT 1 COMMENT '难度 1-5',
    explanation varchar(500) DEFAULT NULL COMMENT '解释',
    play_count int DEFAULT 0 COMMENT '游玩次数',
    correct_count int DEFAULT 0 COMMENT '答对次数',
    status tinyint DEFAULT 1 COMMENT '状态 1-正常 0-禁用',
    create_time datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted tinyint DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (id),
    KEY idx_category (category),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='答题表';

-- 家庭挑战表
CREATE TABLE IF NOT EXISTS family_challenge (
    id bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
    family_id bigint NOT NULL COMMENT '家庭ID',
    title varchar(200) NOT NULL COMMENT '标题',
    description varchar(1000) DEFAULT NULL COMMENT '描述',
    type int DEFAULT 1 COMMENT '类型 1-健康挑战 2-任务挑战 3-习惯挑战',
    target_type varchar(50) DEFAULT NULL COMMENT '目标类型',
    target_value decimal(10,2) DEFAULT NULL COMMENT '目标值',
    start_date date DEFAULT NULL COMMENT '开始日期',
    end_date date DEFAULT NULL COMMENT '结束日期',
    creator_id bigint DEFAULT NULL COMMENT '创建者ID',
    participant_count int DEFAULT 0 COMMENT '参与人数',
    status tinyint DEFAULT 1 COMMENT '状态 1-正常 0-禁用',
    create_time datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted tinyint DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (id),
    KEY idx_family_id (family_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家庭挑战表';

-- 挑战参与者表
CREATE TABLE IF NOT EXISTS challenge_participant (
    id bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
    challenge_id bigint NOT NULL COMMENT '挑战ID',
    family_id bigint NOT NULL COMMENT '家庭ID',
    user_id bigint NOT NULL COMMENT '用户ID',
    current_value decimal(10,2) DEFAULT 0 COMMENT '当前值',
    progress int DEFAULT 0 COMMENT '进度百分比',
    checkin_count int DEFAULT 0 COMMENT '打卡次数',
    last_checkin_date date DEFAULT NULL COMMENT '最后打卡日期',
    is_completed tinyint DEFAULT 0 COMMENT '是否完成 0-否 1-是',
    status tinyint DEFAULT 1 COMMENT '状态 1-正常 0-退出',
    create_time datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted tinyint DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_challenge_user (challenge_id,user_id),
    KEY idx_challenge_id (challenge_id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='挑战参与者表';

-- 打卡记录表
CREATE TABLE IF NOT EXISTS challenge_checkin (
    id bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
    challenge_id bigint NOT NULL COMMENT '挑战ID',
    participant_id bigint NOT NULL COMMENT '参与者ID',
    user_id bigint NOT NULL COMMENT '用户ID',
    checkin_date date DEFAULT NULL COMMENT '打卡日期',
    note varchar(500) DEFAULT NULL COMMENT '备注',
    image varchar(500) DEFAULT NULL COMMENT '图片',
    value decimal(10,2) DEFAULT NULL COMMENT '打卡值',
    status tinyint DEFAULT 1 COMMENT '状态 1-正常 0-取消',
    create_time datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted tinyint DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (id),
    KEY idx_challenge_id (challenge_id),
    KEY idx_user_id (user_id),
    KEY idx_checkin_date (checkin_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='打卡记录表';

-- ============================================
-- 3. 插入示例数据
-- ============================================

-- 插入示例谜语
INSERT INTO game_riddle (question, answer, hint, category, difficulty, explanation, status) VALUES
('一口咬掉牛尾巴，是什么字？', '告', '牛去掉下面一竖，加上口', '字谜', 2, '牛去掉下面一竖是“牛”字头，加上口就是“告”字', 1),
('小时穿黑衣，大时穿绿袍，水里过日子，岸上来睡觉。', '青蛙', '会跳，呱呱叫', '动物谜', 1, '青蛙小时候是黑蝌蚪，长大是绿青蛙', 1),
('五个兄弟，住在一起，名字不同，高矮不齐。', '手指', '每个人都有，五个', '人体谜', 1, '五根手指长短不同', 1),
('千条线，万条线，掉到水里看不见。', '雨', '天上落下来，水里的', '自然谜', 1, '雨滴像线，落入水中消失', 1),
('独木造高楼，没瓦没砖头，人在水下走，水在人上流。', '雨伞', '下雨天用', '物品谜', 2, '雨伞像高楼，伞面挡雨', 1);

-- 插入示例题目
INSERT INTO game_quiz (question, option_a, option_b, option_c, option_d, correct_answer, category, difficulty, explanation, status) VALUES
('中国的首都是哪里？', '上海', '北京', '广州', '深圳', 'B', '地理', 1, '北京是中华人民共和国的首都', 1),
('一年有多少个节气？', '12', '24', '36', '48', 'B', '文化', 1, '中国传统有二十四节气', 1),
('彩虹有几种颜色？', '5', '6', '7', '8', 'C', '自然', 1, '彩虹有红橙黄绿青蓝紫七种颜色', 1),
('人体最大的器官是什么？', '心脏', '肝脏', '皮肤', '大脑', 'C', '人体', 2, '皮肤是人体最大的器官', 1),
('“床前明月光”的下一句是？', '疑是地上霜', '举头望明月', '低头思故乡', '处处闻啼鸟', 'A', '诗词', 1, '李白《静夜思》：床前明月光，疑是地上霜', 1);

