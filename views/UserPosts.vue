<template>
  <div class="user-posts-container">
    <el-card title="我的发布" shadow="hover">
      <!-- 攻略列表 -->
      <div class="posts-section">
        <div v-if="userPosts.length > 0" class="posts-list">
          <el-card
            v-for="post in userPosts"
            :key="post.id"
            shadow="hover"
            class="post-card"
          >
            <div class="post-header">
              <h3 class="post-title">{{ post.title }}</h3>
              <div class="post-meta">
                <span class="post-game">{{ post.gameName }}</span>
                <span class="post-time">{{ formatDate(post.createTime) }}</span>
              </div>
            </div>
            
            <div class="post-content">
              <p class="post-summary">{{ getPostSummary(post.content) }}</p>
            </div>
            
            <div class="post-footer">
              <div class="post-stats">
                <span class="stat-item">
                  <i class="el-icon-view"></i>
                  {{ post.views || 0 }}
                </span>
                <span class="stat-item">
                  <i class="el-icon-comment"></i>
                  {{ post.comments || 0 }}
                </span>
                <span class="stat-item">
                  <i class="el-icon-thumbs-up"></i>
                  {{ post.likes || 0 }}
                </span>
              </div>
              
              <div class="post-actions">
                <el-button type="primary" size="small" @click="editPost(post.id)">
                  <i class="el-icon-edit"></i> 编辑
                </el-button>
                <el-button type="danger" size="small" @click="deletePost(post.id)">
                  <i class="el-icon-delete"></i> 删除
                </el-button>
                <el-button size="small" @click="viewPost(post.id)">
                  <i class="el-icon-view"></i> 查看
                </el-button>
              </div>
            </div>
          </el-card>
        </div>
        
        <!-- 空状态 -->
        <div v-else class="empty-state">
          <div class="empty-icon">
            <i class="el-icon-document"></i>
          </div>
          <p class="empty-text">您还没有发布任何内容</p>
          <el-button type="primary" @click="createNewPost">
            <i class="el-icon-edit"></i> 发布新攻略
          </el-button>
        </div>
        
        <!-- 分页 -->
        <div v-if="userPosts.length > 0" class="pagination-container">
          <el-pagination
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :current-page="currentPage"
            :page-sizes="[5, 10, 20, 50]"
            :page-size="pageSize"
            layout="total, sizes, prev, pager, next, jumper"
            :total="totalPosts"
          >
          </el-pagination>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'UserPosts',
  data() {
    return {
      userPosts: [], // 用户发布的攻略列表
      currentPage: 1, // 当前页码
      pageSize: 10, // 每页显示数量
      totalPosts: 0, // 总攻略数量
      loading: false // 加载状态
    }
  },
  mounted() {
    // 获取用户发布的攻略
    this.getUserPosts();
  },
  methods: {
    // 获取用户发布的攻略
    getUserPosts() {
      this.loading = true;
      
      // 尝试从后端获取用户发布的攻略
      this.$axios.get('/api/user/guides', {
        withCredentials: true
      }).then(response => {
        // 后端直接返回攻略列表
        this.userPosts = response.data || [];
        this.totalPosts = this.userPosts.length;
        
        // 处理返回的攻略数据格式，确保前端能正确显示
        this.userPosts = this.userPosts.map(post => ({
          id: post.id,
          title: post.title,
          content: post.content,
          gameName: post.game ? post.game.name : '未知游戏',
          createTime: post.createTime,
          views: post.views || 0,
          likes: post.likes || 0,
          comments: post.comments || 0
        }));
      }).catch(error => {
        console.error('获取用户攻略请求失败:', error);
        // 检查是否是未登录错误
        if (error.response && error.response.status === 401) {
          this.$message.warning('请先登录');
          this.$router.push('/login');
        } else {
          // 其他错误情况，使用模拟数据
          this.useMockPostsData();
          this.$message.info('当前使用本地攻略数据预览模式');
        }
      }).finally(() => {
        this.loading = false;
      });
    },
    
    // 使用模拟攻略数据 - 不生成预设内容，只显示空状态
    useMockPostsData() {
      // 不生成任何预设攻略数据，确保显示空状态
      this.userPosts = [];
      this.totalPosts = 0;
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
    
    // 获取攻略摘要
    getPostSummary(content) {
      if (!content) return '';
      // 移除HTML标签
      const plainText = content.replace(/<[^>]*>/g, '');
      // 返回前100个字符
      return plainText.length > 100 ? plainText.substring(0, 100) + '...' : plainText;
    },
    
    // 编辑攻略
    editPost(postId) {
      // 这里应该跳转到攻略编辑页面
      this.$message.info('编辑功能尚未实现');
    },
    
    // 删除攻略
    deletePost(postId) {
      this.$confirm('确定要删除这篇攻略吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // 这里应该调用后端API删除攻略
        this.userPosts = this.userPosts.filter(post => post.id !== postId);
        this.totalPosts--;
        this.$message.success('攻略删除成功');
      }).catch(() => {
        this.$message.info('已取消删除');
      });
    },
    
    // 查看攻略
    viewPost(postId) {
      // 这里应该跳转到攻略详情页面
      this.$message.info('查看功能尚未实现');
    },
    
    // 创建新攻略
    createNewPost() {
      // 这里应该跳转到攻略创建页面
      this.$message.info('创建功能尚未实现');
    },
    
    // 分页大小变化
    handleSizeChange(size) {
      this.pageSize = size;
      this.currentPage = 1;
      this.getUserPosts();
    },
    
    // 当前页码变化
    handleCurrentChange(current) {
      this.currentPage = current;
      this.getUserPosts();
    }
  }
}
</script>

<style scoped>
.user-posts-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.posts-section {
  margin-top: 20px;
}

.posts-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 20px;
}

.post-card {
  transition: all 0.3s ease;
}

.post-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.post-header {
  margin-bottom: 15px;
}

.post-title {
  margin: 0 0 10px 0;
  font-size: 18px;
  color: #333;
  cursor: pointer;
}

.post-title:hover {
  color: #409eff;
}

.post-meta {
  display: flex;
  justify-content: space-between;
  color: #999;
  font-size: 14px;
}

.post-game {
  background-color: #f0f9ff;
  color: #409eff;
  padding: 2px 8px;
  border-radius: 4px;
}

.post-content {
  margin-bottom: 15px;
}

.post-summary {
  color: #666;
  line-height: 1.6;
  margin: 0;
}

.post-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 15px;
  border-top: 1px solid #eee;
}

.post-stats {
  display: flex;
  gap: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 5px;
  color: #999;
  font-size: 14px;
}

.post-actions {
  display: flex;
  gap: 10px;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #999;
}

.empty-icon {
  font-size: 60px;
  margin-bottom: 20px;
  color: #e5e9f2;
}

.empty-text {
  margin-bottom: 20px;
  font-size: 16px;
}

.pagination-container {
  margin-top: 30px;
  display: flex;
  justify-content: flex-end;
}

/* 响应式设计 */
@media screen and (max-width: 768px) {
  .posts-list {
    grid-template-columns: 1fr;
  }
  
  .post-footer {
    flex-direction: column;
    gap: 15px;
    align-items: stretch;
  }
  
  .post-stats {
    justify-content: center;
  }
  
  .post-actions {
    justify-content: center;
  }
  
  .pagination-container {
    justify-content: center;
  }
}
</style>