<template>
  <view class="filter-section">
    <view class="section-header">
      <view class="section-title">
        <text class="date-text">{{ dateText }}</text>
        <text class="task-count" v-if="taskCount > 0">({{ taskCount }})</text>
      </view>
      <view class="section-actions">
        <text class="filter-btn" @click="toggleFilter">
          {{ currentFilterLabel }}
        </text>
      </view>
    </view>
    
    <!-- 筛选下拉 -->
    <view v-if="showFilter" class="filter-dropdown">
      <text 
        v-for="filter in filters" 
        :key="filter.value"
        class="filter-option"
        :class="{ active: currentFilter === filter.value }"
        @click="selectFilter(filter.value)"
      >{{ filter.label }}</text>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  dateText: {
    type: String,
    required: true
  },
  taskCount: {
    type: Number,
    default: 0
  },
  currentFilter: {
    type: String,
    default: 'all'
  },
  showFilter: {
    type: Boolean,
    default: false
  }
})

const filters = [
  { label: '全部', value: 'all' },
  { label: '待办', value: 'pending' },
  { label: '已完成', value: 'completed' }
]

const currentFilterLabel = computed(() => {
  const filter = filters.find(f => f.value === props.currentFilter)
  return filter ? filter.label : '全部'
})

const emit = defineEmits(['update:showFilter', 'selectFilter'])

const toggleFilter = () => {
  emit('update:showFilter', !props.showFilter)
}

const selectFilter = (filter) => {
  emit('selectFilter', filter)
  emit('update:showFilter', false)
}
</script>

<style lang="scss" scoped>
.filter-section {
  margin-bottom: 15px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  
  .section-title {
    display: flex;
    align-items: center;
    gap: 8px;
    
    .date-text {
      font-size: 18px;
      font-weight: 600;
      color: #1F2937;
    }
    
    .task-count {
      font-size: 14px;
      color: #9CA3AF;
    }
  }
  
  .filter-btn {
    font-size: 13px;
    color: #667EEA;
    background: rgba(102, 126, 234, 0.1);
    padding: 6px 12px;
    border-radius: 16px;
  }
}

.filter-dropdown {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
  padding-bottom: 15px;
  border-bottom: 1px solid #F3F4F6;
  
  .filter-option {
    font-size: 13px;
    color: #6B7280;
    padding: 6px 14px;
    background: #F3F4F6;
    border-radius: 16px;
    
    &.active {
      background: #667EEA;
      color: #fff;
    }
  }
}
</style>
