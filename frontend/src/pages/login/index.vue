<template>
  <view class="login-page">
    <view class="login-bg">
      <view class="bg-circle circle-1"></view>
      <view class="bg-circle circle-2"></view>
      <view class="bg-circle circle-3"></view>
    </view>
    
    <view class="login-content">
      <view class="login-header">
        <view class="logo-wrapper">
          <view class="logo-svg">
            <svg viewBox="0 0 200 200" xmlns="http://www.w3.org/2000/svg">
              <circle cx="100" cy="100" r="90" fill="#6B8DD6"/>
              <polygon points="100,45 55,95 145,95" fill="white"/>
              <rect x="65" y="95" width="70" height="60" fill="white"/>
              <rect x="88" y="125" width="24" height="30" fill="#6B8DD6"/>
              <rect x="75" y="105" width="15" height="15" fill="#6B8DD6"/>
              <rect x="110" y="105" width="15" height="15" fill="#6B8DD6"/>
            </svg>
          </view>
        </view>
        <view class="title">欢迎回家</view>
        <view class="subtitle">记录美好生活的每一天</view>
      </view>
      
      <view class="login-form glass">
        <!-- 登录方式切换 -->
        <view class="login-tabs">
          <view 
            class="tab-item" 
            :class="{ active: loginType === 'password' }"
            @click="loginType = 'password'"
          >
            <text>密码登录</text>
            <view class="tab-indicator" v-if="loginType === 'password'"></view>
          </view>
          <view 
            class="tab-item" 
            :class="{ active: loginType === 'sms' }"
            @click="loginType = 'sms'"
          >
            <text>验证码登录</text>
            <view class="tab-indicator" v-if="loginType === 'sms'"></view>
          </view>
        </view>
        
        <!-- 密码登录 -->
        <template v-if="loginType === 'password'">
          <view class="form-item">
            <view class="input-wrapper">
              <text class="input-icon">👤</text>
              <input
                v-model="form.username"
                class="login-input"
                placeholder="请输入用户名/手机号"
                placeholder-class="input-placeholder"
                type="text"
              />
            </view>
          </view>
          
          <view class="form-item">
            <view class="input-wrapper password-wrapper">
              <text class="input-icon">🔒</text>
              <input
                v-model="form.password"
                class="login-input password-input"
                :type="passwordVisible ? 'text' : 'password'"
                placeholder="请输入密码"
                placeholder-class="input-placeholder"
              />
              <view class="password-toggle" @click="togglePasswordVisible">
                <up-icon :name="passwordVisible ? 'eye-off' : 'eye'" size="20" color="#999"></up-icon>
              </view>
            </view>
          </view>
        </template>
        
        <!-- 验证码登录 -->
        <template v-else>
          <view class="form-item">
            <view class="input-wrapper">
              <text class="input-icon">📱</text>
              <input
                v-model="form.phone"
                class="login-input"
                placeholder="请输入手机号"
                placeholder-class="input-placeholder"
                maxlength="11"
                type="number"
              />
            </view>
          </view>
          
          <view class="form-item code-item">
            <view class="input-wrapper code-wrapper">
              <text class="input-icon">🔢</text>
              <input
                v-model="form.code"
                class="login-input"
                placeholder="请输入验证码"
                placeholder-class="input-placeholder"
                maxlength="6"
                type="number"
              />
            </view>
            <view 
              class="code-btn" 
              :class="{ disabled: codeCountdown > 0 }"
              @click="sendCode"
            >
              {{ codeCountdown > 0 ? `${codeCountdown}s` : '获取验证码' }}
            </view>
          </view>
        </template>
        
        <view class="form-options">
          <text class="forgot" @click="forgotPassword">忘记密码？</text>
        </view>
        
        <button 
          class="login-btn" 
          :loading="loading"
          :disabled="loading"
          @click="handleLogin"
        >
          <text v-if="!loading" class="btn-text">登 录</text>
          <text v-else class="btn-text">登录中...</text>
        </button>
        
        <view class="tips-text">
          <text>请使用家人分享的邀请码登录</text>
        </view>
      </view>
      
      <!-- 微信登录 -->
      <view class="other-login">
        <view class="divider">
          <view class="line"></view>
          <text class="divider-text">其他登录方式</text>
          <view class="line"></view>
        </view>
        
        <view class="login-icons">
          <view class="icon-item" @click="wxLogin">
            <view class="icon wechat">
              <up-icon name="weixin-fill" size="44" color="#fff"></up-icon>
            </view>
            <text class="icon-text">微信</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '../../stores/user'
import { userApi } from '../../api/index'

const userStore = useUserStore()
const loginType = ref('password')
const loading = ref(false)
const codeCountdown = ref(0)
const passwordVisible = ref(false)

// 加载保存的账号密码
const loadSavedCredentials = () => {
  const savedUsername = uni.getStorageSync('savedUsername')
  const savedPhone = uni.getStorageSync('savedPhone')
  if (savedUsername) {
    form.username = savedUsername
  }
  if (savedPhone) {
    form.phone = savedPhone
  }
}

// 保存账号密码
const saveCredentials = () => {
  if (form.username) {
    uni.setStorageSync('savedUsername', form.username)
  }
  if (form.phone) {
    uni.setStorageSync('savedPhone', form.phone)
  }
}

onMounted(() => {
  loadSavedCredentials()
})

const togglePasswordVisible = () => {
  passwordVisible.value = !passwordVisible.value
}

const form = reactive({
  username: '',
  password: '',
  phone: '',
  code: ''
})

const handleLogin = async () => {
  if (loginType.value === 'password') {
    if (!form.username || !form.password) {
      uni.showToast({ title: '请填写完整信息', icon: 'none' })
      return
    }
  } else {
    if (!form.phone || !form.code) {
      uni.showToast({ title: '请填写完整信息', icon: 'none' })
      return
    }
  }
  
  loading.value = true
  try {
    const loginData = loginType.value === 'password' 
      ? { username: form.username, password: form.password, loginType: 'password' }
      : { phone: form.phone, code: form.code, loginType: 'sms' }
    
    await userStore.login(loginData)
    saveCredentials()
    uni.showToast({ title: '登录成功', icon: 'success' })

    setTimeout(() => {
      uni.switchTab({ url: '/pages/home/index' })
    }, 1500)
  } catch (e) {
    console.error('登录失败:', e)
    uni.showToast({ 
      title: e.message || '登录失败，请检查网络或账号密码', 
      icon: 'none',
      duration: 2500
    })
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

const forgotPassword = () => {
  uni.navigateTo({ url: '/pages/forgot-password/index' })
}

const wxLogin = () => {
  uni.showToast({ title: '微信登录开发中', icon: 'none' })
}
</script>

<style lang="scss" scoped>
/* H5 兼容性重置 */
page {
  min-height: 100vh;
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

.login-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #6B8DD6 0%, #8B5CF6 50%, #A78BFA 100%);
  position: relative;
  overflow: hidden;
}

/* 动态背景 */
.login-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  overflow: hidden;
  
  .bg-circle {
    position: absolute;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.08);
    animation: float 8s ease-in-out infinite;
    
    &.circle-1 {
      width: 600rpx;
      height: 600rpx;
      top: -200rpx;
      right: -150rpx;
      animation-delay: 0s;
    }
    
    &.circle-2 {
      width: 400rpx;
      height: 400rpx;
      bottom: 100rpx;
      left: -100rpx;
      animation-delay: 2s;
    }
    
    &.circle-3 {
      width: 300rpx;
      height: 300rpx;
      bottom: -50rpx;
      right: 50rpx;
      animation-delay: 4s;
    }
  }
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) scale(1);
  }
  50% {
    transform: translateY(-30rpx) scale(1.05);
  }
}

.login-content {
  position: relative;
  z-index: 1;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  padding: 60rpx 40rpx 40rpx;
  box-sizing: border-box;
}

.login-header {
  text-align: center;
  margin-bottom: 50rpx;
  flex-shrink: 0;
  
  .logo-wrapper {
    display: inline-block;
    background: rgba(255, 255, 255, 0.95);
    border-radius: 32rpx;
    padding: 20rpx;
    margin-bottom: 30rpx;
    box-shadow: 0 16rpx 40rpx rgba(0, 0, 0, 0.15);
    animation: scaleIn 0.6s cubic-bezier(0.4, 0, 0.2, 1);
    
    .logo-svg {
      width: 140rpx;
      height: 140rpx;
      display: block;
    }
  }
  
  .title {
    font-size: 48rpx;
    font-weight: 700;
    color: #fff;
    margin-bottom: 16rpx;
    line-height: 1.4;
    text-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.15);
    animation: fadeIn 0.8s ease-out;
  }
  
  .subtitle {
    font-size: 28rpx;
    color: rgba(255, 255, 255, 0.9);
    line-height: 1.5;
    animation: fadeIn 1s ease-out;
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes scaleIn {
  from {
    opacity: 0;
    transform: scale(0.8);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.login-form {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 40rpx;
  padding: 40rpx 36rpx;
  margin-bottom: 30rpx;
  flex-shrink: 0;
  box-shadow: 0 20rpx 60rpx rgba(0, 0, 0, 0.2);
  backdrop-filter: blur(20rpx);
  animation: slideUp 0.7s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(60rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.login-tabs {
  display: flex;
  margin-bottom: 40rpx;
  position: relative;
  
  .tab-item {
    flex: 1;
    text-align: center;
    padding: 24rpx 0;
    font-size: 30rpx;
    color: #8b9aad;
    position: relative;
    transition: all 0.3s ease;
    font-weight: 500;
    
    &.active {
      color: #6B8DD6;
      font-weight: 600;
      
      .tab-indicator {
        transform: scaleX(1);
      }
    }
    
    .tab-indicator {
      position: absolute;
      bottom: 0;
      left: 50%;
      transform: translateX(-50%) scaleX(0);
      width: 60rpx;
      height: 6rpx;
      background: linear-gradient(90deg, #6B8DD6, #8B5CF6);
      border-radius: 3rpx;
      transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    }
  }
}

.form-item {
  margin-bottom: 28rpx;
}

.input-wrapper {
  display: flex;
  align-items: center;
  background: #f8f9fc;
  border-radius: 20rpx;
  padding: 0 24rpx;
  height: 100rpx;
  border: 2rpx solid transparent;
  transition: all 0.25s ease;
  
  &:focus-within {
    background: #fff;
    border-color: #6B8DD6;
    box-shadow: 0 0 0 6rpx rgba(107, 141, 214, 0.1);
  }
  
  .input-icon {
    font-size: 32rpx;
    margin-right: 20rpx;
    opacity: 0.7;
  }
  
  .login-input {
    flex: 1;
    height: 100%;
    font-size: 30rpx;
    color: #2d3748;
    background: transparent;
    
    /* #ifdef H5 */
    line-height: normal;
    /* #endif */
  }
  
  .input-placeholder {
    color: #a0aec0;
    font-size: 28rpx;
  }
  
  .password-toggle {
    padding: 16rpx;
    margin-right: -8rpx;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.2s ease;
    
    &:active {
      background: rgba(0, 0, 0, 0.05);
    }
  }
}

.code-item {
  display: flex;
  align-items: center;
  gap: 20rpx;
  
  .code-wrapper {
    flex: 1;
  }
}

.code-btn {
  padding: 0 32rpx;
  height: 100rpx;
  background: linear-gradient(135deg, #6B8DD6 0%, #8B5CF6 100%);
  color: #fff;
  border-radius: 20rpx;
  font-size: 26rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  white-space: nowrap;
  flex-shrink: 0;
  font-weight: 600;
  box-shadow: 0 8rpx 24rpx rgba(107, 141, 214, 0.35);
  transition: all 0.25s ease;
  
  &.disabled {
    background: #cbd5e0;
    box-shadow: none;
    opacity: 0.7;
  }
  
  &:active:not(.disabled) {
    transform: scale(0.96);
    box-shadow: 0 4rpx 12rpx rgba(107, 141, 214, 0.25);
  }
}

.form-options {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 36rpx;
  
  .forgot {
    font-size: 26rpx;
    color: #6B8DD6;
    padding: 8rpx 0;
    font-weight: 500;
    transition: all 0.2s ease;
    
    &:active {
      opacity: 0.7;
    }
  }
}

.login-btn {
  width: 100%;
  height: 100rpx;
  background: linear-gradient(135deg, #6B8DD6 0%, #8B5CF6 100%);
  color: #fff;
  border-radius: 50rpx;
  font-size: 34rpx;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 12rpx 32rpx rgba(107, 141, 214, 0.4);
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  margin: 0;
  padding: 0;
  border: none;
  
  .btn-text {
    color: #fff;
    font-size: 34rpx;
    font-weight: 600;
  }
  
  &:active {
    transform: translateY(2rpx) scale(0.98);
    box-shadow: 0 6rpx 20rpx rgba(107, 141, 214, 0.3);
  }
  
  &[disabled] {
    opacity: 0.7;
  }
}

.tips-text {
  text-align: center;
  margin-top: 32rpx;
  padding: 16rpx 0;
  
  text {
    font-size: 24rpx;
    color: #999;
  }
}

.other-login {
  flex-shrink: 0;
  margin-top: auto;
  padding-bottom: 20rpx;
  animation: fadeIn 1.2s ease-out;
  
  .divider {
    display: flex;
    align-items: center;
    margin-bottom: 32rpx;
    
    .line {
      flex: 1;
      height: 2rpx;
      background: rgba(255, 255, 255, 0.3);
    }
    
    .divider-text {
      padding: 0 24rpx;
      font-size: 24rpx;
      color: rgba(255, 255, 255, 0.8);
    }
  }
}

.login-icons {
  display: flex;
  justify-content: center;
  
  .icon-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 16rpx;
    transition: all 0.25s ease;
    
    &:active {
      transform: scale(0.92);
    }
    
    .icon {
      width: 100rpx;
      height: 100rpx;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 16rpx;
      box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.15);
      transition: all 0.3s ease;
      
      &.wechat {
        background: linear-gradient(135deg, #07c160 0%, #05a350 100%);
      }
    }
    
    .icon-text {
      font-size: 24rpx;
      color: #fff;
      font-weight: 500;
    }
  }
}

/* 响应式适配 - 小屏幕手机 */
@media screen and (max-height: 700px) {
  .login-header {
    margin-bottom: 30rpx;
    
    .logo-wrapper {
      padding: 16rpx;
      margin-bottom: 20rpx;
      
      .logo-svg {
        width: 120rpx;
        height: 120rpx;
      }
    }
    
    .title {
      font-size: 42rpx;
    }
    
    .subtitle {
      font-size: 26rpx;
    }
  }
  
  .login-form {
    padding: 32rpx 28rpx;
    border-radius: 32rpx;
  }
  
  .login-tabs {
    margin-bottom: 28rpx;
    
    .tab-item {
      padding: 20rpx 0;
      font-size: 28rpx;
    }
  }
  
  .input-wrapper {
    height: 90rpx;
  }
  
  .code-btn {
    height: 90rpx;
  }
  
  .login-btn {
    height: 92rpx;
  }
}

/* 大屏适配 */
@media screen and (min-width: 768px) {
  .login-content {
    padding: 80rpx 100rpx;
    align-items: center;
    justify-content: center;
  }
  
  .login-form {
    max-width: 560rpx;
    width: 100%;
    padding: 48rpx 44rpx;
  }
  
  .login-header {
    max-width: 560rpx;
    width: 100%;
  }
  
  .other-login {
    max-width: 560rpx;
    width: 100%;
  }
}
</style>