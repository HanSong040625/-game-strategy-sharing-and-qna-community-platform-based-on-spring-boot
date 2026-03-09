<template>
  <div class="app">
    <!-- 全局导航栏 - 管理员路由下隐藏 -->
    <nav class="navbar" v-if="!isAdminRoute">
      <div class="navbar-container">
        <div class="navbar-brand">
          <!-- 删除游戏社区标签 -->
        </div>
        <div class="navbar-menu">
          <el-menu mode="horizontal" :default-active="activeIndex" @select="handleSelect">
            <el-menu-item index="/home" :route="{ path: '/home' }">
              <i class="el-icon-house"></i>
              <span>首页</span>
            </el-menu-item>
            <el-menu-item index="/questions" :route="{ path: '/questions' }">
              <i class="el-icon-chat-dot-round"></i>
              <span>问答专区</span>
            </el-menu-item>
            <el-menu-item index="/resources" :route="{ path: '/resources' }">
              <i class="el-icon-folder-opened"></i>
              <span>资源管理</span>
            </el-menu-item>
            <el-menu-item index="/ai-assistant" :route="{ path: '/ai-assistant' }">
              <i class="el-icon-robot"></i>
              <span>AI助手</span>
            </el-menu-item>
            <!-- 管理员菜单，只有管理员可见 -->
            <el-menu-item index="/admin" :route="{ path: '/admin' }" v-if="isLoggedInAdmin">
              <i class="el-icon-s-tools"></i>
              <span>管理后台</span>
            </el-menu-item>
          </el-menu>
        </div>
        <!-- 搜索框 -->
        <div class="navbar-search">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索游戏..."
            prefix-icon="el-icon-search"
            clearable
            @keyup.enter="handleSearch"
            class="navbar-search-input"
          ></el-input>
          <el-button type="primary" @click="handleSearch" class="navbar-search-button">搜索</el-button>
        </div>
        
        <div class="navbar-user">
          <!-- 通知图标 - 二级下拉菜单 -->
          <!-- 管理员不显示消息模块 -->
          <div class="notification-dropdown" v-if="isLoggedIn && !isLoggedInAdmin">
            <div 
              class="notification-trigger" 
              @mouseenter="showNotificationDropdown = true"
              @mouseleave="handleNotificationMouseLeave"
            >
              <div class="notification-icon">
                <i class="el-icon-message"></i>
                <div class="message-text">消息</div>
                <span v-if="unreadNotificationCount > 0" class="notification-badge">{{ unreadNotificationCount }}</span>
              </div>
            </div>
            
            <!-- 二级下拉菜单 -->
            <div 
              :class="{ 'notification-dropdown-menu': true, 'show': showNotificationDropdown }"
              @mouseenter="showNotificationDropdown = true"
              @mouseleave="handleNotificationMouseLeave"
            >
              <!-- 私聊按钮 -->
              <div class="notification-menu-item" @click="goToPrivateChat">
                <i class="el-icon-chat-line-round"></i>
                <span>私聊</span>
                <span v-if="unreadPrivateMessageCount > 0" class="category-badge">{{ unreadPrivateMessageCount }}</span>
              </div>
              
              <!-- 评论我的按钮 -->
              <div class="notification-menu-item" @click="goToNotificationCenter('comment')">
                <i class="el-icon-chat-dot-round"></i>
                <span>评论我的</span>
                <span v-if="commentNotificationsCount > 0" class="category-badge">{{ commentNotificationsCount }}</span>
              </div>
              
              <!-- 系统通知按钮 -->
              <div class="notification-menu-item" @click="goToNotificationCenter('system')">
                <i class="el-icon-bell"></i>
                <span>系统通知</span>
                <span v-if="systemNotificationsCount > 0" class="category-badge">{{ systemNotificationsCount }}</span>
              </div>
            </div>
          </div>
          <!-- 用户头像 - 二级下拉菜单 -->
          <div class="avatar-dropdown" v-if="isLoggedIn">
            <div 
              class="avatar-trigger" 
              @mouseenter="showAvatarDropdown = true"
              @mouseleave="handleAvatarMouseLeave"
            >
              <!-- 用户头像 - 简化实现确保稳定性 -->
              <div class="user-avatar" @click="goToProfileSection('user-avatar')" title="点击更换头像">
                <img v-if="userAvatar" :src="getImageUrl(userAvatar)" :alt="username" class="avatar-image" @error="handleAvatarError">
                <div v-else class="avatar-placeholder">
                  <i class="el-icon-user"></i>
                </div>
              </div>
            </div>
            
            <!-- 二级下拉菜单 -->
            <div 
              :class="{ 'avatar-dropdown-menu': true, 'show': showAvatarDropdown }"
              @mouseenter="showAvatarDropdown = true"
              @mouseleave="handleAvatarMouseLeave"
            >
              <!-- 用户统计信息区域 -->
              <div class="user-stats-section">
                <div class="user-stats-item" @click="goToUserProfile">
                  <div class="stats-label">关注</div>
                  <div class="stats-value">{{ followingCount }}</div>
                </div>
                <div class="user-stats-item" @click="goToUserProfile">
                  <div class="stats-label">粉丝</div>
                  <div class="stats-value">{{ followerCount }}</div>
                </div>
              </div>
              
              <!-- 分割线 -->
              <div class="menu-divider"></div>
              
              <!-- 我的信息按钮 -->
              <div class="avatar-menu-item" @click="goToProfileSection('user-info')">
                <i class="el-icon-user"></i>
                <span>我的信息</span>
              </div>
              
              <!-- 我的头像按钮 -->
              <div class="avatar-menu-item" @click="goToProfileSection('user-avatar')">
                <i class="el-icon-picture"></i>
                <span>我的头像</span>
              </div>
              
              <!-- 我的发布按钮 -->
              <div class="avatar-menu-item" @click="goToProfileSection('user-posts')">
                <i class="el-icon-edit"></i>
                <span>我的发布</span>
              </div>
            </div>
          </div>
          <span class="username" v-if="username">{{ username }}</span>
          <span v-if="isLoggedInAdmin" class="admin-badge">管理员</span>
          <el-button v-if="isLoggedIn" @click="handleLogout">退出登录</el-button>
        </div>
      </div>
    </nav>
    
    <!-- 主内容区 -->
    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<script>
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

export default {
  name: 'App',
  data() {
    return {
      // 使用响应式数据存储登录状态
      userLoginStatus: false,
      userName: '',
      userAvatar: '', // 用户头像URL
      searchKeyword: '', // 搜索关键字
      // 用户头像下拉菜单相关
      showAvatarDropdown: false,
     // --------------------------------消息通知-----------------------------
      // 添加管理员状态
      isAdmin: false,
      // 未读通知数量
      unreadNotificationCount: 0,
      // 通知轮询相关
      // 轮询间隔时间（毫秒），设置为1.5秒检查一次新通知
      pollingInterval: 10000, // 减少到10秒，提高实时性
      // 轮询定时器引用
      notificationPollingTimer: null,
      // 二级下拉菜单相关
      showNotificationDropdown: false,
      // 分类通知数据
      commentNotifications: [],
      systemNotifications: [],
      // WebSocket相关
      stompClient: null,
      websocketConnected: false,
      websocketReconnectTimer: null,
      // --------------------------------消息通知-------------------------------
      // 用户关注和粉丝数相关
      followingCount: 0,
      followerCount: 0,
      // 私聊相关
      unreadPrivateMessageCount: 0
    };
  },
  computed: {
    isLoggedIn() {
      // 基于响应式数据判断登录状态
      return this.userLoginStatus;
    },
    // 计算属性：是否是已登录的管理员
    isLoggedInAdmin() {
      return this.isLoggedIn && this.isAdmin;
    },
    // 计算属性：当前是否是管理员路由
    isAdminRoute() {
      return this.$route.path.startsWith('/admin');
    },
    username() {
      return this.userName;
    },
    activeIndex() {
      return this.$route.path;
    },
    // 计算评论通知数量
    commentNotificationsCount() {
      return this.commentNotifications.filter(n => !n.isRead).length;
    },
    // 计算系统通知数量
    systemNotificationsCount() {
      return this.systemNotifications.filter(n => !n.isRead).length;
    }
  },
  mounted() {
    // 初始化登录状态
    this.updateLoginStatus();
    
    // 添加延迟以确保登录状态已正确设置后再获取头像
    setTimeout(() => {
      if (this.isLoggedIn) {
        this.fetchUserAvatar();
      }
    }, 500);
    
    // 监听头像更新事件，当UserAvatar.vue上传新头像时触发
    this.setupAvatarUpdateListener();
    
    // 立即尝试建立WebSocket连接
    setTimeout(() => {
      this.setupWebSocketConnection();
    }, 1000);
    
    // 监听路由变化，更新菜单选中状态
    this.$router.afterEach(() => {
      this.$nextTick(() => {
        // 路由变化后自动更新菜单选中状态
        this.updateLoginStatus();
        // 每次路由变化都重新获取用户头像，确保及时更新
        if (this.isLoggedIn) {

          this.fetchUserAvatar();
          
          // 路由变化时重新建立WebSocket连接
          this.setupWebSocketConnection();
          
          // 如果是个人中心相关页面，立即获取
          if (this.$route.path.includes('/profile')) {
    
            // 使用setTimeout确保路由完全切换后再执行
            setTimeout(() => {
              this.fetchUserAvatar();
            }, 100);
          }
        }
      });
    });
    
    // 每隔30秒自动刷新一次头像，确保显示最新的头像
    this.avatarRefreshInterval = setInterval(() => {
      if (this.isLoggedIn) {
    
        this.fetchUserAvatar();
      }
    }, 30000);
    
    // 设置通知轮询，确保用户能及时收到新通知
    this.setupNotificationPolling();
  },
  beforeDestroy() {
    // 清除定时器
    if (this.avatarRefreshInterval) {
      clearInterval(this.avatarRefreshInterval);
    }
  },
  
  /**
   * 组件销毁前的清理工作
   * 功能：在组件销毁时清除轮询定时器和WebSocket连接
   * 作用：防止内存泄漏和无效的网络请求
   */
  beforeUnmount() {
     // console.log('🧹 组件销毁前，清除通知轮询和WebSocket连接...');
      this.clearNotificationPolling();
      this.disconnectWebSocket();
    },
    methods: {
        /**
         * 处理搜索操作
         * 点击搜索按钮或按回车键时触发
         * 优化：添加防重复跳转逻辑，避免NavigationDuplicated错误
         */
        handleSearch() {
          // 去除首尾空格
          const keyword = this.searchKeyword.trim();
          
          // 验证搜索关键字不为空
          if (!keyword) {
            this.$message.warning('请输入搜索关键字');
            return;
          }
          
         // console.log('执行搜索，关键字:', keyword);
          
          // 获取当前路由信息
          const currentPath = this.$route.path;
          const currentKeyword = this.$route.query.keyword || '';
          
          // 只有当当前不是搜索页面或者搜索关键字不同时，才执行路由跳转
          // 这样可以避免重复跳转到相同的搜索页面，防止NavigationDuplicated错误
          if (currentPath !== '/search' || currentKeyword !== keyword) {
            this.$router.push({
              path: '/search',
              query: { keyword: keyword }
            });
          } else {
          //  console.log('当前已在搜索页面且关键字相同，无需重复跳转');
          }
        },
        
        // 更新登录状态
    updateLoginStatus() {
      const storedUsername = localStorage.getItem('username');
    //  console.log('🔍 检查登录状态，用户名:', storedUsername);
      
      // 更新登录状态
      if (storedUsername) {
        this.userLoginStatus = true;
        this.userName = storedUsername;
        
        // 立即从localStorage恢复头像URL，确保页面刷新后立即显示
        let hasLocalAvatar = false;
        try {
          const userInfoStr = localStorage.getItem('user');
          if (userInfoStr) {
            const userInfo = JSON.parse(userInfoStr);
            const cachedAvatar = userInfo.avatar;
            if (cachedAvatar) {
              console.log('🔄 在updateLoginStatus中恢复头像URL:', cachedAvatar);
              this.userAvatar = cachedAvatar;
              hasLocalAvatar = true;
            } else {
              console.log('⚠️ 本地用户信息中无头像数据');
            }
          } else {
            console.log('⚠️ 本地无用户信息');
          }
        } catch (cacheError) {
          console.error('❌ 在updateLoginStatus中读取localStorage头像缓存失败:', cacheError);
        }
        
        // 先设置为非管理员状态，等待后端验证
        // 避免临时错误设置导致普通用户被识别为管理员
        this.isAdmin = false;
        localStorage.setItem('isAdmin', 'false');
        
      //  console.log('🔍 临时设置为非管理员状态，等待后端验证');
        
        // 从后端获取真实的管理员状态，确保准确性
      //  console.log('🔄 正在从后端验证用户权限...');
        this.fetchUserRole();
        
        // 获取头像（如果本地没有头像数据，则尝试从后端获取）
        if (!hasLocalAvatar) {
          console.log('🔄 本地无头像数据，尝试从后端获取头像...');
          this.fetchUserAvatar();
        } else {
          console.log('✅ 已使用本地头像数据，跳过后端获取');
        }
        
        // 获取用户关注和粉丝数
        this.fetchUserStats();
        
        // --------------------------------消息通知-------------------------------
        // 获取未读通知数量
        this.fetchUnreadNotificationCount();
        // 重新设置通知轮询，因为登录状态可能已经改变
        this.setupNotificationPolling();
        // 立即建立WebSocket连接
        this.setupWebSocketConnection();
      } else {
      //  console.log('🚫 用户未登录，清除登录状态');
        // 清除登录状态
        this.userLoginStatus = false;
        this.userName = '';
        this.userAvatar = '';
        this.isAdmin = false;
        localStorage.setItem('isAdmin', 'false');
        
        // --------------------------------消息通知-----------------------------
        this.unreadNotificationCount = 0;
        // 用户未登录时清除轮询
        this.clearNotificationPolling();
      }
    },
    
    // 处理通知鼠标离开事件
        handleNotificationMouseLeave() {
          // 延迟隐藏下拉菜单，避免鼠标移动到下拉菜单时立即关闭
          setTimeout(() => {
            if (!this.$el.querySelector('.notification-dropdown-menu:hover')) {
              this.showNotificationDropdown = false;
            }
          }, 300);
        },
        
        // 跳转到用户主页
        goToUserProfile() {
          if (this.isLoggedIn) {
            // 关闭头像下拉菜单
            this.showAvatarDropdown = false;
            
            // 检查当前是否已经在个人资料页面，避免重复导航
            if (this.$route.path !== '/profile') {
              // 跳转到用户主页
              this.$router.push('/profile');
            } else {
              console.log('当前已在个人资料页面，无需重复跳转');
            }
          }
        },
    
    // 设置头像更新事件监听器
    setupAvatarUpdateListener() {
      console.log('🔔 设置头像更新事件监听器');
      
      // 方法1：监听window全局事件
      window.addEventListener('avatar-updated', (event) => {
        console.log('🎯 收到头像更新事件:', event.detail);
        const avatarUrl = event.detail?.avatarUrl;
        if (avatarUrl) {
          console.log('🔄 立即更新是否有编辑权限: true头像:', avatarUrl);
          this.userAvatar = avatarUrl;
          
          // 同时更新本地存储
          this.updateLocalUserAvatar(avatarUrl);
        }
      });
      
      // 方法2：监听localStorage变化（备用方案）
      window.addEventListener('storage', (event) => {
        if (event.key === 'avatar_updated_timestamp') {
          console.log('🔄 检测到头像更新时间戳变化，刷新头像');
          this.fetchUserAvatar();
        }
      });
      
      // 方法3：设置全局函数供UserAvatar.vue调用
      window.refreshNavbarAvatar = (avatarUrl) => {
        console.log('🔄 通过全局函数刷新导航栏头像:', avatarUrl);
        this.userAvatar = avatarUrl;
        this.updateLocalUserAvatar(avatarUrl);
      };
      
      console.log('✅ 头像更新事件监听器设置完成');
    },
    
    // 更新本地存储的用户头像
    updateLocalUserAvatar(avatarUrl) {
      try {
        const userInfoStr = localStorage.getItem('user');
        if (userInfoStr) {
          const userInfo = JSON.parse(userInfoStr);
          userInfo.avatar = avatarUrl;
          localStorage.setItem('user', JSON.stringify(userInfo));
          console.log('💾 已更新本地存储的头像URL');
        }
      } catch (error) {
        console.error('❌ 更新本地存储头像失败:', error);
      }
    },
    
    // 获取分类通知数据
    async fetchCategorizedNotifications() {
      try {
        const token = localStorage.getItem('token');
        const userInfoStr = localStorage.getItem('user');
        
        if (!this.isLoggedIn || !token) return;
        
        // 检查用户信息是否有效
    if (!userInfoStr) {
    //  console.log('❌ 用户信息不存在，跳过分类通知API调用');
      this.commentNotifications = [];
      this.systemNotifications = [];
      return;
    }
        
        // 检查是否有缓存，避免频繁调用
        const now = Date.now();
        const lastFetchTime = localStorage.getItem('lastCategorizedFetchTime');
        const cachedNotifications = localStorage.getItem('categorizedNotifications');
        
        // 如果缓存存在且未过期（1分钟），使用缓存数据
        if (cachedNotifications && lastFetchTime && (now - parseInt(lastFetchTime)) < 60000) {
          try {
            const parsedNotifications = JSON.parse(cachedNotifications);
            this.commentNotifications = parsedNotifications.comment || [];
            this.systemNotifications = parsedNotifications.system || [];
            return;
          } catch (e) {
           // console.warn('缓存数据解析失败，重新获取');
          }
        }
        
        const response = await this.$axios.get('/api/notifications/all', {
          headers: {
            'Content-Type': 'application/json',
            'Authorization': token ? `Bearer ${token}` : ''
          },
          withCredentials: true
        });
        
        if (response.data && response.data.code === 200) {
          const allNotifications = response.data.data || [];
          
          // 分类通知
          this.commentNotifications = allNotifications.filter(n => n.type === 'comment');
          this.systemNotifications = allNotifications.filter(n => n.type === 'system');
          
          // 缓存分类通知数据
          const notificationsToCache = {
            comment: this.commentNotifications,
            system: this.systemNotifications
          };
          localStorage.setItem('categorizedNotifications', JSON.stringify(notificationsToCache));
          localStorage.setItem('lastCategorizedFetchTime', now.toString());
        }
      } catch (error) {
        console.warn('获取分类通知失败:', error.message);
      }
    },
    
    // 处理通知点击事件
    handleNotificationClick(notification) {
      if (notification) {
        // 标记通知为已读
        this.markNotificationAsRead(notification.id);
        
        // 根据通知类型跳转到相应页面
        if (notification.type === 'comment' && notification.relatedId) {
          // 跳转到攻略详情页面
          this.$router.push(`/guide/${notification.relatedId}`);
        } else if (notification.type === 'system') {
          // 系统通知跳转到通知中心
          this.$router.push('/notifications');
        }
      } else {
        // 点击通知图标，跳转到通知中心
        this.$router.push('/notifications');
      }
      
      // 关闭下拉菜单
      this.showNotificationDropdown = false;
    },
    
    // 标记通知为已读
    async markNotificationAsRead(notificationId) {
      try {
        const token = localStorage.getItem('token');
        if (!token) return;
        
        await this.$axios.put(`/api/notifications/read/${notificationId}`, {}, {
          headers: {
            'Authorization': token ? `Bearer ${token}` : ''
          },
          withCredentials: true
        });
        
        // 更新本地通知状态
        this.updateNotificationReadStatus(notificationId);
      } catch (error) {
       // console.warn('标记通知为已读失败:', error.message);
      }
    },
    
    // 更新本地通知状态
    updateNotificationReadStatus(notificationId) {
      // 更新评论通知
      const commentIndex = this.commentNotifications.findIndex(n => n.id === notificationId);
      if (commentIndex !== -1) {
        this.commentNotifications[commentIndex].isRead = true;
      }
      
      // 更新系统通知
      const systemIndex = this.systemNotifications.findIndex(n => n.id === notificationId);
      if (systemIndex !== -1) {
        this.systemNotifications[systemIndex].isRead = true;
      }
      
      // 重新获取未读数量
      this.fetchUnreadNotificationCount();
    },
    
    // 跳转到通知中心
    goToNotificationCenter(type) {
      const currentPath = this.$route.path;
      
      if (type === 'comment') {
        // 评论通知跳转到原通知页面
        if (currentPath !== '/notifications') {
          this.$router.push('/notifications');
        }
      } else if (type === 'system') {
        // 系统通知跳转到新页面
        if (currentPath !== '/system-notifications') {
          this.$router.push('/system-notifications');
        }
      }
      this.showNotificationDropdown = false;
    },
    
    // 处理头像鼠标离开事件
    handleAvatarMouseLeave() {
      // 延迟隐藏下拉菜单，避免鼠标移动到下拉菜单时立即关闭
      setTimeout(() => {
        if (!this.$el.querySelector('.avatar-dropdown-menu:hover')) {
          this.showAvatarDropdown = false;
        }
      }, 300);
    },
    
    // 跳转到个人中心特定部分
    goToProfileSection(section) {
      const path = `/profile/${section}`;
      const currentPath = this.$route.path;
      
      if (currentPath !== path) {
        this.$router.push(path);
      }
      this.showAvatarDropdown = false;
    },
    
    // 获取用户关注和粉丝数
    async fetchUserStats() {
      try {
        const token = localStorage.getItem('token');
        const userInfoStr = localStorage.getItem('user');
        
        if (!this.isLoggedIn || !token || !userInfoStr) return;
        
        const userInfo = JSON.parse(userInfoStr);
        const userId = userInfo.id;
        
        if (!userId) return;
        
        // 获取用户统计信息
        const response = await this.$axios.get(`/api/follow/stats?userId=${userId}`, {
          headers: {
            'Authorization': token ? `Bearer ${token}` : ''
          },
          withCredentials: true
        });
        
        if (response.data && response.data.code === 200) {
          const stats = response.data.data;
          this.followingCount = stats.followingCount || 0;
          this.followerCount = stats.followerCount || 0;
        //  console.log('✅ 获取用户统计信息成功:', { following: this.followingCount, followers: this.followerCount });
        }
      } catch (error) {
        //console.warn('获取用户统计信息失败:', error.message);
        // 设置默认值
        this.followingCount = 0;
        this.followerCount = 0;
      }
    },
    
    // 格式化时间显示
    formatTime(timestamp) {
      if (!timestamp) return '';
      
      const date = new Date(timestamp);
      const now = new Date();
      const diff = now.getTime() - date.getTime();
      
      // 小于1分钟
      if (diff < 60000) {
        return '刚刚';
      }
      
      // 小于1小时
      if (diff < 3600000) {
        return Math.floor(diff / 60000) + '分钟前';
      }
      
      // 小于1天
      if (diff < 86400000) {
        return Math.floor(diff / 3600000) + '小时前';
      }
      
      // 小于1个月
      if (diff < 2592000000) {
        return Math.floor(diff / 86400000) + '天前';
      }
      
      // 显示完整日期
      return date.toLocaleDateString();
    },
    
    // 处理图片URL，确保头像正确显示
    getImageUrl(url) {
      if (!url) return '';
      
      // 如果是绝对URL（包含http://或https://），直接返回
      if (url.startsWith('http://') || url.startsWith('https://')) {
        return url;
      }
      
      // 对于相对路径，构建完整的URL
      // 如果是/assets/avatars/开头的路径，直接使用当前域名构建完整URL
      if (url.startsWith('/assets/avatars/')) {
        return window.location.origin + url;
      }
      
      // 其他相对路径，直接返回（后端已配置静态资源映射）
      return url;
    },
    
    // 处理头像加载失败的情况
    handleAvatarError(event) {
      console.warn('头像加载失败，使用默认头像占位符');
      this.userAvatar = '';
    },
    
    // 获取用户头像和信息 - 简化实现确保核心功能稳定
    fetchUserAvatar() {
      const localUsername = localStorage.getItem('username');
      
      if (localUsername) {
        // 首先立即从localStorage恢复头像URL，确保页面刷新后立即显示
        try {
          const userInfoStr = localStorage.getItem('user');
          if (userInfoStr) {
            const userInfo = JSON.parse(userInfoStr);
            if (userInfo.avatar) {
              this.userAvatar = userInfo.avatar;
              this.userName = userInfo.username || localUsername;

            }
          }
        } catch (cacheError) {
          console.error('读取localStorage头像缓存失败:', cacheError);
        }
        
        // 从后端获取最新用户信息，确保头像数据同步
        this.$axios.get('/api/auth/current-user-full', {
          withCredentials: true
        })
        .then(response => {
          if (response.data && response.data.code === 200 && response.data.data) {
            const userData = response.data.data;
            const avatarUrl = userData.avatar;
            
          
            
            // 更新头像和用户信息
            this.userAvatar = avatarUrl || '';
            this.userName = userData.username || '';
            
            // 更新本地存储的用户信息
            try {
              // 创建完整的用户信息对象，确保头像字段存在
              const userInfo = {
                id: userData.id,
                username: userData.username || localUsername,
                avatar: avatarUrl || '',
                email: userData.email || '',
                createTime: userData.createTime || new Date().toISOString(),
                isAdmin: userData.isAdmin || false
              };
              
              localStorage.setItem('user', JSON.stringify(userInfo));
            } catch (error) {
              console.error('更新本地存储失败:', error);
            }
          }
        })
        .catch(error => {
          // API调用失败时，保持使用localStorage中的头像数据
          console.log('获取用户信息失败，继续使用本地存储的头像');
        });
      } else {
        this.userAvatar = '';
      }
    },
    
    /**
     * 获取未读通知数量
     * 功能：从服务器获取当前用户的未读通知数量
     * 改进：添加详细调试信息，优化错误处理逻辑，支持401错误重定向
     * @param {boolean} forceRefresh 是否强制刷新，忽略缓存
     */
    async fetchUnreadNotificationCount(forceRefresh = false) {
      // 管理员不获取通知
      if (this.isLoggedInAdmin) {
        this.unreadNotificationCount = 0;
        return;
      }
      
    //  console.log('🔔 开始获取未读通知数量');
      
      try {
        // 检查缓存
        const cacheKey = 'notification_count_cache';
        
        // 如果不是强制刷新，检查缓存是否存在且未过期（15秒缓存）
        if (!forceRefresh) {
          const cache = localStorage.getItem(cacheKey);
          if (cache) {
            const cachedData = JSON.parse(cache);
            const now = Date.now();
            // 缓存15秒
            if (now - cachedData.timestamp < 15000) {
            //  console.log('🔄 使用缓存的未读通知数量:', cachedData.count);
              this.unreadNotificationCount = cachedData.count;
              return;
            }
          }
        } else {
        //  console.log('🔄 强制刷新未读通知数量，忽略缓存');
        }

        // 检查登录状态 - 使用localStorage直接判断，因为组件状态可能未更新
        const token = localStorage.getItem('token');
        const username = localStorage.getItem('username');
        const userInfoStr = localStorage.getItem('user');
        
        // 以localStorage为准，同时记录组件状态供调试
        const isActuallyLoggedIn = !!token && !!username;
        
      //  console.log('🔐 登录信息检查:');
        //  console.log('  - localStorage token存在:', !!token);
        //  console.log('  - localStorage username存在:', !!username);
        //  console.log('  - localStorage userInfo存在:', !!userInfoStr);
        //  console.log('  - 组件状态 userLoginStatus:', this.userLoginStatus);
        //  console.log('  - 组件状态 isLoggedIn:', this.isLoggedIn);
       // console.log('  - 综合判断是否登录:', isActuallyLoggedIn);
        
        // 关键修复：如果localStorage中有登录信息但组件状态未更新，尝试强制更新组件状态
        if (isActuallyLoggedIn && !this.userLoginStatus) {
        //  console.log('🔄 检测到localStorage有登录信息但组件状态未更新，尝试强制更新组件状态');
          this.updateLoginStatus();
        }
        
        if (!isActuallyLoggedIn) {
        //  console.log('🚫 用户未登录或登录信息不完整，跳过获取未读通知');
          this.unreadNotificationCount = 0;
          return;
        }

        // 检查用户信息是否有效
        if (!userInfoStr) {
        //  console.log('❌ 用户信息不存在，跳过通知API调用');
          this.unreadNotificationCount = 0;
          return;
        }
        
        // 记录所有与用户相关的信息到控制台
        this.logAllUserInfoForDebug();
        
        // 解析用户信息并检查用户ID是否有效
        let userInfo;
        try {
          userInfo = JSON.parse(userInfoStr);
          
          // 调试日志，查看实际的用户信息
        //  console.log('📊 解析后的用户信息:', userInfo);
          
          // 检查用户ID是否有效（大于0的数字）
          const userId = parseInt(userInfo.id);
          if (!userId || userId <= 0) {
          //  console.warn('⚠️ 用户ID无效 (ID: ' + userInfo.id + ')，尝试重新获取用户信息...');
            
            // 尝试从后端重新获取用户信息
            try {
              const response = await this.$axios.get('/api/auth/current-user', {
                withCredentials: true
              });
              
              if (response.data && response.data.code === 200 && response.data.data?.id > 0) {
                // 更新用户信息
                const newUserInfo = {
                  ...userInfo,
                  id: response.data.data.id
                };
                localStorage.setItem('user', JSON.stringify(newUserInfo));
              //  console.log('✅ 已从后端更新用户ID:', newUserInfo.id);
                // 重新设置userInfo变量，确保后续代码可以使用
                userInfo = newUserInfo;
              } else {
               // console.warn('⚠️ 无法从后端获取有效用户ID');
                this.unreadNotificationCount = 0;
                
                // 更新缓存
                localStorage.setItem(cacheKey, JSON.stringify({
                  count: 0,
                  timestamp: Date.now()
                }));
                return;
              }
            } catch (refreshError) {
            //  console.error('❌ 获取用户信息失败:', refreshError);
              this.unreadNotificationCount = 0;
              
              // 更新缓存
              localStorage.setItem(cacheKey, JSON.stringify({
                count: 0,
                timestamp: Date.now()
              }));
              return;
            }
          }
        } catch (parseError) {
        //  console.error('❌ 解析用户信息失败:', parseError);
          // 尝试清除错误的用户信息，然后从后端获取
          localStorage.removeItem('user');
          try {
          //  console.log('🔄 尝试重新获取用户信息...');
            const response = await this.$axios.get('/api/auth/current-user', {
              withCredentials: true
            });
            
            if (response.data && response.data.code === 200 && response.data.data?.id) {
              const newUserInfo = {
                id: response.data.data.id,
                username: username,
                isAdmin: localStorage.getItem('isAdmin') === 'true'
              };
              localStorage.setItem('user', JSON.stringify(newUserInfo));
            //  console.log('✅ 已重新创建用户信息:', newUserInfo);
              // 重新设置userInfo变量，确保后续代码可以使用
              userInfo = newUserInfo;
            } else {
            //  console.warn('⚠️ 无法从后端获取有效用户信息');
              this.unreadNotificationCount = 0;
              // 更新缓存
              localStorage.setItem(cacheKey, JSON.stringify({
                count: 0,
                timestamp: Date.now()
              }));
              return;
            }
          } catch (refreshError) {
          //  console.error('❌ 重新获取用户信息失败:', refreshError);
            this.unreadNotificationCount = 0;
            return;
          }
        }
        
        // 确保userInfo和userInfo.id有效，然后才继续处理
        if (!userInfo || !userInfo.id || userInfo.id <= 0) {
        //  console.error('❌ 用户信息无效，无法继续加载通知');
          this.unreadNotificationCount = 0;
          // 更新缓存
          localStorage.setItem(cacheKey, JSON.stringify({
            count: 0,
            timestamp: Date.now()
          }));
          return;
        }
        
        const response = await this.$axios.get('/api/notifications/unread', {
          withCredentials: true,
          timeout: 5000
        });

        if (response.data.code === 200) {
          const count = response.data.count || 0;
          this.unreadNotificationCount = count;
          
          // 更新缓存
          localStorage.setItem(cacheKey, JSON.stringify({
            count: count,
            timestamp: Date.now()
          }));
        } else {
          this.unreadNotificationCount = 0;
        }
      } catch (error) {
        // 处理特定错误状态
        if (error.response) {
          const status = error.response.status;
          
          if (status === 401) {
            // 未授权，清除登录状态
            localStorage.removeItem('token');
            localStorage.removeItem('username');
            localStorage.removeItem('user');
            localStorage.removeItem('isAdmin');
            localStorage.removeItem('userRole');
            this.$message.warning('登录已过期，请重新登录');
            this.$router.push('/login');
          } else if (status === 400) {
            // 处理400错误（用户ID为-1的情况）
            this.unreadNotificationCount = 0;
            
            // 更新缓存
            const cacheKey = 'notification_count_cache';
            localStorage.setItem(cacheKey, JSON.stringify({
              count: 0,
              timestamp: Date.now()
            }));
          }
        }
        
        this.unreadNotificationCount = 0;
      }
    },
    
    // 记录所有与用户相关的信息到控制台，用于调试
    logAllUserInfoForDebug() {
      // 此方法用于调试，保留空实现
    },
    
    /**
     * 处理通知点击事件
     * 功能：当用户点击通知图标时更新未读通知数量
     * 原因：确保在用户查看通知后，右上角的红点数量能够及时更新
     */
    handleNotificationClick() {
      
      // 重新获取未读通知数量，确保在用户查看通知后数量能够同步
      this.fetchUnreadNotificationCount();
     
    },
    // --------------------------------消息通知-----------------------------  
    
    /**
     * 设置通知轮询
     * 功能：在用户登录状态下，定期检查新的未读通知
     * 作用：解决用户需要手动刷新页面才能看到新通知的问题
     * 实现：使用setInterval定期调用fetchUnreadNotificationCount方法
     */
    setupNotificationPolling() {
      // 先清除可能存在的旧定时器
      this.clearNotificationPolling();
      
      // 以localStorage为准检查登录状态
      const token = localStorage.getItem('token');
      const username = localStorage.getItem('username');
      const isActuallyLoggedIn = !!token && !!username;
      
      //  console.log('🔄 setupNotificationPolling 登录状态检查:');
      // console.log('  - localStorage token存在:', !!token);
      // console.log('  - localStorage username存在:', !!username);
     // console.log('🔄 setupNotificationPolling 登录状态检查:');
     // console.log('  - localStorage token存在:', !!token);
     // console.log('  - localStorage username存在:', !!username);
     // console.log('  - 组件状态 isLoggedIn:', this.isLoggedIn);
      //console.log('  - 综合判断是否登录:', isActuallyLoggedIn);
      
      // 关键修复：如果localStorage中有登录信息但组件状态未更新，尝试强制更新
      if (isActuallyLoggedIn && !this.isLoggedIn) {
      //  console.log('🔄 setupNotificationPolling: localStorage有登录信息但组件状态未更新，强制更新状态');
        this.updateLoginStatus();
      }
      
      // 只有在用户已登录的情况下才设置轮询
      if (isActuallyLoggedIn) {
        // 设置WebSocket连接
        this.setupWebSocketConnection();
        
        // 设置新的轮询定时器（作为WebSocket的备用方案）
        this.notificationPollingTimer = setInterval(() => {
          this.fetchUnreadNotificationCount();
        }, this.pollingInterval);
      } else {
        // 用户未登录时断开WebSocket连接
        this.disconnectWebSocket();
      }
    },
    
    /**
     * 设置WebSocket连接
     * 功能：建立WebSocket连接并订阅实时通知
     * 作用：实现真正的实时通知推送，避免轮询延迟
     */
    setupWebSocketConnection() {
      // 管理员不连接WebSocket
      if (this.isLoggedInAdmin) {
        return;
      }
      
      // 先断开现有连接
      this.disconnectWebSocket();
      
      // 检查用户是否已登录 - 以localStorage为准
      const token = localStorage.getItem('token');
      const username = localStorage.getItem('username');
      const isActuallyLoggedIn = !!token && !!username;
      

      
      if (!isActuallyLoggedIn) {
        return;
      }
      
      // 获取当前用户ID
      const userInfoStr = localStorage.getItem('user');
      if (!userInfoStr) {
        console.log('❌ App.vue 本地无用户信息，跳过WebSocket连接');
        return;
      }
      
      try {
        const userInfo = JSON.parse(userInfoStr);
        const userId = userInfo.id;
        
        if (!userId || userId <= 0) {
          console.log('❌ App.vue 用户ID无效，跳过WebSocket连接');
          return;
        }
        
        console.log('🔌 App.vue开始建立WebSocket连接，用户ID:', userId);
        
        // 创建STOMP客户端
        this.stompClient = new Client({
          webSocketFactory: () => new SockJS('http://localhost:8080/ws-notifications'),
          reconnectDelay: 2000, // 减少重连延迟到2秒
          heartbeatIncoming: 1000, // 减少心跳间隔到1秒，提高实时性
          heartbeatOutgoing: 1000, // 减少心跳间隔到1秒，提高实时性
          onConnect: () => {
            console.log('✅ App.vue WebSocket连接成功');
            this.websocketConnected = true;
            
            // 订阅用户专属的通知频道
            this.stompClient.subscribe(`/user/${userId}/topic/notifications`, (message) => {
              console.log('🔔 App.vue收到实时通知:', message.body);
              this.handleRealTimeNotification(JSON.parse(message.body));
            });
            
            // 订阅用户专属的私聊消息频道
            this.stompClient.subscribe(`/user/${userId}/topic/private-messages`, (message) => {
              console.log('💬 App.vue收到实时私聊消息:', message.body);
              this.handleRealTimePrivateMessage(JSON.parse(message.body));
            });
            
            // 清除重连定时器
            if (this.websocketReconnectTimer) {
              clearTimeout(this.websocketReconnectTimer);
              this.websocketReconnectTimer = null;
            }
          },
          onStompError: (frame) => {
            console.error('❌ App.vue WebSocket STOMP错误:', frame);
            this.websocketConnected = false;
            this.scheduleWebSocketReconnect();
          },
          onWebSocketClose: () => {
            console.log('🔌 App.vue WebSocket连接关闭');
            this.websocketConnected = false;
            this.scheduleWebSocketReconnect();
          },
          onDisconnect: () => {
            console.log('🔌 App.vue WebSocket连接断开');
            this.websocketConnected = false;
          }
        });
        
        // 激活客户端
        this.stompClient.activate();
        console.log('🔌 App.vue WebSocket客户端已激活');
        
      } catch (error) {
        console.error('❌ App.vue WebSocket连接失败:', error);
        this.scheduleWebSocketReconnect();
      }
    },
    
    /**
     * 处理实时通知
     * 功能：处理从WebSocket接收到的实时通知
     */
    handleRealTimeNotification(notificationData) {
      if (notificationData.type === 'notification') {
        // 收到新通知，更新未读数量
        this.unreadNotificationCount += 1;
        
        // 根据通知类型更新分类通知
        const notification = notificationData.notification;
        if (notification.type === 'comment') {
          this.commentNotifications.unshift(notification);
        } else if (notification.type === 'system') {
          this.systemNotifications.unshift(notification);
        }
        
        // 显示新通知提示
        this.showNewNotificationAlert(notification);
      }
    },
    
    /**
     * 处理实时私聊消息
     * 功能：处理从WebSocket接收到的实时私聊消息
     */
    handleRealTimePrivateMessage(messageData) {
      console.log('💬 App.vue处理实时私聊消息数据:', messageData);
      
      if (messageData.type === 'private_message') {
        // 收到新私聊消息，更新未读数量
        this.unreadPrivateMessageCount += 1;
        console.log('💬 App.vue更新未读消息计数:', this.unreadPrivateMessageCount);
        
        // 显示新消息提示
        console.log('💬 App.vue显示新消息提示');
        this.$message({
          message: '你有一条未读消息',
          type: 'info',
          duration: 3000,
          showClose: true,
          center: true
        });
      }
    },
    
    /**
     * 显示新通知提示
     * 功能：在收到新通知时显示提示信息
     */
    showNewNotificationAlert(notification) {
      const message = `收到新${notification.type === 'comment' ? '评论' : '系统'}通知: ${notification.content}`;
      this.$message({
        message: message,
        type: 'info',
        duration: 3000,
        showClose: true
      });
    },
    
    /**
     * 安排WebSocket重连
     * 功能：在WebSocket连接失败时安排重连
     */
    scheduleWebSocketReconnect() {
      if (this.websocketReconnectTimer) {
        clearTimeout(this.websocketReconnectTimer);
      }
      
      this.websocketReconnectTimer = setTimeout(() => {
        if (this.isLoggedIn) {
          this.setupWebSocketConnection();
        }
      }, 2000); // 减少重连延迟到2秒，提高实时性
    },
    
    /**
     * 断开WebSocket连接
     * 功能：安全地断开WebSocket连接
     */
    disconnectWebSocket() {
      if (this.stompClient) {
        try {
          this.stompClient.deactivate();
        } catch (error) {
          // 忽略断开连接时的错误
        }
        this.stompClient = null;
      }
      
      this.websocketConnected = false;
      
      if (this.websocketReconnectTimer) {
        clearTimeout(this.websocketReconnectTimer);
        this.websocketReconnectTimer = null;
      }
    },
    
    /**
     * 清除通知轮询
     * 功能：清除已存在的轮询定时器
     * 作用：防止定时器重复创建和内存泄漏
     */
    clearNotificationPolling() {
      if (this.notificationPollingTimer) {
        
        clearInterval(this.notificationPollingTimer);
        this.notificationPollingTimer = null;
      }
    },


    // 从后端获取用户角色信息
    async fetchUserRole() {
     // console.log('📋 开始获取用户角色信息');
      try {
        // 获取当前用户名用于日志
        const username = localStorage.getItem('username');
      //  console.log('👤 当前用户名:', username);
        
        // 清除可能存在的错误缓存
        localStorage.setItem('isAdmin', 'false');
        localStorage.setItem('userRole', 'user');
        this.isAdmin = false;
      //  console.log('🧹 已重置管理员状态，准备从后端获取真实权限');
        
        // 直接调用后端接口获取最新的用户权限信息
        const userResponse = await this.$axios.get('/api/auth/current-user-full', {
          withCredentials: true
        });
        
        const data = userResponse.data;
       // console.log('📡 API响应:', data);
        
        if (data.code === 200 && data.data) {
         // console.log('✅ 成功获取用户信息');
        //  console.log('🔍 API返回的isAdmin值:', data.data.isAdmin);
          
          // 存储管理员状态到localStorage（转换为布尔值字符串）
          const isAdminValue = Boolean(data.data.isAdmin);
          localStorage.setItem('isAdmin', isAdminValue.toString());
          // 同时设置用户角色
          localStorage.setItem('userRole', isAdminValue ? 'admin' : 'user');
          // 更新响应式状态
          this.isAdmin = isAdminValue;
          
         // console.log('✅ 管理员权限设置完成');
         // console.log('👑 最终管理员状态:', this.isAdmin);
         // console.log('📝 本地存储的isAdmin:', localStorage.getItem('isAdmin'));
        //  console.log('📝 本地存储的userRole:', localStorage.getItem('userRole'));
          return;
        } else {
          console.warn('⚠️ API返回数据不完整或状态码错误:', data);
        }
      } catch (error) {
       // console.error('❌ 获取用户角色信息失败:', error.message);
        
        // 如果有响应错误
        if (error.response) {
      
          if (error.response.status === 403) {
          
            // 普通用户没有权限访问，直接设置为非管理员和普通用户角色
            this.isAdmin = false;
            localStorage.setItem('isAdmin', 'false');
            localStorage.setItem('userRole', 'user');
            return;
          }
          
          if (error.response.status === 401) {
          
            // 如果是401错误（未授权），可能是token过期，清除登录状态
            this.handleLogout();
            return;
          }
        } else if (error.request) {
          
        } else {
         
        }
        
        // 无论什么错误，默认设置为非管理员，确保安全
    
        this.isAdmin = false;
        localStorage.setItem('isAdmin', 'false');
        localStorage.setItem('userRole', 'user');
        
      }
    },
    handleSelect(key) {
      // 使用组件内的响应式登录状态
      if (!this.isLoggedIn) {
        // 如果未登录，不执行导航并显示提示
        this.$message.warning('请先登录');
        
        // 避免重复导航到登录页
        if (this.$route.path !== '/login') {
          this.$router.replace('/login');
        }
        return;
      }
      
      // 已登录用户正常导航
      if (this.$route.path !== key) {
        this.$router.push(key);
      }
    },
    async handleLogout() {
      try {
        // 调用标准的退出登录接口，后端会自动设置is_online为0
        await this.$axios.post('/api/auth/logout', {}, {
          withCredentials: true,
          timeout: 3000
        }).catch(error => {
          // 即使出错也继续执行退出流程，不阻塞用户
         
        });
      } catch (error) {
        // 不阻止退出流程
     
      } finally {
        // 清除本地存储的所有信息
        localStorage.removeItem('token');
        localStorage.removeItem('username');
        localStorage.removeItem('isAdmin');
        localStorage.removeItem('userRole');
        localStorage.removeItem('user');
        localStorage.removeItem('isOnline');
        
        // 立即更新响应式的登录状态
        this.userLoginStatus = false;
        this.userName = '';
        this.isAdmin = false;
        
        // 清除通知轮询，因为用户已登出
        this.clearNotificationPolling();
        
        // 使用replace跳转到登录页面，避免导航栈中堆积多个登录页面
        this.$router.replace('/login');
      }
    },
    handleBrandClick() {
      // 只有在已登录状态下才执行导航
      if (this.isLoggedIn && this.$route.path !== '/home') {
        this.$router.push('/home');
      }
    },
    
    /**
     * 跳转到私聊页面
     * 功能：导航到私聊页面并关闭下拉菜单
     */
    goToPrivateChat() {
      // 关闭消息下拉菜单
      this.showNotificationDropdown = false;
      
      // 检查登录状态
      if (!this.isLoggedIn) {
        this.$message.warning('请先登录');
        this.$router.push('/login');
        return;
      }
      
      // 导航到私聊页面
      if (this.$route.path !== '/private-chat') {
        this.$router.push('/private-chat');
      }
    }
  }
};
</script>

<style scoped>
.app {
  font-family: 'Arial', sans-serif;
  color: #333;
}

/* 导航栏样式 */
.navbar {
  background-color: #1890ff;
  color: white;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  height: 180.19px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
}

.navbar-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.navbar-brand {
  font-size: 24px;
  font-weight: bold;
}

.navbar-brand a {
  color: white;
  text-decoration: none;
}

.navbar-menu {
  flex: 1;
  margin-left: 50px;
}

/* 搜索框样式 */
.navbar-search {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 20px;
}

.navbar-search-input {
  width: 280px;
  border: 2px solid #e0e0e0;
  border-radius: 4px;
  transition: all 0.3s ease;
  background-color: white;
}

.navbar-search-input .el-input__inner {
  color: #333;
}

.navbar-search-input:focus {
  border-color: #40a9ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

.navbar-search-button {
  transition: all 0.3s ease;
}

.navbar-search-button:hover {
  background-color: #40a9ff;
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.navbar-search-button:active {
  transform: translateY(0);
  box-shadow: none;
}

/*--------------------------------消息通知-----------------------------*/

/* 导航栏用户区域样式 */
.navbar-user {
  margin-left: 20px;
   /*--------------------------------消息通知-----------------------------*/
  display: flex;
  align-items: center;
  gap: 15px;
}

/* 通知下拉菜单容器样式 */
.notification-dropdown {
  position: relative;
}

/* 通知图标样式 */
.notification-icon {
  position: relative;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  padding: 8px;
  border-radius: 6px;
}

.notification-icon:hover {
  background-color: rgba(255, 255, 255, 0.1);
  transform: translateY(-1px);
}

.notification-link {
  color: white;
  display: inline-block;
  padding: 5px;
  transition: color 0.3s;
  text-decoration: none;
}

.notification-link:hover {
  color: rgba(255, 255, 255, 0.8);
  text-decoration: none;
}

/* 信封图标容器样式 */
.envelope-icon {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  line-height: 1;
}

/* 消息文字样式 */
.message-text {
  font-size: 12px;
  margin-top: 2px;
  color: white;
  font-weight: normal;
  text-decoration: none;
}

/* 未读通知数量徽章样式 */
.notification-badge {
  position: absolute;
  top: -5px;
  right: -5px;
  min-width: 16px;
  height: 16px;
  padding: 0 5px;
  font-size: 12px;
  line-height: 16px;
  border-radius: 8px;
  background-color: #f56c6c;
  color: #fff;
  text-align: center;
  white-space: nowrap;
}

/* 二级下拉菜单样式 */
.notification-dropdown-menu {
  position: absolute;
  top: 100%;
  left: 50%;
  width: 200px;
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  padding: 8px 0;
  transform: translateX(-50%) scale(0.95); /* 相对于消息图标居中，初始缩放 */
  opacity: 0;
  transform-origin: top center;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  pointer-events: none;
}

/* 下拉菜单显示时的动画效果 */
.notification-dropdown:hover .notification-dropdown-menu,
.notification-dropdown-menu.show {
  opacity: 1;
  transform: translateX(-50%) scale(1);
  pointer-events: auto;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

/* 二级菜单按钮样式 */
.notification-menu-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  transform: translateX(-5px);
  opacity: 0.8;
}

.notification-menu-item:hover {
  background-color: #f5f5f5;
  transform: translateX(0);
  opacity: 1;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* 菜单项图标动画 */
.notification-menu-item i {
  transition: transform 0.3s ease, color 0.3s ease;
}

.notification-menu-item:hover i {
  transform: scale(1.1);
  color: #409eff;
}

.notification-menu-item i {
  font-size: 16px;
  margin-right: 8px;
  color: #666;
}

.notification-menu-item span {
  font-size: 14px;
  color: #333;
  flex: 1;
}

.notification-menu-item .category-badge {
  position: static;
  margin-left: 8px;
  min-width: 20px;
  height: 20px;
  line-height: 20px;
  font-size: 12px;
}





/* 头像下拉菜单容器样式 */
.avatar-dropdown {
  position: relative;
}

.avatar-trigger {
  cursor: pointer;
}

/* 用户头像样式 */
.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  overflow: hidden;
  border: 2px solid rgba(255, 255, 255, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba(255, 255, 255, 0.1);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 1001;
}

/* 头像触碰时放大2倍的动画 */
.avatar-trigger:hover .user-avatar {
  transform: scale(2);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  border-color: rgba(255, 255, 255, 1);
  background-color: rgba(255, 255, 255, 0.2);
}

.user-avatar .avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-avatar .avatar-placeholder {
  font-size: 18px;
  color: rgba(255, 255, 255, 0.8);
}

/* 头像二级下拉菜单样式 */
.avatar-dropdown-menu {
  position: absolute;
  top: 100%;
  left: 50%;
  width: 160px;
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  padding: 8px 0;
  transform: translateX(-50%) scale(0.95); /* 相对于头像居中，初始缩放 */
  opacity: 0;
  transform-origin: top center;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  pointer-events: none;
  margin-top: 5px;
}

/* 头像下拉菜单显示时的动画效果 */
.avatar-dropdown:hover .avatar-dropdown-menu,
.avatar-dropdown-menu.show {
  opacity: 1;
  transform: translateX(-50%) scale(1);
  pointer-events: auto;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

/* 用户统计信息区域样式 */
.user-stats-section {
  display: flex;
  justify-content: center;
  padding: 12px 8px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 8px;
}

.user-stats-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  padding: 8px 12px;
  border-radius: 6px;
  min-width: 50px;
  margin: 0 8px;
}

.user-stats-item:hover {
  background-color: #f8f9fa;
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.stats-label {
  font-size: 12px;
  color: #666;
  margin-bottom: 4px;
  font-weight: 500;
}

.stats-value {
  font-size: 18px;
  font-weight: bold;
  color: #409eff;
  line-height: 1;
}

/* 菜单分割线样式 */
.menu-divider {
  height: 1px;
  background-color: #f0f0f0;
  margin: 8px 0;
}

/* 头像菜单项样式 */
.avatar-menu-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  transform: translateX(-5px);
  opacity: 0.8;
}

.avatar-menu-item:hover {
  background-color: #f5f5f5;
  transform: translateX(0);
  opacity: 1;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* 头像菜单项图标动画 */
.avatar-menu-item i {
  transition: transform 0.3s ease, color 0.3s ease;
  font-size: 16px;
  margin-right: 8px;
  color: #666;
}

.avatar-menu-item:hover i {
  transform: scale(1.1);
  color: #409eff;
}

.avatar-menu-item span {
  font-size: 14px;
  color: #333;
  flex: 1;
}

/* 用户名样式 */
.username {
  margin-right: 15px;
  font-weight: bold;
  color: white;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .navbar-container {
    flex-wrap: wrap;
    height: auto;
    padding: 10px;
  }
  
  .navbar-search {
    margin: 10px 0;
    width: 100%;
    justify-content: center;
  }
  
  .navbar-search-input {
    width: 200px;
  }
}

/* 管理员徽章样式 */
.admin-badge {
  background-color: #ff4d4f;
  color: white;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
  margin-right: 15px;
}

/* 主内容区样式 */
.main-content {
  padding-top: 200px; /* 为导航栏留出空间 */
  padding-bottom: 50px;
  min-height: 100vh;
}

/* 管理员页面特殊样式 - 移除顶部内边距 */
.main-content .admin-operation-logs,
.main-content .admin-home,
.main-content .admin-user-management,
.main-content .admin-game-management {
  margin-top: -200px;
  width: 100%;
  min-height: calc(100vh + 200px);
}

/* 调整Element UI的菜单样式 */
.el-menu--horizontal {
  background-color: transparent;
  border-bottom: none;
}

.el-menu--horizontal .el-menu-item {
  color: white;
}

.el-menu--horizontal .el-menu-item i {
  color: white;
}

.el-menu--horizontal .el-menu-item.is-active {
  color: white;
  font-weight: bold;
  border-bottom: 2px solid white;
}

.el-menu--horizontal .el-menu-item.is-active i {
  color: white;
}

/* 按钮样式 */
.el-button {
  background-color: rgba(255, 255, 255, 0.2);
  border-color: white;
  color: white;
}

.el-button:hover {
  background-color: rgba(255, 255, 255, 0.3);
  border-color: white;
  color: white;
}
</style>