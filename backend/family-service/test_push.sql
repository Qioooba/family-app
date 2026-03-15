INSERT INTO reminder_config (
    reminder_name, reminder_type, create_type, create_by, push_scope,
    frequency_type, frequency_config, remind_time,
    title_template, content_template, next_execute_time, status
) VALUES (
    '⏰ 功能测试',
    'SYSTEM', 1, 7, 'SELF',
    'ONCE', '{"fixedDate":"2026-03-15","fixedTime":"09:22"}',
    '09:22',
    '⏰ 提醒功能测试',
    '这是测试推送消息，验证图文消息格式',
    DATE_ADD(NOW(), INTERVAL 2 MINUTE),
    1
);
