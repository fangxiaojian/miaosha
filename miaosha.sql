/*
SQLyog Ultimate v12.09 (64 bit)
MySQL - 5.5.10-log : Database - moshu
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`moshu` /*!40100 DEFAULT CHARACTER SET utf8 COLLATE utf8_unicode_ci */;

USE `moshu`;

/*Table structure for table `goods` */

DROP TABLE IF EXISTS `goods`;

CREATE TABLE `goods` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `name` varchar(64) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '商品名字',
  `store` int(11) DEFAULT NULL COMMENT '库存量',
  `start_time` datetime DEFAULT NULL COMMENT '活动开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '活动结束时间',
  `version` int(11) DEFAULT '0' COMMENT '版本号',
  `del_flag` tinyint(1) DEFAULT '0' COMMENT '删除标志，0-未删除，1-已删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

/*Table structure for table `order` */

DROP TABLE IF EXISTS `order`;

CREATE TABLE `order` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `mobile` varchar(11) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户手机号',
  `goods_id` int(11) DEFAULT NULL COMMENT '商品ID',
  `num` int(8) DEFAULT NULL COMMENT '数量',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `del_flag` tinyint(1) DEFAULT '0' COMMENT '删除标识，0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_mobile_goodsid` (`mobile`,`goods_id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/*Table structure for table `user` */

DROP TABLE IF EXISTS `user`;

CREATE TABLE `user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_name` varchar(128) COLLATE utf8_unicode_ci NOT NULL,
  `psw` varchar(128) COLLATE utf8_unicode_ci NOT NULL,
  `email` varchar(64) COLLATE utf8_unicode_ci DEFAULT NULL,
  `mobile` varchar(16) COLLATE utf8_unicode_ci DEFAULT NULL,
  `regist_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `last_login_time` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ix_username` (`user_name`),
  KEY `ix_mobile` (`mobile`),
  KEY `ix_email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

/* Procedure structure for procedure `pro_doorder` */

/*!50003 DROP PROCEDURE IF EXISTS  `pro_doorder` */;

DELIMITER $$

/*!50003 CREATE DEFINER=`root`@`localhost` PROCEDURE `pro_doorder`(IN i_goods_id BIGINT,IN i_mobile varchar(11),IN i_order_time TIMESTAMP ,OUT o_result INT)
BEGIN
	  DECLARE insert_count INT DEFAULT 0;
	    START TRANSACTION ;
	    INSERT IGNORE INTO `order`(mobile,goods_id,num,create_time)VALUES(i_mobile,i_goods_id,1,i_order_time);
	    SELECT ROW_COUNT() INTO insert_count;
	    IF(insert_count = 0)THEN
	     ROLLBACK ;
	     SET o_result=-1;
	    ELSEIF(insert_count<0)THEN
	     ROLLBACK ;
	     SET o_result=-2;
	    ELSE
	     UPDATE goods
	     SET store = store - 1,
		 VERSION = VERSION+1
	     WHERE id = i_goods_id
	       AND end_time > i_order_time 
	       AND start_time < i_order_time
	       AND store>0;
	    SELECT ROW_COUNT() INTO insert_count;
	     IF(insert_count<=0)THEN
	      ROLLBACK ;
	      SET o_result = -2;
	     ELSE
	      COMMIT ;
	      SET o_result = 1;
	     END IF;
	  END IF;
	END */$$
DELIMITER ;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
