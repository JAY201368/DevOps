-- MySQL dump 10.13  Distrib 8.0.42, for Linux (x86_64)
--
-- Host: db4free.net    Database: tomato
-- ------------------------------------------------------
-- Server version	8.3.0

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
-- Table structure for table `advertisements`
--

DROP TABLE IF EXISTS `advertisements`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `advertisements` (
  `id` int NOT NULL AUTO_INCREMENT,
  `content` varchar(500) NOT NULL,
  `image_url` varchar(500) NOT NULL,
  `product_id` int NOT NULL,
  `title` varchar(50) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `advertisements`
--

LOCK TABLES `advertisements` WRITE;
/*!40000 ALTER TABLE `advertisements` DISABLE KEYS */;
/*!40000 ALTER TABLE `advertisements` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `carts`
--

DROP TABLE IF EXISTS `carts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `carts` (
  `cart_item_id` bigint NOT NULL AUTO_INCREMENT,
  `product_id` bigint NOT NULL,
  `quantity` int NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`cart_item_id`),
  KEY `FKmd2ap4oxo3wvgkf4fnaye532i` (`product_id`),
  CONSTRAINT `FKmd2ap4oxo3wvgkf4fnaye532i` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=51 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `carts`
--

LOCK TABLES `carts` WRITE;
/*!40000 ALTER TABLE `carts` DISABLE KEYS */;
INSERT INTO `carts` VALUES (37,45,2,8);
/*!40000 ALTER TABLE `carts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `comments`
--

DROP TABLE IF EXISTS `comments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `comments` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `user_id` bigint NOT NULL COMMENT '评论用户ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `content` text NOT NULL COMMENT '评论内容',
  `rating` decimal(2,1) NOT NULL COMMENT '评分(0-5分)',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `status` tinyint DEFAULT '1' COMMENT '状态(1:正常,0:已删除)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_user_product` (`user_id`,`product_id`),
  KEY `product_id` (`product_id`),
  CONSTRAINT `comments_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `comments_ibfk_2` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品评论表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `comments`
--

LOCK TABLES `comments` WRITE;
/*!40000 ALTER TABLE `comments` DISABLE KEYS */;
/*!40000 ALTER TABLE `comments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_items`
--

DROP TABLE IF EXISTS `order_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_items` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` int NOT NULL,
  `product_id` bigint NOT NULL,
  `quantity` int NOT NULL,
  `price` decimal(10,2) NOT NULL,
  `product_title` varchar(255) NOT NULL,
  `product_cover` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_order_items_orders` (`order_id`),
  KEY `fk_order_items_products` (`product_id`),
  CONSTRAINT `fk_order_items_orders` FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_order_items_products` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=43 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单项表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_items`
--

LOCK TABLES `order_items` WRITE;
/*!40000 ALTER TABLE `order_items` DISABLE KEYS */;
INSERT INTO `order_items` VALUES (33,38,45,1,50.00,'兄弟','https://th.bing.com/th/id/R.c69bb0a78fbc8a57ed1463f015d65058?rik=JlVuPsyDxL6PYw&riu=http%3a%2f%2fi1.hdslb.com%2fbfs%2farchive%2f9dac385d65076147b284cf97b8df4a97a42d146d.jpg&ehk=%2bNli4jgY7nKbwoq%2by5B%2bZL58nyFkrMRmZO7OSyjTw5E%3d&risl=&pid=ImgRaw&r=0'),(34,39,45,2,50.00,'兄弟','https://th.bing.com/th/id/R.c69bb0a78fbc8a57ed1463f015d65058?rik=JlVuPsyDxL6PYw&riu=http%3a%2f%2fi1.hdslb.com%2fbfs%2farchive%2f9dac385d65076147b284cf97b8df4a97a42d146d.jpg&ehk=%2bNli4jgY7nKbwoq%2by5B%2bZL58nyFkrMRmZO7OSyjTw5E%3d&risl=&pid=ImgRaw&r=0'),(35,40,45,2,50.00,'兄弟','https://th.bing.com/th/id/R.c69bb0a78fbc8a57ed1463f015d65058?rik=JlVuPsyDxL6PYw&riu=http%3a%2f%2fi1.hdslb.com%2fbfs%2farchive%2f9dac385d65076147b284cf97b8df4a97a42d146d.jpg&ehk=%2bNli4jgY7nKbwoq%2by5B%2bZL58nyFkrMRmZO7OSyjTw5E%3d&risl=&pid=ImgRaw&r=0'),(36,41,45,2,50.00,'兄弟','https://th.bing.com/th/id/R.c69bb0a78fbc8a57ed1463f015d65058?rik=JlVuPsyDxL6PYw&riu=http%3a%2f%2fi1.hdslb.com%2fbfs%2farchive%2f9dac385d65076147b284cf97b8df4a97a42d146d.jpg&ehk=%2bNli4jgY7nKbwoq%2by5B%2bZL58nyFkrMRmZO7OSyjTw5E%3d&risl=&pid=ImgRaw&r=0'),(37,42,45,2,50.00,'兄弟','https://th.bing.com/th/id/R.c69bb0a78fbc8a57ed1463f015d65058?rik=JlVuPsyDxL6PYw&riu=http%3a%2f%2fi1.hdslb.com%2fbfs%2farchive%2f9dac385d65076147b284cf97b8df4a97a42d146d.jpg&ehk=%2bNli4jgY7nKbwoq%2by5B%2bZL58nyFkrMRmZO7OSyjTw5E%3d&risl=&pid=ImgRaw&r=0'),(38,43,45,1,50.00,'兄弟','https://th.bing.com/th/id/R.c69bb0a78fbc8a57ed1463f015d65058?rik=JlVuPsyDxL6PYw&riu=http%3a%2f%2fi1.hdslb.com%2fbfs%2farchive%2f9dac385d65076147b284cf97b8df4a97a42d146d.jpg&ehk=%2bNli4jgY7nKbwoq%2by5B%2bZL58nyFkrMRmZO7OSyjTw5E%3d&risl=&pid=ImgRaw&r=0'),(39,44,45,1,50.00,'兄弟','https://th.bing.com/th/id/R.c69bb0a78fbc8a57ed1463f015d65058?rik=JlVuPsyDxL6PYw&riu=http%3a%2f%2fi1.hdslb.com%2fbfs%2farchive%2f9dac385d65076147b284cf97b8df4a97a42d146d.jpg&ehk=%2bNli4jgY7nKbwoq%2by5B%2bZL58nyFkrMRmZO7OSyjTw5E%3d&risl=&pid=ImgRaw&r=0'),(40,45,45,2,50.00,'兄弟','https://th.bing.com/th/id/R.c69bb0a78fbc8a57ed1463f015d65058?rik=JlVuPsyDxL6PYw&riu=http%3a%2f%2fi1.hdslb.com%2fbfs%2farchive%2f9dac385d65076147b284cf97b8df4a97a42d146d.jpg&ehk=%2bNli4jgY7nKbwoq%2by5B%2bZL58nyFkrMRmZO7OSyjTw5E%3d&risl=&pid=ImgRaw&r=0'),(41,46,45,2,50.00,'兄弟','https://th.bing.com/th/id/R.c69bb0a78fbc8a57ed1463f015d65058?rik=JlVuPsyDxL6PYw&riu=http%3a%2f%2fi1.hdslb.com%2fbfs%2farchive%2f9dac385d65076147b284cf97b8df4a97a42d146d.jpg&ehk=%2bNli4jgY7nKbwoq%2by5B%2bZL58nyFkrMRmZO7OSyjTw5E%3d&risl=&pid=ImgRaw&r=0'),(42,47,45,2,50.00,'兄弟','https://th.bing.com/th/id/R.c69bb0a78fbc8a57ed1463f015d65058?rik=JlVuPsyDxL6PYw&riu=http%3a%2f%2fi1.hdslb.com%2fbfs%2farchive%2f9dac385d65076147b284cf97b8df4a97a42d146d.jpg&ehk=%2bNli4jgY7nKbwoq%2by5B%2bZL58nyFkrMRmZO7OSyjTw5E%3d&risl=&pid=ImgRaw&r=0');
/*!40000 ALTER TABLE `order_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders` (
  `order_id` int NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `total_amount` decimal(10,2) NOT NULL COMMENT '订单总金额',
  `payment_method` varchar(50) NOT NULL COMMENT '支付方式',
  `status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT '订单支付状态（PENDING, SUCCESS, FAILED, TIMEOUT）',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '订单创建时间',
  `receiver_name` varchar(50) DEFAULT NULL COMMENT '收货人姓名',
  `receiver_phone` varchar(20) DEFAULT NULL COMMENT '收货人手机号',
  `receiver_zipcode` varchar(20) DEFAULT NULL COMMENT '收货人邮编',
  `receiver_address` varchar(255) DEFAULT NULL COMMENT '收货人详细地址',
  `shipping_address` text COMMENT '收货人信息JSON（可选）',
  `alipay_trade_no` varchar(64) DEFAULT NULL,
  `update_time` datetime(6) DEFAULT NULL,
  `expire_time` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`order_id`),
  KEY `fk_orders_user` (`user_id`),
  CONSTRAINT `fk_orders_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=48 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders`
--

LOCK TABLES `orders` WRITE;
/*!40000 ALTER TABLE `orders` DISABLE KEYS */;
INSERT INTO `orders` VALUES (38,4,50.00,'支付宝','CANCELLED','2025-05-08 07:51:44','111','13359223753','111111','11111',NULL,NULL,NULL,'2025-05-08 16:06:44.218000'),(39,4,100.00,'Alipay','CANCELLED','2025-05-08 07:59:08','123','13359223753','123456','111111',NULL,NULL,NULL,'2025-05-08 16:14:07.923000'),(40,4,100.00,'Alipay','CANCELLED','2025-05-08 08:02:13','13','13359223753','123456','11111',NULL,NULL,NULL,'2025-05-08 16:17:13.808000'),(41,4,100.00,'Alipay','CANCELLED','2025-05-08 08:03:54','111','13359223753','123456','11111',NULL,NULL,NULL,'2025-05-08 16:18:54.659000'),(42,4,100.00,'支付宝','CANCELLED','2025-05-22 06:47:03','11','13359223753','123456','ijijijijij',NULL,NULL,NULL,'2025-05-22 15:02:03.569000'),(43,4,50.00,'支付宝','CANCELLED','2025-05-22 06:50:07','111','13359223753','123456','aaaaaaa',NULL,NULL,NULL,'2025-05-22 15:05:07.717000'),(44,4,50.00,'支付宝','PENDING','2025-05-22 06:54:59','11111','13359223753','123456','1232131231232',NULL,NULL,NULL,'2025-05-22 15:09:59.747000'),(45,4,100.00,'支付宝','PENDING','2025-05-22 07:04:09','111','13359223753','123456','123123123123',NULL,NULL,NULL,'2025-05-22 15:19:09.452000'),(46,4,100.00,'支付宝','PENDING','2025-05-22 07:06:40','111','13359223753','123456','123123123',NULL,NULL,NULL,'2025-05-22 15:21:40.411000'),(47,4,100.00,'支付宝','PENDING','2025-05-22 07:23:45','111','13359223753','123456','123456',NULL,NULL,NULL,'2025-05-22 15:38:45.004000');
/*!40000 ALTER TABLE `orders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `products`
--

DROP TABLE IF EXISTS `products`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `products` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `cover` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `detail` varchar(255) DEFAULT NULL,
  `price` decimal(19,2) NOT NULL,
  `rate` double NOT NULL,
  `title` varchar(255) NOT NULL,
  `image_url` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=76 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `products`
--

LOCK TABLES `products` WRITE;
/*!40000 ALTER TABLE `products` DISABLE KEYS */;
INSERT INTO `products` VALUES (45,'https://th.bing.com/th/id/R.c69bb0a78fbc8a57ed1463f015d65058?rik=JlVuPsyDxL6PYw&riu=http%3a%2f%2fi1.hdslb.com%2fbfs%2farchive%2f9dac385d65076147b284cf97b8df4a97a42d146d.jpg&ehk=%2bNli4jgY7nKbwoq%2by5B%2bZL58nyFkrMRmZO7OSyjTw5E%3d&risl=&pid=ImgRaw&r=0','余华小说','lebron Hourunxi',50.00,10,'兄弟',NULL),(46,'http://blue-whale-231250120.oss-cn-nanjing.aliyuncs.com/Snipaste_2024-01-09_23-56-46.png','真相只有一个！！！','休想买得起我',99999999.00,10,'灰原哀',NULL);
/*!40000 ALTER TABLE `products` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `specifications`
--

DROP TABLE IF EXISTS `specifications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `specifications` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `item` varchar(255) NOT NULL,
  `value` varchar(255) NOT NULL,
  `product_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK8mklfk5ymdkx8nfr7rddr60af` (`product_id`),
  CONSTRAINT `FK8mklfk5ymdkx8nfr7rddr60af` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `specifications`
--

LOCK TABLES `specifications` WRITE;
/*!40000 ALTER TABLE `specifications` DISABLE KEYS */;
/*!40000 ALTER TABLE `specifications` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `stockpiles`
--

DROP TABLE IF EXISTS `stockpiles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `stockpiles` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `amount` int NOT NULL,
  `frozen` int NOT NULL,
  `product_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKgfj7xw0trgrvvisavbabl3nd8` (`product_id`),
  CONSTRAINT `FKgfj7xw0trgrvvisavbabl3nd8` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=76 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `stockpiles`
--

LOCK TABLES `stockpiles` WRITE;
/*!40000 ALTER TABLE `stockpiles` DISABLE KEYS */;
INSERT INTO `stockpiles` VALUES (45,99925,0,45),(46,0,0,46);
/*!40000 ALTER TABLE `stockpiles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `avatar` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `location` varchar(255) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` varchar(255) DEFAULT NULL,
  `telephone` varchar(255) DEFAULT NULL,
  `username` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_r43af9ap4edm43mmtq01oddj6` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=43 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (2,NULL,'231250021@smail.hju.edu.cn','方法','侯润曦','$2a$10$FDcVlWm7wynPX0gxoT5pJOnRHkEvKbsiOxubRrRgTE846.v0YKooK','user','18932368080','侯润曦'),(3,NULL,'231250021@smail.hju.edu.cn','方法','侯润洗','$2a$10$63caAHyo1qRlvEJ/DpOe5uzIt8PWnzCIM.8hlEhDiH6ZtoBvi25KW','admin','18932368080','22'),(4,NULL,'231250021@smail.hju.edu.cn','方法','好贵贵','$2a$10$shIIHqCTwBP5UskkEwvd3unUzzcpgFH.qZ8ZemhaBZgx5btKfm27a','user','18932368080','111'),(5,NULL,'231250021@smail.hju.edu.cn','方法','22','$2a$10$VHuI.k.M/jWrNv7KZ4P8WeR9Pe5dnHbBcBuO8XlTAMoJBJUtB34IS','admin','18932368080','222'),(6,NULL,'','','马嘉亮','$2a$10$fMRHhjT3v5wcdH4WD25liu3FuZMlj92nSr6BU2KoPriQK/S164Gei','admin','','Icey'),(7,NULL,'','','我问问','$2a$10$ugo7Rnuy36F5zxeTwLTPGuV87rnp31GxInUY3LYrxfVzh4oygiimK','admin','','yuanshen'),(8,NULL,'','','我问问','$2a$10$5.Bci3yDZguXDH..XMxI6.iJoL28cOiTA.LVV020qzftOtHORXYc6','admin','','yuan'),(9,NULL,'','','无畏','$2a$10$ZdQH99lLLUumR1lJTPM/5Owp4DsGEtWMU9AcX4RhKKVczXYEveQ2q','admin','','2222'),(10,'https://via.placeholder.com/927x370/52d8ab/5b833a.jpg','po4sf0_g3t@yeah.net','北阳市','孛丽芬','$2a$10$W1e3/HUKar30OXMWIsTklOBz2QlLar/gUsgCgIZXC/dRhKucKZ.VG','admin','16829288705','IuJNHHBKfjnG'),(11,'https://via.placeholder.com/1717x1650/38ecec/fdc28e.png','gVwQAeoSlYZL@ZtipMHbZElGV.com','珠林市','呼榕融','$2a$10$zq2hTleJ2.OM9TgF.dHc0.3220iTGwnq2nvei9eibobIPyx55PkC2','admin','13290889668','gVwQAeoSlYZL'),(12,'https://via.placeholder.com/1834x39/aef22e/acc83d.jpg','TYNhMgLNEKAQ@XVABnddwyKZl.com','北头市','魏俊凯','$2a$10$2a76HFajRRAsV3Rnc.VDIeoHSrXLpvGKWhpDfjmg2DdgJp242Np5a','admin','13281863955','TYNhMgLNEKAQ'),(13,'https://via.placeholder.com/1555x1267/b3f16a/f9ab9c.gif','czXBVuOMeKvA@bkirYzndbfbE.com','成沙市','局苡沫','$2a$10$1Vq/vLIRgziCS2m60sSz6./0/t4pVvdZkMI/C9K5AmLJmefAW7znO','admin','16532007298','czXBVuOMeKvA'),(14,'https://via.placeholder.com/1247x1642/d33b9a/5dfd0a.png','l7nrqi_q0b@gmail.com','北徽市','鲍文昊','$2a$10$/ofAa9.hkiFjbd4YFWDxMOvb3sjExSkEaufsFeZ4knnmT/XzP2xLq','admin','15499896538','uLcGMUaDrboe'),(15,'https://via.placeholder.com/356x1249/98ed0b/30d01c.jpeg','nuYyHWxZVoNL@UYHhjyAbGTIJ.com','济口市','路子豪','$2a$10$nq2hhZVYryUkg5U2upwLyewDpETiAcx.XDSH2Z6eDnsd0RXDgJWTW','admin','13547617445','nuYyHWxZVoNL'),(16,'https://via.placeholder.com/1397x1185/f7b6dc/1affce.gif','vpvnahalNFZj@McgNaFegXbMD.com','安头市','夏国秀','$2a$10$1EV9tFEt9sZRB6xNtD4evuhHQl1ro4HlRokdRcOV/VGzaJFjWDvP2','admin','15095339247','vpvnahalNFZj'),(17,'https://via.placeholder.com/1576x304/1b4cb7/babc3b.webp','lzbmd224@163.com','南阳市','姜蒙','$2a$10$k80Y1MAydOqfMZZx7s37y.TunmAIDkakkjnMvQkaGCw.HXhXBHQF2','admin','14810019449','gSMYaoBhscgY'),(18,'https://via.placeholder.com/797x1111/101afa/daaebd.webp','BdvfPHxrAgSN@ecpkzDaqDnxb.com','武乡县','綦海燕','$2a$10$GVXxIzJZzTOZYTMjWg71Zuu8lbRnt9JncSOQrZU5efslD9grgLkaC','admin','17859342850','BdvfPHxrAgSN'),(19,'https://via.placeholder.com/221x120/f8f9ba/359ca4.png','bwFrLXWSkuiZ@gfAvwSPoSGDG.com','安阳市','邹梓馨','$2a$10$NlJJ7p6mt.ajDrwjNxoZV.0E92EC3xanJXXoI5HQOTR6M7emyQ0B2','admin','17815089170','bwFrLXWSkuiZ'),(20,'https://via.placeholder.com/437x223/fcfde3/ac1d3e.jpeg','XyBvrsFmCzCC@nqguEaiLxBMk.com','珠门市','大敬彪','$2a$10$ne.vi9Y2bnkMZ7cGKn5XLu45hn6l2BkDg1U/KnvzePaYcI5fJhUf6','admin','18187266091','XyBvrsFmCzCC'),(21,'https://via.placeholder.com/1475x737/24d71d/5c8b5f.jpg','BaWiasgpTkcK@ELcoEdIPLOVl.com','南乡县','裔沐阳','$2a$10$iaHsem7.NBtN.ughH04PAuwK/e2V1m0vKlaWb5T9cWvqMLmTbrfqK','admin','17495014178','BaWiasgpTkcK'),(22,'https://via.placeholder.com/767x1018/3f77d0/6e3e6f.gif','RFhxGCtQFPAW@tdASgrvfCRuh.com','海码市','钱茗泽','$2a$10$Oi0jhbRugr0k/YeY1tgnS.QqfVqAJ1viMJuLuWHaSV8by8w9R1fr2','admin','16080215700','RFhxGCtQFPAW'),(23,'https://via.placeholder.com/724x398/8811ac/9a4e80.jpg','HAsGiaSncndv@cnOSgFfhNTRQ.com','济京市','相立伟','$2a$10$OLF8t1k3YNrIkImDOHRpg.LZ07vHlpr8stw0XQ4Pxa5ReI97nl3aS','admin','18646675699','HAsGiaSncndv'),(24,'https://via.placeholder.com/418x416/3ff59c/8dad2a.jpeg','mnazDpbVpTCR@BhBLXMkEAFfW.com','贵汉市','禚欣怡','$2a$10$u6p9q.HYa.jJwgIyNmDxoeIo0Bdj0xFbwTRvbyvYacyC0MP3o7xdu','admin','17508060915','mnazDpbVpTCR'),(25,'https://via.placeholder.com/1944x255/c2d9cc/dd8d97.webp','OEuoopRHjqCJ@bstTObNMhKqb.com','北原市','张安琪','$2a$10$1aj47JhZb87CganEzLk2AOkNH9w2tAYJXfx24QWzwEiVcVpM06SRW','admin','15397662758','OEuoopRHjqCJ'),(26,'https://via.placeholder.com/917x1412/bcb1b3/d6b9e7.jpg','qUNgxiBhAEWv@vpeVpyUXVHuG.com','成州市','初天娇','$2a$10$oo24GgyD/48kGXBASgHqO.ba5b1xLtHKQp8cA7oH8u8ZDwP6JLb4q','admin','15327210771','qUNgxiBhAEWv'),(27,'https://via.placeholder.com/847x1281/9bcda7/397bf3.webp','LEiGpFvuHRcq@tuknTQieOjzT.com','西徽市','阙国珍','$2a$10$2cjSqaDgrpwVhm9py83x/uuqX0kLrXEyQY1ns45UiqzwileN/59Uq','admin','15285310896','LEiGpFvuHRcq'),(28,'https://via.placeholder.com/1938x1555/71dfdb/feba08.png','UwlEuaXmaZxQ@QGyZyDsKEYvP.com','福原市','怀癸霖','$2a$10$/Wfv8aLKmi6B8Liz/H8j4Oh7HFaJ/dnbAFGvxB1xAnRG9wFKQlyHq','admin','18617282101','UwlEuaXmaZxQ'),(29,'https://via.placeholder.com/1092x1110/a2e452/d9fed2.jpg','wOBdvaCHUkFh@AbhhzYBCVRDS.com','吉沙市','翁梓馨','$2a$10$INCk6pzbT8X4NnOklb0tk.iOmqSBm4Pvx5ONhwgSqi94vjKwdi2LC','admin','16182636516','wOBdvaCHUkFh'),(30,'https://via.placeholder.com/964x494/d91ccf/f6cf08.jpeg','LXsmYoaKGzFk@FXKymDNFgxun.com','成海市','夏梓浩','$2a$10$rqoyCNOVTp4YIxMrSU1OA.xX2IjieZ4oDnFZAkKilVLGhqhj6/0jC','admin','14609813665','LXsmYoaKGzFk'),(31,'https://via.placeholder.com/261x153/e6bfdb/fb304c.png','evzKuujsEXOW@pzyMKUXBhPaH.com','济头市','世宇轩','$2a$10$eKYTKIFV9ATVALFawUZJGOZ7iGJjg7f1qCx77Zl1pbhuKyWumJLg2','admin','13139979328','evzKuujsEXOW'),(32,'https://via.placeholder.com/1715x1325/87ed0f/ff538d.jpeg','SCAjxbOzyOHC@ZdQitAFQbqCP.com','包南市','寒国荣','$2a$10$22xLa7ZIiL/Y7ff52qnlue12SRPL1IBXkQ/KnSyHi1OLZpIwWu8su','admin','18745714870','SCAjxbOzyOHC'),(33,'https://via.placeholder.com/375x1746/378edb/4fd79c.webp','IrxKtJMXAZAE@ycdVdgPWvFgp.com','武徽市','翁民','$2a$10$0BPyGugFFgQn3s.XOAsNEuHe1.7dHgE4EhG6diBB3yJDMJa3fen2m','admin','15702367498','IrxKtJMXAZAE'),(34,'https://via.placeholder.com/540x1416/eddfa1/bcf30e.gif','jLmxFIzjazmo@UCIUznlzlPrV.com','贵头市','司马国栋','$2a$10$lY4KE.DD0fCzfsMt0fXw5Oa2gBTii6piPuze7E/R5n06//vtLA.cq','admin','18627811284','jLmxFIzjazmo'),(35,'https://via.placeholder.com/1765x1685/e5f39a/f3fea4.png','slBKCOgNXEBf@YrKLtqAisiSR.com','海徽市','五雨欣','$2a$10$ET5zzUta.fcT6PDw4AILPeLMtHWp2/Gjig2n5WKxZzGgtol1CzuqK','admin','13927408021','slBKCOgNXEBf'),(36,'https://via.placeholder.com/483x1288/f4d85b/7a8cc0.webp','lzMuehrcvtvt@oKXcDPRIShVZ.com','宁徽市','邰宇轩','$2a$10$K2INUxSTngMdgHA4iVtHeeCpqwwWtabRtyv5j0TcyPZoz.nZVE0Pi','admin','14534221559','lzMuehrcvtvt'),(37,'https://via.placeholder.com/1185x143/7efae8/d520f9.jpeg','kWcGToopekQR@PRrsCydTcEPY.com','北门市','郭政君','$2a$10$v.eV/nxainuog1jMjF3JfuFWPHgDem3hlJirtPsVbCMejRYeQAXTu','admin','13930270436','kWcGToopekQR'),(38,'https://via.placeholder.com/1062x769/b7fc20/0eff12.jpeg','HtTULHpKlsYv@ENAhlkYbTlXC.com','福头市','闪雨涵','$2a$10$w2W/.78MSptxSJ777ZQSPuLflj7cjsaKi6jXa4xR6BGePIBIM2nDG','admin','17463546743','HtTULHpKlsYv'),(39,'https://via.placeholder.com/1474x1773/aad4d7/5f9aa3.webp','QpbUDcaAVthc@pubBbIecmCHB.com','吉州市','沙哲新','$2a$10$cLbypp4D.MY6bdsdCb5c2uO58CG6WSK9KOjLFkclfqxbRT4qtVul6','admin','16583318354','QpbUDcaAVthc'),(40,'https://via.placeholder.com/1x1029/cc5e2b/5c2dad.jpeg','buvjxPAlyQlW@EsLATYzTeVHs.com','包原市','功红','$2a$10$1OpotMDEvRsQHbzqnBbdkeMXvK9cAABE1ZP9c0.JX/mUNKfdTb1de','admin','18248117139','buvjxPAlyQlW'),(41,'https://via.placeholder.com/1354x964/49d5d2/cdf8a6.png','DKaDorkPOYPJ@fAOdJlUWwzQJ.com','福码市','明诗雨','$2a$10$0UfN14yO.BOX4dIf1G/B/.vFlUu6TCQXZTg3SSI/tXXOE4nxxN996','admin','16712208699','DKaDorkPOYPJ'),(42,'https://via.placeholder.com/1450x546/f54eca/6aebf5.jpg','dBrvospLcGwD@wOKRuJWpPPNA.com','武都市','闪晨阳','$2a$10$ehAYCg3wF/2CMsHPmsNsLOiFId74zLeB5PjQ05zGWcSiSlDcbcqTS','admin','18375044402','dBrvospLcGwD');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wish_list_items`
--

DROP TABLE IF EXISTS `wish_list_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wish_list_items` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `book_id` bigint NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK51sc8yjt919607c5rj8alihru` (`user_id`),
  CONSTRAINT `FK51sc8yjt919607c5rj8alihru` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wish_list_items`
--

LOCK TABLES `wish_list_items` WRITE;
/*!40000 ALTER TABLE `wish_list_items` DISABLE KEYS */;
INSERT INTO `wish_list_items` VALUES (6,45,'2025-05-22 16:10:08.176396',4),(7,46,'2025-05-22 16:11:28.455499',4);
/*!40000 ALTER TABLE `wish_list_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wishlist_items`
--

DROP TABLE IF EXISTS `wishlist_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wishlist_items` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `added_at` datetime(6) NOT NULL,
  `product_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKqxj7lncd242b59fb78rqegyxj` (`product_id`),
  KEY `FKmmj2k1i459yu449k3h1vx5abp` (`user_id`),
  CONSTRAINT `FKmmj2k1i459yu449k3h1vx5abp` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKqxj7lncd242b59fb78rqegyxj` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wishlist_items`
--

LOCK TABLES `wishlist_items` WRITE;
/*!40000 ALTER TABLE `wishlist_items` DISABLE KEYS */;
/*!40000 ALTER TABLE `wishlist_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wishlists`
--

DROP TABLE IF EXISTS `wishlists`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wishlists` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `user_id` bigint NOT NULL,
  `product_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKl7ao98u2bm8nijc1rv4jobcrx` (`product_id`),
  CONSTRAINT `FKl7ao98u2bm8nijc1rv4jobcrx` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wishlists`
--

LOCK TABLES `wishlists` WRITE;
/*!40000 ALTER TABLE `wishlists` DISABLE KEYS */;
/*!40000 ALTER TABLE `wishlists` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-05-22 16:13:20
