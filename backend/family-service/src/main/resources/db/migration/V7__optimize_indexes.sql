-- ============================================
-- 数据库索引优化脚本
-- 针对家庭小程序常用查询场景优化
-- 兼容 MySQL 5.7+
-- ============================================

USE family_app;

-- ============================================
-- 1. task 表优化（待办任务查询最频繁）
-- ============================================

-- 创建复合索引：按家庭+状态查询，按截止时间排序（首页今日待办）
-- 如果索引已存在会报错，忽略即可
CREATE INDEX idx_task_family_status_due 
ON task(family_id, status, due_time);

-- 创建复合索引：按指派人查询任务（我的任务）
CREATE INDEX idx_task_assignee_status 
ON task(assignee_id, status);

-- 创建索引：按创建时间倒序（最新任务）
CREATE INDEX idx_task_create_time 
ON task(create_time DESC);

-- ============================================
-- 2. water_record 表优化（喝水记录）
-- ============================================

-- 创建复合索引：按用户+日期查询（今日喝水量统计）
CREATE INDEX idx_water_user_date 
ON water_record(user_id, record_date);

-- 创建索引：按记录时间倒序（最近喝水记录）
CREATE INDEX idx_water_record_time 
ON water_record(record_time DESC);

-- ============================================
-- 3. family_member 表优化（家庭成员查询）
-- ============================================

-- 添加单列索引加速查询
CREATE INDEX idx_member_user_id 
ON family_member(user_id);

CREATE INDEX idx_member_family_id 
ON family_member(family_id);

-- ============================================
-- 4. album_photo 表优化（家庭相册）
-- ============================================

-- 创建复合索引：按家庭+创建时间（相册列表）
CREATE INDEX idx_photo_family_time 
ON album_photo(family_id, create_time DESC);

-- ============================================
-- 5. calendar_anniversary 表优化（纪念日）
-- ============================================

-- 创建复合索引：按家庭+日期（近期纪念日）
CREATE INDEX idx_anni_family_date 
ON calendar_anniversary(family_id, anniversary_date);

-- ============================================
-- 6. wish 表优化（心愿墙）
-- ============================================

-- 创建复合索引：按家庭+状态（未完成的心愿）
CREATE INDEX idx_wish_family_status 
ON wish(family_id, status);

-- ============================================
-- 7. diet_record 表优化（饮食记录）
-- ============================================

-- 创建复合索引：按用户+日期
CREATE INDEX idx_diet_user_date 
ON diet_record(user_id, record_date);

-- ============================================
-- 8. weight_record 表优化（体重记录）
-- ============================================

-- 创建复合索引：按用户+记录时间
CREATE INDEX idx_weight_user_time 
ON weight_record(user_id, record_time DESC);

-- ============================================
-- 9. invite_code 表优化（邀请码）
-- ============================================

-- 创建复合索引：按家庭+状态（有效邀请码）
CREATE INDEX idx_invite_family_status 
ON invite_code(family_id, status);

-- ============================================
-- 10. sys_user 表优化（用户表）
-- ============================================

-- 添加当前家庭索引
CREATE INDEX idx_user_current_family 
ON sys_user(current_family_id);

-- ============================================
-- 验证索引创建结果
-- ============================================

SELECT 
    TABLE_NAME,
    INDEX_NAME,
    COLUMN_NAME,
    INDEX_TYPE
FROM information_schema.STATISTICS 
WHERE TABLE_SCHEMA = 'family_app' 
ORDER BY TABLE_NAME, INDEX_NAME;
