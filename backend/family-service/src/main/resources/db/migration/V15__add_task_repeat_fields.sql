-- 为任务表添加重复规则相关字段
-- repeat_type: 重复类型 (0=不重复, 1=每天, 2=每周, 3=每月, 4=每年, 5=工作日, 6=自定义)
-- repeat_rule: JSON格式的重复规则配置
-- repeat_start_date: 重复开始日期
-- repeat_end_date: 重复结束日期（可选）
-- next_run_time: 下次执行时间（用于定时任务触发）

ALTER TABLE task ADD COLUMN repeat_type TINYINT DEFAULT 0 COMMENT '重复类型: 0=不重复, 1=每天, 2=每周, 3=每月, 4=每年, 5=工作日, 6=自定义' AFTER sort_order;
ALTER TABLE task ADD COLUMN repeat_rule VARCHAR(500) DEFAULT NULL COMMENT 'JSON格式的重复规则配置' AFTER repeat_type;
ALTER TABLE task ADD COLUMN repeat_start_date DATE DEFAULT NULL COMMENT '重复开始日期' AFTER repeat_rule;
ALTER TABLE task ADD COLUMN repeat_end_date DATE DEFAULT NULL COMMENT '重复结束日期' AFTER repeat_start_date;
ALTER TABLE task ADD COLUMN next_run_time DATETIME DEFAULT NULL COMMENT '下次执行时间' AFTER repeat_end_date;

-- 添加索引以优化定时任务查询
CREATE INDEX idx_task_next_run_time ON task(next_run_time);
CREATE INDEX idx_task_repeat_type ON task(repeat_type);
