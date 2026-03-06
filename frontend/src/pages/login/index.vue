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
        <!-- 登录方式标题 -->
        <view class="login-tabs single-tab">
          <view class="tab-item active">
            <text>用户名登录</text>
            <view class="tab-indicator"></view>
          </view>
        </view>

        <!-- 用户名输入 -->
        <view class="form-item">
          <view class="input-wrapper">
            <text class="input-icon">👤</text>
            <input
              v-model="form.username"
              class="login-input"
              placeholder="请输入用户名"
              placeholder-class="input-placeholder"
              type="text"
            />
          </view>
        </view>

        <!-- 密码输入 -->
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
              <u-icon :name="passwordVisible ? 'eye-off' : 'eye'" size="20" color="#999"></u-icon>
            </view>
          </view>
        </view>

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

        <!-- 微信一键登录按钮 -->
        <button
          class="wx-login-btn"
          :loading="wxLoading"
          :disabled="wxLoading || loading"
          @click="handleWxLogin"
        >
          <text class="wx-icon">🌏</text>
          <text v-if="!wxLoading" class="btn-text">微信一键登录</text>
          <text v-else class="btn-text">登录中...</text>
        </button>
      </view>

      <!-- 注册链接 -->
      <view class="register-link">
        <text class="link-text" @click="goRegister">还没有账号？立即注册</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()
const loading = ref(false)
const wxLoading = ref(false)
const passwordVisible = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const togglePasswordVisible = () => {
  passwordVisible.value = !passwordVisible.value
}

const handleLogin = async () => {
  const username = form.username?.trim()
  const password = form.password?.trim()

  if (!username || !password) {
    uni.showToast({ title: '请填写完整信息', icon: 'none' })
    return
  }

  loading.value = true
  try {
    const loginData = {
      username: username,
      password: password
    }

    await userStore.login(loginData)
    uni.showToast({ title: '登录成功', icon: 'success' })

    setTimeout(() => {
      uni.reLaunch({ url: '/pages/home/index' })
    }, 500)
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

const forgotPassword = () => {
  uni.navigateTo({ url: '/pages/forgot-password/index' })
}

const goRegister = () => {
  uni.navigateTo({ url: '/pages/register/index' })
}

// 微信一键登录
const handleWxLogin = async () => {
  wxLoading.value = true
  
  try {
    // 调用 uni.login 获取微信 code
    const [loginErr, loginRes] = await uni.login({
      provider: 'weixin'
    })
    
    if (loginErr || !loginRes || !loginRes.code) {
      uni.showToast({ title: '微信授权失败', icon: 'none' })
      return
    }
    
    const code = loginRes.code
    console.log('[WxLogin] 获取 code 成功:', code)
    
    // 调用后端微信登录接口
    await userStore.wxLogin({ code })
    
    uni.showToast({ title: '登录成功', icon: 'success' })
    
    setTimeout(() => {
      uni.reLaunch({ url: '/pages/home/index' })
    }, 500)
  } catch (e) {
    console.error('微信登录失败:', e)
    uni.showToast({
      title: e.message || '微信登录失败，请重试',
      icon: 'none',
      duration: 2500
    })
  } finally {
    wxLoading.value = false
  }
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
  margin: 0 0 24rpx 0;
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

.wx-login-btn {
  width: 100%;
  height: 100rpx;
  background: linear-gradient(135deg, #07C160 0%, #05A350 100%);
  color: #fff;
  border-radius: 50rpx;
  font-size: 34rpx;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 12rpx 32rpx rgba(7, 193, 96, 0.3);
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  margin: 0;
  padding: 0;
  border: none;

  .wx-icon {
    font-size: 36rpx;
    margin-right: 12rpx;
  }

  .btn-text {
    color: #fff;
    font-size: 34rpx;
    font-weight: 600;
  }

  &:active {
    transform: translateY(2rpx) scale(0.98);
    box-shadow: 0 6rpx 20rpx rgba(7, 193, 96, 0.2);
  }

  &[disabled] {
    opacity: 0.7;
  }
}

.register-link {
  text-align: center;
  margin-top: auto;
  padding: 20rpx 0;

  .link-text {
    font-size: 28rpx;
    color: rgba(255, 255, 255, 0.9);
    font-weight: 500;
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
}
</style>
