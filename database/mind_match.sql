-- 默契问答相关表

-- 默契问答问题表
CREATE TABLE IF NOT EXISTS mind_match_question (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    question VARCHAR(500) NOT NULL COMMENT '问题内容',
    type VARCHAR(20) NOT NULL COMMENT '题目类型: spouse-夫妻, parent-亲子, family-家庭',
    target_user_id BIGINT COMMENT '指向的用户ID(被问关于谁)',
    target_answer VARCHAR(500) COMMENT '期望的答案(被问者本人的答案)',
    difficulty INT DEFAULT 1 COMMENT '难度: 1-简单, 2-中等, 3-困难',
    status INT DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    play_count INT DEFAULT 0 COMMENT '使用次数',
    correct_count INT DEFAULT 0 COMMENT '答对次数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='默契问答问题';

-- 默契问答游戏会话表
CREATE TABLE IF NOT EXISTS mind_match_session (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    family_id BIGINT NOT NULL COMMENT '家庭ID',
    round INT DEFAULT 1 COMMENT '游戏轮次',
    question_id BIGINT COMMENT '题目ID',
    player1_id BIGINT NOT NULL COMMENT '玩家1ID',
    player1_answer VARCHAR(500) COMMENT '玩家1答案',
    player1_answered TINYINT(1) DEFAULT 0 COMMENT '玩家1是否已回答',
    player2_id BIGINT NOT NULL COMMENT '玩家2ID',
    player2_answer VARCHAR(500) COMMENT '玩家2答案',
    player2_answered TINYINT(1) DEFAULT 0 COMMENT '玩家2是否已回答',
    is_match TINYINT(1) COMMENT '是否匹配(答案相同)',
    round_points INT DEFAULT 0 COMMENT '本轮得分',
    status VARCHAR(20) DEFAULT 'waiting' COMMENT '会话状态: waiting-等待中, in_progress-进行中, completed-已完成',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted INT DEFAULT 0,
    INDEX idx_family_id (family_id),
    INDEX idx_player1_id (player1_id),
    INDEX idx_player2_id (player2_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='默契问答游戏会话';
