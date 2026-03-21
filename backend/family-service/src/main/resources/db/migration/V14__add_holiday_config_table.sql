-- 节假日配置表
CREATE TABLE IF NOT EXISTS `holiday_config` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `holiday_date` DATE NOT NULL COMMENT '节假日日期',
  `holiday_name` VARCHAR(50) NOT NULL COMMENT '节假日名称',
  `holiday_type` VARCHAR(20) NOT NULL COMMENT '类型: HOLIDAY-节假日, WORKDAY-工作日',
  `year` INT NOT NULL COMMENT '年份',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_holiday_date` (`holiday_date`),
  KEY `idx_year` (`year`),
  KEY `idx_holiday_type` (`holiday_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='节假日配置表';
