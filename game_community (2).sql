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
-- 表的结构 `admin_user`
--

CREATE TABLE `admin_user` (
  `id` bigint(20) NOT NULL COMMENT '管理员唯一id',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `email` varchar(255) COLLATE utf8_unicode_ci NOT NULL COMMENT '邮箱',
  `is_admin` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否为管理员',
  `password` varchar(255) COLLATE utf8_unicode_ci NOT NULL COMMENT '密码',
  `username` varchar(255) COLLATE utf8_unicode_ci NOT NULL COMMENT '管理员昵称'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='管理员信息表';

--
-- 转存表中的数据 `admin_user`
--

INSERT INTO `admin_user` (`id`, `create_time`, `email`, `is_admin`, `password`, `username`) VALUES
(1, '2025-09-13 14:27:24', 'admin2@example.com', 1, '$2a$10$Z6W.QhuvUA0AFBPJBUAk8Oh3J2/ToMciWWPFZRi54ccHyQmpmjXxS', 'admin2'),
(2, '2025-10-23 22:21:21', 'admin@example.com', 1, '$2a$10$8c0ymUyyUgHCWRts4Ve.sOiU1v0eujR1EvNurZ1L65UF8T3zk95de', 'admin'),
(4, NULL, '', 1, '$2a$10$CRjn8WNsS6XQaaVuquL5WeHh94vaCCc3sSCj5UhipPcjnwCPsaUZW', 'admin1');

-- --------------------------------------------------------

--
-- 表的结构 `answer`
--

CREATE TABLE `answer` (
  `id` bigint(20) NOT NULL,
  `content` text COLLATE utf8_unicode_ci COMMENT '回答的内容',
  `create_time` datetime DEFAULT NULL COMMENT '发布时间',
  `dislikes` int(11) DEFAULT NULL,
  `is_accepted` bit(1) DEFAULT NULL,
  `likes` int(11) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL COMMENT '发布用户的id',
  `question_id` bigint(20) DEFAULT NULL COMMENT '问题id'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='问题回答';

--
-- 转存表中的数据 `answer`
--

INSERT INTO `answer` (`id`, `content`, `create_time`, `dislikes`, `is_accepted`, `likes`, `update_time`, `user_id`, `question_id`) VALUES
(6, '不同地图需结合地形特性选武器：零号大坝推荐 M14 突击步枪，其高伤害 + 低后坐力适合中远距离压制，配件选红点瞄准镜 + 消音器 + 扩容弹匣，隐蔽性和续航兼顾；长弓溪谷山火区用 K437，沙暴枪口 + 长矛枪管 + 60 发弹匣能提升近战爆发和稳定性，应对火区灵活转移需求。通用技巧：加装镭射瞄准镜提升腰射精度，巷战可换短枪管增强机动性。', '2025-09-20 17:30:51', 0, b'1', 3, '2025-09-20 17:30:51', 12, 21),
(7, '123123123123123123', '2025-10-20 22:37:29', 1, b'0', 2, '2025-10-20 22:37:29', 20, 21),
(8, '来个固排猛猛得吃', '2025-10-29 11:36:26', 0, b'0', 0, '2025-10-29 11:36:26', 20, 21),
(9, '来个固排猛猛得吃', '2025-10-29 11:36:29', 0, b'0', 0, '2025-10-29 11:36:29', 20, 21),
(10, '测试回答：这个问题很好，我来回答一下！', '2025-10-29 12:08:34', 0, b'0', 0, '2025-10-29 12:08:34', 21, 21),
(11, '123123123', '2025-10-29 21:28:30', 0, b'0', 0, '2025-10-29 21:28:30', 12, 21),
(12, '412333', '2025-10-31 10:17:28', 3, b'0', 2, '2025-10-31 10:17:28', 20, 22);

-- --------------------------------------------------------

--
-- 表的结构 `comment`
--

CREATE TABLE `comment` (
  `id` bigint(20) NOT NULL COMMENT '评论信息id',
  `content` text COLLATE utf8_unicode_ci NOT NULL COMMENT '内容',
  `create_time` datetime DEFAULT NULL COMMENT '发布时间',
  `likes` int(11) DEFAULT '0' COMMENT '此评论点赞数',
  `user_id` bigint(20) NOT NULL COMMENT '发布用户的id',
  `guide_id` bigint(20) NOT NULL COMMENT '攻略id'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='攻略评论';

--
-- 转存表中的数据 `comment`
--

INSERT INTO `comment` (`id`, `content`, `create_time`, `likes`, `user_id`, `guide_id`) VALUES
(7, '测试6', '2025-10-21 20:28:05', 0, 12, 36),
(8, '测试7', '2025-10-21 20:29:31', 0, 12, 36),
(9, '测试8', '2025-10-21 20:34:59', 0, 12, 36),
(10, '测试9', '2025-10-21 20:42:13', 0, 12, 36),
(12, '测试消息', '2025-10-24 22:45:26', 0, 12, 41),
(13, '123123123123123', '2025-10-25 21:16:43', 0, 12, 41),
(14, '访问消息通知', '2025-10-25 22:48:16', 0, 12, 41),
(18, '测试消息通知', '2025-10-26 11:06:46', 0, 12, 41),
(19, '测试', '2025-10-26 11:15:40', 0, 12, 41),
(20, '测试', '2025-10-26 11:19:43', 0, 12, 41),
(21, '测试', '2025-10-26 11:20:37', 0, 12, 41),
(22, '测试', '2025-10-26 11:25:07', 0, 12, 41),
(23, '测试', '2025-10-26 11:38:36', 0, 12, 41),
(24, '测试', '2025-10-26 12:15:27', 0, 12, 41),
(25, '册数1', '2025-10-26 12:15:40', 0, 12, 41),
(26, '测试', '2025-10-28 10:04:28', 0, 12, 41),
(27, '测试', '2025-10-31 09:37:22', 0, 12, 41),
(28, '测试1031', '2025-10-31 09:37:31', 0, 12, 41),
(29, '1212', '2025-10-31 10:09:29', 0, 267, 41);

-- --------------------------------------------------------

--
-- 表的结构 `game`
--

CREATE TABLE `game` (
  `id` bigint(20) NOT NULL,
  `categories` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '标签',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `description` varchar(500) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '宣传语',
  `is_featured` bit(1) DEFAULT NULL COMMENT '是否推荐',
  `logo_url` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `name` varchar(100) COLLATE utf8_unicode_ci NOT NULL COMMENT '游戏名称',
  `poster_url` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `total_views` int(11) DEFAULT '0' COMMENT '总浏览量（攻略+问答）',
  `detailed_info` text COLLATE utf8_unicode_ci COMMENT '详细信息',
  `introduction` text COLLATE utf8_unicode_ci COMMENT '游戏简介'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='游戏列表';

--
-- 转存表中的数据 `game`
--

INSERT INTO `game` (`id`, `categories`, `create_time`, `description`, `is_featured`, `logo_url`, `name`, `poster_url`, `total_views`, `detailed_info`, `introduction`) VALUES
(5, '多人,射击', '2025-09-10 20:29:55', '新一代战术射击品质标杆', b'1', '/uploads/f53491e9-c1ca-4fa3-b0ec-ca1c05b868bd.jpg', '三角洲行动', '/uploads/be9a8e74-88b0-4522-b0f2-9c0d3130c9d8.jpg', 395, '游戏名称：三角洲行动\n上架平台：Android\n开发商：腾讯天美工作室琳琅团队\n安卓版本信息：1.201.37105.632025-09-17 16:16:51更新\n安卓文件大小：1489 MB', '《三角洲行动》是一款由琳琅天上团队研发运营的新一代战术射击品质标杆游戏，移动/PC双端畅玩，未来将登陆主机平台。\n交战搜索搞定就撤、24vs24的超大战场激爽对决、驾驶三栖重型载具火力输出、海量随机事件突发、电影级剧情战役复刻……三大玩法模式，将为你提供丰富又刺激的作战体验。\n快来加入这场惊险刺激的战斗吧！\n烽火地带模式，交战、搜索、搞定就撤！\n选好装备，射击，搜索情报与物资，利用战术打败对手，和队友带出物资。争夺并破译高价值物资——曼德尔砖，有机会获得稀有物品。\n全面战场模式，24vs24海陆空多栖激爽大战场！\n传承于《三角洲特种部队》经典模式，在超大地图体验多人作战。干员们，可驾驶攻击艇、主战坦克、黑鹰重型直升机、武装装甲车、重型全地形车等近10款载具，全面作战。\n黑鹰坠落-战役，经典再燃！（后续上线）\n高清还原电影级作战体验，还原摩加迪沙最经典的城镇、巷战、夜战、坠机之地等。\n“无所畏惧，绝不言退。”\n快来加入这场惊险刺激的战斗吧！'),
(6, '角色扮演,中世纪,开放世界', '2025-09-11 14:38:25', '打瓦吗？我说的是瓦兰迪亚', b'1', '/uploads/9c472396-bb06-493d-9d40-c2f3bdfe1d73.jpg', '骑马与砍杀2：霸主', '/uploads/88bde9c1-b1fa-4008-ae85-2a1cae19130d.jpg', 45, NULL, NULL),
(8, '射击,FPS', '2025-09-11 15:21:21', 'FPS届的黄埔军校.', b'1', '/uploads/2b080077-eb0a-4f7e-8d0b-e8c2f814e55e.jpg', '穿越火线', '/uploads/c1e6ff60-dea3-467e-a37d-04ef2d2ec2c8.jpg', 0, NULL, NULL),
(9, '角色扮演', '2025-10-21 21:50:16', '明日方舟同步ip续作', b'1', '/uploads/e916f726-ffd9-488b-b662-3ff53862d3f0.jpg', '明日方舟：终末地', '/uploads/b3d4c4e8-1e26-4538-894f-10c30774566e.jpg', 0, NULL, NULL),
(10, '动作,米哈游', '2025-10-21 21:58:59', '全新2.3版本「可曾记得梦」现已推出！', b'1', '/uploads/5af1a233-7e40-42d4-8b02-829f00bc8a2c.jpg', '绝区零', '/uploads/7fbb03e0-980b-4311-a134-b3997b0e9164.jpg', 0, NULL, NULL),
(11, '搜打撤', '2025-10-21 22:19:09', '在奇异的梦境中醒来后，你发现自己身处于鸭科夫的世界中。\n一旦离开基地，各种敌人藏匿在不同的地点。在看似平静但危机四伏的鸭鸭世界中，你需要小心谨慎的前进，探索搜集各种物资。\n从初始属性平平无奇，开局只有一把枪的打工鸭开始，收集各种各样奇妙的物品，打造藏身处，强化装备，努力在鸭科夫的世界生存下去——直到安全撤离。	', b'1', '/uploads/c7a2eb7b-cdad-45f0-a3be-57909fb3c936.jpg', '逃离鸭科夫', '/uploads/abd088db-4e48-4c13-a063-ab95919fb602.jpg', 0, NULL, NULL),
(12, '搜打撤', '2025-10-21 22:24:16', '撤的出，才是赢家！', b'1', '/uploads/4ce7f038-b9a1-46c9-8958-d9884ae71fe6.jpg', '暗区突围：无限', '/uploads/21d12e64-fb70-4835-b174-3a0b6ad2e5cd.jpg', 0, NULL, NULL),
(13, '卡牌', '2025-10-21 22:27:14', '必叫你大败而归！', b'1', '/uploads/4ff01748-e96f-472e-a433-03bac10d6654.jpg', '三国杀：一将成名', '/uploads/eb62901f-ac17-4aaf-b996-a2a5e04332e2.jpg', 0, NULL, NULL),
(14, '射击,战舰', '2025-10-21 22:48:59', '全球5000万玩家选择，快节奏5v5', b'1', '/uploads/818d0add-1371-4ed3-b8f9-ac954cdd4b91.jpg', '现代战舰', '/uploads/73136c5f-db6a-4244-89d6-ca69e6e3a8a9.jpg', 0, NULL, NULL),
(15, '111', '2025-10-31 10:11:25', '111111111111111111111111111111111111111111111111111111', b'1', '/assets/covers/785c88f3-dd3a-44e0-88a6-582a57217452.png', '11111111111111111111111111111111', '/assets/covers/05003954-2853-45e3-bc67-b47e37ae4999.png', 0, NULL, NULL),
(16, '123123123123123', '2025-10-31 20:52:54', '日志管理', b'1', '123123123', '日志管理', '123123123123', 0, NULL, '修改');

-- --------------------------------------------------------

--
-- 表的结构 `guide`
--

CREATE TABLE `guide` (
  `id` bigint(20) NOT NULL,
  `content` text COLLATE utf8_unicode_ci NOT NULL,
  `create_time` datetime DEFAULT NULL,
  `title` varchar(200) COLLATE utf8_unicode_ci NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `game_id` bigint(20) NOT NULL,
  `cover_image_url` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `likes` int(11) DEFAULT '0',
  `views` int(11) DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='攻略信息存储表';

--
-- 转存表中的数据 `guide`
--

INSERT INTO `guide` (`id`, `content`, `create_time`, `title`, `user_id`, `game_id`, `cover_image_url`, `likes`, `views`) VALUES
(36, '作为一款硬核战术射击游戏，《三角洲行动》融合了真实军事模拟与快节奏战术对抗。本攻略将从核心机制、战术策略、武器搭配到赛季玩法，全方位帮助新手快速上手并提升胜率。​\n一、核心机制与入门基础​\n兵种选择与定位​\n游戏分为突击、工程、支援、侦察四大兵种，3 人小队需合理搭配。突击兵（如 “疾风”）擅长正面突破，其钻墙电刺可穿透掩体缴械敌人，紧急回避装置能实现战场闪回自救；工程兵（如 “深蓝”）的防爆套装可大幅减伤，钩爪枪能快速转移队友或物资；支援兵 “蛊” 的无人机群能干扰敌方视野，高效治疗技能可缩短队友疗伤时间；侦察兵 “骇爪” 的信号破译器能扫描 60 米内敌人，为团队提供关键情报。​\n快速成长路径​\n每日任务是稳定经验来源，完成 “单局造成 1000 伤害”“击败 15 名敌人” 等目标可快速升级。武器解锁推荐 “摸金模式”，在高活跃度地图（如 “零号大坝”）击败敌人拾取未解锁武器，效率比常规升级快 3 倍。记得使用官方改枪码（如 M4A1 方案码：6EOUG3O09S7O691HHD7V5）一键应用最优配件组合。​\n二、地图战术与实战技巧​\n场景化战术指南​\n城市巷战（如 “断层” 地图）：优先选择冲锋枪或霰弹枪，利用建筑物交替掩护推进。队友分工建议：1 人突破（疾风）、1 人侧翼侦察（无名）、1 人殿后支援（蛊）。投掷突破型闪光弹配合旋刃飞行器的减速效果，可轻松清理房间内敌人。​\n沙漠开阔地：狙击手是核心战力，需搭配侦察兵提供视野。工程兵 “乌鲁鲁” 的速凝掩体可快速构建狙击阵地，支援兵的肾上腺素无人机能提升枪械稳定性。利用沙丘隐蔽移动，驾驶武装直升机进行大范围侦察。​\n丛林地图：植被茂密适合伏击，装备夜视仪和消音器。“无名” 的静默潜袭技能可缩小声音传播范围，配合刀片刺网手雷封锁路径，能有效拦截敌方小队。​\n团队协作关键​\n语音沟通需明确 “敌人位置 - 数量 - 装备” 三要素，如 “3 点钟方向 2 人，携带防爆盾”。遭遇战时遵循 “侦察 - 突破 - 支援” 流程：侦察兵标记目标，突击兵正面牵制，工程兵搭建掩体，支援兵持续治疗。携带曼德尔砖时需全员护送，因其会暴露位置，建议优先选择装甲车转移。​\n三、武器改装与资源管理​\n高效武器配置​\n突击步枪推荐 M4A1 搭配长枪管（提升射程）、消焰器（增强稳定性）和操控性握把，适合中远距离作战；冲锋枪 PBX45 侧重近战，选重塔握把提升容错率。通过 “猎鳄行动” 等限时活动可免费获取 SCAR-H 等强力武器，完成武器专属任务还能解锁龙息弹等特殊弹药。​\n资源优先级排序​\n物资收集遵循 “武器＞防弹衣＞医疗包＞配件” 原则。安全区收缩前 5 分钟需完成基础装备收集，优先搜索保险箱和标记物资点。载具使用需谨慎，直升机虽机动性强但易暴露，城市地图建议选择装甲车，丛林地图则用全地形车。​\n四、赛季内容适配（烈火冲天版本）​\n新地图 “长弓溪谷” 需注意森林山火动态，火势蔓延会造成持续伤害，需及时转移至岩石区域。新干员 “银翼” 的侦察技能可穿透烟雾探测敌人，配合 F-45A 战斗机的空袭能快速清理火区敌人。潮汐监狱副本的典狱长 BOSS 战建议搭配 “威龙” 的磁吸炸弹和 “深蓝” 的防爆盾，先摧毁其护盾再集火输出。​\n五、胜率提升 Checklist​\n组队时确保包含侦察兵和支援兵，提升信息获取与生存能力​\n武器配件优先满足稳定性，后坐力控制是新手最大难点​\n养成 “移动 - 观察 - 射击” 循环习惯，避免站桩输出​\n携带至少 2 颗破片手雷，巷战中可强制敌方暴露位置​\n撤离阶段提前 3 分钟占领高地，架设狙击枪掩护队友​\n通过持续练习武器控制与团队配合，熟悉各地图的战略点位，即使不依赖外挂也能成为战场精英。记住：战术意识与执行力，永远比硬件装备更重要。', '2025-09-20 17:28:01', '《三角洲行动》新手进阶全攻略：从生存到制霸战场', 20, 5, '', 2, 64),
(39, '在《三角洲行动》中，红色物资是提升装备、影响战局的关键。不少玩家都在寻找稳定获取红色物资的方法，以下整理了高效出红的核心技巧，帮助你在战术射击对战中更快积累优势。\n\n \n优先选择高适配兵种\n\n选对兵种是高效出红的基础。侦察兵和突击兵凭借高机动性与侦查能力，成为搜刮红色物资的最优解。\n\n这两类兵种移动速度快，能优先抵达地图关键区域，尤其适合单人玩家用独狼战术穿插盲区，抢先发现隐藏物资点。\n\n它们标配的烟雾弹、无人机等道具，可有效规避敌人视线，保障搜索过程的安全。\n\n\n《三角洲行动》怎么高效获取红色物资 《三角洲行动》高效获取红色物资攻略\n《三角洲行动》怎么高效获取红色物资 《三角洲行动》高效获取红色物资攻略\n\n科学搭配武器装备\n\n合理的武器与装备组合，能同时提升生存能力和搜索效率。\n\n主武器优先选突击步枪，其稳定性强、换弹快，适配中距离遭遇战;若地图视野开阔(如长弓溪谷)，可搭配狙击步枪进行远程警戒。\n\n物资携带需充足，包括弹药、医疗包和防爆装置，确保遭遇敌人时能持续作战;穿戴轻型护甲可进一步提升移速，加快物资点间的转移效率。\n\n\n《三角洲行动》怎么高效获取红色物资 《三角洲行动》高效获取红色物资攻略\n《三角洲行动》怎么高效获取红色物资 《三角洲行动》高效获取红色物资攻略\n\n掌握地图刷新规律\n\n熟悉红色物资的刷新机制，是提高获取概率的核心。\n\n红色物资并非完全随机，战略高地、地下掩体、仓库内部及地图边缘的隐蔽房间，都是高概率刷新区域，可通过实战标记常刷点位。\n\n关注游戏内动态提示，空投降落、占领点开启等特殊事件往往伴随高级物资，及时前往能大幅提升出红几率。\n\n\n《三角洲行动》怎么高效获取红色物资 《三角洲行动》高效获取红色物资攻略\n《三角洲行动》怎么高效获取红色物资 《三角洲行动》高效获取红色物资攻略\n\n积极完成任务挑战\n\n任务与挑战是稳定获取红色物资的重要补充途径。\n\n游戏内的解密彩蛋、密码门破解、特定击杀等任务，会引导玩家前往隐藏区域，完成后通常奖励高价值红色物资，避免盲目搜索浪费时间。\n\n参与限时挑战和团队目标，不仅能赚额外积分，还能解锁专属补给箱，其中包含稀有红色装备。', '2025-10-22 22:32:16', '《三角洲行动》怎么高效获取红色物资 《三角洲行动》高效获取红色物资攻略', 20, 5, '', 0, 7),
(40, '在《三角洲行动》里，破译密码门是获取奖励的关键环节，主要有 “声音破译” 和 “符号破译” 两种核心方式，具体操作如下：\n\n1. 声音破译(密码门 / 电脑)\n\n密码门或隐藏信息的电脑，不会直接给出可见提示，但会发出 “滴滴” 声。玩家需仔细分辨声音长短，短滴代表 “・”，长滴代表 “-”。\n\n记录完整的 “・”“-” 组合后，对照摩斯密码表翻译即可得到数字密码。比如 “三短两长” 对应数字 2，“三长两短” 对应数字 8。\n\n电脑隐藏信息的破译逻辑与密码门一致，只需根据屏幕提示，用同样的摩斯密码规则解读即可。\n\n\n《三角洲行动》密码门怎么破译 《三角洲行动》密码门破译攻略\n《三角洲行动》密码门怎么破译 《三角洲行动》密码门破译攻略\n\n\n《三角洲行动》密码门怎么破译 《三角洲行动》密码门破译攻略\n《三角洲行动》密码门怎么破译 《三角洲行动》密码门破译攻略\n2. 符号破译(符号密码门)\n\n这类密码门的线索会直接标注在墙壁上，以符号图案的形式呈现。\n\n玩家需先找到对应的 “密码卡片”，再将墙壁上的符号与卡片上的符号一一对应，匹配成功后就能确定数字密码，开启大门。\n\n无论哪种破译方式，成功开门后通常都能获得丰厚的游戏内奖励，建议探索时多留意细节线索。', '2025-10-22 22:32:57', '《三角洲行动》密码门怎么破译 《三角洲行动》密码门破译攻略', 20, 5, '', 1, 4),
(41, '三角洲行动“燎原计划”版本上线后，全新玩法“火域求生”和限定载具“雷霆战车”让账号交易热度再攀新高。但“封脸处罚”的严格执行，让不少玩家在“低价买号”和“安全保障”间犹豫不决。据第三方交易监测数据显示，2025年11月三角洲行动买号咨询量增长42%，其中超60%玩家担忧账号关联风险。其实，只要选对平台和方法，就能轻松实现“低价买号又省心”。今天就带来三角洲行动买号的安全省心攻略，详解7881游戏交易平台的特色保障。傻', '2025-10-22 22:37:55', '三角洲行动买号去哪买便宜？安全省心攻略 ', 20, 5, '', 2, 107),
(44, '23213123213213213213123123', '2025-10-29 21:32:47', '1231232132132132131', 12, 6, '', 0, 2),
(45, '13221321321312321312312', '2025-10-29 21:32:51', '123213123123213213213123123', 12, 6, '', 0, 1),
(46, '21321321321313213213123213213', '2025-10-29 21:33:02', '123213123213123测试修改1', 12, 6, '/assets/covers/badfadd2-0941-4024-969e-58b4445faa8a.png', 0, 40),
(52, '232132313123123123123', '2025-10-31 20:38:17', '123123132312313', 20, 11, '', 0, 0),
(55, '22313121231231312', '2025-10-31 21:14:15', '123123313', 20, 9, '', 0, 0),
(56, '1321321312313213', '2025-10-31 21:14:23', '1231231233', 20, 14, '', 0, 1);

-- --------------------------------------------------------

--
-- 表的结构 `guide_like`
--

CREATE TABLE `guide_like` (
  `id` bigint(20) NOT NULL,
  `create_time` datetime DEFAULT NULL,
  `guide_id` bigint(20) NOT NULL COMMENT '点赞的攻略id',
  `user_id` bigint(20) NOT NULL COMMENT '点赞者的id'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='点赞信息';

--
-- 转存表中的数据 `guide_like`
--

INSERT INTO `guide_like` (`id`, `create_time`, `guide_id`, `user_id`) VALUES
(3, '2025-09-21 15:54:22', 36, 20),
(4, '2025-10-20 22:47:22', 36, 12),
(5, '2025-10-22 22:33:06', 40, 20),
(6, '2025-10-28 10:04:21', 41, 12),
(7, '2025-10-31 10:09:09', 41, 267);

-- --------------------------------------------------------

--
-- 表的结构 `notification`
--

CREATE TABLE `notification` (
  `id` bigint(20) NOT NULL,
  `recipient_id` bigint(20) NOT NULL,
  `sender_id` bigint(20) NOT NULL,
  `content` text COLLATE utf8_unicode_ci NOT NULL,
  `type` varchar(50) COLLATE utf8_unicode_ci NOT NULL,
  `related_id` bigint(20) DEFAULT NULL,
  `is_read` tinyint(1) DEFAULT '0',
  `create_time` datetime NOT NULL,
  `sender_type` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='消息通知';

--
-- 转存表中的数据 `notification`
--

INSERT INTO `notification` (`id`, `recipient_id`, `sender_id`, `content`, `type`, `related_id`, `is_read`, `create_time`, `sender_type`) VALUES
(21, 18, 20, 'test1 评论了你的攻略《test3测试消息通知》：23123131233', 'comment', 15, 0, '2025-10-26 10:25:38', NULL),
(22, 18, 21, 'test4 评论了你的攻略《test3测试消息通知》：123213123', 'comment', 16, 0, '2025-10-26 10:33:08', NULL),
(23, 18, 21, 'test4 评论了你的攻略《test3测试消息通知》：1232133213', 'comment', 17, 0, '2025-10-26 10:33:40', NULL),
(36, 12, 1, '因违反社区规定，你的攻略《测试112123123123》已被删除', 'system', 42, 0, '2025-10-30 21:57:27', 'admin'),
(42, 12, 1, '因违反社区规定，你的攻略《123123213123213123》已被删除', 'system', 43, 0, '2025-10-31 20:28:50', 'admin'),
(34, 12, 4, '因违反社区规定，你的攻略《测试删除》已被删除', 'system', 44, 1, '2025-10-26 12:51:56', NULL),
(35, 20, 12, 'test2 评论了你的攻略《三角洲行动买号去哪买便宜？安全省心攻略 》：测试', 'comment', 26, 1, '2025-10-28 10:04:28', 'user'),
(37, 12, 1, '因违反社区规定，你的攻略《123213213》已被删除', 'system', 48, 0, '2025-10-30 21:57:33', 'admin'),
(38, 12, 12, '您已成功删除攻略《12321321312321》', 'system', 47, 0, '2025-10-31 08:45:41', 'user'),
(39, 20, 12, 'test2 评论了你的攻略《三角洲行动买号去哪买便宜？安全省心攻略 》：测试', 'comment', 27, 1, '2025-10-31 09:37:22', 'user'),
(40, 20, 12, 'test2 评论了你的攻略《三角洲行动买号去哪买便宜？安全省心攻略 》：测试1031', 'comment', 28, 1, '2025-10-31 09:37:31', 'user'),
(41, 20, 267, 'test7 评论了你的攻略《三角洲行动买号去哪买便宜？安全省心攻略 》：1212', 'comment', 29, 1, '2025-10-31 10:09:29', 'user'),
(43, 20, 1, '因违反社区规定，你的攻略《123123213》已被删除', 'system', 50, 0, '2025-10-31 20:33:06', 'admin'),
(44, 21, 1, '因违反社区规定，你的攻略《测试攻略封面》已被删除', 'system', 49, 0, '2025-10-31 20:36:00', 'admin'),
(45, 20, 1, '因违反社区规定，你的攻略《1231231231321321323》已被删除', 'system', 54, 0, '2025-10-31 20:39:26', 'admin'),
(46, 20, 1, '因违反社区规定，你的攻略《1232131313132131》已被删除', 'system', 53, 0, '2025-10-31 20:47:25', 'admin'),
(47, 20, 1, '因违反社区规定，你的攻略《测试删除11111》已被删除', 'system', 51, 0, '2025-10-31 20:49:12', 'admin');

-- --------------------------------------------------------

--
-- 表的结构 `operation_log`
--

CREATE TABLE `operation_log` (
  `id` bigint(20) NOT NULL,
  `admin_id` bigint(20) NOT NULL,
  `admin_name` varchar(50) COLLATE utf8_unicode_ci NOT NULL,
  `error_message` text COLLATE utf8_unicode_ci,
  `ip_address` varchar(45) COLLATE utf8_unicode_ci DEFAULT NULL,
  `is_success` bit(1) NOT NULL,
  `operation_details` text COLLATE utf8_unicode_ci,
  `operation_time` datetime NOT NULL,
  `operation_type` varchar(50) COLLATE utf8_unicode_ci NOT NULL,
  `target_id` bigint(20) DEFAULT NULL,
  `target_name` varchar(100) COLLATE utf8_unicode_ci DEFAULT NULL,
  `target_type` varchar(50) COLLATE utf8_unicode_ci NOT NULL,
  `user_agent` varchar(500) COLLATE utf8_unicode_ci DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- 转存表中的数据 `operation_log`
--

INSERT INTO `operation_log` (`id`, `admin_id`, `admin_name`, `error_message`, `ip_address`, `is_success`, `operation_details`, `operation_time`, `operation_type`, `target_id`, `target_name`, `target_type`, `user_agent`) VALUES
(1, 1, 'admin2', NULL, '0:0:0:0:0:0:0:1', b'1', '管理员登录系统', '2025-10-31 20:48:35', 'ADMIN_LOGIN', NULL, '管理员登录', 'SYSTEM', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 Edg/141.0.0.0'),
(2, 1, 'admin2', NULL, '0:0:0:0:0:0:0:1', b'1', '删除用户攻略：测试删除11111 (作者：test1)', '2025-10-31 20:49:12', 'DELETE_GUIDE', 51, '测试删除11111', 'GUIDE', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 Edg/141.0.0.0'),
(3, 4, 'admin1', NULL, '0:0:0:0:0:0:0:1', b'1', '管理员登录系统', '2025-10-31 20:50:50', 'ADMIN_LOGIN', NULL, '管理员登录', 'SYSTEM', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 Edg/141.0.0.0'),
(4, 4, 'admin1', NULL, '0:0:0:0:0:0:0:1', b'1', '添加新游戏：日志管理', '2025-10-31 20:52:54', 'ADD_GAME', 16, '日志管理', 'GAME', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 Edg/141.0.0.0'),
(5, 4, 'admin1', NULL, '0:0:0:0:0:0:0:1', b'1', '更新游戏信息：日志管理', '2025-10-31 20:53:45', 'UPDATE_GAME', 16, '日志管理', 'GAME', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 Edg/141.0.0.0'),
(6, 1, 'admin2', NULL, '0:0:0:0:0:0:0:1', b'1', '管理员登录系统', '2025-10-31 21:00:38', 'ADMIN_LOGIN', NULL, '管理员登录', 'SYSTEM', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 Edg/141.0.0.0'),
(7, 1, 'admin2', NULL, '0:0:0:0:0:0:0:1', b'1', '管理员登录系统', '2025-10-31 21:02:35', 'ADMIN_LOGIN', NULL, '管理员登录', 'SYSTEM', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) TraeCN/1.104.3 Chrome/138.0.7204.251 Electron/37.6.1 Safari/537.36');

-- --------------------------------------------------------

--
-- 表的结构 `question`
--

CREATE TABLE `question` (
  `id` bigint(20) NOT NULL,
  `content` text COLLATE utf8_unicode_ci,
  `create_time` datetime DEFAULT NULL,
  `dislikes` int(11) DEFAULT NULL,
  `likes` int(11) DEFAULT NULL,
  `title` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `view_count` int(11) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `game_id` bigint(20) DEFAULT NULL,
  `answer_count` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='问题列表';

--
-- 转存表中的数据 `question`
--

INSERT INTO `question` (`id`, `content`, `create_time`, `dislikes`, `likes`, `title`, `update_time`, `view_count`, `user_id`, `game_id`, `answer_count`) VALUES
(21, '比如零号大坝这种复杂地形和长弓溪谷这种有山火的场景，配件搭配有什么讲究吗？', '2025-09-20 17:30:01', 0, 0, '不同地图该怎么选武器', '2025-09-20 17:30:01', 217, 20, 5, 2),
(22, '测试提问问题123123123123123', '2025-10-29 21:32:17', 0, 0, '测试提问123123', '2025-10-29 21:32:17', 10, 12, 5, 1);

-- --------------------------------------------------------

--
-- 表的结构 `sequence_table`
--

CREATE TABLE `sequence_table` (
  `seq_name` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `seq_count` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- 转存表中的数据 `sequence_table`
--

INSERT INTO `sequence_table` (`seq_name`, `seq_count`) VALUES
('admin_seq', 1264),
('user_seq', 267);

-- --------------------------------------------------------

--
-- 表的结构 `user`
--

CREATE TABLE `user` (
  `id` bigint(20) NOT NULL COMMENT '用户唯一id',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登录用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '加密的登录密码',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户注册邮箱',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '用户注册时间',
  `is_admin` tinyint(1) NOT NULL DEFAULT '0',
  `last_login` datetime DEFAULT NULL,
  `is_online` int(11) NOT NULL DEFAULT '0',
  `avatar_url` varchar(500) COLLATE utf8_unicode_ci DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='用户信息表';

--
-- 转存表中的数据 `user`
--

INSERT INTO `user` (`id`, `username`, `password`, `email`, `create_time`, `is_admin`, `last_login`, `is_online`, `avatar_url`) VALUES
(12, 'test2', '$2a$10$hMPqmn/qxkPeV2zq8thwtOc1uBzRNXmQXZkFrtw3DVTI8YZrlIV.q', 'test2@qq.com', '2025-09-20 17:30:37', 0, '2025-10-28 12:21:12', 0, '/assets/avatars/test2_1761836137860.png'),
(18, 'test3', '$2a$10$h6zIaLj.QvzKCztw8uh1r.u7VBpMOaK6y3y4KbeXed1vCOr1vYU4O', 'test3@qq.com', '2025-10-22 17:53:29', 0, '2025-10-28 12:21:12', 1, NULL),
(20, 'test1', '$2a$10$HUsYuslJPSwnf1Yj36PIR.plgLBDzfNwrf.tjEOcBALS0ickhmQWK', 'test1@qq.com', '2025-09-10 16:55:59', 0, '2025-10-28 12:21:11', 0, '/uploads/ddcdd381-674a-49d4-9e1e-a2eaf8bc3372.jpg'),
(21, 'test4', '$2a$10$1UiKIaAdVZyG4fqzpM/Qau/n8kOEj2QnWZAOJk28FCi3cMfi25vIe', 'test4@qq.com', '2025-10-22 18:28:28', 0, '2025-10-29 21:26:36', 1, '/assets/avatars/test4_1761834675459.png'),
(265, 'test5', '$2a$10$.2FhLF16MqLPyyWFI5/L3Ow6QFbnwE5sm50qX4weSa28/.kLpwOd6', 'test5@qq.com', '2025-10-28 12:26:32', 0, NULL, 0, NULL),
(266, 'test6', '$2a$10$jhhoFT40XP5NZVbHbHcVY.7EPlY.jpwwMg6LPf/uCbMQE8LuMuzKq', 'test6@qq.com', '2025-10-31 09:38:42', 0, NULL, 0, NULL),
(267, 'test7', '$2a$10$3do8mQtJTvFY62tlbd74X.Hd/XSqAax4IcTp2mUoOJDDviLuZX2ES', 'test7@qq.com', '2025-10-31 10:08:45', 0, NULL, 0, NULL);

-- --------------------------------------------------------

--
-- 表的结构 `user_follow`
--

CREATE TABLE `user_follow` (
  `id` bigint(20) NOT NULL,
  `create_time` datetime DEFAULT NULL,
  `follower_id` bigint(20) NOT NULL,
  `following_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- 转储表的索引
--

--
-- 表的索引 `admin_user`
--
ALTER TABLE `admin_user`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UK_6etwowal6qxvr7xuvqcqmnnk7` (`email`),
  ADD UNIQUE KEY `UK_lvod9bfm438ex1071ku1glb70` (`username`);

--
-- 表的索引 `answer`
--
ALTER TABLE `answer`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FK68tbcw6bunvfjaoscaj851xpb` (`user_id`),
  ADD KEY `FK8frr4bcabmmeyyu60qt7iiblo` (`question_id`);

--
-- 表的索引 `comment`
--
ALTER TABLE `comment`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FK8kcum44fvpupyw6f5baccx25c` (`user_id`),
  ADD KEY `FKqrshjg06vmtqingtr3717cqtt` (`guide_id`);

--
-- 表的索引 `game`
--
ALTER TABLE `game`
  ADD PRIMARY KEY (`id`);

--
-- 表的索引 `guide`
--
ALTER TABLE `guide`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FKgptklcbuwy5f1y3m79olxiair` (`user_id`),
  ADD KEY `FKfk8msarwwhjwdigpq6riwod1r` (`game_id`);

--
-- 表的索引 `guide_like`
--
ALTER TABLE `guide_like`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UKqgvpfvrxgxrnpk5vitee9bpiy` (`user_id`,`guide_id`),
  ADD KEY `FK8p8771d1hxf9vbb5fbk92rark` (`guide_id`);

--
-- 表的索引 `notification`
--
ALTER TABLE `notification`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FKnbt1hengkgjqru2q44q8rlc2c` (`sender_id`),
  ADD KEY `FKqnduwq6ix2pxx1add03905i1i` (`recipient_id`);

--
-- 表的索引 `operation_log`
--
ALTER TABLE `operation_log`
  ADD PRIMARY KEY (`id`);

--
-- 表的索引 `question`
--
ALTER TABLE `question`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FK4ekrlbqiybwk8abhgclfjwnmc` (`user_id`),
  ADD KEY `FKsva05a9urpsqn3jx02mu1b2y3` (`game_id`);

--
-- 表的索引 `sequence_table`
--
ALTER TABLE `sequence_table`
  ADD PRIMARY KEY (`seq_name`);

--
-- 表的索引 `user`
--
ALTER TABLE `user`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_username` (`username`),
  ADD UNIQUE KEY `uk_email` (`email`);

--
-- 表的索引 `user_follow`
--
ALTER TABLE `user_follow`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UKp8vhuhxu2u1fm8qg7hvn4y1gs` (`follower_id`,`following_id`),
  ADD KEY `FKcjqk3xqtnelr14y8i3gp8dshw` (`following_id`);

--
-- 在导出的表使用AUTO_INCREMENT
--

--
-- 使用表AUTO_INCREMENT `admin_user`
--
ALTER TABLE `admin_user`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '管理员唯一id', AUTO_INCREMENT=5;

--
-- 使用表AUTO_INCREMENT `answer`
--
ALTER TABLE `answer`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=13;

--
-- 使用表AUTO_INCREMENT `comment`
--
ALTER TABLE `comment`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '评论信息id', AUTO_INCREMENT=30;

--
-- 使用表AUTO_INCREMENT `game`
--
ALTER TABLE `game`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=17;

--
-- 使用表AUTO_INCREMENT `guide`
--
ALTER TABLE `guide`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=57;

--
-- 使用表AUTO_INCREMENT `guide_like`
--
ALTER TABLE `guide_like`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- 使用表AUTO_INCREMENT `notification`
--
ALTER TABLE `notification`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=48;

--
-- 使用表AUTO_INCREMENT `operation_log`
--
ALTER TABLE `operation_log`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- 使用表AUTO_INCREMENT `question`
--
ALTER TABLE `question`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=23;

--
-- 使用表AUTO_INCREMENT `user`
--
ALTER TABLE `user`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户唯一id', AUTO_INCREMENT=268;

--
-- 使用表AUTO_INCREMENT `user_follow`
--
ALTER TABLE `user_follow`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- 限制导出的表
--

--
-- 限制表 `answer`
--
ALTER TABLE `answer`
  ADD CONSTRAINT `FK68tbcw6bunvfjaoscaj851xpb` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  ADD CONSTRAINT `FK8frr4bcabmmeyyu60qt7iiblo` FOREIGN KEY (`question_id`) REFERENCES `question` (`id`);

--
-- 限制表 `comment`
--
ALTER TABLE `comment`
  ADD CONSTRAINT `FK8kcum44fvpupyw6f5baccx25c` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  ADD CONSTRAINT `FKqrshjg06vmtqingtr3717cqtt` FOREIGN KEY (`guide_id`) REFERENCES `guide` (`id`);

--
-- 限制表 `guide`
--
ALTER TABLE `guide`
  ADD CONSTRAINT `FKfk8msarwwhjwdigpq6riwod1r` FOREIGN KEY (`game_id`) REFERENCES `game` (`id`),
  ADD CONSTRAINT `FKgptklcbuwy5f1y3m79olxiair` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

--
-- 限制表 `guide_like`
--
ALTER TABLE `guide_like`
  ADD CONSTRAINT `FK8p8771d1hxf9vbb5fbk92rark` FOREIGN KEY (`guide_id`) REFERENCES `guide` (`id`),
  ADD CONSTRAINT `FKeij3mxyu0melgf4l124mnv1fy` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

--
-- 限制表 `question`
--
ALTER TABLE `question`
  ADD CONSTRAINT `FK4ekrlbqiybwk8abhgclfjwnmc` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  ADD CONSTRAINT `FKsva05a9urpsqn3jx02mu1b2y3` FOREIGN KEY (`game_id`) REFERENCES `game` (`id`);

--
-- 限制表 `user_follow`
--
ALTER TABLE `user_follow`
  ADD CONSTRAINT `FK80ys44o46lqqaoft28emh0476` FOREIGN KEY (`follower_id`) REFERENCES `user` (`id`),
  ADD CONSTRAINT `FKcjqk3xqtnelr14y8i3gp8dshw` FOREIGN KEY (`following_id`) REFERENCES `user` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
