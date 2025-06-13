<template>
  <div class="product-management">
    <div class="page-header">
      <h2>商品管理</h2>
      <el-button type="primary" @click="showAddDialog">
        <el-icon><Plus /></el-icon>
        添加商品
      </el-button>
    </div>

    <!-- 搜索和筛选 -->
    <div class="filter-section">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="商品名称">
          <el-input
            v-model="searchForm.name"
            placeholder="请输入商品名称"
            clearable
            @input="handleSearch"
          />
        </el-form-item>
        <el-form-item label="商品状态">
          <el-select 
            v-model="searchForm.status" 
            placeholder="请选择状态" 
            clearable
            style="width: 200px;" 
          >
            <el-option label="上架" value="active" />
            <el-option label="下架" value="inactive" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 商品表格 -->
    <div class="table-section">
      <el-table 
        :data="filteredProducts" 
        style="width: 100%"
        v-loading="loading"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="商品图片" width="100">
          <template #default="scope">
            <el-image
              style="width: 60px; height: 60px"
              :src="scope.row.image || '/placeholder-product.png'"
              fit="cover"
            />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商品名称" min-width="150" />
        <el-table-column prop="price" label="价格" width="100">
          <template #default="scope">
            ¥{{ scope.row.price }}
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="100" />
        <el-table-column prop="sales" label="销量" width="100" />
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'active' ? 'success' : 'danger'">
              {{ scope.row.status === 'active' ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="320" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="editProduct(scope.row)">编辑</el-button>
            <el-button 
              size="small" 
              :type="scope.row.status === 'active' ? 'warning' : 'success'"
              @click="toggleStatus(scope.row)"
            >
              {{ scope.row.status === 'active' ? '下架' : '上架' }}
            </el-button>
            <el-button 
              size="small" 
              type="danger" 
              @click="deleteProduct(scope.row)"
            >
              删除
            </el-button>
            <el-button 
              size="small" 
              type="info"
              @click="showProductDetail(scope.row)"
            >
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10]"
          layout="total, prev, pager, next, jumper"
          :total="totalProducts"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
        <span class="page-size-info">10/page</span>
      </div>
    </div>

    <!-- 添加/编辑商品组件 (复用同一个组件) -->
    <AddProduct
      v-model:visible="productDialogVisible"
      :product-data="currentProductData"
      :available-categories="availableCategories"
      :existing-products="products"
      :is-edit="isEditMode"
      @success="handleProductSuccess"
      @cancel="handleProductCancel"
    />

    <!-- 商品详情组件 -->
    <ProductDetail
      v-model:visible="detailDialogVisible"
      :product-detail="selectedProductDetail"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'
import { Plus } from '@element-plus/icons-vue'

// 导入子组件
import AddProduct from './components/AddProduct.vue'
import ProductDetail from './components/ProductDetail.vue'

// 使用统一的类型定义
interface Product {
  id: number
  name: string
  price: number
  image?: string
  description?: string
  stock: number
  sales: number
  category: string
  categoryId?: number
  status: 'active' | 'inactive'
  createTime: string
  creationdate: string
  expirationdate: string
  storagemethod: string
  size: number | string
  kgs: string
}

interface ProductForm {
  id?: number
  name: string
  price: number
  image?: string
  description?: string
  stock: number
  categoryName: string
  cateId?: number
  status: 'active' | 'inactive'
  creationdate: string
  expirationdate: string
  storagemethod: string
  size: number
  kgs: string
  imageFile?: string
}

interface SearchForm {
  name: string
  status: string
}

interface Category {
  catId: number
  name: string
}

// 响应式数据
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const totalProducts = ref(0)

// 合并对话框控制 - 使用一个组件处理添加和编辑
const productDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const isEditMode = ref(false)

// 数据
const products = ref<Product[]>([])
const availableCategories = ref<Category[]>([])
const selectedProductDetail = ref<Product | null>(null)

// 当前操作的商品数据
const currentProductData = ref<ProductForm>({
  name: '',
  price: 0,
  image: '',
  description: '',
  stock: 0,
  categoryName: '',
  cateId: undefined,
  status: 'active',
  creationdate: '',
  expirationdate: '',
  storagemethod: '常温保存',
  size: 0,
  kgs: '',
  imageFile: ''
})

const searchForm = ref<SearchForm>({
  name: '',
  status: ''
})

// 🔥 工具函数：获取当前时间 YYYY-MM-DD HH:mm:ss 格式
const getCurrentDateTime = (): string => {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  const hours = String(now.getHours()).padStart(2, '0')
  const minutes = String(now.getMinutes()).padStart(2, '0')
  const seconds = String(now.getSeconds()).padStart(2, '0')
  
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

// 🔥 工具函数：提取日期部分
const extractDateOnly = (dateTimeString: string): string => {
  if (!dateTimeString) return ''
  
  try {
    // 如果包含时间，提取日期部分
    if (dateTimeString.includes(' ')) {
      return dateTimeString.split(' ')[0]
    }
    
    // 如果是ISO格式，转换为YYYY-MM-DD
    if (dateTimeString.includes('T')) {
      return new Date(dateTimeString).toISOString().split('T')[0]
    }
    
    return dateTimeString
  } catch (error) {
    console.error('日期提取错误:', error)
    return ''
  }
}

// 🔥 工具函数：格式化生产日期
const formatCreationDateTime = (dateString: string): string => {
  if (!dateString) return ''
  
  // 如果只有日期（YYYY-MM-DD），添加 00:00:00
  if (dateString.match(/^\d{4}-\d{2}-\d{2}$/)) {
    return `${dateString} 00:00:00`
  }
  
  // 如果已经包含时间，直接返回
  return dateString
}

// 🔥 通过分类名称搜索分类ID的函数
const searchCategoryId = async (categoryName: string): Promise<number | undefined> => {
  if (!categoryName) return undefined

  try {
    const response = await axios.get('/api/api/categories/search', {
      params: {
        name: categoryName
      }
    })
    
    if (response.data && response.data.cateId) {
      return response.data.cateId
    }
    
    return undefined
  } catch (error) {
    console.error('查询分类ID失败:', error)
    return undefined
  }
}

// 🔥 工具函数：根据分类名称查找分类ID
const getCategoryIdByName = (categoryName: string): number | undefined => {
  const category = availableCategories.value.find(cat => cat.name === categoryName)
  return category?.catId
}

// 计算属性
const filteredProducts = computed(() => {
  let filtered = products.value

  if (searchForm.value.name) {
    filtered = filtered.filter(product =>
      product.name.toLowerCase().includes(searchForm.value.name.toLowerCase())
    )
  }

  if (searchForm.value.status) {
    filtered = filtered.filter(product => product.status === searchForm.value.status)
  }

  totalProducts.value = filtered.length
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filtered.slice(start, end)
})

// 方法
onMounted(() => {
  loadProducts()
  loadCategories()
})

// 加载商品分类
const loadCategories = async () => {
  try {
    const response = await axios.get('/api/api/categories')
    console.log('分类数据:', response.data)
    availableCategories.value = response.data
  } catch (error) {
    console.error('加载分类数据失败:', error)
    ElMessage.error('加载商品分类失败')
    updateAvailableCategoriesFromProducts()
  }
}

// 从现有商品中提取分类作为备选方案
const updateAvailableCategoriesFromProducts = () => {
  const categorySet = new Set<string>()
  products.value.forEach(product => {
    if (product.category) {
      categorySet.add(product.category)
    }
  })
  availableCategories.value = Array.from(categorySet).map((name, index) => ({
    catId: index + 1,
    name
  }))
}

// 🔥 数据转换函数 - 增强处理
const transformProductData = (item: any): Product => {
  // 安全地处理可能为null或undefined的字段
  const safeGet = (value: any, defaultValue: any = '') => {
    return value !== null && value !== undefined ? value : defaultValue
  }

  return {
    id: safeGet(item.goodsId, 0),
    name: safeGet(item.name),
    price: safeGet(item.price, 0),
    stock: safeGet(item.num, 0),
    sales: safeGet(item.sales, 0),
    category: safeGet(item.categoryName),
    categoryId: safeGet(item.cateId),
    description: safeGet(item.desc),
    image: safeGet(item.imgUrl),
    status: item.state === 1 ? 'active' : 'inactive',
    createTime: item.addtime ? new Date(item.addtime).toLocaleString('zh-CN') : '',
    creationdate: safeGet(item.creationdate),
    expirationdate: safeGet(item.expirationdate),
    storagemethod: safeGet(item.storagemethod, '常温保存'),
    size: safeGet(item.size, 0),
    kgs: safeGet(item.kgs)
  }
}

// 加载商品数据
const loadProducts = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/api/goods')
    console.log('API响应数据:', response.data)
    
    if (Array.isArray(response.data)) {
      products.value = response.data.map(transformProductData)
      console.log('处理后的商品数据:', products.value)
    } else {
      console.warn('API返回的数据不是数组格式:', response.data)
      products.value = []
    }
    
    if (availableCategories.value.length === 0) {
      updateAvailableCategoriesFromProducts()
    }
  } catch (error) {
    console.error('加载商品数据失败:', error)
    ElMessage.error('加载商品数据失败')
    products.value = []
  } finally {
    loading.value = false
  }
}

// 搜索相关
const handleSearch = () => {
  currentPage.value = 1
}

const resetSearch = () => {
  searchForm.value = {
    name: '',
    status: ''
  }
  currentPage.value = 1
}

// 分页相关
const handleSizeChange = (val: number) => {
  pageSize.value = val
  currentPage.value = 1
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
}

// 显示添加对话框
const showAddDialog = () => {
  isEditMode.value = false
  // 重置为默认值
  const currentDate = new Date().toISOString().split('T')[0]
  currentProductData.value = {
    name: '',
    price: 0,
    image: '',
    description: '',
    stock: 0,
    categoryName: '',
    cateId: undefined,
    status: 'active',
    creationdate: currentDate,
    expirationdate: '',
    storagemethod: '常温保存',
    size: 0,
    kgs: '',
    imageFile: ''
  }
  productDialogVisible.value = true
}

// 🔥 编辑商品 - 修复日期格式处理和分类ID
const editProduct = async (product: Product) => {
  isEditMode.value = true
  
  console.log('=== 编辑商品调试信息 ===')
  console.log('原始商品数据:', product)
  
  // 正确处理日期格式 - 只提取日期部分用于日期选择器
  const dateOnly = extractDateOnly(product.creationdate)
  console.log('提取的日期:', dateOnly)
  
  // 🔥 查询分类ID
  const categoryId = product.categoryId || await searchCategoryId(product.category)
  console.log('分类ID:', categoryId)
  
  currentProductData.value = {
    id: product.id,
    name: product.name,
    price: product.price,
    image: product.image,
    description: product.description,
    stock: product.stock,
    categoryName: product.category,
    cateId: categoryId, // 🔥 设置查询到的分类ID
    status: product.status,
    creationdate: dateOnly, // 🔥 只传递日期部分给日期选择器
    expirationdate: product.expirationdate,
    storagemethod: product.storagemethod || '常温保存',
    size: Number(product.size) || 0, // 🔥 确保size是数字类型
    kgs: product.kgs || '',
    imageFile: product.image || ''
  }
  
  console.log('设置的表单数据:', currentProductData.value)
  productDialogVisible.value = true
}

// 显示商品详情
const showProductDetail = (product: Product) => {
  console.log('选中的商品数据:', product)
  selectedProductDetail.value = product
  detailDialogVisible.value = true
}

// 🔥 切换商品状态 - 修正日期时间格式
const toggleStatus = async (product: Product) => {
  const action = product.status === 'active' ? '下架' : '上架'
  try {
    await ElMessageBox.confirm(`确定要${action}商品 "${product.name}" 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    console.log('=== 状态切换调试信息 ===')
    console.log('切换前状态:', product.status)
    console.log('原始创建日期:', product.creationdate)

    // 🔥 格式化生产日期：提取日期部分 + 00:00:00
    const dateOnly = extractDateOnly(product.creationdate)
    const formattedCreationDate = formatCreationDateTime(dateOnly)
    console.log('格式化后的生产日期:', formattedCreationDate)

    // 🔥 查询分类ID
    const categoryId = product.categoryId || await searchCategoryId(product.category)
    
    if (!categoryId) {
      ElMessage.error('无法获取分类ID，操作失败')
      return
    }

    const requestData = {
      goodsId: product.id,
      name: product.name,
      desc: product.description || '',
      categoryName: product.category,
      cateId: categoryId, // 🔥 使用查询到的分类ID
      price: Number(product.price),
      num: Number(product.stock),
      size: Number(product.size) || 0,
      kgs: product.kgs || '',
      creationdate: formattedCreationDate, // 🔥 生产日期：原日期 + 00:00:00
      expirationdate: product.expirationdate || '',
      storagemethod: product.storagemethod || '常温保存',
      addtime: getCurrentDateTime(), // 🔥 当前时间 YYYY-MM-DD HH:mm:ss
      state: product.status === 'active' ? 0 : 1, // 🔥 切换状态
      imgUrl: product.image || ''
    }

    console.log('发送的状态切换数据:', JSON.stringify(requestData, null, 2))

    const response = await axios.put(`/api/api/goods/${product.id}`, requestData)
    console.log('状态切换响应:', response)

    // 更新本地状态
    product.status = product.status === 'active' ? 'inactive' : 'active'
    console.log('切换后状态:', product.status)
    
    ElMessage.success(`商品${action}成功`)
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error(`${action}商品失败:`, error)
      if (error.response) {
        console.error('错误响应:', error.response.data)
        ElMessage.error(`${action}失败: ${error.response.data?.message || error.response.statusText}`)
      } else {
        ElMessage.error(`${action}失败`)
      }
    }
  }
}

// 🔥 删除商品 - 增强错误处理
const deleteProduct = async (product: Product) => {
  try {
    await ElMessageBox.confirm(`确定要删除商品 "${product.name}" 吗？`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'error'
    })

    console.log('=== 删除商品调试信息 ===')
    console.log('要删除的商品ID:', product.id)

    const response = await axios.delete(`/api/api/goods/${product.id}`)
    console.log('删除响应:', response)

    // 从本地数组中移除
    const index = products.value.findIndex(p => p.id === product.id)
    if (index !== -1) {
      products.value.splice(index, 1)
      console.log('已从本地列表中移除商品')
    }
    
    ElMessage.success('商品删除成功')
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除商品失败:', error)
      if (error.response) {
        console.error('删除错误响应:', error.response.data)
        ElMessage.error(`删除失败: ${error.response.data?.message || error.response.statusText}`)
      } else {
        ElMessage.error('删除失败')
      }
    }
  }
}

// 处理子组件事件
const handleProductSuccess = () => {
  console.log('商品操作成功，重新加载商品列表')
  loadProducts() // 重新加载商品列表
}

const handleProductCancel = () => {
  console.log('用户取消了商品操作')
  // 取消操作，可以在这里添加额外逻辑
}
</script>

<style scoped>
.product-management {
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

.filter-section {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.table-section {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 15px;
}

.page-size-info {
  color: #606266;
  font-size: 14px;
}
</style>