<template>
  <div class="admin-game-management">
    <div class="admin-header">
      <div class="header-content">
          <div class="header-left">
            <el-button type="primary" size="small" @click="handleBack" class="back-button">
              <i class="el-icon-arrow-left"></i>
              返回控制台
            </el-button>
            <h1>游戏管理</h1>
          </div>
          <el-button type="primary" @click="handleAddGame">
            <i class="el-icon-plus"></i>
            添加游戏
          </el-button>
        </div>
    </div>
    
    <div class="admin-content">
      <el-card shadow="hover">
        <template #header>
          <div class="table-header">
            <span>游戏列表</span>
            <div class="search-area">
              <el-input
                v-model="searchKeyword"
                placeholder="搜索游戏名称"
                style="width: 200px; margin-right: 10px;"
                clearable
                @clear="handleSearch"
                @keyup.enter="handleSearch"
              >
                <template #prefix>
                  <i class="el-icon-search"></i>
                </template>
              </el-input>
              <el-button type="primary" @click="handleSearch">搜索</el-button>
            </div>
          </div>
        </template>
        
        <el-table
          :data="filteredGames"
          v-loading="loading"
          style="width: 100%"
          stripe
        >
          <el-table-column prop="id" label="ID" width="80" align="center"></el-table-column>
          <el-table-column prop="name" label="游戏名称" min-width="150">
            <template #default="{ row }">
              <div class="game-info">
                <el-image
                  v-if="row.coverImage"
                  :src="row.coverImage"
                  :preview-src-list="[row.coverImage]"
                  style="width: 40px; height: 40px; border-radius: 4px; margin-right: 10px;"
                  fit="cover"
                ></el-image>
                <span>{{ row.name }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="category" label="分类" width="120" align="center"></el-table-column>
          <el-table-column prop="status" label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === 'active' ? 'success' : 'danger'">
                {{ row.status === 'active' ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" width="180" align="center">
            <template #default="{ row }">
              {{ formatDate(row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column prop="updatedAt" label="更新时间" width="180" align="center">
            <template #default="{ row }">
              {{ formatDate(row.updatedAt) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" align="center" fixed="right">
            <template #default="{ row }">
              <el-button
                size="small"
                type="primary"
                @click="handleEditGame(row)"
              >
                编辑
              </el-button>
              <el-button
                size="small"
                :type="row.status === 'active' ? 'warning' : 'success'"
                @click="handleToggleStatus(row)"
              >
                {{ row.status === 'active' ? '禁用' : '启用' }}
              </el-button>
              <el-button
                size="small"
                type="danger"
                @click="handleDeleteGame(row)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          ></el-pagination>
        </div>
      </el-card>
    </div>
    
    <!-- 添加/编辑游戏对话框 -->
    <el-dialog
      :title="dialogTitle"
      :visible="dialogVisible"
      width="500px"
      :before-close="handleDialogClose"
      append-to-body
      @close="handleDialogClose"
    >
      <el-form
        ref="gameForm"
        :model="gameForm"
        :rules="gameRules"
        label-width="80px"
      >
        <el-form-item label="游戏名称" prop="name">
          <el-input v-model="gameForm.name" placeholder="请输入游戏名称"></el-input>
        </el-form-item>
        
        <el-form-item label="分类" prop="categories">
          <el-select v-model="gameForm.categories" placeholder="请选择分类" style="width: 100%">
            <el-option label="角色扮演" value="角色扮演"></el-option>
            <el-option label="FPS" value="FPS"></el-option>
            <el-option label="中世纪" value="中世纪"></el-option>
            <el-option label="战舰" value="战舰"></el-option>
            <el-option label="搜打撤" value="搜打撤"></el-option>
            <el-option label="开放世界" value="开放世界"></el-option>
            <el-option label="动作冒险" value="动作冒险"></el-option>
            <el-option label="策略游戏" value="策略游戏"></el-option>
            <el-option label="射击游戏" value="射击游戏"></el-option>
            <el-option label="体育竞技" value="体育竞技"></el-option>
            <el-option label="其他" value="其他"></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="封面图片" prop="logoUrl">
          <el-upload
            class="avatar-uploader"
            action="/api/upload"
            :show-file-list="false"
            :on-success="handleUploadSuccess"
            :before-upload="beforeUpload"
          >
            <img v-if="gameForm.logoUrl" :src="gameForm.logoUrl" class="avatar">
            <i v-else class="el-icon-plus avatar-uploader-icon"></i>
          </el-upload>
        </el-form-item>

      <el-form-item label = "游戏描述" prop = "description"> 
      <el-input v-model = "gameForm.description" placeholder = "请输入游戏描述" type = "textarea" rows = "3"> </el-input>
      </el-form-item>

      <el-form-item label = "游戏介绍" prop = "introducetion">
        <el-input v-model="gameForm.introduction" placeholder="请输入游戏介绍" type = "textarea" row = "4"></el-input>
      </el-form-item>

      <el-form-item label = "详细信息" prop = "detailedInfo">
        <el-input v-model="gameForm.detailedInfo" placeholder = "请输入详细信息" type = "textarea" rows = "5"></el-input>
      </el-form-item>

      <el-form-item label = "海报图片上传" prop = "posterUrl">
        <el-upload class = "avatar-uploader" action = "/api/upload":show-file-list = "false":on-success = "handlePosterUploadSuccess":before-upload = "beforeUpload"><img v-if="gameForm.posterUrl" :src="gameForm.posterUrl" class="avatar">
    <i v-else class="el-icon-plus avatar-uploader-icon"></i></el-upload>
      </el-form-item>

      <el-form-item label="是否推荐">
  <el-switch v-model="gameForm.isFeatured"></el-switch>
</el-form-item>

      </el-form>
      
      <template #footer>
        <el-button @click="handleDialogClose">取消</el-button>
        <el-button type="primary" @click="handleSubmitGame" :loading="submitting">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'AdminGameManagement',
  data() {
    return {
      games: [],
      loading: false,
      searchKeyword: '',
      currentPage: 1,
      pageSize: 10,
      total: 0,
      
      dialogVisible: false,
      dialogTitle: '添加游戏',
      submitting: false,
      currentGameId: null,
      
      gameForm: {
        name: '',
        categories: '',
        description:'',
        introducetion:'',
        detailedInfo:'',
        logoUrl: '',
        posterUrl:'',
        isFeatured:false
      },
      
      gameRules: {
        name: [
          { required: true, message: '请输入游戏名称', trigger: 'blur' }
        ],
        category: [
          { required: true, message: '请选择游戏分类', trigger: 'change' }
        ]
      }
    }
  },
  computed: {
    filteredGames() {
      if (!this.searchKeyword) {
        return this.games
      }
      return this.games.filter(game => 
        game.name.toLowerCase().includes(this.searchKeyword.toLowerCase())
      )
    }
  },
  mounted() {
    this.loadGames()
  },
  methods: {
    loadGames() {
      this.loading = true
      this.$axios.get('/api/game/admin/list')
        .then(response => {
          // 处理后端返回的Result对象格式
          if (response.data && response.data.success) {
            this.games = response.data.data || []
            this.total = this.games.length
          } else {
            this.games = []
            this.total = 0
          }
        })
        .catch(error => {
          console.error('加载游戏列表失败:', error)
          this.$message.error('加载游戏列表失败')
          this.games = []
          this.total = 0
        })
        .finally(() => {
          this.loading = false
        })
    },
    
    handleSearch() {
      this.currentPage = 1
      this.loadGames()
    },
    
    handleSizeChange(size) {
      this.pageSize = size
      this.loadGames()
    },
    
    handleCurrentChange(page) {
      this.currentPage = page
      this.loadGames()
    },
    
    handleAddGame() {
      this.dialogTitle = '添加游戏'
      this.currentGameId = null
      this.resetForm()
      this.dialogVisible = true
    },
    
    handleEditGame(game) {
      console.log('编辑游戏被调用:', game)
      this.dialogTitle = '编辑游戏'
      this.currentGameId = game.id
      // 确保所有字段都有值
      this.gameForm = {
        name: game.name || '',//‘‘用来显示空值，确保表单字段始终有值
        categories: game.categories || '',
        description:game.description || '',
        introduction: game.introduction || '',
         detailedInfo: game.detailedInfo || '',
        logoUrl: game.logoUrl || '',
        posterUrl: game.posterUrl || '',
        isFeatured: game.isFeatured || false
      }
      console.log('设置的游戏表单数据:', this.gameForm)
      console.log('设置 dialogVisible 为 true')
      this.dialogVisible = true
      console.log('当前 dialogVisible 值:', this.dialogVisible)
    },
    handlePosterUploadSuccess(response)
    {
      if( typeof response === 'string')
    {
      this.gameForm.posterUrl = response
      this.$message.success('上传成功')} else if(response && response.success){
        this.gameForm.posterUrl = response.data.url
        this.$message.success('上传成功')
      }
    },
    
    handleToggleStatus(game) {
      const newStatus = game.status === 'active' ? 'inactive' : 'active'
      const action = newStatus === 'active' ? '启用' : '禁用'
      
      this.$confirm(`确定要${action}游戏"${game.name}"吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$axios.put(`/api/admin/games/${game.id}/status`, {
          status: newStatus
        })
          .then(response => {
            if (response.data && response.data.success) {
              this.$message.success(`${action}成功`)
              this.loadGames()
            }
          })
          .catch(error => {
            console.error(`${action}游戏失败:`, error)
            this.$message.error(`${action}失败`)
          })
      })
    },
    
    handleDeleteGame(game) {
      this.$confirm(`确定要删除游戏"${game.name}"吗？此操作不可恢复！`, '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'error'
      }).then(() => {
        this.$axios.delete(`/api/game/admin/delete/${game.id}`)
        //this.$axios.delete发送delete请求，删除资源
          .then(response => {
            if (response.data && response.data.success) {
              this.$message.success('删除成功')
              this.loadGames()
            }
          })
          .catch(error => {
            console.error('删除游戏失败:', error)
            this.$message.error('删除失败')
          })
      })
    },
    
    // 返回首页
    handleBack() {
      this.$router.push('/admin/home');
    },
    
    handleSubmitGame() {
      this.$refs.gameForm.validate((valid) => {
        if (valid) {
          this.submitting = true
          
          // 准备提交的数据
          let submitData = { ...this.gameForm }
          // 在更新时添加 id 字段
          if (this.currentGameId) {
            submitData.id = this.currentGameId
          }
          
          console.log('提交的数据:', submitData)
          
          const request = this.currentGameId 
            ? this.$axios.put(`/api/game/admin/update`, submitData)
            : this.$axios.post('/api/game/admin/add', submitData)
          
          request
            .then(response => {
              console.log('响应数据:', response.data)
              // 处理后端返回的Result对象格式
              if (response.data && response.data.success) {
                this.$message.success(this.currentGameId ? '编辑成功' : '添加成功')
                this.dialogVisible = false
                this.loadGames()
              }
            })
            .catch(error => {
              console.error('操作失败:', error)
              console.error('错误响应:', error.response)
              this.$message.error('操作失败')
            })
            .finally(() => {
              this.submitting = false
            })
        }
      })
    },
    
    handleDialogClose() {
      this.dialogVisible = false
      this.resetForm()
    },
    
    resetForm() {
      this.gameForm = {
        name: '',
       categories: '',
        description: '',
       introduction: '',
        detailedInfo: '',
       logoUrl: '',
        posterUrl: '',
       isFeatured: false
      }
      if (this.$refs.gameForm) {
        this.$refs.gameForm.clearValidate()
      }
    },
    
    handleUploadSuccess(response) {
      // 后端返回的是直接的字符串路径，而不是 JSON 对象
      if (typeof response === 'string') {
        this.gameForm.logoUrl = response
        this.$message.success('上传成功')
      } else if (response && response.success) {
        // 兼容 JSON 格式的响应
        this.gameForm.logoUrl = response.data.url
        this.$message.success('上传成功')
      }
    },
    
    beforeUpload(file) {
      const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
      const isLt2M = file.size / 1024 / 1024 < 2
      
      if (!isJPG) {
        this.$message.error('上传图片只能是 JPG/PNG 格式!')
      }
      if (!isLt2M) {
        this.$message.error('上传图片大小不能超过 2MB!')
      }
      return isJPG && isLt2M
    },
    
    formatDate(dateString) {
      if (!dateString) return ''
      return new Date(dateString).toLocaleString('zh-CN')
    }
  }
}
</script>

<style scoped>
.admin-game-management {
  min-height: 100vh;
  background-color: #f5f7fa;
  margin: 0;
  padding: 0;
}

.admin-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 20px 0;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.header-content h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
}

.back-button {
  background-color: rgba(255, 255, 255, 0.2);
  border-color: white;
  color: white;
}

.back-button:hover {
  background-color: rgba(255, 255, 255, 0.3);
  border-color: white;
  color: white;
}

.admin-content {
  max-width: 1200px;
  margin: 20px auto;
  padding: 0 20px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-area {
  display: flex;
  align-items: center;
}

.game-info {
  display: flex;
  align-items: center;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.avatar-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 178px;
  height: 178px;
}

.avatar-uploader:hover {
  border-color: #409EFF;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  line-height: 178px;
  text-align: center;
}

.avatar {
  width: 178px;
  height: 178px;
  display: block;
}

@media (max-width: 768px) {
  .table-header {
    flex-direction: column;
    gap: 10px;
    align-items: stretch;
  }
  
  .search-area {
    justify-content: space-between;
  }
}
</style>