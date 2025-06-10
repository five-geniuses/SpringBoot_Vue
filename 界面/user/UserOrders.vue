<template>
  <div class="orders-container">
    <h2>我的订单</h2>
    <div class="orders-content">
      <el-empty description="暂无订单记录">
        <el-button type="primary" @click="goToProducts">去购物</el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const goToProducts = () => {
  router.push('/user/product/list')
}
</script>

<style scoped>
.orders-container {
  padding: 20px;
}

.orders-content {
  text-align: center;
  margin-top: 50px;
}
</style>