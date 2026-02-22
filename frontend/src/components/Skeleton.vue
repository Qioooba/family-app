<!-- Skeleton.vue - 骨架屏组件 -->
<template>
  <view class="skeleton" :class="{ active: loading }">
    <!-- 列表骨架 -->
    <view v-if="type === 'list'" class="skeleton-list"
003e
      <view v-for="i in rows" :key="i" class="skeleton-row"
003e
        <view class="skeleton-avatar"></view>
        <view class="skeleton-content"
003e
          <view class="skeleton-title"></view>
          <view class="skeleton-text"></view>
        </view>
      </view>
    </view>
    
    <!-- 卡片骨架 -->
    <view v-else-if="type === 'card'" class="skeleton-card"
003e
      <view class="skeleton-image"></view>
      <view class="skeleton-body"
003e
        <view class="skeleton-title"></view>
        <view class="skeleton-text short"></view>
      </view>
    </view>
    
    <!-- 详情骨架 -->
    <view v-else-if="type === 'detail'" class="skeleton-detail"
003e
      <view class="skeleton-header"></view>
      <view class="skeleton-section" v-for="i in 3" :key="i">
        <view class="skeleton-line"></view>
        <view class="skeleton-line short"></view>
      </view>
    </view>
  </view>
</template>

<script setup>
defineProps({
  loading: { type: Boolean, default: true },
  type: { type: String, default: 'list' },
  rows: { type: Number, default: 5 }
})
</script>

<style scoped>
.skeleton { display: none; }
.skeleton.active { display: block; }
.skeleton-row { display: flex; align-items: center; padding: 16px; background: #fff; margin-bottom: 1px; }
.skeleton-avatar { width: 44px; height: 44px; background: #f0f0f0; border-radius: 50%; margin-right: 12px; animation: pulse 1.5s infinite; }
.skeleton-content { flex: 1; }
.skeleton-title { height: 16px; background: #f0f0f0; border-radius: 4px; margin-bottom: 8px; width: 60%; animation: pulse 1.5s infinite; }
.skeleton-text { height: 12px; background: #f0f0f0; border-radius: 4px; width: 40%; animation: pulse 1.5s infinite; }
.skeleton-card { background: #fff; border-radius: 12px; overflow: hidden; }
.skeleton-image { height: 160px; background: #f0f0f0; animation: pulse 1.5s infinite; }
.skeleton-body { padding: 16px; }
.skeleton-header { height: 200px; background: #f0f0f0; animation: pulse 1.5s infinite; }
.skeleton-section { padding: 16px; }
.skeleton-line { height: 14px; background: #f0f0f0; border-radius: 4px; margin-bottom: 10px; animation: pulse 1.5s infinite; }
.skeleton-line.short { width: 50%; }
@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}
</style>
