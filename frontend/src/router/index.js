import { createRouter, createWebHistory } from 'vue-router'
import App from '../App.vue'

// 定义路由
const routes = [
  {
    path: '/',
    name: 'Home',
    component: App
  },
  // 这里可以根据项目需要添加更多的路由
  {
    path: '/table-manager',
    name: 'TableManager',
    // 懒加载组件
    component: () => import('../components/TableDataManager.vue')
  },
  {
    path: '/sql-converter',
    name: 'SqlConverter',
    // 懒加载MySQL到PostgreSQL转换组件
    component: () => import('../components/MySqlToPgConverter.vue')
  }
]

// 创建路由实例
const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router