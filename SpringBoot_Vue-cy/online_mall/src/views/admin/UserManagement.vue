<template>
  <div class="user-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="title-section">
          <h2>
            <el-icon class="title-icon"><UserFilled /></el-icon>
            用户管理
          </h2>
          <p class="subtitle">管理系统用户信息，支持查看、编辑、删除等操作</p>
        </div>
        <div class="header-actions">
          <el-button type="primary" @click="loadUsers" :loading="loading">
            <el-icon><Refresh /></el-icon>
            刷新数据
          </el-button>
        </div>
      </div>
    </div>

    <!-- 统计信息卡片 -->
    <div class="stats-section">
      <div class="stats-cards">
        <div class="stat-card total-users">
          <div class="stat-icon">
            <el-icon><User /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ totalUsers }}</div>
            <div class="stat-label">总用户数</div>
          </div>
        </div>
        
        <div class="stat-card admin-users">
          <div class="stat-icon">
            <el-icon><Star /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ adminCount }}</div>
            <div class="stat-label">管理员</div>
          </div>
        </div>
        
        <div class="stat-card normal-users">
          <div class="stat-icon">
            <el-icon><Avatar /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ normalUserCount }}</div>
            <div class="stat-label">普通用户</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <div class="filter-section">
      <div class="filter-header">
        <h3>
          <el-icon><Search /></el-icon>
          筛选条件
        </h3>
        <el-button text @click="toggleAdvancedSearch">
          <el-icon><Setting /></el-icon>
          {{ showAdvancedSearch ? '简单搜索' : '高级搜索' }}
        </el-button>
      </div>
      
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户名">
          <el-input
            v-model="searchForm.username"
            placeholder="请输入用户名"
            clearable
            @input="handleLocalSearch"
            @clear="resetSearch"
            class="search-input"
          >
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        
        <!-- 高级搜索选项 -->
        <template v-if="showAdvancedSearch">
          <el-form-item label="用户角色">
            <el-select v-model="searchForm.role" placeholder="选择角色" clearable class="search-select" @change="handleLocalSearch">
              <el-option label="全部" value="" />
              <el-option label="管理员" :value="0" />
              <el-option label="普通用户" :value="1" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="性别">
            <el-select v-model="searchForm.gender" placeholder="选择性别" clearable class="search-select" @change="handleLocalSearch">
              <el-option label="全部" value="" />
              <el-option label="男" value="男" />
              <el-option label="女" value="女" />
            </el-select>
          </el-form-item>
        </template>
        
        <el-form-item>
          <el-button type="primary" @click="handleLocalSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="resetSearch">
            <el-icon><RefreshLeft /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 用户表格 -->
    <div class="table-section">
      <div class="table-header">
        <h3>
          <el-icon><List /></el-icon>
          用户列表
        </h3>
        <div class="table-tools">
          <el-text type="info" size="small">
            共 {{ displayUsers.length }} 条记录，当前显示第 {{ (currentPage - 1) * pageSize + 1 }}-{{ Math.min(currentPage * pageSize, displayUsers.length) }} 条
          </el-text>
        </div>
      </div>
      
      <el-table 
        :data="paginatedUsers" 
        style="width: 100%" 
        v-loading="loading"
        row-key="userId"
        class="user-table"
        :header-cell-style="{ background: '#f5f7fa', color: '#606266' }"
        stripe
      >
        <el-table-column prop="userId" label="ID" width="80" align="center">
          <template #default="scope">
            <el-tag size="small" type="info">{{ scope.row.userId }}</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="username" label="用户信息" min-width="160">
          <template #default="scope">
            <div class="user-info-cell">
              <el-avatar :size="32" class="user-avatar">
                <el-icon><UserFilled /></el-icon>
              </el-avatar>
              <div class="user-details">
                <div class="username">{{ scope.row.username }}</div>
                <div class="user-meta">ID: {{ scope.row.userId }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="gender" label="性别" width="80" align="center">
          <template #default="scope">
            <el-tag 
              :type="scope.row.gender === '男' ? 'primary' : 'danger'" 
              size="small"
              v-if="scope.row.gender"
            >
              <el-icon>
                <Male v-if="scope.row.gender === '男'" />
                <Female v-else />
              </el-icon>
              {{ scope.row.gender }}
            </el-tag>
            <span v-else class="empty-data">未设置</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="telephone" label="联系方式" width="140">
          <template #default="scope">
            <div v-if="scope.row.telephone" class="contact-info">
              <el-icon><Phone /></el-icon>
              {{ scope.row.telephone }}
            </div>
            <span v-else class="empty-data">未填写</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="address" label="地址" min-width="150" show-overflow-tooltip>
          <template #default="scope">
            <div v-if="scope.row.address" class="address-info">
              <el-icon><Location /></el-icon>
              {{ scope.row.address }}
            </div>
            <span v-else class="empty-data">未填写</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="introduce" label="个人介绍" min-width="180" show-overflow-tooltip>
          <template #default="scope">
            <div v-if="scope.row.introduce" class="intro-info">
              {{ scope.row.introduce }}
            </div>
            <span v-else class="empty-data">暂无介绍</span>
          </template>
        </el-table-column>
        
        <el-table-column label="角色" width="120" align="center">
          <template #default="scope">
            <el-tag 
              :type="scope.row.role === 0 ? 'danger' : 'success'" 
              effect="dark"
              size="small"
            >
              <el-icon>
                <Star v-if="scope.row.role === 0" />
                <User v-else />
              </el-icon>
              {{ scope.row.role === 0 ? '管理员' : '普通用户' }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="scope">
            <div class="action-buttons">
              <el-button 
                size="small" 
                type="primary" 
                @click="editUser(scope.row)" 
                circle
                :icon="Edit"
                title="编辑用户"
              />
              <el-button 
                size="small" 
                type="warning" 
                @click="changePassword(scope.row)" 
                circle
                :icon="Key"
                title="修改密码"
              />
              <el-button
                size="small"
                type="danger"
                @click="deleteUser(scope.row)"
                :disabled="scope.row.role === 0 && scope.row.userId === currentUserId"
                circle
                :icon="Delete"
                title="删除用户"
              />
            </div>
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
          :total="displayUsers.length"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          background
        />
      </div>
    </div>

    <!-- 编辑用户对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="编辑用户信息"
      width="700px"
      :show-close="false"
      :before-close="handleDialogClose"
      class="user-dialog"
    >
      <div class="dialog-content">
        <div class="user-preview">
          <el-avatar :size="60" class="preview-avatar">
            <el-icon><UserFilled /></el-icon>
          </el-avatar>
          <div class="preview-info">
            <h4>{{ userForm.username }}</h4>
            <el-tag :type="userForm.role === 0 ? 'danger' : 'success'" size="small">
              {{ userForm.role === 0 ? '管理员' : '普通用户' }}
            </el-tag>
          </div>
        </div>
        
        <el-divider />
        
        <el-form ref="userFormRef" :model="userForm" :rules="userRules" label-width="100px">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="用户名">
                <el-input v-model="userForm.username" disabled>
                  <template #prefix>
                    <el-icon><User /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="性别" prop="gender">
                <el-radio-group v-model="userForm.gender">
                  <el-radio value="男">
                    <el-icon><Male /></el-icon>
                    男
                  </el-radio>
                  <el-radio value="女">
                    <el-icon><Female /></el-icon>
                    女
                  </el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="电话" prop="telephone">
                <el-input v-model="userForm.telephone" placeholder="请输入手机号">
                  <template #prefix>
                    <el-icon><Phone /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="用户角色" prop="role">
                <el-radio-group v-model="userForm.role">
                  <el-radio :value="1">
                    <el-icon><User /></el-icon>
                    普通用户
                  </el-radio>
                  <el-radio :value="0">
                    <el-icon><Star /></el-icon>
                    管理员
                  </el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-form-item label="地址" prop="address">
            <el-input v-model="userForm.address" placeholder="请输入地址">
              <template #prefix>
                <el-icon><Location /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          
          <el-form-item label="个人介绍" prop="introduce">
            <el-input
              v-model="userForm.introduce"
              type="textarea"
              :rows="3"
              placeholder="请输入个人介绍"
              show-word-limit
              maxlength="200"
            />
          </el-form-item>
        </el-form>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false" size="large">
            <el-icon><Close /></el-icon>
            取消
          </el-button>
          <el-button type="primary" @click="saveUser" :loading="saving" size="large">
            <el-icon><Check /></el-icon>
            {{ saving ? '更新中...' : '确认更新' }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 修改密码对话框 -->
    <el-dialog
      v-model="passwordDialogVisible"
      title="修改用户密码"
      width="500px"
      :show-close="false"
      :before-close="handlePasswordDialogClose"
      class="password-dialog"
    >
      <div class="dialog-content">
        <div class="password-user-info">
          <el-avatar :size="50" class="preview-avatar">
            <el-icon><UserFilled /></el-icon>
          </el-avatar>
          <div class="user-info">
            <h4>{{ currentPasswordUser.username }}</h4>
            <el-text type="info" size="small">ID: {{ currentPasswordUser.userId }}</el-text>
          </div>
        </div>
        
        <el-divider />
        
        <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="100px">
          <el-form-item label="新密码" prop="newPassword">
            <el-input
              v-model="passwordForm.newPassword"
              type="password"
              placeholder="请输入新密码"
              show-password
            >
              <template #prefix>
                <el-icon><Key /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input
              v-model="passwordForm.confirmPassword"
              type="password"
              placeholder="请再次确认新密码"
              show-password
            >
              <template #prefix>
                <el-icon><Key /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          
          <el-alert
            title="温馨提示"
            type="info"
            :closable="false"
            show-icon
          >
            <template #default>
              <p>• 密码长度至少6位字符</p>
              <p>• 建议包含字母、数字和特殊字符</p>
              <p>• 修改后用户需要使用新密码登录</p>
            </template>
          </el-alert>
        </el-form>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="passwordDialogVisible = false" size="large">
            <el-icon><Close /></el-icon>
            取消
          </el-button>
          <el-button type="danger" @click="resetPassword" :loading="passwordSaving" size="large" plain>
            <el-icon><RefreshLeft /></el-icon>
            重置为默认密码
          </el-button>
          <el-button type="primary" @click="savePassword" :loading="passwordSaving" size="large">
            <el-icon><Check /></el-icon>
            {{ passwordSaving ? '修改中...' : '确认修改' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import axios from 'axios'
import { 
  UserFilled, User, Avatar, Search, Setting, 
  Refresh, List, Edit, Delete, Phone, Location, 
  Male, Female, RefreshLeft, Close, Check, Star, Key
} from '@element-plus/icons-vue'
import type { AxiosError } from 'axios'

interface User {
  userId: number
  username: string
  gender: string
  telephone: string
  address: string
  introduce: string
  role: number
  createTime: string
  password?: string
}

interface UserForm {
  userId?: number
  username: string
  gender: string
  telephone: string
  address: string
  introduce: string
  role: number
  password?: string
}

interface SearchForm {
  username: string
  role: string | number
  gender: string
}

interface PasswordForm {
  newPassword: string
  confirmPassword: string
}

// 响应式数据
const loading = ref(false)
const saving = ref(false)
const passwordSaving = ref(false)
const dialogVisible = ref(false)
const passwordDialogVisible = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const currentUserId = ref(1)
const showAdvancedSearch = ref(false)

const userFormRef = ref<FormInstance>()
const passwordFormRef = ref<FormInstance>()

const searchForm = ref<SearchForm>({
  username: '',
  role: '',
  gender: ''
})

const userForm = ref<UserForm>({
  username: '',
  gender: '',
  telephone: '',
  address: '',
  introduce: '',
  role: 1
})

const passwordForm = ref<PasswordForm>({
  newPassword: '',
  confirmPassword: ''
})

const currentPasswordUser = ref<User>({
  userId: 0,
  username: '',
  gender: '',
  telephone: '',
  address: '',
  introduce: '',
  role: 1,
  createTime: ''
})

const users = ref<User[]>([])
const allUsers = ref<User[]>([])

// 计算属性
const totalUsers = computed(() => {
  return allUsers.value.length
})

const adminCount = computed(() => {
  return allUsers.value.filter(user => user.role === 0).length
})

const normalUserCount = computed(() => {
  return allUsers.value.filter(user => user.role === 1).length
})

const displayUsers = computed(() => {
  let filteredUsers = [...users.value]
  
  if (searchForm.value.username.trim()) {
    filteredUsers = filteredUsers.filter(user => 
      user.username.toLowerCase().includes(searchForm.value.username.toLowerCase().trim())
    )
  }
  
  if (searchForm.value.role !== '' && searchForm.value.role !== null) {
    filteredUsers = filteredUsers.filter(user => user.role === searchForm.value.role)
  }
  
  if (searchForm.value.gender !== '') {
    filteredUsers = filteredUsers.filter(user => user.gender === searchForm.value.gender)
  }
  
  return filteredUsers
})

const paginatedUsers = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return displayUsers.value.slice(start, end)
})

const validatePasswordConfirm = (rule: any, value: string, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== passwordForm.value.newPassword) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const userRules = {
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  telephone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ]
}

const passwordRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validatePasswordConfirm, trigger: 'blur' }
  ]
}

// 方法
onMounted(() => {
  loadUsers()
})

const loadUsers = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/api/admin/users', {
      params: {
        pageNum: 1,
        pageSize: 1000
      },
      withCredentials: true
    })
    
    if (response.status === 200) {
      allUsers.value = response.data.records || []
      users.value = [...allUsers.value]
      resetPagination()
    }
  } catch (error) {
    const axiosError = error as AxiosError
    if (axiosError.response?.status === 400) {
      ElMessage.error('无权限访问用户列表')
    } else {
      ElMessage.error('加载用户数据失败')
    }
  } finally {
    loading.value = false
  }
}

const toggleAdvancedSearch = () => {
  showAdvancedSearch.value = !showAdvancedSearch.value
  if (!showAdvancedSearch.value) {
    searchForm.value.role = ''
    searchForm.value.gender = ''
    handleLocalSearch()
  }
}

const handleLocalSearch = () => {
  currentPage.value = 1
}

const resetSearch = () => {
  searchForm.value.username = ''
  searchForm.value.role = ''
  searchForm.value.gender = ''
  users.value = [...allUsers.value]
  resetPagination()
}

const resetPagination = () => {
  currentPage.value = 1
}

const handleSizeChange = (val: number) => {
  pageSize.value = val
  currentPage.value = 1
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
}

const editUser = async (user: User) => {
  try {
    const response = await axios.get(`/api/api/admin/user/${user.userId}`, {
      withCredentials: true
    })
    
    if (response.status === 200) {
      userForm.value = { ...response.data }
      dialogVisible.value = true
    }
  } catch (error) {
    const axiosError = error as AxiosError
    if (axiosError.response?.status === 400) {
      const errorMsg = axiosError.response.data as string
      if (errorMsg === '用户不存在') {
        ElMessage.error('用户不存在')
      } else if (errorMsg === '无权限访问') {
        ElMessage.error('无权限编辑用户')
      } else {
        ElMessage.error('获取用户详情失败')
      }
    } else {
      ElMessage.error('获取用户详情失败')
    }
  }
}

const changePassword = (user: User) => {
  currentPasswordUser.value = { ...user }
  passwordForm.value = {
    newPassword: '',
    confirmPassword: ''
  }
  passwordDialogVisible.value = true
}

const handleDialogClose = () => {
  userFormRef.value?.resetFields()
}

const handlePasswordDialogClose = () => {
  passwordFormRef.value?.resetFields()
}

const saveUser = async () => {
  try {
    await userFormRef.value?.validate()
    saving.value = true

    const response = await axios.put(`/api/api/admin/user/${userForm.value.userId}`, userForm.value, {
      withCredentials: true
    })
    
    if (response.status === 200) {
      ElMessage.success('更新成功')
      dialogVisible.value = false
      const index = allUsers.value.findIndex(u => u.userId === userForm.value.userId)
      if (index !== -1) {
        allUsers.value[index] = { ...allUsers.value[index], ...userForm.value }
        users.value = [...allUsers.value]
      }
    }
  } catch (error) {
    const axiosError = error as AxiosError
    if (axiosError.response?.status === 400) {
      const errorMsg = axiosError.response.data as string
      if (errorMsg === '无权限访问') {
        ElMessage.error('无权限更新用户')
      } else if (errorMsg === '更新失败') {
        ElMessage.error('更新失败，请检查输入信息')
      } else {
        ElMessage.error('更新失败')
      }
    } else {
      ElMessage.error('更新用户失败')
    }
  } finally {
    saving.value = false
  }
}

const savePassword = async () => {
  try {
    await passwordFormRef.value?.validate()
    passwordSaving.value = true

    const updateData = {
      password: passwordForm.value.newPassword
    }

    const response = await axios.put(`/api/api/admin/user/${currentPasswordUser.value.userId}`, updateData, {
      withCredentials: true
    })
    
    if (response.status === 200) {
      ElMessage.success('密码修改成功')
      passwordDialogVisible.value = false
    }
  } catch (error) {
    const axiosError = error as AxiosError
    if (axiosError.response?.status === 400) {
      const errorMsg = axiosError.response.data as string
      ElMessage.error(errorMsg || '密码修改失败')
    } else {
      ElMessage.error('密码修改失败')
    }
  } finally {
    passwordSaving.value = false
  }
}

const resetPassword = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要重置用户 "${currentPasswordUser.value.username}" 的密码为默认密码(123456)吗？`,
      '重置密码确认',
      {
        confirmButtonText: '确定重置',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    passwordSaving.value = true
    
    const updateData = {
      password: '123456'
    }

    const response = await axios.put(`/api/api/admin/user/${currentPasswordUser.value.userId}`, updateData, {
      withCredentials: true
    })

    if (response.status === 200) {
      ElMessage.success('密码已重置为：123456')
      passwordDialogVisible.value = false
    }
  } catch (error) {
    if (error !== 'cancel') {
      const axiosError = error as AxiosError
      if (axiosError.response?.status === 400) {
        const errorMsg = axiosError.response.data as string
        ElMessage.error(errorMsg || '密码重置失败')
      } else {
        ElMessage.error('密码重置失败')
      }
    }
  } finally {
    passwordSaving.value = false
  }
}

const deleteUser = async (user: User) => {
  if (user.role === 0 && user.userId === currentUserId.value) {
    ElMessage.warning('不能删除当前登录的管理员账户')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要删除用户 "${user.username}" 吗？删除后无法恢复！`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'error'
      }
    )

    const response = await axios.delete(`/api/api/admin/user/${user.userId}`, {
      withCredentials: true
    })

    if (response.status === 200) {
      ElMessage.success('用户删除成功')
      allUsers.value = allUsers.value.filter(u => u.userId !== user.userId)
      users.value = [...allUsers.value]
      if (paginatedUsers.value.length === 0 && currentPage.value > 1) {
        currentPage.value--
      }
    }
  } catch (error) {
    if (error !== 'cancel') {
      const axiosError = error as AxiosError
      if (axiosError.response?.status === 400) {
        const errorMsg = axiosError.response.data as string
        if (errorMsg === '无权限访问') {
          ElMessage.error('无权限删除用户')
        } else if (errorMsg === '删除失败') {
          ElMessage.error('删除失败，用户可能不存在或有关联数据')
        } else {
          ElMessage.error('删除失败')
        }
      } else {
        ElMessage.error('删除用户失败')
      }
    }
  }
}

const getCurrentUser = () => {
}

onMounted(() => {
  getCurrentUser()
})
</script>

<style scoped>
.user-management {
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  min-height: 100vh;
}

/* 页面头部样式 - 保持浅色背景，修改文字颜色 */
.page-header {
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 16px;
  margin-bottom: 24px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.header-content {
  padding: 32px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #2c3e50;
}

.title-section h2 {
  margin: 0 0 8px 0;
  font-size: 28px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 12px;
  color: #2c3e50;
}

.title-icon {
  font-size: 32px;
  color: #667eea;
}

.subtitle {
  margin: 0;
  opacity: 0.8;
  font-size: 14px;
  color: #5a6c7d;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.header-actions .el-button {
  border: 2px solid #667eea;
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
  backdrop-filter: blur(10px);
}

.header-actions .el-button:hover {
  background: rgba(102, 126, 234, 0.2);
  transform: translateY(-2px);
  color: #667eea;
  border-color: #667eea;
}

/* 统计卡片样式 */
.stats-section {
  margin-bottom: 24px;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 20px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  right: 0;
  width: 100px;
  height: 100px;
  border-radius: 50%;
  opacity: 0.1;
  transform: translate(30px, -30px);
}

.stat-card.total-users::before {
  background: #409eff;
}

.stat-card.admin-users::before {
  background: #f56c6c;
}

.stat-card.normal-users::before {
  background: #67c23a;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.stat-icon {
  font-size: 28px;
  margin-bottom: 12px;
}

.stat-card.total-users .stat-icon {
  color: #409eff;
}

.stat-card.admin-users .stat-icon {
  color: #f56c6c;
}

.stat-card.normal-users .stat-icon {
  color: #67c23a;
}

.stat-number {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
  line-height: 1;
  margin-bottom: 4px;
}

.stat-label {
  color: #606266;
  font-size: 14px;
}

/* 筛选区域样式 */
.filter-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.filter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.filter-header h3 {
  margin: 0;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
}

.search-form {
  margin: 0;
}

.search-input, .search-select {
  width: 200px;
}

/* 表格区域样式 */
.table-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.table-header h3 {
  margin: 0;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-table {
  border-radius: 8px;
  overflow: hidden;
}

.user-info-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.user-details {
  flex: 1;
}

.username {
  font-weight: 500;
  color: #303133;
  margin-bottom: 2px;
}

.user-meta {
  font-size: 12px;
  color: #909399;
}

.contact-info, .address-info {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #606266;
}

.intro-info {
  max-width: 150px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #606266;
}

.empty-data {
  color: #c0c4cc;
  font-style: italic;
  font-size: 12px;
}

.action-buttons {
  display: flex;
  gap: 12px;
  justify-content: center;
  align-items: center;
}

.action-buttons .el-button {
  width: 32px;
  height: 32px;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.action-buttons .el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.action-buttons .el-button.is-disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.action-buttons .el-button.is-disabled:hover {
  transform: none;
  box-shadow: none;
}

.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

/* 对话框样式 */
.user-dialog .el-dialog__body,
.password-dialog .el-dialog__body {
  padding: 20px 30px;
}

.dialog-content {
  max-height: 70vh;
  overflow-y: auto;
}

.user-preview, .password-user-info {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 8px;
  margin-bottom: 20px;
}

.preview-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.preview-info h4, .user-info h4 {
  margin: 0 0 8px 0;
  color: #303133;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px 0 0 0;
}

/* 密码对话框专用样式 */
.password-user-info {
  background: linear-gradient(135deg, #fff7e6 0%, #ffe7ba 100%);
}

.password-dialog .el-alert {
  margin-top: 16px;
}

.password-dialog .el-alert p {
  margin: 2px 0;
  font-size: 12px;
  color: #606266;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .user-management {
    padding: 16px;
  }
  
  .header-content {
    flex-direction: column;
    gap: 20px;
    text-align: center;
    padding: 24px;
  }
  
  .header-actions {
    width: 100%;
    justify-content: center;
  }
  
  .stats-cards {
    grid-template-columns: 1fr;
  }
  
  .filter-header {
    flex-direction: column;
    gap: 12px;
    text-align: center;
  }
  
  .search-form {
    display: block;
  }
  
  .search-form .el-form-item {
    margin-bottom: 16px;
  }
  
  .search-input, .search-select {
    width: 100%;
  }
  
  .table-header {
    flex-direction: column;
    gap: 12px;
    text-align: center;
  }
  
  .user-info-cell {
    flex-direction: column;
    text-align: center;
    gap: 8px;
  }
  
  .action-buttons {
    flex-direction: column;
    gap: 4px;
  }
  
  .user-preview, .password-user-info {
    flex-direction: column;
    text-align: center;
  }
}

@media (max-width: 480px) {
  .title-section h2 {
    font-size: 22px;
  }
  
  .stat-number {
    font-size: 24px;
  }
  
  .el-table {
    font-size: 12px;
  }
}

/* 动画效果 */
.stat-card {
  animation: slideInUp 0.6s ease-out;
}

.stat-card:nth-child(1) { animation-delay: 0.1s; }
.stat-card:nth-child(2) { animation-delay: 0.2s; }
.stat-card:nth-child(3) { animation-delay: 0.3s; }

@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.filter-section, .table-section {
  animation: fadeInUp 0.8s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>