<template>
  <div class="guide-detail-container">
    <!-- 返回按钮 -->
    <div class="back-button-container">
      <el-button @click="goBack" icon="el-icon-arrow-left">返回</el-button>
    </div>
    
    <!-- 攻略内容区域 -->
    <div class="guide-content-wrapper">
      <!-- 攻略头部信息 -->
      <div class="guide-header">
        <!-- 攻略封面图片 -->
        <div class="guide-cover" v-if="guideInfo?.coverImageUrl">
          <img :src="getImageUrl(guideInfo.coverImageUrl)" :alt="guideInfo.title" class="cover-image">
        </div>
        
        <!-- 编辑按钮区域 -->
        <div class="edit-button-container" v-if="canEdit">
          <el-button type="primary" icon="el-icon-edit" @click="handleEditGuide">
            编辑攻略
          </el-button>
        </div>
        
        <h1 class="guide-title">{{ guideInfo?.title || '加载中...' }}</h1>
        <div class="guide-meta">
          <span class="author"><i class="el-icon-user"></i> {{ guideInfo?.author?.username || '未知作者' }}</span>
          <span class="time"><i class="el-icon-time"></i> {{ formatDate(guideInfo?.createTime) }}</span>
          <span class="game"><i class="el-icon-gamepad"></i> {{ guideInfo?.game?.name || '未知游戏' }}</span>
          <div class="stats">
            <span class="views"><i class="el-icon-view"></i> {{ guideInfo?.views || 0 }} 浏览</span>
            <span class="likes"><i class="el-icon-thumb"></i> {{ guideInfo?.likes || 0 }} 点赞</span>
          </div>
        </div>
      </div>
      
      <!-- 攻略正文 -->
      <div class="guide-content">
        <div v-if="loading" class="loading-state">
          <i class="el-icon-loading"></i>
          <p>加载中...</p>
        </div>
        <div v-else-if="!guideInfo" class="error-state">
          <el-empty description="攻略不存在或已被删除"></el-empty>
        </div>
        <div v-else class="content-text" v-html="guideInfo.content"></div>
      </div>
      
      <!-- 互动区域 -->
      <div class="interaction-area">
        <el-button type="primary" icon="el-icon-thumb" @click="handleLike" :loading="liking">
          {{ isLiked ? '已点赞' : '点赞' }} ({{ guideInfo?.likes || 0 }})
        </el-button>
        <!-- 分享按钮 -->
        <el-button type="success" icon="el-icon-share" @click="handleShare" style="margin-left: 10px;">
          分享
        </el-button>
        <!-- 管理员删除按钮 -->
        <!-- 只有管理员才能看到并使用删除按钮 -->
        <el-button type="danger" icon="el-icon-delete" @click="handleDeleteGuide" style="margin-left: 10px;" v-if="isAdmin">
          删除攻略(管理员操作)
        </el-button>
      </div>
      
      <!-- 评论区 - 仿照问题回答功能重新实现 -->
      <div class="comment-section">
        <h3 class="comment-section-title">评论区 ({{ commentList.length > 0 ? commentList.length : 0 }})</h3>
        
        <!-- 评论输入框 -->
        <div class="comment-input-area" v-if="currentUser">
          <el-input
            v-model="commentContent"
            type="textarea"
            :rows="3"
            placeholder="写下你的评论..."
            resize="none"
          ></el-input>
          <div class="comment-input-footer">
            <el-button type="primary" @click="submitComment" :loading="submittingComment">
              发表评论
            </el-button>
          </div>
        </div>
        <div v-else class="login-tip">
          <el-alert
            title="请先登录后再发表评论"
            type="info"
            :closable="false"
          ></el-alert>
        </div>
        
        <!-- 评论列表 - 仿照问题回答功能的样式 -->
        <div class="comment-list">
          <div v-if="loadingComments" class="loading-comments">
            <el-icon><i class="el-icon-loading"></i></el-icon>
            <span>加载评论中...</span>
          </div>
          <div v-else-if="commentList.length === 0" class="no-comments">
            <el-empty description="暂无评论，快来发表第一条评论吧"></el-empty>
          </div>
          <div v-else>
            <!-- 评论项 - 仿照问题回答样式 -->
            <div v-for="comment in commentList" :key="comment.id" class="comment-answer-item">
              <!-- 评论头部信息 -->
              <div class="comment-header">
                <span class="comment-author">{{ comment.author?.username || '匿名用户' }}</span>
                <span class="comment-time">{{ formatDate(comment.createTime) }}</span>
                
                <!-- 评论操作按钮 -->
                <div class="comment-actions">
                  <button type="button" class="el-button el-button--text el-button--small" @click="handleCommentLike(comment)">
                    <i class="el-icon-thumb"></i>
                    <span>{{ comment.isLiked ? '已赞' : '赞' }} ({{ comment.likes || 0 }})</span>
                  </button>
                </div>
              </div>
              
              <!-- 评论内容 -->
              <div class="comment-content">{{ comment.content }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 分享对话框 -->
    <el-dialog
      title="分享攻略"
      :visible.sync="shareDialogVisible"
      width="600px"
      :before-close="handleShareDialogClose">
      <div class="share-dialog-content">
        <!-- 分享方式选择 -->
        <div class="share-method">
          <h4>选择分享方式：</h4>
          <el-radio-group v-model="shareMethod">
            <el-radio label="private">私聊分享</el-radio>
          </el-radio-group>
        </div>
        
        <!-- 私聊联系人选择 -->
        <div class="private-share-section" v-if="shareMethod === 'private'">
          <h4>选择联系人：</h4>
          <div class="contact-list">
            <div v-if="loadingContacts" class="loading-contacts">
              <i class="el-icon-loading"></i>
              <span>加载联系人中...</span>
            </div>
            <div v-else-if="contacts.length === 0" class="no-contacts">
              <el-empty description="暂无联系人"></el-empty>
            </div>
            <div v-else class="contact-items">
              <div 
                v-for="contact in contacts" 
                :key="contact.id" 
                class="contact-item"
                :class="{ 'selected': selectedContact && selectedContact.id === contact.id }"
                @click="selectContact(contact)">
                <img :src="getImageUrl(contact.avatarUrl)" :alt="contact.username" class="contact-avatar">
                <span class="contact-name">{{ contact.username }}</span>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 分享消息输入 -->
        <div class="share-message">
          <h4>分享消息：</h4>
          <el-input
            v-model="shareMessage"
            type="textarea"
            :rows="3"
            placeholder="请输入分享消息（可选）"
            resize="none">
          </el-input>
        </div>
      </div>
      
      <span slot="footer" class="dialog-footer">
        <el-button @click="handleShareDialogClose">取消</el-button>
        <el-button 
          type="primary" 
          @click="confirmShare" 
          :disabled="!canShare"
          :loading="sharing">
          确认分享
        </el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'GuideDetail',
  data() {
    return {
      guideInfo: null,         // 攻略详情信息
      loading: true,           // 攻略加载状态
      isLiked: false,          // 用户是否已点赞攻略
      liking: false,           // 点赞操作中的状态
      deleting: false,         // 删除操作中的状态
      guideId: null,           // 攻略ID
      
      // 评论相关数据 - 仿照问题回答功能重新设计
      commentList: [],         // 评论列表数据
      commentContent: '',      // 评论输入内容
      loadingComments: false,  // 评论加载状态
      submittingComment: false, // 评论提交状态
      currentUser: null,        // 当前登录用户信息
      // 分享功能相关数据
      shareDialogVisible: false,
      shareMethod: 'private',
      contacts: [],
      loadingContacts: false,
      selectedContact: null,
      shareMessage: '',
      sharing: false
    }
  },
  
  computed: {
      // 判断当前用户是否是管理员
      // 严格验证管理员状态，确保只有真正的管理员才能看到删除按钮
      isAdmin() {
        // 首先检查是否有当前用户，没有用户肯定不是管理员
        if (!this.currentUser) {
          console.log('管理员检查：当前用户为空，返回false');
          return false;
        }
        
        // 严格检查localStorage中的isAdmin标识
        const isAdminFromStorage = localStorage.getItem('isAdmin') === 'true';
        
        // 严格检查用户对象中的管理员标志
        let isAdminFromUser = false;
        try {
          const userStr = localStorage.getItem('user');
          if (userStr) {
            const userObj = JSON.parse(userStr);
            // 严格检查各种可能的管理员字段
            isAdminFromUser = userObj.isAdmin === true || 
                             userObj.isAdmin === 'true' || 
                             userObj.role === 'admin' ||
                             userObj.role === 'ADMIN' ||
                             userObj.isadmin === 1;
          }
        } catch (e) {
          console.error('解析用户对象失败:', e);
        }
        
        // 综合结果，需要同时满足多个条件才认为是管理员
        const isAdmin = isAdminFromStorage && isAdminFromUser;
        return isAdmin;
      },
      
      // 判断当前用户是否有编辑权限（仅攻略作者可以编辑）
      canEdit() {
        if (!this.currentUser || !this.guideInfo) {
          console.log('权限检查失败：currentUser或guideInfo为空', {
            currentUser: this.currentUser,
            guideInfo: this.guideInfo
          });
          return false;
        }
        
        // 获取当前用户ID（支持多种可能的ID字段）
        const currentUserId = this.currentUser.id || this.currentUser.userId || this.currentUser.user_id;
        
        // 获取攻略作者ID（支持多种可能的ID字段）
        const authorId = this.guideInfo.author?.id || this.guideInfo.authorId || this.guideInfo.author_id;
        
        console.log('=== 权限检查详细调试信息 ===');
        console.log('当前登录用户信息:', this.currentUser);
        console.log('当前用户ID:', currentUserId, '类型:', typeof currentUserId);
        console.log('攻略详情信息:', this.guideInfo);
        console.log('攻略作者信息:', this.guideInfo.author);
        console.log('攻略作者ID:', authorId, '类型:', typeof authorId);
        console.log('用户ID与作者ID是否相等:', currentUserId == authorId);
        console.log('用户ID与作者ID严格相等:', currentUserId === authorId);
        console.log('是否有编辑权限:', currentUserId == authorId);
        console.log('=== 权限检查结束 ===');
        
        // 只有当前用户是攻略作者时才有编辑权限
        // 使用宽松比较，因为ID可能是字符串或数字
        return currentUserId == authorId;
      }
    },
  
  created() {
    // 从路由参数中获取攻略ID
    this.guideId = this.$route.params.id;
    if (this.guideId) {
      this.fetchGuideDetail();
    }
  },
  
  // 组件挂载后获取评论列表和用户信息
  mounted() {
    // 确保在挂载后获取用户信息，因为localStorage在组件完全挂载后可能更容易访问
    this.getCurrentUser();
    
    if (this.guideId) {
      this.fetchComments();
    }
  },
  
  // 监听路由变化，确保在切换到其他攻略时也能正确获取用户信息
  watch: {
    $route() {
      this.guideId = this.$route.params.id;
      this.getCurrentUser();
      if (this.guideId) {
        this.fetchGuideDetail();
        this.fetchComments();
      }
    }
  },
  
  methods: {
    /**
     * 获取图片完整URL路径
     * @param {string} url - 原始图片URL
     * @returns {string} - 完整的图片URL路径
     */
    getImageUrl(url) {
      // 健壮性检查，避免空值或非字符串类型
      if (!url || typeof url !== 'string') {
        return '';
      }
      
      // 如果URL已经包含完整路径则直接返回
      if (url.startsWith('http://') || url.startsWith('https://')) {
        return url;
      }
      
      // 处理相对路径（/uploads/、/avatars/ 或 /assets/avatars/）
      if (url.startsWith('/uploads/') || url.startsWith('/avatars/') || url.startsWith('/assets/avatars/')) {
        // 在开发环境中，直接返回相对路径，webpack dev server会处理
        return url;
      }
      
      // 其他情况返回原始URL
      return url;
    },
    
    /**
     * 获取攻略详情数据
     */
    fetchGuideDetail() {
      this.loading = true;
      this.$axios.get(`/api/guide/${this.guideId}`, {
        withCredentials: true
      })
      .then(response => {
        this.guideInfo = response.data;
        this.loading = false;
        
        // 获取攻略详情后，检查用户是否已经点赞了这篇攻略
        this.checkUserLiked();
      })
      .catch(error => {
        console.error('获取攻略详情失败:', error);
        this.loading = false;
        this.$message.error('获取攻略详情失败，请稍后重试');
      });
    },
    
    /**
     * 检查用户是否已经点赞了这篇攻略
     */
    checkUserLiked() {
      if (this.guideId && this.currentUser) {
        this.$axios.get(`/api/guide/${this.guideId}/check-like`, {
          withCredentials: true
        })
        .then(response => {
          if (response.data && response.data.isLiked !== undefined) {
            this.isLiked = response.data.isLiked;
          }
        })
        .catch(error => {
          console.error('检查点赞状态失败:', error);
          // 失败时不做处理，保持默认值
        });
      }
    },
    
    /**
     * 处理攻略点赞
     */
    handleLike() {
      // 防止重复点击或未登录点赞
      if (this.liking || !this.currentUser) {
        if (!this.currentUser) {
          this.$message.info('请先登录');
        }
        return;
      }
      
      this.liking = true;
      
      // 调用后端API进行点赞
      this.$axios.post(`/api/guide/${this.guideId}/like`, {}, {
        withCredentials: true
      })
      .then(response => {
        this.liking = false;
        
        if (response.data && response.data.success) {
          // 更新前端状态
          this.isLiked = true;
          this.guideInfo.likes = response.data.likes; // 使用后端返回的最新点赞数
          this.$message.success('点赞成功');
        } else {
          this.$message.error('点赞失败：' + (response.data.message || '未知错误'));
        }
      })
      .catch(error => {
        this.liking = false;
        console.error('点赞请求失败:', error);
        this.$message.error('点赞失败，请稍后重试');
      });
    },
    
    /**
     * 格式化日期显示
     * @param {string} dateString - 日期字符串
     * @returns {string} - 格式化后的日期
     */
    formatDate(dateString) {
      if (!dateString) return '';
      
      try {
        const date = new Date(dateString);
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        const hours = String(date.getHours()).padStart(2, '0');
        const minutes = String(date.getMinutes()).padStart(2, '0');
        return `${year}-${month}-${day} ${hours}:${minutes}`;
      } catch (error) {
        console.error('日期格式化错误:', error);
        return '';
      }
    },
    
    /**
     * 返回上一页
     */
    goBack() {
      this.$router.go(-1);
    },
    
    /**
     * 获取当前登录用户信息
     */
    getCurrentUser() {
      try {
        let userStr = localStorage.getItem('user');
        console.log('User string from localStorage:', userStr);
        
        // 首先获取用户名
        const username = localStorage.getItem('username');
        console.log('Current username from localStorage:', username);
        
        // 初始化用户对象
        let user = null;
        
        // 如果有用户字符串，解析它
        if (userStr) {
          user = JSON.parse(userStr);
          console.log('Parsed user object from storage:', user);
        }
        
        // 如果用户对象不存在，但有用户名，创建一个基本用户对象
    if (!user && username) {
      console.log('Creating user object from username');
      user = {
        username: username,
        isOnline: 1
      };
      
      // 不再硬编码管理员用户名检查，管理员状态应从后端获取
      // 避免为特定用户名自动设置管理员权限
      console.log('创建基本用户对象，管理员状态需从后端获取');
    }
        
        // 直接检查用户登录状态，不创建测试用户
        if (!user && !username) {
          console.log('未登录状态，不创建任何测试用户');
          // 保持user为null，这样系统会正常处理未登录状态
        }
        
        // 设置当前用户
        this.currentUser = user;
        console.log('Final current user:', this.currentUser);
        
        // 如果用户是管理员，确保localStorage中的isAdmin标志已设置
        if (user && (user.isAdmin || user.role === 'admin')) {
          localStorage.setItem('isAdmin', 'true');
          console.log('Admin flag set in localStorage');
        }
        
        // 调试信息
        console.log('Current admin status:', this.isAdmin);
      } catch (error) {
        console.error('获取用户信息失败:', error);
        this.currentUser = null;
        localStorage.removeItem('isAdmin');
      }
    },
    
    /**
     * 更新登录状态 - 重新验证用户登录状态
     */
    async updateLoginStatus() {
      console.log('开始更新登录状态验证');
      
      try {
        // 检查localStorage中的基本登录信息
        const username = localStorage.getItem('username');
        const token = localStorage.getItem('token');
        
        if (!username || !token) {
          console.log('未找到登录信息，清除用户状态');
          this.currentUser = null;
          localStorage.removeItem('isAdmin');
          return;
        }
        
        // 尝试调用后端接口验证登录状态
        const response = await this.$axios.get('/api/auth/current-user-full', {
          withCredentials: true,
          timeout: 5000
        });
        
        if (response.data && response.data.code === 200) {
          console.log('登录状态验证成功，用户仍然在线');
          // 更新用户信息
          const userInfo = {
            id: response.data.data?.id || 0,
            username: response.data.data?.username || username,
            isAdmin: response.data.data?.isAdmin || false,
            isOnline: 1
          };
          
          localStorage.setItem('user', JSON.stringify(userInfo));
          localStorage.setItem('isAdmin', userInfo.isAdmin.toString());
          this.currentUser = userInfo;
          
          console.log('登录状态更新成功:', userInfo);
        } else {
          console.log('登录状态验证失败，清除用户状态');
          this.currentUser = null;
          localStorage.removeItem('user');
          localStorage.removeItem('isAdmin');
          localStorage.removeItem('token');
        }
      } catch (error) {
        console.error('登录状态验证失败:', error);
        // 如果验证失败，清除登录状态
        this.currentUser = null;
        localStorage.removeItem('user');
        localStorage.removeItem('isAdmin');
        localStorage.removeItem('token');
        
        // 如果是401错误，提示用户重新登录
        if (error.response && error.response.status === 401) {
          console.log('用户登录已过期');
        }
      }
    },
    
    /**
     * 处理攻略删除 - 简化版本
     * 只有管理员才能看到删除按钮，所以直接信任删除请求
     */
    async handleDeleteGuide() {
      // 确认对话框
      this.$confirm('确定要删除这篇攻略吗？此操作不可撤销！', '提示', {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        this.deleting = true;
        
        try {
          // 发送删除请求前，确保认证信息正确传递
          const requestConfig = {
            withCredentials: true,
            headers: {
              // 确保Session认证信息正确传递
              'X-Requested-With': 'XMLHttpRequest'
            }
          };
          console.log('删除请求配置:', requestConfig);
          const response = await this.$axios.delete(`/api/guide/${this.guideId}`, requestConfig);
          
          this.deleting = false;
          this.$message.success('攻略删除成功');
          
          // 删除成功后返回上一页
          setTimeout(() => {
            this.$router.go(-1);
          }, 1000);
          
        } catch (error) {
          this.deleting = false;
          
          if (error.response) {
            const status = error.response.status;
            const data = error.response.data;
            
            if (status === 401) {
              this.$message.error('登录已过期，请重新登录');
            } else if (status === 403) {
              this.$message.error('您没有权限删除此攻略');
            } else if (status === 404) {
              this.$message.error('攻略不存在或已被删除');
            } else {
              this.$message.error('删除失败: ' + (data || '未知错误'));
            }
          } else if (error.request) {
            this.$message.error('网络错误，请检查您的网络连接');
          } else {
            this.$message.error('请求错误: ' + error.message);
          }
        }
      }).catch(() => {
        this.$message.info('已取消删除');
      });
    },
    

    
    /**
     * 获取评论列表 - 重新实现为简化版本，避免分页错误
     */
    fetchComments() {
      this.loadingComments = true;
      
      // 使用实际的后端API获取评论列表
      this.$axios.get(`/api/guide/${this.guideId}/comments`, {
        withCredentials: true
      })
      .then(response => {
        // 确保返回的数据是数组格式
        if (Array.isArray(response.data)) {
          this.commentList = response.data;
        } else if (response.data && Array.isArray(response.data.comments)) {
          this.commentList = response.data.comments;
        } else {
          this.commentList = [];
          console.warn('评论数据格式异常');
        }
        this.loadingComments = false;
      })
      .catch(error => {
        console.error('获取评论列表失败:', error);
        this.commentList = []; // 发生错误时清空评论列表避免显示问题
        this.loadingComments = false;
        
        // 不显示错误提示，避免干扰用户体验
      });
    },
    
    /**
     * 提交评论 - 重新实现评论提交功能
     */
    submitComment() {
      // 验证评论内容
      if (!this.commentContent.trim()) {
        this.$message.warning('请输入评论内容');
        return;
      }
      
      // 验证评论长度
      if (this.commentContent.trim().length > 500) {
        this.$message.warning('评论内容不能超过500个字符');
        return;
      }
      
      // 验证用户登录状态
      if (!this.currentUser) {
        this.$message.info('请先登录');
        return;
      }
      
      this.submittingComment = true;
      
      // 使用实际的后端API提交评论
      this.$axios.post(`/api/guide/${this.guideId}/comments`, {
        content: this.commentContent.trim()
      }, {
        withCredentials: true
      })
      .then(response => {
        // 添加到评论列表开头
        if (response.data) {
          this.commentList.unshift(response.data);
        }
        
        // 清空输入框
        this.commentContent = '';
        
        this.$message.success('评论发表成功');
      })
      .catch(error => {
        console.error('提交评论失败:', error);
        // 检查是否是未登录错误
        if (error.response && error.response.status === 401) {
          this.$message.error('请先登录');
        } else {
          this.$message.error('提交评论失败');
        }
      })
      .finally(() => {
        this.submittingComment = false;
      });
    },
    
    /**
     * 处理评论点赞 - 仿照问题回答的点赞功能
     * @param {Object} comment - 评论对象
     */
    handleCommentLike(comment) {
      // 防止重复点击或未登录点赞
      if (comment.liking || !this.currentUser) {
        if (!this.currentUser) {
          this.$message.info('请先登录');
        }
        return;
      }
      
      comment.liking = true;
      
      // 使用实际的后端API点赞
      this.$axios.post(`/api/comment/${comment.id}/like`, {}, {
        withCredentials: true
      })
      .then(response => {
        comment.liking = false;
        
        // 更新点赞状态和数量
        if (response.data) {
          comment.isLiked = response.data.isLiked;
          comment.likes = response.data.likes;
          
          this.$message.success(comment.isLiked ? '点赞成功' : '取消点赞成功');
        }
      })
      .catch(error => {
        comment.liking = false;
        console.error('评论点赞失败:', error);
        
        // 检查是否是未登录错误
        if (error.response && error.response.status === 401) {
          this.$message.error('请先登录');
        } else {
          this.$message.error('点赞失败');
        }
      });
    },
    
    /**
     * 处理编辑攻略
     */
    handleEditGuide() {
      // 检查用户是否有编辑权限
      if (!this.canEdit) {
        this.$message.error('您没有编辑此攻略的权限');
        return;
      }
      
      // 跳转到编辑页面
      this.$router.push(`/guide/edit/${this.guideId}`);
    },
    
    // 分享功能相关方法
    handleShare() {
      console.log('打开分享对话框，攻略ID:', this.$route.params.id);
      this.shareDialogVisible = true;
      this.loadContacts();
    },
    
    handleShareDialogClose() {
      this.shareDialogVisible = false;
      this.selectedContact = null;
      this.shareMessage = '';
      this.sharing = false;
    },
    
    async loadContacts() {
      try {
        this.loadingContacts = true;
        console.log('开始加载联系人列表');
        
        // 调用私聊联系人API获取联系人列表
        const response = await this.$axios.get('/api/private-messages/conversations');
        console.log('联系人API响应:', response);
        
        if (response && response.data && response.data.conversations) {
          this.contacts = response.data.conversations.map(conversation => ({
            id: conversation.otherUser.id,
            username: conversation.otherUser.username,
            avatarUrl: conversation.otherUser.avatarUrl
          }));
          console.log('成功加载联系人:', this.contacts.length);
        } else {
          console.warn('联系人API返回数据格式异常');
          this.contacts = [];
        }
      } catch (error) {
        console.error('加载联系人失败:', error);
        this.$message.error('加载联系人失败');
        this.contacts = [];
      } finally {
        this.loadingContacts = false;
      }
    },
    
    selectContact(contact) {
      console.log('选择联系人:', contact);
      this.selectedContact = contact;
    },
    
    get canShare() {
      return this.selectedContact !== null;
    },
    
    async confirmShare() {
      if (!this.canShare) {
        this.$message.warning('请选择联系人');
        return;
      }
      
      try {
        this.sharing = true;
        console.log('开始分享攻略:', {
          攻略ID: this.$route.params.id,
          联系人ID: this.selectedContact.id,
          分享消息: this.shareMessage
        });
        
        // 构建分享消息内容 - 使用Markdown格式链接
        const guideUrl = `${window.location.origin}/guide/${this.$route.params.id}`;
        const messageContent = this.shareMessage || `分享攻略：${this.guideInfo.title}`;
        const fullMessage = `${messageContent}\n\n📎 [点击查看攻略详情](${guideUrl})`;
        
        // 调用私聊发送消息API - 使用URL参数格式，并对内容进行URL编码
        const response = await this.$axios.post('/api/private-messages/send', null, {
          params: {
            receiverId: this.selectedContact.id,
            content: encodeURIComponent(fullMessage)
          }
        });
        
        console.log('分享成功，API响应:', response);
        this.$message.success('分享成功');
        this.handleShareDialogClose();
        
      } catch (error) {
        console.error('分享失败:', error);
        this.$message.error('分享失败，请重试');
      } finally {
        this.sharing = false;
      }
    }
  }
}
</script>

<style scoped>
/* 攻略详情容器 */
.guide-detail-container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding: 20px;
}

/* 返回按钮容器 */
.back-button-container {
  margin-bottom: 20px;
}

/* 攻略内容包装器 */
.guide-content-wrapper {
  max-width: 900px;
  margin: 0 auto;
  background-color: white;
  border-radius: 8px;
  padding: 40px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
}

/* 攻略头部 */
.guide-header {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
}

/* 编辑按钮容器 */
.edit-button-container {
  margin-bottom: 20px;
  text-align: right;
}

/* 攻略封面 */
.guide-cover {
  margin-bottom: 20px;
  text-align: center;
}

.cover-image {
  max-width: 100%;
  max-height: 400px;
  object-fit: cover;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

/* 攻略标题 */
.guide-title {
  margin: 0 0 20px 0;
  font-size: 28px;
  color: #333;
  font-weight: bold;
  line-height: 1.4;
}

/* 攻略元信息 */
.guide-meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
  font-size: 14px;
  color: #666;
}

.guide-meta i {
  margin-right: 5px;
}

/* 统计信息 */
.stats {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-left: auto;
}

/* 攻略内容 */
.guide-content {
  margin-bottom: 40px;
}

/* 加载和错误状态 */
.loading-state, .error-state {
  text-align: center;
  padding: 60px 0;
}

.content-text {
  font-size: 16px;
  line-height: 2;
  color: #333;
  word-wrap: break-word;
  overflow-wrap: break-word;
  white-space: pre-wrap;
}

.content-text p {
  margin-bottom: 20px;
}

.content-text img {
  max-width: 100%;
  height: auto;
  margin: 20px 0;
  border-radius: 4px;
}

.content-text h2, .content-text h3 {
  margin: 30px 0 15px 0;
  color: #333;
}

/* 互动区域 */
.interaction-area {
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
  margin-bottom: 40px;
}

/* 评论区样式 - 仿照问题回答功能 */
.comment-section {
  margin-top: 20px;
  padding-top: 30px;
  border-top: 1px solid #f0f0f0;
}

.comment-section-title {
  margin: 0 0 20px 0;
  font-size: 20px;
  color: #333;
  font-weight: 600;
}

/* 评论输入区域 */
.comment-input-area {
  margin-bottom: 30px;
}

.comment-input-footer {
  margin-top: 10px;
  text-align: right;
}

.login-tip {
  margin-bottom: 30px;
}

/* 评论列表 */
.comment-list {
  margin-bottom: 20px;
}

.loading-comments {
  text-align: center;
  padding: 30px 0;
  color: #999;
}

.loading-comments i {
  font-size: 20px;
  margin-right: 10px;
}

.no-comments {
  padding: 40px 0;
}

/* 评论项 - 仿照问题回答样式 */
.comment-answer-item {
  background-color: #fafafa;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  border: 1px solid #f0f0f0;
  transition: all 0.3s ease;
}

.comment-answer-item:hover {
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  border-color: #e6f7ff;
}

/* 评论头部 */
.comment-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  flex-wrap: wrap;
  gap: 10px;
}

.comment-author {
  font-weight: 500;
  color: #333;
  font-size: 14px;
}

.comment-time {
  font-size: 12px;
  color: #999;
}

/* 评论操作按钮 */
.comment-actions {
  display: flex;
  gap: 15px;
}

.comment-actions .el-button--text {
  color: #999;
  font-size: 12px;
}

.comment-actions .el-button--text:hover {
  color: #409eff;
}

/* 评论内容 */
.comment-content {
  font-size: 15px;
  line-height: 1.8;
  color: #333;
  word-break: break-word;
  padding: 10px 0;
  background-color: white;
  border-radius: 4px;
  padding: 15px;
  border-left: 3px solid #409eff;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .guide-detail-container {
    padding: 10px;
  }
  
  .guide-content-wrapper {
    padding: 20px;
  }
  
  .guide-title {
    font-size: 24px;
  }
  
  .guide-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .stats {
    margin-left: 0;
  }
  
  .content-text {
    font-size: 15px;
  }
  
  /* 响应式评论区 */
  .comment-section {
    margin-top: 30px;
    padding-top: 20px;
  }
  
  .comment-section-title {
    font-size: 18px;
  }
  
  .comment-answer-item {
    padding: 15px;
  }
  
  .comment-content {
    font-size: 14px;
  }
  
  .comment-header {
    flex-direction: column;
    align-items: flex-start;
  }
}

/* 分享功能样式 */
.share-dialog {
  max-width: 600px;
}

.share-method-selector {
  margin-bottom: 20px;
}

.contacts-list {
  max-height: 300px;
  overflow-y: auto;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  padding: 10px;
}

.contact-item {
  display: flex;
  align-items: center;
  padding: 10px;
  cursor: pointer;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.contact-item:hover {
  background-color: #f5f5f5;
}

.contact-item.selected {
  background-color: #e6f7ff;
  border: 1px solid #1890ff;
}

.contact-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  margin-right: 12px;
  object-fit: cover;
}

.contact-info {
  flex: 1;
}

.contact-username {
  font-weight: 500;
  color: #333;
}

.share-message-input {
  margin: 20px 0;
}

.share-dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
}

.loading-contacts {
  text-align: center;
  padding: 20px;
  color: #666;
}

.empty-contacts {
  text-align: center;
  padding: 40px 20px;
  color: #999;
}
</style>