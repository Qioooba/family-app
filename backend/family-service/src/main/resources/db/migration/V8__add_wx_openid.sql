-- 添加微信OpenID字段
ALTER TABLE sys_user ADD COLUMN wx_openid VARCHAR(100) DEFAULT NULL COMMENT '微信OpenID' AFTER phone;
