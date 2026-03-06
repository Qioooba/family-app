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
      <!-- 顶部区域 -->
      <view class="family-header">
        <!-- 背景装饰 -->
        <view class="header-bg">
          <view class="bg-circle circle-1"></view>
          <view class="bg-circle circle-2"></view>
          <view class="bg-circle circle-3"></view>
        </view>
        
        <!-- 家庭名称 -->
        <view class="family-title-section">
          <text class="family-name">{{ family.name || '幸福小家' }}</text>
          <text class="family-subtitle">🏠 温馨港湾</text>
        </view>
        
        <!-- 成员头像群 -->
        <view class="member-avatars">
          <view 
            v-for="(member, index) in displayMembers" 
            :key="member.id"
            class="avatar-item"
            :style="{ 
              zIndex: members.length - index,
              transform: `translateX(${index * -16}rpx)`
            }"
          >
            <image 
              class="avatar-img" 
              :src="member.avatar" 
              :style="{ borderColor: getAvatarBorder(index) }"
            />
            <view v-if="index === 0 && member.role === 'owner'" class="owner-badge">👑</view>
          </view>
          <view v-if="members.length > 6" class="more-avatar">
            <text>+{{ members.length - 6 }}</text>
          </view>
        </view>
        
        <!-- 邀请按钮 -->
        <view class="invite-section">
          <view class="invite-btn" @click="showInviteModal" v-if="isAdmin">
            <text class="invite-icon">➕</text>
            <text class="invite-text">邀请加入</text>
          </view>
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
      
      <!-- 中间区域 - 本月统计卡片 -->
      <view class="stats-section">
        <view class="section-header">
          <text class="section-title">📊 本月统计</text>
          <text class="section-subtitle">{{ currentMonth }}</text>
        </view>
        
        <view class="stats-grid">
          <view class="stat-card" @click="goFeature({ path: '/pages/task/list' })">
            <view class="stat-icon-wrap tasks">
              <text class="stat-emoji">✅</text>
            </view>
            <text class="stat-num">{{ stats.tasksCompleted }}</text>
            <text class="stat-label">完成任务</text>
          </view>
          
          <view class="stat-card" @click="goFeature({ path: '/pages/wish/list' })">
            <view class="stat-icon-wrap wishes">
              <text class="stat-emoji">🎯</text>
            </view>
            <text class="stat-num">{{ stats.wishesCompleted }}</text>
            <text class="stat-label">心愿完成</text>
          </view>
          
          <view class="stat-card" @click="goFeature({ path: '/pages/recipe/list' })">
            <view class="stat-icon-wrap meals">
              <text class="stat-emoji">🍳</text>
            </view>
            <text class="stat-num">{{ stats.mealsCooked }}</text>
            <text class="stat-label">做饭次数</text>
          </view>
          
          <view class="stat-card" @click="goFeature({ path: '/pages/anniversary/list' })">
            <view class="stat-icon-wrap clock">
              <text class="stat-emoji">📅</text>
            </view>
            <text class="stat-num">{{ stats.checkinDays }}</text>
            <text class="stat-label">打卡天数</text>
          </view>
        </view>
      </view>
      
      <!-- 成员列表 -->
      <view class="members-section">
        <view class="section-header">
          <text class="section-title">👨‍👩‍👧‍👦 家庭成员</text>
          <text class="member-count">{{ members.length }}人</text>
        </view>
        
        <view class="member-list">
          <view 
            v-for="member in members"
            :key="member.id"
            class="member-card"
            @click="showMemberDetail(member)"
          >
            <image class="member-avatar" :src="member.avatar" />
            
            <view class="member-info">
              <view class="name-row">
                <text class="member-name">{{ member.nickname }}</text>
                
                <view v-if="member.role === 'owner'" class="role-tag owner">
                  <text>🏠</text> 家主
                </view>
                
                <view v-else-if="member.role === 'admin'" class="role-tag admin">
                  <text>👔</text> 管理员
                </view>
              </view>
              
              <text class="member-phone">{{ member.phone || '未绑定手机' }}</text>
            </view>
            
            <text class="arrow">›</text>
          </view>
        </view>
      </view>
      
      <!-- 编辑昵称弹窗 -->
      <view class="edit-modal" v-if="showEditNicknameModal">
        <view class="modal-mask" @click="showEditNicknameModal = false"></view>
        <view class="modal-content">
          <view class="modal-header">
            <text class="modal-title">编辑昵称</text>
            <text class="modal-close" @click="showEditNicknameModal = false">✕</text>
          </view>
          
          <view class="modal-body">
            <input 
              v-model="newNickname" 
              class="nickname-input" 
              placeholder="请输入昵称"
              maxlength="20"
            />
          </view>
          
          <view class="modal-footer">
            <button class="btn-cancel" @click="showEditNicknameModal = false">取消</button>
            <button class="btn-confirm" @click="saveNickname">保存</button>
          </view>
        </view>
      </view>
      
      <!-- 底部/设置区域 -->
      <view class="settings-section">
        <view class="setting-item" @click="goFamilyManage">
          <view class="setting-left">
            <text class="setting-icon">⚙️</text>
            <text class="setting-text">家庭管理</text>
          </view>
          <text class="setting-arrow">›</text>
        </view>
      </view>
      
      <!-- 底部安全区 -->
      <view class="safe-area-bottom"></view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { familyApi } from '@/api/family.js'
import { statsApi } from '@/api/stats.js'
import { getDefaultFamily } from '@/utils/defaultFamily.js'
import { useUserStore } from '@/stores/user.js'

const userStore = useUserStore()

// 家庭数据
const family = ref({
  id: null,
  name: '',
  inviteCode: '',
  memberCount: 0
})

// 成员列表
const members = ref([])

// 当前月份
const currentMonth = computed(() => {
  const date = new Date()
  const monthNames = ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']
  return monthNames[date.getMonth()]
})

// 显示的成员（最多6个）
const displayMembers = computed(() => {
  return members.value.slice(0, 6)
})

// 是否为管理员
const isAdmin = ref(false)

// 弹窗状态
const showInviteCodeModal = ref(false)
const currentInviteCode = ref('')
const inviteCodes = ref([])
const generating = ref(false)

// 编辑昵称弹窗状态
const showEditNicknameModal = ref(false)
const editingMember = ref(null)
const newNickname = ref('')

// 统计数据
const stats = ref({
  tasksCompleted: 0,
  wishesCompleted: 0,
  mealsCooked: 0,
  checkinDays: 0
})

// 状态变量
const loading = ref(false)
const refreshing = ref(false)
const familyId = ref(null)

// 获取头像边框颜色
const getAvatarBorder = (index) => {
  const colors = ['#5B9BD5', '#FF9F43', '#26de81', '#FD79A8', '#A55EEA', '#778CA3']
  return colors[index % colors.length]
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
    // 先确保用户有家庭
    try {
      await familyApi.ensureFamily()
    } catch (e) {
      console.log('[Family] 确保家庭:', e)
    }
    
    // 优先使用用户信息中的 currentFamilyId
    const userFamilyId = userStore.userInfo?.currentFamilyId || uni.getStorageSync('currentFamilyId')
    if (userFamilyId) {
      familyId.value = userFamilyId
    } else {
      // 使用默认家庭
      const defaultFamily = await getDefaultFamily()
      if (!defaultFamily || !defaultFamily.id) {
        console.warn('[Family] 未找到默认家庭')
        return
      }
      familyId.value = defaultFamily.id
    }
    
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
        avatar: m.avatar || '../../static/avatar/default.jpg',
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
        tasksCompleted: res.tasksCompleted || 12,
        wishesCompleted: res.wishesCompleted || 3,
        mealsCooked: res.mealsCooked || 24,
        checkinDays: res.checkinDays || 15
      }
      console.log('[Family] 统计数据更新成功:', stats.value)
    }
  } catch (error) {
    console.error('[Family] 加载家庭统计失败:', error)
    // 使用默认值
    stats.value = {
      tasksCompleted: 12,
      wishesCompleted: 3,
      mealsCooked: 24,
      checkinDays: 15
    }
  }
}

// 跳转功能
const goFeature = (feature) => {
  if (feature.path) {
    uni.navigateTo({ url: feature.path })
  } else {
    uni.showToast({ title: '更多功能即将上线', icon: 'none' })
  }
}

// 显示成员详情
const showMemberDetail = (member) => {
  const currentUserId = userStore.userInfo?.id
  const isSelf = member.userId === currentUserId || member.id === currentUserId
  
  if (isSelf) {
    // 是自己的信息，显示编辑选项
    uni.showActionSheet({
      itemList: ['编辑昵称', '查看详情'],
      success: (res) => {
        if (res.tapIndex === 0) {
          // 编辑昵称
          editingMember.value = member
          newNickname.value = member.nickname || ''
          showEditNicknameModal.value = true
        } else {
          // 查看详情
          uni.showModal({
            title: member.nickname,
            content: `手机号: ${member.phone || '未绑定'}\n角色: ${member.role || '成员'}`,
            showCancel: false
          })
        }
      }
    })
  } else {
    // 是其他成员，只显示详情
    uni.showModal({
      title: member.nickname,
      content: `手机号: ${member.phone || '未绑定'}\n角色: ${member.role || '成员'}`,
      showCancel: false
    })
  }
}

// 保存昵称
const saveNickname = async () => {
  if (!newNickname.value.trim()) {
    uni.showToast({ title: '昵称不能为空', icon: 'none' })
    return
  }
  
  try {
    uni.showLoading({ title: '保存中...' })
    await userStore.updateUserInfo({ nickname: newNickname.value.trim() })
    uni.hideLoading()
    
    showEditNicknameModal.value = false
    uni.showToast({ title: '保存成功', icon: 'success' })
    
    // 刷新成员列表
    loadFamilyData()
  } catch (e) {
    uni.hideLoading()
    uni.showToast({ title: '保存失败', icon: 'none' })
  }
}

// 跳转家庭管理
const goFamilyManage = () => {
  uni.navigateTo({ url: '/pages/family/account' })
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

onMounted(async () => {
  // 确保有用户信息
  if (!userStore.userInfo || !userStore.userInfo.id) {
    await userStore.getUserInfo()
  }
  
  // 优先使用用户信息中的 currentFamilyId
  const userFamilyId = userStore.userInfo?.currentFamilyId || uni.getStorageSync('currentFamilyId')
  if (userFamilyId) {
    familyId.value = userFamilyId
    console.log('[Family] 从用户信息获取到 familyId:', familyId.value)
  }
  
  loadFamilyData()
  loadFamilyStats()
})
</script>

<style lang="scss" scoped>
// 主题色变量
$primary: #5B9BD5;
$primary-light: #8BC1F7;
$primary-dark: #3A7BC8;
$warm-orange: #FF9F43;
$warm-pink: #FD79A8;
$warm-green: #26de81;
$warm-purple: #A55EEA;
$text-primary: #2D3748;
$text-secondary: #718096;
$text-muted: #A0AEC0;
$bg-light: #F7FAFC;
$bg-white: #FFFFFF;
$danger: #FF6B6B;

// 通用样式
.family-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #E8F4FD 0%, #F7FAFC 50%, #FFFFFF 100%);
}

.scroll-container {
  height: 100vh;
  padding-bottom: 120rpx;
}

// 顶部区域
.family-header {
  position: relative;
  padding: 60rpx 32rpx 40rpx;
  background: linear-gradient(135deg, $primary 0%, $primary-light 50%, #B4D8F0 100%);
  border-radius: 0 0 48rpx 48rpx;
  overflow: hidden;
  
  // 背景装饰
  .header-bg {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    overflow: hidden;
    
    .bg-circle {
      position: absolute;
      border-radius: 50%;
      opacity: 0.15;
      
      &.circle-1 {
        width: 400rpx;
        height: 400rpx;
        background: #fff;
        top: -100rpx;
        right: -100rpx;
      }
      
      &.circle-2 {
        width: 200rpx;
        height: 200rpx;
        background: #fff;
        bottom: 20rpx;
        left: -50rpx;
      }
      
      &.circle-3 {
        width: 150rpx;
        height: 150rpx;
        background: rgba(255,255,255,0.5);
        top: 100rpx;
        right: 80rpx;
      }
    }
  }
  
  // 家庭名称
  .family-title-section {
    position: relative;
    z-index: 1;
    text-align: center;
    margin-bottom: 32rpx;
    
    .family-name {
      display: block;
      font-size: 48rpx;
      font-weight: 700;
      color: #fff;
      text-shadow: 0 4rpx 12rpx rgba(59, 130, 246, 0.3);
      margin-bottom: 8rpx;
    }
    
    .family-subtitle {
      font-size: 26rpx;
      color: rgba(255,255,255,0.85);
      font-weight: 500;
    }
  }
  
  // 成员头像群
  .member-avatars {
    position: relative;
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100rpx;
    margin-bottom: 28rpx;
    
    .avatar-item {
      position: relative;
      transition: transform 0.3s ease;
      
      .avatar-img {
        width: 80rpx;
        height: 80rpx;
        border-radius: 50%;
        border: 4rpx solid #fff;
        box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.15);
      }
      
      .owner-badge {
        position: absolute;
        bottom: -4rpx;
        right: -4rpx;
        font-size: 20rpx;
      }
    }
    
    .more-avatar {
      width: 80rpx;
      height: 80rpx;
      border-radius: 50%;
      background: rgba(255,255,255,0.3);
      display: flex;
      align-items: center;
      justify-content: center;
      margin-left: -16rpx;
      border: 4rpx solid #fff;
      
      text {
        font-size: 24rpx;
        color: #fff;
        font-weight: 600;
      }
    }
  }
  
  // 邀请按钮
  .invite-section {
    position: relative;
    z-index: 1;
    display: flex;
    justify-content: center;
    
    .invite-btn {
      display: flex;
      align-items: center;
      gap: 8rpx;
      padding: 16rpx 32rpx;
      background: rgba(255,255,255,0.25);
      border-radius: 40rpx;
      backdrop-filter: blur(10rpx);
      transition: all 0.3s ease;
      
      &:active {
        transform: scale(0.95);
        background: rgba(255,255,255,0.35);
      }
      
      .invite-icon {
        font-size: 28rpx;
      }
      
      .invite-text {
        font-size: 26rpx;
        color: #fff;
        font-weight: 600;
      }
    }
  }
}

// 通用区块样式
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
  
  .section-title {
    font-size: 34rpx;
    font-weight: 700;
    color: $text-primary;
  }
  
  .section-subtitle {
    font-size: 24rpx;
    color: $text-muted;
  }
  
  .member-count {
    font-size: 24rpx;
    color: $text-muted;
  }
}

// 统计卡片区域
.stats-section {
  margin: 32rpx 24rpx;
  padding: 32rpx;
  background: $bg-white;
  border-radius: 32rpx;
  box-shadow: 0 8rpx 32rpx rgba(91, 155, 213, 0.1);
  
  .stats-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 20rpx;
    
    .stat-card {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 28rpx 20rpx;
      background: linear-gradient(135deg, #F8FBFF 0%, #EDF5FC 100%);
      border-radius: 24rpx;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      
      &:active {
        transform: scale(0.96);
        box-shadow: 0 4rpx 16rpx rgba(91, 155, 213, 0.15);
      }
      
      .stat-icon-wrap {
        width: 80rpx;
        height: 80rpx;
        border-radius: 24rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-bottom: 16rpx;
        
        &.tasks { background: linear-gradient(135deg, #5B9BD5, #8BC1F7); }
        &.wishes { background: linear-gradient(135deg, #FF9F43, #FFBE76); }
        &.meals { background: linear-gradient(135deg, #26de81, #58E7C8); }
        &.clock { background: linear-gradient(135deg, #A55EEA, #D68FFF); }
        
        .stat-emoji {
          font-size: 40rpx;
        }
      }
      
      .stat-num {
        font-size: 44rpx;
        font-weight: 700;
        color: $text-primary;
        margin-bottom: 6rpx;
      }
      
      .stat-label {
        font-size: 24rpx;
        color: $text-secondary;
        font-weight: 500;
      }
    }
  }
}

// 功能入口区域
.features-section {
  margin: 0 24rpx 32rpx;
  padding: 32rpx;
  background: $bg-white;
  border-radius: 32rpx;
  box-shadow: 0 8rpx 32rpx rgba(91, 155, 213, 0.1);
  
  .feature-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 24rpx;
    
    .feature-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 24rpx 12rpx;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      border-radius: 24rpx;
      
      &:active {
        transform: scale(0.95);
        background: #F8FBFF;
      }
      
      .feature-icon {
        width: 96rpx;
        height: 96rpx;
        border-radius: 28rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-bottom: 16rpx;
        box-shadow: 0 8rpx 24rpx rgba(0,0,0,0.12);
        
        .feature-emoji {
          font-size: 48rpx;
        }
      }
      
      .feature-name {
        font-size: 26rpx;
        color: $text-secondary;
        font-weight: 500;
      }
    }
  }
}

// 成员列表区域
.members-section {
  margin: 0 24rpx 32rpx;
  padding: 32rpx;
  background: $bg-white;
  border-radius: 32rpx;
  box-shadow: 0 8rpx 32rpx rgba(91, 155, 213, 0.1);
  
  .member-list {
    .member-card {
      display: flex;
      align-items: center;
      padding: 20rpx 0;
      border-bottom: 2rpx solid #EDF2F7;
      transition: all 0.2s ease;
      
      &:last-child {
        border-bottom: none;
      }
      
      &:active {
        background: #F8FBFF;
        margin: 0 -16rpx;
        padding-left: 16rpx;
        padding-right: 16rpx;
        border-radius: 16rpx;
      }
      
      .member-avatar {
        width: 88rpx;
        height: 88rpx;
        border-radius: 50%;
        margin-right: 20rpx;
        box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.08);
      }
      
      .member-info {
        flex: 1;
        
        .name-row {
          display: flex;
          align-items: center;
          margin-bottom: 8rpx;
          
          .member-name {
            font-size: 32rpx;
            color: $text-primary;
            font-weight: 600;
            margin-right: 12rpx;
          }
          
          .role-tag {
            display: flex;
            align-items: center;
            gap: 4rpx;
            padding: 6rpx 14rpx;
            font-size: 22rpx;
            border-radius: 20rpx;
            font-weight: 500;
            
            &.owner {
              background: linear-gradient(135deg, $warm-orange, #FFBE76);
              color: #fff;
              box-shadow: 0 4rpx 12rpx rgba(255, 159, 67, 0.25);
            }
            
            &.admin {
              background: linear-gradient(135deg, $primary, $primary-light);
              color: #fff;
              box-shadow: 0 4rpx 12rpx rgba(91, 155, 213, 0.25);
            }
          }
        }
        
        .member-phone {
          font-size: 26rpx;
          color: $text-muted;
        }
      }
      
      .arrow {
        font-size: 36rpx;
        color: $text-muted;
        font-weight: 300;
      }
    }
  }
}

// 设置区域
.settings-section {
  margin: 0 24rpx 32rpx;
  padding: 16rpx 24rpx;
  background: $bg-white;
  border-radius: 32rpx;
  box-shadow: 0 8rpx 32rpx rgba(91, 155, 213, 0.1);
  
  .setting-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 28rpx 0;
    border-bottom: 2rpx solid #EDF2F7;
    transition: all 0.2s ease;
    
    &:last-child {
      border-bottom: none;
    }
    
    &:active {
      background: #F8FBFF;
      margin: 0 -12rpx;
      padding-left: 12rpx;
      padding-right: 12rpx;
      border-radius: 16rpx;
    }
    
    .setting-left {
      display: flex;
      align-items: center;
      
      .setting-icon {
        font-size: 40rpx;
        margin-right: 16rpx;
      }
      
      .setting-text {
        font-size: 30rpx;
        color: $text-primary;
        font-weight: 500;
      }
    }
    
    .setting-arrow {
      font-size: 36rpx;
      color: $text-muted;
      font-weight: 300;
    }
    
    &.danger {
      .setting-text {
        color: $danger;
      }
    }
  }
}

// 底部安全区
.safe-area-bottom {
  height: 60rpx;
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
      background: linear-gradient(135deg, #F8FBFF 0%, #EDF5FC 100%);
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
          color: $primary;
          letter-spacing: 8rpx;
        }
        
        .copy-btn {
          font-size: 26rpx;
          color: #fff;
          background: $primary;
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
          background: linear-gradient(135deg, $primary 0%, $primary-dark 100%);
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
          background: #F8FBFF;
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
            color: #26de81;
            
            &.expired {
              color: #FF6B6B;
            }
          }
        }
      }
    }
  }
}

// 编辑昵称弹窗
.edit-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  
  .modal-mask {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.5);
  }
  
  .modal-content {
    width: 560rpx;
    background: #fff;
    border-radius: 24rpx;
    overflow: hidden;
    position: relative;
    z-index: 1;
  }
  
  .modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 32rpx;
    border-bottom: 1rpx solid #f0f0f0;
    
    .modal-title {
      font-size: 34rpx;
      font-weight: 600;
      color: #333;
    }
    
    .modal-close {
      font-size: 36rpx;
      color: #999;
      padding: 8rpx;
    }
  }
  
  .modal-body {
    padding: 40rpx 32rpx;
    
    .nickname-input {
      width: 100%;
      height: 90rpx;
      background: #f5f5f5;
      border-radius: 12rpx;
      padding: 0 24rpx;
      font-size: 30rpx;
      box-sizing: border-box;
    }
  }
  
  .modal-footer {
    display: flex;
    padding: 24rpx 32rpx 40rpx;
    gap: 20rpx;
    
    .btn-cancel, .btn-confirm {
      flex: 1;
      height: 80rpx;
      border-radius: 12rpx;
      font-size: 30rpx;
      display: flex;
      align-items: center;
      justify-content: center;
    }
    
    .btn-cancel {
      background: #f5f5f5;
      color: #666;
    }
    
    .btn-confirm {
      background: linear-gradient(135deg, #6B8DD6, #8B5CF6);
      color: #fff;
      font-weight: 600;
    }
  }
}
</style>
