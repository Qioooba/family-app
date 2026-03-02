-- 删除 real_name 字段（如果存在）
ALTER TABLE sys_user DROP COLUMN IF EXISTS real_name;
ALTER TABLE family_member DROP COLUMN IF EXISTS real_name;

-- 确保 nickname 不为空
UPDATE sys_user SET nickname = username WHERE nickname IS NULL OR nickname = '';
UPDATE family_member SET nickname = '家人' WHERE nickname IS NULL OR nickname = '';
