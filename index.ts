import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Regist from '../views/Regist.vue'

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
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router