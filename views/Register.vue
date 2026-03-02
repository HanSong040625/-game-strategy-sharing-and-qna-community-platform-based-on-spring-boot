<template>
  <div class="register-container">
    <el-card title="用户注册" shadow="hover" style="width: 400px; margin: 100px auto;">
      <el-form :model="registerForm" :rules="registerRules" ref="registerForm" label-width="80px">
        <!-- 用户名输入框 -->
        <el-form-item label="用户名" prop="username">
          <el-input v-model="registerForm.username" placeholder="请输入用户名"></el-input>
        </el-form-item>
        <!-- 密码输入框 -->
        <el-form-item label="密码" prop="password">
          <el-input type="password" v-model="registerForm.password" placeholder="请输入密码"></el-input>
        </el-form-item>
        <!-- 确认密码输入框 -->
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input type="password" v-model="registerForm.confirmPassword" placeholder="请再次输入密码"></el-input>
        </el-form-item>
        <!-- 邮箱输入框 -->
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="registerForm.email" placeholder="请输入邮箱"></el-input>
        </el-form-item>

        <!-- 按钮区域 -->
        <el-form-item>
          <el-button type="primary" @click="handleRegister" style="width: 100%">注册</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'Register',
  data() {
    return {
      // 注册表单数据
      registerForm: {
        username: '',
        password: '',
        confirmPassword: '',
        email: ''
      },
      // 表单验证规则
      registerRules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
          { min: 3, max: 20, message: '用户名长度应在3-20个字符之间', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请再次输入密码', trigger: 'blur' },
          {
            validator: (rule, value, callback) => {
              if (value !== this.registerForm.password) {
                callback(new Error('两次输入的密码不一致'));
              } else {
                callback();
              }
            },
            trigger: 'blur'
          }
        ],
        email: [
          { required: true, message: '请输入邮箱', trigger: 'blur' },
          { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
        ]
      }
    }
  },
  methods: {
    // 注册按钮点击事件
    handleRegister() {
      // 先做表单验证
      this.$refs.registerForm.validate((isValid) => {
        if (isValid) {
          // 验证通过，发请求到后端注册接口
          const params = new URLSearchParams();
          params.append('username', this.registerForm.username);
          params.append('password', this.registerForm.password);
          params.append('email', this.registerForm.email);
          
          console.log('准备发送注册请求:', this.registerForm);
          // 由于axios配置了baseURL，这里只需要相对路径
          console.log('请求URL: /api/auth/register');
          console.log('请求参数:', params.toString());
          
          this.$axios.post('/api/auth/register', params, {
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
          })
          .then(res => {
            console.log('注册请求响应:', res);
            if (res.data && res.data.code === 200) {
              this.$message.success('注册成功！请登录');
              // 注册成功后跳转到登录页
              this.$router.push('/login');
            } else {
              console.error('注册失败，响应内容:', res.data);
              this.$message.error(res.data ? res.data.message : '注册失败');
            }
          })
          .catch(error => {
            console.error('注册请求错误:', error);
            if (error.response) {
              console.error('错误状态码:', error.response.status);
              console.error('错误响应:', error.response.data);
              
              // 提取后端返回的具体错误信息
              let errorMessage = '注册失败，请重试！';
              if (error.response.data) {
                // 尝试解析JSON响应
                try {
                  const errorData = typeof error.response.data === 'string' 
                    ? JSON.parse(error.response.data) 
                    : error.response.data;
                  
                  if (errorData.message) {
                    errorMessage = errorData.message;
                  }
                } catch (parseError) {
                  // 如果解析失败，使用原始错误信息
                  if (typeof error.response.data === 'string') {
                    errorMessage = error.response.data;
                  }
                }
              }
              this.$message.error(errorMessage);
            } else {
              this.$message.error('注册失败，请重试！');
            }
          });
        }
      });
    }
  }
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f0f2f5;
}
</style>