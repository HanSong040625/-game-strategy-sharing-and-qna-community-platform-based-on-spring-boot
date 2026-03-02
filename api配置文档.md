# API 配置文档

## 启动服务
- 前端网址：localhost:8081
- 终端输入：npm run dev

## 1. 用户认证相关接口（/api/auth）

### 1.1 健康检查
- **接口路径**：/api/auth/health
- **请求方法**：GET
- **功能描述**：用于前端检查后端服务状态
- **响应格式**：
  ```json
  {
    "code": 200,
    "message": "服务正常",
    "data": null
  }
  ```

### 1.2 用户登录
- **接口路径**：/api/auth/login
- **请求方法**：POST
- **功能描述**：用户登录
- **请求参数**：
  - username：用户名
  - password：密码
- **响应格式**：
  ```json
  {
    "code": 200,
    "message": "登录成功",
    "data": {
      "username": "test",
      "isAdmin": false
    }
  }
  ```

### 1.3 用户注册
- **接口路径**：/api/auth/register
- **请求方法**：POST
- **功能描述**：用户注册
- **请求参数**：
  - username：用户名
  - password：密码
  - email：邮箱
- **响应格式**：
  ```json
  {
    "code": 200,
    "message": "注册成功",
    "data": null
  }
  ```

### 1.4 用户登出
- **接口路径**：/api/auth/logout
- **请求方法**：POST
- **功能描述**：用户登出
- **响应格式**：
  ```json
  {
    "code": 200,
    "message": "登出成功",
    "data": null
  }
  ```

### 1.5 获取当前登录用户信息
- **接口路径**：/api/auth/current-user
- **请求方法**：GET
- **功能描述**：获取当前登录用户基本信息
- **响应格式**：
  ```json
  {
    "code": 200,
    "message": "成功",
    "data": {
      "id": 1,
      "username": "test"
    }
  }
  ```

### 1.6 获取当前登录用户完整信息
- **接口路径**：/api/auth/current-user-full
- **请求方法**：GET
- **功能描述**：获取当前登录用户完整信息
- **响应格式**：
  ```json
  {
    "code": 200,
    "message": "成功",
    "data": {
      "id": 1,
      "username": "test",
      "email": "test@example.com",
      "createTime": "2023-01-01 00:00:00",
      "isAdmin": false,
      "isOnline": 0,
      "avatar": "",
      "avatarUrl": ""
    }
  }
  ```

### 1.7 修改密码
- **接口路径**：/api/auth/change-password
- **请求方法**：POST
- **功能描述**：修改用户密码
- **请求参数**：
  - currentPassword：当前密码
  - newPassword：新密码
- **响应格式**：
  ```json
  {
    "code": 200,
    "message": "密码修改成功",
    "data": null
  }
  ```

### 1.8 更新用户个人信息
- **接口路径**：/api/auth/update-profile
- **请求方法**：POST
- **功能描述**：更新用户个人信息
- **请求参数**：
  - username：新用户名
  - email：新邮箱
- **响应格式**：
  ```json
  {
    "code": 200,
    "message": "个人信息修改成功",
    "data": null
  }
  ```

### 1.9 头像上传
- **接口路径**：/api/auth/upload-avatar
- **请求方法**：POST
- **功能描述**：上传用户头像
- **请求参数**：
  - file：头像文件
- **响应格式**：
  ```json
  {
    "code": 200,
    "message": "头像上传成功",
    "data": {
      "avatarUrl": "/assets/avatars/test_1234567890.jpg"
    }
  }
  ```

### 1.10 更新头像URL
- **接口路径**：/api/auth/update-avatar
- **请求方法**：POST
- **功能描述**：更新用户头像URL
- **请求参数**：
  - avatarUrl：头像URL
- **响应格式**：
  ```json
  {
    "code": 200,
    "message": "头像更新成功",
    "data": {
      "avatarUrl": "/assets/avatars/test_1234567890.jpg"
    }
  }
  ```

### 1.11 获取指定用户信息
- **接口路径**：/api/auth/user/{userId}
- **请求方法**：GET
- **功能描述**：通过用户ID获取用户信息
- **响应格式**：
  ```json
  {
    "code": 200,
    "message": "成功",
    "data": {
      "id": 1,
      "username": "test",
      "email": "test@example.com",
      "createTime": "2023-01-01 00:00:00",
      "avatar": ""
    }
  }
  ```

### 1.12 根据用户名获取用户信息
- **接口路径**：/api/auth/user/by-username/{username}
- **请求方法**：GET
- **功能描述**：通过用户名获取用户信息
- **响应格式**：
  ```json
  {
    "code": 200,
    "message": "成功",
    "data": {
      "id": 1,
      "username": "test",
      "email": "test@example.com",
      "createTime": "2023-01-01 00:00:00",
      "avatar": ""
    }
  }
  ```

### 1.13 获取用户列表
- **接口路径**：/api/auth/users/list
- **请求方法**：GET
- **功能描述**：获取所有用户列表（用于私聊功能）
- **响应格式**：
  ```json
  {
    "code": 200,
    "message": "成功",
    "data": [
      {
        "id": 1,
        "username": "test1",
        "email": "test1@example.com",
        "registrationDate": "2023-01-01 00:00:00",
        "lastLogin": "2023-01-02 00:00:00",
        "isAdmin": false,
        "isOnline": 0
      }
    ]
  }
  ```

## 2. 管理员相关接口（/api/admin）

### 2.1 管理员登录
- **接口路径**：/api/admin/login
- **请求方法**：POST
- **功能描述**：管理员登录
- **请求参数**：
  - username：用户名
  - password：密码
- **响应格式**：
  ```json
  {
    "code": 200,
    "message": "登录成功",
    "data": {
      "username": "admin",
      "isAdmin": true,
      "token": "1234567890-admin"
    }
  }
  ```

### 2.2 管理员登出
- **接口路径**：/api/admin/logout
- **请求方法**：POST
- **功能描述**：管理员登出
- **响应格式**：
  ```json
  {
    "code": 200,
    "message": "退出成功",
    "data": null
  }
  ```

### 2.3 管理员注册
- **接口路径**：/api/admin/register
- **请求方法**：POST
- **功能描述**：管理员注册
- **请求参数**：
  - username：用户名
  - password：密码
  - email：邮箱
- **响应格式**：
  ```json
  {
    "code": 200,
    "message": "注册成功",
    "data": null
  }
  ```

### 2.4 获取当前登录管理员信息
- **接口路径**：/api/admin/current-admin
- **请求方法**：GET
- **功能描述**：获取当前登录管理员基本信息
- **响应格式**：
  ```json
  {
    "code": 200,
    "message": "成功",
    "data": {
      "username": "admin"
    }
  }
  ```

### 2.5 获取当前登录管理员完整信息
- **接口路径**：/api/admin/current-admin-full
- **请求方法**：GET
- **功能描述**：获取当前登录管理员完整信息
- **响应格式**：
  ```json
  {
    "code": 200,
    "message": "成功",
    "data": {
      "username": "admin",
      "email": "admin@example.com",
      "createTime": "2023-01-01 00:00:00",
      "isAdmin": true
    }
  }
  ```

### 2.6 修改管理员密码
- **接口路径**：/api/admin/change-password
- **请求方法**：POST
- **功能描述**：修改管理员密码
- **请求参数**：
  - currentPassword：当前密码
  - newPassword：新密码
- **响应格式**：
  ```json
  {
    "code": 200,
    "message": "密码修改成功",
    "data": null
  }
  ```

### 2.7 更新管理员个人信息
- **接口路径**：/api/admin/update-profile
- **请求方法**：POST
- **功能描述**：更新管理员个人信息
- **请求参数**：
  - username：新用户名
  - email：新邮箱
- **响应格式**：
  ```json
  {
    "code": 200,
    "message": "个人信息更新成功",
    "data": null
  }
  ```

### 2.8 检查管理员权限
- **接口路径**：/api/admin/check-permission
- **请求方法**：GET
- **功能描述**：检查管理员权限
- **响应格式**：
  ```json
  {
    "code": 200,
    "message": "管理员权限验证通过",
    "data": "您拥有管理员权限，可以访问所有管理功能"
  }
  ```

### 2.9 获取管理员仪表盘数据
- **接口路径**：/api/admin/dashboard
- **请求方法**：GET
- **功能描述**：获取管理后台的统计信息
- **响应格式**：
  ```json
  {
    "success": true,
    "message": "获取仪表盘数据成功",
    "data": {
      "userCount": 100,
      "gameCount": 50,
      "guideCount": 200,
      "questionCount": 300,
      "recentActivities": [
        {
          "id": 1,
          "action": "ADMIN_LOGIN 管理员登录",
          "content": "ADMIN_LOGIN 管理员登录",
          "user": "admin",
          "time": "2023-01-01 00:00:00"
        }
      ]
    }
  }
  ```

### 2.10 获取所有普通用户列表
- **接口路径**：/api/admin/users
- **请求方法**：GET
- **功能描述**：获取所有普通用户数据;进行分页，返回当前分页信息
- **响应格式**：
  ```json
  {
    "code": 200,
    "message": "成功",
    "data": [
      {
        "id": 1,
        "username": "test1",
        "email": "test1@example.com",
        "registrationDate": "2023-01-01 00:00:00",
        "lastLogin": "2023-01-02 00:00:00",
        "isAdmin": false,
        "isBanned": 0
      }
    ]
  }
  ```

### 2.11 更新用户状态
- **接口路径**：/api/admin/users/{id}/status
- **请求方法**：PUT
- **功能描述**：更改用户封号状态
- **请求参数**：
  - status：用户状态（active/正常, inactive/禁用）
- **响应格式**：
  ```json
  {
    "code": 200,
    "message": "状态更新成功",
    "data": null
  }
  ```

## 3. 游戏相关接口（/api/game）

### 3.1 获取游戏列表
- **接口路径**：/api/game/list
- **请求方法**：GET
- **功能描述**：获取所有游戏列表
- **响应格式**：
  ```json
  {
    "code": 200,
    "message": "成功",
    "data": [
      {
        "id": 1,
        "name": "游戏名称",
        "description": "游戏描述",
        "logoUrl": "/assets/game/logos/game1.jpg",
        "posterUrl": "/assets/game/posters/game1.jpg",
        "releaseDate": "2023-01-01",
        "developer": "开发商",
        "publisher": "发行商",
        "categories": "角色扮演,FPS",
        "introduction": "游戏介绍"
      }
    ]
  }
  ```

### 3.2 获取游戏详情
- **接口路径**：/api/game/{id}
- **请求方法**：GET
- **功能描述**：获取游戏详细信息
- **响应格式**：
  ```json
  {
    "code": 200,
    "message": "成功",
    "data": {
      "id": 1,
      "name": "游戏名称",
      "description": "游戏描述",
      "logoUrl": "/assets/game/logos/game1.jpg",
      "posterUrl": "/assets/game/posters/game1.jpg",
      "releaseDate": "2023-01-01",
      "developer": "开发商",
      "publisher": "发行商",
      "categories": "角色扮演,FPS",
      "introduction": "游戏介绍"
    }
  }
  ```

### 3.3 获取推荐游戏
- **接口路径**：/api/game/recommended
- **请求方法**：GET
- **功能描述**：获取推荐游戏列表
- **响应格式**：
  ```json
  {
    "code": 200,
    "message": "成功",
    "data": [
      {
        "id": 1,
        "name": "游戏名称",
        "logoUrl": "/assets/game/logos/game1.jpg",
        "categories": "角色扮演,FPS"
      }
    ]
  }
  ```

### 3.4 管理员添加游戏
- **接口路径**：/api/game/admin/add
- **请求方法**：POST
- **功能描述**：管理员添加新游戏
- **请求参数**：
  - name：游戏名称
  - description：游戏描述
  - logoUrl：游戏图标URL
  - posterUrl：游戏海报URL
  - releaseDate：发行日期
  - developer：开发商
  - publisher：发行商
  - categories：游戏分类
  - introduction：游戏介绍
- **响应格式**：
  ```json
  {
    "code": 200,
    "message": "游戏添加成功",
    "data": {
      "id": 1
    }
  }
  ```

### 3.5 管理员更新游戏
- **接口路径**：/api/game/admin/update/{id}
- **请求方法**：PUT
- **功能描述**：管理员更新游戏信息
- **请求参数**：
  - name：游戏名称
  - description：游戏描述
  - logoUrl：游戏图标URL
  - posterUrl：游戏海报URL
  - releaseDate：发行日期
  - developer：开发商
  - publisher：发行商
  - categories：游戏分类
  - introduction：游戏介绍
- **响应格式**：
  ```json
  {
    "code": 200,
    "message": "游戏更新成功",
    "data": null
  }
  ```

### 3.6 管理员删除游戏
- **接口路径**：/api/game/admin/delete/{id}
- **请求方法**：DELETE
- **功能描述**：管理员删除游戏
- **响应格式**：
  ```json
  {
    "code": 200,
    "message": "游戏删除成功",
    "data": null
  }
  ```

## 4. 问答相关接口（/api/questions）

### 4.1 获取问题列表
- **接口路径**：/api/questions
- **请求方法**：GET
- **功能描述**：获取问题列表（支持分页）
- **请求参数**：
  - gameId：游戏ID（可选）
  - page：当前页码（默认1）
  - pageSize：每页数量（默认10）
- **响应格式**：
  ```json
  {
    "questions": [
      {
        "id": 1,
        "title": "问题标题",
        "content": "问题内容",
        "createTime": "2023-01-01 00:00:00",
        "author": {
          "id": 1,
          "username": "test"
        },
        "game": {
          "id": 1,
          "name": "游戏名称"
        },
        "answerCount": 2
      }
    ],
    "total": 10
  }
  ```

### 4.2 根据游戏ID获取问题列表
- **接口路径**：/api/questions/game/{gameId}
- **请求方法**：GET
- **功能描述**：根据游戏ID获取问题列表
- **响应格式**：
  ```json
  [
    {
      "id": 1,
      "title": "问题标题",
      "content": "问题内容",
      "createTime": "2023-01-01 00:00:00",
      "author": {
        "id": 1,
        "username": "test"
      },
      "game": {
        "id": 1,
        "name": "游戏名称"
      }
    }
  ]
  ```

### 4.3 创建问题
- **接口路径**：/api/questions/question/create
- **请求方法**：POST
- **功能描述**：创建新问题
- **请求参数**：
  - title：问题标题
  - content：问题内容
  - gameId：游戏ID
- **响应格式**：
  ```json
  {
    "id": 1,
    "title": "问题标题",
    "content": "问题内容",
    "createTime": "2023-01-01 00:00:00",
    "author": {
      "id": 1,
      "username": "test"
    },
    "game": {
      "id": 1,
      "name": "游戏名称"
    }
  }
  ```

## 5. 私信相关接口（/api/messages）

### 5.1 发送私信
- **接口路径**：/api/messages/send
- **请求方法**：POST
- **功能描述**：发送私信
- **请求参数**：
  - recipientId：接收者ID
  - content：消息内容
- **响应格式**：
  ```json
  {
    "code": 200,
    "message": "消息发送成功",
    "data": {
      "id": 1,
      "senderId": 1,
      "recipientId": 2,
      "content": "消息内容",
      "sendTime": "2023-01-01 00:00:00",
      "isRead": false
    }
  }
  ```

### 5.2 获取私信列表
- **接口路径**：/api/messages/list
- **请求方法**：GET
- **功能描述**：获取用户的私信列表
- **响应格式**：
  ```json
  {
    "code": 200,
    "message": "成功",
    "data": [
      {
        "id": 1,
        "senderId": 2,
        "senderUsername": "user2",
        "recipientId": 1,
        "content": "消息内容",
        "sendTime": "2023-01-01 00:00:00",
        "isRead": false
      }
    ]
  }
  ```

### 5.3 标记私信为已读
- **接口路径**：/api/messages/{id}/read
- **请求方法**：PUT
- **功能描述**：标记私信为已读
- **响应格式**：
  ```json
  {
    "code": 200,
    "message": "标记成功",
    "data": null
  }
  ```

### 5.4 获取未读消息数量
- **接口路径**：/api/messages/unread-count
- **请求方法**：GET
- **功能描述**：获取用户的未读消息数量
- **响应格式**：
  ```json
  {
    "code": 200,
    "message": "成功",
    "data": {
      "count": 5
    }
  }
  ```

## 6. WebSocket 相关

### 6.1 连接WebSocket
- **连接地址**：ws://localhost:8080/ws
- **功能描述**：建立WebSocket连接，用于实时消息通知
- **订阅路径**：/user/queue/private-messages
- **消息格式**：
  ```json
  {
    "type": "privateMessage",
    "data": {
      "senderId": 1,
      "senderUsername": "user1",
      "content": "消息内容",
      "sendTime": "2023-01-01 00:00:00"
    }
  }
  ```