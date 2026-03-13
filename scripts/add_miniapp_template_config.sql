-- 添加小程序订阅消息配置
INSERT INTO sys_config (config_key, config_value, description, category) VALUES
('wechat.miniapp.template_id.task_assigned', '', '任务指派通知模板ID', 'wechat_miniapp'),
('wechat.miniapp.template_id.task_completed', '', '任务完成通知模板ID', 'wechat_miniapp')
ON DUPLICATE KEY UPDATE config_value = VALUES(config_value);
