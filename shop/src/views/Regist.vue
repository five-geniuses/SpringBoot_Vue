<template>
  <div class="register-container">
    <el-card class="register-card">
      <template #header>
        <div class="card-header">
          <span>用户注册</span>
        </div>
      </template>
      
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" @blur="checkUsername"></el-input>
        </el-form-item>
        
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password"></el-input>
        </el-form-item>
        
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password"></el-input>
        </el-form-item>
        
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email"></el-input>
        </el-form-item>
        
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone"></el-input>
        </el-form-item>
        
        <el-form-item label="地址" prop="address">
          <el-input v-model="form.address"></el-input>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="submitForm">注册</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import { useRouter } from 'vue-router'

export default {
  name: 'Register',
  setup() {
    const router = useRouter()
    const formRef = ref(null)
    
    const form = reactive({
      username: '',
      password: '',
      confirmPassword: '',
      email: '',
      phone: '',
      address: ''
    })
    
    const validatePass2 = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请再次输入密码'))
      } else if (value !== form.password) {
        callback(new Error('两次输入密码不一致!'))
      } else {
        callback()
      }
    }
    
    const rules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
      ],
      confirmPassword: [
        { required: true, validator: validatePass2, trigger: 'blur' }
      ],
      email: [
        { required: true, message: '请输入邮箱地址', trigger: 'blur' },
        { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
      ],
      phone: [
        { required: true, message: '请输入手机号', trigger: 'blur' },
        { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
      ],
      address: [
        { required: true, message: '请输入地址', trigger: 'blur' }
      ]
    }
    
    const checkUsername = async () => {
      try {
        const response = await axios.get(`/api/user/check-username?username=${form.username}`)
        if (response.data.exists) {
          ElMessage.error('用户名已存在')
          form.username = ''
        }
      } catch (error) {
        console.error('检查用户名出错:', error)
      }
    }
    
    const submitForm = () => {
      if (!formRef.value) return
      
      formRef.value.validate(async (valid) => {
        if (valid) {
          try {
            const { confirmPassword, ...registerData } = form
            const response = await axios.post('/api/user/register', registerData)
            ElMessage.success('注册成功')
            router.push('/login')
          } catch (error) {
            ElMessage.error(error.response?.data || '注册失败')
          }
        }
      })
    }
    
    const resetForm = () => {
      if (!formRef.value) return
      formRef.value.resetFields()
    }
    
    return {
      formRef,
      form,
      rules,
      checkUsername,
      submitForm,
      resetForm
    }
  }
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f5f5f5;
}

.register-card {
  width: 480px;
}

.card-header {
  text-align: center;
  font-size: 20px;
  font-weight: bold;
}

.el-form-item {
  margin-bottom: 20px;
}
</style> 