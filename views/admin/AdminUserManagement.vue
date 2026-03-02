<template>
  <div class="admin-user-management">
    <div class="admin-header">
      <div class="header-content">
        <div class="header-left">
          <el-button type="primary" size="small" icon="el-icon-arrow-left" @click="handleBack" class="back-button">返回控制台</el-button>
          <h1>用户管理</h1>
        </div>
        <el-button type="primary" @click="handleAddUser">
          <i class="el-icon-plus"></i>
          添加用户
        </el-button>
      </div>
    </div>
    
    <div class="admin-content">
      <el-card shadow="hover">
        <template #header>
          <div class="table-header">
            <span>用户列表</span>
            <div class="search-area">
              <el-input
                v-model="searchKeyword"
                placeholder="搜索用户名或邮箱"
                style="width: 200px; margin-right: 10px;"
                clearable
                @clear="handleSearch"
                @keyup.enter="handleSearch"
              >
                <template #prefix>
                  <i class="el-icon-search"></i>
                </template>
              </el-input>
              <el-select
                v-model="filterRole"
                placeholder="用户角色"
                style="width: 120px; margin-right: 10px;"
                clearable
                @change="handleSearch"
              >
                <el-option label="普通用户" value="user"></el-option>
                <el-option label="管理员" value="admin"></el-option>
              </el-select>
              <el-select
                v-model="filterStatus"
                placeholder="用户状态"
                style="width: 120px; margin-right: 10px;"
                clearable
                @change="handleSearch"
              >
                <el-option label="正常" value="active"></el-option>
                <el-option label="禁用" value="inactive"></el-option>
              </el-select>
              <el-button type="primary" @click="handleSearch">搜索</el-button>
            </div>
          </div>
        </template>
        
        <el-table
          :data="filteredUsers"
          v-loading="loading"
          style="width: 100%"
          stripe
        >
          <el-table-column prop="id" label="ID" width="80" align="center"></el-table-column>
          <el-table-column prop="username" label="用户名" min-width="120"></el-table-column>
          <el-table-column prop="email" label="邮箱" min-width="180"></el-table-column>
          <el-table-column prop="role" label="角色" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.role === 'admin' ? 'danger' : 'primary'">
                {{ row.role === 'admin' ? '管理员' : '普通用户' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === 'active' ? 'success' : 'danger'">
                {{ row.status === 'active' ? '正常' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="注册时间" width="180" align="center">
            <template #default="{ row }">
              {{ formatDate(row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column prop="lastLogin" label="最后登录" width="180" align="center">
            <template #default="{ row }">
              {{ formatDate(row.lastLogin) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="250" align="center" fixed="right">
            <template #default="{ row }">
              <el-button
                size="small"
                type="primary"
                @click="handleEditUser(row)"
              >
                编辑
              </el-button>
              <el-button
                size="small"
                :type="row.status === 'active' ? 'warning' : 'success'"
                @click="handleToggleStatus(row)"
              >
                {{ row.status === 'active' ? '禁用' : '启用' }}
              </el-button>
              <el-button
                size="small"
                type="danger"
                @click="handleDeleteUser(row)"
                :disabled="row.role === 'admin'"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          ></el-pagination>
        </div>
      </el-card>
    </div>
    
    <!-- 添加/编辑用户对话框 -->
    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      width="500px"
      :before-close="handleDialogClose"
    >
      <el-form
        ref="userForm"
        :model="userForm"
        :rules="userRules"
        label-width="80px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" placeholder="请输入用户名"></el-input>
        </el-form-item>
        
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" placeholder="请输入邮箱地址"></el-input>
        </el-form-item>
        
        <el-form-item label="密码" prop="password" v-if="!currentUserId">
          <el-input
            v-model="userForm.password"
            type="password"
            placeholder="请输入密码"
            show-password
          ></el-input>
        </el-form-item>
        
        <el-form-item label="确认密码" prop="confirmPassword" v-if="!currentUserId">
          <el-input
            v-model="userForm.confirmPassword"
            type="password"
            placeholder="请确认密码"
            show-password
          ></el-input>
        </el-form-item>
        
        <el-form-item label="用户角色" prop="role">
          <el-radio-group v-model="userForm.role">
            <el-radio label="user">普通用户</el-radio>
            <el-radio label="admin">管理员</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="用户状态" prop="status">
          <el-radio-group v-model="userForm.status">
            <el-radio label="active">正常</el-radio>
            <el-radio label="inactive">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="handleDialogClose">取消</el-button>
        <el-button type="primary" @click="handleSubmitUser" :loading="submitting">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'AdminUserManagement',
  data() {
    const validatePassword = (rule, value, callback) => {
      if (!this.currentUserId && !value) {
        callback(new Error('请输入密码'))
      } else if (value && value.length < 6) {
        callback(new Error('密码长度不能少于6位'))
      } else {
        callback()
      }
    }
    
    const validateConfirmPassword = (rule, value, callback) => {
      if (!this.currentUserId && !value) {
        callback(new Error('请确认密码'))
      } else if (value !== this.userForm.password) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    }
    
    return {
      users: [],
      loading: false,
      searchKeyword: '',
      filterRole: '',
      filterStatus: '',
      currentPage: 1,
      pageSize: 10,
      total: 0,
      
      dialogVisible: false,
      dialogTitle: '添加用户',
      submitting: false,
      currentUserId: null,
      
      userForm: {
        username: '',
        email: '',
        password: '',
        confirmPassword: '',
        role: 'user',
        status: 'active'
      },
      
      userRules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
          { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
        ],
        email: [
          { required: true, message: '请输入邮箱地址', trigger: 'blur' },
          { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
        ],
        password: [
          { validator: validatePassword, trigger: 'blur' }
        ],
        confirmPassword: [
          { validator: validateConfirmPassword, trigger: 'blur' }
        ],
        role: [
          { required: true, message: '请选择用户角色', trigger: 'change' }
        ],
        status: [
          { required: true, message: '请选择用户状态', trigger: 'change' }
        ]
      }
    }
  },
  computed: {
    filteredUsers() {
      let filtered = this.users
      
      if (this.searchKeyword) {
        filtered = filtered.filter(user => 
          user.username.toLowerCase().includes(this.searchKeyword.toLowerCase()) ||
          user.email.toLowerCase().includes(this.searchKeyword.toLowerCase())
        )
      }
      
      if (this.filterRole) {
        filtered = filtered.filter(user => user.role === this.filterRole)
      }
      
      if (this.filterStatus) {
        filtered = filtered.filter(user => user.status === this.filterStatus)
      }
      
      return filtered
    }
  },
  mounted() {
    this.loadUsers()
  },
  methods: {
    loadUsers() {
      this.loading = true//loading用于处理加载指示器
      this.$axios.get('/api/admin/users',
        {//$axios的路径是/api/admin/users，匹配admincontroller文件的@GetMapping("/users")接口，参数传向这里。
          params:{
            page:this.currentPage,
            pageSize:this.pageSize
          }
        }
      )//向后端发送http get请求，获取所有用户的列表数据
      //this.$axios是vue实例上的axios实例，用于发起http请求，get用于获取数据
      .then(response => {//.then用于处理异步操作成功响应；response处理后端api返回的数据
        console.log('获取用户列表响应：',response)
        if(response.data && response.data.success){
          let responseData = response.data.data || {}
          let userList = responseData.users || [] //response.data是后端api返回的完整响应对象
          this.total = responseData.total || 0//total控制总记录数
          //后端返回的始终是当前页的数据，response存储的就是后端返回的数据，所以response存储当前页用户数据集合
          this.users = userList.map(user => ({
            id:user.id,
            username:user.username,
            email:user.email,
            role:user.isAdmin?'admin':'user',
            status:user.isBanned?'inactive':'active',
            createdAt:user.registrationDate,//用户注册时间
            lastLogin:user.lastLogin
          }))
          console.log('转换后的用户列表',this.users)
        }else{
          console.error('获取用户列表失败：',response.data.message)
          this.$message.error('获取用户列表失败：' + response.data.message)
          //消息提示组件
        }
        this.loading = false
      })
    },
    
    handleSearch() {
      this.currentPage = 1
      this.loadUsers()
    },
    
    handleSizeChange(size) {
      this.pageSize = size
      this.loadUsers()
    },
    
    handleCurrentChange(page) {
      this.currentPage = page
      this.loadUsers()
    },
    
    handleAddUser() {
      this.dialogTitle = '添加用户'
      this.currentUserId = null
      this.resetForm()
      this.dialogVisible = true
    },
    
    handleEditUser(user) {
      this.dialogTitle = '编辑用户'
      this.currentUserId = user.id
      this.userForm = {
        username: user.username,
        email: user.email,
        password: '',
        confirmPassword: '',
        role: user.role,
        status: user.status
      }
      this.dialogVisible = true
    },
    
    handleToggleStatus(user) {
      const newStatus = user.status === 'active' ? 'inactive' : 'active'
      const action = newStatus === 'active' ? '启用' : '禁用'
      
      this.$confirm(`确定要${action}用户"${user.username}"吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$axios.put(`/api/admin/users/${user.id}/status`, { status: newStatus })
          .then(response => {
            if (response.data && response.data.success) {
              this.$message.success(`${action}成功`)
              this.loadUsers()
            } else {
              console.error(`${action}用户失败：`, response.data.message)
              this.$message.error(`${action}失败：${response.data.message}`)
            }
          })
          .catch(error => {
            console.error(`${action}用户失败:`, error)
            this.$message.error(`${action}失败:网络错误`)
          })
      })
    },
    
    handleDeleteUser(user) {
      this.$confirm(`确定要删除用户"${user.username}"吗？此操作不可恢复！`, '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'error'
      }).then(() => {
        this.$axios.delete(`/api/admin/users/${user.id}`)
          .then(response => {
            if (response.data && response.data.success) {
              this.$message.success('删除成功')
              this.loadUsers()
            }
          })
          .catch(error => {
            console.error('删除用户失败:', error)
            this.$message.error('删除失败')
          })
      })
    },
    
    // 返回首页
    handleBack() {
      this.$router.push('/admin/home');
    },
    
    handleSubmitUser() {
      this.$refs.userForm.validate((valid) => {
        if (valid) {
          this.submitting = true
          
          const formData = { ...this.userForm }
          if (this.currentUserId) {
            // 编辑时不需要密码字段，除非用户输入了新密码
            if (!formData.password) {
              delete formData.password
              delete formData.confirmPassword
            }
          }
          
          const request = this.currentUserId 
            ? this.$axios.put(`/api/admin/users/${this.currentUserId}`, formData)
            : this.$axios.post('/api/admin/users', formData)
          
          request
            .then(response => {
              if (response.data && response.data.success) {
                this.$message.success(this.currentUserId ? '编辑成功' : '添加成功')
                this.dialogVisible = false
                this.loadUsers()
              }
            })
            .catch(error => {
              console.error('操作失败:', error)
              this.$message.error('操作失败')
            })
            .finally(() => {
              this.submitting = false
            })
        }
      })
    },
    
    handleDialogClose() {
      this.dialogVisible = false
      this.resetForm()
    },
    
    resetForm() {
      this.userForm = {
        username: '',
        email: '',
        password: '',
        confirmPassword: '',
        role: 'user',
        status: 'active'
      }
      if (this.$refs.userForm) {
        this.$refs.userForm.clearValidate()
      }
    },
    
    formatDate(dateString) {
      if (!dateString) return '从未登录'
      return new Date(dateString).toLocaleString('zh-CN')
    }
  }
}
</script>

<style scoped>
.admin-user-management {
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
  gap: 15px;
}

.header-content h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
}

.back-button {
  background-color: rgba(255, 255, 255, 0.2);
  border-color: white;
  color: white;
}

.back-button:hover {
  background-color: rgba(255, 255, 255, 0.3);
  border-color: white;
  color: white;
}

.admin-content {
  max-width: 1200px;
  margin: 20px auto;
  padding: 0 20px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-area {
  display: flex;
  align-items: center;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

@media (max-width: 768px) {
  .table-header {
    flex-direction: column;
    gap: 10px;
    align-items: stretch;
  }
  
  .search-area {
    flex-wrap: wrap;
    gap: 10px;
    justify-content: space-between;
  }
  
  .search-area .el-input,
  .search-area .el-select {
    flex: 1;
    min-width: 120px;
  }
}
</style>