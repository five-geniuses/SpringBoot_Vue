<template>
  <div class="login-container">
    <!-- 背景图片 -->
    <div class="background-image"></div>

    <!-- 主要内容 -->
    <div class="content-wrapper">
      <!-- 左侧品牌区域 -->
      <div class="brand-section">
        <div class="brand-content">
          <div class="logo-wrapper">
            <div class="logo-icon">🛍️</div>
            <h1 class="brand-title">欢迎光临</h1>
          </div>
          <p class="brand-subtitle">购物商城</p>
          <div class="brand-desc">⭐ 优质商品 · 贴心服务 · 购物新体验 ⭐</div>
        </div>
      </div>

      <!-- 右侧注册表单 -->
      <div class="login-section">
        <div class="login-card">
          <!-- 头部用户图标 -->
          <div class="login-header">
            <div class="user-avatar">
              <span>👤</span>
            </div>
            <h2 class="login-title">注册账户</h2>
            <p class="login-subtitle">请输入以下信息完成注册</p>
          </div>

          <!-- 分割线 -->
          <div class="section-divider">
            <span class="divider-text">注册信息</span>
          </div>

          <!-- 注册表单 -->
          <el-form 
            ref="formRef" 
            :rules="rules" 
            :model="user" 
            class="login-form"
            @submit.prevent="register"
          >
            <!-- 用户名输入框 -->
            <el-form-item prop="username">
              <el-input 
                v-model="user.username" 
                placeholder="请输入用户名"
                size="large"
                class="form-input"
                clearable
              >
                <template #prefix>
                  <el-icon class="input-icon">
                    <User />
                  </el-icon>
                </template>
              </el-input>
            </el-form-item>

            <!-- 密码输入框 -->
            <el-form-item prop="password">
              <el-input 
                type="password" 
                v-model="user.password" 
                placeholder="请输入密码" 
                show-password
                size="large"
                class="form-input"
              >
                <template #prefix>
                  <el-icon class="input-icon">
                    <Lock />
                  </el-icon>
                </template>
              </el-input>
            </el-form-item>

            <!-- 性别选择 -->
            <el-form-item prop="gender">
              <el-select 
                v-model="user.gender" 
                placeholder="请选择性别"
                size="large"
                class="form-input"
                style="width: 100%"
              >
                <template #prefix>
                  <el-icon class="input-icon">
                    <Avatar />
                  </el-icon>
                </template>
                <el-option label="男" value="男"></el-option>
                <el-option label="女" value="女"></el-option>
              </el-select>
            </el-form-item>

            <!-- 电话输入框 -->
            <el-form-item prop="telephone">
              <el-input 
                type="tel" 
                v-model="user.telephone" 
                placeholder="请输入手机号"
                size="large"
                class="form-input"
                clearable
              >
                <template #prefix>
                  <el-icon class="input-icon">
                    <Phone />
                  </el-icon>
                </template>
              </el-input>
            </el-form-item>

            <!-- 个人介绍输入框 -->
            <el-form-item prop="introduce">
              <el-input 
                type="textarea" 
                v-model="user.introduce" 
                placeholder="请输入个人介绍（可选）"
                :rows="2"
                class="form-input"
                clearable
              >
              </el-input>
            </el-form-item>

            <!-- 地址输入框 -->
            <el-form-item prop="address">
              <el-input 
                v-model="user.address" 
                placeholder="请输入地址（可选）"
                size="large"
                class="form-input"
                clearable
              >
                <template #prefix>
                  <el-icon class="input-icon">
                    <Location />
                  </el-icon>
                </template>
              </el-input>
            </el-form-item>

            <!-- 注册按钮 -->
            <el-form-item>
              <el-button 
                type="primary"
                size="large"
                class="login-button"
                native-type="submit"
                :loading="loading"
                round
              >
                <span v-if="!loading">🚀 立即注册</span>
                <span v-else>注册中...</span>
              </el-button>
            </el-form-item>
          </el-form>

          <!-- 底部登录链接 -->
          <div class="login-footer">
            <router-link to="/login" class="register-link">
              🔐 已有账号？立即登录
            </router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, type FormInstance } from 'element-plus';
import { User, Lock, Phone, Location, Avatar } from '@element-plus/icons-vue';
import axios from 'axios';

const router = useRouter();
const formRef = ref<FormInstance>();

interface RegisterForm {
  username: string;
  password: string;
  gender: string;
  telephone: string;
  introduce: string;
  address: string;
  role: number;
}

// 表单数据
const user = reactive<RegisterForm>({
  username: "",
  password: "",
  gender: "",
  telephone: "",
  introduce: "",
  address: "",
  role: 1
});

// 验证规则
const rules = {
  username: [
    { required: true, message: "用户名不能为空", trigger: "blur" },
    { min: 1, max: 20, message: "用户名长度必须是1-20个字符", trigger: "blur" }
  ],
  password: [
    { required: true, message: "密码不能为空", trigger: "blur" },
    { min: 6, message: "密码长度至少为6个字符", trigger: "blur" }
  ],
  gender: [
    { required: true, message: "请选择性别", trigger: "change" }
  ],
  telephone: [
    { required: true, message: "手机号不能为空", trigger: "blur" },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: ['blur', 'change'] }
  ]
};

const loading = ref(false);

const register = async () => {
  try {
    await formRef.value?.validate();
    loading.value = true;

    const requestData = {
      username: user.username,
      password: user.password,
      gender: user.gender,
      telephone: user.telephone,
      introduce: user.introduce || "",
      address: user.address || "",
      role: 1
    };

    console.log('发送注册请求:', requestData);

    const response = await axios.post('/api/user/register', requestData, {
      headers: {
        'Content-Type': 'application/json'
      }
    });

    console.log('注册响应:', response);
    handleRegisterResponse(response.data);
  } catch (error) {
    handleRegisterError(error);
  } finally {
    loading.value = false;
  }
};

const handleRegisterResponse = (data: any) => {
  console.log('注册返回数据:', data);
  
  // 根据返回的数据判断注册是否成功
  if (data === "注册成功！" || data === "success" || (typeof data === 'string' && data.includes("成功"))) {
    ElMessage.success("注册成功！请登录");
    router.push("/login");
  } else if (typeof data === 'object' && data.success) {
    ElMessage.success("注册成功！请登录");
    router.push("/login");
  } else {
    ElMessage.error(data?.message || data || "注册失败");
  }
};

const handleRegisterError = (error: any) => {
  console.error("注册错误详情:", error);
  
  if (error.response) {
    const status = error.response.status;
    const errorData = error.response.data;
    
    console.log('错误状态码:', status);
    console.log('错误数据:', errorData);
    
    let errorMsg = "注册失败";
    
    if (status === 415) {
      errorMsg = "请求格式不被支持";
    } else if (status === 405) {
      errorMsg = "请求方法不被允许";
    } else if (status === 400) {
      errorMsg = "请求参数错误";
    } else if (status === 500) {
      errorMsg = "服务器内部错误";
    } else if (typeof errorData === 'string') {
      errorMsg = errorData;
    } else if (errorData && errorData.message) {
      errorMsg = errorData.message;
    } else if (errorData && errorData.error) {
      errorMsg = errorData.error;
    }
    
    ElMessage.error(errorMsg);
  } else if (error.request) {
    ElMessage.error("无法连接到服务器，请检查网络或后端服务");
  } else {
    ElMessage.error("请求配置错误: " + error.message);
  }
};
</script>

<style scoped>
/* 保持原有样式不变... */
.login-container {
  position: relative;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
}

.background-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: url('/background.png');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  z-index: 1;
}

.content-wrapper {
  position: relative;
  display: flex;
  width: 100%;
  height: 100%;
  z-index: 2;
}

/* 左侧品牌区域 */
.brand-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  color: white;
}

.brand-content {
  text-align: center;
  max-width: 500px;
}

.logo-wrapper {
  margin-bottom: 2rem;
}

.logo-icon {
  font-size: 4rem;
  margin-bottom: 2rem;
  display: block;
  animation: float 3s ease-in-out infinite;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
}

.brand-title {
  font-size: 4rem;
  font-weight: bold;
  margin: 0 0 1.5rem 0;
  text-shadow: 3px 3px 6px rgba(0, 0, 0, 0.4);
  letter-spacing: 3px;
}

.brand-subtitle {
  font-size: 1.3rem;
  margin-bottom: 1.5rem;
  opacity: 0.95;
  line-height: 1.6;
  font-weight: 300;
  text-shadow: 1px 1px 3px rgba(0, 0, 0, 0.3);
}

.brand-desc {
  font-size: 1.1rem;
  opacity: 0.9;
  font-weight: 400;
  text-shadow: 1px 1px 3px rgba(0, 0, 0, 0.3);
}

/* 右侧注册区域 */
.login-section {
  width: 450px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem 1.5rem;
}

.login-card {
  width: 100%;
  max-width: 400px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  padding: 2.5rem 2rem;
  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.6);
  animation: slideInRight 0.8s ease-out;
  max-height: 90vh;
  overflow-y: auto;
}

/* 注册头部 */
.login-header {
  text-align: center;
  margin-bottom: 1.5rem;
}

.user-avatar {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #ec4899, #f472b6);
  border-radius: 50%;
  margin-bottom: 1rem;
  font-size: 1.8rem;
  box-shadow: 0 8px 20px rgba(236, 72, 153, 0.3);
  animation: pulse 2s infinite;
}

.login-title {
  margin: 0 0 0.5rem 0;
  font-size: 1.8rem;
  font-weight: 700;
  color: #1f2937;
  letter-spacing: 1px;
}

.login-subtitle {
  margin: 0;
  color: #6b7280;
  font-size: 0.9rem;
  font-weight: 400;
}

/* 分割线 */
.section-divider {
  text-align: center;
  margin: 1.5rem 0;
  position: relative;
}

.section-divider::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(to right, transparent, #d1d5db, transparent);
}

.divider-text {
  background: rgba(255, 255, 255, 0.95);
  padding: 0 1.2rem;
  color: #9ca3af;
  font-size: 0.8rem;
  position: relative;
  z-index: 1;
  font-weight: 500;
}

/* 注册表单 */
.login-form {
  margin-bottom: 1.5rem;
}

.login-form .el-form-item {
  margin-bottom: 1.2rem;
}

.input-icon {
  color: #ec4899 !important;
  font-size: 1.1rem;
}

:deep(.form-input .el-input__wrapper) {
  border-radius: 12px;
  border: 2px solid #f1f5f9;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
  padding: 14px 16px;
  background-color: #fafbfc;
  font-size: 0.95rem;
}

:deep(.form-input .el-input__wrapper:hover) {
  border-color: #ec4899;
  background-color: #ffffff;
  box-shadow: 0 4px 12px rgba(236, 72, 153, 0.15);
}

:deep(.form-input.is-focus .el-input__wrapper) {
  border-color: #ec4899;
  box-shadow: 0 0 0 3px rgba(236, 72, 153, 0.12);
  background-color: #ffffff;
}

:deep(.form-input .el-input__inner) {
  color: #374151;
  font-weight: 500;
}

:deep(.form-input .el-input__inner::placeholder) {
  color: #9ca3af;
  font-weight: 400;
}

/* 下拉选择框样式 */
:deep(.el-select .el-input .el-input__wrapper) {
  border-radius: 12px;
  border: 2px solid #f1f5f9;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
  padding: 14px 16px;
  background-color: #fafbfc;
  font-size: 0.95rem;
}

:deep(.el-select .el-input .el-input__wrapper:hover) {
  border-color: #ec4899;
  background-color: #ffffff;
  box-shadow: 0 4px 12px rgba(236, 72, 153, 0.15);
}

:deep(.el-select.is-focus .el-input .el-input__wrapper) {
  border-color: #ec4899;
  box-shadow: 0 0 0 3px rgba(236, 72, 153, 0.12);
  background-color: #ffffff;
}

/* 文本域样式 */
:deep(.el-textarea .el-textarea__inner) {
  border-radius: 12px;
  border: 2px solid #f1f5f9;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
  padding: 14px 16px;
  background-color: #fafbfc;
  font-size: 0.95rem;
  color: #374151;
  font-weight: 500;
}

:deep(.el-textarea .el-textarea__inner:hover) {
  border-color: #ec4899;
  background-color: #ffffff;
  box-shadow: 0 4px 12px rgba(236, 72, 153, 0.15);
}

:deep(.el-textarea.is-focus .el-textarea__inner) {
  border-color: #ec4899;
  box-shadow: 0 0 0 3px rgba(236, 72, 153, 0.12);
  background-color: #ffffff;
}

.login-button {
  width: 100%;
  height: 48px;
  background: linear-gradient(135deg, #ec4899, #f472b6);
  border: none;
  font-size: 1rem;
  font-weight: 600;
  letter-spacing: 0.8px;
  transition: all 0.3s ease;
  box-shadow: 0 6px 20px rgba(236, 72, 153, 0.3);
  margin-top: 0.5rem;
}

.login-button:hover {
  background: linear-gradient(135deg, #db2777, #ec4899);
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(236, 72, 153, 0.4);
}

.login-button:active {
  transform: translateY(0);
}

/* 底部链接 */
.login-footer {
  text-align: center;
  padding-top: 1.5rem;
  border-top: 1px solid #f3f4f6;
}

.register-link {
  color: #ec4899;
  text-decoration: none;
  font-weight: 600;
  font-size: 0.9rem;
  transition: all 0.3s ease;
  display: inline-block;
}

.register-link:hover {
  color: #db2777;
  transform: translateY(-1px);
}

/* 动画效果 */
@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-15px);
  }
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}

@keyframes slideInRight {
  from {
    transform: translateX(50px);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .content-wrapper {
    flex-direction: column;
  }
  
  .brand-section {
    flex: none;
    height: 35%;
    padding: 1.5rem;
  }
  
  .brand-title {
    font-size: 2.5rem;
  }
  
  .login-section {
    width: 100%;
    height: 65%;
    padding: 1rem;
  }
  
  .login-card {
    padding: 2rem 1.5rem;
    margin: 0 1rem;
  }
}

@media (max-width: 480px) {
  .brand-title {
    font-size: 2rem;
  }
  
  .login-card {
    padding: 1.8rem 1.2rem;
    margin: 0 0.8rem;
  }
  
  .user-avatar {
    width: 50px;
    height: 50px;
    font-size: 1.5rem;
  }
  
  .login-title {
    font-size: 1.6rem;
  }
}

/* 表单验证错误样式 */
:deep(.el-form-item.is-error .el-input__wrapper) {
  border-color: #ef4444;
  box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.12);
}

:deep(.el-form-item.is-error .el-textarea__inner) {
  border-color: #ef4444;
  box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.12);
}

:deep(.el-form-item__error) {
  font-size: 0.8rem;
  margin-top: 0.3rem;
}
</style>