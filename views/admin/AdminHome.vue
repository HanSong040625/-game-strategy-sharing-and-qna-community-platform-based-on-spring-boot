<template>
  <div class="admin-home">
    <div class="admin-header">
      <div class="header-content">
        <div class="header-left">
          <h1>管理员控制台</h1>
        </div>
        <div class="admin-info">
          <span>欢迎，{{ adminName }}</span>
          <el-button type="text" @click="handleLogout">退出登录</el-button>
        </div>
      </div>
    </div>
    
    <div class="admin-content">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="dashboard-card" shadow="hover">
            <div class="card-content">
              <div class="card-icon">
                <i class="el-icon-user-solid"></i>
              </div>
              <div class="card-info">
                <div class="card-value">{{ userCount }}</div>
                <div class="card-label">用户总数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="6">
          <el-card class="dashboard-card" shadow="hover">
            <div class="card-content">
              <div class="card-icon">
                <i class="el-icon-s-platform"></i>
              </div>
              <div class="card-info">
                <div class="card-value">{{ gameCount }}</div>
                <div class="card-label">游戏总数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="6">
          <el-card class="dashboard-card" shadow="hover">
            <div class="card-content">
              <div class="card-icon">
                <i class="el-icon-document"></i>
              </div>
              <div class="card-info">
                <div class="card-value">{{ guideCount }}</div>
                <div class="card-label">攻略总数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="6">
          <el-card class="dashboard-card" shadow="hover">
            <div class="card-content">
              <div class="card-icon">
                <i class="el-icon-chat-dot-round"></i>
              </div>
              <div class="card-info">
                <div class="card-value">{{ questionCount }}</div>
                <div class="card-label">问题总数</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      
      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :span="12">
          <el-card class="quick-actions" shadow="hover">
            <template #header>
              <span>快速操作</span>
            </template>
            <div class="action-buttons">
              <el-button type="primary" @click="goToGameManagement">
                <i class="el-icon-s-platform"></i>
                游戏管理
              </el-button>
              <el-button type="success" @click="goToUserManagement">
                <i class="el-icon-user-solid"></i>
                用户管理
              </el-button>
              <el-button type="warning" @click="goToOperationLogs">
                <i class="el-icon-document"></i>
                操作日志
              </el-button>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="12">
          <el-card class="recent-activities" shadow="hover">
            <template #header>
              <span>最近活动</span>
            </template>
            <div class="activity-list">
              <div v-for="activity in recentActivities" :key="activity.id" class="activity-item">
                <div class="activity-content">
                  <span class="activity-text">{{ activity.content }}</span>
                  <span class="activity-time">{{ activity.time }}</span>
                </div>
              </div>
              <div v-if="recentActivities.length === 0" class="no-activities">
                暂无活动记录
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AdminHome',
  data() {
    return {
      adminName: localStorage.getItem('username') || '管理员',
      userCount: 0,
      gameCount: 0,
      guideCount: 0,
      questionCount: 0,
      recentActivities: []
    }
  },
  mounted() {
    console.log('AdminHome组件已挂载，准备加载统计数据')
    console.log('当前用户名:', this.adminName)
    console.log('初始统计数据值:', {
      userCount: this.userCount,
      gameCount: this.gameCount,
      guideCount: this.guideCount,
      questionCount: this.questionCount
    })
    this.loadDashboardData()
  },
  methods: {
    loadDashboardData() {
      // 加载统计数据
      console.log('开始加载仪表盘统计数据...')
      this.$axios.get('/api/admin/dashboard')
        .then(response => {
          console.log('仪表盘数据响应:', response)
          if (response.data.success) {
            const data = response.data.data
            console.log('获取到的统计数据:')
            console.log('用户总数:', data.userCount || 0)
            console.log('游戏总数:', data.gameCount || 0)
            console.log('攻略总数:', data.guideCount || 0)
            console.log('问答总数:', data.questionCount || 0)
            console.log('最近活动:', data.recentActivities || [])
            
            this.userCount = data.userCount || 0
            this.gameCount = data.gameCount || 0
            this.guideCount = data.guideCount || 0
            this.questionCount = data.questionCount || 0
            this.recentActivities = data.recentActivities || []
          } else {
            console.warn('请求成功但返回了错误状态:', response.data)
          }
        })
        .catch(error => {
          console.error('加载仪表盘数据失败:', error)
          console.error('错误详情:', error.response || error)
        })
    },
    
    goToGameManagement() {
      this.$router.push('/admin/game-management')
    },
    
    goToUserManagement() {
      this.$router.push('/admin/user-management')
    },
    
    goToOperationLogs() {
      this.$router.push('/admin/operation-logs')
    },
    
    handleLogout() {
      this.$confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$axios.post('/api/admin/logout')
          .finally(() => {
            localStorage.removeItem('token')
            localStorage.removeItem('username')
            localStorage.removeItem('userRole')
            this.$message.success('已退出登录')
            this.$router.push('/admin/login')
          })
      })
    },
    

  }
}
</script>

<style scoped>
.admin-home {
  min-height: 100vh;
  background-color: #f5f7fa;
  margin: 0;
  padding: 0;
}

.admin-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 20px 0;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-content h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
}

.admin-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.admin-content {
  max-width: 1200px;
  margin: 20px auto;
  padding: 0 20px;
}

.dashboard-card {
  margin-bottom: 20px;
}

.card-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-icon {
  font-size: 40px;
  color: #409EFF;
}

.card-info {
  text-align: right;
}

.card-value {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
}

.card-label {
  font-size: 14px;
  color: #909399;
  margin-top: 5px;
}

.quick-actions, .recent-activities {
  min-height: 200px;
}

.action-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  justify-content: center;
  align-items: center;
  height: 100%;
}

.action-buttons .el-button {
  flex: 1;
  min-width: 120px;
  padding: 15px 20px;
  font-size: 14px;
  white-space: nowrap;
}

.activity-list {
  max-height: 140px;
  overflow-y: auto;
}

.activity-item {
  padding: 8px 0;
  border-bottom: 1px solid #ebeef5;
}

.activity-item:last-child {
  border-bottom: none;
}

.activity-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.activity-text {
  font-size: 14px;
  color: #606266;
}

.activity-time {
  font-size: 12px;
  color: #909399;
}

.no-activities {
  text-align: center;
  color: #909399;
  font-size: 14px;
  padding: 20px 0;
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    gap: 10px;
    text-align: center;
  }
  
  .admin-info {
    justify-content: center;
  }
}
</style>