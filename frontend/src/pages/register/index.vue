<template>
  <view class="register-page">
    <view class="register-header">
      <image class="logo" src="/static/logo.png" mode="aspectFit" />
      <view class="title">创建账号</view>
      <view class="subtitle">加入我们的家庭小助手</view>
    </view>
    
    <view class="register-form">
      <view class="form-item">
        <u-input
          v-model="form.username"
          placeholder="请输入用户名"
          prefixIcon="account"
          :prefixIconStyle="{ color: '#999' }"
          maxlength="20"
        />
      </view>
      
      <view class="form-item">
        <u-input
          v-model="form.nickname"
          placeholder="请输入昵称"
          prefixIcon="man-add"
          :prefixIconStyle="{ color: '#999' }"
          maxlength="20"
        />
      </view>
      
      <view class="form-item">
        <u-input
          v-model="form.phone"
          placeholder="请输入手机号"
          prefixIcon="phone"
          :prefixIconStyle="{ color: '#999' }"
          maxlength="11"
          type="number"
        />
      </view>
      
      <view class="form-item code-item">
        <u-input
          v-model="form.code"
          placeholder="请输入验证码"
          prefixIcon="email"
          :prefixIconStyle="{ color: '#999' }"
          maxlength="6"
          type="number"
        >
          <template #suffix>
            <view 
              class="code-btn" 
              :class="{ disabled: codeCountdown > 0 }"
              @click="sendCode"
            >
              {{ codeCountdown > 0 ? `${codeCountdown}s` : '获取验证码' }}
            </view>
          </template>
        </u-input>
      </view>
      
      <view class="form-item">
        <u-input
          v-model="form.password"
          type="password"
          placeholder="请设置密码（6-20位）"
          prefixIcon="lock"
          :prefixIconStyle="{ color: '#999' }"
          maxlength="20"
        />
      </view>
      
      <view class="form-item">
        <u-input
          v-model="form.confirmPassword"
          type="password"
          placeholder="请确认密码"
          prefixIcon="lock-fill"
          :prefixIconStyle="{ color: '#999' }"
          maxlength="20"
        />
      </view>
      
      <view class="agreement">
        <u-checkbox v-model="agreed" shape="circle" size="14">
          <text class="agreement-text">
            我已阅读并同意<text class="link" @click.stop="showAgreement">《用户协议》</text>和<text class="link" @click.stop="showPrivacy">《隐私政策》</text>
          </text>
        </u-checkbox>
      </view>
      
      <u-button 
        type="primary" 
        size="large" 
        :loading="loading"
        @click="handleRegister"
      >
        注册
      </u-button>
      
      <view class="login-link">
        已有账号？<text @click="goLogin">立即登录</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useUserStore } from '../../stores/user'
import { userApi } from '../../api/index'

const userStore = useUserStore()
const loading = ref(false)
const codeCountdown = ref(0)
const agreed = ref(false)

const form = reactive({
  username: '',
  nickname: '',
  phone: '',
  code: '',
  password: '',
  confirmPassword: ''
})

// 表单验证
const validateForm = () => {
  if (!form.username.trim()) {
    uni.showToast({ title: '请输入用户名', icon: 'none' })
    return false
  }
  if (!form.nickname.trim()) {
    uni.showToast({ title: '请输入昵称', icon: 'none' })
    return false
  }
  if (!form.phone || form.phone.length !== 11) {
    uni.showToast({ title: '请输入正确手机号', icon: 'none' })
    return false
  }
  if (!form.code || form.code.length !== 6) {
    uni.showToast({ title: '请输入6位验证码', icon: 'none' })
    return false
  }
  if (!form.password || form.password.length < 6) {
    uni.showToast({ title: '密码至少6位', icon: 'none' })
    return false
  }
  if (form.password !== form.confirmPassword) {
    uni.showToast({ title: '两次密码不一致', icon: 'none' })
    return false
  }
  if (!agreed.value) {
    uni.showToast({ title: '请同意用户协议', icon: 'none' })
    return false
  }
  return true
}

const handleRegister = async () => {
  if (!validateForm()) return
  
  loading.value = true
  try {
    const registerData = {
      username: form.username,
      nickname: form.nickname,
      phone: form.phone,
      code: form.code,
      password: form.password
    }
    
    await userStore.register(registerData)
    uni.showToast({ title: '注册成功', icon: 'success' })
    
    // 注册成功后自动登录
    setTimeout(async () => {
      try {
        await userStore.login({
          username: form.username,
          password: form.password
        })
        uni.switchTab({ url: '/pages/home/index' })
      } catch (e) {
        // 自动登录失败，跳转到登录页
        uni.navigateTo({ url: '/pages/login/index' })
      }
    }, 1500)
  } catch (e) {
    console.error('注册失败:', e)
  } finally {
    loading.value = false
  }
}

const sendCode = async () => {
  if (codeCountdown.value > 0) return
  if (!form.phone || form.phone.length !== 11) {
    uni.showToast({ title: '请输入正确手机号', icon: 'none' })
    return
  }
  
  try {
    await userApi.sendSmsCode(form.phone)
    uni.showToast({ title: '验证码已发送', icon: 'success' })
    
    codeCountdown.value = 60
    const timer = setInterval(() => {
      codeCountdown.value--
      if (codeCountdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (e) {
    uni.showToast({ title: e.message || '发送失败', icon: 'none' })
  }
}

const goLogin = () => {
  uni.navigateBack()
}

const showAgreement = () => {
  uni.showModal({
    title: '用户协议',
    content: '这里是用户协议内容...',
    showCancel: false
  })
}

const showPrivacy = () => {
  uni.showModal({
    title: '隐私政策',
    content: '这里是隐私政策内容...',
    showCancel: false
  })
}
</script>

<style lang="scss" scoped>
.register-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 60rpx 40rpx;
}

.register-header {
  text-align: center;
  margin-bottom: 60rpx;
  
  .logo {
    width: 160rpx;
    height: 160rpx;
    margin-bottom: 30rpx;
    background: #fff;
    border-radius: 40rpx;
    padding: 20rpx;
  }
  
  .title {
    font-size: 48rpx;
    font-weight: bold;
    color: #fff;
    margin-bottom: 16rpx;
  }
  
  .subtitle {
    font-size: 28rpx;
    color: rgba(255,255,255,0.8);
  }
}

.register-form {
  background: #fff;
  border-radius: 24rpx;
  padding: 40rpx;
}

.form-item {
  margin-bottom: 30rpx;
}

.code-btn {
  padding: 12rpx 24rpx;
  background: #5B8FF9;
  color: #fff;
  border-radius: 8rpx;
  font-size: 24rpx;
  
  &.disabled {
    background: #ccc;
  }
}

.agreement {
  margin-bottom: 40rpx;
  
  .agreement-text {
    font-size: 24rpx;
    color: #999;
  }
  
  .link {
    color: #5B8FF9;
  }
}

.login-link {
  text-align: center;
  margin-top: 30rpx;
  font-size: 26rpx;
  color: #999;
  
  text {
    color: #5B8FF9;
    margin-left: 8rpx;
  }
}
</style>
