import Vue from 'vue'
import Router from 'vue-router'
import Login from './views/Login.vue'
import Register from './views/Register.vue'
import Home from './views/Home.vue'
import Profile from './views/Profile.vue'
import UserProfile from './views/UserProfile.vue'
import AdminDashboard from './views/AdminDashboard.vue'
import GameManagement from './views/GameManagement.vue'
import GameZone from './views/GameZone.vue'
import GameDetail from './views/GameDetail.vue'
import GuideDetail from './views/GuideDetail.vue'
import GuideEdit from './views/GuideEdit.vue'
import QuestionDetail from './views/QuestionDetail.vue'
import SimpleQuestions from './views/SimpleQuestions.vue'
import QuestionsPage from './views/QuestionsPage.vue'
import SearchResults from './views/SearchResults.vue'
import ResourceManagement from './views/ResourceManagement.vue'//ResourceManagement ：变量名，不等于变量组件，生成变量组件
import AiAssistant from './views/AiAssistant.vue'

// 导入新的管理员组件
import AdminLogin from './views/admin/AdminLogin.vue'
import AdminHome from './views/admin/AdminHome.vue'
import AdminGameManagement from './views/admin/AdminGameManagement.vue'
import AdminUserManagement from './views/admin/AdminUserManagement.vue'
import AdminOperationLog from './views/admin/AdminOperationLog.vue'

Vue.use(Router)

// 懒加载组件，提高性能
const ProfileLayout = () => import('./views/ProfileLayout.vue')
const UserInfo = () => import('./views/UserInfo.vue')
const UserPosts = () => import('./views/UserPosts.vue')
const UserAvatar = () => import('./views/UserAvatar.vue')

// 创建路由实例
const router = new Router({
  routes: [
    // 添加根路径路由 - 根据用户类型重定向
    {
      path: '/',
      redirect: (to) => {
        const userRole = localStorage.getItem('userRole');
        if (userRole === 'admin') {
          return '/admin/home';
        } else {
          return '/home';
        }
      }
    },
    // 普通用户路由
    {
      path: '/login',
      name: 'Login',
      component: Login
    },
    {
      path: '/register',
      name: 'Register',
      component: Register
    },
    {
      path: '/home',
      name: 'Home',
      component: Home,
      meta: {
        requiresAuth: true
      }
    },
    {
      path: '/search',
      name: 'SearchResults',
      component: SearchResults,
      meta: {
        requiresAuth: true,
        title: '搜索结果'
      }
    },
    // 为了兼容性，保留原个人中心路由，但改名为/profile-old
    {
      path: '/profile-old',
      name: 'Profile',
      component: Profile,
      meta: {
        requiresAuth: true
      }
    },
    // 新的个人中心布局路由
    {
      path: '/profile',
      component: ProfileLayout,
      meta: {
        requiresAuth: true
      },
      children: [
        {
          path: 'user-info',
          name: 'UserInfo',
          component: UserInfo
        },
        {
          path: 'user-posts',
          name: 'UserPosts',
          component: UserPosts
        },
        {
          path: 'user-avatar',
          name: 'UserAvatar',
          component: UserAvatar
        }
      ]
    },
    // 评论通知页面
    {
      path: '/notifications',
      name: 'NotificationCenter',
      component: () => import('./views/NotificationCenter.vue'),
      meta: {
        requiresAuth: true
      }
    },
    // 系统通知页面
    {
      path: '/system-notifications',
      name: 'SystemNotificationCenter',
      component: () => import('./views/SystemNotificationCenter.vue'),
      meta: {
        requiresAuth: true
      }
    },
    // 私聊页面
    {
      path: '/private-chat',
      name: 'PrivateChat',
      component: () => import('./views/PrivateChat.vue'),
      meta: {
        requiresAuth: true
      }
    },
    {
      path: '/game/:id',
      name: 'GameZone',
      component: GameZone,
      meta: {
        requiresAuth: true
      }
    },
    {
      path: '/game/:id/detail',
      name: 'GameDetail',
      component: GameDetail,
      meta: {
        requiresAuth: true
      }
    },
    {
      path: '/guide/:id',
      name: 'GuideDetail',
      component: GuideDetail,
      meta: {
        requiresAuth: true
      }
    },
    {
      path: '/guide/edit/:id',
      name: 'GuideEdit',
      component: GuideEdit,
      meta: {
        requiresAuth: true
      }
    },
    {
      path: '/simple-questions',
      name: 'SimpleQuestions',
      component: SimpleQuestions,
      meta: {
        requiresAuth: true
      }
    },
    {
      path: '/questions',
      name: 'QuestionsPage',
      component: QuestionsPage,
      meta: {
        requiresAuth: true
      }
    },
    {
      path: '/resources',//路由路径
      name: 'ResourceManagement',//路由名称，用于在代码中跳转
      component: ResourceManagement,//组件对象，不是文件名，component: 后面加的就是import导入时的组件变量名，但在这里是组件对象的意思
      meta: { requiresAuth: true }//需要登录才能访问
    },
    {
      path: '/ai-assistant',
      name: 'AiAssistant',
      component: AiAssistant,
      meta: { requiresAuth: true }
    },
    {
      path: '/question/:id', 
      name: 'QuestionDetail', 
      component: QuestionDetail,
      meta: { requiresAuth: true }
    },
    {
      path: '/user/:username', 
      name: 'UserProfile', 
      component: UserProfile,
      meta: { requiresAuth: true }
    },
    // 存储状态测试页面
    {
      path: '/storage-test',
      name: 'StorageTest',
      component: () => import('./views/StorageStateTest.vue'),//按需加载，访问页面时才会开始加载资源
      meta: { requiresAuth: true }
    },
    // 管理员路由 - 使用独立的前缀和组件
    {
      path: '/admin/login',
      name: 'AdminLogin',
      component: AdminLogin
    },
    {
      path: '/admin/home',
      name: 'AdminHome',
      component: AdminHome,
      meta: {
        requiresAuth: true,
        requiresAdmin: true
      }
    },
    {
      path: '/admin/game-management',
      name: 'AdminGameManagement',
      component: AdminGameManagement,
      meta: {
        requiresAuth: true,
        requiresAdmin: true
      }
    },
    {
      path: '/admin/user-management',
      name: 'AdminUserManagement',
      component: AdminUserManagement,
      meta: {
        requiresAuth: true,
        requiresAdmin: true,
        title: '用户管理'
      }
    },
    {
      path: '/admin/operation-logs',
      name: 'AdminOperationLog',
      component: AdminOperationLog,
      meta: {
        requiresAuth: true,
        requiresAdmin: true,
        title: '操作日志'
      }
    },

    // 处理404页面
    {
      path: '/404',
      name: 'NotFound',
      component: () => import('./views/NotFound.vue')
    },
    // 通配符路由重定向到404
    {
      path: '*',
      redirect: '/404'
    }// Vue Router 是 "按顺序匹配路由" ，找不到上面的路径时，执行404
  ]
})

// 路由守卫现在在main.js中统一处理，避免重复和冲突

export default router//固定语法，在vue router文件最后一行加入