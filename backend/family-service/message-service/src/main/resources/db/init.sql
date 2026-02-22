-- 消息服务数据库初始化脚本

-- 消息表
CREATE TABLE IF NOT EXISTS `message` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `title` varchar(200) NOT NULL COMMENT '消息标题',
    `content` text COMMENT '消息内容',
    `type` tinyint(4) NOT NULL DEFAULT '1' COMMENT '消息类型：1-系统通知 2-家庭公告 3-@我的 4-私信',
    `sender_id` bigint(20) DEFAULT '0' COMMENT '发送者ID',
    `sender_name` varchar(100) DEFAULT NULL COMMENT '发送者名称',
    `sender_avatar` varchar(500) DEFAULT NULL COMMENT '发送者头像',
    `receiver_id` bigint(20) DEFAULT '0' COMMENT '接收者ID(0表示全体用户)',
    `family_id` bigint(20) DEFAULT NULL COMMENT '家庭组ID',
    `biz_type` varchar(50) DEFAULT NULL COMMENT '业务类型',
    `biz_id` bigint(20) DEFAULT NULL COMMENT '业务ID',
    `template_id` bigint(20) DEFAULT NULL COMMENT '模板ID',
    `template_params` json DEFAULT NULL COMMENT '模板参数',
    `is_read` tinyint(1) DEFAULT '0' COMMENT '是否已读：0-未读 1-已读',
    `priority` tinyint(4) DEFAULT '2' COMMENT '优先级：1-低 2-中 3-高',
    `expire_time` datetime DEFAULT NULL COMMENT '过期时间',
    `extra_data` json DEFAULT NULL COMMENT '额外数据',
    `status` tinyint(1) DEFAULT '1' COMMENT '状态：0-禁用 1-启用',
    `deleted` tinyint(1) DEFAULT '0' COMMENT '是否删除：0-未删除 1-已删除',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_receiver_id` (`receiver_id`),
    KEY `idx_type` (`type`),
    KEY `idx_sender_id` (`sender_id`),
    KEY `idx_family_id` (`family_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息表';

-- 消息模板表
CREATE TABLE IF NOT EXISTS `message_template` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `code` varchar(100) NOT NULL COMMENT '模板编码',
    `name` varchar(200) NOT NULL COMMENT '模板名称',
    `type` tinyint(4) NOT NULL DEFAULT '1' COMMENT '模板类型：1-系统 2-家庭 3-私信',
    `title_template` varchar(500) DEFAULT NULL COMMENT '消息标题模板',
    `content_template` text COMMENT '消息内容模板',
    `params` json DEFAULT NULL COMMENT '参数定义',
    `biz_type` varchar(50) DEFAULT NULL COMMENT '业务类型',
    `is_enabled` tinyint(1) DEFAULT '1' COMMENT '是否启用：0-禁用 1-启用',
    `description` varchar(500) DEFAULT NULL COMMENT '描述',
    `status` tinyint(1) DEFAULT '1' COMMENT '状态',
    `deleted` tinyint(1) DEFAULT '0' COMMENT '是否删除',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    KEY `idx_type` (`type`),
    KEY `idx_biz_type` (`biz_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息模板表';

-- 消息已读状态表(用于广播消息的已读追踪)
CREATE TABLE IF NOT EXISTS `message_read_status` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `message_id` bigint(20) NOT NULL COMMENT '消息ID',
    `user_id` bigint(20) NOT NULL COMMENT '用户ID',
    `is_read` tinyint(1) DEFAULT '0' COMMENT '是否已读',
    `first_read_time` datetime DEFAULT NULL COMMENT '首次阅读时间',
    `last_read_time` datetime DEFAULT NULL COMMENT '最后阅读时间',
    `status` tinyint(1) DEFAULT '1' COMMENT '状态',
    `deleted` tinyint(1) DEFAULT '0' COMMENT '是否删除',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_message_user` (`message_id`, `user_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_message_id` (`message_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息已读状态表';

-- 插入默认消息模板
INSERT INTO `message_template` (`code`, `name`, `type`, `title_template`, `content_template`, `params`, `biz_type`, `is_enabled`, `description`) VALUES
('WELCOME', '欢迎消息', 1, '欢迎来到家庭小程序', '亲爱的{username}，欢迎加入家庭小程序！', '["username"]', 'system', 1, '用户注册后的欢迎消息'),
('FAMILY_INVITE', '家庭邀请', 2, '{inviter}邀请你加入家庭', '{inviter}邀请你加入"{familyName}"家庭，点击查看详情。', '["inviter", "familyName"]', 'family', 1, '家庭邀请通知'),
('TASK_REMIND', '任务提醒', 3, '任务提醒：{taskName}', '你有一个任务"{taskName}"即将到期，请及时处理。', '["taskName", "dueTime"]', 'task', 1, '任务到期提醒'),
('SYSTEM_NOTICE', '系统公告', 1, '{title}', '{content}', '["title", "content"]', 'system', 1, '系统公告模板'),
('MENTION', '提到我', 3, '{username}提到了你', '{username}在{location}提到了你：{content}', '["username", "location", "content"]', 'mention', 1, '@提醒模板');
