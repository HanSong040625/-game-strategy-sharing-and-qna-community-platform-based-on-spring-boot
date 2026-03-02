<template>
  <div class="admin-login-container">
    <div class="login-box">
      <div class="login-header">
        <h2>管理员登录</h2>
        <p>请使用管理员账号登录系统</p>
      </div>
      
      <el-form ref="loginForm" :model="loginForm" :rules="loginRules" class="login-form">
         <!--  表单项容器，用来包裹一个具体的输入控件（这里是 <el-input>） 
              prop="username" 这个输入框的值需要按照 rules（校验规则）中名为 "username" 的规则进行验证
              
              
              -->
        <el-form-item prop="username"> 
              <!--  
              输入框组件，是用户实际输入内容的地方。
              
              -->
          <el-input
            v-model="loginForm.username"
            prefix-icon="el-icon-user"
            placeholder="管理员账号"
            size="large"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            prefix-icon="el-icon-lock"
            type="password"
            placeholder="密码"
            size="large"
            show-password
          />
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="login-btn"
            :loading="loading"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="login-footer">
        <el-link type="primary" @click="goToUserLogin">
          ← 返回用户登录
        </el-link>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AdminLogin',
  data() {
    return {
      loginForm: {
        username: '',
        password: ''
      },
      loading: false,
      loginRules: {
        username: [
          { required: true, message: '请输入管理员账号', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' }
        ]
      }
    }
  },
  methods: {
    handleLogin() {
      this.$refs.loginForm.validate((valid) => {
        if (valid) {
          this.loading = true
          
          // 准备表单格式的参数
          const params = new URLSearchParams()
          params.append('username', this.loginForm.username)
          params.append('password', this.loginForm.password)
          
          this.$axios.post('/api/admin/login', params, {
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            withCredentials: true
          })
            .then(response => {
              if (response.data.code === 200) {
                // 存储管理员信息
                localStorage.setItem('token', response.data.data.token)
                localStorage.setItem('username', response.data.data.username)
                localStorage.setItem('userRole', 'admin')
                localStorage.setItem('isAdmin', 'true')
                
                this.$message.success('管理员登录成功')
                this.$router.push('/admin/home')
              } else {
                this.$message.error(response.data.message || '登录失败')
              }
            })
            .catch(error => {
              console.error('登录失败:', error)
              this.$message.error('登录失败，请检查账号密码')
            })
            .finally(() => {
              this.loading = false
            })
        }
      })
    },
    
    goToUserLogin() {
      this.$router.push('/login')
    }
  }
}
</script>

<style scoped>
.admin-login-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.login-box {
  background: white;
  border-radius: 10px;
  padding: 40px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 400px;
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h2 {
  color: #333;
  margin-bottom: 10px;
  font-weight: 600;
}

.login-header p {
  color: #666;
  font-size: 14px;
}

.login-form {
  margin-bottom: 20px;
}

.login-btn {
  width: 100%;
  margin-top: 10px;
}

.login-footer {
  text-align: center;
  border-top: 1px solid #eee;
  padding-top: 20px;
}

@media (max-width: 480px) {
  .login-box {
    padding: 30px 20px;
  }
}
</style>