-- MySQL dump 10.13  Distrib 5.1.73, for redhat-linux-gnu (x86_64)
--
-- Host: db6    Database: WalDB
-- ------------------------------------------------------
-- Server version	5.6.36-82.0-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `DP_bins_chr1`
--

DROP TABLE IF EXISTS `DP_bins_chr1`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DP_bins_chr1` (
  `sample_id` mediumint(8) unsigned NOT NULL,
  `block_id` mediumint(8) unsigned NOT NULL,
  `DP_string` varchar(20000) DEFAULT NULL,
  PRIMARY KEY (`block_id`,`sample_id`),
  KEY `sample_idx` (`sample_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `DP_bins_chr10`
--

DROP TABLE IF EXISTS `DP_bins_chr10`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DP_bins_chr10` (
  `sample_id` mediumint(8) unsigned NOT NULL,
  `block_id` mediumint(8) unsigned NOT NULL,
  `DP_string` varchar(20000) DEFAULT NULL,
  PRIMARY KEY (`block_id`,`sample_id`),
  KEY `sample_idx` (`sample_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `DP_bins_chr11`
--

DROP TABLE IF EXISTS `DP_bins_chr11`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DP_bins_chr11` (
  `sample_id` mediumint(8) unsigned NOT NULL,
  `block_id` mediumint(8) unsigned NOT NULL,
  `DP_string` varchar(20000) DEFAULT NULL,
  PRIMARY KEY (`block_id`,`sample_id`),
  KEY `sample_idx` (`sample_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `DP_bins_chr12`
--

DROP TABLE IF EXISTS `DP_bins_chr12`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DP_bins_chr12` (
  `sample_id` mediumint(8) unsigned NOT NULL,
  `block_id` mediumint(8) unsigned NOT NULL,
  `DP_string` varchar(20000) DEFAULT NULL,
  PRIMARY KEY (`block_id`,`sample_id`),
  KEY `sample_idx` (`sample_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `DP_bins_chr13`
--

DROP TABLE IF EXISTS `DP_bins_chr13`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DP_bins_chr13` (
  `sample_id` mediumint(8) unsigned NOT NULL,
  `block_id` mediumint(8) unsigned NOT NULL,
  `DP_string` varchar(20000) DEFAULT NULL,
  PRIMARY KEY (`block_id`,`sample_id`),
  KEY `sample_idx` (`sample_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `DP_bins_chr14`
--

DROP TABLE IF EXISTS `DP_bins_chr14`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DP_bins_chr14` (
  `sample_id` mediumint(8) unsigned NOT NULL,
  `block_id` mediumint(8) unsigned NOT NULL,
  `DP_string` varchar(20000) DEFAULT NULL,
  PRIMARY KEY (`block_id`,`sample_id`),
  KEY `sample_idx` (`sample_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `DP_bins_chr15`
--

DROP TABLE IF EXISTS `DP_bins_chr15`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DP_bins_chr15` (
  `sample_id` mediumint(8) unsigned NOT NULL,
  `block_id` mediumint(8) unsigned NOT NULL,
  `DP_string` varchar(20000) DEFAULT NULL,
  PRIMARY KEY (`block_id`,`sample_id`),
  KEY `sample_idx` (`sample_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `DP_bins_chr16`
--

DROP TABLE IF EXISTS `DP_bins_chr16`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DP_bins_chr16` (
  `sample_id` mediumint(8) unsigned NOT NULL,
  `block_id` mediumint(8) unsigned NOT NULL,
  `DP_string` varchar(20000) DEFAULT NULL,
  PRIMARY KEY (`block_id`,`sample_id`),
  KEY `sample_idx` (`sample_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `DP_bins_chr17`
--

DROP TABLE IF EXISTS `DP_bins_chr17`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DP_bins_chr17` (
  `sample_id` mediumint(8) unsigned NOT NULL,
  `block_id` mediumint(8) unsigned NOT NULL,
  `DP_string` varchar(20000) DEFAULT NULL,
  PRIMARY KEY (`block_id`,`sample_id`),
  KEY `sample_idx` (`sample_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `DP_bins_chr18`
--

DROP TABLE IF EXISTS `DP_bins_chr18`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DP_bins_chr18` (
  `sample_id` mediumint(8) unsigned NOT NULL,
  `block_id` mediumint(8) unsigned NOT NULL,
  `DP_string` varchar(20000) DEFAULT NULL,
  PRIMARY KEY (`block_id`,`sample_id`),
  KEY `sample_idx` (`sample_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `DP_bins_chr19`
--

DROP TABLE IF EXISTS `DP_bins_chr19`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DP_bins_chr19` (
  `sample_id` mediumint(8) unsigned NOT NULL,
  `block_id` mediumint(8) unsigned NOT NULL,
  `DP_string` varchar(20000) DEFAULT NULL,
  PRIMARY KEY (`block_id`,`sample_id`),
  KEY `sample_idx` (`sample_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `DP_bins_chr2`
--

DROP TABLE IF EXISTS `DP_bins_chr2`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DP_bins_chr2` (
  `sample_id` mediumint(8) unsigned NOT NULL,
  `block_id` mediumint(8) unsigned NOT NULL,
  `DP_string` varchar(20000) DEFAULT NULL,
  PRIMARY KEY (`block_id`,`sample_id`),
  KEY `sample_idx` (`sample_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `DP_bins_chr20`
--

DROP TABLE IF EXISTS `DP_bins_chr20`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DP_bins_chr20` (
  `sample_id` mediumint(8) unsigned NOT NULL,
  `block_id` mediumint(8) unsigned NOT NULL,
  `DP_string` varchar(20000) DEFAULT NULL,
  PRIMARY KEY (`block_id`,`sample_id`),
  KEY `sample_idx` (`sample_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `DP_bins_chr21`
--

DROP TABLE IF EXISTS `DP_bins_chr21`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DP_bins_chr21` (
  `sample_id` mediumint(8) unsigned NOT NULL,
  `block_id` mediumint(8) unsigned NOT NULL,
  `DP_string` varchar(20000) DEFAULT NULL,
  PRIMARY KEY (`block_id`,`sample_id`),
  KEY `sample_idx` (`sample_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `DP_bins_chr22`
--

DROP TABLE IF EXISTS `DP_bins_chr22`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DP_bins_chr22` (
  `sample_id` mediumint(8) unsigned NOT NULL,
  `block_id` mediumint(8) unsigned NOT NULL,
  `DP_string` varchar(20000) DEFAULT NULL,
  PRIMARY KEY (`block_id`,`sample_id`),
  KEY `sample_idx` (`sample_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `DP_bins_chr3`
--

DROP TABLE IF EXISTS `DP_bins_chr3`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DP_bins_chr3` (
  `sample_id` mediumint(8) unsigned NOT NULL,
  `block_id` mediumint(8) unsigned NOT NULL,
  `DP_string` varchar(20000) DEFAULT NULL,
  PRIMARY KEY (`block_id`,`sample_id`),
  KEY `sample_idx` (`sample_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `DP_bins_chr4`
--

DROP TABLE IF EXISTS `DP_bins_chr4`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DP_bins_chr4` (
  `sample_id` mediumint(8) unsigned NOT NULL,
  `block_id` mediumint(8) unsigned NOT NULL,
  `DP_string` varchar(20000) DEFAULT NULL,
  PRIMARY KEY (`block_id`,`sample_id`),
  KEY `sample_idx` (`sample_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `DP_bins_chr5`
--

DROP TABLE IF EXISTS `DP_bins_chr5`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DP_bins_chr5` (
  `sample_id` mediumint(8) unsigned NOT NULL,
  `block_id` mediumint(8) unsigned NOT NULL,
  `DP_string` varchar(20000) DEFAULT NULL,
  PRIMARY KEY (`block_id`,`sample_id`),
  KEY `sample_idx` (`sample_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `DP_bins_chr6`
--

DROP TABLE IF EXISTS `DP_bins_chr6`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DP_bins_chr6` (
  `sample_id` mediumint(8) unsigned NOT NULL,
  `block_id` mediumint(8) unsigned NOT NULL,
  `DP_string` varchar(20000) DEFAULT NULL,
  PRIMARY KEY (`block_id`,`sample_id`),
  KEY `sample_idx` (`sample_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `DP_bins_chr7`
--

DROP TABLE IF EXISTS `DP_bins_chr7`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DP_bins_chr7` (
  `sample_id` mediumint(8) unsigned NOT NULL,
  `block_id` mediumint(8) unsigned NOT NULL,
  `DP_string` varchar(20000) DEFAULT NULL,
  PRIMARY KEY (`block_id`,`sample_id`),
  KEY `sample_idx` (`sample_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `DP_bins_chr8`
--

DROP TABLE IF EXISTS `DP_bins_chr8`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DP_bins_chr8` (
  `sample_id` mediumint(8) unsigned NOT NULL,
  `block_id` mediumint(8) unsigned NOT NULL,
  `DP_string` varchar(20000) DEFAULT NULL,
  PRIMARY KEY (`block_id`,`sample_id`),
  KEY `sample_idx` (`sample_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `DP_bins_chr9`
--

DROP TABLE IF EXISTS `DP_bins_chr9`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DP_bins_chr9` (
  `sample_id` mediumint(8) unsigned NOT NULL,
  `block_id` mediumint(8) unsigned NOT NULL,
  `DP_string` varchar(20000) DEFAULT NULL,
  PRIMARY KEY (`block_id`,`sample_id`),
  KEY `sample_idx` (`sample_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `DP_bins_chrMT`
--

DROP TABLE IF EXISTS `DP_bins_chrMT`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DP_bins_chrMT` (
  `sample_id` mediumint(8) unsigned NOT NULL,
  `block_id` mediumint(8) unsigned NOT NULL,
  `DP_string` varchar(20000) DEFAULT NULL,
  PRIMARY KEY (`block_id`,`sample_id`),
  KEY `sample_idx` (`sample_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `DP_bins_chrX`
--

DROP TABLE IF EXISTS `DP_bins_chrX`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DP_bins_chrX` (
  `sample_id` mediumint(8) unsigned NOT NULL,
  `block_id` mediumint(8) unsigned NOT NULL,
  `DP_string` varchar(20000) DEFAULT NULL,
  PRIMARY KEY (`block_id`,`sample_id`),
  KEY `sample_idx` (`sample_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `DP_bins_chrY`
--

DROP TABLE IF EXISTS `DP_bins_chrY`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DP_bins_chrY` (
  `sample_id` mediumint(8) unsigned NOT NULL,
  `block_id` mediumint(8) unsigned NOT NULL,
  `DP_string` varchar(20000) DEFAULT NULL,
  PRIMARY KEY (`block_id`,`sample_id`),
  KEY `sample_idx` (`sample_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `called_variant_chr1`
--

DROP TABLE IF EXISTS `called_variant_chr1`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `called_variant_chr1` (
  `sample_id` mediumint(8) unsigned NOT NULL,
  `variant_id` int(10) unsigned NOT NULL,
  `block_id` mediumint(8) unsigned NOT NULL,
  `GT` tinyint(3) unsigned NOT NULL,
  `DP` smallint(5) unsigned DEFAULT NULL,
  `AD_REF` smallint(5) unsigned DEFAULT NULL,
  `AD_ALT` smallint(5) unsigned DEFAULT NULL,
  `GQ` tinyint(3) unsigned NOT NULL,
  `PL_AA` mediumint(8) unsigned DEFAULT NULL,
  `PL_AB` mediumint(8) unsigned DEFAULT NULL,
  `PL_BB` mediumint(8) unsigned DEFAULT NULL,
  `VQSLOD` float DEFAULT NULL,
  `SOR` float DEFAULT NULL,
  `FS` float DEFAULT NULL,
  `MQ` tinyint(3) unsigned DEFAULT NULL,
  `QD` tinyint(3) unsigned DEFAULT NULL,
  `QUAL` mediumint(8) unsigned DEFAULT NULL,
  `ReadPosRankSum` float DEFAULT NULL,
  `MQRankSum` float DEFAULT NULL,
  `FILTER` enum('PASS','LIKELY','INTERMEDIATE','FAIL') DEFAULT NULL,
  `highest_impact` enum('HIGH','MODERATE','LOW','MODIFIER') NOT NULL,
  `PID_variant_id` int(10) unsigned DEFAULT NULL,
  `PGT` tinyint(3) unsigned DEFAULT NULL,
  `HP_variant_id` int(10) unsigned DEFAULT NULL,
  `HP_GT` tinyint(3) unsigned DEFAULT NULL,
  `PQ` mediumint(8) unsigned DEFAULT NULL,
  PRIMARY KEY (`block_id`,`highest_impact`,`sample_id`,`variant_id`),
  KEY `variant_idx` (`variant_id`),
  KEY `sample_variant_idx` (`sample_id`,`variant_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `called_variant_chr10`
--

DROP TABLE IF EXISTS `called_variant_chr10`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `called_variant_chr10` (
  `sample_id` mediumint(8) unsigned NOT NULL,
  `variant_id` int(10) unsigned NOT NULL,
  `block_id` mediumint(8) unsigned NOT NULL,
  `GT` tinyint(3) unsigned NOT NULL,
  `DP` smallint(5) unsigned DEFAULT NULL,
  `AD_REF` smallint(6) DEFAULT NULL,
  `AD_ALT` smallint(5) unsigned DEFAULT NULL,
  `GQ` tinyint(3) unsigned NOT NULL,
  `PL_AA` mediumint(8) unsigned DEFAULT NULL,
  `PL_AB` mediumint(8) unsigned DEFAULT NULL,
  `PL_BB` mediumint(8) unsigned DEFAULT NULL,
  `VQSLOD` float DEFAULT NULL,
  `SOR` float DEFAULT NULL,
  `FS` float DEFAULT NULL,
  `MQ` tinyint(3) unsigned DEFAULT NULL,
  `QD` tinyint(3) unsigned DEFAULT NULL,
  `QUAL` mediumint(8) unsigned DEFAULT NULL,
  `ReadPosRankSum` float DEFAULT NULL,
  `MQRankSum` float DEFAULT NULL,
  `FILTER` enum('PASS','LIKELY','INTERMEDIATE','FAIL') DEFAULT NULL,
  `highest_impact` enum('HIGH','MODERATE','LOW','MODIFIER') NOT NULL,
  `PID_variant_id` int(10) unsigned DEFAULT NULL,
  `PGT` tinyint(3) unsigned DEFAULT NULL,
  `HP_variant_id` int(10) unsigned DEFAULT NULL,
  `HP_GT` tinyint(3) unsigned DEFAULT NULL,
  `PQ` mediumint(8) unsigned DEFAULT NULL,
  PRIMARY KEY (`block_id`,`highest_impact`,`sample_id`,`variant_id`),
  KEY `variant_idx` (`variant_id`),
  KEY `sample_variant_idx` (`sample_id`,`variant_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `called_variant_chr11`
--

DROP TABLE IF EXISTS `called_variant_chr11`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `called_variant_chr11` (
  `sample_id` mediumint(8) unsigned NOT NULL,
  `variant_id` int(10) unsigned NOT NULL,
  `block_id` mediumint(8) unsigned NOT NULL,
  `GT` tinyint(3) unsigned NOT NULL,
  `DP` smallint(5) unsigned DEFAULT NULL,
  `AD_REF` smallint(6) DEFAULT NULL,
  `AD_ALT` smallint(5) unsigned DEFAULT NULL,
  `GQ` tinyint(3) unsigned NOT NULL,
  `PL_AA` mediumint(8) unsigned DEFAULT NULL,
  `PL_AB` mediumint(8) unsigned DEFAULT NULL,
  `PL_BB` mediumint(8) unsigned DEFAULT NULL,
  `VQSLOD` float DEFAULT NULL,
  `SOR` float DEFAULT NULL,
  `FS` float DEFAULT NULL,
  `MQ` tinyint(3) unsigned DEFAULT NULL,
  `QD` tinyint(3) unsigned DEFAULT NULL,
  `QUAL` mediumint(8) unsigned DEFAULT NULL,
  `ReadPosRankSum` float DEFAULT NULL,
  `MQRankSum` float DEFAULT NULL,
  `FILTER` enum('PASS','LIKELY','INTERMEDIATE','FAIL') DEFAULT NULL,
  `highest_impact` enum('HIGH','MODERATE','LOW','MODIFIER') NOT NULL,
  `PID_variant_id` int(10) unsigned DEFAULT NULL,
  `PGT` tinyint(3) unsigned DEFAULT NULL,
  `HP_variant_id` int(10) unsigned DEFAULT NULL,
  `HP_GT` tinyint(3) unsigned DEFAULT NULL,
  `PQ` mediumint(8) unsigned DEFAULT NULL,
  PRIMARY KEY (`block_id`,`highest_impact`,`sample_id`,`variant_id`),
  KEY `variant_idx` (`variant_id`),
  KEY `sample_variant_idx` (`sample_id`,`variant_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `called_variant_chr12`
--

DROP TABLE IF EXISTS `called_variant_chr12`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `called_variant_chr12` (
  `sample_id` mediumint(8) unsigned NOT NULL,
  `variant_id` int(10) unsigned NOT NULL,
  `block_id` mediumint(8) unsigned NOT NULL,
  `GT` tinyint(3) unsigned NOT NULL,
  `DP` smallint(5) unsigned DEFAULT NULL,
  `AD_REF` smallint(6) DEFAULT NULL,
  `AD_ALT` smallint(5) unsigned DEFAULT NULL,
  `GQ` tinyint(3) unsigned NOT NULL,
  `PL_AA` mediumint(8) unsigned DEFAULT NULL,
  `PL_AB` mediumint(8) unsigned DEFAULT NULL,
  `PL_BB` mediumint(8) unsigned DEFAULT NULL,
  `VQSLOD` float DEFAULT NULL,
  `SOR` float DEFAULT NULL,
  `FS` float DEFAULT NULL,
  `MQ` tinyint(3) unsigned DEFAULT NULL,
  `QD` tinyint(3) unsigned DEFAULT NULL,
  `QUAL` mediumint(8) unsigned DEFAULT NULL,
  `ReadPosRankSum` float DEFAULT NULL,
  `MQRankSum` float DEFAULT NULL,
  `FILTER` enum('PASS','LIKELY','INTERMEDIATE','FAIL') DEFAULT NULL,
  `highest_impact` enum('HIGH','MODERATE','LOW','MODIFIER') NOT NULL,
  `PID_variant_id` int(10) unsigned DEFAULT NULL,
  `PGT` tinyint(3) unsigned DEFAULT NULL,
  `HP_variant_id` int(10) unsigned DEFAULT NULL,
  `HP_GT` tinyint(3) unsigned DEFAULT NULL,
  `PQ` mediumint(8) unsigned DEFAULT NULL,
  PRIMARY KEY (`block_id`,`highest_impact`,`sample_id`,`variant_id`),
  KEY `variant_idx` (`variant_id`),
  KEY `sample_variant_idx` (`sample_id`,`variant_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `called_variant_chr13`
--

DROP TABLE IF EXISTS `called_variant_chr13`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `called_variant_chr13` (
  `sample_id` mediumint(8) unsigned NOT NULL,
  `variant_id` int(10) unsigned NOT NULL,
  `block_id` mediumint(8) unsigned NOT NULL,
  `GT` tinyint(3) unsigned NOT NULL,
  `DP` smallint(5) unsigned DEFAULT NULL,
  `AD_REF` smallint(6) DEFAULT NULL,
  `AD_ALT` smallint(5) unsigned DEFAULT NULL,
  `GQ` tinyint(3) unsigned NOT NULL,
  `PL_AA` mediumint(8) unsigned DEFAULT NULL,
  `PL_AB` mediumint(8) unsigned DEFAULT NULL,
  `PL_BB` mediumint(8) unsigned DEFAULT NULL,
  `VQSLOD` float DEFAULT NULL,
  `SOR` float DEFAULT NULL,
  `FS` float DEFAULT NULL,
  `MQ` tinyint(3) unsigned DEFAULT NULL,
  `QD` tinyint(3) unsigned DEFAULT NULL,
  `QUAL` mediumint(8) unsigned DEFAULT NULL,
  `ReadPosRankSum` float DEFAULT NULL,
  `MQRankSum` float DEFAULT NULL,
  `FILTER` enum('PASS','LIKELY','INTERMEDIATE','FAIL') DEFAULT NULL,
  `highest_impact` enum('HIGH','MODERATE','LOW','MODIFIER') NOT NULL,
  `PID_variant_id` int(10) unsigned DEFAULT NULL,
  `PGT` tinyint(3) unsigned DEFAULT NULL,
  `HP_variant_id` int(10) unsigned DEFAULT NULL,
  `HP_GT` tinyint(3) unsigned DEFAULT NULL,
  `PQ` mediumint(8) unsigned DEFAULT NULL,
  PRIMARY KEY (`block_id`,`highest_impact`,`sample_id`,`variant_id`),
  KEY `variant_idx` (`variant_id`),
  KEY `sample_variant_idx` (`sample_id`,`variant_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `called_variant_chr14`
--

DROP TABLE IF EXISTS `called_variant_chr14`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `called_variant_chr14` (
  `sample_id` mediumint(8) unsigned NOT NULL,
  `variant_id` int(10) unsigned NOT NULL,
  `block_id` mediumint(8) unsigned NOT NULL,
  `GT` tinyint(3) unsigned NOT NULL,
  `DP` smallint(5) unsigned DEFAULT NULL,
  `AD_REF` smallint(6) DEFAULT NULL,
  `AD_ALT` smallint(5) unsigned DEFAULT NULL,
  `GQ` tinyint(3) unsigned NOT NULL,
  `PL_AA` mediumint(8) unsigned DEFAULT NULL,
  `PL_AB` mediumint(8) unsigned DEFAULT NULL,
  `PL_BB` mediumint(8) unsigned DEFAULT NULL,
  `VQSLOD` float DEFAULT NULL,
  `SOR` float DEFAULT NULL,
  `FS` float DEFAULT NULL,
  `MQ` tinyint(3) unsigned DEFAULT NULL,
  `QD` tinyint(3) unsigned DEFAULT NULL,
  `QUAL` mediumint(8) unsigned DEFAULT NULL,
  `ReadPosRankSum` float DEFAULT NULL,
  `MQRankSum` float DEFAULT NULL,
  `FILTER` enum('PASS','LIKELY','INTERMEDIATE','FAIL') DEFAULT NULL,
  `highest_impact` enum('HIGH','MODERATE','LOW','MODIFIER') NOT NULL,
  `PID_variant_id` int(10) unsigned DEFAULT NULL,
  `PGT` tinyint(3) unsigned DEFAULT NULL,
  `HP_variant_id` int(10) unsigned DEFAULT NULL,
  `HP_GT` tinyint(3) unsigned DEFAULT NULL,
  `PQ` mediumint(8) unsigned DEFAULT NULL,
  PRIMARY KEY (`block_id`,`highest_impact`,`sample_id`,`variant_id`),
  KEY `variant_idx` (`variant_id`),
  KEY `sample_variant_idx` (`sample_id`,`variant_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `called_variant_chr15`
--

DROP TABLE IF EXISTS `called_variant_chr15`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `called_variant_chr15` (
  `sample_id` mediumint(8) unsigned NOT NULL,
  `variant_id` int(10) unsigned NOT NULL,
  `block_id` mediumint(8) unsigned NOT NULL,
  `GT` tinyint(3) unsigned NOT NULL,
  `DP` smallint(5) unsigned DEFAULT NULL,
  `AD_REF` smallint(6) DEFAULT NULL,
  `AD_ALT` smallint(5) unsigned DEFAULT NULL,
  `GQ` tinyint(3) unsigned NOT NULL,
  `PL_AA` mediumint(8) unsigned DEFAULT NULL,
  `PL_AB` mediumint(8) unsigned DEFAULT NULL,
  `PL_BB` mediumint(8) unsigned DEFAULT NULL,
  `VQSLOD` float DEFAULT NULL,
  `SOR` float DEFAULT NULL,
  `FS` float DEFAULT NULL,
  `MQ` tinyint(3) unsigned DEFAULT NULL,
  `QD` tinyint(3) unsigned DEFAULT NULL,
  `QUAL` mediumint(8) unsigned DEFAULT NULL,
  `ReadPosRankSum` float DEFAULT NULL,
  `MQRankSum` float DEFAULT NULL,
  `FILTER` enum('PASS','LIKELY','INTERMEDIATE','FAIL') DEFAULT NULL,
  `highest_impact` enum('HIGH','MODERATE','LOW','MODIFIER') NOT NULL,
  `PID_variant_id` int(10) unsigned DEFAULT NULL,
  `PGT` tinyint(3) unsigned DEFAULT NULL,
  `HP_variant_id` int(10) unsigned DEFAULT NULL,
  `HP_GT` tinyint(3) unsigned DEFAULT NULL,
  `PQ` mediumint(8) unsigned DEFAULT NULL,
  PRIMARY KEY (`block_id`,`highest_impact`,`sample_id`,`variant_id`),
  KEY `variant_idx` (`variant_id`),
  KEY `sample_variant_idx` (`sample_id`,`variant_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `called_variant_chr16`
--

DROP TABLE IF EXISTS `called_variant_chr16`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `called_variant_chr16` (
  `sample_id` mediumint(8) unsigned NOT NULL,
  `variant_id` int(10) unsigned NOT NULL,
  `block_id` mediumint(8) unsigned NOT NULL,
  `GT` tinyint(3) unsigned NOT NULL,
  `DP` smallint(5) unsigned DEFAULT NULL,
  `AD_REF` smallint(6) DEFAULT NULL,
  `AD_ALT` smallint(5) unsigned DEFAULT NULL,
  `GQ` tinyint(3) unsigned NOT NULL,
  `PL_AA` mediumint(8) unsigned DEFAULT NULL,
  `PL_AB` mediumint(8) unsigned DEFAULT NULL,
  `PL_BB` mediumint(8) unsigned DEFAULT NULL,
  `VQSLOD` float DEFAULT NULL,
  `SOR` float DEFAULT NULL,
  `FS` float DEFAULT NULL,
  `MQ` tinyint(3) unsigned DEFAULT NULL,
  `QD` tinyint(3) unsigned DEFAULT NULL,
  `QUAL` mediumint(8) unsigned DEFAULT NULL,
  `ReadPosRankSum` float DEFAULT NULL,
  `MQRankSum` float DEFAULT NULL,
  `FILTER` enum('PASS','LIKELY','INTERMEDIATE','FAIL') DEFAULT NULL,
  `highest_impact` enum('HIGH','MODERATE','LOW','MODIFIER') NOT NULL,
  `PID_variant_id` int(10) unsigned DEFAULT NULL,
  `PGT` tinyint(3) unsigned DEFAULT NULL,
  `HP_variant_id` int(10) unsigned DEFAULT NULL,
  `HP_GT` tinyint(3) unsigned DEFAULT NULL,
  `PQ` mediumint(8) unsigned DEFAULT NULL,
  PRIMARY KEY (`block_id`,`highest_impact`,`sample_id`,`variant_id`),
  KEY `variant_idx` (`variant_id`),
  KEY `sample_variant_idx` (`sample_id`,`variant_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `called_variant_chr17`
--

DROP TABLE IF EXISTS `called_variant_chr17`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `called_variant_chr17` (
  `sample_id` mediumint(8) unsigned NOT NULL,
  `variant_id` int(10) unsigned NOT NULL,
  `block_id` mediumint(8) unsigned NOT NULL,
  `GT` tinyint(3) unsigned NOT NULL,
  `DP` smallint(5) unsigned DEFAULT NULL,
  `AD_REF` smallint(6) DEFAULT NULL,
  `AD_ALT` smallint(5) unsigned DEFAULT NULL,
  `GQ` tinyint(3) unsigned NOT NULL,
  `PL_AA` mediumint(8) unsigned DEFAULT NULL,
  `PL_AB` mediumint(8) unsigned DEFAULT NULL,
  `PL_BB` mediumint(8) unsigned DEFAULT NULL,
  `VQSLOD` float DEFAULT NULL,
  `SOR` float DEFAULT NULL,
  `FS` float DEFAULT NULL,
  `MQ` tinyint(3) unsigned DEFAULT NULL,
  `QD` tinyint(3) unsigned DEFAULT NULL,
  `QUAL` mediumint(8) unsigned DEFAULT NULL,
  `ReadPosRankSum` float DEFAULT NULL,
  `MQRankSum` float DEFAULT NULL,
  `FILTER` enum('PASS','LIKELY','INTERMEDIATE','FAIL') DEFAULT NULL,
  `highest_impact` enum('HIGH','MODERATE','LOW','MODIFIER') NOT NULL,
  `PID_variant_id` int(10) unsigned DEFAULT NULL,
  `PGT` tinyint(3) unsigned DEFAULT NULL,
  `HP_variant_id` int(10) unsigned DEFAULT NULL,
  `HP_GT` tinyint(3) unsigned DEFAULT NULL,
  `PQ` mediumint(8) unsigned DEFAULT NULL,
  PRIMARY KEY (`block_id`,`highest_impact`,`sample_id`,`variant_id`),
  KEY `variant_idx` (`variant_id`),
  KEY `sample_variant_idx` (`sample_id`,`variant_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `called_variant_chr18`
--

DROP TABLE IF EXISTS `called_variant_chr18`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `called_variant_chr18` (
  `sample_id` mediumint(8) unsigned NOT NULL,
  `variant_id` int(10) unsigned NOT NULL,
  `block_id` mediumint(8) unsigned NOT NULL,
  `GT` tinyint(3) unsigned NOT NULL,
  `DP` smallint(5) unsigned DEFAULT NULL,
  `AD_REF` smallint(6) DEFAULT NULL,
  `AD_ALT` smallint(5) unsigned DEFAULT NULL,
  `GQ` tinyint(3) unsigned NOT NULL,
  `PL_AA` mediumint(8) unsigned DEFAULT NULL,
  `PL_AB` mediumint(8) unsigned DEFAULT NULL,
  `PL_BB` mediumint(8) unsigned DEFAULT NULL,
  `VQSLOD` float DEFAULT NULL,
  `SOR` float DEFAULT NULL,
  `FS` float DEFAULT NULL,
  `MQ` tinyint(3) unsigned DEFAULT NULL,
  `QD` tinyint(3) unsigned DEFAULT NULL,
  `QUAL` mediumint(8) unsigned DEFAULT NULL,
  `ReadPosRankSum` float DEFAULT NULL,
  `MQRankSum` float DEFAULT NULL,
  `FILTER` enum('PASS','LIKELY','INTERMEDIATE','FAIL') DEFAULT NULL,
  `highest_impact` enum('HIGH','MODERATE','LOW','MODIFIER') NOT NULL,
  `PID_variant_id` int(10) unsigned DEFAULT NULL,
  `PGT` tinyint(3) unsigned DEFAULT NULL,
  `HP_variant_id` int(10) unsigned DEFAULT NULL,
  `HP_GT` tinyint(3) unsigned DEFAULT NULL,
  `PQ` mediumint(8) unsigned DEFAULT NULL,
  PRIMARY KEY (`block_id`,`highest_impact`,`sample_id`,`variant_id`),
  KEY `variant_idx` (`variant_id`),
  KEY `sample_variant_idx` (`sample_id`,`variant_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `called_variant_chr19`
--

DROP TABLE IF EXISTS `called_variant_chr19`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `called_variant_chr19` (
  `sample_id` mediumint(8) unsigned NOT NULL,
  `variant_id` int(10) unsigned NOT NULL,
  `block_id` mediumint(8) unsigned NOT NULL,
  `GT` tinyint(3) unsigned NOT NULL,
  `DP` smallint(5) unsigned DEFAULT NULL,
  `AD_REF` smallint(6) DEFAULT NULL,
  `AD_ALT` smallint(5) unsigned DEFAULT NULL,
  `GQ` tinyint(3) unsigned NOT NULL,
  `PL_AA` mediumint(8) unsigned DEFAULT NULL,
  `PL_AB` mediumint(8) unsigned DEFAULT NULL,
  `PL_BB` mediumint(8) unsigned DEFAULT NULL,
  `VQSLOD` float DEFAULT NULL,
  `SOR` float DEFAULT NULL,
  `FS` float DEFAULT NULL,
  `MQ` tinyint(3) unsigned DEFAULT NULL,
  `QD` tinyint(3) unsigned DEFAULT NULL,
  `QUAL` mediumint(8) unsigned DEFAULT NULL,
  `ReadPosRankSum` float DEFAULT NULL,
  `MQRankSum` float DEFAULT NULL,
  `FILTER` enum('PASS','LIKELY','INTERMEDIATE','FAIL') DEFAULT NULL,
  `highest_impact` enum('HIGH','MODERATE','LOW','MODIFIER') NOT NULL,
  `PID_variant_id` int(10) unsigned DEFAULT NULL,
  `PGT` tinyint(3) unsigned DEFAULT NULL,
  `HP_variant_id` int(10) unsigned DEFAULT NULL,
  `HP_GT` tinyint(3) unsigned DEFAULT NULL,
  `PQ` mediumint(8) unsigned DEFAULT NULL,
  PRIMARY KEY (`block_id`,`highest_impact`,`sample_id`,`variant_id`),
  KEY `variant_idx` (`variant_id`),
  KEY `sample_variant_idx` (`sample_id`,`variant_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `called_variant_chr2`
--

DROP TABLE IF EXISTS `called_variant_chr2`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `called_variant_chr2` (
  `sample_id` mediumint(8) unsigned NOT NULL,
  `variant_id` int(10) unsigned NOT NULL,
  `block_id` mediumint(8) unsigned NOT NULL,
  `GT` tinyint(3) unsigned NOT NULL,
  `DP` smallint(5) unsigned DEFAULT NULL,
  `AD_REF` smallint(5) unsigned DEFAULT NULL,
  `AD_ALT` smallint(5) unsigned DEFAULT NULL,
  `GQ` tinyint(3) unsigned NOT NULL,
  `PL_AA` mediumint(8) unsigned DEFAULT NULL,
  `PL_AB` mediumint(8) unsigned DEFAULT NULL,
  `PL_BB` mediumint(8) unsigned DEFAULT NULL,
  `VQSLOD` float DEFAULT NULL,
  `SOR` float DEFAULT NULL,
  `FS` float DEFAULT NULL,
  `MQ` tinyint(3) unsigned DEFAULT NULL,
  `QD` tinyint(3) unsigned DEFAULT NULL,
  `QUAL` mediumint(8) unsigned DEFAULT NULL,
  `ReadPosRankSum` float DEFAULT NULL,
  `MQRankSum` float DEFAULT NULL,
  `FILTER` enum('PASS','LIKELY','INTERMEDIATE','FAIL') DEFAULT NULL,
  `highest_impact` enum('HIGH','MODERATE','LOW','MODIFIER') NOT NULL,
  `PID_variant_id` int(10) unsigned DEFAULT NULL,
  `PGT` tinyint(3) unsigned DEFAULT NULL,
  `HP_variant_id` int(10) unsigned DEFAULT NULL,
  `HP_GT` tinyint(3) unsigned DEFAULT NULL,
  `PQ` mediumint(8) unsigned DEFAULT NULL,
  PRIMARY KEY (`block_id`,`highest_impact`,`sample_id`,`variant_id`),
  KEY `variant_idx` (`variant_id`),
  KEY `sample_variant_idx` (`sample_id`,`variant_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `called_variant_chr20`
--

DROP TABLE IF EXISTS `called_variant_chr20`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `called_variant_chr20` (
  `sample_id` mediumint(8) unsigned NOT NULL,
  `variant_id` int(10) unsigned NOT NULL,
  `block_id` mediumint(8) unsigned NOT NULL,
  `GT` tinyint(3) unsigned NOT NULL,
  `DP` smallint(5) unsigned DEFAULT NULL,
  `AD_REF` smallint(6) DEFAULT NULL,
  `AD_ALT` smallint(5) unsigned DEFAULT NULL,
  `GQ` tinyint(3) unsigned NOT NULL,
  `PL_AA` mediumint(8) unsigned DEFAULT NULL,
  `PL_AB` mediumint(8) unsigned DEFAULT NULL,
  `PL_BB` mediumint(8) unsigned DEFAULT NULL,
  `VQSLOD` float DEFAULT NULL,
  `SOR` float DEFAULT NULL,
  `FS` float DEFAULT NULL,
  `MQ` tinyint(3) unsigned DEFAULT NULL,
  `QD` tinyint(3) unsigned DEFAULT NULL,
  `QUAL` mediumint(8) unsigned DEFAULT NULL,
  `ReadPosRankSum` float DEFAULT NULL,
  `MQRankSum` float DEFAULT NULL,
  `FILTER` enum('PASS','LIKELY','INTERMEDIATE','FAIL') DEFAULT NULL,
  `highest_impact` enum('HIGH','MODERATE','LOW','MODIFIER') NOT NULL,
  `PID_variant_id` int(10) unsigned DEFAULT NULL,
  `PGT` tinyint(3) unsigned DEFAULT NULL,
  `HP_variant_id` int(10) unsigned DEFAULT NULL,
  `HP_GT` tinyint(3) unsigned DEFAULT NULL,
  `PQ` mediumint(8) unsigned DEFAULT NULL,
  PRIMARY KEY (`block_id`,`highest_impact`,`sample_id`,`variant_id`),
  KEY `variant_idx` (`variant_id`),
  KEY `sample_variant_idx` (`sample_id`,`variant_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `called_variant_chr21`
--

DROP TABLE IF EXISTS `called_variant_chr21`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `called_variant_chr21` (
  `sample_id` mediumint(8) unsigned NOT NULL,
  `variant_id` int(10) unsigned NOT NULL,
  `block_id` mediumint(8) unsigned NOT NULL,
  `GT` tinyint(3) unsigned NOT NULL,
  `DP` smallint(5) unsigned DEFAULT NULL,
  `AD_REF` smallint(6) DEFAULT NULL,
  `AD_ALT` smallint(5) unsigned DEFAULT NULL,
  `GQ` tinyint(3) unsigned NOT NULL,
  `PL_AA` mediumint(8) unsigned DEFAULT NULL,
  `PL_AB` mediumint(8) unsigned DEFAULT NULL,
  `PL_BB` mediumint(8) unsigned DEFAULT NULL,
  `VQSLOD` float DEFAULT NULL,
  `SOR` float DEFAULT NULL,
  `FS` float DEFAULT NULL,
  `MQ` tinyint(3) unsigned DEFAULT NULL,
  `QD` tinyint(3) unsigned DEFAULT NULL,
  `QUAL` mediumint(8) unsigned DEFAULT NULL,
  `ReadPosRankSum` float DEFAULT NULL,
  `MQRankSum` float DEFAULT NULL,
  `FILTER` enum('PASS','LIKELY','INTERMEDIATE','FAIL') DEFAULT NULL,
  `highest_impact` enum('HIGH','MODERATE','LOW','MODIFIER') NOT NULL,
  `PID_variant_id` int(10) unsigned DEFAULT NULL,
  `PGT` tinyint(3) unsigned DEFAULT NULL,
  `HP_variant_id` int(10) unsigned DEFAULT NULL,
  `HP_GT` tinyint(3) unsigned DEFAULT NULL,
  `PQ` mediumint(8) unsigned DEFAULT NULL,
  PRIMARY KEY (`block_id`,`highest_impact`,`sample_id`,`variant_id`),
  KEY `variant_idx` (`variant_id`),
  KEY `sample_variant_idx` (`sample_id`,`variant_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `called_variant_chr22`
--

DROP TABLE IF EXISTS `called_variant_chr22`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `called_variant_chr22` (
  `sample_id` mediumint(8) unsigned NOT NULL,
  `variant_id` int(10) unsigned NOT NULL,
  `block_id` mediumint(8) unsigned NOT NULL,
  `GT` tinyint(3) unsigned NOT NULL,
  `DP` smallint(5) unsigned DEFAULT NULL,
  `AD_REF` smallint(6) DEFAULT NULL,
  `AD_ALT` smallint(5) unsigned DEFAULT NULL,
  `GQ` tinyint(3) unsigned NOT NULL,
  `PL_AA` mediumint(8) unsigned DEFAULT NULL,
  `PL_AB` mediumint(8) unsigned DEFAULT NULL,
  `PL_BB` mediumint(8) unsigned DEFAULT NULL,
  `VQSLOD` float DEFAULT NULL,
  `SOR` float DEFAULT NULL,
  `FS` float DEFAULT NULL,
  `MQ` tinyint(3) unsigned DEFAULT NULL,
  `QD` tinyint(3) unsigned DEFAULT NULL,
  `QUAL` mediumint(8) unsigned DEFAULT NULL,
  `ReadPosRankSum` float DEFAULT NULL,
  `MQRankSum` float DEFAULT NULL,
  `FILTER` enum('PASS','LIKELY','INTERMEDIATE','FAIL') DEFAULT NULL,
  `highest_impact` enum('HIGH','MODERATE','LOW','MODIFIER') NOT NULL,
  `PID_variant_id` int(10) unsigned DEFAULT NULL,
  `PGT` tinyint(3) unsigned DEFAULT NULL,
  `HP_variant_id` int(10) unsigned DEFAULT NULL,
  `HP_GT` tinyint(3) unsigned DEFAULT NULL,
  `PQ` mediumint(8) unsigned DEFAULT NULL,
  PRIMARY KEY (`block_id`,`highest_impact`,`sample_id`,`variant_id`),
  KEY `variant_idx` (`variant_id`),
  KEY `sample_variant_idx` (`sample_id`,`variant_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `called_variant_chr3`
--

DROP TABLE IF EXISTS `called_variant_chr3`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `called_variant_chr3` (
  `sample_id` mediumint(8) unsigned NOT NULL,
  `variant_id` int(10) unsigned NOT NULL,
  `block_id` mediumint(8) unsigned NOT NULL,
  `GT` tinyint(3) unsigned NOT NULL,
  `DP` smallint(5) unsigned DEFAULT NULL,
  `AD_REF` smallint(6) DEFAULT NULL,
  `AD_ALT` smallint(5) unsigned DEFAULT NULL,
  `GQ` tinyint(3) unsigned NOT NULL,
  `PL_AA` mediumint(8) unsigned DEFAULT NULL,
  `PL_AB` mediumint(8) unsigned DEFAULT NULL,
  `PL_BB` mediumint(8) unsigned DEFAULT NULL,
  `VQSLOD` float DEFAULT NULL,
  `SOR` float DEFAULT NULL,
  `FS` float DEFAULT NULL,
  `MQ` tinyint(3) unsigned DEFAULT NULL,
  `QD` tinyint(3) unsigned DEFAULT NULL,
  `QUAL` mediumint(8) unsigned DEFAULT NULL,
  `ReadPosRankSum` float DEFAULT NULL,
  `MQRankSum` float DEFAULT NULL,
  `FILTER` enum('PASS','LIKELY','INTERMEDIATE','FAIL') DEFAULT NULL,
  `highest_impact` enum('HIGH','MODERATE','LOW','MODIFIER') NOT NULL,
  `PID_variant_id` int(10) unsigned DEFAULT NULL,
  `PGT` tinyint(3) unsigned DEFAULT NULL,
  `HP_variant_id` int(10) unsigned DEFAULT NULL,
  `HP_GT` tinyint(3) unsigned DEFAULT NULL,
  `PQ` mediumint(8) unsigned DEFAULT NULL,
  PRIMARY KEY (`block_id`,`highest_impact`,`sample_id`,`variant_id`),
  KEY `variant_idx` (`variant_id`),
  KEY `sample_variant_idx` (`sample_id`,`variant_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `called_variant_chr4`
--

DROP TABLE IF EXISTS `called_variant_chr4`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `called_variant_chr4` (
  `sample_id` mediumint(8) unsigned NOT NULL,
  `variant_id` int(10) unsigned NOT NULL,
  `block_id` mediumint(8) unsigned NOT NULL,
  `GT` tinyint(3) unsigned NOT NULL,
  `DP` smallint(5) unsigned DEFAULT NULL,
  `AD_REF` smallint(6) DEFAULT NULL,
  `AD_ALT` smallint(5) unsigned DEFAULT NULL,
  `GQ` tinyint(3) unsigned NOT NULL,
  `PL_AA` mediumint(8) unsigned DEFAULT NULL,
  `PL_AB` mediumint(8) unsigned DEFAULT NULL,
  `PL_BB` mediumint(8) unsigned DEFAULT NULL,
  `VQSLOD` float DEFAULT NULL,
  `SOR` float DEFAULT NULL,
  `FS` float DEFAULT NULL,
  `MQ` tinyint(3) unsigned DEFAULT NULL,
  `QD` tinyint(3) unsigned DEFAULT NULL,
  `QUAL` mediumint(8) unsigned DEFAULT NULL,
  `ReadPosRankSum` float DEFAULT NULL,
  `MQRankSum` float DEFAULT NULL,
  `FILTER` enum('PASS','LIKELY','INTERMEDIATE','FAIL') DEFAULT NULL,
  `highest_impact` enum('HIGH','MODERATE','LOW','MODIFIER') NOT NULL,
  `PID_variant_id` int(10) unsigned DEFAULT NULL,
  `PGT` tinyint(3) unsigned DEFAULT NULL,
  `HP_variant_id` int(10) unsigned DEFAULT NULL,
  `HP_GT` tinyint(3) unsigned DEFAULT NULL,
  `PQ` mediumint(8) unsigned DEFAULT NULL,
  PRIMARY KEY (`block_id`,`highest_impact`,`sample_id`,`variant_id`),
  KEY `variant_idx` (`variant_id`),
  KEY `sample_variant_idx` (`sample_id`,`variant_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `called_variant_chr5`
--

DROP TABLE IF EXISTS `called_variant_chr5`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `called_variant_chr5` (
  `sample_id` mediumint(8) unsigned NOT NULL,
  `variant_id` int(10) unsigned NOT NULL,
  `block_id` mediumint(8) unsigned NOT NULL,
  `GT` tinyint(3) unsigned NOT NULL,
  `DP` smallint(5) unsigned DEFAULT NULL,
  `AD_REF` smallint(6) DEFAULT NULL,
  `AD_ALT` smallint(5) unsigned DEFAULT NULL,
  `GQ` tinyint(3) unsigned NOT NULL,
  `PL_AA` mediumint(8) unsigned DEFAULT NULL,
  `PL_AB` mediumint(8) unsigned DEFAULT NULL,
  `PL_BB` mediumint(8) unsigned DEFAULT NULL,
  `VQSLOD` float DEFAULT NULL,
  `SOR` float DEFAULT NULL,
  `FS` float DEFAULT NULL,
  `MQ` tinyint(3) unsigned DEFAULT NULL,
  `QD` tinyint(3) unsigned DEFAULT NULL,
  `QUAL` mediumint(8) unsigned DEFAULT NULL,
  `ReadPosRankSum` float DEFAULT NULL,
  `MQRankSum` float DEFAULT NULL,
  `FILTER` enum('PASS','LIKELY','INTERMEDIATE','FAIL') DEFAULT NULL,
  `highest_impact` enum('HIGH','MODERATE','LOW','MODIFIER') NOT NULL,
  `PID_variant_id` int(10) unsigned DEFAULT NULL,
  `PGT` tinyint(3) unsigned DEFAULT NULL,
  `HP_variant_id` int(10) unsigned DEFAULT NULL,
  `HP_GT` tinyint(3) unsigned DEFAULT NULL,
  `PQ` mediumint(8) unsigned DEFAULT NULL,
  PRIMARY KEY (`block_id`,`highest_impact`,`sample_id`,`variant_id`),
  KEY `variant_idx` (`variant_id`),
  KEY `sample_variant_idx` (`sample_id`,`variant_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `called_variant_chr6`
--

DROP TABLE IF EXISTS `called_variant_chr6`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `called_variant_chr6` (
  `sample_id` mediumint(8) unsigned NOT NULL,
  `variant_id` int(10) unsigned NOT NULL,
  `block_id` mediumint(8) unsigned NOT NULL,
  `GT` tinyint(3) unsigned NOT NULL,
  `DP` smallint(5) unsigned DEFAULT NULL,
  `AD_REF` smallint(6) DEFAULT NULL,
  `AD_ALT` smallint(5) unsigned DEFAULT NULL,
  `GQ` tinyint(3) unsigned NOT NULL,
  `PL_AA` mediumint(8) unsigned DEFAULT NULL,
  `PL_AB` mediumint(8) unsigned DEFAULT NULL,
  `PL_BB` mediumint(8) unsigned DEFAULT NULL,
  `VQSLOD` float DEFAULT NULL,
  `SOR` float DEFAULT NULL,
  `FS` float DEFAULT NULL,
  `MQ` tinyint(3) unsigned DEFAULT NULL,
  `QD` tinyint(3) unsigned DEFAULT NULL,
  `QUAL` mediumint(8) unsigned DEFAULT NULL,
  `ReadPosRankSum` float DEFAULT NULL,
  `MQRankSum` float DEFAULT NULL,
  `FILTER` enum('PASS','LIKELY','INTERMEDIATE','FAIL') DEFAULT NULL,
  `highest_impact` enum('HIGH','MODERATE','LOW','MODIFIER') NOT NULL,
  `PID_variant_id` int(10) unsigned DEFAULT NULL,
  `PGT` tinyint(3) unsigned DEFAULT NULL,
  `HP_variant_id` int(10) unsigned DEFAULT NULL,
  `HP_GT` tinyint(3) unsigned DEFAULT NULL,
  `PQ` mediumint(8) unsigned DEFAULT NULL,
  PRIMARY KEY (`block_id`,`highest_impact`,`sample_id`,`variant_id`),
  KEY `variant_idx` (`variant_id`),
  KEY `sample_variant_idx` (`sample_id`,`variant_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `called_variant_chr7`
--

DROP TABLE IF EXISTS `called_variant_chr7`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `called_variant_chr7` (
  `sample_id` mediumint(8) unsigned NOT NULL,
  `variant_id` int(10) unsigned NOT NULL,
  `block_id` mediumint(8) unsigned NOT NULL,
  `GT` tinyint(3) unsigned NOT NULL,
  `DP` smallint(5) unsigned DEFAULT NULL,
  `AD_REF` smallint(6) DEFAULT NULL,
  `AD_ALT` smallint(5) unsigned DEFAULT NULL,
  `GQ` tinyint(3) unsigned NOT NULL,
  `PL_AA` mediumint(8) unsigned DEFAULT NULL,
  `PL_AB` mediumint(8) unsigned DEFAULT NULL,
  `PL_BB` mediumint(8) unsigned DEFAULT NULL,
  `VQSLOD` float DEFAULT NULL,
  `SOR` float DEFAULT NULL,
  `FS` float DEFAULT NULL,
  `MQ` tinyint(3) unsigned DEFAULT NULL,
  `QD` tinyint(3) unsigned DEFAULT NULL,
  `QUAL` mediumint(8) unsigned DEFAULT NULL,
  `ReadPosRankSum` float DEFAULT NULL,
  `MQRankSum` float DEFAULT NULL,
  `FILTER` enum('PASS','LIKELY','INTERMEDIATE','FAIL') DEFAULT NULL,
  `highest_impact` enum('HIGH','MODERATE','LOW','MODIFIER') NOT NULL,
  `PID_variant_id` int(10) unsigned DEFAULT NULL,
  `PGT` tinyint(3) unsigned DEFAULT NULL,
  `HP_variant_id` int(10) unsigned DEFAULT NULL,
  `HP_GT` tinyint(3) unsigned DEFAULT NULL,
  `PQ` mediumint(8) unsigned DEFAULT NULL,
  PRIMARY KEY (`block_id`,`highest_impact`,`sample_id`,`variant_id`),
  KEY `variant_idx` (`variant_id`),
  KEY `sample_variant_idx` (`sample_id`,`variant_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `called_variant_chr8`
--

DROP TABLE IF EXISTS `called_variant_chr8`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `called_variant_chr8` (
  `sample_id` mediumint(8) unsigned NOT NULL,
  `variant_id` int(10) unsigned NOT NULL,
  `block_id` mediumint(8) unsigned NOT NULL,
  `GT` tinyint(3) unsigned NOT NULL,
  `DP` smallint(5) unsigned DEFAULT NULL,
  `AD_REF` smallint(6) DEFAULT NULL,
  `AD_ALT` smallint(5) unsigned DEFAULT NULL,
  `GQ` tinyint(3) unsigned NOT NULL,
  `PL_AA` mediumint(8) unsigned DEFAULT NULL,
  `PL_AB` mediumint(8) unsigned DEFAULT NULL,
  `PL_BB` mediumint(8) unsigned DEFAULT NULL,
  `VQSLOD` float DEFAULT NULL,
  `SOR` float DEFAULT NULL,
  `FS` float DEFAULT NULL,
  `MQ` tinyint(3) unsigned DEFAULT NULL,
  `QD` tinyint(3) unsigned DEFAULT NULL,
  `QUAL` mediumint(8) unsigned DEFAULT NULL,
  `ReadPosRankSum` float DEFAULT NULL,
  `MQRankSum` float DEFAULT NULL,
  `FILTER` enum('PASS','LIKELY','INTERMEDIATE','FAIL') DEFAULT NULL,
  `highest_impact` enum('HIGH','MODERATE','LOW','MODIFIER') NOT NULL,
  `PID_variant_id` int(10) unsigned DEFAULT NULL,
  `PGT` tinyint(3) unsigned DEFAULT NULL,
  `HP_variant_id` int(10) unsigned DEFAULT NULL,
  `HP_GT` tinyint(3) unsigned DEFAULT NULL,
  `PQ` mediumint(8) unsigned DEFAULT NULL,
  PRIMARY KEY (`block_id`,`highest_impact`,`sample_id`,`variant_id`),
  KEY `variant_idx` (`variant_id`),
  KEY `sample_variant_idx` (`sample_id`,`variant_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `called_variant_chr9`
--

DROP TABLE IF EXISTS `called_variant_chr9`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `called_variant_chr9` (
  `sample_id` mediumint(8) unsigned NOT NULL,
  `variant_id` int(10) unsigned NOT NULL,
  `block_id` mediumint(8) unsigned NOT NULL,
  `GT` tinyint(3) unsigned NOT NULL,
  `DP` smallint(5) unsigned DEFAULT NULL,
  `AD_REF` smallint(6) DEFAULT NULL,
  `AD_ALT` smallint(5) unsigned DEFAULT NULL,
  `GQ` tinyint(3) unsigned NOT NULL,
  `PL_AA` mediumint(8) unsigned DEFAULT NULL,
  `PL_AB` mediumint(8) unsigned DEFAULT NULL,
  `PL_BB` mediumint(8) unsigned DEFAULT NULL,
  `VQSLOD` float DEFAULT NULL,
  `SOR` float DEFAULT NULL,
  `FS` float DEFAULT NULL,
  `MQ` tinyint(3) unsigned DEFAULT NULL,
  `QD` tinyint(3) unsigned DEFAULT NULL,
  `QUAL` mediumint(8) unsigned DEFAULT NULL,
  `ReadPosRankSum` float DEFAULT NULL,
  `MQRankSum` float DEFAULT NULL,
  `FILTER` enum('PASS','LIKELY','INTERMEDIATE','FAIL') DEFAULT NULL,
  `highest_impact` enum('HIGH','MODERATE','LOW','MODIFIER') NOT NULL,
  `PID_variant_id` int(10) unsigned DEFAULT NULL,
  `PGT` tinyint(3) unsigned DEFAULT NULL,
  `HP_variant_id` int(10) unsigned DEFAULT NULL,
  `HP_GT` tinyint(3) unsigned DEFAULT NULL,
  `PQ` mediumint(8) unsigned DEFAULT NULL,
  PRIMARY KEY (`block_id`,`highest_impact`,`sample_id`,`variant_id`),
  KEY `variant_idx` (`variant_id`),
  KEY `sample_variant_idx` (`sample_id`,`variant_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `called_variant_chrMT`
--

DROP TABLE IF EXISTS `called_variant_chrMT`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `called_variant_chrMT` (
  `sample_id` mediumint(8) unsigned NOT NULL,
  `variant_id` int(10) unsigned NOT NULL,
  `block_id` mediumint(8) unsigned NOT NULL,
  `GT` tinyint(3) unsigned NOT NULL,
  `DP` smallint(5) unsigned DEFAULT NULL,
  `AD_REF` smallint(6) DEFAULT NULL,
  `AD_ALT` smallint(5) unsigned DEFAULT NULL,
  `GQ` tinyint(3) unsigned NOT NULL,
  `PL_AA` mediumint(8) unsigned DEFAULT NULL,
  `PL_AB` mediumint(8) unsigned DEFAULT NULL,
  `PL_BB` mediumint(8) unsigned DEFAULT NULL,
  `VQSLOD` float DEFAULT NULL,
  `SOR` float DEFAULT NULL,
  `FS` float DEFAULT NULL,
  `MQ` tinyint(3) unsigned DEFAULT NULL,
  `QD` tinyint(3) unsigned DEFAULT NULL,
  `QUAL` mediumint(8) unsigned DEFAULT NULL,
  `ReadPosRankSum` float DEFAULT NULL,
  `MQRankSum` float DEFAULT NULL,
  `FILTER` enum('PASS','LIKELY','INTERMEDIATE','FAIL') DEFAULT NULL,
  `highest_impact` enum('HIGH','MODERATE','LOW','MODIFIER') NOT NULL,
  `PID_variant_id` int(10) unsigned DEFAULT NULL,
  `PGT` tinyint(3) unsigned DEFAULT NULL,
  `HP_variant_id` int(10) unsigned DEFAULT NULL,
  `HP_GT` tinyint(3) unsigned DEFAULT NULL,
  `PQ` mediumint(8) unsigned DEFAULT NULL,
  PRIMARY KEY (`block_id`,`highest_impact`,`sample_id`,`variant_id`),
  KEY `variant_idx` (`variant_id`),
  KEY `sample_variant_idx` (`sample_id`,`variant_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `called_variant_chrX`
--

DROP TABLE IF EXISTS `called_variant_chrX`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `called_variant_chrX` (
  `sample_id` mediumint(8) unsigned NOT NULL,
  `variant_id` int(10) unsigned NOT NULL,
  `block_id` mediumint(8) unsigned NOT NULL,
  `GT` tinyint(3) unsigned NOT NULL,
  `DP` smallint(5) unsigned DEFAULT NULL,
  `AD_REF` smallint(6) DEFAULT NULL,
  `AD_ALT` smallint(5) unsigned DEFAULT NULL,
  `GQ` tinyint(3) unsigned NOT NULL,
  `PL_AA` mediumint(8) unsigned DEFAULT NULL,
  `PL_AB` mediumint(8) unsigned DEFAULT NULL,
  `PL_BB` mediumint(8) unsigned DEFAULT NULL,
  `VQSLOD` float DEFAULT NULL,
  `SOR` float DEFAULT NULL,
  `FS` float DEFAULT NULL,
  `MQ` tinyint(3) unsigned DEFAULT NULL,
  `QD` tinyint(3) unsigned DEFAULT NULL,
  `QUAL` mediumint(8) unsigned DEFAULT NULL,
  `ReadPosRankSum` float DEFAULT NULL,
  `MQRankSum` float DEFAULT NULL,
  `FILTER` enum('PASS','LIKELY','INTERMEDIATE','FAIL') DEFAULT NULL,
  `highest_impact` enum('HIGH','MODERATE','LOW','MODIFIER') NOT NULL,
  `PID_variant_id` int(10) unsigned DEFAULT NULL,
  `PGT` tinyint(3) unsigned DEFAULT NULL,
  `HP_variant_id` int(10) unsigned DEFAULT NULL,
  `HP_GT` tinyint(3) unsigned DEFAULT NULL,
  `PQ` mediumint(8) unsigned DEFAULT NULL,
  PRIMARY KEY (`block_id`,`highest_impact`,`sample_id`,`variant_id`),
  KEY `variant_idx` (`variant_id`),
  KEY `sample_variant_idx` (`sample_id`,`variant_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `called_variant_chrY`
--

DROP TABLE IF EXISTS `called_variant_chrY`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `called_variant_chrY` (
  `sample_id` mediumint(8) unsigned NOT NULL,
  `variant_id` int(10) unsigned NOT NULL,
  `block_id` mediumint(8) unsigned NOT NULL,
  `GT` tinyint(3) unsigned NOT NULL,
  `DP` smallint(5) unsigned DEFAULT NULL,
  `AD_REF` smallint(6) DEFAULT NULL,
  `AD_ALT` smallint(5) unsigned DEFAULT NULL,
  `GQ` tinyint(3) unsigned NOT NULL,
  `PL_AA` mediumint(8) unsigned DEFAULT NULL,
  `PL_AB` mediumint(8) unsigned DEFAULT NULL,
  `PL_BB` mediumint(8) unsigned DEFAULT NULL,
  `VQSLOD` float DEFAULT NULL,
  `SOR` float DEFAULT NULL,
  `FS` float DEFAULT NULL,
  `MQ` tinyint(3) unsigned DEFAULT NULL,
  `QD` tinyint(3) unsigned DEFAULT NULL,
  `QUAL` mediumint(8) unsigned DEFAULT NULL,
  `ReadPosRankSum` float DEFAULT NULL,
  `MQRankSum` float DEFAULT NULL,
  `FILTER` enum('PASS','LIKELY','INTERMEDIATE','FAIL') DEFAULT NULL,
  `highest_impact` enum('HIGH','MODERATE','LOW','MODIFIER') NOT NULL,
  `PID_variant_id` int(10) unsigned DEFAULT NULL,
  `PGT` tinyint(3) unsigned DEFAULT NULL,
  `HP_variant_id` int(10) unsigned DEFAULT NULL,
  `HP_GT` tinyint(3) unsigned DEFAULT NULL,
  `PQ` mediumint(8) unsigned DEFAULT NULL,
  PRIMARY KEY (`block_id`,`highest_impact`,`sample_id`,`variant_id`),
  KEY `variant_idx` (`variant_id`),
  KEY `sample_variant_idx` (`sample_id`,`variant_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `codingandsplice_effect`
--

DROP TABLE IF EXISTS `codingandsplice_effect`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `codingandsplice_effect` (
  `id` tinyint(3) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `custom_transcript_ids_chr1`
--

DROP TABLE IF EXISTS `custom_transcript_ids_chr1`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `custom_transcript_ids_chr1` (
  `id` int(11) NOT NULL,
  `transcript_ids` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `transcript_idx` (`transcript_ids`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `custom_transcript_ids_chr10`
--

DROP TABLE IF EXISTS `custom_transcript_ids_chr10`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `custom_transcript_ids_chr10` (
  `id` int(11) NOT NULL,
  `transcript_ids` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `transcript_idx` (`transcript_ids`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `custom_transcript_ids_chr11`
--

DROP TABLE IF EXISTS `custom_transcript_ids_chr11`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `custom_transcript_ids_chr11` (
  `id` int(11) NOT NULL,
  `transcript_ids` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `transcript_idx` (`transcript_ids`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `custom_transcript_ids_chr12`
--

DROP TABLE IF EXISTS `custom_transcript_ids_chr12`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `custom_transcript_ids_chr12` (
  `id` int(11) NOT NULL,
  `transcript_ids` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `transcript_idx` (`transcript_ids`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `custom_transcript_ids_chr13`
--

DROP TABLE IF EXISTS `custom_transcript_ids_chr13`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `custom_transcript_ids_chr13` (
  `id` int(11) NOT NULL,
  `transcript_ids` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `transcript_idx` (`transcript_ids`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `custom_transcript_ids_chr14`
--

DROP TABLE IF EXISTS `custom_transcript_ids_chr14`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `custom_transcript_ids_chr14` (
  `id` int(11) NOT NULL,
  `transcript_ids` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `transcript_idx` (`transcript_ids`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `custom_transcript_ids_chr15`
--

DROP TABLE IF EXISTS `custom_transcript_ids_chr15`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `custom_transcript_ids_chr15` (
  `id` int(11) NOT NULL,
  `transcript_ids` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `transcript_idx` (`transcript_ids`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `custom_transcript_ids_chr16`
--

DROP TABLE IF EXISTS `custom_transcript_ids_chr16`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `custom_transcript_ids_chr16` (
  `id` int(11) NOT NULL,
  `transcript_ids` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `transcript_idx` (`transcript_ids`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `custom_transcript_ids_chr17`
--

DROP TABLE IF EXISTS `custom_transcript_ids_chr17`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `custom_transcript_ids_chr17` (
  `id` int(11) NOT NULL,
  `transcript_ids` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `transcript_idx` (`transcript_ids`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `custom_transcript_ids_chr18`
--

DROP TABLE IF EXISTS `custom_transcript_ids_chr18`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `custom_transcript_ids_chr18` (
  `id` int(11) NOT NULL,
  `transcript_ids` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `transcript_idx` (`transcript_ids`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `custom_transcript_ids_chr19`
--

DROP TABLE IF EXISTS `custom_transcript_ids_chr19`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `custom_transcript_ids_chr19` (
  `id` int(11) NOT NULL,
  `transcript_ids` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `transcript_idx` (`transcript_ids`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `custom_transcript_ids_chr2`
--

DROP TABLE IF EXISTS `custom_transcript_ids_chr2`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `custom_transcript_ids_chr2` (
  `id` int(11) NOT NULL,
  `transcript_ids` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `transcript_idx` (`transcript_ids`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `custom_transcript_ids_chr20`
--

DROP TABLE IF EXISTS `custom_transcript_ids_chr20`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `custom_transcript_ids_chr20` (
  `id` int(11) NOT NULL,
  `transcript_ids` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `transcript_idx` (`transcript_ids`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `custom_transcript_ids_chr21`
--

DROP TABLE IF EXISTS `custom_transcript_ids_chr21`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `custom_transcript_ids_chr21` (
  `id` int(11) NOT NULL,
  `transcript_ids` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `transcript_idx` (`transcript_ids`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `custom_transcript_ids_chr22`
--

DROP TABLE IF EXISTS `custom_transcript_ids_chr22`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `custom_transcript_ids_chr22` (
  `id` int(11) NOT NULL,
  `transcript_ids` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `transcript_idx` (`transcript_ids`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `custom_transcript_ids_chr3`
--

DROP TABLE IF EXISTS `custom_transcript_ids_chr3`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `custom_transcript_ids_chr3` (
  `id` int(11) NOT NULL,
  `transcript_ids` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `transcript_idx` (`transcript_ids`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `custom_transcript_ids_chr4`
--

DROP TABLE IF EXISTS `custom_transcript_ids_chr4`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `custom_transcript_ids_chr4` (
  `id` int(11) NOT NULL,
  `transcript_ids` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `transcript_idx` (`transcript_ids`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `custom_transcript_ids_chr5`
--

DROP TABLE IF EXISTS `custom_transcript_ids_chr5`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `custom_transcript_ids_chr5` (
  `id` int(11) NOT NULL,
  `transcript_ids` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `transcript_idx` (`transcript_ids`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `custom_transcript_ids_chr6`
--

DROP TABLE IF EXISTS `custom_transcript_ids_chr6`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `custom_transcript_ids_chr6` (
  `id` int(11) NOT NULL,
  `transcript_ids` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `transcript_idx` (`transcript_ids`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `custom_transcript_ids_chr7`
--

DROP TABLE IF EXISTS `custom_transcript_ids_chr7`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `custom_transcript_ids_chr7` (
  `id` int(11) NOT NULL,
  `transcript_ids` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `transcript_idx` (`transcript_ids`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `custom_transcript_ids_chr8`
--

DROP TABLE IF EXISTS `custom_transcript_ids_chr8`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `custom_transcript_ids_chr8` (
  `id` int(11) NOT NULL,
  `transcript_ids` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `transcript_idx` (`transcript_ids`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `custom_transcript_ids_chr9`
--

DROP TABLE IF EXISTS `custom_transcript_ids_chr9`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `custom_transcript_ids_chr9` (
  `id` int(11) NOT NULL,
  `transcript_ids` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `transcript_idx` (`transcript_ids`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `custom_transcript_ids_chrMT`
--

DROP TABLE IF EXISTS `custom_transcript_ids_chrMT`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `custom_transcript_ids_chrMT` (
  `id` int(11) NOT NULL,
  `transcript_ids` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `transcript_idx` (`transcript_ids`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `custom_transcript_ids_chrX`
--

DROP TABLE IF EXISTS `custom_transcript_ids_chrX`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `custom_transcript_ids_chrX` (
  `id` int(11) NOT NULL,
  `transcript_ids` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `transcript_idx` (`transcript_ids`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `custom_transcript_ids_chrY`
--

DROP TABLE IF EXISTS `custom_transcript_ids_chrY`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `custom_transcript_ids_chrY` (
  `id` int(11) NOT NULL,
  `transcript_ids` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `transcript_idx` (`transcript_ids`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `effect_ranking`
--

DROP TABLE IF EXISTS `effect_ranking`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `effect_ranking` (
  `id` tinyint(3) unsigned NOT NULL AUTO_INCREMENT,
  `impact` enum('HIGH','MODERATE','LOW','MODIFIER') NOT NULL,
  `effect` varchar(49) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=TokuDB AUTO_INCREMENT=64 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `external_table_meta`
--

DROP TABLE IF EXISTS `external_table_meta`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `external_table_meta` (
  `id` int(5) NOT NULL AUTO_INCREMENT,
  `data_name` varchar(25) NOT NULL DEFAULT '',
  `version_number` varchar(25) NOT NULL DEFAULT '',
  `notes` text,
  `create_date` date NOT NULL,
  `update_date` date DEFAULT NULL,
  `updater_id` varchar(10) DEFAULT NULL,
  `data_source` varchar(200) NOT NULL,
  PRIMARY KEY (`data_name`,`version_number`,`create_date`,`data_source`),
  KEY `id` (`id`,`data_source`)
) ENGINE=TokuDB AUTO_INCREMENT=168 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `full_impact`
--

DROP TABLE IF EXISTS `full_impact`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `full_impact` (
  `input_impact` enum('HIGH','MODERATE','LOW','MODIFIER') NOT NULL,
  PRIMARY KEY (`input_impact`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hgnc`
--

DROP TABLE IF EXISTS `hgnc`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `hgnc` (
  `chrom` varchar(2) NOT NULL,
  `start` int(10) unsigned NOT NULL,
  `end` int(10) unsigned NOT NULL,
  `gene` varchar(50) NOT NULL,
  PRIMARY KEY (`gene`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `indel_chr1`
--

DROP TABLE IF EXISTS `indel_chr1`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `indel_chr1` (
  `variant_id` int(10) unsigned NOT NULL,
  `POS` int(10) unsigned NOT NULL,
  `REF` varchar(255) NOT NULL,
  `ALT` varchar(255) NOT NULL,
  `indel_length` smallint(6) NOT NULL,
  PRIMARY KEY (`variant_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `indel_chr10`
--

DROP TABLE IF EXISTS `indel_chr10`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `indel_chr10` (
  `variant_id` int(10) unsigned NOT NULL,
  `POS` int(10) unsigned NOT NULL,
  `REF` varchar(255) NOT NULL,
  `ALT` varchar(255) NOT NULL,
  `indel_length` smallint(6) NOT NULL,
  PRIMARY KEY (`variant_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `indel_chr11`
--

DROP TABLE IF EXISTS `indel_chr11`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `indel_chr11` (
  `variant_id` int(10) unsigned NOT NULL,
  `POS` int(10) unsigned NOT NULL,
  `REF` varchar(255) NOT NULL,
  `ALT` varchar(255) NOT NULL,
  `indel_length` smallint(6) NOT NULL,
  PRIMARY KEY (`variant_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `indel_chr12`
--

DROP TABLE IF EXISTS `indel_chr12`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `indel_chr12` (
  `variant_id` int(10) unsigned NOT NULL,
  `POS` int(10) unsigned NOT NULL,
  `REF` varchar(255) NOT NULL,
  `ALT` varchar(255) NOT NULL,
  `indel_length` smallint(6) NOT NULL,
  PRIMARY KEY (`variant_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `indel_chr13`
--

DROP TABLE IF EXISTS `indel_chr13`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `indel_chr13` (
  `variant_id` int(10) unsigned NOT NULL,
  `POS` int(10) unsigned NOT NULL,
  `REF` varchar(255) NOT NULL,
  `ALT` varchar(255) NOT NULL,
  `indel_length` smallint(6) NOT NULL,
  PRIMARY KEY (`variant_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `indel_chr14`
--

DROP TABLE IF EXISTS `indel_chr14`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `indel_chr14` (
  `variant_id` int(10) unsigned NOT NULL,
  `POS` int(10) unsigned NOT NULL,
  `REF` varchar(255) NOT NULL,
  `ALT` varchar(255) NOT NULL,
  `indel_length` smallint(6) NOT NULL,
  PRIMARY KEY (`variant_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `indel_chr15`
--

DROP TABLE IF EXISTS `indel_chr15`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `indel_chr15` (
  `variant_id` int(10) unsigned NOT NULL,
  `POS` int(10) unsigned NOT NULL,
  `REF` varchar(255) NOT NULL,
  `ALT` varchar(255) NOT NULL,
  `indel_length` smallint(6) NOT NULL,
  PRIMARY KEY (`variant_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `indel_chr16`
--

DROP TABLE IF EXISTS `indel_chr16`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `indel_chr16` (
  `variant_id` int(10) unsigned NOT NULL,
  `POS` int(10) unsigned NOT NULL,
  `REF` varchar(255) NOT NULL,
  `ALT` varchar(255) NOT NULL,
  `indel_length` smallint(6) NOT NULL,
  PRIMARY KEY (`variant_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `indel_chr17`
--

DROP TABLE IF EXISTS `indel_chr17`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `indel_chr17` (
  `variant_id` int(10) unsigned NOT NULL,
  `POS` int(10) unsigned NOT NULL,
  `REF` varchar(255) NOT NULL,
  `ALT` varchar(255) NOT NULL,
  `indel_length` smallint(6) NOT NULL,
  PRIMARY KEY (`variant_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `indel_chr18`
--

DROP TABLE IF EXISTS `indel_chr18`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `indel_chr18` (
  `variant_id` int(10) unsigned NOT NULL,
  `POS` int(10) unsigned NOT NULL,
  `REF` varchar(255) NOT NULL,
  `ALT` varchar(255) NOT NULL,
  `indel_length` smallint(6) NOT NULL,
  PRIMARY KEY (`variant_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `indel_chr19`
--

DROP TABLE IF EXISTS `indel_chr19`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `indel_chr19` (
  `variant_id` int(10) unsigned NOT NULL,
  `POS` int(10) unsigned NOT NULL,
  `REF` varchar(255) NOT NULL,
  `ALT` varchar(255) NOT NULL,
  `indel_length` smallint(6) NOT NULL,
  PRIMARY KEY (`variant_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `indel_chr2`
--

DROP TABLE IF EXISTS `indel_chr2`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `indel_chr2` (
  `variant_id` int(10) unsigned NOT NULL,
  `POS` int(10) unsigned NOT NULL,
  `REF` varchar(255) NOT NULL,
  `ALT` varchar(255) NOT NULL,
  `indel_length` smallint(6) NOT NULL,
  PRIMARY KEY (`variant_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `indel_chr20`
--

DROP TABLE IF EXISTS `indel_chr20`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `indel_chr20` (
  `variant_id` int(10) unsigned NOT NULL,
  `POS` int(10) unsigned NOT NULL,
  `REF` varchar(255) NOT NULL,
  `ALT` varchar(255) NOT NULL,
  `indel_length` smallint(6) NOT NULL,
  PRIMARY KEY (`variant_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `indel_chr21`
--

DROP TABLE IF EXISTS `indel_chr21`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `indel_chr21` (
  `variant_id` int(10) unsigned NOT NULL,
  `POS` int(10) unsigned NOT NULL,
  `REF` varchar(255) NOT NULL,
  `ALT` varchar(255) NOT NULL,
  `indel_length` smallint(6) NOT NULL,
  PRIMARY KEY (`variant_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `indel_chr22`
--

DROP TABLE IF EXISTS `indel_chr22`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `indel_chr22` (
  `variant_id` int(10) unsigned NOT NULL,
  `POS` int(10) unsigned NOT NULL,
  `REF` varchar(255) NOT NULL,
  `ALT` varchar(255) NOT NULL,
  `indel_length` smallint(6) NOT NULL,
  PRIMARY KEY (`variant_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `indel_chr3`
--

DROP TABLE IF EXISTS `indel_chr3`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `indel_chr3` (
  `variant_id` int(10) unsigned NOT NULL,
  `POS` int(10) unsigned NOT NULL,
  `REF` varchar(255) NOT NULL,
  `ALT` varchar(255) NOT NULL,
  `indel_length` smallint(6) NOT NULL,
  PRIMARY KEY (`variant_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `indel_chr4`
--

DROP TABLE IF EXISTS `indel_chr4`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `indel_chr4` (
  `variant_id` int(10) unsigned NOT NULL,
  `POS` int(10) unsigned NOT NULL,
  `REF` varchar(255) NOT NULL,
  `ALT` varchar(255) NOT NULL,
  `indel_length` smallint(6) NOT NULL,
  PRIMARY KEY (`variant_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `indel_chr5`
--

DROP TABLE IF EXISTS `indel_chr5`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `indel_chr5` (
  `variant_id` int(10) unsigned NOT NULL,
  `POS` int(10) unsigned NOT NULL,
  `REF` varchar(255) NOT NULL,
  `ALT` varchar(255) NOT NULL,
  `indel_length` smallint(6) NOT NULL,
  PRIMARY KEY (`variant_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `indel_chr6`
--

DROP TABLE IF EXISTS `indel_chr6`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `indel_chr6` (
  `variant_id` int(10) unsigned NOT NULL,
  `POS` int(10) unsigned NOT NULL,
  `REF` varchar(255) NOT NULL,
  `ALT` varchar(255) NOT NULL,
  `indel_length` smallint(6) NOT NULL,
  PRIMARY KEY (`variant_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `indel_chr7`
--

DROP TABLE IF EXISTS `indel_chr7`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `indel_chr7` (
  `variant_id` int(10) unsigned NOT NULL,
  `POS` int(10) unsigned NOT NULL,
  `REF` varchar(255) NOT NULL,
  `ALT` varchar(255) NOT NULL,
  `indel_length` smallint(6) NOT NULL,
  PRIMARY KEY (`variant_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `indel_chr8`
--

DROP TABLE IF EXISTS `indel_chr8`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `indel_chr8` (
  `variant_id` int(10) unsigned NOT NULL,
  `POS` int(10) unsigned NOT NULL,
  `REF` varchar(255) NOT NULL,
  `ALT` varchar(255) NOT NULL,
  `indel_length` smallint(6) NOT NULL,
  PRIMARY KEY (`variant_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `indel_chr9`
--

DROP TABLE IF EXISTS `indel_chr9`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `indel_chr9` (
  `variant_id` int(10) unsigned NOT NULL,
  `POS` int(10) unsigned NOT NULL,
  `REF` varchar(255) NOT NULL,
  `ALT` varchar(255) NOT NULL,
  `indel_length` smallint(6) NOT NULL,
  PRIMARY KEY (`variant_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `indel_chrMT`
--

DROP TABLE IF EXISTS `indel_chrMT`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `indel_chrMT` (
  `variant_id` int(10) unsigned NOT NULL,
  `POS` int(10) unsigned NOT NULL,
  `REF` varchar(255) NOT NULL,
  `ALT` varchar(255) NOT NULL,
  `indel_length` smallint(6) NOT NULL,
  PRIMARY KEY (`variant_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `indel_chrX`
--

DROP TABLE IF EXISTS `indel_chrX`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `indel_chrX` (
  `variant_id` int(10) unsigned NOT NULL,
  `POS` int(10) unsigned NOT NULL,
  `REF` varchar(255) NOT NULL,
  `ALT` varchar(255) NOT NULL,
  `indel_length` smallint(6) NOT NULL,
  PRIMARY KEY (`variant_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `indel_chrY`
--

DROP TABLE IF EXISTS `indel_chrY`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `indel_chrY` (
  `variant_id` int(10) unsigned NOT NULL,
  `POS` int(10) unsigned NOT NULL,
  `REF` varchar(255) NOT NULL,
  `ALT` varchar(255) NOT NULL,
  `indel_length` smallint(6) NOT NULL,
  PRIMARY KEY (`variant_id`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `loftee`
--

DROP TABLE IF EXISTS `loftee`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `loftee` (
  `chr` varchar(2) NOT NULL,
  `pos` int(10) unsigned NOT NULL,
  `ref` varchar(512) NOT NULL,
  `alt` varchar(512) NOT NULL,
  `is_hc_in_ccds` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`chr`,`pos`,`ref`,`alt`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `low_impact`
--

DROP TABLE IF EXISTS `low_impact`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `low_impact` (
  `input_impact` enum('HIGH','MODERATE','LOW','MODIFIER') NOT NULL,
  PRIMARY KEY (`input_impact`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `matched_indels`
--

DROP TABLE IF EXISTS `matched_indels`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `matched_indels` (
  `CHROM` varchar(2) NOT NULL,
  `variant_id` int(10) unsigned NOT NULL,
  `POS` int(10) unsigned NOT NULL,
  `REF` varchar(255) NOT NULL,
  `ALT` varchar(255) NOT NULL,
  `sample_id` mediumint(8) unsigned DEFAULT NULL,
  PRIMARY KEY (`CHROM`,`variant_id`,`POS`,`REF`,`ALT`),
  KEY `variant_idx` (`variant_id`),
  KEY `pos_idx` (`CHROM`,`POS`,`REF`,`ALT`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `multiallelic_variant_site`
--

DROP TABLE IF EXISTS `multiallelic_variant_site`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `multiallelic_variant_site` (
  `chr` varchar(2) NOT NULL,
  `pos` int(10) unsigned NOT NULL,
  PRIMARY KEY (`chr`,`pos`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `multiallelic_variant_site_2`
--

DROP TABLE IF EXISTS `multiallelic_variant_site_2`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `multiallelic_variant_site_2` (
  `chr` varchar(2) NOT NULL,
  `pos` int(10) unsigned NOT NULL,
  PRIMARY KEY (`chr`,`pos`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `rs_number`
--

DROP TABLE IF EXISTS `rs_number`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `rs_number` (
  `chrom` varchar(2) NOT NULL,
  `POS` int(10) unsigned NOT NULL,
  `rs_number` int(10) unsigned NOT NULL,
  PRIMARY KEY (`rs_number`)
) ENGINE=TokuDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sample`
--

DROP TABLE IF EXISTS `sample`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sample` (
  `sample_id` mediumint(8) unsigned NOT NULL AUTO_INCREMENT,
  `sample_name` varchar(45) NOT NULL,
  `sample_type` enum('genome','exome','custom_capture','merged','genome_as_fake_exome') NOT NULL,
  `capture_kit` varchar(255) DEFAULT NULL,
  `prep_id` mediumint(8) DEFAULT NULL,
  `priority` tinyint(4) NOT NULL DEFAULT '4',
  `sample_finished` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `sample_failure` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `experiment_id` int(11) NOT NULL DEFAULT '0',
  `analysis_id` int(11) NOT NULL DEFAULT '0',
  `seq_gender` enum('M','F','Ambiguous') DEFAULT NULL,
  `broad_phenotype` enum('amyotrophic lateral sclerosis','autoimmune disease','bone disease','brain malformation','cancer','cardiovascular disease','congenital disorder','control','control mild neuropsychiatric disease','covid-19','dementia','dermatological disease','diseases that affect the ear','endocrine disorder','epilepsy','febrile seizures','fetal ultrasound anomaly','gastrointestinal disease','healthy family member','hematological disease','infectious disease','intellectual disability','kidney and urological disease','liver disease','metabolic disease','neurodegenerative','nonhuman','obsessive compulsive disorder','ophthalmic disease','other','other neurodevelopmental disease','other neurological disease','other neuropsychiatric disease','primary immune deficiency','pulmonary disease','schizophrenia','sudden death','alzheimers disease','cerebral palsy') DEFAULT NULL,
  `ethnicity` enum('African','Caucasian','EastAsian','Hispanic','MiddleEastern','SouthAsian') DEFAULT NULL,
  `available_control_use` tinyint(1) unsigned DEFAULT '0',
  `s_update` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `s_insert` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`sample_id`),
  UNIQUE KEY `sample_idx` (`sample_name`,`sample_type`,`capture_kit`,`sample_finished`),
  KEY `initialization_idx` (`sample_finished`),
  KEY `prep_idx` (`prep_id`),
  KEY `sample_failure_idx` (`sample_failure`),
  KEY `sample_type_idx` (`sample_type`),
  KEY `sample_name_idx` (`sample_name`),
  KEY `broad_phenotype_idx` (`broad_phenotype`),
  KEY `ethnicity_idx` (`ethnicity`),
  KEY `available_control_use_idx` (`available_control_use`)
) ENGINE=TokuDB AUTO_INCREMENT=137962 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `variant_chr1`
--

DROP TABLE IF EXISTS `variant_chr1`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `variant_chr1` (
  `variant_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `POS` int(10) unsigned NOT NULL,
  `REF` varchar(255) NOT NULL,
  `ALT` varchar(255) NOT NULL,
  `rs_number` int(10) unsigned DEFAULT NULL,
  `transcript_stable_id` int(11) NOT NULL DEFAULT '0',
  `effect_id` tinyint(3) unsigned NOT NULL,
  `HGVS_c` varchar(255) DEFAULT NULL,
  `HGVS_p` varchar(255) DEFAULT NULL,
  `polyphen_humdiv` smallint(5) unsigned DEFAULT NULL,
  `polyphen_humvar` smallint(5) unsigned DEFAULT NULL,
  `gene` varchar(128) DEFAULT NULL,
  `indel_length` smallint(6) NOT NULL DEFAULT '0',
  `has_high_quality_call` tinyint(4) NOT NULL,
  PRIMARY KEY (`POS`,`variant_id`,`effect_id`,`transcript_stable_id`),
  KEY `variant_idx` (`variant_id`),
  KEY `gene_idx` (`gene`),
  KEY `effect_idx` (`effect_id`),
  KEY `variant_exists_idx` (`POS`,`indel_length`,`REF`,`ALT`,`variant_id`,`effect_id`,`has_high_quality_call`),
  KEY `has_high_quality_call_idx` (`has_high_quality_call`),
  KEY `variant_gene_idx` (`variant_id`,`gene`)
) ENGINE=TokuDB AUTO_INCREMENT=23278776 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `variant_chr10`
--

DROP TABLE IF EXISTS `variant_chr10`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `variant_chr10` (
  `variant_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `POS` int(10) unsigned NOT NULL,
  `REF` varchar(255) NOT NULL,
  `ALT` varchar(255) NOT NULL,
  `rs_number` int(10) unsigned DEFAULT NULL,
  `transcript_stable_id` int(11) NOT NULL DEFAULT '0',
  `effect_id` tinyint(3) unsigned NOT NULL,
  `HGVS_c` varchar(255) DEFAULT NULL,
  `HGVS_p` varchar(255) DEFAULT NULL,
  `polyphen_humdiv` smallint(5) unsigned DEFAULT NULL,
  `polyphen_humvar` smallint(5) unsigned DEFAULT NULL,
  `gene` varchar(128) DEFAULT NULL,
  `indel_length` smallint(6) NOT NULL DEFAULT '0',
  `has_high_quality_call` tinyint(4) NOT NULL,
  PRIMARY KEY (`POS`,`variant_id`,`effect_id`,`transcript_stable_id`),
  KEY `variant_idx` (`variant_id`),
  KEY `gene_idx` (`gene`),
  KEY `effect_idx` (`effect_id`),
  KEY `variant_exists_idx` (`POS`,`indel_length`,`REF`,`ALT`,`variant_id`,`effect_id`,`has_high_quality_call`),
  KEY `has_high_quality_call_idx` (`has_high_quality_call`)
) ENGINE=TokuDB AUTO_INCREMENT=12441273 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `variant_chr11`
--

DROP TABLE IF EXISTS `variant_chr11`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `variant_chr11` (
  `variant_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `POS` int(10) unsigned NOT NULL,
  `REF` varchar(255) NOT NULL,
  `ALT` varchar(255) NOT NULL,
  `rs_number` int(10) unsigned DEFAULT NULL,
  `transcript_stable_id` int(11) NOT NULL DEFAULT '0',
  `effect_id` tinyint(3) unsigned NOT NULL,
  `HGVS_c` varchar(255) DEFAULT NULL,
  `HGVS_p` varchar(255) DEFAULT NULL,
  `polyphen_humdiv` smallint(5) unsigned DEFAULT NULL,
  `polyphen_humvar` smallint(5) unsigned DEFAULT NULL,
  `gene` varchar(128) DEFAULT NULL,
  `indel_length` smallint(6) NOT NULL DEFAULT '0',
  `has_high_quality_call` tinyint(4) NOT NULL,
  PRIMARY KEY (`POS`,`variant_id`,`effect_id`,`transcript_stable_id`),
  KEY `variant_idx` (`variant_id`),
  KEY `gene_idx` (`gene`),
  KEY `effect_idx` (`effect_id`),
  KEY `variant_exists_idx` (`POS`,`indel_length`,`REF`,`ALT`,`variant_id`,`effect_id`,`has_high_quality_call`),
  KEY `has_high_quality_call_idx` (`has_high_quality_call`)
) ENGINE=TokuDB AUTO_INCREMENT=13225585 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `variant_chr12`
--

DROP TABLE IF EXISTS `variant_chr12`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `variant_chr12` (
  `variant_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `POS` int(10) unsigned NOT NULL,
  `REF` varchar(255) NOT NULL,
  `ALT` varchar(255) NOT NULL,
  `rs_number` int(10) unsigned DEFAULT NULL,
  `transcript_stable_id` int(11) NOT NULL DEFAULT '0',
  `effect_id` tinyint(3) unsigned NOT NULL,
  `HGVS_c` varchar(255) DEFAULT NULL,
  `HGVS_p` varchar(255) DEFAULT NULL,
  `polyphen_humdiv` smallint(5) unsigned DEFAULT NULL,
  `polyphen_humvar` smallint(5) unsigned DEFAULT NULL,
  `gene` varchar(128) DEFAULT NULL,
  `indel_length` smallint(6) NOT NULL DEFAULT '0',
  `has_high_quality_call` tinyint(4) NOT NULL,
  PRIMARY KEY (`POS`,`variant_id`,`effect_id`,`transcript_stable_id`),
  KEY `variant_idx` (`variant_id`),
  KEY `gene_idx` (`gene`),
  KEY `effect_idx` (`effect_id`),
  KEY `variant_exists_idx` (`POS`,`indel_length`,`REF`,`ALT`,`variant_id`,`effect_id`,`has_high_quality_call`),
  KEY `has_high_quality_call_idx` (`has_high_quality_call`)
) ENGINE=TokuDB AUTO_INCREMENT=12929919 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `variant_chr13`
--

DROP TABLE IF EXISTS `variant_chr13`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `variant_chr13` (
  `variant_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `POS` int(10) unsigned NOT NULL,
  `REF` varchar(255) NOT NULL,
  `ALT` varchar(255) NOT NULL,
  `rs_number` int(10) unsigned DEFAULT NULL,
  `transcript_stable_id` int(11) NOT NULL DEFAULT '0',
  `effect_id` tinyint(3) unsigned NOT NULL,
  `HGVS_c` varchar(255) DEFAULT NULL,
  `HGVS_p` varchar(255) DEFAULT NULL,
  `polyphen_humdiv` smallint(5) unsigned DEFAULT NULL,
  `polyphen_humvar` smallint(5) unsigned DEFAULT NULL,
  `gene` varchar(128) DEFAULT NULL,
  `indel_length` smallint(6) NOT NULL DEFAULT '0',
  `has_high_quality_call` tinyint(4) NOT NULL,
  PRIMARY KEY (`POS`,`variant_id`,`effect_id`,`transcript_stable_id`),
  KEY `variant_idx` (`variant_id`),
  KEY `gene_idx` (`gene`),
  KEY `effect_idx` (`effect_id`),
  KEY `variant_exists_idx` (`POS`,`indel_length`,`REF`,`ALT`,`variant_id`,`effect_id`,`has_high_quality_call`),
  KEY `has_high_quality_call_idx` (`has_high_quality_call`)
) ENGINE=TokuDB AUTO_INCREMENT=7421633 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `variant_chr14`
--

DROP TABLE IF EXISTS `variant_chr14`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `variant_chr14` (
  `variant_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `POS` int(10) unsigned NOT NULL,
  `REF` varchar(255) NOT NULL,
  `ALT` varchar(255) NOT NULL,
  `rs_number` int(10) unsigned DEFAULT NULL,
  `transcript_stable_id` int(11) NOT NULL DEFAULT '0',
  `effect_id` tinyint(3) unsigned NOT NULL,
  `HGVS_c` varchar(255) DEFAULT NULL,
  `HGVS_p` varchar(255) DEFAULT NULL,
  `polyphen_humdiv` smallint(5) unsigned DEFAULT NULL,
  `polyphen_humvar` smallint(5) unsigned DEFAULT NULL,
  `gene` varchar(128) DEFAULT NULL,
  `indel_length` smallint(6) NOT NULL DEFAULT '0',
  `has_high_quality_call` tinyint(4) NOT NULL,
  PRIMARY KEY (`POS`,`variant_id`,`effect_id`,`transcript_stable_id`),
  KEY `variant_idx` (`variant_id`),
  KEY `gene_idx` (`gene`),
  KEY `effect_idx` (`effect_id`),
  KEY `variant_exists_idx` (`POS`,`indel_length`,`REF`,`ALT`,`variant_id`,`effect_id`,`has_high_quality_call`),
  KEY `has_high_quality_call_idx` (`has_high_quality_call`)
) ENGINE=TokuDB AUTO_INCREMENT=8511428 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `variant_chr15`
--

DROP TABLE IF EXISTS `variant_chr15`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `variant_chr15` (
  `variant_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `POS` int(10) unsigned NOT NULL,
  `REF` varchar(255) NOT NULL,
  `ALT` varchar(255) NOT NULL,
  `rs_number` int(10) unsigned DEFAULT NULL,
  `transcript_stable_id` int(11) NOT NULL DEFAULT '0',
  `effect_id` tinyint(3) unsigned NOT NULL,
  `HGVS_c` varchar(255) DEFAULT NULL,
  `HGVS_p` varchar(255) DEFAULT NULL,
  `polyphen_humdiv` smallint(5) unsigned DEFAULT NULL,
  `polyphen_humvar` smallint(5) unsigned DEFAULT NULL,
  `gene` varchar(128) DEFAULT NULL,
  `indel_length` smallint(6) NOT NULL DEFAULT '0',
  `has_high_quality_call` tinyint(4) NOT NULL,
  PRIMARY KEY (`POS`,`variant_id`,`effect_id`,`transcript_stable_id`),
  KEY `variant_idx` (`variant_id`),
  KEY `gene_idx` (`gene`),
  KEY `effect_idx` (`effect_id`),
  KEY `variant_exists_idx` (`POS`,`indel_length`,`REF`,`ALT`,`variant_id`,`effect_id`,`has_high_quality_call`),
  KEY `has_high_quality_call_idx` (`has_high_quality_call`)
) ENGINE=TokuDB AUTO_INCREMENT=8496563 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `variant_chr16`
--

DROP TABLE IF EXISTS `variant_chr16`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `variant_chr16` (
  `variant_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `POS` int(10) unsigned NOT NULL,
  `REF` varchar(255) NOT NULL,
  `ALT` varchar(255) NOT NULL,
  `rs_number` int(10) unsigned DEFAULT NULL,
  `transcript_stable_id` int(11) NOT NULL DEFAULT '0',
  `effect_id` tinyint(3) unsigned NOT NULL,
  `HGVS_c` varchar(255) DEFAULT NULL,
  `HGVS_p` varchar(255) DEFAULT NULL,
  `polyphen_humdiv` smallint(5) unsigned DEFAULT NULL,
  `polyphen_humvar` smallint(5) unsigned DEFAULT NULL,
  `gene` varchar(128) DEFAULT NULL,
  `indel_length` smallint(6) NOT NULL DEFAULT '0',
  `has_high_quality_call` tinyint(4) NOT NULL,
  PRIMARY KEY (`POS`,`variant_id`,`effect_id`,`transcript_stable_id`),
  KEY `variant_idx` (`variant_id`),
  KEY `gene_idx` (`gene`),
  KEY `effect_idx` (`effect_id`),
  KEY `variant_exists_idx` (`POS`,`indel_length`,`REF`,`ALT`,`variant_id`,`effect_id`,`has_high_quality_call`),
  KEY `has_high_quality_call_idx` (`has_high_quality_call`)
) ENGINE=TokuDB AUTO_INCREMENT=9911865 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `variant_chr17`
--

DROP TABLE IF EXISTS `variant_chr17`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `variant_chr17` (
  `variant_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `POS` int(10) unsigned NOT NULL,
  `REF` varchar(255) NOT NULL,
  `ALT` varchar(255) NOT NULL,
  `rs_number` int(10) unsigned DEFAULT NULL,
  `transcript_stable_id` int(11) NOT NULL DEFAULT '0',
  `effect_id` tinyint(3) unsigned NOT NULL,
  `HGVS_c` varchar(255) DEFAULT NULL,
  `HGVS_p` varchar(255) DEFAULT NULL,
  `polyphen_humdiv` smallint(5) unsigned DEFAULT NULL,
  `polyphen_humvar` smallint(5) unsigned DEFAULT NULL,
  `gene` varchar(128) DEFAULT NULL,
  `indel_length` smallint(6) NOT NULL DEFAULT '0',
  `has_high_quality_call` tinyint(4) NOT NULL,
  PRIMARY KEY (`POS`,`variant_id`,`effect_id`,`transcript_stable_id`),
  KEY `variant_idx` (`variant_id`),
  KEY `gene_idx` (`gene`),
  KEY `effect_idx` (`effect_id`),
  KEY `variant_exists_idx` (`POS`,`indel_length`,`REF`,`ALT`,`variant_id`,`effect_id`,`has_high_quality_call`),
  KEY `has_high_quality_call_idx` (`has_high_quality_call`)
) ENGINE=TokuDB AUTO_INCREMENT=10674640 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `variant_chr18`
--

DROP TABLE IF EXISTS `variant_chr18`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `variant_chr18` (
  `variant_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `POS` int(10) unsigned NOT NULL,
  `REF` varchar(255) NOT NULL,
  `ALT` varchar(255) NOT NULL,
  `rs_number` int(10) unsigned DEFAULT NULL,
  `transcript_stable_id` int(11) NOT NULL DEFAULT '0',
  `effect_id` tinyint(3) unsigned NOT NULL,
  `HGVS_c` varchar(255) DEFAULT NULL,
  `HGVS_p` varchar(255) DEFAULT NULL,
  `polyphen_humdiv` smallint(5) unsigned DEFAULT NULL,
  `polyphen_humvar` smallint(5) unsigned DEFAULT NULL,
  `gene` varchar(128) DEFAULT NULL,
  `indel_length` smallint(6) NOT NULL DEFAULT '0',
  `has_high_quality_call` tinyint(4) NOT NULL,
  PRIMARY KEY (`POS`,`variant_id`,`effect_id`,`transcript_stable_id`),
  KEY `variant_idx` (`variant_id`),
  KEY `gene_idx` (`gene`),
  KEY `effect_idx` (`effect_id`),
  KEY `variant_exists_idx` (`POS`,`indel_length`,`REF`,`ALT`,`variant_id`,`effect_id`,`has_high_quality_call`),
  KEY `has_high_quality_call_idx` (`has_high_quality_call`)
) ENGINE=TokuDB AUTO_INCREMENT=6091347 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `variant_chr19`
--

DROP TABLE IF EXISTS `variant_chr19`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `variant_chr19` (
  `variant_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `POS` int(10) unsigned NOT NULL,
  `REF` varchar(255) NOT NULL,
  `ALT` varchar(255) NOT NULL,
  `rs_number` int(10) unsigned DEFAULT NULL,
  `transcript_stable_id` int(11) NOT NULL DEFAULT '0',
  `effect_id` tinyint(3) unsigned NOT NULL,
  `HGVS_c` varchar(255) DEFAULT NULL,
  `HGVS_p` varchar(255) DEFAULT NULL,
  `polyphen_humdiv` smallint(5) unsigned DEFAULT NULL,
  `polyphen_humvar` smallint(5) unsigned DEFAULT NULL,
  `gene` varchar(128) DEFAULT NULL,
  `indel_length` smallint(6) NOT NULL DEFAULT '0',
  `has_high_quality_call` tinyint(4) NOT NULL,
  PRIMARY KEY (`POS`,`variant_id`,`effect_id`,`transcript_stable_id`),
  KEY `variant_idx` (`variant_id`),
  KEY `gene_idx` (`gene`),
  KEY `effect_idx` (`effect_id`),
  KEY `variant_exists_idx` (`POS`,`indel_length`,`REF`,`ALT`,`variant_id`,`effect_id`,`has_high_quality_call`),
  KEY `has_high_quality_call_idx` (`has_high_quality_call`)
) ENGINE=TokuDB AUTO_INCREMENT=10081497 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `variant_chr2`
--

DROP TABLE IF EXISTS `variant_chr2`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `variant_chr2` (
  `variant_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `POS` int(10) unsigned NOT NULL,
  `REF` varchar(255) NOT NULL,
  `ALT` varchar(255) NOT NULL,
  `rs_number` int(10) unsigned DEFAULT NULL,
  `transcript_stable_id` int(11) NOT NULL DEFAULT '0',
  `effect_id` tinyint(3) unsigned NOT NULL,
  `HGVS_c` varchar(255) DEFAULT NULL,
  `HGVS_p` varchar(255) DEFAULT NULL,
  `polyphen_humdiv` smallint(5) unsigned DEFAULT NULL,
  `polyphen_humvar` smallint(5) unsigned DEFAULT NULL,
  `gene` varchar(128) DEFAULT NULL,
  `indel_length` smallint(6) NOT NULL DEFAULT '0',
  `has_high_quality_call` tinyint(4) NOT NULL,
  PRIMARY KEY (`POS`,`variant_id`,`effect_id`,`transcript_stable_id`),
  KEY `variant_idx` (`variant_id`),
  KEY `gene_idx` (`gene`),
  KEY `effect_idx` (`effect_id`),
  KEY `variant_exists_idx` (`POS`,`indel_length`,`REF`,`ALT`,`variant_id`,`effect_id`,`has_high_quality_call`),
  KEY `has_high_quality_call_idx` (`has_high_quality_call`)
) ENGINE=TokuDB AUTO_INCREMENT=21302993 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `variant_chr20`
--

DROP TABLE IF EXISTS `variant_chr20`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `variant_chr20` (
  `variant_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `POS` int(10) unsigned NOT NULL,
  `REF` varchar(255) NOT NULL,
  `ALT` varchar(255) NOT NULL,
  `rs_number` int(10) unsigned DEFAULT NULL,
  `transcript_stable_id` int(11) NOT NULL DEFAULT '0',
  `effect_id` tinyint(3) unsigned NOT NULL,
  `HGVS_c` varchar(255) DEFAULT NULL,
  `HGVS_p` varchar(255) DEFAULT NULL,
  `polyphen_humdiv` smallint(5) unsigned DEFAULT NULL,
  `polyphen_humvar` smallint(5) unsigned DEFAULT NULL,
  `gene` varchar(128) DEFAULT NULL,
  `indel_length` smallint(6) NOT NULL DEFAULT '0',
  `has_high_quality_call` tinyint(4) NOT NULL,
  PRIMARY KEY (`POS`,`variant_id`,`effect_id`,`transcript_stable_id`),
  KEY `variant_idx` (`variant_id`),
  KEY `gene_idx` (`gene`),
  KEY `effect_idx` (`effect_id`),
  KEY `variant_exists_idx` (`POS`,`indel_length`,`REF`,`ALT`,`variant_id`,`effect_id`,`has_high_quality_call`),
  KEY `has_high_quality_call_idx` (`has_high_quality_call`)
) ENGINE=TokuDB AUTO_INCREMENT=6477104 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `variant_chr21`
--

DROP TABLE IF EXISTS `variant_chr21`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `variant_chr21` (
  `variant_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `POS` int(10) unsigned NOT NULL,
  `REF` varchar(255) NOT NULL,
  `ALT` varchar(255) NOT NULL,
  `rs_number` int(10) unsigned DEFAULT NULL,
  `transcript_stable_id` int(11) NOT NULL DEFAULT '0',
  `effect_id` tinyint(3) unsigned NOT NULL,
  `HGVS_c` varchar(255) DEFAULT NULL,
  `HGVS_p` varchar(255) DEFAULT NULL,
  `polyphen_humdiv` smallint(5) unsigned DEFAULT NULL,
  `polyphen_humvar` smallint(5) unsigned DEFAULT NULL,
  `gene` varchar(128) DEFAULT NULL,
  `indel_length` smallint(6) NOT NULL DEFAULT '0',
  `has_high_quality_call` tinyint(4) NOT NULL,
  PRIMARY KEY (`POS`,`variant_id`,`effect_id`,`transcript_stable_id`),
  KEY `variant_idx` (`variant_id`),
  KEY `gene_idx` (`gene`),
  KEY `effect_idx` (`effect_id`),
  KEY `variant_exists_idx` (`POS`,`indel_length`,`REF`,`ALT`,`variant_id`,`effect_id`,`has_high_quality_call`),
  KEY `has_high_quality_call_idx` (`has_high_quality_call`)
) ENGINE=TokuDB AUTO_INCREMENT=3514236 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `variant_chr22`
--

DROP TABLE IF EXISTS `variant_chr22`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `variant_chr22` (
  `variant_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `POS` int(10) unsigned NOT NULL,
  `REF` varchar(255) NOT NULL,
  `ALT` varchar(255) NOT NULL,
  `rs_number` int(10) unsigned DEFAULT NULL,
  `transcript_stable_id` int(11) NOT NULL DEFAULT '0',
  `effect_id` tinyint(3) unsigned NOT NULL,
  `HGVS_c` varchar(255) DEFAULT NULL,
  `HGVS_p` varchar(255) DEFAULT NULL,
  `polyphen_humdiv` smallint(5) unsigned DEFAULT NULL,
  `polyphen_humvar` smallint(5) unsigned DEFAULT NULL,
  `gene` varchar(128) DEFAULT NULL,
  `indel_length` smallint(6) NOT NULL DEFAULT '0',
  `has_high_quality_call` tinyint(4) NOT NULL,
  PRIMARY KEY (`POS`,`variant_id`,`effect_id`,`transcript_stable_id`),
  KEY `variant_idx` (`variant_id`),
  KEY `gene_idx` (`gene`),
  KEY `effect_idx` (`effect_id`),
  KEY `variant_exists_idx` (`POS`,`indel_length`,`REF`,`ALT`,`variant_id`,`effect_id`,`has_high_quality_call`),
  KEY `has_high_quality_call_idx` (`has_high_quality_call`)
) ENGINE=TokuDB AUTO_INCREMENT=5014117 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `variant_chr3`
--

DROP TABLE IF EXISTS `variant_chr3`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `variant_chr3` (
  `variant_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `POS` int(10) unsigned NOT NULL,
  `REF` varchar(255) NOT NULL,
  `ALT` varchar(255) NOT NULL,
  `rs_number` int(10) unsigned DEFAULT NULL,
  `transcript_stable_id` int(11) NOT NULL DEFAULT '0',
  `effect_id` tinyint(3) unsigned NOT NULL,
  `HGVS_c` varchar(255) DEFAULT NULL,
  `HGVS_p` varchar(255) DEFAULT NULL,
  `polyphen_humdiv` smallint(5) unsigned DEFAULT NULL,
  `polyphen_humvar` smallint(5) unsigned DEFAULT NULL,
  `gene` varchar(128) DEFAULT NULL,
  `indel_length` smallint(6) NOT NULL DEFAULT '0',
  `has_high_quality_call` tinyint(4) NOT NULL,
  PRIMARY KEY (`POS`,`variant_id`,`effect_id`,`transcript_stable_id`),
  KEY `variant_idx` (`variant_id`),
  KEY `gene_idx` (`gene`),
  KEY `effect_idx` (`effect_id`),
  KEY `variant_exists_idx` (`POS`,`indel_length`,`REF`,`ALT`,`variant_id`,`effect_id`,`has_high_quality_call`),
  KEY `has_high_quality_call_idx` (`has_high_quality_call`)
) ENGINE=TokuDB AUTO_INCREMENT=16867077 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `variant_chr4`
--

DROP TABLE IF EXISTS `variant_chr4`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `variant_chr4` (
  `variant_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `POS` int(10) unsigned NOT NULL,
  `REF` varchar(255) NOT NULL,
  `ALT` varchar(255) NOT NULL,
  `rs_number` int(10) unsigned DEFAULT NULL,
  `transcript_stable_id` int(11) NOT NULL DEFAULT '0',
  `effect_id` tinyint(3) unsigned NOT NULL,
  `HGVS_c` varchar(255) DEFAULT NULL,
  `HGVS_p` varchar(255) DEFAULT NULL,
  `polyphen_humdiv` smallint(5) unsigned DEFAULT NULL,
  `polyphen_humvar` smallint(5) unsigned DEFAULT NULL,
  `gene` varchar(128) DEFAULT NULL,
  `indel_length` smallint(6) NOT NULL DEFAULT '0',
  `has_high_quality_call` tinyint(4) NOT NULL,
  PRIMARY KEY (`POS`,`variant_id`,`effect_id`,`transcript_stable_id`),
  KEY `variant_idx` (`variant_id`),
  KEY `gene_idx` (`gene`),
  KEY `effect_idx` (`effect_id`),
  KEY `variant_exists_idx` (`POS`,`indel_length`,`REF`,`ALT`,`variant_id`,`effect_id`,`has_high_quality_call`),
  KEY `has_high_quality_call_idx` (`has_high_quality_call`)
) ENGINE=TokuDB AUTO_INCREMENT=14831880 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `variant_chr5`
--

DROP TABLE IF EXISTS `variant_chr5`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `variant_chr5` (
  `variant_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `POS` int(10) unsigned NOT NULL,
  `REF` varchar(255) NOT NULL,
  `ALT` varchar(255) NOT NULL,
  `rs_number` int(10) unsigned DEFAULT NULL,
  `transcript_stable_id` int(11) NOT NULL DEFAULT '0',
  `effect_id` tinyint(3) unsigned NOT NULL,
  `HGVS_c` varchar(255) DEFAULT NULL,
  `HGVS_p` varchar(255) DEFAULT NULL,
  `polyphen_humdiv` smallint(5) unsigned DEFAULT NULL,
  `polyphen_humvar` smallint(5) unsigned DEFAULT NULL,
  `gene` varchar(128) DEFAULT NULL,
  `indel_length` smallint(6) NOT NULL DEFAULT '0',
  `has_high_quality_call` tinyint(4) NOT NULL,
  PRIMARY KEY (`POS`,`variant_id`,`effect_id`,`transcript_stable_id`),
  KEY `variant_idx` (`variant_id`),
  KEY `gene_idx` (`gene`),
  KEY `effect_idx` (`effect_id`),
  KEY `variant_exists_idx` (`POS`,`indel_length`,`REF`,`ALT`,`variant_id`,`effect_id`,`has_high_quality_call`),
  KEY `has_high_quality_call_idx` (`has_high_quality_call`)
) ENGINE=TokuDB AUTO_INCREMENT=14679714 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `variant_chr6`
--

DROP TABLE IF EXISTS `variant_chr6`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `variant_chr6` (
  `variant_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `POS` int(10) unsigned NOT NULL,
  `REF` varchar(255) NOT NULL,
  `ALT` varchar(255) NOT NULL,
  `rs_number` int(10) unsigned DEFAULT NULL,
  `transcript_stable_id` int(11) NOT NULL DEFAULT '0',
  `effect_id` tinyint(3) unsigned NOT NULL,
  `HGVS_c` varchar(255) DEFAULT NULL,
  `HGVS_p` varchar(255) DEFAULT NULL,
  `polyphen_humdiv` smallint(5) unsigned DEFAULT NULL,
  `polyphen_humvar` smallint(5) unsigned DEFAULT NULL,
  `gene` varchar(128) DEFAULT NULL,
  `indel_length` smallint(6) NOT NULL DEFAULT '0',
  `has_high_quality_call` tinyint(4) NOT NULL,
  PRIMARY KEY (`POS`,`variant_id`,`effect_id`,`transcript_stable_id`),
  KEY `variant_idx` (`variant_id`),
  KEY `gene_idx` (`gene`),
  KEY `effect_idx` (`effect_id`),
  KEY `variant_exists_idx` (`POS`,`indel_length`,`REF`,`ALT`,`variant_id`,`effect_id`,`has_high_quality_call`),
  KEY `has_high_quality_call_idx` (`has_high_quality_call`)
) ENGINE=TokuDB AUTO_INCREMENT=14631319 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `variant_chr7`
--

DROP TABLE IF EXISTS `variant_chr7`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `variant_chr7` (
  `variant_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `POS` int(10) unsigned NOT NULL,
  `REF` varchar(255) NOT NULL,
  `ALT` varchar(255) NOT NULL,
  `rs_number` int(10) unsigned DEFAULT NULL,
  `transcript_stable_id` int(11) NOT NULL DEFAULT '0',
  `effect_id` tinyint(3) unsigned NOT NULL,
  `HGVS_c` varchar(255) DEFAULT NULL,
  `HGVS_p` varchar(255) DEFAULT NULL,
  `polyphen_humdiv` smallint(5) unsigned DEFAULT NULL,
  `polyphen_humvar` smallint(5) unsigned DEFAULT NULL,
  `gene` varchar(128) DEFAULT NULL,
  `indel_length` smallint(6) NOT NULL DEFAULT '0',
  `has_high_quality_call` tinyint(4) NOT NULL,
  PRIMARY KEY (`POS`,`variant_id`,`effect_id`,`transcript_stable_id`),
  KEY `variant_idx` (`variant_id`),
  KEY `gene_idx` (`gene`),
  KEY `effect_idx` (`effect_id`),
  KEY `variant_exists_idx` (`POS`,`indel_length`,`REF`,`ALT`,`variant_id`,`effect_id`,`has_high_quality_call`),
  KEY `has_high_quality_call_idx` (`has_high_quality_call`)
) ENGINE=TokuDB AUTO_INCREMENT=14772688 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `variant_chr8`
--

DROP TABLE IF EXISTS `variant_chr8`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `variant_chr8` (
  `variant_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `POS` int(10) unsigned NOT NULL,
  `REF` varchar(255) NOT NULL,
  `ALT` varchar(255) NOT NULL,
  `rs_number` int(10) unsigned DEFAULT NULL,
  `transcript_stable_id` int(11) NOT NULL DEFAULT '0',
  `effect_id` tinyint(3) unsigned NOT NULL,
  `HGVS_c` varchar(255) DEFAULT NULL,
  `HGVS_p` varchar(255) DEFAULT NULL,
  `polyphen_humdiv` smallint(5) unsigned DEFAULT NULL,
  `polyphen_humvar` smallint(5) unsigned DEFAULT NULL,
  `gene` varchar(128) DEFAULT NULL,
  `indel_length` smallint(6) NOT NULL DEFAULT '0',
  `has_high_quality_call` tinyint(4) NOT NULL,
  PRIMARY KEY (`POS`,`variant_id`,`effect_id`,`transcript_stable_id`),
  KEY `variant_idx` (`variant_id`),
  KEY `gene_idx` (`gene`),
  KEY `effect_idx` (`effect_id`),
  KEY `variant_exists_idx` (`POS`,`indel_length`,`REF`,`ALT`,`variant_id`,`effect_id`,`has_high_quality_call`),
  KEY `has_high_quality_call_idx` (`has_high_quality_call`)
) ENGINE=TokuDB AUTO_INCREMENT=12383188 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `variant_chr9`
--

DROP TABLE IF EXISTS `variant_chr9`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `variant_chr9` (
  `variant_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `POS` int(10) unsigned NOT NULL,
  `REF` varchar(255) NOT NULL,
  `ALT` varchar(255) NOT NULL,
  `rs_number` int(10) unsigned DEFAULT NULL,
  `transcript_stable_id` int(11) NOT NULL DEFAULT '0',
  `effect_id` tinyint(3) unsigned NOT NULL,
  `HGVS_c` varchar(255) DEFAULT NULL,
  `HGVS_p` varchar(255) DEFAULT NULL,
  `polyphen_humdiv` smallint(5) unsigned DEFAULT NULL,
  `polyphen_humvar` smallint(5) unsigned DEFAULT NULL,
  `gene` varchar(128) DEFAULT NULL,
  `indel_length` smallint(6) NOT NULL DEFAULT '0',
  `has_high_quality_call` tinyint(4) NOT NULL,
  PRIMARY KEY (`POS`,`variant_id`,`effect_id`,`transcript_stable_id`),
  KEY `variant_idx` (`variant_id`),
  KEY `gene_idx` (`gene`),
  KEY `effect_idx` (`effect_id`),
  KEY `variant_exists_idx` (`POS`,`indel_length`,`REF`,`ALT`,`variant_id`,`effect_id`,`has_high_quality_call`),
  KEY `has_high_quality_call_idx` (`has_high_quality_call`)
) ENGINE=TokuDB AUTO_INCREMENT=11224151 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `variant_chrMT`
--

DROP TABLE IF EXISTS `variant_chrMT`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `variant_chrMT` (
  `variant_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `POS` int(10) unsigned NOT NULL,
  `REF` varchar(255) NOT NULL,
  `ALT` varchar(255) NOT NULL,
  `rs_number` int(10) unsigned DEFAULT NULL,
  `transcript_stable_id` int(11) NOT NULL DEFAULT '0',
  `effect_id` tinyint(3) unsigned NOT NULL,
  `HGVS_c` varchar(255) DEFAULT NULL,
  `HGVS_p` varchar(255) DEFAULT NULL,
  `polyphen_humdiv` smallint(5) unsigned DEFAULT NULL,
  `polyphen_humvar` smallint(5) unsigned DEFAULT NULL,
  `gene` varchar(128) DEFAULT NULL,
  `indel_length` smallint(6) NOT NULL DEFAULT '0',
  `has_high_quality_call` tinyint(4) NOT NULL,
  PRIMARY KEY (`POS`,`variant_id`,`effect_id`,`transcript_stable_id`),
  KEY `variant_idx` (`variant_id`),
  KEY `gene_idx` (`gene`),
  KEY `effect_idx` (`effect_id`),
  KEY `variant_exists_idx` (`POS`,`indel_length`,`REF`,`ALT`,`variant_id`,`effect_id`,`has_high_quality_call`),
  KEY `has_high_quality_call_idx` (`has_high_quality_call`),
  KEY `variant_gene_idx` (`variant_id`,`gene`)
) ENGINE=TokuDB AUTO_INCREMENT=16617 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `variant_chrX`
--

DROP TABLE IF EXISTS `variant_chrX`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `variant_chrX` (
  `variant_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `POS` int(10) unsigned NOT NULL,
  `REF` varchar(255) NOT NULL,
  `ALT` varchar(255) NOT NULL,
  `rs_number` int(10) unsigned DEFAULT NULL,
  `transcript_stable_id` int(11) NOT NULL DEFAULT '0',
  `effect_id` tinyint(3) unsigned NOT NULL,
  `HGVS_c` varchar(255) DEFAULT NULL,
  `HGVS_p` varchar(255) DEFAULT NULL,
  `polyphen_humdiv` smallint(5) unsigned DEFAULT NULL,
  `polyphen_humvar` smallint(5) unsigned DEFAULT NULL,
  `gene` varchar(128) DEFAULT NULL,
  `indel_length` smallint(6) NOT NULL DEFAULT '0',
  `has_high_quality_call` tinyint(4) NOT NULL,
  PRIMARY KEY (`POS`,`variant_id`,`effect_id`,`transcript_stable_id`),
  KEY `variant_idx` (`variant_id`),
  KEY `gene_idx` (`gene`),
  KEY `effect_idx` (`effect_id`),
  KEY `variant_exists_idx` (`POS`,`indel_length`,`REF`,`ALT`,`variant_id`,`effect_id`,`has_high_quality_call`),
  KEY `has_high_quality_call_idx` (`has_high_quality_call`)
) ENGINE=TokuDB AUTO_INCREMENT=9544269 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `variant_chrY`
--

DROP TABLE IF EXISTS `variant_chrY`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `variant_chrY` (
  `variant_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `POS` int(10) unsigned NOT NULL,
  `REF` varchar(255) NOT NULL,
  `ALT` varchar(255) NOT NULL,
  `rs_number` int(10) unsigned DEFAULT NULL,
  `transcript_stable_id` int(11) NOT NULL DEFAULT '0',
  `effect_id` tinyint(3) unsigned NOT NULL,
  `HGVS_c` varchar(255) DEFAULT NULL,
  `HGVS_p` varchar(255) DEFAULT NULL,
  `polyphen_humdiv` smallint(5) unsigned DEFAULT NULL,
  `polyphen_humvar` smallint(5) unsigned DEFAULT NULL,
  `gene` varchar(128) DEFAULT NULL,
  `indel_length` smallint(6) NOT NULL DEFAULT '0',
  `has_high_quality_call` tinyint(4) NOT NULL,
  PRIMARY KEY (`POS`,`variant_id`,`effect_id`,`transcript_stable_id`),
  KEY `variant_idx` (`variant_id`),
  KEY `gene_idx` (`gene`),
  KEY `effect_idx` (`effect_id`),
  KEY `variant_exists_idx` (`POS`,`indel_length`,`REF`,`ALT`,`variant_id`,`effect_id`,`has_high_quality_call`),
  KEY `has_high_quality_call_idx` (`has_high_quality_call`)
) ENGINE=TokuDB AUTO_INCREMENT=417146 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2020-06-25 10:48:15
