<template>
  <div class="profile-container">
    <el-card title="个人中心" shadow="hover">
      <!-- 用户基本信息 -->
      <div class="user-info-section">
        <h3>基本信息</h3>
        <!-- 头像上传区域 -->
        <div class="avatar-section">
          <div class="avatar-upload">
            <div class="avatar-preview">
              <img v-if="userInfo.avatar" :src="getImageUrl(userInfo.avatar)" :alt="userInfo.username" class="avatar-image">
              <div v-else class="avatar-placeholder">
                <i class="el-icon-user"></i>
              </div>
            </div>
            <div class="avatar-actions">
              <el-upload
                class="avatar-uploader"
                action="/api/auth/upload-avatar"
                :show-file-list="false"
                :on-success="handleAvatarSuccess"
                :before-upload="beforeAvatarUpload"
                :headers="{ 'Content-Type': 'multipart/form-data' }"
                :with-credentials="true">
                <el-button size="small" type="primary">上传头像</el-button>
              </el-upload>
              <el-button v-if="userInfo.avatar" size="small" @click="removeAvatar" type="danger">删除头像</el-button>
            </div>
          </div>
        </div>
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
  name: 'Profile',
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
        ]}
      }
    },
    methods: {
    // 处理图片URL，确保相对路径能够正确访问
    getImageUrl(url) {
      if (!url) return '';
      
      // 如果是绝对URL（包含http://或https://），直接返回
      if (url.startsWith('http://') || url.startsWith('https://')) {
        return url;
      }
      
      // 对于相对路径，构建完整的URL
      // 如果是/assets/avatars/开头的路径，直接使用当前域名构建完整URL
      if (url.startsWith('/assets/avatars/')) {
        return window.location.origin + url;
      }
      
      // 其他相对路径，直接返回（后端已配置静态资源映射）
      return url;
    },
    
    // 头像上传成功处理
    handleAvatarSuccess(response, file) {
        if (response.code === 200) {
          // 修复：正确提取头像URL（后端返回格式为 response.data.data.avatarUrl）
          const avatarUrl = response.data?.data?.avatarUrl || response.data?.avatarUrl || '';
          this.userInfo.avatar = avatarUrl;
          this.$message.success('头像上传成功');
          
          // 触发全局头像更新事件，通知其他组件（如导航栏）更新头像
          this.triggerAvatarRefresh(avatarUrl);
          
          // 异步更新后端数据库中的头像URL
          this.updateUserAvatarInBackend(avatarUrl);
        } else {
          this.$message.error(response.message || '头像上传失败');
        }
      },
      
      // 头像上传前验证
      beforeAvatarUpload(file) {
        const isJPG = file.type === 'image/jpeg' || file.type === 'image/jpg';
        const isPNG = file.type === 'image/png';
        const isLt2M = file.size / 1024 / 1024 < 2;

        if (!isJPG && !isPNG) {
          this.$message.error('头像只能是 JPG 或 PNG 格式!');
          return false;
        }
        if (!isLt2M) {
          this.$message.error('头像大小不能超过 2MB!');
          return false;
        }
        return true;
      },
      
      // 删除头像
      removeAvatar() {
        this.$confirm('确定要删除头像吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          this.$axios.post('/api/auth/remove-avatar', {}, {
            withCredentials: true
          }).then(response => {
            const data = response.data;
            if (data.code === 200) {
              this.userInfo.avatar = '';
              this.$message.success('头像删除成功');
            } else {
              this.$message.error(data.message || '头像删除失败');
            }
          }).catch(error => {
            console.error('删除头像失败:', error);
            this.$message.warning('当前无法删除头像，请稍后重试');
          });
        }).catch(() => {
          // 用户取消删除
        });
      },
      
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
            this.$axios.post('/api/auth/update-profile', {
              username: this.editInfoForm.username,
              email: this.editInfoForm.email
            }, {
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
      },
      
      // 触发全局头像更新事件（与UserAvatar.vue保持一致）
      triggerAvatarRefresh(avatarUrl) {
        console.log('Profile.vue: 触发头像更新事件，头像URL:', avatarUrl);
        
        // 方法1: 通过Vue事件总线（如果已配置）
        if (this.$bus) {
          this.$bus.$emit('avatar-updated', avatarUrl);
        }
        
        // 方法2: 通过window全局事件
        if (window.dispatchEvent) {
          const event = new CustomEvent('avatar-updated', { 
            detail: { avatarUrl: avatarUrl } 
          });
          window.dispatchEvent(event);
        }
        
        // 方法3: 通过全局函数（如果存在）
        if (window.refreshNavbarAvatar && typeof window.refreshNavbarAvatar === 'function') {
          window.refreshNavbarAvatar(avatarUrl);
        }
        
        // 方法4: 通过localStorage时间戳变化（App.vue会监听）
        const timestamp = new Date().getTime();
        localStorage.setItem('avatar_updated_timestamp', timestamp.toString());
        
        // 同时更新本地存储的头像URL
        if (avatarUrl) {
          localStorage.setItem('userAvatar', avatarUrl);
        }
      },
      
      // 异步更新后端数据库中的头像URL
      updateUserAvatarInBackend(avatarUrl) {
        if (!avatarUrl) return;
        
        // 延迟执行，确保头像上传完成后再更新数据库
        setTimeout(() => {
          this.$axios.post('/api/auth/update-avatar', {
            avatarUrl: avatarUrl
          }, {
            withCredentials: true
          }).then(response => {
            console.log('Profile.vue: 后端头像URL更新成功', response.data);
          }).catch(error => {
            console.error('Profile.vue: 后端头像URL更新失败:', error);
          });
        }, 1000); // 延迟1秒执行
      }
    },
    // 页面加载时获取用户信息
    mounted() {
      this.getUserInfo();
    },
    // 页面加载前检查登录状态
    beforeRouteEnter(to, from, next) {
      // 检查localStorage中是否有username
      const username = localStorage.getItem('username');
      
      if (username) {
        console.log('用户已登录，允许进入个人中心页面');
        next();
      } else {
        console.log('用户未登录，重定向到登录页');
        next('/login');
      }
    }
  }

</script>

<style scoped>
.profile-container {
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

.avatar-section {
  margin-bottom: 20px;
}

.avatar-upload {
  display: flex;
  align-items: center;
  gap: 20px;
}

.avatar-preview {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  overflow: hidden;
  border: 2px solid #dcdfe6;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
}

.avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  font-size: 40px;
  color: #c0c4cc;
}

.avatar-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.avatar-uploader {
  display: inline-block;
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
   margin-right: 100px;
 }
</style>