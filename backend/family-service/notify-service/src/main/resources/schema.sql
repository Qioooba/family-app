-- =============================================
-- 通知服务数据库表结构
-- =============================================

-- 通知表
CREATE TABLE IF NOT EXISTS `notification` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` bigint(20) NOT NULL COMMENT '接收用户ID',
    `sender_id` bigint(20) DEFAULT '0' COMMENT '发送用户ID（系统发送为0）',
    `type` tinyint(4) NOT NULL COMMENT '通知类型：1-系统通知 2-家庭公告 3-提醒 4-活动通知 5-任务通知',
    `channel` tinyint(4) NOT NULL COMMENT '通知渠道：1-站内信 2-邮件 3-短信 4-微信模板消息',
    `title` varchar(200) NOT NULL COMMENT '通知标题',
    `content` text COMMENT '通知内容',
    `biz_type` varchar(50) DEFAULT NULL COMMENT '关联业务类型',
    `biz_id` bigint(20) DEFAULT NULL COMMENT '关联业务ID',
    `is_read` tinyint(4) DEFAULT '0' COMMENT '是否已读：0-未读 1-已读',
    `read_time` datetime DEFAULT NULL COMMENT '读取时间',
    `send_time` datetime DEFAULT NULL COMMENT '发送时间',
    `scheduled_time` datetime DEFAULT NULL COMMENT '计划发送时间（定时发送）',
    `send_status` tinyint(4) DEFAULT '1' COMMENT '发送状态：0-待发送 1-已发送 2-发送失败',
    `fail_reason` varchar(500) DEFAULT NULL COMMENT '失败原因',
    `family_id` bigint(20) DEFAULT NULL COMMENT '家庭ID',
    `extra_data` text COMMENT '扩展数据（JSON格式）',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `status` tinyint(4) DEFAULT '1' COMMENT '状态：0-禁用 1-启用',
    `deleted` tinyint(4) DEFAULT '0' COMMENT '是否删除：0-否 1-是',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_type` (`type`),
    KEY `idx_is_read` (`is_read`),
    KEY `idx_send_status` (`send_status`),
    KEY `idx_family_id` (`family_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';

-- 通知模板表
CREATE TABLE IF NOT EXISTS `notification_template` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `code` varchar(50) NOT NULL COMMENT '模板编码',
    `name` varchar(100) NOT NULL COMMENT '模板名称',
    `type` tinyint(4) NOT NULL COMMENT '模板类型：1-系统通知 2-家庭公告 3-提醒 4-活动通知 5-任务通知',
    `channels` varchar(50) DEFAULT '1' COMMENT '适用渠道：1-站内信 2-邮件 3-短信 4-微信模板消息（多个用逗号分隔）',
    `title` varchar(200) NOT NULL COMMENT '模板标题',
    `content` text NOT NULL COMMENT '模板内容',
    `email_content` text COMMENT '邮件模板内容（HTML）',
    `sms_content` varchar(500) DEFAULT NULL COMMENT '短信模板内容',
    `wx_template_id` varchar(100) DEFAULT NULL COMMENT '微信模板ID',
    `wx_template_data` text COMMENT '微信模板数据（JSON格式）',
    `variables` text COMMENT '模板变量说明（JSON格式）',
    `is_system` tinyint(4) DEFAULT '0' COMMENT '是否系统模板：0-否 1-是',
    `create_user_id` bigint(20) DEFAULT '0' COMMENT '创建用户ID',
    `description` varchar(500) DEFAULT NULL COMMENT '描述说明',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `status` tinyint(4) DEFAULT '1' COMMENT '状态：0-禁用 1-启用',
    `deleted` tinyint(4) DEFAULT '0' COMMENT '是否删除：0-否 1-是',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    KEY `idx_type` (`type`),
    KEY `idx_is_system` (`is_system`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知模板表';

-- 通知设置表
CREATE TABLE IF NOT EXISTS `notification_setting` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` bigint(20) NOT NULL COMMENT '用户ID',
    `enable_in_app` tinyint(4) DEFAULT '1' COMMENT '是否开启站内信通知：0-关闭 1-开启',
    `enable_email` tinyint(4) DEFAULT '0' COMMENT '是否开启邮件通知：0-关闭 1-开启',
    `enable_sms` tinyint(4) DEFAULT '0' COMMENT '是否开启短信通知：0-关闭 1-开启',
    `enable_wx` tinyint(4) DEFAULT '1' COMMENT '是否开启微信通知：0-关闭 1-开启',
    `notify_system` tinyint(4) DEFAULT '1' COMMENT '系统通知设置：0-关闭 1-开启',
    `notify_announcement` tinyint(4) DEFAULT '1' COMMENT '家庭公告设置：0-关闭 1-开启',
    `notify_reminder` tinyint(4) DEFAULT '1' COMMENT '提醒通知设置：0-关闭 1-开启',
    `notify_activity` tinyint(4) DEFAULT '1' COMMENT '活动通知设置：0-关闭 1-开启',
    `notify_task` tinyint(4) DEFAULT '1' COMMENT '任务通知设置：0-关闭 1-开启',
    `quiet_start_time` varchar(10) DEFAULT '22:00' COMMENT '免打扰开始时间（HH:mm格式）',
    `quiet_end_time` varchar(10) DEFAULT '08:00' COMMENT '免打扰结束时间（HH:mm格式）',
    `enable_quiet` tinyint(4) DEFAULT '0' COMMENT '是否开启免打扰：0-关闭 1-开启',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `status` tinyint(4) DEFAULT '1' COMMENT '状态：0-禁用 1-启用',
    `deleted` tinyint(4) DEFAULT '0' COMMENT '是否删除：0-否 1-是',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    KEY `idx_enable_quiet` (`enable_quiet`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知设置表';
