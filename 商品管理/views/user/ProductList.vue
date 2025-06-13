<template>
  <div class="product-list-container">
    <!-- 顶部搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="请输入商品名称"
        class="search-input"
        clearable
        @input="handleSearch"
      >
        <template #append>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
        </template>
      </el-input>
    </div>

    <!-- 商品列表 -->
    <div class="product-grid" v-loading="loading">
      <div
        v-for="product in displayProducts"
        :key="product.goodsId"
        class="product-card"
        @click="selectProduct(product)"
        :class="{ 'selected': selectedProduct?.goodsId === product.goodsId }"
      >
        <!-- 商品图片 -->
        <div class="product-image">
          <el-image
            :src="getImageUrl(product.imgUrl)"
            :alt="product.name"
            fit="cover"
          >
            <template #error>
              <div class="image-slot">
                <el-icon><Picture /></el-icon>
              </div>
            </template>
          </el-image>
        </div>

        <!-- 商品信息 -->
        <div class="product-info">
          <h3 class="product-name">{{ product.name }}</h3>
          <div class="product-desc">{{ product.desc || '暂无描述' }}</div>
          <div class="product-price">
            <span class="current-price">¥{{ product.price }}</span>
          </div>
          <div class="product-stats">
            <span class="category">分类: {{ product.categoryName || '未知' }}</span>
            <span class="stock">库存: {{ product.num }}</span>
          </div>
          <div class="product-meta">
            <span class="size">规格: {{ product.size || '标准' }}</span>
            <span class="storage">存储: {{ product.storagemethod || '未知' }}</span>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="product-actions">
          <el-button
            type="primary"
            size="small"
            @click.stop="addToCart(product)"
            :disabled="product.num <= 0"
          >
            <el-icon><ShoppingCart /></el-icon>
            加入购物车
          </el-button>
        </div>

        <!-- 商品状态标签 -->
        <div class="product-badges">
          <el-tag v-if="isNewProduct(product.creationdate)" type="success" size="small">新品</el-tag>
          <el-tag v-if="product.num > 0 && product.num <= 10" type="warning" size="small">
            库存紧张
          </el-tag>
        </div>
      </div>
    </div>

    <!-- 商品详情弹窗-->
    <el-dialog
      v-model="showProductDetail"
      :title="selectedProduct?.name || '商品详情'"
      width="70%"
      class="product-detail-dialog"
      :close-on-click-modal="false"
      destroy-on-close
      append-to-body
    >
      <ProductDetail 
        v-if="selectedProduct" 
        :product="formatProductForDetail(selectedProduct)" 
        @add-to-cart="addToCartFromDetail" 
        @buy-now="buyNowFromDetail"
      />
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="closeProductDetail">关闭</el-button>
          <el-button 
            type="primary" 
            @click="quickAddToCart"
            :disabled="!selectedProduct || (selectedProduct.num ?? 0) <= 0"
          >
            <el-icon><ShoppingCart /></el-icon>
            {{ selectedProduct && (selectedProduct.num ?? 0) > 0 ? '快速加入购物车' : '暂无库存' }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 空状态 -->
    <el-empty
      v-if="!loading && displayProducts.length === 0"
      description="暂无有库存的商品"
      :image-size="200"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Picture, ShoppingCart } from '@element-plus/icons-vue'
import ProductDetail from './ProductDetail.vue'
import axios from 'axios'

// 后端返回的商品数据结构（根据API文档调整）
interface BackendProduct {
  goodsId: number
  name: string
  desc: string
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
  imgUrl: string | null
}

// 转换后的商品数据结构（用于ProductDetail组件）
interface Product {
  id: number
  name: string
  price: number
  originalPrice?: number
  image?: string
  description?: string
  sales?: number
  stock: number
  isHot?: boolean
  isNew?: boolean
  category?: string
  specs?: Record<string, any>
}

const loading = ref(false)
const searchKeyword = ref('')
const products = ref<BackendProduct[]>([])
const selectedProduct = ref<BackendProduct | null>(null)
const showProductDetail = ref(false)

// 过滤后的商品列表（只显示有库存的商品）
const filteredProducts = computed(() => {
  // 首先过滤掉库存为0的商品
  const inStockProducts = products.value.filter(product => product.num > 0)
  
  // 如果有搜索关键词，再进行搜索过滤
  if (!searchKeyword.value) {
    return inStockProducts
  }
  
  return inStockProducts.filter(product =>
    product.name.toLowerCase().includes(searchKeyword.value.toLowerCase()) ||
    (product.desc && product.desc.toLowerCase().includes(searchKeyword.value.toLowerCase()))
  )
})

// 显示的商品列表（按库存数量排序，库存多的在前）
const displayProducts = computed(() => {
  return [...filteredProducts.value].sort((a, b) => {
    // 按库存数量从高到低排序
    if (a.num !== b.num) {
      return b.num - a.num
    }
    
    // 如果库存相同，按创建时间排序（新的在前）
    const timeA = new Date(a.addtime || a.creationdate || 0).getTime()
    const timeB = new Date(b.addtime || b.creationdate || 0).getTime()
    return timeB - timeA
  })
})

// 获取商品列表
const fetchProducts = async () => {
  try {
    loading.value = true
    const response = await axios.get('/api/api/goods')
    
    console.log('API返回的原始数据:', response.data)
    
    // 处理不同的数据结构
    let productData: any[] = []
    if (Array.isArray(response.data)) {
      productData = response.data
    } else if (response.data.data && Array.isArray(response.data.data)) {
      productData = response.data.data
    } else if (response.data.goods && Array.isArray(response.data.goods)) {
      productData = response.data.goods
    } else {
      console.warn('未知的数据格式:', response.data)
      productData = []
    }
    
    console.log('提取的商品数组:', productData)
    
    // 确保数据格式正确并添加调试信息
    products.value = productData.map((item: any, index: number) => {
      console.log(`处理第${index + 1}个商品:`, item)
      
      // 确保 num 字段有有效值
      let numValue = 0
      if (typeof item.num === 'number') {
        numValue = item.num
      } else if (typeof item.stock === 'number') {
        numValue = item.stock
      } else if (typeof item.num === 'string' && !isNaN(Number(item.num))) {
        numValue = Number(item.num)
      } else if (typeof item.stock === 'string' && !isNaN(Number(item.stock))) {
        numValue = Number(item.stock)
      }
      
      const processedProduct: BackendProduct = {
        goodsId: item.goodsId || item.id || 0,
        name: item.name || '未知商品',
        desc: item.desc || item.description || '',
        categoryName: item.categoryName || item.category || '未知分类',
        price: Number(item.price) || 0,
        num: numValue,
        kgs: Number(item.kgs) || 0,
        size: item.size || '标准',
        creationdate: item.creationdate || item.createTime || '',
        expirationdate: item.expirationdate || item.expireTime || '',
        storagemethod: item.storagemethod || item.storage || '常温',
        addtime: item.addtime || item.addTime || '',
        state: Number(item.state) >= 0 ? Number(item.state) : 1,
        imgUrl: item.imgUrl || item.image || null
      }
      
      console.log(`处理后的第${index + 1}个商品:`, processedProduct)
      return processedProduct
    })
    
    console.log('最终处理的商品数据:', products.value)
    console.log('商品总数量:', products.value.length)
    
    // 统计库存情况
    const stockStats = {
      total: products.value.length,
      inStock: products.value.filter(p => p.num > 0).length,
      outOfStock: products.value.filter(p => p.num <= 0).length,
      displayed: displayProducts.value.length
    }
    console.log('库存统计:', stockStats)
    
  } catch (error) {
    console.error('获取商品列表失败:', error)
    ElMessage.error('获取商品列表失败')
    products.value = []
  } finally {
    loading.value = false
  }
}

// 获取图片URL
const getImageUrl = (imgUrl: string | null): string => {
  if (!imgUrl) {
    return '/placeholder-image.png'
  }
  
  if (imgUrl.startsWith('http://') || imgUrl.startsWith('https://')) {
    return imgUrl
  }
  
  const baseUrl = 'http://localhost:8080'
  return `${baseUrl}${imgUrl.startsWith('/') ? '' : '/'}${imgUrl}`
}

// 判断是否为新品（7天内创建的商品）
const isNewProduct = (creationdate: string): boolean => {
  if (!creationdate) return false
  
  try {
    const createTime = new Date(creationdate).getTime()
    const now = Date.now()
    const sevenDays = 7 * 24 * 60 * 60 * 1000
    
    return (now - createTime) <= sevenDays
  } catch (error) {
    console.error('日期解析错误:', error)
    return false
  }
}

// 将后端商品数据转换为ProductDetail组件需要的格式
const formatProductForDetail = (backendProduct: BackendProduct): Product => {
  console.log('转换商品详情数据:', backendProduct)
  
  const formattedProduct: Product = {
    id: backendProduct.goodsId,
    name: backendProduct.name,
    price: backendProduct.price,
    image: getImageUrl(backendProduct.imgUrl),
    description: backendProduct.desc || '暂无商品详情',
    stock: backendProduct.num,
    category: backendProduct.categoryName,
    isNew: isNewProduct(backendProduct.creationdate),
    specs: {
      '规格': backendProduct.size || '标准',
      '重量': backendProduct.kgs ? `${backendProduct.kgs}kg` : '未知',
      '存储方式': backendProduct.storagemethod || '常温',
      '分类': backendProduct.categoryName || '未知',
      '生产日期': backendProduct.creationdate ? formatDate(backendProduct.creationdate) : '未知',
      '保质期至': backendProduct.expirationdate ? formatDate(backendProduct.expirationdate) : '未知',
      '商品状态': backendProduct.state === 1 ? '正常' : '下架'
    }
  }
  
  console.log('转换后的详情数据:', formattedProduct)
  return formattedProduct
}

// 格式化日期
const formatDate = (dateStr: string): string => {
  try {
    return new Date(dateStr).toLocaleDateString('zh-CN')
  } catch (error) {
    return dateStr
  }
}

// 搜索处理
const handleSearch = () => {
  console.log('搜索关键词:', searchKeyword.value)
  console.log('过滤后的商品数量:', filteredProducts.value.length)
  console.log('显示的商品数量:', displayProducts.value.length)
}

// 选择商品 - 弹出详情对话框
const selectProduct = (product: BackendProduct) => {
  console.log('选择商品:', product)
  console.log('商品库存:', product.num)
  
  selectedProduct.value = product
  showProductDetail.value = true
  
  console.log('已设置选中商品，准备显示详情弹窗')
}

// 关闭商品详情弹窗
const closeProductDetail = () => {
  showProductDetail.value = false
  selectedProduct.value = null
}

// 快速加入购物车（从弹窗底部按钮）
const quickAddToCart = async () => {
  if (selectedProduct.value) {
    await addToCart(selectedProduct.value)
    closeProductDetail()
  }
}

// 加入购物车
const addToCart = async (product: BackendProduct) => {
  if (product.num <= 0) {
    ElMessage.warning('商品暂无库存')
    return
  }

  try {
    // TODO: 调用加入购物车的API
    ElMessage.success(`${product.name} 已加入购物车`)
    
    // 更新本地库存显示
    const index = products.value.findIndex(p => p.goodsId === product.goodsId)
    if (index !== -1) {
      products.value[index].num = Math.max(0, products.value[index].num - 1)
      
      // 如果库存变为0，商品会自动从显示列表中消失（因为computed会重新计算）
      if (products.value[index].num === 0) {
        ElMessage.info(`${product.name} 库存已售完，已从商品列表中移除`)
      }
    }
  } catch (error) {
    console.error('加入购物车失败:', error)
    ElMessage.error('加入购物车失败')
  }
}

// 从详情页加入购物车
const addToCartFromDetail = async (product: Product, quantity: number = 1) => {
  const backendProduct = products.value.find(p => p.goodsId === product.id)
  if (!backendProduct) {
    ElMessage.error('商品信息错误')
    return
  }

  if (backendProduct.num < quantity) {
    ElMessage.warning('库存不足')
    return
  }

  try {
    ElMessage.success(`${backendProduct.name} 已加入购物车 (数量: ${quantity})`)
    
    // 更新本地库存显示
    const index = products.value.findIndex(p => p.goodsId === backendProduct.goodsId)
    if (index !== -1) {
      products.value[index].num = Math.max(0, products.value[index].num - quantity)
      
      // 如果库存变为0，关闭详情弹窗并提示
      if (products.value[index].num === 0) {
        closeProductDetail()
        ElMessage.info(`${backendProduct.name} 库存已售完，已从商品列表中移除`)
      }
    }
  } catch (error) {
    console.error('加入购物车失败:', error)
    ElMessage.error('加入购物车失败')
  }
}

// 从详情页立即购买
const buyNowFromDetail = async (product: Product, quantity: number = 1) => {
  const backendProduct = products.value.find(p => p.goodsId === product.id)
  if (!backendProduct) {
    ElMessage.error('商品信息错误')
    return
  }

  if (backendProduct.num < quantity) {
    ElMessage.warning('库存不足')
    return
  }

  try {
    ElMessage.success(`准备购买 ${backendProduct.name} (数量: ${quantity})`)
    closeProductDetail()
  } catch (error) {
    console.error('立即购买失败:', error)
    ElMessage.error('立即购买失败')
  }
}

onMounted(() => {
  fetchProducts()
})
</script>

<style scoped>
.product-list-container {
  padding: 0;
  width: 100%;
  height: 100%;
}

.search-bar {
  margin-bottom: 30px;
  padding: 0 20px;
}

.search-input {
  max-width: 400px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 40px;
  padding: 0 20px;
}

.product-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  transition: all 0.3s ease;
  cursor: pointer;
  position: relative;
}

.product-card:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.product-card.selected {
  border: 2px solid #ec4899;
  box-shadow: 0 4px 20px rgba(236, 72, 153, 0.2);
}

.product-image {
  height: 200px;
  overflow: hidden;
}

.product-image .el-image {
  width: 100%;
  height: 100%;
}

.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background: #f5f7fa;
  color: #909399;
  font-size: 30px;
}

.product-info {
  padding: 15px;
}

.product-name,
.product-desc {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-price {
  margin-bottom: 8px;
}

.current-price {
  font-size: 20px;
  font-weight: bold;
  color: #e74c3c;
}

.product-stats {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #666;
  margin-bottom: 8px;
}

.product-meta {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  color: #999;
}

.product-actions {
  padding: 15px;
  border-top: 1px solid #f0f0f0;
}

.product-actions .el-button {
  width: 100%;
}

.product-badges {
  position: absolute;
  top: 10px;
  left: 10px;
  display: flex;
  flex-direction: column;
  gap: 5px;
}

/* 商品详情弹窗样式 */
:deep(.product-detail-dialog) {
  .el-dialog {
    margin-top: 5vh !important;
    margin-bottom: 5vh !important;
    height: 60vh;
    max-height: 60vh;
    display: flex;
    flex-direction: column;
  }

  .el-dialog__header {
    padding: 15px 20px 10px 20px;
    border-bottom: 1px solid #ebeef5;
    flex-shrink: 0;
  }

  .el-dialog__body {
    flex: 1;
    padding: 0;
    overflow: hidden;
  }

  .el-dialog__footer {
    padding: 12px 20px;
    border-top: 1px solid #ebeef5;
    background-color: #f8f9fa;
    flex-shrink: 0;
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .product-grid {
    grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  }
  
  :deep(.product-detail-dialog .el-dialog) {
    width: 70% !important;
  }
}

@media (max-width: 768px) {
  .product-grid {
    grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
    gap: 15px;
    padding: 0 15px;
  }
  
  .search-bar {
    padding: 0 15px;
  }
  
  :deep(.product-detail-dialog .el-dialog) {
    width: 85% !important;
    margin-top: 3vh !important;
    margin-bottom: 3vh !important;
    height: 70vh;
    max-height: 70vh;
  }
  
  .dialog-footer {
    flex-direction: column;
  }
  
  .dialog-footer .el-button {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .product-grid {
    grid-template-columns: 1fr;
    padding: 0 10px;
  }
  
  .search-input {
    max-width: 100%;
  }
  
  .search-bar {
    padding: 0 10px;
  }
  
  :deep(.product-detail-dialog .el-dialog) {
    width: 95% !important;
    margin: 2vh !important;
    height: 80vh !important;
    max-height: 80vh !important;
    border-radius: 8px !important;
  }
}
</style>