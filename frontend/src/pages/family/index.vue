<template>
  <view class="family-page">
    <!-- 顶部渐变背景 -->
    <view class="header-bg">
      <view class="bg-pattern"></view>
    </view>
    
    <!-- 下拉刷新 -->
    <scroll-view 
      scroll-y 
      class="scroll-container"
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >
      <!-- 家庭信息卡 -->
      <view class="family-header">
        <view class="family-main">
          <view class="family-avatar-wrapper">
            <image class="family-avatar" src="/static/family-avatar.jpg" />
            <view class="family-badge">
              <text>{{ members.length }}</text>
            </view>
          </view>
          <view class="family-info">
            <text class="family-name">{{ family.name || '幸福小家' }}</text>
            <text class="family-desc">{{ members.length }} 位家庭成员</text>
          </view>
        </view>
        <view class="header-actions">
          <view class="action-btn invite" @click="showInviteModal" v-if="isAdmin">
            <text class="btn-icon">+</text>
            <text class="btn-text">邀请</text>
          </view>
        </view>
      </view>
      
      <!-- 成员头像墙 - 水平滚动 -->
      <view class="members-wall">
        <scroll-view scroll-x class="members-scroll" enhanced :show-scrollbar="false">
          <view class="members-list">
            <view 
              v-for="(member, index) in members" 
              :key="member.id"
              class="member-item"
              :style="{ animationDelay: `${index * 0.05}s` }"
            >
              <view class="avatar-wrapper">
                <image class="member-avatar" :src="member.avatar" />
                <view v-if="member.role === 'owner'" class="role-badge owner">👑</view>
                <view v-else-if="member.role === 'admin'" class="role-badge admin">⭐</view>
              </view>
              <text class="member-name">{{ member.nickname }}</text>
            </view>
          </view>
        </scroll-view>
      </view>
      
      <!-- 家庭功能入口 - 重新设计 -->
      <view class="features-section">
        <view class="section-title">
          <text class="title-icon">🏠</text>
          <text class="title-text">家庭空间</text>
        </view>
        
        <view class="features-grid">
          <view 
            v-for="(feature, index) in features" 
            :key="index"
            class="feature-card"
            :class="`feature-${index}`"
            @click="goFeature(feature)"
            :style="{ animationDelay: `${index * 0.08}s` }"
          >
            <view class="feature-icon-wrapper" :style="{ background: feature.bgColor }">
              <u-icon :name="feature.icon" size="40" color="#fff"></u-icon>
            </view>
            <view class="feature-info">
              <text class="feature-name">{{ feature.name }}</text>
              <text class="feature-desc">{{ feature.desc }}</text>
            </view>
            <view class="feature-arrow">›</view>
          </view>
        </view>
      </view>
      
      <!-- 家庭统计数据 - 卡片式设计 -->
      <view class="stats-section">
        <view class="section-title">
          <text class="title-icon">📊</text>
          <text class="title-text">本月统计</text>
        </view>
        
        <view class="stats-grid">
          <view class="stat-item tasks">
            <view class="stat-icon">✅</view>
            <view class="stat-content">
              <text class="stat-num">{{ stats.tasksCompleted }}</text>
              <text class="stat-label">任务完成</text>
            </view>
          </view>
          
          <view class="stat-item wishes">
            <view class="stat-icon">💝</view>
            <view class="stat-content">
              <text class="stat-num">{{ stats.wishesCompleted }}</text>
              <text class="stat-label">心愿实现</text>
            </view>
          </view>
          
          <view class="stat-item meals">
            <view class="stat-icon">🍳</view>
            <view class="stat-content">
              <text class="stat-num">{{ stats.mealsCooked }}</text>
              <text class="stat-label">家常菜谱</text>
            </view>
          </view>
          
          <view class="stat-item photos">
            <view class="stat-icon">📸</view>
            <view class="stat-content">
              <text class="stat-num">{{ stats.photos }}</text>
              <text class="stat-label">家庭照片</text>
            </view>
          </view>
        </view>
      </view>
      
      <!-- 底部留白 -->
      <view class="bottom-space"></view>
    </scroll-view>
    
    <!-- 邀请码弹窗 - 优化设计 -->
    <view class="invite-modal" v-if="showInviteCodeModal">
      <view class="modal-mask" @click="showInviteCodeModal = false"></view>
      <view class="modal-content">
        <view class="modal-header">
          <view class="header-icon">💌</view>
          <text class="modal-title">邀请家人</text>
          <text class="modal-close" @click="showInviteCodeModal = false">✕</text>
        </view>
        
        <view class="invite-body" v-if="currentInviteCode">
          <view class="code-card">
            <text class="code-label">邀请码</text>
            <view class="code-display">
              <text class="code-value">{{ currentInviteCode }}</text>
              <view class="copy-btn" @click="copyInviteCode">
                <text class="copy-icon">📋</text>
                <text>复制</text>
              </view>
            </view>
            <text class="code-hint">有效期30天，最多可使用5次</text>
          </view>
        </view>
        
        <view class="invite-empty" v-else>
          <text class="empty-icon">🎫</text>
          <text class="empty-text">暂无有效邀请码</text>
        </view>
        
        <view class="modal-actions">
          <button class="btn-generate" @click="generateNewCode" :disabled="generating">
            <text v-if="generating">生成中...</text>
            <text v-else>生成新邀请码</text>
          </button>
        </view>
        
        <!-- 邀请码历史 -->
        <view class="invite-history" v-if="inviteCodes.length > 0">
          <view class="history-header">
            <text class="history-title">历史邀请码</text>
          </view>
          <view class="history-list">
            <view class="history-item" v-for="item in inviteCodes.slice(0, 3)" :key="item.id">
              <view class="item-left">
                <text class="history-code">{{ item.code }}</text>
                <text class="history-date">{{ formatDate(item.createdAt) }}</text>
              </view>
              <view class="item-right">
                <text class="history-count" :class="{ full: item.usedCount >= item.maxUses }">
                  {{ item.usedCount }}/{{ item.maxUses }}
                </text>
                <view class="status-badge" :class="{ expired: isCodeExpired(item) }">
                  {{ isCodeExpired(item) ? '已过期' : '有效' }}
                </view>
              </view>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { familyApi } from '@/api/family.js'
import { statsApi } from '@/api/stats.js'
import { getDefaultFamily } from '@/utils/defaultFamily.js'
import { useUserStore } from '@/stores/user.js'

const userStore = useUserStore()

const family = ref({
  id: null,
  name: '',
  inviteCode: '',
  memberCount: 0
})

const members = ref([])
const isAdmin = ref(false)
const showInviteCodeModal = ref(false)
const currentInviteCode = ref('')
const inviteCodes = ref([])
const generating = ref(false)

const features = [
  { 
    name: '家庭相册', 
    icon: 'photo', 
    bgColor: 'linear-gradient(135deg, #FF6B6B 0%, #FF8E8E 100%)', 
    path: '/pages/family/album',
    desc: '记录美好瞬间'
  },
  { 
    name: '家庭动态', 
    icon: 'chat', 
    bgColor: 'linear-gradient(135deg, #96CEB4 0%, #7FCDCD 100%)', 
    path: '/pages/family/moments',
    desc: '分享生活点滴'
  }
]

const stats = ref({
  tasksCompleted: 0,
  wishesCompleted: 0,
  mealsCooked: 0,
  photos: 0
})

const refreshing = ref(false)
const familyId = ref(null)

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getMonth() + 1}月${date.getDate()}日`
}

// 检查用户是否为管理员
const checkAdminStatus = async () => {
  try {
    const userInfo = userStore.userInfo || uni.getStorageSync('userInfo')
    if (!familyId.value || !userInfo || !userInfo.id) return
    
    const res = await familyApi.checkAdmin(familyId.value, userInfo.id)
    isAdmin.value = res.isAdmin || false
  } catch (e) {
    console.error('[Family] 检查管理员状态失败:', e)
    isAdmin.value = false
  }
}

// 加载家庭数据和成员
const loadFamilyData = async () => {
  try {
    const defaultFamily = await getDefaultFamily()
    if (!defaultFamily || !defaultFamily.id) {
      console.warn('[Family] 未找到默认家庭')
      return
    }
    
    familyId.value = defaultFamily.id
    
    const familyRes = await familyApi.getById(familyId.value)
    if (familyRes) {
      family.value = {
        id: familyRes.id,
        name: familyRes.name || '幸福小家',
        inviteCode: familyRes.inviteCode || '',
        memberCount: familyRes.memberCount || 0
      }
    }
    
    const membersRes = await familyApi.getMembers(familyId.value)
    if (membersRes && Array.isArray(membersRes)) {
      members.value = membersRes.map(m => ({
        id: m.id || m.userId,
        nickname: m.nickname || m.name || '未知',
        avatar: m.avatar || '/static/avatar/default.jpg',
        phone: m.phone || '',
        role: m.role || 'member',
        isOnline: m.isOnline || false
      }))
    }
    
    await checkAdminStatus()
    
    if (isAdmin.value) {
      await loadInviteCodes()
    }
  } catch (error) {
    console.error('[Family] 加载家庭数据失败:', error)
  }
}

// 加载邀请码列表
const loadInviteCodes = async () => {
  try {
    const res = await familyApi.getInviteCodes(familyId.value)
    if (res && Array.isArray(res)) {
      inviteCodes.value = res
      const validCode = res.find(c => c.status === 1 && !isCodeExpired(c))
      if (validCode) {
        currentInviteCode.value = validCode.code
      }
    }
  } catch (e) {
    console.error('[Family] 加载邀请码失败:', e)
  }
}

// 检查邀请码是否过期
const isCodeExpired = (code) => {
  if (!code.expiresAt) return false
  return new Date(code.expiresAt) < new Date()
}

// 显示邀请弹窗
const showInviteModal = async () => {
  showInviteCodeModal.value = true
  if (!currentInviteCode.value) {
    await generateNewCode()
  }
}

// 生成新邀请码
const generateNewCode = async () => {
  if (generating.value) return
  
  generating.value = true
  try {
    const userInfo = userStore.userInfo || uni.getStorageSync('userInfo')
    const res = await familyApi.createInviteCode(familyId.value, userInfo.id, 5, 30)
    
    if (res && res.code === 200) {
      currentInviteCode.value = res.data.code
      uni.showToast({ title: '邀请码已生成', icon: 'success' })
      await loadInviteCodes()
    } else {
      uni.showToast({ title: res.message || '生成失败', icon: 'none' })
    }
  } catch (e) {
    console.error('[Family] 生成邀请码失败:', e)
    uni.showToast({ title: '生成失败', icon: 'none' })
  } finally {
    generating.value = false
  }
}

// 复制邀请码
const copyInviteCode = () => {
  uni.setClipboardData({
    data: currentInviteCode.value,
    success: () => uni.showToast({ title: '已复制', icon: 'success' })
  })
}

// 获取家庭本月统计
const loadFamilyStats = async () => {
  try {
    const defaultFamily = await getDefaultFamily()
    if (!defaultFamily || !defaultFamily.id) {
      familyId.value = 1
    } else {
      familyId.value = defaultFamily.id
    }
    
    const res = await statsApi.getFamilyMonthlyStats(familyId.value)
    if (res) {
      stats.value = {
        tasksCompleted: res.tasksCompleted || 0,
        wishesCompleted: res.wishesCompleted || 0,
        mealsCooked: res.mealsCooked || 0,
        photos: res.photos || 0
      }
    }
  } catch (error) {
    console.error('[Family] 加载家庭统计失败:', error)
  }
}

// 下拉刷新
const onRefresh = async () => {
  refreshing.value = true
  await loadFamilyData()
  await loadFamilyStats()
  refreshing.value = false
  uni.showToast({
    title: '刷新成功',
    icon: 'success',
    duration: 1000
  })
}

onMounted(async () => {
  if (!userStore.userInfo || !userStore.userInfo.id) {
    await userStore.getUserInfo()
  }
  
  const userFamilyId = userStore.userInfo?.currentFamilyId || uni.getStorageSync('currentFamilyId')
  if (userFamilyId) {
    familyId.value = userFamilyId
  }
  
  loadFamilyData()
  loadFamilyStats()
})

const goFeature = (feature) => {
  uni.navigateTo({ url: feature.path })
}
</script>

<style lang="scss" scoped>
.family-page {
  min-height: 100vh;
  background: #f8f9fc;
  position: relative;
}

/* 顶部渐变背景 */
.header-bg {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 400rpx;
  background: linear-gradient(135deg, #6B8DD6 0%, #8B5CF6 50%, #A78BFA 100%);
  border-radius: 0 0 60rpx 60rpx;
  z-index: 0;
  
  .bg-pattern {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    opacity: 0.1;
    background-image: 
      radial-gradient(circle at 20% 30%, rgba(255,255,255,0.8) 0%, transparent 40%),
      radial-gradient(circle at 80% 20%, rgba(255,255,255,0.6) 0%, transparent 35%),
      radial-gradient(circle at 40% 70%, rgba(255,255,255,0.4) 0%, transparent 30%);
  }
}

.scroll-container {
  position: relative;
  z-index: 1;
  height: 100vh;
  padding-top: 40rpx;
  padding-bottom: 160rpx;
}

/* 家庭头部卡片 */
.family-header {
  margin: 0 32rpx;
  padding: 32rpx;
  background: #fff;
  border-radius: 32rpx;
  box-shadow: 0 12rpx 40rpx rgba(107, 141, 214, 0.15);
  display: flex;
  align-items: center;
  justify-content: space-between;
  
  .family-main {
    display: flex;
    align-items: center;
    flex: 1;
  }
  
  .family-avatar-wrapper {
    position: relative;
    margin-right: 24rpx;
    
    .family-avatar {
      width: 120rpx;
      height: 120rpx;
      border-radius: 32rpx;
      box-shadow: 0 8rpx 24rpx rgba(107, 141, 214, 0.25);
    }
    
    .family-badge {
      position: absolute;
      bottom: -8rpx;
      right: -8rpx;
      min-width: 40rpx;
      height: 40rpx;
      background: linear-gradient(135deg, #FF6B6B, #FF8E8E);
      border-radius: 20rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 0 12rpx;
      box-shadow: 0 4rpx 12rpx rgba(255, 107, 107, 0.3);
      
      text {
        font-size: 22rpx;
        color: #fff;
        font-weight: 600;
      }
    }
  }
  
  .family-info {
    .family-name {
      font-size: 40rpx;
      font-weight: 700;
      color: #2d3748;
      display: block;
      margin-bottom: 8rpx;
    }
    
    .family-desc {
      font-size: 26rpx;
      color: #8b9aad;
    }
  }
  
  .header-actions {
    .action-btn {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 16rpx 24rpx;
      border-radius: 20rpx;
      transition: all 0.25s ease;
      
      &:active {
        transform: scale(0.95);
      }
      
      &.invite {
        background: linear-gradient(135deg, #68d391 0%, #48bb78 100%);
        box-shadow: 0 8rpx 24rpx rgba(104, 211, 145, 0.35);
      }
      
      .btn-icon {
        font-size: 36rpx;
        color: #fff;
        font-weight: 300;
        line-height: 1;
      }
      
      .btn-text {
        font-size: 22rpx;
        color: #fff;
        font-weight: 500;
        margin-top: 4rpx;
      }
    }
  }
}

/* 成员头像墙 */
.members-wall {
  margin: 32rpx 0;
  
  .members-scroll {
    white-space: nowrap;
  }
  
  .members-list {
    display: flex;
    padding: 0 32rpx;
    gap: 32rpx;
  }
  
  .member-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    animation: fadeInUp 0.5s ease-out forwards;
    opacity: 0;
    
    .avatar-wrapper {
      position: relative;
      margin-bottom: 12rpx;
      
      .member-avatar {
        width: 120rpx;
        height: 120rpx;
        border-radius: 50%;
        border: 4rpx solid #fff;
        box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.1);
      }
      
      .role-badge {
        position: absolute;
        bottom: 0;
        right: 0;
        width: 40rpx;
        height: 40rpx;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 20rpx;
        border: 3rpx solid #fff;
        
        &.owner {
          background: linear-gradient(135deg, #fc8181, #f56565);
        }
        
        &.admin {
          background: linear-gradient(135deg, #6B8DD6, #8B5CF6);
        }
      }
    }
    
    .member-name {
      font-size: 26rpx;
      color: #5a6c7d;
      font-weight: 500;
      max-width: 120rpx;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 功能区域 */
.features-section {
  margin: 0 32rpx 32rpx;
  
  .section-title {
    display: flex;
    align-items: center;
    margin-bottom: 24rpx;
    
    .title-icon {
      font-size: 32rpx;
      margin-right: 12rpx;
    }
    
    .title-text {
      font-size: 32rpx;
      font-weight: 700;
      color: #2d3748;
    }
  }
  
  .features-grid {
    display: flex;
    flex-direction: column;
    gap: 20rpx;
  }
  
  .feature-card {
    display: flex;
    align-items: center;
    padding: 28rpx;
    background: #fff;
    border-radius: 28rpx;
    box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.06);
    transition: all 0.25s ease;
    animation: slideIn 0.5s ease-out forwards;
    opacity: 0;
    transform: translateX(-20rpx);
    
    &:active {
      transform: scale(0.98);
      box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
    }
    
    .feature-icon-wrapper {
      width: 88rpx;
      height: 88rpx;
      border-radius: 24rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 24rpx;
      box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.12);
    }
    
    .feature-info {
      flex: 1;
      
      .feature-name {
        font-size: 30rpx;
        font-weight: 600;
        color: #2d3748;
        display: block;
        margin-bottom: 6rpx;
      }
      
      .feature-desc {
        font-size: 24rpx;
        color: #8b9aad;
      }
    }
    
    .feature-arrow {
      font-size: 40rpx;
      color: #c5d0e0;
    }
  }
}

@keyframes slideIn {
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

/* 统计区域 */
.stats-section {
  margin: 0 32rpx;
  
  .section-title {
    display: flex;
    align-items: center;
    margin-bottom: 24rpx;
    
    .title-icon {
      font-size: 32rpx;
      margin-right: 12rpx;
    }
    
    .title-text {
      font-size: 32rpx;
      font-weight: 700;
      color: #2d3748;
    }
  }
  
  .stats-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 20rpx;
  }
  
  .stat-item {
    display: flex;
    align-items: center;
    padding: 28rpx;
    background: #fff;
    border-radius: 24rpx;
    box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.06);
    transition: all 0.25s ease;
    
    &:active {
      transform: scale(0.96);
    }
    
    &.tasks .stat-icon {
      background: linear-gradient(135deg, #68d391, #48bb78);
    }
    
    &.wishes .stat-icon {
      background: linear-gradient(135deg, #fc8181, #f56565);
    }
    
    &.meals .stat-icon {
      background: linear-gradient(135deg, #f6ad55, #ed8936);
    }
    
    &.photos .stat-icon {
      background: linear-gradient(135deg, #6B8DD6, #8B5CF6);
    }
    
    .stat-icon {
      width: 72rpx;
      height: 72rpx;
      border-radius: 20rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 36rpx;
      margin-right: 20rpx;
      box-shadow: 0 6rpx 16rpx rgba(0, 0, 0, 0.1);
    }
    
    .stat-content {
      .stat-num {
        font-size: 40rpx;
        font-weight: 700;
        color: #2d3748;
        display: block;
        line-height: 1.2;
      }
      
      .stat-label {
        font-size: 24rpx;
        color: #8b9aad;
      }
    }
  }
}

.bottom-space {
  height: 40rpx;
}

/* 邀请码弹窗 */
.invite-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 999;
  
  .modal-mask {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.5);
    backdrop-filter: blur(4rpx);
  }
  
  .modal-content {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    background: #fff;
    border-radius: 48rpx 48rpx 0 0;
    padding: 48rpx;
    max-height: 80vh;
    overflow-y: auto;
    animation: slideUp 0.3s ease-out;
    
    .modal-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 40rpx;
      
      .header-icon {
        width: 64rpx;
        height: 64rpx;
        background: linear-gradient(135deg, #6B8DD6, #8B5CF6);
        border-radius: 20rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 36rpx;
      }
      
      .modal-title {
        flex: 1;
        font-size: 36rpx;
        font-weight: 700;
        color: #2d3748;
        margin-left: 20rpx;
      }
      
      .modal-close {
        width: 56rpx;
        height: 56rpx;
        background: #f1f5f9;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 32rpx;
        color: #8b9aad;
      }
    }
    
    .invite-body {
      .code-card {
        background: linear-gradient(135deg, #f8f9fc 0%, #f0f4f8 100%);
        border-radius: 32rpx;
        padding: 40rpx;
        text-align: center;
        
        .code-label {
          font-size: 26rpx;
          color: #8b9aad;
          display: block;
          margin-bottom: 20rpx;
        }
        
        .code-display {
          display: flex;
          align-items: center;
          justify-content: center;
          gap: 24rpx;
          margin-bottom: 20rpx;
          
          .code-value {
            font-size: 56rpx;
            font-weight: 700;
            color: #6B8DD6;
            letter-spacing: 8rpx;
          }
          
          .copy-btn {
            display: flex;
            align-items: center;
            gap: 8rpx;
            padding: 16rpx 28rpx;
            background: #6B8DD6;
            border-radius: 32rpx;
            
            text {
              font-size: 26rpx;
              color: #fff;
              font-weight: 500;
            }
            
            .copy-icon {
              font-size: 28rpx;
            }
          }
        }
        
        .code-hint {
          font-size: 24rpx;
          color: #8b9aad;
        }
      }
    }
    
    .invite-empty {
      text-align: center;
      padding: 80rpx 40rpx;
      
      .empty-icon {
        font-size: 80rpx;
        display: block;
        margin-bottom: 20rpx;
      }
      
      .empty-text {
        font-size: 28rpx;
        color: #8b9aad;
      }
    }
    
    .modal-actions {
      margin-top: 32rpx;
      
      .btn-generate {
        width: 100%;
        height: 96rpx;
        background: linear-gradient(135deg, #6B8DD6 0%, #8B5CF6 100%);
        border-radius: 48rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        border: none;
        
        text {
          font-size: 30rpx;
          color: #fff;
          font-weight: 600;
        }
        
        &[disabled] {
          opacity: 0.6;
        }
      }
    }
    
    .invite-history {
      margin-top: 48rpx;
      padding-top: 32rpx;
      border-top: 2rpx solid #f1f5f9;
      
      .history-header {
        margin-bottom: 24rpx;
        
        .history-title {
          font-size: 28rpx;
          font-weight: 600;
          color: #5a6c7d;
        }
      }
      
      .history-list {
        .history-item {
          display: flex;
          align-items: center;
          justify-content: space-between;
          padding: 24rpx;
          background: #f8f9fc;
          border-radius: 20rpx;
          margin-bottom: 16rpx;
          
          .item-left {
            .history-code {
              font-size: 30rpx;
              color: #2d3748;
              font-weight: 600;
              letter-spacing: 4rpx;
              display: block;
              margin-bottom: 8rpx;
            }
            
            .history-date {
              font-size: 24rpx;
              color: #8b9aad;
            }
          }
          
          .item-right {
            display: flex;
            align-items: center;
            gap: 16rpx;
            
            .history-count {
              font-size: 24rpx;
              color: #68d391;
              font-weight: 500;
              
              &.full {
                color: #fc8181;
              }
            }
            
            .status-badge {
              padding: 8rpx 16rpx;
              background: #68d391;
              border-radius: 16rpx;
              font-size: 22rpx;
              color: #fff;
              font-weight: 500;
              
              &.expired {
                background: #cbd5e0;
              }
            }
          }
        }
      }
    }
  }
}

@keyframes slideUp {
  from {
    transform: translateY(100%);
  }
  to {
    transform: translateY(0);
  }
}
</style>