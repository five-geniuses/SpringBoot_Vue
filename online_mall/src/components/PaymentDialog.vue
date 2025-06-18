<template>
  <el-dialog
    v-model="dialogVisible"
    title="订单支付"
    width="400px"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
  >
    <div class="payment-content">
      <div class="order-info">
        <div class="amount-display">
          <span class="currency">¥</span>
          <span class="amount">{{ orderInfo.totalAmount }}</span>
        </div>
        <div class="order-details">
          <p>订单号：{{ orderInfo.orderNo }}</p>
        </div>
      </div>

      <div class="payment-methods">
        <div class="method-title">选择支付方式</div>
        <el-radio-group v-model="selectedMethod" class="method-list">
          <el-radio label="balance" border>
            <div class="method-item">
              <i class="el-icon-wallet">💰</i>
              <span>余额支付</span>
            </div>
          </el-radio>
          <el-radio label="wechat" border>
            <div class="method-item">
              <i class="el-icon-wechat">💚</i>
              <span>微信支付</span>
            </div>
          </el-radio>
          <el-radio label="alipay" border>
            <div class="method-item">
              <i class="el-icon-alipay">💙</i>
              <span>支付宝</span>
            </div>
          </el-radio>
        </el-radio-group>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleCancel">取消支付</el-button>
        <el-button type="primary" :loading="paying" @click="handlePay">
          {{ paying ? '支付中...' : '确认支付' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, defineProps, defineEmits, watch } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  visible: {
    type: Boolean,
    required: true
  },
  orderInfo: {
    type: Object,
    required: true,
    default: () => ({
      orderNo: '',
      totalAmount: 0
    })
  }
})

const emit = defineEmits(['update:visible', 'pay-success', 'pay-cancel'])

const dialogVisible = ref(props.visible)
const selectedMethod = ref('alipay')
const paying = ref(false)

// 监听visible属性变化
watch(() => props.visible, (newVal: boolean) => {
  dialogVisible.value = newVal
})

// 监听对话框关闭
watch(dialogVisible, (newVal: boolean) => {
  if (!newVal) {
    emit('update:visible', false)
  }
})

const handlePay = async () => {
  if (!selectedMethod.value) {
    ElMessage.warning('请选择支付方式')
    return
  }

  paying.value = true
  
  try {
    if (selectedMethod.value === 'alipay') {
      // 调用支付宝支付
      console.log('开始创建支付订单...')
      const requestData = {
        outTradeNo: props.orderInfo.orderNo,
        totalAmount: props.orderInfo.totalAmount.toFixed(2),
        subject: `订单${props.orderInfo.orderNo}`,
        returnUrl: `${window.location.origin}/user/orders`
      }
      console.log('请求数据：', requestData)

      const response = await fetch('http://localhost:8080/api/pay/create', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json'
        },
        credentials: 'include',
        body: JSON.stringify(requestData)
      })

      if (!response.ok) {
        throw new Error(`支付创建失败: ${response.status} ${response.statusText}`)
      }

      const result = await response.json()
      
      if (result.code === 200) {
        console.log('创建支付表单...')
        // 创建一个临时div来放置支付表单
        const div = document.createElement('div')
        div.innerHTML = result.data
        document.body.appendChild(div)
        
        // 获取并设置表单
        const form = div.getElementsByTagName('form')[0]
        if (!form) {
          document.body.removeChild(div)
          throw new Error('支付表单创建失败')
        }
        
        // 在新窗口中打开支付表单
        form.setAttribute('target', '_blank')
        form.submit()
        
        // 移除临时div
        document.body.removeChild(div)

        // 开始轮询支付状态
        let attempts = 0
        const maxAttempts = 12 // 最多轮询12次
        const interval = 5000 // 每5秒查询一次

        const checkPayStatus = async () => {
          try {
            const updateResponse = await fetch(
              `http://localhost:8080/api/pay/manual-update/${requestData.outTradeNo}`,
              {
                method: 'POST',
                headers: {
                  'Content-Type': 'application/json'
                }
              }
            )

            if (updateResponse.ok) {
              const updateResult = await updateResponse.json()
              if (updateResult.code === 200) {
                console.log('订单支付状态更新成功')
                ElMessage.success('支付成功！')
                emit('pay-success')
                dialogVisible.value = false
                return true
              }
            }
            return false
          } catch (error) {
            console.error('检查支付状态失败：', error)
            return false
          }
        }

        const pollStatus = async () => {
          while (attempts < maxAttempts) {
            const success = await checkPayStatus()
            if (success) {
              break
            }
            attempts++
            if (attempts < maxAttempts) {
              await new Promise(resolve => setTimeout(resolve, interval))
            }
          }
        }

        // 开始轮询
        pollStatus()
      } else {
        throw new Error(result.message || '创建支付订单失败')
      }
    } else {
      // 其他支付方式的模拟支付过程
      await new Promise(resolve => setTimeout(resolve, 2000))
      
      if (Math.random() < 0.9) {
        ElMessage.success('支付成功！')
        emit('pay-success')
        dialogVisible.value = false
      } else {
        throw new Error('支付失败')
      }
    }
  } catch (error: any) {
    console.error('支付失败：', error)
    ElMessage.error(error.message || '支付失败，请重试')
  } finally {
    paying.value = false
  }
}

const handleCancel = () => {
  emit('pay-cancel')
  dialogVisible.value = false
}
</script>

<style scoped>
.payment-content {
  padding: 20px 0;
}

.order-info {
  text-align: center;
  margin-bottom: 30px;
}

.amount-display {
  font-size: 32px;
  color: #409EFF;
  font-weight: bold;
  margin-bottom: 10px;
}

.currency {
  font-size: 24px;
  margin-right: 4px;
}

.order-details {
  color: #666;
  font-size: 14px;
}

.payment-methods {
  margin-top: 20px;
}

.method-title {
  font-size: 16px;
  margin-bottom: 15px;
  color: #333;
}

.method-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.method-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
}
</style> 