<template>
  <!-- 商品详情弹窗 -->
  <el-dialog
    v-model="dialogVisible"
    title="商品详情"
    width="800px"
    class="product-detail-dialog"
  >
    <div class="product-detail-container" v-if="productDetail">
      <!-- 商品头部信息 -->
      <div class="product-header">
        <div class="product-image-section">
          <el-image
            :src="productDetail.image || '/placeholder-product.png'"
            fit="cover"
            class="product-main-image"
          />
        </div>
        <div class="product-basic-info">
          <h2 class="product-title">{{ productDetail.name }}</h2>
          <div class="product-price">
            <span class="price-symbol">¥</span>
            <span class="price-value">{{ productDetail.price }}</span>
          </div>
          <div class="product-status">
            <el-tag 
             :type="productDetail.status === 'active' ? 'success' : 'danger'" 
             size="large"
           >
             {{ productDetail.status === 'active' ? '上架中' : '已下架' }}
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
                 <span class="value">{{ productDetail.stock }} 件</span>
               </div>
               <div class="info-item">
                 <span class="label">累计销量：</span>
                 <span class="value">{{ productDetail.sales }} 件</span>
               </div>
               <div class="info-item">
                 <span class="label">商品尺寸：</span>
                 <span class="value">{{ productDetail.size || '-' }}</span>
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
                 <span class="value">{{ productDetail.category }}</span>
               </div>
               <div class="info-item">
                 <span class="label">创建时间：</span>
                 <span class="value">{{ productDetail.createTime }}</span>
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
                 <span class="value">{{ formatDateOnly(productDetail.creationdate) }}</span>
               </div>
             </el-col>
             <el-col :span="8">
               <div class="info-item">
                 <span class="label">商品保质期：</span>
                 <span class="value">{{ productDetail.expirationdate || '-' }}</span>
               </div>
             </el-col>
             <el-col :span="8">
               <div class="info-item">
                 <span class="label">储存方式：</span>
                 <span class="value">{{ productDetail.storagemethod || '常温保存' }}</span>
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
             {{ productDetail.description || '暂无描述' }}
           </p>
         </div>
       </div>
     </div>
   </div>
 </el-dialog>
</template>

<script setup lang="ts">
import { computed, type PropType } from 'vue'
import { Box, Collection, Calendar, Document } from '@element-plus/icons-vue'

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
 size: number | string
 kgs: string
}

// Props
const props = defineProps({
 visible: {
   type: Boolean,
   default: false
 },
 productDetail: {
   type: Object as PropType<Product | null>,
   default: null
 }
})

// Emits
const emit = defineEmits(['update:visible'])

// 计算属性
const dialogVisible = computed({
 get: () => props.visible,
 set: (value) => emit('update:visible', value)
})

// 格式化日期
const formatDateOnly = (dateString: string): string => {
 if (!dateString) return '-'
 try {
   if (dateString.includes(' ')) {
     return dateString.split(' ')[0]
   }
   return dateString
 } catch (error) {
   console.error('日期格式化错误:', error)
   return dateString
 }
}
</script>

<style scoped>
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
 background: white;
 border-radius: 12px;
 box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.product-image-section {
 flex-shrink: 0;
}

.product-main-image {
 width: 150px;
 height: 150px;
 border-radius: 12px;
 border: 2px solid #e4e7ed;
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
 color: #303133;
}

.product-price {
 margin-bottom: 15px;
}

.price-symbol {
 font-size: 24px;
 color: #909399;
}

.price-value {
 font-size: 36px;
 font-weight: bold;
 margin-left: 5px;
 color: #e74c3c;
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
 color: #909399;
}

/* 响应式设计 */
@media (max-width: 768px) {
 .product-header {
   flex-direction: column;
   gap: 20px;
 }
 
 .product-main-image {
   width: 120px;
   height: 120px;
 }
 
 .product-title {
   font-size: 24px;
 }
 
 .price-value {
   font-size: 28px;
 }
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