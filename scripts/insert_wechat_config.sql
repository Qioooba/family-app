-- 插入企业微信配置到数据库
-- 从代码中找到的配置信息

INSERT INTO sys_config (config_key, config_value, description, category) VALUES
('wechat.work.corpid', 'ww6c1c7590db91ef85', '企业微信CorpID', 'wechat_work'),
('wechat.work.agentid', '1000002', '企业微信应用AgentID', 'wechat_work'),
('wechat.work.secret', 'Ne0oN5Y8mNmRA_wkIP7I4PMn_sr2GFPkbBABqUaEEE4', '企业微信应用Secret', 'wechat_work'),
('wechat.work.userid', 'XIAOXHUSHOU', '企业微信小助手UserID', 'wechat_work')
ON DUPLICATE KEY UPDATE 
    config_value = VALUES(config_value),
    update_time = NOW();

-- 查看配置是否插入成功
SELECT * FROM sys_config WHERE category = 'wechat_work';
