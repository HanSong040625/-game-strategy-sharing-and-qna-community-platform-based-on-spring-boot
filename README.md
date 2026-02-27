# 🎮 游戏社区平台

一个基于 Spring Boot + Vue.js 的游戏社区平台，提供游戏攻略、问答交流、实时私信等核心功能。

## 📖 项目简介

本项目是一个综合性游戏社区平台，用户可以浏览游戏信息、发布攻略、参与问答、与其他用户实时私信交流。系统采用前后端分离架构，实现了基于 WebSocket 的实时通信、智能权重排序算法和个性化推荐等核心功能。

## ✨ 核心功能

- **基于 WebSocket 的实时通信系统**：实现了用户私聊消息和系统通知的实时推送，支持消息订阅、已读状态同步和多用户并发通信。

- **智能权重排序算法**：通过动态计算攻略的点赞数与浏览量权重（likes × 1 + views × 0.1），实现热门内容的智能排序与轮播展示。

- **个性化推荐系统**：基于用户浏览行为追踪游戏权重，当用户进入游戏专区时自动更新权重值，实现主页游戏卡片的个性化排序推荐。

## 🛠️ 技术栈

### 后端
| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 2.7.15 | 核心框架 |
| Spring Security | - | 权限认证与安全控制 |
| Spring Data JPA | - | 数据持久层 |
| Spring WebSocket | - | 实时通信 |
| MyBatis | 2.2.2 | 数据持久层 |
| MySQL | 8.0 | 关系型数据库 |

### 前端
| 技术 | 版本 | 说明 |
|------|------|------|
| Vue.js | 2.6.14 | 前端框架 |
| Vue Router | 3.5.1 | 路由管理 |
| Element UI | 2.15.14 | UI 组件库 |
| Axios | 0.21.1 | HTTP 请求 |
| Webpack | 5.88.0 | 构建工具 |

## 📁 项目结构

```
game_commiuty_bi_she/
├── src/main/java/com/huadetec/gamecommunity/
│   ├── controller/          # 控制器层
│   ├── service/             # 服务层
│   ├── repository/          # 数据访问层
│   ├── entity/              # 实体类
│   └── config/              # 配置类
├── frontend/                # 前端项目
│   ├── src/
│   │   ├── views/           # 页面组件
│   │   ├── components/      # 公共组件
│   │   ├── api/             # API 接口
│   │   └── utils/           # 工具函数
│   └── package.json
├── application.yml          # 后端配置文件
└── pom.xml                  # Maven 依赖配置
```

## 🚀 快速开始

### 环境要求

- JDK 17+
- Node.js 14+
- MySQL 8.0+
- Maven 3.6+

### 数据库配置

1. 创建数据库：
```sql
CREATE DATABASE game_community CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 修改 `application.yml` 中的数据库配置：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/game_community?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: your_username
    password: your_password
```

### 后端启动

```bash
# 进入项目根目录
cd game_commiuty_bi_she

# 编译项目
mvn clean install

# 启动后端服务
mvn spring-boot:run
```

后端服务将在 `http://localhost:8080` 启动。

### 前端启动

```bash
# 进入前端目录
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端服务将在 `http://localhost:8081` 启动。

## 📸 功能截图

### 首页
首页展示热门攻略轮播和游戏列表，支持个性化推荐排序。

### 游戏专区
展示游戏详情、攻略列表和问答内容。

### 攻略详情
支持攻略浏览、点赞、评论等交互功能。

### 私信功能
基于 WebSocket 的实时私信通信。

## 🔐 用户角色

| 角色 | 权限 |
|------|------|
| 普通用户 | 浏览内容、发布攻略、提问回答、私信交流 |
| 管理员 | 用户管理、游戏管理、内容审核、操作日志查看 |

## 📝 API 接口

主要 API 接口：

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/auth/login` | POST | 用户登录 |
| `/api/auth/register` | POST | 用户注册 |
| `/api/game/list` | GET | 获取游戏列表 |
| `/api/guide/{id}` | GET | 获取攻略详情 |
| `/api/questions` | GET | 获取问答列表 |
| `/api/private-messages/send` | POST | 发送私信 |




