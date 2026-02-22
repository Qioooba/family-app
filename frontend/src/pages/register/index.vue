<template>
  <view class="register-page">
    <view class="register-header">
      <image class="logo" src="/static/logo.png" mode="aspectFit" />
      <view class="title">创建账号</view>
      <view class="subtitle">加入我们的家庭小助手</view>
    </view>
    
    <view class="register-form">
      <view class="form-item">
        <text class="input-label">用户名</text>
        <view class="input-wrapper">
          <text class="input-icon">👤</text>
          <input
            v-model="form.username"
            class="register-input"
            placeholder="请输入用户名"
            placeholder-class="input-placeholder"
            maxlength="20"
            type="text"
          />
        </view>
      </view>
      
      <view class="form-item">
        <text class="input-label">昵称</text>
        <view class="input-wrapper">
          <text class="input-icon">😊</text>
          <input
            v-model="form.nickname"
            class="register-input"
            placeholder="请输入昵称"
            placeholder-class="input-placeholder"
            maxlength="20"
            type="text"
          />
        </view>
      </view>
      
      <view class="form-item">
        <text class="input-label">手机号</text>
        <view class="input-wrapper">
          <text class="input-icon">📱</text>
          <input
            v-model="form.phone"
            class="register-input"
            placeholder="请输入手机号"
            placeholder-class="input-placeholder"
            maxlength="11"
            type="number"
          />
        </view>
      </view>
      
      <view class="form-item code-item">
        <view class="code-input-wrapper">
          <text class="input-label">验证码</text>
          <view class="input-wrapper">
            <text class="input-icon">✉️</text>
            <input
              v-model="form.code"
              class="register-input"
              placeholder="请输入验证码"
              placeholder-class="input-placeholder"
              maxlength="6"
              type="number"
            />
          </view>
        </view>
        <view 
          class="code-btn" 
          :class="{ disabled: codeCountdown > 0 }"
          @click="sendCode"
        >
          {{ codeCountdown > 0 ? `${codeCountdown}s` : '获取验证码' }}
        </view>
      </view>
      
      <view class="form-item">
        <text class="input-label">密码</text>
        <view class="input-wrapper">
          <text class="input-icon">🔒</text>
          <input
            v-model="form.password"
            class="register-input"
            password
            placeholder="请设置密码（6-20位）"
            placeholder-class="input-placeholder"
            maxlength="20"
          />
        </view>
      </view>
      
      <view class="form-item">
        <text class="input-label">确认密码</text>
        <view class="input-wrapper">
          <text class="input-icon">🔐</text>
          <input
            v-model="form.confirmPassword"
            class="register-input"
            password
            placeholder="请确认密码"
            placeholder-class="input-placeholder"
            maxlength="20"
          />
        </view>
      </view>
      
      <view class="agreement">
        <view class="checkbox-wrapper" @click="agreed = !agreed">
          <view class="checkbox" :class="{ checked: agreed }">
            <text v-if="agreed" class="check-icon">✓</text>
          </view>
          <text class="agreement-text">
            我已阅读并同意<text class="link" @click.stop="showAgreement">《用户协议》</text>和<text class="link" @click.stop="showPrivacy">《隐私政策》</text>
          </text>
        </view>
      </view>
      
      <button 
        class="register-btn" 
        :loading="loading"
        :disabled="loading"
        @click="handleRegister"
      >
        <text v-if="!loading" class="btn-text">注 册</text>
        <text v-else class="btn-text">注册中...</text>
      </button>
      
      <view class="login-link">
        <text class="link-text">已有账号？</text>
        <text class="link-action" @click="goLogin">立即登录</text>
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
/* H5 兼容性重置 */
page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

/* #ifdef H5 */
input, button {
  appearance: none;
  -webkit-appearance: none;
  outline: none;
  border: none;
  background: transparent;
}

button::after {
  border: none;
}
/* #endif */

.register-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40rpx;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
}

.register-header {
  text-align: center;
  margin-bottom: 40rpx;
  flex-shrink: 0;
  
  .logo {
    width: 140rpx;
    height: 140rpx;
    margin-bottom: 20rpx;
    background: #fff;
    border-radius: 32rpx;
    padding: 16rpx;
  }
  
  .title {
    font-size: 44rpx;
    font-weight: bold;
    color: #fff;
    margin-bottom: 12rpx;
    line-height: 1.4;
  }
  
  .subtitle {
    font-size: 26rpx;
    color: rgba(255,255,255,0.85);
    line-height: 1.4;
  }
}

.register-form {
  background: #fff;
  border-radius: 24rpx;
  padding: 32rpx;
  flex-shrink: 0;
  box-shadow: 0 8rpx 32rpx rgba(0,0,0,0.15);
}

.form-item {
  margin-bottom: 20rpx;
}

.input-label {
  display: block;
  font-size: 26rpx;
  color: #333;
  margin-bottom: 8rpx;
  font-weight: 500;
}

.input-wrapper {
  display: flex;
  align-items: center;
  height: 84rpx;
  padding: 0 20rpx;
  background: #f8f9fa;
  border-radius: 12rpx;
  border: 2rpx solid #e8e8e8;
  
  .input-icon {
    font-size: 32rpx;
    margin-right: 16rpx;
    flex-shrink: 0;
  }
  
  .register-input {
    flex: 1;
    height: 100%;
    font-size: 28rpx;
    color: #333;
    background: transparent;
    border: none;
    
    /* #ifdef H5 */
    line-height: normal;
    /* #endif */
  }
  
  &:focus-within {
    border-color: #5B8FF9;
    background: #fff;
  }
}

.input-placeholder {
  color: #bbb;
  font-size: 28rpx;
}

.code-item {
  display: flex;
  align-items: flex-end;
  gap: 16rpx;
  
  .code-input-wrapper {
    flex: 1;
  }
}

.code-btn {
  padding: 0 24rpx;
  height: 84rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border-radius: 12rpx;
  font-size: 26rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  white-space: nowrap;
  flex-shrink: 0;
  font-weight: 500;
  box-shadow: 0 4rpx 12rpx rgba(102, 126, 234, 0.3);
  
  &.disabled {
    background: #ccc;
    box-shadow: none;
  }
  
  &:active:not(.disabled) {
    opacity: 0.9;
    transform: translateY(2rpx);
  }
}

.agreement {
  margin: 24rpx 0 32rpx;
  
  .checkbox-wrapper {
    display: flex;
    align-items: flex-start;
    gap: 12rpx;
    
    .checkbox {
      width: 32rpx;
      height: 32rpx;
      border-radius: 50%;
      border: 2rpx solid #ddd;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
      margin-top: 2rpx;
      
      &.checked {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        border-color: transparent;
      }
      
      .check-icon {
        color: #fff;
        font-size: 20rpx;
        font-weight: bold;
      }
    }
    
    .agreement-text {
      font-size: 24rpx;
      color: #666;
      line-height: 1.5;
    }
    
    .link {
      color: #5B8FF9;
    }
  }
}

/* 注册按钮 - 优化样式 */
.register-btn {
  width: 100%;
  height: 92rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border-radius: 46rpx;
  font-size: 32rpx;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 24rpx rgba(102, 126, 234, 0.4);
  transition: all 0.3s ease;
  margin: 0;
  padding: 0;
  
  .btn-text {
    color: #fff;
    font-size: 32rpx;
    font-weight: 600;
  }
  
  &:active {
    transform: translateY(2rpx);
    box-shadow: 0 4rpx 16rpx rgba(102, 126, 234, 0.3);
  }
  
  &[disabled] {
    opacity: 0.7;
  }
}

.login-link {
  text-align: center;
  margin-top: 24rpx;
  padding: 12rpx 0;
  
  .link-text {
    font-size: 26rpx;
    color: #999;
  }
  
  .link-action {
    font-size: 26rpx;
    color: #5B8FF9;
    font-weight: 500;
    margin-left: 8rpx;
  }
}

/* 响应式适配 - 小屏幕手机 */
@media screen and (max-height: 700px) {
  .register-header {
    margin-bottom: 24rpx;
    
    .logo {
      width: 100rpx;
      height: 100rpx;
    }
    
    .title {
      font-size: 36rpx;
    }
    
    .subtitle {
      font-size: 22rpx;
    }
  }
  
  .register-form {
    padding: 24rpx;
  }
  
  .form-item {
    margin-bottom: 16rpx;
  }
  
  .input-wrapper {
    height: 76rpx;
  }
  
  .code-btn {
    height: 76rpx;
  }
  
  .register-btn {
    height: 84rpx;
  }
}

/* 大屏适配 */
@media screen and (min-width: 768px) {
  .register-page {
    padding: 60rpx 80rpx;
    align-items: center;
    justify-content: center;
  }
  
  .register-form {
    max-width: 600rpx;
    width: 100%;
    padding: 40rpx;
  }
  
  .register-header {
    max-width: 600rpx;
    width: 100%;
  }
}
</style>