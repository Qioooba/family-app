-- 更新齐军和陶陶的企业微信内部成员ID

UPDATE sys_user 
SET work_user_id = 'QiJun' 
WHERE nickname = '齐军' OR username LIKE '%qi%jun%' OR id = 1;

UPDATE sys_user 
SET work_user_id = 'TaoTao' 
WHERE nickname = '陶陶' OR username LIKE '%tao%' OR id = 2;

-- 查看更新结果
SELECT id, username, nickname, work_user_id, external_user_id 
FROM sys_user 
WHERE work_user_id IS NOT NULL;
