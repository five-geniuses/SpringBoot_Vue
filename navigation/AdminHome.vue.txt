<!-- views/admin/AdminHome.vue -->
<template>
  <el-container class="admin-layout">
    <!-- 左侧导航栏 -->
    <el-aside width="200px">
      <Sidebar />
    </el-aside>

    <!-- 右侧内容区域 -->
    <el-main class="main-content">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup lang="ts">
import Sidebar from '@/components/Sidebar.vue'
</script>

<style scoped>
.admin-layout {
  height: 100vh;
}

.el-aside {
  background-color: #2c3e50;
  color: #ecf0f1;
}

.el-main {
  background-color: #f9fafb;
  padding: 20px;
}
</style>