<template>
  <div class="user-management">
    <div class="page-header">
      <h2>用户管理</h2>
      <el-button type="primary" @click="showAddDialog">
        <el-icon><Plus /></el-icon>
        添加用户
      </el-button>
    </div>

    <!-- 搜索和筛选 -->
    <div class="filter-section">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户名">
          <el-input
            v-model="searchForm.username"
            placeholder="请输入用户名"
            clearable
            @input="handleSearch"
          />
        </el-form-item>
        <el-form-item label="用户角色">
          <el-select v-model="searchForm.role" placeholder="请选择角色" clearable>
            <el-option label="管理员" :value="0" />
            <el-option label="普通用户" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="性别">
          <el-select v-model="searchForm.gender" placeholder="请选择性别" clearable>
            <el-option label="男" value="男" />
            <el-option label="女" value="女" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 用户表格 -->
    <div class="table-section">
      <el-table 
        :data="filteredUsers" 
        style="width: 100%"
        v-loading="loading"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="gender" label="性别" width="80" />
        <el-table-column prop="telephone" label="电话" width="130" />
        <el-table-column prop="address" label="地址" min-width="150" show-overflow-tooltip />
        <el-table-column prop="introduce" label="个人介绍" min-width="150" show-overflow-tooltip />
        <el-table-column label="角色" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.role === 0 ? 'danger' : 'success'">
              {{ scope.row.role === 0 ? '管理员' : '普通用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="editUser(scope.row)">编辑</el-button>
            <el-button 
              size="small" 
              @click="resetPassword(scope.row)"
              type="warning"
            >
              重置密码
            </el-button>
            <el-button 
              size="small" 
              type="danger" 
              @click="deleteUser(scope.row)"
              :disabled="scope.row.role === 0 && scope.row.id === currentUserId"
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
          :total="totalUsers"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 添加/编辑用户对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :before-close="handleDialogClose"
    >
      <el-form
        ref="userFormRef"
        :model="userForm"
        :rules="userRules"
        label-width="100px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input 
            v-model="userForm.username" 
            placeholder="请输入用户名"
            :disabled="isEdit"
          />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEdit">
          <el-input 
            v-model="userForm.password" 
            type="password"
            placeholder="请输入密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="userForm.gender">
            <el-radio value="男">男</el-radio>
            <el-radio value="女">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="电话" prop="telephone">
          <el-input 
            v-model="userForm.telephone" 
            placeholder="请输入手机号"
          />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input 
            v-model="userForm.address" 
            placeholder="请输入地址"
          />
        </el-form-item>
        <el-form-item label="个人介绍" prop="introduce">
          <el-input
            v-model="userForm.introduce"
            type="textarea"
            :rows="3"
            placeholder="请输入个人介绍"
          />
        </el-form-item>
        <el-form-item label="用户角色" prop="role">
          <el-radio-group v-model="userForm.role">
            <el-radio :value="1">普通用户</el-radio>
            <el-radio :value="0">管理员</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveUser" :loading="saving">
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

interface User {
  id: number
  username: string
  password?: string
  gender: string
  telephone: string
  address: string
  introduce: string
  role: number
  createTime: string
}

interface UserForm {
  id?: number
  username: string
  password?: string
  gender: string
  telephone: string
  address: string
  introduce: string
  role: number
}

interface SearchForm {
  username: string
  role: number | string
  gender: string
}

// 响应式数据
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const totalUsers = ref(0)
const currentUserId = ref(1) // 当前登录用户ID

const userFormRef = ref<FormInstance>()

const searchForm = ref<SearchForm>({
  username: '',
  role: '',
  gender: ''
})

const userForm = ref<UserForm>({
  username: '',
  password: '',
  gender: '',
  telephone: '',
  address: '',
  introduce: '',
  role: 1
})

// 模拟用户数据
const users = ref<User[]>([
  {
    id: 1,
    username: 'admin',
    gender: '男',
    telephone: '13800138000',
    address: '北京市朝阳区',
    introduce: '系统管理员',
    role: 0,
    createTime: '2025-01-01 10:00:00'
  },
  {
    id: 2,
    username: 'user001',
    gender: '女',
    telephone: '13800138001',
    address: '上海市浦东新区',
    introduce: '普通用户',
    role: 1,
    createTime: '2025-01-02 11:00:00'
  },
  {
    id: 3,
    username: 'user002',
    gender: '男',
    telephone: '13800138002',
    address: '广州市天河区',
    introduce: '喜欢购物的用户',
    role: 1,
    createTime: '2025-01-03 12:00:00'
  }
])
// 表单验证规则
const userRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 1, max: 20, message: '用户名长度必须是1-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少为6个字符', trigger: 'blur' }
  ],
  gender: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ],
  telephone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ]
}

// 计算属性
const filteredUsers = computed(() => {
  let filtered = users.value

  if (searchForm.value.username) {
    filtered = filtered.filter(user =>
      user.username.toLowerCase().includes(searchForm.value.username.toLowerCase())
    )
  }

  if (searchForm.value.role !== '') {
    filtered = filtered.filter(user => user.role === Number(searchForm.value.role))
  }

  if (searchForm.value.gender) {
    filtered = filtered.filter(user => user.gender === searchForm.value.gender)
  }

  totalUsers.value = filtered.length
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filtered.slice(start, end)
})

const dialogTitle = computed(() => isEdit.value ? '编辑用户' : '添加用户')

// 方法
onMounted(() => {
  loadUsers()
})

const loadUsers = async () => {
  loading.value = true
  try {
    // 这里可以调用API获取用户数据
    // const response = await axios.get('/api/admin/users')
    // users.value = response.data
    await new Promise(resolve => setTimeout(resolve, 500)) // 模拟加载
  } catch (error) {
    ElMessage.error('加载用户数据失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
}

const resetSearch = () => {
  searchForm.value = {
    username: '',
    role: '',
    gender: ''
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
  userForm.value = {
    username: '',
    password: '',
    gender: '',
    telephone: '',
    address: '',
    introduce: '',
    role: 1
  }
  dialogVisible.value = true
}

const editUser = (user: User) => {
  isEdit.value = true
  userForm.value = { ...user }
  delete userForm.value.password // 编辑时不显示密码
  dialogVisible.value = true
}

const handleDialogClose = () => {
  userFormRef.value?.resetFields()
}

const saveUser = async () => {
  try {
    await userFormRef.value?.validate()
    saving.value = true

    if (isEdit.value) {
      // 更新用户
      const index = users.value.findIndex(u => u.id === userForm.value.id)
      if (index !== -1) {
        users.value[index] = {
          ...users.value[index],
          ...userForm.value
        }
      }
      ElMessage.success('用户更新成功')
    } else {
      // 添加用户
      const newUser: User = {
        ...userForm.value,
        id: Date.now(),
        createTime: new Date().toLocaleString('zh-CN')
      } as User
      users.value.unshift(newUser)
      ElMessage.success('用户添加成功')
    }

    dialogVisible.value = false
  } catch (error) {
    console.error('保存用户失败:', error)
  } finally {
    saving.value = false
  }
}

const resetPassword = async (user: User) => {
  try {
    await ElMessageBox.confirm(`确定要重置用户 "${user.username}" 的密码吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    // 这里调用重置密码API
    ElMessage.success('密码重置成功，新密码为：123456')
  } catch {
    // 用户取消
  }
}

const deleteUser = async (user: User) => {
  if (user.role === 0 && user.id === currentUserId.value) {
    ElMessage.warning('不能删除当前登录的管理员账户')
    return
  }

  try {
    await ElMessageBox.confirm(`确定要删除用户 "${user.username}" 吗？`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'error'
    })

    const index = users.value.findIndex(u => u.id === user.id)
    if (index !== -1) {
      users.value.splice(index, 1)
      ElMessage.success('用户删除成功')
    }
  } catch {
    // 用户取消
  }
}
</script>

<style scoped>
.user-management {
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