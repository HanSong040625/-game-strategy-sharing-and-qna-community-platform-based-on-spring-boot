-- phpMyAdmin SQL Dump
-- version 4.8.5
-- https://www.phpmyadmin.net/
--
-- 主机： localhost
-- 生成日期： 2025-11-01 21:29:17
-- 服务器版本： 5.7.26
-- PHP 版本： 7.3.4

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET AUTOCOMMIT = 0;
START TRANSACTION;
SET time_zone = "+00:00";

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- 数据库： `game_community`
--

-- --------------------------------------------------------

--
-- 表的结构 `file_resource`
--

CREATE TABLE `file_resource` (
  `id` bigint(20) NOT NULL COMMENT '文件资源唯一ID',
  `original_name` varchar(500) COLLATE utf8_unicode_ci NOT NULL COMMENT '原始文件名',
  `file_name` varchar(255) COLLATE utf8_unicode_ci NOT NULL COMMENT '存储文件名（UUID格式）',
  `file_path` varchar(1000) COLLATE utf8_unicode_ci NOT NULL COMMENT '文件存储路径',
  `file_url` varchar(1000) COLLATE utf8_unicode_ci NOT NULL COMMENT '文件访问URL',
  `file_size` bigint(20) NOT NULL COMMENT '文件大小（字节）',
  `file_type` varchar(100) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '文件类型/扩展名',
  `uploader_id` bigint(20) NOT NULL COMMENT '上传者用户ID',
  `uploader_name` varchar(255) COLLATE utf8_unicode_ci NOT NULL COMMENT '上传者用户名',
  `upload_time` datetime NOT NULL COMMENT '上传时间',
  `description` text COLLATE utf8_unicode_ci COMMENT '文件描述',
  `category` varchar(100) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '文件分类',
  `tags` varchar(500) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '文件标签',
  `download_count` int(11) NOT NULL DEFAULT '0' COMMENT '下载次数',
  `view_count` int(11) NOT NULL DEFAULT '0' COMMENT '查看次数'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='文件资源信息表';

--
-- 转存表中的数据 `file_resource`
--

-- 暂无初始数据

--
-- 表的索引 `file_resource`
--
ALTER TABLE `file_resource`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_uploader_id` (`uploader_id`),
  ADD KEY `idx_upload_time` (`upload_time`),
  ADD KEY `idx_file_type` (`file_type`),
  ADD KEY `idx_category` (`category`);

--
-- 在导出的表使用AUTO_INCREMENT
--

--
-- 使用表AUTO_INCREMENT `file_resource`
--
ALTER TABLE `file_resource`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '文件资源唯一ID';

--
-- 限制导出的表
--

--
-- 限制表 `file_resource`
--
ALTER TABLE `file_resource`
  ADD CONSTRAINT `fk_file_resource_uploader` FOREIGN KEY (`uploader_id`) REFERENCES `user` (`id`) ON DELETE CASCADE;

--
-- 为文件资源表添加序列记录
--
INSERT INTO `sequence_table` (`seq_name`, `seq_count`) VALUES ('file_resource_seq', 0);

COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;