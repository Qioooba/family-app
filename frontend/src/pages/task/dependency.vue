<template>
  <view class="page-container">
    <!-- 头部 -->
    <view class="header">
      <view class="header-left" @click="goBack">
        <text class="back-icon">‹</text>
      </view>
      <view class="header-title">任务依赖图</view>
      <view class="header-right">
        <view class="action-btn" @click="toggleLayout"
>
          <text class="btn-icon">🔄</text>
        </view>
        <view class="action-btn" @click="showHelp">
          <text class="btn-icon">❓</text>
        </view>
      </view>
    </view>
    
    <!-- 统计栏 -->
    <view class="stats-bar">
      <view class="stat-item">
        <text class="stat-value">{{ stats.total }}</text>
        <text class="stat-label">总任务</text>
      </view>
      <view class="stat-divider"></text>
      <view class="stat-item">
        <text class="stat-value">{{ stats.independent }}</text>
        <text class="stat-label">独立任务</text>
      </view>
      <view class="stat-divider"></text>
      <view class="stat-item"
>
        <text class="stat-value">{{ stats.withDependencies }}</text>
        <text class="stat-label">有依赖</text>
      </view>
      <view class="stat-divider"></text>
      <view class="stat-item">
        <text class="stat-value" :class="{ warning: stats.circular > 0 }">{{ stats.circular }}</text>
        <text class="stat-label">循环依赖</text>
      </view>
    </view>
    
    <!-- 依赖图主体 -->
    <view class="graph-container">
      <!-- 缩放控制 -->
      <view class="zoom-controls">
        <view class="zoom-btn" @click="zoomIn">+</view>
        <text class="zoom-level">{{ Math.round(zoom * 100) }}%</text>
        <view class="zoom-btn" @click="zoomOut">-</view>
        <view class="zoom-btn reset" @click="resetZoom">⟲</view>
      </view>
      
      <!-- 图例 -->
      <view class="legend">
        <view class="legend-item">
          <view class="legend-dot" style="background: #10B981;"></text>
          <text>已完成</text>
        </view>
        <view class="legend-item">
          <view class="legend-dot" style="background: #3B82F6;"></text>
          <text>进行中</text>
        </view>
        <view class="legend-item">
          <view class="legend-dot" style="background: #6B7280;"></text>
          <text>待办</text>
        </view>
        <view class="legend-item">
          <view class="legend-line"></text>
          <text>依赖关系</text>
        </view>
      </view>
      
      <!-- 图形画布 -->
      <scroll-view 
        class="graph-canvas-wrapper" 
        scroll-x 
        scroll-y
        :scroll-left="scrollLeft"
        :scroll-top="scrollTop"
      >
        <view 
          class="graph-canvas"
          :style="canvasStyle"
          @touchstart="onTouchStart"
          @touchmove="onTouchMove"
          @touchend="onTouchEnd"
        >
          <!-- SVG 连接线 -->
          <svg class="connections-layer" :style="{ width: canvasWidth + 'px', height: canvasHeight + 'px' }">
            <defs>
              <marker id="arrowhead" markerWidth="10" markerHeight="7" refX="28" refY="3.5" orient="auto">
                <polygon points="0 0, 10 3.5, 0 7" fill="#9CA3AF" />
              </marker>
              <marker id="arrowhead-highlight" markerWidth="10" markerHeight="7" refX="28" refY="3.5" orient="auto">
                <polygon points="0 0, 10 3.5, 0 7" fill="#3B82F6" />
              </marker>
            </defs>
            
            <g v-for="(conn, index) in connections" :key="index">
              <path
                :d="conn.path"
                :stroke="conn.highlighted ? '#3B82F6' : '#9CA3AF'"
                :stroke-width="conn.highlighted ? 3 : 2"
                fill="none"
                :marker-end="conn.highlighted ? 'url(#arrowhead-highlight)' : 'url(#arrowhead)'"
                :class="{ highlighted: conn.highlighted, animated: conn.animated }"
              />
            </g>
          </svg>
          
          <!-- 任务节点 -->
          <view 
            v-for="(node, index) in nodes" :key="node.id || index" 
            
            class="task-node"
            :class="{
              'status-done': node.status === 2,
              'status-doing': node.status === 1,
              'status-todo': node.status === 0,
              'has-dependency': node.dependencies?.length > 0,
              'is-dependency': node.isDependency,
              'selected': selectedNode?.id === node.id,
              'highlighted': highlightedNodes.includes(node.id)
            }"
            :style="getNodeStyle(node)"
            @click="selectNode(node)"
            @longpress="showNodeMenu(node)"
          >
            <view class="node-content">
              <text class="node-title">{{ truncateText(node.title, 6) }}</text>
              
              <view class="node-meta">
                <text v-if="node.dueDate" class="node-date">{{ formatDate(node.dueDate) }}</text>
                <view v-if="node.dependencies?.length > 0" class="dependency-badge"
>
                  <text>🔗{{ node.dependencies.length }}</text>
                </view>
              </view>
            </view>
            
            <!-- 依赖指示器 -->
            <view v-if="node.dependencies?.length > 0" class="dependency-indicator in"
>
              <text>←</text>
            </view>
            <view v-if="node.isDependency" class="dependency-indicator out">
              <text>→</text>
            </view>
          </view>
          
          <!-- 空状态 -->
          <view v-if="nodes.length === 0" class="graph-empty"
>
            <text class="empty-icon">🔗</text>
            <text class="empty-text">暂无任务依赖关系</text>
            <text class="empty-hint">在任务详情中设置前置任务来创建依赖</text>
          </view>
        </view>
      </scroll-view>
    </view>
    
    <!-- 选中节点详情 -->
    <view v-if="selectedNode" class="node-detail-panel"
>
      <view class="panel-header">
        <view class="panel-title">
          <view class="status-dot" :class="'status-' + selectedNode.status"></text>
          <text>{{ selectedNode.title }}</text>
        </view>
        <text class="close-btn" @click="selectedNode = null">✕</text>
      </view>
      
      <view class="panel-body">
        <!-- 前置依赖 -->
        <view class="dependency-section" v-if="selectedNode.dependencies?.length > 0">
          <text class="section-title">前置任务（需要先完成）</text>
          <view 
            v-for="dep in getDependencyTasks(selectedNode.dependencies)" 
            :key="dep.id"
            class="dependency-item"
            :class="{ completed: dep.status === 2 }"
            @click="highlightNode(dep.id)"
          >
            <text class="dep-title">{{ dep.title }}</text>
            <text class="dep-status" :class="'status-' + dep.status">{{ getStatusText(dep) }}</text>
          </view>
        </view>
        
        <!-- 后置影响 -->
        <view class="dependency-section" v-if="getDependentTasks(selectedNode.id).length > 0">
          <text class="section-title">后续任务（依赖于此）</text>
          <view 
            v-for="dep in getDependentTasks(selectedNode.id)" 
            :key="dep.id"
            class="dependency-item"
            :class="{ blocked: selectedNode.status !== 2 && dep.status !== 2 }"
            @click="highlightNode(dep.id)"
          >
            <text class="dep-title">{{ dep.title }}</text>
            <text class="dep-status" :class="'status-' + dep.status">{{ getStatusText(dep) }}</text>
          </view>
        </view>
        
        <view v-if="!selectedNode.dependencies?.length && !getDependentTasks(selectedNode.id).length">
          <view class="no-deps">
            <text>此任务没有依赖关系</text>
          </view>
        </view>
      </view>
      
      <view class="panel-footer">
        <view class="panel-btn primary" @click="goToTaskDetail(selectedNode)">查看详情</view>
        <view class="panel-btn" @click="addDependency">添加依赖</view>
      </view>
    </view>
    
    <!-- 底部提示 -->
    <view class="bottom-hint" v-if="!selectedNode">
      <text>点击任务查看详情 • 长按任务显示选项 • 双指缩放画布</text>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { taskApi } from '../../api/index.js'
import dayjs from 'dayjs'

// 常量
const NODE_WIDTH = 120
const NODE_HEIGHT = 70
const GRID_SIZE = 160

// 响应式数据
const tasks = ref([])
const layout = ref('tree') // 'tree' | 'list'
const zoom = ref(1)
const scrollLeft = ref(0)
const scrollTop = ref(0)
const selectedNode = ref(null)
const highlightedNodes = ref([])

// 触摸相关
const touchStart = ref({ x: 0, y: 0 })
const isDragging = ref(false)

// 模拟依赖数据
const mockDependencies = {
  // taskId: [dependencyTaskIds]
}

// 计算属性
const nodes = computed(() => {
  return tasks.value.map((task, index) => {
    const pos = calculateNodePosition(task, index)
    const dependencies = mockDependencies[task.id] || []
    
    return {
      ...task,
      x: pos.x,
      y: pos.y,
      dependencies,
      isDependency: tasks.value.some(t => 
        (mockDependencies[t.id] || []).includes(task.id)
      )
    }
  })
})

const connections = computed(() => {
  const conns = []
  
  nodes.value.forEach(node => {
    if (node.dependencies?.length > 0) {
      node.dependencies.forEach(depId => {
        const depNode = nodes.value.find(n => n.id === depId)
        if (depNode) {
          const isHighlighted = selectedNode.value && (
            selectedNode.value.id === node.id || 
            selectedNode.value.id === depId
          )
          
          conns.push({
            from: depNode,
            to: node,
            path: calculateConnectionPath(depNode, node),
            highlighted: isHighlighted,
            animated: depNode.status !== 2 && node.status !== 2
          })
        }
      })
    }
  })
  
  return conns
})

const canvasWidth = computed(() => {
  if (nodes.value.length === 0) return 800
  const maxX = Math.max(...nodes.value.map(n => n.x + NODE_WIDTH))
  return Math.max(maxX + 100, 800)
})

const canvasHeight = computed(() => {
  if (nodes.value.length === 0) return 600
  const maxY = Math.max(...nodes.value.map(n => n.y + NODE_HEIGHT))
  return Math.max(maxY + 100, 600)
})

const canvasStyle = computed(() => ({
  width: canvasWidth.value * zoom.value + 'px',
  height: canvasHeight.value * zoom.value + 'px',
  transform: `scale(${zoom.value})`,
  transformOrigin: '0 0'
}))

const stats = computed(() => {
  const total = tasks.value.length
  const withDeps = tasks.value.filter(t => 
    (mockDependencies[t.id] || []).length > 0
  ).length
  const independent = total - withDeps
  const circular = 0 // 需要检测算法
  
  return { total, independent, withDependencies: withDeps, circular }
})

// 方法
const loadTasks = async () => {
  try {
    const familyId = uni.getStorageSync('currentFamilyId') || 1
    const res = await taskApi.getList(familyId)
    tasks.value = res || []
    
    // 模拟一些依赖关系
    generateMockDependencies()
  } catch (e) {
    console.error('加载任务失败', e)
  }
}

const generateMockDependencies = () => {
  // 只为演示生成一些随机依赖
  if (tasks.value.length >= 3) {
    mockDependencies[tasks.value[2].id] = [tasks.value[0].id]
    if (tasks.value.length >= 5) {
      mockDependencies[tasks.value[4].id] = [tasks.value[2].id, tasks.value[1]?.id].filter(Boolean)
    }
  }
}

const calculateNodePosition = (task, index) => {
  if (layout.value === 'list') {
    return {
      x: 50,
      y: 50 + index * GRID_SIZE
    }
  }
  
  // 树形布局 - 简单版本
  const deps = mockDependencies[task.id] || []
  if (deps.length === 0) {
    // 根节点放在最左边
    const rootIndex = tasks.value.filter(t => 
      !(mockDependencies[t.id] || []).length
    ).indexOf(task)
    return {
      x: 50,
      y: 100 + rootIndex * GRID_SIZE * 1.5
    }
  }
  
  // 有依赖的节点放在依赖节点的右边
  const depNode = tasks.value.find(t => t.id === deps[0])
  const depIndex = tasks.value.indexOf(depNode)
  const depPos = calculateNodePosition(depNode, depIndex)
  
  return {
    x: depPos.x + GRID_SIZE,
    y: depPos.y + 80
  }
}

const calculateConnectionPath = (from, to) => {
  const startX = from.x + NODE_WIDTH
  const startY = from.y + NODE_HEIGHT / 2
  const endX = to.x
  const endY = to.y + NODE_HEIGHT / 2
  
  const midX = (startX + endX) / 2
  
  return `M ${startX} ${startY} C ${midX} ${startY}, ${midX} ${endY}, ${endX} ${endY}`
}

const getNodeStyle = (node) => ({
  left: node.x + 'px',
  top: node.y + 'px',
  width: NODE_WIDTH + 'px',
  height: NODE_HEIGHT + 'px'
})

const truncateText = (text, length) => {
  if (!text) return ''
  return text.length > length ? text.substring(0, length) + '...' : text
}

const formatDate = (date) => {
  if (!date) return ''
  return dayjs(date).format('MM-DD')
}

const getStatusText = (task) => {
  const map = { 0: '待办', 1: '进行中', 2: '已完成' }
  return map[task.status] || '待办'
}

const getDependencyTasks = (depIds) => {
  return depIds.map(id => tasks.value.find(t => t.id === id)).filter(Boolean)
}

const getDependentTasks = (taskId) => {
  return tasks.value.filter(t => 
    (mockDependencies[t.id] || []).includes(taskId)
  )
}

// 交互
const selectNode = (node) => {
  selectedNode.value = node
  highlightedNodes.value = [node.id, ...(node.dependencies || [])]
  
  // 高亮相关的依赖和依赖者
  const dependents = getDependentTasks(node.id)
  highlightedNodes.value.push(...dependents.map(d => d.id))
}

const highlightNode = (nodeId) => {
  const node = nodes.value.find(n => n.id === nodeId)
  if (node) selectNode(node)
}

const showNodeMenu = (node) => {
  uni.showActionSheet({
    itemList: ['查看详情', '添加依赖', '高亮相关任务'],
    success: (res) => {
      switch (res.tapIndex) {
        case 0:
          goToTaskDetail(node)
          break
        case 1:
          addDependency()
          break
        case 2:
          selectNode(node)
          break
      }
    }
  })
}

const goToTaskDetail = (node) => {
  uni.navigateTo({ url: `/pages/task/detail?id=${node.id}` })
}

const addDependency = () => {
  uni.showToast({ title: '添加依赖功能开发中', icon: 'none' })
}

// 缩放控制
const zoomIn = () => {
  if (zoom.value < 2) zoom.value += 0.1
}

const zoomOut = () => {
  if (zoom.value > 0.5) zoom.value -= 0.1
}

const resetZoom = () => {
  zoom.value = 1
  scrollLeft.value = 0
  scrollTop.value = 0
}

const toggleLayout = () => {
  layout.value = layout.value === 'tree' ? 'list' : 'tree'
  uni.showToast({ title: layout.value === 'tree' ? '切换为树形布局' : '切换为列表布局', icon: 'none' })
}

const showHelp = () => {
  uni.showModal({
    title: '任务依赖图使用帮助',
    content: `• 箭头表示任务依赖关系\n• 灰色：待办  蓝色：进行中  绿色：已完成\n• 点击任务查看依赖详情\n• 使用 +/- 按钮缩放画布`,
    showCancel: false
  })
}

// 触摸拖动
const onTouchStart = (e) => {
  const touch = e.touches[0]
  touchStart.value = { x: touch.clientX, y: touch.clientY }
  isDragging.value = false
}

const onTouchMove = (e) => {
  if (e.touches.length > 1) return // 多点触摸不处理
  
  const touch = e.touches[0]
  const dx = touch.clientX - touchStart.value.x
  const dy = touch.clientY - touchStart.value.y
  
  if (Math.abs(dx) > 5 || Math.abs(dy) > 5) {
    isDragging.value = true
  }
}

const onTouchEnd = () => {
  isDragging.value = false
}

const goBack = () => {
  uni.navigateBack()
}

onMounted(() => {
  loadTasks()
})
</script>

<style lang="scss" scoped>
.page-container {
  min-height: 100vh;
  background: #F8FAFC;
}

// 头部
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 50px 20px 20px;
  background: linear-gradient(135deg, #EC4899 0%, #DB2777 100%);
  
  .header-left {
    width: 40px;
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
    
    .back-icon {
      font-size: 32px;
      color: #fff;
      font-weight: 300;
    }
  }
  
  .header-title {
    font-size: 20px;
    font-weight: 600;
    color: #fff;
  }
  
  .header-right {
    display: flex;
    gap: 10px;
    
    .action-btn {
      width: 40px;
      height: 40px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: rgba(255,255,255,0.2);
      border-radius: 50%;
      
      .btn-icon {
        font-size: 16px;
      }
    }
  }
}

// 统计栏
.stats-bar {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  padding: 15px;
  background: #fff;
  margin: 10px 15px;
  border-radius: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  
  .stat-item {
    text-align: center;
    
    .stat-value {
      font-size: 22px;
      font-weight: 700;
      color: #EC4899;
      display: block;
      
      &.warning {
        color: #EF4444;
      }
    }
    
    .stat-label {
      font-size: 11px;
      color: #9CA3AF;
    }
  }
  
  .stat-divider {
    width: 1px;
    height: 24px;
    background: #E5E7EB;
  }
}

// 图形容器
.graph-container {
  position: relative;
  margin: 0 15px 15px;
  background: #fff;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  height: calc(100vh - 240px);
}

// 缩放控制
.zoom-controls {
  position: absolute;
  top: 15px;
  right: 15px;
  display: flex;
  align-items: center;
  gap: 8px;
  background: rgba(255,255,255,0.95);
  padding: 8px 12px;
  border-radius: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  z-index: 10;
  
  .zoom-btn {
    width: 28px;
    height: 28px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #F3F4F6;
    border-radius: 50%;
    font-size: 16px;
    color: #374151;
    
    &.reset {
      font-size: 14px;
    }
  }
  
  .zoom-level {
    font-size: 13px;
    color: #6B7280;
    min-width: 40px;
    text-align: center;
  }
}

// 图例
.legend {
  position: absolute;
  bottom: 15px;
  left: 15px;
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  background: rgba(255,255,255,0.95);
  padding: 10px 15px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  z-index: 10;
  
  .legend-item {
    display: flex;
    align-items: center;
    gap: 6px;
    
    .legend-dot {
      width: 10px;
      height: 10px;
      border-radius: 50%;
    }
    
    .legend-line {
      width: 16px;
      height: 2px;
      background: #9CA3AF;
    }
    
    text {
      font-size: 11px;
      color: #6B7280;
    }
  }
}

// 画布包装器
.graph-canvas-wrapper {
  width: 100%;
  height: 100%;
}

// 画布
.graph-canvas {
  position: relative;
  background: 
    linear-gradient(rgba(243, 244, 246, 0.5) 1px, transparent 1px),
    linear-gradient(90deg, rgba(243, 244, 246, 0.5) 1px, transparent 1px);
  background-size: 40px 40px;
}

// SVG 连接线层
.connections-layer {
  position: absolute;
  top: 0;
  left: 0;
  pointer-events: none;
  
  path {
    transition: all 0.3s;
    
    &.highlighted {
      filter: drop-shadow(0 0 4px rgba(59, 130, 246, 0.5));
    }
    
    &.animated {
      stroke-dasharray: 8;
      animation: dash 1s linear infinite;
    }
  }
}

@keyframes dash {
  to {
    stroke-dashoffset: -16;
  }
}

// 任务节点
.task-node {
  position: absolute;
  background: #fff;
  border-radius: 12px;
  padding: 10px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  border: 2px solid transparent;
  transition: all 0.3s;
  
  &:active {
    transform: scale(0.98);
  }
  
  &.status-done {
    border-color: #10B981;
    background: #F0FDF4;
  }
  
  &.status-doing {
    border-color: #3B82F6;
    background: #EFF6FF;
  }
  
  &.status-todo {
    border-color: #6B7280;
    background: #F9FAFB;
  }
  
  &.selected {
    border-color: #EC4899;
    box-shadow: 0 0 0 4px rgba(236, 72, 153, 0.2);
  }
  
  &.highlighted {
    transform: scale(1.05);
    z-index: 10;
  }
  
  .node-content {
    .node-title {
      font-size: 13px;
      font-weight: 600;
      color: #1F2937;
      display: block;
      margin-bottom: 6px;
      line-height: 1.3;
    }
    
    .node-meta {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .node-date {
        font-size: 10px;
        color: #9CA3AF;
      }
      
      .dependency-badge {
        font-size: 10px;
        color: #3B82F6;
        background: rgba(59, 130, 246, 0.1);
        padding: 2px 6px;
        border-radius: 6px;
      }
    }
  }
  
  .dependency-indicator {
    position: absolute;
    width: 16px;
    height: 16px;
    background: #9CA3AF;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    
    text {
      font-size: 10px;
      color: #fff;
    }
    
    &.in {
      left: -8px;
      top: 50%;
      transform: translateY(-50%);
    }
    
    &.out {
      right: -8px;
      top: 50%;
      transform: translateY(-50%);
    }
  }
}

// 空状态
.graph-empty {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  
  .empty-icon {
    font-size: 48px;
    margin-bottom: 12px;
    display: block;
  }
  
  .empty-text {
    font-size: 16px;
    color: #6B7280;
    margin-bottom: 8px;
    display: block;
  }
  
  .empty-hint {
    font-size: 12px;
    color: #9CA3AF;
  }
}

// 节点详情面板
.node-detail-panel {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  border-radius: 24px 24px 0 0;
  box-shadow: 0 -4px 20px rgba(0,0,0,0.1);
  z-index: 100;
  max-height: 50vh;
  
  .panel-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20px;
    border-bottom: 1px solid #F3F4F6;
    
    .panel-title {
      display: flex;
      align-items: center;
      gap: 10px;
      
      .status-dot {
        width: 10px;
        height: 10px;
        border-radius: 50%;
        
        &.status-0 { background: #6B7280; }
        &.status-1 { background: #3B82F6; }
        &.status-2 { background: #10B981; }
      }
      
      text {
        font-size: 17px;
        font-weight: 600;
        color: #1F2937;
      }
    }
    
    .close-btn {
      font-size: 20px;
      color: #9CA3AF;
    }
  }
  
  .panel-body {
    padding: 20px;
    max-height: 200px;
    overflow-y: auto;
  }
  
  .dependency-section {
    margin-bottom: 20px;
    
    &:last-child {
      margin-bottom: 0;
    }
    
    .section-title {
      font-size: 13px;
      color: #9CA3AF;
      margin-bottom: 10px;
      display: block;
    }
    
    .dependency-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px;
      background: #F9FAFB;
      border-radius: 12px;
      margin-bottom: 8px;
      
      &.completed {
        opacity: 0.6;
      }
      
      &.blocked {
        background: #FEF2F2;
      }
      
      .dep-title {
        font-size: 14px;
        color: #374151;
      }
      
      .dep-status {
        font-size: 11px;
        padding: 3px 8px;
        border-radius: 8px;
        
        &.status-0 {
          background: #F3F4F6;
          color: #6B7280;
        }
        
        &.status-1 {
          background: #DBEAFE;
          color: #2563EB;
        }
        &.status-2 {
          background: #D1FAE5;
          color: #059669;
        }
      }
    }
  }
  
  .no-deps {
    text-align: center;
    padding: 30px;
    
    text {
      font-size: 14px;
      color: #9CA3AF;
    }
  }
  
  .panel-footer {
    display: flex;
    gap: 12px;
    padding: 20px;
    border-top: 1px solid #F3F4F6;
    
    .panel-btn {
      flex: 1;
      padding: 14px;
      border-radius: 12px;
      text-align: center;
      font-size: 15px;
      font-weight: 500;
      background: #F3F4F6;
      color: #374151;
      
      &.primary {
        background: linear-gradient(135deg, #EC4899, #DB2777);
        color: #fff;
      }
    }
  }
}

// 底部提示
.bottom-hint {
  position: fixed;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(0,0,0,0.7);
  padding: 10px 20px;
  border-radius: 20px;
  
  text {
    font-size: 12px;
    color: #fff;
  }
}
</style>
