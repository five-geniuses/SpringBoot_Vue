<template>
  <div class="cart-container">
    <div class="cart-header">
      <h2>我的购物车</h2>
      <div class="header-right">
        <span v-if="cartItems.length > 0" class="item-count">
          共{{ cartItems.length }}件商品
        </span>
        <el-button 
          @click="refreshCart" 
          :loading="refreshLoading"
          :icon="Refresh"
          size="default"
          type="primary"
          plain
          circle
          class="refresh-btn"
          title="刷新购物车"
        />
      </div>
    </div>

    <!-- 立即购买提示 -->
    <div v-if="isBuyNowMode" class="buy-now-notice">
      <el-alert
        title="立即购买模式"
        type="success"
        description="您正在进行立即购买，系统已为您自动选择商品并准备结算"
        show-icon
        :closable="false"
      />
    </div>

    <!-- 空购物车提示 -->
    <div v-if="cartItems.length === 0" class="empty-cart">
      <el-empty description="购物车还是空的，去逛逛吧~">
        <el-button type="primary" @click="goToProducts" size="large">
          <el-icon><ShoppingCart /></el-icon>
          去购物
        </el-button>
      </el-empty>
    </div>

    <!-- 购物车内容 -->
    <div v-else>
      <el-table 
        ref="tableRef"
        :data="cartItems" 
        style="width: 100%" 
        row-key="cartId"
        :loading="loading"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        
        <el-table-column label="商品信息" min-width="300">
          <template #default="{ row }">
            <div class="product-info">
              <div class="product-image-wrapper">
                <el-image
                  :src="getImageUrl(row.imgUrl)"
                  :alt="row.goodsName"
                  class="product-image"
                  fit="cover"
                  lazy
                  :preview-src-list="[getImageUrl(row.imgUrl)]"
                >
                  <template #error>
                    <div class="image-slot">
                      <el-icon><Picture /></el-icon>
                      <span>图片加载失败</span>
                    </div>
                  </template>
                  <template #placeholder>
                    <div class="image-slot">
                      <el-icon><Loading /></el-icon>
                    </div>
                  </template>
                </el-image>
                <el-tag 
                  v-if="row.stock && row.stock <= 10" 
                  type="warning" 
                  size="small" 
                  class="stock-warning"
                >
                  仅剩{{ row.stock }}件
                </el-tag>
                <!-- 立即购买标记 -->
                <el-tag 
                  v-if="isBuyNowItem(row)" 
                  type="success" 
                  size="small" 
                  class="buy-now-badge"
                >
                  立即购买
                </el-tag>
              </div>
              <div class="product-details">
                <h4 class="product-name">{{ row.goodsName }}</h4>
                <div class="product-tags">
                  <el-tag size="small" type="info">正品保证</el-tag>
                  <el-tag size="small" type="success">7天无理由退换</el-tag>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="单价" width="120" align="center">
          <template #default="{ row }">
            <div class="price-info">
              <span class="current-price">¥{{ row.price }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="数量" width="220" align="center">
          <template #default="{ row }">
            <div class="quantity-controls">
              <div class="quantity-input-group">
                <el-button 
                  size="small" 
                  :disabled="row.quantity <= 1"
                  @click="decreaseQuantity(row)"
                  class="quantity-btn"
                >
                  -
                </el-button>
                <el-input 
                  v-model.number="row.quantity"
                  size="small"
                  class="quantity-input"
                  :min="1"
                  @blur="validateQuantity(row)"
                  @input="debounceUpdateQuantity(row)"
                />
                <el-button 
                  size="small" 
                  @click="increaseQuantity(row)"
                  class="quantity-btn"
                >
                  +
                </el-button>
              </div>
              <div class="stock-info">
                <span class="stock-text" v-if="row.stock && row.stock <= 10">
                  仅剩{{ row.stock }}件
                </span>
                <span class="stock-text" v-else-if="row.stock">
                  库存{{ row.stock }}件
                </span>
              </div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="小计" width="150" align="center">
          <template #default="{ row }">
            <span class="subtotal">¥{{ row.totalPrice }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-tooltip content="移除商品" placement="top">
              <el-button 
                link 
                type="danger" 
                @click="removeItem(row)"
                :icon="Delete"
                size="small"
              >
                删除
              </el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <!-- 底部操作栏 -->
      <div class="cart-footer">
        <div class="footer-left">
          <el-checkbox
            v-model="isAllSelected"
            :indeterminate="isIndeterminate"
            @change="toggleSelectAll"
            class="select-all-checkbox"
          >
            {{ isAllSelected ? '取消全选' : '全选' }}
          </el-checkbox>
          
          <el-button @click="clearCart" link style="color: #e74c3c" :icon="Delete">
            清空购物车
          </el-button>
          
          <span class="selection-info">
            已选择 {{ selectedItems.length }} / {{ cartItems.length }} 件商品
          </span>
        </div>
        
        <div class="footer-right">
          <div class="price-summary">
            <div class="selected-info">
              已选择{{ selectedItems.length }}件商品
            </div>
            <div class="total">
              <span>合计：</span>
              <span class="totalPrice">¥{{ selectedTotalPrice }}</span>
            </div>
          </div>
          <el-button 
            type="primary" 
            @click="checkout" 
            size="large"
            :disabled="selectedItems.length === 0"
            :loading="checkoutLoading"
          >
            {{ isBuyNowMode ? '立即结算' : '去结算' }}({{ selectedItems.length }})
          </el-button>
        </div>
      </div>
    </div>
  </div>
  
  <!-- 结算弹窗 -->
  <el-dialog 
    v-model="showCheckoutDialog" 
    title="填写收货信息" 
    width="500px"
    :close-on-click-modal="false"
  >
    <el-form 
      ref="formRef" 
      :model="checkoutForm" 
      :rules="formRules"
      label-width="90px" 
      @submit.prevent="submitOrder"
    >
      <el-form-item label="收件人" prop="name">
        <el-input 
          v-model="checkoutForm.name" 
          placeholder="请输入收件人姓名"
          :prefix-icon="User"
        />
      </el-form-item>

      <el-form-item label="联系电话" prop="phone">
        <el-input 
          v-model="checkoutForm.phone" 
          placeholder="请输入联系电话"
          :prefix-icon="Phone"
          maxlength="11"
        />
      </el-form-item>

      <el-form-item label="收货地址" prop="address">
        <el-input 
          v-model="checkoutForm.address" 
          type="textarea" 
          rows="3" 
          placeholder="请输入详细地址"
          maxlength="200"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="备注信息">
        <el-input 
          v-model="checkoutForm.remark" 
          type="textarea" 
          rows="2" 
          placeholder="选填，如送货时间等要求"
          maxlength="100"
          show-word-limit
        />
      </el-form-item>

      <el-form-item style="text-align: right; margin-bottom: 0;">
        <el-button @click="showCheckoutDialog = false">取消</el-button>
        <el-button 
          type="primary" 
          native-type="submit"
          :loading="submitLoading"
        >
          创建订单
        </el-button>
      </el-form-item>
    </el-form>
  </el-dialog>

  <!-- 支付选择弹窗 -->
  <el-dialog 
    v-model="showPaymentDialog" 
    title="选择支付方式" 
    width="450px"
    :close-on-click-modal="false"
    :show-close="false"
  >
    <div class="payment-options">
      <!-- 订单摘要 -->
      <div class="order-summary">
        <div class="summary-header">
          <h4>订单摘要</h4>
          <el-tag type="success" size="small">订单已创建</el-tag>
        </div>
        <div class="summary-content">
          <div class="summary-row">
            <span>订单编号：</span>
            <span class="order-no">{{ createdOrder?.orderNo }}</span>
          </div>
          <div class="summary-row">
            <span>商品数量：</span>
            <span>{{ selectedItems.length }}件</span>
          </div>
          <div class="summary-row">
            <span>收货人：</span>
            <span>{{ checkoutForm.name }}</span>
          </div>
          <div class="summary-row">
            <span>联系电话：</span>
            <span>{{ checkoutForm.phone }}</span>
          </div>
          <div class="summary-row total-row">
            <span>订单总额：</span>
            <span class="total-amount">¥{{ createdOrder?.totalAmount || selectedTotalPrice }}</span>
          </div>
        </div>
      </div>

      <!-- 支付选项 -->
      <div class="payment-choices">
        <h4>请选择支付方式：</h4>
        <div class="payment-buttons">
          <el-button 
            type="primary" 
            size="large"
            :loading="paymentLoading"
            @click="handlePaymentChoice('immediate')"
            class="payment-btn immediate-pay"
          >
            <el-icon><CreditCard /></el-icon>
            立即支付
          </el-button>
          
          <el-button 
            type="default" 
            size="large"
            :loading="paymentLoading"
            @click="handlePaymentChoice('later')"
            class="payment-btn later-pay"
          >
            <el-icon><Clock /></el-icon>
            稍后支付
          </el-button>
        </div>
        
        <div class="payment-tips">
          <p class="tip-item">
            <el-icon><InfoFilled /></el-icon>
            立即支付：完成支付，订单状态变为已支付
          </p>
          <p class="tip-item">
            <el-icon><InfoFilled /></el-icon>
            稍后支付：订单保存为待支付，可在订单中心完成支付
          </p>
        </div>
      </div>
    </div>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="cancelOrder" :disabled="paymentLoading" link>
          取消订单
        </el-button>
      </span>
    </template>
  </el-dialog>

  <!-- 推荐商品区域 -->
  <div class="recommendations-section">
    <!-- 区域标题 -->
    <div class="section-header">
      <h3>
        <el-icon><Star /></el-icon>
        猜你喜欢
      </h3>
    </div>
    
    <!-- 推荐商品网格布局 -->
    <div class="recommendation-grid">
      <!-- 单个推荐商品卡片 -->
      <div 
        v-for="product in recommendedProducts" 
        :key="product.goodsId" 
        class="recommendation-item"
        @click="addRecommendedToCart(product)"
      >
        <!-- 商品图片 -->
        <el-image
          :src="getImageUrl(product.imgUrl)"
          :alt="product.name"
          class="recommendation-image"
          fit="cover"
          lazy
        />
        
        <!-- 商品信息区域 -->
        <div class="recommendation-info">
          <!-- 商品名称 -->
          <p class="product-name">{{ product.name }}</p>
          
          <!-- 商品描述 -->
          <p class="product-description">
            {{ product.desc || '优质商品，值得信赖' }}
          </p>
          
          <!-- 价格和添加按钮 -->
          <div class="product-price-row">
            <span class="product-price">¥{{ product.price }}</span>
            <el-button size="small" type="primary" plain>
              <el-icon><Plus /></el-icon>
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { 
  Delete, 
  ShoppingCart, 
  User, 
  Phone, 
  Star,
  Plus,
  Check,
  Select,
  CreditCard,
  Clock,
  InfoFilled,
  Picture,
  Loading,
  Refresh
} from '@element-plus/icons-vue'
import axios from 'axios'

// 防抖函数
function debounce<T extends (...args: any[]) => void>(func: T, wait: number): (...args: Parameters<T>) => void {
  let timeout: ReturnType<typeof setTimeout>
  return function(...args: Parameters<T>) {
    clearTimeout(timeout)
    timeout = setTimeout(() => func(...args), wait)
  }
}

// 数据库cart表结构对应的接口
interface CartItem {
  cartId: number
  userId: number
  goodsId: number  // 自增ID
  goodsName: string
  quantity: number
  price: number
  totalPrice: number
  imgUrl?: string
  addTime?: string
  updateTime?: string
  stock?: number   // 添加库存字段
}

// 商品接口（用于推荐商品）
interface RecommendedProduct {
  goodsId: number  // 自增ID
  name: string
  price: number
  imgUrl?: string
  num?: number     // 库存数量
  desc: string
}

// 订单结果接口
interface OrderResult {
  orderId: number
  orderNo: string
  totalAmount: number
  orderState: number
  payState: number
  receiverInfo?: {
    name: string
    phone: string
    address: string
  }
  items?: Array<{
    goodsId: number
    goodsName: string
    quantity: number
    price: number
    totalPrice: number
  }>
}

interface CheckoutForm {
  name: string
  phone: string
  address: string
  remark: string
}

const router = useRouter()
const route = useRoute()

// 表格引用
const tableRef = ref()

// 响应式数据
const loading = ref(false)
const refreshLoading = ref(false)
const checkoutLoading = ref(false)
const submitLoading = ref(false)
const paymentLoading = ref(false)
const selectedItems = ref<CartItem[]>([])
const cartItems = ref<CartItem[]>([])

// 立即购买相关状态
const buyNowParams = ref({
  isBuyNow: false,
  goodsId: 0,
  quantity: 0,
  autoCheckout: false
})

// 当前用户ID - 实际应从登录状态获取
const currentUserId = computed(() => {
  try {
    const userInfo = sessionStorage.getItem('Atuserinfo')
    if (userInfo) {
      const user = JSON.parse(userInfo)
      return user.userId 
    }
  } catch (error) {
    console.error('获取用户ID失败:', error)
  }
})

// 弹窗控制
const showCheckoutDialog = ref(false)
const showPaymentDialog = ref(false)

// 订单信息
const createdOrder = ref<OrderResult | null>(null)

// 表单数据和验证规则
const formRef = ref<FormInstance>()
const checkoutForm = ref<CheckoutForm>({
  name: '',
  phone: '',
  address: '',
  remark: ''
})

const formRules = {
  name: [
    { required: true, message: '请输入收件人姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  address: [
    { required: true, message: '请输入收货地址', trigger: 'blur' },
    { min: 10, message: '请输入详细地址，至少10个字符', trigger: 'blur' }
  ]
}

// 推荐商品数据
const recommendedProducts = ref<RecommendedProduct[]>([])

// 立即购买模式判断
const isBuyNowMode = computed(() => buyNowParams.value.isBuyNow)

// 判断是否为立即购买的商品
const isBuyNowItem = (item: CartItem): boolean => {
  return buyNowParams.value.isBuyNow && item.goodsId === buyNowParams.value.goodsId
}

// 全选相关的计算属性
const isAllSelected = computed({
  get: () => {
    return cartItems.value.length > 0 && selectedItems.value.length === cartItems.value.length
  },
  set: (value: boolean) => {
    if (!tableRef.value) return
    
    if (value) {
      // 全选
      cartItems.value.forEach(row => {
        tableRef.value!.toggleRowSelection(row, true)
      })
    } else {
      // 取消全选
      tableRef.value.clearSelection()
    }
  }
})

// 半选状态（部分选中）
const isIndeterminate = computed(() => {
  const selectedCount = selectedItems.value.length
  return selectedCount > 0 && selectedCount < cartItems.value.length
})

// 计算属性
const selectedTotalPrice = computed(() => {
  return selectedItems.value.reduce((sum, item) => sum + item.totalPrice, 0).toFixed(2)
})

// 图片URL处理
const getImageUrl = (imgUrl?: string) => {
  if (!imgUrl) return '/default-product.png'
  // 如果是完整URL直接返回
  if (imgUrl.startsWith('http')) return imgUrl
  // 如果是以/开头的路径，直接返回
  if (imgUrl.startsWith('/')) return imgUrl
  // 否则拼接API基础路径
  return `/api/uploads/${imgUrl}`
}

// 处理立即购买的路由参数
const handleBuyNowParams = () => {
  const { buyNow, goodsId, quantity, autoCheckout } = route.query
  
  if (buyNow === '1' && goodsId && quantity) {
    buyNowParams.value = {
      isBuyNow: true,
      goodsId: Number(goodsId),
      quantity: Number(quantity),
      autoCheckout: autoCheckout === '1'
    }
    
    console.log('检测到立即购买参数:', buyNowParams.value)
    
    // 清除URL参数，避免刷新页面时重复处理
    router.replace({ path: route.path })
    
    return true
  }
  
  return false
}

// 自动选择立即购买的商品并打开结算弹窗
const handleAutoCheckout = async () => {
  if (!buyNowParams.value.autoCheckout || !buyNowParams.value.isBuyNow) return
  
  await nextTick() // 等待DOM更新
  
  // 等待购物车数据加载完成
  let retries = 0
  const maxRetries = 10
  
  const trySelectAndCheckout = () => {
    const targetItem = cartItems.value.find(item => 
      item.goodsId === buyNowParams.value.goodsId
    )
    
    if (targetItem && tableRef.value) {
      console.log('找到立即购买商品，自动选中:', targetItem)
      
      // 清除所有选择
      tableRef.value.clearSelection()
      
      // 选中目标商品
      tableRef.value.toggleRowSelection(targetItem, true)
      
      // 延迟打开结算弹窗，确保选择状态更新
      setTimeout(() => {
        if (selectedItems.value.length > 0) {
          ElMessage.success('已为您自动选择商品，请填写收货信息')
          showCheckoutDialog.value = true
        }
      }, 500)
      
      return true
    } else {
      retries++
      if (retries < maxRetries) {
        console.log(`未找到立即购买商品，重试 ${retries}/${maxRetries}`)
        setTimeout(trySelectAndCheckout, 500)
      } else {
        console.warn('立即购买商品未找到，可能已不在购物车中')
        ElMessage.warning('商品可能已不在购物车中，请手动选择')
      }
      return false
    }
  }
  
  trySelectAndCheckout()
}

// API请求函数
const api = {
  // 获取用户购物车
  async getUserCart(userId: number) {
    try {
      loading.value = true
      const response = await axios.get(`/api/api/cart/list/${userId}`)
      if (response.data.success) {
        // 确保返回的数据包含必要的字段，并统一字段名称
        return (response.data.data || []).map((item: any) => ({
          ...item,
          goodsId: Number(item.goodsId || item.goods_id),
          cartId: Number(item.cartId || item.cart_id),
          price: Number(item.price),
          quantity: Number(item.quantity),
          totalPrice: Number(item.totalPrice || item.total_price),
          goodsName: item.goodsName || item.goods_name || item.name,
          imgUrl: item.imgUrl || item.img_url || item.image,
          stock: Number(item.stock || item.num || 0)
        }))
      } else {
        throw new Error(response.data.message)
      }
    } catch (error: any) {
      console.error('获取购物车失败:', error)
      ElMessage.error(error.response?.data?.message || error.message || '获取购物车数据失败')
      return []
    } finally {
      loading.value = false
    }
  },

  // 更新购物车商品数量
  async updateQuantity(cartId: number, quantity: number) {
    try {
      const response = await axios.put('/api/api/cart/update', {
        cartId,
        quantity
      })
      
      if (response.data.success) {
        return response.data.data
      } else {
        throw new Error(response.data.message)
      }
    } catch (error: any) {
      console.error('更新数量失败:', error)
      const errorMsg = error.response?.data?.message || error.message || '更新数量失败'
      ElMessage.error(errorMsg)
      throw error
    }
  },

  // 从购物车移除商品
  async removeFromCart(cartId: number) {
    try {
      const response = await axios.delete(`/api/api/cart/remove/${cartId}`)
      if (response.data.success) {
        return response.data.data
      } else {
        throw new Error(response.data.message)
      }
    } catch (error: any) {
      console.error('删除商品失败:', error)
      const errorMsg = error.response?.data?.message || error.message || '删除商品失败'
      ElMessage.error(errorMsg)
      throw error
    }
  },

  // 清空购物车
  async clearUserCart(userId: number) {
    try {
      const response = await axios.delete(`/api/api/cart/clear/${userId}`)
      if (response.data.success) {
        return response.data.data
      } else {
        throw new Error(response.data.message)
      }
    } catch (error: any) {
      console.error('清空购物车失败:', error)
      const errorMsg = error.response?.data?.message || error.message || '清空购物车失败'
      ElMessage.error(errorMsg)
      throw error
    }
  },

  // 添加商品到购物车
  async addToCart(userId: number, goodsId: number, quantity: number) {
    try {
      const response = await axios.post('/api/api/cart/add', {
        userId,
        goodsId,
        quantity
      })
      
      if (response.data.success) {
        return response.data.data
      } else {
        throw new Error(response.data.message)
      }
    } catch (error: any) {
      console.error('添加商品失败:', error)
      const errorMsg = error.response?.data?.message || error.message || '添加商品到购物车失败'
      ElMessage.error(errorMsg)
      throw error
    }
  },

  // 创建订单
  async createOrder(orderData: {
    userId: number
    receiverName: string
    receiverPhone: string
    receiverAddress: string
    remark?: string
    cartIds: number[]
  }) {
    try {
      // 构建 URLSearchParams 来正确传递数组参数
      const params = new URLSearchParams()
      params.append('userId', orderData.userId.toString())
      params.append('receiverName', orderData.receiverName)
      params.append('receiverPhone', orderData.receiverPhone)
      params.append('receiverAddress', orderData.receiverAddress)
      if (orderData.remark) {
        params.append('remark', orderData.remark)
      }
      
      // 数组参数需要逐个添加
      orderData.cartIds.forEach(cartId => {
        params.append('cartIds', cartId.toString())
      })

      const response = await axios.post('/api/api/orders/create', params, {
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded'
        }
      })
      return response.data
    } catch (error) {
      console.error('创建订单失败:', error)
      ElMessage.error('创建订单失败')
      throw error
    }
  },

  // 支付订单
  async payOrder(orderNo: string) {
    try {
      const response = await axios.post(`/api/api/orders/pay/${orderNo}`, {
        payState: 1 // 传递支付状态：1表示已支付
      })
      return response.data
    } catch (error) {
      console.error('支付失败:', error)
      ElMessage.error('支付失败')
      throw error
    }
  },

  // 取消订单（如果后端支持的话）
  async cancelOrder(orderNo: string) {
    try {
      const response = await axios.post(`/api/api/orders/cancel/${orderNo}`)
      return response.data
    } catch (error) {
      console.error('取消订单失败:', error)
      ElMessage.error('取消订单失败')
      throw error
    }
  },

  // 获取所有商品（用于推荐）
  async getAllGoods() {
    try {
      const response = await axios.get('/api/api/goods')
      return response.data
    } catch (error) {
      console.error('获取商品失败:', error)
      return []
    }
  }
}

// 加载购物车数据
const loadCartData = async () => {
  try {
    const result = await api.getUserCart(currentUserId.value)
    cartItems.value = Array.isArray(result) ? result : []
    
    // 如果是立即购买模式，加载完数据后自动处理
    if (buyNowParams.value.autoCheckout) {
      await handleAutoCheckout()
    }
  } catch (error) {
    console.error('加载购物车数据失败:', error)
  }
}

// 加载推荐商品
const loadRecommendedProducts = async () => {
  try {
    const goods = await api.getAllGoods()
    if (Array.isArray(goods) && goods.length > 0) {
      const shuffled = goods.sort(() => 0.5 - Math.random())
      recommendedProducts.value = shuffled.slice(0, 4)
    }
  } catch (error) {
    console.error('加载推荐商品失败:', error)
  }
}

// 防抖的数量更新
const debounceUpdateQuantity = debounce(async (item: CartItem) => {
  await validateQuantity(item)
}, 500)

// 验证数量并更新到后端
const validateQuantity = async (item: CartItem) => {
  if (!item.goodsId || !item.cartId) {
    ElMessage.error('商品数据无效')
    return
  }
  
  if (typeof item.quantity !== 'number' || isNaN(item.quantity)) {
    item.quantity = 1
  }
  
  if (item.quantity < 1) {
    item.quantity = 1
    ElMessage.warning('数量不能小于1')
  }
  
  // 检查库存
  if (item.stock !== undefined && item.quantity > item.stock) {
    item.quantity = item.stock
    ElMessage.warning(`库存不足，已调整为最大可购买数量：${item.stock}`)
  }

  try {
    const updatedCart = await api.updateQuantity(item.cartId, item.quantity)
    // 更新本地数据
    Object.assign(item, updatedCart)
    ElMessage.success('数量已更新')
  } catch (error) {
    // 如果更新失败，重新加载购物车数据
    await loadCartData()
  }
}

// 增加数量
const increaseQuantity = async (item: CartItem) => {
  const newQuantity = item.quantity + 1
  try {
    const updatedCart = await api.updateQuantity(item.cartId, newQuantity)
    // 更新本地数据
    Object.assign(item, updatedCart)
    ElMessage.success('数量已增加')
  } catch (error) {
    // 错误处理已在 api.updateQuantity 中完成
  }
}

// 减少数量
const decreaseQuantity = async (item: CartItem) => {
  if (item.quantity > 1) {
    const newQuantity = item.quantity - 1
    try {
      const updatedCart = await api.updateQuantity(item.cartId, newQuantity)
      // 更新本地数据
      Object.assign(item, updatedCart)
      ElMessage.success('数量已减少')
    } catch (error) {
      // 错误处理已在 api.updateQuantity 中完成
    }
  } else {
    ElMessage.warning('数量不能少于1件')
  }
}

// 处理选择变化
const handleSelectionChange = (selection: CartItem[]) => {
  selectedItems.value = selection
}

// 全选/取消全选切换
const toggleSelectAll = (value: boolean) => {
  if (!tableRef.value) return
  
  if (value) {
    // 全选所有商品
    cartItems.value.forEach(row => {
      tableRef.value!.toggleRowSelection(row, true)
    })
  } else {
    // 取消全选
    tableRef.value.clearSelection()
  }
}

// 删除商品
const removeItem = async (item: CartItem) => {
  try {
    await ElMessageBox.confirm('确定要删除该商品吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await api.removeFromCart(item.cartId)
    
    // 从本地数组中移除
    const index = cartItems.value.findIndex(cartItem => cartItem.cartId === item.cartId)
    if (index > -1) {
      cartItems.value.splice(index, 1)
    }
    
    // 从选中列表中移除
    const selectedIndex = selectedItems.value.findIndex(selectedItem => selectedItem.cartId === item.cartId)
    if (selectedIndex > -1) {
      selectedItems.value.splice(selectedIndex, 1)
    }
    
    ElMessage.success('商品已删除')
    
    // 如果删除的是立即购买商品，退出立即购买模式
    if (isBuyNowItem(item)) {
      buyNowParams.value.isBuyNow = false
      ElMessage.info('已退出立即购买模式')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除商品失败:', error)
    }
  }
}

// 清空购物车
const clearCart = async () => {
  try {
    await ElMessageBox.confirm('确定要清空购物车吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await api.clearUserCart(currentUserId.value)
    
    cartItems.value = []
    selectedItems.value = []
    
    // 退出立即购买模式
    buyNowParams.value.isBuyNow = false
    
    ElMessage.success('购物车已清空')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('清空购物车失败:', error)
    }
  }
}

// 去结算
const checkout = () => {
  if (selectedItems.value.length === 0) {
    ElMessage.warning('请先选择要结算的商品')
    return
  }
  showCheckoutDialog.value = true
}

// 提交订单信息 - 修改后的版本，直接创建订单
const submitOrder = async () => {
  if (!formRef.value) return
  
  try {
    submitLoading.value = true
    
    // 1. 表单验证
    await formRef.value.validate()
    
    // 2. 准备订单数据
    const cartIds = selectedItems.value.map(item => item.cartId)
    const orderData = {
      userId: currentUserId.value,
      receiverName: checkoutForm.value.name,
      receiverPhone: checkoutForm.value.phone,
      receiverAddress: checkoutForm.value.address,
      remark: checkoutForm.value.remark || '',
      cartIds
    }
    
    console.log('创建订单参数:', orderData)
    
    // 3. 创建订单
    const result = await api.createOrder(orderData)
    console.log('订单创建结果:', result)
    
    // 4. 保存订单信息
    createdOrder.value = result.data || result
    
    // 5. 显示订单创建成功消息
    if (isBuyNowMode.value) {
      ElMessage.success('立即购买订单创建成功！')
    } else {
      ElMessage.success('订单创建成功！')
    }
    
    // 6. 关闭收货信息弹窗，显示支付选择弹窗
    showCheckoutDialog.value = false
    showPaymentDialog.value = true
    
  } catch (error) {
    console.error('创建订单失败:', error)
    ElMessage.error('订单创建失败，请重试')
  } finally {
    submitLoading.value = false
  }
}

// 处理支付选择 - 修改后的版本
const handlePaymentChoice = async (paymentType: 'immediate' | 'later') => {
  if (!createdOrder.value) {
    ElMessage.error('订单信息异常，请重试')
    return
  }

  try {
    paymentLoading.value = true
    
    if (paymentType === 'immediate') {
      // 立即支付 - 调用支付接口
      const payResult = await api.payOrder(createdOrder.value.orderNo)
      console.log('支付结果:', payResult)
      
      ElMessage.success('支付成功！')
      
      // 跳转到订单详情页面，标记为已支付
      router.push({
        path: '/user/orders',
        query: { 
          orderNo: createdOrder.value.orderNo,
          fromCart: '1',
          paid: '1',
          buyNow: isBuyNowMode.value ? '1' : '0'
        }
      })
    } else {
      // 稍后支付 - 直接跳转
      ElMessage.success('您可以稍后在订单中心完成支付')
      
      router.push({
        path: '/user/orders',
        query: { 
          orderNo: createdOrder.value.orderNo,
          fromCart: '1',
          buyNow: isBuyNowMode.value ? '1' : '0'
        }
      })
    }

    // 关闭支付选择弹窗
    showPaymentDialog.value = false
    
    // 重新加载购物车数据
    await loadCartData()
    selectedItems.value = []
    
    // 重置表单和订单信息
    checkoutForm.value = {
      name: '',
      phone: '',
      address: '',
      remark: ''
    }
    createdOrder.value = null
    
    // 退出立即购买模式
    buyNowParams.value.isBuyNow = false
    
  } catch (error) {
    console.error('支付处理失败:', error)
    ElMessage.error('支付失败，请重试')
  } finally {
    paymentLoading.value = false
  }
}

// 取消订单
const cancelOrder = async () => {
  if (!createdOrder.value) return
  
  try {
    await ElMessageBox.confirm('确定要取消这个订单吗？', '提示', {
      confirmButtonText: '确定取消',
      cancelButtonText: '继续支付',
      type: 'warning'
    })
    
    // 调用取消订单接口（如果后端有提供）
    try {
      await api.cancelOrder(createdOrder.value.orderNo)
      ElMessage.success('订单已取消')
    } catch (error) {
      // 如果后端没有取消订单接口，只是前端处理
      ElMessage.info('已返回购物车')
    }
    
    showPaymentDialog.value = false
    createdOrder.value = null
    
    // 退出立即购买模式
    buyNowParams.value.isBuyNow = false
    
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消订单失败:', error)
    }
  }
}

// 添加推荐商品到购物车
const addRecommendedToCart = async (product: RecommendedProduct) => {
  if (!product.goodsId) {
    ElMessage.error('商品ID无效')
    return
  }
  
  try {
    // 检查库存
    if (product.num !== undefined && product.num <= 0) {
      ElMessage.warning('商品库存不足')
      return
    }
    
    await api.addToCart(currentUserId.value, product.goodsId, 1)
    ElMessage.success('商品已添加到购物车')
    
    // 重新加载购物车数据
    await loadCartData()
    
    // 自动选中新添加的商品
    nextTick(() => {
      if (tableRef.value) {
        const newItem = cartItems.value.find(item => item.goodsId === product.goodsId)
        if (newItem) {
          tableRef.value.toggleRowSelection(newItem, true)
        }
      }
    })
  } catch (error) {
    console.error('添加商品失败:', error)
  }
}

// 去商品页
const goToProducts = () => {
  router.push('/user/product/list')
}

// 刷新购物车
const refreshCart = async () => {
  try {
    refreshLoading.value = true
    
    // 清除当前选择状态
    if (tableRef.value) {
      tableRef.value.clearSelection()
    }
    selectedItems.value = []
    
    // 退出立即购买模式
    buyNowParams.value.isBuyNow = false
    
    // 并行刷新购物车数据和推荐商品
    await Promise.all([
      loadCartData(),
      loadRecommendedProducts()
    ])
    
    ElMessage.success('购物车已刷新')
  } catch (error) {
    console.error('刷新购物车失败:', error)
    ElMessage.error('刷新失败，请重试')
  } finally {
    refreshLoading.value = false
  }
}

// 监听购物车数据变化，确保选择状态同步
watch(cartItems, () => {
  // 购物车数据变化时，如果不是立即购买模式，清除之前的选择状态
  if (!buyNowParams.value.isBuyNow && tableRef.value) {
    tableRef.value.clearSelection()
    selectedItems.value = []
  }
}, { deep: true })

// 页面加载时初始化数据
onMounted(async () => {
  // 1. 处理立即购买参数
  const hasBuyNowParams = handleBuyNowParams()
  
  // 2. 加载数据
  await Promise.all([
    loadCartData(),
    loadRecommendedProducts()
  ])
  
  // 3. 如果有立即购买参数但没有设置自动结算，手动触发
  if (hasBuyNowParams && !buyNowParams.value.autoCheckout) {
    await handleAutoCheckout()
  }
})
</script>

<style scoped>
.cart-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.cart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.cart-header h2 {
  margin: 0;
  color: #2c3e50;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

.item-count {
  color: #909399;
  font-size: 14px;
}

.refresh-btn {
  transition: all 0.3s ease;
}

.refresh-btn:hover {
  transform: rotate(180deg);
}

.buy-now-notice {
  margin-bottom: 20px;
}

.empty-cart {
  text-align: center;
  padding: 60px 0;
}

/* 立即购买标记样式 */
.buy-now-badge {
  position: absolute;
  top: -10px;
  left: -15px;
  z-index: 10;
  white-space: nowrap;
  font-size: 10px;
  padding: 3px 8px;
  border-radius: 10px;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  background: linear-gradient(135deg, #67c23a, #85ce61);
  border: none;
  color: white;
}

/* 表格样式 */
.cart-container :deep(.el-table) {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.cart-container :deep(.el-table__header) {
  background-color: #f8f9fa;
}

.cart-container :deep(.el-table th) {
  background-color: #f8f9fa !important;
  color: #606266;
  font-weight: 600;
}

.cart-container :deep(.el-table td) {
  border-bottom: 1px solid #f0f0f0;
  overflow: visible !important;
}

.cart-container :deep(.el-table .cell) {
  overflow: visible !important;
}

/* 商品信息样式 */
.product-info {
  display: flex;
  align-items: flex-start;
  gap: 15px;
}

.product-image-wrapper {
  position: relative;
  flex-shrink: 0;
  width: 80px;
  height: 80px;
  border-radius: 8px;
  overflow: hidden;
  margin: 8px 12px 0 0;
}

.product-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.2s;
}

.product-image:hover {
  transform: scale(1.05);
}

.stock-warning {
  position: absolute;
  top: -10px;
  right: -15px;
  z-index: 10;
  white-space: nowrap;
  font-size: 10px;
  padding: 3px 8px;
  border-radius: 10px;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  min-width: max-content;
}

.product-details {
  flex: 1;
  min-width: 0;
}

.product-name {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  line-height: 1.4;
}

.product-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

/* 价格样式 */
.price-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.current-price {
  font-weight: bold;
  color: #e74c3c;
  font-size: 16px;
}

/* 数量控制样式 */
.quantity-controls {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.quantity-input-group {
  display: flex;
  align-items: center;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
  background: white;
}

.quantity-btn {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 0;
  background: #f5f7fa;
  color: #606266;
  font-weight: bold;
  font-size: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.quantity-btn:hover:not(.is-disabled) {
  background: #409eff;
  color: white;
}

.quantity-btn:active:not(.is-disabled) {
  background: #337ecc;
}

.quantity-btn.is-disabled {
  background: #f5f7fa;
  color: #c0c4cc;
  cursor: not-allowed;
}

.quantity-input {
  width: 60px;
  text-align: center;
}

.quantity-input :deep(.el-input__wrapper) {
  border: none;
  border-radius: 0;
  box-shadow: none;
  padding: 0 8px;
}

.quantity-input :deep(.el-input__inner) {
  text-align: center;
  font-weight: 600;
  color: #2c3e50;
}

.stock-info {
  text-align: center;
  width: 100%;
  padding: 0 4px;
  min-width: 80px;
}

.stock-text {
  font-size: 12px;
  color: #909399;
  white-space: nowrap;
  display: inline-block;
  width: auto;
  text-align: center;
  min-width: max-content;
}

/* 小计样式 */
.subtotal {
  color: #e74c3c;
  font-weight: bold;
  font-size: 16px;
}

/* 底部操作栏样式 */
.cart-footer {
  margin-top: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.footer-left {
  display: flex;
  gap: 20px;
  align-items: center;
}

.select-all-checkbox {
  font-weight: 600;
}

.select-all-checkbox :deep(.el-checkbox__label) {
  color: #409eff;
  font-weight: 600;
}

.selection-info {
  color: #606266;
  font-size: 14px;
  background: rgba(64, 158, 255, 0.1);
  padding: 4px 12px;
  border-radius: 12px;
  border: 1px solid rgba(64, 158, 255, 0.3);
}

.footer-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.price-summary {
  text-align: right;
}

.selected-info {
  font-size: 14px;
  color: #606266;
  margin-bottom: 4px;
}

.total {
  font-size: 18px;
  font-weight: bold;
}

.totalPrice {
  color: #e74c3c;
  margin-left: 10px;
  font-size: 22px;
}

/* 支付选择弹窗样式 */
.payment-options {
  padding: 10px 0;
}

.order-summary {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 24px;
}

.summary-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.summary-header h4 {
  margin: 0;
  color: #2c3e50;
  font-size: 16px;
}

.summary-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  color: #606266;
}

.order-no {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-weight: 600;
  color: #409eff;
  font-size: 13px;
}

.total-row {
  border-top: 1px solid #e4e7ed;
  padding-top: 8px;
  margin-top: 8px;
  font-weight: 600;
  font-size: 16px;
  color: #2c3e50;
}

.total-amount {
  color: #e74c3c;
  font-size: 18px;
  font-weight: bold;
}

.payment-choices h4 {
  margin: 0 0 16px 0;
  color: #2c3e50;
  font-size: 16px;
}

.payment-buttons {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}

.payment-btn {
  flex: 1;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 8px;
  transition: all 0.3s;
}

.immediate-pay {
  background: linear-gradient(135deg, #409eff 0%, #67c23a 100%);
  border: none;
  color: white;
}

.immediate-pay:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(64, 158, 255, 0.3);
}

.later-pay {
  background: linear-gradient(135deg, #909399 0%, #c0c4cc 100%);
  border: none;
  color: white;
}

.later-pay:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(144, 147, 153, 0.3);
}

.payment-tips {
  background: #fff7ed;
  border: 1px solid #fed7aa;
  border-radius: 6px;
  padding: 12px;
}

.tip-item {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 8px 0;
  font-size: 13px;
  color: #92400e;
}

.tip-item:last-child {
  margin-bottom: 0;
}

.tip-item .el-icon {
  color: #f59e0b;
  font-size: 14px;
}

/* 推荐商品区域样式 */
.recommendations-section {
  margin-top: 40px;
  padding: 24px;
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
}

.section-header h3 {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 20px 0;
  color: #2c3e50;
  font-size: 20px;
}

.recommendation-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
}

.recommendation-item {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  height: 280px;
}

.recommendation-item:hover {
  transform: translateY(-8px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.recommendation-image {
  width: 100%;
  height: 160px;
  object-fit: cover;
}

.recommendation-info {
  padding: 16px;
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.product-name {
  font-size: 15px;
  color: #2c3e50;
  margin-bottom: 8px;
  font-weight: 500;
  line-height: 1.4;
}

.product-description {
  font-size: 12px;
  color: #909399;
  margin-bottom: 12px;
  line-height: 1.3;
  display: -webkit-box;
  -webkit-line-clamp: 2; 
  line-clamp: 2; 
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
}

.product-price-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.product-price {
  font-size: 18px;
  color: #e74c3c;
  font-weight: bold;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .cart-container {
    padding: 10px;
  }
  
  .cart-header h2 {
    font-size: 18px;
  }
  
  .header-right {
    gap: 10px;
  }
  
  .item-count {
    font-size: 12px;
  }
  
  .cart-footer {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .footer-right {
    justify-content: space-between;
  }
  
  .recommendation-grid {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
    gap: 15px;
  }
  
  .product-info {
    flex-direction: column;
    text-align: center;
  }
  
  .product-image {
    width: 60px;
    height: 60px;
  }

  .payment-buttons {
    flex-direction: column;
  }
  
  .payment-btn {
    height: 45px;
  }
}

/* 加载动画 */
.cart-container :deep(.el-loading-mask) {
  border-radius: 12px;
}

.image-slot {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background: #f5f7fa;
  color: #909399;
}

.image-slot .el-icon {
  font-size: 24px;
  margin-bottom: 8px;
}

.image-slot span {
  font-size: 12px;
  color: #909399;
}
</style>