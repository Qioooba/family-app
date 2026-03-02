-- 添加真实姓名字段
-- sys_user 表
ALTER TABLE sys_user ADD COLUMN real_name VARCHAR(50) DEFAULT NULL COMMENT '真实姓名' AFTER phone;
ALTER TABLE sys_user ADD COLUMN nickname VARCHAR(50) DEFAULT NULL COMMENT '昵称' AFTER real_name;

-- family_member 表
ALTER TABLE family_member ADD COLUMN real_name VARCHAR(50) DEFAULT NULL COMMENT '家庭成员真实姓名' AFTER nickname;

-- 迁移现有数据
-- 1. 将现有nickname复制到real_name
UPDATE sys_user SET real_name = nickname WHERE real_name IS NULL AND nickname IS NOT NULL;

-- 2. 如果nickname为空但real_name有值，将real_name复制到nickname
UPDATE sys_user SET nickname = real_name WHERE nickname IS NULL AND real_name IS NOT NULL;

-- 3. 如果两者都为空，使用username
UPDATE sys_user SET real_name = username WHERE real_name IS NULL;

-- 4. 同步到family_member表
UPDATE family_member fm
JOIN sys_user u ON fm.user_id = u.id
SET fm.real_name = u.real_name
WHERE fm.real_name IS NULL;
