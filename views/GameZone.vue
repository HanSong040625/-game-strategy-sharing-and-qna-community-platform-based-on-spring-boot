<template>
  <div class="game-zone-container">
    <!-- 发布攻略对话框 -->
    <el-dialog
      title="发布攻略"
      :visible.sync="publishGuideDialogVisible"
      width="800px"
      :before-close="handlePublishDialogClose"
    >
      <el-form ref="publishGuideForm" :model="publishGuideForm" :rules="publishGuideRules" label-width="100px">
        <el-form-item label="攻略标题" prop="title">
          <el-input v-model="publishGuideForm.title" placeholder="请输入攻略标题"></el-input>
        </el-form-item>
        <el-form-item label="攻略封面" prop="coverImageUrl">
          <el-input v-model="publishGuideForm.coverImageUrl" placeholder="请输入攻略封面URL" style="width: 70%; display: inline-block;"></el-input>
          <el-upload
            class="upload-demo"
            action="/api/upload"
            :show-file-list="false"
            :on-success="handleCoverUploadSuccess"
            :on-error="handleUploadError"
            :before-upload="beforeCoverUpload"
            :with-credentials="true"
            :limit="1"
            :file-list="[]"
            :accept="'image/*'"
            style="display: inline-block; margin-left: 10px;">
            <el-button type="primary">上传图片</el-button>
          </el-upload>
          <div v-if="publishGuideForm.coverImageUrl" class="upload-preview" style="margin-top: 10px;">
            <img :src="getImageUrl(publishGuideForm.coverImageUrl)" alt="攻略封面" class="preview-image">
            <el-button type="text" @click="removeCoverImage" class="remove-btn">移除</el-button>
          </div>
        </el-form-item>
        <el-form-item label="攻略内容" prop="content">
          <div style="position: relative;">
            <el-input 
              ref="contentInput"
              v-model="publishGuideForm.content" 
              type="textarea" 
              placeholder="请输入攻略内容" 
              rows="15"
              @focus="saveCursorPosition"
              @blur="saveCursorPosition"
              @input="saveCursorPosition"
            ></el-input>
            <el-upload
              class="insert-image-btn"
              action="/api/upload"
              :show-file-list="false"
              :on-success="handleInsertImageSuccess"
              :on-error="handleUploadError"
              :before-upload="beforeCoverUpload"
              :with-credentials="true"
              :limit="1"
              :file-list="[]"
              :accept="'image/*'"
              style="position: absolute; bottom: 10px; right: 10px;">
              <el-button type="primary" size="small">插入图片</el-button>
            </el-upload>
          </div>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="handlePublishDialogClose">取消</el-button>
        <el-button type="primary" @click="handlePublishGuide">发布</el-button>
      </div>
    </el-dialog>
    <!-- 游戏信息头部 -->
    <div v-if="gameInfo" class="game-header">
      <div class="game-banner">
        <img :src="getImageUrl(gameInfo.posterUrl)" :alt="gameInfo.name + '封面'" class="banner-image">
        <div class="game-overlay">
          <img :src="getImageUrl(gameInfo.logoUrl)" :alt="gameInfo.name + 'Logo'" class="game-logo">
          <h1 class="game-title">{{ gameInfo.name }}</h1>
          <div class="game-tags">
            <el-tag v-for="category in gameCategories" :key="category">{{ category }}</el-tag>
          </div>
        </div>
      </div>
      <div class="game-description">
        <p>{{ gameInfo.description }}</p>
      </div>
    </div>

    <!-- 内容区域 -->
    <div class="content-section">
      <!-- 侧边导航 -->
      <div class="sidebar">
        <el-menu
          :default-active="activeTab"
          class="el-menu-vertical-demo"
          @select="handleTabChange"
        >
          <el-menu-item index="guides">
            <i class="el-icon-notebook-2"></i>
            <span slot="title">攻略</span>
          </el-menu-item>
          <!-- 问答菜单已隐藏 -->
        </el-menu>
        
        <!-- 相关游戏推荐  自定义子组件-->
        <related-games 
          :current-game-id="gameId" 
          :current-game-categories="gameCategories"
        />
      </div>

      <!-- 主要内容 -->
        <div class="main-content">
          <!-- 攻略列表区域 -->
          <div v-show="activeTab === 'guides'" class="guides-section">
            <div class="section-header">
            
            <div class="filter-bar">
              <el-input
                v-model="searchQuery"
                placeholder="搜索攻略"
                prefix-icon="el-icon-search"
                class="search-input"
                @keyup.enter="handleSearch"
                clearable
              >
                <template slot="append">
                  <el-button @click="handleSearch" icon="el-icon-search"></el-button>
                </template>
              </el-input>
              <el-select v-model="guideFilter" placeholder="筛选攻略" class="filter-select">
                <el-option label="最新发布" value="latest"></el-option>
                <el-option label="浏览最高" value="hot"></el-option>
                <el-option label="点赞最多" value="official"></el-option>
              </el-select>
              <el-button type="primary" @click="navigateToPublishGuide">发布攻略</el-button>
              <el-button v-if="isSearching" type="info" @click="clearSearch">清空搜索</el-button>
            </div>
          </div>

          <!-- 攻略列表 -->
          <div class="guides-list">
            <div v-if="(isSearching && searchResults.length === 0) || (!isSearching && guides.length === 0)" class="empty-state">
              <el-empty :description="isSearching ? '未找到相关攻略' : '暂无攻略，快来发布第一篇吧！'"></el-empty>
            </div>
            <div v-else>
              <div v-for="guide in (isSearching ? searchResults : guides)" :key="guide.id" class="guide-item" @click="navigateToGuideDetail(guide.id)">
              <div class="guide-cover" v-if="guide.coverImageUrl">
                <img :src="getImageUrl(guide.coverImageUrl)" :alt="guide.title" class="cover-image">
              </div>
              <div class="guide-content">
                <h3 class="guide-title">{{ guide.title }}</h3>
                <div class="guide-meta">
                  <span class="author">{{ typeof guide.author === 'object' ? (guide.author.username || '未知作者') : (guide.author || '未知作者') }}</span>
                  <span class="time">{{ formatDate(guide.createTime) }}</span>
                  <div class="stats">
                    <span class="views"><i class="el-icon-view"></i> {{ guide.views || 0 }} 浏览</span>
                    <span class="likes"><i class="el-icon-thumb"></i> {{ guide.likes || 0 }} 点赞</span>
                    <span class="comments"><i class="el-icon-chat-line-round"></i> {{ guide.comments || 0 }} 评论</span>
                    <span v-if="isSearching" class="search-weight"><i class="el-icon-sort"></i> 匹配度: {{ guide.searchWeight }}</span>
                  </div>
                </div>
                <p class="guide-summary">{{ getPostSummary(guide.content) }}</p>
              </div>
              </div>
            </div>
          </div>

          <!-- 分页 -->
          <div v-if="guides.length > 0" class="pagination">
            <el-pagination
              background
              layout="prev, pager, next"
              :total="totalGuides"
              :page-size="pageSize"
              :current-page="currentPage"
              @current-change="handlePageChange"
            ></el-pagination>
          </div>
          
          <!-- 问答内容区域 - 已隐藏 -->
          <div v-show="false" class="questions-section">
            <div style="margin-bottom: 10px; padding: 10px; background-color: #f0f9ff; border: 1px solid #bae7ff;">
              <p>调试信息：</p>
              <p>当前activeTab: {{ activeTab }}</p>
              <p>传递给QuestionList的gameId: {{ gameId }}</p>
              <p>gameInfo: {{ JSON.stringify(gameInfo) }}</p>
            </div>
            <!-- 主问答列表组件 - 修正为kebab-case命名 -->
            <question-list :game-id="gameId" @hook:mounted="onQuestionListMounted" @hook:error="onQuestionListError" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import QuestionList from '../components/QuestionList.vue';
import RelatedGames from '../components/RelatedGames.vue';
import sensitiveWordFilter from '../utils/sensitiveWordFilter.js';
export default {
  name: 'GameZone',
  components: {
    QuestionList,
    RelatedGames
  },
  data() {
    return {
      gameInfo: null,
      // 默认选中攻略选项卡，进入页面直接显示攻略内容
      activeTab: 'guides',
      guideFilter: 'latest',
      guides: [],
      totalGuides: 0,
      currentPage: 1,
      pageSize: 10,
      gameId: null,
      // 搜索相关
      searchQuery: '',
      searchResults: [],
      isSearching: false,
      // 发布攻略相关数据
      publishGuideDialogVisible: false,
      publishGuideForm: {
        title: '',
        content: '',
        coverImageUrl: ''
      },
      // 敏感词过滤相关标志
      sensitiveFilterInitialized: false,
      publishGuideRules: {
        title: [
          { required: true, message: '请输入攻略标题', trigger: 'blur' },
          { min: 2, max: 200, message: '标题长度在 2 到 200 个字符', trigger: 'blur' }
        ],
        content: [
          { required: true, message: '请输入攻略内容', trigger: 'blur' },
          { min: 10, message: '内容至少需要10个字符', trigger: 'blur' }
        ]
      },
      // 光标位置相关
      cursorPosition: 0
    }
  },
  computed: {
    // 使用computed属性安全地处理游戏分类
    gameCategories() {
      // 确保gameInfo和categories属性存在
      if (!this.gameInfo || !this.gameInfo.categories) {
        return [];
      }
      // 将分类字符串分割并去除空白
      return this.gameInfo.categories.split(',').map(cat => cat.trim());
    }
  },
  watch: {
    // 监听路由参数变化，当游戏ID改变时重新获取数据
    '$route.params.id': {
      immediate: true,
      handler(newId) {
        if (newId) {
          this.gameId = Number(newId);
          this.resetPageData();
          this.fetchGameInfo();
          this.fetchGameGuides();
        }
      }
    }
  },
  created() {
      // 从路由参数中获取游戏ID，并转换为数值类型
      this.gameId = Number(this.$route.params.id);
      if (this.gameId) {
        this.fetchGameInfo();
        // 同时获取攻略列表，确保页面加载后攻略能够显示
        this.fetchGameGuides();
      }
      // 初始化空的攻略数据
      this.guides = [];
      this.totalGuides = 0;
    },
  async mounted() {
  
    // 默认显示攻略内容
  
    
    // 初始化敏感词过滤器
    await this.initializeSensitiveFilter();
  },
  methods: {
    // 处理标签切换
    handleTabChange(tab) {
      this.activeTab = tab;
    },
    
    // 处理搜索
    handleSearch() {
      if (!this.searchQuery.trim()) {
        this.clearSearch();
        return;
      }
      
      this.isSearching = true;
      const query = this.searchQuery.toLowerCase().trim();
      
      // 计算每个攻略的权重并排序
      this.searchResults = this.guides.map(guide => {
        const weight = this.calculateSearchWeight(guide, query);
        return {
          ...guide,
          searchWeight: weight
        };
      }).filter(guide => guide.searchWeight > 0) // 只保留权重大于0的结果
        .sort((a, b) => b.searchWeight - a.searchWeight); // 按权重降序排序
      
      console.log('搜索结果:', this.searchResults);
    },
    
    // 清空搜索
    clearSearch() {
      this.searchQuery = '';
      this.isSearching = false;
      this.searchResults = [];
    },
    
    // 重置页面数据
    resetPageData() {
      this.gameInfo = null;
      this.guides = [];
      this.totalGuides = 0;
      this.currentPage = 1;
      this.searchQuery = '';
      this.isSearching = false;
      this.searchResults = [];
      console.log('页面数据已重置，准备加载新游戏数据，游戏ID:', this.gameId);
    },
    
    // 计算搜索权重
    calculateSearchWeight(guide, query) {
      let weight = 0;
      
      // 标题中每个关键词出现一次权重+2
      if (guide.title && typeof guide.title === 'string') {
        const titleLower = guide.title.toLowerCase();
        // 简单的关键词匹配计算出现次数
        let titleCount = 0;
        let pos = titleLower.indexOf(query);
        while (pos !== -1) {
          titleCount++;
          pos = titleLower.indexOf(query, pos + 1);
        }
        weight += titleCount * 2;
      }
      
      // 正文中每个关键词出现一次权重+1
      if (guide.content && typeof guide.content === 'string') {
        const contentLower = guide.content.toLowerCase();
        let contentCount = 0;
        let pos = contentLower.indexOf(query);
        while (pos !== -1) {
          contentCount++;
          pos = contentLower.indexOf(query, pos + 1);
        }
        weight += contentCount;
      }
      
      return weight;
    },
    
    // 获取游戏信息
    fetchGameInfo() {
      this.$axios.get(`/api/game/${this.gameId}`, {
        withCredentials: true
      })
      .then(response => {
        console.log('API响应原始数据:', response);
        console.log('响应data字段:', response.data);
        
        // 修复：API返回的数据直接放在response.data中，不是response.data.data
        this.gameInfo = response.data;
        // gameCategories现在由computed属性处理，不需要在这里设置
        console.log('游戏信息获取成功:', this.gameInfo);
        console.log('当前游戏ID:', this.gameId);
        // 安全访问categories属性
        if (this.gameInfo && this.gameInfo.categories) {
          console.log('当前游戏标签:', this.gameInfo.categories);
          console.log('标签类型:', typeof this.gameInfo.categories);
          console.log('标签长度:', this.gameInfo.categories.length);
        } else {
          console.log('当前游戏标签: 未定义或为空');
          console.log('gameInfo对象:', this.gameInfo);
        }
        console.log('computed gameCategories:', this.gameCategories);
      })
      .catch(error => {
        console.error('获取游戏信息失败:', error);
        this.$message.error('获取游戏信息失败，请稍后重试');
      });
    },
    
    // 监听QuestionList组件挂载
    onQuestionListMounted() {
      console.log('QuestionList组件已挂载，gameId:', this.gameId);
      // 移除成功提示消息，避免干扰用户体验
    },
    
    // 监听QuestionList组件错误
    onQuestionListError(error) {
      console.error('QuestionList组件加载错误:', error);
      this.$message.error('问答列表组件加载失败');
    },

    // 获取游戏攻略
    fetchGameGuides() {
      const params = {
        page: this.currentPage,
        size: this.pageSize,
        sort: this.guideFilter
      };

      this.$axios.get(`/api/game/${this.gameId}/guides`, {
        params,
        withCredentials: true
      })
      .then(response => {
        // 根据后端返回的数据格式调整
        this.guides = response.data || [];
        this.totalGuides = this.guides.length;
        
        // 格式化攻略数据，确保前端渲染正常
        this.guides.forEach(guide => {
          guide.author = guide.author?.username || '未知作者';
          guide.views = guide.views || 0;
          guide.likes = guide.likes || 0;
          guide.comments = guide.comments || 0;
        });
      })
      .catch(error => {
        console.error('获取游戏攻略失败:', error);
        this.guides = [];
        this.totalGuides = 0;
      });
    },



    // 处理分页变化
    handlePageChange(page) {
      this.currentPage = page;
      this.fetchGameGuides();
    },

    // 打开发布攻略对话框
    navigateToPublishGuide() {
      // 检查用户是否已登录
      // 同时依赖localStorage和后端会话验证
      const username = localStorage.getItem('username');
      console.log('检查登录状态:', {username, cookie: document.cookie});
      
      if (!username) {
        this.$message.error('请先登录');
        this.$router.push('/login');
        return;
      }
      
      // 增加一个主动验证会话有效性的步骤
      this.$axios.get('/api/auth/current-user', {
        withCredentials: true
      }).then(response => {
        console.log('会话验证结果:', response.data);
        if (response.data.code === 200) {
          this.publishGuideForm = {
            title: '',
            content: ''
          };
          this.publishGuideDialogVisible = true;
        } else {
          // 会话可能已过期
          console.warn('会话验证失败，需要重新登录');
          localStorage.removeItem('username');
          this.$message.error('登录已过期，请重新登录');
          this.$router.push('/login');
        }
      }).catch(error => {
        console.error('会话验证请求失败:', error);
        // 即使验证请求失败，也尝试打开对话框，让用户尝试发布操作
        this.publishGuideForm = {
          title: '',
          content: ''
        };
        this.publishGuideDialogVisible = true;
      });
    },

    // 处理发布攻略对话框关闭
    handlePublishDialogClose() {
      this.$refs.publishGuideForm && this.$refs.publishGuideForm.resetFields();
      this.publishGuideDialogVisible = false;
    },

    // 处理封面图片上传成功
      handleCoverUploadSuccess(res) {
        // 检查响应格式，确保获取正确的文件路径
        let filePath = res;
        if (typeof res === 'string') {
          filePath = res;
        } else if (res && res.data) {
          filePath = res.data;
        }
        
        this.publishGuideForm.coverImageUrl = filePath;
        this.$message.success('封面上传成功！');
      },
      
      // 处理上传错误
      handleUploadError(err) {
        console.error('图片上传失败:', err);
        this.$message.error('图片上传失败');
      },

      // 上传前的验证
      beforeCoverUpload(file) {
        const isLt10M = file.size / 1024 / 1024 < 10;
        if (!isLt10M) {
          this.$message.error('上传图片大小不能超过 10MB!');
          return false;
        }
        const isImage = file.type.startsWith('image/');
        if (!isImage) {
          this.$message.error('只能上传图片文件!');
          return false;
        }
        return true;
      },

      // 处理超出限制的情况
      handleUploadExceed(files, fileList) {
        this.$message.error('只能上传一个封面图片');
      },

      // 移除封面图片
      removeCoverImage() {
        this.publishGuideForm.coverImageUrl = '';
      },
      
      // 保存光标位置
      saveCursorPosition() {
        // 尝试获取textarea元素
        const textarea = this.$refs.contentInput && this.$refs.contentInput.$el.querySelector('textarea');
        if (textarea) {
          this.cursorPosition = textarea.selectionStart;
        }
      },
      
      // 处理插入图片成功
      handleInsertImageSuccess(res) {
        // 检查响应格式，确保获取正确的文件路径
        let filePath = res;
        if (typeof res === 'string') {
          filePath = res;
        } else if (res && res.data) {
          filePath = res.data;
        }
        
        // 获取完整的图片URL
        const imageUrl = this.getImageUrl(filePath);
        
        // 构建HTML格式的图片标签，而不是Markdown格式
        const imageTag = `\n<img src="${imageUrl}" alt="图片" style="max-width: 100%; height: auto; margin: 20px 0; border-radius: 4px;">\n`;
        
        // 获取当前内容
        const content = this.publishGuideForm.content;
        
        // 在光标位置插入图片
        this.publishGuideForm.content = content.substring(0, this.cursorPosition) + 
                                        imageTag + 
                                        content.substring(this.cursorPosition);
        
        // 更新光标位置
        this.cursorPosition += imageTag.length;
        
        // 让textarea重新获得焦点
        setTimeout(() => {
          const textarea = this.$refs.contentInput && this.$refs.contentInput.$el.querySelector('textarea');
          if (textarea) {
            textarea.focus();
            textarea.setSelectionRange(this.cursorPosition, this.cursorPosition);
          }
        }, 100);
        
        this.$message.success('图片插入成功！');
      },

      /**
       * 初始化敏感词过滤器
       * 在组件挂载时调用，加载敏感词库
       */
      async initializeSensitiveFilter() {
        try {
          console.log('开始初始化敏感词过滤器...');
          const success = await sensitiveWordFilter.initialize();
          this.sensitiveFilterInitialized = success;
          console.log(`敏感词过滤器初始化${success ? '成功' : '失败'}`);
          return success;
        } catch (error) {
          console.error('初始化敏感词过滤器时发生错误:', error);
          this.sensitiveFilterInitialized = false;
          return false;
        }
      },
      
      /**
       * 使用敏感词过滤器检测文本
       * @param {string} text - 待检测的文本
       * @returns {object} - 检测结果 {hasSensitive: boolean, sensitiveWords: array}
       */
      checkSensitiveWords(text) {
        console.log('调用敏感词检测函数，文本长度:', text ? text.length : 0);
        
        if (!text) {
          console.log('文本为空，无需检测');
          return { hasSensitive: false, sensitiveWords: [] };
        }
        
        try {
          // 使用敏感词过滤工具进行检测
          const checkResult = sensitiveWordFilter.checkSensitiveWords(text);
          console.log('敏感词检测结果:', checkResult);
          return checkResult;
        } catch (error) {
          console.error('敏感词检测出错:', error);
          // 出错时使用备用检测
          return this.fallbackSensitiveCheck(text);
        }
      },
      
      /**
       * 备用敏感词检测（当工具检测失败时使用）
       * 直接使用简单的字符串匹配，确保中文敏感词能被正确识别
       * @param {string} text - 待检测的文本
       * @returns {object} - 检测结果 {hasSensitive: boolean, sensitiveWords: array}
       */
      fallbackSensitiveCheck(text) {
        console.log('使用备用敏感词检测');
        // 常见中文敏感词列表
        const sensitiveWords = [
          '傻逼', '智障', '脑残', '废物', '垃圾', '白痴', 
          '色情', '黄色', '赌博', '毒品', '反动', '暴力',
          'fuck', 'shit', 'cunt', 'asshole', 'porn', 'gambling'
        ];
        const foundWords = [];
        
        for (const word of sensitiveWords) {
          if (text.includes(word)) {
            console.log('备用检测发现敏感词:', word);
            foundWords.push(word);
          }
        }
        
        return {
          hasSensitive: foundWords.length > 0,
          sensitiveWords: foundWords
        };
      },

      /**
       * 处理发布攻略
       * 在发布前进行敏感词检测，防止发布不良内容
       * 采用双重检测机制确保敏感内容不会被发布
       */
      async handlePublishGuide() {
        console.log('开始处理攻略发布');
        
        // 使用async/await重构validate逻辑
        try {
          // 首先验证表单
          await new Promise((resolve, reject) => {
            this.$refs.publishGuideForm.validate(valid => {
              if (valid) {
                resolve();
              } else {
                reject(new Error('表单验证失败'));
              }
            });
          });
          
          console.log('表单验证通过，开始敏感词检测流程');
          
          // 确保敏感词过滤器已初始化，如果未初始化则尝试初始化
          if (!this.sensitiveFilterInitialized) {
            console.log('敏感词过滤器未初始化，尝试初始化...');
            await this.initializeSensitiveFilter();
          }
          
          // 1. 进行敏感词检测 - 首先检测标题
          console.log('检测标题敏感词:', this.publishGuideForm.title);
          const titleCheck = this.checkSensitiveWords(this.publishGuideForm.title);
          if (titleCheck.hasSensitive) {
            const errorMsg = `攻略标题包含敏感词汇：${titleCheck.sensitiveWords.join('、')}，请修改后重试`;
            console.log(errorMsg);
            this.$message.error(errorMsg);
            return;
          }
          console.log('标题敏感词检测通过');
          
          // 2. 检测攻略内容
          console.log('检测内容敏感词');
          const contentCheck = this.checkSensitiveWords(this.publishGuideForm.content);
          if (contentCheck.hasSensitive) {
            const errorMsg = `攻略内容包含敏感词汇：${contentCheck.sensitiveWords.join('、')}，请修改后重试`;
            console.log(errorMsg);
            this.$message.error(errorMsg);
            return;
          }
          console.log('内容敏感词检测通过');
          
          // 3. 再次进行最终敏感词检查（双重保障）
          console.log('执行最终敏感词检查');
          const finalCheck = this.fallbackSensitiveCheck(
            this.publishGuideForm.title + ' ' + this.publishGuideForm.content
          );
          if (finalCheck.hasSensitive) {
            const errorMsg = `检测到敏感内容：${finalCheck.sensitiveWords.join('、')}，请修改后重试`;
            console.log(errorMsg);
            this.$message.error(errorMsg);
            return;
          }
          console.log('所有敏感词检测通过');
          
          console.log(`当前敏感词库包含 ${sensitiveWordFilter.getWordCount()} 个敏感词`);
            
            // 准备攻略数据
            const guideData = {
              title: this.publishGuideForm.title,
              content: this.publishGuideForm.content,
              gameId: this.gameId,
              // 添加封面图片URL（如果有）
              coverImageUrl: this.publishGuideForm.coverImageUrl || ''
            };

          console.log('准备发布攻略:', {data: guideData, username: localStorage.getItem('username'), cookie: document.cookie});
          
          // 发送请求发布攻略
          this.$axios.post('/api/guide/create', guideData, {
            withCredentials: true, // 显式设置，确保cookie传递
            headers: {
              'X-Requested-With': 'XMLHttpRequest' // 添加XHR标识
            }
          })
          .then(response => {
            console.log('发布攻略成功:', response);
            this.$message.success('攻略发布成功');
            this.publishGuideDialogVisible = false;
            // 重新加载攻略列表
            this.fetchGameGuides();
          })
          .catch(error => {
            console.error('发布攻略失败:', {
              status: error.response?.status,
              data: error.response?.data,
              message: error.message,
              config: error.config
            });
            
            // 显示更详细的错误信息，帮助排查问题
            const errorMsg = error.response?.data ? 
              typeof error.response.data === 'string' ? error.response.data : (error.response.data.message || '发布攻略失败') : 
              '发布攻略失败，请稍后重试';
            this.$message.error(errorMsg);
            
            // 如果是认证失败，引导用户重新登录
            if (error.response?.status === 401) {
              console.warn('认证失败，清除本地登录状态');
              // 清除本地登录状态
              localStorage.removeItem('username');
              localStorage.removeItem('isAdmin');
              
              // 延迟跳转，让用户看到错误信息
              setTimeout(() => {
                this.$router.push('/login');
              }, 1500);
            } else if (error.request) {
              // 请求已发送但未收到响应
              this.$message.error('服务器无响应，请检查网络连接');
            }
          });
        } catch (error) {
          // 捕获表单验证失败或其他异常
          console.error('发布攻略过程中发生错误:', error);
          if (error.message !== '表单验证失败') {
            this.$message.error('操作失败，请稍后重试');
          }
        }
    },

    // 获取图片URL
    getImageUrl(url) {
      if (!url) {
        return '/static/images/default-poster.jpg';
      }
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

    // 格式化日期
    formatDate(dateString) {
      if (!dateString) return '';
      const date = new Date(dateString);
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      return `${year}-${month}-${day}`;
    },

    // 获取攻略摘要
    getPostSummary(content) {
      if (!content) return '';
      // 返回前150个字符
      return content.length > 150 ? content.substring(0, 150) + '...' : content;
    },
    
    // 跳转到攻略详情页面
    navigateToGuideDetail(guideId) {
      this.$router.push(`/guide/${guideId}`);
    }
  }
}
</script>

<style scoped>
.game-zone-container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

/* 图片上传样式 */
.upload-preview {
  margin-top: 10px;
  display: flex;
  align-items: center;
}

.preview-image {
  width: 200px;
  height: 120px;
  object-fit: cover;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
}

.remove-btn {
  margin-left: 10px;
  color: #f56c6c;
}

.remove-btn:hover {
  color: #f78989;
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

.game-title {
  margin: 0 0 10px 0;
  font-size: 42px;
  font-weight: bold;
}

.game-tags {
  margin-top: 10px;
}

.game-tags .el-tag {
  background-color: rgba(255,255,255,0.2);
  border: 1px solid rgba(255,255,255,0.3);
  color: white;
  margin-right: 10px;
}

.game-description {
  background-color: white;
  padding: 30px 60px;
  border-bottom: 1px solid #e6e6e6;
}

.game-description p {
  margin: 0;
  line-height: 1.8;
  color: #333;
  font-size: 16px;
}

/* 内容区域 */
.content-section {
  display: flex;
  max-width: 1400px;
  margin: 0 auto;
  padding: 40px 20px;
}

/* 侧边导航 */
.sidebar {
  flex: 0 0 240px;
  margin-right: 40px;
}

.sidebar .el-menu {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
}

.sidebar .el-menu-item {
  height: 60px;
  line-height: 60px;
  font-size: 16px;
}

.sidebar .el-menu-item.is-active {
  color: #1890ff;
  background-color: #e6f7ff;
}

/* 主要内容 */
.main-content {
  flex: 1;
  min-width: 0;
}

/* 攻略区域 */
.guides-section {
  background-color: white;
  border-radius: 8px;
  padding: 30px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.section-header h2 {
  margin: 0;
  font-size: 24px;
  color: #333;
}

.filter-bar {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 10px;
}

.search-input {
  width: 250px;
  margin-right: 10px;
}

.filter-select {
  width: 150px;
  margin-right: 10px;
}

.search-weight {
  background-color: #e6f7ff;
  color: #1890ff;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  margin-left: 10px;
}

/* 攻略列表 */
.guides-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.guide-item {
  display: flex;
  padding: 20px 0;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: all 0.3s;
}

.guide-item:hover {
  background-color: #fafafa;
  transform: translateX(5px);
}

.guide-item:last-child {
  border-bottom: none;
}

.guide-cover {
  flex: 0 0 160px;
  height: 100px;
  margin-right: 20px;
  overflow: hidden;
  border-radius: 8px;
}

.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.guide-content {
  flex: 1;
  min-width: 0;
}

.guide-title {
  margin: 0 0 10px 0;
  font-size: 18px;
  color: #333;
  font-weight: bold;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.guide-meta {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 10px;
  font-size: 14px;
  color: #999;
}

.author {
  color: #666;
}

.stats {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-left: auto;
}

.stats i {
  margin-right: 5px;
}

.guide-summary {
  margin: 0;
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

/* 分页 */
.pagination {
  margin-top: 30px;
  text-align: center;
}

/* 空状态 */
.empty-state {
  padding: 60px 0;
  text-align: center;
}

/* 问答区域样式 */
.questions-section {
  background-color: white;
  border-radius: 8px;
  padding: 30px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
  margin-top: 0;
}



/* 响应式设计 */
@media (max-width: 1200px) {
  .game-overlay {
    padding: 30px 40px;
  }
  
  .game-logo {
    width: 100px;
    height: 100px;
    margin-right: 20px;
  }
  
  .game-title {
    font-size: 32px;
  }
  
  .game-description {
    padding: 20px 40px;
  }
}

@media (max-width: 768px) {
  .content-section {
    flex-direction: column;
    padding: 20px;
  }
  
  .sidebar {
    flex: none;
    margin-right: 0;
    margin-bottom: 20px;
  }
  
  .game-banner {
    height: 250px;
  }
  
  .game-overlay {
    padding: 20px;
  }
  
  .game-logo {
    width: 80px;
    height: 80px;
  }
  
  .game-title {
    font-size: 24px;
  }
  
  .game-description {
    padding: 20px;
  }
  
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  
  .filter-bar {
    width: 100%;
    justify-content: space-between;
  }
  
  .guide-item {
    flex-direction: column;
  }
  
  .guide-cover {
    flex: none;
    width: 100%;
    height: 200px;
    margin-right: 0;
    margin-bottom: 15px;
  }
}
</style>