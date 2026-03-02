
<!--    
游戏管理功能界面    

-->
<template>
  <div class="game-management-container">
    <el-card title="游戏管理" shadow="hover">
      <!-- 操作按钮区域 -->
      <div class="action-bar">
        <el-button type="primary" @click="showAddGameDialog">
          <i class="el-icon-plus"></i> 添加游戏
        </el-button>
        <el-input
          v-model="searchKeyword"
          placeholder="搜索游戏名称"
          clearable
          class="search-input"
          @keyup.enter.native="searchGames"
        >
          <i slot="prefix" class="el-icon-search"></i>
        </el-input>
      </div>

      <!-- 游戏列表 -->
      <el-table
        v-loading="loading"
        :data="gamesData"
        style="width: 100%"
        border
      >
        <el-table-column prop="id" label="ID" width="80" fixed></el-table-column>
        <el-table-column prop="name" label="游戏名称" width="180" fixed></el-table-column>
        <el-table-column prop="description" label="描述" min-width="200"></el-table-column>
        <el-table-column prop="categories" label="分类" width="150"></el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template slot-scope="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="isFeatured" label="是否推荐" width="100">
          <template slot-scope="scope">
            <el-switch
              v-model="scope.row.isFeatured"
              active-color="#13ce66"
              inactive-color="#ff4949"
              @change="handleFeaturedChange(scope.row)"
            ></el-switch>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template slot-scope="scope">
            <el-button type="primary" size="small" @click="showEditGameDialog(scope.row)">
              编辑
            </el-button>
            <el-button type="danger" size="small" @click="handleDeleteGame(scope.row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>

      <!-- 添加游戏对话框 -->
      <el-dialog
        title="添加游戏"
        :visible.sync="addGameDialogVisible"
        width="60%"
        :before-close="handleAddDialogClose"
      >
        <el-form ref="addGameForm" :model="addGameForm" :rules="gameFormRules" label-width="100px">
          <el-form-item label="游戏名称" prop="name">
            <el-input v-model="addGameForm.name" placeholder="请输入游戏名称"></el-input>
          </el-form-item>
          <el-form-item label="游戏描述" prop="description">
            <el-input v-model="addGameForm.description" type="textarea" placeholder="请输入游戏描述"></el-input>
          </el-form-item>
          <el-form-item label="游戏简介" prop="introduction">
            <el-input v-model="addGameForm.introduction" type="textarea" :rows="3" placeholder="请输入游戏简介"></el-input>
          </el-form-item>
          <el-form-item label="详细信息" prop="detailedInfo">
            <el-input v-model="addGameForm.detailedInfo" type="textarea" :rows="5" placeholder="请输入游戏的详细信息，如游戏特色、玩法介绍等"></el-input>
          </el-form-item>
          <el-form-item label="Logo URL" prop="logoUrl">
            <el-input v-model="addGameForm.logoUrl" placeholder="请输入游戏Logo URL" style="width: 70%; display: inline-block;"></el-input>
            <el-upload
              class="upload-demo"
              action="/api/upload"
              :show-file-list="false"
              :on-success="handleLogoUploadSuccess.bind(this, 'add')"
              :on-error="handleUploadError"
              :before-upload="beforeUpload"
              :with-credentials="true"
              :limit="1"
              :file-list="[]"
              :accept="'image/*'"
              style="display: inline-block; margin-left: 10px;">
              <el-button type="primary">上传图片</el-button>
            </el-upload>
          </el-form-item>
          <el-form-item label="海报URL" prop="posterUrl">
            <el-input v-model="addGameForm.posterUrl" placeholder="请输入游戏海报URL" style="width: 70%; display: inline-block;"></el-input>
            <el-upload
              class="upload-demo"
              action="/api/upload"
              :show-file-list="false"
              :on-success="handlePosterUploadSuccess.bind(this, 'add')"
              :on-error="handleUploadError"
              :before-upload="beforeUpload"
              :with-credentials="true"
              :limit="1"
              :file-list="[]"
              :accept="'image/*'"
              style="display: inline-block; margin-left: 10px;">
              <el-button type="primary">上传图片</el-button>
            </el-upload>
          </el-form-item>
          <el-form-item label="游戏分类" prop="categories">
            <el-input v-model="addGameForm.categories" placeholder="请输入游戏分类，用逗号分隔"></el-input>
          </el-form-item>
          <el-form-item label="是否推荐">
            <el-switch v-model="addGameForm.isFeatured" active-color="#13ce66" inactive-color="#ff4949"></el-switch>
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button @click="handleAddDialogClose">取消</el-button>
          <el-button type="primary" @click="handleAddGame">确定</el-button>
        </div>
      </el-dialog>

      <!-- 编辑游戏对话框 -->
      <el-dialog
        title="编辑游戏"
        :visible.sync="editGameDialogVisible"
        width="60%"
        :before-close="handleEditDialogClose"
      >
        <el-form ref="editGameForm" :model="editGameForm" :rules="gameFormRules" label-width="100px">
          <el-form-item label="游戏ID" prop="id">
            <el-input v-model="editGameForm.id" disabled></el-input>
          </el-form-item>
          <el-form-item label="游戏名称" prop="name">
            <el-input v-model="editGameForm.name" placeholder="请输入游戏名称"></el-input>
          </el-form-item>
          <el-form-item label="游戏描述" prop="description">
            <el-input v-model="editGameForm.description" type="textarea" placeholder="请输入游戏描述"></el-input>
          </el-form-item>
          <el-form-item label="游戏简介" prop="introduction">
            <el-input v-model="editGameForm.introduction" type="textarea" :rows="3" placeholder="请输入游戏简介"></el-input>
          </el-form-item>
          <el-form-item label="详细信息" prop="detailedInfo">
            <el-input v-model="editGameForm.detailedInfo" type="textarea" :rows="5" placeholder="请输入游戏的详细信息，如游戏特色、玩法介绍等"></el-input>
          </el-form-item>
          <el-form-item label="Logo URL" prop="logoUrl">
            <el-input v-model="editGameForm.logoUrl" placeholder="请输入游戏Logo URL" style="width: 70%; display: inline-block;"></el-input>
            <el-upload
              class="upload-demo"
              action="/api/upload"
              :show-file-list="false"
              :on-success="handleLogoUploadSuccess.bind(this, 'edit')"
              :on-error="handleUploadError"
              :before-upload="beforeUpload"
              :with-credentials="true"
              :limit="1"
              :file-list="[]"
              :accept="'image/*'"
              style="display: inline-block; margin-left: 10px;">
              <el-button type="primary">上传图片</el-button>
            </el-upload>
          </el-form-item>
          <el-form-item label="海报URL" prop="posterUrl">
            <el-input v-model="editGameForm.posterUrl" placeholder="请输入游戏海报URL" style="width: 70%; display: inline-block;"></el-input>
            <el-upload
              class="upload-demo"
              action="/api/upload"
              :show-file-list="false"
              :on-success="handlePosterUploadSuccess.bind(this, 'edit')"
              :on-error="handleUploadError"
              :before-upload="beforeUpload"
              :with-credentials="true"
              :limit="1"
              :file-list="[]"
              :accept="'image/*'"
              style="display: inline-block; margin-left: 10px;">
              <el-button type="primary">上传图片</el-button>
            </el-upload>
          </el-form-item>
          <el-form-item label="游戏分类" prop="categories">
            <el-input v-model="editGameForm.categories" placeholder="请输入游戏分类，用逗号分隔"></el-input>
          </el-form-item>
          <el-form-item label="是否推荐">
            <el-switch v-model="editGameForm.isFeatured" active-color="#13ce66" inactive-color="#ff4949"></el-switch>
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button @click="handleEditDialogClose">取消</el-button>
          <el-button type="primary" @click="handleEditGame">确定</el-button>
        </div>
      </el-dialog>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'GameManagement',
  data() {
    return {
      loading: false,
      gamesData: [],
      total: 0,
      currentPage: 1,
      pageSize: 10,
      searchKeyword: '',
      
      // 添加游戏对话框
      addGameDialogVisible: false,
      addGameForm: {
        name: '',
        description: '',
        introduction: '',
        detailedInfo: '',
        logoUrl: '',
        posterUrl: '',
        categories: '',
        isFeatured: false
      },
      
      // 编辑游戏对话框
      editGameDialogVisible: false,
      editGameForm: {
        id: null,
        name: '',
        description: '',
        introduction: '',
        detailedInfo: '',
        logoUrl: '',
        posterUrl: '',
        categories: '',
        isFeatured: false
      },
      
      // 表单验证规则
      gameFormRules: {
        name: [
          { required: true, message: '请输入游戏名称', trigger: 'blur' },
          { min: 2, max: 100, message: '游戏名称长度在 2 到 100 个字符', trigger: 'blur' }
        ],
        description: [
          { required: true, message: '请输入游戏描述', trigger: 'blur' }
        ],
        logoUrl: [
          { required: true, message: '请输入游戏Logo URL', trigger: 'blur' }
        ],
        posterUrl: [
          { required: true, message: '请输入游戏海报URL', trigger: 'blur' }
        ],
        categories: [
          { required: true, message: '请输入游戏分类', trigger: 'blur' }
        ]
      }
    }
  },
  
  mounted() {
    this.loadGames();
  },
  
  // 路由守卫：检查用户是否登录且是管理员
  beforeRouteEnter(to, from, next) {
    // 检查localStorage中是否有username
    const username = localStorage.getItem('username');
    
    if (username) {
      // 这里需要检查用户是否为管理员
      const isAdmin = localStorage.getItem('isAdmin') === 'true';
      
      if (isAdmin) {
        console.log('用户是管理员，允许进入游戏管理页面');
        next();
      } else {
        console.log('用户不是管理员，拒绝访问游戏管理页面');
        next('/home');
      }
    } else {
      console.log('用户未登录，重定向到登录页');
      next('/login');
    }
  },
  
  methods: {
    // 处理海报上传成功
    handlePosterUploadSuccess(type, res) {
      // 检查响应格式，确保获取正确的文件路径
      let filePath = res;
      if (typeof res === 'string') {
        filePath = res;
      } else if (res && res.data) {
        filePath = res.data;
      }
      
      // 根据类型更新对应的表单
      if (type === 'add') {
        this.addGameForm.posterUrl = filePath;
      } else if (type === 'edit') {
        this.editGameForm.posterUrl = filePath;
      }
      
      this.$message.success('图片上传成功！');
    },
    
    // 处理Logo上传成功
    handleLogoUploadSuccess(type, res) {
      // 检查响应格式，确保获取正确的文件路径
      let filePath = res;
      if (typeof res === 'string') {
        filePath = res;
      } else if (res && res.data) {
        filePath = res.data;
      }
      
      // 根据类型更新对应的表单
      if (type === 'add') {
        this.addGameForm.logoUrl = filePath;
      } else if (type === 'edit') {
        this.editGameForm.logoUrl = filePath;
      }
      
      this.$message.success('Logo上传成功！');
    },
    
    // 处理上传错误
    handleUploadError(err) {
      console.error('图片上传失败:', err);
      this.$message.error('图片上传失败');
    },
    
    // 文件上传前的检查
    beforeUpload(file) {
      const isLt10M = file.size / 1024 / 1024 < 10;
      if (!isLt10M) {
        this.$message.error('上传图片大小不能超过10MB!');
        return false;
      }
      
      const isImage = /^image\//.test(file.type);
      if (!isImage) {
        this.$message.error('请上传图片文件!');
        return false;
      }
      
      return true;
    },
    
    // 加载游戏列表
    loadGames() {
      this.loading = true;
      this.$axios.get('/api/game/admin/list', {
        withCredentials: true
      })
      .then(response => {
        this.loading = false;
        this.gamesData = response.data || [];
        this.total = this.gamesData.length;
        
        // 处理搜索关键词
        if (this.searchKeyword) {
          this.searchGames();
        }
      })
      .catch(error => {
        this.loading = false;
        console.error('获取游戏列表失败:', error);
        this.$message.error('获取游戏列表失败，请稍后重试');
        
        // 如果API调用失败，使用模拟数据
        this.gamesData = [
          {
            id: 1,
            name: '三角洲行动',
            description: '新一代战术射击品质标杆',
            logoUrl: '/dist/images/sanjiaozhou/san_jiao_zhou_logo.jpg',
            posterUrl: '/dist/images/sanjiaozhou/san_jiao_zhou_logo.jpg', // 使用logo作为临时海报
            categories: '射击,战术,多人',
            isFeatured: true,
            createTime: new Date().toISOString()
          },
          {
            id: 2,
            name: '部落冲突',
            description: '一款经典的策略战争游戏',
            logoUrl: '/dist/images/buluochongtu/buluochongtu-image.svg',
            posterUrl: '/dist/images/buluochongtu/buluochongtu-image.svg',
            categories: '策略,战争',
            isFeatured: false,
            createTime: new Date().toISOString()
          }
        ];
        
        // 更新数据库中的实际游戏记录图片路径
        // 注意：这里只是临时解决方案，实际应该通过编辑游戏功能来更新
        this.total = this.gamesData.length;
      });
    },
    
    /**
     * 简单的汉字到拼音首字母转换函数
     * 使用内置方法而非庞大的映射表
     * 这个函数会将每个汉字转换为其拼音首字母
     * 
     * @param {string} text - 需要转换的文本
     * @returns {string} - 转换后的拼音首字母字符串
     */
    getPinyin(text) {
      // 输入参数验证
      if (!text || typeof text !== 'string') {
        return '';
      }
      
      // 使用String.prototype.charCodeAt方法来判断汉字并转换为拼音首字母
      // 基于Unicode编码范围判断汉字
      let pinyin = '';
      
      for (let i = 0; i < text.length; i++) {
        const char = text.charAt(i);
        const code = char.charCodeAt(0);
        
        // 非汉字字符直接转为小写添加到结果
        if (code < 0x4e00 || code > 0x9fa5) {
          pinyin += char.toLowerCase();
          continue;
        }
        
        // 使用Unicode编码范围映射到拼音首字母
        // 这是一个简化的映射，基于汉字的Unicode编码范围
        // 实际应用中，更复杂的拼音映射可能需要专门的库
        if (code >= 0x4e00 && code <= 0x4e6e) pinyin += 'a';
        else if (code <= 0x4f69) pinyin += 'b';
        else if (code <= 0x5170) pinyin += 'c';
        else if (code <= 0x5341) pinyin += 'd';
        else if (code <= 0x5514) pinyin += 'e';
        else if (code <= 0x5706) pinyin += 'f';
        else if (code <= 0x58eb) pinyin += 'g';
        else if (code <= 0x5ab0) pinyin += 'h';
        else if (code <= 0x5d0e) pinyin += 'j'; // 注意：没有i开头的拼音
        else if (code <= 0x5e72) pinyin += 'k';
        else if (code <= 0x5f90) pinyin += 'l';
        else if (code <= 0x627e) pinyin += 'm';
        else if (code <= 0x6587) pinyin += 'n';
        else if (code <= 0x672a) pinyin += 'o';
        else if (code <= 0x6b66) pinyin += 'p';
        else if (code <= 0x6e38) pinyin += 'q';
        else if (code <= 0x7117) pinyin += 'r';
        else if (code <= 0x738b) pinyin += 's';
        else if (code <= 0x77ed) pinyin += 't';
        else if (code <= 0x7a92) pinyin += 'w';
        else if (code <= 0x7eaa) pinyin += 'x';
        else if (code <= 0x843d) pinyin += 'y';
        else if (code <= 0x9fa5) pinyin += 'z';
        else pinyin += char.toLowerCase();
      }
      
      return pinyin;
    },
    
    /**
     * 搜索游戏
     * 优先调用搜索API，当用户输入字母时检索数据库中的game表name字段
     * 如果API调用失败，降级使用本地过滤搜索
     */
    searchGames() {
      if (!this.searchKeyword) {
        this.loadGames();
        return;
      }
      
   
      
      // 准备搜索参数
      const searchParams = {
        keyword: this.searchKeyword.trim()
      };
      
      // 优先调用搜索API
      this.$axios.get('/api/game/admin/search', {
        params: searchParams,
        withCredentials: true
      })
      .then(response => {
       
        this.gamesData = response.data || [];
        this.total = this.gamesData.length;
        this.currentPage = 1;
      })
      .catch(error => {
      
        
        // 降级使用本地过滤
        const keyword = this.searchKeyword.toLowerCase();
        
        // 如果当前已经有游戏数据，直接在本地过滤
        if (this.gamesData && this.gamesData.length > 0) {
          this.filterGamesLocally(keyword);
        } else {
          // 如果没有游戏数据，先加载所有游戏再过滤
          this.loadGamesWithLocalFilter(keyword);
        }
      });
    },
    
    /**
     * 本地过滤游戏数据
     * 在已有游戏数据的基础上进行本地搜索过滤
     * @param {string} keyword - 搜索关键字（已转为小写）
     */
    filterGamesLocally(keyword) {
      // 保存原始的游戏列表以便后续使用
      if (!this.originalGamesData) {
        this.originalGamesData = [...this.gamesData];
      }
      
      // 使用本地过滤实现搜索
      this.gamesData = this.originalGamesData.filter(game => {
        const gameNameLower = game.name.toLowerCase();
        const gameNamePinyin = this.getPinyin(game.name);
        
        // 匹配条件：直接匹配游戏名称或拼音匹配
        return gameNameLower.includes(keyword) || gameNamePinyin.includes(keyword);
      });
      
      this.total = this.gamesData.length;
      this.currentPage = 1;
    },
    
    /**
     * 加载所有游戏并进行本地过滤
     * 当没有现有游戏数据时使用此方法
     * @param {string} keyword - 搜索关键字（已转为小写）
     */
    loadGamesWithLocalFilter(keyword) {
      this.loading = true;
      
      this.$axios.get('/api/game/admin/list', {
        withCredentials: true
      })
      .then(response => {
        this.loading = false;
        const allGames = response.data || [];
        
        // 保存原始数据
        this.originalGamesData = allGames;
        
        // 应用本地过滤
        this.gamesData = allGames.filter(game => {
          const gameNameLower = game.name.toLowerCase();
          const gameNamePinyin = this.getPinyin(game.name);
          
          // 匹配条件：直接匹配游戏名称或拼音匹配
          return gameNameLower.includes(keyword) || gameNamePinyin.includes(keyword);
        });
        
        this.total = this.gamesData.length;
        this.currentPage = 1;
      })
      .catch(error => {
        this.loading = false;
    
        this.$message.error('搜索失败，请稍后重试');
        
        // 清空搜索结果
        this.gamesData = [];
        this.total = 0;
      });
    },
    
    // 分页大小变化
    handleSizeChange(size) {
      this.pageSize = size;
      this.currentPage = 1;
    },
    
    // 当前页码变化
    handleCurrentChange(current) {
      this.currentPage = current;
    },
    
    // 显示添加游戏对话框
    showAddGameDialog() {
      this.addGameForm = {
        name: '',
        description: '',
        logoUrl: '',
        posterUrl: '',
        categories: '',
        isFeatured: false
      };
      this.addGameDialogVisible = true;
    },
    
    // 处理添加对话框关闭
    handleAddDialogClose() {
      this.$refs.addGameForm && this.$refs.addGameForm.resetFields();
      this.addGameDialogVisible = false;
    },
    
    // 处理添加游戏
    handleAddGame() {
      this.$refs.addGameForm.validate(valid => {
        if (valid) {
          this.$axios.post('/api/game/admin/add', this.addGameForm, {
            withCredentials: true
          })
          .then(response => {
            this.$message.success('添加游戏成功');
            this.addGameDialogVisible = false;
            this.loadGames(); // 重新加载游戏列表
          })
          .catch(error => {
       
            this.$message.error(error.response?.data?.message || '添加游戏失败，请稍后重试');
          });
        }
      });
    },
    
    // 显示编辑游戏对话框
    showEditGameDialog(game) {
      this.editGameForm = { ...game };
      this.editGameDialogVisible = true;
    },
    
    // 处理编辑对话框关闭
    handleEditDialogClose() {
      this.$refs.editGameForm && this.$refs.editGameForm.resetFields();
      this.editGameDialogVisible = false;
    },
    
    // 处理编辑游戏
    handleEditGame() {
      // 在发送请求前检查登录状态
      const username = localStorage.getItem('username');
      const isAdmin = localStorage.getItem('isAdmin') === 'true';
      
      if (!username) {
        this.$message.error('请先登录');
        this.$router.push('/login');
        return;
      }
      
      if (!isAdmin) {
        this.$message.error('您没有管理员权限执行此操作');
        return;
      }
      
      this.$refs.editGameForm.validate(valid => {
        if (valid) {
          // 确保id是数字类型
          const gameToUpdate = {
            ...this.editGameForm,
            id: Number(this.editGameForm.id) || null
          };
          
          
          
          this.$axios.put('/api/game/admin/update', gameToUpdate, {
            withCredentials: true,
            headers: {
              'Content-Type': 'application/json'
            }
          })
          .then(response => {
       
            this.$message.success('更新游戏成功');
            this.editGameDialogVisible = false;
            this.loadGames(); // 重新加载游戏列表
          })
          .catch(error => {
           
            console.error('错误详细信息:', {
              response: error.response,
              status: error.response?.status,
              data: error.response?.data,
              config: error.config
            });
            
            // 根据错误状态码提供更具体的错误信息
            if (error.response?.status === 401) {
              this.$message.error('会话已过期，请重新登录');
              localStorage.removeItem('username');
              localStorage.removeItem('isAdmin');
              this.$router.push('/login');
            } else if (error.response?.status === 403) {
              this.$message.error('您没有权限执行此操作，请确认您是以管理员身份登录');
            } else {
              this.$message.error(error.response?.data?.message || '更新游戏失败，请稍后重试');
            }
          });
        }
      });
    },
    
    // 处理删除游戏
    handleDeleteGame(game) {
      this.$confirm(`确定要删除游戏"${game.name}"吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$axios.delete(`/api/game/admin/delete/${game.id}`, {
          withCredentials: true
        })
        .then(response => {
          this.$message.success('删除游戏成功');
          this.loadGames(); // 重新加载游戏列表
        })
        .catch(error => {
       
          this.$message.error(error.response?.data?.message || '删除游戏失败，请稍后重试');
        });
      }).catch(() => {
        this.$message.info('已取消删除');
      });
    },
    
    // 处理推荐状态变化
    handleFeaturedChange(game) {
      const isFeatured = game.isFeatured;
      
      this.$axios.patch(`/api/game/admin/featured/${game.id}`, { isFeatured }, {
        withCredentials: true
      })
      .then(response => {
        this.$message.success(`设置游戏"${game.name}"${isFeatured ? '推荐' : '取消推荐'}成功`);
      })
      .catch(error => {
        console.error('设置推荐状态失败:', error);
        // 恢复原来的状态
        game.isFeatured = !isFeatured;
        this.$message.error(error.response?.data?.message || '设置推荐状态失败，请稍后重试');
      });
    },
    
    // 格式化日期
    formatDate(dateString) {
      if (!dateString) return '';
      const date = new Date(dateString);
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
    }
  }
}
</script>

<style scoped>
.game-management-container {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.search-input {
  width: 300px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}

.el-input__inner {
  width: 100%;
}

.el-form-item {
  margin-bottom: 15px;
}
</style>