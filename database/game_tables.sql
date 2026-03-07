-- ============================================
-- 游戏模块数据库表
-- 创建时间: 2026-03-07
-- ============================================

-- 游戏基础表
CREATE TABLE IF NOT EXISTS game_base (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '游戏ID',
    code VARCHAR(50) NOT NULL COMMENT '游戏代码',
    name VARCHAR(100) NOT NULL COMMENT '游戏名称',
    description TEXT COMMENT '游戏描述',
    category ENUM('puzzle', 'party', 'competition', 'cooperation', 'other') DEFAULT 'other' COMMENT '游戏分类',
    min_players INT DEFAULT 1 COMMENT '最少人数',
    max_players INT DEFAULT 10 COMMENT '最多人数',
    duration_min INT DEFAULT 10 COMMENT '预计时长(分钟)',
    difficulty TINYINT DEFAULT 2 COMMENT '难度 1简单 2中等 3困难',
    icon VARCHAR(100) COMMENT '图标',
    cover_image VARCHAR(255) COMMENT '封面图',
    rules TEXT COMMENT '游戏规则说明',
    is_multiplayer TINYINT DEFAULT 0 COMMENT '是否支持多人',
    is_online TINYINT DEFAULT 1 COMMENT '是否可在线玩',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态 0禁用 1正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_code (code),
    INDEX idx_category (category),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='游戏基础表';

-- 游戏记录表
CREATE TABLE IF NOT EXISTS game_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    family_id BIGINT NOT NULL COMMENT '家庭ID',
    game_id BIGINT NOT NULL COMMENT '游戏ID',
    session_id BIGINT COMMENT '游戏会话ID',
    player_id BIGINT NOT NULL COMMENT '玩家ID',
    player_type ENUM('human', 'ai') DEFAULT 'human' COMMENT '玩家类型',
    score INT DEFAULT 0 COMMENT '得分',
    rank INT COMMENT '排名',
    result ENUM('win', 'lose', 'draw', 'abandon') COMMENT '结果',
    duration_sec INT COMMENT '游戏时长(秒)',
    data JSON COMMENT '游戏数据',
    played_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '游戏时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_family_game (family_id, game_id),
    INDEX idx_player (player_id),
    INDEX idx_played_at (played_at),
    FOREIGN KEY (game_id) REFERENCES game_base(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='游戏记录表';

-- 游戏积分表
CREATE TABLE IF NOT EXISTS game_score (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '积分ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    family_id BIGINT NOT NULL COMMENT '家庭ID',
    game_id BIGINT COMMENT '游戏ID NULL为总积分',
    total_score INT DEFAULT 0 COMMENT '总积分',
    win_count INT DEFAULT 0 COMMENT '胜利次数',
    lose_count INT DEFAULT 0 COMMENT '失败次数',
    draw_count INT DEFAULT 0 COMMENT '平局次数',
    play_count INT DEFAULT 0 COMMENT '游戏次数',
    best_score INT DEFAULT 0 COMMENT '最高得分',
    avg_score DECIMAL(6,2) DEFAULT 0 COMMENT '平均得分',
    level INT DEFAULT 1 COMMENT '等级',
    title VARCHAR(50) COMMENT '称号',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_game (user_id, game_id),
    INDEX idx_family_user (family_id, user_id),
    INDEX idx_total_score (total_score DESC),
    FOREIGN KEY (game_id) REFERENCES game_base(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='游戏积分表';

-- 游戏配置表
CREATE TABLE IF NOT EXISTS game_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
    family_id BIGINT NOT NULL COMMENT '家庭ID',
    game_id BIGINT NOT NULL COMMENT '游戏ID',
    config_key VARCHAR(100) NOT NULL COMMENT '配置项',
    config_value TEXT COMMENT '配置值',
    description VARCHAR(200) COMMENT '配置说明',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_family_game_key (family_id, game_id, config_key),
    FOREIGN KEY (game_id) REFERENCES game_base(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='游戏配置表';

-- 猜谜题库表
CREATE TABLE IF NOT EXISTS game_riddle (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '题目ID',
    question VARCHAR(500) NOT NULL COMMENT '谜面',
    answer VARCHAR(200) NOT NULL COMMENT '谜底',
    hint VARCHAR(300) COMMENT '提示',
    category ENUM('animal', 'plant', 'item', 'idiom', 'character', 'place', 'other') DEFAULT 'other' COMMENT '分类',
    difficulty TINYINT DEFAULT 2 COMMENT '难度 1简单 2中等 3困难',
    source VARCHAR(100) COMMENT '来源',
    use_count INT DEFAULT 0 COMMENT '使用次数',
    correct_rate DECIMAL(4,2) DEFAULT 0 COMMENT '正确率',
    status TINYINT DEFAULT 1 COMMENT '状态 0禁用 1正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_category (category),
    INDEX idx_difficulty (difficulty),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='猜谜题库表';

-- 问答题目表
CREATE TABLE IF NOT EXISTS game_quiz (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '题目ID',
    question VARCHAR(500) NOT NULL COMMENT '题目',
    question_type ENUM('text', 'image', 'audio') DEFAULT 'text' COMMENT '题目类型',
    media_url VARCHAR(255) COMMENT '媒体文件URL',
    options JSON COMMENT '选项 [{option, is_correct}]',
    correct_answer VARCHAR(200) NOT NULL COMMENT '正确答案',
    explanation TEXT COMMENT '答案解析',
    category ENUM('science', 'history', 'geography', 'art', 'sport', 'life', 'entertainment', 'other') DEFAULT 'other' COMMENT '分类',
    difficulty TINYINT DEFAULT 2 COMMENT '难度 1简单 2中等 3困难',
    tags JSON COMMENT '标签',
    source VARCHAR(100) COMMENT '来源',
    use_count INT DEFAULT 0 COMMENT '使用次数',
    correct_rate DECIMAL(4,2) DEFAULT 0 COMMENT '正确率',
    status TINYINT DEFAULT 1 COMMENT '状态 0禁用 1正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_category (category),
    INDEX idx_difficulty (difficulty),
    INDEX idx_status (status),
    FULLTEXT INDEX ft_question (question) WITH PARSER ngram
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问答题目表';

-- 成语库表
CREATE TABLE IF NOT EXISTS game_idiom (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '成语ID',
    idiom VARCHAR(50) NOT NULL COMMENT '成语',
    pinyin VARCHAR(200) COMMENT '拼音',
    meaning TEXT COMMENT '释义',
    origin TEXT COMMENT '出处',
    example TEXT COMMENT '例句',
    first_char CHAR(1) COMMENT '首字',
    last_char CHAR(1) COMMENT '尾字',
    length TINYINT DEFAULT 4 COMMENT '字数',
    difficulty TINYINT DEFAULT 2 COMMENT '难度 1简单 2中等 3困难',
    tags JSON COMMENT '标签',
    use_count INT DEFAULT 0 COMMENT '使用次数',
    status TINYINT DEFAULT 1 COMMENT '状态 0禁用 1正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_first_char (first_char),
    INDEX idx_last_char (last_char),
    INDEX idx_length (length),
    INDEX idx_status (status),
    UNIQUE KEY uk_idiom (idiom),
    FULLTEXT INDEX ft_idiom (idiom, meaning) WITH PARSER ngram
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成语库表';

-- 谁是卧底词库
CREATE TABLE IF NOT EXISTS game_undercover_word (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '词组ID',
    civilian_word VARCHAR(50) NOT NULL COMMENT '平民词',
    spy_word VARCHAR(50) NOT NULL COMMENT '卧底词',
    category VARCHAR(50) COMMENT '分类',
    difficulty TINYINT DEFAULT 2 COMMENT '难度 1简单 2中等 3困难',
    similarity TINYINT COMMENT '相似度 1-10',
    tips TEXT COMMENT '提示说明',
    use_count INT DEFAULT 0 COMMENT '使用次数',
    rating DECIMAL(2,1) DEFAULT 5.0 COMMENT '评分',
    status TINYINT DEFAULT 1 COMMENT '状态 0禁用 1正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_category (category),
    INDEX idx_difficulty (difficulty),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='谁是卧底词库';

-- 真心话大冒险题库
CREATE TABLE IF NOT EXISTS game_truth_dare (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '题目ID',
    content TEXT NOT NULL COMMENT '内容',
    type ENUM('truth', 'dare') NOT NULL COMMENT '类型 truth真心话 dare大冒险',
    level ENUM('mild', 'medium', 'spicy', 'extreme') DEFAULT 'medium' COMMENT '尺度 mild温和 medium中等 spicy刺激 extreme极限',
    category VARCHAR(50) COMMENT '分类',
    tags JSON COMMENT '标签',
    min_players INT DEFAULT 2 COMMENT '最少人数',
    suitable_for JSON COMMENT '适合对象 [couple, family, friends, party]',
    use_count INT DEFAULT 0 COMMENT '使用次数',
    rating DECIMAL(2,1) DEFAULT 5.0 COMMENT '评分',
    status TINYINT DEFAULT 1 COMMENT '状态 0禁用 1正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_type (type),
    INDEX idx_level (level),
    INDEX idx_category (category),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='真心话大冒险题库';

-- 你画我猜词库
CREATE TABLE IF NOT EXISTS game_drawing_word (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '词汇ID',
    word VARCHAR(100) NOT NULL COMMENT '词汇',
    category ENUM('animal', 'food', 'item', 'action', 'character', 'movie', 'idiom', 'other') DEFAULT 'other' COMMENT '分类',
    difficulty TINYINT DEFAULT 2 COMMENT '难度 1简单 2中等 3困难',
    hints JSON COMMENT '提示词列表',
    drawing_time INT DEFAULT 60 COMMENT '绘画时间(秒)',
    use_count INT DEFAULT 0 COMMENT '使用次数',
    correct_rate DECIMAL(4,2) DEFAULT 0 COMMENT '猜中率',
    status TINYINT DEFAULT 1 COMMENT '状态 0禁用 1正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_category (category),
    INDEX idx_difficulty (difficulty),
    INDEX idx_status (status),
    FULLTEXT INDEX ft_word (word) WITH PARSER ngram
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='你画我猜词库';

-- K歌歌曲库
CREATE TABLE IF NOT EXISTS game_karaoke_song (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '歌曲ID',
    name VARCHAR(200) NOT NULL COMMENT '歌名',
    artist VARCHAR(100) COMMENT '歌手',
    album VARCHAR(100) COMMENT '专辑',
    category ENUM('pop', 'classic', 'folk', 'rock', 'children', 'oldies', 'other') DEFAULT 'pop' COMMENT '分类',
    difficulty TINYINT DEFAULT 2 COMMENT '难度 1简单 2中等 3困难',
    language VARCHAR(20) DEFAULT '中文' COMMENT '语言',
    decade VARCHAR(20) COMMENT '年代',
    mood VARCHAR(50) COMMENT '情绪',
    lyrics TEXT COMMENT '歌词',
    lyrics_url VARCHAR(255) COMMENT '歌词文件URL',
    audio_url VARCHAR(255) COMMENT '音频URL',
    duration INT COMMENT '时长(秒)',
    tags JSON COMMENT '标签',
    use_count INT DEFAULT 0 COMMENT '使用次数',
    rating DECIMAL(2,1) DEFAULT 5.0 COMMENT '评分',
    status TINYINT DEFAULT 1 COMMENT '状态 0禁用 1正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_category (category),
    INDEX idx_difficulty (difficulty),
    INDEX idx_language (language),
    INDEX idx_status (status),
    FULLTEXT INDEX ft_name (name, artist) WITH PARSER ngram
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='K歌歌曲库';

-- 寻宝线索表
CREATE TABLE IF NOT EXISTS game_treasure_clue (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '线索ID',
    family_id BIGINT NOT NULL COMMENT '家庭ID',
    hunt_id BIGINT NOT NULL COMMENT '寻宝活动ID',
    sequence INT NOT NULL COMMENT '线索序号',
    clue_type ENUM('text', 'image', 'audio', 'video', 'riddle', 'qr') DEFAULT 'text' COMMENT '线索类型',
    content TEXT NOT NULL COMMENT '线索内容',
    hint TEXT COMMENT '提示',
    answer VARCHAR(200) COMMENT '答案',
    location_name VARCHAR(200) COMMENT '地点名称',
    location_lat DECIMAL(10,7) COMMENT '纬度',
    location_lng DECIMAL(10,7) COMMENT '经度',
    radius INT DEFAULT 50 COMMENT '触发半径(米)',
    next_clue_id BIGINT COMMENT '下一条线索ID',
    reward TEXT COMMENT '找到后的奖励',
    image_url VARCHAR(255) COMMENT '线索图片',
    status TINYINT DEFAULT 1 COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_hunt (hunt_id),
    INDEX idx_sequence (sequence)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='寻宝线索表';

-- 厨艺比拼记录表
CREATE TABLE IF NOT EXISTS game_cooking_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    family_id BIGINT NOT NULL COMMENT '家庭ID',
    session_id BIGINT COMMENT '活动会话ID',
    theme VARCHAR(100) COMMENT '主题',
    participant_id BIGINT NOT NULL COMMENT '参赛者ID',
    dish_name VARCHAR(100) NOT NULL COMMENT '菜品名称',
    description TEXT COMMENT '菜品描述',
    images JSON COMMENT '菜品图片',
    video_url VARCHAR(255) COMMENT '视频URL',
    recipe_id BIGINT COMMENT '关联菜谱ID',
    cooking_time INT COMMENT '烹饪时长(分钟)',
    difficulty TINYINT COMMENT '难度自评',
    scores JSON COMMENT '各项评分 {taste, appearance, creativity, difficulty}',
    total_score DECIMAL(4,2) COMMENT '总分',
    rank INT COMMENT '排名',
    comments JSON COMMENT '评委评语',
    status TINYINT DEFAULT 0 COMMENT '状态 0进行中 1已完成',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    judged_at DATETIME COMMENT '评分时间',
    INDEX idx_family_session (family_id, session_id),
    INDEX idx_participant (participant_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='厨艺比拼记录表';

-- 运动会记录表
CREATE TABLE IF NOT EXISTS game_sports_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    family_id BIGINT NOT NULL COMMENT '家庭ID',
    session_id BIGINT COMMENT '活动会话ID',
    event_name VARCHAR(100) NOT NULL COMMENT '项目名称',
    event_type ENUM('track', 'field', 'team', 'fun', 'other') DEFAULT 'other' COMMENT '项目类型',
    participant_id BIGINT NOT NULL COMMENT '参赛者ID',
    result VARCHAR(100) COMMENT '成绩',
    result_unit VARCHAR(20) COMMENT '成绩单位',
    score INT COMMENT '得分',
    rank INT COMMENT '名次',
    is_record TINYINT DEFAULT 0 COMMENT '是否破纪录',
    record_proof VARCHAR(255) COMMENT '证明图片/视频',
    remarks TEXT COMMENT '备注',
    status TINYINT DEFAULT 1 COMMENT '状态',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_family_session (family_id, session_id),
    INDEX idx_participant (participant_id),
    INDEX idx_event (event_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运动会记录表';

-- 游戏会话表(支持多人游戏)
CREATE TABLE IF NOT EXISTS game_session (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '会话ID',
    family_id BIGINT NOT NULL COMMENT '家庭ID',
    game_id BIGINT NOT NULL COMMENT '游戏ID',
    room_code VARCHAR(20) COMMENT '房间号',
    host_id BIGINT NOT NULL COMMENT '房主ID',
    player_ids JSON COMMENT '参与玩家ID列表',
    max_players INT DEFAULT 4 COMMENT '最大人数',
    settings JSON COMMENT '游戏设置',
    status ENUM('waiting', 'playing', 'paused', 'finished', 'abandoned') DEFAULT 'waiting' COMMENT '状态',
    current_round INT DEFAULT 0 COMMENT '当前回合',
    total_rounds INT DEFAULT 1 COMMENT '总回合数',
    current_player_id BIGINT COMMENT '当前行动玩家',
    game_data JSON COMMENT '游戏数据',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    winner_ids JSON COMMENT '获胜者ID列表',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_family_game (family_id, game_id),
    INDEX idx_room_code (room_code),
    INDEX idx_host (host_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time),
    FOREIGN KEY (game_id) REFERENCES game_base(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='游戏会话表';

-- 游戏会话玩家表(多对多关系)
CREATE TABLE IF NOT EXISTS game_session_player (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    session_id BIGINT NOT NULL COMMENT '会话ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    player_name VARCHAR(50) COMMENT '玩家名称',
    avatar VARCHAR(255) COMMENT '玩家头像',
    seat_number INT COMMENT '座位号',
    is_ready TINYINT DEFAULT 0 COMMENT '是否准备',
    is_online TINYINT DEFAULT 1 COMMENT '是否在线',
    score INT DEFAULT 0 COMMENT '当前得分',
    status ENUM('waiting', 'playing', 'left', 'kicked') DEFAULT 'waiting' COMMENT '状态',
    join_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    leave_time DATETIME COMMENT '离开时间',
    UNIQUE KEY uk_session_user (session_id, user_id),
    INDEX idx_session (session_id),
    INDEX idx_user (user_id),
    FOREIGN KEY (session_id) REFERENCES game_session(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='游戏会话玩家表';
