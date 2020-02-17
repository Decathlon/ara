DROP DATABASE IF EXISTS `ara-dev`;
CREATE DATABASE `ara-dev`;

USE `ara-dev`;

-- MySQL dump 10.14  Distrib 5.5.56-MariaDB, for debian-linux-gnu (x86_64)
--
-- ------------------------------------------------------
-- Server version	5.5.56-MariaDB-1~wheezy

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
-- Table structure for table `DATABASECHANGELOG`
--

DROP TABLE IF EXISTS `DATABASECHANGELOG`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DATABASECHANGELOG` (
  `ID` varchar(255) NOT NULL,
  `AUTHOR` varchar(255) NOT NULL,
  `FILENAME` varchar(255) NOT NULL,
  `DATEEXECUTED` datetime NOT NULL,
  `ORDEREXECUTED` int(11) NOT NULL,
  `EXECTYPE` varchar(10) NOT NULL,
  `MD5SUM` varchar(35) DEFAULT NULL,
  `DESCRIPTION` varchar(255) DEFAULT NULL,
  `COMMENTS` varchar(255) DEFAULT NULL,
  `TAG` varchar(255) DEFAULT NULL,
  `LIQUIBASE` varchar(20) DEFAULT NULL,
  `CONTEXTS` varchar(255) DEFAULT NULL,
  `LABELS` varchar(255) DEFAULT NULL,
  `DEPLOYMENT_ID` varchar(10) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `DATABASECHANGELOGLOCK`
--

DROP TABLE IF EXISTS `DATABASECHANGELOGLOCK`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DATABASECHANGELOGLOCK` (
  `ID` int(11) NOT NULL,
  `LOCKED` bit(1) NOT NULL,
  `LOCKGRANTED` datetime DEFAULT NULL,
  `LOCKEDBY` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `communication`
--

DROP TABLE IF EXISTS `communication`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `communication` (
  `code` varchar(32) COLLATE utf8_unicode_ci NOT NULL,
  `name` varchar(64) COLLATE utf8_unicode_ci NOT NULL,
  `type` varchar(4) COLLATE utf8_unicode_ci NOT NULL,
  `message` longtext COLLATE utf8_unicode_ci,
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `project_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_communication_projectid_code` (`project_id`,`code`),
  UNIQUE KEY `unique_communication_projectid_name` (`project_id`,`name`),
  CONSTRAINT `fk_communication_projectid` FOREIGN KEY (`project_id`) REFERENCES `project` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `country`
--

DROP TABLE IF EXISTS `country`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `country` (
  `code` varchar(2) COLLATE utf8_unicode_ci NOT NULL,
  `name` varchar(50) COLLATE utf8_unicode_ci NOT NULL,
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `project_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_country_projectid_code` (`project_id`,`code`),
  UNIQUE KEY `unique_country_projectid_name` (`project_id`,`name`),
  CONSTRAINT `fk_country_projectid` FOREIGN KEY (`project_id`) REFERENCES `project` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `country_deployment`
--

DROP TABLE IF EXISTS `country_deployment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `country_deployment` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `execution_id` bigint(20) DEFAULT NULL,
  `platform` varchar(32) COLLATE utf8_unicode_ci NOT NULL,
  `job_url` varchar(256) COLLATE utf8_unicode_ci DEFAULT NULL,
  `status` varchar(16) COLLATE utf8_unicode_ci NOT NULL,
  `result` varchar(16) COLLATE utf8_unicode_ci DEFAULT NULL,
  `start_date_time` datetime DEFAULT NULL,
  `estimated_duration` bigint(20) DEFAULT NULL,
  `duration` bigint(20) DEFAULT NULL,
  `country_id` bigint(20) NOT NULL,
  `job_link` varchar(256) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `job_url` (`job_url`),
  UNIQUE KEY `unique_country_deployment` (`execution_id`,`country_id`),
  UNIQUE KEY `job_link` (`job_link`),
  KEY `fk_countrydeployment_countryid` (`country_id`),
  KEY `index_country_deployment_execution_id` (`execution_id`),
  CONSTRAINT `fk_countrydeployment_countryid` FOREIGN KEY (`country_id`) REFERENCES `country` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_countrydeployment_executionid` FOREIGN KEY (`execution_id`) REFERENCES `execution` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=129 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `cycle_definition`
--

DROP TABLE IF EXISTS `cycle_definition`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cycle_definition` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(16) COLLATE utf8_unicode_ci NOT NULL,
  `branch` varchar(16) COLLATE utf8_unicode_ci NOT NULL,
  `branch_position` int(11) NOT NULL,
  `project_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_cycle_definition_projectid_name_branch` (`project_id`,`name`,`branch`),
  CONSTRAINT `fk_cycledefinition_projectid` FOREIGN KEY (`project_id`) REFERENCES `project` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `error`
--

DROP TABLE IF EXISTS `error`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `error` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `step` varchar(2048) COLLATE utf8_unicode_ci NOT NULL,
  `step_definition` varchar(2048) COLLATE utf8_unicode_ci NOT NULL,
  `exception` longtext COLLATE utf8_unicode_ci NOT NULL,
  `step_line` int(11) NOT NULL,
  `executed_scenario_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_error` (`executed_scenario_id`,`step_line`),
  KEY `index_error_exception` (`exception`(512)),
  KEY `index_error_step` (`step`(1024)),
  KEY `index_error_step_definition` (`step_definition`(1024)),
  CONSTRAINT `fk_error_executedscenarioid` FOREIGN KEY (`executed_scenario_id`) REFERENCES `executed_scenario` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=587 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `executed_scenario`
--

DROP TABLE IF EXISTS `executed_scenario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `executed_scenario` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `run_id` bigint(20) NOT NULL,
  `feature_file` varchar(256) COLLATE utf8_unicode_ci NOT NULL,
  `feature_name` varchar(256) COLLATE utf8_unicode_ci NOT NULL,
  `feature_tags` varchar(256) COLLATE utf8_unicode_ci DEFAULT NULL,
  `tags` varchar(256) COLLATE utf8_unicode_ci DEFAULT NULL,
  `severity` varchar(32) COLLATE utf8_unicode_ci NOT NULL,
  `name` varchar(512) COLLATE utf8_unicode_ci NOT NULL,
  `line` int(11) NOT NULL,
  `cucumber_id` varchar(640) COLLATE utf8_unicode_ci NOT NULL,
  `content` longtext COLLATE utf8_unicode_ci NOT NULL,
  `start_date_time` datetime DEFAULT NULL,
  `screenshot_url` varchar(512) COLLATE utf8_unicode_ci DEFAULT NULL,
  `video_url` varchar(512) COLLATE utf8_unicode_ci DEFAULT NULL,
  `logs_url` varchar(512) COLLATE utf8_unicode_ci DEFAULT NULL,
  `http_requests_url` varchar(512) COLLATE utf8_unicode_ci DEFAULT NULL,
  `java_script_errors_url` varchar(512) COLLATE utf8_unicode_ci DEFAULT NULL,
  `diff_report_url` varchar(512) COLLATE utf8_unicode_ci DEFAULT NULL,
  `cucumber_report_url` varchar(512) COLLATE utf8_unicode_ci DEFAULT NULL,
  `api_server` varchar(16) COLLATE utf8_unicode_ci DEFAULT NULL,
  `selenium_node` varchar(128) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_executed_scenario` (`run_id`,`feature_file`,`name`,`line`),
  KEY `index_executed_scenario_feature_file` (`feature_file`),
  KEY `index_executed_scenario_feature_name` (`feature_name`),
  KEY `index_executed_scenario_name` (`name`),
  KEY `index_executed_scenario_cucumber_id` (`cucumber_id`),
  KEY `index_executed_scenario_run_id` (`run_id`),
  CONSTRAINT `fk_executedscenario_runid` FOREIGN KEY (`run_id`) REFERENCES `run` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3533 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `execution`
--

DROP TABLE IF EXISTS `execution`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `execution` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(16) COLLATE utf8_unicode_ci NOT NULL,
  `branch` varchar(16) COLLATE utf8_unicode_ci NOT NULL,
  `release` varchar(32) COLLATE utf8_unicode_ci DEFAULT NULL,
  `version` varchar(64) COLLATE utf8_unicode_ci DEFAULT NULL,
  `build_date_time` datetime DEFAULT NULL,
  `test_date_time` datetime NOT NULL,
  `job_url` varchar(256) COLLATE utf8_unicode_ci NOT NULL,
  `status` varchar(16) COLLATE utf8_unicode_ci DEFAULT NULL,
  `discard_reason` varchar(512) COLLATE utf8_unicode_ci DEFAULT NULL,
  `result` varchar(16) COLLATE utf8_unicode_ci DEFAULT NULL,
  `acceptance` varchar(16) COLLATE utf8_unicode_ci NOT NULL,
  `cycle_definition_id` bigint(20) NOT NULL,
  `blocking_validation` bit(1) DEFAULT NULL,
  `quality_thresholds` varchar(256) COLLATE utf8_unicode_ci DEFAULT NULL,
  `duration` bigint(20) DEFAULT NULL,
  `estimated_duration` bigint(20) DEFAULT NULL,
  `quality_status` varchar(10) COLLATE utf8_unicode_ci DEFAULT NULL,
  `quality_severities` varchar(4096) COLLATE utf8_unicode_ci DEFAULT NULL,
  `job_link` varchar(256) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `job_url` (`job_url`),
  UNIQUE KEY `unique_execution` (`job_url`),
  UNIQUE KEY `unique_execution_bk` (`cycle_definition_id`,`test_date_time`),
  UNIQUE KEY `job_link` (`job_link`),
  KEY `index_cycle_run_cycle_definition_id` (`cycle_definition_id`),
  CONSTRAINT `fk_execution_cycledefinitionid` FOREIGN KEY (`cycle_definition_id`) REFERENCES `cycle_definition` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=81 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `execution_completion_request`
--

DROP TABLE IF EXISTS `execution_completion_request`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `execution_completion_request` (
  `job_url` varchar(256) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`job_url`),
  UNIQUE KEY `unique_execution_completion_request` (`job_url`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `functionality`
--

DROP TABLE IF EXISTS `functionality`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `functionality` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `parent_id` bigint(20) DEFAULT NULL,
  `order` double NOT NULL,
  `type` varchar(13) COLLATE utf8_unicode_ci NOT NULL,
  `name` varchar(512) COLLATE utf8_unicode_ci NOT NULL,
  `country_codes` varchar(128) COLLATE utf8_unicode_ci DEFAULT NULL,
  `team_id` bigint(20) DEFAULT NULL,
  `severity` varchar(32) COLLATE utf8_unicode_ci DEFAULT NULL,
  `created` varchar(10) COLLATE utf8_unicode_ci DEFAULT NULL,
  `started` bit(1) DEFAULT NULL,
  `covered_scenarios` int(11) DEFAULT NULL,
  `ignored_scenarios` int(11) DEFAULT NULL,
  `comment` longtext COLLATE utf8_unicode_ci,
  `covered_country_scenarios` varchar(512) COLLATE utf8_unicode_ci DEFAULT NULL,
  `ignored_country_scenarios` varchar(512) COLLATE utf8_unicode_ci DEFAULT NULL,
  `not_automatable` bit(1) DEFAULT NULL,
  `project_id` bigint(20) NOT NULL,
  `creation_date_time` datetime NOT NULL,
  `update_date_time` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_functionality_projectparentorder` (`project_id`,`parent_id`,`order`),
  UNIQUE KEY `unique_functionality_projectparentname` (`project_id`,`parent_id`,`name`),
  KEY `fk_functionality_teamid` (`team_id`),
  KEY `index_parent_id` (`parent_id`),
  CONSTRAINT `fk_functionality_parentid` FOREIGN KEY (`parent_id`) REFERENCES `functionality` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_functionality_projectid` FOREIGN KEY (`project_id`) REFERENCES `project` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_functionality_teamid` FOREIGN KEY (`team_id`) REFERENCES `team` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=177 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `functionality_coverage`
--

DROP TABLE IF EXISTS `functionality_coverage`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `functionality_coverage` (
  `functionality_id` bigint(20) NOT NULL,
  `scenario_id` bigint(20) NOT NULL,
  PRIMARY KEY (`functionality_id`,`scenario_id`),
  KEY `fk_functionalitycoverage_scenarioid` (`scenario_id`),
  CONSTRAINT `fk_functionalitycoverage_functionalityid` FOREIGN KEY (`functionality_id`) REFERENCES `functionality` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_functionalitycoverage_scenarioid` FOREIGN KEY (`scenario_id`) REFERENCES `scenario` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `problem`
--

DROP TABLE IF EXISTS `problem`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `problem` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(256) COLLATE utf8_unicode_ci NOT NULL,
  `comment` longtext COLLATE utf8_unicode_ci,
  `status` varchar(21) COLLATE utf8_unicode_ci NOT NULL,
  `blamed_team_id` bigint(20) DEFAULT NULL,
  `defect_id` varchar(32) COLLATE utf8_unicode_ci DEFAULT NULL,
  `root_cause_id` bigint(20) DEFAULT NULL,
  `creation_date_time` datetime NOT NULL,
  `defect_existence` varchar(11) COLLATE utf8_unicode_ci DEFAULT NULL,
  `closing_date_time` datetime DEFAULT NULL,
  `project_id` bigint(20) NOT NULL,
  `first_seen_date_time` datetime DEFAULT NULL,
  `last_seen_date_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_problem_projectid_name` (`project_id`,`name`),
  UNIQUE KEY `unique_problem_projectid_defectid` (`project_id`,`defect_id`),
  KEY `fk_problem_blamedteamid` (`blamed_team_id`),
  KEY `fk_problem_rootcauseid` (`root_cause_id`),
  CONSTRAINT `fk_problem_blamedteamid` FOREIGN KEY (`blamed_team_id`) REFERENCES `team` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_problem_rootcauseid` FOREIGN KEY (`root_cause_id`) REFERENCES `root_cause` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `problem_occurrence`
--

DROP TABLE IF EXISTS `problem_occurrence`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `problem_occurrence` (
  `error_id` bigint(20) NOT NULL,
  `problem_pattern_id` bigint(20) NOT NULL,
  PRIMARY KEY (`error_id`,`problem_pattern_id`),
  KEY `fk_problemoccurrence_problempatternid` (`problem_pattern_id`),
  CONSTRAINT `fk_problemoccurrence_errorid` FOREIGN KEY (`error_id`) REFERENCES `error` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_problemoccurrence_problempatternid` FOREIGN KEY (`problem_pattern_id`) REFERENCES `problem_pattern` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `problem_pattern`
--

DROP TABLE IF EXISTS `problem_pattern`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `problem_pattern` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `problem_id` bigint(20) NOT NULL,
  `feature_file` varchar(256) COLLATE utf8_unicode_ci DEFAULT NULL,
  `feature_name` varchar(256) COLLATE utf8_unicode_ci DEFAULT NULL,
  `scenario_name` varchar(512) COLLATE utf8_unicode_ci DEFAULT NULL,
  `step` varchar(2048) COLLATE utf8_unicode_ci DEFAULT NULL,
  `step_definition` varchar(2048) COLLATE utf8_unicode_ci DEFAULT NULL,
  `exception` longtext COLLATE utf8_unicode_ci,
  `release` varchar(32) COLLATE utf8_unicode_ci DEFAULT NULL,
  `type_is_browser` bit(1) DEFAULT NULL,
  `type_is_mobile` bit(1) DEFAULT NULL,
  `platform` varchar(32) COLLATE utf8_unicode_ci DEFAULT NULL,
  `type_id` bigint(20) DEFAULT NULL,
  `country_id` bigint(20) DEFAULT NULL,
  `scenario_name_starts_with` bit(1) NOT NULL,
  `step_starts_with` bit(1) NOT NULL,
  `step_definition_starts_with` bit(1) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_problempattern_problemid` (`problem_id`),
  KEY `fk_problempattern_typeid` (`type_id`),
  KEY `fk_problempattern_countryid` (`country_id`),
  CONSTRAINT `fk_problempattern_countryid` FOREIGN KEY (`country_id`) REFERENCES `country` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_problempattern_problemid` FOREIGN KEY (`problem_id`) REFERENCES `problem` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_problempattern_typeid` FOREIGN KEY (`type_id`) REFERENCES `type` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `project`
--

DROP TABLE IF EXISTS `project`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `project` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(32) COLLATE utf8_unicode_ci NOT NULL,
  `name` varchar(64) COLLATE utf8_unicode_ci NOT NULL,
  `default_at_startup` bit(1) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `root_cause`
--

DROP TABLE IF EXISTS `root_cause`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `root_cause` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(128) COLLATE utf8_unicode_ci NOT NULL,
  `project_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_root_cause_projectid_name` (`project_id`,`name`),
  CONSTRAINT `fk_rootcause_projectid` FOREIGN KEY (`project_id`) REFERENCES `project` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=33 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `run`
--

DROP TABLE IF EXISTS `run`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `run` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `execution_id` bigint(20) DEFAULT NULL,
  `platform` varchar(32) COLLATE utf8_unicode_ci NOT NULL,
  `job_url` varchar(256) COLLATE utf8_unicode_ci DEFAULT NULL,
  `status` varchar(16) COLLATE utf8_unicode_ci DEFAULT NULL,
  `country_tags` varchar(32) COLLATE utf8_unicode_ci DEFAULT NULL,
  `severity_tags` varchar(64) COLLATE utf8_unicode_ci DEFAULT NULL,
  `include_in_thresholds` bit(1) DEFAULT NULL,
  `start_date_time` datetime DEFAULT NULL,
  `estimated_duration` bigint(20) DEFAULT NULL,
  `duration` bigint(20) DEFAULT NULL,
  `type_id` bigint(20) NOT NULL,
  `country_id` bigint(20) NOT NULL,
  `job_link` varchar(256) COLLATE utf8_unicode_ci DEFAULT NULL,
  `comment` varchar(256) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `job_url` (`job_url`),
  UNIQUE KEY `unique_run` (`execution_id`,`country_id`,`type_id`),
  UNIQUE KEY `job_link` (`job_link`),
  KEY `fk_run_typeid` (`type_id`),
  KEY `fk_run_countryid` (`country_id`),
  KEY `index_run_execution_id` (`execution_id`),
  CONSTRAINT `fk_run_countryid` FOREIGN KEY (`country_id`) REFERENCES `country` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_run_executionid` FOREIGN KEY (`execution_id`) REFERENCES `execution` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_run_typeid` FOREIGN KEY (`type_id`) REFERENCES `type` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=353 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `scenario`
--

DROP TABLE IF EXISTS `scenario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `scenario` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `feature_file` varchar(256) COLLATE utf8_unicode_ci NOT NULL,
  `feature_name` varchar(256) COLLATE utf8_unicode_ci NOT NULL,
  `feature_tags` varchar(256) COLLATE utf8_unicode_ci DEFAULT NULL,
  `tags` varchar(256) COLLATE utf8_unicode_ci DEFAULT NULL,
  `ignored` bit(1) NOT NULL,
  `country_codes` varchar(128) COLLATE utf8_unicode_ci DEFAULT NULL,
  `severity` varchar(32) COLLATE utf8_unicode_ci DEFAULT NULL,
  `name` varchar(512) COLLATE utf8_unicode_ci NOT NULL,
  `line` int(11) NOT NULL,
  `content` longtext COLLATE utf8_unicode_ci NOT NULL,
  `wrong_functionality_ids` varchar(256) COLLATE utf8_unicode_ci DEFAULT NULL,
  `wrong_country_codes` varchar(128) COLLATE utf8_unicode_ci DEFAULT NULL,
  `wrong_severity_code` varchar(32) COLLATE utf8_unicode_ci DEFAULT NULL,
  `source_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_scenario` (`source_id`,`feature_file`,`name`,`line`),
  KEY `index_scenario_source_id` (`source_id`),
  CONSTRAINT `fk_scenario_sourceid` FOREIGN KEY (`source_id`) REFERENCES `source` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=161 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `setting`
--

DROP TABLE IF EXISTS `setting`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `setting` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `project_id` bigint(20) NOT NULL,
  `code` varchar(64) COLLATE utf8_unicode_ci NOT NULL,
  `value` varchar(512) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_setting` (`project_id`,`code`),
  CONSTRAINT `fk_setting_projectid` FOREIGN KEY (`project_id`) REFERENCES `project` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `severity`
--

DROP TABLE IF EXISTS `severity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `severity` (
  `code` varchar(32) COLLATE utf8_unicode_ci NOT NULL,
  `position` int(11) NOT NULL,
  `name` varchar(32) COLLATE utf8_unicode_ci NOT NULL,
  `short_name` varchar(16) COLLATE utf8_unicode_ci NOT NULL,
  `default_on_missing` bit(1) NOT NULL,
  `initials` varchar(8) COLLATE utf8_unicode_ci NOT NULL,
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `project_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_severity_projectid_code` (`project_id`,`code`),
  UNIQUE KEY `unique_severity_projectid_name` (`project_id`,`name`),
  UNIQUE KEY `unique_severity_projectid_position` (`project_id`,`position`),
  UNIQUE KEY `unique_severity_projectid_shortname` (`project_id`,`short_name`),
  UNIQUE KEY `unique_severity_projectid_initials` (`project_id`,`initials`),
  CONSTRAINT `fk_severity_projectid` FOREIGN KEY (`project_id`) REFERENCES `project` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `source`
--

DROP TABLE IF EXISTS `source`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `source` (
  `code` varchar(16) COLLATE utf8_unicode_ci NOT NULL,
  `name` varchar(32) COLLATE utf8_unicode_ci NOT NULL,
  `letter` char(1) COLLATE utf8_unicode_ci NOT NULL,
  `technology` varchar(16) COLLATE utf8_unicode_ci NOT NULL,
  `vcs_url` varchar(256) COLLATE utf8_unicode_ci NOT NULL,
  `default_branch` varchar(16) COLLATE utf8_unicode_ci NOT NULL,
  `postman_country_root_folders` bit(1) NOT NULL,
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `project_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_source_projectid_code` (`project_id`,`code`),
  UNIQUE KEY `unique_source_projectid_name` (`project_id`,`name`),
  UNIQUE KEY `unique_source_projectid_letter` (`project_id`,`letter`),
  CONSTRAINT `fk_source_projectid` FOREIGN KEY (`project_id`) REFERENCES `project` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `team`
--

DROP TABLE IF EXISTS `team`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `team` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(128) COLLATE utf8_unicode_ci NOT NULL,
  `assignable_to_problems` bit(1) NOT NULL DEFAULT b'1',
  `assignable_to_functionalities` bit(1) NOT NULL DEFAULT b'1',
  `project_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_team_projectid_name` (`project_id`,`name`),
  CONSTRAINT `fk_team_projectid` FOREIGN KEY (`project_id`) REFERENCES `project` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=41 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `type`
--

DROP TABLE IF EXISTS `type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `type` (
  `code` varchar(16) COLLATE utf8_unicode_ci NOT NULL DEFAULT '',
  `name` varchar(50) COLLATE utf8_unicode_ci NOT NULL,
  `is_browser` bit(1) NOT NULL,
  `is_mobile` bit(1) NOT NULL,
  `source_id` bigint(20) DEFAULT NULL,
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `project_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_type_projectid_code` (`project_id`,`code`),
  UNIQUE KEY `unique_type_projectid_name` (`project_id`,`name`),
  KEY `fk_type_sourceid` (`source_id`),
  CONSTRAINT `fk_type_projectid` FOREIGN KEY (`project_id`) REFERENCES `project` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_type_sourceid` FOREIGN KEY (`source_id`) REFERENCES `source` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;