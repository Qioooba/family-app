-- V13__add_system_config_table.sql
-- 创建系统配置表，存储企业微信等敏感配置

CREATE TABLE IF NOT EXISTS sys_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    config_key VARCHAR(100) NOT NULL COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    description VARCHAR(255) COMMENT '配置说明',
    category VARCHAR(50) DEFAULT 'general' COMMENT '配置分类：general/wechat/work等',
    is_encrypted TINYINT DEFAULT 0 COMMENT '是否加密存储 0否 1是',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_config_key (config_key),
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 插入企业微信配置（默认空值，需要管理员配置）
INSERT INTO sys_config (config_key, config_value, description, category) VALUES
('wechat.work.corpid', '', '企业微信CorpID', 'wechat_work'),
('wechat.work.agentid', '', '企业微信应用AgentID', 'wechat_work'),
('wechat.work.secret', '', '企业微信应用Secret', 'wechat_work'),
('wechat.work.userid', 'XIAOXHUSHOU', '企业微信小助手UserID', 'wechat_work'),
('wechat.appid', 'wxbdc70536c5e52b82', '微信小程序AppID', 'wechat_miniapp'),
('wechat.appsecret', '', '微信小程序AppSecret', 'wechat_miniapp');
