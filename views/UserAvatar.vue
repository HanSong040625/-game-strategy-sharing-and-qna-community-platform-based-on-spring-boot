<template>
  <div class="user-avatar-container">
    <el-card shadow="hover">
      <div slot="header">
        <span>我的头像</span>
      </div>
      
      <!-- 头像显示和操作区域 -->
      <div class="avatar-management">
        <!-- 当前头像预览 -->
        <div class="avatar-preview-section">
          <h3>当前头像</h3>
          <div class="avatar-display">
            <div class="avatar-circle">
              <img v-if="userInfo.avatar" :src="getImageUrl(userInfo.avatar)" :alt="userInfo.username" class="avatar-image">
              <div v-else class="avatar-placeholder">
                <i class="el-icon-user"></i>
              </div>
            </div>
            <div class="avatar-info">
              <p class="username">{{ userInfo.username }}</p>
              <p class="email">{{ userInfo.email }}</p>
            </div>
          </div>
        </div>
        
        <!-- 头像操作按钮 -->
        <div class="avatar-actions-section">
          <h3>头像操作</h3>
          <div class="action-buttons">
            <!-- 上传头像按钮 -->
            <el-upload
              class="avatar-uploader"
              action="/api/auth/upload-avatar"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
              :before-upload="beforeAvatarUpload"
              :with-credentials="true"
              :limit="1"
              :file-list="[]"
              :accept="'image/*'">
              <el-button type="primary" size="medium" class="action-button">
                <i class="el-icon-upload"></i>
                上传新头像
              </el-button>
            </el-upload>
            
            <!-- 删除头像按钮 -->
            <el-button 
              v-if="userInfo.avatar" 
              type="danger" 
              size="medium" 
              class="action-button"
              @click="removeAvatar">
              <i class="el-icon-delete"></i>
              删除头像
            </el-button>
            
            <!-- 重置为默认头像 -->
            <el-button 
              type="info" 
              size="medium" 
              class="action-button"
              @click="resetToDefaultAvatar">
              <i class="el-icon-refresh"></i>
              重置为默认头像
            </el-button>
          </div>
        </div>
        
        <!-- 头像使用说明 -->
        <div class="avatar-instructions">
          <h3>使用说明</h3>
          <ul>
            <li>支持 JPG、PNG 格式的图片</li>
            <li>图片大小不能超过 2MB</li>
            <li>头像将在社区内所有位置显示</li>
          </ul>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'UserAvatar',
  data() {
    return {
      // 用户信息
      userInfo: {
        username: '',
        email: '',
        avatar: '',
        createTime: '',
        isAdmin: false
      }
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

      
      let avatarUrl = '';
      
      // 处理不同的响应格式
      if (typeof response === 'string') {
        // 如果是字符串格式（通用上传接口返回的路径）
        avatarUrl = response;
        console.log('✅ [格式识别] 检测到字符串格式响应');
        console.log('🔗 [头像URL]', avatarUrl);
      } else if (response && response.code === 200) {
        // 如果是JSON格式（专用头像上传接口）
        // 修复：后端返回格式为 response.data.data.avatarUrl
        avatarUrl = response.data?.data?.avatarUrl || response.data?.avatarUrl || '';
        console.log('✅ [格式识别] 检测到JSON格式响应');
        console.log('🔗 [头像URL]', avatarUrl);
        console.log('📋 [响应结构]', JSON.stringify(response, null, 2));
      } else if (response && response.data) {
        // 其他可能的JSON格式
        avatarUrl = response.data;
        console.log('✅ [格式识别] 检测到其他JSON格式响应');
        console.log('🔗 [头像URL]', avatarUrl);
      } else {
        console.log('❌ [格式识别] 无法识别的响应格式');
      }
      
      if (avatarUrl) {
        console.log('🎯 [前端更新] 开始更新前端显示');
        console.log('🔄 [更新前] userInfo.avatar:', this.userInfo.avatar);
        console.log('🔄 [更新后] 新头像URL:', avatarUrl);
        
        // 直接更新用户头像URL（与游戏卡片上传逻辑保持一致）
        this.userInfo.avatar = avatarUrl;
        this.$message.success('头像上传成功');
        
        console.log('✅ [前端更新] 前端显示更新完成');
        
        // 更新本地存储的用户信息
        this.updateLocalUserInfo();
        
        // 触发全局事件，通知导航栏刷新头像显示
        this.triggerAvatarRefresh(avatarUrl);
        
        // 调用后端API更新用户头像URL（异步操作，不影响前端显示）
        this.updateUserAvatarInBackend(avatarUrl);
      } else {
        console.error('❌ [上传失败] 无法获取头像URL');
        this.$message.error('头像上传失败：无法获取头像URL');
      }
      
      console.log('🏁 === 头像上传流程结束 ===');
    },
    
    // 调用后端API更新用户头像（异步操作）
    updateUserAvatarInBackend(avatarUrl) {
      console.log('🌐 === 开始调用后端更新头像接口 ===');
      console.log('📅 [时间戳]', new Date().toISOString());
      console.log('🔗 [请求URL]', '/api/auth/update-avatar');
      console.log('📤 [请求数据]', JSON.stringify({ avatarUrl: avatarUrl }, null, 2));
      console.log('🔐 [认证信息] withCredentials: true');
      
      this.$axios.post('/api/auth/update-avatar', {
        avatarUrl: avatarUrl
      }, {
        withCredentials: true
      }).then(response => {
        console.log('✅ [后端响应] 接口调用成功');
        console.log('📄 [响应数据]', JSON.stringify(response.data, null, 2));
        console.log('📊 [响应状态]', response.status, response.statusText);
        
        const data = response.data;
        if (data.code === 200) {
          console.log('🎯 [后端更新] 头像URL已成功更新到后端数据库');
          console.log('💾 [存储位置] 数据库用户表avatar字段');
        } else {
          console.warn('⚠️ [后端更新] 头像URL更新到后端失败');
          console.warn('📋 [错误信息]', data.message);
          console.warn('🔢 [错误码]', data.code);
        }
      }).catch(error => {
        console.error('❌ [后端更新] 更新头像到后端失败');
        console.error('📋 [错误详情]', error);
        if (error.response) {
          console.error('🔢 [响应状态]', error.response.status);
          console.error('📄 [响应数据]', error.response.data);
        }
        // 这里不显示错误提示，因为前端已经成功更新了显示
      }).finally(() => {
        console.log('🏁 === 后端更新接口调用结束 ===');
      });
    },
    
    // 头像上传前验证
    beforeAvatarUpload(file) {
      console.log('🔍 === 头像上传前验证开始 ===');
      console.log('📅 [时间戳]', new Date().toISOString());
      console.log('📁 [文件信息] 名称:', file.name, '类型:', file.type, '大小:', file.size, 'bytes');
      
      const isJPG = file.type === 'image/jpeg' || file.type === 'image/jpg';
      const isPNG = file.type === 'image/png';
      const isLt2M = file.size / 1024 / 1024 < 2;

      console.log('✅ [格式验证] JPG格式:', isJPG, 'PNG格式:', isPNG);
      console.log('✅ [大小验证] 小于2MB:', isLt2M, '实际大小:', (file.size / 1024 / 1024).toFixed(2), 'MB');

      if (!isJPG && !isPNG) {
        console.error('❌ [验证失败] 文件格式不符合要求');
        this.$message.error('头像只能是 JPG 或 PNG 格式!');
        return false;
      }
      if (!isLt2M) {
        console.error('❌ [验证失败] 文件大小超过限制');
        this.$message.error('头像大小不能超过 2MB!');
        return false;
      }
      
      console.log('✅ [验证通过] 文件格式和大小均符合要求');
      console.log('🏁 === 头像上传前验证结束 ===');
      return true;
    },
    
    // 删除头像
    removeAvatar() {
      console.log('🗑️ === 开始删除头像操作 ===');
      console.log('📅 [时间戳]', new Date().toISOString());
      console.log('📊 [当前头像URL]', this.userInfo.avatar);
      console.log('👤 [用户信息] 用户名:', this.userInfo.username);
      
      this.$confirm('确定要删除头像吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        console.log('✅ [用户确认] 用户确认删除头像');
        console.log('🌐 [开始调用] 调用后端删除头像接口');
        console.log('🔗 [请求URL]', '/api/auth/remove-avatar');
        console.log('🔐 [认证信息] withCredentials: true');
        
        this.$axios.post('/api/auth/remove-avatar', {}, {
          withCredentials: true
        }).then(response => {
          console.log('✅ [后端响应] 删除头像接口调用成功');
          console.log('📄 [响应数据]', JSON.stringify(response.data, null, 2));
          console.log('📊 [响应状态]', response.status, response.statusText);
          
          const data = response.data;
          if (data.code === 200) {
            console.log('🔄 [前端更新] 开始更新前端显示');
            console.log('📊 [更新前] userInfo.avatar:', this.userInfo.avatar);
            
            this.userInfo.avatar = '';
            
            console.log('📊 [更新后] userInfo.avatar:', this.userInfo.avatar);
            console.log('✅ [前端更新] 前端显示更新完成');
            
            this.$message.success('头像删除成功');
            
            // 更新本地存储的用户信息
            this.updateLocalUserInfo();
            console.log('💾 [本地存储] 用户信息已更新');
            
            // 触发头像刷新事件
            this.triggerAvatarRefresh('');
            console.log('🔄 [事件触发] 头像刷新事件已触发');
            
            console.log('🏁 === 头像删除操作完成 ===');
          } else {
            console.error('❌ [删除失败] 后端返回非成功状态');
            console.error('📋 [错误信息]', data.message);
            console.error('🔢 [错误码]', data.code);
            this.$message.error(data.message || '头像删除失败');
          }
        }).catch(error => {
          console.error('❌ [删除失败] 删除头像接口调用失败');
          console.error('📋 [错误详情]', error);
          if (error.response) {
            console.error('🔢 [响应状态]', error.response.status);
            console.error('📄 [响应数据]', error.response.data);
          }
          this.$message.warning('当前无法删除头像，请稍后重试');
        });
      }).catch(() => {
        console.log('🚫 [用户取消] 用户取消删除头像操作');
        // 用户取消删除
      });
    },
    
    // 重置为默认头像
    resetToDefaultAvatar() {
      console.log('🔄 === 开始重置为默认头像操作 ===');
      console.log('📅 [时间戳]', new Date().toISOString());
      console.log('📊 [当前头像URL]', this.userInfo.avatar);
      console.log('👤 [用户信息] 用户名:', this.userInfo.username);
      
      this.$confirm('确定要重置为默认头像吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
  
        
        this.$axios.post('/api/auth/reset-avatar', {}, {
          withCredentials: true
        }).then(response => {
          console.log('✅ [后端响应] 重置头像接口调用成功');
          console.log('📄 [响应数据]', JSON.stringify(response.data, null, 2));
          console.log('📊 [响应状态]', response.status, response.statusText);
          
          const data = response.data;
          if (data.code === 200) {
            console.log('🔄 [前端更新] 开始更新前端显示');
            console.log('📊 [更新前] userInfo.avatar:', this.userInfo.avatar);
            console.log('📊 [默认头像URL]', data.data.defaultAvatarUrl || '');
            
            this.userInfo.avatar = data.data.defaultAvatarUrl || '';
            
            console.log('📊 [更新后] userInfo.avatar:', this.userInfo.avatar);
            console.log('✅ [前端更新] 前端显示更新完成');
            
            this.$message.success('头像重置成功');
            
            // 更新本地存储的用户信息
            this.updateLocalUserInfo();
            console.log('💾 [本地存储] 用户信息已更新');
            
            // 触发头像刷新事件
            this.triggerAvatarRefresh(data.data.defaultAvatarUrl || '');
            console.log('🔄 [事件触发] 头像刷新事件已触发');
            
            console.log('🏁 === 头像重置操作完成 ===');
          } else {
            console.error('❌ [重置失败] 后端返回非成功状态');
            console.error('📋 [错误信息]', data.message);
            console.error('🔢 [错误码]', data.code);
            this.$message.error(data.message || '头像重置失败');
          }
        }).catch(error => {
          console.error('❌ [重置失败] 重置头像接口调用失败');
          console.error('📋 [错误详情]', error);
          if (error.response) {
            console.error('🔢 [响应状态]', error.response.status);
            console.error('📄 [响应数据]', error.response.data);
          }
          this.$message.warning('当前无法重置头像，请稍后重试');
        });
      }).catch(() => {
        console.log('🚫 [用户取消] 用户取消重置头像操作');
        // 用户取消重置
      });
    },
    
    // 更新本地存储的用户信息
    updateLocalUserInfo() {
      console.log('💾 [UserAvatar] 开始更新本地存储的用户信息');
      const userInfoStr = localStorage.getItem('user');
      console.log('📊 [UserAvatar] 更新前localStorage中的user信息:', userInfoStr);
      
      if (userInfoStr) {
        try {
          const userInfo = JSON.parse(userInfoStr);
          console.log('🔄 [UserAvatar] 更新头像URL:');
          console.log('   - 旧头像URL:', userInfo.avatar);
          console.log('   - 新头像URL:', this.userInfo.avatar);
          
          userInfo.avatar = this.userInfo.avatar;
          localStorage.setItem('user', JSON.stringify(userInfo));
          
          console.log('✅ [UserAvatar] 本地存储更新完成');
          console.log('📋 [UserAvatar] 更新后的user信息:', JSON.stringify(userInfo));
        } catch (error) {
          console.error('❌ [UserAvatar] 更新本地用户信息失败:', error);
        }
      } else {
        console.log('⚠️ [UserAvatar] localStorage中未找到user信息，无法更新');
      }
    },
    
    // 触发全局事件，通知导航栏刷新头像显示
    triggerAvatarRefresh(avatarUrl) {
      console.log('🚀 触发头像刷新事件，新头像URL:', avatarUrl);
      
      // 方法1：使用Vue全局事件总线（如果已配置）
      if (this.$bus) {
        this.$bus.$emit('avatar-updated', avatarUrl);
        console.log('✅ 已通过事件总线发送头像更新事件');
      }
      
      // 方法2：使用window全局事件
      window.dispatchEvent(new CustomEvent('avatar-updated', {
        detail: { avatarUrl: avatarUrl }
      }));
      console.log('✅ 已通过window事件发送头像更新事件');
      
      // 方法3：直接调用App.vue的fetchUserAvatar方法（如果可能）
      if (window.refreshNavbarAvatar) {
        window.refreshNavbarAvatar(avatarUrl);
        console.log('✅ 已通过全局函数刷新导航栏头像');
      }
      
      // 方法4：设置一个全局标志，App.vue可以监听这个标志
      localStorage.setItem('avatar_updated_timestamp', Date.now().toString());
      console.log('✅ 已设置头像更新时间戳');
    },
    
    // 获取用户信息
    getUserInfo() {
      console.log('🔍 [UserAvatar] 开始获取用户信息流程');
      const localUsername = localStorage.getItem('username');
      console.log('👤 [UserAvatar] 本地用户名:', localUsername);
      
      if (localUsername) {
        console.log('🌐 [UserAvatar] 开始调用后端API获取用户信息');
        console.log('📡 [UserAvatar] API请求: GET /api/auth/current-user-full');
        
        // 首先尝试从后端获取真实用户信息
        this.$axios.get('/api/auth/current-user-full', {
          withCredentials: true
        }).then(response => {
          console.log('✅ [UserAvatar] 后端API响应成功');
          console.log('📄 [UserAvatar] 响应数据:', response.data);
          
          const data = response.data;
          if (data.code === 200) {
            console.log('🎯 [UserAvatar] 后端返回成功状态，更新用户信息');
            this.userInfo = data.data;
            
            // 新增：详细检查重新登录后获取的avatarUrl
            console.log('🔍 [UserAvatar] 重新登录后获取的userInfo：', this.userInfo);
            console.log('📊 [UserAvatar] avatarUrl是否存在:', !!this.userInfo.avatar);
            console.log('🔗 [UserAvatar] avatarUrl值:', this.userInfo.avatar);
            console.log('💾 [UserAvatar] 更新后的用户信息:');
            console.log('   - 用户名:', this.userInfo.username);
            console.log('   - 头像URL:', this.userInfo.avatar);
            console.log('   - 邮箱:', this.userInfo.email);
            
            // 更新本地存储
            this.updateLocalUserInfo();
            console.log('✅ [UserAvatar] 用户信息获取和更新完成');
          } else {
            console.log('⚠️ [UserAvatar] 后端返回非成功状态，使用本地模拟数据');
            console.log('📋 [UserAvatar] 错误码:', data.code, '错误信息:', data.message);
            // 后端返回非成功状态，使用本地模拟数据
            this.useMockUserData(localUsername);
          }
        }).catch(error => {
          console.error('❌ [UserAvatar] 后端API请求失败:', error);
          console.error('📋 [UserAvatar] 错误详情:', error.response);
          // 请求失败，使用本地模拟数据
          this.useMockUserData(localUsername);
          this.$message.info('当前使用本地用户信息预览模式');
        });
      } else {
        console.log('🚫 [UserAvatar] 没有本地用户名，用户未登录');
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
        avatar: '',
        createTime: dateString,
        isAdmin: false
      };
    },
    
    // 设置页面刷新监听器
    setupPageRefreshListener() {
      console.log('🔔 [UserAvatar] 设置页面刷新监听器');
      
      this.pageRefreshHandler = () => {
        console.log('🔄 [UserAvatar] 页面即将刷新，记录当前状态');
        console.log('📊 [UserAvatar] 刷新前用户信息状态:');
        console.log('   - 用户名:', this.userInfo.username);
        console.log('   - 头像URL:', this.userInfo.avatar);
        console.log('   - 邮箱:', this.userInfo.email);
        console.log('   - 刷新时间:', new Date().toISOString());
        
        // 记录localStorage状态
        console.log('💾 [UserAvatar] 刷新前localStorage状态:');
        console.log('   - username:', localStorage.getItem('username'));
        console.log('   - user信息:', localStorage.getItem('user'));
        console.log('   - token:', localStorage.getItem('token') ? '存在' : '不存在');
        
        // 设置刷新标记
        localStorage.setItem('avatar_page_refresh_time', new Date().toISOString());
        console.log('✅ [UserAvatar] 页面刷新标记已设置');
      };
      
      window.addEventListener('beforeunload', this.pageRefreshHandler);
      console.log('✅ [UserAvatar] 页面刷新监听器设置完成');
    }
  },
  mounted() {
    console.log('🔄 [UserAvatar] 页面挂载完成，开始获取用户信息');
    console.log('📊 [UserAvatar] 当前localStorage状态检查:');
    console.log('   - username:', localStorage.getItem('username'));
    console.log('   - user信息:', localStorage.getItem('user'));
    console.log('   - token:', localStorage.getItem('token') ? '存在' : '不存在');
    
    // 检查localStorage中存储的用户头像信息
    try {
      const userInfoStr = localStorage.getItem('user');
      if (userInfoStr) {
        const userInfo = JSON.parse(userInfoStr);
        console.log('💾 [UserAvatar] localStorage中存储的头像信息:');
        console.log('   - 用户名:', userInfo.username);
        console.log('   - 头像URL:', userInfo.avatar);
        console.log('   - 存储时间:', new Date().toISOString());
      } else {
        console.log('⚠️ [UserAvatar] localStorage中未找到user信息');
      }
    } catch (error) {
      console.error('❌ [UserAvatar] 解析localStorage用户信息失败:', error);
    }
    
    // 添加页面刷新监听器
    this.setupPageRefreshListener();
    
    this.getUserInfo();
  },
  
  beforeUnmount() {
    // 清理页面刷新监听器
    if (this.pageRefreshHandler) {
      window.removeEventListener('beforeunload', this.pageRefreshHandler);
    }
  },
}
</script>

<style scoped>
.user-avatar-container {
  padding: 20px;
}

.avatar-management {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.avatar-preview-section,
.avatar-actions-section,
.avatar-instructions {
  background-color: #f9f9f9;
  padding: 20px;
  border-radius: 8px;
  border: 1px solid #e6e6e6;
}

.avatar-preview-section h3,
.avatar-actions-section h3,
.avatar-instructions h3 {
  margin-top: 0;
  margin-bottom: 15px;
  color: #333;
  font-size: 16px;
  border-bottom: 1px solid #e6e6e6;
  padding-bottom: 10px;
}

.avatar-display {
  display: flex;
  align-items: center;
  gap: 20px;
}

.avatar-circle {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  overflow: hidden;
  border: 3px solid #409eff;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
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

.avatar-info {
  flex: 1;
}

.username {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin: 0 0 5px 0;
}

.email {
  font-size: 14px;
  color: #666;
  margin: 0;
}

.action-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
}

.action-button {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border-radius: 6px;
  transition: all 0.3s ease;
}

.action-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.avatar-instructions ul {
  margin: 0;
  padding-left: 20px;
  color: #666;
  line-height: 1.6;
}

.avatar-instructions li {
  margin-bottom: 8px;
}

/* 响应式设计 */
@media screen and (max-width: 768px) {
  .avatar-display {
    flex-direction: column;
    text-align: center;
  }
  
  .action-buttons {
    flex-direction: column;
  }
  
  .action-button {
    width: 100%;
    justify-content: center;
  }
}
</style>