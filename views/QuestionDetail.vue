<template>
  <div class="question-detail-container">
    <el-card class="question-detail-card" style="overflow: visible;">
      <!-- 问题头部 -->
      <div class="question-header" style="overflow: visible;">
        <h1 class="question-title">{{ question.title }}</h1>
        <div class="question-meta" style="overflow: visible;">
          <!-- 使用新的容器结构包装头像和用户名 -->
          <!-- 使用内联样式直接控制间距 -->
          <!-- 用户信息显示，添加交互功能 -->
          <div 
                style="display: flex; align-items: center; gap: 0px; margin-right: 16px; position: relative; cursor: pointer; overflow: visible;"
                @mouseenter="showUserDropdown('question')"
                @mouseleave="hideUserDropdown"
                @click="goToUserProfile(question.author?.username)"
              >
            <div style="width: 32px; height: 32px; border-radius: 50%; overflow: hidden;">
              <img v-if="getAuthorAvatar(question.author)" 
                   :src="getImageUrl(getAuthorAvatar(question.author))" 
                   :alt="question.author?.username || '用户头像'" 
                   style="width: 100%; height: 100%; object-fit: cover;">
              <div v-else style="width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; color: #c0c4cc; font-size: 14px; background-color: #f0f2f5;">
                <i class="el-icon-user"></i>
              </div>
            </div>
            <span>{{ question.author?.username || '匿名用户' }}</span>
            
            <!-- 用户二级下拉菜单 -->
              <div 
                v-if="showDropdown === 'question' && question.author" 
                class="user-dropdown-menu"
                style="position: absolute; top: -10px; right: -10px; transform: translate(100%, -100%); min-width: 150px; z-index: 9999; overflow: visible;"
              >
                <div class="dropdown-arrow" style="position: absolute; bottom: -8px; left: 20px; width: 0; height: 0; border-left: 8px solid transparent; border-right: 8px solid transparent; border-top: 8px solid #fff;"></div>
                <div class="dropdown-content" style="background: white; border-radius: 6px; box-shadow: 0 2px 12px rgba(0,0,0,0.15); padding: 10px; border: 1px solid #ebeef5; min-width: 120px;">
                  <div style="display: flex; flex-direction: column; align-items: center; margin-bottom: 10px;">
                    <div style="width: 40px; height: 40px; border-radius: 50%; overflow: hidden; margin-bottom: 8px;">
                      <img v-if="getAuthorAvatar(question.author)" 
                           :src="getImageUrl(getAuthorAvatar(question.author))" 
                           :alt="question.author?.username || '用户头像'" 
                           style="width: 100%; height: 100%; object-fit: cover;">
                      <div v-else style="width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; color: #c0c4cc; font-size: 16px; background-color: #f0f2f5;">
                        <i class="el-icon-user"></i>
                      </div>
                    </div>
                    <span style="font-size: 14px; font-weight: 500;">{{ question.author?.username || '匿名用户' }}</span>
                  </div>
                  <div class="user-info-item" style="margin-bottom: 8px; display: flex; justify-content: space-between; align-items: center; width: 100%;">
                    <span style="font-size: 14px; color: #606266;">关注：</span>
                    <span style="font-size: 14px; font-weight: 500;">0</span>
                  </div>
                  <div class="user-info-item" style="display: flex; justify-content: space-between; align-items: center; width: 100%;">
                    <span style="font-size: 14px; color: #606266;">粉丝：</span>
                    <span style="font-size: 14px; font-weight: 500;">0</span>
                  </div>
                </div>
              </div>
          </div>
          <span class="question-time">{{ formatDate(question.createTime) }}</span>
          <span class="question-answers">{{ question.answerCount || 0 }} 个回答</span>
          <span class="question-views">{{ question.viewCount || 0 }} 次浏览</span>
        </div>
      </div>

      <!-- 问题内容 -->
      <div class="question-content">
        <div v-if="loading" class="loading-state">
          <i class="el-icon-loading"></i>
          <span>加载中...</span>
        </div>
        <div v-else-if="error" class="error-state">
          <i class="el-icon-warning"></i>
          <span>{{ error }}</span>
        </div>
        <div v-else class="content-text" v-html="formatContent(question.content)"></div>
      </div>

      <!-- 问题图片 -->
      <div v-if="question.imageUrls && question.imageUrls.length > 0" class="question-images">
        <img v-for="(url, index) in question.imageUrls" :key="index" :src="getImageUrl(url)" class="question-image" />
      </div>

      <!-- 回答区域 -->
      <div class="answers-section" style="overflow: visible;">
        
        <!-- 回答输入框 -->
        <div v-if="isLogin" class="answer-input-section">
          <el-card>
            <el-form ref="answerForm" :model="answerForm" :rules="answerRules">
              <el-form-item label="我的回答" prop="content">
                <el-input
                  v-model="answerForm.content"
                  type="textarea"
                  placeholder="请输入你的回答..."
                  :rows="4"
                  maxlength="2000"
                  show-word-limit
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleSubmitAnswer">提交回答</el-button>
              </el-form-item>
            </el-form>
          </el-card>
        </div>

        <!-- 回答列表 -->
        <div class="answers-list" style="overflow: visible;">
          <div v-if="answers.length === 0" class="no-answers">
            <i class="el-icon-message"></i>
            <span>暂无回答，来做第一个回答者吧！</span>
          </div>
          <div v-else>
            <el-card v-for="answer in answers" :key="answer.id" :class="['answer-card', { 'best-answer-card': answer.accepted }]" style="overflow: visible;">
              <div class="answer-header" style="overflow: visible;">
                <!-- 使用新的容器结构包装头像和用户名 -->
                <!-- 使用内联样式直接控制间距 -->
                <!-- 用户信息显示，添加交互功能 -->
                <div 
                    style="display: flex; align-items: center; gap: 0px; margin-right: 16px; position: relative; cursor: pointer; overflow: visible;"
                    @mouseenter="showUserDropdown('answer', answer.id)"
                    @mouseleave="hideUserDropdown"
                    @click="goToUserProfile(answer.author?.username)"
                  >
                  <div style="width: 32px; height: 32px; border-radius: 50%; overflow: hidden;">
                    <img v-if="getAuthorAvatar(answer.author)" 
                         :src="getImageUrl(getAuthorAvatar(answer.author))" 
                         :alt="answer.author?.username || '用户头像'" 
                         style="width: 100%; height: 100%; object-fit: cover;">
                    <div v-else style="width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; color: #c0c4cc; font-size: 14px; background-color: #f0f2f5;">
                      <i class="el-icon-user"></i>
                    </div>
                  </div>
                  <span>{{ answer.author?.username || '匿名用户' }}</span>
                  
                  <!-- 用户二级下拉菜单 -->
                  <div 
                    v-if="showDropdown === `answer_${answer.id}` && answer.author" 
                    class="user-dropdown-menu"
                    style="position: absolute; top: -10px; right: -10px; transform: translate(100%, -100%); min-width: 150px; z-index: 9999; overflow: visible;"
                  >
                    <div class="dropdown-arrow" style="position: absolute; bottom: -8px; left: 20px; width: 0; height: 0; border-left: 8px solid transparent; border-right: 8px solid transparent; border-top: 8px solid #fff;"></div>
                    <div class="dropdown-content" style="background: white; border-radius: 6px; box-shadow: 0 2px 12px rgba(0,0,0,0.15); padding: 10px; border: 1px solid #ebeef5; min-width: 120px;">
                      <div style="display: flex; flex-direction: column; align-items: center; margin-bottom: 10px;">
                        <div style="width: 40px; height: 40px; border-radius: 50%; overflow: hidden; margin-bottom: 8px;">
                          <img v-if="getAuthorAvatar(answer.author)" 
                               :src="getImageUrl(getAuthorAvatar(answer.author))" 
                               :alt="answer.author?.username || '用户头像'" 
                               style="width: 100%; height: 100%; object-fit: cover;">
                          <div v-else style="width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; color: #c0c4cc; font-size: 16px; background-color: #f0f2f5;">
                            <i class="el-icon-user"></i>
                          </div>
                        </div>
                        <span style="font-size: 14px; font-weight: 500;">{{ answer.author?.username || '匿名用户' }}</span>
                      </div>
                      <div class="user-info-item" style="margin-bottom: 8px; display: flex; justify-content: space-between; align-items: center; width: 100%;">
                        <span style="font-size: 14px; color: #606266;">关注：</span>
                        <span style="font-size: 14px; font-weight: 500;">0</span>
                      </div>
                      <div class="user-info-item" style="display: flex; justify-content: space-between; align-items: center; width: 100%;">
                        <span style="font-size: 14px; color: #606266;">粉丝：</span>
                        <span style="font-size: 14px; font-weight: 500;">0</span>
                      </div>
                    </div>
                  </div>
                </div>
                <span class="answer-time">{{ formatDate(answer.createTime) }}</span>
                <div class="answer-actions" v-if="!answer.accepted">
                  <el-button type="text" size="small" @click="handleVote(answer.id, 'up')">
                    <i class="el-icon-thumb"></i>
                    <span>{{ answer.upVotes }}</span>
                  </el-button>
                  <el-button type="text" size="small" @click="handleVote(answer.id, 'down')">
                    <i class="el-icon-thumb"></i>
                    <span>{{ answer.downVotes }}</span>
                  </el-button>
                  <!-- 只有提问者可以看到设为最佳回答按钮 -->
                  <el-button type="text" size="small" @click="handleAcceptAnswer(answer.id)" v-if="isAuthor" style="color: #1890ff;">
                    <i class="el-icon-flag"></i>
                    <span>设为最佳回答</span>
                  </el-button>
                </div>
                <span v-if="answer.accepted" class="best-answer-tag">
                  <i class="el-icon-trophy"></i>
                  <span>最佳回答</span>
                </span>
              </div>
              <div class="answer-content" v-html="formatContent(answer.content)"></div>
            </el-card>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'QuestionDetail',
  components: {
  },
  data() {
      return {
        question: {
          id: '',
          title: '',
          content: '',
          createTime: '',
          author: {},
          answerCount: 0,
          viewCount: 0,
          imageUrls: []
        },
        answers: [],
        loading: false,
        error: '',
        isLogin: false,
        isAuthor: false,
        currentUser: null, // 当前登录用户信息
        userAvatars: {}, // 存储用户ID到头像URL的映射
        userStats: {}, // 存储用户的关注和粉丝数量
        showDropdown: null, // 当前显示的下拉菜单标识
        answerForm: {
          content: ''
        },
        answerRules: {
          content: [
            { required: true, message: '请输入回答内容', trigger: 'blur' },
            { min: 5, max: 2000, message: '回答长度在 5 到 2000 个字符', trigger: 'blur' }
          ]
        },
        hasClearedLogs: false // 标记是否已执行过日志清除
      };
    },
  mounted() {
    // 仅在首次访问时清除控制台日志
    this.clearConsoleLogsOnce();
    
    this.checkLoginStatus();
    this.fetchUserAvatar();
    this.fetchQuestionDetail();
    
    // 监听头像更新事件，确保头像更新后问答专区也能及时刷新
    this.setupAvatarRefreshListener();
  },
  
  beforeDestroy() {
    // 清理事件监听器
    this.cleanupAvatarRefreshListener();
  },
  methods: {
    // 仅在首次访问时清除控制台日志
    clearConsoleLogsOnce() {
      // 检查是否已经执行过日志清除
      if (this.hasClearedLogs) {
        return;
      }
      
      // 检查localStorage中是否有页面访问标记
      const pageVisitKey = 'questionDetailPageVisited';
      const hasVisited = localStorage.getItem(pageVisitKey);
      
      if (!hasVisited) {
        // 首次访问该页面，清除控制台日志
        console.clear();
        console.log('🔍 问答详情页面首次访问，已清除控制台日志');
        
        // 设置页面访问标记，避免重复清除
        localStorage.setItem(pageVisitKey, 'true');
        
        // 设置组件状态标记
        this.hasClearedLogs = true;
      } else {
        // 非首次访问，保留现有日志
        console.log('🔍 问答详情页面非首次访问，保留控制台日志');
        this.hasClearedLogs = true;
      }
    },
    
    // 检查登录状态
    checkLoginStatus() {
      const username = localStorage.getItem('username');
      const userInfoStr = localStorage.getItem('user');
      this.isLogin = !!username;
      // 解析缓存的用户信息用于头像显示
      if (userInfoStr) {
        try {
          const userInfo = JSON.parse(userInfoStr);
          this.currentUser = userInfo;
          // 保存当前用户的头像
          if (userInfo.username && userInfo.avatar) {
            this.userAvatars[userInfo.username] = userInfo.avatar;
          }
        } catch (e) {
          console.error('解析用户信息失败:', e);
        }
      }
    },
    
    // 获取用户头像和信息 - 与导航栏保持一致的方法
    fetchUserAvatar() {
      const localUsername = localStorage.getItem('username');
      
      if (localUsername) {
        // 与导航栏相同的方法，从后端获取完整用户信息
        this.$axios.get('/api/auth/current-user-full', {
          withCredentials: true
        })
        .then(response => {
          if (response.data && response.data.code === 200 && response.data.data) {
            const userData = response.data.data;
            const avatarUrl = userData.avatar;
            
            console.log('🔍 QuestionDetail.vue - 重新登录后获取的userInfo:', userData);
            console.log('🔍 QuestionDetail.vue - avatarUrl是否存在:', !!avatarUrl);
            console.log('🔍 QuestionDetail.vue - avatarUrl具体值:', avatarUrl);
            
            // 更新当前用户的头像信息
            if (userData.username && avatarUrl) {
              this.userAvatars[userData.username] = avatarUrl;
              
              // 移除强制重新渲染，避免循环
            }
            
            // 更新本地存储的用户信息
            try {
              const userInfoStr = localStorage.getItem('user');
              let userInfo = userInfoStr ? JSON.parse(userInfoStr) : {};
              
              // 更新头像和其他信息
              userInfo.avatar = avatarUrl;
              userInfo.username = userData.username;
              userInfo.email = userData.email;
              
              localStorage.setItem('user', JSON.stringify(userInfo));
              console.log('🔍 QuestionDetail.vue - 更新后的localStorage user信息:', userInfo);
            } catch (error) {
              console.error('❌ 问答专区更新本地存储失败:', error);
            }
          }
        })
        .catch(error => {
          console.error('❌ 问答专区获取用户信息失败:', error);
          console.error('错误详情:', error.response);
          
          // 在API失败时，尝试从localStorage获取缓存头像作为备选
          try {
            const userInfoStr = localStorage.getItem('user');
            if (userInfoStr) {
              const userInfo = JSON.parse(userInfoStr);
              const cachedAvatar = userInfo.avatar;
              const cachedUsername = userInfo.username;
              console.log('🔄 问答专区使用缓存头像URL:', cachedAvatar);
              
              if (cachedUsername && cachedAvatar) {
                this.userAvatars[cachedUsername] = cachedAvatar;
                // 移除强制更新，避免循环
              }
            }
          } catch (cacheError) {
            console.error('❌ 问答专区读取缓存头像失败:', cacheError);
          }
        });
      } else {
        console.log('🚫 问答专区未找到本地用户名，用户可能未登录');
      }
    },
    
    // 通过用户名获取用户头像
    fetchUserAvatarByUsername(username) {
      if (!username) return;
      
      // 避免重复请求
      if (this.pendingAvatarRequests && this.pendingAvatarRequests[username]) {
        return;
      }
      
      if (!this.pendingAvatarRequests) {
        this.pendingAvatarRequests = {};
      }
      
      this.pendingAvatarRequests[username] = true;
      
      console.log(`开始获取用户${username}的头像信息`);
      
      this.$axios.get(`/api/auth/user/by-username/${username}`, {
        withCredentials: true,
        headers: {
          'X-Requested-With': 'XMLHttpRequest'
        }
      })
      .then(response => {
        // 修复：接口返回的是JSON格式，需要检查code和data
        if (response.data && response.data.code === 200 && response.data.data) {
          const userInfo = response.data.data;
          console.log(`获取到用户${username}信息:`, userInfo);
          
          if (userInfo && userInfo.avatar) {
            let avatarUrl = userInfo.avatar;
            
            // 关键修复：API返回的路径是正确的，不需要映射
            // /assets/avatars/test2_1762128205639.png 是正确路径
            console.log(`获取到用户${username}头像路径:`, avatarUrl);
            
            // 使用getImageUrl方法处理头像路径，确保路径正确
            this.userAvatars[username] = this.getImageUrl(avatarUrl);
            console.log(`设置用户${username}头像URL:`, this.userAvatars[username]);
            
            // 强制刷新视图，确保新头像立即显示
            this.$forceUpdate();
          }
        } else {
          console.warn(`获取用户${username}信息失败，响应格式异常:`, response.data);
        }
      })
      .catch(error => {
        console.error(`获取用户${username}信息失败:`, error);
        // 设置默认头像，而不是null，确保始终有头像显示，并使用getImageUrl处理路径
        this.userAvatars[username] = this.getImageUrl('/assets/avatars/default-avatar.svg');
        // 强制刷新视图
        this.$forceUpdate();
      })
      .finally(() => {
        delete this.pendingAvatarRequests[username];
      });
    },
    
    // 获取所有相关用户的头像
    fetchAllUserAvatars() {
      // 获取问题作者的头像
      if (this.question.author && this.question.author.username) {
        this.fetchUserAvatarByUsername(this.question.author.username);
      }
      
      // 获取所有回答作者的头像
      this.answers.forEach(answer => {
        if (answer.author && answer.author.username) {
          this.fetchUserAvatarByUsername(answer.author.username);
        }
      });
    },

    // 获取问题详情
    fetchQuestionDetail() {
      const questionId = this.$route.params.id;
      this.loading = true;
      this.error = '';
      
      // 先获取问题详情
      this.$axios.get(`/api/questions/${questionId}`, {
        withCredentials: true,
        headers: {
          'X-Requested-With': 'XMLHttpRequest'
        }
      })
      .then(response => {
        // 后端直接返回Question对象，而不是包装对象
        this.question = response.data || {};
        
        // 确保answerCount和viewCount有默认值
        if (this.question.answerCount === undefined || this.question.answerCount === null) {
          this.question.answerCount = 0;
        }
        if (this.question.viewCount === undefined || this.question.viewCount === null) {
          this.question.viewCount = 0;
        }
        
        // 检查当前用户是否为提问者
        const username = localStorage.getItem('username');
        this.isAuthor = this.question.author && this.question.author.username === username;
        
        // 直接从问题数据中提取提问者头像，确保头像信息被保存
        if (this.question.author && this.question.author.username) {
          // 直接尝试从author对象中获取头像
          if (this.question.author.avatar) {
            this.userAvatars[this.question.author.username] = this.question.author.avatar;
          } else {
            // 如果没有头像，标记为已检查以避免重复查询
            this.userAvatars[this.question.author.username] = null;
          }
        }
        
        // 然后获取回答列表（fetchAnswers方法内部会调用fetchAllUserAvatars）
        this.fetchAnswers();
      })
      .catch(error => {
        console.error('获取问题详情失败:', error);
        
        // 如果是因为未登录导致的错误，尝试使用模拟数据或显示默认值
        if (error.response && error.response.status === 401) {
          console.warn('未登录状态，使用默认问题数据');
          this.question = {
            id: questionId,
            title: '问题详情',
            content: '请登录后查看完整问题内容',
            answerCount: 0,
            viewCount: 0,
            author: { username: '匿名用户' }
          };
          this.answers = [];
          this.loading = false;
        } else {
          this.error = '获取问题详情失败，请稍后重试';
          this.loading = false;
        }
      });
    },
    
    // 获取回答列表
    fetchAnswers() {
      const questionId = this.$route.params.id;
      
      this.$axios.get(`/api/questions/${questionId}/answers`, {
        withCredentials: true,
        headers: {
          'X-Requested-With': 'XMLHttpRequest'
        }
      })
      .then(response => {
        // 获取回答列表并确保最佳回答置顶显示
        let answersList = response.data || [];
        
        // 排序逻辑：最佳回答(accepted为true)排在最前面，其他按创建时间排序
        answersList.sort((a, b) => {
          // 先按最佳回答状态排序
          if (a.accepted && !b.accepted) return -1;
          if (!a.accepted && b.accepted) return 1;
          
          // 对于非最佳回答，按创建时间排序（从旧到新）
          const dateA = new Date(a.createTime).getTime();
          const dateB = new Date(b.createTime).getTime();
          return dateA - dateB;
        });
        
        // 直接从回答数据中提取所有回答者的头像信息
        answersList.forEach(answer => {
          if (answer.author && answer.author.username) {
            // 直接从author对象中获取头像
            if (answer.author.avatar) {
              this.userAvatars[answer.author.username] = answer.author.avatar;
            } else {
              // 如果没有头像，标记为已检查
              this.userAvatars[answer.author.username] = null;
            }
          }
        });
        
        this.answers = answersList;
        
        // 更新问题中的回答数量
        this.question.answerCount = answersList.length;
        
        // 立即获取所有相关用户的头像
        this.fetchAllUserAvatars();
      })
      .catch(error => {
        console.error('获取回答列表失败:', error);
        this.answers = [];
      })
      .finally(() => {
        this.loading = false;
      });
    },

    // 获取图片URL
    getImageUrl(url) {
      if (!url) return '/assets/avatars/default-avatar.svg';
      
      // 如果是绝对URL（包含http://或https://），直接返回
      if (url.startsWith('http://') || url.startsWith('https://')) {
        return url;
      }
      
      // 关键修复：对于相对路径，直接返回，webpack开发服务器已配置静态资源服务
      // /assets/avatars/路径现在由webpack devServer直接提供服务
      return url;
    },
    
    // 重写：获取作者头像 - 优先使用localStorage中的用户头像信息（与导航栏保持一致）
    getAuthorAvatar(author) {
      if (!author || !author.username) {
        return this.getImageUrl('/assets/avatars/default-avatar.svg');
      }
      
      const username = author.username;
      console.log(`获取用户${username}头像`);
      
      // 关键修复：如果是当前登录用户，优先从localStorage获取头像（与导航栏逻辑一致）
      const currentUsername = localStorage.getItem('username');
      if (username === currentUsername) {
        try {
          const userInfoStr = localStorage.getItem('user');
          if (userInfoStr) {
            const userInfo = JSON.parse(userInfoStr);
            if (userInfo.avatar) {
              console.log(`使用当前登录用户${username}的头像: ${userInfo.avatar}`);
              const processedAvatar = this.getImageUrl(userInfo.avatar);
              this.userAvatars[username] = processedAvatar;
              return processedAvatar;
            }
          }
        } catch (error) {
          console.warn('从localStorage获取用户头像失败:', error);
        }
      }
      
      // 先检查本地缓存
      if (this.userAvatars[username]) {
        const cachedAvatar = this.userAvatars[username];
        console.log(`使用缓存头像: ${cachedAvatar}`);
        return cachedAvatar;
      }
      
      // 如果author对象中直接有avatar属性，使用它
      if (author.avatar) {
        console.log(`使用author对象中的头像: ${author.avatar}`);
        // 确保路径正确处理后再缓存
        const processedAvatar = this.getImageUrl(author.avatar);
        this.userAvatars[username] = processedAvatar;
        return processedAvatar;
      }
      
      // 关键修复：对于所有用户（包括非当前登录用户），立即异步获取头像
      // 确保头像获取请求被触发，即使author.avatar为空
      this.fetchUserAvatarByUsername(username);
      
      // 默认返回通用默认头像，并确保路径正确处理
      return this.getImageUrl('/assets/avatars/default-avatar.svg');
    },
    
    // 显示用户下拉菜单
    showUserDropdown(type, id = null) {
      if (type === 'question') {
        this.showDropdown = 'question';
        // 鼠标触碰时强制刷新当前用户的头像
        if (this.question.author && this.question.author.username) {
          this.fetchUserAvatarByUsername(this.question.author.username);
        }
      } else if (type === 'answer' && id) {
        this.showDropdown = `answer_${id}`;
        // 鼠标触碰时强制刷新当前回答用户的头像
        const answer = this.answers.find(a => a.id === id);
        if (answer && answer.author && answer.author.username) {
          this.fetchUserAvatarByUsername(answer.author.username);
        }
      }
    },
    
    // 隐藏用户下拉菜单
    hideUserDropdown() {
      // 添加一个短暂的延迟，以便用户可以点击下拉菜单中的内容
      setTimeout(() => {
        this.showDropdown = null;
      }, 200);
    },
    
    // 跳转到用户个人主页
    goToUserProfile(username) {
      if (!username) return;
      // 修改为正确的用户个人主页路径
      this.$router.push({ name: 'UserProfile', params: { username } });
    },
    
    // 获取用户关注数量
    getUserFollowingCount(username) {
      if (this.userStats[username] && this.userStats[username].following) {
        return this.userStats[username].following;
      }
      // 模拟数据，实际应该从后端获取
      return Math.floor(Math.random() * 100);
    },
    
    // 获取用户粉丝数量
    getUserFollowerCount(username) {
      if (this.userStats[username] && this.userStats[username].followers) {
        return this.userStats[username].followers;
      }
      // 模拟数据，实际应该从后端获取
      return Math.floor(Math.random() * 50) + 10;
    },

    // 格式化日期
    formatDate(dateString) {
      if (!dateString) return '';
      const date = new Date(dateString);
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      const hours = String(date.getHours()).padStart(2, '0');
      const minutes = String(date.getMinutes()).padStart(2, '0');
      return `${year}-${month}-${day} ${hours}:${minutes}`;
    },

    // 格式化内容（处理图片和换行）
    formatContent(content) {
      if (!content) return '';
      // 将换行符转换为<br>
      let formattedContent = content.replace(/\n/g, '<br>');
      
      // 为内容中的图片URL添加适当的样式
      formattedContent = formattedContent.replace(
        /<img([^>]*)src="([^"]*)"([^>]*)>/g,
        '<img$1src="$2"$3 style="max-width: 100%; height: auto; margin: 10px 0; border-radius: 4px;">'
      );
      
      return formattedContent;
    },

    // 处理提交回答
    handleSubmitAnswer() {
      this.$refs.answerForm.validate(valid => {
        if (valid) {
          const questionId = this.$route.params.id;
          
          // 准备回答数据
          const answerData = {
            content: this.answerForm.content,
            questionId: questionId
          };
          
          // 发送请求提交回答
          this.$axios.post(`/api/questions/${questionId}/answers`, answerData, {
            withCredentials: true,
            headers: {
              'X-Requested-With': 'XMLHttpRequest'
            }
          })
          .then(response => {
            this.$message.success('回答提交成功');
            this.answerForm.content = '';
            // 重新加载问题详情和回答
            this.fetchQuestionDetail();
          })
          .catch(error => {
            console.error('提交回答失败:', error);
            
            // 显示更详细的错误信息
            const errorMsg = error.response?.data ? 
              typeof error.response.data === 'string' ? error.response.data : (error.response.data.message || '提交回答失败') : 
              '提交回答失败，请稍后重试';
            this.$message.error(errorMsg);
            
            // 如果是认证失败，引导用户重新登录
            if (error.response?.status === 401) {
              console.warn('认证失败，清除本地登录状态');
              localStorage.removeItem('username');
              localStorage.removeItem('isAdmin');
              this.isLogin = false;
              
              setTimeout(() => {
                this.$router.push('/login');
              }, 1500);
            }
          });
        }
      });
    },

    // 处理投票
    handleVote(answerId, voteType) {
      if (!this.isLogin) {
        this.$message.warning('请先登录');
        this.$router.push('/login');
        return;
      }
      
      // 构造查询参数
      const params = {
        answerId: answerId,
        isLike: voteType === 'up' // voteType为'up'表示点赞，否则为踩
      };
      
      this.$axios.post('/api/questions/answer/vote', null, {
        params: params,
        withCredentials: true,
        headers: {
          'X-Requested-With': 'XMLHttpRequest'
        }
      })
      .then(response => {
        this.$message.success('投票成功');
        // 重新加载问题详情和回答
        this.fetchQuestionDetail();
      })
      .catch(error => {
        console.error('投票失败:', error);
        
        // 显示更详细的错误信息
        const errorMsg = error.response?.data ? 
          typeof error.response.data === 'string' ? error.response.data : (error.response.data.message || '投票失败') : 
          '投票失败，请稍后重试';
        this.$message.error(errorMsg);
      });
    },

    // 处理设为最佳回答
    handleAcceptAnswer(answerId) {
      if (!this.isLogin) {
        this.$message.warning('请先登录');
        this.$router.push('/login');
        return;
      }
      
      if (!this.isAuthor) {
        this.$message.warning('只有提问者可以设置最佳回答');
        return;
      }
      
      const questionId = this.$route.params.id;
      
      this.$axios.post(`/api/questions/${questionId}/accept-answer/${answerId}`, null, {
        withCredentials: true,
        headers: {
          'X-Requested-With': 'XMLHttpRequest'
        }
      })
      .then(response => {
        this.$message.success('已将该回答设为最佳回答');
        // 重新加载问题详情和回答
        this.fetchQuestionDetail();
      })
      .catch(error => {
        console.error('设为最佳回答失败:', error);
        
        // 显示更详细的错误信息
        const errorMsg = error.response?.data ? 
          typeof error.response.data === 'string' ? error.response.data : (error.response.data.message || '设为最佳回答失败') : 
          '设为最佳回答失败，请稍后重试';
        this.$message.error(errorMsg);
      });
    },
    
    // 设置头像刷新事件监听器
    setupAvatarRefreshListener() {
      
      // 绑定this上下文
      this.boundHandleAvatarUpdated = this.handleAvatarUpdated.bind(this);
      
      // 监听全局事件总线
      if (this.$bus) {
        this.$bus.$on('avatar-updated', this.boundHandleAvatarUpdated);
      }
      
      // 监听window全局事件
      if (window.addEventListener) {
        window.addEventListener('avatar-updated', this.boundHandleAvatarUpdated);
      }
      
      // 监听自定义事件
      document.addEventListener('avatar-updated', this.boundHandleAvatarUpdated);
    },
    
    // 清理头像刷新事件监听器
    cleanupAvatarRefreshListener() {
      
      if (!this.boundHandleAvatarUpdated) return;
      
      // 清理全局事件总线
      if (this.$bus) {
        this.$bus.$off('avatar-updated', this.boundHandleAvatarUpdated);
      }
      
      // 清理window全局事件
      if (window.removeEventListener) {
        window.removeEventListener('avatar-updated', this.boundHandleAvatarUpdated);
      }
      
      // 清理自定义事件
      document.removeEventListener('avatar-updated', this.boundHandleAvatarUpdated);
    },
    
    // 处理头像更新事件
    handleAvatarUpdated(avatarUrl) {
      // 获取当前登录用户名
      const currentUsername = localStorage.getItem('username');
      if (currentUsername) {
        // 更新当前用户的头像映射
        this.userAvatars[currentUsername] = avatarUrl;
        
        // 强制重新渲染组件，确保头像立即更新
        this.$forceUpdate();
      }
    }
  }
};
</script>

<style scoped>
.question-detail-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

/* 简化的样式，专注于问答区域的布局 */
.question-meta,
.answer-header {
  display: flex;
  align-items: center;
}

.question-meta {
  font-size: 14px;
  color: #666;
}

.answer-header {
  justify-content: space-between;
  margin-bottom: 15px;
  flex-wrap: wrap;
}

/* 其他元信息的间距 */
.question-time,
.question-answers,
.question-views,
.answer-time {
  margin-right: 16px;
}

/* 确保用户头像和用户名在同一行 */
.question-author,
.answer-author {
  display: inline-flex;
  align-items: center;
  vertical-align: middle;
}

.question-detail-card {
  background-color: white;
  border-radius: 4px;
}

.question-header {
  padding: 20px 0;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 20px;
}

.question-title {
  font-size: 24px;
  font-weight: 500;
  color: #333;
  margin: 0 0 15px 0;
  line-height: 1.4;
}

.question-content {
  margin-bottom: 30px;
}

.loading-state, .error-state, .no-answers {
  text-align: center;
  padding: 60px 0;
  color: #666;
}

.loading-state .el-icon, .error-state .el-icon, .no-answers .el-icon {
  font-size: 48px;
  margin-bottom: 16px;
  color: #999;
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

.question-images {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  margin-bottom: 30px;
}

.question-image {
  max-width: 300px;
  max-height: 200px;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid #e0e0e0;
}

.answers-section {
  margin-top: 30px;
}

.answers-title {
  font-size: 20px;
  font-weight: 500;
  color: #333;
  margin-bottom: 20px;
}

.answer-input-section {
  margin-bottom: 30px;
}

.answers-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.answer-card {
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  transition: all 0.3s ease;
}

.answer-card:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.answer-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 15px;
  flex-wrap: wrap;
  gap: 15px;
}

.answer-author {
  font-weight: 500;
  color: #333;
}

.answer-time {
  font-size: 14px;
  color: #999;
}

.answer-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.best-answer-tag {
  background-color: #fff1f0;
  color: #ff4d4f;
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 14px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 4px;
}

.best-answer-tag i {
  color: #ff7a45;
}

/* 为最佳回答卡片添加特殊样式 */
.best-answer-card {
  border: 2px solid #ff7a45;
  background-color: #fffaf0;
  position: relative;
  margin-bottom: 30px;
}

.best-answer-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  background-color: #ff7a45;
}

.best-answer-card .answer-content {
  font-size: 16px;
  line-height: 1.8;
  color: #333;
}

.answer-content {
  font-size: 16px;
  line-height: 1.8;
  color: #333;
  word-wrap: break-word;
  overflow-wrap: break-word;
  white-space: pre-wrap;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .question-detail-container {
    padding: 10px;
  }
  
  .question-title {
    font-size: 20px;
  }
  
  .question-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .answer-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .question-image {
    max-width: 100%;
  }
}
</style>