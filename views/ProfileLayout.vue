<template>
  <div class="profile-layout">
    <!-- 侧边栏 -->
    <div class="sidebar">
      <div class="sidebar-header">
        <h2>个人中心</h2>
      </div>
      <div class="sidebar-menu">
        <el-menu
          :default-active="activeMenu"
          class="el-menu-vertical-demo"
          router
          @select="handleMenuSelect"
        >
          <el-menu-item index="/profile/user-info">
            <i class="el-icon-user"></i>
            <span slot="title">我的信息</span>
          </el-menu-item>
          <el-menu-item index="/profile/user-avatar">
            <i class="el-icon-picture"></i>
            <span slot="title">我的头像</span>
          </el-menu-item>
          <el-menu-item index="/profile/user-posts">
            <i class="el-icon-edit"></i>
            <span slot="title">我的发布</span>
          </el-menu-item>
        </el-menu>
      </div>
    </div>
    
    <!-- 主内容区域 -->
    <div class="main-content">
      <router-view></router-view>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ProfileLayout',
  data() {
    return {
      activeMenu: ''
    }
  },
  computed: {
    userInfo() {
      // 检查用户是否是管理员
      const userInfo = localStorage.getItem('user');
      if (userInfo) {
        return JSON.parse(userInfo);
      }
      return { isAdmin: false };
    }
  },
  mounted() {
    // 设置当前激活的菜单
    this.setActiveMenu();
    
    // 监听路由变化
    this.$router.afterEach(() => {
      this.setActiveMenu();
    });
  },
  methods: {
    setActiveMenu() {
      // 根据当前路由设置激活的菜单
      const currentPath = this.$route.path;
      if (currentPath.includes('user-info')) {
        this.activeMenu = '/profile/user-info';
      } else if (currentPath.includes('user-avatar')) {
        this.activeMenu = '/profile/user-avatar';
      } else if (currentPath.includes('user-posts')) {
        this.activeMenu = '/profile/user-posts';
      }
    },
    handleMenuSelect(index) {
      // 处理菜单选择事件，避免重复导航和导航冲突
      if (this.$route.path !== index) {
        // 添加短暂延迟，防止与页面刷新时的路由初始化发生冲突
        setTimeout(() => {
          // 再次检查路径，确保在延迟期间没有其他导航发生
          if (this.$route.path !== index) {
            this.$router.push(index);
          }
        }, 50);
      }
    }
  },
  beforeRouteEnter(to, from, next) {
    // 检查登录状态
    const username = localStorage.getItem('username');
    if (username) {
      next();
    } else {
      next('/login');
    }
  }
}
</script>

<style scoped>
.profile-layout {
  display: flex;
  min-height: calc(100vh - 60px); /* 减去导航栏高度 */
  background-color: #f5f7fa;
}

.sidebar {
  width: 200px;
  background-color: #fff;
  box-shadow: 2px 0 6px rgba(0, 0, 0, 0.1);
  position: fixed;
  height: calc(100vh - 60px);
  overflow-y: auto;
}

.sidebar-header {
  padding: 20px;
  text-align: center;
  border-bottom: 1px solid #e6e6e6;
}

.sidebar-header h2 {
  margin: 0;
  font-size: 18px;
  color: #333;
}

.sidebar-menu {
  padding: 10px 0;
}

.el-menu-vertical-demo {
  border-right: none;
}

.el-menu-vertical-demo:not(.el-menu--collapse) {
  width: 200px;
}

.el-menu-item {
  height: 48px;
  line-height: 48px;
}

.main-content {
  flex: 1;
  margin-left: 200px;
  padding: 20px;
}

/* 响应式设计 */
@media screen and (max-width: 768px) {
  .sidebar {
    width: 60px;
  }
  
  .sidebar-header h2 {
    display: none;
  }
  
  .el-menu-item span {
    display: none;
  }
  
  .main-content {
    margin-left: 60px;
  }
}
</style>