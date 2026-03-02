<template>
  <div class="private-chat">
    <div class="chat-container">
      <!-- 左侧：对话列表 -->
      <div class="conversation-list">
        <div class="conversation-header">
          <h2>私聊</h2>
          <el-button type="primary" size="small" @click="showUserSelector = true">
            发起聊天
          </el-button>
        </div>
        
        <div class="conversation-search">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索对话"
            prefix-icon="el-icon-search"
            size="small"
            clearable
          />
        </div>
        
        <div class="conversations">
          <div 
            v-for="conversation in filteredConversations"
            :key="conversation.otherUser.id"
            class="conversation-item"
            :class="{ active: activeConversation && activeConversation.otherUser.id === conversation.otherUser.id }"
            @click="selectConversation(conversation)"
          >
            <div class="conversation-avatar">
              <el-avatar :src="conversation.otherUser.avatarUrl" :size="40">
                {{ conversation.otherUser.username.charAt(0).toUpperCase() }}
              </el-avatar>
            </div>
            <div class="conversation-info">
              <div class="conversation-header">
                <span class="username">{{ conversation.otherUser.username }}</span>
                <span class="time">{{ formatTime(conversation.lastMessageTime) }}</span>
              </div>
              <div class="conversation-preview">
                <span class="message-preview">{{ getMessagePreview(conversation.latestMessage) }}</span>
                <span v-if="conversation.unreadCount > 0" class="unread-badge">
                  {{ conversation.unreadCount }}
                </span>
              </div>
            </div>
          </div>
          
          <div v-if="filteredConversations.length === 0" class="empty-conversations">
            <p>暂无对话记录</p>
            <el-button type="text" @click="showUserSelector = true">
              开始第一个对话
            </el-button>
          </div>
        </div>
      </div>
      
      <!-- 右侧：聊天界面 -->
      <div class="chat-interface">
        <div v-if="activeConversation" class="chat-window">
          <!-- 聊天头部 -->
          <div class="chat-header">
            <div class="chat-partner">
              <el-avatar :src="activeConversation.otherUser.avatarUrl" :size="40">
                {{ activeConversation.otherUser.username.charAt(0).toUpperCase() }}
              </el-avatar>
              <div class="partner-info">
                <span class="username">{{ activeConversation.otherUser.username }}</span>
                <span class="online-status" :class="{ online: isUserOnline(activeConversation.otherUser.id) }">
                  {{ isUserOnline(activeConversation.otherUser.id) ? '在线' : '离线' }}
                </span>
              </div>
            </div>
            <div class="chat-actions">
              <el-button type="text" icon="el-icon-more" @click="showChatActions"></el-button>
            </div>
          </div>
          
          <!-- 消息列表 -->
          <div class="messages-container" ref="messagesContainer">
            <div class="messages">
              <div 
                v-for="message in messages"
                :key="message.id"
                class="message-item"
                :class="{ 'own-message': message.senderId === currentUser.id, 'other-message': message.senderId !== currentUser.id }"
              >
                <div class="message-avatar">
                  <el-avatar 
                    :src="getMessageAvatar(message)" 
                    :size="32"
                  >
                    {{ getMessageUsername(message).charAt(0).toUpperCase() }}
                  </el-avatar>
                </div>
                <div class="message-content">
                  <div class="message-bubble">
                    <div v-html="renderMessageContent(message.content)"></div>
                    <div class="message-meta">
                      <span class="time">{{ formatMessageTime(message.sendTime) }}</span>
                      <span v-if="message.senderId === currentUser.id" class="read-status">
                        已发送
                      </span>
                    </div>
                  </div>
                </div>
              </div>
              
              <div v-if="messages.length === 0" class="empty-messages">
                <p>还没有消息，开始聊天吧！</p>
              </div>
            </div>
          </div>
          
          <!-- 消息输入框 
           @keydown.enter.exact.prevent="sendMessage" 按回车键触发
           @click="sendMessage" 点击按钮触发
          
          -->
          <div class="message-input">
            <div class="input-container">
              <el-input
                v-model="newMessage"
                type="textarea"
                :rows="2"
                placeholder="输入消息..."
                :maxlength="1000"
                show-word-limit
                @keydown.enter.exact.prevent="sendMessage"
              />
              <el-button 
                type="primary" 
                :disabled="!newMessage.trim()"
                @click="sendMessage"
                class="send-btn"
              >
                发送
              </el-button>
            </div>
          </div>
        </div>
        
        <div v-else class="no-conversation-selected">
          <div class="welcome-message">
            <h3>欢迎使用私聊功能</h3>
            <p>选择一个对话开始聊天，或者发起新的聊天</p>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 用户选择对话框 -->
    <el-dialog
      title="选择聊天对象"
      :visible.sync="showUserSelector"
      width="500px"
      :before-close="handleCloseUserSelector"
    >
      <user-selector
        v-if="showUserSelector"
        @user-selected="handleUserSelected"
        @cancel="showUserSelector = false"
      />
    </el-dialog>
  </div>
</template>

<script>
import request from '@/utils/request';
import UserSelector from '@/components/UserSelector.vue';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

export default {
  name: 'PrivateChat',
  components: {
    UserSelector
  },
  data() {//存储需要动态变化的数据
    return {
      conversations: [],//存所有聊天对象;conversations 是 "通讯录"（记着所有能聊的人）
      activeConversation: null,//当前选中的聊天对话，包含对方用户信息
      messages: [],//messages 是 "和当前人聊的内容"
      newMessage: '',//在输入框里输入的文字;是 "刚写好还没发的话"。
      searchKeyword: '',
      showUserSelector: false,
      currentUser: {},
      onlineUsers: [],
      loading: false
    };
  },
  computed: {
    filteredConversations() {
      if (!this.searchKeyword) {
        return this.conversations;
      }
      const keyword = this.searchKeyword.toLowerCase();
      return this.conversations.filter(conversation => 
        conversation.otherUser.username.toLowerCase().includes(keyword)
      );
    }
  },
  mounted() {
    console.log('📢 PrivateChat 组件已挂载');
    this.loadCurrentUser();
    this.loadConversations();
    this.setupWebSocketConnection();
    this.startPolling();
  },
  beforeDestroy() {
    this.stopPolling();
    this.disconnectWebSocket();
  },
  watch: {
    async activeConversation(newConversation) {
      if (newConversation) {
        await this.loadMessages(newConversation.otherUser.id);
        this.scrollToBottom();
      }
    }
  },
  methods: {//处理各种 “动作”
    async loadCurrentUser() {
      try {
        const userInfo = localStorage.getItem('user');
        if (userInfo) {
          this.currentUser = JSON.parse(userInfo);
        }
      } catch (error) {
        console.error('加载当前用户信息失败:', error);
        this.$message.error('加载用户信息失败');
      }
    },
    
    async loadConversations() {
      try {
        this.loading = true;
       
        
        const response = await request.get('/api/private-messages/conversations');
        
        console.log('📥 加载对话列表API响应:', response);
        
        if (response && response.code === 200) {
          // 修正：对话列表数据在response.conversations中，而不是response.data.data.conversations
          this.conversations = response.conversations || response.data?.conversations || [];
          console.log('✅ 加载对话列表成功:', this.conversations.length, '对话内容:', this.conversations);
          
          // 如果有对话，自动选择第一个
          if (this.conversations.length > 0 && !this.activeConversation) {
            this.selectConversation(this.conversations[0]);
          }
        } else {
          const errorMessage = response?.message || '加载对话列表失败';
          this.$message.error(errorMessage);
        }
      } catch (error) {
        if (error.response && error.response.status === 401) {
          this.$message.warning('请先登录');
          this.$router.push('/login');
        } else {
          this.$message.error('加载对话列表失败: ' + (error.response?.data?.message || error.message));
        }
      } finally {
        this.loading = false;
      }
    },
    
    async loadMessages(targetUserId) {
      try {
        const response = await request.get(`/api/private-messages/conversation/${targetUserId}`);
        

        
        if (response && response.code === 200) {
           // 修正：消息数据可能在response.messages中
           const messages = response.messages || response.data?.messages || [];
           this.messages = messages;
        
         } else {
           const errorMessage = response?.message || '加载消息失败';
           
           this.$message.error(errorMessage);
         }
      } catch (error) {
        
        this.$message.error('加载消息失败: ' + (error.response?.data?.message || error.message));
      }
    },
    
    async sendMessage() {//点击发送按钮
      if (!this.newMessage.trim() || !this.activeConversation) {//阻止无效发送;发送前先 “把关”，如果不符合条件就不继续执行（相当于发微信时，没输入文字或没选聊天对象，点发送也没用）
        //this.newMessage.trim()：检查输入框内容。this.newMessage 是输入的文字，trim() 去掉前后空格，如果结果为空（比如只输入了空格），则条件成立。
       // !this.activeConversation：检查是否选了聊天对象。this.activeConversation 存着当前聊天的人，如果没选（值为 null），则条件成立。
//return：如果上面两个条件有一个成立，就退出函数，不执行后面的发送逻辑。
        return;
      }
      
      try {
     
        // const 声明一个常量response，用于存储API响应结果
        // await 等待异步操作完成，直到request.post执行完毕
        // request.post 调用封装的HTTP POST请求方法
        // '/api/private-messages/send' 是后端API的URL路径
        const response = await request.post('/api/private-messages/send', null, {
          //request.post(...)：用 POST 方法调用后端接口
          ///api/private-messages/send：后端接收私聊消息的接口地址
          params: {
            receiverId: this.activeConversation.otherUser.id,
            content: this.newMessage.trim()
          }
        });
        
        
        
        // 检查响应结构，可能是 response.code 而不是 response.data.code
        if ((response.status === 200) || (response.code === 200) || (response.data && response.data.code === 200)) {
  
          // 清空输入框
          this.newMessage = '';
          // 重新加载消息
          await this.loadMessages(this.activeConversation.otherUser.id);
          // 滚动到底部
          this.scrollToBottom();
          // 重新加载对话列表以更新最新消息
          await this.loadConversations();
        } else {
         
          this.$message.error(response.message || response.data?.message || '发送消息失败');
        }
      } catch (error) {
        console.error('❌ 发送消息失败:', {
          error: error,
          message: error.message,
          response: error.response
        });
        this.$message.error('发送消息失败: ' + (error.response?.data?.message || error.message));
      }
    },
    
    selectConversation(conversation) {
      this.activeConversation = conversation;
    },
    
    handleUserSelected(user) {
      // 检查是否已经存在该用户的对话
      const existingConversation = this.conversations.find(
        conv => conv.otherUser.id === user.id
      );
      
      if (existingConversation) {
        this.selectConversation(existingConversation);
      } else {
        // 创建新的对话
        const newConversation = {
          otherUser: user,
          latestMessage: null,
          unreadCount: 0,
          lastMessageTime: new Date()
        };
        this.conversations.unshift(newConversation);
        this.selectConversation(newConversation);
      }
      
      this.showUserSelector = false;
    },
    
    handleCloseUserSelector(done) {
      this.showUserSelector = false;
      done();
    },
    
    getMessagePreview(message) {
      if (!message) return '暂无消息';
      const content = message.content;
      return content.length > 30 ? content.substring(0, 30) + '...' : content;
    },
    
    formatTime(time) {
      if (!time) return '';
      const date = new Date(time);
      const now = new Date();
      const diff = now - date;
      
      if (diff < 60000) return '刚刚';
      if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前';
      if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前';
      if (diff < 604800000) return Math.floor(diff / 86400000) + '天前';
      
      return date.toLocaleDateString();
    },
    
    formatMessageTime(time) {
      if (!time) return '';
      const date = new Date(time);
      return date.toLocaleTimeString('zh-CN', { 
        hour: '2-digit', 
        minute: '2-digit' 
      });
    },
    
    getMessageAvatar(message) {
      let avatarUrl = '';
      
      if (message.senderId === this.currentUser.id) {
        // 当前登录用户头像 - 优先从localStorage获取
        const userInfoStr = localStorage.getItem('user');
        if (userInfoStr) {
          try {
            const userInfo = JSON.parse(userInfoStr);
            avatarUrl = userInfo.avatar || '';
          } catch (error) {
            console.warn('从localStorage获取当前用户头像失败:', error);
          }
        }
        
        // 如果localStorage中没有，使用currentUser.avatarUrl
        if (!avatarUrl && this.currentUser.avatarUrl) {
          avatarUrl = this.currentUser.avatarUrl;
        }
      } else if (this.activeConversation) {
        // 对方用户头像 - 从对话信息中获取
        avatarUrl = this.activeConversation.otherUser.avatarUrl || '';
      }
      
      // 使用getImageUrl方法处理头像路径，确保路径正确
      return this.getImageUrl(avatarUrl);
    },
    
    getMessageUsername(message) {
      if (message.senderId === this.currentUser.id) {
        return this.currentUser.username;
      } else if (this.activeConversation) {
        return this.activeConversation.otherUser.username;
      }
      return '';
    },
    
    isUserOnline(userId) {
      return this.onlineUsers.includes(userId);
    },
    
    /**
     * 渲染消息内容，处理URL解码和Markdown链接
     * 功能：将URL编码的消息内容解码并渲染为可点击的链接
     */
    renderMessageContent(content) {
      if (!content) return '';
      
      // 1. 先进行URL解码
      let decodedContent = decodeURIComponent(content);
      
      // 2. 处理Markdown格式的链接 [文本](URL)
      // 将 [文本](URL) 格式转换为 <a href="URL" target="_blank">文本</a>
      const linkRegex = /\[([^\]]+)\]\(([^)]+)\)/g;
      decodedContent = decodedContent.replace(linkRegex, (match, text, url) => {
        // 清理URL中的多余引号和空格
        let cleanUrl = url.trim();
        // 移除URL开头和结尾的多余引号
        cleanUrl = cleanUrl.replace(/^["']|["']$/g, '');
        // 确保URL是有效的
        if (!cleanUrl.startsWith('http://') && !cleanUrl.startsWith('https://')) {
          // 如果是相对路径，添加协议和域名
          cleanUrl = window.location.origin + cleanUrl;
        }
        return `<a href="${cleanUrl}" target="_blank" class="message-link">${text}</a>`;
      });
      
      // 3. 处理纯文本中的URL，将其转换为可点击链接
      const urlRegex = /(https?:\/\/[^\s]+)/g;
      decodedContent = decodedContent.replace(urlRegex, (match, url) => {
        let cleanUrl = url.trim();
        cleanUrl = cleanUrl.replace(/^["']|["']$/g, '');
        return `<a href="${cleanUrl}" target="_blank" class="message-link">${cleanUrl}</a>`;
      });
      
      // 4. 将换行符转换为HTML换行
      decodedContent = decodedContent.replace(/\n/g, '<br>');
      
      return decodedContent;
    },
    
    scrollToBottom() {
      this.$nextTick(() => {
        const container = this.$refs.messagesContainer;
        if (container) {
          container.scrollTop = container.scrollHeight;
        }
      });
    },
    
    showChatActions() {
      // 显示聊天操作菜单
      this.$message.info('聊天操作功能开发中');
    },
    
    startPolling() {
      // 每30秒检查一次新消息
      this.pollingInterval = setInterval(async () => {
        if (this.activeConversation) {
          await this.loadMessages(this.activeConversation.otherUser.id);
        }
        await this.loadConversations();
      }, 30000);
    },
    
    stopPolling() {
      if (this.pollingInterval) {
        clearInterval(this.pollingInterval);
      }
    },
    
    /**
     * 设置WebSocket连接
     * 功能：建立WebSocket连接并订阅私聊消息
     * 作用：实现私聊消息的实时接收
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
        
        console.log('🔌 PrivateChat开始建立WebSocket连接，用户ID:', userId);
        
        // 创建STOMP客户端
        this.stompClient = new Client({
          webSocketFactory: () => new SockJS('http://localhost:8080/ws-notifications'),
          reconnectDelay: 2000,
          heartbeatIncoming: 1000,
          heartbeatOutgoing: 1000,
          onConnect: () => {
            console.log('✅ PrivateChat WebSocket连接成功');
            this.websocketConnected = true;
            
            // 订阅用户专属的私聊消息频道
            this.stompClient.subscribe(`/user/${userId}/topic/private-messages`, (message) => {
              console.log('💬 PrivateChat收到实时私聊消息:', message.body);
              this.handleRealTimePrivateMessage(JSON.parse(message.body));
            });
          },
          onStompError: (frame) => {
            console.error('❌ PrivateChat WebSocket STOMP错误:', frame);
            this.websocketConnected = false;
          },
          onWebSocketClose: () => {
            console.log('🔌 PrivateChat WebSocket连接关闭');
            this.websocketConnected = false;
          },
          onDisconnect: () => {
            console.log('🔌 PrivateChat WebSocket连接断开');
            this.websocketConnected = false;
          }
        });
        
        // 激活客户端
        this.stompClient.activate();
        
      } catch (error) {
        console.error('❌ PrivateChat WebSocket连接失败:', error);
      }
    },
    
    /**
     * 断开WebSocket连接
     * 功能：断开WebSocket连接并清理资源
     */
    disconnectWebSocket() {
      if (this.stompClient) {
        try {
          this.stompClient.deactivate();
          console.log('🔌 PrivateChat WebSocket连接已断开');
        } catch (error) {
          console.error('❌ PrivateChat WebSocket断开失败:', error);
        }
        this.stompClient = null;
      }
      this.websocketConnected = false;
    },
    
    /**
     * 处理实时私聊消息
     * 功能：处理从WebSocket接收到的实时私聊消息
     * 作用：在私聊界面自动更新消息列表
     */
    handleRealTimePrivateMessage(messageData) {
      console.log('💬 PrivateChat处理实时私聊消息数据:', messageData);
      
      if (messageData.type === 'private_message') {
        // 构建消息对象，使用WebSocket发送的字段
        const message = {
          id: messageData.messageId,
          senderId: messageData.senderId,
          receiverId: messageData.receiverId,
          content: messageData.content,
          messageType: messageData.messageType || 'TEXT',
          sendTime: messageData.sendTime
        };
        
        // 检查消息是否属于当前活跃对话
        if (this.activeConversation && 
            this.activeConversation.otherUser.id === message.senderId) {
          console.log('💬 收到当前对话的新消息，添加到消息列表');
          
          // 将新消息添加到列表末尾
          this.messages.push(message);
          
          // 滚动到底部显示新消息
          this.scrollToBottom();
          
          // 显示新消息提示 - 注释掉，让App.vue统一处理
          // this.showNewMessageAlert(message);
        } else {
          console.log('💬 收到其他对话的新消息，更新对话列表');
          
          // 更新对话列表中的最新消息
          this.updateConversationWithNewMessage(message);
        }
        
        // 播放新消息提示音（可选）
        this.playMessageSound();
      }
    },
    
    /**
     * 更新对话列表中的最新消息
     * 功能：根据新消息更新对话列表
     */
    updateConversationWithNewMessage(message) {
      const conversationIndex = this.conversations.findIndex(
        conv => conv.otherUser.id === message.senderId
      );
      
      if (conversationIndex !== -1) {
        // 更新现有对话
        const conversation = this.conversations[conversationIndex];
        conversation.latestMessage = message;
        conversation.lastMessageTime = message.createTime;
        conversation.unreadCount += 1;
        
        // 将对话移到列表顶部
        this.conversations.splice(conversationIndex, 1);
        this.conversations.unshift(conversation);
        
        console.log('✅ 对话列表已更新，新消息来自:', conversation.otherUser.username);
      } else {
        // 创建新对话
        console.log('💬 收到新用户的私聊消息，需要加载用户信息');
        this.loadConversations(); // 重新加载对话列表
      }
    },
    
    /**
     * 显示新消息提示
     * 功能：在收到新消息时显示提示信息
     */
    showNewMessageAlert(message) {
      const senderName = this.getMessageUsername(message);
      const preview = message.content.length > 20 ? 
        message.content.substring(0, 20) + '...' : message.content;
      
      this.$message({
        message: `💬 ${senderName}: ${preview}`,
        type: 'info',
        duration: 3000,
        showClose: true
      });
    },
    
    /**
     * 播放新消息提示音
     * 功能：播放新消息到达的提示音
     */
    playMessageSound() {
      // 简单的浏览器提示音实现
      try {
        const audioContext = new (window.AudioContext || window.webkitAudioContext)();
        const oscillator = audioContext.createOscillator();
        const gainNode = audioContext.createGain();
        
        oscillator.connect(gainNode);
        gainNode.connect(audioContext.destination);
        
        oscillator.frequency.value = 800;
        oscillator.type = 'sine';
        
        gainNode.gain.setValueAtTime(0.3, audioContext.currentTime);
        gainNode.gain.exponentialRampToValueAtTime(0.01, audioContext.currentTime + 0.15);
        
        oscillator.start(audioContext.currentTime);
        oscillator.stop(audioContext.currentTime + 0.15);
      } catch (error) {
        console.log('🔊 音频播放失败，跳过提示音');
      }
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
        console.log('🔄 PrivateChat尝试重新连接WebSocket');
        this.setupWebSocketConnection();
      }, 5000);
    },
    
    /**
     * 获取图片URL
     * 功能：正确处理头像路径，确保路径正确
     * 作用：与问答专区保持一致的头像加载逻辑
     */
    getImageUrl(url) {
      if (!url) return '/assets/avatars/default-avatar.svg';
      
      // 如果是绝对URL（包含http://或https://），直接返回
      if (url.startsWith('http://') || url.startsWith('https://')) {
        return url;
      }
      
      // 关键修复：对于相对路径，直接返回，webpack开发服务器已配置静态资源服务
      // /assets/avatars/路径现在由webpack devServer直接提供服务
      return url;
    }
  }
};
</script>

<style scoped>
.private-chat {
  height: calc(100vh - 100px);
  background: #f5f5f5;
}

.chat-container {
  display: flex;
  height: 100%;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.conversation-list {
  width: 300px;
  border-right: 1px solid #e6e6e6;
  display: flex;
  flex-direction: column;
}

.conversation-header {
  padding: 16px;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.conversation-header h2 {
  margin: 0;
  font-size: 18px;
  color: #333;
}

.conversation-search {
  padding: 12px;
  border-bottom: 1px solid #e6e6e6;
}

.conversations {
  flex: 1;
  overflow-y: auto;
}

.conversation-item {
  display: flex;
  padding: 12px;
  cursor: pointer;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.2s;
}

.conversation-item:hover {
  background-color: #f8f9fa;
}

.conversation-item.active {
  background-color: #e3f2fd;
  border-left: 3px solid #1890ff;
}

.conversation-avatar {
  margin-right: 12px;
}

.conversation-info {
  flex: 1;
  min-width: 0;
}

.conversation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.username {
  font-weight: 600;
  color: #333;
}

.time {
  font-size: 12px;
  color: #999;
}

.conversation-preview {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.message-preview {
  font-size: 13px;
  color: #666;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
}

.unread-badge {
  background: #ff4d4f;
  color: white;
  border-radius: 10px;
  padding: 2px 6px;
  font-size: 12px;
  min-width: 18px;
  text-align: center;
}

.empty-conversations {
  text-align: center;
  padding: 40px 20px;
  color: #999;
}

.chat-interface {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.chat-window {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.chat-header {
  padding: 16px;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chat-partner {
  display: flex;
  align-items: center;
}

.partner-info {
  margin-left: 12px;
}

.partner-info .username {
  display: block;
  font-weight: 600;
  font-size: 16px;
}

.online-status {
  font-size: 12px;
  color: #999;
}

.online-status.online {
  color: #52c41a;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  height: calc(100vh - 200px); /* 固定高度，类似微信的聊天区域 */
  max-height: 600px; /* 最大高度限制 */
}

.messages {
  max-width: 800px;
  margin: 0 auto;
  min-height: 100%; /* 确保消息容器有最小高度 */
}

.message-item {
  display: flex;
  margin-bottom: 16px;
}

.message-item.own-message {
  flex-direction: row-reverse;
}

.message-avatar {
  margin: 0 8px;
}

.message-content {
  max-width: 70%;
}

.message-bubble {
  background: #f0f0f0;
  padding: 12px 16px;
  border-radius: 18px;
  position: relative;
}

.own-message .message-bubble {
  background: #1890ff;
  color: white;
}

.message-bubble p {
  margin: 0 0 8px 0;
  word-wrap: break-word;
}

.message-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  opacity: 0.8;
}

.message-input {
  padding: 16px;
  border-top: 1px solid #e6e6e6;
}

.input-container {
  display: flex;
  align-items: flex-end;
  gap: 12px;
}

.input-container .el-textarea {
  flex: 1;
}

.send-btn {
  height: 56px;
}

.no-conversation-selected {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
}

.welcome-message {
  text-align: center;
}

.welcome-message h3 {
  margin-bottom: 8px;
  color: #666;
}

.empty-messages {
  text-align: center;
  padding: 40px;
  color: #999;
}

/* 消息链接样式 */
.message-link {
  color: #1890ff;
  text-decoration: underline;
  cursor: pointer;
}

.message-link:hover {
  color: #40a9ff;
  text-decoration: none;
}

.own-message .message-link {
  color: #e6f7ff;
}

.own-message .message-link:hover {
  color: #ffffff;
}
</style>