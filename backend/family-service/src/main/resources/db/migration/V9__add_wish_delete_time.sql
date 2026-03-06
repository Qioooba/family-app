-- 为wish表添加delete_time字段
ALTER TABLE wish ADD COLUMN delete_time DATETIME NULL COMMENT '删除时间' AFTER finish_time;
