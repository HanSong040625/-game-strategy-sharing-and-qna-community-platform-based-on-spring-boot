<template>
  <div class="online-status-container">
    <h2>在线状态设置</h2>
    <div class="status-card">
      <div class="status-info">
        <span class="label">当前状态：</span>
        <el-tag :type="isOnline ? 'success' : 'info'" class="status-tag">
          {{ isOnline ? '在线' : '离线' }}
        </el-tag>
      </div>
      <el-button 
        :type="isOnline ? 'warning' : 'primary'" 
        @click="toggleOnlineStatus"
        :loading="isUpdating"
        class="toggle-button"
      >
        {{ isOnline ? '设置为离线' : '设置为在线' }}
      </el-button>
    </div>
    <div class="debug-section">
      <h3>状态调试</h3>
      <el-button type="info" @click="checkCurrentStatus" size="small">
        检查当前状态
      </el-button>
      <div v-if="debugInfo" class="debug-info">
        <pre>{{ debugInfo }}</pre>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'OnlineStatusToggle',
  data() {
    return {
      isOnline: false,
      isUpdating: false,
      debugInfo: null
    };
  },
  mounted() {
    // 组件加载时检查当前状态
    this.checkCurrentStatus();
  },
  methods: {
    // 切换在线状态
    async toggleOnlineStatus() {
      try {
        this.isUpdating = true;
        console.log('开始切换在线状态...');
        
        // 调用后端接口更新状态
        const newStatus = this.isOnline ? 0 : 1;
        const response = await this.$axios.get(`/api/auth/update-online-status?status=${newStatus}`, {
          withCredentials: true,
          timeout: 5000
        });
        
        console.log('在线状态更新响应:', response.data);
        
        if (response.data && response.data.code === 200) {
          // 更新本地状态
          this.isOnline = newStatus === 1;
          // 更新localStorage中的状态
          localStorage.setItem('isOnline', newStatus.toString());
          // 更新用户对象中的状态
          const userInfo = JSON.parse(localStorage.getItem('user') || '{}');
          userInfo.isOnline = newStatus;
          localStorage.setItem('user', JSON.stringify(userInfo));
          
          this.$message.success('在线状态更新成功！');
        } else {
          this.$message.error(response.data?.message || '更新失败');
        }
      } catch (error) {
        console.error('更新在线状态时出错:', error);
        if (error.response && error.response.status === 401) {
          this.$message.error('登录已过期，请重新登录');
          this.$router.push('/login');
        } else {
          this.$message.error('更新在线状态失败，请稍后重试');
        }
      } finally {
        this.isUpdating = false;
        // 更新调试信息
        this.checkCurrentStatus();
      }
    },
    
    // 检查当前在线状态
    async checkCurrentStatus() {
      try {
        const username = localStorage.getItem('username');
        if (!username) {
          this.$message.warning('未找到用户名，请先登录');
          return;
        }
        
        console.log('检查用户在线状态:', username);
        const response = await this.$axios.get(`/api/auth/debug/online-status/${username}`, {
          withCredentials: true,
          timeout: 5000
        });
        
        console.log('在线状态查询结果:', response.data);
        
        if (response.data) {
          // 更新本地在线状态
          this.isOnline = response.data.isOnline === 1;
          // 更新调试信息
          this.debugInfo = JSON.stringify({
            username: response.data.username,
            isOnline: response.data.isOnline,
            lastLogin: response.data.lastLogin,
            timestamp: new Date().toLocaleString()
          }, null, 2);
        }
      } catch (error) {
        console.error('检查在线状态时出错:', error);
        this.debugInfo = JSON.stringify({
          error: error.message,
          timestamp: new Date().toLocaleString()
        }, null, 2);
      }
    }
  }
};
</script>

<style scoped>
.online-status-container {
  max-width: 600px;
  margin: 0 auto;
  padding: 20px;
}

.status-card {
  background-color: #f5f7fa;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.status-info {
  margin-bottom: 15px;
  display: flex;
  align-items: center;
}

.label {
  font-size: 16px;
  margin-right: 10px;
}

.status-tag {
  font-size: 14px;
  padding: 4px 12px;
}

.toggle-button {
  width: 100%;
  padding: 10px;
  font-size: 16px;
}

.debug-section {
  background-color: #f0f2f5;
  border-radius: 8px;
  padding: 20px;
}

.debug-section h3 {
  margin-top: 0;
  margin-bottom: 15px;
  color: #606266;
}

.debug-info {
  margin-top: 15px;
  background-color: #fff;
  border-radius: 4px;
  padding: 15px;
  overflow-x: auto;
}

.debug-info pre {
  margin: 0;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 14px;
  line-height: 1.6;
  color: #303133;
}
</style>