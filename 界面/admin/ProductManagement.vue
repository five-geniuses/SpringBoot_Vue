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
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
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
              :preview-src-list="[scope.row.image || '/placeholder-product.png']"
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
        <el-table-column label="操作" width="200" fixed="right">
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
          :total="totalProducts"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 添加/编辑商品对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :before-close="handleDialogClose"
    >
      <el-form
        ref="productFormRef"
        :model="productForm"
        :rules="productRules"
        label-width="100px"
      >
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="productForm.name" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="商品价格" prop="price">
          <el-input-number
            v-model="productForm.price"
            :min="0"
            :precision="2"
            placeholder="请输入商品价格"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="原价" prop="originalPrice">
          <el-input-number
            v-model="productForm.originalPrice"
            :min="0"
            :precision="2"
            placeholder="请输入原价（可选）"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="库存数量" prop="stock">
          <el-input-number
            v-model="productForm.stock"
            :min="0"
            placeholder="请输入库存数量"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="商品分类" prop="category">
          <el-select v-model="productForm.category" placeholder="请选择分类" style="width: 100%">
            <el-option label="糖果" value="糖果" />
            <el-option label="巧克力" value="巧克力" />
            <el-option label="软糖" value="软糖" />
            <el-option label="硬糖" value="硬糖" />
            <el-option label="棒棒糖" value="棒棒糖" />
          </el-select>
        </el-form-item>
        <el-form-item label="商品描述" prop="description">
          <el-input
            v-model="productForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入商品描述"
          />
        </el-form-item>
        <el-form-item label="商品图片" prop="image">
          <el-input v-model="productForm.image" placeholder="请输入图片URL" />
          <div v-if="productForm.image" class="image-preview">
            <el-image
              style="width: 100px; height: 100px; margin-top: 10px"
              :src="productForm.image"
              fit="cover"
            />
          </div>
        </el-form-item>
        <el-form-item label="商品状态" prop="status">
          <el-radio-group v-model="productForm.status">
            <el-radio value="active">上架</el-radio>
            <el-radio value="inactive">下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveProduct" :loading="saving">
            {{ isEdit ? '更新' : '添加' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

interface Product {
  id: number
  name: string
  price: number
  originalPrice?: number
  image?: string
  description?: string
  stock: number
  sales: number
  category: string
  status: 'active' | 'inactive'
  createTime: string
}

interface ProductForm {
  id?: number
  name: string
  price: number
  originalPrice?: number
  image?: string
  description?: string
  stock: number
  category: string
  status: 'active' | 'inactive'
}

interface SearchForm {
  name: string
  status: string
}

// 响应式数据
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const totalProducts = ref(0)

const productFormRef = ref<FormInstance>()

const searchForm = ref<SearchForm>({
  name: '',
  status: ''
})

const productForm = ref<ProductForm>({
  name: '',
  price: 0,
  originalPrice: 0,
  image: '',
  description: '',
  stock: 0,
  category: '',
  status: 'active'
})

// 模拟商品数据
const products = ref<Product[]>([
  {
    id: 1,
    name: '红色的糖果',
    price: 56,
    originalPrice: 80,
    stock: 184,
    sales: 120,
    category: '糖果',
    description: '红色经典糖果，甜而不腻',
    image: '/placeholder-product.png',
    status: 'active',
    createTime: '2025-01-01 10:00:00'
  },
  {
    id: 2,
    name: '绿的糖果',
    price: 67,
    originalPrice: 90,
    stock: 193,
    sales: 88,
    category: '糖果',
    description: '清新绿色糖果',
    image: '/placeholder-product.png',
    status: 'active',
    createTime: '2025-01-02 11:00:00'
  },
  {
    id: 3,
    name: '黄色的糖果',
    price: 80,
    originalPrice: 100,
    stock: 0,
    sales: 75,
    category: '糖果',
    description: '阳光黄色糖果',
    image: '/placeholder-product.png',
    status: 'inactive',
    createTime: '2025-01-03 12:00:00'
  }
])

// 表单验证规则
const productRules = {
  name: [
    { required: true, message: '请输入商品名称', trigger: 'blur' }
  ],
  price: [
    { required: true, message: '请输入商品价格', trigger: 'blur' }
  ],
  stock: [
    { required: true, message: '请输入库存数量', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择商品分类', trigger: 'change' }
  ]
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

const dialogTitle = computed(() => isEdit.value ? '编辑商品' : '添加商品')

// 方法
onMounted(() => {
  loadProducts()
})

const loadProducts = async () => {
  loading.value = true
  try {
    // 这里可以调用API获取商品数据
    // const response = await axios.get('/api/admin/products')
    // products.value = response.data
    await new Promise(resolve => setTimeout(resolve, 500)) // 模拟加载
  } catch (error) {
    ElMessage.error('加载商品数据失败')
  } finally {
    loading.value = false
  }
}

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

const handleSizeChange = (val: number) => {
  pageSize.value = val
  currentPage.value = 1
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
}

const showAddDialog = () => {
  isEdit.value = false
  productForm.value = {
    name: '',
    price: 0,
    originalPrice: 0,
    image: '',
    description: '',
    stock: 0,
    category: '',
    status: 'active'
  }
  dialogVisible.value = true
}

const editProduct = (product: Product) => {
  isEdit.value = true
  productForm.value = { ...product }
  dialogVisible.value = true
}

const handleDialogClose = () => {
  productFormRef.value?.resetFields()
}

const saveProduct = async () => {
  try {
    await productFormRef.value?.validate()
    saving.value = true

    if (isEdit.value) {
      // 更新商品
      const index = products.value.findIndex(p => p.id === productForm.value.id)
      if (index !== -1) {
        products.value[index] = {
          ...products.value[index],
          ...productForm.value
        }
      }
      ElMessage.success('商品更新成功')
    } else {
      // 添加商品
      const newProduct: Product = {
        ...productForm.value,
        id: Date.now(),
        sales: 0,
        createTime: new Date().toLocaleString('zh-CN')
      } as Product
      products.value.unshift(newProduct)
      ElMessage.success('商品添加成功')
    }

    dialogVisible.value = false
  } catch (error) {
    console.error('保存商品失败:', error)
  } finally {
    saving.value = false
  }
}

const toggleStatus = async (product: Product) => {
  const action = product.status === 'active' ? '下架' : '上架'
  try {
    await ElMessageBox.confirm(`确定要${action}商品 "${product.name}" 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    product.status = product.status === 'active' ? 'inactive' : 'active'
    ElMessage.success(`商品${action}成功`)
  } catch {
    // 用户取消
  }
}

const deleteProduct = async (product: Product) => {
  try {
    await ElMessageBox.confirm(`确定要删除商品 "${product.name}" 吗？`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'error'
    })

    const index = products.value.findIndex(p => p.id === product.id)
    if (index !== -1) {
      products.value.splice(index, 1)
      ElMessage.success('商品删除成功')
    }
  } catch {
    // 用户取消
  }
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

.page-header h2 {
  margin: 0;
  color: #2c3e50;
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

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.image-preview {
  margin-top: 10px;
}

.dialog-footer {
  text-align: right;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 15px;
  }
  
  .search-form {
    display: block;
  }
  
  .search-form .el-form-item {
    margin-bottom: 15px;
  }
}
</style>