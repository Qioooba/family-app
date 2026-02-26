<template>
  <view class="family-page">
    <!-- 下拉刷新 -->
    <scroll-view 
      scroll-y 
      class="scroll-container"
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >
    <!-- 家庭信息卡 - 简洁顶部 -->
    <view class="family-header-simple">
      <view class="family-left">
        <image class="family-avatar-large" src="/static/family-avatar.jpg" />
        <view class="family-info-simple">
          <text class="family-name-simple">{{ family.name || '幸福小家' }}</text>
        </view>
      </view>
      <view class="invite-btn-simple" @click="showInviteModal" v-if="isAdmin">
        <text>+ 邀请</text>
      </view>
    </view>
    
    <!-- 邀请码弹窗 -->
    <view class="invite-modal" v-if="showInviteCodeModal">
      <view class="modal-mask" @click="showInviteCodeModal = false"></view>
      <view class="modal-content">
        <view class="modal-header">
          <text class="modal-title">邀请家人</text>
          <text class="modal-close" @click="showInviteCodeModal = false">✕</text>
        </view>
        
        <view class="invite-code-display" v-if="currentInviteCode">
          <text class="code-label">邀请码</text>
          <view class="code-value">
            <text>{{ currentInviteCode }}</text>
            <text class="copy-btn" @click="copyInviteCode">复制</text>
          </view>
          <text class="code-hint">分享给家人，让他们加入家庭</text>
        </view>
        
        <view class="invite-code-actions">
          <button class="action-btn primary" @click="generateNewCode" :disabled="generating">
            {{ generating ? '生成中...' : '生成新邀请码' }}
          </button>
        </view>
        
        <!-- 邀请码历史列表 -->
        <view class="invite-history" v-if="inviteCodes.length > 0">
          <text class="history-title">历史邀请码</text>
          <view class="history-list">
            <view class="history-item" v-for="item in inviteCodes" :key="item.id">
              <text class="history-code">{{ item.code }}</text>
              <text class="history-status" :class="{ expired: isCodeExpired(item) }">
                {{ isCodeExpired(item) ? '已过期' : `${item.usedCount}/${item.maxUses}次` }}
              </text>
            </view>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 成员列表 -->
    <view class="members-section">
      <view class="section-header">
        <text class="section-title">👨‍👩‍👧‍👦 家庭成员</text>
      </view>
      
      <view class="member-list">
        <view 
          v-for="member in members" 
          :key="member.id"
          class="member-card"
        >
          <image class="member-avatar" :src="member.avatar" />
          
          <view class="member-info">
            <view class="name-row">
              <text class="member-name">{{ member.nickname }}</text>
              
              <view v-if="member.role === 'owner'" class="role-tag owner">🏠 家主</view>
              
              <view v-else-if="member.role === 'admin'" class="role-tag admin">👔 管理员</view>
            </view>
            
            <text class="member-phone">{{ member.phone || '未绑定手机' }}</text>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 家庭功能 -->
    <view class="family-features">
      <view class="section-header">
        <text class="section-title">🏠 家庭空间</text>
      </view>
      
      <view class="feature-grid">
        <view 
          v-for="(feature, index) in features" 
          :key="index"
          class="feature-item"
          @click="goFeature(feature)"
        >
          <view class="feature-icon" :style="{ background: feature.bgColor }">
            <u-icon :name="feature.icon" size="44" color="#fff"></u-icon>
          </view>
          
          <text class="feature-name">{{ feature.name }}</text>
        </view>
      </view>
    </view>
    
    <!-- 家庭统计数据 -->
    <view class="family-stats">
      <view class="section-header">
        <text class="section-title">📊 本月统计</text>
      </view>
      
      <view class="stats-grid">
        <view class="stat-card">
          <text class="stat-num">{{ stats.tasksCompleted }}</text>
          <text class="stat-label">任务完成</text>
        </view>
        
        <view class="stat-card">
          <text class="stat-num">{{ stats.wishesCompleted }}</text>
          <text class="stat-label">心愿实现</text>
        </view>
        
        <view class="stat-card">
          <text class="stat-num">{{ stats.mealsCooked }}</text>
          <text class="stat-label">家常菜谱</text>
        </view>
        
        <view class="stat-card">
          <text class="stat-num">{{ stats.photos }}</text>
          <text class="stat-label">家庭照片</text>
        </view>
      </view>
    </view>
    </scroll-view>
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
  { name: '家庭相册', icon: 'photo', bgColor: '#FF6B6B', path: '/pages/family/album' },
  { name: '家庭动态', icon: 'chat', bgColor: '#96CEB4', path: '/pages/family/moments' }
]

const stats = ref({
  tasksCompleted: 0,
  wishesCompleted: 0,
  mealsCooked: 0,
  photos: 0
})

const loading = ref(false)
const refreshing = ref(false)
const familyId = ref(null)

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
    // 使用默认家庭
    const defaultFamily = await getDefaultFamily()
    if (!defaultFamily || !defaultFamily.id) {
      console.warn('[Family] 未找到默认家庭')
      return
    }
    
    familyId.value = defaultFamily.id
    console.log('[Family] 开始加载家庭数据, familyId:', familyId.value)
    
    // 获取家庭详情
    const familyRes = await familyApi.getById(familyId.value)
    console.log('[Family] 家庭详情:', familyRes)
    
    if (familyRes) {
      family.value = {
        id: familyRes.id,
        name: familyRes.name || '幸福小家',
        inviteCode: familyRes.inviteCode || '',
        memberCount: familyRes.memberCount || 0
      }
      console.log('[Family] 家庭信息更新成功:', family.value)
    }
    
    // 获取家庭成员
    const membersRes = await familyApi.getMembers(familyId.value)
    console.log('[Family] 成员列表:', membersRes)
    
    if (membersRes && Array.isArray(membersRes)) {
      members.value = membersRes.map(m => ({
        id: m.id || m.userId,
        nickname: m.nickname || m.name || '未知',
        avatar: m.avatar || '/static/avatar/default.jpg',
        phone: m.phone || '',
        role: m.role || 'member',
        isOnline: m.isOnline || false
      }))
      console.log('[Family] 成员数据更新成功:', members.value)
    }
    
    // 检查管理员状态
    await checkAdminStatus()
    
    // 如果是管理员，加载邀请码
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
      // 获取最新的有效邀请码
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

// 编辑家庭名称
const editFamilyName = () => {
  uni.showModal({
    title: '编辑家庭名称',
    placeholderText: '请输入新名称',
    editable: true,
    confirmText: '保存',
    cancelText: '取消',
    success: async (res) => {
      if (res.confirm && res.content && res.content.trim()) {
        const newName = res.content.trim()
        try {
          uni.showLoading({ title: '保存中...' })
          // 调用API保存到后端
          await familyApi.update({ id: familyId.value, name: newName })
          // 更新本地显示
          family.value.name = newName
          uni.hideLoading()
          uni.showToast({ title: '已更新', icon: 'success' })
        } catch (e) {
          console.error('更新家庭名称失败:', e)
          uni.hideLoading()
          uni.showToast({ title: '更新失败', icon: 'none' })
        }
      }
    }
  })
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
      // 刷新邀请码列表
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
    // 使用默认家庭
    const defaultFamily = await getDefaultFamily()
    if (!defaultFamily || !defaultFamily.id) {
      console.warn('[Family] 未找到默认家庭，使用默认值')
      familyId.value = 1
    } else {
      familyId.value = defaultFamily.id
    }
    
    console.log('[Family] 开始加载家庭统计, familyId:', familyId.value)
    const res = await statsApi.getFamilyMonthlyStats(familyId.value)
    console.log('[Family] 统计接口返回:', res)
    
    if (res) {
      stats.value = {
        tasksCompleted: res.tasksCompleted || 0,
        wishesCompleted: res.wishesCompleted || 0,
        mealsCooked: res.mealsCooked || 0,
        photos: res.photos || 0
      }
      console.log('[Family] 统计数据更新成功:', stats.value)
    }
  } catch (error) {
    console.error('[Family] 加载家庭统计失败:', error)
    // 使用默认值
    stats.value = {
      tasksCompleted: 0,
      wishesCompleted: 0,
      mealsCooked: 0,
      photos: 0
    }
  }
}

// 下拉刷新
const onRefresh = async () => {
  refreshing.value = true
  console.log('[Family] 下拉刷新')
  await loadFamilyData()
  await loadFamilyStats()
  refreshing.value = false
  uni.showToast({
    title: '刷新成功',
    icon: 'success',
    duration: 1000
  })
}

onMounted(() => {
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
  background: #f5f5f5;
}

.scroll-container {
  height: 100vh;
  padding-top: 200rpx;
  padding-bottom: 160rpx; /* 为tabBar留出足够空间 */
}

.family-header-simple {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 200rpx;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12rpx 24rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 0 0 32rpx 32rpx;
  
  .family-left {
    display: flex;
    align-items: center;
    flex: 1;
  }
  
  .family-avatar-large {
    width: 56rpx;
    height: 56rpx;
    border-radius: 12rpx;
    margin-right: 12rpx;
  }
  
  .family-info-simple {
    .family-name-simple {
      font-size: 28rpx;
      color: #fff;
      font-weight: 600;
      display: block;
    }
    
    .member-count-simple {
      font-size: 20rpx;
      color: rgba(255,255,255,0.75);
    }
  }
  
  .invite-btn-simple {
    padding: 8rpx 16rpx;
    background: rgba(255,255,255,0.25);
    border-radius: 16rpx;
    
    text {
      font-size: 20rpx;
      color: #fff;
    }
  }
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
  }
  
  .modal-content {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    background: #fff;
    border-radius: 40rpx 40rpx 0 0;
    padding: 40rpx;
    max-height: 70vh;
    overflow-y: auto;
    
    .modal-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 40rpx;
      
      .modal-title {
        font-size: 36rpx;
        font-weight: 700;
        color: #333;
      }
      
      .modal-close {
        font-size: 40rpx;
        color: #999;
        padding: 10rpx;
      }
    }
    
    .invite-code-display {
      text-align: center;
      padding: 40rpx 0;
      background: linear-gradient(135deg, #f8f9fc 0%, #f0f4f8 100%);
      border-radius: 24rpx;
      margin-bottom: 32rpx;
      
      .code-label {
        display: block;
        font-size: 26rpx;
        color: #999;
        margin-bottom: 16rpx;
      }
      
      .code-value {
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 24rpx;
        
        text {
          font-size: 48rpx;
          font-weight: 700;
          color: #6B8DD6;
          letter-spacing: 8rpx;
        }
        
        .copy-btn {
          font-size: 26rpx;
          color: #fff;
          background: #6B8DD6;
          padding: 12rpx 24rpx;
          border-radius: 30rpx;
          letter-spacing: 2rpx;
        }
      }
      
      .code-hint {
        display: block;
        font-size: 24rpx;
        color: #999;
        margin-top: 20rpx;
      }
    }
    
    .invite-code-actions {
      margin-bottom: 32rpx;
      
      .action-btn {
        width: 100%;
        height: 88rpx;
        border-radius: 44rpx;
        font-size: 30rpx;
        font-weight: 600;
        display: flex;
        align-items: center;
        justify-content: center;
        
        &.primary {
          background: linear-gradient(135deg, #6B8DD6 0%, #8B5CF6 100%);
          color: #fff;
        }
        
        &[disabled] {
          opacity: 0.6;
        }
      }
    }
    
    .invite-history {
      .history-title {
        display: block;
        font-size: 28rpx;
        color: #666;
        margin-bottom: 20rpx;
        font-weight: 500;
      }
      
      .history-list {
        .history-item {
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding: 20rpx;
          background: #f8f9fc;
          border-radius: 16rpx;
          margin-bottom: 16rpx;
          
          .history-code {
            font-size: 28rpx;
            color: #333;
            font-weight: 500;
            letter-spacing: 4rpx;
          }
          
          .history-status {
            font-size: 24rpx;
            color: #68d391;
            
            &.expired {
              color: #fc8181;
            }
          }
        }
      }
    }
  }
}

.members-section {
  margin: 20rpx;
  padding: 24rpx;
  background: #fff;
  border-radius: 16rpx;
  
  .section-header {
    margin-bottom: 20rpx;
    
    .section-title {
      font-size: 30rpx;
      font-weight: 600;
      color: #1a1a1a;
    }
  }
  
  .member-list {
    .member-card {
      display: flex;
      align-items: center;
      padding: 24rpx 0;
      border-bottom: 2rpx solid #f1f5f9;
      transition: all 0.2s ease;
      
      &:last-child {
        border-bottom: none;
      }
      
      &:active {
        background: #f8f9fc;
        border-radius: 16rpx;
        margin: 0 -16rpx;
        padding-left: 16rpx;
        padding-right: 16rpx;
      }
      
      .member-avatar {
        width: 96rpx;
        height: 96rpx;
        border-radius: 50%;
        margin-right: 24rpx;
        box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.08);
      }
      
      .member-info {
        flex: 1;
        
        .name-row {
          display: flex;
          align-items: center;
          margin-bottom: 10rpx;
          
          .member-name {
            font-size: 32rpx;
            color: #2d3748;
            font-weight: 600;
            margin-right: 16rpx;
          }
          
          .role-tag {
            padding: 6rpx 16rpx;
            font-size: 22rpx;
            border-radius: 20rpx;
            font-weight: 500;
            
            &.owner {
              background: linear-gradient(135deg, #fc8181, #f56565);
              color: #fff;
              box-shadow: 0 4rpx 12rpx rgba(252, 129, 129, 0.25);
            }
            
            &.admin {
              background: linear-gradient(135deg, #6B8DD6, #8B5CF6);
              color: #fff;
              box-shadow: 0 4rpx 12rpx rgba(107, 141, 214, 0.25);
            }
          }
        }
        
        .member-phone {
          font-size: 26rpx;
          color: #8b9aad;
        }
      }
    }
  }
}

.family-features {
  margin: 20rpx;
  padding: 24rpx;
  background: #fff;
  border-radius: 16rpx;
  
  .section-header {
    margin-bottom: 20rpx;
    
    .section-title {
      font-size: 30rpx;
      font-weight: 600;
      color: #1a1a1a;
    }
  }
  
  .feature-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 24rpx;
    max-width: 400rpx;
    margin: 0 auto;
    
    .feature-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 16rpx 0;
      transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
      border-radius: 20rpx;
      
      &:active {
        transform: scale(0.95);
        background: #f8f9fc;
      }
      
      .feature-icon {
        width: 100rpx;
        height: 100rpx;
        border-radius: 28rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-bottom: 16rpx;
        box-shadow: 0 8rpx 24rpx rgba(0,0,0,0.12);
        transition: all 0.3s ease;
      }
      
      .feature-name {
        font-size: 26rpx;
        color: #5a6c7d;
        font-weight: 500;
      }
    }
  }
}

.family-stats {
  margin: 24rpx;
  padding: 28rpx;
  background: #fff;
  border-radius: 24rpx;
  box-shadow: 0 4rpx 20rpx rgba(107, 141, 214, 0.06);
  
  .section-header {
    margin-bottom: 28rpx;
    
    .section-title {
      font-size: 34rpx;
      font-weight: 700;
      color: #2d3748;
    }
  }
  
  .stats-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20rpx;
    
    .stat-card {
      text-align: center;
      padding: 28rpx 16rpx;
      background: linear-gradient(135deg, #f8f9fc 0%, #f0f4f8 100%);
      border-radius: 24rpx;
      transition: all 0.25s ease;
      
      &:active {
        transform: scale(0.95);
      }
      
      .stat-num {
        font-size: 40rpx;
        font-weight: 700;
        color: #6B8DD6;
        display: block;
        margin-bottom: 8rpx;
      }
      
      .stat-label {
        font-size: 24rpx;
        color: #8b9aad;
        font-weight: 500;
      }
    }
  }
}
</style>
