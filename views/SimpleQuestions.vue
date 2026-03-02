<template>
  <div class="simple-questions-container">
    <div class="page-header">
      <h1>游戏问答专区</h1>
    </div>
    
    <!-- 问答区域 -->
    <div class="questions-section">
      <!-- 问答区域头部 -->
      <div class="section-header">
        <h2>问答列表</h2>
        <div class="filter-bar">
          <el-button type="primary" @click="handleAskQuestion">我要提问</el-button>
        </div>
      </div>

      <!-- 问答列表 -->
      <div class="questions-list">
        <div v-if="loading" class="loading-state">
          <i class="el-icon-loading"></i>
          <p>加载中...</p>
        </div>
        <div v-else-if="error" class="error-state">
          <i class="el-icon-warning"></i>
          <p>{{ error }}</p>
        </div>
        <div v-else-if="questions.length === 0" class="empty-state">
          <el-empty description="暂无问答，快来发布第一个问题吧！"></el-empty>
        </div>
        <div v-else>
          <div v-for="question in questions" :key="question.id" class="question-item" @click="navigateToQuestionDetail(question.id)">
            <div class="question-content">
              <h3 class="question-title">{{ question.title }}</h3>
              <p class="question-summary">{{ getQuestionSummary(question.content) }}</p>
              <div class="question-meta">
                <span class="author"><i class="el-icon-user"></i> {{ question.author?.username || '未知作者' }}</span>
                <span class="time"><i class="el-icon-time"></i> {{ formatDate(question.createTime) }}</span>
                <div class="stats">
                  <span class="answers"><i class="el-icon-chat-dot-square"></i> {{ question.answerCount || 0 }} 回答</span>
                  <span class="views"><i class="el-icon-view"></i> {{ question.viewCount || 0 }} 浏览</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 提问对话框 -->
      <el-dialog
        v-model="askQuestionDialogVisible"
        title="我要提问"
        width="600px"
        :before-close="handleCloseDialog"
      >
        <el-form
          ref="askQuestionForm"
          :model="askQuestionForm"
          :rules="askQuestionRules"
          label-width="80px"
        >
          <el-form-item label="问题标题" prop="title">
            <el-input v-model="askQuestionForm.title" placeholder="请输入问题标题" maxlength="100" show-word-limit />
          </el-form-item>
          <el-form-item label="问题描述" prop="content">
            <el-input
              v-model="askQuestionForm.content"
              type="textarea"
              placeholder="请详细描述你的问题"
              :rows="6"
              maxlength="2000"
              show-word-limit
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="askQuestionDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="handleSubmitQuestion">发布问题</el-button>
          </span>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script>
export default {
  name: 'SimpleQuestions',
  data() {
 
    
    return {
      questions: mockQuestions,
      loading: false,
      error: '',
      askQuestionDialogVisible: false,
      askQuestionForm: {
        title: '',
        content: ''
      },
      askQuestionRules: {
        title: [
          { required: true, message: '请输入问题标题', trigger: 'blur' },
          { min: 5, max: 100, message: '标题长度在 5 到 100 个字符', trigger: 'blur' }
        ],
        content: [
          { required: true, message: '请输入问题描述', trigger: 'blur' },
          { min: 10, max: 2000, message: '描述长度在 10 到 2000 个字符', trigger: 'blur' }
        ]
      }
    };
  },
  mounted() {
    console.log('SimpleQuestions mounted');
    // 组件挂载时加载模拟数据
    this.loadQuestions();
  },
  methods: {
    // 加载问题数据（使用模拟数据）
    loadQuestions() {
      this.loading = true;
      this.error = '';
      
      // 模拟API请求延迟
      setTimeout(() => {
        this.loading = false;
        console.log('Questions loaded successfully');
      }, 500);
    },
    
    // 处理提问
    handleAskQuestion() {
      // 检查用户登录状态
      const username = localStorage.getItem('username');
      if (!username) {
        this.$message.error('请先登录');
        this.$router.push('/login');
        return;
      }
      
      this.askQuestionDialogVisible = true;
    },
    
    // 处理对话框关闭
    handleCloseDialog(done) {
      if (this.$refs.askQuestionForm) {
        this.$refs.askQuestionForm.resetFields();
      }
      done();
    },
    
    // 处理提交问题
    handleSubmitQuestion() {
      this.$refs.askQuestionForm.validate(valid => {
        if (valid) {
          // 准备问题数据
          const questionData = {
            title: this.askQuestionForm.title,
            content: this.askQuestionForm.content
          };
          
          console.log('Submitting question:', questionData);
          
          // 模拟提交成功
          setTimeout(() => {
            this.$message.success('问题发布成功');
            this.askQuestionDialogVisible = false;
            this.$refs.askQuestionForm.resetFields();
            
            // 添加新问题到列表
            const newQuestion = {
              id: Date.now(),
              ...questionData,
              author: { username: localStorage.getItem('username') || '用户' },
              createTime: new Date().getTime(),
              answerCount: 0,
              viewCount: 0
            };
            this.questions.unshift(newQuestion);
          }, 500);
        }
      });
    },
    
    // 格式化日期
    formatDate(dateString) {
      if (!dateString) return '';
      const date = new Date(dateString);
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      return `${year}-${month}-${day}`;
    },
    
    // 获取问题摘要
    getQuestionSummary(content) {
      if (!content) return '';
      // 返回前150个字符
      return content.length > 150 ? content.substring(0, 150) + '...' : content;
    },
    
    // 跳转到问题详情页面
    navigateToQuestionDetail(questionId) {
      this.$router.push(`/question/${questionId}`);
    }
  }
};
</script>

<style scoped>
.simple-questions-container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding: 20px;
}

.page-header {
  text-align: center;
  margin-bottom: 30px;
}

.page-header h1 {
  font-size: 32px;
  color: #333;
  margin: 0;
}

/* 问答区域样式 */
.questions-section {
  max-width: 1200px;
  margin: 0 auto;
  background-color: white;
  border-radius: 8px;
  padding: 30px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
}

/* 头部样式 */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.section-header h2 {
  margin: 0;
  font-size: 24px;
  color: #333;
}

/* 筛选栏样式 */
.filter-bar {
  display: flex;
  align-items: center;
}

/* 问答列表样式 */
.questions-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 问答项样式 */
.question-item {
  padding: 20px 0;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: all 0.3s;
}

.question-item:hover {
  background-color: #fafafa;
  transform: translateX(5px);
}

.question-item:last-child {
  border-bottom: none;
}

/* 问答内容样式 */
.question-content {
  flex: 1;
  min-width: 0;
}

.question-title {
  margin: 0 0 10px 0;
  font-size: 18px;
  color: #333;
  font-weight: bold;
  transition: color 0.3s;
}

.question-item:hover .question-title {
  color: #1890ff;
}

.question-summary {
  margin: 0 0 15px 0;
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

/* 元信息样式 */
.question-meta {
  display: flex;
  align-items: center;
  gap: 20px;
  font-size: 12px;
  color: #999;
}

.question-meta .author,
.question-meta .time {
  display: flex;
  align-items: center;
  gap: 5px;
}

.question-meta .stats {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-left: auto;
}

.question-meta .answers,
.question-meta .views {
  display: flex;
  align-items: center;
  gap: 5px;
}

/* 加载和错误状态 */
.loading-state,
.error-state,
.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #999;
}

.loading-state i,
.error-state i {
  font-size: 48px;
  margin-bottom: 15px;
  display: block;
}

.loading-state i {
  animation: rotate 1s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.error-state i {
  color: #f56c6c;
}
</style>