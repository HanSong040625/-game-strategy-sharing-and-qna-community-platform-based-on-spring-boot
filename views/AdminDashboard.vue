<template>
  <div class="admin-container">
    <el-card title="管理员后台" shadow="hover">
      <div class="admin-welcome">
        <h2>欢迎访问管理员后台</h2>
        <p>您当前以管理员身份登录系统</p>
      </div>
      

      
      <div class="admin-actions">
        <h3>管理操作</h3>
        <el-button type="primary" @click="manageUsers" class="action-button">用户管理</el-button>
        <el-button type="primary" @click="manageGames" class="action-button">游戏管理</el-button>
        <el-button type="primary" @click="manageOperationLogs" class="action-button">操作日志管理</el-button>
        <el-button type="primary" @click="systemSettings" class="action-button">系统设置</el-button>
      </div>
      
      <div class="admin-info">
        <el-alert
          title="管理员提示"
          type="info"
          show-icon
          :closable="false"
        >
          此页面仅供管理员查看，请确保妥善保管您的账号信息。
        </el-alert>
      </div>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'AdminDashboard',
  data() {
    return {
    };
  },
  // 路由守卫：检查用户是否登录且是管理员
  beforeRouteEnter(to, from, next) {
    // 检查localStorage中是否有username
    const username = localStorage.getItem('username');
    
    if (username) {
      // 这里需要检查用户是否为管理员
      const isAdmin = localStorage.getItem('isAdmin') === 'true';
      
      if (isAdmin) {
       
        next();
      } else {
     
        next('/home');
      }
    } else {
    
      next('/login');
    }
  },
  methods: {
    // 用户管理操作
    manageUsers() {
      this.$router.push('/admin/user-management');
    },
    // 游戏管理操作
    manageGames() {
      this.$router.push('/admin/game-management');
    },
    // 操作日志管理操作
    manageOperationLogs() {
      this.$router.push('/admin/operation-logs');
    },
    // 系统设置操作
    systemSettings() {
      this.$message.info('系统设置功能尚未实现');
    }
  }
}
</script>

<style scoped>
.admin-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.admin-welcome {
  margin-bottom: 30px;
  text-align: center;
}

.admin-welcome h2 {
  color: #409eff;
  margin-bottom: 10px;
}

.admin-stats {
  margin-bottom: 30px;
}

.admin-actions {
  margin-bottom: 30px;
  text-align: center;
}

.admin-actions h3 {
  margin-bottom: 20px;
  color: #333;
}

.action-button {
  margin: 0 10px;
}

.admin-info {
  margin-top: 30px;
}
</style>