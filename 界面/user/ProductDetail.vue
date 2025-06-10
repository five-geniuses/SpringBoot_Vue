<template>
  <div class="product-detail" v-if="product">
    <div class="detail-content">
      <!-- 商品图片 -->
      <div class="product-image-section">
        <div class="main-image">
          <img :src="product.image || '/placeholder-product.png'" :alt="product.name" />
        </div>
      </div>

      <!-- 商品信息 -->
      <div class="product-info-section">
        <h1 class="product-title">{{ product.name }}</h1>
        
        <div class="price-section">
          <span class="current-price">¥{{ product.price }}</span>
          <span v-if="product.originalPrice" class="original-price">¥{{ product.originalPrice }}</span>
          <span v-if="product.originalPrice" class="discount">
            {{ Math.round((1 - product.price / product.originalPrice) * 100) }}% OFF
          </span>
        </div>

        <div class="product-stats">
          <div class="stat-item">
            <span class="label">库存：</span>
            <span class="value" :class="{ 'low-stock': product.stock < 10 }">{{ product.stock }}</span>
          </div>
          <div class="stat-item">
            <span class="label">销量：</span>
            <span class="value">{{ product.sales || 0 }}</span>
          </div>
        </div>

        <div class="description-section">
          <h3>商品描述</h3>
          <p>{{ product.description || '暂无描述' }}</p>
        </div>

        <!-- 购买选项 -->
        <div class="purchase-section">
          <div class="quantity-selector">
            <span class="label">数量：</span>
            <el-input-number
              v-model="quantity"
              :min="1"
              :max="product.stock"
              size="large"
              style="width: 120px"
            />
          </div>

          <div class="action-buttons">
            <el-button
              type="primary"
              size="large"
              :icon="ShoppingCart"
              @click="handleAddToCart"
              :disabled="product.stock === 0"
              style="width: 180px"
            >
              {{ product.stock === 0 ? '缺货' : '加入购物车' }}
            </el-button>
            <el-button
              type="danger"
              size="large"
              @click="handleBuyNow"
              :disabled="product.stock === 0"
              style="width: 180px"
            >
              立即购买
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 商品详细信息tabs -->
    <div class="detail-tabs">
      <el-tabs v-model="activeTab" type="border-card">
        <el-tab-pane label="商品详情" name="detail">
          <div class="tab-content">
            <h4>商品特色</h4>
            <ul>
              <li>精选优质原料制作</li>
              <li>口感丰富，回味无穷</li>
              <li>包装精美，适合送礼</li>
              <li>严格质量控制，安全放心</li>
            </ul>
            
            <h4>商品规格</h4>
            <table class="spec-table">
              <tr>
                <td>商品名称</td>
                <td>{{ product.name }}</td>
              </tr>
              <tr>
                <td>商品价格</td>
                <td>¥{{ product.price }}</td>
              </tr>
              <tr>
                <td>库存数量</td>
                <td>{{ product.stock }}</td>
              </tr>
              <tr>
                <td>商品分类</td>
                <td>{{ product.category || '糖果' }}</td>
              </tr>
            </table>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="用户评价" name="reviews">
          <div class="tab-content">
            <div class="review-summary">
              <div class="rating-overview">
                <span class="rating-score">4.8</span>
                <el-rate v-model="averageRating" disabled show-score />
                <span class="review-count">(128条评价)</span>
              </div>
            </div>
            
            <div class="review-list">
              <div class="review-item" v-for="review in mockReviews" :key="review.id">
                <div class="review-header">
                  <span class="username">{{ review.username }}</span>
                  <el-rate v-model="review.rating" disabled size="small" />
                  <span class="review-date">{{ review.date }}</span>
                </div>
                <p class="review-content">{{ review.content }}</p>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { ShoppingCart } from '@element-plus/icons-vue'

interface Product {
  id: number
  name: string
  price: number
  originalPrice?: number
  image?: string
  description?: string
  stock: number
  sales?: number
  category?: string
}

interface Review {
  id: number
  username: string
  rating: number
  content: string
  date: string
}

const props = defineProps<{
  product: Product
}>()

const emit = defineEmits<{
  addToCart: [product: Product, quantity: number]
  buyNow: [product: Product, quantity: number]
}>()

const quantity = ref(1)
const activeTab = ref('detail')
const averageRating = ref(4.8)

// 模拟评价数据
const mockReviews: Review[] = [
  {
    id: 1,
    username: '用户***123',
    rating: 5,
    content: '糖果味道很好，包装也很精美，孩子很喜欢！',
    date: '2024-01-15'
  },
  {
    id: 2,
    username: '甜***爱好者',
    rating: 4,
    content: '口感不错，就是价格稍微有点贵，整体还是满意的。',
    date: '2024-01-12'
  },
  {
    id: 3,
    username: '购***达人',
    rating: 5,
    content: '发货速度很快，糖果新鲜，味道正宗，会回购的！',
    date: '2024-01-10'
  }
]

const handleAddToCart = () => {
  emit('addToCart', props.product, quantity.value)
}

const handleBuyNow = () => {
  emit('buyNow', props.product, quantity.value)
}
</script>

<style scoped>
.product-detail {
  padding: 20px;
}

.detail-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 40px;
  margin-bottom: 30px;
}

.product-image-section {
  text-align: center;
}

.main-image {
  border-radius: 12px;
  overflow: hidden;
  background-color: #f8f9fa;
  max-width: 400px;
  margin: 0 auto;
}

.main-image img {
  width: 100%;
  height: auto;
  max-height: 400px;
  object-fit: cover;
}

.product-info-section {
  padding: 0 20px;
}

.product-title {
  font-size: 28px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0 0 20px 0;
  line-height: 1.3;
}

.price-section {
  margin: 20px 0;
  padding: 15px 0;
  border-bottom: 1px solid #eee;
}

.current-price {
  font-size: 32px;
  font-weight: 700;
  color: #e74c3c;
  margin-right: 15px;
}

.original-price {
  font-size: 20px;
  color: #95a5a6;
  text-decoration: line-through;
  margin-right: 10px;
}

.discount {
  background: #e74c3c;
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
}

.product-stats {
  display: flex;
  gap: 30px;
  margin: 20px 0;
  padding: 15px 0;
  border-bottom: 1px solid #eee;
}

.stat-item {
  display: flex;
  align-items: center;
}

.stat-item .label {
  color: #7f8c8d;
  margin-right: 5px;
}

.stat-item .value {
  font-weight: 600;
  color: #2c3e50;
}

.stat-item .value.low-stock {
  color: #e74c3c;
}

.description-section {
  margin: 20px 0;
  padding: 15px 0;
  border-bottom: 1px solid #eee;
}

.description-section h3 {
  margin: 0 0 10px 0;
  color: #2c3e50;
}

.description-section p {
  color: #7f8c8d;
  line-height: 1.6;
}

.purchase-section {
  margin: 30px 0;
}

.quantity-selector {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.quantity-selector .label {
  margin-right: 15px;
  font-weight: 500;
  color: #2c3e50;
}

.action-buttons {
  display: flex;
  gap: 15px;
}

.detail-tabs {
  margin-top: 30px;
}

.tab-content {
  padding: 20px;
}

.tab-content h4 {
  margin: 20px 0 10px 0;
  color: #2c3e50;
}

.tab-content ul {
  margin: 10px 0;
  padding-left: 20px;
}

.tab-content li {
  margin: 5px 0;
  color: #7f8c8d;
  line-height: 1.6;
}

.spec-table {
  width: 100%;
  border-collapse: collapse;
  margin: 15px 0;
}

.spec-table td {
  padding: 10px;
  border: 1px solid #eee;
}

.spec-table td:first-child {
  background-color: #f8f9fa;
  font-weight: 500;
  width: 120px;
}

.review-summary {
  margin-bottom: 20px;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.rating-overview {
  display: flex;
  align-items: center;
  gap: 15px;
}

.rating-score {
  font-size: 36px;
  font-weight: 700;
  color: #e74c3c;
}

.review-count {
  color: #7f8c8d;
}

.review-list {
  margin-top: 20px;
}

.review-item {
  padding: 15px 0;
  border-bottom: 1px solid #eee;
}

.review-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 10px;
}

.username {
  font-weight: 600;
  color: #2c3e50;
}

.review-date {
  color: #95a5a6;
  font-size: 14px;
}

.review-content {
  color: #7f8c8d;
  line-height: 1.6;
  margin: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .detail-content {
    grid-template-columns: 1fr;
    gap: 20px;
  }
  
  .product-info-section {
    padding: 0;
  }
  
  .action-buttons {
    flex-direction: column;
  }
  
  .action-buttons .el-button {
    width: 100%;
  }
  
  .product-stats {
    flex-direction: column;
    gap: 10px;
  }
  
  .rating-overview {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
}
</style>