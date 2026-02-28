-- ============================================
-- 家庭配置变更脚本
-- 将默认家庭ID从4改为1，名称改为"幸福小家"
-- ============================================

USE family_app;

-- 开始事务
START TRANSACTION;

-- 1. 检查是否存在id=1的家庭，如果存在则删除相关数据
DELETE FROM family_member WHERE family_id = 1;
DELETE FROM family WHERE id = 1;

-- 2. 更新id=4的家庭为id=1，并设置名称为"幸福小家"
UPDATE family SET id = 1, name = '幸福小家' WHERE id = 4;

-- 3. 更新 family_member 表：将 family_id 从 4 改为 1
UPDATE family_member SET family_id = 1 WHERE family_id = 4;

-- 4. 更新 sys_user 表：将 current_family_id 从 4 改为 1
UPDATE sys_user SET current_family_id = 1 WHERE current_family_id = 4;

-- 5. 更新其他相关表的 family_id（使用条件判断表是否存在）
SET @tbl_exists = (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'family_app' AND table_name = 'task');
SET @sql = IF(@tbl_exists > 0, 'UPDATE task SET family_id = 1 WHERE family_id = 4', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @tbl_exists = (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'family_app' AND table_name = 'wish');
SET @sql = IF(@tbl_exists > 0, 'UPDATE wish SET family_id = 1 WHERE family_id = 4', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @tbl_exists = (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'family_app' AND table_name = 'recipe');
SET @sql = IF(@tbl_exists > 0, 'UPDATE recipe SET family_id = 1 WHERE family_id = 4', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @tbl_exists = (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'family_app' AND table_name = 'ingredient');
SET @sql = IF(@tbl_exists > 0, 'UPDATE ingredient SET family_id = 1 WHERE family_id = 4', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @tbl_exists = (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'family_app' AND table_name = 'diet_record');
SET @sql = IF(@tbl_exists > 0, 'UPDATE diet_record SET family_id = 1 WHERE family_id = 4', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @tbl_exists = (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'family_app' AND table_name = 'anniversary');
SET @sql = IF(@tbl_exists > 0, 'UPDATE anniversary SET family_id = 1 WHERE family_id = 4', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @tbl_exists = (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'family_app' AND table_name = 'vote');
SET @sql = IF(@tbl_exists > 0, 'UPDATE vote SET family_id = 1 WHERE family_id = 4', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @tbl_exists = (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'family_app' AND table_name = 'family_album');
SET @sql = IF(@tbl_exists > 0, 'UPDATE family_album SET family_id = 1 WHERE family_id = 4', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @tbl_exists = (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'family_app' AND table_name = 'photo');
SET @sql = IF(@tbl_exists > 0, 'UPDATE photo SET family_id = 1 WHERE family_id = 4', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @tbl_exists = (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'family_app' AND table_name = 'family_file');
SET @sql = IF(@tbl_exists > 0, 'UPDATE family_file SET family_id = 1 WHERE family_id = 4', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @tbl_exists = (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'family_app' AND table_name = 'shopping_list');
SET @sql = IF(@tbl_exists > 0, 'UPDATE shopping_list SET family_id = 1 WHERE family_id = 4', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @tbl_exists = (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'family_app' AND table_name = 'inventory');
SET @sql = IF(@tbl_exists > 0, 'UPDATE inventory SET family_id = 1 WHERE family_id = 4', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @tbl_exists = (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'family_app' AND table_name = 'game_wheel');
SET @sql = IF(@tbl_exists > 0, 'UPDATE game_wheel SET family_id = 1 WHERE family_id = 4', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @tbl_exists = (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'family_app' AND table_name = 'coupon');
SET @sql = IF(@tbl_exists > 0, 'UPDATE coupon SET family_id = 1 WHERE family_id = 4', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @tbl_exists = (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'family_app' AND table_name = 'ai_chat_history');
SET @sql = IF(@tbl_exists > 0, 'UPDATE ai_chat_history SET family_id = 1 WHERE family_id = 4', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @tbl_exists = (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'family_app' AND table_name = 'moment');
SET @sql = IF(@tbl_exists > 0, 'UPDATE moment SET family_id = 1 WHERE family_id = 4', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 提交事务
COMMIT;

-- 验证结果
SELECT '家庭信息' as check_item, id, name FROM family WHERE id = 1;
SELECT '家庭成员数' as check_item, COUNT(*) as count FROM family_member WHERE family_id = 1;
SELECT '用户默认家庭数' as check_item, COUNT(*) as count FROM sys_user WHERE current_family_id = 1;
