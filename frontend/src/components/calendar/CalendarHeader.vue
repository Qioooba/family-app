<template>
  <view class="calendar-header">
    <PageHeader 
      title="任务日历" 
      @back="$emit('back')"
      @action="$emit('add')"
    />
    <TaskStatsCards :stats="stats" />
  </view>
</template>

<script setup>
import PageHeader from '../common/PageHeader.vue'
import TaskStatsCards from '../task/TaskStatsCards.vue'

defineProps({
  stats: {
    type: Object,
    required: true
  }
})

defineEmits(['back', 'add'])
</script>

<style lang="scss" scoped>
.calendar-header {
  // 样式已在原组件中定义
}
</style>
