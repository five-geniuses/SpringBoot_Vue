<template>
  <div class="product-detail" v-if="productData">
    <div class="detail-content">
      <!-- 商品图片 -->
      <div class="product-image-section">
        <div class="main-image">
          <img :src="getImageUrl(productData.goods.imgUrl)" :alt="productData.goods.name" />
        </div>
      </div>

      <!-- 商品信息 -->
      <div class="product-info-section">
        <h1 class="product-title">{{ productData.goods.name }}</h1>
        
        <div class="price-section">
          <span class="current-price">¥{{ productData.goods.price }}</span>
        </div>

        <div class="product-stats">
          <div class="stat-item">
            <span class="label">库存：</span>
            <span class="value" :class="{ 'low-stock': productData.goods.num < 10 }">{{ productData.goods.num }}</span>
          </div>
          <div class="stat-item">
            <span class="label">销量：</span>
            <span class="value">{{ productData.goods.sales || 0 }}</span>
          </div>
        </div>

        <div class="description-section">
          <h3>商品描述</h3>
          <p>{{ productData.goods.desc || '暂无描述' }}</p>
        </div>

        <!-- 购买选项 -->
        <div class="purchase-section">
          <div class="quantity-selector">
            <span class="label">数量：</span>
            <el-input-number
              v-model="quantity"
              :min="1"
              :max="productData.goods.num"
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
              :disabled="productData.goods.num === 0"
              style="width: 180px"
            >
              {{ productData.goods.num === 0 ? '缺货' : '加入购物车' }}
            </el-button>
            <el-button
              type="danger"
              size="large"
              @click="handleBuyNow"
              :disabled="productData.goods.num === 0"
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
            <h4>商品规格</h4>
            <table class="spec-table">
              <tr>
                <td>商品名称</td>
                <td>{{ productData.goods.name }}</td>
              </tr>
              <tr>
                <td>商品价格</td>
                <td>¥{{ productData.goods.price }}</td>
              </tr>
              <tr>
                <td>库存数量</td>
                <td>{{ productData.goods.num }}</td>
              </tr>
              <tr>
                <td>商品分类</td>
                <td>{{ productData.goods.categoryName || '未知' }}</td>
              </tr>
              <tr>
                <td>商品规格</td>
                <td>{{ productData.goods.size || '标准' }}</td>
              </tr>
              <tr>
                <td>重量</td>
                <td>{{ productData.goods.kgs || 0 }}kg</td>
              </tr>
              <tr>
                <td>存储方式</td>
                <td>{{ productData.goods.storagemethod || '常温' }}</td>
              </tr>
              <tr>
                <td>生产日期</td>
                <td>{{ formatDate(productData.goods.creationdate) }}</td>
              </tr>
              <tr v-if="productData.goods.expirationdate">
                <td>保质期</td>
                <td>{{ productData.goods.expirationdate }}</td>
              </tr>
            </table>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="用户评价" name="reviews">
          <div class="tab-content">
            <div class="review-summary">
              <div class="rating-overview">
                <span class="rating-score">{{ productData.avgRating || 0 }}</span>
                <el-rate :model-value="productData.avgRating || 0" disabled show-score />
                <span class="review-count">({{ productData.comments.total }}条评价)</span>
              </div>
            </div>
            
            <div class="review-list" v-if="productData.comments.records.length > 0">
              <div class="review-item" v-for="review in productData.comments.records" :key="review.commentId">
                <div class="review-header">
                  <span class="username">{{ review.userName }}</span>
                  <el-rate :model-value="review.rating" disabled size="small" />
                  <span class="review-date">{{ formatDate(review.createTime) }}</span>
                </div>
                <p class="review-content">{{ review.content }}</p>
              </div>
            </div>
            
            <div v-else class="no-reviews">
              <el-empty description="暂无评价" :image-size="100" />
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

// 根据API接口定义数据结构
interface Comment {
  commentId: number
  userId: number
  goodsId: number
  rating: number
  content: string
  createTime: string
  userName: string
  goodsName: string
}

interface Goods {
  goodsId: number
  name: string
  desc: string
  cateId: number
  categoryName: string
  price: number
  num: number
  kgs: number
  size: string
  creationdate: string
  expirationdate: string
  storagemethod: string
  addtime: string
  state: number
  imgUrl: string
  sales: number
  avgRating: number
}

interface ProductDetailData {
  comments: {
    records: Comment[]
    total: number
    size: number
    current: number
    pages: number
  }
  goods: Goods
  avgRating: number
}

const props = defineProps<{
  productData: ProductDetailData
}>()

const emit = defineEmits<{
  addToCart: [goods: Goods, quantity: number]
  buyNow: [goods: Goods, quantity: number]
}>()

const quantity = ref(1)
const activeTab = ref('detail')

// 获取图片URL
const getImageUrl = (imgUrl: string | null): string => {
  if (!imgUrl) {
    return '/placeholder-product.png'
  }
  
  // 如果已经是完整URL，直接返回
  if (imgUrl.startsWith('http://') || imgUrl.startsWith('https://')) {
    return imgUrl
  }
  
  // 如果已经包含/uploads/路径，直接拼接baseUrl
  if (imgUrl.startsWith('/uploads/')) {
    return `http://localhost:8080${imgUrl}`
  }
  
  // 否则拼接 /uploads/ + imgUrl
  return `http://localhost:8080/uploads/${imgUrl}`
}

// 格式化日期
const formatDate = (dateStr: string): string => {
  if (!dateStr || dateStr === 'string') return '未知'
  
  try {
    const date = new Date(dateStr)
    return date.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit'
    })
  } catch (error) {
    return dateStr
  }
}

const handleAddToCart = () => {
  emit('addToCart', props.productData.goods, quantity.value)
}

const handleBuyNow = () => {
  emit('buyNow', props.productData.goods, quantity.value)
}
</script>

<style scoped>
.product-detail {
  padding: 15px;                        
}

.detail-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 25px;                         
  margin-bottom: 20px;                
}

.product-info-section {
  padding: 0 15px;                    
}

.product-title {
  font-size: 24px;                    
  font-weight: 700;
  color: #2c3e50;
  margin: 0 0 15px 0;                  
  line-height: 1.2;                   
}

.price-section {
  margin: 15px 0;                     
  padding: 12px 0;                      
  border-bottom: 1px solid #eee;
}

.current-price {
  font-size: 28px;                    
  font-weight: 700;
  color: #e74c3c;
  margin-right: 15px;
}

.product-stats {
  display: flex;
  gap: 25px;                            
  margin: 15px 0;                     
  padding: 12px 0;                    
  border-bottom: 1px solid #eee;
}

.description-section {
  margin: 15px 0;                      
  padding: 12px 0;                    
  border-bottom: 1px solid #eee;
}

.description-section h3 {
  margin: 0 0 8px 0;                  
  color: #2c3e50;
}

.purchase-section {
  margin: 20px 0;                     
}

.quantity-selector {
  display: flex;
  align-items: center;
  margin-bottom: 15px;              
}

.detail-tabs {
  margin-top: 20px;                 
}

.main-image {
  border-radius: 12px;
  overflow: hidden;
  background-color: #f8f9fa;
  max-width: 350px;                  
  margin: 0 auto;
}

.main-image img {
  width: 100%;
  height: auto;
  max-height: 350px;                  
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

.review-item:last-child {
  border-bottom: none;
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

.no-reviews {
  text-align: center;
  padding: 40px 0;
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