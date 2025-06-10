<template>
  <div class="product-list-container">
    <!-- 头部搜索区域 -->
    <div class="search-header">
      <h2>商品列表</h2>
      <div class="search-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="请输入商品名称"
          size="large"
          clearable
          @input="handleSearch"
          style="width: 300px"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>
    </div>

    <!-- 商品卡片列表 -->
    <div class="product-grid">
      <div
        v-for="product in filteredProducts"
        :key="product.id"
        class="product-card"
        @click="showProductDetail(product)"
      >
        <div class="product-image">
          <img :src="product.image || '/placeholder-product.png'" :alt="product.name" />
          <div class="product-overlay">
            <el-button type="primary" round size="small">查看详情</el-button>
          </div>
        </div>
        <div class="product-info">
          <h3 class="product-name">{{ product.name }}</h3>
          <div class="product-price">
            <span class="current-price">¥{{ product.price }}</span>
            <span v-if="product.originalPrice" class="original-price">¥{{ product.originalPrice }}</span>
          </div>
          <div class="product-stats">
            <span>库存: {{ product.stock }}</span>
            <span>销量: {{ product.sales || 0 }}</span>
          </div>
          <div class="product-actions">
            <el-button 
              type="primary" 
              size="small" 
              :icon="ShoppingCart"
              @click.stop="addToCart(product)"
              :disabled="product.stock === 0"
            >
              {{ product.stock === 0 ? '缺货' : '加入购物车' }}
            </el-button>
            <el-button 
              type="warning" 
              size="small"
              @click.stop="buyNow(product)"
              :disabled="product.stock === 0"
            >
              立即购买
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 商品详情抽屉 -->
    <el-drawer
      v-model="showDetail"
      title="商品详情"
      direction="btt"
      size="50%"
      :with-header="true"
    >
      <ProductDetail 
        v-if="selectedProduct"
        :product="selectedProduct"
        @add-to-cart="addToCart"
        @buy-now="buyNow"
      />
    </el-drawer>

    <!-- 空状态 -->
    <el-empty 
      v-if="filteredProducts.length === 0"
      description="暂无商品"
      :image-size="200"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, ShoppingCart } from '@element-plus/icons-vue'
import ProductDetail from './ProductDetail.vue'

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

const products = ref<Product[]>([])
const searchKeyword = ref('')
const showDetail = ref(false)
const selectedProduct = ref<Product | null>(null)

// 模拟商品数据 (你可以从后端API获取)
const mockProducts: Product[] = [
  {
    id: 1,
    name: '1111',
    price: 0,
    originalPrice: 10,
    stock: 894,
    sales: 50,
    description: '美味的糖果，口感丰富',
    image: '/placeholder-product.png'
  },
  {
    id: 2,
    name: '9999',
    price: 0,
    originalPrice: 15,
    stock: 0,
    sales: 30,
    description: '经典口味糖果',
    image: '/placeholder-product.png'
  },
  {
    id: 3,
    name: '红色的糖果',
    price: 56,
    originalPrice: 80,
    stock: 184,
    sales: 120,
    description: '红色经典糖果，甜而不腻',
    image: '/placeholder-product.png'
  },
  {
    id: 4,
    name: '绿的糖果',
    price: 67,
    originalPrice: 90,
    stock: 193,
    sales: 88,
    description: '清新绿色糖果',
    image: '/placeholder-product.png'
  },
  {
    id: 5,
    name: '黄色的糖果',
    price: 80,
    originalPrice: 100,
    stock: 198,
    sales: 75,
    description: '阳光黄色糖果',
    image: '/placeholder-product.png'
  },
  {
    id: 6,
    name: '绿色的糖果',
    price: 70,
    originalPrice: 85,
    stock: 196,
    sales: 95,
    description: '天然绿色糖果',
    image: '/placeholder-product.png'
  },
  {
    id: 7,
    name: '紫色的糖果',
    price: 33,
    originalPrice: 50,
    stock: 200,
    sales: 60,
    description: '神秘紫色糖果',
    image: '/placeholder-product.png'
  },
  {
    id: 8,
    name: '黄色的糖果',
    price: 97,
    originalPrice: 120,
    stock: 200,
    sales: 110,
    description: '高端黄色糖果',
    image: '/placeholder-product.png'
  }
]

// 计算过滤后的商品
const filteredProducts = computed(() => {
  if (!searchKeyword.value.trim()) {
    return products.value
  }
  return products.value.filter(product =>
    product.name.toLowerCase().includes(searchKeyword.value.toLowerCase())
  )
})

// 初始化商品数据
onMounted(async () => {
  await fetchProducts()
})

// 获取商品数据
const fetchProducts = async () => {
  try {
    // 这里可以调用真实的API
    // const response = await axios.get('/api/products')
    // products.value = response.data
    
    // 现在使用模拟数据
    products.value = mockProducts
  } catch (error) {
    console.error('获取商品列表失败:', error)
    ElMessage.error('获取商品列表失败')
    // 如果API失败，使用模拟数据
    products.value = mockProducts
  }
}

// 搜索处理
const handleSearch = () => {
  // 搜索逻辑已在计算属性中处理
}

// 显示商品详情
const showProductDetail = (product: Product) => {
  selectedProduct.value = product
  showDetail.value = true
}

// 加入购物车
const addToCart = (product: Product) => {
  if (product.stock === 0) {
    ElMessage.warning('商品库存不足')
    return
  }
  
  // 这里可以调用加入购物车的API
  console.log('加入购物车:', product)
  ElMessage.success(`${product.name} 已加入购物车`)
}

// 立即购买
const buyNow = (product: Product) => {
  if (product.stock === 0) {
    ElMessage.warning('商品库存不足')
    return
  }
  
  // 这里可以跳转到订单页面或调用购买API
  console.log('立即购买:', product)
  ElMessage.success(`正在为您购买 ${product.name}`)
}
</script>

<style scoped>
.product-list-container {
  padding: 20px;
  min-height: 100%;
  background-color: #f5f7fa;
}

.search-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.search-header h2 {
  margin: 0;
  color: #2c3e50;
  font-size: 24px;
  font-weight: 600;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  padding: 10px;
}

.product-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  cursor: pointer;
  position: relative;
}

.product-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.product-image {
  position: relative;
  height: 200px;
  overflow: hidden;
  background-color: #f8f9fa;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.product-card:hover .product-image img {
  transform: scale(1.05);
}

.product-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.product-card:hover .product-overlay {
  opacity: 1;
}

.product-info {
  padding: 20px;
}

.product-name {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 10px 0;
  line-height: 1.4;
}

.product-price {
  margin: 10px 0;
}

.current-price {
  font-size: 22px;
  font-weight: 700;
  color: #e74c3c;
  margin-right: 10px;
}

.original-price {
  font-size: 16px;
  color: #95a5a6;
  text-decoration: line-through;
}

.product-stats {
  display: flex;
  justify-content: space-between;
  margin: 15px 0;
  font-size: 14px;
  color: #7f8c8d;
}

.product-actions {
  display: flex;
  gap: 10px;
  margin-top: 15px;
}

.product-actions .el-button {
  flex: 1;
  font-weight: 500;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .search-header {
    flex-direction: column;
    gap: 15px;
    text-align: center;
  }
  
  .product-grid {
    grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
    gap: 15px;
  }
  
  .product-actions {
    flex-direction: column;
  }
}
</style>