-- 创建系统配置表并插入企业微信配置

CREATE TABLE IF NOT EXISTS sys_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    config_key VARCHAR(100) NOT NULL COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    description VARCHAR(255) COMMENT '配置说明',
    category VARCHAR(50) DEFAULT 'general' COMMENT '配置分类',
    is_encrypted TINYINT DEFAULT 0 COMMENT '是否加密存储',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_config_key (config_key),
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 插入/更新企业微信配置
INSERT INTO sys_config (config_key, config_value, description, category) VALUES
('wechat.work.corpid', '', '企业微信CorpID', 'wechat_work'),
('wechat.work.agentid', '', '企业微信应用AgentID', 'wechat_work'),
('wechat.work.secret', '', '企业微信应用Secret', 'wechat_work'),
('wechat.work.userid', '', '企业微信小助手UserID', 'wechat_work')
ON DUPLICATE KEY UPDATE 
    config_value = VALUES(config_value),
    description = VALUES(description),
    category = VALUES(category),
    update_time = NOW();

-- 查看配置
SELECT * FROM sys_config WHERE category = 'wechat_work';
