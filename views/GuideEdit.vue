<template>
  <div class="guide-edit-container">
    <!-- 返回按钮 -->
    <div class="back-button-container">
      <el-button @click="goBack" icon="el-icon-arrow-left">返回</el-button>
    </div>
    
    <!-- 编辑表单区域 -->
    <div class="edit-form-wrapper">
      <h1 class="edit-title">编辑攻略</h1>
      
      <!-- 编辑表单 -->
      <el-form ref="editForm" :model="editForm" :rules="rules" label-width="100px" class="edit-form">
        <!-- 攻略标题 -->
        <el-form-item label="攻略标题" prop="title">
          <el-input 
            v-model="editForm.title" 
            placeholder="请输入攻略标题"
            maxlength="200"
            show-word-limit
          ></el-input>
        </el-form-item>
        
        <!-- 游戏选择 -->
        <el-form-item label="所属游戏" prop="gameId">
          <el-select v-model="editForm.gameId" placeholder="请选择游戏" style="width: 100%">
            <el-option 
              v-for="game in gameList" 
              :key="game.id" 
              :label="game.name" 
              :value="game.id"
            ></el-option>
          </el-select>
        </el-form-item>
        
        <!-- 封面图片 -->
        <el-form-item label="封面图片">
          <div class="cover-image-upload">
            <el-upload
              class="avatar-uploader"
              action="/api/upload"
              :show-file-list="false"
              :on-success="handleCoverSuccess"
              :before-upload="beforeCoverUpload"
              :with-credentials="true"
            >
              <img v-if="editForm.coverImageUrl" :src="getImageUrl(editForm.coverImageUrl)" class="cover-image">
              <i v-else class="el-icon-plus avatar-uploader-icon"></i>
            </el-upload>
            <div class="upload-tip">点击上传封面图片（建议尺寸：800x400）</div>
          </div>
        </el-form-item>
        
        <!-- 攻略内容 -->
        <el-form-item label="攻略内容" prop="content">
          <el-input
            type="textarea"
            v-model="editForm.content"
            :rows="15"
            placeholder="请输入攻略内容，支持Markdown格式"
            resize="vertical"
          ></el-input>
        </el-form-item>
        
        <!-- 操作按钮 -->
        <el-form-item>
          <el-button type="primary" @click="submitEdit" :loading="submitting">保存修改</el-button>
          <el-button @click="goBack">取消</el-button>
          <el-button type="danger" @click="previewGuide" style="margin-left: 20px;">预览</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <!-- 预览对话框 -->
    <el-dialog
      title="攻略预览"
      :visible.sync="previewVisible"
      width="80%"
      top="5vh"
    >
      <div class="preview-content">
        <h1>{{ editForm.title }}</h1>
        <div v-if="editForm.coverImageUrl" class="preview-cover">
          <img :src="getImageUrl(editForm.coverImageUrl)" :alt="editForm.title" class="cover-image">
        </div>
        <div class="preview-text" v-html="formatContent(editForm.content)"></div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="previewVisible = false">关闭预览</el-button>
        <el-button type="primary" @click="submitEdit">确认保存</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { marked } from 'marked';

export default {
  name: 'GuideEdit',
  data() {
    return {
      guideId: null,           // 攻略ID
      guideInfo: null,         // 攻略原始信息
      editForm: {
        title: '',             // 攻略标题
        content: '',           // 攻略内容
        gameId: null,          // 游戏ID
        coverImageUrl: ''      // 封面图片URL
      },
      gameList: [],            // 游戏列表
      submitting: false,       // 提交状态
      previewVisible: false,   // 预览对话框显示状态
      token: '',              // 用户token
      
      // 表单验证规则
      rules: {
        title: [
          { required: true, message: '请输入攻略标题', trigger: 'blur' },
          { min: 1, max: 200, message: '标题长度在 1 到 200 个字符', trigger: 'blur' }
        ],
        content: [
          { required: true, message: '请输入攻略内容', trigger: 'blur' }
        ],
        gameId: [
          { required: true, message: '请选择游戏', trigger: 'change' }
        ]
      }
    }
  },
  
  created() {
    // 从路由参数中获取攻略ID
    this.guideId = this.$route.params.id;
    this.token = localStorage.getItem('token') || '';
    
    if (this.guideId) {
      this.fetchGuideDetail();
      this.fetchGameList();
    }
  },
  
  methods: {
    // 获取攻略详情
    async fetchGuideDetail() {
      try {
        console.log('开始获取攻略详情，攻略ID：', this.guideId);
        const response = await this.$axios.get(`/api/guide/${this.guideId}`);
        console.log('获取攻略详情完整响应：', response);
        console.log('获取攻略详情响应数据：', response.data);
        console.log('响应状态码：', response.status);
        console.log('响应头信息：', response.headers);
        
        // 后端直接返回攻略信息，没有success字段
        if (response.data && response.data.id) {
          this.guideInfo = response.data;
          console.log('攻略详情完整信息：', JSON.stringify(this.guideInfo, null, 2));
          console.log('游戏信息：', this.guideInfo.game);
          console.log('游戏ID：', this.guideInfo.game?.id);
          
          // 填充表单数据
          this.editForm = {
            title: this.guideInfo.title || '',
            content: this.guideInfo.content || '',
            gameId: this.guideInfo.game?.id || null,
            coverImageUrl: this.guideInfo.coverImageUrl || ''
          };
          console.log('表单数据已填充：', this.editForm);
          console.log('当前游戏列表：', this.gameList);
        } else {
          console.error('获取攻略详情失败：攻略信息不完整，响应数据：', response.data);
          this.$message.error('获取攻略详情失败：攻略信息不完整');
        }
      } catch (error) {
        console.error('获取攻略详情失败：', error);
        console.error('错误详情：', error.response);
        console.error('错误状态码：', error.response?.status);
        console.error('错误数据：', error.response?.data);
        this.$message.error('获取攻略详情失败');
      }
    },
    
    // 获取游戏列表
    async fetchGameList() {
      try {
        console.log('开始获取游戏列表');
        const response = await this.$axios.get('/api/game/list');
        console.log('获取游戏列表完整响应：', response);
        console.log('获取游戏列表响应数据：', response.data);
        console.log('响应状态码：', response.status);
        console.log('游戏列表数据类型：', typeof response.data);
        console.log('游戏列表是否为数组：', Array.isArray(response.data));
        
        // 后端直接返回游戏列表数组，没有success字段
        if (Array.isArray(response.data)) {
          this.gameList = response.data;
          console.log('游戏列表已加载，数量：', this.gameList.length);
          console.log('游戏列表详情：', JSON.stringify(this.gameList, null, 2));
        } else {
          console.error('获取游戏列表失败：返回数据格式不正确，实际数据：', response.data);
          this.$message.error('获取游戏列表失败：返回数据格式不正确');
        }
      } catch (error) {
        console.error('获取游戏列表失败：', error);
        console.error('错误详情：', error.response);
        console.error('错误状态码：', error.response?.status);
        console.error('错误数据：', error.response?.data);
        this.$message.error('获取游戏列表失败');
      }
    },
    
    // 提交编辑
    async submitEdit() {
      this.$refs.editForm.validate(async (valid) => {
        if (!valid) {
          this.$message.error('请完善表单信息');
          return;
        }
        
        this.submitting = true;
        try {
          const response = await this.$axios.put(`/api/guide/${this.guideId}`, this.editForm);
          if (response.data.success) {
            this.$message.success('攻略编辑成功');
            // 返回攻略详情页面
            this.$router.push(`/guide/${this.guideId}`);
          } else {
            this.$message.error('编辑失败：' + response.data.message);
          }
        } catch (error) {
          console.error('编辑失败：', error);
          if (error.response && error.response.data) {
            this.$message.error('编辑失败：' + error.response.data);
          } else {
            this.$message.error('编辑失败，请检查网络连接');
          }
        } finally {
          this.submitting = false;
        }
      });
    },
    
    // 预览攻略
    previewGuide() {
      this.$refs.editForm.validate((valid) => {
        if (valid) {
          this.previewVisible = true;
        } else {
          this.$message.error('请完善表单信息后再预览');
        }
      });
    },
    
    // 处理封面图片上传成功
    handleCoverSuccess(response) {
      console.log('上传响应：', response);
      
      // 后端直接返回文件路径字符串，不是JSON对象
      if (typeof response === 'string' && (response.startsWith('/assets/') || response.startsWith('/uploads/'))) {
        this.editForm.coverImageUrl = response;
        this.$message.success('封面图片上传成功');
      } else if (response && response.fileUrl) {
        // 兼容JSON格式响应
        this.editForm.coverImageUrl = response.fileUrl;
        this.$message.success('封面图片上传成功');
      } else {
        console.error('上传失败，响应数据：', response);
        this.$message.error('封面图片上传失败');
      }
    },
    
    // 上传前检查
    beforeCoverUpload(file) {
      const isJPG = file.type === 'image/jpeg' || file.type === 'image/png';
      const isLt2M = file.size / 1024 / 1024 < 2;

      if (!isJPG) {
        this.$message.error('上传封面图片只能是 JPG/PNG 格式!');
      }
      if (!isLt2M) {
        this.$message.error('上传封面图片大小不能超过 2MB!');
      }
      return isJPG && isLt2M;
    },
    
    // 格式化内容（Markdown转HTML）
    formatContent(content) {
      if (!content) return '';
      return marked(content);
    },
    
    // 获取图片完整URL
    getImageUrl(url) {
      console.log('获取图片URL，原始路径：', url);
      if (!url) return '';
      if (url.startsWith('http')) {
        console.log('返回HTTP路径：', url);
        return url;
      }
      // 如果是以/assets/开头的路径，直接返回（图片保存在前端本地）
      if (url.startsWith('/assets/')) {
        console.log('返回本地assets路径：', url);
        return url;
      }
      // 其他情况走API代理
      const apiUrl = `/api${url}`;
      console.log('返回API代理路径：', apiUrl);
      return apiUrl;
    },
    
    // 返回上一页
    goBack() {
      this.$router.go(-1);
    }
  }
}
</script>

<style scoped>
.guide-edit-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.back-button-container {
  margin-bottom: 20px;
}

.edit-form-wrapper {
  background: #fff;
  border-radius: 8px;
  padding: 30px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.edit-title {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
  font-size: 24px;
}

.edit-form {
  max-width: 800px;
  margin: 0 auto;
}

.cover-image-upload {
  text-align: center;
}

.avatar-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 300px;
  height: 150px;
  margin: 0 auto;
}

.avatar-uploader:hover {
  border-color: #409EFF;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 300px;
  height: 150px;
  line-height: 150px;
  text-align: center;
}

.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.upload-tip {
  margin-top: 10px;
  color: #909399;
  font-size: 12px;
}

.preview-content {
  max-height: 70vh;
  overflow-y: auto;
}

.preview-cover {
  margin: 20px 0;
  text-align: center;
}

.preview-cover img {
  max-width: 100%;
  max-height: 300px;
  border-radius: 8px;
}

.preview-text {
  line-height: 1.6;
  font-size: 14px;
}

.preview-text >>> h1,
.preview-text >>> h2,
.preview-text >>> h3 {
  margin-top: 20px;
  margin-bottom: 10px;
}

.preview-text >>> p {
  margin-bottom: 10px;
}

.preview-text >>> ul,
.preview-text >>> ol {
  margin-left: 20px;
  margin-bottom: 10px;
}

.preview-text >>> code {
  background-color: #f4f4f4;
  padding: 2px 4px;
  border-radius: 3px;
  font-family: 'Courier New', monospace;
}

.preview-text >>> pre {
  background-color: #f4f4f4;
  padding: 10px;
  border-radius: 5px;
  overflow-x: auto;
}

.preview-text >>> blockquote {
  border-left: 4px solid #ddd;
  padding-left: 15px;
  margin-left: 0;
  color: #666;
}
</style>