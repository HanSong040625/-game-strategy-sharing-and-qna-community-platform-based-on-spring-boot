<template>
  <div>
    <div class="user-profile-container">
      <!-- 用户基本信息卡片 -->
      <el-card class="user-info-card" shadow="hover">
        <div class="user-header">
          <!-- 用户基本信息 -->
          <div class="user-details">
            <h2 class="username">{{ userInfo.username }}</h2>
            <div class="user-stats">
              <div class="stat-item" @click="showFollowingList">
                <span class="stat-label">关注</span>
                <span class="stat-value">{{ userStats.followingCount || 0 }}</span>
              </div>
              <div class="stat-item" @click="showFollowersList">
                <span class="stat-label">粉丝</span>
                <span class="stat-value">{{ userStats.followersCount || 0 }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">攻略</span>
                <span class="stat-value">{{ userStats.guideCount || 0 }}</span>
              </div>
            </div>
            
            <!-- 关注按钮 -->
            <div class="follow-section">
              <el-button 
                v-if="!isCurrentUser" 
                :type="isFollowing ? 'info' : 'primary'" 
                :loading="followLoading"
                @click="handleFollowAction">
                {{ isFollowing ? '已关注' : '关注' }}
              </el-button>
              <el-button v-else type="text" disabled>这是您自己</el-button>
              <!-- 测试按钮 -->
              <el-button type="warning" @click="showFollowingList">测试关注列表</el-button>
              <el-button type="warning" @click="showFollowersList">测试粉丝列表</el-button>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 用户内容区域 -->
      <div class="user-content">
        <!-- 攻略列表 -->
        <el-card class="guides-section" shadow="hover">
          <template #header>
            <div class="section-header">
              <h3>发布的攻略</h3>
              <span class="guide-count">共 {{ guides.length }} 篇</span>
            </div>
          </template>
          
          <div v-if="guides.length > 0" class="guides-list">
            <div v-for="guide in guides" :key="guide.id" class="guide-item">
              <div class="guide-info">
                <h4 class="guide-title">{{ guide.title }}</h4>
                <p class="guide-summary">{{ guide.summary || '暂无描述' }}</p>
                <div class="guide-meta">
                  <el-tag v-if="guide.gameName" type="info" size="small">{{ guide.gameName }}</el-tag>
                  <span class="create-time">{{ formatDate(guide.createTime) }}</span>
                  <span class="view-count">
                    <i class="el-icon-view"></i>
                    {{ guide.viewCount || 0 }}
                  </span>
                  <span class="like-count">
                    <i class="el-icon-star-on"></i>
                    {{ guide.likeCount || 0 }}
                  </span>
                </div>
              </div>
              <div class="guide-actions">
                <el-button type="primary" size="small" @click="viewGuideDetail(guide.id)">查看详情</el-button>
              </div>
            </div>
          </div>
          
          <div v-else class="empty-state">
            <i class="el-icon-document"></i>
            <p>该用户还没有发布任何攻略</p>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 关系列表对话框 -->
    <el-dialog
      :title="relationshipDialog.title"
      :visible.sync="relationshipDialog.visible"
      width="600px"
    >
      <div class="relationship-dialog-content">
        <div v-if="relationshipDialog.users.length === 0" class="empty-state">
          <i class="el-icon-user"></i>
          <p>{{ relationshipDialog.title === '关注列表' ? '还没有关注任何人' : '还没有粉丝' }}</p>
        </div>
        <div v-else class="relationship-list">
          <div v-for="user in relationshipDialog.users" :key="user.id" class="user-item">
            <div class="user-info">
              <div class="user-avatar">
                <img :src="getImageUrl(user.avatar)" :alt="user.username" class="avatar-image">
              </div>
              <div class="user-details">
                <h4 class="username">{{ user.username }}</h4>
                <p class="follow-time" v-if="user.followTime">
                  {{ formatDate(user.followTime) }}
                </p>
              </div>
            </div>
            <div class="user-actions">
              <el-button type="primary" size="small" @click="viewUserProfile(user.username)">
                查看资料
              </el-button>
            </div>
          </div>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="relationshipDialog.visible = false">关闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'UserProfile',
  data() {
    return {
      // 用户ID（从路由参数获取）
      userId: null,
      // 当前登录用户ID
      currentUserId: null,
      // 用户信息
      userInfo: {
        id: null,
        username: '',
        email: '',
        avatar: '',
        createTime: ''
      },
      // 用户统计信息
      userStats: {
        followingCount: 0,
        followersCount: 0,
        guideCount: 0
      },
      // 关注状态
      isFollowing: false,
      followLoading: false,
      // 攻略列表
      guides: [],
      // 关注列表
      followingList: [],
      // 粉丝列表
      followersList: [],
      // 关系对话框
      relationshipDialog: {
        visible: false,
        title: '',
        users: []
      }
    }
  },
  computed: {
    // 判断是否是当前用户
    isCurrentUser() {
      return this.userId && this.currentUserId && this.userId === this.currentUserId;
    }
  },
  methods: {
    // 获取图片URL
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

    // 格式化日期
    formatDate(dateString) {
      if (!dateString) return '';
      const date = new Date(dateString);
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      const hours = String(date.getHours()).padStart(2, '0');
      const minutes = String(date.getMinutes()).padStart(2, '0');
      return `${year}-${month}-${day} ${hours}:${minutes}`;
    },

    // 根据用户名获取用户ID
    async getUserIdByUsername(username) {
      try {
        console.log('🔍 开始根据用户名获取用户ID:', username);
        const token = localStorage.getItem('token');
        const response = await this.$axios.get(`/api/auth/user/by-username/${username}`, {
          headers: {
            'Authorization': token ? `Bearer ${token}` : ''
          },
          withCredentials: true
        });
        
        console.log('📡 获取用户IDAPI响应:', response.data);
        
        // 后端返回包含用户信息的响应，检查response.data.data.id
        if (response.data && response.data.code === 200 && response.data.data && response.data.data.id) {
          const userId = response.data.data.id;
          console.log('✅ 成功获取用户ID:', userId);
          return userId;
        } else {
          console.warn('⚠️ API返回数据不完整:', response.data);
        }
      } catch (error) {
        console.error('❌ 获取用户ID失败:', error);
        // 记录详细的错误信息
        if (error.response) {
          console.error('❌ 错误状态码:', error.response.status);
          console.error('❌ 错误响应数据:', error.response.data);
        }
      }
      
      // 如果API调用失败，尝试备用方案
      try {
        console.log('🔄 尝试备用方案：获取所有用户列表');
        const token = localStorage.getItem('token');
        const response = await this.$axios.get('/api/auth/users/list', {
          headers: {
            'Authorization': token ? `Bearer ${token}` : ''
          },
          withCredentials: true
        });
        
        if (response.data && response.data.code === 200 && response.data.data) {
          const users = response.data.data;
          const targetUser = users.find(user => user.username === username);
          if (targetUser && targetUser.id) {
            console.log('✅ 备用方案成功获取用户ID:', targetUser.id);
            return targetUser.id;
          }
        }
      } catch (fallbackError) {
        console.error('❌ 备用方案也失败:', fallbackError);
      }
      
      return null;
    },

    // 获取用户信息
    async getUserInfo() {
      try {
        console.log('🔍 开始获取用户信息，当前userId:', this.userId);
        
        // 如果userId为null或不是数字，先获取用户ID
        let targetUserId = this.userId;
        if (!this.userId || isNaN(this.userId)) {
          console.log('🔄 userId为null或不是数字，需要先获取用户ID');
          // 从路由参数获取用户名
          const username = this.$route.params.username;
          if (!username) {
            console.error('❌ 无法获取用户名，路由参数为空');
            this.$message.error('用户不存在');
            return;
          }
          targetUserId = await this.getUserIdByUsername(username);
          if (!targetUserId) {
            console.error('❌ 无法获取用户ID，用户可能不存在');
            this.$message.error('用户不存在');
            return;
          }
          console.log('✅ 成功获取到用户ID:', targetUserId);
          // 更新userId
          this.userId = targetUserId;
        }
        
        const token = localStorage.getItem('token');
        console.log('📡 调用获取用户信息API，目标用户ID:', targetUserId);
        
        const response = await this.$axios.get(`/api/auth/user/${targetUserId}`, {
          headers: {
            'Authorization': token ? `Bearer ${token}` : ''
          },
          withCredentials: true
        });
        
        console.log('📡 获取用户信息API响应:', response.data);
        
        if (response.data.code === 200) {
          // 确保所有用户信息字段都被正确赋值
          const userData = response.data.data;
          this.userInfo = {
            id: userData.id || null,
            username: userData.username || '',
            email: userData.email || '',
            avatar: userData.avatar || userData.avatarUrl || '',
            createTime: userData.createTime || '',
            isOnline: userData.isOnline || 0,
            isAdmin: userData.isAdmin || false
          };
          console.log('✅ 用户信息获取成功:', this.userInfo);
        } else {
          console.warn('⚠️ API返回状态码异常:', response.data.code);
          this.$message.error('获取用户信息失败');
        }
      } catch (error) {
        console.error('❌ 获取用户信息失败:', error);
        // 记录详细的错误信息
        if (error.response) {
          console.error('❌ 错误状态码:', error.response.status);
          console.error('❌ 错误响应数据:', error.response.data);
        }
        this.$message.error('获取用户信息失败');
      }
    },

    // 获取用户统计信息
    async getUserStats() {
      try {
        console.log('🔍 开始获取用户统计信息，用户ID:', this.userId);
        const token = localStorage.getItem('token');
        const response = await this.$axios.get(`/api/follow/stats?userId=${this.userId}`, {
          headers: {
            'Authorization': token ? `Bearer ${token}` : ''
          },
          withCredentials: true
        });
        console.log('✅ 获取用户统计信息响应:', response.data);
        if (response.data.code === 200) {
          this.userStats = response.data.data;
          // 攻略数从攻略列表的长度获取
          this.userStats.guideCount = this.guides ? this.guides.length : 0;
          console.log('✅ 用户统计信息获取成功:', this.userStats);
        } else {
          console.warn('⚠️ 获取用户统计信息响应码异常:', response.data.code);
          // 设置默认值
          this.userStats = {
            followingCount: 0,
            followersCount: 0,
            guideCount: this.guides ? this.guides.length : 0
          };
        }
      } catch (error) {
        console.error('❌ 获取用户统计信息失败:', error);
        // 设置默认值
        this.userStats = {
          followingCount: 0,
          followersCount: 0,
          guideCount: this.guides ? this.guides.length : 0
        };
      }
    },

    // 检查关注状态
    async checkFollowStatus() {
      if (this.isCurrentUser) return;
      
      try {
        const token = localStorage.getItem('token');
        const response = await this.$axios.get(`/api/follow/is-following?followingId=${this.userId}`, {
          headers: {
            'Authorization': token ? `Bearer ${token}` : ''
          },
          withCredentials: true
        });
        if (response.data.code === 200) {
          this.isFollowing = response.data.data.isFollowing;
        }
      } catch (error) {
        console.error('检查关注状态失败:', error);
      }
    },

    // 处理关注/取消关注操作
    async handleFollowAction() {
      if (this.isCurrentUser) return;
      
      this.followLoading = true;
      try {
        const token = localStorage.getItem('token');
        const config = {
          headers: {
            'Authorization': token ? `Bearer ${token}` : '',
            'Content-Type': 'application/json'
          },
          withCredentials: true
        };
        
        if (this.isFollowing) {
          // 取消关注
          const response = await this.$axios.post(`/api/follow/unfollow?followingId=${this.userId}`, null, config);
          
          if (response.data.code === 200) {
            this.isFollowing = false;
            this.userStats.followersCount = Math.max(0, this.userStats.followersCount - 1);
            this.$message.success('取消关注成功');
          }
        } else {
          // 关注
          const response = await this.$axios.post(`/api/follow/follow?followingId=${this.userId}`, null, config);
          
          if (response.data.code === 200) {
            this.isFollowing = true;
            this.userStats.followersCount += 1;
            this.$message.success('关注成功');
          }
        }
      } catch (error) {
        console.error('关注操作失败:', error);
        this.$message.error('操作失败，请重试');
      } finally {
        this.followLoading = false;
      }
    },

    // 处理关注状态变化
    handleFollowChanged(userId, newFollowStatus) {
      // 如果关注的是当前页面用户，更新关注状态
      if (userId === this.userId) {
        this.isFollowing = newFollowStatus;
        // 更新粉丝数量
        if (newFollowStatus) {
          this.userStats.followersCount = (this.userStats.followersCount || 0) + 1;
        } else {
          this.userStats.followersCount = Math.max(0, (this.userStats.followersCount || 0) - 1);
        }
      }
      
      // 更新关注列表和粉丝列表中的关注状态
      this.updateRelationshipListStatus(userId, newFollowStatus);
    },

    // 更新关系列表中的关注状态
    updateRelationshipListStatus(userId, newFollowStatus) {
      // 更新关注列表
      const followingUser = this.followingList.find(user => user.id === userId);
      if (followingUser) {
        followingUser.isFollowing = newFollowStatus;
      }
      
      // 更新粉丝列表
      const followerUser = this.followersList.find(user => user.id === userId);
      if (followerUser) {
        followerUser.isFollowing = newFollowStatus;
      }
      
      // 更新弹窗中的用户列表
      if (this.relationshipDialog.users.length > 0) {
        const dialogUser = this.relationshipDialog.users.find(user => user.id === userId);
        if (dialogUser) {
          dialogUser.isFollowing = newFollowStatus;
        }
      }
    },

    // 获取用户攻略列表
    async getUserGuides() {
      try {
        console.log('🔍 开始获取用户攻略列表，用户ID:', this.userId);
        const token = localStorage.getItem('token');
        const response = await this.$axios.get(`/api/follow/user/${this.userId}/guides`, {
          headers: {
            'Authorization': token ? `Bearer ${token}` : ''
          },
          withCredentials: true
        });
        console.log('✅ 获取用户攻略列表响应:', response.data);
        if (response.data.code === 200) {
          this.guides = response.data.data || [];
          console.log('✅ 成功获取用户攻略列表，数量:', this.guides.length);
        } else {
          console.warn('⚠️ 获取用户攻略列表响应码异常:', response.data.code, response.data.message);
          this.guides = [];
        }
      } catch (error) {
        console.error('❌ 获取用户攻略列表失败:', error);
        this.guides = [];
      }
    },

    // 查看攻略详情
    viewGuideDetail(guideId) {
      this.$router.push(`/guide/${guideId}`);
    },

    // 查看用户资料
    viewUserProfile(username) {
      this.relationshipDialog.visible = false;
      this.$router.push(`/user/${username}`);
    },

    // 根据用户名获取用户ID（路由监听器专用）
    async getUserIdByUsernameForRoute(username) {
      try {
        console.log('🔍 路由监听器：开始根据用户名获取用户ID:', username);
        if (!username) {
          console.error('❌ 用户名参数为空');
          this.$message.error('用户不存在');
          return;
        }
        const userId = await this.getUserIdByUsername(username);
        if (userId) {
          this.userId = userId;
          this.getCurrentUserId();
          await this.initPageData();
        } else {
          console.error('❌ 无法获取用户ID，用户可能不存在');
          this.$message.error('用户不存在');
        }
      } catch (error) {
        console.error('❌ 路由监听器获取用户ID失败:', error);
        this.$message.error('获取用户信息失败');
      }
    },

    // 获取当前用户ID
    getCurrentUserId() {
      try {
        console.log('🔍 开始获取当前用户ID');
        // 从localStorage中获取当前用户信息
        const userInfoStr = localStorage.getItem('user');
        if (userInfoStr) {
          const userInfo = JSON.parse(userInfoStr);
          if (userInfo && userInfo.id) {
            this.currentUserId = userInfo.id;
            console.log('✅ 成功获取当前用户ID:', this.currentUserId);
          } else {
            console.warn('⚠️ 用户信息中无ID字段');
            this.currentUserId = null;
          }
        } else {
          console.warn('⚠️ 未找到用户信息，用户可能未登录');
          this.currentUserId = null;
        }
      } catch (error) {
        console.error('❌ 解析用户信息失败:', error);
        this.currentUserId = null;
      }
    },

    // 获取关注列表
    async getFollowingList() {
      try {
        console.log('🔍 开始获取关注列表，用户ID:', this.userId);
        const token = localStorage.getItem('token');
        const response = await this.$axios.get(`/api/follow/following?userId=${this.userId}`, {
          headers: {
            'Authorization': token ? `Bearer ${token}` : ''
          },
          withCredentials: true
        });
        console.log('✅ 获取关注列表响应:', response.data);
        if (response.data && response.data.code === 200 && response.data.data) {
          this.followingList = response.data.data || [];
          console.log('✅ 成功获取关注列表，数量:', this.followingList.length);
        } else {
          console.warn('⚠️ 获取关注列表响应码异常:', response.data ? response.data.code : '无响应数据');
          this.followingList = [];
        }
      } catch (error) {
        console.error('❌ 获取关注列表失败:', error);
        this.followingList = [];
      }
    },

    // 获取粉丝列表
    async getFollowersList() {
      try {
        console.log('🔍 开始获取粉丝列表，用户ID:', this.userId);
        const token = localStorage.getItem('token');
        const response = await this.$axios.get(`/api/follow/followers?userId=${this.userId}`, {
          headers: {
            'Authorization': token ? `Bearer ${token}` : ''
          },
          withCredentials: true
        });
        console.log('✅ 获取粉丝列表响应:', response.data);
        if (response.data && response.data.code === 200 && response.data.data) {
          this.followersList = response.data.data || [];
          console.log('✅ 成功获取粉丝列表，数量:', this.followersList.length);
        } else {
          console.warn('⚠️ 获取粉丝列表响应码异常:', response.data ? response.data.code : '无响应数据');
          this.followersList = [];
        }
      } catch (error) {
        console.error('❌ 获取粉丝列表失败:', error);
        this.followersList = [];
      }
    },

    // 显示关注列表
    async showFollowingList() {
      try {
        console.log('🔍 开始显示关注列表');
        // 先获取最新的关注列表数据
        await this.getFollowingList();
        console.log('✅ 关注列表数据已更新，数量:', this.followingList.length);
        this.relationshipDialog.title = '关注列表';
        this.relationshipDialog.users = this.followingList;
        this.relationshipDialog.visible = true;
        console.log('✅ 关注列表对话框已显示');
      } catch (error) {
        console.error('❌ 显示关注列表失败:', error);
        this.$message.error('获取关注列表失败');
      }
    },

    // 显示粉丝列表
    async showFollowersList() {
      try {
        console.log('🔍 开始显示粉丝列表');
        // 先获取最新的粉丝列表数据
        await this.getFollowersList();
        console.log('✅ 粉丝列表数据已更新，数量:', this.followersList.length);
        this.relationshipDialog.title = '粉丝列表';
        this.relationshipDialog.users = this.followersList;
        this.relationshipDialog.visible = true;
        console.log('✅ 粉丝列表对话框已显示');
      } catch (error) {
        console.error('❌ 显示粉丝列表失败:', error);
        this.$message.error('获取粉丝列表失败');
      }
    },

    // 初始化页面数据
    async initPageData() {
      try {
        console.log('🔍 开始初始化页面数据，用户ID:', this.userId);
        if (!this.userId) {
          console.error('❌ userId为空，无法初始化页面数据');
          return;
        }
        // 先获取用户基本信息，确保用户信息加载完成
        await this.getUserInfo();
        
        // 然后按顺序获取数据：先获取攻略列表，再获取统计信息（统计信息依赖攻略列表）
        await this.getUserGuides();
        await this.getUserStats();
        
        // 获取关注列表和粉丝列表
        console.log('🔍 开始获取关注列表和粉丝列表');
        await this.getFollowingList();
        await this.getFollowersList();
        console.log('✅ 关注列表数量:', this.followingList.length);
        console.log('✅ 粉丝列表数量:', this.followersList.length);
        
        if (!this.isCurrentUser) {
          await this.checkFollowStatus();
        }
        console.log('✅ 页面数据初始化完成');
      } catch (error) {
        console.error('初始化页面数据失败:', error);
      }
    }
  },
  
  watch: {
    // 监听路由参数变化
    '$route.params.username': {
      immediate: true,
      handler(newUsername) {
        if (newUsername) {
          // 根据用户名获取用户ID
          this.getUserIdByUsernameForRoute(newUsername);
        }
      }
    }
  },
  
  mounted() {
    // 从路由参数获取用户名
    const username = this.$route.params.username;
    if (username) {
      this.getUserIdByUsernameForRoute(username);
    }
  }
}
</script>

<style scoped>
.user-profile-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.user-info-card {
  margin-bottom: 20px;
}

.user-header {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  overflow: hidden;
  background-color: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  font-size: 48px;
  color: #999;
}

.user-details {
  flex: 1;
}

.username {
  margin: 0 0 10px 0;
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.user-stats {
  display: flex;
  gap: 30px;
  margin-bottom: 15px;
}

.stat-item {
  text-align: center;
}

.stat-label {
  display: block;
  font-size: 14px;
  color: #666;
  margin-bottom: 5px;
}

.stat-value {
  display: block;
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.stat-item {
  cursor: pointer;
  transition: color 0.3s;
}

.stat-item:hover {
  color: #409eff;
}

.follow-section {
  margin-top: 10px;
}

.user-content {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 20px;
}

.guides-section {
  margin-bottom: 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-controls {
  display: flex;
  align-items: center;
  gap: 10px;
}

.guide-count {
  color: #666;
  font-size: 14px;
}

.guides-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.guide-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 20px;
  border: 1px solid #e8e8e8;
  border-radius: 12px;
  transition: all 0.3s ease;
  background: white;
}

.guide-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
  border-color: #409eff;
}

.guide-info {
  flex: 1;
  margin-right: 20px;
}

.guide-title {
  margin: 0 0 12px 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
  line-height: 1.4;
}

.guide-summary {
  margin: 0 0 15px 0;
  color: #666;
  font-size: 14px;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.guide-meta {
  display: flex;
  align-items: center;
  gap: 15px;
  font-size: 13px;
  color: #999;
}

.guide-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.guide-meta i {
  font-size: 14px;
}

.guide-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 120px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #999;
}

.empty-state i {
  font-size: 64px;
  margin-bottom: 15px;
  opacity: 0.5;
}

.empty-state p {
  font-size: 16px;
  margin: 0;
}

.relationship-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.following-section,
.followers-section {
  margin-bottom: 0;
}

.relationship-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.view-more {
  text-align: center;
  padding: 10px;
  border-top: 1px solid #e8e8e8;
}

.relationship-dialog-content {
  max-height: 400px;
  overflow-y: auto;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}

.user-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  border-bottom: 1px solid #f0f0f0;
}

.user-item:last-child {
  border-bottom: none;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.user-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  overflow: hidden;
  background-color: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-details {
  flex: 1;
}

.user-details .username {
  margin: 0 0 5px 0;
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.follow-time {
  margin: 0;
  font-size: 12px;
  color: #999;
}

.user-actions {
  min-width: 100px;
  text-align: right;
}

.relationship-dialog-content {
  max-height: 400px;
  overflow-y: auto;
}

.relationship-list {
  display: flex;
  flex-direction: column;
  gap: 0;
}

@media (max-width: 768px) {
  .user-profile-container {
    padding: 10px;
  }
  
  .user-header {
    flex-direction: column;
    text-align: center;
    gap: 15px;
  }
  
  .user-content {
    grid-template-columns: 1fr;
  }
  
  .user-stats {
    justify-content: center;
  }
  
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .section-controls {
    width: 100%;
    justify-content: space-between;
  }
  
  .guide-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  
  .guide-info {
    margin-right: 0;
  }
  
  .guide-actions {
    width: 100%;
    flex-direction: row;
    justify-content: flex-end;
  }
  
  .guide-meta {
    flex-wrap: wrap;
  }
}
</style>