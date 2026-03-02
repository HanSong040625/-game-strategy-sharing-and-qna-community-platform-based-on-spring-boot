<template>
  <div class="login-container" style="margin-top: 0; padding-top: 50px;">
    <el-card title="登录系统" shadow="hover" style="width: 400px; margin: 0 auto; padding-top: 100px; background-color: #ffffff;">
      <el-form :model="loginForm" :rules="loginRules" ref="loginForm" label-width="80px">
        <!-- 用户名输入框 -->
        <el-form-item label="用户名" prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入用户名"></el-input>
        </el-form-item>
        <!-- 密码输入框 -->
        <el-form-item label="密码" prop="password">
          <el-input type="password" v-model="loginForm.password" placeholder="请输入密码"></el-input>
        </el-form-item>
        <!-- 按钮区域 -->
        <el-form-item>
          <el-button type="primary" @click="handleLogin" style="width: 100%">登录</el-button>
        </el-form-item>
        <el-form-item>
          <div style="text-align: center;">
            <span>还没有账号？</span>
            <el-link type="primary" @click="goToRegister">立即注册</el-link>
          </div>
        </el-form-item>
        <el-form-item>
          <div style="text-align: center; margin-top: 10px;">
            <el-link type="info" @click="goToAdminLogin" style="font-size: 12px;">
              <i class="el-icon-s-custom"></i>
              管理员登录入口
            </el-link>
          </div>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'Login',
  data() {
    return {
      // 登录表单数据
      loginForm: {
        username: '',
        password: ''
      },
      // 表单验证规则
      loginRules: {
        username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }]
      }
    }
  },
  methods: {
    // 登录按钮点击事件
    async handleLogin() {
      // 先做表单验证
      this.$refs.loginForm.validate(async (isValid) => {
        if (!isValid) return;
        
        try {
          // 准备登录参数
          const params = new URLSearchParams();
          params.append('username', this.loginForm.username);
          params.append('password', this.loginForm.password);
          
          // 先尝试管理员登录
          let loginResponse = await this.tryAdminLogin(params);
          
          // 如果管理员登录失败，尝试普通用户登录
          if (!loginResponse) {
            loginResponse = await this.tryUserLogin(params);
          }
          
          // 如果登录成功，处理登录状态
          if (loginResponse) {
            await this.handleLoginSuccess(loginResponse);
          }
        } catch (error) {
          this.showLoginErrorMessage(error);
        }
      });
    },
    
    // 尝试管理员登录
    async tryAdminLogin(params) {
      try {
        const response = await this.$axios.post('/api/admin/login', params, {
          headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
          withCredentials: true,
          responseType: 'json'
        });
        
        if (response.data.code === 200) {
          return { ...response.data, isAdmin: true };
        }
      } catch (error) {
        // 管理员登录失败是正常的，继续尝试普通用户登录
       
      }
      return null;
    },
    
    // 尝试普通用户登录
    async tryUserLogin(params) {
      try {
        const response = await this.$axios.post('/api/auth/login', params, {
          headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
          withCredentials: true,
          responseType: 'json'
        });
        
        if (response.data.code === 200) {
          return { ...response.data, isAdmin: false };
        }
      } catch (error) {
        throw error; // 普通用户登录失败需要抛出错误
      }
      return null;
    },
    
    // 处理登录成功
    async handleLoginSuccess(loginResponse) {
      this.$message.success('登录成功！');
      
      // 直接从登录响应中获取在线状态
      const isOnlineFromResponse = loginResponse.data?.isOnline || 0;
      const username = loginResponse.data?.username || this.loginForm.username;
      const token = loginResponse.data?.token || Math.random().toString(36).substr(2); // 生成临时token
      
      console.log('✅ 完整登录响应:', loginResponse);
      console.log('✅ 登录响应中的isOnline值:', isOnlineFromResponse);
      console.log('✅ 登录响应中的token值:', token);
      
      // 正确获取管理员状态，从data对象中获取
      const adminStatusFromResponse = loginResponse.data?.isAdmin || loginResponse.isAdmin || false;
      console.log('🔍 登录响应中的管理员状态:', adminStatusFromResponse);
      
      // 立即获取用户头像信息，确保登录后头像能正确显示
      let userAvatar = '';
      try {
        console.log('🔄 登录成功后立即获取用户头像信息...');
        const avatarResponse = await this.$axios.get('/api/auth/current-user-full', {
          withCredentials: true,
          timeout: 5000
        });
        
        if (avatarResponse.data && avatarResponse.data.code === 200 && avatarResponse.data.data) {
          userAvatar = avatarResponse.data.data.avatar || '';
          console.log('✅ 成功获取用户头像:', userAvatar);
        } else {
          console.warn('⚠️ 获取头像响应格式异常:', avatarResponse.data);
          // 尝试从用户信息接口获取头像
          try {
            const userInfoResponse = await this.$axios.get(`/api/auth/user/by-username/${username}`, {
              withCredentials: true,
              timeout: 3000
            });
            if (userInfoResponse.data && userInfoResponse.data.code === 200 && userInfoResponse.data.data.avatar) {
              userAvatar = userInfoResponse.data.data.avatar;
              console.log('✅ 通过用户信息接口获取头像成功:', userAvatar);
            }
          } catch (userInfoError) {
            console.error('❌ 通过用户信息接口获取头像失败:', userInfoError.message);
          }
        }
      } catch (avatarError) {
        console.error('❌ 获取用户头像失败:', avatarError.message);
        // 失败时尝试备用接口
        try {
          const backupResponse = await this.$axios.get(`/api/auth/user/by-username/${username}`, {
            withCredentials: true,
            timeout: 3000
          });
          if (backupResponse.data && backupResponse.data.code === 200 && backupResponse.data.data.avatar) {
            userAvatar = backupResponse.data.data.avatar;
            console.log('✅ 通过备用接口获取头像成功:', userAvatar);
          }
        } catch (backupError) {
          console.error('❌ 备用接口也失败，头像将使用默认值');
        }
      }
      
      // 保存用户信息到localStorage，包含头像字段
      const userInfo = {
        id: loginResponse.data?.id || Date.now(),
        username: username,
        avatar: userAvatar, // 添加头像字段
        isAdmin: adminStatusFromResponse,
        isOnline: isOnlineFromResponse === 1,
        timestamp: new Date().getTime()
      };
      
      console.log('💾 准备保存的用户信息:', userInfo);
      localStorage.setItem('user', JSON.stringify(userInfo));
      localStorage.setItem('username', username);
      localStorage.setItem('token', token); // 保存token到localStorage
      localStorage.setItem('isOnline', isOnlineFromResponse.toString());
      localStorage.setItem('isAdmin', adminStatusFromResponse ? 'true' : 'false');
      localStorage.setItem('userRole', adminStatusFromResponse ? 'admin' : 'user');
      console.log('✅ 本地存储设置完成');
      console.log('📝 本地存储的isAdmin:', localStorage.getItem('isAdmin'));
      console.log('📝 本地存储的userRole:', localStorage.getItem('userRole'));
      console.log('📝 本地存储的token:', localStorage.getItem('token'));
      console.log('📝 本地存储的用户头像:', userAvatar);
      
      // 验证在线状态是否正确设置
      await this.verifyOnlineStatus(username);
      
      // 获取用户真实角色信息（重要：验证管理员状态）
      await this.fetchUserRoleAfterLogin();
      
      // 跳转到首页
      this.$router.push('/home');
    },
    
    // 验证在线状态
    async verifyOnlineStatus(username) {
      try {
        // 添加短暂延迟，确保数据库事务完成
        await new Promise(resolve => setTimeout(resolve, 300));
        
        // 使用正确的接口获取用户信息，返回JSON格式
        const userInfoResponse = await this.$axios.get(`/api/auth/user/by-username/${username}`, {
          withCredentials: true,
          timeout: 3000 // 添加超时设置
        });
        
        if (userInfoResponse.data && userInfoResponse.data.code === 200) {
          console.log('✅ 在线状态验证成功');
          // 可以选择更新localStorage中的在线状态
          if (userInfoResponse.data.data) {
            localStorage.setItem('isOnline', '1');
          }
        } else {
          console.warn('⚠️ 在线状态验证接口返回异常:', userInfoResponse.data || '无返回数据');
        }
      } catch (error) {
        console.warn('⚠️ 在线状态验证失败（非关键错误）:', error.message || '未知错误');
        // 明确记录这只是调试信息，不影响登录流程
        console.log('ℹ️ 登录流程将继续，在线状态验证失败不影响核心功能');
        // 即使验证失败，也不抛出错误，确保登录流程继续
      }
    },
    // 跳转到注册页面
    goToRegister() {
      this.$router.push('/register');
    },
    
    // 跳转到管理员登录页面
    goToAdminLogin() {
      this.$router.push('/admin/login');
    },
    
    // 显示登录错误信息的辅助方法
    showLoginErrorMessage(error) {
      if (error.response) {
        // 显示后端返回的具体错误信息，但过滤掉"用户不存在"的提示
        if (error.response.data && error.response.data.message) {
          // 如果是"用户不存在"的错误，显示更友好的提示
          if (error.response.data.message === '用户不存在') {
            this.$message.error('登录失败：用户名或密码错误');
          } else {
            this.$message.error('登录失败：' + error.response.data.message);
          }
        } else {
          this.$message.error('登录失败：服务器返回错误状态码 ' + error.response.status);
        }
      } else if (error.request) {
        this.$message.error('登录失败：无法连接到服务器，请检查网络');
      } else {
        this.$message.error('登录失败：请求配置错误');
      }
    },
    
    // 登录成功后获取用户角色信息
    async fetchUserRoleAfterLogin() {
      try {
        const currentUsername = localStorage.getItem('username') || '';
        
        // 获取用户完整信息用于验证（强制从后端获取真实状态）
        const userResponse = await this.$axios.get('/api/auth/current-user-full', {
          withCredentials: true,
          timeout: 5000
        });

        if (userResponse.data.code === 200 && userResponse.data.data) {
          // 使用后端返回的真实管理员状态，忽略之前可能错误的设置
          const isAdminValue = Boolean(userResponse.data.data.isAdmin);
          console.log('🔍 后端返回的真实管理员状态:', isAdminValue);
          
          const updatedUserInfo = {
            id: userResponse.data.data.id || 0,
            username: userResponse.data.data.username || currentUsername,
            isAdmin: isAdminValue,
            isOnline: userResponse.data.data.isOnline === 1,
            timestamp: new Date().getTime()
          };
          
          console.log('💾 更新后的用户信息（基于后端真实状态）:', updatedUserInfo);
          localStorage.setItem('user', JSON.stringify(updatedUserInfo));
          localStorage.setItem('isAdmin', isAdminValue ? 'true' : 'false'); // 明确设置管理员标识
          localStorage.setItem('userRole', isAdminValue ? 'admin' : 'user'); // 设置用户角色
          console.log('✅ 用户角色信息已根据后端真实状态更新');
        } else {
          // 接口返回但数据不完整，强制设置为普通用户
          console.warn('⚠️ 后端接口返回数据不完整，强制设置为普通用户');
          const updatedUserInfo = {
            id: 0,
            username: currentUsername,
            isAdmin: false,
            isOnline: false,
            timestamp: new Date().getTime()
          };
          localStorage.setItem('user', JSON.stringify(updatedUserInfo));
          localStorage.setItem('isAdmin', 'false'); // 明确设置为非管理员
          localStorage.setItem('userRole', 'user'); // 设置用户角色
        }
        
        // 不再验证在线状态
      } catch (userError) {
        // 所有接口都失败，强制设置为普通用户
        console.error('❌ 获取用户角色信息失败，强制设置为普通用户:', userError.message);
        const currentUsername = localStorage.getItem('username') || '';
        const updatedUserInfo = {
          id: 0,
          username: currentUsername,
          isAdmin: false,
          isOnline: false,
          timestamp: new Date().getTime()
        };
        localStorage.setItem('user', JSON.stringify(updatedUserInfo));
        localStorage.setItem('isAdmin', 'false');
        localStorage.setItem('userRole', 'user'); // 设置用户角色
      }
    }
  }
}
</script>

<style scoped>
.login-container {
  background-color: #f5f5f5;
  min-height: 100vh;
}
</style>