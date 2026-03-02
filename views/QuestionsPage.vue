<template>
  <div class="questions-page">
    <div class="page-header">
      <h1>游戏问答专区</h1>
      <p>与其他玩家交流游戏心得，解决游戏中遇到的问题</p>
    </div>
    
    <div class="container">
      <!-- 提问按钮 -->
      <div class="action-bar">
        <el-button type="primary" @click="openAskDialog">
          <i class="el-icon-edit"></i> 我要提问
        </el-button>
      </div>
      
      <!-- 问答列表 -->
      <div class="questions-list">
        <div v-if="loading" class="loading-container">
          <i class="el-icon-loading loading-icon"></i>
          <span>加载问题中...</span>
        </div>
        
        <div v-else-if="questions.length === 0" class="empty-container">
          <el-empty description="暂无问题，快来发布第一个问题吧！" />
        </div>
        
        <div v-else>
          <div v-for="question in questions" :key="question.id" class="question-card">
            <div class="question-header">
              <h3 class="question-title">{{ question.title }}</h3>
              <div class="question-meta">
                <span class="author">{{ question.author }}</span>
                <span class="time">{{ formatDate(question.createTime) }}</span>
              </div>
            </div>
            <div class="question-content">
              {{ getSummary(question.content) }}
            </div>
            <div class="question-footer">
              <div class="stats">
                <span><i class="el-icon-chat-dot-square"></i> {{ question.answerCount || 0 }} 回答</span>
                <span><i class="el-icon-view"></i> {{ question.viewCount || 0 }} 浏览</span>
              </div>
              <el-button type="text" size="small" @click="viewQuestion(question.id)">查看详情</el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 提问对话框 -->
    <el-dialog
      :visible.sync="dialogVisible"
      title="发布问题"
      width="600px"
    >
      <el-form
        ref="questionForm"
        :model="questionForm"
        :rules="formRules"
        label-width="80px"
      >
        <el-form-item label="问题标题" prop="title">
          <el-input v-model="questionForm.title" placeholder="请输入问题标题" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="问题详情" prop="content">
          <el-input
            v-model="questionForm.content"
            type="textarea"
            placeholder="请详细描述你的问题"
            :rows="6"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="所属游戏" prop="gameId">
          <el-select v-model="questionForm.gameId" placeholder="请选择问题所属游戏" filterable>
            <el-option
              v-for="game in games"
              :key="game.id"
              :label="game.name"
              :value="game.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitQuestion">发布</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'QuestionsPage',
  components: {},

  data() {
    return {
      // 初始化空问题数组
      questions: [],
      loading: false,
      dialogVisible: false,
      // 游戏列表，用于选择问题所属游戏
      games: [],
      gameLoading: false,
      questionForm: {
        title: '',
        content: '',
        gameId: null // 用户选择的游戏ID
      },
      formRules: {
        title: [
          { required: true, message: '请输入问题标题', trigger: 'blur' },
          { min: 5, max: 100, message: '标题长度在5-100个字符之间', trigger: 'blur' }
        ],
        content: [
          { required: true, message: '请输入问题详情', trigger: 'blur' },
          { min: 10, max: 2000, message: '问题详情长度在10-2000个字符之间', trigger: 'blur' }
        ],
        gameId: [
          { required: true, message: '请选择问题所属游戏', trigger: 'change' }
        ]
      }
    }
  },
  mounted() {
    this.loadQuestions()
    this.loadGames()
  },
  methods: {
    // 加载问题列表
    loadQuestions() {
      this.loading = true
      this.$axios.get('/api/questions')
        .then(response => {
          if (response.data && response.data.success) {
            this.questions = response.data.data.questions || []
            this.total = response.data.data.total || 0
          } else {
            this.questions = []
            this.total = 0
          }
        })
        .catch(error => {
          console.error('获取问题失败:', error)
          this.$message.error('获取问题列表失败')
          this.questions = []
          this.total = 0
        })
        .finally(() => {
          this.loading = false
        })
    },
    
    // 加载游戏列表
    loadGames() {
      this.gameLoading = true
      // 使用正确的API端点获取游戏列表数据
      this.$axios.get('/api/game/list')
        .then(response => {
          if (response.data && response.data.success) {
            this.games = response.data.data || []
            console.log('获取游戏列表成功:', this.games)
            // 如果有游戏数据，默认选择第一个
            if (this.games && this.games.length > 0) {
              this.questionForm.gameId = this.games[0].id
            }
          } else {
            this.games = []
          }
        })
        .catch(error => {
          console.error('获取游戏列表失败:', {
            status: error.response?.status,
            data: error.response?.data,
            message: error.message,
            config: error.config
          })
          this.$message.error('获取游戏列表失败，请检查网络连接')
          this.games = []
        })
        .finally(() => {
          this.gameLoading = false
        })
    },
    
    // 打开提问对话框
    openAskDialog() {
      // 检查登录状态，与系统统一使用username判断登录状态
      const username = localStorage.getItem('username');
      
      if (!username) {
        this.$message.error('请先登录')
        this.$router.push('/login')
        return
      }
      
      // 重置表单
      this.$nextTick(() => {
        if (this.$refs.questionForm) {
          this.$refs.questionForm.resetFields()
        }
      })
      
      this.dialogVisible = true
    },
    
    // 提交问题
    submitQuestion() {
      this.$refs.questionForm.validate((valid) => {
        if (valid) {
          const username = localStorage.getItem('username')
          const newQuestion = {
            id: Date.now(),
            title: this.questionForm.title,
            content: this.questionForm.content,
            author: username,
            createTime: new Date().getTime(),
            answerCount: 0,
            viewCount: 0
          }
          
          this.dialogVisible = false
          
          // 调用后端API提交问题 - 设置默认gameId为1（通用问答）
          // 安全获取用户信息
          const userStr = localStorage.getItem('user');
          const user = userStr ? JSON.parse(userStr) : null;
          
          // 确保有用户信息
          if (!user) {
            console.error('用户信息不存在，尝试重新获取');
            // 不再创建临时用户对象，提示用户需要完整登录
          this.$message.warning('请完成登录以获取完整用户权限');
          setTimeout(() => {
            this.$router.push('/login');
          }, 1000);
          }
          
          // 确保user对象和user.id存在
          if (!user || !user.id) {
            console.error('用户信息不完整');
          this.$message.warning('请完成登录以获取完整用户权限');
          setTimeout(() => {
            this.$router.push('/login');
          }, 1000);
          }
          
          // 获取更新后的用户信息
          const updatedUserStr = localStorage.getItem('user');
          const updatedUser = updatedUserStr ? JSON.parse(updatedUserStr) : null;
          
          // 准备问题数据，确保gameId正确转换为Long类型
          const questionData = {
            title: this.questionForm.title,
            content: this.questionForm.content,
            gameId: parseInt(this.questionForm.gameId) // 确保转换为数字类型
          };
          

          console.log('gameId类型:', typeof questionData.gameId);

          console.log('准备发布问题:', {data: questionData, username: localStorage.getItem('username'), cookie: document.cookie});
          
          // 发送请求发布问题，使用相对路径并添加认证配置
          // 优化请求配置，确保能够正确传递用户信息
          this.$axios.post('/api/questions/question/create', questionData, {
            withCredentials: true, // 显式设置，确保cookie传递
            headers: {
              'X-Requested-With': 'XMLHttpRequest', // 添加XHR标识
              'Content-Type': 'application/json' // 显式设置内容类型
            }
          })
          .then(response => {
            console.log('发布问题成功:', response);
            this.loadQuestions()
            this.$message.success('问题发布成功')
          })
          .catch(error => {
            console.error('发布问题失败:', {
              status: error.response?.status,
              data: error.response?.data,
              message: error.message,
              config: error.config
            });
            
            // 显示更详细的错误信息
            const errorMsg = error.response?.data ? 
              typeof error.response.data === 'string' ? error.response.data : (error.response.data.message || '问题提交失败') : 
              '问题提交失败，请稍后重试';
            this.$message.error(errorMsg);
            
            // 如果是认证失败，引导用户重新登录
            if (error.response?.status === 401) {
              console.warn('认证失败，清除本地登录状态');
              // 清除本地登录状态
              localStorage.removeItem('username');
              localStorage.removeItem('isAdmin');
              
              // 延迟跳转，让用户看到错误信息
              setTimeout(() => {
                this.$router.push('/login');
              }, 1500);
            } else if (error.request) {
              // 请求已发送但未收到响应
              this.$message.error('服务器无响应，请检查网络连接');
            }
          })
          .finally(() => {
            this.loading = false
          })
        }
      })
    },
    
    // 查看问题详情
    viewQuestion(id) {
      this.$router.push(`/question/${id}`)
    },
    
    // 格式化日期
    formatDate(time) {
      const date = new Date(time)
      const now = new Date()
      const diff = now - date
      
      if (diff < 60000) {
        return '刚刚'
      } else if (diff < 3600000) {
        return Math.floor(diff / 60000) + '分钟前'
      } else if (diff < 86400000) {
        return Math.floor(diff / 3600000) + '小时前'
      } else {
        const month = date.getMonth() + 1
        const day = date.getDate()
        return `${month}月${day}日`
      }
    },
    
    // 获取内容摘要
    getSummary(content) {
      return content.length > 150 ? content.substring(0, 150) + '...' : content
    }
  }
}
</script>

<style scoped>
.questions-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-top: 220px; /* 为导航栏留出空间 */
  padding-bottom: 50px;
}

.page-header {
  text-align: center;
  margin-bottom: 40px;
}

.page-header h1 {
  font-size: 36px;
  color: #333;
  margin-bottom: 10px;
}

.page-header p {
  font-size: 16px;
  color: #666;
}

.container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 0 20px;
}

.action-bar {
  margin-bottom: 20px;
}

.questions-list {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.loading-container,
.empty-container {
  text-align: center;
  padding: 60px 20px;
  color: #999;
}

.loading-icon {
  font-size: 36px;
  margin-bottom: 10px;
}

.question-card {
  padding: 20px 0;
  border-bottom: 1px solid #f0f0f0;
  transition: all 0.3s;
}

.question-card:hover {
  background-color: #fafafa;
  transform: translateX(5px);
}

.question-card:last-child {
  border-bottom: none;
}

.question-header h3 {
  margin: 0 0 10px 0;
  font-size: 18px;
  color: #333;
  font-weight: bold;
}

.question-meta {
  display: flex;
  gap: 20px;
  font-size: 14px;
  color: #999;
  margin-bottom: 15px;
}

.question-content {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  margin-bottom: 15px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.question-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
}

.stats {
  display: flex;
  gap: 20px;
  color: #999;
}

.stats i {
  margin-right: 5px;
}
</style>