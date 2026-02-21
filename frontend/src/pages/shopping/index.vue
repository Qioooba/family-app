<template>
  <view class="shopping-page">
    <!-- 头部 -->
    <view class="shopping-header">
      <view class="header-title">智能购物</view>
      <view class="scan-btn" @click="scanCode">
        <text class="icon">📷</text>
        <text>扫码</text>
      </view>
    </view>

    <!-- 快捷操作 -->
    <view class="quick-actions">
      <view class="action-card" @click="createList"
      >
        <view class="action-icon" style="background: linear-gradient(135deg, #3b82f6, #2563eb);"
        >📝</view>
        <view class="action-info"
        >
          <text class="action-name">新建清单</text>
          <text class="action-desc">创建购物计划</text>
        </view>
      </view>
      
      <view class="action-card" @click="goInventory"
      >
        <view class="action-icon" style="background: linear-gradient(135deg, #10b981, #059669);"
        >📦</view>
        <view class="action-info"
        >
          <text class="action-name">库存管理</text>
          <text class="action-desc">查看家中存货</text>
        </view>
      </view>
    </view>

    <!-- 进行中的清单 -->
    <view class="section">
      <view class="section-header"
      >
        <text class="section-title">进行中的清单</text>
        <text class="section-more">查看全部 ›</text>
      </view>
      
      <view v-for="(list, index) in activeLists" :key="index" class="list-card"
      >
        <view class="list-header"
        >
          <view class="list-info"
          >
            <text class="list-name">{{ list.name }}</text>
            <text class="list-meta">{{ list.itemCount }}项 · 预计¥{{ list.estimatedAmount }}</text>
          </view>
          <view class="list-progress"
          >
            <view class="progress-ring"
          >
              <text class="progress-text">{{ list.progress }}%</text>
            </view>
          </view>
        </view>
        
        <view class="list-items"
        >
          <view 
            v-for="(item, i) in list.items.slice(0, 3)" 
            :key="i"
            class="list-item"
            :class="{ checked: item.checked }"
          >
            <view class="checkbox" :class="{ checked: item.checked }"></view>
            <text class="item-name">{{ item.name }}</text>
            <text class="item-qty">{{ item.quantity }}{{ item.unit }}</text>
          </view>
          <view v-if="list.items.length > 3" class="more-items"
          >
            还有 {{ list.items.length - 3 }} 项...
          </view>
        </view>
        
        <view class="list-footer"
        >
          <view class="assignee"
          >
            <text>指派给：{{ list.assignee }}</text>
          </view>
          <view class="list-actions"
          >
            <text class="action">编辑</text>
            <text class="action primary">去购买</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 临期提醒 -->
    <view class="section">
      <view class="section-header"
      >
        <text class="section-title">⏰ 临期提醒</text>
      </view>
      
      <view class="expiry-list"
      >
        <view v-for="(item, index) in expiringItems" :key="index" class="expiry-item"
        >
          <view class="expiry-icon" :class="{ urgent: item.days <= 3 }"
          >{{ item.days <= 3 ? '⚠️' : '⏰' }}</view>
          <view class="expiry-info"
          >
            <text class="expiry-name">{{ item.name }}</text>
            <text class="expiry-date" :class="{ urgent: item.days <= 3 }">
              {{ item.days <= 0 ? '已过期' : item.days + '天后过期' }}
            </text>
          </view>
          <view class="expiry-qty">{{ item.quantity }}{{ item.unit }}</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { shoppingApi } from '../../api/index.js'

const activeLists = ref([])
const expiringItems = ref([])
const loading = ref(false)

// 加载购物清单
const loadShoppingLists = async () => {
  loading.value = true
  try {
    const familyId = uni.getStorageSync('currentFamilyId') || 1
    const res = await shoppingApi.getLists(familyId)
    activeLists.value = res || []
  } catch (e) {
    console.error('加载购物清单失败', e)
  } finally {
    loading.value = false
  }
}

// 页面加载时获取数据
onMounted(() => {
  loadShoppingLists()
})

const createList = () => {
  uni.showModal({
    title: '新建购物清单',
    editable: true,
    placeholderText: '输入清单名称...',
    success: async (res) => {
      if (res.confirm && res.content) {
        try {
          const familyId = uni.getStorageSync('currentFamilyId') || 1
          await shoppingApi.createList({
            name: res.content,
            familyId: familyId
          })
          uni.showToast({ title: '创建成功', icon: 'success' })
          loadShoppingLists()
        } catch (e) {
          uni.showToast({ title: '创建失败', icon: 'none' })
        }
      }
    }
  })
}

const goInventory = () => {
  uni.showToast({ title: '库存管理开发中', icon: 'none' })
}

const scanCode = () => {
  uni.showToast({ title: '扫码功能开发中', icon: 'none' })
}
</script>

<style lang="scss" scoped>
.shopping-page {
  min-height: 100vh;
  background: #0f0f23;
  padding-bottom: 100px;
}

.shopping-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 50px 20px 20px;
  
  .header-title {
    font-size: 24px;
    font-weight: 700;
    color: #fff;
  }
  
  .scan-btn {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 10px 16px;
    background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
    border-radius: 20px;
    color: #fff;
    font-size: 14px;
    
    .icon {
      font-size: 18px;
    }
  }
}

.quick-actions {
  display: flex;
  gap: 12px;
  padding: 0 20px;
  margin-bottom: 20px;
  
  .action-card {
    flex: 1;
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 16px;
    background: rgba(255,255,255,0.05);
    border-radius: 16px;
    border: 1px solid rgba(255,255,255,0.08);
    
    .action-icon {
      width: 48px;
      height: 48px;
      border-radius: 14px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 24px;
    }
    
    .action-info {
      .action-name {
        display: block;
        font-size: 15px;
        font-weight: 600;
        color: #fff;
      }
      
      .action-desc {
        font-size: 12px;
        color: #64748b;
        margin-top: 4px;
      }
    }
  }
}

.section {
  margin-bottom: 20px;
  
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0 20px;
    margin-bottom: 12px;
    
    .section-title {
      font-size: 17px;
      font-weight: 600;
      color: #fff;
    }
    
    .section-more {
      font-size: 13px;
      color: #6366f1;
    }
  }
}

.list-card {
  margin: 0 20px 12px;
  padding: 18px;
  background: rgba(255,255,255,0.05);
  border-radius: 20px;
  border: 1px solid rgba(255,255,255,0.08);
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  
  .list-name {
    display: block;
    font-size: 16px;
    font-weight: 600;
    color: #fff;
  }
  
  .list-meta {
    font-size: 12px;
    color: #64748b;
    margin-top: 4px;
  }
  
  .progress-ring {
    width: 44px;
    height: 44px;
    border-radius: 50%;
    border: 3px solid rgba(99,102,241,0.3);
    border-top-color: #6366f1;
    display: flex;
    align-items: center;
    justify-content: center;
    
    .progress-text {
      font-size: 12px;
      font-weight: 600;
      color: #6366f1;
    }
  }
}

.list-items {
  padding: 12px;
  background: rgba(255,255,255,0.03);
  border-radius: 12px;
  margin-bottom: 15px;
  
  .list-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 8px 0;
    
    &.checked {
      opacity: 0.5;
    }
    
    .checkbox {
      width: 18px;
      height: 18px;
      border: 2px solid #64748b;
      border-radius: 50%;
      
      &.checked {
        background: #10b981;
        border-color: #10b981;
      }
    }
    
    .item-name {
      flex: 1;
      font-size: 14px;
      color: #e2e8f0;
      
      text-decoration: line-through;
      opacity: 0.5;
    }
    
    &:not(.checked) .item-name {
      text-decoration: none;
      opacity: 1;
    }
    
    .item-qty {
      font-size: 12px;
      color: #64748b;
    }
  }
  
  .more-items {
    text-align: center;
    padding: 8px;
    font-size: 12px;
    color: #64748b;
  }
}

.list-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  .assignee {
    font-size: 12px;
    color: #64748b;
  }
  
  .list-actions {
    display: flex;
    gap: 16px;
    
    .action {
      font-size: 13px;
      color: #64748b;
      
      &.primary {
        color: #6366f1;
        font-weight: 500;
      }
    }
  }
}

.expiry-list {
  padding: 0 20px;
}

.expiry-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px;
  background: rgba(255,255,255,0.05);
  border-radius: 12px;
  margin-bottom: 8px;
  
  .expiry-icon {
    font-size: 20px;
    
    &.urgent {
      animation: pulse 1.5s infinite;
    }
  }
  
  .expiry-info {
    flex: 1;
    
    .expiry-name {
      display: block;
      font-size: 14px;
      color: #fff;
    }
    
    .expiry-date {
      font-size: 12px;
      color: #f59e0b;
      margin-top: 2px;
      
      &.urgent {
        color: #ef4444;
      }
    }
  }
  
  .expiry-qty {
    font-size: 13px;
    color: #64748b;
  }
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}
</style>
