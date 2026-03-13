-- V12__add_wechat_work_fields.sql
-- 添加企业微信相关字段

-- 添加企业微信成员ID字段（内部员工）
ALTER TABLE sys_user 
ADD COLUMN IF NOT EXISTS work_user_id VARCHAR(100) COMMENT '企业微信成员ID（内部通讯录）' AFTER union_id;

-- 添加企业微信外部联系人ID字段（客户）
ALTER TABLE sys_user 
ADD COLUMN IF NOT EXISTS external_user_id VARCHAR(100) COMMENT '企业微信外部联系人ID（客户联系）' AFTER work_user_id;

-- 添加索引加速查询
ALTER TABLE sys_user 
ADD INDEX IF NOT EXISTS idx_work_user_id (work_user_id),
ADD INDEX IF NOT EXISTS idx_external_user_id (external_user_id);
