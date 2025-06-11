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
            placeholder="请选择状态" clearable
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
        <el-form-item label="库存数量" prop="stock">
          <el-input-number
            v-model="productForm.stock"
            :min="0"
            placeholder="请输入库存数量"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="商品分类" prop="categoryName">
          <el-select v-model="productForm.categoryName" placeholder="请选择分类" style="width: 100%">
            <el-option 
              v-for="category in availableCategories" 
              :key="category" 
              :label="category" 
              :value="category" 
            />
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

    <!-- 美化的商品详情弹窗 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="商品详情"
      width="800px"
      class="product-detail-dialog"
    >
      <div class="product-detail-container" v-if="selectedProductDetail">
        <!-- 商品头部信息 -->
        <div class="product-header">
          <div class="product-image-section">
            <el-image
              :src="selectedProductDetail.image || '/placeholder-product.png'"
              fit="cover"
              class="product-main-image"
            />
          </div>
          <div class="product-basic-info">
            <h2 class="product-title">{{ selectedProductDetail.name }}</h2>
            <div class="product-price">
              <span class="price-symbol">¥</span>
              <span class="price-value">{{ selectedProductDetail.price }}</span>
            </div>
            <div class="product-status">
              <el-tag 
                :type="selectedProductDetail.status === 'active' ? 'success' : 'danger'" 
                size="large"
              >
                {{ selectedProductDetail.status === 'active' ? '上架中' : '已下架' }}
              </el-tag>
            </div>
          </div>
        </div>

        <!-- 商品详细信息 -->
        <div class="product-details">
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="detail-card">
                <h3 class="card-title">
                  <el-icon><Box /></el-icon>
                  库存信息
                </h3>
                <div class="card-content">
                  <div class="info-item">
                    <span class="label">当前库存：</span>
                    <span class="value">{{ selectedProductDetail.stock }} 件</span>
                  </div>
                  <div class="info-item">
                    <span class="label">累计销量：</span>
                    <span class="value">{{ selectedProductDetail.sales }} 件</span>
                  </div>
                </div>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="detail-card">
                <h3 class="card-title">
                  <el-icon><Collection /></el-icon>
                  分类信息
                </h3>
                <div class="card-content">
                  <div class="info-item">
                    <span class="label">商品分类：</span>
                    <span class="value">{{ selectedProductDetail.category }}</span>
                  </div>
                  <div class="info-item">
                    <span class="label">创建时间：</span>
                    <span class="value">{{ selectedProductDetail.createTime }}</span>
                  </div>
                </div>
              </div>
            </el-col>
          </el-row>

          <!-- 质量信息 -->
          <div class="detail-card full-width">
            <h3 class="card-title">
              <el-icon><Calendar /></el-icon>
              质量信息
            </h3>
            <div class="card-content">
              <el-row :gutter="20">
                <el-col :span="8">
                  <div class="info-item">
                    <span class="label">生产日期：</span>
                    <span class="value">{{ formatDate(selectedProductDetail.creationdate) }}</span>
                  </div>
                </el-col>
                <el-col :span="8">
                  <div class="info-item">
                    <span class="label">保质期至：</span>
                    <span class="value">{{ formatDate(selectedProductDetail.expirationdate) }}</span>
                  </div>
                </el-col>
                <el-col :span="8">
                  <div class="info-item">
                    <span class="label">储存方式：</span>
                    <span class="value">{{ selectedProductDetail.storagemethod || '常温保存' }}</span>
                  </div>
                </el-col>
              </el-row>
            </div>
          </div>

          <!-- 商品描述 -->
          <div class="detail-card full-width">
            <h3 class="card-title">
              <el-icon><Document /></el-icon>
              商品描述
            </h3>
            <div class="card-content">
              <p class="product-description">
                {{ selectedProductDetail.description || '暂无描述' }}
              </p>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import axios from 'axios'
import { Plus, Box, Collection, Calendar, Document } from '@element-plus/icons-vue'

interface Product {
  id: number
  name: string
  price: number
  image?: string
  description?: string
  stock: number
  sales: number
  category: string
  status: 'active' | 'inactive'
  createTime: string
  creationdate: string
  expirationdate: string
  storagemethod: string
}

interface ProductForm {
  id?: number
  name: string
  price: number
  originalPrice?: number
  image?: string
  description?: string
  stock: number
  categoryName: string
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
const detailDialogVisible = ref(false)
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
  image: '',
  description: '',
  stock: 0,
  categoryName: '',
  status: 'active'
})

const products = ref<Product[]>([])
const availableCategories = ref<string[]>([])

// 查看商品详情相关
const selectedProductDetail = ref<Product | null>(null)

const showProductDetail = (product: Product) => {
  selectedProductDetail.value = product
  detailDialogVisible.value = true
}

const formatDate = (dateString: string): string => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toISOString().split('T')[0]
}

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
  categoryName: [
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
    const response = await axios.get('/api/api/goods')
    products.value = response.data.map((item: any) => ({
      id: item.goodsId,
      name: item.name,
      price: item.price,
      stock: item.num,
      sales: item.sales ?? 0,
      // 直接使用接口返回的 categoryName
      category: item.categoryName,
      description: item.desc,
      image: item.imgUrl,
      status: item.state === 1 ? 'active' : 'inactive',
      createTime: new Date(item.addtime).toLocaleString('zh-CN'),
      creationdate: item.creationdate,
      expirationdate: item.expirationdate,
      storagemethod: item.storagemethod
    }))
    
    // 从商品数据中提取所有可用的分类
    updateAvailableCategories()
  } catch (error) {
    ElMessage.error('加载商品数据失败')
  } finally {
    loading.value = false
  }
}

// 从商品数据中提取可用分类
const updateAvailableCategories = () => {
  const categorySet = new Set<string>()
  products.value.forEach(product => {
    if (product.category) {
      categorySet.add(product.category)
    }
  })
  availableCategories.value = Array.from(categorySet)
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
    image: '',
    description: '',
    stock: 0,
    categoryName: availableCategories.value[0] || '',
    status: 'active'
  }
  dialogVisible.value = true
}

const editProduct = (product: Product) => {
  isEdit.value = true
  productForm.value = {
    id: product.id,
    name: product.name,
    price: product.price,
    image: product.image,
    description: product.description,
    stock: product.stock,
    categoryName: product.category,
    status: product.status
  }
  dialogVisible.value = true
}

const handleDialogClose = () => {
  productFormRef.value?.resetFields()
}

const saveProduct = async () => {
  try {
    await productFormRef.value?.validate()
    saving.value = true

    const goodsData = {
      goods: {
        goodsId: productForm.value.id || 0,
        name: productForm.value.name,
        desc: productForm.value.description,
        categoryName: productForm.value.categoryName,
        price: productForm.value.price,
        num: productForm.value.stock,
        creationdate: new Date().toISOString(),
        expirationdate: new Date(Date.now() + 180 * 24 * 60 * 60 * 1000).toISOString(),
        storagemethod: '常温保存',
        addtime: new Date().toISOString(),
        state: productForm.value.status === 'active' ? 1 : 0,
        imgUrl: productForm.value.image
      },
      file: ''
    }

    if (isEdit.value) {
      await axios.put(`/api/api/goods/${productForm.value.id}`, goodsData)
      ElMessage.success('商品更新成功')
    } else {
      await axios.post('/api/api/goods', goodsData)
      ElMessage.success('商品添加成功')
    }

    dialogVisible.value = false
    loadProducts()
  } catch (error) {
    ElMessage.error('操作失败')
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

    await axios.put(`/api/api/goods/${product.id}`, {
      goods: {
        goodsId: product.id,
        name: product.name,
        desc: product.description,
        categoryName: product.category,
        price: product.price,
        num: product.stock,
        creationdate: product.creationdate,
        expirationdate: product.expirationdate,
        storagemethod: product.storagemethod || '常温保存',
        addtime: new Date().toISOString(),
        state: product.status === 'active' ? 0 : 1,
        imgUrl: product.image
      },
      file: ''
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

    await axios.delete(`/api/api/goods/${product.id}`)
    const index = products.value.findIndex(p => p.id === product.id)
    if (index !== -1) {
      products.value.splice(index, 1)
      ElMessage.success('商品删除成功')
    }
    // 更新分类列表
    updateAvailableCategories()
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

/* 商品详情弹窗样式 */
.product-detail-container {
  max-height: 70vh;
  overflow-y: auto;
}

.product-header {
  display: flex;
  gap: 30px;
  margin-bottom: 30px;
  padding: 20px;
  background: transparent;
  border-radius: 12px;
  color: white;
}

.product-image-section {
  flex-shrink: 0;
}

.product-main-image {
  width: 150px;
  height: 150px;
  border-radius: 12px;
  border: 3px solid rgba(255, 255, 255, 0.3);
}

.product-basic-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.product-title {
  margin: 0 0 15px 0;
  font-size: 28px;
  font-weight: bold;
}

.product-price {
  margin-bottom: 15px;
}

.price-symbol {
  font-size: 24px;
  opacity: 0.8;
}

.price-value {
  font-size: 36px;
  font-weight: bold;
  margin-left: 5px;
}

.product-status {
  align-self: flex-start;
}

.product-details {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.detail-card {
  background: white;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s ease;
}

.detail-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.detail-card.full-width {
  width: 100%;
}

.card-title {
  margin: 0;
  padding: 15px 20px;
  background: #f8f9fa;
  border-bottom: 1px solid #e4e7ed;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-content {
  padding: 20px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.info-item:last-child {
  margin-bottom: 0;
  border-bottom: none;
}

.info-item .label {
  color: #909399;
  font-weight: 500;
  min-width: 100px;
}

.info-item .value {
  color: #303133;
  font-weight: 600;
  text-align: right;
  flex: 1;
}

.product-description {
  line-height: 1.6;
  color: #606266;
  margin: 0;
  padding: 10px;
  background: #f9f9f9;
  border-radius: 6px;
  border-left: 4px solid #409eff;
}

/* 对话框自定义样式 */
:deep(.product-detail-dialog .el-dialog__body) {
  padding: 20px;
}

:deep(.product-detail-dialog .el-dialog__header) {
  background: white;
  padding: 20px;
  border-bottom: 1px solid #e4e7ed;
}

:deep(.product-detail-dialog .el-dialog__title) {
  color: #303133;
  font-size: 18px;
  font-weight: bold;
}

:deep(.product-detail-dialog .el-dialog__close) {
  color: white;
}

.image-preview {
  margin-top: 10px;
}
</style>