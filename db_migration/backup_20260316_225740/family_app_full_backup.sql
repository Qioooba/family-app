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
-- Dumping data for table `album_photo`
--

LOCK TABLES `album_photo` WRITE;
/*!40000 ALTER TABLE `album_photo` DISABLE KEYS */;
/*!40000 ALTER TABLE `album_photo` ENABLE KEYS */;
UNLOCK TABLES;

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
-- Dumping data for table `calendar_anniversary`
--

LOCK TABLES `calendar_anniversary` WRITE;
/*!40000 ALTER TABLE `calendar_anniversary` DISABLE KEYS */;
INSERT INTO `calendar_anniversary` VALUES (1,1,'哈','birthday',NULL,'2026-05-06',0,NULL,NULL,'哈哈',NULL,NULL,'2026-03-06 13:23:17','2026-03-06 16:04:13',1),(2,1,'摸','family',NULL,'2026-03-06',0,NULL,NULL,'地',NULL,NULL,'2026-03-06 13:23:43','2026-03-06 16:04:01',1),(3,1,'地','birthday',NULL,NULL,0,NULL,NULL,'',NULL,NULL,'2026-03-06 14:05:23','2026-03-06 14:07:00',1),(4,1,'姐姐','birthday',NULL,'2026-05-06',0,NULL,NULL,'',NULL,NULL,'2026-03-06 14:05:34','2026-03-06 16:03:48',1),(5,1,'呃呃呃','birthday',NULL,'2026-03-09',0,NULL,NULL,'时',NULL,NULL,'2026-03-06 16:03:58','2026-03-06 16:04:03',1),(6,1,'呃呃呃呃','birthday',NULL,'2026-01-11',0,NULL,NULL,'呃呃呃呃',NULL,NULL,'2026-03-06 16:04:31','2026-03-14 19:31:07',1),(7,1,'哈哈','family',NULL,'2026-04-07',0,NULL,NULL,'呃呃呃',NULL,NULL,'2026-03-06 16:29:07','2026-03-09 12:49:30',1),(8,1,'哈哈哈','love',NULL,'2026-03-10',0,NULL,NULL,'',NULL,NULL,'2026-03-06 22:00:12','2026-03-09 12:49:28',1),(9,1,'内容嗖嗖嗖','work',NULL,'2026-05-08',0,NULL,NULL,'',NULL,NULL,'2026-03-08 21:48:57','2026-03-14 19:31:09',1),(10,1,'恋爱500天','love',NULL,'2026-04-30',0,NULL,NULL,'恋爱五百天怎么玩',NULL,NULL,'2026-03-09 12:39:12','2026-03-09 12:39:12',0);
/*!40000 ALTER TABLE `calendar_anniversary` ENABLE KEYS */;
UNLOCK TABLES;

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
-- Dumping data for table `challenge_checkin`
--

LOCK TABLES `challenge_checkin` WRITE;
/*!40000 ALTER TABLE `challenge_checkin` DISABLE KEYS */;
/*!40000 ALTER TABLE `challenge_checkin` ENABLE KEYS */;
UNLOCK TABLES;

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
-- Dumping data for table `challenge_participant`
--

LOCK TABLES `challenge_participant` WRITE;
/*!40000 ALTER TABLE `challenge_participant` DISABLE KEYS */;
/*!40000 ALTER TABLE `challenge_participant` ENABLE KEYS */;
UNLOCK TABLES;

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
-- Dumping data for table `diet_record`
--

LOCK TABLES `diet_record` WRITE;
/*!40000 ALTER TABLE `diet_record` DISABLE KEYS */;
/*!40000 ALTER TABLE `diet_record` ENABLE KEYS */;
UNLOCK TABLES;

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
-- Dumping data for table `family`
--

LOCK TABLES `family` WRITE;
/*!40000 ALTER TABLE `family` DISABLE KEYS */;
INSERT INTO `family` VALUES (1,'我的家庭',NULL,1,NULL,1,0,10737418240,'2026-03-01 23:53:46','2026-03-02 17:13:49'),(5,'测试家庭',NULL,1,NULL,1,0,10737418240,'2026-03-07 01:28:07','2026-03-07 01:28:07');
/*!40000 ALTER TABLE `family` ENABLE KEYS */;
UNLOCK TABLES;

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
-- Dumping data for table `family_challenge`
--

LOCK TABLES `family_challenge` WRITE;
/*!40000 ALTER TABLE `family_challenge` DISABLE KEYS */;
/*!40000 ALTER TABLE `family_challenge` ENABLE KEYS */;
UNLOCK TABLES;

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
-- Dumping data for table `family_invite_code`
--

LOCK TABLES `family_invite_code` WRITE;
/*!40000 ALTER TABLE `family_invite_code` DISABLE KEYS */;
INSERT INTO `family_invite_code` VALUES (1,1,'111222',1,10000,2,'2036-03-07 02:02:49',1,'2026-03-07 02:02:49');
/*!40000 ALTER TABLE `family_invite_code` ENABLE KEYS */;
UNLOCK TABLES;

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
-- Dumping data for table `family_member`
--

LOCK TABLES `family_member` WRITE;
/*!40000 ALTER TABLE `family_member` DISABLE KEYS */;
INSERT INTO `family_member` VALUES (6,1,7,'member','齐老大','2026-03-01 00:00:40','2026-03-01 00:00:40','2026-03-11 23:21:40'),(10,5,1,'owner',NULL,'2026-03-07 01:28:07','2026-03-07 01:28:07','2026-03-07 01:28:07'),(15,1,16,'member','陶陶','2026-03-09 12:03:16','2026-03-09 12:03:15','2026-03-09 23:06:31');
/*!40000 ALTER TABLE `family_member` ENABLE KEYS */;
UNLOCK TABLES;

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
-- Dumping data for table `game_quiz`
--

LOCK TABLES `game_quiz` WRITE;
/*!40000 ALTER TABLE `game_quiz` DISABLE KEYS */;
INSERT INTO `game_quiz` VALUES (1,'ä¸­å›½çš„é¦–éƒ½æ˜¯å“ªé‡Œï¼Ÿ','ä¸Šæµ·','åŒ—äº¬','å¹¿å·ž','æ·±åœ³','B','åœ°ç†',1,'åŒ—äº¬æ˜¯ä¸­åŽäººæ°‘å…±å’Œå›½çš„é¦–éƒ½',0,0,1,'2026-02-28 01:18:16','2026-02-28 01:18:16',0),(2,'ä¸€å¹´æœ‰å¤šå°‘ä¸ªèŠ‚æ°”ï¼Ÿ','12','24','36','48','B','æ–‡åŒ–',1,'ä¸­å›½ä¼ ç»Ÿæœ‰äºŒåå››èŠ‚æ°”',0,0,1,'2026-02-28 01:18:16','2026-02-28 01:18:16',0),(3,'å½©è™¹æœ‰å‡ ç§é¢œè‰²ï¼Ÿ','5','6','7','8','C','è‡ªç„¶',1,'å½©è™¹æœ‰çº¢æ©™é»„ç»¿é’è“ç´«ä¸ƒç§é¢œè‰²',0,0,1,'2026-02-28 01:18:16','2026-02-28 01:18:16',0),(4,'äººä½“æœ€å¤§çš„å™¨å®˜æ˜¯ä»€ä¹ˆï¼Ÿ','å¿ƒè„','è‚è„','çš®è‚¤','å¤§è„‘','C','äººä½“',2,'çš®è‚¤æ˜¯äººä½“æœ€å¤§çš„å™¨å®˜',0,0,1,'2026-02-28 01:18:16','2026-02-28 01:18:16',0),(5,'\"åºŠå‰æ˜Žæœˆå…‰\"çš„ä¸‹ä¸€å¥æ˜¯ï¼Ÿ','ç–‘æ˜¯åœ°ä¸Šéœœ','ä¸¾å¤´æœ›æ˜Žæœˆ','ä½Žå¤´æ€æ•…ä¹¡','å¤„å¤„é—»å•¼é¸Ÿ','A','è¯—è¯',1,'æŽç™½ã€Šé™å¤œæ€ã€‹ï¼šåºŠå‰æ˜Žæœˆå…‰ï¼Œç–‘æ˜¯åœ°ä¸Šéœœ',0,0,1,'2026-02-28 01:18:16','2026-02-28 01:18:16',0);
/*!40000 ALTER TABLE `game_quiz` ENABLE KEYS */;
UNLOCK TABLES;

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
-- Dumping data for table `game_riddle`
--

LOCK TABLES `game_riddle` WRITE;
/*!40000 ALTER TABLE `game_riddle` DISABLE KEYS */;
INSERT INTO `game_riddle` VALUES (1,'ä¸€å£å’¬æŽ‰ç‰›å°¾å·´ï¼Œæ˜¯ä»€ä¹ˆå­—ï¼Ÿ','å‘Š','ç‰›åŽ»æŽ‰ä¸‹é¢ä¸€ç«–ï¼ŒåŠ ä¸Šå£','å­—è°œ',2,'ç‰›åŽ»æŽ‰ä¸‹é¢ä¸€ç«–æ˜¯\"ç‰›\"å­—å¤´ï¼ŒåŠ ä¸Šå£å°±æ˜¯\"å‘Š\"å­—',0,0,1,'2026-02-28 01:18:16','2026-02-28 01:18:16',0),(2,'å°æ—¶ç©¿é»‘è¡£ï¼Œå¤§æ—¶ç©¿ç»¿è¢ï¼Œæ°´é‡Œè¿‡æ—¥å­ï¼Œå²¸ä¸Šæ¥ç¡è§‰ã€‚','é’è›™','ä¼šè·³ï¼Œå‘±å‘±å«','åŠ¨ç‰©è°œ',1,'é’è›™å°æ—¶å€™æ˜¯é»‘èŒèšªï¼Œé•¿å¤§æ˜¯ç»¿é’è›™',0,0,1,'2026-02-28 01:18:16','2026-02-28 01:18:16',0),(3,'äº”ä¸ªå…„å¼Ÿï¼Œä½åœ¨ä¸€èµ·ï¼Œåå­—ä¸åŒï¼Œé«˜çŸ®ä¸é½ã€‚','æ‰‹æŒ‡','æ¯ä¸ªäººéƒ½æœ‰ï¼Œäº”ä¸ª','äººä½“è°œ',1,'äº”æ ¹æ‰‹æŒ‡é•¿çŸ­ä¸åŒ',0,0,1,'2026-02-28 01:18:16','2026-02-28 01:18:16',0),(4,'åƒæ¡çº¿ï¼Œä¸‡æ¡çº¿ï¼ŒæŽ‰åˆ°æ°´é‡Œçœ‹ä¸è§ã€‚','é›¨','å¤©ä¸Šè½ä¸‹æ¥ï¼Œæ°´é‡Œçš„','è‡ªç„¶è°œ',1,'é›¨æ»´åƒçº¿ï¼Œè½å…¥æ°´ä¸­æ¶ˆå¤±',0,0,1,'2026-02-28 01:18:16','2026-02-28 01:18:16',0),(5,'ç‹¬æœ¨é€ é«˜æ¥¼ï¼Œæ²¡ç“¦æ²¡ç –å¤´ï¼Œäººåœ¨æ°´ä¸‹èµ°ï¼Œæ°´åœ¨äººä¸Šæµã€‚','é›¨ä¼ž','ä¸‹é›¨å¤©ç”¨','ç‰©å“è°œ',2,'é›¨ä¼žåƒé«˜æ¥¼ï¼Œä¼žé¢æŒ¡é›¨',0,0,1,'2026-02-28 01:18:16','2026-02-28 01:18:16',0);
/*!40000 ALTER TABLE `game_riddle` ENABLE KEYS */;
UNLOCK TABLES;

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
-- Dumping data for table `health_record`
--

LOCK TABLES `health_record` WRITE;
/*!40000 ALTER TABLE `health_record` DISABLE KEYS */;
/*!40000 ALTER TABLE `health_record` ENABLE KEYS */;
UNLOCK TABLES;

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
-- Dumping data for table `holiday_config`
--

LOCK TABLES `holiday_config` WRITE;
/*!40000 ALTER TABLE `holiday_config` DISABLE KEYS */;
/*!40000 ALTER TABLE `holiday_config` ENABLE KEYS */;
UNLOCK TABLES;

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
-- Dumping data for table `ingredient`
--

LOCK TABLES `ingredient` WRITE;
/*!40000 ALTER TABLE `ingredient` DISABLE KEYS */;
/*!40000 ALTER TABLE `ingredient` ENABLE KEYS */;
UNLOCK TABLES;

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
-- Dumping data for table `invite_code`
--

LOCK TABLES `invite_code` WRITE;
/*!40000 ALTER TABLE `invite_code` DISABLE KEYS */;
/*!40000 ALTER TABLE `invite_code` ENABLE KEYS */;
UNLOCK TABLES;

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
-- Dumping data for table `points_transaction`
--

LOCK TABLES `points_transaction` WRITE;
/*!40000 ALTER TABLE `points_transaction` DISABLE KEYS */;
/*!40000 ALTER TABLE `points_transaction` ENABLE KEYS */;
UNLOCK TABLES;

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
-- Dumping data for table `recipe`
--

LOCK TABLES `recipe` WRITE;
/*!40000 ALTER TABLE `recipe` DISABLE KEYS */;
/*!40000 ALTER TABLE `recipe` ENABLE KEYS */;
UNLOCK TABLES;

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
-- Dumping data for table `recipe_record`
--

LOCK TABLES `recipe_record` WRITE;
/*!40000 ALTER TABLE `recipe_record` DISABLE KEYS */;
/*!40000 ALTER TABLE `recipe_record` ENABLE KEYS */;
UNLOCK TABLES;

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
-- Dumping data for table `reminder_config`
--

LOCK TABLES `reminder_config` WRITE;
/*!40000 ALTER TABLE `reminder_config` DISABLE KEYS */;
/*!40000 ALTER TABLE `reminder_config` ENABLE KEYS */;
UNLOCK TABLES;

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
-- Dumping data for table `reminder_log`
--

LOCK TABLES `reminder_log` WRITE;
/*!40000 ALTER TABLE `reminder_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `reminder_log` ENABLE KEYS */;
UNLOCK TABLES;

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
-- Dumping data for table `sys_config`
--

LOCK TABLES `sys_config` WRITE;
/*!40000 ALTER TABLE `sys_config` DISABLE KEYS */;
INSERT INTO `sys_config` VALUES (1,'wechat.work.corpid','ww6c1c7590db91ef85','ä¼ä¸šå¾®ä¿¡CorpID','wechat_work',0,'2026-03-14 02:12:34','2026-03-14 02:12:34'),(2,'wechat.work.agentid','1000002','ä¼ä¸šå¾®ä¿¡åº”ç”¨AgentID','wechat_work',0,'2026-03-14 02:12:34','2026-03-14 02:12:34'),(3,'wechat.work.secret','Ne0oN5Y8mNmRA_wkIP7I4PMn_sr2GFPkbBABqUaEEE4','ä¼ä¸šå¾®ä¿¡åº”ç”¨Secret','wechat_work',0,'2026-03-14 02:12:34','2026-03-14 02:12:34'),(4,'wechat.work.userid','XIAOZHUSHOU','ä¼ä¸šå¾®ä¿¡å°åŠ©æ‰‹UserID','wechat_work',0,'2026-03-14 02:12:34','2026-03-14 02:12:34'),(5,'wechat.miniapp.appid','wxbdc70536c5e52b82','å¾®ä¿¡å°ç¨‹åºAppID','wechat_miniapp',0,'2026-03-14 02:41:48','2026-03-14 02:41:48'),(6,'wechat.miniapp.secret','ee79692373557e80d206c09a1876dbea','å¾®ä¿¡å°ç¨‹åºAppSecret','wechat_miniapp',0,'2026-03-14 02:41:48','2026-03-14 02:41:48');
/*!40000 ALTER TABLE `sys_config` ENABLE KEYS */;
UNLOCK TABLES;

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
-- Dumping data for table `sys_user`
--

LOCK TABLES `sys_user` WRITE;
/*!40000 ALTER TABLE `sys_user` DISABLE KEYS */;
INSERT INTO `sys_user` VALUES (1,'admin','$2b$12$QVPuzAiLalhHkzc1uKfgrunnackKy.3TddjNCl.fn20hRwvtD4uF6','ç®¡ç†å‘˜',NULL,'13800138000',NULL,NULL,0,'2026-03-07 01:27:48',1,1,'2026-02-28 23:47:44','2026-02-28 23:56:34',0,NULL,NULL),(7,'15861890687','$2b$12$GQE67iPIqImHW.OwV3rT4eqGtYGnw/eNUUjpngl68EaK9RTJb0TkW','齐老大','/api/avatars/5f3dcf18f69a44589848183e1eeba22f.jpg','15861890687','oTY9R3W0FZg66UsdQA3UcFXaimq8',NULL,0,'2026-03-13 14:23:58',1,1,'2026-03-01 00:00:40','2026-03-16 00:40:46',0,'QiJun','wmDorvDgAA7h68ixTWSTvWGrj6G5elDQ'),(9,'13900139001','$2a$10$E8CXbSPHPbH6K6f8jvKYJOpF2vixxpqXs9LYjqLfwvmRMPybAI3gm','张三',NULL,'13900139001',NULL,NULL,0,'2026-03-08 01:43:07',1,1,'2026-03-02 18:12:29','2026-03-02 18:12:29',0,NULL,NULL),(10,'testuser123','$2a$10$XPZC.Q/yaOaJVZGLBkPc5eYevUPY1ylBLqpzo5HsYwckUnq2VX31m','更新后的测试用户',NULL,NULL,NULL,NULL,0,'2026-03-05 01:04:03',1,1,'2026-03-05 01:02:37','2026-03-05 01:02:37',0,NULL,NULL),(11,'13900000001','$2a$10$uKOC2W/WzjkNqkujDo1W/uioCAWLx2FIy8RJPD/FfvXaHTtuBQR/.','测试用户1',NULL,'13900000001',NULL,NULL,0,'2026-03-07 01:10:00',1,1,'2026-03-07 01:09:52','2026-03-07 01:09:52',0,NULL,NULL),(12,'test_api_user','$2a$10$Ul99c62ekkiEmp6vGVno5eTaDG9hyr3J97XLVW6wLqtOjYbuRsavq','API测试用户',NULL,'13999990001',NULL,NULL,0,'2026-03-07 01:33:17',1,1,'2026-03-07 01:33:14','2026-03-07 01:33:14',0,NULL,NULL),(13,'test2025','$2a$10$L.5C.0rTdqeZW5cFJKYV9OKl7lunN9I45LUYKoAj/giGgah044kge','测试用户2025',NULL,NULL,NULL,NULL,0,'2026-03-07 22:42:05',1,1,'2026-03-07 22:42:00','2026-03-07 22:42:00',0,NULL,NULL),(14,'15705181808','$2a$10$pJJZlUaZ9SYHGt3AT9T0lOCdyHoL8W2Va2i8.5sxz6J.o2o9gv1BW','测试',NULL,NULL,NULL,NULL,0,'2026-03-08 22:56:35',1,1,'2026-03-08 22:56:33','2026-03-10 00:21:00',0,NULL,NULL),(15,'deleted_15','','微信用户1624',NULL,NULL,'oTY9R3SU5fCToEV_oQ4lOyPGPpA0',NULL,0,'2026-03-09 11:53:15',1,1,'2026-03-09 11:53:15','2026-03-09 23:22:21',0,NULL,NULL),(16,'17368741926','','陶陶','/api/avatars/21a9dde24d8f40f4857fad325eacce97.jpg','17368741926','oTY9R3b6lscukvpc4adEWGYEkdZc',NULL,0,'2026-03-09 14:47:07',1,1,'2026-03-09 12:03:16','2026-03-15 02:00:22',0,'TaoTao','wmDorvDgAA67CvAyiEzc8kEJD2mWtd6A');
/*!40000 ALTER TABLE `sys_user` ENABLE KEYS */;
UNLOCK TABLES;

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
-- Dumping data for table `task`
--

LOCK TABLES `task` WRITE;
/*!40000 ALTER TABLE `task` DISABLE KEYS */;
INSERT INTO `task` VALUES (1,1,NULL,'测试任务',NULL,NULL,0,2,NULL,1,'2026-02-28 15:00:00',NULL,'none',NULL,NULL,0,NULL,'2026-03-01 01:52:02','2026-02-28 23:52:08','2026-03-02 00:11:50',0,NULL,1,'2026-03-02 00:11:50',0),(2,1,NULL,'测试任务2',NULL,NULL,1,2,NULL,1,'2026-02-28 16:30:45',NULL,'none',NULL,NULL,0,NULL,'2026-03-02 01:31:01','2026-02-28 23:52:32','2026-03-02 01:31:06',0,NULL,1,'2026-03-02 01:31:06',0),(10,1,NULL,'爸爸',NULL,NULL,0,0,7,7,'2026-03-02 15:00:00',NULL,'none',NULL,NULL,0,NULL,NULL,'2026-03-02 00:16:50','2026-03-02 00:16:56',0,NULL,1,'2026-03-02 00:16:56',0),(16,1,NULL,'的',NULL,NULL,0,0,7,7,'2026-03-02 15:00:00',NULL,'none',NULL,NULL,0,NULL,NULL,'2026-03-02 12:30:37','2026-03-02 16:26:28',0,NULL,1,'2026-03-02 16:26:28',0),(20,1,NULL,'测试任务',NULL,NULL,0,2,9,9,'2026-03-03 10:00:00',NULL,'none',NULL,NULL,0,NULL,'2026-03-04 00:04:48','2026-03-02 18:14:55','2026-03-04 00:04:48',0,NULL,0,NULL,0),(27,1,NULL,'API测试任务',NULL,NULL,0,2,10,10,'2026-03-05 15:00:00',NULL,'none',NULL,NULL,0,NULL,'2026-03-05 01:02:53','2026-03-05 01:02:51','2026-03-05 01:02:53',0,NULL,0,NULL,0),(47,1,NULL,'API测试任务已更新',NULL,NULL,0,0,11,11,NULL,NULL,'none',NULL,NULL,0,NULL,NULL,'2026-03-07 01:10:06','2026-03-07 01:10:10',0,NULL,1,'2026-03-07 01:10:10',0),(48,1,NULL,'【API测试】购买生活用品(已更新)','去超市买牛奶、面包、鸡蛋和水果',NULL,0,1,NULL,12,NULL,NULL,'none',NULL,NULL,0,NULL,NULL,'2026-03-07 01:33:58','2026-03-07 01:34:26',0,NULL,0,NULL,0),(49,1,NULL,'【API测试】高优先级任务','这是一个高优先级测试任务',NULL,2,0,NULL,12,NULL,NULL,'none',NULL,NULL,0,NULL,'2026-03-07 01:34:29','2026-03-07 01:34:09','2026-03-07 01:34:43',0,NULL,0,NULL,0),(50,1,NULL,'【API测试】普通待办任务',NULL,NULL,0,0,12,12,NULL,NULL,'none',NULL,NULL,0,NULL,NULL,'2026-03-07 01:34:13','2026-03-07 01:34:13',0,NULL,0,NULL,0),(51,1,NULL,'【API测试】测试删除功能','此任务用于测试删除接口',NULL,0,0,NULL,12,NULL,NULL,'none',NULL,NULL,0,NULL,NULL,'2026-03-07 01:34:17','2026-03-07 01:34:32',0,NULL,1,'2026-03-07 01:34:32',0),(52,1,NULL,'【1586测试】去超市购物(已更新)','买牛奶、面包、鸡蛋、水果',NULL,1,1,NULL,7,NULL,NULL,'none',NULL,NULL,0,NULL,NULL,'2026-03-07 01:36:10','2026-03-07 01:36:18',0,NULL,0,NULL,0),(54,1,NULL,'【1586测试】临时任务',NULL,NULL,0,0,NULL,7,NULL,NULL,'none',NULL,NULL,0,NULL,NULL,'2026-03-07 01:36:18','2026-03-07 01:36:27',0,NULL,1,'2026-03-07 01:36:27',0),(55,1,NULL,'测试任务','测试内容','测试备注',0,0,13,13,'2026-03-08 15:00:00',NULL,'none',NULL,NULL,0,NULL,NULL,'2026-03-07 22:42:14','2026-03-07 22:42:14',0,NULL,0,NULL,0),(56,1,NULL,'测试带空格格式','测试内容','测试备注',0,0,13,13,'2026-03-07 22:43:00',NULL,'none',NULL,NULL,0,NULL,NULL,'2026-03-07 22:49:38','2026-03-07 22:49:38',0,NULL,0,NULL,0),(57,1,NULL,'测试ISO格式','测试内容','ISO格式测试',0,0,13,13,'2026-03-08 15:00:00',NULL,'none',NULL,NULL,0,NULL,NULL,'2026-03-07 22:49:51','2026-03-07 22:49:51',0,NULL,0,NULL,0),(65,1,NULL,'测试时间选择器',NULL,NULL,0,0,NULL,13,'2026-03-08 23:59:00',NULL,'none',NULL,NULL,0,NULL,NULL,'2026-03-08 01:24:15','2026-03-08 01:24:15',0,NULL,0,NULL,0),(74,1,NULL,'无聊发呆',NULL,'活干了一大半了，无聊发呆啦(┯_┯)哈哈哈哈',0,0,16,16,'2026-03-09 17:34:00',NULL,'none',NULL,NULL,0,NULL,'2026-03-12 08:11:01','2026-03-09 17:35:14','2026-03-12 08:15:09',0,NULL,0,NULL,0),(76,1,NULL,'买车',NULL,'这群人天天说买车，说了几年了啊。无语',0,2,7,16,'2026-03-12 08:10:00',NULL,'none',NULL,NULL,0,NULL,'2026-03-12 08:21:13','2026-03-10 10:41:58','2026-03-12 08:21:13',0,NULL,0,NULL,0),(77,1,NULL,'养龙虾',NULL,'都在讨论养龙虾了，程序员是不是要失业了，之前我们还讨论过这个话题。这两天他们天天说。',0,2,7,16,'2026-03-12 08:10:00',NULL,'none',NULL,NULL,0,NULL,'2026-03-12 08:21:10','2026-03-10 11:05:16','2026-03-12 08:21:10',0,NULL,0,NULL,0),(84,1,NULL,'你好',NULL,'你好哇',0,2,7,7,'2026-03-12 00:17:00',NULL,'none',NULL,NULL,0,NULL,'2026-03-12 01:25:14','2026-03-12 00:17:14','2026-03-12 01:25:14',0,NULL,0,NULL,0),(85,1,NULL,'你好',NULL,'',0,2,7,7,'2026-03-12 00:18:00',NULL,'none',NULL,NULL,0,NULL,'2026-03-12 01:32:31','2026-03-12 00:18:20','2026-03-12 01:32:31',0,NULL,0,NULL,0),(86,1,NULL,'123',NULL,'',0,2,7,7,'2026-03-12 00:25:00',NULL,'none',NULL,NULL,0,NULL,'2026-03-12 00:25:44','2026-03-12 00:25:39','2026-03-12 00:25:44',0,NULL,0,NULL,0),(87,1,NULL,'123',NULL,'',0,2,16,7,'2026-03-12 00:27:00',NULL,'none',NULL,NULL,0,NULL,'2026-03-12 08:11:02','2026-03-12 00:28:06','2026-03-12 08:11:02',0,NULL,0,NULL,0),(88,1,NULL,'333',NULL,'',0,2,7,7,'2026-03-12 01:02:00',NULL,'none',NULL,NULL,0,NULL,'2026-03-12 01:05:50','2026-03-12 01:02:47','2026-03-12 01:05:50',0,NULL,0,NULL,0),(89,1,NULL,'下午看小程序',NULL,'哟',0,2,16,7,'2026-03-12 14:05:00',NULL,'none',NULL,NULL,0,NULL,'2026-03-14 12:40:29','2026-03-12 13:09:19','2026-03-14 12:40:29',0,NULL,0,NULL,0),(90,1,NULL,'准时吃饭 太秀了',NULL,'吃',0,2,16,7,'2026-03-14 17:30:00',NULL,'none',NULL,NULL,0,NULL,'2026-03-14 19:26:44','2026-03-14 17:19:17','2026-03-14 19:26:44',0,NULL,0,NULL,0),(91,1,NULL,'准时吃饭',NULL,'煲仔皇',0,2,16,7,'2026-03-14 18:43:00',NULL,'none',NULL,NULL,0,NULL,'2026-03-14 19:27:26','2026-03-14 17:44:28','2026-03-14 19:27:26',0,NULL,0,NULL,0),(92,1,NULL,'等煲仔皇上桌',NULL,'',0,2,16,7,'2026-03-14 17:59:00',NULL,'none',NULL,NULL,0,NULL,'2026-03-14 19:26:48','2026-03-14 17:53:17','2026-03-14 19:26:48',0,NULL,0,NULL,0),(93,1,NULL,'开始吃煲仔饭',NULL,'',0,2,16,7,'2026-03-14 18:44:00',NULL,'none',NULL,NULL,0,NULL,'2026-03-14 19:26:56','2026-03-14 18:08:27','2026-03-14 19:26:56',0,NULL,0,NULL,0),(94,1,NULL,'逛奥乐齐',NULL,'',0,2,16,7,'2026-03-14 18:20:00',NULL,'none',NULL,NULL,0,NULL,'2026-03-14 19:26:49','2026-03-14 18:14:13','2026-03-14 19:26:49',0,NULL,0,NULL,0),(95,1,NULL,'奥乐齐买肉！！',NULL,'买买买',0,2,16,7,'2026-03-14 18:20:00',NULL,'none',NULL,NULL,0,NULL,'2026-03-14 19:26:50','2026-03-14 18:19:11','2026-03-14 19:26:50',0,NULL,0,NULL,0),(96,1,NULL,'奥乐齐买鸡爪子',NULL,'买鸡爪子指甲剪掉',0,2,16,7,'2026-03-14 18:20:00',NULL,'none',NULL,NULL,0,NULL,'2026-03-14 19:26:53','2026-03-14 18:44:00','2026-03-14 19:26:53',0,NULL,0,NULL,0),(97,1,NULL,'奥乐齐买鸡翅',NULL,'红烧鸡翅',0,2,16,7,'2026-03-14 18:55:00',NULL,'none',NULL,NULL,0,NULL,'2026-03-14 19:27:00','2026-03-14 18:51:27','2026-03-14 19:27:00',0,NULL,0,NULL,0),(98,1,NULL,'买榴莲大福',NULL,'吃榴莲',0,2,16,7,'2026-03-14 19:17:00',NULL,'none',NULL,NULL,0,NULL,'2026-03-14 19:27:01','2026-03-14 19:08:11','2026-03-14 19:27:01',0,NULL,0,NULL,0),(99,1,NULL,'坐公交车到家',NULL,'需要一个苦力帮忙拎东西',0,2,7,16,'2026-03-14 19:49:00',NULL,'none',NULL,NULL,0,NULL,'2026-03-14 19:51:08','2026-03-14 19:50:46','2026-03-14 19:51:08',0,NULL,0,NULL,0),(100,1,NULL,'买车票',NULL,'',0,2,16,7,'2026-03-15 16:00:00',NULL,'none',NULL,NULL,0,NULL,'2026-03-15 23:18:14','2026-03-14 22:01:27','2026-03-15 23:18:14',0,NULL,0,NULL,0),(101,1,NULL,'准备去南湖',NULL,'做好准备',0,2,16,7,'2026-03-15 11:15:00',NULL,'none',NULL,NULL,0,NULL,'2026-03-15 23:18:17','2026-03-15 10:09:16','2026-03-15 23:18:17',0,NULL,0,NULL,0);
/*!40000 ALTER TABLE `task` ENABLE KEYS */;
UNLOCK TABLES;

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
-- Dumping data for table `task_reminder`
--

LOCK TABLES `task_reminder` WRITE;
/*!40000 ALTER TABLE `task_reminder` DISABLE KEYS */;
/*!40000 ALTER TABLE `task_reminder` ENABLE KEYS */;
UNLOCK TABLES;

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
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

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
-- Dumping data for table `user_points`
--

LOCK TABLES `user_points` WRITE;
/*!40000 ALTER TABLE `user_points` DISABLE KEYS */;
INSERT INTO `user_points` VALUES (1,1,0,0,0,'2026-03-08 01:43:25'),(2,7,0,0,0,'2026-03-08 01:43:25'),(3,9,0,0,0,'2026-03-08 01:43:25'),(4,10,0,0,0,'2026-03-08 01:43:25'),(5,11,0,0,0,'2026-03-08 01:43:25'),(6,12,0,0,0,'2026-03-08 01:43:25'),(7,13,0,0,0,'2026-03-08 01:43:25');
/*!40000 ALTER TABLE `user_points` ENABLE KEYS */;
UNLOCK TABLES;

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
-- Dumping data for table `user_water_goal`
--

LOCK TABLES `user_water_goal` WRITE;
/*!40000 ALTER TABLE `user_water_goal` DISABLE KEYS */;
INSERT INTO `user_water_goal` VALUES (1,7,2000,'2026-03-01 02:00:38','2026-03-08 21:48:16',0),(2,14,3000,'2026-03-08 22:57:03','2026-03-08 22:57:03',0),(3,16,1500,'2026-03-09 12:46:56','2026-03-09 12:46:56',0);
/*!40000 ALTER TABLE `user_water_goal` ENABLE KEYS */;
UNLOCK TABLES;

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
-- Dumping data for table `vote`
--

LOCK TABLES `vote` WRITE;
/*!40000 ALTER TABLE `vote` DISABLE KEYS */;
/*!40000 ALTER TABLE `vote` ENABLE KEYS */;
UNLOCK TABLES;

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
-- Dumping data for table `vote_record`
--

LOCK TABLES `vote_record` WRITE;
/*!40000 ALTER TABLE `vote_record` DISABLE KEYS */;
/*!40000 ALTER TABLE `vote_record` ENABLE KEYS */;
UNLOCK TABLES;

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
-- Dumping data for table `water_record`
--

LOCK TABLES `water_record` WRITE;
/*!40000 ALTER TABLE `water_record` DISABLE KEYS */;
INSERT INTO `water_record` VALUES (1,1,250,'2026-02-28','23:57:56','2026-02-28 23:57:57'),(2,1,300,'2026-02-28','23:59:27','2026-02-28 23:59:27'),(3,1,200,'2026-03-01','00:00:00','2026-03-01 00:00:00'),(4,1,500,'2026-03-01','00:00:10','2026-03-01 00:00:11'),(5,1,100,'2026-03-01','00:00:41','2026-03-01 00:00:42'),(6,1,100,'2026-03-01','00:00:46','2026-03-01 00:00:47'),(7,7,200,'2026-03-01','00:12:09','2026-03-01 00:12:09'),(8,7,200,'2026-03-01','00:14:53','2026-03-01 00:14:54'),(9,7,200,'2026-03-01','00:17:49','2026-03-01 00:17:50'),(10,7,300,'2026-03-01','00:18:21','2026-03-01 00:18:21'),(11,7,200,'2026-03-01','00:32:52','2026-03-01 00:32:52'),(12,1,200,'2026-03-01','00:38:40','2026-03-01 00:38:41'),(13,1,300,'2026-03-01','00:38:49','2026-03-01 00:38:49'),(14,7,150,'2026-03-01','00:42:16','2026-03-01 00:42:17'),(15,7,200,'2026-03-01','00:52:50','2026-03-01 00:52:51'),(16,7,250,'2026-03-01','00:59:46','2026-03-01 00:59:47'),(17,7,300,'2026-03-01','01:21:16','2026-03-01 01:21:16'),(18,7,200,'2026-03-01','01:45:25','2026-03-01 01:45:26'),(19,7,200,'2026-03-01','01:45:30','2026-03-01 01:45:31'),(20,7,200,'2026-03-01','01:45:35','2026-03-01 01:45:35'),(23,7,350,'2026-03-01','01:57:04','2026-03-01 01:57:05'),(24,7,200,'2026-03-01','01:57:10','2026-03-01 01:57:10'),(28,7,200,'2026-03-01','02:48:13','2026-03-01 02:48:14'),(29,7,120,'2026-03-01','02:57:14','2026-03-01 02:57:14'),(34,7,200,'2026-03-01','23:42:37','2026-03-01 23:42:38'),(35,7,100,'2026-03-01','23:42:40','2026-03-01 23:42:40'),(36,7,250,'2026-03-01','23:42:41','2026-03-01 23:42:42'),(37,7,200,'2026-03-01','23:55:05','2026-03-01 23:55:05'),(38,7,100,'2026-03-02','00:00:05','2026-03-02 00:00:05'),(39,7,350,'2026-03-02','00:00:07','2026-03-02 00:00:07'),(40,7,200,'2026-03-02','00:00:10','2026-03-02 00:00:11'),(41,7,500,'2026-03-02','00:00:14','2026-03-02 00:00:14'),(42,7,100,'2026-03-02','01:23:53','2026-03-02 01:23:53'),(43,7,20,'2026-03-02','11:11:36','2026-03-02 11:11:36'),(44,7,100,'2026-03-02','16:31:48','2026-03-02 16:31:48'),(45,7,250,'2026-03-02','16:31:50','2026-03-02 16:31:50'),(46,7,350,'2026-03-02','16:31:52','2026-03-02 16:31:53'),(47,7,1,'2026-03-02','16:31:56','2026-03-02 16:31:57'),(48,7,200,'2026-03-02','18:55:30','2026-03-02 18:55:30'),(49,7,500,'2026-03-02','18:55:31','2026-03-02 18:55:32'),(50,7,200,'2026-03-03','11:10:04','2026-03-03 11:10:05'),(51,7,100,'2026-03-03','11:10:06','2026-03-03 11:10:07'),(53,7,500,'2026-03-03','21:06:15','2026-03-03 21:06:16'),(54,7,250,'2026-03-03','23:52:41','2026-03-03 23:52:41'),(55,7,350,'2026-03-03','23:52:43','2026-03-03 23:52:43'),(57,7,1035,'2026-03-03','23:58:50','2026-03-03 23:58:51'),(59,7,100,'2026-03-04','00:05:00','2026-03-04 00:05:00'),(61,7,250,'2026-03-04','23:39:40','2026-03-04 23:39:41'),(62,10,200,'2026-03-05','01:02:58','2026-03-05 01:02:59'),(63,7,100,'2026-03-05','13:06:58','2026-03-05 13:06:58'),(64,7,200,'2026-03-05','13:06:59','2026-03-05 13:07:00'),(65,7,150,'2026-03-05','13:07:05','2026-03-05 13:07:06'),(66,7,100,'2026-03-05','13:07:07','2026-03-05 13:07:07'),(67,7,150,'2026-03-05','13:07:17','2026-03-05 13:07:17'),(68,7,200,'2026-03-05','23:29:35','2026-03-05 23:29:36'),(70,7,200,'2026-03-05','23:51:50','2026-03-05 23:51:51'),(71,7,200,'2026-03-06','10:05:21','2026-03-06 10:05:21'),(72,7,250,'2026-03-06','10:05:28','2026-03-06 10:05:29'),(73,7,200,'2026-03-06','13:20:41','2026-03-06 13:20:42'),(74,7,30,'2026-03-06','13:20:55','2026-03-06 13:20:56'),(75,7,100,'2026-03-06','16:05:51','2026-03-06 16:05:51'),(76,7,200,'2026-03-07','00:09:18','2026-03-07 00:09:18'),(77,7,100,'2026-03-07','01:19:28','2026-03-07 01:19:29'),(79,7,1,'2026-03-07','01:20:09','2026-03-07 01:20:09'),(80,7,350,'2026-03-07','01:47:27','2026-03-07 01:47:27'),(81,7,3,'2026-03-07','01:47:33','2026-03-07 01:47:33'),(82,7,500,'2026-03-08','11:23:56','2026-03-08 11:23:56'),(83,7,3,'2026-03-08','11:24:12','2026-03-08 11:24:13'),(84,7,100,'2026-03-08','21:26:52','2026-03-08 21:26:53'),(85,7,200,'2026-03-08','21:26:55','2026-03-08 21:26:55'),(86,7,350,'2026-03-08','21:27:07','2026-03-08 21:27:07'),(87,7,200,'2026-03-08','21:27:26','2026-03-08 21:27:26'),(88,7,200,'2026-03-08','21:27:28','2026-03-08 21:27:29'),(89,7,200,'2026-03-08','21:27:34','2026-03-08 21:27:34'),(90,7,200,'2026-03-08','21:29:14','2026-03-08 21:29:14'),(91,7,200,'2026-03-08','21:29:20','2026-03-08 21:29:21'),(93,7,3,'2026-03-08','21:48:33','2026-03-08 21:48:33'),(94,14,200,'2026-03-08','22:56:57','2026-03-08 22:56:58'),(95,14,200,'2026-03-08','22:57:03','2026-03-08 22:57:04'),(96,15,150,'2026-03-09','11:54:06','2026-03-09 11:54:07'),(97,16,350,'2026-03-09','12:04:10','2026-03-09 12:04:11'),(98,16,350,'2026-03-09','12:04:14','2026-03-09 12:04:14'),(99,15,200,'2026-03-09','12:08:36','2026-03-09 12:08:37'),(100,15,150,'2026-03-09','12:58:35','2026-03-09 12:58:35'),(101,16,350,'2026-03-09','17:34:17','2026-03-09 17:34:17'),(102,7,200,'2026-03-11','23:14:22','2026-03-11 23:14:22'),(103,7,250,'2026-03-11','23:14:25','2026-03-11 23:14:26'),(104,7,125,'2026-03-11','23:33:00','2026-03-11 23:33:01'),(105,7,150,'2026-03-11','23:33:02','2026-03-11 23:33:02'),(106,7,175,'2026-03-11','23:33:06','2026-03-11 23:33:06'),(107,7,250,'2026-03-11','23:33:07','2026-03-11 23:33:08'),(108,7,350,'2026-03-11','23:33:08','2026-03-11 23:33:09'),(111,7,200,'2026-03-12','01:04:05','2026-03-12 01:04:05'),(112,7,200,'2026-03-12','01:25:37','2026-03-12 01:25:38');
/*!40000 ALTER TABLE `water_record` ENABLE KEYS */;
UNLOCK TABLES;

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
-- Dumping data for table `weight_record`
--

LOCK TABLES `weight_record` WRITE;
/*!40000 ALTER TABLE `weight_record` DISABLE KEYS */;
/*!40000 ALTER TABLE `weight_record` ENABLE KEYS */;
UNLOCK TABLES;

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

--
-- Dumping data for table `wish`
--

LOCK TABLES `wish` WRITE;
/*!40000 ALTER TABLE `wish` DISABLE KEYS */;
INSERT INTO `wish` VALUES (1,1,1,'experience','åŽ»æµ·è¾¹åº¦å‡','æƒ³è¦å’Œå®¶äººä¸€èµ·åŽ»ä¸‰äºšåº¦å‡',NULL,NULL,'2026-07-01','public',3,2,0,NULL,0,NULL,NULL,'2026-02-23 15:55:02','2026-03-06 16:27:55',1),(2,1,1,'item','ä¹°ä¸€å°æ–°ç›¸æœº','æƒ³è¦ä¸€å°å•åç›¸æœº',NULL,NULL,'2026-06-01','public',2,2,1,NULL,30,NULL,NULL,'2026-02-23 15:55:02','2026-03-06 16:27:52',1),(3,1,1,'gift','测试愿望',NULL,NULL,NULL,NULL,'couple',0,1,2,1,100,NULL,'2026-03-06 00:00:00','2026-03-06 15:16:17','2026-03-07 01:07:13',1),(4,1,1,'gift','测试愿望',NULL,NULL,NULL,NULL,'couple',0,1,0,NULL,0,NULL,NULL,'2026-03-06 15:23:21','2026-03-06 16:28:06',1),(5,1,1,'gift','测试愿望',NULL,NULL,NULL,NULL,'couple',0,1,0,NULL,0,NULL,NULL,'2026-03-06 15:24:12','2026-03-06 16:28:04',1),(6,1,1,'gift','测试愿望123',NULL,NULL,NULL,NULL,'couple',0,1,0,NULL,0,NULL,NULL,'2026-03-06 15:24:31','2026-03-06 16:28:03',1),(7,1,1,'gift','测试愿望123',NULL,NULL,NULL,NULL,'couple',0,1,0,NULL,0,NULL,NULL,'2026-03-06 15:24:52','2026-03-06 16:28:02',1),(8,1,1,'gift','测试愿望123',NULL,NULL,NULL,NULL,'couple',0,1,0,NULL,0,NULL,NULL,'2026-03-06 15:25:24','2026-03-06 16:28:00',1),(9,1,1,'gift','测试愿望',NULL,NULL,NULL,NULL,'couple',0,1,0,NULL,0,NULL,NULL,'2026-03-06 15:25:45','2026-03-06 16:27:59',1),(10,1,1,'gift','测试愿望',NULL,NULL,NULL,NULL,'couple',0,1,0,NULL,0,NULL,NULL,'2026-03-06 15:25:50','2026-03-06 16:27:58',1),(11,1,1,'travel','去日本旅游','希望明年春天去日本看樱花',5000.00,10000.00,'2026-04-01','couple',0,1,0,NULL,0,NULL,NULL,'2026-03-06 15:34:51','2026-03-07 01:52:36',1),(12,1,1,'electronic','买摩托车','啊',NULL,NULL,'2026-10-06','couple',0,1,1,1,0,NULL,NULL,'2026-03-06 16:28:37','2026-03-07 01:52:37',1),(13,1,1,'electronic','和哈啦啦啦','啦咯啦咯啦咯啦咯',1.00,2.00,'2026-05-07','couple',0,1,2,1,100,NULL,'2026-03-07 00:00:00','2026-03-07 01:53:01','2026-03-09 12:44:06',1),(14,1,1,'electronic','买摩托车','',10000.00,20000.00,'2026-12-07','couple',0,1,1,1,0,NULL,NULL,'2026-03-07 23:51:09','2026-03-09 12:44:00',1),(15,1,1,'dinner','就手机关机了','经贸',NULL,NULL,'2026-03-08','couple',0,1,1,1,0,NULL,NULL,'2026-03-08 21:49:18','2026-03-09 12:43:58',1),(16,1,1,'dinner','看樱花','去无锡看，在南京看，到处看',NULL,NULL,'2026-03-22','couple',0,1,1,1,0,NULL,NULL,'2026-03-09 12:30:18','2026-03-09 12:30:22',0),(17,1,1,'dinner','周末干嘛呢','周末出去玩还是在家待着睡觉还是逛街？',NULL,NULL,'2026-03-14','couple',0,1,0,NULL,0,NULL,NULL,'2026-03-09 12:49:28','2026-03-09 12:49:28',0),(18,1,1,'gift','测试删除','',NULL,NULL,NULL,'couple',0,1,2,1,100,NULL,'2026-03-09 00:00:00','2026-03-09 22:45:02','2026-03-09 22:45:08',0),(19,1,1,'gift','测试图','',NULL,NULL,NULL,'couple',0,1,0,NULL,0,NULL,NULL,'2026-03-11 23:43:20','2026-03-12 00:07:28',1),(20,1,1,'gift','测试','',NULL,NULL,NULL,'couple',0,1,0,NULL,0,NULL,NULL,'2026-03-11 23:43:26','2026-03-11 23:44:56',1),(21,1,1,'gift','测试哦哦哦他那','',NULL,NULL,NULL,'couple',0,1,2,1,100,NULL,'2026-03-11 00:00:00','2026-03-11 23:43:34','2026-03-11 23:44:52',0),(22,1,1,'gift','你明明','',NULL,NULL,NULL,'couple',0,1,0,NULL,0,NULL,NULL,'2026-03-11 23:43:43','2026-03-11 23:44:35',1),(23,1,1,'gift','测试','',1.00,1.00,'2026-03-11','couple',0,1,1,1,0,NULL,NULL,'2026-03-11 23:44:07','2026-03-11 23:44:24',1),(24,1,1,'electronic','123','456',1.00,3.00,'2026-04-12','couple',0,1,2,1,100,NULL,'2026-03-12 00:00:00','2026-03-12 00:07:49','2026-03-12 00:07:57',0);
/*!40000 ALTER TABLE `wish` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'family_app'
--
/*!50003 DROP PROCEDURE IF EXISTS `add_column_if_not_exists` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = latin1 */ ;
/*!50003 SET character_set_results = latin1 */ ;
/*!50003 SET collation_connection  = latin1_swedish_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `add_column_if_not_exists`(
    IN p_table_name VARCHAR(255),
    IN p_column_name VARCHAR(255),
    IN p_column_def VARCHAR(255)
)
BEGIN
    SET @col_exist = (
        SELECT COUNT(*)
        FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = 'family_app'
        AND TABLE_NAME = p_table_name
        AND COLUMN_NAME = p_column_name
    );

    IF @col_exist = 0 THEN
        SET @sql = CONCAT('ALTER TABLE ', p_table_name, ' ADD COLUMN ', p_column_name, ' ', p_column_def);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
SET @@SESSION.SQL_LOG_BIN = @MYSQLDUMP_TEMP_LOG_BIN;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-03-16 22:57:52
