<template>
  <div class="user-info-container">
    <el-card title="我的信息" shadow="hover">
      <!-- 用户基本信息 -->
      <div class="user-info-section">
        <h3>基本信息</h3>
        <div class="info-item">
          <span class="info-label">用户名：</span>
          <span class="info-value">{{ userInfo.username }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">邮箱：</span>
          <span class="info-value">{{ userInfo.email }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">注册时间：</span>
          <span class="info-value">{{ formatDate(userInfo.createTime) }}</span>
        </div>

      </div>

      <!-- 修改密码区域 -->
      <div class="password-section">
        <h3>修改密码</h3>
        <el-form :model="passwordForm" :rules="passwordRules" ref="passwordForm" label-width="100px">
          <el-form-item label="当前密码" prop="currentPassword">
            <el-input type="password" v-model="passwordForm.currentPassword" placeholder="请输入当前密码"></el-input>
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input type="password" v-model="passwordForm.newPassword" placeholder="请输入新密码"></el-input>
          </el-form-item>
          <el-form-item label="确认新密码" prop="confirmNewPassword">
            <el-input type="password" v-model="passwordForm.confirmNewPassword" placeholder="请再次输入新密码"></el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handlePasswordChange">修改密码</el-button>
            <el-button @click="resetPasswordForm">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 修改个人信息区域 -->
      <div class="edit-info-section">
        <h3>修改个人信息</h3>
        <el-form :model="editInfoForm" :rules="editInfoRules" ref="editInfoForm" label-width="100px">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="editInfoForm.username" placeholder="请输入用户名"></el-input>
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="editInfoForm.email" placeholder="请输入邮箱"></el-input>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="handleInfoEdit">保存修改</el-button>
            <el-button @click="resetEditInfoForm">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'UserInfo',
  data() {
    return {
      // 用户信息
      userInfo: {
        username: '',
        email: '',
        createTime: '',
        isAdmin: false
      },
      // 修改密码表单
      passwordForm: {
        currentPassword: '',
        newPassword: '',
        confirmNewPassword: ''
      },
      // 修改密码表单验证规则
      passwordRules: {
        currentPassword: [
          { required: true, message: '请输入当前密码', trigger: 'blur' }
        ],
        newPassword: [
          { required: true, message: '请输入新密码', trigger: 'blur' },
          { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
        ],
        confirmNewPassword: [
          { required: true, message: '请再次输入新密码', trigger: 'blur' },
          {
            validator: (rule, value, callback) => {
              if (value !== this.passwordForm.newPassword) {
                callback(new Error('两次输入的密码不一致'));
              } else {
                callback();
              }
            },
            trigger: 'blur'
          }
        ]
      },
      // 修改个人信息表单
      editInfoForm: {
        username: '',
        email: ''
      },
      // 修改个人信息表单验证规则
      editInfoRules: {
        username: [
          { required: true, message: '用户名不能为空', trigger: 'blur' },
          { min: 3, max: 20, message: '用户名长度应在3-20个字符之间', trigger: 'blur' }
        ],
        email: [
          { required: true, message: '邮箱不能为空', trigger: 'blur' },
          { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
        ]
      }
    }
  },
  mounted() {
    // 获取用户完整信息
    this.getUserInfo();
  },
  methods: {
    // 获取用户完整信息
    getUserInfo() {
      // 临时解决方案：如果后端会话有问题，使用本地存储的用户名模拟用户信息
      const localUsername = localStorage.getItem('username');
      
      if (localUsername) {
        // 首先尝试从后端获取真实用户信息
        this.$axios.get('/api/auth/current-user-full', {
          withCredentials: true // 确保发送cookies
        }).then(response => {
          const data = response.data;
          if (data.code === 200) {
            this.userInfo = data.data;
            // 初始化编辑表单
            this.editInfoForm = {
              username: data.data.username,
              email: data.data.email
            };
            // 保存用户信息到本地存储
          localStorage.setItem('user', JSON.stringify(data.data));
          } else {
            // 后端返回非成功状态，使用本地模拟数据
            console.warn('后端返回非成功状态，使用本地模拟数据');
            this.useMockUserData(localUsername);
          }
        }).catch(error => {
          // 请求失败，使用本地模拟数据
          console.warn('获取用户信息失败，使用本地模拟数据:', error.message);
          this.useMockUserData(localUsername);
          
          // 显示友好提示，而不是错误信息
          this.$message.info('当前使用本地用户信息预览模式');
        });
      } else {
        // 没有本地用户名，显示登录提示
        this.$message.warning('请先登录');
        this.$router.push('/login');
      }
    },
    

    
    // 使用本地模拟数据
    useMockUserData(username) {
      const now = new Date();
      const dateString = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')} ${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}:${String(now.getSeconds()).padStart(2, '0')}`;
      
      // 设置模拟用户数据
      this.userInfo = {
        username: username,
        email: `${username}@example.com`,
        createTime: dateString,
        isAdmin: false
      };
      
      // 初始化编辑表单
      this.editInfoForm = {
        username: username,
        email: `${username}@example.com`
      };
      
      // 保存用户信息到本地存储
      localStorage.setItem('user', JSON.stringify(this.userInfo));
    },
    
    // 格式化日期
    formatDate(dateString) {
      if (!dateString) return '';
      const date = new Date(dateString);
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      const hours = String(date.getHours()).padStart(2, '0');
      const minutes = String(date.getMinutes()).padStart(2, '0');
      const seconds = String(date.getSeconds()).padStart(2, '0');
      return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
    },
    
    // 修改密码
    handlePasswordChange() {
      this.$refs.passwordForm.validate((isValid) => {
        if (isValid) {
          this.$axios.post('/api/auth/change-password', {
            currentPassword: this.passwordForm.currentPassword,
            newPassword: this.passwordForm.newPassword
          }, {
            withCredentials: true // 确保发送cookies
          }).then(response => {
            const data = response.data;
            if (data.code === 200) {
              this.$message.success('密码修改成功');
              this.resetPasswordForm();
            } else {
              this.$message.error(data.message || '密码修改失败');
            }
          }).catch(error => {
            console.error('修改密码失败:', error);
            // 后端会话可能有问题，显示友好提示
            this.$message.warning('当前无法修改密码，请稍后重试');
          });
        }
      });
    },
    
    // 重置密码表单
    resetPasswordForm() {
      this.$refs.passwordForm.resetFields();
    },
    
    // 修改个人信息
    handleInfoEdit() {
      this.$refs.editInfoForm.validate((isValid) => {
        if (isValid) {
          // 简化数据发送逻辑 - 直接发送原始值，由后端处理转换
          const requestData = {
            username: this.editInfoForm.username,
            email: this.editInfoForm.email
          };
          
          console.log('发送的请求数据:', requestData);
          
          this.$axios.post('/api/auth/update-profile', requestData, {
            withCredentials: true // 确保发送cookies
          }).then(response => {
            const data = response.data;
            if (data.code === 200) {
              this.$message.success('个人信息修改成功');
              // 更新用户信息
              this.getUserInfo();
              // 更新本地存储的用户名
              localStorage.setItem('username', this.editInfoForm.username);
            } else {
              this.$message.error(data.message || '个人信息修改失败');
            }
          }).catch(error => {
            console.error('修改个人信息失败:', error);
            console.error('错误详情:', error.response?.data);
            // 后端会话可能有问题，显示友好提示
            this.$message.warning('当前无法修改个人信息，请稍后重试');
          });
        }
      });
    },
    
    // 重置个人信息表单
    resetEditInfoForm() {
      this.editInfoForm = {
        username: this.userInfo.username,
        email: this.userInfo.email
      };
      this.$refs.editInfoForm.clearValidate();
    }
  }
}
</script>

<style scoped>
.user-info-container {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.user-info-section,
.password-section,
.edit-info-section {
  margin-bottom: 30px;
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 5px;
}

.user-info-section h3,
.password-section h3,
.edit-info-section h3 {
  margin-top: 0;
  margin-bottom: 20px;
  color: #333;
  font-size: 18px;
  border-bottom: 2px solid #409eff;
  padding-bottom: 10px;
}

.info-item {
  margin-bottom: 15px;
  display: flex;
  align-items: center;
}

.info-label {
  font-weight: bold;
  width: 100px;
  color: #666;
}

.info-value {
  color: #333;
}

.el-form-item {
  margin-bottom: 15px;
}

.el-button {
  margin-right: 10px;
}
</style>