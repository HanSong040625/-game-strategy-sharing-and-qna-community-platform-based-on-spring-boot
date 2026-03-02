<template>
  <div class="game-detail-container">
    <!-- 游戏头部信息 -->
    <div class="game-header">
      <div class="game-banner">
        <img 
          :src="getImageUrl(gameInfo.posterUrl)" 
          :alt="gameInfo.name + '横幅'" 
          class="banner-image"
        >
        <div class="game-overlay">
          <img 
            :src="getImageUrl(gameInfo.logoUrl)" 
            :alt="gameInfo.name + 'Logo'" 
            class="game-logo"
          >
          <div class="game-title-section">
            <h1 class="game-title">{{ gameInfo.name }}</h1>
            <div class="game-tags">
              <el-tag 
                v-for="category in gameCategories" 
                :key="category" 
                type="info"
              >
                {{ category }}
              </el-tag>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 游戏描述 -->
    <div class="game-description">
      <p>{{ gameInfo.description }}</p>
    </div>

    <!-- 游戏简介 -->
    <div v-if="gameInfo.introduction" class="game-introduction">
      <h3 class="section-title">游戏简介</h3>
      <div class="introduction-content">
        <p>{{ gameInfo.introduction }}</p>
      </div>
    </div>

    <!-- 详细信息 -->
    <div v-if="gameInfo.detailedInfo" class="game-detailed-info">
      <h3 class="section-title">详细信息</h3>
      <div class="detailed-info-content">
        <p>{{ gameInfo.detailedInfo }}</p>
      </div>
    </div>

    <!-- 游戏统计信息 -->
    <div class="game-stats">
      <div class="stats-container">
        <div class="stat-item">
          <div class="stat-value">{{ gameStats.guideCount || 0 }}</div>
          <div class="stat-label">攻略数量</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ gameStats.questionCount || 0 }}</div>
          <div class="stat-label">问答数量</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ gameStats.totalViews || 0 }}</div>
          <div class="stat-label">总浏览量</div>
        </div>
      </div>
    </div>

    <!-- 操作按钮 -->
    <div class="action-buttons">
      <el-button type="primary" size="large" @click="enterGameZone">
        <i class="el-icon-s-promotion"></i> 进入游戏专区
      </el-button>
      <el-button size="large" @click="goBack">
        <i class="el-icon-back"></i> 返回首页
      </el-button>
    </div>

    <!-- 相关游戏推荐 -->
    <related-games 
      :current-game-id="gameInfo.id" 
      :current-game-categories="gameCategories"
      v-if="!loading && !error"
    />

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="6" animated />
    </div>

    <!-- 错误状态 -->
    <div v-else-if="error" class="error-container">
      <el-result
        icon="error"
        title="加载失败"
        :sub-title="error"
      >
        <template slot="extra">
          <el-button type="primary" @click="retryLoad">重新加载</el-button>
        </template>
      </el-result>
    </div>
  </div>
</template>

<script>
export default {
  name: 'GameDetail',
  data() {
    return {
      gameInfo: {},
      gameStats: {},
      loading: true,
      error: null
    }
  },
  computed: {
    // 处理游戏分类
    gameCategories() {
      if (!this.gameInfo.categories) return [];
      return this.gameInfo.categories.split(',').map(cat => cat.trim()).filter(cat => cat);
    }
  },
  created() {
    this.loadGameDetail();
  },
  methods: {
    // 加载游戏详情
    async loadGameDetail() {
      this.loading = true;
      this.error = null;
      
      try {
        const gameId = this.$route.params.id;
        
        // 获取游戏基本信息
        const gameResponse = await this.$axios.get(`/api/game/${gameId}`, {
          withCredentials: true
        });
        
        if (gameResponse.data) {
          this.gameInfo = gameResponse.data;
          
          // 调试信息：检查是否接收到游戏简介和详细信息字段
          console.log('✅ 游戏详情数据接收成功:');
          console.log('  - 游戏ID:', this.gameInfo.id);
          console.log('  - 游戏名称:', this.gameInfo.name);
          console.log('  - 游戏简介字段是否存在:', 'introduction' in this.gameInfo);
          console.log('  - 游戏简介值:', this.gameInfo.introduction);
          console.log('  - 详细信息字段是否存在:', 'detailedInfo' in this.gameInfo);
          console.log('  - 详细信息值:', this.gameInfo.detailedInfo);
          console.log('  - 完整响应数据:', JSON.stringify(this.gameInfo, null, 2));
        } else {
          throw new Error('获取游戏信息失败');
        }
        
        // 获取游戏统计数据
        await this.loadGameStats(gameId);
        
      } catch (error) {
        console.error('加载游戏详情失败:', error);
        this.error = error.response?.data?.message || '加载游戏详情失败，请稍后重试';
      } finally {
        this.loading = false;
      }
    },
    
    // 加载游戏统计数据
    async loadGameStats(gameId) {
      try {
        // 获取攻略数量
        const guidePromise = this.$axios.get(`/api/game/${gameId}/guides`, {
          withCredentials: true
        }).then(response => (response.data || []).length);
        
        // 获取问答数量
        const questionPromise = this.$axios.get(`/api/questions?gameId=${gameId}`, {
          withCredentials: true
        }).then(response => {
          const questions = response.data?.questions || [];
          return questions.length;
        });
        
        // 总浏览量现在从后端游戏详情接口返回，直接使用gameInfo.totalViews
        const totalViews = this.gameInfo.totalViews || 0;
        
        const [guideCount, questionCount] = await Promise.all([
          guidePromise,
          questionPromise
        ]);
        
        this.gameStats = {
          guideCount,
          questionCount,
          totalViews
        };
        
      } catch (error) {
        console.error('获取游戏统计数据失败:', error);
        // 统计数据获取失败不影响主要功能
      }
    },
    
    // 图片URL处理
    getImageUrl(url) {
      if (!url) return '';
      
      // 检查URL是否已包含完整路径
      if (url.startsWith('http://') || url.startsWith('https://')) {
        return url;
      }
      
      // 处理相对路径
      if (url.startsWith('/uploads/') || url.startsWith('/avatars/') || url.startsWith('/assets/avatars/')) {
        return url;
      }
      
      // 其他情况返回原始URL
      return url;
    },
    
    // 进入游戏专区
    enterGameZone() {
      this.$router.push(`/game/${this.$route.params.id}`);
    },
    
    // 返回首页
    goBack() {
      this.$router.push('/home');
    },
    
    // 重新加载
    retryLoad() {
      this.loadGameDetail();
    }
  }
}
</script>

<style scoped>
.game-detail-container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

/* 游戏头部 */
.game-header {
  width: 100%;
}

.game-banner {
  position: relative;
  width: 100%;
  height: 400px;
  overflow: hidden;
}

.banner-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.game-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 40px 60px;
  background: linear-gradient(to top, rgba(0,0,0,0.8), rgba(0,0,0,0));
  color: white;
  display: flex;
  align-items: flex-end;
}

.game-logo {
  width: 120px;
  height: 120px;
  object-fit: contain;
  border-radius: 6px;
  margin-right: 30px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.5);
}

.game-title-section {
  flex: 1;
}

.game-title {
  margin: 0 0 10px 0;
  font-size: 42px;
  font-weight: bold;
  text-shadow: 2px 2px 4px rgba(0,0,0,0.5);
}

.game-tags {
  margin-top: 10px;
}

.game-tags .el-tag {
  background-color: rgba(255,255,255,0.2);
  border: 1px solid rgba(255,255,255,0.3);
  color: white;
  margin-right: 10px;
  font-size: 14px;
}

/* 游戏描述 */
.game-description {
  background-color: white;
  padding: 40px 60px;
  border-bottom: 1px solid #e6e6e6;
}

.game-description p {
  margin: 0;
  line-height: 1.8;
  color: #333;
  font-size: 16px;
  text-align: center;
  max-width: 800px;
  margin: 0 auto;
}

/* 游戏简介 */
.game-introduction {
  background-color: white;
  padding: 40px 60px;
  border-bottom: 1px solid #e6e6e6;
}

.game-introduction .section-title {
  margin: 0 0 20px 0;
  font-size: 24px;
  font-weight: bold;
  color: #333;
  text-align: center;
}

.introduction-content p {
  margin: 0;
  line-height: 1.8;
  color: #666;
  font-size: 16px;
  text-align: left;
  max-width: 800px;
  margin: 0 auto;
}

/* 详细信息 */
.game-detailed-info {
  background-color: white;
  padding: 40px 60px;
  border-bottom: 1px solid #e6e6e6;
}

.game-detailed-info .section-title {
  margin: 0 0 20px 0;
  font-size: 24px;
  font-weight: bold;
  color: #333;
  text-align: center;
}

.detailed-info-content p {
  margin: 0;
  line-height: 1.8;
  color: #666;
  font-size: 16px;
  text-align: left;
  max-width: 800px;
  margin: 0 auto;
  white-space: pre-wrap;
}

/* 游戏统计信息 */
.game-stats {
  background-color: white;
  padding: 40px 0;
}

.stats-container {
  display: flex;
  justify-content: center;
  gap: 60px;
  max-width: 800px;
  margin: 0 auto;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 36px;
  font-weight: bold;
  color: #1890ff;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

/* 操作按钮 */
.action-buttons {
  background-color: white;
  padding: 40px 0;
  text-align: center;
  border-top: 1px solid #e6e6e6;
}

.action-buttons .el-button {
  margin: 0 15px;
  min-width: 160px;
}

/* 加载状态 */
.loading-container {
  padding: 40px;
  background-color: white;
  margin: 20px;
  border-radius: 8px;
}

/* 错误状态 */
.error-container {
  padding: 40px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .game-overlay {
    padding: 20px 30px;
    flex-direction: column;
    align-items: flex-start;
  }
  
  .game-logo {
    width: 80px;
    height: 80px;
    margin-right: 0;
    margin-bottom: 15px;
  }
  
  .game-title {
    font-size: 28px;
  }
  
  .game-description {
    padding: 20px 30px;
  }
  
  .stats-container {
    gap: 30px;
    flex-wrap: wrap;
  }
  
  .stat-item {
    flex: 1;
    min-width: 100px;
  }
  
  .action-buttons .el-button {
    display: block;
    width: 100%;
    margin: 10px 0;
  }
}
</style>