<template>
  <div class="order-management">
    <div class="page-header">
      <h2>订单管理</h2>
      <div class="header-stats">
        <el-statistic title="今日订单" :value="todayOrders" />
        <el-statistic title="总订单数" :value="allOrders.length" />
        <el-statistic title="今日营业额" :value="todayRevenue" prefix="¥" />
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <div class="filter-section">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="订单号" class="search-item">
          <el-input
            v-model="searchForm.orderNumber"
            placeholder="请输入订单号"
            clearable
            @input="handleLocalSearch"
            @clear="handleLocalSearch"
            class="search-input"
            prefix-icon="Search"
          />
        </el-form-item>
        <el-form-item label="用户ID" class="search-item">
          <el-input
            v-model="searchForm.userId"
            placeholder="请输入用户ID"
            clearable
            @input="handleLocalSearch"
            @clear="handleLocalSearch"
            class="search-input"
            prefix-icon="User"
          />
        </el-form-item>
        <el-form-item label="订单状态" class="search-item">
          <el-select 
            v-model="searchForm.status" 
            placeholder="请选择状态" 
            clearable 
            @change="handleLocalSearch"
            class="status-select"
          >
            <el-option label="待付款" value="0" />
            <el-option label="待发货" value="1" />
            <el-option label="待收货" value="2" />
            <el-option label="已完成" value="3" />
            <el-option label="已取消" value="4" />
          </el-select>
        </el-form-item>
        <el-form-item class="search-actions">
          <el-button @click="resetSearch" :disabled="loading" class="reset-btn">
            <el-icon><RefreshLeft /></el-icon>
            重置
          </el-button>
          <el-button type="success" @click="exportOrders" :loading="loading" class="export-btn">
            <el-icon><Download /></el-icon>
            <span v-if="!loading">导出Excel</span>
            <span v-else>导出中...</span>
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 订单表格 -->
    <div class="table-section">
      <div class="table-header">
        <div class="table-title">
          <h3>订单列表</h3>
          <el-tag v-if="hasActiveSearch" type="primary" class="filter-tag">
            <el-icon><Filter /></el-icon>
            已筛选：{{ filteredOrders.length }} / {{ allOrders.length }} 条记录
          </el-tag>
        </div>
      </div>
      
      <el-table 
        :data="paginatedOrders" 
        style="width: 100%"
        v-loading="loading"
        :empty-text="hasActiveSearch ? '没有找到匹配的订单' : '暂无订单数据'"
        class="orders-table"
      >
        <el-table-column prop="orderNo" label="订单编号" width="280" show-overflow-tooltip>
          <template #default="scope">
            <div class="order-no-cell">
              <el-tooltip :content="scope.row.orderNo" placement="top">
                <span class="order-no-text">{{ scope.row.orderNo }}</span>
              </el-tooltip>
              <el-button 
                size="small" 
                text 
                @click="copyOrderNo(scope.row.orderNo)"
                class="copy-btn"
                title="复制订单号"
              >
                <el-icon><DocumentCopy /></el-icon>
              </el-button>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="userId" label="用户ID" width="100" align="center">
          <template #default="scope">
            <el-tag size="small" type="info" round>{{ scope.row.userId }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="商品名称" min-width="120">
          <template #default="scope">
            <div class="order-items">
              <div v-if="scope.row.orderItems && scope.row.orderItems.length > 0">
                <div v-for="item in scope.row.orderItems.slice(0, 2)" :key="item.itemId || item.goodsId" class="order-item">
                  <div class="item-info">
                    <el-image
                      :src="getImageUrl(item.imgUrl)"
                      style="width: 30px; height: 30px; margin-right: 8px; border-radius: 4px;"
                      fit="cover"
                      :alt="item.goodsName"
                      loading="lazy"
                    >
                      <template #error>
                        <div class="image-placeholder">📦</div>
                      </template>
                      <template #placeholder>
                        <div class="image-placeholder loading">⏳</div>
                      </template>
                    </el-image>
                    <span class="goods-name">{{ item.goodsName || '商品名称获取中...' }}</span>
                  </div>
                </div>
                <span v-if="scope.row.orderItems.length > 2" class="more-items">
                  等{{ scope.row.orderItems.length }}件商品
                </span>
              </div>
              <span v-else class="no-items">商品信息加载中...</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="订单金额" width="120" sortable align="left">
          <template #default="scope">
            <span class="amount">¥{{ formatPrice(scope.row.totalAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="订单状态" width="110" align="center">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.orderState)" size="small" round>
              {{ getStatusText(scope.row.orderState) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" width="160" sortable align="center">
          <template #default="scope">
            <div class="time-cell">
              <div class="time-date">{{ formatDate(scope.row.createTime) }}</div>
              <div class="time-hour">{{ formatTime(scope.row.createTime) }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300" fixed="right" align="center">
          <template #default="scope">
            <div class="action-buttons">
              <el-button size="small" @click="viewOrder(scope.row)" class="action-btn">
                <el-icon><View /></el-icon>
                查看详情
              </el-button>
              <el-button 
                v-if="scope.row.orderState === 1" 
                size="small" 
                type="primary"
                @click="shipOrder(scope.row)"
                class="action-btn"
              >
                <el-icon><Van /></el-icon>
                发货
              </el-button>
              <el-button 
                v-if="scope.row.hasComments"
                size="small" 
                type="info"
                @click="viewComments(scope.row)"
                class="action-btn"
              >
                <el-icon><ChatDotRound /></el-icon>
                查看评论
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination" v-if="filteredOrders.length > 0">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="filteredOrders.length"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          background
        />
      </div>
    </div>

    <!-- 订单详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="订单详情"
      width="800px"
      destroy-on-close
    >
      <div v-if="selectedOrder" class="order-detail">
        <div class="detail-section">
          <h3>订单信息</h3>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="订单号">{{ selectedOrder.orderNo }}</el-descriptions-item>
            <el-descriptions-item label="用户ID">{{ selectedOrder.userId }}</el-descriptions-item>
            <el-descriptions-item label="订单状态">
              <el-tag :type="getStatusType(selectedOrder.orderState)">
                {{ getStatusText(selectedOrder.orderState) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="订单金额">¥{{ formatPrice(selectedOrder.totalAmount) }}</el-descriptions-item>
            <el-descriptions-item label="下单时间">{{ selectedOrder.createTime }}</el-descriptions-item>
            <el-descriptions-item label="支付时间" v-if="selectedOrder.payTime">{{ selectedOrder.payTime }}</el-descriptions-item>
            <el-descriptions-item label="发货时间" v-if="selectedOrder.deliveryTime">{{ selectedOrder.deliveryTime }}</el-descriptions-item>
            <el-descriptions-item label="完成时间" v-if="selectedOrder.completeTime">{{ selectedOrder.completeTime }}</el-descriptions-item>
            <el-descriptions-item label="收货人姓名">{{ selectedOrder.receiverName }}</el-descriptions-item>
            <el-descriptions-item label="收货人电话">{{ selectedOrder.receiverPhone }}</el-descriptions-item>
            <el-descriptions-item label="收货地址" :span="2">{{ selectedOrder.receiverAddress }}</el-descriptions-item>
            <el-descriptions-item label="备注" :span="2" v-if="selectedOrder.remark">{{ selectedOrder.remark }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="detail-section">
          <h3>商品列表</h3>
          <el-table :data="orderItemsWithDetails" style="width: 100%" v-loading="itemsLoading">
            <el-table-column label="商品图片" width="80">
              <template #default="scope">
                <el-image
                  style="width: 50px; height: 50px"
                  :src="getImageUrl(scope.row.goodsDetail?.imgUrl || scope.row.imgUrl)"
                  fit="cover"
                  :alt="scope.row.goodsDetail?.name || scope.row.goodsName"
                  loading="lazy"
                >
                  <template #error>
                    <div class="image-placeholder">📦</div>
                  </template>
                  <template #placeholder>
                    <div class="image-placeholder loading">⏳</div>
                  </template>
                </el-image>
              </template>
            </el-table-column>
            <el-table-column label="商品名称">
              <template #default="scope">
                {{ scope.row.goodsDetail?.name || scope.row.goodsName }}
              </template>
            </el-table-column>
            <el-table-column prop="buyPrice" label="单价" width="100">
              <template #default="scope">
                ¥{{ formatPrice(scope.row.buyPrice) }}
              </template>
            </el-table-column>
            <el-table-column prop="quantity" label="数量" width="80" />
            <el-table-column label="小计" width="100">
              <template #default="scope">
                ¥{{ formatPrice(scope.row.buyPrice * scope.row.quantity) }}
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </el-dialog>

    <!-- 评论对话框 -->
    <el-dialog
      v-model="commentsDialogVisible"
      :title="`用户评论 - 订单号: ${currentViewingOrder?.orderNo || ''} - 用户ID: ${currentViewingOrder?.userId || ''}`"
      width="700px"
      destroy-on-close
    >
      <div v-loading="commentsLoading">
        <!-- 订单和用户信息 -->
        <div v-if="currentViewingOrder" class="order-user-info">
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="订单号">{{ currentViewingOrder.orderNo }}</el-descriptions-item>
            <el-descriptions-item label="用户ID">{{ currentViewingOrder.userId }}</el-descriptions-item>
            <el-descriptions-item label="订单金额">¥{{ formatPrice(currentViewingOrder.totalAmount) }}</el-descriptions-item>
            <el-descriptions-item label="下单时间">{{ currentViewingOrder.createTime }}</el-descriptions-item>
          </el-descriptions>
        </div>
        
        <div v-if="comments.length === 0 && !commentsLoading" class="no-comments">
          <div class="empty-icon">💬</div>
          <p>该用户尚未对此订单中的商品进行评价</p>
          <p class="no-comments-hint">用户需要在订单完成后才能对商品进行评价</p>
        </div>
        <div v-else class="comments-list">
          <div v-for="comment in comments" :key="comment.commentId" class="comment-item">
            <div class="comment-header">
              <span class="username">用户ID: {{ comment.userId }}</span>
              <el-rate 
                :model-value="comment.rating || 5" 
                disabled 
                show-score 
                text-color="#ff9900"
                void-color="#f5f5f5"
                disabled-void-color="#f5f5f5"
              />
              <span class="comment-time">{{ formatCommentTime(comment.createTime) }}</span>
            </div>
            <div class="comment-content user-comment">
              {{ comment.content || '用户未留言' }}
            </div>
            <div class="goods-info">
              <i class="goods-icon">📦</i>
              商品: {{ comment.goodsName }}
            </div>
          </div>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="commentsDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Search, 
  User, 
  RefreshLeft, 
  Download, 
  Filter, 
  View, 
  Van, 
  ChatDotRound,
  DocumentCopy 
} from '@element-plus/icons-vue'
import axios from 'axios'

interface OrderItem {
  itemId: number
  orderId: number
  goodsId: number
  goodsName: string
  buyPrice: number
  quantity: number
  goodsDetail?: any
  imgUrl?: string
  desc?: string
  price?: number
  currentPrice?: number
  kgs?: number
  size?: string
  creationdate?: string
  expirationdate?: string
  storagemethod?: string
}

interface Order {
  orderId: number
  orderNo: string
  userId: number
  orderState: number
  createTime: string
  payTime?: string
  deliveryTime?: string
  completeTime?: string
  totalAmount: number
  receiverName: string
  receiverPhone: string
  receiverAddress: string
  remark?: string
  orderItems: OrderItem[]
  hasComments?: boolean
}

interface SearchForm {
  orderNumber: string
  userId: string
  status: string
}

interface Comment {
  commentId: number
  userId: number
  goodsId: number
  rating: number
  content: string
  createTime: string
  goodsName: string
}

// 响应式数据
const loading = ref(false)
const itemsLoading = ref(false)
const commentsLoading = ref(false)
const detailDialogVisible = ref(false)
const commentsDialogVisible = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const selectedOrder = ref<Order | null>(null)
const orderItemsWithDetails = ref<OrderItem[]>([])
const comments = ref<Comment[]>([])
const currentViewingOrder = ref<Order | null>(null)

// 所有订单数据
const allOrders = ref<Order[]>([])

// 统计数据
const todayOrders = ref(0)
const todayRevenue = ref(0)

const searchForm = ref<SearchForm>({
  orderNumber: '',
  userId: '',
  status: ''
})

// 图片URL处理函数
const getImageUrl = (imgUrl: string | null | undefined): string => {
  if (!imgUrl) {
    return '/placeholder-image.png'
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

// 价格格式化函数
const formatPrice = (price: number | string): string => {
  const numPrice = typeof price === 'string' ? parseFloat(price) : price
  if (isNaN(numPrice)) return '0.00'
  return numPrice.toFixed(2)
}

// 计算属性：是否有激活的搜索条件
const hasActiveSearch = computed(() => {
  return !!(searchForm.value.orderNumber || searchForm.value.userId || searchForm.value.status)
})

// 计算属性：筛选后的订单
const filteredOrders = computed(() => {
  let filtered = allOrders.value

  // 按订单号筛选
  if (searchForm.value.orderNumber && searchForm.value.orderNumber.trim()) {
    const orderNo = searchForm.value.orderNumber.trim().toLowerCase()
    filtered = filtered.filter(order => 
      order.orderNo.toLowerCase().includes(orderNo)
    )
  }

  // 按用户ID筛选
  if (searchForm.value.userId && searchForm.value.userId.trim()) {
    const userId = searchForm.value.userId.trim()
    filtered = filtered.filter(order => 
      order.userId.toString().includes(userId)
    )
  }

  // 按状态筛选
  if (searchForm.value.status) {
    const status = parseInt(searchForm.value.status)
    filtered = filtered.filter(order => order.orderState === status)
  }

  return filtered
})

// 计算属性：分页后的订单
const paginatedOrders = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredOrders.value.slice(start, end)
})

// 获取今日统计数据
const loadTodayStats = async () => {
  try {
    const todayStatsResponse = await axios.get('/api/api/orders/admin/today-stats')
    todayOrders.value = todayStatsResponse.data.todayOrders || 0
    todayRevenue.value = todayStatsResponse.data.todayRevenue || 0
  } catch (error) {
    // 如果统计接口失败，保持默认值
    todayOrders.value = 0
    todayRevenue.value = 0
  }
}

// 主要的数据加载函数
const loadOrders = async () => {
  loading.value = true
  try {
    // 获取今日统计数据
    await loadTodayStats()
    
    // 分页获取所有订单数据
    let currentPageNum = 0
    let hasMore = true
    const allOrdersTemp = []
    const processedOrderNos = new Set() // 防止重复数据
    
    while (hasMore) {
      const response = await axios.get('/api/api/orders/admin/list', {
        params: { page: currentPageNum, size: 10 }
      })
      
      const batchData = response.data.records || []
      const currentResponsePage = response.data.current || 0
      const totalPages = response.data.pages || 0
      
      // 过滤重复订单
      const uniqueOrders = batchData.filter((order: { orderNo: unknown }) => {
        if (processedOrderNos.has(order.orderNo)) {
          return false
        }
        processedOrderNos.add(order.orderNo)
        return true
      })
      
      allOrdersTemp.push(...uniqueOrders)
      
      // 判断是否继续
      if (batchData.length === 0 || currentResponsePage >= totalPages) {
        hasMore = false
      } else {
        currentPageNum++
      }
      
      // 安全限制
      if (currentPageNum >= 100) {
        hasMore = false
      }
    }
    
    allOrders.value = allOrdersTemp
    
    // 获取订单商品信息
    if (allOrders.value.length > 0) {
      await fetchOrdersWithItems()
    }
    
  } catch (error: any) {
    if (error.response?.status === 500) {
      ElMessage.error('服务器内部错误，请稍后重试')
    } else if (error.response?.status === 404) {
      ElMessage.error('API接口不存在，请检查后端服务')
    } else {
      ElMessage.error('加载订单数据失败')
    }
    allOrders.value = []
  } finally {
    loading.value = false
  }
}

// 获取订单的商品信息
const fetchOrdersWithItems = async () => {
  const orderNos = [...new Set(allOrders.value.map(order => order.orderNo))]
  
  for (const orderNo of orderNos) {
    try {
      const response = await axios.get(`/api/api/orders/detail/${orderNo}`)
      
      if (response.data && response.data.orderItems) {
        const orderIndex = allOrders.value.findIndex(o => o.orderNo === orderNo)
        if (orderIndex !== -1) {
          const enrichedItems = await enrichOrderItemsWithGoodsDetail(response.data.orderItems)
          allOrders.value[orderIndex].orderItems = enrichedItems
          
          const orderUserId = allOrders.value[orderIndex].userId
          const hasComments = await checkOrderHasComments(enrichedItems, orderUserId)
          allOrders.value[orderIndex].hasComments = hasComments
        }
      }
    } catch (error) {
      // 静默处理单个订单的错误
      continue
    }
  }
}

// 检查订单是否有评论
const checkOrderHasComments = async (orderItems: OrderItem[], userId: number) => {
  try {
    // 首先尝试通过用户评论API获取
    const response = await axios.get(`/api/api/comments/user/${userId}`)
    const userComments = response.data
    
    let comments = []
    if (Array.isArray(userComments)) {
      comments = userComments
    } else if (userComments.records && Array.isArray(userComments.records)) {
      comments = userComments.records
    }
    
    // 检查是否有该订单中商品的评论
    for (const item of orderItems) {
      const hasComment = comments.some((comment: any) => comment.goodsId === item.goodsId)
      if (hasComment) {
        return true
      }
    }
    
    return false
  } catch (error) {
    // 如果用户评论API失败，尝试商品评论API
    try {
      for (const item of orderItems) {
        const response = await axios.get(`/api/api/comments/goods/${item.goodsId}`)
        const commentsData = response.data
        
        if (commentsData) {
          let allComments = []
          if (commentsData.comments && commentsData.comments.records) {
            allComments = commentsData.comments.records
          } else if (commentsData.records && Array.isArray(commentsData.records)) {
            allComments = commentsData.records
          } else if (Array.isArray(commentsData)) {
            allComments = commentsData
          }
          
          const hasUserComment = allComments.some((comment: any) => comment.userId === userId)
          if (hasUserComment) {
            return true
          }
        }
      }
    } catch (error) {
      // 静默处理错误
    }
    
    return false
  }
}

// 获取商品详细信息
const fetchGoodsDetail = async (goodsId: number): Promise<any> => {
  try {
    const response = await axios.get(`/api/api/goods/${goodsId}`)
    if (response.data) {
      return response.data
    }
  } catch (error) {
    // 静默处理错误
  }
  return null
}

// 为订单商品补充详细信息
const enrichOrderItemsWithGoodsDetail = async (orderItems: OrderItem[]): Promise<OrderItem[]> => {
  const enrichedItems = await Promise.all(
    orderItems.map(async (item) => {
      const goodsDetail = await fetchGoodsDetail(item.goodsId)
      if (goodsDetail) {
        return {
          ...item,
          goodsName: goodsDetail.name || item.goodsName,
          imgUrl: goodsDetail.imgUrl || item.imgUrl,
          buyPrice: item.buyPrice || goodsDetail.price,
          quantity: item.quantity,
          currentPrice: goodsDetail.price,
          desc: goodsDetail.desc,
          kgs: goodsDetail.kgs,
          size: goodsDetail.size,
          creationdate: goodsDetail.creationdate,
          expirationdate: goodsDetail.expirationdate,
          storagemethod: goodsDetail.storagemethod
        }
      }
      return {
        ...item,
        buyPrice: item.buyPrice || item.price || 0
      }
    })
  )
  return enrichedItems
}

// 生命周期
onMounted(() => {
  loadOrders()
})

// 工具函数
const getStatusType = (status: number) => {
  const types = {
    0: 'warning',  // 待付款
    1: 'info',     // 待发货
    2: 'primary',  // 待收货
    3: 'success',  // 已完成
    4: 'danger'    // 已取消
  }
  return types[status as keyof typeof types] || 'info'
}

const getStatusText = (status: number | string) => {
  const statusNum = typeof status === 'string' ? parseInt(status) : status
  const texts = {
    0: '待付款',
    1: '待发货',
    2: '待收货',
    3: '已完成',
    4: '已取消'
  }
  return texts[statusNum as keyof typeof texts] || '未知状态'
}

const formatCommentTime = (timeStr: string) => {
  if (!timeStr) return '-'
  try {
    return new Date(timeStr).toLocaleString('zh-CN')
  } catch (error) {
    return timeStr
  }
}

const formatDate = (timeStr: string) => {
  if (!timeStr) return '-'
  try {
    return new Date(timeStr).toLocaleDateString('zh-CN')
  } catch (error) {
    return timeStr
  }
}

const formatTime = (timeStr: string) => {
  if (!timeStr) return '-'
  try {
    return new Date(timeStr).toLocaleTimeString('zh-CN', { 
      hour: '2-digit', 
      minute: '2-digit' 
    })
  } catch (error) {
    return timeStr
  }
}

// 复制订单号
const copyOrderNo = async (orderNo: string) => {
  try {
    await navigator.clipboard.writeText(orderNo)
    ElMessage.success('订单号已复制到剪贴板')
  } catch (error) {
    // 降级处理
    const textArea = document.createElement('textarea')
    textArea.value = orderNo
    document.body.appendChild(textArea)
    textArea.select()
    document.execCommand('copy')
    document.body.removeChild(textArea)
    ElMessage.success('订单号已复制到剪贴板')
  }
}

// 事件处理函数
const handleLocalSearch = () => {
  currentPage.value = 1
}

const resetSearch = () => {
  searchForm.value = {
    orderNumber: '',
    userId: '',
    status: ''
  }
  currentPage.value = 1
}

const handleSizeChange = (val: number) => {
  pageSize.value = val
  currentPage.value = 1
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
}

// 查看订单详情
const viewOrder = async (order: Order) => {
  selectedOrder.value = order
  detailDialogVisible.value = true
  
  if (order.orderItems && order.orderItems.length > 0) {
    orderItemsWithDetails.value = order.orderItems
  }
  
  try {
    itemsLoading.value = true
    
    const response = await axios.get(`/api/api/orders/detail/${order.orderNo}`)
    const orderDetail = response.data
    selectedOrder.value = { ...selectedOrder.value, ...orderDetail }
    
    if (orderDetail.orderItems && orderDetail.orderItems.length > 0) {
      const enrichedItems = await enrichOrderItemsWithGoodsDetail(orderDetail.orderItems)
      orderItemsWithDetails.value = enrichedItems
    } else if (!orderItemsWithDetails.value.length) {
      orderItemsWithDetails.value = order.orderItems || []
    }
    
  } catch (error) {
    ElMessage.error('获取订单详情失败')
    
    if (!orderItemsWithDetails.value.length) {
      orderItemsWithDetails.value = selectedOrder.value?.orderItems || []
    }
  } finally {
    itemsLoading.value = false
  }
}

// 发货处理
const shipOrder = async (order: Order) => {
  try {
    await ElMessageBox.confirm(`确定要发货订单吗？`, '提示', {
      confirmButtonText: '确定发货',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    try {
      await axios.post(`/api/api/orders/deliver/${order.orderNo}`)
      ElMessage.success('订单发货成功')
      loadOrders()
    } catch (error) {
      ElMessage.error('发货失败')
    }
  } catch (error) {
    // 用户取消操作
  }
}

// 查看评论
const viewComments = async (order: Order) => {
  currentViewingOrder.value = order
  commentsDialogVisible.value = true
  commentsLoading.value = true
  comments.value = []
  
  try {
    if (!order?.orderItems || order.orderItems.length === 0) {
      ElMessage.warning('该订单没有商品信息')
      return
    }
    
    // 获取该用户的所有评论
    let userComments = []
    
    try {
      const userCommentsResponse = await axios.get(`/api/api/comments/user/${order.userId}`)
      const userCommentsData = userCommentsResponse.data
      
      if (userCommentsData) {
        if (Array.isArray(userCommentsData)) {
          userComments = userCommentsData
        } else if (userCommentsData.records && Array.isArray(userCommentsData.records)) {
          userComments = userCommentsData.records
        }
      }
    } catch (error) {
      // 用户评论API失败时的处理
    }
    
    if (userComments.length > 0) {
      const orderComments = []
      
      for (const item of order.orderItems) {
        const itemComment = userComments.find((comment: any) => comment.goodsId === item.goodsId)
        if (itemComment) {
          orderComments.push({
            ...itemComment,
            goodsName: item.goodsName
          })
        }
      }
      
      comments.value = orderComments
      
      if (orderComments.length === 0) {
        ElMessage.info('该用户尚未对此订单中的商品进行评价')
      }
    } else {
      // 通过商品评论API获取并筛选
      const allComments = []
      
      for (const item of order.orderItems) {
        try {
          const response = await axios.get(`/api/api/comments/goods/${item.goodsId}`)
          const responseData = response.data
          
          let goodsComments = []
          if (responseData) {
            if (responseData.comments && responseData.comments.records && Array.isArray(responseData.comments.records)) {
              goodsComments = responseData.comments.records
            } else if (responseData.records && Array.isArray(responseData.records)) {
              goodsComments = responseData.records
            } else if (Array.isArray(responseData)) {
              goodsComments = responseData
            }
          }
          
          const userCommentsForItem = goodsComments.filter((comment: any) => 
            comment.userId === order.userId
          )
          
          if (userCommentsForItem.length > 0) {
            const commentsWithGoodsName = userCommentsForItem.map((comment: any) => ({
              ...comment,
              goodsName: item.goodsName,
              commentId: comment.commentId || `comment_${Date.now()}_${Math.random()}`,
              userId: comment.userId,
              rating: comment.rating || 5,
              content: comment.content || '用户未留言',
              createTime: comment.createTime || new Date().toISOString().slice(0, 19).replace('T', ' ')
            }))
            allComments.push(...commentsWithGoodsName)
          }
        } catch (error) {
          // 静默处理单个商品的错误
          continue
        }
      }
      
      comments.value = allComments
      
      if (allComments.length === 0) {
        ElMessage.info('该用户尚未对此订单中的商品进行评价')
      }
    }
  } catch (error) {
    ElMessage.error('获取评论失败')
  } finally {
    commentsLoading.value = false
  }
}

// 导出订单
const exportOrders = async () => {
  try {
    loading.value = true
    
    const ordersToExport = hasActiveSearch.value ? filteredOrders.value : allOrders.value
    
    if (ordersToExport.length === 0) {
      ElMessage.warning('没有数据可导出')
      return
    }

    // 创建CSV内容
    const headers = ['订单编号', '用户ID', '商品名称', '订单金额', '订单状态', '下单时间', '收货人', '收货电话', '收货地址']
    const csvContent = [
      headers.join(','),
      ...ordersToExport.map((order: Order) => [
        order.orderNo,
        order.userId,
        (order.orderItems || []).map(item => item.goodsName).join(';'),
        formatPrice(order.totalAmount),
        getStatusText(order.orderState),
        order.createTime,
        order.receiverName || '',
        order.receiverPhone || '',
        `"${order.receiverAddress || ''}"`
      ].join(','))
    ].join('\n')

    // 下载文件
    const blob = new Blob(['\ufeff' + csvContent], { type: 'text/csv;charset=utf-8;' })
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = `订单数据_${new Date().toISOString().slice(0, 10)}.csv`
    link.click()
    
    ElMessage.success(`导出成功！共导出${ordersToExport.length}条记录`)
  } catch (error) {
    ElMessage.error('导出失败，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.order-management {
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 24px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
}

.page-header h2 {
  margin: 0;
  color: #2c3e50;
  font-size: 1.8em;
  font-weight: 600;
}

.header-stats {
  display: flex;
  gap: 32px;
}

.filter-section {
  background: white;
  padding: 24px;
  border-radius: 16px;
  margin-bottom: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
}

.search-form {
  margin: 0;
  display: flex;
  align-items: end;
  gap: 16px;
  flex-wrap: wrap;
}

.search-item {
  margin-bottom: 0 !important;
}

.search-input {
  width: 220px;
}

.search-input :deep(.el-input__inner) {
  border-radius: 10px;
  border: 2px solid #e2e8f0;
  transition: all 0.3s ease;
  font-size: 14px;
  padding: 0 12px;
}

.search-input :deep(.el-input__inner:focus) {
  border-color: #4fabb0;
  box-shadow: 0 0 0 3px rgba(79, 171, 176, 0.1);
}

.status-select {
  width: 160px;
}

.status-select :deep(.el-select__wrapper) {
  border-radius: 10px;
  border: 2px solid #e2e8f0;
  transition: all 0.3s ease;
  padding: 0 12px;
}

.status-select :deep(.el-select__wrapper:hover) {
  border-color: #4fabb0;
}

.status-select :deep(.el-select__wrapper.is-focused) {
  border-color: #4fabb0;
  box-shadow: 0 0 0 3px rgba(79, 171, 176, 0.1);
}

.search-actions {
  margin-bottom: 0 !important;
  margin-left: auto;
}

.reset-btn, .export-btn {
  border-radius: 10px;
  padding: 10px 20px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.reset-btn {
  background: #f1f5f9;
  border: 2px solid #e2e8f0;
  color: #64748b;
}

.reset-btn:hover {
  background: #e2e8f0;
  border-color: #cbd5e1;
  transform: translateY(-1px);
}

.export-btn {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  border: none;
  color: white;
}

.export-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.4);
}

.table-section {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.table-header {
  padding: 20px 24px;
  border-bottom: 1px solid #f1f5f9;
  background: #f8fafc;
}

.table-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.table-title h3 {
  margin: 0;
  color: #1e293b;
  font-size: 1.2em;
  font-weight: 600;
}

.filter-tag {
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
  border: none;
  color: white;
  padding: 6px 12px;
  border-radius: 8px;
  font-size: 12px;
}

.orders-table {
  --el-table-border-color: #f1f5f9;
  --el-table-header-bg-color: #f8fafc;
  --el-table-header-text-color: #374151;
  --el-table-row-hover-bg-color: #f8fafc;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.orders-table :deep(.el-table__header) {
  font-weight: 600;
}

.orders-table :deep(.el-table__header th) {
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-bottom: 2px solid #e2e8f0;
  font-size: 13px;
  color: #1e293b;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.orders-table :deep(.el-table__body tr) {
  transition: all 0.2s ease;
}

.orders-table :deep(.el-table__body tr:hover > td) {
  background-color: #f0f9ff !important;
  transform: scale(1.002);
}

.orders-table :deep(.el-table__row--striped td) {
  background-color: #fafbfc;
}

.orders-table :deep(.el-table__cell) {
  padding: 12px 8px;
  border-bottom: 1px solid #f1f5f9;
}

.order-no-cell {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.order-no-text {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-family: 'Courier New', monospace;
  font-size: 13px;
  color: #1e40af;
  background: #f0f9ff;
  padding: 4px 8px;
  border-radius: 4px;
  border: 1px solid #e0f2fe;
}

.copy-btn {
  padding: 4px;
  margin: 0;
  background: transparent;
  border: none;
  color: #64748b;
  cursor: pointer;
  border-radius: 4px;
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.copy-btn:hover {
  background: #f1f5f9;
  color: #1e40af;
  transform: scale(1.1);
}

.order-items {
  font-size: 14px;
}

.order-item {
  margin-bottom: 6px;
  color: #666;
}

.item-info {
  display: flex;
  align-items: center;
}

.goods-name {
  flex: 1;
  word-break: break-word;
  font-size: 13px;
  line-height: 1.4;
}

.image-placeholder {
  width: 30px;
  height: 30px;
  background: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  margin-right: 8px;
  font-size: 12px;
}

.image-placeholder.loading {
  background: linear-gradient(90deg, #f0f0f0 0%, #e0e0e0 50%, #f0f0f0 100%);
  animation: shimmer 1.5s infinite;
}

@keyframes shimmer {
  0% { background-position: -100px 0; }
  100% { background-position: 100px 0; }
}

.more-items {
  color: #9ca3af;
  font-style: italic;
  font-size: 11px;
  margin-top: 4px;
  display: block;
}

.no-items {
  color: #9ca3af;
  font-style: italic;
  font-size: 12px;
}

.amount {
  color: #dc2626;
  font-weight: 700;
  font-size: 14px;
}

.amount-cell {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 8px 12px;
  background: linear-gradient(135deg, #fef2f2 0%, #fee2e2 100%);
  border-radius: 8px;
  border-left: 3px solid #dc2626;
}

.time-cell {
  text-align: center;
  line-height: 1.2;
}

.time-date {
  color: #374151;
  font-weight: 600;
  font-size: 13px;
  margin-bottom: 2px;
}

.time-hour {
  color: #6b7280;
  font-size: 11px;
  font-weight: 500;
}

.action-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: center;
  align-items: center;
}

.action-btn {
  border-radius: 8px;
  font-size: 12px;
  padding: 6px 12px;
  transition: all 0.3s ease;
  font-weight: 500;
  border: 1px solid transparent;
  display: flex;
  align-items: center;
  gap: 4px;
}

.action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.action-btn .el-icon {
  font-size: 14px;
}

.pagination {
  padding: 20px;
  display: flex;
  justify-content: center;
  background: #f8fafc;
  border-top: 1px solid #f1f5f9;
}

.order-detail {
  padding: 10px 0;
}

.detail-section {
  margin-bottom: 30px;
}

.detail-section h3 {
  margin: 0 0 15px 0;
  color: #2c3e50;
  font-size: 16px;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
}

.no-comments {
  text-align: center;
  color: #999;
  padding: 40px 0;
}

.empty-icon {
  font-size: 3em;
  margin-bottom: 16px;
}

.no-comments-hint {
  color: #999;
  font-size: 12px;
  margin-top: 8px;
  font-style: italic;
}

.comments-list {
  max-height: 400px;
  overflow-y: auto;
}

.order-user-info {
  margin-bottom: 20px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 12px;
  border-left: 4px solid #4fabb0;
}

.comment-item {
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.2s ease;
}

.comment-item:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.comment-item:last-child {
  margin-bottom: 0;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 12px;
  flex-wrap: wrap;
  padding-bottom: 8px;
  border-bottom: 1px solid #f1f5f9;
}

.username {
  font-weight: bold;
  color: #1e40af;
  background: #dbeafe;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.comment-time {
  color: #64748b;
  font-size: 12px;
  margin-left: auto;
  background: #f1f5f9;
  padding: 2px 8px;
  border-radius: 4px;
}

.comment-content.user-comment {
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border-left: 3px solid #0ea5e9;
  color: #0c4a6e;
  font-weight: 500;
  padding: 12px 16px;
  border-radius: 8px;
  margin: 8px 0;
  line-height: 1.6;
}

.goods-info {
  color: #64748b;
  font-size: 13px;
  font-style: italic;
  background: #f8fafc;
  padding: 8px 12px;
  border-radius: 6px;
  margin-top: 8px;
  border-left: 3px solid #94a3b8;
}

.goods-icon {
  margin-right: 6px;
  font-size: 14px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .search-form {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .search-actions {
    margin-left: 0;
    align-self: center;
  }
  
  .search-input {
    width: 100%;
  }
  
  .status-select {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .order-management {
    padding: 12px;
  }
  
  .page-header {
    flex-direction: column;
    gap: 16px;
    padding: 20px;
  }
  
  .header-stats {
    flex-direction: column;
    gap: 16px;
    width: 100%;
  }
  
  .filter-section {
    padding: 20px;
  }
  
  .table-title {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
  
  .action-buttons {
    flex-direction: column;
    gap: 4px;
  }
  
  .action-btn {
    width: 100%;
    justify-content: center;
  }
  
  .comment-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .comment-time {
    margin-left: 0;
    align-self: flex-end;
  }
}

/* Element Plus 组件样式覆盖 */
:deep(.el-statistic__number) {
  color: #1e293b;
  font-weight: 700;
}

:deep(.el-statistic__title) {
  color: #64748b;
  font-weight: 500;
}

:deep(.el-button) {
  font-weight: 500;
}

:deep(.el-tag) {
  border: none;
  font-weight: 500;
}

:deep(.el-form-item__label) {
  color: #374151;
  font-weight: 500;
}

:deep(.el-pagination) {
  --el-pagination-button-bg-color: white;
  --el-pagination-hover-color: #4fabb0;
}
</style>