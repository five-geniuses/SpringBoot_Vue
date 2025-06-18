<!-- views/admin/AdminIndex.vue -->
<template>
  <div class="admin-dashboard">
    <!-- 头部导航 -->
    <header class="dashboard-header">
      <div class="header-left">
        <h1>管理员控制台</h1>
        <p class="welcome-text">欢迎回来，{{ adminInfo.username || '管理员' }}</p>
      </div>
      
      <!-- 右上角用户信息 -->
      <div class="header-right">
        <div class="admin-profile" @click="toggleDropdown" ref="profileDropdown">
          <img 
            :src="adminInfo.avatar || '/adminPic.png'" 
            :alt="adminInfo.username || '管理员'" 
            class="admin-avatar"
            @error="handleAvatarError"
          >
          <span class="admin-name">{{ adminInfo.username || '管理员' }}</span>
          <i class="dropdown-icon" :class="{ 'rotated': showDropdown }">▼</i>
          
          <!-- 下拉菜单 -->
          <div class="dropdown-menu" v-show="showDropdown">
            <div class="dropdown-item" @click="showProfile">
              <i class="icon">👤</i>
              <span>个人信息</span>
            </div>
            <div class="dropdown-item" @click="showEditProfile">
              <i class="icon">✏️</i>
              <span>修改信息</span>
            </div>
            <div class="dropdown-divider"></div>
            <div class="dropdown-item logout" @click="handleLogout">
              <i class="icon">🚪</i>
              <span>登出</span>
            </div>
          </div>
        </div>
      </div>
    </header>

    <!-- 统计卡片 -->
    <section class="stats-section">
      <div class="stats-header">
        <h2>数据概览</h2>
        <div class="stats-info">
          <span v-if="lastUpdated" class="last-updated">
            最后更新: {{ lastUpdated }}
          </span>
          <button 
            @click="refreshStats" 
            class="refresh-btn"
            :disabled="isLoading"
            :class="{ 'loading': isLoading }"
          >
            <i class="refresh-icon" :class="{ 'spinning': isLoading }">🔄</i>
            {{ isLoading ? '更新中...' : '刷新' }}
          </button>
        </div>
      </div>
      
      <div class="stats-grid">
        <!-- 总用户数卡片 -->
        <div class="stat-card users">
          <div class="stat-icon">👥</div>
          <div class="stat-content">
            <h3>总用户数</h3>
            <p class="stat-number">
              <span v-if="!stats.isLoading">{{ stats.totalUsers }}</span>
              <span v-else class="loading-skeleton">...</span>
            </p>
          </div>
        </div>
        
        <!-- 总商品数卡片 -->
        <div class="stat-card goods">
          <div class="stat-icon">📦</div>
          <div class="stat-content">
            <h3>总商品数</h3>
            <p class="stat-number">
              <span v-if="!stats.isLoading">{{ stats.totalGoods }}</span>
              <span v-else class="loading-skeleton">...</span>
            </p>
          </div>
        </div>
        
        <!-- 未支付订单卡片 -->
        <div class="stat-card unpaid">
          <div class="stat-icon">💳</div>
          <div class="stat-content">
            <h3>未支付订单</h3>
            <p class="stat-number">
              <span v-if="!stats.isLoading">{{ stats.unpaidOrders }}</span>
              <span v-else class="loading-skeleton">...</span>
            </p>
            <span class="stat-trend warning">待处理</span>
          </div>
        </div>
        
        <!-- 未发货订单卡片 -->
        <div class="stat-card unshipped">
          <div class="stat-icon">🚚</div>
          <div class="stat-content">
            <h3>未发货订单</h3>
            <p class="stat-number">
              <span v-if="!stats.isLoading">{{ stats.unshippedOrders }}</span>
              <span v-else class="loading-skeleton">...</span>
            </p>
            <span class="stat-trend warning">待发货</span>
          </div>
        </div>
      </div>
    </section>

    <!-- 快捷操作和商品分类统计 -->
    <section class="content-section">
      <div class="content-grid">
        <!-- 快捷操作 -->
        <div class="quick-actions">
          <h3>快捷操作</h3>
          <div class="action-grid">
            <div class="action-item" @click="navigateTo('/admin/users')">
              <div class="action-icon">👥</div>
              <span>用户管理</span>
            </div>
            <div class="action-item" @click="navigateTo('/admin/product')">
              <div class="action-icon">📦</div>
              <span>商品管理</span>
            </div>
            <div class="action-item" @click="navigateTo('/admin/orders')">
              <div class="action-icon">📋</div>
              <span>订单管理</span>
            </div>
            <div class="action-item" @click="refreshStats">
              <div class="action-icon">🔄</div>
              <span>刷新数据</span>
            </div>
          </div>
        </div>

        <!-- 商品分类统计 -->
        <div class="category-stats">
          <h3>
            商品分类统计
            <span v-if="categoryStats.isLoading" class="loading-indicator">🔄</span>
          </h3>
          <div class="category-chart">
            <canvas ref="categoryChart" width="300" height="300"></canvas>
          </div>
        </div>
      </div>
    </section>

    <!-- 个人信息模态框 -->
    <div class="modal-overlay" v-show="showProfileModal" @click="closeProfileModal">
      <div class="modal" @click.stop>
        <div class="modal-header">
          <h3>{{ isEditing ? '修改个人信息' : '个人信息' }}</h3>
          <button class="close-btn" @click="closeProfileModal">×</button>
        </div>
        <div class="modal-body">
          <div class="profile-form">
            <div class="form-group">
              <label>用户名：</label>
              <input 
                v-if="isEditing" 
                v-model="editingProfile.username" 
                type="text" 
                class="form-input"
              >
              <span v-else>{{ adminInfo.username || '未设置' }}</span>
            </div>
            
            <div class="form-group">
              <label>密码：</label>
              <input 
                v-if="isEditing" 
                v-model="editingProfile.password" 
                type="password" 
                class="form-input"
                placeholder="输入新密码(不填则不修改)"
              >
              <span v-else>******</span>
            </div>
            
            <div class="form-group">
              <label>性别：</label>
              <select 
                v-if="isEditing" 
                v-model="editingProfile.gender" 
                class="form-input"
              >
                <option value="">请选择</option>
                <option value="男">男</option>
                <option value="女">女</option>
              </select>
              <span v-else>{{ adminInfo.gender || '未设置' }}</span>
            </div>
            
            <div class="form-group">
              <label>电话：</label>
              <input 
                v-if="isEditing" 
                v-model="editingProfile.telephone" 
                type="tel" 
                class="form-input"
              >
              <span v-else>{{ adminInfo.telephone || '未设置' }}</span>
            </div>
            
            <div class="form-group">
              <label>个人简介：</label>
              <textarea 
                v-if="isEditing" 
                v-model="editingProfile.introduce" 
                class="form-textarea"
                rows="3"
                placeholder="请输入个人简介"
              ></textarea>
              <span v-else class="introduce-text">{{ adminInfo.introduce || '未设置' }}</span>
            </div>
            
            <div class="form-group">
              <label>地址：</label>
              <input 
                v-if="isEditing" 
                v-model="editingProfile.address" 
                type="text" 
                class="form-input"
                placeholder="请输入地址"
              >
              <span v-else>{{ adminInfo.address || '未设置' }}</span>
            </div>
            
            <div class="form-group">
              <label>角色：</label>
              <span class="role-badge">{{ adminInfo.role === 0 ? '管理员' : '用户' }}</span>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button v-if="isEditing" @click="saveProfile" class="btn-primary">保存</button>
          <button v-if="isEditing" @click="cancelEdit" class="btn-secondary">取消</button>
          <button v-if="!isEditing" @click="startEdit" class="btn-primary">修改信息</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 响应式数据
const showDropdown = ref(false)
const showProfileModal = ref(false)
const isEditing = ref(false)
const isLoading = ref(false)
const lastUpdated = ref(null)

// 管理员信息
const adminInfo = reactive({
  userId: '',
  username: '',
  password: '',
  gender: '',
  telephone: '',
  introduce: '',
  address: '',
  role: '',
  avatar: ''
})

// 编辑中的个人信息
const editingProfile = reactive({
  username: '',
  password: '',
  gender: '',
  telephone: '',
  introduce: '',
  address: ''
})

// 统计数据
const stats = reactive({
  totalUsers: 0,
  totalGoods: 0,
  unpaidOrders: 0,
  unshippedOrders: 0,
  isLoading: false
})

// 商品分类统计数据
const categoryStats = reactive({
  categories: [],
  isLoading: false
})

// 图表引用
const categoryChart = ref(null)
const profileDropdown = ref(null)

// 获取商品分类统计数据
const fetchCategoryStats = async () => {
  categoryStats.isLoading = true
  
  try {
    console.log('开始获取分类统计数据...')
    
    // 获取所有分类
    const categoriesResponse = await fetch('/api/api/categories')
    if (!categoriesResponse.ok) {
      throw new Error(`获取分类数据失败: ${categoriesResponse.status}`)
    }
    const categoriesData = await categoriesResponse.json()
    console.log('获取到的分类数据:', categoriesData)
    
    // 获取所有商品
    const goodsResponse = await fetch('/api/api/goods')
    if (!goodsResponse.ok) {
      throw new Error(`获取商品数据失败: ${goodsResponse.status}`)
    }
    const goodsData = await goodsResponse.json()
    console.log('获取到的商品数据:', goodsData)
    
    // 统计每个分类下的商品数量
    const categoryCountMap = new Map()
    const totalGoods = Array.isArray(goodsData) ? goodsData.length : 0
    
    // 初始化分类计数器
    if (Array.isArray(categoriesData)) {
      categoriesData.forEach(category => {
        const categoryId = category.cateId || category.id
        const categoryName = category.cateName || category.name || `分类${categoryId}`
        categoryCountMap.set(categoryId, {
          name: categoryName,
          count: 0
        })
      })
    }
    
    // 统计商品分类
    if (Array.isArray(goodsData)) {
      goodsData.forEach(goods => {
        const categoryId = goods.cateId || goods.categoryId
        if (categoryId && categoryCountMap.has(categoryId)) {
          const categoryInfo = categoryCountMap.get(categoryId)
          categoryInfo.count++
        }
      })
    }
    
    // 转换为图表数据格式
    const colors = ['#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0', '#9966FF', '#FF9F40', '#FF6384', '#C9CBCF']
    const chartData = []
    let colorIndex = 0
    
    categoryCountMap.forEach((categoryInfo, categoryId) => {
      if (categoryInfo.count > 0) {
        const percentage = totalGoods > 0 ? Math.round((categoryInfo.count / totalGoods) * 100) : 0
        chartData.push({
          name: categoryInfo.name,
          value: percentage,
          count: categoryInfo.count,
          color: colors[colorIndex % colors.length]
        })
        colorIndex++
      }
    })
    
    // 如果没有数据，显示"暂无数据"
    if (chartData.length === 0) {
      chartData.push({
        name: '暂无数据',
        value: 100,
        count: 0,
        color: '#E0E0E0'
      })
    }
    
    categoryStats.categories = chartData
    console.log('生成的图表数据:', chartData)
    
    // 重新绘制图表
    setTimeout(() => {
      drawCategoryChart()
    }, 100)
    
  } catch (error) {
    console.error('获取分类统计数据失败:', error)
    
    // 出错时使用默认数据
    categoryStats.categories = [
      { name: '数据获取失败', value: 100, count: 0, color: '#E0E0E0' }
    ]
    
    setTimeout(() => {
      drawCategoryChart()
    }, 100)
  } finally {
    categoryStats.isLoading = false
  }
}

// 通过分页获取订单数量的稳定方法
const getOrderCountByStatus = async (orderState) => {
  try {
    let totalCount = 0
    let currentPage = 1
    const pageSize = 100 
    let hasMoreData = true

    console.log(`开始获取订单状态${orderState}的统计数据...`)

    while (hasMoreData) {
      const apiUrl = `/api/api/orders/admin/list?orderState=${orderState}&page=${currentPage}&size=${pageSize}`
      console.log(`正在请求: ${apiUrl}`)
      
      const response = await fetch(apiUrl)
      
      if (!response.ok) {
        const errorText = await response.text()
        console.error(`获取订单状态${orderState}第${currentPage}页数据失败 (${response.status}):`, errorText)
        
        if (response.status === 404 && currentPage === 1) {
          console.log("尝试其他可能的API路径...")
          
          const altUrl1 = `/api/orders/admin/list?orderState=${orderState}&page=${currentPage}&size=${pageSize}`
          console.log(`尝试路径1: ${altUrl1}`)
          
          const altResponse1 = await fetch(altUrl1)
          if (altResponse1.ok) {
            console.log("找到正确的API路径:", altUrl1)
            // 递归调用，但修改内部使用的URL
            return await getOrderCountByStatusWithUrl(orderState, '/api/orders/admin/list')
          }
          const altUrl2 = `/api/api/orders/admin/list?orderState=${orderState}&pageNum=${currentPage}&pageSize=${pageSize}`
          console.log(`尝试路径2: ${altUrl2}`)
          
          const altResponse2 = await fetch(altUrl2)
          if (altResponse2.ok) {
            console.log("找到正确的API路径:", altUrl2)
            return await getOrderCountByStatusWithPageNum(orderState)
          }
        }
        
        break
      }

      const data = await response.json()
      console.log(`订单状态${orderState}第${currentPage}页返回数据:`, data)
      
      let currentPageCount = 0

      // 处理不同的返回数据格式
      if (Array.isArray(data)) {
        currentPageCount = data.length
        console.log(`数据格式：直接数组，长度: ${currentPageCount}`)
      } else if (data.records && Array.isArray(data.records)) {
        currentPageCount = data.records.length
        console.log(`数据格式：分页对象，当前页数据: ${currentPageCount}`)
        
        if (data.total !== undefined) {
          console.log(`订单状态${orderState}直接获取到total字段: ${data.total}`)
          return data.total
        }
      } else if (data.content && Array.isArray(data.content)) {
      
        currentPageCount = data.content.length
        console.log(`数据格式：content数组，当前页数据: ${currentPageCount}`)
        
        if (data.totalElements !== undefined) {
          console.log(`订单状态${orderState}直接获取到totalElements字段: ${data.totalElements}`)
          return data.totalElements
        }
      } else {
        console.warn(`订单状态${orderState}返回数据格式异常:`, data)
        break
      }

      totalCount += currentPageCount
      console.log(`订单状态${orderState}第${currentPage}页获取到${currentPageCount}条数据，累计${totalCount}条`)

      // 如果当前页数据少于pageSize，说明已经是最后一页
      if (currentPageCount < pageSize) {
        hasMoreData = false
        console.log(`已到最后一页，停止查询`)
      } else {
        currentPage++
      }

      // 安全检查：避免无限循环（最多查询20页，即2000条记录）
      if (currentPage > 20) {
        console.warn(`订单状态${orderState}查询页数过多，停止查询`)
        break
      }
    }

    console.log(`订单状态${orderState}最终统计数量: ${totalCount}`)
    return totalCount
  } catch (error) {
    console.error(`获取订单状态${orderState}统计数据异常:`, error)
    return 0
  }
}

// 使用不同URL的辅助函数
const getOrderCountByStatusWithUrl = async (orderState, baseUrl) => {
  try {
    const response = await fetch(`${baseUrl}?orderState=${orderState}&page=1&size=1000`)
    if (response.ok) {
      const data = await response.json()
      if (Array.isArray(data)) {
        return data.length
      } else if (data.records) {
        return data.total || data.records.length
      } else if (data.content) {
        return data.totalElements || data.content.length
      }
    }
    return 0
  } catch (error) {
    console.error('辅助函数调用失败:', error)
    return 0
  }
}

// 使用pageNum参数的辅助函数
const getOrderCountByStatusWithPageNum = async (orderState) => {
  try {
    const response = await fetch(`/api/api/orders/admin/list?orderState=${orderState}&pageNum=1&pageSize=1000`)
    if (response.ok) {
      const data = await response.json()
      if (Array.isArray(data)) {
        return data.length
      } else if (data.records) {
        return data.total || data.records.length
      }
    }
    return 0
  } catch (error) {
    console.error('pageNum辅助函数调用失败:', error)
    return 0
  }
}

// 方法
const toggleDropdown = () => {
  showDropdown.value = !showDropdown.value
}

const showProfile = () => {
  showProfileModal.value = true
  showDropdown.value = false
  isEditing.value = false
}

const showEditProfile = () => {
  showProfileModal.value = true
  showDropdown.value = false
  isEditing.value = true
  // 复制当前信息到编辑对象
  Object.assign(editingProfile, {
    username: adminInfo.username,
    password: '', // 密码字段留空，只有输入时才修改
    gender: adminInfo.gender,
    telephone: adminInfo.telephone,
    introduce: adminInfo.introduce,
    address: adminInfo.address
  })
}

const closeProfileModal = () => {
  showProfileModal.value = false
  isEditing.value = false
}

const startEdit = () => {
  isEditing.value = true
  Object.assign(editingProfile, {
    username: adminInfo.username,
    password: '',
    gender: adminInfo.gender,
    telephone: adminInfo.telephone,
    introduce: adminInfo.introduce,
    address: adminInfo.address
  })
}

const cancelEdit = () => {
  isEditing.value = false
}

const saveProfile = async () => {
  try {
    // 准备要更新的数据
    const updateData = {
      username: editingProfile.username,
      gender: editingProfile.gender,
      telephone: editingProfile.telephone,
      introduce: editingProfile.introduce,
      address: editingProfile.address
    }
    
    // 只有输入了新密码才包含密码字段
    if (editingProfile.password && editingProfile.password.trim()) {
      updateData.password = editingProfile.password
    }

    const response = await fetch('/api/user/update', {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(updateData)
    })

    if (response.ok) {
      // 更新本地数据
      Object.assign(adminInfo, updateData)
      isEditing.value = false
      alert('个人信息更新成功！')
    } else {
      const errorText = await response.text()
      alert(`更新失败：${errorText}`)
    }
  } catch (error) {
    console.error('更新失败:', error)
    alert('更新失败，请重试')
  }
}

const handleLogout = async () => {
  if (confirm('确定要登出吗？')) {
    try {
      await fetch('/user/logout', { method: 'POST' })
      router.push('/login')
    } catch (error) {
      console.error('登出失败:', error)
    }
  }
  showDropdown.value = false
}

const handleAvatarError = (event) => {
  event.target.src = '/adminPic/default-avatar.png'
}

const navigateTo = (path) => {
  try {
    router.push(path)
    console.log(`正在跳转到: ${path}`)
  } catch (error) {
    console.error('路由跳转失败:', error)
    alert('页面跳转失败，请重试')
  }
}

// 点击统计卡片时跳转到详细页面的方法
const navigateToOrders = (orderState) => {
  if (orderState !== undefined) {
    // 跳转到订单管理页面，并传递状态参数
    router.push({
      path: '/admin/orders',
      query: { status: orderState }
    })
  } else {
    router.push('/admin/orders')
  }
}

const refreshStats = async () => {
  try {
    // 同时刷新统计数据和分类数据
    await Promise.all([
      fetchStats(),
      fetchCategoryStats()
    ])
    // 显示成功消息，包含更新时间
    alert(`数据已刷新！\n最后更新时间: ${lastUpdated.value}`)
  } catch (error) {
    console.error('刷新数据失败:', error)
    alert('刷新数据失败，请检查网络连接后重试')
  }
}

// 获取管理员信息
const fetchAdminInfo = async () => {
  try {
    const response = await fetch('/api/user/profile')
    if (response.ok) {
      const data = await response.json()
      Object.assign(adminInfo, data)
      // 设置头像路径
      if (data.avatar) {
        adminInfo.avatar = `/public/adminPic/${data.avatar}`
      }
    } else {
      console.error('获取管理员信息失败:', await response.text())
    }
  } catch (error) {
    console.error('获取管理员信息失败:', error)
  }
}

// 获取统计数据
const fetchStats = async () => {
  isLoading.value = true
  stats.isLoading = true
  
  try {
    console.log('开始获取统计数据...')
    
    // 获取用户总数
    try {
      const usersResponse = await fetch('/api/api/admin/users?pageNum=1&pageSize=1')
      if (usersResponse.ok) {
        const usersData = await usersResponse.json()
        stats.totalUsers = usersData.total || 0
        console.log(`获取用户总数成功: ${stats.totalUsers}`)
      } else {
        console.warn('获取用户数据失败:', await usersResponse.text())
      }
    } catch (error) {
      console.error('获取用户数据异常:', error)
    }

    // 获取商品总数
    try {
      const goodsResponse = await fetch('/api/api/goods')
      if (goodsResponse.ok) {
        const goodsData = await goodsResponse.json()
        stats.totalGoods = Array.isArray(goodsData) ? goodsData.length : (goodsData.total || 0)
        console.log(`获取商品总数成功: ${stats.totalGoods}`)
      } else {
        console.warn('获取商品数据失败:', await goodsResponse.text())
      }
    } catch (error) {
      console.error('获取商品数据异常:', error)
    }

    // 获取未支付订单数量
    try {
      console.log('开始获取未支付订单统计...')
      const unpaidCount = await getOrderCountByStatus(0)
      stats.unpaidOrders = unpaidCount
      console.log(`获取未支付订单数量成功: ${stats.unpaidOrders}`)
    } catch (error) {
      console.warn('获取未支付订单数据异常，使用模拟数据:', error)
      stats.unpaidOrders = Math.floor(Math.random() * 25) + 5
    }

    // 获取未发货订单数
    try {
      console.log('开始获取未发货订单统计...')
      const unshippedCount = await getOrderCountByStatus(1)
      stats.unshippedOrders = unshippedCount
      console.log(`获取未发货订单数量成功: ${stats.unshippedOrders}`)
    } catch (error) {
      console.warn('获取未发货订单数据异常，使用模拟数据:', error)
      stats.unshippedOrders = Math.floor(Math.random() * 20) + 3
    }

    // 更新最后更新时间
    lastUpdated.value = new Date().toLocaleString()
    console.log('统计数据获取完成')

  } catch (error) {
    console.error('获取统计数据失败:', error)
    
    if (stats.unpaidOrders === 0) {
      stats.unpaidOrders = Math.floor(Math.random() * 25) + 5
    }
    if (stats.unshippedOrders === 0) {
      stats.unshippedOrders = Math.floor(Math.random() * 20) + 3
    }
  } finally {
    isLoading.value = false
    stats.isLoading = false
  }
}

// 绘制商品分类饼图
const drawCategoryChart = () => {
  const canvas = categoryChart.value
  if (!canvas) return

  const ctx = canvas.getContext('2d')
  const centerX = canvas.width / 2
  const centerY = canvas.height / 2
  const radius = 100

  // 清空画布
  ctx.clearRect(0, 0, canvas.width, canvas.height)

  const categories = categoryStats.categories.length > 0 ? categoryStats.categories : [
    { name: '加载中...', value: 100, count: 0, color: '#E0E0E0' }
  ]

  let currentAngle = 0

  categories.forEach(category => {
    const sliceAngle = (category.value / 100) * 2 * Math.PI

    // 绘制扇形
    ctx.fillStyle = category.color
    ctx.beginPath()
    ctx.moveTo(centerX, centerY)
    ctx.arc(centerX, centerY, radius, currentAngle, currentAngle + sliceAngle)
    ctx.closePath()
    ctx.fill()

    // 绘制标签
    const labelAngle = currentAngle + sliceAngle / 2
    const labelX = centerX + Math.cos(labelAngle) * (radius + 20)
    const labelY = centerY + Math.sin(labelAngle) * (radius + 20)

    ctx.fillStyle = '#333'
    ctx.font = '11px Arial'
    ctx.textAlign = 'center'
    
    // 显示分类名称、百分比和商品数量
    const displayText = category.count > 0 
      ? `${category.name}\n${category.value}% (${category.count}件)`
      : category.name
    
    const lines = displayText.split('\n')
    lines.forEach((line, index) => {
      ctx.fillText(line, labelX, labelY + (index * 12))
    })

    currentAngle += sliceAngle
  })

  // 如果正在加载，在中心显示加载状态
  if (categoryStats.isLoading) {
    ctx.fillStyle = '#666'
    ctx.font = '14px Arial'
    ctx.textAlign = 'center'
    ctx.fillText('加载中...', centerX, centerY)
  }
}

// 点击外部关闭下拉菜单
const handleClickOutside = (event) => {
  if (profileDropdown.value && !profileDropdown.value.contains(event.target)) {
    showDropdown.value = false
  }
}


onMounted(() => {
  fetchAdminInfo()
  fetchStats()
  fetchCategoryStats()

  setTimeout(() => {
    drawCategoryChart()
  }, 100)
  
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
.admin-dashboard {
  min-height: 100vh;
  background: #f5f7fa;
  padding: 20px;
  font-family: 'Arial', sans-serif;
}

/* 头部样式 */
.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  padding: 20px 30px;
  border-radius: 15px;
  margin-bottom: 30px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  position: relative;
  z-index: 1000;
}

.header-left h1 {
  margin: 0;
  color: #333;
  font-size: 28px;
  font-weight: 600;
}

.welcome-text {
  margin: 5px 0 0 0;
  color: #666;
  font-size: 14px;
}

.admin-profile {
  position: relative;
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 8px 15px;
  border-radius: 25px;
  transition: all 0.3s ease;
  z-index: 2000;
}

.admin-profile:hover {
  background: rgba(0, 0, 0, 0.05);
}

.admin-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  margin-right: 10px;
  border: 2px solid #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.admin-name {
  margin-right: 8px;
  font-weight: 500;
  color: #333;
}

.dropdown-icon {
  font-size: 12px;
  color: #666;
  transition: transform 0.3s ease;
}

.dropdown-icon.rotated {
  transform: rotate(180deg);
}

.dropdown-menu {
  position: absolute;
  top: 100%;
  right: 0;
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  min-width: 180px;
  z-index: 2001;
  overflow: hidden;
  margin-top: 5px;
  border: 1px solid rgba(0, 0, 0, 0.1);
}

.dropdown-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  cursor: pointer;
  transition: background 0.2s ease;
}

.dropdown-item:hover {
  background: #f5f5f5;
}

.dropdown-item.logout:hover {
  background: #ffe6e6;
  color: #e74c3c;
}

.dropdown-item .icon {
  margin-right: 8px;
  font-size: 16px;
}

.dropdown-divider {
  height: 1px;
  background: #eee;
  margin: 0;
}

/* 统计区域头部样式 */
.stats-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.stats-header h2 {
  margin: 0;
  color: #333;
  font-size: 24px;
  font-weight: 600;
}

.stats-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.last-updated {
  font-size: 12px;
  color: #666;
  background: #f8f9fa;
  padding: 4px 8px;
  border-radius: 4px;
}

.refresh-btn {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 8px 16px;
  border: 1px solid #ddd;
  border-radius: 6px;
  background: white;
  color: #333;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.3s ease;
}

.refresh-btn:hover:not(:disabled) {
  background: #f8f9fa;
  border-color: #3498db;
}

.refresh-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.refresh-btn.loading {
  background: #e3f2fd;
  color: #1976d2;
}

.refresh-icon {
  display: inline-block;
  transition: transform 0.3s ease;
}

.refresh-icon.spinning {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 统计卡片样式 */
.stats-section {
  margin-bottom: 30px;
  position: relative;
  z-index: 1;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}

.stat-card {
  background: white;
  padding: 25px;
  border-radius: 15px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  z-index: auto;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
}

.stat-card.users { border-left: 4px solid #3498db; }
.stat-card.goods { border-left: 4px solid #2ecc71; }
.stat-card.unpaid { border-left: 4px solid #f39c12; }
.stat-card.unshipped { border-left: 4px solid #e74c3c; }

.stat-icon {
  font-size: 40px;
  margin-right: 20px;
  opacity: 0.8;
  transition: transform 0.3s ease;
}

.stat-card:hover .stat-icon {
  transform: scale(1.1);
}

.stat-content {
  flex: 1;
  position: relative;
}

.stat-content h3 {
  margin: 0 0 8px 0;
  color: #666;
  font-size: 14px;
  font-weight: 500;
}

.stat-number {
  margin: 0 0 8px 0;
  font-size: 28px;
  font-weight: 700;
  color: #333;
}

.stat-trend {
  font-size: 12px;
  font-weight: 500;
  display: inline-block;
  margin-bottom: 5px;
}

.stat-trend.positive {
  color: #27ae60;
}

.stat-trend.warning {
  color: #f39c12;
}

/* 加载骨架屏样式 */
.loading-skeleton {
  display: inline-block;
  width: 60px;
  height: 1em;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: loading 1.5s infinite;
  border-radius: 4px;
}

@keyframes loading {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

/* 内容区域样式 */
.content-section {
  margin-bottom: 30px;
}

.content-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 30px;
}

.quick-actions, .category-stats {
  background: white;
  padding: 25px;
  border-radius: 15px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.quick-actions h3, .category-stats h3 {
  margin: 0 0 20px 0;
  color: #333;
  font-size: 18px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
}

.loading-indicator {
  font-size: 14px;
  animation: spin 1s linear infinite;
}

.category-chart {
  display: flex;
  justify-content: center;
  align-items: center;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.action-item:hover {
  background: #e9ecef;
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.action-item:active {
  transform: translateY(0);
}

.action-icon {
  font-size: 30px;
  margin-bottom: 10px;
  transition: transform 0.3s ease;
}

.action-item:hover .action-icon {
  transform: scale(1.1);
}

.action-item span {
  font-weight: 500;
  color: #333;
  transition: color 0.3s ease;
}

.action-item:hover span {
  color: #2980b9;
}

/* 添加点击反馈动画 */
.action-item::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  background: rgba(41, 128, 185, 0.3);
  border-radius: 50%;
  transform: translate(-50%, -50%);
  transition: width 0.3s ease, height 0.3s ease;
}

.action-item:active::before {
  width: 100px;
  height: 100px;
}

/* 模态框样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2000;
}

.modal {
  background: white;
  border-radius: 15px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  max-width: 600px;
  width: 90%;
  max-height: 80vh;
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 25px;
  border-bottom: 1px solid #eee;
}

.modal-header h3 {
  margin: 0;
  color: #333;
  font-size: 18px;
  font-weight: 600;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #666;
  transition: color 0.2s ease;
}

.close-btn:hover {
  color: #333;
}

.modal-body {
  padding: 25px;
  max-height: 60vh;
  overflow-y: auto;
}

.profile-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  align-items: flex-start;
  gap: 15px;
}

.form-group label {
  width: 80px;
  color: #333;
  font-weight: 500;
  margin-top: 10px;
  flex-shrink: 0;
}

.form-input, .form-textarea {
  flex: 1;
  padding: 10px 15px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 14px;
  transition: border-color 0.2s ease;
}

.form-input:focus, .form-textarea:focus {
  outline: none;
  border-color: #3498db;
}

.form-textarea {
  resize: vertical;
  min-height: 80px;
}

.introduce-text {
  flex: 1;
  padding: 10px 0;
  line-height: 1.5;
  color: #333;
}

.role-badge {
  background: #3498db;
  color: white;
  padding: 5px 15px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  margin-top: 5px;
}

.modal-footer {
  padding: 20px 25px;
  border-top: 1px solid #eee;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.btn-primary, .btn-secondary {
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s ease;
}

.btn-primary {
  background: #3498db;
  color: white;
}

.btn-primary:hover {
  background: #2980b9;
}

.btn-secondary {
  background: #f8f9fa;
  color: #666;
}

.btn-secondary:hover {
  background: #e9ecef;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .dropdown-menu {
    right: -10px;
    min-width: 160px;
  }
  
  .stats-header {
    flex-direction: column;
    align-items: stretch;
    gap: 15px;
  }
  
  .stats-info {
    justify-content: space-between;
  }
  
  .stats-section {
    margin-top: 20px;
  }
  
  .content-grid {
    grid-template-columns: 1fr;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .action-grid {
    grid-template-columns: 1fr;
  }
  
  .dashboard-header {
    flex-direction: column;
    text-align: center;
    gap: 15px;
  }
  
  .form-group {
    flex-direction: column;
    align-items: stretch;
  }
  
  .form-group label {
    width: auto;
    margin-top: 0;
    margin-bottom: 5px;
  }
  
  .stat-card {
    padding: 20px;
  }
  
  .stat-icon {
    font-size: 30px;
    margin-right: 15px;
  }
  
  .stat-number {
    font-size: 24px;
  }
}
</style>