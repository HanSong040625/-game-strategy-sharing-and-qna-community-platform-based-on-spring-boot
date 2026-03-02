<template>
  <div class="admin-operation-log">
    <div class="admin-header">
      <div class="header-content">
        <div class="header-left">
          <h1>操作日志管理</h1>
        </div>
        <div class="admin-info">
          <span>欢迎，{{ adminName }}</span>
          <el-button type="text" @click="handleLogout">退出登录</el-button>
        </div>
      </div>
    </div>
    
    <div class="admin-content">
      <el-card shadow="hover">
        <template #header>
          <div class="card-header">
            <span>操作日志列表</span>
            <div class="header-actions">
              <el-date-picker
                v-model="dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                @change="handleDateChange"
              />
              <el-select v-model="operationType" placeholder="操作类型" @change="handleFilterChange">
                <el-option label="全部" value="" />
                <el-option label="添加" value="add" />
                <el-option label="编辑" value="edit" />
                <el-option label="删除" value="delete" />
                <el-option label="审核" value="audit" />
              </el-select>
              <el-button type="primary" @click="refreshLogs">刷新</el-button>
            </div>
          </div>
        </template>
        
        <el-table :data="logs" style="width: 100%">
          <el-table-column prop="id" label="日志ID" width="100" />
          <el-table-column prop="adminId" label="管理员ID" width="120" />
          <el-table-column prop="adminName" label="管理员" width="150" />
          <el-table-column prop="operationType" label="操作类型" width="120" />
          <el-table-column prop="targetType" label="目标类型" width="120" />
          <el-table-column prop="targetId" label="目标ID" width="100" />
          <el-table-column prop="description" label="操作描述" />
          <el-table-column prop="result" label="操作结果" width="100">
            <template slot-scope="scope">
              <el-tag :type="scope.row.result === 'success' ? 'success' : 'danger'">
                {{ scope.row.result === 'success' ? '成功' : '失败' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="operationTime" label="操作时间" width="200" />
        </el-table>
        
        <div class="pagination" v-if="total > 0">
          <el-pagination
            background
            layout="prev, pager, next, jumper, ->, total"
            :total="total"
            :page-size="pageSize"
            :current-page="currentPage"
            @current-change="handlePageChange"
          />
        </div>
      </el-card>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AdminOperationLog',
  data() {
    return {
      adminName: localStorage.getItem('username') || '管理员',
      logs: [],
      total: 0,
      currentPage: 1,
      pageSize: 20,
      dateRange: null,
      operationType: ''
    }
  },
  mounted() {
    console.log('AdminOperationLog组件已挂载，准备加载操作日志数据')
    this.loadLogs()
  },
  methods: {
    loadLogs() {
      console.log('开始加载操作日志数据...')
      this.$axios.get('/api/admin/operation-logs', {
        params: {
          page: this.currentPage - 1,
          size: this.pageSize
        }
      })
        .then(response => {
          console.log('操作日志数据响应:', response)
          if (response.data && response.data.success) {
            const data = response.data.data || {}
            this.logs = data.logs || []
            this.total = data.totalItems || 0
            this.currentPage = (data.currentPage || 0) + 1 || 1
          } else {
            this.logs = []
            this.total = 0
          }
        })
        .catch(error => {
          console.error('加载操作日志失败:', error)
          this.$message.error('加载操作日志失败，请重试')
          this.logs = []
          this.total = 0
        })
    },
    
    handlePageChange(page) {
      this.currentPage = page
      this.loadLogs()
    },
    
    handleDateChange() {
      // 日期范围变化处理
      console.log('日期范围变化:', this.dateRange)
      // 这里可以添加根据日期范围过滤的逻辑
    },
    
    handleFilterChange() {
      // 操作类型过滤处理
      console.log('操作类型变化:', this.operationType)
      // 这里可以添加根据操作类型过滤的逻辑
    },
    
    refreshLogs() {
      this.loadLogs()
      this.$message.success('已刷新操作日志')
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
    }
  }
}
</script>

<style scoped>
.admin-operation-log {
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

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
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
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .header-actions {
    width: 100%;
    flex-wrap: wrap;
  }
}
</style>