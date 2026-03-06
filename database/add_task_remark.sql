-- 添加任务备注字段
ALTER TABLE task ADD COLUMN remark TEXT COMMENT '备注' AFTER content;
