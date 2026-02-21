<template>
  <view class="login-page">
    <view class="login-header">
      <image class="logo" src="/static/logo.png" mode="aspectFit" />
      <view class="title">欢迎回家</view>
      <view class="subtitle">记录美好生活的每一天</view>
    </view>
    
    <view class="login-form">
      <!-- 登录方式切换 -->
      <view class="login-tabs">
        <view 
          class="tab-item" 
          :class="{ active: loginType === 'password' }"
          @click="loginType = 'password'"
        >
          密码登录
        </view>
        <view 
          class="tab-item" 
          :class="{ active: loginType === 'sms' }"
          @click="loginType = 'sms'"
        >
          验证码登录
        </view>
      </view>
      
      <!-- 密码登录 -->
      <template v-if="loginType === 'password'">
        <view class="form-item">
          <u-input
            v-model="form.username"
            placeholder="请输入用户名/手机号"
            prefixIcon="account"
            :prefixIconStyle="{ color: '#999' }"
          />
        </view>
        
        <view class="form-item">
          <u-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            prefixIcon="lock"
            :prefixIconStyle="{ color: '#999' }"
          />
        </view>
      </template>
      
      <!-- 验证码登录 -->
      <template v-else>
        <view class="form-item">
          <u-input
            v-model="form.phone"
            placeholder="请输入手机号"
            prefixIcon="phone"
            :prefixIconStyle="{ color: '#999' }"
            maxlength="11"
          />
        </view>
        
        <view class="form-item code-item">
          <u-input
            v-model="form.code"
            placeholder="请输入验证码"
            prefixIcon="email"
            :prefixIconStyle="{ color: '#999' }"
            maxlength="6"
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
      </template>
      
      <view class="form-options">
        <text class="forgot" @click="forgotPassword">忘记密码？</text>
      </view>
      
      <u-button 
        type="primary" 
        size="large" 
        :loading="loading"
        @click="handleLogin"
      >
        登录
      </u-button>
      
      <view class="register-link">
        还没有账号？<text @click="goRegister">立即注册</text>
      </view>
    </view>
    
    <!-- 微信登录 -->
    <view class="other-login">
      <view class="divider">
        <view class="line"></view>
        <text>其他登录方式</text>
        <view class="line"></view>
      </view>
      
      <view class="login-icons">
        <view class="icon-item" @click="wxLogin">
          <view class="icon wechat">
            <u-icon name="weixin-fill" size="40" color="#fff"></u-icon>
          </view>
          <text>微信</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()
const loginType = ref('password')
const loading = ref(false)
const codeCountdown = ref(0)

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
      ? { username: form.username, password: form.password }
      : { phone: form.phone, code: form.code }
    
    await userStore.login(loginData)
    uni.showToast({ title: '登录成功', icon: 'success' })
    
    setTimeout(() => {
      uni.switchTab({ url: '/pages/home/index' })
    }, 1500)
  } catch (e) {
    console.error(e)
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
  
  // TODO: 调用发送验证码接口
  uni.showToast({ title: '验证码已发送', icon: 'success' })
  
  codeCountdown.value = 60
  const timer = setInterval(() => {
    codeCountdown.value--
    if (codeCountdown.value <= 0) {
      clearInterval(timer)
    }
  }, 1000)
}

const goRegister = () => {
  uni.navigateTo({ url: '/pages/register/index' })
}

const forgotPassword = () => {
  uni.navigateTo({ url: '/pages/forgot-password/index' })
}

const wxLogin = () => {
  uni.showToast({ title: '微信登录开发中', icon: 'none' })
}
</script>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 60rpx 40rpx;
}

.login-header {
  text-align: center;
  margin-bottom: 80rpx;
  
  .logo {
    width: 180rpx;
    height: 180rpx;
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

.login-form {
  background: #fff;
  border-radius: 24rpx;
  padding: 40rpx;
  margin-bottom: 40rpx;
}

.login-tabs {
  display: flex;
  margin-bottom: 40rpx;
  border-bottom: 2rpx solid #f0f0f0;
  
  .tab-item {
    flex: 1;
    text-align: center;
    padding: 24rpx 0;
    font-size: 30rpx;
    color: #999;
    position: relative;
    
    &.active {
      color: #5B8FF9;
      font-weight: 500;
      
      &::after {
        content: '';
        position: absolute;
        bottom: -2rpx;
        left: 50%;
        transform: translateX(-50%);
        width: 60rpx;
        height: 4rpx;
        background: #5B8FF9;
        border-radius: 2rpx;
      }
    }
  }
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

.form-options {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 40rpx;
  
  .forgot {
    font-size: 26rpx;
    color: #999;
  }
}

.register-link {
  text-align: center;
  margin-top: 30rpx;
  font-size: 26rpx;
  color: #999;
  
  text {
    color: #5B8FF9;
    margin-left: 8rpx;
  }
}

.other-login {
  .divider {
    display: flex;
    align-items: center;
    margin-bottom: 40rpx;
    
    .line {
      flex: 1;
      height: 2rpx;
      background: rgba(255,255,255,0.3);
    }
    
    text {
      padding: 0 30rpx;
      font-size: 24rpx;
      color: rgba(255,255,255,0.8);
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
    
    .icon {
      width: 90rpx;
      height: 90rpx;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 12rpx;
      
      &.wechat {
        background: #07c160;
      }
    }
    
    text {
      font-size: 24rpx;
      color: #fff;
    }
  }
}
</style>
