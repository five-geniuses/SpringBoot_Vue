<template>
  <!-- 添加/编辑商品对话框 -->
  <el-dialog
    v-model="dialogVisible"
    :title="dialogTitle"
    width="900px"
    :before-close="handleDialogClose"
    class="product-dialog"
  >
    <div class="dialog-content">
      <el-form
        ref="productFormRef"
        :model="productForm"
        :rules="productRules"
        label-width="120px"
        class="product-form"
      >
        <!-- 基本信息卡片 -->
        <el-card class="form-card" header="基本信息" shadow="never">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="商品名称" prop="name">
                <el-input 
                  v-model="productForm.name" 
                  placeholder="请输入商品名称"
                  prefix-icon="Goods"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="商品分类" prop="categoryName">
                <el-select 
                  v-model="productForm.categoryName" 
                  placeholder="请选择商品分类" 
                  style="width: 100%"
                  filterable
                  :loading="categoriesLoading"
                  @change="handleCategoryChange"
                >
                  <el-option 
                    v-for="category in availableCategories" 
                    :key="category.catId" 
                    :label="category.name" 
                    :value="category.name"
                  />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="商品价格" prop="price">
                <el-input-number
                  v-model="productForm.price"
                  :min="0"
                  :precision="2"
                  placeholder="请输入商品价格"
                  style="width: 100%"
                  controls-position="right"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="库存数量" prop="stock">
                <el-input-number
                  v-model="productForm.stock"
                  :min="0"
                  placeholder="请输入库存数量"
                  style="width: 100%"
                  controls-position="right"
                />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="商品重量" prop="kgs">
                <el-input 
                  v-model="productForm.kgs" 
                  placeholder="请输入商品重量"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="商品尺寸" prop="size">
                <el-input-number
                  v-model="productForm.size"
                  :min="0"
                  placeholder="请输入商品尺寸"
                  style="width: 100%"
                  controls-position="right"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </el-card>

        <!-- 日期信息卡片 -->
        <el-card class="form-card" header="日期信息" shadow="never">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="生产日期" prop="creationdate">
                <el-date-picker
                  v-model="productForm.creationdate"
                  type="date"
                  placeholder="请选择生产日期"
                  style="width: 100%"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="保质期" prop="expirationdate">
                <el-input 
                  v-model="productForm.expirationdate" 
                  placeholder="请输入保质期"
                />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="储存方式" prop="storagemethod">
            <el-select 
              v-model="productForm.storagemethod" 
              placeholder="请选择储存方式" 
              style="width: 100%"
            >
              <el-option label="常温保存" value="常温保存" />
              <el-option label="冷藏保存" value="冷藏保存" />
              <el-option label="冷冻保存" value="冷冻保存" />
              <el-option label="阴凉干燥处保存" value="阴凉干燥处保存" />
              <el-option label="密封保存" value="密封保存" />
            </el-select>
          </el-form-item>
        </el-card>

        <!-- 商品详情卡片 -->
        <el-card class="form-card" header="商品详情" shadow="never">
          <el-form-item label="商品描述" prop="description">
            <el-input
              v-model="productForm.description"
              type="textarea"
              :rows="4"
              placeholder="请输入商品详细描述"
              show-word-limit
              maxlength="500"
            />
          </el-form-item>
          
          <el-form-item label="商品图片" prop="imageFile">
            <input
              type="file"
              ref="fileInput"
              style="display: none"
              accept="image/jpeg,image/png"
              @change="handleFileChange"
            />
            <div class="image-preview" v-if="productForm.imagePreview || productForm.image">
              <el-image
                :src="productForm.imagePreview || getImageUrl(productForm.image)"
                fit="cover"
                class="preview-image"
                @error="handleImageLoadError"
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
              <el-button 
                type="danger" 
                circle 
                class="remove-image" 
                @click="removeImage"
              >
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
            <div v-else class="upload-placeholder" @click="triggerUpload">
              <el-icon><Plus /></el-icon>
              <span>点击上传商品图片</span>
            </div>
          </el-form-item>

          <el-form-item label="商品状态" prop="status">
            <el-radio-group v-model="productForm.status" class="status-radio">
              <el-radio value="active" class="status-option">
                <el-icon class="status-icon success"><CircleCheck /></el-icon>
                上架销售
              </el-radio>
              <el-radio value="inactive" class="status-option">
                <el-icon class="status-icon danger"><CircleClose /></el-icon>
                暂时下架
              </el-radio>
            </el-radio-group>
          </el-form-item>
        </el-card>
      </el-form>
    </div>
    
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleCancel" size="large">
          <el-icon><Close /></el-icon>
          取消
        </el-button>
        <el-button 
          type="primary" 
          @click="handleSave" 
          :loading="saving"
          size="large"
        >
          <el-icon v-if="!saving"><Check /></el-icon>
          {{ isEdit ? '更新商品' : '添加商品' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch, type PropType } from 'vue'
import { ElMessage, type FormInstance, type UploadProps, type UploadInstance } from 'element-plus'
import axios from 'axios'
import { 
  Plus, Delete, CircleCheck, CircleClose, Close, Check, Goods, Picture, Loading 
} from '@element-plus/icons-vue'

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
  imagePreview?: string
}

interface Category {
  catId: number
  name: string
}

// Props
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  productData: {
    type: Object as PropType<ProductForm>,
    default: () => ({
      name: '',
      price: 0,
      image: '',
      description: '',
      stock: 0,
      categoryName: '',
      cateId: undefined,
      status: 'active' as const,
      creationdate: '',
      expirationdate: '',
      storagemethod: '常温保存',
      size: 0,
      kgs: '',
      imageFile: '',
      imagePreview: ''
    })
  },
  availableCategories: {
    type: Array as PropType<Category[]>,
    default: () => []
  },
  isEdit: {
    type: Boolean,
    default: false
  },
  existingProducts: {
    type: Array as PropType<{ id: number }[]>,
    default: () => []
  }
})

// Emits
const emit = defineEmits<{
  'update:visible': [value: boolean]
  'success': [data: any]
  'cancel': []
}>()

// 响应式数据
const saving = ref(false)
const categoriesLoading = ref(false)
const productFormRef = ref<FormInstance>()
const uploadRef = ref<UploadInstance>()
const productForm = ref<ProductForm>({ ...props.productData })

// 监听props变化
watch(() => props.productData, (newData) => {
  productForm.value = { ...newData }
  if (newData.categoryName && !newData.cateId) {
    searchCategoryId(newData.categoryName)
  }
}, { deep: true })

watch(() => props.visible, (newVisible) => {
  if (newVisible) {
    if (!props.isEdit) {
      const currentDate = new Date().toISOString().split('T')[0]
      productForm.value = {
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
        imageFile: '',
        imagePreview: ''
      }
    } else {
      productForm.value = { ...props.productData }
      if (productForm.value.categoryName && !productForm.value.cateId) {
        searchCategoryId(productForm.value.categoryName)
      }
    }
  }
})

// 计算属性
const dialogVisible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
})

const dialogTitle = computed(() => props.isEdit ? '编辑商品' : '添加商品')

// 处理保存时的图片验证
const validateImage = (rule: any, value: any, callback: any) => {
  if (!productForm.value.imageFile && !productForm.value.image) {
    callback(new Error('请上传商品图片'))
  } else {
    callback()
  }
}

// 表单验证规则
const productRules = {
  name: [
    { required: true, message: '请输入商品名称', trigger: 'blur' },
    { min: 2, max: 50, message: '商品名称长度应在2-50个字符之间', trigger: 'blur' }
  ],
  categoryName: [
    { required: true, message: '请选择商品分类', trigger: 'change' }
  ],
  price: [
    { required: true, message: '请输入商品价格', trigger: 'blur' },
    { type: 'number', min: 0, message: '价格不能小于0', trigger: 'blur' }
  ],
  stock: [
    { required: true, message: '请输入库存数量', trigger: 'blur' },
    { type: 'number', min: 0, message: '库存不能小于0', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入商品描述', trigger: 'blur' },
    { min: 10, max: 500, message: '商品描述长度应在10-500个字符之间', trigger: 'blur' }
  ],
  imageFile: [
    { validator: validateImage, trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择商品状态', trigger: 'change' }
  ],
  creationdate: [
    { required: true, message: '请选择生产日期', trigger: 'change' }
  ],
  expirationdate: [
    { required: true, message: '请输入保质期', trigger: 'blur' }
  ],
  storagemethod: [
    { required: true, message: '请选择储存方式', trigger: 'change' }
  ],
  kgs: [
    { required: true, message: '请输入商品重量', trigger: 'blur' }
  ],
  size: [
    { required: true, message: '请输入商品尺寸', trigger: 'blur' },
    { type: 'number', min: 0, message: '尺寸不能小于0', trigger: 'blur' }
  ]
}

// 🔥 通过分类名称搜索分类ID的函数
const searchCategoryId = async (categoryName: string) => {
  if (!categoryName) {
    productForm.value.cateId = undefined
    return
  }

  try {
    const response = await axios.get('/api/api/categories/search', {
      params: {
        name: categoryName
      }
    })
    
    if (response.data && response.data.cateId) {
      productForm.value.cateId = response.data.cateId
    } else {
      productForm.value.cateId = undefined
    }
  } catch (error: any) {
    console.error('查询分类ID失败:', error)
    productForm.value.cateId = undefined
  }
}

// 🔥 分类选择处理函数
const handleCategoryChange = async (categoryName: string) => {
  if (!categoryName) {
    productForm.value.cateId = undefined
    return
  }
  
  await searchCategoryId(categoryName)
}

// 🔥 获取最小缺失的商品ID
const generateNewGoodsId = (): number => {
  if (!props.existingProducts || props.existingProducts.length === 0) {
    return 1
  }

  const existingIds = props.existingProducts
    .map(product => product.id)
    .filter(id => id && !isNaN(id))
    .sort((a, b) => a - b)

  let expectedId = 1
  for (const id of existingIds) {
    if (id === expectedId) {
      expectedId++
    } else if (id > expectedId) {
      break
    }
  }

  return expectedId
}

// 🔥 格式化生产日期：日期 + 00:00:00
const formatCreationDateTime = (dateString: string): string => {
  if (!dateString) return ''
  
  // 如果只有日期（YYYY-MM-DD），添加 00:00:00
  if (dateString.match(/^\d{4}-\d{2}-\d{2}$/)) {
    return `${dateString} 00:00:00`
  }
  
  // 如果已经包含时间，直接返回
  return dateString
}

// 🔥 获取当前时间：YYYY-MM-DD HH:mm:ss 格式
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

// 获取图片URL
const getImageUrl = (imgUrl?: string) => {
  if (!imgUrl) return ''  // 如果没有图片，返回空字符串而不是默认图片
  
  // 如果是完整URL直接返回
  if (imgUrl.startsWith('http')) return imgUrl
  
  // 如果是base64数据，直接返回
  if (imgUrl.startsWith('data:image')) return imgUrl
  
  // 确保路径以/开头
  const normalizedPath = imgUrl.startsWith('/') ? imgUrl : `/${imgUrl}`
  
  // 返回完整URL
  return `http://localhost:8080${normalizedPath}`
}

// 图片加载错误处理
const handleImageLoadError = () => {
  ElMessage.warning('图片加载失败，请检查图片路径是否正确')
}

// 移除图片
const removeImage = () => {
  productForm.value.imageFile = ''
  productForm.value.image = ''
  productForm.value.imagePreview = ''
}

// 对话框关闭处理
const handleDialogClose = () => {
  productFormRef.value?.resetFields()
  productForm.value.image = ''
  productForm.value.imageFile = ''
  productForm.value.imagePreview = ''
}

// 取消按钮处理
const handleCancel = () => {
  emit('cancel')
  emit('update:visible', false)
  handleDialogClose()
}

// 🔥 保存商品 - 修正日期时间格式
const handleSave = async () => {
  try {
    await productFormRef.value?.validate()
    saving.value = true

    const goodsId = props.isEdit ? productForm.value.id : generateNewGoodsId()

    // 🔥 格式化生产日期：选择的日期 + 00:00:00
    const formattedCreationDate = formatCreationDateTime(productForm.value.creationdate)

    // 基本验证
    if (!productForm.value.name || !productForm.value.name.trim()) {
      throw new Error('商品名称不能为空')
    }
    
    if (!productForm.value.price || productForm.value.price <= 0) {
      throw new Error('商品价格必须大于0')
    }
    
    if (productForm.value.stock === null || productForm.value.stock === undefined || productForm.value.stock < 0) {
      throw new Error('库存数量不能小于0')
    }

    // 🔥 分类ID验证和获取
    if (!productForm.value.cateId && productForm.value.categoryName) {
      await searchCategoryId(productForm.value.categoryName)
    }

    if (!productForm.value.cateId) {
      throw new Error('无法获取分类ID，请重新选择商品分类')
    }

    // 处理图片URL
    let imgUrl = productForm.value.imageFile || productForm.value.image || ''
    if (imgUrl.startsWith('/uploads/')) {
      imgUrl = imgUrl.substring('/uploads/'.length)
    }

    const goodsData = {
      goodsId: goodsId,
      name: productForm.value.name.trim(),
      desc: productForm.value.description || '',
      categoryName: productForm.value.categoryName || '',
      cateId: productForm.value.cateId,
      price: Number(productForm.value.price),
      num: Number(productForm.value.stock),
      size: Number(productForm.value.size) || 0,
      kgs: productForm.value.kgs || '',
      creationdate: formattedCreationDate,
      expirationdate: productForm.value.expirationdate || '',
      storagemethod: productForm.value.storagemethod || '常温保存',
      addtime: getCurrentDateTime(),
      state: productForm.value.status === 'active' ? 1 : 0,
      imgUrl: imgUrl
    }

    console.log('发送的商品数据:', JSON.stringify(goodsData, null, 2))

    // 发送请求
    let response
    if (props.isEdit) {
      response = await axios.put(`/api/api/goods/${productForm.value.id}`, goodsData)
      ElMessage.success('商品更新成功')
    } else {
      response = await axios.post('/api/api/goods', goodsData)
      ElMessage.success('商品添加成功')
    }

    emit('success', goodsData)
    emit('update:visible', false)
    handleDialogClose()
  } catch (error: any) {
    console.error('保存商品失败:', error)
    
    if (error.response) {
      let errorMessage = '操作失败'
      if (error.response.data?.message) {
        errorMessage = error.response.data.message
      } else if (error.response.data?.error) {
        errorMessage = error.response.data.error
      } else if (error.response.statusText) {
        errorMessage = error.response.statusText
      }
      
      ElMessage.error(`${errorMessage} (状态码: ${error.response.status})`)
    } else if (error.request) {
      ElMessage.error('网络请求失败，请检查网络连接和后端服务状态')
    } else if (error.message) {
      ElMessage.error(error.message)
    } else {
      ElMessage.error('操作失败，请重试')
    }
  } finally {
    saving.value = false
  }
}

// 文件上传相关
const fileInput = ref<HTMLInputElement | null>(null)

const triggerUpload = () => {
  fileInput.value?.click()
}

// 验证文件
const validateFile = (file: File): boolean => {
  // 验证文件类型
  const allowedTypes = ['image/jpeg', 'image/png', 'image/gif']
  if (!allowedTypes.includes(file.type)) {
    ElMessage.error('只允许上传 JPG、PNG 或 GIF 格式的图片')
    return false
  }

  // 验证文件大小（2MB）
  const maxSize = 2 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error('图片大小不能超过2MB')
    return false
  }

  return true
}

const handleFileChange = async (event: Event) => {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  
  if (!file) return
  
  // 验证文件
  if (!validateFile(file)) {
    input.value = ''
    return
  }
  
  try {
    // 创建预览
    const reader = new FileReader()
    reader.onload = (e) => {
      // 设置临时预览
      productForm.value.imagePreview = e.target?.result as string
    }
    reader.readAsDataURL(file)
    
    const formData = new FormData()
    formData.append('file', file)
    
    const response = await axios.post('http://localhost:8080/goods/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    
    console.log('上传响应:', response.data)
    
    if (response.data && response.data.code === 200) {
      const fileName = response.data.data
      if (!fileName || typeof fileName !== 'string') {
        throw new Error('服务器返回的文件名无效')
      }
      
      // 更新表单数据 - 只存储文件名
      productForm.value.imageFile = fileName
      productForm.value.image = fileName  // 确保使用简短的文件名
      
      // 验证表单字段
      productFormRef.value?.validateField('imageFile')
      
      ElMessage.success('图片上传成功')
    } else {
      throw new Error(response.data?.message || '上传失败')
    }
  } catch (error: any) {
    console.error('上传失败:', error)
    let errorMessage = '上传失败'
    
    if (error.response) {
      console.error('错误响应:', error.response)
      if (error.response.status === 404) {
        errorMessage = '上传接口不存在，请确保后端服务正常'
      } else if (error.response.status === 405) {
        errorMessage = '上传方法不被允许，请检查接口配置'
      } else if (error.response.status === 413) {
        errorMessage = '文件太大，请选择小于2MB的图片'
      } else if (error.response.data?.message) {
        errorMessage = error.response.data.message
      }
    } else if (error.message) {
      errorMessage = error.message
    }
    
    ElMessage.error(errorMessage)
  } finally {
    // 清理文件输入
    if (input) {
      input.value = ''
    }
  }
}
</script>

<style scoped>
/* 保持所有原有样式不变 */
.product-dialog {
  border-radius: 8px;
}

:deep(.product-dialog .el-dialog) {
  border-radius: 8px;
  overflow: hidden;
}

:deep(.product-dialog .el-dialog__header) {
  background: #ffffff;
  padding: 20px 30px;
  margin: 0;
  border-radius: 0;
  border-bottom: 1px solid #e4e7ed;
}

:deep(.product-dialog .el-dialog__title) {
  color: #303133;
  font-size: 20px;
  font-weight: 600;
}

:deep(.product-dialog .el-dialog__close) {
  color: #909399;
  font-size: 18px;
}

:deep(.product-dialog .el-dialog__close:hover) {
  color: #409eff;
}

:deep(.product-dialog .el-dialog__body) {
  padding: 0;
  background: #f8f9fa;
}

.dialog-content {
  padding: 30px;
  max-height: 70vh;
  overflow-y: auto;
}

.product-form {
  max-width: none;
}

.form-card {
  margin-bottom: 20px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #e4e7ed;
}

:deep(.form-card .el-card__header) {
  background: #f8f9fa;
  border-bottom: 1px solid #e4e7ed;
  font-weight: 600;
  color: #303133;
  padding: 15px 20px;
}

:deep(.form-card .el-card__body) {
  padding: 25px;
  background: white;
}

.image-upload-section {
  width: 100%;
}

.image-uploader {
  border: 2px dashed #d9d9d9;
  border-radius: 8px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
}

.image-uploader:hover {
  border-color: #409eff;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 200px;
  height: 200px;
  background: #fafafa;
}

.upload-icon {
  font-size: 48px;
  color: #c0c4cc;
  margin-bottom: 16px;
}

.upload-text {
  text-align: center;
}

.upload-text p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.upload-tip {
  color: #909399;
  font-size: 12px;
  margin-top: 4px;
}

.image-preview-container {
  position: relative;
  width: 200px;
  height: 200px;
}

.uploaded-image {
  width: 100%;
  height: 100%;
  display: block;
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.image-preview-container:hover .image-overlay {
  opacity: 1;
}

:deep(.image-uploader .el-upload) {
  border: none;
  border-radius: 8px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.status-radio {
  display: flex;
  gap: 20px;
}

.status-option {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  border: 2px solid #e4e7ed;
  border-radius: 8px;
  transition: all 0.3s ease;
  cursor: pointer;
  background: white;
}

.status-option:hover {
  border-color: #409eff;
  background: #f0f8ff;
}

:deep(.status-radio .el-radio.is-checked .status-option) {
  border-color: #409eff;
  background: #f0f8ff;
}

.status-icon {
  font-size: 16px;
}

.status-icon.success {
  color: #67c23a;
}

.status-icon.danger {
  color: #f56c6c;
}

:deep(.status-radio .el-radio__input) {
  display: none;
}

:deep(.status-radio .el-radio__label) {
  padding-left: 0;
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
}

.dialog-footer {
  padding: 20px 30px;
  background: white;
  border-top: 1px solid #e4e7ed;
  display: flex;
  justify-content: flex-end;
  gap: 15px;
}

.dialog-footer .el-button {
  padding: 12px 24px;
  font-weight: 500;
  border-radius: 6px;
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 表单项美化 */
:deep(.product-form .el-form-item__label) {
  font-weight: 600;
  color: #303133;
}

:deep(.product-form .el-input__wrapper) {
  border-radius: 6px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

:deep(.product-form .el-input__wrapper:hover) {
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
}

:deep(.product-form .el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

:deep(.product-form .el-textarea__inner) {
  border-radius: 6px;
  border: 1px solid #dcdfe6;
  transition: all 0.3s ease;
}

:deep(.product-form .el-textarea__inner:hover) {
  border-color: #c0c4cc;
}

:deep(.product-form .el-textarea__inner:focus) {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

:deep(.product-form .el-select .el-input__wrapper) {
  transition: all 0.3s ease;
}

:deep(.product-form .el-input-number) {
  width: 100%;
}

:deep(.product-form .el-input-number .el-input__wrapper) {
  border-radius: 6px;
}

:deep(.product-form .el-date-editor) {
  width: 100%;
}

:deep(.product-form .el-date-editor .el-input__wrapper) {
 border-radius: 6px;
}

/* 表单验证错误提示美化 */
:deep(.el-form-item__error) {
 color: #f56c6c;
 font-size: 12px;
 padding-top: 4px;
}

/* 滚动条美化 */
.dialog-content::-webkit-scrollbar {
 width: 6px;
}

.dialog-content::-webkit-scrollbar-track {
 background: #f1f1f1;
 border-radius: 3px;
}

.dialog-content::-webkit-scrollbar-thumb {
 background: #c1c1c1;
 border-radius: 3px;
}

.dialog-content::-webkit-scrollbar-thumb:hover {
 background: #a8a8a8;
}

/* 响应式设计 */
@media (max-width: 768px) {
 .dialog-content {
   padding: 20px;
 }
 
 .status-radio {
   flex-direction: column;
   gap: 10px;
 }
 
 .upload-placeholder {
   width: 150px;
   height: 150px;
 }
 
 .image-preview-container {
   width: 150px;
   height: 150px;
 }
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

.preview-image {
  width: 100%;
  height: 100%;
  border-radius: 8px;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 200px;
  background: #f5f7fa;
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.upload-placeholder:hover {
  border-color: #409eff;
  color: #409eff;
}

.upload-placeholder .el-icon {
  font-size: 32px;
  margin-bottom: 8px;
}

.upload-placeholder span {
  font-size: 14px;
  color: #909399;
}

.image-preview {
  position: relative;
  width: 100%;
  height: 200px;
  border-radius: 8px;
  overflow: hidden;
}

.remove-image {
  position: absolute;
  top: 8px;
  right: 8px;
  opacity: 0.8;
}

.remove-image:hover {
  opacity: 1;
}
</style>