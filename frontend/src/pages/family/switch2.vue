<template>
  <view class="family-switch-page">
    <!-- 顶部导航 -->
    <view class="nav-bar">
      <view class="back-btn" @click="goBack">
        <up-icon name="arrow-left" size="40" color="#333"></up-icon>
      </view>
      <text class="title">切换家庭</text>
      <view class="right-btn" @click="showJoinModal">
        <text>加入</text>
      </view>
    </view>

    <view class="content">
      <!-- 当前家庭 -->
      <view class="current-section">
        <text class="section-label">当前家庭</text>
        
        <view class="current-family-card">
          <view class="family-avatar"
>
            <text class="family-icon">{{ currentFamily.icon }}</text>
            <view v-if="currentFamily.isDefault" class="default-badge">默认</view>
          </view>          
          
          <view class="family-info">
            <text class="family-name">{{ currentFamily.name }}</text>
            <text class="family-role">我是：{{ currentFamily.myRole }}</text>
            <view class="family-members"
>
              <view
                v-for="(member, index) in currentFamily.members.slice(0, 4)"
                :key="index"
                class="member-avatar-small"
              >
                <image :src="member.avatar" mode="aspectFill" />
              </view>
              
              <text v-if="currentFamily.members.length > 4" class="more-members"
>+{{ currentFamily.members.length - 4 }}</text>
            </view>
          </view>          
          
          <view class="current-tag">当前</view>
        </view>
      </view>

      <!-- 我的家庭列表 -->
      <view class="families-section">
        <view class="section-header">
          <text class="section-title">我的家庭</text>
          <text class="section-count">{{ families.length }}个</text>
        </view>

        <view class="family-list"
>
          <view
            v-for="family in otherFamilies"
            :key="family.id"
            class="family-item"
            @click="selectFamily(family)"
          >
            <view class="item-avatar">
              <text>{{ family.icon }}</text>
            </view>            
            
            <view class="item-info"
>
              <view class="item-header"
>
                <text class="item-name">{{ family.name }}</text>
                <view v-if="family.unread" class="unread-badge">{{ family.unread }}</view>
              </view>              
              
              <text class="item-role">{{ family.myRole }} · {{ family.members.length }}人</text>
              
              <text class="item-recent">{{ family.recentActivity }}</text>
            </view>            
            
            <view class="item-action"
>
              <text class="switch-btn">切换</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 快捷操作 -->
      <view class="quick-actions"
>
        <view class="action-card" @click="createFamily">
          <view class="action-icon create"
>
            <up-icon name="plus" size="40" color="#fff"></up-icon>
          </view>          
          
          <view class="action-info"
>
            <text class="action-title">创建新家庭</text>
            <text class="action-desc">创建属于您的家庭空间</text>
          </view>          
          <up-icon name="arrow-right" size="28" color="#ccc"></up-icon>
        </view>

        <view class="action-card" @click="showJoinModal"
>
          <view class="action-icon join"
>
            <up-icon name="user-plus" size="40" color="#fff"></up-icon>
          </view>          
          
          <view class="action-info"
>
            <text class="action-title">加入家庭</text>
            <text class="action-desc">通过邀请码加入</text>
          </view>          
          <up-icon name="arrow-right" size="28" color="#ccc"></up-icon>
        </view>
      </view>

      <!-- 家庭管理 -->
      <view class="management-section"
>
        <view class="section-title">家庭管理</view>
        
        <view class="manage-list">
          <view class="manage-item" @click="manageMembers"
>
            <up-icon name="account" size="36" color="#5B8FF9"></up-icon>
            <text class="item-label">成员管理</text>
            <text class="item-value">{{ currentFamily.members.length }}人</text>
            <up-icon name="arrow-right" size="28" color="#ccc"></up-icon>
          </view>          
          
          <view class="manage-item" @click="setDefault"
>
            <up-icon name="star" size="36" color="#FAAD14"></up-icon>
            <text class="item-label">设为默认</text>
            <text class="item-value">{{ currentFamily.isDefault ? '已是默认' : '' }}</text>
            <up-icon name="arrow-right" size="28" color="#ccc"></up-icon>
          </view>          
          
          <view class="manage-item" @click="familySettings"
>
            <up-icon name="setting" size="36" color="#52C41A"></up-icon>
            <text class="item-label">家庭设置</text>
            <up-icon name="arrow-right" size="28" color="#ccc"></up-icon>
          </view>          
          
          <view class="manage-item danger" @click="exitFamily">
            <up-icon name="logout" size="36" color="#FF4D4F"></up-icon>
            <text class="item-label">退出家庭</text>
            <up-icon name="arrow-right" size="28" color="#ccc"></up-icon>
          </view>
        </view>
      </view>
    </view>

    <!-- 切换确认弹窗 -->
    <up-popup
      v-model:show="switchModalVisible"
      mode="center"
    >
      <view class="switch-modal">
        <view class="modal-icon">🏠</view>        
        
        <text class="modal-title">切换家庭</text>        
        <text class="modal-desc">
          确定要切换到 "{{ selectedFamily?.name }}" 吗？
        </text>        
        
        <view class="modal-info"
>
          <text>角色：{{ selectedFamily?.myRole }}</text>
          <text>成员：{{ selectedFamily?.members.length }}人</text>
        </view>

        <view class="modal-actions"
>
          <view class="btn-cancel" @click="switchModalVisible = false">取消</view>          
          <view class="btn-confirm" @click="confirmSwitch">>切换</view>
        </view>
      </view>
    </up-popup>

    <!-- 加入家庭弹窗 -->
    <up-popup
      v-model:show="joinModalVisible"
      mode="bottom"
      round
      closeable
    >
      <view class="join-modal"
>
        <view class="modal-header"
>
          <text class="modal-title">加入家庭</text>
        </view>

        <view class="modal-body"
>
          <text class="input-label">请输入家庭邀请码</text>          
          
          <view class="code-input-wrap"
>
            <input
              v-model="inviteCode"
              placeholder="请输入6位邀请码"
              class="code-input"
              maxlength="6"
            />
          </view>          
          
          <text class="input-hint">邀请码可向家庭管理员索取</text>
        </view>

        <view class="modal-footer"
>
          <view class="btn-confirm" @click="joinFamily">加入家庭</view>
        </view>
      </view>
    </up-popup>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'

// 响应式数据
const currentFamilyId = ref(1)
const switchModalVisible = ref(false)
const joinModalVisible = ref(false)
const selectedFamily = ref(null)
const inviteCode = ref('')

// 当前家庭
const currentFamily = ref({
  id: 1,
  name: '幸福小家',
  icon: '🏠',
  myRole: '爸爸',
  isDefault: true,
  members: [
    { name: '爸爸', avatar: '/static/avatar/dad.png', role: 'owner' },
    { name: '妈妈', avatar: '/static/avatar/mom.png', role: 'member' },
    { name: '宝贝', avatar: '/static/avatar/kid.png', role: 'member' }
  ]
})

// 其他家庭列表
const families = ref([
  {
    id: 2,
    name: '爷爷奶奶家',
    icon: '🏡',
    myRole: '儿子',
    members: [
      { name: '爷爷', avatar: '/static/avatar/grandpa.png', role: 'owner' },
      { name: '奶奶', avatar: '/static/avatar/grandma.png', role: 'member' },
      { name: '爸爸', avatar: '/static/avatar/dad.png', role: 'member' }
    ],
    unread: 3,
    recentActivity: '爷爷发布了新动态'
  },
  {
    id: 3,
    name: '外公外婆家',
    icon: '🏘️',
    myRole: '女婿',
    members: [
      { name: '外公', avatar: '/static/avatar/grandpa2.png', role: 'owner' },
      { name: '外婆', avatar: '/static/avatar/grandma2.png', role: 'member' }
    ],
    unread: 0,
    recentActivity: '2天前更新'
  },
  {
    id: 4,
    name: '表姐家',
    icon: '🏢',
    myRole: '表弟',
    members: [
      { name: '表姐', avatar: '/static/avatar/cousin.png', role: 'owner' },
      { name: '表姐夫', avatar: '/static/avatar/cousin2.png', role: 'member' }
    ],
    unread: 1,
    recentActivity: '表姐分享了照片'
  }
])

// 计算属性
const otherFamilies = computed(() => {
  return families.value.filter(f => f.id !== currentFamilyId.value)
})

// 方法
const selectFamily = (family) => {
  selectedFamily.value = family
  switchModalVisible.value = true
}

const confirmSwitch = () => {
  switchModalVisible.value = false
  
  uni.showLoading({ title: '切换中...' })
  
  setTimeout(() => {
    uni.hideLoading()
    
    // 更新当前家庭
    const oldFamily = { ...currentFamily.value, id: currentFamilyId.value }
    const newFamily = selectedFamily.value
    
    // 交换位置
    currentFamily.value = { 
      ...newFamily, 
      isDefault: false,
      myRole: newFamily.myRole 
    }
    currentFamilyId.value = newFamily.id
    
    // 更新列表
    const index = families.value.findIndex(f => f.id === newFamily.id)
    if (index > -1) {
      families.value[index] = { 
        ...oldFamily, 
        unread: 0,
        recentActivity: '刚刚访问'
      }
    }
    
    uni.showToast({ 
      title: `已切换到${newFamily.name}`,
      icon: 'success'
    })
    
    // 通知首页刷新
    const pages = getCurrentPages()
    const indexPage = pages.find(p => p.route === 'pages/home/index')
    if (indexPage) {
      indexPage.$vm?.refreshFamilyData?.()
    }
  }, 1000)
}

const showJoinModal = () => {
  inviteCode.value = ''
  joinModalVisible.value = true
}

const joinFamily = () => {
  if (!inviteCode.value || inviteCode.value.length !== 6) {
    uni.showToast({ title: '请输入6位邀请码', icon: 'none' })
    return
  }
  
  joinModalVisible.value = false
  uni.showLoading({ title: '加入中...' })
  
  setTimeout(() => {
    uni.hideLoading()
    uni.showToast({ title: '加入成功', icon: 'success' })
    // 实际项目中这里会添加新家庭到列表
  }, 1500)
}

const createFamily = () => {
  uni.navigateTo({ url: '/pages/family/create' })
}

const manageMembers = () => {
  uni.navigateTo({ url: '/pages/family/members' })
}

const setDefault = () => {
  if (currentFamily.value.isDefault) {
    uni.showToast({ title: '已是默认家庭', icon: 'none' })
    return
  }
  
  uni.showModal({
    title: '设为默认',
    content: '是否将此家庭设为默认打开的家庭？',
    success: (res) => {
      if (res.confirm) {
        currentFamily.value.isDefault = true
        uni.showToast({ title: '设置成功', icon: 'success' })
      }
    }
  })
}

const familySettings = () => {
  uni.navigateTo({ url: '/pages/family/settings' })
}

const exitFamily = () => {
  uni.showModal({
    title: '退出家庭',
    content: `确定要退出 "${currentFamily.value.name}" 吗？退出后将无法查看该家庭的数据。`,
    confirmColor: '#FF4D4F',
    success: (res) => {
      if (res.confirm) {
        uni.showToast({ title: '已退出', icon: 'success' })
        // 实际项目中这里会调用退出接口
      }
    }
  })
}

const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.family-switch-page {
  min-height: 100vh;
  background: #f5f6fa;
}

.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 30rpx;
  padding-top: 60rpx;
  background: #fff;
  border-bottom: 1rpx solid #f0f0f0;

  .back-btn {
    width: 60rpx;
    height: 60rpx;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .title {
    font-size: 34rpx;
    font-weight: 600;
    color: #333;
  }

  .right-btn {
    padding: 10rpx 24rpx;
    background: #5B8FF9;
    border-radius: 30rpx;

    text {
      font-size: 26rpx;
      color: #fff;
    }
  }
}

.content {
  padding: 30rpx;
  padding-bottom: 50rpx;
}

// 当前家庭
.current-section {
  margin-bottom: 40rpx;

  .section-label {
    font-size: 26rpx;
    color: #999;
    margin-bottom: 16rpx;
    display: block;
  }
}

.current-family-card {
  display: flex;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 24rpx;
  padding: 30rpx;
  position: relative;

  .family-avatar {
    position: relative;
    width: 100rpx;
    height: 100rpx;
    background: rgba(255, 255, 255, 0.2);
    border-radius: 24rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 24rpx;

    .family-icon {
      font-size: 56rpx;
    }

    .default-badge {
      position: absolute;
      bottom: -8rpx;
      right: -8rpx;
      padding: 4rpx 12rpx;
      background: #FAAD14;
      border-radius: 12rpx;
      font-size: 20rpx;
      color: #fff;
    }
  }

  .family-info {
    flex: 1;

    .family-name {
      font-size: 36rpx;
      font-weight: 600;
      color: #fff;
      display: block;
      margin-bottom: 8rpx;
    }

    .family-role {
      font-size: 26rpx;
      color: rgba(255, 255, 255, 0.8);
      display: block;
      margin-bottom: 16rpx;
    }

    .family-members {
      display: flex;
      align-items: center;

      .member-avatar-small {
        width: 48rpx;
        height: 48rpx;
        border-radius: 50%;
        border: 2rpx solid #fff;
        margin-left: -12rpx;
        overflow: hidden;

        &:first-child {
          margin-left: 0;
        }

        image {
          width: 100%;
          height: 100%;
        }
      }

      .more-members {
        width: 48rpx;
        height: 48rpx;
        border-radius: 50%;
        background: rgba(255, 255, 255, 0.3);
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 20rpx;
        color: #fff;
        margin-left: -12rpx;
      }
    }
  }

  .current-tag {
    position: absolute;
    top: 20rpx;
    right: 20rpx;
    padding: 6rpx 16rpx;
    background: rgba(255, 255, 255, 0.2);
    border-radius: 8rpx;
    font-size: 22rpx;
    color: #fff;
  }
}

// 家庭列表
.families-section {
  margin-bottom: 40rpx;

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;

    .section-title {
      font-size: 30rpx;
      font-weight: 600;
      color: #333;
    }

    .section-count {
      font-size: 26rpx;
      color: #999;
    }
  }
}

.family-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.family-item {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;

  .item-avatar {
    width: 80rpx;
    height: 80rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #f5f6fa;
    border-radius: 16rpx;
    font-size: 40rpx;
    margin-right: 20rpx;
  }

  .item-info {
    flex: 1;
    min-width: 0;

    .item-header {
      display: flex;
      align-items: center;
      margin-bottom: 8rpx;

      .item-name {
        font-size: 30rpx;
        font-weight: 500;
        color: #333;
        margin-right: 12rpx;
      }

      .unread-badge {
        padding: 4rpx 12rpx;
        background: #FF4D4F;
        border-radius: 20rpx;
        font-size: 20rpx;
        color: #fff;
      }
    }

    .item-role {
      font-size: 24rpx;
      color: #999;
      display: block;
      margin-bottom: 4rpx;
    }

    .item-recent {
      font-size: 22rpx;
      color: #5B8FF9;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      display: block;
    }
  }

  .item-action {
    .switch-btn {
      padding: 12rpx 24rpx;
      background: #f0f5ff;
      border-radius: 24rpx;
      font-size: 26rpx;
      color: #5B8FF9;
    }
  }
}

// 快捷操作
.quick-actions {
  margin-bottom: 40rpx;
}

.action-card {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 16rpx;

  .action-icon {
    width: 80rpx;
    height: 80rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 16rpx;
    margin-right: 20rpx;

    &.create {
      background: linear-gradient(135deg, #52C41A 0%, #73D13D 100%);
    }

    &.join {
      background: linear-gradient(135deg, #1890FF 0%, #40A9FF 100%);
    }
  }

  .action-info {
    flex: 1;

    .action-title {
      font-size: 30rpx;
      font-weight: 500;
      color: #333;
      display: block;
      margin-bottom: 8rpx;
    }

    .action-desc {
      font-size: 24rpx;
      color: #999;
    }
  }
}

// 管理区域
.management-section {
  .section-title {
    font-size: 30rpx;
    font-weight: 600;
    color: #333;
    margin-bottom: 20rpx;
    display: block;
  }
}

.manage-list {
  background: #fff;
  border-radius: 16rpx;
  overflow: hidden;
}

.manage-item {
  display: flex;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1rpx solid #f5f5f5;

  &:last-child {
    border-bottom: none;
  }

  &.danger {
    .item-label {
      color: #FF4D4F;
    }
  }

  .item-label {
    flex: 1;
    font-size: 30rpx;
    color: #333;
    margin-left: 20rpx;
  }

  .item-value {
    font-size: 26rpx;
    color: #999;
    margin-right: 20rpx;
  }
}

// 切换弹窗
.switch-modal {
  width: 560rpx;
  background: #fff;
  border-radius: 24rpx;
  padding: 50rpx 40rpx;
  text-align: center;

  .modal-icon {
    font-size: 80rpx;
    margin-bottom: 20rpx;
  }

  .modal-title {
    font-size: 36rpx;
    font-weight: 600;
    color: #333;
    display: block;
    margin-bottom: 16rpx;
  }

  .modal-desc {
    font-size: 28rpx;
    color: #666;
    display: block;
    margin-bottom: 20rpx;
  }

  .modal-info {
    background: #f5f6fa;
    border-radius: 12rpx;
    padding: 20rpx;
    margin-bottom: 30rpx;

    text {
      font-size: 26rpx;
      color: #999;
      display: block;

      &:first-child {
        margin-bottom: 8rpx;
      }
    }
  }

  .modal-actions {
    display: flex;
    gap: 20rpx;

    .btn-cancel, .btn-confirm {
      flex: 1;
      padding: 24rpx 0;
      border-radius: 12rpx;
      font-size: 30rpx;
    }

    .btn-cancel {
      background: #f5f5f5;
      color: #666;
    }

    .btn-confirm {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: #fff;
    }
  }
}

// 加入弹窗
.join-modal {
  padding: 30rpx 0;

  .modal-header {
    text-align: center;
    padding: 0 30rpx 30rpx;
    border-bottom: 1rpx solid #f5f5f5;

    .modal-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
    }
  }

  .modal-body {
    padding: 30rpx;

    .input-label {
      font-size: 28rpx;
      color: #333;
      margin-bottom: 20rpx;
      display: block;
    }

    .code-input-wrap {
      background: #f5f6fa;
      border-radius: 12rpx;
      padding: 0 24rpx;
      margin-bottom: 16rpx;

      .code-input {
        height: 100rpx;
        text-align: center;
        font-size: 48rpx;
        font-weight: 600;
        color: #333;
        letter-spacing: 20rpx;
      }
    }

    .input-hint {
      font-size: 24rpx;
      color: #999;
      text-align: center;
      display: block;
    }
  }

  .modal-footer {
    padding: 0 30rpx;

    .btn-confirm {
      padding: 24rpx 0;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border-radius: 12rpx;
      text-align: center;
      font-size: 30rpx;
      color: #fff;
    }
  }
}
</style>