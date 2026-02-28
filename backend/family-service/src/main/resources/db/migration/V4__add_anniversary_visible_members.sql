-- 纪念日隐私权限控制 - 添加可见成员字段
-- 执行方式：在 MySQL 客户端中运行
-- ============================================

USE family_app;

-- 为纪念日表添加可见成员字段
ALTER TABLE calendar_anniversary 
ADD COLUMN IF NOT EXISTS visible_members varchar(1000) DEFAULT NULL COMMENT 'JSON格式存储可见成员ID列表，如：[1,2,3]';
