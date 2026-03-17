mysqldump: [Warning] Using a password on the command line interface can be insecure.
Warning: A partial dump from a server that has GTIDs will by default include the GTIDs of all transactions, even those that changed suppressed parts of the database. If you don't want to restore GTIDs, pass --set-gtid-purged=OFF. To make a complete dump, pass --all-databases --triggers --routines --events. 
Warning: A dump from a server that has GTIDs enabled will by default include the GTIDs of all transactions, even those that were executed during its extraction and might not be represented in the dumped data. This might result in an inconsistent data dump. 
In order to ensure a consistent backup of the database, pass --single-transaction or --lock-all-tables or --source-data. 
-- MySQL dump 10.13  Distrib 9.6.0, for macos15.7 (arm64)
--
-- Host: localhost    Database: family_app
-- ------------------------------------------------------
-- Server version	9.6.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
SET @MYSQLDUMP_TEMP_LOG_BIN = @@SESSION.SQL_LOG_BIN;
SET @@SESSION.SQL_LOG_BIN= 0;

--
-- GTID state at the beginning of the backup 
--

SET @@GLOBAL.GTID_PURGED=/*!80000 '+'*/ '13d5f426-103a-11f1-ba56-2d6b939d5cf3:1-1579';

--
-- Table structure for table `album_photo`
--

DROP TABLE IF EXISTS `album_photo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `album_photo` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `family_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `album_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `photo_url` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL,
  `thumbnail_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `location` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `tags` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `latitude` double DEFAULT NULL,
  `longitude` double DEFAULT NULL,
  `taken_at` datetime DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_family_id` (`family_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `calendar_anniversary`
--

DROP TABLE IF EXISTS `calendar_anniversary`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `calendar_anniversary` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `family_id` bigint NOT NULL,
  `title` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `type` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `date_type` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `target_date` date DEFAULT NULL,
  `is_recurring` tinyint DEFAULT '0',
  `reminder_days` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `related_user_id` bigint DEFAULT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `images` text COLLATE utf8mb4_unicode_ci,
  `icon` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0' COMMENT 'æ˜¯å¦åˆ é™¤(0-å¦ 1-æ˜¯)',
  PRIMARY KEY (`id`),
  KEY `idx_family_id` (`family_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `challenge_checkin`
--

DROP TABLE IF EXISTS `challenge_checkin`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `challenge_checkin` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `challenge_id` bigint NOT NULL COMMENT 'æŒ‘æˆ˜ID',
  `participant_id` bigint NOT NULL COMMENT 'å‚ä¸Žè€…ID',
  `user_id` bigint NOT NULL COMMENT 'ç”¨æˆ·ID',
  `checkin_date` date DEFAULT NULL COMMENT 'æ‰“å¡æ—¥æœŸ',
  `note` varchar(500) DEFAULT NULL COMMENT 'å¤‡æ³¨',
  `image` varchar(500) DEFAULT NULL COMMENT 'å›¾ç‰‡',
  `value` decimal(10,2) DEFAULT NULL COMMENT 'æ‰“å¡å€¼',
  `status` tinyint DEFAULT '1' COMMENT 'çŠ¶æ€ 1-æ­£å¸¸ 0-å–æ¶ˆ',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  `is_deleted` tinyint DEFAULT '0' COMMENT 'æ˜¯å¦åˆ é™¤',
  PRIMARY KEY (`id`),
  KEY `idx_challenge_id` (`challenge_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_checkin_date` (`checkin_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='æ‰“å¡è®°å½•è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `challenge_participant`
--

DROP TABLE IF EXISTS `challenge_participant`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `challenge_participant` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `challenge_id` bigint NOT NULL COMMENT 'æŒ‘æˆ˜ID',
  `family_id` bigint NOT NULL COMMENT 'å®¶åº­ID',
  `user_id` bigint NOT NULL COMMENT 'ç”¨æˆ·ID',
  `current_value` decimal(10,2) DEFAULT '0.00' COMMENT 'å½“å‰å€¼',
  `progress` int DEFAULT '0' COMMENT 'è¿›åº¦ç™¾åˆ†æ¯”',
  `checkin_count` int DEFAULT '0' COMMENT 'æ‰“å¡æ¬¡æ•°',
  `last_checkin_date` date DEFAULT NULL COMMENT 'æœ€åŽæ‰“å¡æ—¥æœŸ',
  `is_completed` tinyint DEFAULT '0' COMMENT 'æ˜¯å¦å®Œæˆ 0-å¦ 1-æ˜¯',
  `status` tinyint DEFAULT '1' COMMENT 'çŠ¶æ€ 1-æ­£å¸¸ 0-é€€å‡º',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  `is_deleted` tinyint DEFAULT '0' COMMENT 'æ˜¯å¦åˆ é™¤',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_challenge_user` (`challenge_id`,`user_id`),
  KEY `idx_challenge_id` (`challenge_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='æŒ‘æˆ˜å‚ä¸Žè€…è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `diet_record`
--

DROP TABLE IF EXISTS `diet_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `diet_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `family_id` bigint DEFAULT NULL,
  `meal_type` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `food_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `calories` int DEFAULT '0',
  `protein` double DEFAULT '0',
  `carbs` double DEFAULT '0',
  `fat` double DEFAULT '0',
  `quantity` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `unit` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `record_date` date DEFAULT NULL,
  `record_time` datetime DEFAULT NULL,
  `image_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_record_date` (`record_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `family`
--

DROP TABLE IF EXISTS `family`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `family` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `avatar` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `creator_id` bigint DEFAULT NULL,
  `invite_code` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `member_count` int DEFAULT '0',
  `storage_used` bigint DEFAULT '0',
  `storage_limit` bigint DEFAULT '10737418240',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `family_challenge`
--

DROP TABLE IF EXISTS `family_challenge`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `family_challenge` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `family_id` bigint NOT NULL COMMENT 'å®¶åº­ID',
  `title` varchar(200) NOT NULL COMMENT 'æ ‡é¢˜',
  `description` varchar(1000) DEFAULT NULL COMMENT 'æè¿°',
  `type` int DEFAULT '1' COMMENT 'ç±»åž‹ 1-å¥åº·æŒ‘æˆ˜ 2-ä»»åŠ¡æŒ‘æˆ˜ 3-ä¹ æƒ¯æŒ‘æˆ˜',
  `target_type` varchar(50) DEFAULT NULL COMMENT 'ç›®æ ‡ç±»åž‹',
  `target_value` decimal(10,2) DEFAULT NULL COMMENT 'ç›®æ ‡å€¼',
  `start_date` date DEFAULT NULL COMMENT 'å¼€å§‹æ—¥æœŸ',
  `end_date` date DEFAULT NULL COMMENT 'ç»“æŸæ—¥æœŸ',
  `creator_id` bigint DEFAULT NULL COMMENT 'åˆ›å»ºè€…ID',
  `participant_count` int DEFAULT '0' COMMENT 'å‚ä¸Žäººæ•°',
  `status` tinyint DEFAULT '1' COMMENT 'çŠ¶æ€ 1-æ­£å¸¸ 0-ç¦ç”¨',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  `is_deleted` tinyint DEFAULT '0' COMMENT 'æ˜¯å¦åˆ é™¤',
  PRIMARY KEY (`id`),
  KEY `idx_family_id` (`family_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='å®¶åº­æŒ‘æˆ˜è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `family_invite_code`
--

DROP TABLE IF EXISTS `family_invite_code`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `family_invite_code` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'é‚€è¯·ç ID',
  `family_id` bigint NOT NULL COMMENT 'å®¶åº­ID',
  `code` varchar(20) NOT NULL COMMENT 'é‚€è¯·ç ',
  `creator_id` bigint NOT NULL COMMENT 'åˆ›å»ºè€…ID',
  `max_uses` int DEFAULT '1' COMMENT 'æœ€å¤§ä½¿ç”¨æ¬¡æ•°',
  `used_count` int DEFAULT '0' COMMENT 'å·²ä½¿ç”¨æ¬¡æ•°',
  `expires_at` datetime DEFAULT NULL COMMENT 'è¿‡æœŸæ—¶é—´',
  `status` tinyint DEFAULT '1' COMMENT 'çŠ¶æ€: 1-æœ‰æ•ˆ, 0-æ— æ•ˆ',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_family_id` (`family_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='å®¶åº­é‚€è¯·ç è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `family_member`
--

DROP TABLE IF EXISTS `family_member`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `family_member` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `family_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `role` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'member',
  `nickname` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `join_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_family_user` (`family_id`,`user_id`),
  KEY `idx_member_user_id` (`user_id`),
  KEY `idx_member_family_id` (`family_id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `game_quiz`
--

DROP TABLE IF EXISTS `game_quiz`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `game_quiz` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `question` varchar(500) NOT NULL COMMENT 'é—®é¢˜',
  `option_a` varchar(200) DEFAULT NULL COMMENT 'é€‰é¡¹A',
  `option_b` varchar(200) DEFAULT NULL COMMENT 'é€‰é¡¹B',
  `option_c` varchar(200) DEFAULT NULL COMMENT 'é€‰é¡¹C',
  `option_d` varchar(200) DEFAULT NULL COMMENT 'é€‰é¡¹D',
  `correct_answer` varchar(10) NOT NULL COMMENT 'æ­£ç¡®ç­”æ¡ˆ A/B/C/D',
  `category` varchar(50) DEFAULT NULL COMMENT 'åˆ†ç±»',
  `difficulty` int DEFAULT '1' COMMENT 'éš¾åº¦ 1-5',
  `explanation` varchar(500) DEFAULT NULL COMMENT 'è§£é‡Š',
  `play_count` int DEFAULT '0' COMMENT 'æ¸¸çŽ©æ¬¡æ•°',
  `correct_count` int DEFAULT '0' COMMENT 'ç­”å¯¹æ¬¡æ•°',
  `status` tinyint DEFAULT '1' COMMENT 'çŠ¶æ€ 1-æ­£å¸¸ 0-ç¦ç”¨',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  `is_deleted` tinyint DEFAULT '0' COMMENT 'æ˜¯å¦åˆ é™¤',
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ç­”é¢˜è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `game_riddle`
--

DROP TABLE IF EXISTS `game_riddle`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `game_riddle` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `question` varchar(500) NOT NULL COMMENT 'è°œé¢',
  `answer` varchar(200) NOT NULL COMMENT 'è°œåº•',
  `hint` varchar(300) DEFAULT NULL COMMENT 'æç¤º',
  `category` varchar(50) DEFAULT NULL COMMENT 'åˆ†ç±»',
  `difficulty` int DEFAULT '1' COMMENT 'éš¾åº¦ 1-5',
  `explanation` varchar(500) DEFAULT NULL COMMENT 'è§£é‡Š',
  `play_count` int DEFAULT '0' COMMENT 'æ¸¸çŽ©æ¬¡æ•°',
  `correct_count` int DEFAULT '0' COMMENT 'ç­”å¯¹æ¬¡æ•°',
  `status` tinyint DEFAULT '1' COMMENT 'çŠ¶æ€ 1-æ­£å¸¸ 0-ç¦ç”¨',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  `is_deleted` tinyint DEFAULT '0' COMMENT 'æ˜¯å¦åˆ é™¤',
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='è°œè¯­è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `health_record`
--

DROP TABLE IF EXISTS `health_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `health_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `family_id` bigint DEFAULT NULL,
  `record_type` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `value` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `unit` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `record_date` date DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `holiday_config`
--

DROP TABLE IF EXISTS `holiday_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `holiday_config` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `holiday_date` date NOT NULL COMMENT 'èŠ‚å‡æ—¥æ—¥æœŸ',
  `holiday_name` varchar(50) DEFAULT NULL COMMENT 'èŠ‚å‡æ—¥åç§°',
  `holiday_type` varchar(20) DEFAULT NULL COMMENT 'HOLIDAY-èŠ‚å‡æ—¥ WORKDAY-è°ƒä¼‘å·¥ä½œæ—¥',
  `year` int DEFAULT NULL COMMENT 'å¹´ä»½',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_date` (`holiday_date`),
  KEY `idx_year` (`year`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='èŠ‚å‡æ—¥é…ç½®è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ingredient`
--

DROP TABLE IF EXISTS `ingredient`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ingredient` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `family_id` bigint DEFAULT NULL COMMENT 'å®¶åº­ID',
  `name` varchar(200) NOT NULL COMMENT 'é£Ÿæåç§°',
  `category` varchar(100) DEFAULT NULL COMMENT 'åˆ†ç±»',
  `quantity` decimal(10,2) DEFAULT NULL COMMENT 'æ•°é‡',
  `unit` varchar(50) DEFAULT NULL COMMENT 'å•ä½',
  `storage_location` varchar(100) DEFAULT NULL COMMENT 'å­˜æ”¾ä½ç½®',
  `purchase_date` date DEFAULT NULL COMMENT 'è´­ä¹°æ—¥æœŸ',
  `expire_date` date DEFAULT NULL COMMENT 'è¿‡æœŸæ—¥æœŸ',
  `reminder_days` int DEFAULT '3' COMMENT 'æé†’å¤©æ•°',
  `status` tinyint DEFAULT '0' COMMENT 'çŠ¶æ€ 0-æ­£å¸¸ 1-å·²è¿‡æœŸ 2-å¿«è¿‡æœŸ',
  `image` varchar(500) DEFAULT NULL COMMENT 'å›¾ç‰‡URL',
  `recognized_data` text COMMENT 'è¯†åˆ«æ•°æ®',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  `is_deleted` tinyint DEFAULT '0' COMMENT 'æ˜¯å¦åˆ é™¤',
  PRIMARY KEY (`id`),
  KEY `idx_family_id` (`family_id`),
  KEY `idx_expire_date` (`expire_date`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='é£Ÿæè¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `invite_code`
--

DROP TABLE IF EXISTS `invite_code`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `invite_code` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `family_id` bigint NOT NULL,
  `creator_id` bigint NOT NULL,
  `code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `max_uses` int DEFAULT '1',
  `used_count` int DEFAULT '0',
  `expires_at` datetime DEFAULT NULL,
  `status` tinyint DEFAULT '1',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `points_transaction`
--

DROP TABLE IF EXISTS `points_transaction`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `points_transaction` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT 'ç”¨æˆ·ID',
  `points` int NOT NULL COMMENT 'å˜åŠ¨ç§¯åˆ†ï¼ˆæ­£æ•°ä¸ºå¢žåŠ ï¼Œè´Ÿæ•°ä¸ºå‡å°‘ï¼‰',
  `type` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'ç±»åž‹ï¼šearn/spend',
  `source` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'æ¥æº',
  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'æè¿°',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ç§¯åˆ†æµæ°´è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `recipe`
--

DROP TABLE IF EXISTS `recipe`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `recipe` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `family_id` bigint DEFAULT NULL,
  `creator_id` bigint DEFAULT NULL,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `category` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `tags` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ingredients` text COLLATE utf8mb4_unicode_ci,
  `steps` text COLLATE utf8mb4_unicode_ci,
  `image_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `cook_time` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `difficulty` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `source` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `source_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `view_count` int DEFAULT '0',
  `favorite_count` int DEFAULT '0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_family_id` (`family_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `recipe_record`
--

DROP TABLE IF EXISTS `recipe_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `recipe_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `family_id` bigint DEFAULT NULL,
  `recipe_id` bigint DEFAULT NULL,
  `recipe_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `image_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `cook_time` datetime DEFAULT NULL,
  `remark` text COLLATE utf8mb4_unicode_ci,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `reminder_config`
--

DROP TABLE IF EXISTS `reminder_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reminder_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ä¸»é”®ID',
  `reminder_name` varchar(100) NOT NULL COMMENT 'æé†’åç§°ï¼šå¦‚"æ—©ä¸Šå–æ°´"',
  `reminder_type` varchar(30) NOT NULL COMMENT 'ç±»åž‹ï¼šWATER-å–æ°´ MEDICINE-ç”¨è¯ EXPIRE-ä¿è´¨æœŸ BIRTHDAY-ç”Ÿæ—¥ FINANCE-è´¢åŠ¡ SYSTEM-ç³»ç»Ÿ SCHEDULE-å®šæ—¶ä»»åŠ¡',
  `create_type` tinyint DEFAULT '1' COMMENT '1-ä¸ªäºº 2-ç³»ç»Ÿ',
  `create_by` bigint NOT NULL COMMENT 'åˆ›å»ºäººID',
  `push_scope` varchar(20) DEFAULT 'SELF' COMMENT 'SELF-ä»…è‡ªå·± SPECIFIED-æŒ‡å®šç”¨æˆ· ALL-å…¨éƒ¨',
  `target_user_ids` json DEFAULT NULL COMMENT 'ç›®æ ‡ç”¨æˆ·IDåˆ—è¡¨',
  `cron_expression` varchar(100) DEFAULT NULL COMMENT 'Cronè¡¨è¾¾å¼ï¼šå¦‚ 0 0 8 * * ?ï¼ˆæ¯å¤©8ç‚¹ï¼‰',
  `frequency_type` varchar(20) DEFAULT NULL COMMENT 'é¢‘çŽ‡ç±»åž‹ï¼šONCE-ä¸€æ¬¡æ€§ DAILY-æ¯å¤© HOURLY-æ¯å°æ—¶ WEEKLY-æ¯å‘¨ MONTHLY-æ¯æœˆ YEARLY-æ¯å¹´ INTERVAL-é—´éš”',
  `frequency_config` json DEFAULT NULL COMMENT 'é¢‘çŽ‡é…ç½®ï¼š',
  `title_template` varchar(200) DEFAULT NULL COMMENT 'æ¶ˆæ¯æ ‡é¢˜æ¨¡æ¿',
  `content_template` text COMMENT 'æ¶ˆæ¯å†…å®¹æ¨¡æ¿',
  `business_data` json DEFAULT NULL COMMENT 'ä¸šåŠ¡æ•°æ®ï¼š',
  `next_execute_time` datetime DEFAULT NULL COMMENT 'ä¸‹æ¬¡æ‰§è¡Œæ—¶é—´ï¼ˆç³»ç»Ÿè‡ªåŠ¨è®¡ç®—ï¼‰',
  `last_execute_time` datetime DEFAULT NULL COMMENT 'ä¸Šæ¬¡æ‰§è¡Œæ—¶é—´',
  `last_execute_result` varchar(20) DEFAULT NULL COMMENT 'SUCCESS/FAILED',
  `execute_count` int DEFAULT '0' COMMENT 'å·²æ‰§è¡Œæ¬¡æ•°',
  `max_execute_count` int DEFAULT '0' COMMENT 'æœ€å¤§æ‰§è¡Œæ¬¡æ•°ï¼ˆ0æ— é™ï¼‰',
  `status` tinyint DEFAULT '1' COMMENT '0-åœç”¨ 1-å¯ç”¨ 2-æš‚åœ',
  `priority` tinyint DEFAULT '5' COMMENT 'ä¼˜å…ˆçº§1-10',
  `quiet_hours_start` varchar(10) DEFAULT NULL COMMENT 'å…æ‰“æ‰°å¼€å§‹æ—¶é—´ï¼š22:00',
  `quiet_hours_end` varchar(10) DEFAULT NULL COMMENT 'å…æ‰“æ‰°ç»“æŸæ—¶é—´ï¼š08:00',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `remind_time` varchar(10) DEFAULT NULL COMMENT 'æé†’æ—¶é—´',
  PRIMARY KEY (`id`),
  KEY `idx_create_by` (`create_by`),
  KEY `idx_status` (`status`),
  KEY `idx_next_execute` (`next_execute_time`),
  KEY `idx_reminder_type` (`reminder_type`),
  KEY `idx_frequency_type` (`frequency_type`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='æé†’é…ç½®è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `reminder_log`
--

DROP TABLE IF EXISTS `reminder_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reminder_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `reminder_id` bigint NOT NULL COMMENT 'æé†’ID',
  `user_id` bigint NOT NULL COMMENT 'æŽ¥æ”¶ç”¨æˆ·ID',
  `execute_time` datetime DEFAULT NULL COMMENT 'è®¡åˆ’æ‰§è¡Œæ—¶é—´',
  `actual_execute_time` datetime DEFAULT NULL COMMENT 'å®žé™…æ‰§è¡Œæ—¶é—´',
  `finish_time` datetime DEFAULT NULL COMMENT 'å®Œæˆæ—¶é—´',
  `title` varchar(200) DEFAULT NULL COMMENT 'å‘é€çš„æ ‡é¢˜',
  `content` text COMMENT 'å‘é€çš„å†…å®¹',
  `push_status` varchar(20) DEFAULT NULL COMMENT 'SUCCESS/FAILED/SKIPPED',
  `push_result` text COMMENT 'æŽ¨é€è¿”å›žç»“æžœ',
  `error_msg` text COMMENT 'é”™è¯¯ä¿¡æ¯',
  `read_status` tinyint DEFAULT '0' COMMENT '0-æœªè¯» 1-å·²è¯»',
  `read_time` datetime DEFAULT NULL COMMENT 'é˜…è¯»æ—¶é—´',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_reminder_id` (`reminder_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_execute_time` (`execute_time`),
  KEY `idx_status` (`push_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='æé†’æ‰§è¡Œæ—¥å¿—è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sys_config`
--

DROP TABLE IF EXISTS `sys_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_config` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `config_key` varchar(100) NOT NULL COMMENT 'é…ç½®é”®',
  `config_value` text COMMENT 'é…ç½®å€¼',
  `description` varchar(255) DEFAULT NULL COMMENT 'é…ç½®è¯´æ˜Ž',
  `category` varchar(50) DEFAULT 'general' COMMENT 'é…ç½®åˆ†ç±»',
  `is_encrypted` tinyint DEFAULT '0' COMMENT 'æ˜¯å¦åŠ å¯†å­˜å‚¨',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`),
  KEY `idx_category` (`category`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ç³»ç»Ÿé…ç½®è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sys_user`
--

DROP TABLE IF EXISTS `sys_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `password` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `nickname` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `avatar` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `wx_openid` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'å¾®ä¿¡OpenID',
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `gender` int DEFAULT '0',
  `last_login_time` datetime DEFAULT NULL,
  `status` int DEFAULT '1',
  `current_family_id` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` int DEFAULT '0',
  `work_user_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'ä¼ä¸šå¾®ä¿¡æˆå‘˜ID',
  `external_user_id` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'ä¼ä¸šå¾®ä¿¡å¤–éƒ¨è”ç³»äººID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `phone` (`phone`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `task`
--

DROP TABLE IF EXISTS `task`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `task` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `family_id` bigint DEFAULT NULL COMMENT 'å®¶åº­ID',
  `category_id` bigint DEFAULT NULL COMMENT 'åˆ†ç±»ID',
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'æ ‡é¢˜',
  `content` text COLLATE utf8mb4_unicode_ci COMMENT 'å†…å®¹',
  `remark` text COLLATE utf8mb4_unicode_ci,
  `priority` int DEFAULT '0' COMMENT 'ä¼˜å…ˆçº§ 0-æ™®é€š 1-é‡è¦ 2-ç´§æ€¥',
  `status` int DEFAULT '0' COMMENT 'çŠ¶æ€ 0-å¾…å®Œæˆ 1-è¿›è¡Œä¸­ 2-å·²å®Œæˆ',
  `assignee_id` bigint DEFAULT NULL COMMENT 'è¢«æŒ‡æ´¾äººID',
  `creator_id` bigint DEFAULT NULL COMMENT 'åˆ›å»ºäººID',
  `due_time` datetime DEFAULT NULL COMMENT 'æˆªæ­¢æ—¶é—´',
  `remind_time` datetime DEFAULT NULL COMMENT 'æé†’æ—¶é—´',
  `repeat_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT 'none' COMMENT 'é‡å¤ç±»åž‹: none-ä¸é‡å¤ daily-æ¯å¤© weekly-æ¯å‘¨ monthly-æ¯æœˆ yearly-æ¯å¹´',
  `repeat_rule` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'é‡å¤è§„åˆ™',
  `location` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'ä½ç½®',
  `parent_id` bigint DEFAULT '0' COMMENT 'çˆ¶ä»»åŠ¡ID',
  `attachments` text COLLATE utf8mb4_unicode_ci COMMENT 'é™„ä»¶JSON',
  `finish_time` datetime DEFAULT NULL COMMENT 'å®Œæˆæ—¶é—´',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  `is_archived` tinyint DEFAULT '0' COMMENT 'æ˜¯å¦å½’æ¡£',
  `archive_time` datetime DEFAULT NULL COMMENT 'å½’æ¡£æ—¶é—´',
  `is_deleted` tinyint DEFAULT '0' COMMENT 'æ˜¯å¦åˆ é™¤',
  `delete_time` datetime DEFAULT NULL COMMENT 'åˆ é™¤æ—¶é—´',
  `sort_order` int DEFAULT '0' COMMENT 'æŽ’åº',
  PRIMARY KEY (`id`),
  KEY `idx_family_id` (`family_id`),
  KEY `idx_status` (`status`),
  KEY `idx_assignee_id` (`assignee_id`),
  KEY `idx_due_time` (`due_time`),
  KEY `idx_task_family_status_due` (`family_id`,`status`,`due_time`),
  KEY `idx_task_assignee_status` (`assignee_id`,`status`),
  KEY `idx_task_create_time` (`create_time` DESC)
) ENGINE=InnoDB AUTO_INCREMENT=102 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ä»»åŠ¡è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `task_reminder`
--

DROP TABLE IF EXISTS `task_reminder`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `task_reminder` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `task_id` bigint NOT NULL COMMENT 'ä»»åŠ¡ID',
  `reminder_type` varchar(50) DEFAULT 'time' COMMENT 'æé†’ç±»åž‹: time-æ—¶é—´æé†’ location-ä½ç½®æé†’',
  `reminder_time` datetime DEFAULT NULL COMMENT 'æé†’æ—¶é—´',
  `location_name` varchar(200) DEFAULT NULL COMMENT 'ä½ç½®åç§°',
  `location_lat` decimal(10,6) DEFAULT NULL COMMENT 'ä½ç½®çº¬åº¦',
  `location_lng` decimal(10,6) DEFAULT NULL COMMENT 'ä½ç½®ç»åº¦',
  `radius` int DEFAULT '100' COMMENT 'ä½ç½®æé†’åŠå¾„(ç±³)',
  `is_triggered` tinyint DEFAULT '0' COMMENT 'æ˜¯å¦å·²è§¦å‘ 0-å¦ 1-æ˜¯',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  `is_deleted` tinyint DEFAULT '0' COMMENT 'æ˜¯å¦åˆ é™¤',
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_reminder_time` (`reminder_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ä»»åŠ¡æé†’è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `nickname` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `avatar` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` tinyint DEFAULT '1',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_points`
--

DROP TABLE IF EXISTS `user_points`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_points` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT 'ç”¨æˆ·ID',
  `total_points` int DEFAULT '0' COMMENT 'æ€»ç§¯åˆ†',
  `available_points` int DEFAULT '0' COMMENT 'å¯ç”¨ç§¯åˆ†',
  `spent_points` int DEFAULT '0' COMMENT 'å·²èŠ±è´¹ç§¯åˆ†',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ç”¨æˆ·ç§¯åˆ†è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_water_goal`
--

DROP TABLE IF EXISTS `user_water_goal`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_water_goal` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `target_amount` int NOT NULL DEFAULT '2000' COMMENT '每日目标饮水量（毫升）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint DEFAULT '0' COMMENT '是否删除(0-否 1-是)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户饮水目标设置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `vote`
--

DROP TABLE IF EXISTS `vote`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vote` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `family_id` bigint NOT NULL,
  `creator_id` bigint NOT NULL,
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `vote_type` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'single',
  `max_select` int DEFAULT '1',
  `is_anonymous` tinyint DEFAULT '0',
  `can_change` tinyint DEFAULT '1',
  `min_votes` int DEFAULT '1',
  `pass_threshold` double DEFAULT '0.5',
  `end_time` datetime DEFAULT NULL,
  `status` tinyint DEFAULT '1',
  `options` text COLLATE utf8mb4_unicode_ci,
  `result` text COLLATE utf8mb4_unicode_ci,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_family_id` (`family_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `vote_record`
--

DROP TABLE IF EXISTS `vote_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vote_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `vote_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `option_ids` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_vote_id` (`vote_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `water_record`
--

DROP TABLE IF EXISTS `water_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `water_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `amount` int DEFAULT '0',
  `record_date` date DEFAULT NULL,
  `record_time` time DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_record_date` (`record_date`),
  KEY `idx_water_user_date` (`user_id`,`record_date`),
  KEY `idx_water_record_time` (`record_time` DESC)
) ENGINE=InnoDB AUTO_INCREMENT=113 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `weight_record`
--

DROP TABLE IF EXISTS `weight_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `weight_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `weight` double NOT NULL,
  `bmi` double DEFAULT NULL,
  `fat_rate` double DEFAULT NULL,
  `record_date` date DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `wish`
--

DROP TABLE IF EXISTS `wish`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wish` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `family_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `type` varchar(20) NOT NULL DEFAULT 'custom',
  `title` varchar(200) NOT NULL,
  `description` text,
  `budget_min` decimal(10,2) DEFAULT NULL,
  `budget_max` decimal(10,2) DEFAULT NULL,
  `expect_date` date DEFAULT NULL,
  `visibility` varchar(20) DEFAULT 'public',
  `priority` int DEFAULT '2',
  `difficulty` int DEFAULT '2',
  `status` int DEFAULT '0',
  `claimant_id` bigint DEFAULT NULL,
  `progress` int DEFAULT '0',
  `images` json DEFAULT NULL,
  `finish_time` datetime DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` int DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_family_id` (`family_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
SET @@SESSION.SQL_LOG_BIN = @MYSQLDUMP_TEMP_LOG_BIN;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-03-16 22:58:04
