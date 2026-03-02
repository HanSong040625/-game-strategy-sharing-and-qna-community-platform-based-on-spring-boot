<template>
  <div class="user-management-container">
    <el-card title="用户管理" shadow="hover">
      <!-- 返回按钮 -->
      <div class="header-actions">
        <el-button @click="goBack" class="back-button">返回后台</el-button>
      </div>
      
      <!-- 搜索和筛选区域 -->
      <div class="search-filter">
        <el-input 
          v-model="searchKeyword" 
          placeholder="搜索用户名或邮箱" 
          prefix-icon="el-icon-search"
          style="width: 300px; margin-right: 10px;"
        ></el-input>
        <el-button type="primary" @click="filterUsers">搜索</el-button>
      </div>
      
      <!-- 用户列表 -->
      <div class="user-list">
        <el-table 
          :data="filteredUsers" 
          style="width: 100%"
          stripe
          border
        >
          <el-table-column prop="id" label="用户ID" width="80" align="center"></el-table-column>
          <el-table-column prop="username" label="用户名" width="180"></el-table-column>
          <el-table-column prop="email" label="邮箱" width="250"></el-table-column>
          <el-table-column prop="registrationDate" label="注册日期" width="180"></el-table-column>
          <el-table-column prop="lastLogin" label="最后登录时间" width="180"></el-table-column>
          <el-table-column prop="isOnline" label="在线状态" width="100" align="center">
            <template slot-scope="scope">
              <el-tag :type="scope.row.isOnline ? 'success' : 'info'">{{ scope.row.isOnline ? '在线' : '离线' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="用户状态" width="100" align="center">
            <template slot-scope="scope">
              <el-tag :type="scope.row.status === 'active' ? 'success' : 'danger'">
                {{ scope.row.status === 'active' ? '活跃' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" align="center">
            <template slot-scope="scope">
              <el-button 
                size="small" 
                type="danger" 
                @click="toggleUserStatus(scope.row)"
                :disabled="scope.row.isAdmin"
              >
                {{ scope.row.status === 'active' ? '禁用' : '启用' }}
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          background
          layout="prev, pager, next"
          :total="filteredUsers.length"
          :page-size="pageSize"
          v-model="currentPage"
          @current-change="handleCurrentChange"
        ></el-pagination>
      </div>
      
      <!-- 加载状态 -->
      <el-dialog
        :visible.sync="loading"
        title="加载中"
        :show-close="false"
        :close-on-click-modal="false"
        width="30%"
      >
        <el-row type="flex" justify="center" align="middle">
          <el-col :span="24" style="text-align: center;">
            <el-loading :fullscreen="false" text="正在获取用户数据..."></el-loading>
          </el-col>
        </el-row>
      </el-dialog>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'UserManagement',
  data() {
    return {
      users: [], // 所有用户数据
      searchKeyword: '', // 搜索关键字
      currentPage: 1, // 当前页码
      pageSize: 10, // 每页显示数量
      loading: false // 加载状态
    };
  },
  computed: {
    // 过滤后的用户列表（排除管理员）
    filteredUsers() {
      let result = this.users.filter(user => !user.isAdmin);
      
      // 如果有搜索关键字，进行搜索过滤
      if (this.searchKeyword) {
        const keyword = this.searchKeyword.toLowerCase();
        result = result.filter(user => 
          user.username.toLowerCase().includes(keyword) ||
          user.email.toLowerCase().includes(keyword)
        );
      }
      
      return result;
    },
    // 分页后的用户列表
    paginatedUsers() {
      const start = (this.currentPage - 1) * this.pageSize;
      const end = start + this.pageSize;
      return this.filteredUsers.slice(start, end);
    }
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
  created() {
    // 组件创建时加载用户数据
    this.loadUsers();
  },
  methods: {
    // 返回后台首页
    goBack() {
      this.$router.push('/admin');
    },
    
    // 加载所有用户数据
    loadUsers() {
      this.loading = true;
      console.log('正在加载用户数据...');
      
      // 调用后端API获取真实用户数据
      this.$axios.get('/api/admin/users')
        .then(response => {
          const data = response.data;
          if (data.code === 200) {
            // 处理返回的用户数据
            this.users = data.data.map(user => ({
              ...user,
              // 确保每个用户都有status字段
              status: user.status || 'active',
              // 确保isOnline字段存在且为布尔值
              isOnline: user.isOnline === true
            }));
          } else {
            this.$message.error('获取用户数据失败: ' + data.message);
          }
          this.loading = false;
        })
        .catch(error => {
          console.error('获取用户列表失败:', error);
          this.$message.error('获取用户数据失败，请稍后重试');
          this.loading = false;
        });
    },
    
    // 过滤用户
    filterUsers() {
      console.log('搜索用户:', this.searchKeyword);
      // 重置到第一页
      this.currentPage = 1;
    },
    
    // 处理页码变化
    handleCurrentChange(val) {
      console.log('切换到页码:', val);
      this.currentPage = val;
    },
    
    // 切换用户状态（启用/禁用）
    toggleUserStatus(user) {
      console.log('切换用户状态:', user.username, '当前状态:', user.status);
      
      this.$confirm(`确定要${user.status === 'active' ? '禁用' : '启用'}用户「${user.username}」吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // 在实际项目中，这里应该调用后端API更新用户状态
        // 例如：/api/user/toggle-status
        
        // 模拟API调用成功
        setTimeout(() => {
          // 更新本地数据
          user.status = user.status === 'active' ? 'disabled' : 'active';
          
          this.$message({
            type: 'success',
            message: `用户「${user.username}」${user.status === 'active' ? '启用' : '禁用'}成功`
          });
        }, 500);
      }).catch(() => {
        this.$message({
          type: 'info',
          message: '取消操作'
        });
      });
    }
  }
};
</script>

<style scoped>
.user-management-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.header-actions {
  margin-bottom: 20px;
  text-align: right;
}

.back-button {
  margin-bottom: 10px;
}

.search-filter {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
}

.user-list {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}
</style>