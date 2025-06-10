// router/index.ts
import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Regist from '../views/Regist.vue'
import AdminHome from '../views/admin/AdminHome.vue'
import UserHome from '../views/user/UserHome.vue'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/regist',
    name: 'Regist',
    component: Regist
  },
  {
    path: '/admin',
    name: 'Admin',
    component: AdminHome,
    redirect: '/admin/index',
    children: [
      {
        path: 'index',
        name: 'AdminIndex',
        component: () => import('../views/admin/AdminIndex.vue') // 可选：你可以先保留这个首页
      }
    ]
  },
  {
    path: '/user',
    name: 'User',
    component: UserHome,
    redirect: '/user/index',
    children: [
      {
        path: 'index',
        name: 'UserIndex',
        component: () => import('../views/user/UserIndex.vue') // 同上
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router