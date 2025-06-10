<template>
  <div class="order-management">
    <div class="page-header">
      <h2>订单管理</h2>
      <div class="header-stats">
        <el-statistic title="今日订单" :value="todayOrders" />
        <el-statistic title="总订单数" :value="totalOrders" />
        <el-statistic title="今日营业额" :value="todayRevenue" prefix="¥" />
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <div class="filter-section">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="订单号">
          <el-input
            v-model="searchForm.orderNumber"
            placeholder="请输入订单号"
            clearable
            @input="handleSearch"
          />
        </el-form-item>
        <el-form-item label="用户名">
          <el-input
            v-model="searchForm.username"
            placeholder="请输入用户名"
            clearable
            @input="handleSearch"
          />
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="待付款" value="pending" />
            <el-option label="待发货" value="paid" />
            <el-option label="待收货" value="shipped" />
            <el-option label="已完成" value="completed" />
            <el-option label="已取消" value="cancelled" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
          <el-button type="success" @click="exportOrders">导出Excel</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 订单表格 -->
    <div class="table-section">
      <el-table 
        :data="filteredOrders" 
        style="width: 100%"
        v-loading="loading"
      >
        <el-table-column prop="orderNumber" label="订单号" width="150" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column label="商品信息" min-width="200">
          <template #default="scope">
            <div class="order-items">
              <div v-for="item in scope.row.items.slice(0, 2)" :key="item.id" class="order-item">
                <span>{{ item.name }} x{{ item.quantity }}</span>
              </div>
              <span v-if="scope.row.items.length > 2" class="more-items">
                等{{ scope.row.items.length }}件商品
              </span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="订单金额" width="120">
          <template #default="scope">
            ¥{{ scope.row.totalAmount.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column label="订单状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" width="180" />
        <el-table-column prop="payTime" label="支付时间" width="180">
          <template #default="scope">
            {{ scope.row.payTime || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="viewOrder(scope.row)">查看</el-button>
            <el-button 
              v-if="scope.row.status === 'paid'" 
              size="small" 
              type="primary"
              @click="shipOrder(scope.row)"
            >
              发货
            </el-button>
            <el-button 
              v-if="scope.row.status === 'pending'" 
              size="small" 
              type="danger"
              @click="cancelOrder(scope.row)"
            >
              取消
            </el-button>
            <el-dropdown v-if="scope.row.status === 'completed'">
              <el-button size="small">
                更多<el-icon class="el-icon--right"><arrow-down /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="refundOrder(scope.row)">
                    退款
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="totalOrdersCount"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 订单详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="订单详情"
      width="800px"
    >
      <div v-if="selectedOrder" class="order-detail">
        <div class="detail-section">
          <h3>订单信息</h3>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="订单号">{{ selectedOrder.orderNumber }}</el-descriptions-item>
            <el-descriptions-item label="用户名">{{ selectedOrder.username }}</el-descriptions-item>
            <el-descriptions-item label="订单状态">
              <el-tag :type="getStatusType(selectedOrder.status)">
                {{ getStatusText(selectedOrder.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="订单金额">¥{{ selectedOrder.totalAmount.toFixed(2) }}</el-descriptions-item>
            <el-descriptions-item label="下单时间">{{ selectedOrder.createTime }}</el-descriptions-item>
            <el-descriptions-item label="支付时间">{{ selectedOrder.payTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="收货地址" :span="2">{{ selectedOrder.address || '-' }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="detail-section">
          <h3>商品列表</h3>
          <el-table :data="selectedOrder.items" style="width: 100%">
            <el-table-column label="商品图片" width="80">
              <template #default="scope">
                <el-image
                  style="width: 50px; height: 50px"
                  :src="scope.row.image || '/placeholder-product.png'"
                  fit="cover"
                />
              </template>
            </el-table-column>
            <el-table-column prop="name" label="商品名称" />
            <el-table-column prop="price" label="单价" width="100">
              <template #default="scope">
                ¥{{ scope.row.price }}
              </template>
            </el-table-column>
            <el-table-column prop="quantity" label="数量" width="80" />
            <el-table-column label="小计" width="100">
              <template #default="scope">
                ¥{{ (scope.row.price * scope.row.quantity).toFixed(2) }}
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'

interface OrderItem {
  id: number
  name: string
  price: number
  quantity: number
  image?: string
}

interface Order {
  id: number
  orderNumber: string
  username: string
  status: 'pending' | 'paid' | 'shipped' | 'completed' | 'cancelled'
  createTime: string
  payTime?: string
  totalAmount: number
  address?: string
  items: OrderItem[]
}

interface SearchForm {
  orderNumber: string
  username: string
  status: string
  dateRange: string[]
}

// 响应式数据
const loading = ref(false)
const detailDialogVisible = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const totalOrdersCount = ref(0)
const selectedOrder = ref<Order | null>(null)

const searchForm = ref<SearchForm>({
  orderNumber: '',
  username: '',
  status: '',
  dateRange: []
})

// 统计数据
const todayOrders = ref(15)
const totalOrders = ref(1248)
const todayRevenue = ref(3560.00)

// 模拟订单数据
const orders = ref<Order[]>([
  {
    id: 1,
    orderNumber: '202501100001',
    username: 'user001',
    status: 'pending',
    createTime: '2025-01-10 14:30:00',
    totalAmount: 123.00,
    address: '上海市浦东新区张江高科技园区',
    items: [
      { id: 1, name: '红色的糖果', price: 56, quantity: 2, image: '/placeholder-product.png' },
      { id: 2, name: '绿的糖果', price: 67, quantity: 1, image: '/placeholder-product.png' }
    ]
  },
  {
    id: 2,
    orderNumber: '202501090002',
    username: 'user002',
    status: 'paid',
    createTime: '2025-01-09 10:15:00',
    payTime: '2025-01-09 10:16:30',
    totalAmount: 80.00,
    address: '北京市朝阳区建国门外大街',
    items: [
      { id: 3, name: '黄色的糖果', price: 80, quantity: 1, image: '/placeholder-product.png' }
    ]
  },
  {
    id: 3,
    orderNumber: '202501080003',
    username: 'user001',
    status: 'shipped',
    createTime: '2025-01-08 16:45:00',
    payTime: '2025-01-08 16:47:20',
    totalAmount: 33.00,
    address: '广州市天河区珠江新城',
    items: [
      { id: 4, name: '紫色的糖果', price: 33, quantity: 1, image: '/placeholder-product.png' }
    ]
  },
  {
    id: 4,
    orderNumber: '202501070004',
    username: 'user003',
    status: 'completed',
    createTime: '2025-01-07 09:20:00',
    payTime: '2025-01-07 09:22:15',
    totalAmount: 194.00,
    address: '深圳市南山区科技园',
    items: [
      { id: 5, name: '绿色的糖果', price: 70, quantity: 1, image: '/placeholder-product.png' },
      { id: 6, name: '黄色的糖果', price: 97, quantity: 1, image: '/placeholder-product.png' },
      { id: 7, name: '红色小糖果', price: 27, quantity: 1, image: '/placeholder-product.png' }
    ]
  }
])

// 计算属性
const filteredOrders = computed(() => {
  let filtered = orders.value

  if (searchForm.value.orderNumber) {
    filtered = filtered.filter(order =>
      order.orderNumber.includes(searchForm.value.orderNumber)
    )
  }

  if (searchForm.value.username) {
    filtered = filtered.filter(order =>
      order.username.toLowerCase().includes(searchForm.value.username.toLowerCase())
    )
  }

  if (searchForm.value.status) {
    filtered = filtered.filter(order => order.status === searchForm.value.status)
  }

  if (searchForm.value.dateRange && searchForm.value.dateRange.length === 2) {
    const [startDate, endDate] = searchForm.value.dateRange
    filtered = filtered.filter(order => {
      const orderDate = order.createTime.split(' ')[0]
      return orderDate >= startDate && orderDate <= endDate
    })
  }

  totalOrdersCount.value = filtered.length
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filtered.slice(start, end)
})

// 方法
onMounted(() => {
  loadOrders()
})

const loadOrders = async () => {
  loading.value = true
  try {
    // 这里可以调用API获取订单数据
    // const response = await axios.get('/api/admin/orders')
    // orders.value = response.data
    await new Promise(resolve => setTimeout(resolve, 500)) // 模拟加载
  } catch (error) {
    ElMessage.error('加载订单数据失败')
  } finally {
    loading.value = false
  }
}

const getStatusType = (status: string) => {
  const types = {
    pending: 'warning',
    paid: 'info',
    shipped: 'primary',
    completed: 'success',
    cancelled: 'danger'
  }
  return types[status as keyof typeof types] || 'info'
}

const getStatusText = (status: string) => {
  const texts = {
    pending: '待付款',
    paid: '待发货',
    shipped: '待收货',
    completed: '已完成',
    cancelled: '已取消'
  }
  return texts[status as keyof typeof texts] || '未知状态'
}

const handleSearch = () => {
  currentPage.value = 1
}

const resetSearch = () => {
  searchForm.value = {
    orderNumber: '',
    username: '',
    status: '',
    dateRange: []
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

const viewOrder = (order: Order) => {
  selectedOrder.value = order
  detailDialogVisible.value = true
}

const shipOrder = async (order: Order) => {
  try {
    await ElMessageBox.confirm(`确定要发货订单 "${order.orderNumber}" 吗？`, '提示', {
      confirmButtonText: '确定发货',
      cancelButtonText: '取消',
      type: 'info'
    })

    order.status = 'shipped'
    ElMessage.success('订单发货成功')
  } catch {
    // 用户取消
  }
}

const cancelOrder = async (order: Order) => {
  try {
    await ElMessageBox.confirm(`确定要取消订单 "${order.orderNumber}" 吗？`, '警告', {
      confirmButtonText: '确定取消',
      cancelButtonText: '保留订单',
      type: 'warning'
    })

    order.status = 'cancelled'
    ElMessage.success('订单已取消')
  } catch {
    // 用户取消
  }
}

const refundOrder = async (order: Order) => {
  try {
    await ElMessageBox.confirm(`确定要为订单 "${order.orderNumber}" 退款吗？`, '退款确认', {
      confirmButtonText: '确定退款',
      cancelButtonText: '取消',
      type: 'warning'
    })

    ElMessage.success('退款申请已提交')
  } catch {
    // 用户取消
  }
}

const exportOrders = () => {
  ElMessage.success('导出功能开发中...')
}
</script>

<style scoped>
.order-management {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.page-header h2 {
  margin: 0;
  color: #2c3e50;
}

.header-stats {
  display: flex;
  gap: 30px;
}

.filter-section {
  background: white;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.search-form {
  margin: 0;
}

.table-section {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.order-items {
  font-size: 14px;
}

.order-item {
  margin-bottom: 4px;
  color: #666;
}

.more-items {
  color: #999;
  font-style: italic;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
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

/* 响应式设计 */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 15px;
  }
  
  .header-stats {
    flex-direction: column;
    gap: 15px;
    width: 100%;
  }
  
  .search-form {
    display: block;
  }
  
  .search-form .el-form-item {
    margin-bottom: 15px;
  }
}
</style>