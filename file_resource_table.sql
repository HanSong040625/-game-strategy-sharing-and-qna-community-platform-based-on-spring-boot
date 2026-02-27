-- 文件资源信息表
-- 用于存储每个上传文件的信息，包括存储路径和上传者

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
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已删除（0-未删除，1-已删除）',
  `delete_time` datetime DEFAULT NULL COMMENT '删除时间',
  `deleted_by` bigint(20) DEFAULT NULL COMMENT '删除者用户ID',
  `description` text COLLATE utf8_unicode_ci COMMENT '文件描述',
  `category` varchar(100) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '文件分类',
  `tags` varchar(500) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '文件标签',
  `download_count` int(11) NOT NULL DEFAULT '0' COMMENT '下载次数',
  `view_count` int(11) NOT NULL DEFAULT '0' COMMENT '查看次数',
  PRIMARY KEY (`id`),
  KEY `idx_uploader_id` (`uploader_id`),
  KEY `idx_upload_time` (`upload_time`),
  KEY `idx_file_type` (`file_type`),
  KEY `idx_category` (`category`),
  KEY `idx_is_deleted` (`is_deleted`),
  CONSTRAINT `fk_file_resource_uploader` FOREIGN KEY (`uploader_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='文件资源信息表';

-- 为文件资源表添加序列记录
INSERT INTO `sequence_table` (`seq_name`, `seq_count`) VALUES ('file_resource_seq', 0);

-- 文件资源表索引说明：
-- idx_uploader_id: 按上传者查询文件
-- idx_upload_time: 按上传时间排序和查询
-- idx_file_type: 按文件类型查询
-- idx_category: 按文件分类查询
-- idx_is_deleted: 查询已删除/未删除文件

-- 表字段说明：
-- id: 主键，使用序列生成器自动生成
-- original_name: 用户上传时的原始文件名
-- file_name: 服务器存储的文件名（UUID格式，避免重名冲突）
-- file_path: 文件在服务器上的物理存储路径
-- file_url: 文件的前端访问URL
-- file_size: 文件大小，用于显示和限制
-- file_type: 文件扩展名，用于分类和图标显示
-- uploader_id: 上传者ID，关联用户表
-- uploader_name: 上传者用户名，便于显示
-- upload_time: 上传时间，用于排序和统计
-- is_deleted: 软删除标记，支持文件恢复
-- delete_time: 删除时间，用于审计
-- deleted_by: 删除者，用于权限控制
-- description: 文件描述，用户可填写
-- category: 文件分类，便于管理
-- tags: 文件标签，支持搜索
-- download_count: 下载次数统计
-- view_count: 查看次数统计