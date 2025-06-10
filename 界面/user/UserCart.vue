<template>
  <div class="cart-container">
    <h2>购物车</h2>
    <div v-if="cartItems.length === 0" class="empty-cart">
      <el-empty description="购物车还是空的，去逛逛吧~">
        <el-button type="primary" @click="goToProducts">去购物</el-button>
      </el-empty>
    </div>
    <div v-else>
      <!-- 购物车商品列表 -->
      <div class="cart-content">
        <p>购物车功能开发中...</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const cartItems = ref([])

const goToProducts = () => {
  router.push('/user/product/list')
}
</script>

<style scoped>
.cart-container {
  padding: 20px;
}

.empty-cart {
  text-align: center;
  margin-top: 50px;
}
</style>