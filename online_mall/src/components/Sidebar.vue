<!-- components/Sidebar.vue -->
<template>
  <div class="sidebar-container">
    <!-- Logo 区域 -->
    <div class="logo-section">
      <div class="logo-container">
        <img src="/icon.png" alt="Logo" class="logo-image" />
        <div class="logo-text">
          <h3 class="brand-name">{{ role === 0 ? '管理后台' : '用户中心' }}</h3>
          <p class="brand-subtitle">{{ role === 0 ? 'Admin Dashboard' : 'User Portal' }}</p>
        </div>
      </div>
    </div>

    <!-- 分割线 -->
    <div class="divider"></div>

    <!-- 导航菜单 -->
    <div class="menu-section">
      <div class="menu-header">
        <span class="menu-title">{{ role === 0 ? '系统管理' : '个人中心' }}</span>
      </div>
      <el-menu
        :default-active="activeIndex"
        router
        class="sidebar-menu"
        background-color="transparent"
        text-color="#475569"
        active-text-color="#3b82f6"
      >
        <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path" class="menu-item">
          <el-icon class="menu-icon"><component :is="item.icon" /></el-icon>
          <span class="menu-text">{{ item.title }}</span>
        </el-menu-item>
      </el-menu>
    </div>

    <!-- 底部装饰 -->
    <div class="footer-decoration">
      <div class="decoration-dots">
        <span class="dot"></span>
        <span class="dot"></span>
        <span class="dot"></span>
      </div>
    </div>
  </div>
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
.sidebar-container {
  height: 100%;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
  border-right: 1px solid #e2e8f0;
  display: flex;
  flex-direction: column;
  position: relative;
}

/* Logo 区域样式 */
.logo-section {
  padding: 20px 16px;
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
  border-bottom: 1px solid #e2e8f0;
}

.logo-container {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-image {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.15);
  transition: transform 0.3s ease;
}

.logo-image:hover {
  transform: scale(1.05);
}

.logo-text {
  flex: 1;
}

.brand-name {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
  line-height: 1.2;
}

.brand-subtitle {
  font-size: 11px;
  color: #64748b;
  margin: 2px 0 0 0;
  font-weight: 400;
  letter-spacing: 0.5px;
}

/* 分割线 */
.divider {
  height: 1px;
  background: linear-gradient(90deg, transparent 0%, #e2e8f0 50%, transparent 100%);
  margin: 0 16px;
}

/* 菜单区域 */
.menu-section {
  flex: 1;
  padding: 16px 8px;
  overflow-y: auto;
}

.menu-header {
  padding: 0 8px 12px 8px;
  border-bottom: 1px solid #f1f5f9;
  margin-bottom: 8px;
}

.menu-title {
  font-size: 12px;
  font-weight: 600;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.sidebar-menu {
  border-right: none;
  background: transparent;
}

.menu-item {
  display: flex;
  align-items: center;
  margin: 4px 0;
  border-radius: 12px;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.menu-item:hover {
  background: linear-gradient(135deg, #e0f2fe 0%, #f0f9ff 100%) !important;
  transform: translateX(2px);
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.15);
}

.menu-item.is-active {
  background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%) !important;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.2);
  border-left: 3px solid #3b82f6;
}

.menu-item.is-active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  width: 3px;
  background: linear-gradient(180deg, #3b82f6 0%, #1d4ed8 100%);
  border-radius: 0 2px 2px 0;
}

.menu-icon {
  font-size: 18px;
  margin-right: 12px;
  transition: all 0.3s ease;
  color: #64748b;
}

.menu-item:hover .menu-icon {
  color: #3b82f6;
  transform: scale(1.1);
}

.menu-item.is-active .menu-icon {
  color: #3b82f6;
  transform: scale(1.05);
}

.menu-text {
  font-weight: 500;
  font-size: 14px;
  transition: all 0.3s ease;
  color: #475569;
}

.menu-item:hover .menu-text {
  color: #1e40af;
  font-weight: 600;
}

.menu-item.is-active .menu-text {
  color: #1e40af;
  font-weight: 600;
}

/* 底部装饰 */
.footer-decoration {
  padding: 16px;
  display: flex;
  justify-content: center;
  border-top: 1px solid #f1f5f9;
}

.decoration-dots {
  display: flex;
  gap: 6px;
}

.dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: linear-gradient(45deg, #e2e8f0 0%, #cbd5e1 100%);
  animation: pulse 2s infinite;
}

.dot:nth-child(2) {
  animation-delay: 0.5s;
}

.dot:nth-child(3) {
  animation-delay: 1s;
}

@keyframes pulse {
  0%, 100% {
    opacity: 0.4;
    transform: scale(1);
  }
  50% {
    opacity: 1;
    transform: scale(1.2);
  }
}

/* 添加顶部装饰线 */
.sidebar-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, #3b82f6 0%, #1d4ed8 50%, #3b82f6 100%);
  opacity: 0.6;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .logo-section {
    padding: 16px 12px;
  }
  
  .logo-image {
    width: 32px;
    height: 32px;
  }
  
  .brand-name {
    font-size: 14px;
  }
  
  .brand-subtitle {
    font-size: 10px;
  }
  
  .menu-section {
    padding: 12px 4px;
  }
  
  .menu-item {
    margin: 2px 0;
    border-radius: 8px;
  }
  
  .menu-icon {
    font-size: 16px;
    margin-right: 8px;
  }
  
  .menu-text {
    font-size: 13px;
  }
  
  .footer-decoration {
    padding: 12px;
  }
}
</style>