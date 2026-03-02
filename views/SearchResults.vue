<template>
  <div class="search-results-container">
    <!-- 搜索结果头部 -->
    <div class="search-header">
      <h2>搜索结果: "{{ searchKeyword }}"</h2>
      <!-- 返回首页按钮 -->
      <el-button type="primary" @click="goBack">返回首页</el-button>
    </div>
    
    <!-- 搜索结果内容区域 -->
    <div class="search-content">
      <!-- 搜索结果为空时显示 -->
      <div v-if="searchResults.length === 0" class="no-results">
        <el-empty description="没有找到相关游戏"></el-empty>
      </div>
      
      <!-- 搜索结果列表 -->
      <div v-else class="games-list">
        <!-- 渲染搜索到的游戏卡片 -->
        <div v-for="game in searchResults" :key="game.id" class="game-card">
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
                  <el-tag v-for="category in game.categories.split(',')" :key="category" class="el-tag--light">{{ category.trim() }}</el-tag>
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
// 使用require方式导入pinyin-pro库
const { pinyin } = require('pinyin-pro');

export default {
  name: 'SearchResults',
  data() {
    return {
      searchKeyword: '', // 搜索关键字
      searchResults: [] // 搜索结果列表
    }
  },
  created() {
    // 组件创建时获取URL中的搜索参数
    this.initializeSearch()
  },
  watch: {
    // 监听路由变化，处理可能的参数更新
    '$route'(to) {
      this.initializeSearch()
    }
  },
  methods: {
    /**
     * 初始化搜索功能
     * 从路由参数中获取搜索关键字并执行搜索
     */
    initializeSearch() {
      // 从URL参数中获取搜索关键字
      this.searchKeyword = this.$route.query.keyword || ''
      console.log('初始化搜索，关键字:', this.searchKeyword)
      
      // 如果存在搜索关键字，执行搜索
      if (this.searchKeyword.trim()) {
        this.performSearch()
      }
    },
    
    /**
     * 执行搜索操作
     * 从数据库中获取游戏数据进行前端过滤搜索
     */
    performSearch() {
      console.log('开始搜索游戏:', this.searchKeyword)
      
      // 直接调用fetchGamesAndSearch，使用正确的API路径获取游戏数据
      this.fetchGamesAndSearch()
    },
    
    /**
     * 获取游戏数据并进行前端搜索
     * 从后端获取所有游戏数据，然后在前端进行过滤
     * 确保从数据库中获取数据，不使用静态数据
     */
    fetchGamesAndSearch() {
      console.log('【搜索过程】获取游戏数据进行前端搜索，关键字:', this.searchKeyword)
      
      // 尝试获取游戏数据 - 使用正确的API路径：/api/game/list
      this.$axios.get('/api/game/list', { withCredentials: true })
        .then(response => {
          const allGames = response.data || []
          console.log('【搜索过程-步骤2】获取数据库中的所有游戏列表，共', allGames.length, '个游戏')
          
          // 执行前端搜索过滤
          this.filterGamesByKeyword(allGames)
        })
        .catch(error => {
          console.error('获取游戏数据失败:', error)
          console.error('错误详情:', error.response || error.message)
          
          // 如果/api/game/list不可用，尝试使用/api/game/featured
          this.$axios.get('/api/game/featured', { withCredentials: true })
            .then(response => {
              const featuredGames = response.data || []
              console.log('【搜索过程-步骤2】从featured端点获取到游戏数据，共', featuredGames.length, '个游戏')
              
              // 执行前端搜索过滤
              this.filterGamesByKeyword(featuredGames)
            })
            .catch(finalError => {
              console.error('所有获取游戏数据的尝试都失败了:', finalError)
              console.error('错误详情:', finalError.response || finalError.message)
              
              // 清空搜索结果
              this.searchResults = []
              
              // 显示错误提示
              this.$message.error('获取游戏数据失败，请稍后重试')
            })
        })
    },
    
    /**
     * 根据关键字过滤游戏
     * @param {Array} games - 游戏数据数组
     */
    filterGamesByKeyword(games) {
      const keyword = this.searchKeyword.toLowerCase().trim()
      
      // 判断用户输入是否只包含英文字母
      const isEnglishInput = /^[a-zA-Z]+$/.test(keyword)
      
      console.log('【搜索过程-步骤1】搜索关键字:', keyword, '是否为英文字母:', isEnglishInput)
      
      // 步骤3: 将每个游戏的名称转换成英文并输出
      console.log('【搜索过程-步骤3】将每个游戏的名称转换成英文:')
      games.forEach((game, index) => {
        if (game && game.name) {
          // 使用getPinyin方法将游戏名称转换为拼音（更适合中文游戏名称）
          const gameNameEnglish = this.getPinyin(game.name)
          console.log(`  游戏${index + 1}: 原始名称='${game.name}', 英文部分='${gameNameEnglish}'`)
        }
      })
      
      // 过滤游戏
      let filteredGames = games.filter(game => {
        if (!game || !game.name) {
          return false;
        }
        
        // 转换游戏名称为小写
        const gameNameLower = game.name.toLowerCase()
        
        // 中文搜索逻辑 - 将游戏名称中的汉字转为拼音首字母后匹配
        const gameNamePinyin = this.getPinyin(game.name).toLowerCase()
        
        // 提取游戏名称中的英文字母部分
        const gameNameLetters = this.extractLetters(game.name)
        
        // 判断匹配情况
        let matchResult = false;
        
        if (isEnglishInput) {
          // 如果用户输入的是纯英文，则执行英文搜索逻辑
          // 方法1: 纯字母匹配 - 只比较字母部分
          const pureLetterMatch = gameNameLetters.includes(keyword)
          
          // 方法2: 名称中直接查找英文关键字
          const directNameMatch = gameNameLower.includes(keyword)
          
          // 方法3: 拼音匹配 - 搜索拼音中是否包含关键字
          // 注意：这里直接使用拼音结果，不做去空格处理，以支持音节匹配
          const pinyinMatch = gameNamePinyin.includes(keyword)
          
          // 综合匹配方法
          matchResult = pureLetterMatch || directNameMatch || pinyinMatch
        } else {
          // 如果用户输入的包含非英文字符（如中文），则执行原有搜索逻辑
          // 情况1: 直接匹配游戏名称（大小写不敏感）
          const nameMatch = gameNameLower.includes(keyword)
          
          // 情况2: 拼音匹配
          const pinyinMatch = gameNamePinyin.includes(keyword)
          
          matchResult = nameMatch || pinyinMatch
        }
        
        return matchResult
      })
      
      // 步骤4: 输出匹配结果
      console.log('【搜索过程-步骤4】是否有正确的匹配结果:', filteredGames.length > 0)
      console.log(`  匹配到的游戏数量: ${filteredGames.length}`)
      
      if (filteredGames.length > 0) {
        console.log('  匹配到的游戏列表:')
        filteredGames.forEach((game, index) => {
          if (game && game.name) {
            console.log(`    ${index + 1}. ${game.name}`)
          }
        })
      } else {
        console.log('  未找到任何匹配的游戏')
      }
      
      // 按匹配优先级排序
      filteredGames.sort((a, b) => {
        const keyword = this.searchKeyword.toLowerCase()
        
        // 根据搜索类型调整排序逻辑
        if (/^[a-zA-Z]+$/.test(keyword)) {
          // 英文搜索的排序逻辑
          const aLetters = this.extractLetters(a.name)
          const bLetters = this.extractLetters(b.name)
          const aName = a.name.toLowerCase()
          const bName = b.name.toLowerCase()
          
          // 计算匹配得分
          const calculateScore = (gameName, letters, keyword) => {
            let score = 0;
            
            // 完全匹配得分最高
            if (letters === keyword || gameName === keyword) {
              score += 1000;
            }
            // 开头匹配次之
            else if (gameName.startsWith(keyword)) {
              score += 500;
            }
            // 包含完整关键字
            else if (gameName.includes(keyword)) {
              score += 300;
            }
            // 包含所有字符
            else if (keyword.split('').every(char => letters.includes(char))) {
              score += 100;
            }
            
            // 匹配字符数量得分
            const matchCount = keyword.split('').filter(char => 
              letters.includes(char)
            ).length;
            score += matchCount * 10;
            
            return score;
          }
          
          const aScore = calculateScore(aName, aLetters, keyword)
          const bScore = calculateScore(bName, bLetters, keyword)
          
          // 得分高的排在前面
          if (aScore !== bScore) {
            return bScore - aScore;
          }
        }
        
        // 默认按名称字母顺序排序
        return a.name.toLowerCase().localeCompare(b.name.toLowerCase())
      })
      
      this.searchResults = filteredGames
      console.log('前端搜索完成，找到', filteredGames.length, '个匹配结果')
    },
    
    /**
     * 提取字符串中的英文字母
     * 这个辅助方法用于从游戏名称中提取所有英文字母（纯字母字符串）
     * @param {string} text - 输入文本
     * @returns {string} - 提取的英文字母
     */
    extractLetters(text) {
      if (!text || typeof text !== 'string') {
        return '';
      }
      return text.toLowerCase().replace(/[^a-z]/g, '');
    },
    
    /**
     * 降级搜索方案
     * 当搜索API不可用时，直接调用fetchGamesAndSearch方法
     */
    fallbackLocalSearch() {
      console.log('执行降级搜索')
      this.fetchGamesAndSearch()
    },
    
    /**
     * 汉字到完整拼音转换函数
     * 将汉字转换为对应的完整拼音
     * 使用pinyin-pro库实现准确的拼音转换
     * 
     * @param {string} text - 需要转换的文本
     * @returns {string} - 转换后的完整拼音字符串
     */
    getPinyin(text) {
      // 输入参数验证
      if (!text || typeof text !== 'string') {
        return '';
      }
      
      try {
        // 完全重新配置pinyin库，确保彻底去除声调
        let result = pinyin(text, {
          tone: 0, // 使用0值确保完全去除所有声调（包括数字和符号）
          type: 'string', // 输出格式为字符串
          nonZh: 'conserve', // 保留非中文字符
          v: true // 直接使用v代替ü
        });
        
        // 额外的正则替换，确保所有可能的声调符号都被移除
        // 匹配各种带声调的字母
        result = result.replace(/[āáǎàēéěèīíǐìōóǒòūúǔùǖǘǚǜ]/g, function(match) {
          // 返回对应的无声调字母
          const toneMap = {
            'ā':'a', 'á':'a', 'ǎ':'a', 'à':'a',
            'ē':'e', 'é':'e', 'ě':'e', 'è':'e',
            'ī':'i', 'í':'i', 'ǐ':'i', 'ì':'i',
            'ō':'o', 'ó':'o', 'ǒ':'o', 'ò':'o',
            'ū':'u', 'ú':'u', 'ǔ':'u', 'ù':'u',
            'ǖ':'v', 'ǘ':'v', 'ǚ':'v', 'ǜ':'v'
          };
          return toneMap[match] || match;
        });
        
        // 最后确保所有的ü都被转换为v
        result = result.replace(/ü/g, 'v');
        
        return result;
      } catch (error) {
        console.error('拼音转换出错:', error);
        
        // 降级方案：如果库转换失败，尝试使用正则表达式直接替换
        let fallbackResult = text;
        // 使用正则表达式移除所有带声调的字符
        fallbackResult = fallbackResult.replace(/[āáǎàēéěèīíǐìōóǒòūúǔùǖǘǚǜ]/g, function(match) {
          const toneMap = {
            'ā':'a', 'á':'a', 'ǎ':'a', 'à':'a',
            'ē':'e', 'é':'e', 'ě':'e', 'è':'e',
            'ī':'i', 'í':'i', 'ǐ':'i', 'ì':'i',
            'ō':'o', 'ó':'o', 'ǒ':'o', 'ò':'o',
            'ū':'u', 'ú':'u', 'ǔ':'u', 'ù':'u',
            'ǖ':'v', 'ǘ':'v', 'ǚ':'v', 'ǜ':'v'
          };
          return toneMap[match] || match;
        });
        
        return fallbackResult.toLowerCase();
      }
    },
    
    /**
     * 获取图片URL
     * 处理不同格式的图片路径，确保正确显示
     * @param {string} url - 原始图片URL
     * @returns {string} - 处理后的图片URL
     */
    getImageUrl(url) {
      // 健壮性检查
      if (!url) {
        console.log('URL为空或null，返回默认图片')
        return '/static/images/default-poster.jpg'
      }
      
      // 检查URL是否已包含完整路径
      if (url.startsWith('http://') || url.startsWith('https://')) {
        return url
      }
      
      // 处理相对路径（/uploads/、/avatars/ 或 /assets/avatars/）
      if (url.startsWith('/uploads/') || url.startsWith('/avatars/') || url.startsWith('/assets/avatars/')) {
        // 在开发环境中，直接返回相对路径，webpack dev server会处理
        return url;
      }
      
      // 其他情况返回原始URL
      return url;
    },
    
    /**
     * 进入游戏专区
     * @param {number} gameId - 游戏ID
     */
    enterGameZone(gameId) {
      this.$router.push(`/game/${gameId}`)
    },
    
    /**
     * 查看游戏详情
     * @param {number} gameId - 游戏ID
     */
    viewGameDetail(gameId) {
      this.$router.push(`/game/${gameId}/detail`)
    },
    
    /**
     * 返回首页
     */
    goBack() {
      this.$router.push('/')
    }
  }
}
</script>

<style scoped>
.search-results-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

/* 搜索头部样式 */
.search-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding: 20px 0;
  border-bottom: 1px solid #eee;
}

.search-header h2 {
  margin: 0;
  color: #333;
  font-size: 24px;
}

/* 搜索内容区域 */
.search-content {
  min-height: 400px;
}

/* 无搜索结果样式 */
.no-results {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 400px;
  background-color: #fafafa;
  border-radius: 8px;
}

/* 游戏列表样式 */
.games-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 游戏卡片样式 */
.game-card {
  display: flex;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

/* 卡片悬停效果 */
.game-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
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
  transition: transform 0.5s ease;
}

.game-poster:hover .game-poster-image {
  transform: scale(1.05);
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
  width: 60px;
  height: 60px;
  margin-right: 15px;
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
  font-weight: 600;
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
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 游戏操作按钮 */
.game-actions {
  display: flex;
  gap: 10px;
}

.game-actions .el-button {
  margin-top: auto;
}
</style>