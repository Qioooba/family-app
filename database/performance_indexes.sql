-- ============================================
-- 性能优化索引创建脚本
-- 执行时间: 2026-02-23
-- ============================================

USE family_app;

-- ============================================
-- 任务表索引
-- ============================================
-- 用户任务查询优化（我的任务）
CREATE INDEX idx_task_user_id ON task(assignee_id);
-- 家庭任务按状态查询优化
CREATE INDEX idx_task_family_status ON task(family_id, status);
-- 今日任务截止时间查询
CREATE INDEX idx_task_due_time ON task(due_time);
-- 创建者任务查询
CREATE INDEX idx_task_creator ON task(creator_id);

-- ============================================
-- 心愿表索引
-- ============================================
-- 家庭心愿按状态查询
CREATE INDEX idx_wish_family_status ON wish(family_id, status);
-- 用户创建的心愿
CREATE INDEX idx_wish_user_id ON wish(user_id);
-- 认领者查询
CREATE INDEX idx_wish_claimant ON wish(claimant_id);
-- 期望日期查询
CREATE INDEX idx_wish_expect_date ON wish(expect_date);

-- ============================================
-- 菜谱表索引
-- ============================================
-- 分类查询
CREATE INDEX idx_recipe_category ON recipe(category);
-- 家庭菜谱查询
CREATE INDEX idx_recipe_family_id ON recipe(family_id);
-- 状态查询（只查启用的）
CREATE INDEX idx_recipe_status ON recipe(status);
-- 评分排序
CREATE INDEX idx_recipe_rating ON recipe(rating DESC);

-- ============================================
-- 纪念日表索引
-- ============================================
-- 关联用户查询（生日等）
CREATE INDEX idx_anniversary_user_id ON anniversary(related_user_id);
-- 家庭纪念日按日期排序
CREATE INDEX idx_anniversary_family_date ON anniversary(family_id, target_date);
-- 重复类型查询
CREATE INDEX idx_anniversary_recurring ON anniversary(is_recurring);

-- ============================================
-- 食材表索引
-- ============================================
-- 家庭食材过期提醒
CREATE INDEX idx_ingredient_family_expire ON ingredient(family_id, expire_date);
-- 状态查询（快过期/已过期）
CREATE INDEX idx_ingredient_status ON ingredient(status);

-- ============================================
-- 动态表索引
-- ============================================
-- 家庭动态时间排序（首页）
CREATE INDEX idx_moment_family_time ON moment(family_id, create_time DESC);
-- 用户动态查询
CREATE INDEX idx_moment_user ON moment(user_id);

-- ============================================
-- 投票表索引
-- ============================================
-- 家庭投票按状态
CREATE INDEX idx_vote_family_status ON vote(family_id, status);
-- 截止时间查询（进行中）
CREATE INDEX idx_vote_end_time ON vote(end_time);

-- ============================================
-- 投票记录表索引
-- ============================================
-- 投票统计查询
CREATE INDEX idx_vote_record_vote ON vote_record(vote_id);

-- ============================================
-- 文件表索引
-- ============================================
-- 文件夹内文件查询
CREATE INDEX idx_file_folder ON family_file(folder_id, family_id);
-- 上传者查询
CREATE INDEX idx_file_uploader ON family_file(uploader_id);

-- ============================================
-- 饮食记录表索引
-- ============================================
-- 用户按日期查询（健康统计）
CREATE INDEX idx_diet_user_date ON diet_record(user_id, record_date);
-- 家庭饮食记录
CREATE INDEX idx_diet_family ON diet_record(family_id);

-- ============================================
-- AI对话历史表索引
-- ============================================
-- 用户会话查询
CREATE INDEX idx_ai_user_session ON ai_chat_history(user_id, session_id);
-- 时间清理
CREATE INDEX idx_ai_create_time ON ai_chat_history(create_time);

-- ============================================
-- 通知表索引
-- ============================================
-- 用户未读通知
CREATE INDEX idx_notification_user_read ON notification(user_id, is_read);
-- 类型查询
CREATE INDEX idx_notification_type ON notification(type);

-- ============================================
-- 查看索引创建结果
-- ============================================
SELECT TABLE_NAME, INDEX_NAME, COLUMN_NAME, CARDINALITY 
FROM INFORMATION_SCHEMA.STATISTICS 
WHERE TABLE_SCHEMA = 'family_app' 
AND INDEX_NAME != 'PRIMARY'
ORDER BY TABLE_NAME, INDEX_NAME;
