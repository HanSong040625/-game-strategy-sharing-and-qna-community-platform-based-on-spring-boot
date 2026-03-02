<template>
  <div class="resource-management-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">资源中心</h1>
      <p class="page-description">共享的游戏资源、MOD、插件等文件，所有用户都可以查看和下载</p>
    </div>

    <!-- 资源上传区域 -->
    <div class="upload-section">
      <el-card class="upload-card">
        <div slot="header" class="card-header">
          <span class="card-title">上传资源</span>
        </div>
        <!-- 文件夹上传区域 -->
         
        <div class="folder-upload-area">
           <!--     
          :file-list="fileList"存储上传的文件列表
          :show-file-list="true" 是否显示文件列表
          :limit="50" 最多上传50个文件
          :auto-upload="false" 关闭自动上传，选择文件后需要手动点击上传按钮
          accept="*/*" 允许的文件类型，*/*表示不限制任何文件类型
          drag开启拖拽模式，用户可以直接把电脑里的文件 / 文件夹拖到组件区域完成选择
           -->
          <el-upload
            ref="upload"
            class="upload-demo"
            :action="uploadAction"
            :multiple="true"
            :auto-upload="false"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            :before-upload="beforeUpload"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :limit="50"
            :show-file-list="true"
            accept="*/*"
            :headers="uploadHeaders"
            :with-credentials="true"
            drag>
            
            <i class="el-icon-upload"></i>
            <div class="el-upload__text">
              <p>将文件夹或文件拖拽到此处，或<em>点击上传</em></p>
              <p class="upload-tip">支持上传文件夹（自动递归上传所有文件）</p>
              <p class="upload-tip">单个文件最大支持 2GB，支持所有文件类型</p>
            </div>
          </el-upload>
          
          <!-- 上传控制按钮 -->
          <div class="upload-controls">
            <!-- @click="submitUpload" 点击事件，执行submitupload方法-->
            <el-button type="primary" @click="submitUpload" :loading="uploading">
              开始上传
            </el-button>
            <el-button @click="clearFiles">清空列表</el-button>
            <el-button @click="selectFolder" type="text">
              <i class="el-icon-folder-opened"></i> 选择文件夹
            </el-button>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 资源列表区域 -->
    <div class="resource-list-section">
      <el-card class="resource-list-card">
        <div slot="header" class="card-header">
          <span class="card-title">共享资源</span>
          <div class="list-controls">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索资源名称"
              prefix-icon="el-icon-search"
              class="search-input"
              clearable>
            </el-input>
            <el-button-group>
              <el-button :type="sortType === 'time' ? 'primary' : 'default'" @click="changeSort('time')">
                按时间排序
              </el-button>
              <el-button :type="sortType === 'name' ? 'primary' : 'default'" @click="changeSort('name')">
                按名称排序
              </el-button>
            </el-button-group>
          </div>
        </div>

        <!-- 资源列表 -->
        <div class="resource-list">
          <div v-if="loading" class="loading-state">
            <i class="el-icon-loading"></i>
            <p>加载中...</p>
          </div>
          
          <div v-else-if="filteredResources.length === 0" class="empty-state">
            <el-empty description="暂无资源，请上传您的第一个资源文件"></el-empty>
          </div>
          
          <div v-else class="resource-items">
            <div v-for="resource in paginatedResources" :key="resource.id" class="resource-item">
              <div class="resource-icon">
                <i :class="getFileIcon(resource.fileType)"></i>
              </div>
              <div class="resource-info">
                <h4 class="resource-name">{{ resource.name }}</h4>
                <p class="resource-meta">
                  <span class="file-size">{{ formatFileSize(resource.size) }}</span>
                  <span class="file-type">{{ resource.fileType }}</span>
                  <span class="upload-time">{{ formatDate(resource.uploadTime) }}</span>
                  <span class="uploader" v-if="resource.uploader">上传者: {{ resource.uploader }}</span>
                  <span class="uploader" v-else>上传者: 未知</span>
                </p>
                <p class="resource-description" v-if="resource.description">
                  {{ resource.description }}
                </p>
              </div>
              <div class="resource-actions">
                <el-button type="primary" size="small" @click="downloadResource(resource)">
                  下载
                </el-button>
                <el-button type="warning" size="small" @click="editResource(resource)">
                  编辑
                </el-button>
                <el-button type="danger" size="small" @click="deleteResource(resource)">
                  删除
                </el-button>
              </div>
            </div>
          </div>
          
          <!-- 分页控件 -->
          <div class="pagination-container" v-if="filteredResources.length > pageSize">
            <el-pagination
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
              :current-page="currentPage"
              :page-sizes="[10, 20, 50, 100]"
              :page-size="pageSize"
              layout="total, sizes, prev, pager, next, jumper"
              :total="filteredResources.length">
            </el-pagination>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 资源编辑对话框 -->
    <el-dialog
      title="编辑资源信息"
      :visible.sync="editDialogVisible"
      width="500px"
      @close="closeEditDialog">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="资源名称">
          <el-input v-model="editForm.name"></el-input>
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            type="textarea"
            :rows="3"
            v-model="editForm.description"
            placeholder="请输入资源描述（可选）">
          </el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveResourceEdit" :loading="savingEdit">
          保存
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'ResourceManagement',
  data() {
    return {
      // 上传相关
      fileList: [],
      uploading: false,
      
      // 资源列表相关
      resources: [],
      loading: false,
      searchKeyword: '',
      sortType: 'time', // time, name
      
      // 分页相关
      currentPage: 1,
      pageSize: 10,
      
      // 编辑对话框相关
      editDialogVisible: false,
      editForm: {
        id: null,
        name: '',
        description: ''
      },
      savingEdit: false
    }
  },
  
  computed: {
    // 上传API地址
    uploadAction() {
      return '/api/resources/upload';//后端通信路径
    },
    
    // 上传请求头
    uploadHeaders() {
      const token = localStorage.getItem('token');
      const headers = {
        'X-Requested-With': 'XMLHttpRequest'
      };
      
      if (token) {
        headers['Authorization'] = `Bearer ${token}`;
      }
      
      return headers;
    },
    
    // 过滤后的资源列表
    filteredResources() {
      let filtered = this.resources.filter(resource => 
        resource.name.toLowerCase().includes(this.searchKeyword.toLowerCase())
      )
      
      // 排序
      if (this.sortType === 'time') {
        filtered.sort((a, b) => new Date(b.uploadTime) - new Date(a.uploadTime))
      } else if (this.sortType === 'name') {
        filtered.sort((a, b) => a.name.localeCompare(b.name))
      }
      
      return filtered
    },
    
    // 分页后的资源列表
    paginatedResources() {
      const start = (this.currentPage - 1) * this.pageSize
      const end = start + this.pageSize
      return this.filteredResources.slice(start, end)
    }
  },
  
  mounted() {
    // 检查登录状态，如果未登录，提示用户登录
    this.checkLoginAndLoadResources()
  },
  
  methods: {
    // 检查登录状态并加载资源
    checkLoginAndLoadResources() {
      const token = localStorage.getItem('token');
      const isLoggedIn = !!token;
      
      if (!isLoggedIn) {
        this.$message.info('部分资源可能需要登录后查看，请考虑登录以获取完整功能')
      }
      
      // 即使未登录也尝试加载资源，让后端决定是否允许访问
      this.loadResources()
    },
    
    // 加载资源列表
    async loadResources() {
      this.loading = true
      try {
        // 调用后端API获取资源列表
        const token = localStorage.getItem('token')
        const headers = {
          'X-Requested-With': 'XMLHttpRequest'
        }
        
        if (token) {
          headers['Authorization'] = `Bearer ${token}`
        }
        
        const response = await this.$axios.get('/api/resources/list', {
          withCredentials: true,
          headers: headers
        })
        
        console.log('获取资源列表响应:', response)
        
        // 确保正确处理后端返回的数据结构
        let resources = []
        if (response.data && response.data.code === 200 && response.data.data) {
          // 从后端数据中提取资源列表
          resources = response.data.data.resources || []
          
          // 为每个资源添加name属性，使用originalName或fileName作为显示名称
          resources = resources.map(resource => ({
            ...resource,
            name: resource.originalName || resource.fileName || '未知文件名',
            // 确保size字段存在，使用fileSize
            size: resource.fileSize || 0
          }))
          
          console.log('成功加载资源列表，数量:', resources.length)
          console.log('资源数据示例:', resources.slice(0, 1))
          
          this.resources = resources
        } else if (response.data && response.data.code === 401) {
          console.warn('未授权访问资源列表，需要登录')
          this.$message.warning('查看资源需要登录，请先登录')
          this.resources = []
        } else {
          console.error('获取资源列表失败:', response.data)
          this.$message.error(`获取资源列表失败: ${response.data?.message || '未知错误'}`)
          this.resources = []
        }
        
        // 如果没有数据，给用户一个友好提示
        if (this.resources.length === 0) {
          this.$message.info('暂无资源数据')
        }
      } catch (error) {
        console.error('加载资源失败:', error)
        
        // 处理401未授权错误
        if (error.response && error.response.status === 401) {
          console.warn('未授权访问资源列表，需要登录')
          this.$message.warning('查看资源需要登录，请先登录')
        } else if (error.response && error.response.data) {
          this.$message.error(`加载资源失败: ${error.response.data.message || error.response.data}`)
        } else {
          this.$message.error('加载资源失败，请检查网络连接')
        }
        
        // 清空资源列表
        this.resources = []
      } finally {
        // 确保无论如何都会重置loading状态
        this.loading = false
      }
    },
    
    // 文件选择变化
    handleFileChange(file, fileList) {
      this.fileList = fileList
    },
    
    // 文件移除
    handleFileRemove(file, fileList) {
      this.fileList = fileList
    },
    
    // 上传前验证
    beforeUpload(file) {
      const isLt2G = file.size / 1024 / 1024 / 1024 < 2
      if (!isLt2G) {
        this.$message.error('文件大小不能超过 2GB!')
        return false
      }
      return true
    },
    
    // 提交上传
    submitUpload() {
      if (this.fileList.length === 0) {
        this.$message.warning('请先选择要上传的文件')
        return
      }
      
      // 检查登录状态，只有登录用户才能上传
      const token = localStorage.getItem('token');
      const username = localStorage.getItem('username');
      const isLoggedIn = !!token && !!username;
      
      if (!isLoggedIn) {
        this.$message.warning('请先登录后再上传资源')
        // 使用window.location.href避免路由守卫冲突
        setTimeout(() => {
          window.location.href = '/login';
        }, 1000);
        return
      }
      
      this.uploading = true
      this.$refs.upload.submit()
    },
    
    // 上传成功
    handleUploadSuccess(response, file, fileList) {
      this.uploading = false
      this.$message.success('文件上传成功')
      
      // 清空文件列表
      this.clearFiles()
      
      this.loadResources() // 重新加载资源列表
    },
    
    // 上传失败
    handleUploadError(error, file, fileList) {
      this.uploading = false
      console.error('文件上传失败详情:', error)
      
      // 处理401未授权错误
      if (error && error.response && error.response.status === 401) {
        this.$message.error('请先登录后再上传文件')
        // 清除本地登录状态
        localStorage.removeItem('token');
        localStorage.removeItem('username');
        localStorage.removeItem('user');
        localStorage.removeItem('isAdmin');
        
        // 延迟跳转到登录页，避免路由冲突
        setTimeout(() => {
          if (this.$route.path !== '/login') {
            this.$router.replace('/login');
          }
        }, 1500);
      } else if (error && error.response && error.response.data) {
        this.$message.error(`文件上传失败: ${error.response.data.message || error.response.data}`)
      } else if (error && error.message) {
        this.$message.error(`文件上传失败: ${error.message}`)
      } else {
        this.$message.error('文件上传失败，请检查网络连接或联系管理员')
      }
    },
    
    // 清空文件列表
    clearFiles() {
      console.log('清空文件列表，当前文件数量:', this.fileList.length)
      
      try {
        // 首先清空本地fileList数组
        this.fileList = []
        
        // 尝试清空el-upload组件的内部文件列表
        if (this.$refs.upload && this.$refs.upload.clearFiles) {
          this.$refs.upload.clearFiles()
          console.log('el-upload组件文件列表已清空')
        } else {
          console.warn('el-upload组件引用不存在或clearFiles方法不可用')
          // 如果el-upload的clearFiles方法不可用，尝试其他方式
          if (this.$refs.upload) {
            // 尝试重置el-upload组件的文件列表
            this.$refs.upload.uploadFiles = []
            console.log('通过直接设置uploadFiles数组清空列表')
          }
        }
        
        console.log('清空文件列表操作完成')
        
      } catch (error) {
        console.error('清空文件列表时发生错误:', error)
        this.$message.error('清空文件列表失败，请重试')
      }
    },
    
    // 选择文件夹
    selectFolder() {
      // 在实际项目中，这里可以调用浏览器的文件夹选择API
      this.$message.info('请使用拖拽或点击上传区域选择文件夹')
    },
    
    // 获取文件图标
    getFileIcon(fileType) {
      const iconMap = {
        'zip': 'el-icon-files',
        'rar': 'el-icon-files',
        '7z': 'el-icon-files',
        'js': 'el-icon-document',
        'json': 'el-icon-document',
        'txt': 'el-icon-document',
        'pdf': 'el-icon-document',
        'doc': 'el-icon-document',
        'docx': 'el-icon-document',
        'xls': 'el-icon-document',
        'xlsx': 'el-icon-document',
        'ppt': 'el-icon-document',
        'pptx': 'el-icon-document',
        'jpg': 'el-icon-picture',
        'jpeg': 'el-icon-picture',
        'png': 'el-icon-picture',
        'gif': 'el-icon-picture',
        'bmp': 'el-icon-picture',
        'mp4': 'el-icon-video-camera',
        'avi': 'el-icon-video-camera',
        'mov': 'el-icon-video-camera',
        'mp3': 'el-icon-headset',
        'wav': 'el-icon-headset',
        'flac': 'el-icon-headset'
      }
      return iconMap[fileType.toLowerCase()] || 'el-icon-document'
    },
    
    // 格式化文件大小
    formatFileSize(bytes) {
      if (bytes === 0) return '0 B'
      const k = 1024
      const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
      const i = Math.floor(Math.log(bytes) / Math.log(k))
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
    },
    
    // 格式化日期
    formatDate(date) {
      if (!date) return ''
      const d = new Date(date)
      return d.toLocaleDateString('zh-CN') + ' ' + d.toLocaleTimeString('zh-CN', { 
        hour: '2-digit', 
        minute: '2-digit' 
      })
    },
    
    // 下载资源
    async downloadResource(resource) {
      try {
        this.$message.success(`开始下载: ${resource.name}`)
        
        // 调用后端下载API
        const response = await this.$axios.get(`/api/resources/download/${resource.fileName}`, {
          responseType: 'blob', // 重要：指定响应类型为blob
          withCredentials: true
        })
        
        // 创建下载链接
        const url = window.URL.createObjectURL(new Blob([response.data]))
        const link = document.createElement('a')
        link.href = url
        link.setAttribute('download', resource.originalName || resource.name)
        document.body.appendChild(link)
        link.click()
        
        // 清理
        window.URL.revokeObjectURL(url)
        document.body.removeChild(link)
        
        this.$message.success(`下载完成: ${resource.name}`)
        
      } catch (error) {
        console.error('下载失败:', error)
        this.$message.error(`下载失败: ${resource.name}`)
      }
    },
    
    // 编辑资源
    editResource(resource) {
      this.editForm = {
        id: resource.id,
        name: resource.name,
        description: resource.description || ''
      }
      this.editDialogVisible = true
    },
    
    // 保存编辑
    async saveResourceEdit() {
      if (!this.editForm.name.trim()) {
        this.$message.warning('请输入资源名称')
        return
      }
      
      this.savingEdit = true
      try {
        // 模拟API调用
        await new Promise(resolve => setTimeout(resolve, 500))
        
        // 更新本地数据
        const index = this.resources.findIndex(r => r.id === this.editForm.id)
        if (index !== -1) {
          this.resources[index].name = this.editForm.name
          this.resources[index].description = this.editForm.description
        }
        
        this.$message.success('资源信息更新成功')
        this.editDialogVisible = false
      } catch (error) {
        this.$message.error('保存失败')
      } finally {
        this.savingEdit = false
      }
    },
    
    // 关闭编辑对话框
    closeEditDialog() {
      this.editForm = {
        id: null,
        name: '',
        description: ''
      }
    },
    
    // 删除资源
    deleteResource(resource) {
      // 检查登录状态
      const token = localStorage.getItem('token');
      const username = localStorage.getItem('username');
      const isLoggedIn = !!token && !!username;
      
      if (!isLoggedIn) {
        this.$message.warning('请先登录后再删除资源')
        // 使用window.location.href避免路由守卫冲突
        setTimeout(() => {
          window.location.href = '/login';
        }, 1000);
        return;
      }
      
      // 检查权限：只有上传者或管理员可以删除
      const isAdmin = username.toLowerCase().includes('admin');
      const isUploader = resource.uploader === username;
      
      if (!isAdmin && !isUploader) {
        this.$message.error('您没有权限删除此资源，只有上传者或管理员可以删除')
        return;
      }
      
      this.$confirm(`确定要删除资源 "${resource.name}" 吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          // 调用后端API删除资源
          const response = await this.$axios.delete(`/api/resources/delete/${resource.fileName}`, {
            withCredentials: true,
            headers: {
              'X-Requested-With': 'XMLHttpRequest',
              'Authorization': `Bearer ${token}`
            }
          })
          
          if (response.data && response.data.code === 200) {
            // 从本地数据中删除
            this.resources = this.resources.filter(r => r.id !== resource.id)
            this.$message.success('资源删除成功')
          } else {
            this.$message.error(response.data.message || '删除失败')
          }
        } catch (error) {
          console.error('删除失败:', error)
          if (error.response && error.response.data) {
            this.$message.error(`删除失败: ${error.response.data.message || error.response.data}`)
          } else {
            this.$message.error('删除失败，请检查网络连接')
          }
        }
      }).catch(() => {
        // 用户取消删除
      })
    },
    
    // 改变排序方式
    changeSort(type) {
      this.sortType = type
      this.currentPage = 1
    },
    
    // 分页大小改变
    handleSizeChange(val) {
      this.pageSize = val
      this.currentPage = 1
    },
    
    // 当前页改变
    handleCurrentChange(val) {
      this.currentPage = val
    }
  }
}
</script>

<style scoped>
.resource-management-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  text-align: center;
  margin-bottom: 30px;
}

.page-title {
  font-size: 28px;
  color: #303133;
  margin-bottom: 10px;
}

.page-description {
  font-size: 16px;
  color: #606266;
}

.upload-section {
  margin-bottom: 30px;
}

.upload-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.folder-upload-area {
  padding: 20px;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin: 5px 0;
}

.upload-controls {
  margin-top: 20px;
  text-align: center;
}

.resource-list-section {
  margin-bottom: 30px;
}

.list-controls {
  display: flex;
  gap: 15px;
  align-items: center;
}

.search-input {
  width: 200px;
}

.resource-list {
  min-height: 400px;
}

.loading-state {
  text-align: center;
  padding: 60px 0;
  color: #909399;
}

.empty-state {
  padding: 60px 0;
}

.resource-items {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.resource-item {
  display: flex;
  align-items: center;
  padding: 15px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  transition: all 0.3s;
}

.resource-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
}

.resource-icon {
  font-size: 32px;
  color: #409eff;
  margin-right: 15px;
}

.resource-info {
  flex: 1;
}

.resource-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.resource-meta {
  display: flex;
  gap: 15px;
  font-size: 12px;
  color: #909399;
  margin-bottom: 5px;
}

.uploader {
  color: #67c23a;
  font-weight: 500;
}

.resource-description {
  font-size: 14px;
  color: #606266;
  margin-top: 5px;
}

.resource-actions {
  display: flex;
  gap: 8px;
}

.pagination-container {
  margin-top: 20px;
  text-align: center;
}

@media (max-width: 768px) {
  .resource-management-container {
    padding: 10px;
  }
  
  .resource-item {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .resource-actions {
    margin-top: 15px;
    width: 100%;
    justify-content: flex-end;
  }
  
  .list-controls {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-input {
    width: 100%;
  }
}
</style>