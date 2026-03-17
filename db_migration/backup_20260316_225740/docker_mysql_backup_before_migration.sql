mysqldump: [Warning] Using a password on the command line interface can be insecure.
-- MySQL dump 10.13  Distrib 8.0.45, for Linux (aarch64)
--
-- Host: localhost    Database: family_app
-- ------------------------------------------------------
-- Server version	8.0.45

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

--
-- Table structure for table `anniversary`
--

DROP TABLE IF EXISTS `anniversary`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `anniversary` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `family_id` bigint NOT NULL,
  `user_id` bigint DEFAULT NULL,
  `title` varchar(200) NOT NULL,
  `date` date NOT NULL,
  `type` varchar(50) DEFAULT 'birthday',
  `description` text,
  `repeat_type` varchar(20) DEFAULT 'yearly',
  `is_advance_remind` tinyint DEFAULT '1',
  `advance_days` int DEFAULT '3',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_family_id` (`family_id`),
  KEY `idx_date` (`date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `anniversary`
--

LOCK TABLES `anniversary` WRITE;
/*!40000 ALTER TABLE `anniversary` DISABLE KEYS */;
/*!40000 ALTER TABLE `anniversary` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `anniversary_reminder`
--

DROP TABLE IF EXISTS `anniversary_reminder`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `anniversary_reminder` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `anniversary_id` bigint NOT NULL COMMENT 'çºªå¿µæ—¥ID',
  `reminder_days` int DEFAULT '7' COMMENT 'æå‰å‡ å¤©æé†’',
  `reminder_type` enum('app','sms','email') DEFAULT 'app' COMMENT 'æé†’æ–¹å¼',
  `is_enabled` tinyint DEFAULT '1' COMMENT 'æ˜¯å¦å¯ç”¨',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='çºªå¿µæ—¥æé†’è®¾ç½®è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `anniversary_reminder`
--

LOCK TABLES `anniversary_reminder` WRITE;
/*!40000 ALTER TABLE `anniversary_reminder` DISABLE KEYS */;
/*!40000 ALTER TABLE `anniversary_reminder` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `api_log`
--

DROP TABLE IF EXISTS `api_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `api_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint DEFAULT NULL,
  `method` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `request_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `request_body` text COLLATE utf8mb4_unicode_ci,
  `response_status` int DEFAULT NULL,
  `response_body` text COLLATE utf8mb4_unicode_ci,
  `ip_address` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `execute_time` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT NULL,
  `reminder_time` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `api_log`
--

LOCK TABLES `api_log` WRITE;
/*!40000 ALTER TABLE `api_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `api_log` ENABLE KEYS */;
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
  `title` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `date` date DEFAULT NULL,
  `type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT 'birthday',
  `description` text COLLATE utf8mb4_unicode_ci,
  `creator_id` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  `date_type` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'solar',
  `target_date` date DEFAULT NULL,
  `is_recurring` tinyint DEFAULT '0',
  `reminder_days` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `related_user_id` bigint DEFAULT NULL,
  `images` text COLLATE utf8mb4_unicode_ci,
  `icon` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `calendar_anniversary`
--

LOCK TABLES `calendar_anniversary` WRITE;
/*!40000 ALTER TABLE `calendar_anniversary` DISABLE KEYS */;
/*!40000 ALTER TABLE `calendar_anniversary` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `family`
--

DROP TABLE IF EXISTS `family`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `family` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `description` varchar(500) DEFAULT NULL,
  `avatar` varchar(255) DEFAULT NULL,
  `creator_id` bigint DEFAULT NULL COMMENT 'åˆ›å»ºè€…ID',
  `member_count` int DEFAULT '1' COMMENT 'æˆå‘˜æ•°',
  `storage_used` bigint DEFAULT '0',
  `storage_limit` bigint DEFAULT '10737418240',
  `owner_id` bigint DEFAULT NULL,
  `invite_code` varchar(20) DEFAULT NULL,
  `status` tinyint DEFAULT '1',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `family`
--

LOCK TABLES `family` WRITE;
/*!40000 ALTER TABLE `family` DISABLE KEYS */;
INSERT INTO `family` VALUES (1,'å¹¸ç¦å°å®¶',NULL,NULL,NULL,1,0,10737418240,1,NULL,1,'2026-02-27 01:39:35','2026-02-28 15:59:47'),(2,'我的家庭',NULL,NULL,NULL,1,0,10737418240,1,NULL,1,'2026-02-27 01:17:56','2026-02-27 01:17:56'),(3,'我的家庭',NULL,NULL,NULL,1,0,10737418240,1,NULL,1,'2026-02-27 01:18:04','2026-02-27 01:18:04'),(5,'幸福小家',NULL,NULL,NULL,1,0,10737418240,1,NULL,1,'2026-02-27 03:07:00','2026-02-27 03:07:00'),(6,'Updated Family Name',NULL,NULL,NULL,1,0,10737418240,1,NULL,1,'2026-02-27 13:09:47','2026-02-27 13:11:09'),(7,'我的家庭',NULL,NULL,NULL,1,0,10737418240,1,NULL,1,'2026-02-27 17:30:46','2026-02-27 17:30:46'),(8,'我的家庭',NULL,NULL,NULL,1,0,10737418240,1,NULL,1,'2026-02-27 21:38:40','2026-02-27 21:38:40'),(9,'测试家庭',NULL,NULL,NULL,1,0,10737418240,1,NULL,1,'2026-02-27 23:22:55','2026-02-27 23:22:55'),(10,'测试家庭2',NULL,NULL,NULL,1,0,10737418240,1,NULL,1,'2026-02-27 23:37:41','2026-02-27 23:37:41'),(11,'最终测试家庭',NULL,NULL,NULL,1,0,10737418240,1,NULL,1,'2026-02-28 00:04:32','2026-02-28 00:04:32');
/*!40000 ALTER TABLE `family` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `family_invite_code`
--

DROP TABLE IF EXISTS `family_invite_code`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `family_invite_code` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `family_id` bigint NOT NULL,
  `creator_id` bigint DEFAULT NULL,
  `code` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `expire_time` datetime DEFAULT NULL,
  `max_uses` int DEFAULT '1',
  `used_count` int DEFAULT '0',
  `expires_at` datetime DEFAULT NULL,
  `used_by` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` tinyint DEFAULT '1',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `family_invite_code`
--

LOCK TABLES `family_invite_code` WRITE;
/*!40000 ALTER TABLE `family_invite_code` DISABLE KEYS */;
INSERT INTO `family_invite_code` VALUES (1,3,1,'E0D18521',NULL,5,0,'2026-03-29 01:18:04',NULL,1,'2026-02-26 17:18:04'),(2,4,1,'B8DB15B8',NULL,5,0,'2026-03-29 01:39:35',NULL,1,'2026-02-26 17:39:35'),(3,1,1,'A4FC2DB3',NULL,5,0,'2026-03-29 02:45:05',NULL,1,'2026-02-26 18:45:05'),(4,5,1,'0957025E',NULL,5,0,'2026-03-29 03:07:00',NULL,1,'2026-02-26 19:07:00'),(5,6,1,'EEBCE18C',NULL,5,0,'2026-03-29 13:09:47',NULL,1,'2026-02-27 05:09:47'),(6,7,1,'5EC5FCAB',NULL,5,0,'2026-03-29 17:30:46',NULL,1,'2026-02-27 09:30:46'),(7,8,1,'3E76A606',NULL,5,0,'2026-03-29 21:38:40',NULL,1,'2026-02-27 13:38:40'),(8,9,1,'585EE484',NULL,5,0,'2026-03-29 23:22:55',NULL,1,'2026-02-27 15:22:55'),(9,10,1,'5484910C',NULL,5,0,'2026-03-29 23:37:41',NULL,1,'2026-02-27 15:37:41'),(10,11,1,'C75B9147',NULL,5,0,'2026-03-30 00:04:32',NULL,1,'2026-02-27 16:04:32'),(11,1,NULL,'111222','2026-12-31 23:59:59',100,0,'2036-03-08 23:59:59',NULL,1,'2026-03-06 18:20:39');
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
  `nickname` varchar(50) DEFAULT NULL COMMENT 'å®¶åº­å†…æ˜µç§°',
  `role` varchar(20) DEFAULT 'member',
  `status` tinyint DEFAULT '1',
  `join_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_family_user` (`family_id`,`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `family_member`
--

LOCK TABLES `family_member` WRITE;
/*!40000 ALTER TABLE `family_member` DISABLE KEYS */;
/*!40000 ALTER TABLE `family_member` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `family_report`
--

DROP TABLE IF EXISTS `family_report`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `family_report` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `family_id` bigint NOT NULL COMMENT 'å®¶åº­ID',
  `report_type` enum('weekly','monthly','yearly') NOT NULL COMMENT 'æŠ¥å‘Šç±»åž‹',
  `report_date` date NOT NULL COMMENT 'æŠ¥å‘Šæ—¥æœŸ',
  `title` varchar(100) NOT NULL COMMENT 'æŠ¥å‘Šæ ‡é¢˜',
  `content` json DEFAULT NULL COMMENT 'æŠ¥å‘Šå†…å®¹JSON',
  `score` int DEFAULT NULL COMMENT 'ç»¼åˆè¯„åˆ†',
  `is_read` tinyint DEFAULT '0' COMMENT 'æ˜¯å¦å·²è¯»',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='å®¶åº­æŠ¥å‘Šè¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `family_report`
--

LOCK TABLES `family_report` WRITE;
/*!40000 ALTER TABLE `family_report` DISABLE KEYS */;
/*!40000 ALTER TABLE `family_report` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notification`
--

DROP TABLE IF EXISTS `notification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notification` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `title` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `content` text COLLATE utf8mb4_unicode_ci,
  `type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `is_read` tinyint DEFAULT '0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notification`
--

LOCK TABLES `notification` WRITE;
/*!40000 ALTER TABLE `notification` DISABLE KEYS */;
/*!40000 ALTER TABLE `notification` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `recipe`
--

DROP TABLE IF EXISTS `recipe`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `recipe` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `family_id` bigint DEFAULT NULL COMMENT 'å®¶åº­ID NULLä¸ºç³»ç»Ÿèœè°±',
  `name` varchar(100) DEFAULT NULL,
  `description` text COMMENT 'ç®€ä»‹',
  `category` varchar(50) DEFAULT NULL COMMENT 'åˆ†ç±»',
  `cuisine` varchar(50) DEFAULT NULL COMMENT 'èœç³»',
  `difficulty` tinyint DEFAULT '2' COMMENT 'éš¾åº¦ 1ç®€å• 2ä¸­ç­‰ 3å›°éš¾',
  `time` int DEFAULT NULL COMMENT 'çƒ¹é¥ªæ—¶é—´åˆ†é’Ÿ',
  `servings` int DEFAULT '2' COMMENT 'ä»½é‡',
  `cover_image` varchar(255) DEFAULT NULL COMMENT 'å°é¢å›¾',
  `images` json DEFAULT NULL COMMENT 'æ­¥éª¤å›¾åˆ—è¡¨',
  `ingredients` json DEFAULT NULL COMMENT 'é£Ÿææ¸…å•',
  `steps` json DEFAULT NULL COMMENT 'æ­¥éª¤è¯¦æƒ…',
  `nutrition` json DEFAULT NULL COMMENT 'è¥å…»æˆåˆ†',
  `tags` json DEFAULT NULL COMMENT 'æ ‡ç­¾',
  `source` enum('system','custom','ai') DEFAULT 'custom' COMMENT 'æ¥æº',
  `creator_id` bigint DEFAULT NULL COMMENT 'åˆ›å»ºè€…',
  `favorite_count` int DEFAULT '0' COMMENT 'æ”¶è—æ•°',
  `make_count` int DEFAULT '0' COMMENT 'åšè¿‡æ¬¡æ•°',
  `rating` decimal(2,1) DEFAULT '5.0' COMMENT 'è¯„åˆ†',
  `status` tinyint DEFAULT '1' COMMENT 'çŠ¶æ€ 0ç¦ç”¨ 1æ­£å¸¸',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `reminder_time` varchar(50) DEFAULT NULL,
  `cooking_time` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_family_id` (`family_id`),
  KEY `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='èœè°±è¡¨';
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
  `user_id` bigint NOT NULL COMMENT 'ç”¨æˆ·ID',
  `family_id` bigint DEFAULT NULL COMMENT 'å®¶åº­ID',
  `recipe_id` bigint DEFAULT NULL COMMENT 'èœè°±ID',
  `recipe_name` varchar(100) DEFAULT NULL COMMENT 'èœè°±åç§°',
  `image_url` varchar(255) DEFAULT NULL COMMENT 'å›¾ç‰‡URL',
  `cook_time` datetime DEFAULT NULL COMMENT 'çƒ¹é¥ªæ—¶é—´',
  `remark` varchar(500) DEFAULT NULL COMMENT 'å¤‡æ³¨',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_family_id` (`family_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='èœè°±è®°å½•è¡¨';
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
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `reminder_name` varchar(100) NOT NULL COMMENT 'æé†’åç§°',
  `reminder_type` varchar(20) DEFAULT 'SYSTEM' COMMENT 'æé†’ç±»åž‹ï¼šWATER/MEDICINE/BIRTHDAY/FINANCE/EXPIRE/SYSTEM',
  `create_type` tinyint DEFAULT '1' COMMENT 'åˆ›å»ºç±»åž‹ï¼š1-ç”¨æˆ·åˆ›å»º 2-ç³»ç»Ÿåˆ›å»º',
  `create_by` bigint DEFAULT NULL COMMENT 'åˆ›å»ºäººID',
  `push_scope` varchar(20) DEFAULT 'SELF' COMMENT 'æŽ¨é€èŒƒå›´ï¼šSELF-ä»…è‡ªå·± ALL-å…¨éƒ¨ç”¨æˆ· SPECIFIED-æŒ‡å®šç”¨æˆ·',
  `target_user_ids` text COMMENT 'ç›®æ ‡ç”¨æˆ·IDåˆ—è¡¨(JSONæ ¼å¼)',
  `cron_expression` varchar(50) DEFAULT NULL COMMENT 'Cronè¡¨è¾¾å¼ï¼ˆå…¼å®¹æ—§ç‰ˆï¼‰',
  `remind_time` varchar(10) DEFAULT NULL COMMENT 'æé†’æ—¶é—´ï¼Œå¦‚ 08:00',
  `frequency_type` varchar(20) DEFAULT NULL COMMENT 'é¢‘çŽ‡ç±»åž‹ï¼šONCE/DAILY/HOURLY/WEEKLY/MONTHLY/YEARLY/INTERVAL',
  `frequency_config` text COMMENT 'é¢‘çŽ‡é…ç½®(JSONæ ¼å¼)',
  `title_template` varchar(200) DEFAULT NULL COMMENT 'æ ‡é¢˜æ¨¡æ¿',
  `content_template` text COMMENT 'å†…å®¹æ¨¡æ¿',
  `business_data` text COMMENT 'ä¸šåŠ¡æ•°æ®(JSONæ ¼å¼)',
  `next_execute_time` datetime DEFAULT NULL COMMENT 'ä¸‹æ¬¡æ‰§è¡Œæ—¶é—´',
  `last_execute_time` datetime DEFAULT NULL COMMENT 'ä¸Šæ¬¡æ‰§è¡Œæ—¶é—´',
  `last_execute_result` varchar(20) DEFAULT NULL COMMENT 'æ‰§è¡Œç»“æžœï¼šSUCCESS/FAILED/NO_TARGET',
  `execute_count` int DEFAULT '0' COMMENT 'å·²æ‰§è¡Œæ¬¡æ•°',
  `max_execute_count` int DEFAULT '0' COMMENT 'æœ€å¤§æ‰§è¡Œæ¬¡æ•°ï¼ˆ0æ— é™ï¼‰',
  `status` tinyint DEFAULT '1' COMMENT 'çŠ¶æ€ï¼š0-åœç”¨ 1-å¯ç”¨',
  `priority` tinyint DEFAULT '5' COMMENT 'ä¼˜å…ˆçº§1-10',
  `quiet_hours_start` varchar(10) DEFAULT NULL COMMENT 'å…æ‰“æ‰°å¼€å§‹æ—¶é—´ï¼š22:00',
  `quiet_hours_end` varchar(10) DEFAULT NULL COMMENT 'å…æ‰“æ‰°ç»“æŸæ—¶é—´ï¼š08:00',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  PRIMARY KEY (`id`),
  KEY `idx_create_by` (`create_by`),
  KEY `idx_status` (`status`),
  KEY `idx_next_execute` (`next_execute_time`),
  KEY `idx_frequency_type` (`frequency_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='æé†’é…ç½®è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reminder_config`
--

LOCK TABLES `reminder_config` WRITE;
/*!40000 ALTER TABLE `reminder_config` DISABLE KEYS */;
/*!40000 ALTER TABLE `reminder_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `stats_cache`
--

DROP TABLE IF EXISTS `stats_cache`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `stats_cache` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `stat_type` varchar(50) NOT NULL COMMENT 'ç»Ÿè®¡ç±»åž‹',
  `target_id` bigint NOT NULL COMMENT 'ç›®æ ‡IDï¼ˆç”¨æˆ·IDæˆ–å®¶åº­IDï¼‰',
  `stat_date` date NOT NULL COMMENT 'ç»Ÿè®¡æ—¥æœŸ',
  `data` json DEFAULT NULL COMMENT 'ç»Ÿè®¡ç»“æžœJSON',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_type_target_date` (`stat_type`,`target_id`,`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ç»Ÿè®¡ç¼“å­˜è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `stats_cache`
--

LOCK TABLES `stats_cache` WRITE;
/*!40000 ALTER TABLE `stats_cache` DISABLE KEYS */;
/*!40000 ALTER TABLE `stats_cache` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_config`
--

DROP TABLE IF EXISTS `sys_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_config` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `config_key` varchar(100) NOT NULL COMMENT '配置键',
  `config_value` text COMMENT '配置值',
  `description` varchar(255) DEFAULT NULL COMMENT '配置说明',
  `category` varchar(50) DEFAULT 'general' COMMENT '配置分类',
  `is_encrypted` tinyint DEFAULT '0' COMMENT '是否加密存储',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`),
  KEY `idx_category` (`category`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_config`
--

LOCK TABLES `sys_config` WRITE;
/*!40000 ALTER TABLE `sys_config` DISABLE KEYS */;
INSERT INTO `sys_config` VALUES (1,'wechat.work.corpid','ww6c1c7590db91ef85','企业微信CorpID','wechat_work',0,'2026-03-13 05:51:23','2026-03-13 05:51:23'),(2,'wechat.work.agentid','1000002','企业微信应用AgentID','wechat_work',0,'2026-03-13 05:51:23','2026-03-13 05:51:23'),(3,'wechat.work.secret','Ne0oN5Y8mNmRA_wkIP7I4PMn_sr2GFPkbBABqUaEEE4','企业微信应用Secret','wechat_work',0,'2026-03-13 05:51:23','2026-03-13 05:51:23'),(4,'wechat.work.userid','XIAOZHUSHOU','企业微信小助手UserID','wechat_work',0,'2026-03-13 05:51:23','2026-03-13 05:59:54');
/*!40000 ALTER TABLE `sys_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_operation_log`
--

DROP TABLE IF EXISTS `sys_operation_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_operation_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint DEFAULT NULL,
  `username` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `operation` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `method` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `params` text COLLATE utf8mb4_unicode_ci,
  `result` text COLLATE utf8mb4_unicode_ci,
  `ip` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `module` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_operation_log`
--

LOCK TABLES `sys_operation_log` WRITE;
/*!40000 ALTER TABLE `sys_operation_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_operation_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_user`
--

DROP TABLE IF EXISTS `sys_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` varchar(100) NOT NULL,
  `nickname` varchar(50) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `avatar` varchar(255) DEFAULT NULL,
  `status` tinyint DEFAULT '1',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `gender` tinyint DEFAULT NULL,
  `birthday` date DEFAULT NULL,
  `height` decimal(5,2) DEFAULT NULL,
  `weight` decimal(5,2) DEFAULT NULL,
  `target_weight` decimal(5,2) DEFAULT NULL,
  `daily_calories` int DEFAULT NULL,
  `open_id` varchar(100) DEFAULT NULL,
  `union_id` varchar(100) DEFAULT NULL,
  `login_type` tinyint DEFAULT NULL,
  `last_login_time` date DEFAULT NULL,
  `last_login_ip` varchar(50) DEFAULT NULL,
  `current_family_id` bigint DEFAULT NULL COMMENT 'å½“å‰å®¶åº­ID',
  `work_user_id` varchar(100) DEFAULT NULL COMMENT '企业微信成员ID（内部通讯录）',
  `external_user_id` varchar(100) DEFAULT NULL COMMENT '企业微信外部联系人ID（客户联系）',
  `wx_openid` varchar(100) DEFAULT NULL COMMENT 'å¾®ä¿¡openid',
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_user`
--

LOCK TABLES `sys_user` WRITE;
/*!40000 ALTER TABLE `sys_user` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `task`
--

DROP TABLE IF EXISTS `task`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `task` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `family_id` bigint DEFAULT NULL,
  `creator_id` bigint DEFAULT NULL,
  `assignee_id` bigint DEFAULT NULL,
  `title` varchar(200) NOT NULL,
  `content` text COMMENT 'å†…å®¹æè¿°',
  `remark` text COMMENT 'å¤‡æ³¨',
  `description` text,
  `status` tinyint DEFAULT '0',
  `is_archived` tinyint DEFAULT '0' COMMENT 'æ˜¯å¦å½’æ¡£ 0å¦ 1æ˜¯',
  `archive_time` datetime DEFAULT NULL COMMENT 'å½’æ¡£æ—¶é—´',
  `sort_order` int DEFAULT '0' COMMENT 'æŽ’åº',
  `is_deleted` tinyint DEFAULT '0' COMMENT 'æ˜¯å¦åˆ é™¤ 0å¦ 1æ˜¯',
  `delete_time` datetime DEFAULT NULL COMMENT 'åˆ é™¤æ—¶é—´',
  `due_time` datetime DEFAULT NULL,
  `remind_time` datetime DEFAULT NULL COMMENT 'æé†’æ—¶é—´',
  `reminder_type` varchar(20) DEFAULT 'time',
  `location` varchar(200) DEFAULT NULL COMMENT 'åœ°ç‚¹',
  `attachments` json DEFAULT NULL COMMENT 'é™„ä»¶åˆ—è¡¨',
  `finish_time` datetime DEFAULT NULL COMMENT 'å®Œæˆæ—¶é—´',
  `complete_time` datetime DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `task`
--

LOCK TABLES `task` WRITE;
/*!40000 ALTER TABLE `task` DISABLE KEYS */;
/*!40000 ALTER TABLE `task` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `task_category`
--

DROP TABLE IF EXISTS `task_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `task_category` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `family_id` bigint NOT NULL COMMENT 'å®¶åº­ID',
  `name` varchar(50) NOT NULL COMMENT 'åˆ†ç±»åç§°',
  `icon` varchar(50) DEFAULT NULL COMMENT 'å›¾æ ‡',
  `color` varchar(20) DEFAULT NULL COMMENT 'é¢œè‰²',
  `sort_order` int DEFAULT '0' COMMENT 'æŽ’åº',
  `status` tinyint DEFAULT '1' COMMENT 'çŠ¶æ€',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ä»»åŠ¡åˆ†ç±»è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `task_category`
--

LOCK TABLES `task_category` WRITE;
/*!40000 ALTER TABLE `task_category` DISABLE KEYS */;
/*!40000 ALTER TABLE `task_category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `task_reminder`
--

DROP TABLE IF EXISTS `task_reminder`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `task_reminder` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `task_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `remind_time` datetime NOT NULL,
  `remind_type` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `location_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `location_lat` decimal(10,7) DEFAULT NULL,
  `location_lng` decimal(10,7) DEFAULT NULL,
  `radius` int DEFAULT '100',
  `is_triggered` tinyint DEFAULT '0',
  `status` tinyint DEFAULT '0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `reminder_type` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'time',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `task_reminder`
--

LOCK TABLES `task_reminder` WRITE;
/*!40000 ALTER TABLE `task_reminder` DISABLE KEYS */;
/*!40000 ALTER TABLE `task_reminder` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `task_schedule`
--

DROP TABLE IF EXISTS `task_schedule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `task_schedule` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `family_id` bigint NOT NULL COMMENT 'å®¶åº­ID',
  `task_name` varchar(100) NOT NULL COMMENT 'ä»»åŠ¡åç§°',
  `assignee_id` bigint NOT NULL COMMENT 'æŒ‡æ´¾ç”¨æˆ·ID',
  `schedule_type` enum('daily','weekly','monthly') DEFAULT 'weekly' COMMENT 'æŽ’ç­ç±»åž‹',
  `schedule_day` int DEFAULT NULL COMMENT 'å‘¨å‡ /å‡ å·',
  `status` tinyint DEFAULT '1' COMMENT '0åœç”¨ 1å¯ç”¨',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='å®¶åŠ¡æŽ’ç­è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `task_schedule`
--

LOCK TABLES `task_schedule` WRITE;
/*!40000 ALTER TABLE `task_schedule` DISABLE KEYS */;
/*!40000 ALTER TABLE `task_schedule` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_activity`
--

DROP TABLE IF EXISTS `user_activity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_activity` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT 'ç”¨æˆ·ID',
  `date` date NOT NULL COMMENT 'æ—¥æœŸ',
  `login_count` int DEFAULT '0' COMMENT 'ç™»å½•æ¬¡æ•°',
  `task_completed` int DEFAULT '0' COMMENT 'å®Œæˆä»»åŠ¡æ•°',
  `diet_recorded` int DEFAULT '0' COMMENT 'é¥®é£Ÿè®°å½•æ•°',
  `active_minutes` int DEFAULT '0' COMMENT 'æ´»è·ƒåˆ†é’Ÿæ•°',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_date` (`user_id`,`date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ç”¨æˆ·æ´»è·ƒåº¦è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_activity`
--

LOCK TABLES `user_activity` WRITE;
/*!40000 ALTER TABLE `user_activity` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_activity` ENABLE KEYS */;
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
  `user_id` bigint DEFAULT NULL,
  `type` varchar(50) DEFAULT 'custom',
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
  `images` text,
  `finish_time` datetime DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_family_id` (`family_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wish`
--

LOCK TABLES `wish` WRITE;
/*!40000 ALTER TABLE `wish` DISABLE KEYS */;
/*!40000 ALTER TABLE `wish` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wish_budget`
--

DROP TABLE IF EXISTS `wish_budget`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wish_budget` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `wish_id` bigint NOT NULL COMMENT 'å¿ƒæ„¿ID',
  `estimated_amount` decimal(10,2) DEFAULT NULL COMMENT 'é¢„ä¼°é‡‘é¢',
  `actual_amount` decimal(10,2) DEFAULT NULL COMMENT 'å®žé™…é‡‘é¢',
  `currency` varchar(10) DEFAULT 'CNY' COMMENT 'è´§å¸',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='å¿ƒæ„¿é¢„ç®—è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wish_budget`
--

LOCK TABLES `wish_budget` WRITE;
/*!40000 ALTER TABLE `wish_budget` DISABLE KEYS */;
/*!40000 ALTER TABLE `wish_budget` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wish_milestone`
--

DROP TABLE IF EXISTS `wish_milestone`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wish_milestone` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `wish_id` bigint NOT NULL COMMENT 'å¿ƒæ„¿ID',
  `title` varchar(100) NOT NULL COMMENT 'é‡Œç¨‹ç¢‘æ ‡é¢˜',
  `description` text COMMENT 'æè¿°',
  `target_date` date DEFAULT NULL COMMENT 'ç›®æ ‡æ—¥æœŸ',
  `is_completed` tinyint DEFAULT '0' COMMENT 'æ˜¯å¦å®Œæˆ',
  `sort_order` int DEFAULT '0' COMMENT 'æŽ’åº',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='å¿ƒæ„¿é‡Œç¨‹ç¢‘è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wish_milestone`
--

LOCK TABLES `wish_milestone` WRITE;
/*!40000 ALTER TABLE `wish_milestone` DISABLE KEYS */;
/*!40000 ALTER TABLE `wish_milestone` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-03-16 14:58:29
