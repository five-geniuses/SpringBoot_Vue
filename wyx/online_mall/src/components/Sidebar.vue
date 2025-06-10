<!-- components/Sidebar.vue -->
<template>
  <el-menu
    :default-active="activeIndex"
    router
    class="sidebar-menu"
    background-color="#2c3e50"
    text-color="#ecf0f1"
    active-text-color="#f39c12"
  >
    <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
      <el-icon><component :is="item.icon" /></el-icon>
      <span>{{ item.title }}</span>
    </el-menu-item>
  </el-menu>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import {
  House,
  ShoppingBag,
  ShoppingCart,
  Tickets,
  User
} from '@element-plus/icons-vue'

const route = useRoute()
const activeIndex = ref('')

// 根据角色生成菜单
const userInfo = JSON.parse(sessionStorage.getItem('Atuserinfo') || '{}')
const role = userInfo.role ?? 1 // 默认为普通用户

const menuItems = [
  ...(role === 0
    ? [
        { path: '/admin/index', title: '首页', icon: House },
        { path: '/admin/product', title: '商品管理', icon: ShoppingBag },
        { path: '/admin/users', title: '用户管理', icon: User },
        { path: '/admin/orders', title: '订单管理', icon: Tickets }
      ]
    : [
        { path: '/user/index', title: '首页', icon: House },
        { path: '/user/product/list', title: '商品列表', icon: ShoppingBag },
        { path: '/user/cart', title: '购物车', icon: ShoppingCart },
        { path: '/user/orders', title: '我的订单', icon: Tickets }
      ])
]

// 初始化激活项
onMounted(() => {
  activeIndex.value = route.path
})

// 监听路由变化并更新激活项
watch(
  () => route.path,
  (newPath) => {
    activeIndex.value = newPath
  }
)
</script>

<style scoped>
.sidebar-menu {
  height: 100%;
  border-right: none;
}

.el-menu-item {
  display: flex;
  align-items: center;
}

.el-menu-item span {
  margin-left: 10px;
}
</style>