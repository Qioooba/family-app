-- 为任务表添加重复规则相关字段
-- repeat_type: 重复类型 (none/daily/weekly/monthly/yearly/workday/custom)
-- repeat_rule: JSON格式的重复规则配置
-- repeat_start_date: 重复开始日期
-- repeat_end_date: 重复结束日期（可选）
-- next_run_time: 下次执行时间（用于定时任务触发）

ALTER TABLE task MODIFY COLUMN repeat_type VARCHAR(20) DEFAULT 'none' COMMENT '重复类型: none/daily/weekly/monthly/yearly/workday/custom';
ALTER TABLE task MODIFY COLUMN repeat_rule VARCHAR(500) DEFAULT NULL COMMENT 'JSON格式的重复规则配置';
SET @repeat_start_date_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'task'
      AND column_name = 'repeat_start_date'
);
SET @repeat_start_date_sql = IF(
    @repeat_start_date_exists = 0,
    'ALTER TABLE task ADD COLUMN repeat_start_date DATE DEFAULT NULL COMMENT ''重复开始日期'' AFTER repeat_rule',
    'SELECT 1'
);
PREPARE repeat_start_date_stmt FROM @repeat_start_date_sql;
EXECUTE repeat_start_date_stmt;
DEALLOCATE PREPARE repeat_start_date_stmt;

SET @repeat_end_date_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'task'
      AND column_name = 'repeat_end_date'
);
SET @repeat_end_date_sql = IF(
    @repeat_end_date_exists = 0,
    'ALTER TABLE task ADD COLUMN repeat_end_date DATE DEFAULT NULL COMMENT ''重复结束日期'' AFTER repeat_start_date',
    'SELECT 1'
);
PREPARE repeat_end_date_stmt FROM @repeat_end_date_sql;
EXECUTE repeat_end_date_stmt;
DEALLOCATE PREPARE repeat_end_date_stmt;

SET @next_run_time_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'task'
      AND column_name = 'next_run_time'
);
SET @next_run_time_sql = IF(
    @next_run_time_exists = 0,
    'ALTER TABLE task ADD COLUMN next_run_time DATETIME DEFAULT NULL COMMENT ''下次执行时间'' AFTER repeat_end_date',
    'SELECT 1'
);
PREPARE next_run_time_stmt FROM @next_run_time_sql;
EXECUTE next_run_time_stmt;
DEALLOCATE PREPARE next_run_time_stmt;

-- 添加索引以优化定时任务查询
SET @idx_task_next_run_time_exists = (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'task'
      AND index_name = 'idx_task_next_run_time'
);
SET @idx_task_next_run_time_sql = IF(
    @idx_task_next_run_time_exists = 0,
    'CREATE INDEX idx_task_next_run_time ON task(next_run_time)',
    'SELECT 1'
);
PREPARE idx_task_next_run_time_stmt FROM @idx_task_next_run_time_sql;
EXECUTE idx_task_next_run_time_stmt;
DEALLOCATE PREPARE idx_task_next_run_time_stmt;

SET @idx_task_repeat_type_exists = (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'task'
      AND index_name = 'idx_task_repeat_type'
);
SET @idx_task_repeat_type_sql = IF(
    @idx_task_repeat_type_exists = 0,
    'CREATE INDEX idx_task_repeat_type ON task(repeat_type)',
    'SELECT 1'
);
PREPARE idx_task_repeat_type_stmt FROM @idx_task_repeat_type_sql;
EXECUTE idx_task_repeat_type_stmt;
DEALLOCATE PREPARE idx_task_repeat_type_stmt;
