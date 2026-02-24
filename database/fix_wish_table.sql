-- 修复心愿墙功能 - 创建缺失的数据库表
-- 执行时间: 2026-02-23

-- 创建心愿表
CREATE TABLE IF NOT EXISTS wish (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    family_id BIGINT NOT NULL COMMENT '家庭ID',
    user_id BIGINT NOT NULL COMMENT '创建者ID',
    type ENUM('item', 'experience', 'learn', 'relation', 'charity', 'goal', 'custom') NOT NULL DEFAULT 'custom' COMMENT '类型',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    description TEXT COMMENT '描述',
    budget_min DECIMAL(10,2) COMMENT '预算下限',
    budget_max DECIMAL(10,2) COMMENT '预算上限',
    expect_date DATE COMMENT '期望实现日期',
    visibility ENUM('public', 'family', 'couple', 'private') DEFAULT 'public' COMMENT '可见性',
    priority TINYINT DEFAULT 2 COMMENT '优先级 1-3',
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
    INDEX idx_status (status),
    INDEX idx_claimant_id (claimant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='心愿表';

-- 创建心愿里程碑表
CREATE TABLE IF NOT EXISTS wish_milestone (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    wish_id BIGINT NOT NULL COMMENT '心愿ID',
    title VARCHAR(100) NOT NULL COMMENT '里程碑标题',
    description TEXT COMMENT '描述',
    target_date DATE COMMENT '目标日期',
    is_completed TINYINT DEFAULT 0 COMMENT '是否完成',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_wish_id (wish_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='心愿里程碑表';

-- 创建心愿预算表（如果需要）
CREATE TABLE IF NOT EXISTS wish_budget (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    wish_id BIGINT NOT NULL COMMENT '心愿ID',
    estimated_amount DECIMAL(10,2) COMMENT '预估金额',
    actual_amount DECIMAL(10,2) COMMENT '实际金额',
    currency VARCHAR(10) DEFAULT 'CNY' COMMENT '货币',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_wish_id (wish_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='心愿预算表';

-- 插入测试数据（可选）
INSERT INTO wish (family_id, user_id, type, title, description, priority, status, progress, expect_date) VALUES
(1, 1, 'experience', '去海边度假', '想要和家人一起去三亚度假，享受阳光沙滩', 3, 0, 0, '2026-07-01'),
(1, 1, 'item', '买一台新相机', '想要一台单反相机记录家庭生活', 2, 1, 30, '2026-06-01'),
(1, 2, 'goal', '学会做饭', '学会做10道家常菜', 2, 0, 0, '2026-12-31'),
(1, 2, 'learn', '学习钢琴', '学会弹奏5首经典曲目', 1, 2, 100, '2026-03-01');
