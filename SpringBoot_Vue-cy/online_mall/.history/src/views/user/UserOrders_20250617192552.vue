<template>
  <div class="orders-container">
    <div class="page-header">
      <h1>我的订单</h1>
      <p>管理您的所有订单信息</p>
    </div>
    
    <!-- 订单筛选 -->
    <div class="filter-section">
      <div class="filter-tabs">
        <div 
          class="filter-tab" 
          :class="{ active: filterState === null }"
          @click="handleFilterChange(null)"
        >
          <i class="filter-icon">📋</i>
          全部订单
        </div>
        <div 
          class="filter-tab" 
          :class="{ active: filterState === 0 }"
          @click="handleFilterChange(0)"
        >
          <i class="filter-icon">⏳</i>
          待处理
        </div>
        <div 
          class="filter-tab" 
          :class="{ active: filterState === 1 }"
          @click="handleFilterChange(1)"
        >
          <i class="filter-icon">📦</i>
          待发货
        </div>
        <div 
          class="filter-tab" 
          :class="{ active: filterState === 2 }"
          @click="handleFilterChange(2)"
        >
          <i class="filter-icon">🚚</i>
          待收货
        </div>
        <div 
          class="filter-tab" 
          :class="{ active: filterState === 3 }"
          @click="handleFilterChange(3)"
        >
          <i class="filter-icon">✅</i>
          已完成
        </div>
        <div 
          class="filter-tab" 
          :class="{ active: filterState === 4 }"
          @click="handleFilterChange(4)"
        >
          <i class="filter-icon">❌</i>
          已取消
        </div>
      </div>
    </div>

    <div class="orders-content">
      <!-- 订单列表 -->
      <div v-if="groupedOrders.length > 0" class="orders-list">
        <div 
          v-for="orderGroup in groupedOrders" 
          :key="orderGroup.orderNo"
          class="order-group-card"
        >
          <!-- 订单组头部信息 -->
          <div class="order-group-header">
            <div class="order-info">
              <div class="order-no">
                <i class="icon">🧾</i>
                订单号：{{ orderGroup.orderNo }}
              </div>
              <div class="order-time">
                <i class="icon">🕒</i>
                下单时间：{{ formatTime(orderGroup.createTime) }}
              </div>
            </div>
            <div class="order-status-info">
              <!-- 只有未取消的订单才显示支付状态 -->
              <div v-if="orderGroup.orderState !== 4" class="pay-status" :class="getPayStatusClass(orderGroup.payState)">
                {{ getPayStatusText(orderGroup.payState) }}
              </div>
              <div class="order-status" :class="getOrderStatusClass(orderGroup.orderState)">
                {{ getOrderStatusText(orderGroup.orderState) }}
              </div>
            </div>
          </div>

          <!-- 订单商品列表 -->
          <div class="order-items-list">
            <div 
              v-for="item in getSortedItems(orderGroup)" 
              :key="item.itemId"
              class="order-item-card"
            >
              <div class="item-content">
                <!-- 商品图片 -->
                <div class="item-image">
                  <el-image 
                    :src="getImageUrl(item.imgUrl)"
                    fit="cover"
                    class="product-img"
                  >
                    <template #error>
                      <div class="image-error">
                        <el-icon size="24"><Picture /></el-icon>
                      </div>
                    </template>
                  </el-image>
                </div>

                <!-- 商品详情 -->
                <div class="item-details">
                  <h3 class="item-name">{{ item.goodsName }}</h3>
                  <div class="item-specs">
                    <span class="spec-item">单价：<span class="price">¥{{ item.price }}</span></span>
                    <span class="spec-item">数量：{{ item.quantity }}</span>
                  </div>
                  <div class="item-total">
                    小计：<span class="total-price">¥{{ item.totalPrice }}</span>
                  </div>
                  <!-- 评论状态显示 -->
                  <div v-if="orderGroup.orderState === 3 && item.commentStatus" class="comment-status">
                    <span v-if="item.commentStatus === 'commented'" class="commented">✅ 已评价</span>
                    <span v-else class="not-commented">🔔 待评价</span>
                  </div>
                </div>

                <!-- 操作按钮 -->
                <div class="item-actions">
                  <el-button 
                    type="primary" 
                    size="small"
                    :loading="detailLoading[orderGroup.orderNo]"
                    @click="showOrderDetail(orderGroup)"
                  >
                    <el-icon><View /></el-icon>
                    查看详情
                  </el-button>
                  
                  <!-- 根据支付状态和订单状态显示不同按钮 -->
                  <!-- 可取消条件：未支付 或 已支付但未发货，且订单未取消 -->
                  <template v-if="canCancelOrder(orderGroup)">
                    <!-- 未支付状态显示支付和取消按钮 -->
                    <template v-if="orderGroup.payState === 0">
                      <el-button 
                        type="success" 
                        size="small"
                        @click="payOrder(orderGroup)"
                      >
                        <el-icon><CreditCard /></el-icon>
                        立即支付
                      </el-button>
                    </template>
                    
                    <el-button 
                      type="danger" 
                      size="small"
                      @click="cancelOrder(orderGroup)"
                    >
                      <el-icon><Close /></el-icon>
                      取消订单
                    </el-button>
                  </template>
                  
                  <template v-else-if="orderGroup.payState === 1 && orderGroup.orderState !== 4">
                    <!-- 已支付且非已取消状态 -->
                    <template v-if="orderGroup.orderState === 2">
                      <!-- 待收货 -->
                      <el-button 
                        type="success" 
                        size="small"
                        @click="confirmReceipt(orderGroup)"
                      >
                        <el-icon><Check /></el-icon>
                        确认收货
                      </el-button>
                    </template>
                    
                    <template v-if="orderGroup.orderState === 3">
                      <!-- 已完成 - 显示单个商品的评价按钮 -->
                      <el-button 
                        v-if="!item.commentStatus || item.commentStatus === 'not_commented'"
                        type="warning" 
                        size="small"
                        @click="openCommentDialog(orderGroup, item)"
                      >
                        <el-icon><Star /></el-icon>
                        评价商品
                      </el-button>
                      <el-button 
                        v-else
                        type="info" 
                        size="small"
                        @click="viewComment(item)"
                      >
                        <el-icon><View /></el-icon>
                        查看评价
                      </el-button>
                    </template>
                  </template>
                </div>
              </div>
            </div>
          </div>

          <!-- 订单总计 -->
          <div class="order-group-footer">
            <div class="shipping-info">
              <i class="icon">📍</i>
              收货地址：{{ orderGroup.receiverAddress }}
              <!-- 备注信息，仅在有备注时显示 -->
              <div v-if="orderGroup.remark && orderGroup.remark.trim()" class="remark-info">
                <i class="icon">📝</i>
                <span class="remark-label">备注：</span>
                <span class="remark-content">{{ orderGroup.remark }}</span>
              </div>
            </div>
            <div class="order-total">
              <span class="total-label">订单总计：</span>
              <span class="total-amount">¥{{ orderGroup.totalAmount }}</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 空状态 -->
      <div v-else-if="!loading" class="empty-state">
        <div class="empty-icon">📦</div>
        <h3>暂无订单记录</h3>
        <p>您还没有任何相关订单，快去购买心仪的商品吧！</p>
        <el-button type="primary" size="large" @click="goToProducts">
          <el-icon><ShoppingBag /></el-icon>
          立即购物
        </el-button>
      </div>
      
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-state">
        <div class="loading-card" v-for="i in 3" :key="i">
          <el-skeleton :rows="4" animated />
        </div>
      </div>
      
      <!-- 分页 -->
      <div v-if="total > 0" class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[5, 10, 20]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          background
        />
      </div>
    </div>

    <!-- 订单详情弹窗 -->
    <el-dialog
      v-model="detailVisible"
      title="订单详情"
      width="900px"
      destroy-on-close
      class="order-detail-dialog"
    >
      <div v-if="currentOrderDetail" class="order-detail">
        <!-- 基本信息 -->
        <el-card class="detail-card" shadow="never">
          <template #header>
            <div class="card-header">
              <i class="header-icon">📋</i>
              <span>基本信息</span>
            </div>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="订单编号">{{ currentOrderDetail.orderNo }}</el-descriptions-item>
            <el-descriptions-item label="订单状态">
              <el-tag :type="getOrderStatusType(currentOrderDetail.orderState)" size="large">
                {{ getOrderStatusText(currentOrderDetail.orderState) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="支付状态" v-if="currentOrderDetail.orderState !== 4">
              <el-tag :type="currentOrderDetail.payState === 1 ? 'success' : 'warning'" size="large">
                {{ getPayStatusText(currentOrderDetail.payState) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="订单金额">
              <span class="amount-highlight">¥{{ currentOrderDetail.totalAmount }}</span>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 商品信息 -->
        <el-card class="detail-card" shadow="never">
          <template #header>
            <div class="card-header">
              <i class="header-icon">📦</i>
              <span>商品信息</span>
            </div>
          </template>
          <div class="detail-items-list">
            <div 
              v-for="item in currentOrderDetail.orderItems" 
              :key="item.itemId"
              class="detail-item-card"
            >
              <div class="detail-item-main">
                <el-image 
                  :src="getImageUrl(item.imgUrl)"
                  fit="cover"
                  class="detail-item-image"
                >
                  <template #error>
                    <div class="image-error">
                      <el-icon><Picture /></el-icon>
                    </div>
                  </template>
                </el-image>
                <div class="detail-item-info">
                  <h4>{{ item.goodsName }}</h4>
                  <div class="detail-item-specs">
                    <span>单价：¥{{ item.price }}</span>
                    <span>数量：{{ item.quantity }}</span>
                    <span class="item-subtotal">合计：¥{{ item.totalPrice }}</span>
                  </div>
                </div>
              </div>
              
              <!-- 商品详细信息 -->
              <div v-if="hasDetailInfo(item)" class="item-detail-info">
                <h5>商品详情</h5>
                <div class="detail-grid">
                  <div v-if="item.desc" class="detail-row">
                    <span class="detail-label">商品描述</span>
                    <span class="detail-value">{{ item.desc }}</span>
                  </div>
                  <div v-if="item.kgs" class="detail-row">
                    <span class="detail-label">重量</span>
                    <span class="detail-value">{{ item.kgs }}kg</span>
                  </div>
                  <div v-if="item.size" class="detail-row">
                    <span class="detail-label">尺寸</span>
                    <span class="detail-value">{{ item.size }}</span>
                  </div>
                  <div v-if="item.creationdate" class="detail-row">
                    <span class="detail-label">生产日期</span>
                    <span class="detail-value">{{ formatDate(item.creationdate) }}</span>
                  </div>
                  <div v-if="item.expirationdate" class="detail-row">
                    <span class="detail-label">保质期</span>
                    <span class="detail-value">{{ item.expirationdate}}</span>
                  </div>
                  <div v-if="item.storagemethod" class="detail-row">
                    <span class="detail-label">储存方式</span>
                    <span class="detail-value">{{ item.storagemethod }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 收件信息 -->
        <el-card class="detail-card" shadow="never">
          <template #header>
            <div class="card-header">
              <i class="header-icon">📍</i>
              <span>收件信息</span>
            </div>
          </template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="收件人">{{ currentOrderDetail.receiverName }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">{{ currentOrderDetail.receiverPhone }}</el-descriptions-item>
            <el-descriptions-item label="收件地址">{{ currentOrderDetail.receiverAddress }}</el-descriptions-item>
            <el-descriptions-item label="备注" v-if="currentOrderDetail.remark">{{ currentOrderDetail.remark }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 时间信息 -->
        <el-card class="detail-card" shadow="never">
          <template #header>
            <div class="card-header">
              <i class="header-icon">🕒</i>
              <span>时间信息</span>
            </div>
          </template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="创建时间">{{ formatTime(currentOrderDetail.createTime) }}</el-descriptions-item>
            <el-descriptions-item label="支付时间" v-if="currentOrderDetail.payTime">
              {{ formatTime(currentOrderDetail.payTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="发货时间" v-if="currentOrderDetail.deliveryTime">
              {{ formatTime(currentOrderDetail.deliveryTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="完成时间" v-if="currentOrderDetail.completeTime">
              {{ formatTime(currentOrderDetail.completeTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="更新时间">{{ formatTime(currentOrderDetail.updateTime) }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </div>
      
      <div v-else class="detail-loading">
        <el-skeleton :rows="8" animated />
      </div>
      
      <template #footer>
        <el-button @click="detailVisible = false" size="large">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 评价弹窗 -->
    <el-dialog
      v-model="commentVisible"
      title="商品评价"
      width="600px"
      destroy-on-close
      class="comment-dialog"
    >
      <div v-if="currentCommentItem" class="comment-form">
        <!-- 商品信息 -->
        <div class="comment-product-info">
          <el-image 
            :src="getImageUrl(currentCommentItem.imgUrl)"
            fit="cover"
            class="comment-product-img"
          >
            <template #error>
              <div class="image-error">
                <el-icon><Picture /></el-icon>
              </div>
            </template>
          </el-image>
          <div class="comment-product-details">
            <h4>{{ currentCommentItem.goodsName }}</h4>
            <p>单价：¥{{ currentCommentItem.price }} × {{ currentCommentItem.quantity }}</p>
          </div>
        </div>

        <!-- 评价表单 -->
        <el-form :model="commentForm" label-width="80px" class="comment-form-content">
          <el-form-item label="评分" required>
            <el-rate
              v-model="commentForm.rating"
              :max="5"
              show-text
              text-color="#ff9900"
              :texts="['很差', '较差', '一般', '满意', '非常满意']"
            />
          </el-form-item>
          <el-form-item label="评价内容" required>
            <el-input
              v-model="commentForm.content"
              type="textarea"
              :rows="4"
              placeholder="请分享您对这件商品的使用感受..."
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
        </el-form>
      </div>
      
      <template #footer>
        <el-button @click="commentVisible = false">取消</el-button>
        <el-button 
          type="primary" 
          @click="submitComment"
          :loading="commentSubmitting"
          :disabled="!commentForm.rating || !commentForm.content.trim()"
        >
          提交评价
        </el-button>
      </template>
    </el-dialog>

    <!-- 查看评价弹窗 -->
    <el-dialog
      v-model="viewCommentVisible"
      title="查看评价"
      width="500px"
      destroy-on-close
      class="view-comment-dialog"
    >
      <div v-if="currentViewComment" class="view-comment-content">
        <div class="comment-rating">
          <span class="rating-label">评分：</span>
          <el-rate
            :model-value="currentViewComment.rating"
            disabled
            show-score
            text-color="#ff9900"
          />
        </div>
        <div class="comment-content">
          <span class="content-label">评价：</span>
          <p>{{ currentViewComment.content }}</p>
        </div>
        <div class="comment-time">
          <span class="time-label">评价时间：</span>
          <span>{{ formatTime(currentViewComment.createTime) }}</span>
        </div>
      </div>
      
      <template #footer>
        <div class="comment-actions">
          <el-button @click="viewCommentVisible = false">关闭</el-button>
          <el-button 
            type="danger" 
            @click="deleteComment"
            :loading="deleteCommentLoading"
          >
            <el-icon><Delete /></el-icon>
            删除评价
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus'
import { 
  Picture, 
  View, 
  CreditCard, 
  Close, 
  Check, 
  Star, 
  ShoppingBag,
  Delete
} from '@element-plus/icons-vue'

// 类型定义
interface OrderItem {
  itemId: number
  orderId: number
  orderNo: string
  goodsId: number
  goodsName: string
  imgUrl: string
  price: number
  quantity: number
  totalPrice: number
  createTime: string
  // 商品详细信息（从商品接口获取）
  desc?: string
  kgs?: number
  size?: string
  creationdate?: string
  expirationdate?: string
  storagemethod?: string
  // 评论状态
  commentStatus?: 'commented' | 'not_commented'
  comment?: Comment
}

interface Order {
  orderId: number
  orderNo: string
  totalAmount: number
  receiverAddress: string
  receiverName: string
  receiverPhone: string
  remark: string
  payTime: string | null
  deliveryTime: string | null
  completeTime: string | null
  payState: number // 0待支付, 1已支付
  createTime: string
  updateTime: string
  userId: number
  orderState: number // 0待处理, 1待发货, 2待收货, 3已完成, 4已取消
}

interface OrderDetail extends Order {
  orderItems: OrderItem[]
}

interface GroupedOrder {
  orderNo: string
  totalAmount: number
  receiverAddress: string
  receiverName: string
  receiverPhone: string
  remark: string
  payTime: string | null
  deliveryTime: string | null
  completeTime: string | null
  payState: number
  createTime: string
  updateTime: string
  orderState: number
  items: OrderItem[]
}

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

interface CommentForm {
  rating: number
  content: string
}

const router = useRouter()

// 响应式数据
const orders = ref<Order[]>([])
const loading = ref(false)
const detailVisible = ref(false)
const currentOrderDetail = ref<OrderDetail | null>(null)
const filterState = ref<number | null>(null)
const detailLoading = ref<Record<string, boolean>>({})

// 评论相关
const commentVisible = ref(false)
const viewCommentVisible = ref(false)
const currentCommentItem = ref<OrderItem | null>(null)
const currentCommentOrder = ref<GroupedOrder | null>(null)
const currentViewComment = ref<Comment | null>(null)
const commentSubmitting = ref(false)
const userComments = ref<Comment[]>([])
const deleteCommentLoading = ref(false)

const commentForm = ref<CommentForm>({
  rating: 5,
  content: ''
})

// 分页数据
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 从 sessionStorage 中获取用户信息
const userInfoString = sessionStorage.getItem('Atuserinfo')
const userInfo = userInfoString ? JSON.parse(userInfoString) : null
const userId = ref(userInfo?.userId || null)

// 将订单按订单号分组
const groupedOrders = computed<GroupedOrder[]>(() => {
  const groups: Record<string, GroupedOrder> = {}
  
  orders.value.forEach(order => {
    if (!groups[order.orderNo]) {
      groups[order.orderNo] = {
        orderNo: order.orderNo,
        totalAmount: order.totalAmount,
        receiverAddress: order.receiverAddress,
        receiverName: order.receiverName,
        receiverPhone: order.receiverPhone,
        remark: order.remark,
        payTime: order.payTime,
        deliveryTime: order.deliveryTime,
        completeTime: order.completeTime,
        payState: order.payState,
        createTime: order.createTime,
        updateTime: order.updateTime,
        orderState: order.orderState,
        items: []
      }
    }
  })
  
  return Object.values(groups)
})

// 判断是否可以取消订单
const canCancelOrder = (orderGroup: GroupedOrder): boolean => {
  // 未支付 或 已支付但未发货（订单状态为待处理或待发货），且订单未取消
  return (orderGroup.payState === 0 || (orderGroup.payState === 1 && orderGroup.orderState <= 1)) && orderGroup.orderState !== 4
}

// 获取用户评论
const fetchUserComments = async () => {
  if (!userId.value) return
  
  try {
    const response = await fetch(`/api/api/comments/user/${userId.value}`)
    if (response.ok) {
      const data = await response.json()
      userComments.value = Array.isArray(data) ? data : (data.records || [])
    }
  } catch (error) {
    console.error('获取用户评论失败:', error)
  }
}

// 检查商品评论状态
const checkItemCommentStatus = (item: OrderItem) => {
  const comment = userComments.value.find(c => c.goodsId === item.goodsId)
  if (comment) {
    item.commentStatus = 'commented'
    item.comment = comment
  } else {
    item.commentStatus = 'not_commented'
  }
}

// 对已完成订单的商品进行排序，未评论的在前
const getSortedItems = (orderGroup: GroupedOrder): OrderItem[] => {
  if (orderGroup.orderState === 3) {
    // 已完成订单：未评论的商品排在前面
    return [...orderGroup.items].sort((a, b) => {
      if (a.commentStatus === 'not_commented' && b.commentStatus !== 'not_commented') return -1
      if (a.commentStatus !== 'not_commented' && b.commentStatus === 'not_commented') return 1
      return 0
    })
  }
  return orderGroup.items
}

// 获取订单列表
const fetchOrders = async () => {
  // 检查是否登录
  if (!userId.value) {
    ElMessage.error('请先登录')
    router.push('/login')
    return
  }

  try {
    loading.value = true

    const params: any = {
      page: currentPage.value,
      size: pageSize.value
    }

    if (filterState.value !== null) {
      params.orderState = filterState.value
    }

    const queryString = new URLSearchParams(params).toString()
    const response = await fetch(`/api/api/orders/user/${userId.value}?${queryString}`)

    if (!response.ok) {
      throw new Error('获取订单失败')
    }

    const data = await response.json()

    if (data.records) {
      orders.value = data.records
      total.value = data.total || data.records.length
    } else {
      orders.value = data
      total.value = data.length
    }

    // 获取用户评论信息
    await fetchUserComments()
    
    await fetchOrdersWithItems()

  } catch (error) {
    console.error('获取订单失败:', error)
    ElMessage.error('获取订单失败')
  } finally {
    loading.value = false
  }
}

// 获取订单的商品信息
const fetchOrdersWithItems = async () => {
  const orderNos = [...new Set(orders.value.map(order => order.orderNo))]
  
  for (const orderNo of orderNos) {
    try {
      const response = await fetch(`/api/api/orders/detail/${orderNo}`)
      if (response.ok) {
        const detail = await response.json()
        const group = groupedOrders.value.find(g => g.orderNo === orderNo)
        if (group && detail.orderItems) {
          // 为每个商品获取详细信息
          group.items = await enrichOrderItemsWithGoodsDetail(detail.orderItems)
          
          // 检查评论状态
          if (group.orderState === 3) {
            group.items.forEach(item => checkItemCommentStatus(item))
          }
        }
      }
    } catch (error) {
      console.error(`获取订单 ${orderNo} 详情失败:`, error)
    }
  }
}

// 显示订单详情
const showOrderDetail = async (orderGroup: GroupedOrder) => {
  try {
    detailLoading.value[orderGroup.orderNo] = true
    
    const response = await fetch(`/api/api/orders/detail/${orderGroup.orderNo}`)
    
    if (!response.ok) {
      throw new Error('获取订单详情失败')
    }
    
    const data = await response.json()
    
    // 为订单商品补充详细信息
    if (data.orderItems && data.orderItems.length > 0) {
      data.orderItems = await enrichOrderItemsWithGoodsDetail(data.orderItems)
    }
    
    currentOrderDetail.value = data
    detailVisible.value = true
    
  } catch (error) {
    console.error('获取订单详情失败:', error)
    ElMessage.error('获取订单详情失败')
  } finally {
    detailLoading.value[orderGroup.orderNo] = false
  }
}

// 打开评论弹窗
const openCommentDialog = (orderGroup: GroupedOrder, item: OrderItem) => {
  currentCommentOrder.value = orderGroup
  currentCommentItem.value = item
  commentForm.value = {
    rating: 5,
    content: ''
  }
  commentVisible.value = true
}

// 验证评论数据
const validateCommentData = (data: any): { valid: boolean; message?: string } => {
  if (!data.userId || typeof data.userId !== 'number') {
    return { valid: false, message: '用户ID无效' }
  }
  
  if (!data.goodsId || typeof data.goodsId !== 'number') {
    return { valid: false, message: '商品ID无效' }
  }
  
  if (!data.rating || typeof data.rating !== 'number' || data.rating < 1 || data.rating > 5) {
    return { valid: false, message: '评分必须是1-5之间的数字' }
  }
  
  if (!data.content || typeof data.content !== 'string' || data.content.trim().length === 0) {
    return { valid: false, message: '评价内容不能为空' }
  }
  
  if (data.content.trim().length > 500) {
    return { valid: false, message: '评价内容不能超过500个字符' }
  }
  
  return { valid: true }
}

// 提交评论
const submitComment = async () => {
  if (!currentCommentItem.value || !userId.value) return
  
  try {
    commentSubmitting.value = true
    
    // 只发送必要的字段，避免400错误
    const commentData = {
      userId: userId.value,
      goodsId: currentCommentItem.value.goodsId,
      rating: commentForm.value.rating,
      content: commentForm.value.content.trim()
    }
    
    // 验证数据
    const validation = validateCommentData(commentData)
    if (!validation.valid) {
      ElMessage.error(validation.message || '数据验证失败')
      return
    }
    
    const response = await fetch('/api/api/comments', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(commentData)
    })
    
    if (!response.ok) {
      const errorData = await response.json().catch(() => null)
      console.error('评论提交失败:', errorData)
      throw new Error(errorData?.message || `提交评价失败 (${response.status})`)
    }
    
    ElMessage.success('评价提交成功！')
    commentVisible.value = false
    
    // 立即刷新
    await fetchOrders()
    
  } catch (error) {
    console.error('提交评价失败:', error)
    ElMessage.error((error as Error).message || '提交评价失败，请稍后重试')
  } finally {
    commentSubmitting.value = false
  }
}

// 查看评论
const viewComment = (item: OrderItem) => {
  if (item.comment) {
    currentViewComment.value = item.comment
    viewCommentVisible.value = true
  }
}

// 删除评论
const deleteComment = async () => {
  if (!currentViewComment.value || !userId.value) return
  
  try {
    await ElMessageBox.confirm(
      '确定要删除这条评价吗？删除后您可以重新评价该商品。',
      '删除评价',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }
    )
    
    deleteCommentLoading.value = true
    
    const response = await fetch(`/api/api/comments/${currentViewComment.value.commentId}?userId=${userId.value}`, {
      method: 'DELETE',
      headers: {
        'Content-Type': 'application/json',
      }
    })
    
    if (!response.ok) {
      const errorData = await response.json().catch(() => null)
      console.error('删除评论失败:', errorData)
      throw new Error(errorData?.message || `删除评价失败 (${response.status})`)
    }
    
    ElMessage.success('评价删除成功！您现在可以重新评价该商品')
    viewCommentVisible.value = false
    
    // 立即刷新
    await fetchOrders()
    
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除评价失败:', error)
      ElMessage.error((error as Error).message || '删除评价失败，请稍后重试')
    }
  } finally {
    deleteCommentLoading.value = false
  }
}

// 获取图片URL
const getImageUrl = (imgUrl: string | null): string => {
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

// 格式化时间
const formatTime = (timeStr: string | null): string => {
  if (!timeStr) return '-'
  return new Date(timeStr).toLocaleString('zh-CN')
}

// 取消订单
const cancelOrder = async (orderGroup: GroupedOrder) => {
  try {
    await ElMessageBox.confirm(
      `确定要取消订单吗？取消后将无法恢复。`, 
      '取消订单', 
      {
        confirmButtonText: '确定取消',
        cancelButtonText: '我再想想',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }
    )
    
    // 显示取消订单加载状态
    const loadingInstance = ElLoading.service({
      lock: true,
      text: '正在取消订单...',
      background: 'rgba(0, 0, 0, 0.7)'
    })
    
    const response = await fetch(`/api/api/orders/cancel/${orderGroup.orderNo}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      }
    })
    
    loadingInstance.close()
    
    if (!response.ok) {
      throw new Error('取消订单失败')
    }
    
    const result = await response.json()
    
    if (result === true || result.success !== false) {
      ElMessage.success('订单已取消')
      // 刷新订单列表
      await fetchOrders()
    } else {
      throw new Error(result.message || '取消订单失败')
    }
    
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消订单失败:', error)
      ElMessage.error((error as Error).message || '取消订单失败，请稍后重试')
    }
  }
}

// 支付订单
const payOrder = async (orderGroup: GroupedOrder) => {
  try {
    await ElMessageBox.confirm(
      `确定要支付订单吗？订单金额：¥${orderGroup.totalAmount}`, 
      '确认支付', 
      {
        confirmButtonText: '确认支付',
        cancelButtonText: '取消',
        type: 'info',
        confirmButtonClass: 'el-button--success'
      }
    )
    
    // 显示支付加载状态
    const loadingInstance = ElLoading.service({
      lock: true,
      text: '正在处理支付...',
      background: 'rgba(0, 0, 0, 0.7)'
    })
    
    const response = await fetch(`/api/api/orders/pay/${orderGroup.orderNo}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      }
    })
    
    loadingInstance.close()
    
    if (!response.ok) {
      throw new Error('支付失败')
    }
    
    const result = await response.json()
    
    if (result === true || result.success !== false) {
      ElMessage.success('支付成功！')
      // 刷新订单列表
      await fetchOrders()
    } else {
      throw new Error(result.message || '支付失败')
    }
    
  } catch (error) {
    if (error !== 'cancel') {
      console.error('支付失败:', error)
      ElMessage.error((error as Error).message || '支付失败，请稍后重试')
    }
  }
}

// 确认收货
const confirmReceipt = async (orderGroup: GroupedOrder) => {
  try {
    await ElMessageBox.confirm(
      `确定已收到订单的商品吗？确认后订单将标记为已完成。`, 
      '确认收货', 
      {
        confirmButtonText: '确认收货',
        cancelButtonText: '我再想想',
        type: 'info',
        confirmButtonClass: 'el-button--success'
      }
    )
    
    // 显示确认收货加载状态
    const loadingInstance = ElLoading.service({
      lock: true,
      text: '正在确认收货...',
      background: 'rgba(0, 0, 0, 0.7)'
    })
    
    const response = await fetch(`/api/api/orders/confirm/${orderGroup.orderNo}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      }
    })
    
    loadingInstance.close()
    
    if (!response.ok) {
      throw new Error('确认收货失败')
    }
    
    const result = await response.json()
    
    if (result === true || result.success !== false) {
      ElMessage.success('收货确认成功！订单已完成')
      // 刷新订单列表
      await fetchOrders()
    } else {
      throw new Error(result.message || '确认收货失败')
    }
    
  } catch (error) {
    if (error !== 'cancel') {
      console.error('确认收货失败:', error)
      ElMessage.error((error as Error).message || '确认收货失败，请稍后重试')
    }
  }
}

// 获取支付状态文本
const getPayStatusText = (state: number): string => {
  return state === 1 ? '已支付' : '待支付'
}

// 获取支付状态样式
const getPayStatusClass = (state: number): string => {
  return state === 1 ? 'pay-status-paid' : 'pay-status-pending'
}

// 获取订单状态文本
const getOrderStatusText = (state: number): string => {
  const statusMap: { [key: number]: string } = {
    0: '待处理',
    1: '待发货',
    2: '待收货',
    3: '已完成',
    4: '已取消'
  }
  return statusMap[state] || '未知状态'
}

// 获取订单状态样式类
const getOrderStatusClass = (state: number): string => {
  const classMap: { [key: number]: string } = {
    0: 'order-status-pending',
    1: 'order-status-processing',
    2: 'order-status-shipping',
    3: 'order-status-completed',
    4: 'order-status-cancelled'
  }
  return classMap[state] || ''
}

// 获取状态标签类型
const getOrderStatusType = (state: number): string => {
  const typeMap: { [key: number]: string } = {
    0: 'warning',
    1: 'info',
    2: 'primary',
    3: 'success',
    4: 'danger'
  }
  return typeMap[state] || 'info'
}

// 获取商品详细信息
const fetchGoodsDetail = async (goodsId: number): Promise<any> => {
  try {
    const response = await fetch(`/api/api/goods/${goodsId}`)
    if (response.ok) {
      return await response.json()
    }
  } catch (error) {
    console.error(`获取商品 ${goodsId} 详情失败:`, error)
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
          desc: goodsDetail.desc,
          kgs: goodsDetail.kgs,
          size: goodsDetail.size,
          creationdate: goodsDetail.creationdate,
          expirationdate: goodsDetail.expirationdate,
          storagemethod: goodsDetail.storagemethod,
          // 更新图片URL，使用商品详情中的图片
          imgUrl: goodsDetail.imgUrl || item.imgUrl
        }
      }
      return item
    })
  )
  return enrichedItems
}

// 格式化日期
const formatDate = (dateStr: string): string => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

// 检查商品是否有详细信息
const hasDetailInfo = (item: OrderItem): boolean => {
  return !!(item.desc || item.kgs || item.size || 
           item.creationdate || item.expirationdate || item.storagemethod)
}

// 筛选状态变化
const handleFilterChange = (state: number | null) => {
  filterState.value = state
  currentPage.value = 1
  fetchOrders()
}

// 分页变化
const handleCurrentChange = (page: number) => {
  currentPage.value = page
  fetchOrders()
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  fetchOrders()
}

// 跳转到商品页面
const goToProducts = () => {
  router.push('/user/product/list')
}

// 组件挂载时获取数据
onMounted(() => {
  fetchOrders()
})
</script>

<style scoped>
.orders-container {
  min-height: 100vh;
  background: #f8fafc;
  padding: 20px;
}

.page-header {
  text-align: center;
  margin-bottom: 24px;
  color: #1f2937;
  background: white;
  padding: 24px;
  border-radius: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.page-header h1 {
  font-size: 2.5em;
  margin: 0 0 10px 0;
  font-weight: 600;
  text-shadow: 0 2px 4px rgba(0,0,0,0.3);
}

.page-header p {
  font-size: 1.1em;
  opacity: 0.9;
  margin: 0;
}

.filter-section {
  margin-bottom: 24px;
  background: white;
  padding: 20px;
  border-radius: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.filter-tabs {
  display: flex;
  justify-content: center;
  gap: 8px;
  flex-wrap: wrap;
}

.filter-tab {
  background: #f1f5f9;
  border: 2px solid transparent;
  border-radius: 12px;
  padding: 10px 20px;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 500;
  font-size: 14px;
}

.filter-tab:hover {
  background: #e2e8f0;
  color: #475569;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(34, 197, 94, 0.15);
}

.filter-tab.active {
  background: #76cdd9;
  color: white;
  border-color: #3cc1d0;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.4);
}

.filter-icon {
  font-size: 1.2em;
}

.orders-content {
  max-width: 1400px;
  margin: 0 auto;
}

.orders-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.order-group-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e2e8f0;
  overflow: hidden;
  transition: all 0.2s ease;
}

.order-group-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.order-group-header {
  background: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
  padding: 16px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-info {
  display: flex;
  align-items: center;
  gap: 24px;
}

.order-no, .order-time {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 500;
  color: #374151;
  font-size: 14px;
}

.order-status-info {
  display: flex;
  gap: 8px;
  align-items: center;
}

.pay-status, .order-status {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.pay-status-pending {
  background: #fef3cd;
  color: #b45309;
}

.pay-status-paid {
  background: #d1fae5;
  color: #065f46;
}

.order-status-pending {
  background: #fef3cd;
  color: #b45309;
}

.order-status-processing {
  background: #dbeafe;
  color: #1d4ed8;
}

.order-status-shipping {
  background: #ede9fe;
  color: #7c3aed;
}

.order-status-completed {
  background: #d1fae5;
  color: #065f46;
}

.order-status-cancelled {
  background: #fee2e2;
  color: #dc2626;
}

.order-items-list {
  padding: 0;
}

.order-item-card {
  border-bottom: 1px solid #f1f5f9;
  transition: all 0.2s ease;
}

.order-item-card:hover {
  background: #f8fafc;
}

.order-item-card:last-child {
  border-bottom: none;
}

.item-content {
  padding: 16px 20px;
  display: flex;
  gap: 16px;
  align-items: center;
}

.item-image {
  flex-shrink: 0;
}

.product-img {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  overflow: hidden;
}

.image-error {
  width: 80px;
  height: 80px;
  background: #f1f5f9;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  color: #9ca3af;
}

.item-details {
  flex: 1;
  min-width: 0;
}

.item-name {
  font-size: 15px;
  font-weight: 600;
  color: #111827;
  margin: 0 0 8px 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2; 
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.item-specs {
  display: flex;
  gap: 16px;
  margin-bottom: 4px;
}

.spec-item {
  font-size: 13px;
  color: #6b7280;
}

.price {
  color: #dc2626;
  font-weight: 600;
}

.item-total {
  font-size: 13px;
  color: #374151;
  font-weight: 500;
}

.total-price {
  color: #dc2626;
  font-weight: 700;
  font-size: 15px;
}

.comment-status {
  margin-top: 4px;
  font-size: 12px;
}

.commented {
  color: #10b981;
  font-weight: 500;
}

.not-commented {
  color: #f59e0b;
  font-weight: 500;
}

.item-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
  align-items: center;
}

.item-actions .el-button {
  border-radius: 6px;
  font-weight: 500;
  font-size: 12px;
  padding: 6px 12px;
}

.order-group-footer {
  background: #f8fafc;
  padding: 16px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid #e2e8f0;
}

.shipping-info {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #6b7280;
  font-size: 13px;
  flex: 1;
  min-width: 0;
}

.shipping-info .icon {
  flex-shrink: 0;
}

.order-total {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  flex-shrink: 0;
}

.total-label {
  color: #374151;
  font-weight: 500;
}

.total-amount {
  color: #dc2626;
  font-weight: 700;
  font-size: 18px;
}

.empty-state {
  text-align: center;
  padding: 48px 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  color: #6b7280;
}

.empty-icon {
  font-size: 3em;
  margin-bottom: 16px;
}

.empty-state h3 {
  margin: 0 0 8px 0;
  color: #374151;
  font-weight: 600;
}

.empty-state p {
  margin: 0 0 24px 0;
  color: #6b7280;
}

.loading-state {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.loading-card {
  background: white;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
  padding: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.order-detail-dialog {
  backdrop-filter: blur(10px);
}

.order-detail {
  max-height: 70vh;
  overflow-y: auto;
}

.detail-card {
  margin-bottom: 16px;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid #e2e8f0;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #374151;
}

.header-icon {
  font-size: 1.1em;
}

.amount-highlight {
  color: #dc2626;
  font-weight: 700;
  font-size: 16px;
}

.detail-items-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.detail-item-card {
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  overflow: hidden;
  background: white;
}

.detail-item-main {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: #f8fafc;
}

.detail-item-image {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  flex-shrink: 0;
}

.detail-item-info {
  flex: 1;
}

.detail-item-info h4 {
  margin: 0 0 12px 0;
  color: #111827;
  font-weight: 600;
  font-size: 16px;
  line-height: 1.4;
}

.detail-item-specs {
  display: flex;
  gap: 16px;
  font-size: 14px;
  color: #6b7280;
  flex-wrap: wrap;
}

.item-detail-info {
  padding: 16px;
  border-top: 1px solid #e2e8f0;
  background: white;
}

.item-detail-info h5 {
  margin: 0 0 12px 0;
  color: #374151;
  font-weight: 600;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.item-detail-info h5::before {
  content: "ℹ️";
  font-size: 16px;
}

.detail-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 8px;
}

.detail-row {
  display: grid;
  grid-template-columns: 80px 1fr;
  gap: 12px;
  align-items: start;
  padding: 8px 0;
  border-bottom: 1px solid #f1f5f9;
}

.detail-row:last-child {
  border-bottom: none;
}

.detail-label {
  font-size: 13px;
  color: #6b7280;
  font-weight: 500;
  flex-shrink: 0;
}

.detail-value {
  font-size: 13px;
  color: #374151;
  line-height: 1.4;
  word-break: break-word;
}

.item-subtotal {
  color: #dc2626 !important;
  font-weight: 600;
}

.detail-loading {
  padding: 24px;
}

/* 评论相关样式 */
.comment-dialog, .view-comment-dialog {
  backdrop-filter: blur(10px);
}

.comment-form {
  max-height: 60vh;
  overflow-y: auto;
}

.comment-product-info {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 8px;
  margin-bottom: 20px;
}

.comment-product-img {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  flex-shrink: 0;
}

.comment-product-details h4 {
  margin: 0 0 8px 0;
  color: #111827;
  font-weight: 600;
}

.comment-product-details p {
  margin: 0;
  color: #6b7280;
  font-size: 14px;
}

.comment-form-content {
  padding: 0 8px;
}

.view-comment-content {
  padding: 16px 0;
}

.comment-rating, .comment-content, .comment-time {
  margin-bottom: 16px;
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.rating-label, .content-label, .time-label {
  color: #374151;
  font-weight: 500;
  min-width: 80px;
  flex-shrink: 0;
}

.comment-content p {
  margin: 0;
  color: #6b7280;
  line-height: 1.6;
  word-break: break-word;
}

.comment-actions {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.icon {
  margin-right: 4px;
  font-size: 1em;
}

/* Element Plus 组件样式覆盖 */
:deep(.el-button) {
  border-radius: 6px;
  font-weight: 500;
}

:deep(.el-card__header) {
  background: #f8fafc;
  color: #374151;
  border-bottom: 1px solid #e2e8f0;
  padding: 16px 20px;
}

:deep(.el-descriptions__title) {
  margin-bottom: 12px;
  font-weight: 600;
  color: #111827;
}

:deep(.el-dialog) {
  border-radius: 12px;
  overflow: hidden;
}

:deep(.el-dialog__header) {
  background: #4fabb0;
  color: white;
  padding: 20px;
  margin: 0;
}

:deep(.el-dialog__title) {
  color: white;
  font-weight: 600;
}

:deep(.el-dialog__headerbtn .el-dialog__close) {
  color: white;
}

:deep(.el-rate__text) {
  font-size: 14px;
  margin-left: 8px;
}

:deep(.el-textarea__inner) {
  border-radius: 8px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #374151;
}
</style>