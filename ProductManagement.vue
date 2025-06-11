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
            <el-option label="电子产品" value="电子产品" />
            <el-option label="食物" value="食物" />
            <el-option label="书籍" value="书籍" />
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

    <!-- 商品详情弹窗 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="商品详情"
      width="600px"
    >
      <div class="product-detail-card" v-if="selectedProductDetail">
        <div style="text-align: center; margin-bottom: 20px;">
          <el-image
            :src="selectedProductDetail.image || '/placeholder-product.png'"
            fit="cover"
            style="width: 120px; height: 120px; border-radius: 8px;"
          />
        </div>

        <el-row :gutter="10" style="margin-bottom: 15px;">
          <el-col :span="8"><strong>商品名称：</strong>{{ selectedProductDetail.name }}</el-col>
          <el-col :span="8"><strong>单价：</strong>¥{{ selectedProductDetail.price }}</el-col>
        </el-row>

        <el-row :gutter="10" style="margin-bottom: 15px;">
          <el-col :span="8"><strong>库存：</strong>{{ selectedProductDetail.stock }}</el-col>
          <el-col :span="8"><strong>销量：</strong>{{ selectedProductDetail.sales }}</el-col>
          <el-col :span="8"><strong>分类：</strong>{{ selectedProductDetail.category }}</el-col>
        </el-row>

        <el-row :gutter="10" style="margin-bottom: 15px;">
          <el-col :span="8">
            <strong>状态：</strong>
            <el-tag :type="selectedProductDetail.status === 'active' ? 'success' : 'danger'">
              {{ selectedProductDetail.status === 'active' ? '上架' : '下架' }}
            </el-tag>
          </el-col>
          <el-col :span="8"><strong>创建时间：</strong>{{ selectedProductDetail.createTime }}</el-col>
          <el-col :span="8"><strong>生产日期：</strong>{{ formatDate(selectedProductDetail.creationdate) }}</el-col>
        </el-row>

        <el-row :gutter="10" style="margin-bottom: 15px;">
          <el-col :span="8"><strong>保质期到期日：</strong>{{ formatDate(selectedProductDetail.expirationdate) }}</el-col>
          <el-col :span="16"><strong>储存方式：</strong>{{ selectedProductDetail.storagemethod || '-' }}</el-col>
        </el-row>

        <el-row :gutter="10">
          <el-col :span="24"><strong>商品描述：</strong><br />{{ selectedProductDetail.description || '-' }}</el-col>
        </el-row>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import axios from 'axios'
import { Plus } from '@element-plus/icons-vue'

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
  category: '',
  status: 'active'
})

const products = ref<Product[]>([])

// 查看商品详情相关
const selectedProductDetail = ref<Product | null>(null)

const showProductDetail = (product: Product) => {
  selectedProductDetail.value = product
  detailDialogVisible.value = true
}

const formatDate = (dateString: string): string => {
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
    const response = await axios.get('/api/api/goods')
    products.value = response.data.map((item: any) => ({
      id: item.goodsId,
      name: item.name,
      price: item.price,
      stock: item.num,
      sales: item.sales ?? 0,
      category: ['电子产品', '食物', '书籍'][item.cateId] || '未知',
      description: item.desc,
      image: item.imgUrl,
      status: item.state === 1 ? 'active' : 'inactive',
      createTime: new Date(item.addtime).toLocaleString('zh-CN'),
      creationdate: item.creationdate,
      expirationdate: item.expirationdate,
      storagemethod: item.storagemethod
    }))
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
    image: '',
    description: '',
    stock: 0,
    category: '电子产品',
    status: 'active'
  }
  dialogVisible.value = true
}

const editProduct = (product: Product) => {
  isEdit.value = true
  productForm.value = {
    ...product,
    category: product.category,
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

    // 构造请求体
    const cateMap: Record<string, number> = {
      '电子产品': 0,
      '食物': 1,
      '书籍': 2
    }
    const goodsData = {
      goods: {
        goodsId: productForm.value.id || 0,
        name: productForm.value.name,
        desc: productForm.value.description,
        cateId: cateMap[productForm.value.category],
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
      // 编辑
      await axios.put(`/api/api/goods/${productForm.value.id}`, goodsData)
      ElMessage.success('商品更新成功')
    } else {
      // 添加
      const res = await axios.post('/api/api/goods', goodsData)
      goodsData.goods.goodsId = res.data.goodsId
      products.value.unshift({
        id: goodsData.goods.goodsId,
        name: goodsData.goods.name,
        price: goodsData.goods.price,
        stock: goodsData.goods.num,
        sales: 0,
        category: productForm.value.category,
        description: goodsData.goods.desc,
        image: goodsData.goods.imgUrl,
        status: goodsData.goods.state === 1 ? 'active' : 'inactive',
        createTime: new Date(goodsData.goods.addtime).toLocaleString('zh-CN'),
        creationdate: goodsData.goods.creationdate,
        expirationdate: goodsData.goods.expirationdate,
        storagemethod: goodsData.goods.storagemethod
      })
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

    // 更新状态
    await axios.put(`/api/api/goods/${product.id}`, {
      goods: {
        ...toBackendFormat(product),
        state: product.status === 'active' ? 0 : 1
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
  } catch {
    // 用户取消
  }
}

// 工具函数：将前端格式转换为后端格式
const toBackendFormat = (p: Product) => ({
  goodsId: p.id,
  name: p.name,
  desc: p.description,
  cateId: { '电子产品': 0, '食物': 1, '书籍': 2 }[p.category] || 0,
  price: p.price,
  num: p.stock,
  size: '',
  creationdate: p.creationdate,
  expirationdate: p.expirationdate,
  storagemethod: p.storagemethod || '常温保存',
  addtime: new Date().toISOString(),
  state: p.status === 'active' ? 1 : 0,
  imgUrl: p.image
})
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
.product-detail-card {
  font-size: 14px;
}
</style>