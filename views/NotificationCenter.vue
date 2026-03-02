<template>
  <div class="notification-center">
    <h1>评论我的</h1>
    
    <!-- 通知操作栏 -->
    <div class="notification-header">
      <div class="notification-stats">
        <span>共有 {{ notifications.length }} 条通知</span>
        <span v-if="unreadCount > 0" class="unread-count">未读: {{ unreadCount }}</span>
      </div>
      <div class="notification-actions">
        <el-button 
          type="primary" 
          @click="markAllAsRead"
          :disabled="unreadCount === 0"
          class="mark-all-btn"
        >
          全部标记为已读
        </el-button>
        <el-button 
          type="danger" 
          @click="deleteAllNotifications"
          :disabled="notifications.length === 0"
          class="delete-all-btn"
        >
          清空所有通知
        </el-button>
      </div>
    </div>
    
    <!-- 通知列表 -->
    <div class="notification-list">
      <div 
        v-for="notification in notifications"
        :key="notification.id"
        class="notification-item"
        :class="{ 'unread': !notification.isRead }"
      >
        <div class="notification-content">
          <div class="notification-header">
            <span class="notification-type">{{ getNotificationTypeLabel(notification.type) }}</span>
            <span class="notification-time">{{ formatTime(notification.createTime) }}</span>
          </div>
          <div class="notification-body">
            <p>{{ notification.content }}</p>
            <p v-if="notification.senderId" class="notification-sender">
              发送者: {{ getSenderDisplayName(notification) }}
            </p>
          </div>
          <div class="notification-actions">
            <el-button 
              size="small" 
              type="text" 
              @click="markAsRead(notification.id)"
              v-if="!notification.isRead"
            >
              标记已读
            </el-button>
            <el-button 
              size="small" 
              type="text" 
              @click="viewRelatedContent(notification)"
              v-if="notification.relatedId"
            >
              查看详情
            </el-button>
          </div>
        </div>
      </div>
      
      <!-- 无通知提示 -->
      <div v-if="notifications.length === 0" class="empty-notifications">
        <p>暂无通知</p>
      </div>
    </div>
  </div>
</template>

<script>
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

export default {
  name: 'NotificationCenter',
  data() {
    return {
      notifications: [],
      unreadCount: 0,
      loading: false,
      stompClient: null,
      websocketConnected: false,
      pollingInterval: null,
      pollingEnabled: true,
      lastPollTime: null
    };
  },
  mounted() {
    console.log('📢 NotificationCenter 组件已挂载');
    // 显示所有本地存储的用户相关信息
    this.logAllUserInfo();
    this.loadAllNotifications();
    // 设置WebSocket连接，监听实时通知
    this.setupWebSocketConnection();
    // 启动轮询机制，作为WebSocket的备用方案
    this.startPolling();
  },
  beforeDestroy() {
    // 组件销毁前断开WebSocket连接
    this.disconnectWebSocket();
    // 停止轮询机制
    this.stopPolling();
  },
  
  methods: {
      // 记录所有与用户相关的信息到控制台
      logAllUserInfo() {
        console.log('🔍 开始记录所有用户相关信息:');
        
        // 记录localStorage中的所有用户相关数据
        const userData = {
          user: localStorage.getItem('user'),
          token: localStorage.getItem('token'),
          username: localStorage.getItem('username'),
          isAdmin: localStorage.getItem('isAdmin'),
          userId: localStorage.getItem('userId')
        };
        
        console.log('📊 localStorage 用户数据:', userData);
        
        // 尝试解析用户对象
        try {
          const userObj = JSON.parse(localStorage.getItem('user'));
          console.log('👤 解析后的用户对象:', userObj);
          
          // 检查对象类型
          console.log('🔧 用户ID类型:', typeof userObj?.id);
          console.log('🔧 用户ID值:', userObj?.id);
          console.log('🔧 用户ID有效性检查:', userObj?.id && typeof userObj.id === 'number' && userObj.id > 0);
        } catch (e) {
          console.error('❌ 解析用户对象失败:', e);
        }
        
        // 检查登录状态 - 与loadAllNotifications方法保持一致
        const token = localStorage.getItem('token');
        const username = localStorage.getItem('username');
        const isLoggedIn = !!token && !!username;
        console.log('🚪 当前登录状态检查:');
        console.log('  - token存在:', !!token);
        console.log('  - username存在:', !!username);
        console.log('  - 综合判断是否登录:', isLoggedIn);
        
        console.log('📝 用户信息记录完成');
      },
    /**
   * 加载所有通知
   * 功能：从服务器获取当前用户的所有通知列表
   * 改进：添加详细调试信息，优化错误处理逻辑，支持401错误重定向
   */
  async loadAllNotifications() {
      console.log('📢 开始加载评论通知...');
      
      // 检查用户登录状态
      const token = localStorage.getItem('token');
      const username = localStorage.getItem('username');
      const userInfoStr = localStorage.getItem('user');
      console.log('🔐 登录信息检查:');
      console.log('  - token存在:', !!token);
      console.log('  - username:', username);
      console.log('  - 是否登录状态:', !!token && !!username);
      
      // 确保用户已登录
      if (!token || !username) {
        console.log('🚫 用户未登录，重定向到登录页');
        this.$message.warning('请先登录查看通知');
        setTimeout(() => {
          this.$router.push('/login');
        }, 1000);
        return;
      }

      // 检查用户信息是否有效
      if (!userInfoStr) {
        console.log('❌ 用户信息不存在');
        this.$message.error('用户信息不完整');
        this.loading = false;
        return;
      }
    
    // 解析用户信息并检查用户ID是否有效
    let userInfo;
    try {
      userInfo = JSON.parse(userInfoStr);
      
      // 调试日志，查看实际的用户信息
      console.log('📊 解析后的用户信息:', userInfo);
      
      // 检查用户ID是否有效（大于等于0的数字，0表示默认值，但实际用户ID应该大于0）
      const userId = parseInt(userInfo.id);
      if (!userId || userId < 0) {
        console.warn('⚠️ 用户ID无效 (ID: ' + userInfo.id + ')，尝试重新获取用户信息...');
        console.log('🔍 详细用户信息检查:');
        console.log('  - 原始ID值:', userInfo.id);
        console.log('  - 解析后ID:', userId);
        console.log('  - ID类型:', typeof userInfo.id);
        console.log('  - 完整用户对象:', userInfo);
        
        // 尝试从后端重新获取用户信息
        try {
          const response = await this.$axios.get('/api/auth/current-user-full', {
            withCredentials: true
          });
          
          if (response.data && response.data.code === 200 && response.data.data?.id > 0) {
            // 更新用户信息
            const newUserInfo = {
              ...userInfo,
              id: response.data.data.id,
              username: response.data.data.username || userInfo.username,
              isAdmin: response.data.data.isAdmin || userInfo.isAdmin
            };
            localStorage.setItem('user', JSON.stringify(newUserInfo));
            console.log('✅ 已从后端更新用户ID:', newUserInfo.id);
            // 重新解析用户信息，确保使用最新的ID
            userInfo = newUserInfo;
          } else {
            console.warn('⚠️ 无法从后端获取有效用户ID');
            this.$message.warning('当前用户状态异常，无法加载通知');
            this.loading = false;
            return;
          }
        } catch (refreshError) {
          console.error('❌ 获取用户信息失败:', refreshError);
          this.$message.warning('当前用户状态异常，无法加载通知');
          this.loading = false;
          return;
        }
      } else if (userId === 0) {
        // ID为0表示默认值，但实际用户ID应该大于0，尝试重新获取
        console.log('🔍 用户ID为默认值0，尝试重新获取有效用户信息...');
        
        // 尝试从后端重新获取用户信息
        try {
          const response = await this.$axios.get('/api/auth/current-user-full', {
            withCredentials: true
          });
          
          if (response.data && response.data.code === 200 && response.data.data?.id > 0) {
            // 更新用户信息
            const newUserInfo = {
              ...userInfo,
              id: response.data.data.id,
              username: response.data.data.username || userInfo.username,
              isAdmin: response.data.data.isAdmin || userInfo.isAdmin
            };
            localStorage.setItem('user', JSON.stringify(newUserInfo));
            console.log('✅ 已从后端更新用户ID:', newUserInfo.id);
            // 重新解析用户信息，确保使用最新的ID
            userInfo = newUserInfo;
          } else {
            console.warn('⚠️ 无法从后端获取有效用户ID，但ID为0可以继续尝试加载通知');
            // ID为0时继续尝试加载通知，不直接返回
          }
        } catch (refreshError) {
          console.error('❌ 获取用户信息失败:', refreshError);
          console.warn('⚠️ 获取用户信息失败，但ID为0可以继续尝试加载通知');
          // ID为0时继续尝试加载通知，不直接返回
        }
      }
    } catch (parseError) {
      console.error('❌ 解析用户信息失败:', parseError);
      // 尝试清除错误的用户信息，然后从后端获取
      localStorage.removeItem('user');
      try {
        console.log('🔄 尝试重新获取用户信息...');
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
          console.log('✅ 已重新创建用户信息:', newUserInfo);
          // 重新设置userInfo变量，确保后续代码可以使用
          userInfo = newUserInfo;
        } else {
          console.warn('⚠️ 无法从后端获取有效用户信息');
          this.$message.warning('当前用户状态异常，无法加载通知');
          this.loading = false;
          return;
        }
      } catch (refreshError) {
        console.error('❌ 重新获取用户信息失败:', refreshError);
        this.$message.error('用户信息异常，请重新登录');
        this.loading = false;
        return;
      }
    }
    
    // 确保userInfo和userInfo.id有效，然后才继续处理
    if (!userInfo || !userInfo.id || userInfo.id <= 0) {
      console.error('❌ 用户信息无效，无法继续加载通知');
      this.$message.warning('当前用户状态异常，无法加载通知');
      this.loading = false;
      return;
    }
    
    console.log('✅ 用户信息验证通过，ID:', userInfo.id);
      
      // 构建请求URL - 加载所有通知，然后过滤出评论类型
      const requestUrl = '/api/notifications/all';
      console.log('🌐 请求信息:');
      console.log('  - 请求URL:', requestUrl);
      console.log('  - 请求方法: GET');
      
      // 检查axios配置信息
      console.log('⚙️ Axios配置检查:');
      console.log('  - baseURL:', this.$axios.defaults.baseURL || '默认（通过代理）');
      console.log('  - withCredentials:', this.$axios.defaults.withCredentials);
      console.log('  - timeout:', this.$axios.defaults.timeout, 'ms');
      
      // 构建请求配置
      const requestConfig = {
        headers: {
          'Content-Type': 'application/json',
          'Authorization': token ? `Bearer ${token}` : ''
        },
        withCredentials: true
      };
      console.log('📋 请求配置:', JSON.stringify(requestConfig, null, 2));
      
      try {
        // 发送请求
        console.log('🚀 发送请求...');
        const response = await this.$axios.get(requestUrl, requestConfig);
        
        // 处理响应
        console.log('✅ 加载所有通知成功!');
        console.log('  - 响应状态:', response.status, response.statusText);
        console.log('  - 响应数据:', JSON.stringify(response.data, null, 2));
        
        if (response.data && response.data.code === 200) {
          // 过滤出评论类型的通知
          this.notifications = (response.data.data || []).filter(notification => 
            notification.type === 'comment'
          );
          this.updateUnreadCount();
          console.log('📊 评论通知列表更新:');
          console.log('  - 总数:', this.notifications.length);
          console.log('  - 未读数:', this.unreadCount);
          console.log('  - 已读数:', this.notifications.filter(n => n.isRead).length);
        } else {
          console.warn('⚠️ 加载通知响应异常:', response.data);
          this.$message.warning(response.data?.message || '通知加载异常');
        }
      } catch (error) {
        // 详细错误处理
        console.error('❌ 加载通知失败!');
        
        console.log('📝 错误详情:');
        console.log('  - 错误类型:', error.name);
        console.log('  - 错误消息:', error.message);
        
        if (error.response) {
          // 服务器返回了错误响应
          console.log('  - 响应状态码:', error.response.status);
          console.log('  - 响应状态文本:', error.response.statusText);
          console.log('  - 响应数据:', JSON.stringify(error.response.data, null, 2));
          console.log('  - 响应头:', error.response.headers);
          
          // 处理401未授权错误
          if (error.response.status === 401) {
            console.log('🔒 检测到未授权错误 (401)');
            if (error.response.data && error.response.data.message) {
              console.log('  - 服务器消息:', error.response.data.message);
              this.$message.error('认证失败: ' + error.response.data.message);
            } else {
              this.$message.error('请先登录');
            }
            
            // 清除登录信息
            console.log('🗑️  清除登录信息...');
            localStorage.removeItem('token');
            localStorage.removeItem('username');
            localStorage.removeItem('user');
            localStorage.removeItem('userRole');
            localStorage.removeItem('isAdmin');
            
            // 延迟跳转到登录页
            setTimeout(() => {
              console.log('🔄 跳转到登录页...');
              this.$router.push('/login');
            }, 1500);
          }
          // 处理400错误（用户ID为-1的情况）
          else if (error.response.status === 400) {
            console.warn('检测到400错误，可能是用户ID为-1的问题，跳过通知API调用');
            this.notifications = [];
            this.unreadCount = 0;
            this.$message.info('当前用户类型无法加载通知');
          }
          // 处理500服务器错误
          else if (error.response.status === 500) {
            console.log('💻 服务器内部错误 (500)');
            const errorMsg = error.response.data?.message || '服务器繁忙，请稍后重试';
            this.$message.error('加载通知失败: ' + errorMsg);
          }
          // 其他HTTP错误
          else {
            console.log('📡 其他HTTP错误');
            this.$message.error(`请求失败: ${error.response.status} ${error.response.statusText}`);
          }
        }
        else if (error.request) {
          // 请求已发送但没有收到响应
          console.log('📤 请求已发送但无响应');
          console.log('  - 请求对象:', error.request);
          this.$message.error('网络错误，请检查连接');
        }
        else {
          // 请求配置出错
          console.log('📋 请求配置错误');
          this.$message.error('请求错误: ' + error.message);
        }
        
        // 重置通知列表
        this.notifications = [];
        this.unreadCount = 0;
        console.log('🔄 重置通知列表和未读数');
      } finally {
        console.log('🏁 加载通知操作完成');
        // 无论成功失败都隐藏加载状态
        this.loading = false;
      }
    },
    
    // 更新未读通知数量
    updateUnreadCount() {
      this.unreadCount = this.notifications.filter(notification => !notification.isRead).length;
    },
    
    /**
     * 标记单个通知为已读
     * 功能：将指定的通知标记为已读状态
     * 改进：添加通知父组件更新未读数量的逻辑，确保右上角红点同步清除
     */
    async markAsRead(notificationId) {
      try {
        // 确保用户已登录且有有效的认证信息
        if (!localStorage.getItem('username') || !localStorage.getItem('token')) {
          this.$message.warning('请先登录');
          this.$router.push('/login');
          return;
        }
        
        console.log('🔄 开始标记通知为已读，ID:', notificationId);
        const response = await this.$axios.put(`/api/notifications/read/${notificationId}`);
        
        if (response.data && response.data.code === 200) {
          const notification = this.notifications.find(n => n.id === notificationId);
          if (notification) {
            notification.isRead = true;
            console.log('✅ 通知已成功标记为已读');
          }
          
          // 更新本地未读计数
          this.updateUnreadCount();
          this.$message.success('已标记为已读');
          
          // 关键改进：通知父组件(App.vue)更新未读数量
          // 这确保了右上角的红点通知能够同步更新
          console.log('📢 通知父组件更新未读数量...');
          if (this.$parent && typeof this.$parent.fetchUnreadNotificationCount === 'function') {
            console.log('🔄 调用父组件fetchUnreadNotificationCount方法，强制刷新');
            this.$parent.fetchUnreadNotificationCount(true); // 强制刷新，忽略缓存
          }
        } else {
          console.warn('⚠️ 标记通知失败，响应异常:', response.data);
          this.$message.error(response.data?.message || '标记失败');
        }
      } catch (error) {
        console.error('❌ 标记通知为已读失败:', error);
        if (error.response && error.response.status === 401) {
          this.$message.error('登录已过期，请重新登录');
          this.$router.push('/login');
        } else {
          this.$message.error('标记失败，请稍后重试');
        }
      }
    },
    
    /**
     * 标记所有通知为已读
     * 功能：将当前用户的所有通知标记为已读状态
     * 改进：添加通知父组件更新未读数量的逻辑，确保右上角红点同步清除
     */
    async markAllAsRead() {
      try {
        // 确保用户已登录且有有效的认证信息
        if (!localStorage.getItem('username') || !localStorage.getItem('token')) {
          this.$message.warning('请先登录');
          this.$router.push('/login');
          return;
        }
        
        console.log('🔄 开始标记所有通知为已读...');
        const response = await this.$axios.put('/api/notifications/read-all');
        
        if (response.data && response.data.code === 200) {
          // 更新所有通知的状态
          this.notifications.forEach(notification => {
            notification.isRead = true;
          });
          
          console.log('✅ 所有通知已成功标记为已读');
          
          // 更新本地未读计数
          this.updateUnreadCount();
          this.$message.success('已将所有通知标记为已读');
          
          // 关键改进：通知父组件(App.vue)更新未读数量
          // 这确保了右上角的红点通知能够同步更新
          console.log('📢 通知父组件更新未读数量...');
          if (this.$parent && typeof this.$parent.fetchUnreadNotificationCount === 'function') {
            console.log('🔄 调用父组件fetchUnreadNotificationCount方法');
            this.$parent.fetchUnreadNotificationCount();
          }
        } else {
          console.warn('⚠️ 标记所有通知失败，响应异常:', response.data);
          this.$message.error(response.data.message || '标记失败');
        }
      } catch (error) {
        console.error('❌ 标记所有通知为已读失败:', error);
        this.$message.error('标记失败，请重试');
      }
    },
    
    /**
     * 清空所有通知
     * 功能：删除当前用户的所有通知记录
     * 步骤：
     * 1. 显示确认对话框防止误操作
     * 2. 检查用户登录状态
     * 3. 调用后端API删除所有通知
     * 4. 成功后清空前端通知列表并更新未读数
     */
    async deleteAllNotifications() {
      console.log('📢 开始清空所有通知流程...');
      
      // 显示确认对话框，防止用户误操作
      this.$confirm('确定要清空所有通知吗？此操作不可恢复！', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        console.log('✅ 用户确认清空所有通知');
        
        try {
          // 确保用户已登录且有有效的认证信息
          const token = localStorage.getItem('token');
          const username = localStorage.getItem('username');
          console.log('🔐 登录信息检查:');
          console.log('  - token存在:', !!token);
          console.log('  - username:', username);
          
          if (!token || !username) {
            console.log('🚫 用户未登录，重定向到登录页');
            this.$message.warning('请先登录');
            this.$router.push('/login');
            return;
          }
          
          // 构建请求配置
          const requestConfig = {
            headers: {
              'Content-Type': 'application/json',
              'Authorization': token ? `Bearer ${token}` : ''
            },
            withCredentials: true
          };
          console.log('📋 请求配置:', JSON.stringify(requestConfig, null, 2));
          
          // 调用后端API删除所有通知
          console.log('🚀 发送删除所有通知请求...');
          const response = await this.$axios.delete('/api/notifications/all', requestConfig);
          
          // 处理响应
          console.log('✅ 删除所有通知成功!');
          console.log('  - 响应状态:', response.status, response.statusText);
          console.log('  - 响应数据:', JSON.stringify(response.data, null, 2));
          
          if (response.data && response.data.code === 200) {
            // 清空前端通知列表
            this.notifications = [];
            // 更新未读通知数量
            this.unreadCount = 0;
            // 显示成功消息
            this.$message.success('所有通知已清空');
            console.log('📊 通知列表已清空');
            
            // 关键改进：通知父组件(App.vue)更新未读数量
            // 这确保了右上角的红点通知能够同步清除
            console.log('📢 通知父组件更新未读数量...');
            if (this.$parent && typeof this.$parent.fetchUnreadNotificationCount === 'function') {
              console.log('🔄 调用父组件fetchUnreadNotificationCount方法');
              this.$parent.fetchUnreadNotificationCount();
            }
          } else {
            console.warn('⚠️ 删除通知响应异常:', response.data);
            this.$message.error(response.data?.message || '清空通知失败');
          }
        } catch (error) {
          // 详细错误处理
          console.error('❌ 清空通知失败!');
          
          console.log('📝 错误详情:');
          console.log('  - 错误类型:', error.name);
          console.log('  - 错误消息:', error.message);
          
          if (error.response) {
            // 服务器返回了错误响应
            console.log('  - 响应状态码:', error.response.status);
            console.log('  - 响应状态文本:', error.response.statusText);
            
            // 处理401未授权错误
            if (error.response.status === 401) {
              console.log('🔒 检测到未授权错误 (401)');
              this.$message.error('认证失败，请重新登录');
              
              // 清除登录信息
              console.log('🗑️  清除登录信息...');
              localStorage.removeItem('token');
              localStorage.removeItem('username');
              
              // 跳转到登录页
              setTimeout(() => {
                console.log('🔄 跳转到登录页...');
                this.$router.push('/login');
              }, 1000);
            } else {
              // 其他HTTP错误
              console.log('📡 其他HTTP错误');
              this.$message.error(`请求失败: ${error.response.status} ${error.response.statusText}`);
            }
          }
          else if (error.request) {
            // 请求已发送但没有收到响应
            console.log('📤 请求已发送但无响应');
            this.$message.error('网络错误，请检查连接');
          }
          else {
            // 请求配置出错
            console.log('📋 请求配置错误');
            this.$message.error('请求错误: ' + error.message);
          }
        } finally {
          console.log('🏁 清空通知操作完成');
        }
      }).catch(() => {
        // 用户取消清空操作
        console.log('❌ 用户取消清空所有通知');
        this.$message.info('已取消清空操作');
      });
    },
    
    // 查看相关内容
    async viewRelatedContent(notification) {
      console.log('查看相关内容:', notification);
      
      try {
        // 根据通知类型和关联ID跳转到相应页面
        if (notification.type === 'answer' && notification.relatedId) {
          console.log('跳转到问答页面，ID:', notification.relatedId);
          this.$router.push(`/question/${notification.relatedId}`);
        } else if (notification.type === 'comment' && notification.relatedId) {
          // 评论通知需要特殊处理：先获取评论详情，再根据评论关联的攻略ID跳转
          console.log('处理评论通知，获取评论详情，评论ID:', notification.relatedId);
          
          // 检查通知内容是否包含攻略相关关键词，确定是攻略评论还是问答评论
          const content = notification.content.toLowerCase();
          const isGuideComment = content.includes('攻略') || content.includes('guide');
          
          if (isGuideComment) {
            // 对于攻略评论，先获取评论详情，找到关联的攻略ID
            try {
              const response = await this.$axios.get(`/api/guide/comment/${notification.relatedId}`, {
                withCredentials: true
              });
              
              if (response.data && response.data.guide) {
                const guideId = response.data.guide.id;
                console.log('成功获取评论详情，攻略ID:', guideId);
                this.$router.push(`/guide/${guideId}`);
              } else {
                console.warn('获取评论详情失败或评论未关联攻略');
                // 备选方案：尝试直接使用评论ID作为攻略ID（兼容性处理）
                this.$router.push(`/guide/${notification.relatedId}`);
              }
            } catch (error) {
              console.error('获取评论详情时出错:', error);
              // 错误情况下，尝试直接使用评论ID作为攻略ID（兼容性处理）
              this.$router.push(`/guide/${notification.relatedId}`);
            }
          } else {
            console.log('跳转到问答评论页面，ID:', notification.relatedId);
            this.$router.push(`/question/${notification.relatedId}`);
          }
        } else if (notification.type === 'like' && notification.relatedId) {
          // 对点赞通知也做类似区分
          const content = notification.content.toLowerCase();
          if (content.includes('攻略') || content.includes('guide')) {
            console.log('跳转到攻略详情页面，ID:', notification.relatedId);
            this.$router.push(`/guide/${notification.relatedId}`);
          } else {
            console.log('跳转到问答页面，ID:', notification.relatedId);
            this.$router.push(`/question/${notification.relatedId}`);
          }
        } else {
          console.warn('暂无相关内容或缺少必要信息');
          this.$message.warning('暂无相关内容');
        }
      } catch (error) {
        console.error('处理通知导航时出错:', error);
        this.$message.error('导航失败，请稍后重试');
      }
    },
    
    // 获取通知类型标签
    getNotificationTypeLabel(type) {
      const typeMap = {
        'answer': '回答通知',
        'comment': '评论通知',
        'like': '点赞通知',
        'system': '系统通知',
        'admin': '管理员通知'
      };
      return typeMap[type] || type;
    },
    
    // 格式化时间
    formatTime(timeString) {
      if (!timeString) return '';
      const date = new Date(timeString);
      const now = new Date();
      const diff = now - date;
      const minutes = Math.floor(diff / (1000 * 60));
      const hours = Math.floor(diff / (1000 * 60 * 60));
      const days = Math.floor(diff / (1000 * 60 * 60 * 24));
      
      if (minutes < 60) {
        return `${minutes}分钟前`;
      } else if (hours < 24) {
        return `${hours}小时前`;
      } else if (days < 30) {
        return `${days}天前`;
      } else {
        return date.toLocaleDateString();
      }
    },
    
    // 获取发送者显示名称
    getSenderDisplayName(notification) {
      if (!notification.senderId) return '系统';
      
      // 根据发送者类型显示不同的名称
      if (notification.senderType === 'admin') {
        return '管理员';
      } else if (notification.senderType === 'user') {
        return '用户';
      } else {
        return '系统';
      }
    },
    
    /**
     * 设置WebSocket连接
     * 功能：建立WebSocket连接并订阅实时通知
     * 作用：在消息通知界面实时接收新通知
     */
    setupWebSocketConnection() {
      // 先断开现有连接
      this.disconnectWebSocket();
      
      // 检查用户是否已登录
      const token = localStorage.getItem('token');
      const userInfoStr = localStorage.getItem('user');
      if (!token || !userInfoStr) {
        console.log('🚫 用户未登录，跳过WebSocket连接');
        return;
      }
      
      try {
        const userInfo = JSON.parse(userInfoStr);
        const userId = userInfo.id;
        
        if (!userId || userId <= 0) {
          console.log('❌ 用户ID无效，跳过WebSocket连接');
          return;
        }
        
        console.log('🔌 NotificationCenter开始建立WebSocket连接，用户ID:', userId);
        
        // 创建STOMP客户端
        this.stompClient = new Client({
          webSocketFactory: () => new SockJS('http://localhost:8080/ws-notifications'),
          reconnectDelay: 2000,
          heartbeatIncoming: 1000,
          heartbeatOutgoing: 1000,
          onConnect: () => {
            console.log('✅ NotificationCenter WebSocket连接成功');
            this.websocketConnected = true;
            
            // 订阅用户专属的通知频道
            this.stompClient.subscribe(`/user/${userId}/topic/notifications`, (message) => {
              console.log('📨 NotificationCenter收到实时通知:', message.body);
              this.handleRealTimeNotification(JSON.parse(message.body));
            });
          },
          onStompError: (frame) => {
            console.error('❌ NotificationCenter WebSocket STOMP错误:', frame);
            this.websocketConnected = false;
          },
          onWebSocketClose: () => {
            console.log('🔌 NotificationCenter WebSocket连接关闭');
            this.websocketConnected = false;
          },
          onDisconnect: () => {
            console.log('🔌 NotificationCenter WebSocket连接断开');
            this.websocketConnected = false;
          }
        });
        
        // 激活客户端
        this.stompClient.activate();
        
      } catch (error) {
        console.error('❌ NotificationCenter WebSocket连接失败:', error);
      }
    },
    
    /**
     * 处理实时通知
     * 功能：处理从WebSocket接收到的实时通知
     * 作用：在消息通知界面自动更新消息列表
     */
    handleRealTimeNotification(notificationData) {
      console.log('📨 NotificationCenter处理实时通知数据:', notificationData);
      
      if (notificationData.type === 'notification') {
        const notification = notificationData.notification;
        
        // 只处理评论类型的通知
        if (notification.type === 'comment') {
          console.log('💬 收到新的评论通知，添加到消息列表');
          
          // 将新通知添加到列表开头
          this.notifications.unshift(notification);
          
          // 更新未读数量
          if (!notification.isRead) {
            this.unreadCount += 1;
          }
          
          // 显示新通知提示
          this.showNewNotificationAlert(notification);
          
          console.log('✅ 评论通知列表已更新，当前总数:', this.notifications.length, '未读数:', this.unreadCount);
        }
      }
    },
    
    /**
     * 显示新通知提示
     * 功能：在收到新通知时显示提示信息
     */
    showNewNotificationAlert(notification) {
      const message = `收到新评论通知: ${notification.content}`;
      this.$message({
        message: message,
        type: 'info',
        duration: 3000,
        showClose: true
      });
    },
    
    /**
     * 断开WebSocket连接
     * 功能：安全地断开WebSocket连接
     */
    disconnectWebSocket() {
      if (this.stompClient) {
        try {
          this.stompClient.deactivate();
          console.log('🔌 NotificationCenter WebSocket连接已断开');
        } catch (error) {
          console.error('❌ 断开WebSocket连接失败:', error);
        }
        this.stompClient = null;
      }
      this.websocketConnected = false;
    },
    
    /**
     * 启动轮询机制
     * 功能：每隔1秒轮询服务器检查新通知
     * 作用：作为WebSocket的备用方案，确保通知实时性
     */
    startPolling() {
      console.log('🔄 NotificationCenter 启动轮询机制');
      
      // 清除现有轮询
      this.stopPolling();
      
      // 设置轮询间隔为1秒
      this.pollingInterval = setInterval(() => {
        if (this.pollingEnabled) {
          this.pollForNewNotifications();
        }
      }, 1000);
      
      console.log('✅ 轮询机制已启动，间隔: 1000ms');
    },
    
    /**
     * 停止轮询机制
     * 功能：安全停止轮询
     */
    stopPolling() {
      if (this.pollingInterval) {
        clearInterval(this.pollingInterval);
        this.pollingInterval = null;
        console.log('🛑 NotificationCenter 轮询机制已停止');
      }
    },
    
    /**
     * 轮询检查新通知
     * 功能：检查是否有新通知需要加载
     * 优化：避免重复加载，只在有新通知时更新
     */
    async pollForNewNotifications() {
      try {
        // 检查用户登录状态
        const token = localStorage.getItem('token');
        const userInfoStr = localStorage.getItem('user');
        
        if (!token || !userInfoStr) {
          console.log('🚫 轮询: 用户未登录，跳过检查');
          return;
        }
        
        // 获取当前未读数量
        const response = await this.$axios.get('/api/notifications/unread', {
          withCredentials: true
        });
        
        const currentUnreadCount = response.data.data || 0;
        
        // 如果未读数量有变化，重新加载通知列表
        if (currentUnreadCount !== this.unreadCount) {
          console.log('🔄 轮询检测到未读数量变化:', {
            当前未读: currentUnreadCount,
            之前未读: this.unreadCount
          });
          
          // 重新加载通知列表
          await this.loadAllNotifications();
          
          console.log('✅ 轮询: 通知列表已更新');
        } else {
          console.log('📊 轮询: 未读数量无变化，跳过更新');
        }
        
      } catch (error) {
        console.error('❌ 轮询检查失败:', error);
        
        // 如果是401错误，可能是token过期，停止轮询
        if (error.response && error.response.status === 401) {
          console.log('🔐 轮询: 用户认证失败，停止轮询');
          this.stopPolling();
        }
      }
    }
  }
};
</script>

<style scoped>
.notification-center {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.notification-center h1 {
  font-size: 24px;
  margin-bottom: 20px;
  color: #333;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #e8e8e8;
}

.notification-actions {
  display: flex;
  gap: 10px;
}

.delete-all-btn {
  background-color: #ff4d4f;
  border-color: #ff4d4f;
}

.delete-all-btn:hover {
  background-color: #ff7875;
  border-color: #ff7875;
}

.notification-stats {
  font-size: 14px;
  color: #666;
}

.unread-count {
  color: #ff4d4f;
  margin-left: 10px;
  font-weight: bold;
}

.notification-list {
  margin-top: 20px;
}

.notification-item {
  padding: 15px;
  margin-bottom: 10px;
  border-radius: 4px;
  border: 1px solid #e8e8e8;
  transition: all 0.3s;
  background-color: #fff;
}

.notification-item:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.notification-item.unread {
  background-color: #e6f7ff;
  border-left: 4px solid #1890ff;
}

.notification-content {
  display: flex;
  flex-direction: column;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  border-bottom: none;
  padding-bottom: 0;
}

.notification-type {
  font-weight: bold;
  color: #1890ff;
}

.notification-time {
  font-size: 12px;
  color: #999;
}

.notification-body {
  margin-bottom: 10px;
}

.notification-body p {
  margin: 0 0 5px 0;
  color: #333;
}

.notification-sender {
  font-size: 12px;
  color: #999;
}

.notification-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.empty-notifications {
  text-align: center;
  padding: 40px;
  color: #999;
  font-size: 16px;
}

.mark-all-btn {
  background-color: #1890ff;
  border-color: #1890ff;
}

.mark-all-btn:hover {
  background-color: #40a9ff;
  border-color: #40a9ff;
}
</style>