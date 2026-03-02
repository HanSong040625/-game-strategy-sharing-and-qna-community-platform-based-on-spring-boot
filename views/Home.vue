<template>
  <div class="home-container">
    
    <!-- 攻略轮播图和热门游戏榜区域 -->
    <div class="carousel-hot-games-section">
      <!-- 左侧轮播图 -->
      <div class="carousel-container" @mouseenter="handleCarouselMouseEnter" @mouseleave="handleCarouselMouseLeave">
        <div class="carousel-wrapper">
          <!-- 轮播轨道 -->
          <div class="carousel-track" :style="carouselStyle">
            <!-- 克隆的最后一张幻灯片 (用于无缝循环) -->
            <div v-if="topGuides.length > 0" 
              class="carousel-slide"
              :key="'last-clone'"
              @click="viewGuideDetail(topGuides[topGuides.length - 1].id)"
            >
              <div class="slide-content">
                <div class="guide-cover">
                  <img :src="getGuideImageUrl(topGuides[topGuides.length - 1])" :alt="topGuides[topGuides.length - 1].title" class="cover-image">
                  <div class="guide-overlay">
                    <div class="guide-stats">
                      <span class="stat-item">
                        <i class="el-icon-view"></i>
                        {{ topGuides[topGuides.length - 1].views || 0 }}
                      </span>
                      <span class="stat-item">
                        <i class="el-icon-star-on"></i>
                        {{ topGuides[topGuides.length - 1].likes || 0 }}
                      </span>
                    </div>
                  </div>
                </div>
                <div class="guide-info">
                  <h4 class="guide-title">{{ topGuides[topGuides.length - 1].title }}</h4>
                  <p class="guide-author">作者：{{ topGuides[topGuides.length - 1].author ? topGuides[topGuides.length - 1].author.username : '未知' }}</p>
                  <p class="guide-game">游戏：{{ topGuides[topGuides.length - 1].game ? topGuides[topGuides.length - 1].game.name : '未知' }}</p>
                  <div class="guide-weight">
                    <span class="weight-score">权重：{{ calculateWeight(topGuides[topGuides.length - 1]) }}</span>
                  </div>
                </div>
              </div>
            </div>
            
            <!-- 实际的幻灯片 -->
            <div 
              v-for="(guide, index) in topGuides" 
              :key="guide.id"
              class="carousel-slide"
              @click="viewGuideDetail(guide.id)"
            >
              <div class="slide-content">
                <div class="guide-cover">
                  <img :src="getGuideImageUrl(guide)" :alt="guide.title" class="cover-image">
                  <div class="guide-overlay">
                    <div class="guide-stats">
                      <span class="stat-item">
                        <i class="el-icon-view"></i>
                        {{ guide.views || 0 }}
                      </span>
                      <span class="stat-item">
                        <i class="el-icon-star-on"></i>
                        {{ guide.likes || 0 }}
                      </span>
                    </div>
                  </div>
                </div>
                <div class="guide-info">
                  <h4 class="guide-title">{{ guide.title }}</h4>
                  <p class="guide-author">作者：{{ guide.author ? guide.author.username : '未知' }}</p>
                  <p class="guide-game">游戏：{{ guide.game ? guide.game.name : '未知' }}</p>
                  <div class="guide-weight">
                    <span class="weight-score">权重：{{ calculateWeight(guide) }}</span>
                  </div>
                </div>
              </div>
            </div>
            
            <!-- 克隆的第一张幻灯片 (用于无缝循环) -->
            <div v-if="topGuides.length > 0" 
              class="carousel-slide"
              :key="'first-clone'"
              @click="viewGuideDetail(topGuides[0].id)"
            >
              <div class="slide-content">
                <div class="guide-cover">
                  <img :src="getGuideImageUrl(topGuides[0])" :alt="topGuides[0].title" class="cover-image">
                  <div class="guide-overlay">
                    <div class="guide-stats">
                      <span class="stat-item">
                        <i class="el-icon-view"></i>
                        {{ topGuides[0].views || 0 }}
                      </span>
                      <span class="stat-item">
                        <i class="el-icon-star-on"></i>
                        {{ topGuides[0].likes || 0 }}
                      </span>
                    </div>
                  </div>
                </div>
                <div class="guide-info">
                  <h4 class="guide-title">{{ topGuides[0].title }}</h4>
                  <p class="guide-author">作者：{{ topGuides[0].author ? topGuides[0].author.username : '未知' }}</p>
                  <p class="guide-game">游戏：{{ topGuides[0].game ? topGuides[0].game.name : '未知' }}</p>
                  <div class="guide-weight">
                    <span class="weight-score">权重：{{ calculateWeight(topGuides[0]) }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 轮播导航按钮 -->
          <button class="nav-btn prev-btn" @click="goPrev" v-if="topGuides.length > 0">
            <i class="el-icon-arrow-left"></i>
          </button>
          <button class="nav-btn next-btn" @click="goNext" v-if="topGuides.length > 0">
            <i class="el-icon-arrow-right"></i>
          </button>
          
          <!-- 轮播指示器 -->
          <div class="carousel-dots" v-if="topGuides.length > 0">
            <span 
              v-for="(guide, index) in topGuides" 
              :key="guide.id"
              class="dot"
              :class="{ active: displayIndex === index }"
              @click="goToIndex(index)"
            ></span>
          </div>
        </div>
      </div>
      
      <!-- 右侧热门游戏榜 -->
      <div class="hot-games-container">
          <div class="hot-games-header">
            <h3 class="hot-games-title">热度飙升榜</h3>
          </div>
          <div class="hot-games-list">
          <div 
            v-for="(game, index) in hotGames" 
            :key="game.id" 
            class="hot-game-item"
            @click="enterGameZone(game.id)"
          >
            <div class="hot-game-rank" :class="{ 
              'rank-1': index === 0, 
              'rank-2': index === 1, 
              'rank-3': index === 2 
            }">
              <span class="rank-number">{{ index + 1 }}</span>
            </div>
            <div class="hot-game-logo">
              <img :src="getImageUrl(game.logoUrl)" :alt="game.name" class="game-logo">
            </div>
            <div class="hot-game-info">
              <div class="hot-game-name">{{ game.name }}</div>
              <div class="hot-game-stats">
                <span class="stat-guides">攻略：{{ game.guideCount || 0 }}</span>
                <span class="stat-questions">问答：{{ game.questionCount || 0 }}</span>
              </div>
            </div>
            <div class="hot-game-total">
              <span class="total-count">{{ (game.guideCount || 0) + (game.questionCount || 0) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 游戏标签搜索组件 -->
    <div class="tag-search-section">
      <div class="tag-search-container">
        <h3 class="tag-search-title">按游戏标签搜索</h3>
        <div class="tag-selector">
          <el-checkbox-group v-model="selectedTags" @change="filterGamesByTags" class="tag-group">
            <el-checkbox 
              v-for="tag in availableTags" 
              :key="tag" 
              :label="tag"
              class="tag-checkbox"
            >
              {{ tag }}
            </el-checkbox>
          </el-checkbox-group>
        </div>
        <div class="tag-search-info">
          <span v-if="selectedTags.length > 0">
            已选择 {{ selectedTags.length }} 个标签：{{ selectedTags.join(', ') }}
          </span>
          <span v-else>请选择游戏标签进行筛选</span>
        </div>
      </div>
    </div>

    <!-- 游戏展示组件 -->
    <div class="games-section">
      <div v-if="filteredGames && filteredGames.length > 0" class="games-list">
        <!-- 渲染所有游戏 -->
        <div v-for="game in filteredGames" :key="game.id" class="game-card">
          <!-- 左侧游戏宣传图片 -->
          <div class="game-poster">
            <img 
              :src="getImageUrl(game.posterUrl)" 
              :alt="game.name + '海报'" 
              class="game-poster-image"
            >
          </div>
          
          <!-- 右侧游戏信息 -->
          <div class="game-info">
            <div class="game-header">
              <!-- 游戏logo -->
              <div class="game-logo">
                <img :src="getImageUrl(game.logoUrl)" :alt="game.name + 'Logo'" class="game-logo-image">
              </div>
              
              <!-- 游戏标题和分类 -->
              <div class="game-title-section">
                <h3 class="game-name">{{ game.name }}</h3>
                <div class="game-category">
                  <el-tag v-for="category in game.categories.split(',')" :key="category">{{ category.trim() }}</el-tag>
                </div>
              </div>
            </div>
            
            <!-- 游戏简介 -->
            <div class="game-description">
              <p>{{ game.description }}</p>
            </div>
            
            <!-- 游戏操作按钮 -->
            <div class="game-actions">
              <el-button type="primary" @click="enterGameZone(game.id)">进入专区</el-button>
              <el-button @click="viewGameDetail(game.id)">查看详情</el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Home',
  data() {
    return {
      username: '',
      featuredGames: [], // 推荐游戏列表
      filteredGames: [], // 过滤后的游戏列表
      topGuides: [], // 按权重排序的前9个攻略
      hotGames: [], // 热门游戏榜数据
      currentIndex: 1, // 当前轮播图索引 (从1开始，因为第一张是克隆的最后一张)
      autoPlayTimer: null, // 自动播放定时器
      isTransitioning: false, // 是否正在过渡中
      selectedTags: [], // 选中的标签
      availableTags: [] // 所有可用的标签
    }
  },
  mounted() {
    // 获取当前登录用户信息
    this.getCurrentUser();
    // 获取游戏数据
    this.getGameData();
    // 获取按权重排序的攻略数据
    this.getTopGuides();
    // 启动轮播图自动播放
    this.startAutoPlay();
    // 测试URL处理逻辑
    this.testImageUrlProcessing();
    // 获取热门游戏榜数据
    this.getHotGames();
  },
  
  beforeDestroy() {
    // 组件销毁时清除定时器
    this.stopAutoPlay();
  },
  
  computed: {
    // 显示的索引（用于指示器高亮）
    displayIndex() {
      if (this.topGuides.length === 0) return 0;
      
      // 计算真实显示的索引
      if (this.currentIndex === 0) {
        return this.topGuides.length - 1;
      } else if (this.currentIndex === this.topGuides.length + 1) {
        return 0;
      } else {
        return this.currentIndex - 1;
      }
    },
    
    // 轮播样式计算
    carouselStyle() {
      return {
        transform: `translateX(-${this.currentIndex * 100}%)`,
        transition: this.isTransitioning ? 'transform 0.5s ease-in-out' : 'none'
      };
    }
  },
  
  methods: {
    // 测试图片URL处理逻辑
    testImageUrlProcessing() {
      // 测试1: 完整URL
      const fullUrl = 'http://example.com/image.jpg';
      // 测试2: 已包含/uploads/前缀的URL
      const uploadsUrl = '/uploads/32f70ad7-6715-4fad-9081-9d30a5daf175.jpg';
      // 测试3: 只有文件名的URL
      const fileName = '32f70ad7-6715-4fad-9081-9d30a5daf175.jpg';
    },
    
    // 获取当前登录用户信息
    getCurrentUser() {
      this.$axios.get('/api/auth/current-user', { withCredentials: true }).then(response => {
        // axios会自动解析JSON响应，不需要再次解析
        const data = response.data;
        if (data && data.success) {
          this.username = data.data.username;
        }
      }).catch(error => {
        console.error('获取用户信息失败:', error);
      });
    },
    
    // 获取游戏数据
    getGameData() {
      // 首先获取推荐游戏
      this.$axios.get('/api/game/featured', { withCredentials: true }).then(response => {
        if (response.data && response.data.success) {
          this.featuredGames = response.data.data || [];
          this.filteredGames = [...this.featuredGames]; // 初始化过滤后的游戏列表
          
          // 提取所有可用的标签
          this.extractAvailableTags();
          
          // 调试输出第一个游戏的海报URL信息
          if (this.featuredGames && this.featuredGames.length > 0) {
          }
        }
      }).catch(error => {
        console.error('获取推荐游戏失败:', error);
        console.error('错误响应:', error.response);
      });
    },
    
    // 获取热门游戏榜数据
    getHotGames() {
      // 获取所有游戏数据
      this.$axios.get('/api/game/list', { withCredentials: true })
        .then(response => {
          let allGames = [];
          if (response.data && response.data.success) {
            allGames = response.data.data || [];
          }
          
          // 为每个游戏获取攻略和问答数量
          const gamePromises = allGames.map(game => 
            this.getGameStats(game.id).then(stats => ({
              ...game,
              guideCount: stats.guideCount,
              questionCount: stats.questionCount
            }))
          );
          
          // 等待所有游戏统计数据获取完成
          Promise.all(gamePromises)
            .then(gamesWithStats => {
              // 按攻略和问答总数降序排序，取前10名
              this.hotGames = gamesWithStats
                .sort((a, b) => {
                  const aTotal = (a.guideCount || 0) + (a.questionCount || 0);
                  const bTotal = (b.guideCount || 0) + (b.questionCount || 0);
                  return bTotal - aTotal;
                })
                .slice(0, 10);
              
              console.log('热门游戏榜数据:', this.hotGames);
            })
            .catch(error => {
              console.error('获取游戏统计数据失败:', error);
            });
        })
        .catch(error => {
          console.error('获取游戏列表失败:', error);
        });
    },
    
    // 获取单个游戏的攻略和问答数量
    getGameStats(gameId) {
      return new Promise((resolve) => {
        // 获取攻略数量 - 使用正确的API路径
        const guidePromise = this.$axios.get(`/api/game/${gameId}/guides`, { withCredentials: true })
          .then(response => (response.data || []).length)
          .catch(() => 0);
        
        // 获取问答数量 - 使用正确的API路径
        const questionPromise = this.$axios.get(`/api/questions?gameId=${gameId}`, { withCredentials: true })
          .then(response => {
            // 问答API返回的是包含questions字段的对象
            const questions = response.data?.questions || [];
            return questions.length;
          })
          .catch(() => 0);
        
        // 等待两个请求都完成
        Promise.all([guidePromise, questionPromise])
          .then(([guideCount, questionCount]) => {
            resolve({ guideCount, questionCount });
          })
          .catch(() => {
            resolve({ guideCount: 0, questionCount: 0 });
          });
      });
    },
    
    // 获取图片URL（处理相对路径）
    getImageUrl(url) {
      // 健壮性检查
      if (!url) {
        console.log('URL为空或null，返回默认图片');
        return '/static/images/default-poster.jpg';
      }
      
      // 检查URL是否已包含完整路径（以http://或https://开头）
      if (url.startsWith('http://') || url.startsWith('https://')) {
        return url;
      }
      
      // 处理相对路径（/uploads/、/avatars/ 或 /assets/avatars/）
      if (url.startsWith('/uploads/') || url.startsWith('/avatars/') || url.startsWith('/assets/avatars/')) {
        // 在开发环境中，直接返回相对路径，webpack dev server会处理
        return url;
      }
      
      // 其他情况返回原始URL
      return url;
    },
    
    // 进入游戏专区
    enterGameZone(gameId) {
      this.$router.push(`/game/${gameId}`)
    },
    viewGameDetail(gameId) {
      this.$router.push(`/game/${gameId}/detail`)
    },
    
    // 获取按权重排序的前9个攻略
    getTopGuides() {
      this.$axios.get('/api/guides/top9', { withCredentials: true })
        .then(response => {
          if (response.data && response.data.success) {
            this.topGuides = response.data.data || [];
  
            // 数据加载后确保轮播图在正确的初始位置
            this.$nextTick(() => {
              this.isTransitioning = false;
              this.currentIndex = 1; // 初始显示第一张真实幻灯片
            });
          }
        })
        .catch(error => {
          console.error('获取按权重排序的攻略失败:', error);
          console.error('错误响应:', error.response);
        });
    },
    
    // 计算攻略权重分数
    calculateWeight(guide) {
      if (!guide) return 0;
      const views = guide.views || 0;
      const likes = guide.likes || 0;
      // 权重计算公式：浏览量 * 0.5 + 点赞数 * 2
      return (views * 0.5 + likes * 2).toFixed(0);
    },
    
    // 获取攻略图片URL
    getGuideImageUrl(guide) {
      // 1. 如果攻略有设置封面图片，优先使用
      if (guide && guide.coverUrl && guide.coverUrl.trim() !== '') {
        const url = guide.coverUrl;
        if (!url) {
          return '/static/images/default-guide.jpg';
        }
        
        if (url.startsWith('http://') || url.startsWith('https://')) {
          return url;
        }
        
        // 处理相对路径（/uploads/、/avatars/ 或 /assets/avatars/）
        if (url.startsWith('/uploads/') || url.startsWith('/avatars/') || url.startsWith('/assets/avatars/')) {
          return url;
        }
        
        return url;
      }
      // 2. 如果攻略没有设置封面图片，使用对应游戏的宣传图
      if (guide.game && guide.game.posterUrl && guide.game.posterUrl.trim() !== '') {
        const url = guide.game.posterUrl;
        if (!url) {
          return '/static/images/default-guide.jpg';
        }
        
        if (url.startsWith('http://') || url.startsWith('https://')) {
          return url;
        }
        
        // 处理相对路径（/uploads/、/avatars/ 或 /assets/avatars/）
        if (url.startsWith('/uploads/') || url.startsWith('/avatars/') || url.startsWith('/assets/avatars/')) {
          return url;
        }
        
        return url;
      }
      // 3. 如果都没有，返回默认图片
      return '/static/images/default-guide.jpg';
    },
    
    // 查看攻略详情
    viewGuideDetail(guideId) {
      this.$router.push(`/guide/${guideId}`);
    },
    
    // 提取所有可用的标签
    extractAvailableTags() {
      const allTags = new Set();
      
      if (this.featuredGames && this.featuredGames.length > 0) {
        this.featuredGames.forEach(game => {
          if (game.categories) {
            const tags = game.categories.split(',').map(tag => tag.trim()).filter(tag => tag !== '');
            tags.forEach(tag => allTags.add(tag));
          }
        });
      }
      
      this.availableTags = Array.from(allTags).sort();
    },
    
    // 根据选中的标签过滤游戏
    filterGamesByTags() {
      if (this.selectedTags.length === 0) {
        // 如果没有选择任何标签，显示所有游戏
        this.filteredGames = [...this.featuredGames];
      } else {
        // 过滤游戏：只有包含所有选中标签的游戏才显示
        this.filteredGames = this.featuredGames.filter(game => {
          if (!game.categories) return false;
          
          const gameTags = game.categories.split(',').map(tag => tag.trim());
          
          // 检查游戏是否包含所有选中的标签
          return this.selectedTags.every(selectedTag => 
            gameTags.includes(selectedTag)
          );
        });
      }
    },
    
    // 下一张 - 使用克隆幻灯片实现无缝循环
    goNext() {
      if (this.isTransitioning || this.topGuides.length === 0) return;
      
      this.isTransitioning = true;
      this.currentIndex++;
      
      // 过渡完成后检查是否需要重置索引
      setTimeout(() => {
        // 如果到达了克隆的第一张幻灯片
        if (this.currentIndex === this.topGuides.length + 1) {
          // 禁用过渡，瞬间跳转到真实的第一张幻灯片
          this.isTransitioning = false;
          this.currentIndex = 1;
        } else {
          this.isTransitioning = false;
        }
      }, 500);
    },
    
    // 上一张 - 使用克隆幻灯片实现无缝循环
    goPrev() {
      if (this.isTransitioning || this.topGuides.length === 0) return;
      
      this.isTransitioning = true;
      this.currentIndex--;
      
      // 过渡完成后检查是否需要重置索引
      setTimeout(() => {
        // 如果到达了克隆的最后一张幻灯片
        if (this.currentIndex === 0) {
          // 禁用过渡，瞬间跳转到真实的最后一张幻灯片
          this.isTransitioning = false;
          this.currentIndex = this.topGuides.length;
        } else {
          this.isTransitioning = false;
        }
      }, 500);
    },
    
    // 跳转到指定索引
    goToIndex(index) {
      if (this.isTransitioning || index < 0 || index >= this.topGuides.length) return;
      
      this.isTransitioning = true;
      this.currentIndex = index + 1; // +1 因为索引从1开始是真实的第一张
      
      setTimeout(() => {
        this.isTransitioning = false;
      }, 500);
    },
    
    // 自动播放控制 - 更可靠的实现
    startAutoPlay() {
      // 清除可能存在的定时器
      this.stopAutoPlay();
      
      this.autoPlayTimer = setInterval(() => {
        this.goNext();
      }, 5000); // 5秒自动切换一次
    },
    
    stopAutoPlay() {
      if (this.autoPlayTimer) {
        clearInterval(this.autoPlayTimer);
        this.autoPlayTimer = null;
      }
    },
    
    // 鼠标悬停时停止自动播放
    handleCarouselMouseEnter() {
      this.stopAutoPlay();
    },
    
    // 鼠标离开时重新开始自动播放
    handleCarouselMouseLeave() {
      this.startAutoPlay();
    }
  }
}
</script>

<style scoped>
.home-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

/* 攻略轮播图样式 */
.guide-carousel-section {
  margin-bottom: 40px;
}

.carousel-container {
  width: 500px;
  height: 463px; /* 增加高度100px */
  background: transparent;
  border-radius: 12px;
  box-shadow: none;
  padding: 0;
  position: relative;
  overflow: hidden;
}

.carousel-title {
  color: white;
  text-align: center;
  margin-bottom: 10px; /* 减少标题间距 */
  font-size: 20px; /* 减小标题字体 */
  font-weight: 600;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.carousel-wrapper {
  width: 100%;
  height: calc(100% - 40px); /* 减少标题区域占用高度 */
  overflow: hidden;
  border-radius: 8px;
  position: relative;
}

.carousel-track {
  display: flex;
  height: 100%;
  /* transition由Vue动态控制，不再这里静态定义 */
}

.carousel-slide {
  flex: 0 0 100%;
  height: 100%;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.carousel-slide:hover {
  transform: scale(1.02);
}

.slide-content {
  width: 100%;
  height: 100%;
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.guide-cover {
  position: relative;
  width: 100%;
  height: 75%; /* 增加图片区域高度比例 */
  overflow: hidden;
}

.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.carousel-slide:hover .cover-image {
  transform: scale(1.1);
}

.guide-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.7));
  padding: 8px; /* 减少内边距 */
}

.guide-stats {
  display: flex;
  gap: 10px; /* 减少间距 */
  color: white;
  font-size: 12px; /* 减小字体 */
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 3px; /* 减少间距 */
}

.guide-info {
  padding: 10px; /* 减少内边距 */
  height: 25%; /* 减少信息区域高度比例 */
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.guide-title {
  font-size: 13px; /* 减小字体 */
  font-weight: 600;
  margin-bottom: 5px; /* 减少间距 */
  color: #333;
  line-height: 1.3; /* 调整行高 */
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 35px; /* 减小高度 */
  max-height: 35px;
}

.guide-author,
.guide-game {
  font-size: 11px; /* 减小字体 */
  color: #666;
  margin-bottom: 3px; /* 减少间距 */
}

.guide-weight {
  text-align: right;
}

.weight-score {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 3px 6px; /* 减少内边距 */
  border-radius: 10px; /* 减小圆角 */
  font-size: 11px; /* 减小字体 */
  font-weight: 500;
}

.carousel-nav {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 15px;
  margin-top: 15px;
}

.nav-btn {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 50%;
  background: rgba(102, 126, 234, 0.8);
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.nav-btn:hover:not(:disabled) {
  background: rgba(102, 126, 234, 1);
  transform: scale(1.1);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.nav-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.carousel-dots {
  display: flex;
  gap: 8px;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: rgba(102, 126, 234, 0.5);
  cursor: pointer;
  transition: all 0.3s ease;
}

.dot.active {
  background: rgba(102, 126, 234, 1);
  transform: scale(1.2);
}

.dot:hover {
    background: rgba(102, 126, 234, 0.8);
  }

  /* 标签搜索区域样式 - 优化版 */
  .tag-search-section {
    margin: 40px 0;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 16px;
    padding: 30px;
    box-shadow: 0 10px 30px rgba(102, 126, 234, 0.3);
    position: relative;
    overflow: hidden;
  }
  
  .tag-search-section::before {
    content: '';
    position: absolute;
    top: -50%;
    left: -50%;
    width: 200%;
    height: 200%;
    background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 70%);
    animation: rotate 15s linear infinite;
  }
  
  @keyframes rotate {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
  }
  
  .tag-search-container {
    max-width: 1200px;
    margin: 0 auto;
    position: relative;
    z-index: 2;
  }
  
  .tag-search-title {
    color: white;
    margin-bottom: 20px;
    font-size: 24px;
    font-weight: 700;
    text-align: center;
    text-shadow: 0 2px 4px rgba(0,0,0,0.3);
  }
  
  .tag-selector {
    margin-bottom: 20px;
    background: rgba(255, 255, 255, 0.95);
    border-radius: 12px;
    padding: 25px;
    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  }
  
  .tag-group {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    justify-content: center;
  }
  
  .tag-checkbox {
    margin-right: 12px;
    margin-bottom: 8px;
    transition: all 0.3s ease;
  }
  
  .tag-checkbox:hover {
    transform: translateY(-2px);
  }
  
  .tag-checkbox .el-checkbox__label {
    font-size: 14px;
    font-weight: 500;
    color: #333;
    padding: 8px 16px;
    background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
    border-radius: 20px;
    border: 1px solid #e1e5e9;
    transition: all 0.3s ease;
  }
  
  .tag-checkbox .el-checkbox__input.is-checked + .el-checkbox__label {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    border-color: #667eea;
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
  }
  
  .tag-checkbox .el-checkbox__input.is-checked + .el-checkbox__label:hover {
    transform: translateY(-1px);
    box-shadow: 0 6px 15px rgba(102, 126, 234, 0.5);
  }
  
  .tag-search-info {
    font-size: 14px;
    font-weight: 500;
    color: white;
    padding: 12px 20px;
    background: rgba(255, 255, 255, 0.2);
    border-radius: 8px;
    backdrop-filter: blur(10px);
    border: 1px solid rgba(255, 255, 255, 0.3);
    text-align: center;
  }

  /* 游戏展示区域样式 */
  .games-section {
    margin-top: 40px;
    backdrop-filter: blur(10px);
  }

.reserved-content {
  position: relative;
  z-index: 2;
  text-align: center;
  color: white;
  padding: 30px;
}

.reserved-content h3 {
  font-size: 24px;
  margin-bottom: 15px;
  font-weight: 600;
}

.reserved-content p {
  font-size: 16px;
  opacity: 0.9;
  line-height: 1.5;
}

/* 搜索区域样式 - 优化版 */
/* 移除紫色渐变背景，采用更简洁的设计 */
.search-section {
  text-align: center;
  margin-bottom: 40px;
  padding: 20px 0; /* 减少内边距 */
  /* 移除紫色渐变背景 */
  /* 移除圆角和文字颜色设置 */
}

/* 搜索框容器 */
.search-box-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px; /* 输入框和按钮之间的间距 */
  max-width: 600px;
  margin: 0 auto;
  padding: 10px;
}

/* 搜索输入框 */
.search-input {
  flex: 1;
  max-width: 400px;
}

/* 输入框内部样式优化 */
.search-input .el-input__inner {
  border-radius: 25px;
  height: 45px; /* 略微降低高度 */
  font-size: 15px;
  padding-left: 45px;
  border: 2px solid #e0e0e0; /* 更明显的边框 */
  transition: all 0.3s ease; /* 过渡动画 */
}

/* 输入框聚焦效果 */
.search-input .el-input__inner:focus {
  border-color: #4CAF50; /* 聚焦时变为绿色边框 */
  box-shadow: 0 0 0 2px rgba(76, 175, 80, 0.2); /* 聚焦光晕效果 */
}

/* 输入框图标样式 */
.search-input .el-input__icon {
  line-height: 45px;
  font-size: 16px;
  color: #666; /* 图标颜色 */
}

/* 搜索按钮 */
.search-button {
  border-radius: 25px;
  height: 45px;
  padding: 0 25px;
  font-size: 15px;
  font-weight: 500;
  background-color: #4CAF50; /* 保持绿色按钮 */
  border: none;
  transition: all 0.3s ease;
}

/* 搜索按钮悬停效果 */
.search-button:hover {
  background-color: #45a049;
  transform: translateY(-1px); /* 轻微上移 */
  box-shadow: 0 2px 8px rgba(76, 175, 80, 0.3); /* 绿色阴影 */
}

/* 搜索按钮点击效果 */
.search-button:active {
  transform: translateY(0); /* 点击时恢复原位 */
  box-shadow: 0 1px 4px rgba(76, 175, 80, 0.2); /* 更小的阴影 */
}

/* 游戏展示区域 */
.games-section {
  margin-top: 40px;
}

.games-section h2 {
  margin-bottom: 20px;
  color: #333;
  font-size: 24px;
}

/* 游戏卡片样式 */
.game-card {
  display: flex;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  margin-bottom: 30px;
}

/* 左侧宣传图片 */
.game-poster {
  flex: 0 0 400px;
  height: 250px;
  overflow: hidden;
}

.game-poster-image {
  width: 400px;
  height: 250px;
  object-fit: cover;
  display: block;
}

/* 右侧游戏信息 */
.game-info {
  flex: 1;
  padding: 20px;
  display: flex;
  flex-direction: column;
}

/* 游戏头部信息 */
.game-header {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

/* 游戏logo */
.game-logo {
  width: 35px;
  height: 35px;
  border-radius: 4px;
  object-fit: contain;
  margin-right: 8px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.2);
  background-color: rgba(255,255,255,0.1);
}

.game-logo-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
  border-radius: 6px;
}

/* 游戏标题区域 */
.game-title-section {
  flex: 1;
}

.game-name {
  margin: 0;
  font-size: 22px;
  color: #333;
}

.game-category {
  margin-top: 5px;
}

.game-category .el-tag {
  margin-right: 5px;
}

/* 游戏描述 */
.game-description {
  flex: 1;
  margin-bottom: 20px;
}

.game-description p {
  color: #666;
  line-height: 1.6;
  margin: 0;
}

/* 游戏操作按钮 */
.game-actions .el-button {
  margin-top: 0;
}


/* 游戏列表容器 */
.games-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 热门游戏榜样式 */
.hot-games-section {
  flex: 0 0 300px;
  height: 463px; /* 与轮播图高度一致 */
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 15px; /* 减少内边距 */
  box-shadow: 0 10px 30px rgba(102, 126, 234, 0.3);
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.hot-games-section::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 70%);
  animation: rotate 15s linear infinite;
}

.hot-games-container {
  position: relative;
  z-index: 2;
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 0; /* 移除内边距 */
  margin-top: -50px; /* 向上移动50px */
}

.hot-games-header {
  margin-bottom: 5px; /* 进一步减少标题区域间距 */
  display: flex;
  justify-content: flex-start; /* 确保标题位于左侧 */
}

.hot-games-title {
  color: black;
  margin-bottom: 5px; /* 减少标题间距 */
  font-size: 14px; /* 缩小字体 */
  font-weight: 700;
  text-align: left; /* 左对齐 */
  text-shadow: 0 1px 2px rgba(0,0,0,0.1);
}

.hot-games-subtitle {
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: 5px; /* 进一步减少副标题间距 */
  font-size: 10px;
  text-align: left; /* 左对齐 */
}

.hot-games-list {
  display: flex;
  flex-direction: column;
  gap: 6px; /* 进一步减少游戏项间距 */
  flex: 1;
  overflow-y: auto;
  max-height: calc(463px - 30px); /* 增加列表可显示高度 */
}

.hot-game-item {
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 8px;
  padding: 6px;
  transition: all 0.3s ease;
  cursor: pointer;
  min-height: 45px;
}

.hot-game-item:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.hot-game-rank {
  flex: 0 0 25px;
  text-align: center;
  font-weight: 700;
  font-size: 16px;
  color: #667eea;
}

/* 第一名金牌样式 */
.hot-game-rank.rank-1 {
  color: #ffd700;
  background: linear-gradient(135deg, #ffd700, #ffed4e);
  border-radius: 50%;
  width: 25px;
  height: 25px;
  line-height: 25px;
  box-shadow: 0 2px 8px rgba(255, 215, 0, 0.4);
}

/* 第二名银牌样式 */
.hot-game-rank.rank-2 {
  color: #c0c0c0;
  background: linear-gradient(135deg, #c0c0c0, #e8e8e8);
  border-radius: 50%;
  width: 25px;
  height: 25px;
  line-height: 25px;
  box-shadow: 0 2px 8px rgba(192, 192, 192, 0.4);
}

/* 第三名铜牌样式 */
.hot-game-rank.rank-3 {
  color: #cd7f32;
  background: linear-gradient(135deg, #cd7f32, #e6b17e);
  border-radius: 50%;
  width: 25px;
  height: 25px;
  line-height: 25px;
  box-shadow: 0 2px 8px rgba(205, 127, 50, 0.4);
}

.hot-game-rank.top-3 {
  color: #ff6b6b;
}

.hot-game-logo {
  flex: 0 0 35px;
  height: 35px;
  margin: 0 8px;
  border-radius: 6px;
  overflow: hidden;
}

.hot-game-logo-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
  background-color: rgba(255,255,255,0.1);
}

.hot-game-info {
  flex: 1;
  min-width: 0;
}

.hot-game-name {
  font-size: 13px;
  font-weight: 600;
  color: #333;
  margin-bottom: 3px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.hot-game-stats {
  display: flex;
  gap: 6px;
  font-size: 11px;
  color: #666;
}

.hot-game-stat {
  display: flex;
  align-items: center;
  gap: 3px;
}

.hot-game-stat.guides {
  color: #667eea;
}

.hot-game-stat.questions {
  color: #ff6b6b;
}

/* 轮播图和热门游戏榜容器布局 */
.carousel-hot-games-section {
  display: flex;
  gap: 20px;
  margin-bottom: 40px;
}

.carousel-section {
  flex: 1;
}

</style>