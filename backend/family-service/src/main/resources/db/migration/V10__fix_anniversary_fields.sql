-- 为anniversary表添加缺少的字段
ALTER TABLE anniversary ADD COLUMN visible_members VARCHAR(500) NULL COMMENT '可见成员' AFTER icon;
