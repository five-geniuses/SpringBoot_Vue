<template>
  <div class="comments-container">
    <!-- 评论头部 -->
    <div class="comments-header">
      <h3>商品评价 ({{ total }}条)</h3>
      <div class="avg-rating">
        <span>平均评分: </span>
        <el-rate 
          v-model="avgRating" 
          disabled 
          show-score 
          text-color="#ff9900" 
          score-template="{value} 分"
        />
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="3" animated />
    </div>

    <!-- 🔥 评论列表 - 修改高度使其填充整个容器 -->
    <div v-else-if="comments.length > 0" class="comments-list">
      <div v-for="comment in comments" :key="comment.commentId" class="comment-item">
        <div class="user-info">
          <span class="username">{{ comment.userName }}</span>
          <el-rate 
            v-model="comment.rating" 
            disabled 
            class="rating" 
          />
          <span class="time">{{ formatTime(comment.createTime) }}</span>
        </div>
        <div class="content">{{ comment.content }}</div>
        <div class="actions">
          <el-button 
            type="danger" 
            size="small" 
            link
            @click="deleteComment(comment)"
          >
            删除
          </el-button>
        </div>
      </div>
    </div>

    <!-- 无评论状态 -->
    <div v-else class="no-comments">
      <el-empty description="暂无评论" />
    </div>

    <!-- 🔥 分页 - 移到最底部 -->
    <div v-if="total > 0" class="pagination-bottom">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        layout="prev, pager, next, total"
        :total="total"
        :page-sizes="[5, 10, 20]"
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

interface Comment {
  commentId: number
  userId: number
  goodsId: number
  rating: number
  content: string
  createTime: string
  userName: string
  goodsName: string
}

interface CommentsResponse {
  comments: {
    records: Comment[]
    total: number
    size: number
    current: number
    pages: number
  }
  goods: any
  avgRating: number
}

const props = defineProps({
  goodsId: {
    type: Number,
    required: true
  }
})

const emit = defineEmits(['comments-updated'])

const comments = ref<Comment[]>([])
const avgRating = ref(0)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const loading = ref(false)

// 格式化时间显示
const formatTime = (timeStr: string): string => {
  if (!timeStr) return ''
  try {
    const date = new Date(timeStr)
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch (error) {
    return timeStr
  }
}

// 🔥 修改后的加载评论接口
const loadComments = async () => {
  if (!props.goodsId) {
    console.warn('goodsId is required')
    return
  }

  loading.value = true
  try {
    console.log('=== 加载评论调试信息 ===')
    console.log('商品ID:', props.goodsId)
    console.log('当前页:', currentPage.value)
    console.log('页面大小:', pageSize.value)

    const response = await axios.get(`/api/api/comments/goods/${props.goodsId}`, {
      params: {
        page: currentPage.value,
        size: pageSize.value
      }
    })
    
    console.log('评论API响应:', response.data)
    
    const data: CommentsResponse = response.data
    
    if (data.comments && data.comments.records) {
      comments.value = data.comments.records
      total.value = data.comments.total || 0
      avgRating.value = data.avgRating || 0
      
      console.log('解析后的评论数据:')
      console.log('评论列表:', comments.value)
      console.log('总数:', total.value)
      console.log('平均评分:', avgRating.value)
    } else {
      console.warn('评论数据格式异常:', data)
      comments.value = []
      total.value = 0
      avgRating.value = 0
    }
    
    emit('comments-updated')
  } catch (error: any) {
    console.error('加载评论失败:', error)
    if (error.response) {
      console.error('错误响应:', error.response.data)
      console.error('状态码:', error.response.status)
    }
    ElMessage.error('加载评论失败')
    comments.value = []
    total.value = 0
    avgRating.value = 0
  } finally {
    loading.value = false
  }
}
// 🔥 修改后的删除评论接口 - 传递 commentId 和 userId
const deleteComment = async (comment: Comment) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户"${comment.userName}"的评论吗？`, 
      '提示', 
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    console.log('=== 删除评论调试信息 ===')
    console.log('要删除的评论ID:', comment.commentId)
    console.log('要删除的用户ID:', comment.userId) // 🔥 新增用户ID日志
    console.log('评论所属商品ID:', comment.goodsId)
    console.log('当前商品ID:', props.goodsId)

    // 确保评论属于当前商品
    if (comment.goodsId !== props.goodsId) {
      ElMessage.error('评论与当前商品不匹配，无法删除')
      return
    }

    // 🔥 修改接口路径，传递两个参数
    const response = await axios.delete(`/api/api/comments/${comment.commentId}`, {
      params: {
        userId: comment.userId
      }
    })
    console.log('删除评论响应:', response)
    
    ElMessage.success('评论删除成功')
    
    // 重新加载评论列表
    await loadComments()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除评论失败:', error)
      if (error.response) {
        console.error('删除错误响应:', error.response.data)
        ElMessage.error(`删除失败: ${error.response.data?.message || error.response.statusText}`)
      } else {
        ElMessage.error('删除评论失败')
      }
    }
  }
}

// 分页变化
const handlePageChange = (page: number) => {
  console.log('页面变化:', page)
  currentPage.value = page
  loadComments()
}

// 页面大小变化
const handleSizeChange = (size: number) => {
  console.log('页面大小变化:', size)
  pageSize.value = size
  currentPage.value = 1
  loadComments()
}

// 监听商品ID变化
watch(() => props.goodsId, (newVal, oldVal) => {
  console.log('goodsId变化:', oldVal, '->', newVal)
  if (newVal) {
    currentPage.value = 1 // 重置页码
    loadComments()
  }
}, { immediate: true })

// 初始加载
onMounted(() => {
  console.log('ProductComments组件挂载, goodsId:', props.goodsId)
  if (props.goodsId) {
    loadComments()
  }
})
</script>

<style scoped>
.comments-container {
  padding: 0;
  background-color: #fff;
  height: 100%;
  display: flex;
  flex-direction: column;
  position: relative; 
}

.comments-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e4e7ed;
  flex-shrink: 0;
}

.comments-header h3 {
  margin: 0;
  color: #303133;
  font-size: 16px;
  font-weight: 600;
}

.avg-rating {
  display: flex;
  align-items: center;
  gap: 8px;
}

.avg-rating span {
  color: #606266;
  font-size: 14px;
}

.loading-container {
  padding: 20px 0;
  flex: 1;
}

.comments-list {
  flex: 1;
  overflow-y: auto;
  min-height: 400px;
  margin-bottom: 60px; 
  padding-bottom: 20px;
}

.comment-item {
  padding: 15px 0;
  border-bottom: 1px solid #f5f5f5;
  transition: background-color 0.3s ease;
}

.comment-item:hover {
  background-color: #fafafa;
  padding-left: 10px;
  padding-right: 10px;
  border-radius: 4px;
}

.comment-item:last-child {
  border-bottom: none;
}

.user-info {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  gap: 12px;
}

.username {
  font-weight: 600;
  color: #409eff;
  font-size: 14px;
}

.rating {
  --el-rate-font-size: 14px;
}

.time {
  color: #909399;
  font-size: 12px;
  margin-left: auto;
}

.content {
  line-height: 1.6;
  margin-bottom: 10px;
  color: #606266;
  font-size: 14px;
  padding-left: 2px;
}

.actions {
  text-align: right;
}

.no-comments {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  margin-bottom: 30px; 
}

.pagination-bottom {
  position: absolute; 
  bottom: 10px; 
  right: 20px; 
  display: flex;
  justify-content: flex-end;
  flex-shrink: 0;
  z-index: 10; 
}

/* 滚动条美化 */
.comments-list::-webkit-scrollbar {
  width: 6px;
}

.comments-list::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.comments-list::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.comments-list::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

@media (max-width: 768px) {
  .comments-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .user-info {
    flex-wrap: wrap;
    gap: 8px;
  }
  
  .time {
    margin-left: 0;
    order: 3;
    flex-basis: 100%;
  }

  .pagination-bottom {
    position: relative;
    bottom: auto;
    right: auto;
    justify-content: center;
    margin-top: 10px;
  }
  
  .comments-list {
    margin-bottom: 20px;
  }
  
  .no-comments {
    margin-bottom: 20px;
  }
}
</style>