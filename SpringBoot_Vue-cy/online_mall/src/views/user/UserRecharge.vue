<template>
  <el-card class="box-card">
    <ul class="msg-box">
      <li>
        <h4>我要充值</h4>
      </li>
      <li>
        <h4 style="margin-bottom: 15px;">充值金额</h4>
        <el-radio-group v-model="amountVal" @change="amountChange">
          <el-radio border :label="''+ 100">充值100</el-radio>
          <el-radio border :label="''+ 500">充值500</el-radio>
          <el-radio border :label="''+ 1000">充值1000</el-radio>
          <el-radio border :label="''+ 2000">充值2000</el-radio>
          <el-radio border :label="''+ 5000">充值5000</el-radio>
          <el-radio border :label="''">自定义</el-radio>
        </el-radio-group>
      </li>
      <li>
        <h4 style="margin-bottom: 15px;">支付方式</h4>
        <el-radio-group v-model="rechargeParams.paymentType" @change="paymentTypeChange">
          <el-radio border :label="''+ 1">支付宝</el-radio>
          <el-radio border :label="''+ 0">微信</el-radio>
        </el-radio-group>
      </li>
      <li>
        <h4 style="margin-bottom: 15px;">支付金额</h4>
        <el-input :disabled="disabled" clearable v-model="rechargeParams.totalAmt" placeholder="请输入金额" style="width: 150px;"></el-input>
      </li>
    </ul>
    <div style="text-align: center; margin-top: 30px;">
      <el-button type="primary" @click="surePay">确认支付</el-button>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'

const amountVal = ref('')
const disabled = ref(false)
const returnUrl = 'http://localhost:5173/#/user/orders' // 支付完成后跳转地址

const rechargeParams = reactive({
  totalAmt: '', // 金额
  paymentType: '0' // 支付方式[0:微信,1:支付宝]
})

// 充值金额选择
const amountChange = (val: string) => {
  rechargeParams.totalAmt = val
  disabled.value = val !== ''
}

// 支付方式选择
const paymentTypeChange = (val: string) => {
  rechargeParams.paymentType = val
}

// 生成订单号
const getOrderNo = () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  const random = Math.floor(Math.random() * 10000)
  return `${year}${month}${day}${random}`
}

// 确认支付
const surePay = async () => {
  if (!rechargeParams.totalAmt) {
    ElMessage.warning('请输入金额')
    return
  }

  if (rechargeParams.paymentType === '0') {
    ElMessage.warning('微信支付功能暂未开放')
    return
  }

  try {
    const response = await fetch('/api/pay/create', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        outTradeNo: getOrderNo(),
        totalAmount: rechargeParams.totalAmt,
        subject: '账户充值',
        returnUrl: returnUrl
      })
    })

    if (!response.ok) {
      throw new Error('支付创建失败')
    }

    const result = await response.json()
    
    if (result.code === 200) {
      // 创建临时div放置支付表单
      const payDiv = document.getElementById('payDiv')
      if (payDiv) {
        document.body.removeChild(payDiv)
      }
      const div = document.createElement('div')
      div.id = 'payDiv'
      div.innerHTML = result.data
      document.body.appendChild(div)
      
      // 提交表单，在新窗口打开支付页面
      const form = document.getElementById('payDiv')?.getElementsByTagName('form')[0]
      if (form) {
        form.setAttribute('target', '_blank')
        form.submit()
        
        ElMessage({
          message: '已打开支付页面，请在新窗口完成支付',
          type: 'success',
          duration: 5000
        })
      }
    } else {
      throw new Error(result.message || '支付创建失败')
    }
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '支付失败')
  }
}
</script>

<style scoped>
.msg-box > li {
  list-style: none;
  border-bottom: 1px solid #c5c5c5;
  padding: 20px 10px;
}

.el-radio-group {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.el-radio {
  margin-right: 0;
}
</style> 