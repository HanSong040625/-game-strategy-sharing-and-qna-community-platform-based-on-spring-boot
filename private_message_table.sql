-- 私聊消息表
-- 用于存储用户之间的私聊消息

CREATE TABLE `private_message` (
  `id` bigint(20) NOT NULL COMMENT '消息唯一ID',
  `sender_id` bigint(20) NOT NULL COMMENT '发送者用户ID',
  `receiver_id` bigint(20) NOT NULL COMMENT '接收者用户ID',
  `content` text COLLATE utf8_unicode_ci NOT NULL COMMENT '消息内容',
  `message_type` varchar(50) COLLATE utf8_unicode_ci NOT NULL DEFAULT 'TEXT' COMMENT '消息类型：TEXT-文本，IMAGE-图片，FILE-文件',
  `send_time` datetime NOT NULL COMMENT '发送时间',
  
  PRIMARY KEY (`id`),
  KEY `idx_sender_id` (`sender_id`),
  KEY `idx_receiver_id` (`receiver_id`),
  KEY `idx_send_time` (`send_time`),
  KEY `idx_conversation` (`sender_id`, `receiver_id`),
  CONSTRAINT `fk_private_message_sender` FOREIGN KEY (`sender_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_private_message_receiver` FOREIGN KEY (`receiver_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='私聊消息表';

-- 为私聊消息表添加序列记录
INSERT INTO `sequence_table` (`seq_name`, `seq_count`) VALUES ('private_message_seq', 0);

-- 私聊消息表索引说明：
-- idx_sender_id: 按发送者查询消息
-- idx_receiver_id: 按接收者查询消息
-- idx_send_time: 按发送时间排序和查询
-- idx_conversation: 查询两个用户之间的对话

-- 表字段说明：
-- id: 主键，使用序列生成器自动生成
-- sender_id: 发送者ID，关联用户表
-- receiver_id: 接收者ID，关联用户表
-- content: 消息内容，支持文本消息
-- message_type: 消息类型，支持文本、图片、文件
-- send_time: 发送时间，用于排序和显示