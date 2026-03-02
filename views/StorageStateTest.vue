<template>
  <div class="storage-state-test">
    <el-card shadow="hover">
      <div slot="header">
        <h2>存储状态保持测试</h2>
        <p>测试localStorage/sessionStorage在页面刷新后的状态保持情况</p>
      </div>
      
      <div class="test-content">
        <!-- 测试控制区域 -->
        <div class="test-controls">
          <el-button type="primary" @click="startTest">开始测试</el-button>
          <el-button type="warning" @click="clearTestData">清除测试数据</el-button>
          <el-button type="info" @click="refreshPage">刷新页面</el-button>
        </div>
        
        <!-- 测试结果展示 -->
        <div class="test-results">
          <h3>测试结果</h3>
          
          <div class="result-section">
            <h4>localStorage状态</h4>
            <el-table :data="localStorageData" border style="width: 100%">
              <el-table-column prop="key" label="键名" width="200"></el-table-column>
              <el-table-column prop="value" label="值" min-width="300"></el-table-column>
              <el-table-column prop="status" label="状态" width="100">
                <template slot-scope="scope">
                  <el-tag :type="scope.row.status === '保持' ? 'success' : 'warning'">
                    {{ scope.row.status }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
          </div>
          
          <div class="result-section">
            <h4>sessionStorage状态</h4>
            <el-table :data="sessionStorageData" border style="width: 100%">
              <el-table-column prop="key" label="键名" width="200"></el-table-column>
              <el-table-column prop="value" label="值" min-width="300"></el-table-column>
              <el-table-column prop="status" label="状态" width="100">
                <template slot-scope="scope">
                  <el-tag :type="scope.row.status === '保持' ? 'success' : 'warning'">
                    {{ scope.row.status }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
          </div>
          
          <div class="result-section">
            <h4>测试日志</h4>
            <div class="log-container">
              <div 
                v-for="(log, index) in logs" 
                :key="index" 
                class="log-item"
                :class="log.type"
              >
                <span class="timestamp">{{ log.timestamp }}</span>
                <span class="message">{{ log.message }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'StorageStateTest',
  data() {
    return {
      localStorageData: [],
      sessionStorageData: [],
      logs: [],
      testStarted: false,
      refreshCount: 0
    }
  },
  mounted() {
    this.log('info', '存储状态测试页面加载完成');
    this.checkStorageState();
    
    // 检查是否有刷新标记
    const refreshMarker = localStorage.getItem('storage_test_refresh_marker');
    if (refreshMarker) {
      this.log('warning', `检测到页面刷新，刷新标记时间: ${new Date(parseInt(refreshMarker)).toISOString()}`);
      localStorage.removeItem('storage_test_refresh_marker');
      this.refreshCount++;
    }
    
    // 记录页面加载时间
    localStorage.setItem('storage_test_page_load_time', Date.now().toString());
  },
  methods: {
    // 添加日志
    log(type, message) {
      const timestamp = new Date().toISOString();
      this.logs.unshift({
        type,
        message,
        timestamp
      });
      
      // 控制台也输出
      const consoleMessage = `[StorageStateTest] ${timestamp} - ${message}`;
      switch (type) {
        case 'error':
          console.error(consoleMessage);
          break;
        case 'warning':
          console.warn(consoleMessage);
          break;
        case 'success':
          console.log(consoleMessage);
          break;
        default:
          console.info(consoleMessage);
      }
    },
    
    // 检查存储状态
    checkStorageState() {
      this.log('info', '开始检查存储状态');
      
      // 检查localStorage
      const localStorageKeys = [
        'username', 'user', 'token', 'userRole', 'userId',
        'storage_test_data', 'storage_test_timestamp',
        'avatar_page_refresh_time', 'storage_test_refresh_marker',
        'storage_test_page_load_time'
      ];
      
      this.localStorageData = localStorageKeys.map(key => {
        const value = localStorage.getItem(key);
        return {
          key,
          value: value ? (key.includes('user') && key !== 'username' ? '有数据' : value) : '空',
          status: value ? '保持' : '丢失'
        };
      });
      
      // 检查sessionStorage
      const sessionStorageKeys = ['username'];
      this.sessionStorageData = sessionStorageKeys.map(key => {
        const value = sessionStorage.getItem(key);
        return {
          key,
          value: value || '空',
          status: value ? '保持' : '丢失'
        };
      });
      
      this.log('success', '存储状态检查完成');
    },
    
    // 开始测试
    startTest() {
      this.log('info', '开始存储状态保持测试');
      this.testStarted = true;
      
      // 设置测试数据
      const testData = {
        timestamp: Date.now(),
        randomValue: Math.random().toString(36).substring(2, 15),
        testType: 'storage_persistence_test'
      };
      
      // 保存到localStorage
      localStorage.setItem('storage_test_data', JSON.stringify(testData));
      localStorage.setItem('storage_test_timestamp', Date.now().toString());
      
      // 保存到sessionStorage
      sessionStorage.setItem('storage_test_data', JSON.stringify(testData));
      sessionStorage.setItem('storage_test_timestamp', Date.now().toString());
      
      this.log('success', '测试数据已设置');
      this.log('info', `localStorage测试数据: ${JSON.stringify(testData)}`);
      this.log('info', `sessionStorage测试数据: ${JSON.stringify(testData)}`);
      
      this.checkStorageState();
      
      // 提示用户刷新页面
      this.$message.success('测试数据已设置，请刷新页面验证状态保持情况');
    },
    
    // 清除测试数据
    clearTestData() {
      this.log('info', '开始清除测试数据');
      
      // 清除localStorage测试数据
      localStorage.removeItem('storage_test_data');
      localStorage.removeItem('storage_test_timestamp');
      localStorage.removeItem('storage_test_refresh_marker');
      localStorage.removeItem('storage_test_page_load_time');
      
      // 清除sessionStorage测试数据
      sessionStorage.removeItem('storage_test_data');
      sessionStorage.removeItem('storage_test_timestamp');
      
      this.log('success', '测试数据已清除');
      this.checkStorageState();
    },
    
    // 刷新页面
    refreshPage() {
      this.log('warning', '准备刷新页面验证状态保持');
      
      // 设置刷新标记
      localStorage.setItem('storage_test_refresh_marker', Date.now().toString());
      
      this.log('info', '页面将在3秒后刷新...');
      this.$message.info('页面将在3秒后刷新，请观察控制台日志');
      
      setTimeout(() => {
        window.location.reload();
      }, 3000);
    }
  }
}
</script>

<style scoped>
.storage-state-test {
  padding: 20px;
}

.test-controls {
  margin-bottom: 20px;
}

.result-section {
  margin-bottom: 30px;
}

.log-container {
  max-height: 300px;
  overflow-y: auto;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 10px;
  background-color: #f5f7fa;
}

.log-item {
  padding: 5px 0;
  border-bottom: 1px solid #ebeef5;
  font-family: 'Courier New', monospace;
  font-size: 12px;
}

.log-item:last-child {
  border-bottom: none;
}

.log-item .timestamp {
  color: #909399;
  margin-right: 10px;
}

.log-item.info .message {
  color: #606266;
}

.log-item.success .message {
  color: #67c23a;
}

.log-item.warning .message {
  color: #e6a23c;
}

.log-item.error .message {
  color: #f56c6c;
}
</style>