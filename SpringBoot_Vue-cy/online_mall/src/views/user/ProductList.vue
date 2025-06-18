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
            :disabled="product.num <= 0 || !isUserLoggedIn"
          >
            <el-icon><ShoppingCart /></el-icon>
            {{ isUserLoggedIn ? '加入购物车' : '请先登录' }}
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

    <!-- 商品详情弹窗 -->
    <el-dialog
      v-model="showProductDetail"
      :title="selectedProduct?.name || '商品详情'"
      width="50%"
      class="product-detail-dialog"
      :close-on-click-modal="false"
      destroy-on-close
      append-to-body
    >
      <div v-loading="detailLoading">
        <ProductDetail 
          v-if="productDetailData" 
          :product-data="productDetailData" 
          @add-to-cart="addToCartFromDetail" 
          @buy-now="buyNowFromDetail"
        />
        <div v-else-if="!detailLoading" class="detail-error">
          <el-empty description="获取商品详情失败" :image-size="100" />
        </div>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="closeProductDetail">关闭</el-button>
          <el-button 
            type="primary" 
            @click="quickAddToCart"
            :disabled="!selectedProduct || (selectedProduct.num ?? 0) <= 0 || detailLoading || !isUserLoggedIn"
          >
            <el-icon><ShoppingCart /></el-icon>
            {{ !isUserLoggedIn ? '请先登录' : 
               selectedProduct && (selectedProduct.num ?? 0) > 0 ? '快速加入购物车' : '暂无库存' }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 空状态 -->
    <el-empty
      v-if="!loading && displayProducts.length === 0"
      description="暂无符合条件的商品"
      :image-size="200"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, Picture, ShoppingCart } from '@element-plus/icons-vue'
import ProductDetail from './ProductDetail.vue'
import axios from 'axios'

// 商品列表接口返回的商品数据结构
interface BackendProduct {
  goodsId: number  // 自增ID
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

// 商品详情接口返回的数据结构
interface Comment {
  commentId: number
  userId: number
  goodsId: number  // 自增ID
  rating: number
  content: string
  createTime: string
  userName: string
  goodsName: string
}

interface Goods {
  goodsId: number  // 自增ID
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

const router = useRouter()

// 响应式数据
const loading = ref(false)
const detailLoading = ref(false)
const searchKeyword = ref('')
const products = ref<BackendProduct[]>([])
const selectedProduct = ref<BackendProduct | null>(null)
const showProductDetail = ref(false)
const productDetailData = ref<ProductDetailData | null>(null)

// 当前用户ID - 从登录状态获取
const currentUserId = computed(() => {
  try {
    const userInfo = sessionStorage.getItem('Atuserinfo')
    if (userInfo) {
      const user = JSON.parse(userInfo)
      return user.userId 
    }
    return null
  } catch (error) {
    console.error('获取用户ID失败:', error)
    return null
  }
})

// 检查用户是否已登录
const isUserLoggedIn = computed(() => {
  return currentUserId.value !== null && currentUserId.value !== undefined
})

// 检查登录状态的通用方法
const checkLoginStatus = (): boolean => {
  if (!isUserLoggedIn.value) {
    ElMessage.warning('请先登录后再进行此操作')
    return false
  }
  return true
}

// 过滤后的商品列表（只显示上架且有库存的商品）
const filteredProducts = computed(() => {
  // 首先过滤：1. 只显示上架商品(state === 1) 2. 只显示有库存的商品(num > 0)
  const availableProducts = products.value.filter(product => 
    product.state === 1 && product.num > 0
  )
  
  // 如果有搜索关键词，再进行搜索过滤
  if (!searchKeyword.value) {
    return availableProducts
  }
  
  return availableProducts.filter(product =>
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
    
    console.log('商品列表API返回的原始数据:', response.data)
    
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
    
    // 确保数据格式正确并添加调试信息
    products.value = productData.map((item: any) => {
      // 确保 goodsId 是有效的数字
      const goodsId = Number(item.goodsId || item.id)
      if (isNaN(goodsId) || goodsId <= 0) {
        console.warn('无效的商品ID:', item.goodsId || item.id)
        return null
      }
      
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
      
      // 确保 state 字段有有效值，默认为上架状态
      let stateValue = 1
      if (typeof item.state === 'number') {
        stateValue = item.state
      } else if (typeof item.state === 'string' && !isNaN(Number(item.state))) {
        stateValue = Number(item.state)
      }
      
      const processedProduct: BackendProduct = {
        goodsId: goodsId,
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
        state: stateValue,
        imgUrl: item.imgUrl || item.image || null
      }
      
      return processedProduct
    }).filter((item): item is BackendProduct => item !== null) // 过滤掉无效的商品数据
    
    console.log('最终处理的商品数据:', products.value)
    
  } catch (error) {
    console.error('获取商品列表失败:', error)
    ElMessage.error('获取商品列表失败')
    products.value = []
  } finally {
    loading.value = false
  }
}

// 获取商品详情
const fetchProductDetail = async (goodsId: number) => {
  try {
    detailLoading.value = true
    const response = await axios.get(`/api/api/comments/goods/${goodsId}`, {
      params: {
        page: 1,
        size: 10
      }
    })
    
    console.log('商品详情API返回的数据:', response.data)
    
    // 检查返回的数据结构
    if (response.data && response.data.goods && response.data.comments) {
      productDetailData.value = response.data
    } else {
      console.error('商品详情数据格式错误:', response.data)
      productDetailData.value = null
      ElMessage.error('商品详情数据格式错误')
    }
    
  } catch (error) {
    console.error('获取商品详情失败:', error)
    ElMessage.error('获取商品详情失败')
    productDetailData.value = null
  } finally {
    detailLoading.value = false
  }
}

// 获取图片URL
const getImageUrl = (imgUrl: string | null): string => {
  if (!imgUrl) return '/default-product.png'
  // 如果是完整URL直接返回
  if (imgUrl.startsWith('http')) return imgUrl
  // 如果是以/开头的路径，直接返回
  if (imgUrl.startsWith('/')) return imgUrl
  // 否则拼接API基础路径
  return `/api/uploads/${imgUrl}`
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
const selectProduct = async (product: BackendProduct) => {
  console.log('选择商品:', product)
  console.log('商品库存:', product.num)
  console.log('商品状态:', product.state === 1 ? '上架' : '下架')
  
  selectedProduct.value = product
  showProductDetail.value = true
  productDetailData.value = null
  
  // 获取商品详情数据
  await fetchProductDetail(product.goodsId)
  
  console.log('已设置选中商品，准备显示详情弹窗')
}

// 关闭商品详情弹窗
const closeProductDetail = () => {
  showProductDetail.value = false
  selectedProduct.value = null
  productDetailData.value = null
}

// 快速添加到购物车
const quickAddToCart = async () => {
  if (!selectedProduct.value) return
  
  // 检查登录状态
  if (!checkLoginStatus()) return

  try {
    const response = await axios.post('/api/api/cart/add', {
      userId: currentUserId.value,
      goodsId: selectedProduct.value.goodsId,
      quantity: 1
    })

    if (response.data.success) {
      ElMessage.success(`${selectedProduct.value.name} 已加入购物车`)
      // 更新本地库存显示
      const index = products.value.findIndex(p => p.goodsId === selectedProduct.value?.goodsId)
      if (index !== -1) {
        products.value[index].num = Math.max(0, products.value[index].num - 1)
        if (productDetailData.value && productDetailData.value.goods.goodsId === selectedProduct.value.goodsId) {
          productDetailData.value.goods.num = products.value[index].num
        }
        if (products.value[index].num === 0) {
          closeProductDetail()
          ElMessage.info(`${selectedProduct.value.name} 库存已售完，已从商品列表中移除`)
        }
      }
    } else {
      throw new Error(response.data.message)
    }
  } catch (error: any) {
    console.error('加入购物车失败:', error)
    const errorMsg = error.response?.data?.message || error.message || '加入购物车失败'
    ElMessage.error(errorMsg)
  }
}

// 加入购物车
const addToCart = async (product: BackendProduct) => {
  // 检查登录状态
  if (!checkLoginStatus()) return
  
  if (!product.goodsId || product.goodsId <= 0) {
    ElMessage.error('商品ID无效')
    return
  }

  if (product.num <= 0) {
    ElMessage.warning('商品暂无库存')
    return
  }

  if (product.state !== 1) {
    ElMessage.warning('商品已下架')
    return
  }

  try {
    const response = await axios.post('/api/api/cart/add', {
      userId: currentUserId.value,
      goodsId: product.goodsId,
      quantity: 1
    })

    if (response.data.success) {
      ElMessage.success(`${product.name} 已加入购物车`)
      // 更新本地库存显示
      const index = products.value.findIndex(p => p.goodsId === product.goodsId)
      if (index !== -1) {
        products.value[index].num = Math.max(0, products.value[index].num - 1)
        if (productDetailData.value && productDetailData.value.goods.goodsId === product.goodsId) {
          productDetailData.value.goods.num = products.value[index].num
        }
        if (products.value[index].num === 0) {
          ElMessage.info(`${product.name} 库存已售完，已从商品列表中移除`)
        }
      }
    } else {
      throw new Error(response.data.message)
    }
  } catch (error: any) {
    console.error('加入购物车失败:', error)
    const errorMsg = error.response?.data?.message || error.message || '加入购物车失败'
    ElMessage.error(errorMsg)
  }
}

// 从详情页加入购物车
const addToCartFromDetail = async (goods: Goods, quantity: number = 1) => {
  // 检查登录状态
  if (!checkLoginStatus()) return
  
  if (!goods.goodsId || goods.goodsId <= 0) {
    ElMessage.error('商品ID无效')
    return
  }

  const backendProduct = products.value.find(p => p.goodsId === goods.goodsId)
  if (!backendProduct) {
    ElMessage.error('商品信息错误')
    return
  }

  if (backendProduct.state !== 1) {
    ElMessage.warning('商品已下架')
    return
  }

  if (backendProduct.num < quantity) {
    ElMessage.warning('库存不足')
    return
  }

  try {
    const response = await axios.post('/api/api/cart/add', {
      userId: currentUserId.value,
      goodsId: goods.goodsId,
      quantity: quantity
    })

    if (response.data.success) {
      ElMessage.success(`${backendProduct.name} 已加入购物车 (数量: ${quantity})`)
      // 更新本地库存显示
      const index = products.value.findIndex(p => p.goodsId === backendProduct.goodsId)
      if (index !== -1) {
        products.value[index].num = Math.max(0, products.value[index].num - quantity)
        if (productDetailData.value && productDetailData.value.goods.goodsId === goods.goodsId) {
          productDetailData.value.goods.num = products.value[index].num
        }
        if (products.value[index].num === 0) {
          closeProductDetail()
          ElMessage.info(`${backendProduct.name} 库存已售完，已从商品列表中移除`)
        }
      }
    } else {
      throw new Error(response.data.message)
    }
  } catch (error: any) {
    console.error('加入购物车失败:', error)
    const errorMsg = error.response?.data?.message || error.message || '加入购物车失败'
    ElMessage.error(errorMsg)
  }
}

// 从详情页立即购买
const buyNowFromDetail = async (goods: Goods, quantity: number = 1) => {
  // 检查登录状态
  if (!checkLoginStatus()) return
  
  if (!goods.goodsId || goods.goodsId <= 0) {
    ElMessage.error('商品ID无效')
    return
  }

  const backendProduct = products.value.find(p => p.goodsId === goods.goodsId)
  if (!backendProduct) {
    ElMessage.error('商品信息错误')
    return
  }

  if (backendProduct.state !== 1) {
    ElMessage.warning('商品已下架')
    return
  }

  if (backendProduct.num < quantity) {
    ElMessage.warning('库存不足')
    return
  }

  try {
    // 先将商品加入购物车
    const response = await axios.post('/api/api/cart/add', {
      userId: currentUserId.value,
      goodsId: goods.goodsId,
      quantity: quantity
    })

    if (response.data.success) {
      ElMessage.success(`正在为您结算 ${backendProduct.name}`)
      closeProductDetail()
      
      // 跳转到购物车页面，并传递立即购买的参数
      router.push({
        path: '/user/cart',
        query: {
          buyNow: '1',  // 标记为立即购买
          goodsId: goods.goodsId.toString(),  // 商品ID
          quantity: quantity.toString(),  // 购买数量
          autoCheckout: '1'  // 自动打开结算弹窗
        }
      })
    } else {
      throw new Error(response.data.message)
    }
  } catch (error: any) {
    console.error('立即购买失败:', error)
    const errorMsg = error.response?.data?.message || error.message || '立即购买失败'
    ElMessage.error(errorMsg)
  }
}

// 组件挂载时获取商品列表
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

.product-name {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 8px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-desc {
  font-size: 14px;
  color: #7f8c8d;
  margin-bottom: 10px;
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

.detail-error {
  padding: 40px 0;
  text-align: center;
}

/* 商品详情弹窗样式 */
:deep(.product-detail-dialog) {
  .el-dialog {
    margin-top: 8vh !important;       
    margin-bottom: 8vh !important;    
    height: 30vh;                   
    display: flex;
    flex-direction: column;
  }

  .el-dialog__header {
    padding: 12px 15px 8px 15px;     
    border-bottom: 1px solid #ebeef5;
    flex-shrink: 0;
  }

  .el-dialog__body {
    flex: 1;
    padding: 0;
    overflow: hidden;
  }

  .el-dialog__footer {
    padding: 10px 15px;               
    border-top: 1px solid #ebeef5;
    background-color: #f8f9fa;
    flex-shrink: 0;
  }
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .product-grid {
    grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  }
  
  :deep(.product-detail-dialog .el-dialog) {
    width: 60% !important;             
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
    width: 75% !important;             
    margin-top: 5vh !important;       
    margin-bottom: 5vh !important;     
    height: 55vh;                     
    max-height: 55vh;             
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
    width: 90% !important;       
    margin: 3vh !important;       
    height: 65vh !important;          
    max-height: 65vh !important;      
    border-radius: 8px !important;
  }
}
</style>