<template>
  <!-- 商品详情弹窗 -->
  <el-dialog
    v-model="dialogVisible"
    title="商品详情"
    width="800px"
    class="product-detail-dialog"
  >
    <div class="product-detail-container" v-if="productDetail">
      <!-- 头部按钮区域 -->
      <div class="detail-header">
        <el-button 
          type="primary" 
          :class="{ active: currentView === 'detail' }"
          @click="switchToDetail"
        >
          商品详情
        </el-button>
        <el-button 
          type="info" 
          :class="{ active: currentView === 'comments' }"
          @click="switchToComments"
        >
          查看商品评论
        </el-button>
      </div>

      <!-- 商品详情视图 -->
      <div v-show="currentView === 'detail'" class="detail-content">
        <el-form 
          :model="productDetail" 
          label-width="100px"
          :rules="rules"
          ref="productFormRef">
          
          <!-- 商品头部信息 -->
          <div class="product-header">
            <div class="product-image-section">
              <el-image
                :src="getImageUrl(productDetail.imgUrl)"
                :alt="productDetail.name"
                fit="cover"
                class="product-main-image"
              >
                <template #error>
                  <div class="image-slot">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
            </div>
            
            <div class="product-upload-section">
              <el-upload
                class="avatar-uploader"
                action="/api/api/upload"
                :show-file-list="false"
                :on-success="handleUploadSuccess"
                :before-upload="beforeUpload"
                :limit="1">
                <el-button type="primary">
                  <el-icon><Upload /></el-icon>
                  {{ productDetail.imgUrl ? '更换图片' : '上传图片' }}
                </el-button>
                <template #tip>
                  <div class="el-upload__tip">
                    只能上传 jpg/png 文件，且不超过 2MB
                  </div>
                </template>
              </el-upload>
            </div>
          </div>

          <!-- 基本信息部分 -->
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="商品名称" prop="name">
                <el-input v-model="productDetail.name" placeholder="请输入商品名称"></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="商品分类" prop="category">
                <el-input v-model="productDetail.category" placeholder="请输入商品分类"></el-input>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="价格" prop="price">
                <el-input-number 
                  v-model="productDetail.price" 
                  :precision="2" 
                  :step="0.1"
                  :min="0"
                  style="width: 100%">
                </el-input-number>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="库存" prop="stock">
                <el-input-number 
                  v-model="productDetail.stock" 
                  :min="0"
                  style="width: 100%">
                </el-input-number>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="重量(kg)" prop="kgs">
                <el-input-number 
                  v-model="productDetail.kgs" 
                  :precision="2"
                  :step="0.1"
                  :min="0"
                  style="width: 100%">
                </el-input-number>
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="商品描述" prop="description">
            <el-input 
              v-model="productDetail.description" 
              type="textarea" 
              :rows="3"
              placeholder="请输入商品描述">
            </el-input>
          </el-form-item>

          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="尺寸" prop="size">
                <el-input v-model="productDetail.size" placeholder="请输入商品尺寸"></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="存储方式" prop="storagemethod">
                <el-input v-model="productDetail.storagemethod" placeholder="请输入存储方式"></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="销量" prop="sales">
                <el-input-number 
                  v-model="productDetail.sales" 
                  :min="0"
                  :disabled="true"
                  style="width: 100%">
                </el-input-number>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="生产日期" prop="creationdate">
                <el-date-picker
                  v-model="productDetail.creationdate"
                  type="datetime"
                  placeholder="选择生产日期"
                  style="width: 100%">
                </el-date-picker>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="保质期" prop="expirationdate">
                <el-input
                  v-model="productDetail.expirationdate"
                  placeholder="请输入保质期"
                  style="width: 100%">
                </el-input>
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="状态">
            <el-switch
              v-model="productDetail.status"
              :active-value="'active'"
              :inactive-value="'inactive'"
              active-text="上架"
              inactive-text="下架">
            </el-switch>
          </el-form-item>
        </el-form>

        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitting">
            {{ submitting ? '提交中...' : '确定' }}
          </el-button>
        </div>
      </div>

      <!-- 🔥 评论视图 - 修改为与详情视图相同的高度 -->
      <div v-show="currentView === 'comments'" class="comments-content">
        <ProductComments 
          v-if="productDetail && productDetail.id && currentView === 'comments'"
          :goodsId="productDetail.id"
          @comments-updated="handleCommentsUpdated"
        />
      </div>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { UploadProps, FormInstance } from 'element-plus'
import { Picture, Upload } from '@element-plus/icons-vue'
import ProductComments from './ProductComments.vue'

// Props 定义
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  productDetail: {
    type: Object,
    default: () => ({})
  }
})

// Emits
const emit = defineEmits(['update:visible', 'update:productDetail'])

// 响应式变量
const dialogVisible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
})

const productFormRef = ref<FormInstance>()
const submitting = ref(false)
const currentView = ref('detail') // 'detail' | 'comments'

// 表单验证规则
const rules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入商品价格', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入商品库存', trigger: 'blur' }],
  description: [{ required: true, message: '请输入商品描述', trigger: 'blur' }]
}

// 监听对话框关闭，重置视图
watch(dialogVisible, (newVal) => {
  if (!newVal) {
    currentView.value = 'detail'
  }
})

// 切换到详情视图
const switchToDetail = () => {
  console.log('切换到商品详情视图')
  currentView.value = 'detail'
}

// 切换到评论视图
const switchToComments = () => {
  console.log('切换到评论视图，商品ID:', props.productDetail?.id)
  console.log('商品详情对象:', props.productDetail)
  currentView.value = 'comments'
}

// 处理评论更新事件
const handleCommentsUpdated = () => {
  console.log('评论已更新')
}

// 获取图片URL
const getImageUrl = (imgUrl: string | null): string => {
  if (!imgUrl) return '/default-product.png'
  if (imgUrl.startsWith('http')) return imgUrl
  if (imgUrl.startsWith('/')) return imgUrl
  return `/api/uploads/${imgUrl}`
}

// 图片上传前的验证
const beforeUpload: UploadProps['beforeUpload'] = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件！')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB！')
    return false
  }
  return true
}

// 图片上传成功的处理
const handleUploadSuccess: UploadProps['onSuccess'] = (response) => {
  if (response.code === 200) {
    emit('update:productDetail', {
      ...props.productDetail,
      imgUrl: response.data.url
    })
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error('图片上传失败')
  }
}

// 提交表单
const submitForm = async () => {
  if (!productFormRef.value) return

  try {
    await productFormRef.value.validate()
    submitting.value = true

    const formData = new FormData()
    const submitData = {
      ...props.productDetail,
      imgUrl: props.productDetail.imgUrl || ''
    }
    formData.append('product', JSON.stringify(submitData))

    const url = props.productDetail.id ? 
      `/api/products/${props.productDetail.id}` : 
      '/api/products'
    
    const method = props.productDetail.id ? 'PUT' : 'POST'

    const response = await fetch(url, {
      method,
      body: formData
    })

    if (response.ok) {
      ElMessage.success(props.productDetail.id ? '更新成功' : '添加成功')
      dialogVisible.value = false
      emit('update:productDetail', null)
    } else {
      const errorData = await response.json()
      throw new Error(errorData.message || '操作失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '提交失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.product-detail-dialog {
  .el-dialog__body {
    padding: 20px;
  }
}

.product-detail-container {
  max-height: 70vh;
  overflow-y: auto;
  padding: 20px;
}

.detail-header {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e4e7ed;
}

.detail-header .el-button {
  border-radius: 20px;
  padding: 8px 20px;
  transition: all 0.3s ease;
}

.detail-header .el-button.active {
  background-color: #409eff;
  color: white;
  border-color: #409eff;
}

.detail-content {
  min-height: 600px;
}

/* 🔥 修改评论视图的样式，使其与详情视图一样大 */
.comments-content {
  min-height: 600px;
  max-height: none;
  overflow: visible;
}

.dialog-footer {
  text-align: right;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e4e7ed;
}

.product-header {
  display: flex;
  gap: 20px;
  margin-bottom: 30px;
  align-items: center;
}

.product-image-section {
  width: 200px;
  height: 200px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #dcdfe6;
  background: #f5f7fa;
}

.product-main-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
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
  font-size: 30px;
  margin-bottom: 10px;
}

.product-upload-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.avatar-uploader {
  text-align: center;
}

.el-upload__tip {
  margin-top: 8px;
  color: #909399;
  font-size: 12px;
}

/* 滚动条美化 */
.product-detail-container::-webkit-scrollbar {
  width: 6px;
}

.product-detail-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.product-detail-container::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.product-detail-container::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>