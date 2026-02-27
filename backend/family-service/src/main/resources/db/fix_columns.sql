-- ============================================
-- 紧急修复：添加缺失的数据库字段
-- 执行方式：在 MySQL 中运行
-- ============================================

-- 食材表 ingredient
ALTER TABLE ingredient
ADD COLUMN IF NOT EXISTS create_time datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
ADD COLUMN IF NOT EXISTS update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
ADD COLUMN IF NOT EXISTS is_deleted tinyint DEFAULT 0 COMMENT '是否删除(0-否 1-是)';

-- 谜语表 game_riddle
ALTER TABLE game_riddle
ADD COLUMN IF NOT EXISTS create_time datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
ADD COLUMN IF NOT EXISTS update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
ADD COLUMN IF NOT EXISTS is_deleted tinyint DEFAULT 0 COMMENT '是否删除';

-- 答题表 game_quiz
ALTER TABLE game_quiz
ADD COLUMN IF NOT EXISTS create_time datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
ADD COLUMN IF NOT EXISTS update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
ADD COLUMN IF NOT EXISTS is_deleted tinyint DEFAULT 0 COMMENT '是否删除';

-- 家庭挑战表 family_challenge
ALTER TABLE family_challenge
ADD COLUMN IF NOT EXISTS create_time datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
ADD COLUMN IF NOT EXISTS update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
ADD COLUMN IF NOT EXISTS is_deleted tinyint DEFAULT 0 COMMENT '是否删除';

-- 挑战参与者表 challenge_participant
ALTER TABLE challenge_participant
ADD COLUMN IF NOT EXISTS create_time datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
ADD COLUMN IF NOT EXISTS update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
ADD COLUMN IF NOT EXISTS is_deleted tinyint DEFAULT 0 COMMENT '是否删除';

-- 打卡记录表 challenge_checkin
ALTER TABLE challenge_checkin
ADD COLUMN IF NOT EXISTS create_time datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
ADD COLUMN IF NOT EXISTS update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
ADD COLUMN IF NOT EXISTS is_deleted tinyint DEFAULT 0 COMMENT '是否删除';

