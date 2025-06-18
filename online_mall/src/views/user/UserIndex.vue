<template>
  <div class="user-index">
    <!-- 顶部文字框 -->
    <div class="top-banner">
      <div class="banner-content">
        <el-icon><ShoppingCart /></el-icon>
        <span class="banner-text">欢迎来到网上商城，祝您购物愉快！</span>
        <el-icon><Star /></el-icon>
      </div>
      <div class="user-info">
        <span class="display-username">{{ currentUsername }}</span>
        <el-popover 
          placement="bottom-end" 
          trigger="click" 
          :offset="12" 
          :popper-class="'user-profile-popper'"
          :width="320"
        >
          <template #default>
            <div class="user-profile">
              <!-- 用户卡片头部 -->
              <div class="profile-header">
                <div class="profile-avatar-container">
                  <img :src="defaultAvatar" alt="用户头像" class="profile-avatar" />
                  <div class="avatar-status"></div>
                </div>
                <div class="profile-basic">
                  <h3 class="profile-username">{{ currentUsername }}</h3>
                </div>
              </div>

              <!-- 用户详细信息 -->
              <div class="profile-details">
                <div class="detail-item">
                  <div class="detail-icon">
                    <el-icon><Avatar /></el-icon>
                  </div>
                  <div class="detail-content">
                    <span class="detail-label">性别</span>
                    <span class="detail-value">{{ currentUserInfo.gender || '未设置' }}</span>
                  </div>
                </div>

                <div class="detail-item">
                  <div class="detail-icon">
                    <el-icon><Phone /></el-icon>
                  </div>
                  <div class="detail-content">
                    <span class="detail-label">手机号</span>
                    <span class="detail-value">{{ currentUserInfo.telephone || '未绑定' }}</span>
                  </div>
                </div>

                <div class="detail-item" v-if="currentUserInfo.intro">
                  <div class="detail-icon">
                    <el-icon><Document /></el-icon>
                  </div>
                  <div class="detail-content">
                    <span class="detail-label">个人简介</span>
                    <span class="detail-value">{{ currentUserInfo.intro }}</span>
                  </div>
                </div>

                <div class="detail-item" v-if="currentUserInfo.address">
                  <div class="detail-icon">
                    <el-icon><Location /></el-icon>
                  </div>
                  <div class="detail-content">
                    <span class="detail-label">地址</span>
                    <span class="detail-value">{{ currentUserInfo.address }}</span>
                  </div>
                </div>
              </div>

              <!-- 操作按钮区域 -->
              <div class="profile-actions">
                <el-button 
                  @click="editUserInfo" 
                  type="primary" 
                  class="action-btn edit-btn"
                  :icon="Edit"
                >
                  编辑资料
                </el-button>
                <el-button 
                  @click="logout" 
                  class="action-btn logout-btn"
                  :icon="SwitchButton"
                >
                  退出登录
                </el-button>
              </div>
            </div>
          </template>
          <template #reference>
            <div class="user-avatar-wrapper">
              <img :src="defaultAvatar" alt="用户头像" class="user-avatar" />
              <div class="avatar-badge"></div>
            </div>
          </template>
        </el-popover>
      </div>
    </div>
    
    <!-- 轮播图区域 -->
    <div class="carousel-container">
      <el-carousel height="350px" indicator-position="outside" arrow="always">
        <el-carousel-item v-for="(item, index) in carouselImages" :key="index">
          <div class="carousel-item-wrapper">
            <img :src="item.src" alt="轮播图" class="carousel-image" />
            <div class="carousel-caption">{{ item.title }}</div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </div>
    
    <!-- 推荐商品展示区域 -->
    <div class="hot-products-container">
      <div class="section-title">
        <el-icon class="section-icon"><ShoppingBag /></el-icon>
        <span>为您推荐</span>
      </div>
      <div class="product-grid" v-loading="productsLoading">
        <div class="product-item" v-for="product in recommendedProducts" :key="product.goodsId" @click="viewProduct(product)">
          <div class="product-image-container">
            <img :src="getProductImage(product.imgUrl)" :alt="product.name" class="product-image" />
            <div class="product-overlay">
              <span class="view-btn">查看详情</span>
            </div>
          </div>
          <div class="product-info">
            <div class="product-name">{{ product.name }}</div>
            <div class="product-price">¥{{ product.price.toFixed(2) }}</div>
            <div class="product-stats">
              <span class="sales">销量: {{ product.sales || 0 }}</span>
              <span class="stock" :class="{ 'low-stock': product.num < 10 }">
                库存: {{ product.num }}
              </span>
            </div>
          </div>
        </div>
        
        <!-- 当没有推荐商品时显示的提示 -->
        <div v-if="!productsLoading && recommendedProducts.length === 0" class="no-recommendations">
          <el-empty description="暂无推荐商品" :image-size="100">
            <template #description>
              <p>系统正在为您分析偏好</p>
              <p>请先浏览和评价一些商品</p>
            </template>
          </el-empty>
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

    <!-- 编辑用户信息对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      title="" 
      width="600px"
      :show-close="false"
      class="edit-dialog"
      append-to-body
    >
      <!-- 自定义弹窗头部 -->
      <div class="dialog-header">
        <div class="header-content">
          <div class="header-icon">
            <el-icon><User /></el-icon>
          </div>
          <div class="header-text">
            <h3>编辑个人信息</h3>
            <p>完善您的个人资料</p>
          </div>
        </div>
        <el-button 
          type="text" 
          @click="dialogVisible = false"
          class="close-btn"
        >
          <el-icon><Close /></el-icon>
        </el-button>
      </div>

      <!-- 表单内容 - 使用两列布局 -->
      <div class="dialog-body">
        <el-form 
          :model="editForm" 
          label-width="0"
          class="edit-form"
          ref="editFormRef"
          :rules="formRules"
        >
          <!-- 第一行：用户名和密码 -->
          <div class="form-row">
            <div class="form-group">
              <div class="form-label">
                <el-icon><User /></el-icon>
                <span>用户名</span>
              </div>
              <el-form-item prop="username">
                <el-input 
                  v-model="editForm.username" 
                  placeholder="请输入用户名"
                  size="large"
                />
              </el-form-item>
            </div>

            <div class="form-group">
              <div class="form-label">
                <el-icon><Lock /></el-icon>
                <span>密码</span>
              </div>
              <el-form-item prop="password">
                <el-input 
                  v-model="editForm.password" 
                  type="password" 
                  show-password 
                  placeholder="请输入密码"
                  size="large"
                />
              </el-form-item>
            </div>
          </div>

          <!-- 第二行：性别和手机号 -->
          <div class="form-row">
            <div class="form-group">
              <div class="form-label">
                <el-icon><Avatar /></el-icon>
                <span>性别</span>
              </div>
              <el-form-item prop="gender">
                <el-radio-group v-model="editForm.gender" class="gender-group-inline">
                  <el-radio value="男" class="gender-radio-inline">
                    <span class="radio-content-inline">
                      <el-icon><Male /></el-icon>
                      <span>男</span>
                    </span>
                  </el-radio>
                  <el-radio value="女" class="gender-radio-inline">
                    <span class="radio-content-inline">
                      <el-icon><Female /></el-icon>
                      <span>女</span>
                    </span>
                  </el-radio>
                </el-radio-group>
              </el-form-item>
            </div>

            <div class="form-group">
              <div class="form-label">
                <el-icon><Phone /></el-icon>
                <span>手机号</span>
              </div>
              <el-form-item prop="telephone">
                <el-input 
                  v-model="editForm.telephone" 
                  placeholder="请输入手机号"
                  size="large"
                />
              </el-form-item>
            </div>
          </div>

          <!-- 第三行：个人简介 -->
          <div class="form-row form-row-full">
            <div class="form-group form-group-full">
              <div class="form-label">
                <el-icon><Document /></el-icon>
                <span>个人简介</span>
              </div>
              <el-form-item prop="intro">
                <el-input 
                  v-model="editForm.intro" 
                  type="textarea" 
                  :rows="2" 
                  placeholder="请输入个人简介"
                  resize="none"
                />
              </el-form-item>
            </div>
          </div>

          <!-- 第四行：地址 -->
          <div class="form-row form-row-full">
            <div class="form-group form-group-full">
              <div class="form-label">
                <el-icon><Location /></el-icon>
                <span>联系地址</span>
              </div>
              <el-form-item prop="address">
                <el-input 
                  v-model="editForm.address" 
                  type="textarea" 
                  :rows="2" 
                  placeholder="请输入联系地址"
                  resize="none"
                />
              </el-form-item>
            </div>
          </div>
        </el-form>
      </div>

      <!-- 自定义底部按钮 -->
      <div class="dialog-footer">
        <el-button @click="dialogVisible = false" size="large" class="cancel-btn">
          <el-icon><Close /></el-icon>
          取消
        </el-button>
        <el-button type="primary" @click="saveUserInfo" size="large" class="save-btn">
          <el-icon><Check /></el-icon>
          保存修改
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, ref, reactive, onMounted } from 'vue'
import { 
  ShoppingCart, 
  Star, 
  User, 
  Lock, 
  Avatar, 
  Phone, 
  Document, 
  Location, 
  Close, 
  Check,
  Male,
  Female,
  Edit,
  SwitchButton,
  ShoppingBag
} from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import ProductDetail from './ProductDetail.vue'

// 响应式用户信息
const currentUserInfo = ref(JSON.parse(sessionStorage.getItem('Atuserinfo') || '{}'))
const currentUsername = computed(() => currentUserInfo.value.username || '用户')

// 默认头像路径
const defaultAvatar = computed(() => {
  return '/public/defaultPic.png'
})

const router = useRouter()

// 控制弹窗显示/隐藏
const dialogVisible = ref(false)

// 推荐商品相关数据
const recommendedProducts = ref([])
const productsLoading = ref(false)

// 商品详情相关数据
const showProductDetail = ref(false)
const selectedProduct = ref(null)
const productDetailData = ref(null)
const detailLoading = ref(false)

// 当前用户ID
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
const checkLoginStatus = () => {
  if (!isUserLoggedIn.value) {
    ElMessage.warning('请先登录后再进行此操作')
    return false
  }
  return true
}

// 编辑表单数据
const editForm = reactive({
  username: '',
  password: '',
  gender: '',
  telephone: '',
  intro: '',
  address: '' 
})

// 表单验证规则
const formRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  telephone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

// 轮播图数据
const carouselImages = ref([
  { src: '/public/p1.png' },
  { src: '/public/p2.png' },
  { src: '/public/p3.png'}
])

// 获取推荐商品
const loadRecommendedProducts = async () => {
  if (!currentUserInfo.value.userId) {
    console.warn('用户未登录，无法获取推荐商品')
    return
  }

  try {
    productsLoading.value = true
    const response = await axios.get(`/api/api/recommend/user/${currentUserInfo.value.userId}`, {
      params: {
        topN: 8 // 获取8个推荐商品
      },
      withCredentials: true
    })
    
    if (response.status === 200 && response.data) {
      recommendedProducts.value = response.data
      console.log('推荐商品加载成功:', response.data)
    } else {
      recommendedProducts.value = []
    }
  } catch (error) {
    console.error('获取推荐商品失败:', error)
    recommendedProducts.value = []
  } finally {
    productsLoading.value = false
  }
}

// 获取商品详情
const fetchProductDetail = async (goodsId) => {
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

// 获取商品图片URL
const getProductImage = (imgUrl) => {
  if (!imgUrl) {
    return '/public/defaultPic.png'
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

// 查看商品详情 - 修改为弹出详情对话框
const viewProduct = async (product) => {
  console.log('查看商品详情:', product)
  
  selectedProduct.value = product
  showProductDetail.value = true
  productDetailData.value = null
  
  // 获取商品详情数据
  await fetchProductDetail(product.goodsId)
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
      const index = recommendedProducts.value.findIndex(p => p.goodsId === selectedProduct.value?.goodsId)
      if (index !== -1) {
        recommendedProducts.value[index].num = Math.max(0, recommendedProducts.value[index].num - 1)
        if (productDetailData.value && productDetailData.value.goods.goodsId === selectedProduct.value.goodsId) {
          productDetailData.value.goods.num = recommendedProducts.value[index].num
        }
        if (recommendedProducts.value[index].num === 0) {
          closeProductDetail()
          ElMessage.info(`${selectedProduct.value.name} 库存已售完`)
        }
      }
    } else {
      throw new Error(response.data.message)
    }
  } catch (error) {
    console.error('加入购物车失败:', error)
    const errorMsg = error.response?.data?.message || error.message || '加入购物车失败'
    ElMessage.error(errorMsg)
  }
}

// 从详情页加入购物车
const addToCartFromDetail = async (goods, quantity = 1) => {
  // 检查登录状态
  if (!checkLoginStatus()) return
  
  if (!goods.goodsId || goods.goodsId <= 0) {
    ElMessage.error('商品ID无效')
    return
  }

  const recommendedProduct = recommendedProducts.value.find(p => p.goodsId === goods.goodsId)
  if (!recommendedProduct) {
    ElMessage.error('商品信息错误')
    return
  }

  if (recommendedProduct.num < quantity) {
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
      ElMessage.success(`${recommendedProduct.name} 已加入购物车 (数量: ${quantity})`)
      // 更新本地库存显示
      const index = recommendedProducts.value.findIndex(p => p.goodsId === recommendedProduct.goodsId)
      if (index !== -1) {
        recommendedProducts.value[index].num = Math.max(0, recommendedProducts.value[index].num - quantity)
        if (productDetailData.value && productDetailData.value.goods.goodsId === goods.goodsId) {
          productDetailData.value.goods.num = recommendedProducts.value[index].num
        }
        if (recommendedProducts.value[index].num === 0) {
          closeProductDetail()
          ElMessage.info(`${recommendedProduct.name} 库存已售完`)
        }
      }
    } else {
      throw new Error(response.data.message)
    }
  } catch (error) {
    console.error('加入购物车失败:', error)
    const errorMsg = error.response?.data?.message || error.message || '加入购物车失败'
    ElMessage.error(errorMsg)
  }
}

// 从详情页立即购买
const buyNowFromDetail = async (goods, quantity = 1) => {
  // 检查登录状态
  if (!checkLoginStatus()) return
  
  if (!goods.goodsId || goods.goodsId <= 0) {
    ElMessage.error('商品ID无效')
    return
  }

  const recommendedProduct = recommendedProducts.value.find(p => p.goodsId === goods.goodsId)
  if (!recommendedProduct) {
    ElMessage.error('商品信息错误')
    return
  }

  if (recommendedProduct.num < quantity) {
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
      ElMessage.success(`正在为您结算 ${recommendedProduct.name}`)
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
  } catch (error) {
    console.error('立即购买失败:', error)
    const errorMsg = error.response?.data?.message || error.message || '立即购买失败'
    ElMessage.error(errorMsg)
  }
}

// 页面加载时获取推荐商品
onMounted(() => {
  loadRecommendedProducts()
})

// 显示弹窗并填充当前用户信息
const editUserInfo = () => {
  Object.assign(editForm, {
    username: currentUserInfo.value.username || '',
    password: currentUserInfo.value.password || '',
    gender: currentUserInfo.value.gender || '',
    telephone: currentUserInfo.value.telephone || '',
    intro: currentUserInfo.value.introduce || '',
    address: currentUserInfo.value.address || ''
  })
  dialogVisible.value = true
}

// 提交修改
const saveUserInfo = async () => {
  try {
    const response = await axios.put('/api/user/update', editForm, {
      headers: {
        'Content-Type': 'application/json' 
      },
      withCredentials: true 
    })
    
    if (response.status === 200) {
      // 重新获取用户信息并更新响应式数据
      const profileResponse = await axios.get('/api/user/profile', {
        withCredentials: true
      })
      
      // 更新本地存储和响应式数据
      sessionStorage.setItem('Atuserinfo', JSON.stringify(profileResponse.data))
      currentUserInfo.value = profileResponse.data
      
      dialogVisible.value = false 
      ElMessage.success('用户信息更新成功')
    }
  } catch (error) {
    console.error('更新失败:', error)
    ElMessage.error(`更新失败: ${error.response?.data?.message || error.message}`)
  }
}

// 退出登录逻辑
const logout = () => {
  sessionStorage.removeItem('Atuserinfo')
  router.push('/login')
}
</script>

<style scoped>
.user-index {
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  min-height: 100vh;
}

.top-banner {
  padding: 0 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 80px;
  font-size: 16px;
  background: linear-gradient(135deg, #e2e8f0 0%, #cbd5e1 100%);
  color: #334155;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-bottom: 1px solid #94a3b8;
}

.banner-content {
  flex: 1;
  text-align: center;
  animation: bounce 2s infinite;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
}

.banner-text {
  font-size: 22px;
  font-weight: 600;
  letter-spacing: 1px;
  color: #1e293b;
  text-shadow: none;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  position: relative;
}

.display-username {
  font-size: 14px;
  color: #334155;
  font-weight: 500;
  margin-right: 8px;
}

.user-avatar-wrapper {
  position: relative;
  cursor: pointer;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50% !important; 
  border: 2px solid #e2e8f0;
  transition: all 0.3s ease;
  object-fit: cover;
  display: block; 
}

.user-avatar:hover {
  transform: scale(1.05);
  border-color: #64748b;
  box-shadow: 0 4px 12px rgba(100, 116, 139, 0.2);
}

.avatar-badge {
  position: absolute;
  bottom: 2px;
  right: 2px;
  width: 10px;
  height: 10px;
  background: #10b981;
  border: 2px solid white;
  border-radius: 50%;
}

:deep(.user-profile-popper) {
  padding: 0 !important;
  border: none !important;
  border-radius: 16px !important;
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.15) !important;
  overflow: hidden !important;
  background: transparent !important;
}

.user-profile {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  min-width: 300px;
}

.profile-header {
  background: linear-gradient(135deg, #e2e8f0 0%, #cbd5e1 100%);
  color: #334155;
  padding: 24px 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  position: relative;
}

.profile-avatar-container {
  position: relative;
}

.profile-avatar {
  width: 64px;
  height: 64px;
  border-radius: 50% !important; 
  border: 3px solid rgba(100, 116, 139, 0.3);
  object-fit: cover;
  display: block; 
}

.avatar-status {
  position: absolute;
  bottom: 4px;
  right: 4px;
  width: 16px;
  height: 16px;
  background: #10b981;
  border: 3px solid #e2e8f0;
  border-radius: 50%;
}

.profile-basic {
  flex: 1;
}

.profile-username {
  margin: 0 0 4px 0;
  font-size: 18px;
  font-weight: 600;
  line-height: 1.2;
}

.profile-subtitle {
  margin: 0;
  font-size: 13px;
  opacity: 0.8;
  line-height: 1.2;
}

.profile-details {
  padding: 20px;
  background: #f8f9fa;
}

.detail-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 16px;
  padding: 12px;
  background: white;
  border-radius: 8px;
  border: 1px solid #f0f0f0;
  transition: all 0.2s ease;
}

.detail-item:hover {
  border-color: #64748b;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(100, 116, 139, 0.1);
}

.detail-item:last-child {
  margin-bottom: 0;
}

.detail-icon {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #94a3b8, #64748b);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 14px;
  flex-shrink: 0;
}

.detail-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-label {
  font-size: 12px;
  color: #64748b;
  font-weight: 500;
}

.detail-value {
  font-size: 14px;
  color: #1e293b;
  font-weight: 400;
  line-height: 1.4;
  word-break: break-all;
}

.profile-actions {
  padding: 20px;
  background: white;
  display: flex;
  gap: 12px;
}

.action-btn {
  flex: 1;
  height: 44px;
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.2s ease;
}

.edit-btn {
  background: linear-gradient(135deg, #475569, #334155) !important;
  border: none !important;
  color: white !important;
}

.edit-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(71, 85, 105, 0.3);
}

.logout-btn {
  background: white !important;
  border: 1px solid #dcdfe6 !important;
  color: #606266 !important;
}

.logout-btn:hover {
  background: #f56c6c !important;
  border-color: #f56c6c !important;
  color: white !important;
  transform: translateY(-1px);
}

/* 编辑弹窗样式 - 调整尺寸和布局 */
:deep(.edit-dialog) {
  .el-dialog {
    border-radius: 16px;
    overflow: hidden;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  }

  .el-dialog__header {
    display: none;
  }

  .el-dialog__body {
    padding: 0;
  }
}

.dialog-header {
  background: linear-gradient(135deg, #475569 0%, #334155 100%);
  color: white;
  padding: 20px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

/* 修改后的 header-icon 样式 - 去掉灰色背景框 */
.header-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  color: white;
}

.header-text h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  line-height: 1.2;
}

.header-text p {
  margin: 4px 0 0 0;
  font-size: 13px;
  opacity: 0.9;
  line-height: 1.2;
}

.close-btn {
  color: white !important;
  border: none !important;
  background: rgba(255, 255, 255, 0.1) !important;
  border-radius: 8px !important;
  width: 32px !important;
  height: 32px !important;
  padding: 0 !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  transition: all 0.2s !important;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.2) !important;
  transform: rotate(90deg);
}

.dialog-body {
  padding: 24px 32px 20px;
  background-color: #fafbfc;
}

.edit-form {
  max-width: 100%;
}

/* 新的表单行布局 */
.form-row {
  display: flex;
  gap: 24px;
  margin-bottom: 20px;
}

.form-row-full {
  margin-bottom: 16px;
}

.form-group {
  flex: 1;
}

.form-group-full {
  width: 100%;
}

.form-label {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  font-weight: 500;
  color: #374151;
  font-size: 14px;
}

.form-label .el-icon {
  color: #64748b;
  font-size: 16px;
}

:deep(.el-form-item) {
  margin-bottom: 0;
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
  border: 1px solid #e5e7eb;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  transition: all 0.2s;
}

:deep(.el-input__wrapper:hover) {
  border-color: #64748b;
}

:deep(.el-input__wrapper.is-focus) {
  border-color: #64748b;
  box-shadow: 0 0 0 3px rgba(100, 116, 139, 0.1);
}

:deep(.el-textarea__inner) {
  border-radius: 8px;
  border: 1px solid #e5e7eb;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  transition: all 0.2s;
}

:deep(.el-textarea__inner:hover) {
  border-color: #64748b;
}

:deep(.el-textarea__inner:focus) {
  border-color: #64748b;
  box-shadow: 0 0 0 3px rgba(100, 116, 139, 0.1);
}

/* 修复性别选择的样式 */
.gender-group-inline {
  display: flex;
  gap: 12px;
  width: 100%;
}

.gender-radio-inline {
  flex: 1;
  margin-right: 0 !important;
}

:deep(.gender-radio-inline .el-radio__input) {
  display: none;
}

:deep(.gender-radio-inline .el-radio__label) {
  padding-left: 0;
  width: 100%;
}

.radio-content-inline {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 8px 12px;
  border: 2px solid #e5e7eb;
  border-radius: 6px;
  background: white;
  transition: all 0.2s;
  cursor: pointer;
  font-weight: 500;
  font-size: 14px;
  width: 100%;
  box-sizing: border-box;
}

:deep(.gender-radio-inline.is-checked) .radio-content-inline {
  border-color: #64748b;
  background: #f8fafc;
  color: #64748b;
}

.radio-content-inline:hover {
  border-color: #64748b;
  background: #f9fafb;
}

.dialog-footer {
  padding: 16px 32px 20px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  background: white;
  border-top: 1px solid #f3f4f6;
}

.cancel-btn {
  border: 1px solid #d1d5db !important;
  color: #6b7280 !important;
  background: white !important;
  border-radius: 8px !important;
  padding: 10px 20px !important;
  font-weight: 500 !important;
  transition: all 0.2s !important;
}

.cancel-btn:hover {
  border-color: #9ca3af !important;
  color: #4b5563 !important;
}

.save-btn {
  background: linear-gradient(135deg, #475569 0%, #334155 100%) !important;
  border: none !important;
  border-radius: 8px !important;
  padding: 10px 20px !important;
  font-weight: 500 !important;
  box-shadow: 0 4px 12px rgba(71, 85, 105, 0.3) !important;
  transition: all 0.2s !important;
}

.save-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(71, 85, 105, 0.4) !important;
}

/* 动画效果 */
.el-icon {
  animation: floatIcon 3s ease-in-out infinite;
  font-size: 20px;
  color: #64748b;
}

@keyframes floatIcon {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-3px); }
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-5px); }
}

/* 新增轮播图样式 - 修复显示问题 */
.carousel-container {
  width: 100%;
  max-width: 1200px;
  margin: 20px auto;
  padding: 0 20px;
  box-sizing: border-box;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

:deep(.el-carousel) {
  border-radius: 12px;
  overflow: hidden;
}

:deep(.el-carousel__container) {
  height: 350px;
}

.carousel-item-wrapper {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
  border-radius: 12px;
}

.carousel-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.carousel-item-wrapper:hover .carousel-image {
  transform: scale(1.05);
}

.carousel-caption {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.6));
  color: white;
  padding: 20px;
  font-size: 18px;
  font-weight: 600;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
}

:deep(.el-carousel__indicator) {
  background-color: rgba(255, 255, 255, 0.4);
  border-radius: 50%;
  width: 12px;
  height: 12px;
}

:deep(.el-carousel__indicator.is-active) {
  background-color: #3b82f6;
}

:deep(.el-carousel__arrow) {
  background-color: rgba(255, 255, 255, 0.8);
  color: #3b82f6;
  border-radius: 50%;
  width: 40px;
  height: 40px;
}

:deep(.el-carousel__arrow:hover) {
  background-color: #3b82f6;
  color: white;
}

/* 推荐商品展示样式 - 更新配色 */
.hot-products-container {
  width: 100%;
  max-width: 1200px;
  margin: 30px auto 40px;
  padding: 0 20px;
  box-sizing: border-box;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 24px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 20px;
  padding: 16px 0;
  border-bottom: 2px solid #e2e8f0;
  position: relative;
}

.section-title::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 60px;
  height: 2px;
  background: linear-gradient(90deg, #3b82f6, #1d4ed8);
  border-radius: 1px;
}

.section-icon {
  color: #3b82f6;
  font-size: 28px;
  animation: none;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  min-height: 300px;
}

.product-item {
  background-color: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  border: 1px solid #f1f5f9;
  cursor: pointer;
}

.product-item:hover {
  transform: translateY(-8px);
  box-shadow: 0 8px 24px rgba(59, 130, 246, 0.15);
  border-color: #3b82f6;
}

.product-image-container {
  width: 100%;
  height: 180px;
  overflow: hidden;
  position: relative;
}

.product-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.product-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(59, 130, 246, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.product-item:hover .product-overlay {
  opacity: 1;
}

.product-item:hover .product-image {
  transform: scale(1.1);
}

.view-btn {
  color: white;
  background: rgba(255, 255, 255, 0.2);
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  border: 1px solid rgba(255, 255, 255, 0.3);
  cursor: pointer;
  transition: all 0.2s ease;
}

.view-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: scale(1.05);
}

.product-info {
  padding: 16px;
}

.product-name {
  font-size: 14px;
  color: #1e293b;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  font-weight: 500;
  line-height: 1.4;
  min-height: 36px;
}

.product-price {
  font-size: 16px;
  font-weight: 600;
  color: #3b82f6;
  margin-bottom: 8px;
}

.product-stats {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #64748b;
}

.product-stats .sales {
  color: #10b981;
}

.product-stats .stock {
  color: #64748b;
}

.product-stats .stock.low-stock {
  color: #ef4444;
  font-weight: 500;
}

.no-recommendations {
  grid-column: 1 / -1;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 200px;
  text-align: center;
}

.no-recommendations :deep(.el-empty__description p) {
  color: #64748b;
  margin: 4px 0;
  font-size: 14px;
}

/* 商品详情弹窗样式 */
:deep(.product-detail-dialog) {
  .el-dialog {
    margin-top: 8vh !important;
    margin-bottom: 8vh !important;
    height: 84vh;
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

.detail-error {
  padding: 40px 0;
  text-align: center;
}

/* 响应式调整 */
@media (max-width: 1024px) {
  .product-grid {
    grid-template-columns: repeat(3, 1fr);
  }
  
  :deep(.product-detail-dialog .el-dialog) {
    width: 60% !important;
  }
}

@media (max-width: 768px) {
  .banner-text {
    font-size: 18px;
  }
  
  .user-profile {
    min-width: 280px;
  }
  
  .profile-header {
    padding: 20px 16px;
  }
  
  .profile-details {
    padding: 16px;
  }
  
  .profile-actions {
    padding: 16px;
    flex-direction: column;
  }
  
  .carousel-container {
    margin: 16px auto;
    padding: 0 16px;
  }
  
  :deep(.el-carousel__container) {
    height: 250px;
  }

  .product-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
  }

  .hot-products-container {
    padding: 0 16px;
  }

  :deep(.edit-dialog .el-dialog) {
    width: 90% !important;
    margin: 5vh auto !important;
  }

  .dialog-header {
    padding: 16px 20px;
  }

  .header-text h3 {
    font-size: 16px;
  }

  .dialog-body {
    padding: 20px 24px 16px;
  }

  .form-row {
    flex-direction: column;
    gap: 16px;
  }

  .dialog-footer {
    padding: 12px 24px 16px;
    flex-direction: column;
  }

  .dialog-footer .el-button {
    width: 100%;
  }
  
  :deep(.product-detail-dialog .el-dialog) {
    width: 75% !important;
    margin-top: 5vh !important;
    margin-bottom: 5vh !important;
    height: 90vh;
    max-height: 90vh;
  }
}

@media (max-width: 480px) {
  .product-grid {
    grid-template-columns: 1fr;
  }
  
  .product-image-container {
    height: 150px;
  }
  
  .section-title {
    font-size: 20px;
  }

  :deep(.edit-dialog .el-dialog) {
    width: 95% !important;
    margin: 3vh auto !important;
  }

  .dialog-body {
    padding: 16px 20px 12px;
  }

  .dialog-footer {
    padding: 12px 20px 16px;
  }

  .form-row {
    gap: 12px;
  }

  .gender-group-inline {
    gap: 8px;
  }

  .radio-content-inline {
    padding: 6px 8px;
    font-size: 13px;
  }

  .banner-content {
    gap: 6px;
  }

  .banner-text {
    font-size: 16px;
    letter-spacing: 0.5px;
  }
  
  :deep(.product-detail-dialog .el-dialog) {
    width: 90% !important;       
    margin: 3vh !important;       
    height: 94vh !important;          
    max-height: 94vh !important;      
    border-radius: 8px !important;
  }
}
</style>