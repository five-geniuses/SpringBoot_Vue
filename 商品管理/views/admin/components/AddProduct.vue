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
            <div class="image-upload-section">
              <!-- 📝 添加路径测试按钮 -->
              <div class="upload-path-selector" style="margin-bottom: 10px;">
                <el-select v-model="currentUploadPath" placeholder="选择上传路径" style="width: 300px;">
                  <el-option label="/api/api/uploads/goods" value="/api/api/uploads/goods" />
                  <el-option label="/api/uploads/goods" value="/api/uploads/goods" />
                  <el-option label="/api/uploads" value="/api/uploads" />
                  <el-option label="/uploads" value="/uploads" />
                  <el-option label="/uploads/goods" value="/uploads/goods" />
                </el-select>
                <el-button @click="testUploadPath" type="info" size="small">测试路径</el-button>
              </div>
              
              <el-input 
                v-model="productForm.image" 
                style="display: none"
              />
              
              <el-upload
                ref="uploadRef"
                class="image-uploader"
                :action="currentUploadPath"
                :show-file-list="false"
                :on-success="handleImageSuccess"
                :before-upload="beforeImageUpload"
                :on-error="handleImageError"
                accept=".jpg,.jpeg,.png,.JPG,.JPEG,.PNG"
                :name="'file'"
              >
                <div v-if="productForm.image" class="image-preview-container">
                  <el-image
                    :src="getFullImageUrl(productForm.image)"
                    fit="cover"
                    class="uploaded-image"
                    :preview-src-list="[getFullImageUrl(productForm.image)]"
                    @error="handleImageLoadError"
                  />
                  <div class="image-overlay">
                    <el-button 
                      size="small" 
                      type="danger" 
                      circle
                      @click.stop="removeImage"
                    >
                      <el-icon><Delete /></el-icon>
                    </el-button>
                  </div>
                </div>
                <div v-else class="upload-placeholder">
                  <el-icon class="upload-icon"><Plus /></el-icon>
                  <div class="upload-text">
                    <p>点击上传图片</p>
                    <p class="upload-tip">支持jpg/png格式，不超过2MB</p>
                  </div>
                </div>
              </el-upload>
              
              <!-- 📝 显示当前图片信息 -->
              <div v-if="productForm.imageFile" class="image-info">
                <p><strong>文件名:</strong> {{ productForm.imageFile }}</p>
                <p><strong>访问路径:</strong> {{ getFullImageUrl(productForm.imageFile) }}</p>
              </div>
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
  Plus, Delete, CircleCheck, CircleClose, Close, Check, Goods 
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
      imageFile: ''
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

// 📝 上传路径管理
const currentUploadPath = ref('/api/api/goods/upload')

// 📝 测试上传路径
const testUploadPath = async () => {
  try {
    const response = await axios.post(currentUploadPath.value, new FormData(), {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    ElMessage.success(`路径 ${currentUploadPath.value} 可访问`)
  } catch (error: any) {
    if (error.response?.status === 405) {
      ElMessage.error(`路径 ${currentUploadPath.value} 方法不允许 (405)`)
    } else if (error.response?.status === 404) {
      ElMessage.error(`路径 ${currentUploadPath.value} 不存在 (404)`)
    } else {
      ElMessage.info(`路径 ${currentUploadPath.value} 响应: ${error.response?.status || '未知错误'}`)
    }
  }
}

// 📝 获取完整图片URL的函数
const getFullImageUrl = (fileName: string): string => {
  if (!fileName) return ''
  
  // 如果已经是完整URL，直接返回
  if (fileName.startsWith('http') || fileName.startsWith('/uploads/')) {
    return fileName
  }
  
  // 如果是blob URL（本地预览），直接返回
  if (fileName.startsWith('blob:')) {
    return fileName
  }
  
  // 否则拼接uploads路径
  return `/uploads/${fileName}`
}

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
        imageFile: ''
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

// 表单验证规则
const productRules = {
  name: [
    { required: true, message: '请输入商品名称', trigger: 'blur' },
    { min: 2, max: 50, message: '商品名称长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  price: [
    { required: true, message: '请输入商品价格', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '商品价格必须大于0', trigger: 'blur' }
  ],
  stock: [
    { required: true, message: '请输入库存数量', trigger: 'blur' },
    { type: 'number', min: 0, message: '库存数量不能小于0', trigger: 'blur' }
  ],
  categoryName: [
    { required: true, message: '请选择商品分类', trigger: 'change' }
  ],
  description: [
    { max: 500, message: '商品描述不能超过500个字符', trigger: 'blur' }
  ],
  creationdate: [
    { required: true, message: '请选择生产日期', trigger: 'change' }
  ],
  expirationdate: [
    { message: '请输入保质期', trigger: 'blur' }
  ],
  storagemethod: [
    { message: '请选择储存方式', trigger: 'change' }
  ],
  imageFile: [
    { message: '请上传商品图片', trigger: 'change' }
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

// 图片上传前的验证
const beforeImageUpload: UploadProps['beforeUpload'] = (rawFile) => {
  const isJPGOrPNG = rawFile.type === 'image/jpeg' || rawFile.type === 'image/png'
  const isLt2M = rawFile.size / 1024 / 1024 < 2

  if (!isJPGOrPNG) {
    ElMessage.error('图片格式只能是 JPG 或 PNG!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

// 📝 图片上传成功回调 - 重点修改
const handleImageSuccess: UploadProps['onSuccess'] = (response, uploadFile) => {
  console.log('上传响应详情:', response)
  console.log('上传文件信息:', uploadFile)
  
  try {
    let fileName = ''
    
    // 尝试不同的响应格式
    if (typeof response === 'string') {
      // 直接返回文件名字符串
      fileName = response
    } else if (response && response.data) {
      // 对象格式 { data: { url: "filename.jpg" } }
      fileName = response.data.url || response.data.fileName || response.data.name || response.data
    } else if (response && response.url) {
      // 对象格式 { url: "filename.jpg" }
      fileName = response.url
    } else if (response && response.fileName) {
      // 对象格式 { fileName: "filename.jpg" }
      fileName = response.fileName
    } else if (response && response.name) {
      // 对象格式 { name: "filename.jpg" }
      fileName = response.name
    } else {
      // 尝试使用原始文件名
      fileName = uploadFile.name || `temp_${Date.now()}.jpg`
    }
    
    // 清理文件名，确保只是文件名，不包含路径
    if (fileName.includes('/')) {
      fileName = fileName.split('/').pop() || fileName
    }
    
    console.log('解析出的文件名:', fileName)
    
    // 设置表单数据
    productForm.value.imageFile = fileName  // 存储到数据库的文件名
    productForm.value.image = fileName      // 用于显示的文件名
    
    // 验证字段
    productFormRef.value?.validateField('imageFile')
    
    ElMessage.success(`图片上传成功！文件名: ${fileName}`)
    
  } catch (error) {
    console.error('处理上传响应时出错:', error)
    ElMessage.error('上传成功但处理响应失败，请重试')
  }
}

// 图片上传失败回调
const handleImageError: UploadProps['onError'] = (error, uploadFile) => {
  console.error('图片上传失败:', error)
  console.error('失败的文件:', uploadFile)
  ElMessage.error(`图片上传失败: ${error.message || '未知错误'}`)
}

// 📝 图片加载错误处理
const handleImageLoadError = () => {
  console.warn('图片加载失败:', productForm.value.image)
  ElMessage.warning('图片加载失败，请检查图片是否存在')
}

// 移除图片
const removeImage = () => {
  productForm.value.image = ''
  productForm.value.imageFile = ''
  productFormRef.value?.validateField('imageFile')
}

// 对话框关闭处理
const handleDialogClose = () => {
  productFormRef.value?.resetFields()
  productForm.value.image = ''
  productForm.value.imageFile = ''
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

    // 📝 处理图片URL - 如果是完整路径，提取文件名
    let imgUrl = productForm.value.image || ''
    if (imgUrl.startsWith('/uploads/')) {
      imgUrl = imgUrl.replace('/uploads/', '')
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
      creationdate: formattedCreationDate, // 🔥 生产日期：选择的日期 + 00:00:00
      expirationdate: productForm.value.expirationdate || '',
      storagemethod: productForm.value.storagemethod || '常温保存',
      addtime: getCurrentDateTime(), // 🔥 添加时间：当前时间 YYYY-MM-DD HH:mm:ss
      state: productForm.value.status === 'active' ? 1 : 0,
      imgUrl: imgUrl // 📝 保存文件名，不包含/uploads/前缀
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
</style>