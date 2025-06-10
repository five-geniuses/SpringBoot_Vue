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
        component: () => import('../views/admin/AdminIndex.vue')
      },
       {
        path: 'product',
        name: 'AdminProduct',
        component: () => import('../views/admin/ProductManagement.vue')
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('../views/admin/UserManagement.vue')
      },
      {
        path: 'orders',
        name: 'AdminOrders',
        component: () => import('../views/admin/OrderManagement.vue')
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
        component: () => import('../views/user/UserIndex.vue')
      },
      {
        path: 'product/list',
        name: 'UserProductList',
        component: () => import('../views/user/ProductList.vue')
      },
      {
        path: 'cart',
        name: 'UserCart',
        component: () => import('../views/user/UserCart.vue')
      },
      {
        path: 'orders',
        name: 'UserOrders',
        component: () => import('../views/user/UserOrders.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router