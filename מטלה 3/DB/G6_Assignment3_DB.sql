CREATE DATABASE  IF NOT EXISTS `bistro_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `bistro_db`;
-- MySQL dump 10.13  Distrib 8.0.44, for Win64 (x86_64)
--
-- Host: localhost    Database: bistro_db
-- ------------------------------------------------------
-- Server version	8.0.44

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `monthly_reports`
--

DROP TABLE IF EXISTS `monthly_reports`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `monthly_reports` (
  `report_id` int NOT NULL AUTO_INCREMENT,
  `report_month` int NOT NULL,
  `report_year` int NOT NULL,
  `report_type` varchar(50) DEFAULT NULL,
  `report_content` text,
  PRIMARY KEY (`report_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `monthly_reports`
--

LOCK TABLES `monthly_reports` WRITE;
/*!40000 ALTER TABLE `monthly_reports` DISABLE KEYS */;
/*!40000 ALTER TABLE `monthly_reports` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders` (
  `order_number` int NOT NULL AUTO_INCREMENT,
  `user_id` int DEFAULT NULL,
  `order_date` date NOT NULL,
  `order_time` time NOT NULL,
  `num_of_diners` int NOT NULL,
  `status` enum('PENDING','APPROVED','ACTIVE','FINISHED','CANCELLED') DEFAULT 'PENDING',
  `confirmation_code` int DEFAULT NULL,
  `actual_arrival_time` time DEFAULT NULL,
  `leaving_time` time DEFAULT NULL,
  `is_bill_sent` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`order_number`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `orders_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=156 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders`
--

LOCK TABLES `orders` WRITE;
/*!40000 ALTER TABLE `orders` DISABLE KEYS */;
INSERT INTO `orders` VALUES (77,NULL,'2025-10-02','12:00:00',2,'FINISHED',1001,'12:05:00','13:15:00',1),(78,NULL,'2025-10-03','19:00:00',4,'CANCELLED',1002,NULL,NULL,0),(79,1,'2025-10-05','13:00:00',3,'FINISHED',1003,'13:10:00','14:30:00',1),(80,NULL,'2025-10-07','20:00:00',2,'FINISHED',1004,'20:05:00','21:30:00',1),(81,2,'2025-10-08','18:30:00',6,'FINISHED',1005,'18:30:00','20:00:00',1),(82,NULL,'2025-10-10','12:30:00',4,'FINISHED',1006,'12:35:00','13:45:00',1),(83,NULL,'2025-10-12','14:00:00',5,'FINISHED',1007,'14:00:00','15:30:00',1),(84,4,'2025-10-15','19:30:00',2,'FINISHED',1008,'19:40:00','21:00:00',1),(85,NULL,'2025-10-18','20:30:00',8,'CANCELLED',1009,NULL,NULL,0),(86,NULL,'2025-10-20','13:00:00',3,'FINISHED',1010,'13:05:00','14:15:00',1),(87,NULL,'2025-10-22','18:00:00',4,'FINISHED',1011,'18:00:00','19:30:00',1),(88,3,'2025-10-25','12:00:00',2,'FINISHED',1012,'12:10:00','13:10:00',1),(89,NULL,'2025-10-27','19:00:00',5,'FINISHED',1013,'19:00:00','20:45:00',1),(90,2,'2025-10-29','21:00:00',2,'FINISHED',1014,'21:05:00','22:15:00',1),(91,NULL,'2025-10-31','20:00:00',6,'FINISHED',1015,'20:15:00','22:00:00',1),(92,NULL,'2025-11-01','12:00:00',2,'FINISHED',1101,'12:00:00','13:00:00',1),(93,1,'2025-11-03','18:30:00',4,'FINISHED',1102,'18:40:00','20:10:00',1),(94,NULL,'2025-11-05','13:30:00',3,'FINISHED',1103,'13:35:00','14:45:00',1),(95,4,'2025-11-08','20:00:00',5,'FINISHED',1104,'20:00:00','21:45:00',1),(96,2,'2025-11-10','19:00:00',2,'FINISHED',1105,'19:05:00','20:20:00',1),(97,NULL,'2025-11-12','12:30:00',6,'CANCELLED',1106,NULL,NULL,0),(98,NULL,'2025-11-14','14:00:00',2,'FINISHED',1107,'14:10:00','15:10:00',1),(99,3,'2025-11-15','20:30:00',4,'FINISHED',1108,'20:30:00','22:00:00',1),(100,NULL,'2025-11-17','13:00:00',3,'FINISHED',1109,'13:00:00','14:30:00',1),(101,1,'2025-11-20','18:00:00',2,'FINISHED',1110,'18:05:00','19:15:00',1),(102,NULL,'2025-11-22','19:30:00',8,'FINISHED',1111,'19:40:00','21:30:00',1),(103,NULL,'2025-11-25','12:00:00',4,'FINISHED',1112,'12:00:00','13:30:00',1),(104,2,'2025-11-27','21:00:00',2,'FINISHED',1113,'21:10:00','22:30:00',1),(105,NULL,'2025-11-28','20:00:00',5,'FINISHED',1114,'20:00:00','21:45:00',1),(106,4,'2025-11-30','13:00:00',6,'FINISHED',1115,'13:10:00','14:50:00',1),(107,NULL,'2025-12-02','12:00:00',2,'FINISHED',1201,'12:05:00','13:00:00',1),(108,NULL,'2025-12-04','19:00:00',8,'FINISHED',1202,'19:00:00','21:30:00',1),(109,1,'2025-12-06','13:30:00',4,'FINISHED',1203,'13:35:00','15:00:00',1),(110,NULL,'2025-12-08','20:00:00',2,'FINISHED',1204,'20:10:00','21:20:00',1),(111,NULL,'2025-12-10','18:30:00',5,'FINISHED',1205,'18:30:00','20:00:00',1),(112,2,'2025-12-12','12:30:00',3,'CANCELLED',1206,NULL,NULL,0),(113,NULL,'2025-12-15','21:00:00',4,'FINISHED',1207,'21:00:00','22:45:00',1),(114,4,'2025-12-18','14:00:00',2,'FINISHED',1208,'14:05:00','15:15:00',1),(115,NULL,'2025-12-20','19:30:00',6,'FINISHED',1209,'19:30:00','21:00:00',1),(116,1,'2025-12-22','13:00:00',2,'FINISHED',1210,'13:10:00','14:10:00',1),(117,NULL,'2025-12-24','18:00:00',10,'FINISHED',1211,'18:00:00','20:30:00',1),(118,NULL,'2025-12-25','20:30:00',5,'FINISHED',1212,'20:45:00','22:30:00',1),(119,NULL,'2025-12-28','12:00:00',3,'FINISHED',1213,'12:00:00','13:30:00',1),(120,2,'2025-12-30','19:00:00',4,'FINISHED',1214,'19:05:00','20:40:00',1),(121,NULL,'2025-12-31','21:30:00',2,'FINISHED',1215,'21:30:00','23:55:00',1),(122,4,'2026-01-01','12:00:00',2,'FINISHED',1301,'12:10:00','13:20:00',1),(123,NULL,'2026-01-02','13:00:00',4,'FINISHED',1302,'13:00:00','14:30:00',1),(124,1,'2026-01-02','19:00:00',2,'FINISHED',1303,'19:05:00','20:15:00',1),(125,NULL,'2026-01-03','18:30:00',6,'FINISHED',1304,'18:30:00','20:00:00',1),(126,NULL,'2026-01-04','12:30:00',3,'FINISHED',1305,'12:35:00','13:50:00',1),(127,3,'2026-01-07','19:30:00',4,'FINISHED',1308,'19:40:00','21:10:00',1),(128,NULL,'2026-01-08','13:00:00',8,'FINISHED',1309,'13:00:00','14:45:00',1),(129,NULL,'2026-01-10','12:00:00',3,'FINISHED',1311,'12:00:00','13:15:00',1),(130,1,'2026-01-11','18:00:00',5,'FINISHED',1312,'18:05:00','19:45:00',1),(131,NULL,'2026-01-12','21:00:00',2,'FINISHED',1313,'21:00:00','22:15:00',1),(132,3,'2026-01-14','13:30:00',6,'FINISHED',1315,'13:30:00','15:00:00',1),(133,NULL,'2026-01-22','21:00:00',2,'APPROVED',5009,NULL,NULL,0),(134,1,'2026-01-22','13:00:00',8,'APPROVED',5010,NULL,NULL,0),(135,NULL,'2026-01-23','19:30:00',4,'APPROVED',5011,NULL,NULL,0),(136,NULL,'2026-01-23','20:30:00',2,'APPROVED',5012,NULL,NULL,0),(137,2,'2026-01-24','12:00:00',3,'APPROVED',5013,NULL,NULL,0),(138,NULL,'2026-01-24','18:30:00',6,'APPROVED',5014,NULL,NULL,0),(139,NULL,'2026-01-25','19:00:00',5,'APPROVED',5015,NULL,NULL,0),(140,NULL,'2026-01-25','20:00:00',2,'APPROVED',5016,NULL,NULL,0),(141,3,'2026-01-26','13:30:00',4,'APPROVED',5017,NULL,NULL,0),(142,NULL,'2026-01-26','19:00:00',2,'APPROVED',5018,NULL,NULL,0),(143,1,'2026-01-27','20:00:00',6,'APPROVED',5019,NULL,NULL,0),(144,NULL,'2026-01-27','21:00:00',2,'APPROVED',5020,NULL,NULL,0),(145,NULL,'2026-01-28','12:30:00',2,'APPROVED',5021,NULL,NULL,0),(146,NULL,'2026-01-28','18:00:00',5,'APPROVED',5022,NULL,NULL,0),(147,NULL,'2026-01-29','19:30:00',4,'APPROVED',5023,NULL,NULL,0),(148,2,'2026-01-29','20:30:00',3,'APPROVED',5024,NULL,NULL,0),(149,NULL,'2026-01-30','13:00:00',2,'APPROVED',5025,NULL,NULL,0),(150,NULL,'2026-01-30','19:00:00',8,'APPROVED',5026,NULL,NULL,0),(151,NULL,'2026-01-31','20:00:00',4,'APPROVED',5027,NULL,NULL,0),(152,1,'2026-01-31','18:30:00',2,'APPROVED',5028,NULL,NULL,0),(153,NULL,'2026-01-21','10:30:00',4,'APPROVED',5007,NULL,NULL,0),(154,NULL,'2026-01-21','11:00:00',2,'APPROVED',5008,NULL,NULL,0),(155,NULL,'2026-01-21','12:30:00',4,'APPROVED',5006,NULL,NULL,0);
/*!40000 ALTER TABLE `orders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `restaurant_tables`
--

DROP TABLE IF EXISTS `restaurant_tables`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `restaurant_tables` (
  `table_id` int NOT NULL,
  `seats` int NOT NULL,
  `status` enum('AVAILABLE','OCCUPIED') DEFAULT 'AVAILABLE',
  `user_id` int DEFAULT NULL,
  PRIMARY KEY (`table_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `restaurant_tables`
--

LOCK TABLES `restaurant_tables` WRITE;
/*!40000 ALTER TABLE `restaurant_tables` DISABLE KEYS */;
INSERT INTO `restaurant_tables` VALUES (1,2,'AVAILABLE',NULL),(2,2,'AVAILABLE',NULL),(3,4,'AVAILABLE',NULL),(4,4,'AVAILABLE',NULL),(5,6,'AVAILABLE',NULL),(6,6,'AVAILABLE',NULL),(7,8,'AVAILABLE',NULL),(8,8,'AVAILABLE',NULL),(9,10,'AVAILABLE',NULL),(10,10,'AVAILABLE',NULL),(11,12,'AVAILABLE',NULL),(12,12,'AVAILABLE',NULL),(13,14,'AVAILABLE',NULL),(14,14,'AVAILABLE',NULL),(15,16,'AVAILABLE',NULL),(16,16,'AVAILABLE',NULL);
/*!40000 ALTER TABLE `restaurant_tables` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `schedule`
--

DROP TABLE IF EXISTS `schedule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `schedule` (
  `identifier` varchar(20) NOT NULL,
  `open_time` varchar(10) DEFAULT NULL,
  `close_time` varchar(10) DEFAULT NULL,
  `is_closed` tinyint(1) DEFAULT '0',
  `schedule_type` varchar(10) DEFAULT NULL,
  `event_name` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`identifier`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `schedule`
--

LOCK TABLES `schedule` WRITE;
/*!40000 ALTER TABLE `schedule` DISABLE KEYS */;
INSERT INTO `schedule` VALUES ('Friday','08:00','22:00',0,'REGULAR',NULL),('Monday','08:00','22:00',0,'REGULAR',NULL),('Saturday','19:00','23:00',0,'REGULAR',NULL),('Sunday','08:00','22:00',0,'REGULAR',NULL),('Thursday','08:00','22:00',0,'REGULAR',NULL),('Tuesday','08:00','22:00',0,'REGULAR',NULL),('Wednesday','08:00','22:00',0,'REGULAR',NULL);
/*!40000 ALTER TABLE `schedule` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `user_id` int NOT NULL AUTO_INCREMENT,
  `phone_number` varchar(20) NOT NULL,
  `email` varchar(100) DEFAULT NULL,
  `first_name` varchar(50) DEFAULT NULL,
  `last_name` varchar(50) DEFAULT NULL,
  `user_type` enum('CASUAL','SUBSCRIBER') NOT NULL DEFAULT 'CASUAL',
  `subscriber_number` int DEFAULT NULL,
  `username` varchar(50) DEFAULT NULL,
  `password` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `subscriber_number` (`subscriber_number`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=53 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'0503063677','oneelabed579@gmail.com','Oneel','Abed','SUBSCRIBER',1,'oneel','pass1'),(2,'0529876543','weaamasad7@gmail.com','Weaam','Asad','SUBSCRIBER',2,'weaam','pass2'),(3,'0509999999','amal@mail.com','Amal','Mulla','SUBSCRIBER',3,'amal','pass3'),(4,'0529999999','danial@mail.com','Danial','Abu Yamn','SUBSCRIBER',4,'danial','pass4'),(39,'0523063688','no-email','Guest','Diner','CASUAL',NULL,'0523063688','casual'),(40,'0524848488','no-email','Guest','Diner','CASUAL',NULL,'0524848488','casual'),(41,'0544646465','no-email','Guest','Diner','CASUAL',NULL,'0544646465','casual'),(42,'0568989833','no-email','Guest','Diner','CASUAL',NULL,'0568989833','casual'),(43,'0532626264','no-email','Guest','Diner','CASUAL',NULL,'0532626264','casual'),(44,'0546978788','no-email','Guest','Diner','CASUAL',NULL,'0546978788','casual'),(45,'0536666666','no-email','Guest','Diner','CASUAL',NULL,'0536666666','casual'),(46,'0541212122','no-email','Guest','Diner','CASUAL',NULL,'0541212122','casual'),(47,'0574646465','no-email','Guest','Diner','CASUAL',NULL,'0574646465','casual'),(50,'0523063677','no-email','Guest','Diner','CASUAL',NULL,'0523063677','casual'),(51,'0502363688','no-email','Guest','Diner','CASUAL',NULL,'0502363688','casual'),(52,'0539999999','oneelabed579@gmail.com','Guest','Diner','CASUAL',NULL,'0539999999','casual');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `waiting_list`
--

DROP TABLE IF EXISTS `waiting_list`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `waiting_list` (
  `waiting_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int DEFAULT NULL,
  `date_requested` date NOT NULL,
  `time_requested` time NOT NULL,
  `num_of_diners` int NOT NULL,
  `status` enum('WAITING','NOTIFIED','FULFILLED','CANCELLED') DEFAULT 'WAITING',
  `confirmation_code` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`waiting_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `waiting_list_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=39 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `waiting_list`
--

LOCK TABLES `waiting_list` WRITE;
/*!40000 ALTER TABLE `waiting_list` DISABLE KEYS */;
INSERT INTO `waiting_list` VALUES (18,NULL,'2025-10-05','12:55:00',4,'CANCELLED','1003'),(19,NULL,'2025-10-12','13:57:00',2,'CANCELLED','1007'),(20,NULL,'2025-10-16','19:23:00',6,'CANCELLED','8008'),(21,1,'2025-10-20','12:50:00',2,'CANCELLED','1010'),(22,NULL,'2025-10-25','11:55:00',5,'CANCELLED','1012'),(23,NULL,'2025-10-31','19:58:00',2,'CANCELLED','1015'),(24,NULL,'2025-11-03','18:25:00',3,'CANCELLED','1102'),(25,2,'2025-11-08','19:53:00',4,'CANCELLED','1104'),(26,NULL,'2025-11-09','18:50:00',2,'CANCELLED','7105'),(27,NULL,'2025-11-20','17:54:00',8,'CANCELLED','1110'),(28,NULL,'2025-11-25','11:50:00',2,'CANCELLED','1112'),(29,NULL,'2025-11-28','19:51:00',5,'CANCELLED','1114'),(30,4,'2025-12-04','18:53:00',2,'CANCELLED','1202'),(31,NULL,'2025-12-10','18:26:00',6,'CANCELLED','1205'),(32,NULL,'2025-12-14','20:55:00',4,'CANCELLED','6207'),(33,4,'2025-12-24','17:53:00',10,'CANCELLED','1211'),(34,NULL,'2025-12-25','20:24:00',2,'CANCELLED','1212'),(35,3,'2025-12-31','21:24:00',4,'CANCELLED','1215'),(36,NULL,'2026-01-04','12:21:00',3,'CANCELLED','1305'),(37,NULL,'2026-01-10','11:58:00',2,'CANCELLED','1311'),(38,NULL,'2026-01-15','13:24:00',5,'CANCELLED','9315');
/*!40000 ALTER TABLE `waiting_list` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-01-17 17:39:33
