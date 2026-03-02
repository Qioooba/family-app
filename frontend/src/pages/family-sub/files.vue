<template>
  <view class="files-page">
    <!-- 自定义导航栏 -->
    <up-navbar title="文件共享" :autoBack="true" bgColor="#fff" :fixed="true" :placeholder="true"></up-navbar>
    
    <!-- 存储空间信息 -->
    <view class="storage-card">
      <view class="storage-info">
        <view class="storage-icon">
          <u-icon name="folder-fill" size="60" color="#667eea"></u-icon>
        </view>
        <view class="storage-detail">
          <text class="storage-title">家庭云存储</text>
          <text class="storage-used">已用 {{ usedSpace }} / {{ totalSpace }}</text>
        </view>
        <view class="storage-percent">{{ usagePercent }}%</view>
      </view>
      <view class="storage-progress">
        <view class="progress-bg">
          <view 
            class="progress-fill" 
            :style="{ width: usagePercent + '%' }"
            :class="{ warning: usagePercent > 80 }"
          ></view>
        </view>
      </view>
    </view>
    
    <!-- 工具栏 -->
    <view class="toolbar">
      <view class="toolbar-left">
        <view class="breadcrumb">
          <text 
            v-for="(crumb, index) in breadcrumbs" 
            :key="index"
            :class="{ active: index === breadcrumbs.length - 1 }"
            @click="navigateTo(index)"
          >
            {{ crumb.name }}
            <text v-if="index < breadcrumbs.length - 1" class="separator">/</text>
          </text>
        </view>
      </view>
      <view class="toolbar-right">
        <view class="view-toggle" @click="toggleView">
          <u-icon :name="viewMode === 'grid' ? 'grid' : 'list'" size="36" color="#666"></u-icon>
        </view>
        <view class="more-btn" @click="showMoreActions">
          <u-icon name="more-dot-fill" size="36" color="#666"></u-icon>
        </view>
      </view>
    </view>
    
    <!-- 搜索栏 -->
    <view class="search-bar">
      <up-search
        v-model="searchKeyword"
        placeholder="搜索文件或文件夹"
        :show-action="false"
        @search="onSearch"
        @clear="clearSearch"
      ></up-search>
    </view>
    
    <!-- 文件列表 - 网格视图 -->
    <scroll-view 
      v-if="viewMode === 'grid'"
      scroll-y 
      class="file-content"
      @scrolltolower="loadMore"
    >
      <view class="file-grid">
        <view 
          v-for="item in filteredFiles" 
          :key="item.id"
          class="file-card"
          :class="{ selected: selectedItems.includes(item.id) }"
          @click="handleFileClick(item)"
          @longpress="handleLongPress(item)"
        >
          <view class="file-checkbox" v-if="isSelectMode" @click.stop="toggleSelect(item)">
            <view class="checkbox" :class="{ checked: selectedItems.includes(item.id) }">
              <u-icon v-if="selectedItems.includes(item.id)" name="checkbox-mark" size="20" color="#fff"></u-icon>
            </view>
          </view>
          <view class="file-icon">
            <image v-if="item.type === 'image'" :src="item.thumbnail" mode="aspectFill" class="thumb-img" />
            <u-icon v-else :name="getFileIcon(item)" size="80" :color="getFileColor(item)"></u-icon>
          </view>
          <view class="file-info">
            <text class="file-name" :class="{ folder: item.isFolder }">{{ item.name }}</text>
            <view class="file-meta">
              <text v-if="!item.isFolder">{{ formatSize(item.size) }}</text>
              <text v-else>{{ item.count }}项</text>
              <text class="file-date">{{ item.date }}</text>
            </view>
          </view>
        </view>
      </view>
      
      <!-- 空状态 -->
      <view v-if="filteredFiles.length === 0" class="empty-state">
        <u-icon name="folder-open" size="120" color="#ddd"></u-icon>
        <text class="empty-text">暂无文件</text>
        <text class="empty-hint">点击右上角上传文件</text>
      </view>
      
      <up-loadmore :status="loadStatus" />
    </scroll-view>
    
    <!-- 文件列表 - 列表视图 -->
    <scroll-view 
      v-else
      scroll-y 
      class="file-content"
      @scrolltolower="loadMore"
    >
      <view class="file-list">
        <view 
          v-for="item in filteredFiles" 
          :key="item.id"
          class="file-row"
          :class="{ selected: selectedItems.includes(item.id) }"
          @click="handleFileClick(item)"
          @longpress="handleLongPress(item)"
        >
          <view class="row-checkbox" v-if="isSelectMode" @click.stop="toggleSelect(item)">
            <view class="checkbox" :class="{ checked: selectedItems.includes(item.id) }">
              <u-icon v-if="selectedItems.includes(item.id)" name="checkbox-mark" size="20" color="#fff"></u-icon>
            </view>
          </view>
          <view class="row-icon">
            <image v-if="item.type === 'image'" :src="item.thumbnail" mode="aspectFill" />
            <u-icon v-else :name="getFileIcon(item)" size="50" :color="getFileColor(item)"></u-icon>
          </view>
          <view class="row-info">
            <text class="row-name">{{ item.name }}</text>
            <text class="row-meta">{{ formatSize(item.size) }} · {{ item.date }}</text>
          </view>
          <view class="row-action" @click.stop="showFileActions(item)">
            <u-icon name="more-dot-fill" size="40" color="#999"></u-icon>
          </view>
        </view>
      </view>
      
      <view v-if="filteredFiles.length === 0" class="empty-state">
        <u-icon name="folder-open" size="120" color="#ddd"></u-icon>
        <text class="empty-text">暂无文件</text>
      </view>
      
      <up-loadmore :status="loadStatus" />
    </scroll-view>
    
    <!-- 底部操作栏 -->
    <view class="bottom-bar" v-if="isSelectMode">
      <view class="select-info">
        <text>已选择 {{ selectedItems.length }} 项</text>
        <text class="select-all" @click="selectAll">{{ isAllSelected ? '取消全选' : '全选' }}</text>
      </view>
      <view class="action-btns">
        <view class="action-btn" @click="downloadSelected">
          <u-icon name="download" size="36" color="#5B8FF9"></u-icon>
          <text>下载</text>
        </view>
        <view class="action-btn" @click="moveSelected">
          <u-icon name="folder" size="36" color="#FAAD14"></u-icon>
          <text>移动</text>
        </view>
        <view class="action-btn" @click="deleteSelected">
          <u-icon name="trash" size="36" color="#FF4D4F"></u-icon>
          <text>删除</text>
        </view>
      </view>
    </view>
    
    <!-- 悬浮上传按钮 -->
    <view class="fab-upload" v-if="!isSelectMode" @click="showUploadMenu">
      <u-icon name="plus" size="48" color="#fff"></u-icon>
    </view>
    
    <!-- 新建文件夹弹窗 -->
    <up-popup :show="showFolderPopup" mode="center" @close="showFolderPopup = false" round="20">
      <view class="folder-popup">
        <text class="popup-title">新建文件夹</text>
        <input 
          v-model="newFolderName" 
          placeholder="请输入文件夹名称"
          class="folder-input"
          focus
        />
        <view class="popup-actions">
          <button class="dialog-btn cancel" @click="showFolderPopup = false">取消</button>
          <button class="dialog-btn primary" @click="createFolder">确定</button>
        </view>
      </view>
    </up-popup>
    
    <!-- 重命名弹窗 -->
    <up-popup :show="showRenamePopup" mode="center" @close="showRenamePopup = false" round="20">
      <view class="folder-popup">
        <text class="popup-title">重命名</text>
        <input 
          v-model="renameValue" 
          placeholder="请输入新名称"
          class="folder-input"
          focus
        />
        <view class="popup-actions">
          <button class="dialog-btn cancel" @click="showRenamePopup = false">取消</button>
          <button class="dialog-btn primary" @click="confirmRename">确定</button>
        </view>
      </view>
    </up-popup>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'

// 数据
const viewMode = ref('grid')
const searchKeyword = ref('')
const isSelectMode = ref(false)
const selectedItems = ref([])
const loadStatus = ref('loadmore')
const showFolderPopup = ref(false)
const showRenamePopup = ref(false)
const newFolderName = ref('')
const renameValue = ref('')
const currentRenameItem = ref(null)

// 存储信息
const usedSpaceBytes = ref(15.6 * 1024 * 1024 * 1024) // 15.6GB
const totalSpaceBytes = ref(50 * 1024 * 1024 * 1024) // 50GB

const usedSpace = computed(() => formatSize(usedSpaceBytes.value))
const totalSpace = computed(() => formatSize(totalSpaceBytes.value))
const usagePercent = computed(() => {
  return Math.round((usedSpaceBytes.value / totalSpaceBytes.value) * 100)
})

// 面包屑
const breadcrumbs = ref([
  { id: 0, name: '全部文件' }
])

// 文件数据
const files = ref([
  { id: 1, name: '旅行照片', isFolder: true, count: 128, date: '2024-02-20', type: 'folder', size: 0 },
  { id: 2, name: '家庭文档', isFolder: true, count: 36, date: '2024-02-18', type: 'folder', size: 0 },
  { id: 3, name: '宝宝视频', isFolder: true, count: 58, date: '2024-02-15', type: 'folder', size: 0 },
  { id: 4, name: '春节聚会.jpg', isFolder: false, type: 'image', size: 3.5 * 1024 * 1024, date: '2024-02-10', thumbnail: '/static/files/photo1.jpg' },
  { id: 5, name: '家庭预算表.xlsx', isFolder: false, type: 'excel', size: 256 * 1024, date: '2024-02-08' },
  { id: 6, name: '宝贝成长记录.pdf', isFolder: false, type: 'pdf', size: 5.2 * 1024 * 1024, date: '2024-02-05' },
  { id: 7, name: '海南旅游攻略.docx', isFolder: false, type: 'word', size: 1.8 * 1024 * 1024, date: '2024-02-01' },
  { id: 8, name: '生日派对.mp4', isFolder: false, type: 'video', size: 256 * 1024 * 1024, date: '2024-01-28' },
  { id: 9, name: '合同扫描件.pdf', isFolder: false, type: 'pdf', size: 2.1 * 1024 * 1024, date: '2024-01-25' },
  { id: 10, name: '音乐合集', isFolder: true, count: 45, date: '2024-01-20', type: 'folder', size: 0 }
])

// 搜索过滤
const filteredFiles = computed(() => {
  if (!searchKeyword.value) return files.value
  const keyword = searchKeyword.value.toLowerCase()
  return files.value.filter(f => f.name.toLowerCase().includes(keyword))
})

const isAllSelected = computed(() => {
  return filteredFiles.value.length > 0 && selectedItems.value.length === filteredFiles.value.length
})

// 获取文件图标
const getFileIcon = (item) => {
  if (item.isFolder) return 'folder-fill'
  const icons = {
    image: 'photo-fill',
    video: 'movie',
    pdf: 'file-text-fill',
    word: 'file-text',
    excel: 'grid-fill',
    audio: 'volume-up',
    zip: 'folder-open'
  }
  return icons[item.type] || 'file-text'
}

const getFileColor = (item) => {
  if (item.isFolder) return '#FAAD14'
  const colors = {
    image: '#FF6B6B',
    video: '#722ED1',
    pdf: '#FF4D4F',
    word: '#5B8FF9',
    excel: '#52C41A',
    audio: '#EB2F96'
  }
  return colors[item.type] || '#999'
}

// 格式化大小
const formatSize = (bytes) => {
  if (bytes === 0) return '-'
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  let i = 0
  while (bytes >= 1024 && i < units.length - 1) {
    bytes /= 1024
    i++
  }
  return bytes.toFixed(1) + ' ' + units[i]
}

// 切换视图
const toggleView = () => {
  viewMode.value = viewMode.value === 'grid' ? 'list' : 'grid'
}

// 文件点击
const handleFileClick = (item) => {
  if (isSelectMode.value) {
    toggleSelect(item)
    return
  }
  
  if (item.isFolder) {
    // 进入文件夹
    breadcrumbs.value.push({ id: item.id, name: item.name })
    uni.showToast({ title: `进入${item.name}`, icon: 'none' })
  } else if (item.type === 'image') {
    // 预览图片
    uni.previewImage({ urls: [item.thumbnail], current: 0 })
  } else {
    // 打开文件
    uni.showModal({
      title: '打开文件',
      content: `是否打开 ${item.name}？`,
      success: (res) => {
        if (res.confirm) {
          uni.showToast({ title: '正在打开...', icon: 'loading' })
        }
      }
    })
  }
}

// 长按
const handleLongPress = (item) => {
  if (!isSelectMode.value) {
    isSelectMode.value = true
    selectedItems.value = [item.id]
  }
}

// 选择
const toggleSelect = (item) => {
  const idx = selectedItems.value.indexOf(item.id)
  if (idx > -1) {
    selectedItems.value.splice(idx, 1)
  } else {
    selectedItems.value.push(item.id)
  }
  
  if (selectedItems.value.length === 0) {
    isSelectMode.value = false
  }
}

// 全选
const selectAll = () => {
  if (isAllSelected.value) {
    selectedItems.value = []
  } else {
    selectedItems.value = filteredFiles.value.map(f => f.id)
  }
}

// 搜索
const onSearch = () => {
  // 搜索逻辑
}

const clearSearch = () => {
  searchKeyword.value = ''
}

// 更多操作
const showMoreActions = () => {
  uni.showActionSheet({
    itemList: ['上传文件', '新建文件夹', '排序方式'],
    success: (res) => {
      switch (res.tapIndex) {
        case 0:
          uploadFile()
          break
        case 1:
          showFolderPopup.value = true
          break
        case 2:
          showSortOptions()
          break
      }
    }
  })
}

// 上传
const showUploadMenu = () => {
  uni.showActionSheet({
    itemList: ['从相册选择', '从文件选择', '新建文件夹'],
    success: (res) => {
      switch (res.tapIndex) {
        case 0:
          uploadFromAlbum()
          break
        case 1:
          uploadFile()
          break
        case 2:
          showFolderPopup.value = true
          break
      }
    }
  })
}

const uploadFromAlbum = () => {
  uni.chooseImage({
    count: 9,
    success: (res) => {
      uni.showLoading({ title: '上传中...' })
      setTimeout(() => {
        res.tempFilePaths.forEach((path, index) => {
          files.value.unshift({
            id: Date.now() + index,
            name: `图片_${Date.now()}_${index}.jpg`,
            isFolder: false,
            type: 'image',
            size: 2.5 * 1024 * 1024,
            date: new Date().toISOString().split('T')[0],
            thumbnail: path
          })
        })
        usedSpaceBytes.value += res.tempFilePaths.length * 2.5 * 1024 * 1024
        uni.hideLoading()
        uni.showToast({ title: '上传成功', icon: 'success' })
      }, 1500)
    }
  })
}

const uploadFile = () => {
  uni.chooseMessageFile({
    count: 5,
    type: 'all',
    success: (res) => {
      uni.showLoading({ title: '上传中...' })
      setTimeout(() => {
        res.tempFiles.forEach((file, index) => {
          files.value.unshift({
            id: Date.now() + index,
            name: file.name,
            isFolder: false,
            type: getFileType(file.name),
            size: file.size,
            date: new Date().toISOString().split('T')[0]
          })
        })
        usedSpaceBytes.value += res.tempFiles.reduce((sum, f) => sum + f.size, 0)
        uni.hideLoading()
        uni.showToast({ title: '上传成功', icon: 'success' })
      }, 1500)
    }
  })
}

const getFileType = (filename) => {
  const ext = filename.split('.').pop().toLowerCase()
  const types = {
    pdf: 'pdf',
    doc: 'word', docx: 'word',
    xls: 'excel', xlsx: 'excel',
    mp4: 'video', mov: 'video', avi: 'video',
    mp3: 'audio', wav: 'audio',
    jpg: 'image', jpeg: 'image', png: 'image', gif: 'image',
    zip: 'zip', rar: 'zip'
  }
  return types[ext] || 'file'
}

// 新建文件夹
const createFolder = () => {
  if (!newFolderName.value.trim()) {
    uni.showToast({ title: '请输入文件夹名称', icon: 'none' })
    return
  }
  files.value.unshift({
    id: Date.now(),
    name: newFolderName.value,
    isFolder: true,
    count: 0,
    date: new Date().toISOString().split('T')[0],
    type: 'folder',
    size: 0
  })
  showFolderPopup.value = false
  newFolderName.value = ''
  uni.showToast({ title: '创建成功', icon: 'success' })
}

// 文件操作
const showFileActions = (item) => {
  const actions = ['重命名', '移动', '下载', '删除']
  uni.showActionSheet({
    itemList: actions,
    success: (res) => {
      switch (res.tapIndex) {
        case 0:
          currentRenameItem.value = item
          renameValue.value = item.name
          showRenamePopup.value = true
          break
        case 1:
          moveItem(item)
          break
        case 2:
          downloadItem(item)
          break
        case 3:
          deleteItem(item)
          break
      }
    }
  })
}

const confirmRename = () => {
  if (currentRenameItem.value && renameValue.value.trim()) {
    currentRenameItem.value.name = renameValue.value
    showRenamePopup.value = false
    uni.showToast({ title: '重命名成功', icon: 'success' })
  }
}

const moveItem = (item) => {
  uni.showToast({ title: '选择目标文件夹', icon: 'none' })
}

const downloadItem = (item) => {
  uni.showLoading({ title: '下载中...' })
  setTimeout(() => {
    uni.hideLoading()
    uni.showToast({ title: '下载完成', icon: 'success' })
  }, 1000)
}

const deleteItem = (item) => {
  uni.showModal({
    title: '确认删除',
    content: `确定删除 ${item.name} 吗？`,
    confirmColor: '#FF4D4F',
    success: (res) => {
      if (res.confirm) {
        const idx = files.value.findIndex(f => f.id === item.id)
        if (idx > -1) {
          if (!files.value[idx].isFolder) {
            usedSpaceBytes.value -= files.value[idx].size
          }
          files.value.splice(idx, 1)
          uni.showToast({ title: '已删除', icon: 'success' })
        }
      }
    }
  })
}

// 批量操作
const downloadSelected = () => {
  uni.showLoading({ title: '打包下载中...' })
  setTimeout(() => {
    uni.hideLoading()
    isSelectMode.value = false
    selectedItems.value = []
    uni.showToast({ title: '下载完成', icon: 'success' })
  }, 1500)
}

const moveSelected = () => {
  uni.showToast({ title: '选择目标文件夹', icon: 'none' })
}

const deleteSelected = () => {
  uni.showModal({
    title: '确认删除',
    content: `确定删除选中的 ${selectedItems.value.length} 项吗？`,
    confirmColor: '#FF4D4F',
    success: (res) => {
      if (res.confirm) {
        files.value = files.value.filter(f => !selectedItems.value.includes(f.id))
        isSelectMode.value = false
        selectedItems.value = []
        uni.showToast({ title: '已删除', icon: 'success' })
      }
    }
  })
}

// 排序
const showSortOptions = () => {
  uni.showActionSheet({
    itemList: ['按名称排序', '按时间排序', '按大小排序'],
    success: (res) => {
      const sorts = ['name', 'date', 'size']
      uni.showToast({ title: '已按' + ['名称', '时间', '大小'][res.tapIndex] + '排序', icon: 'none' })
    }
  })
}

// 导航
const navigateTo = (index) => {
  breadcrumbs.value = breadcrumbs.value.slice(0, index + 1)
}

const loadMore = () => {
  loadStatus.value = 'loading'
  setTimeout(() => {
    loadStatus.value = 'nomore'
  }, 500)
}
</script>

<style lang="scss" scoped>
.files-page {
  min-height: 100vh;
  background: #f5f6fa;
}

.storage-card {
  margin: 20rpx 30rpx;
  padding: 30rpx;
  background: #fff;
  border-radius: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.06);
  
  .storage-info {
    display: flex;
    align-items: center;
    margin-bottom: 20rpx;
    
    .storage-icon {
      width: 100rpx;
      height: 100rpx;
      background: #f0f5ff;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 24rpx;
    }
    
    .storage-detail {
      flex: 1;
      
      .storage-title {
        display: block;
        font-size: 32rpx;
        font-weight: 600;
        color: #333;
        margin-bottom: 8rpx;
      }
      
      .storage-used {
        font-size: 26rpx;
        color: #999;
      }
    }
    
    .storage-percent {
      font-size: 40rpx;
      font-weight: bold;
      color: #667eea;
    }
  }
  
  .storage-progress {
    .progress-bg {
      height: 16rpx;
      background: #f5f5f5;
      border-radius: 8rpx;
      overflow: hidden;
      
      .progress-fill {
        height: 100%;
        background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
        border-radius: 8rpx;
        transition: width 0.3s;
        
        &.warning {
          background: linear-gradient(90deg, #FF6B6B 0%, #FF4D4F 100%);
        }
      }
    }
  }
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 30rpx;
  background: #fff;
  margin: 0 30rpx;
  border-radius: 20rpx 20rpx 0 0;
  
  .breadcrumb {
    font-size: 28rpx;
    color: #999;
    
    text {
      &.active {
        color: #333;
        font-weight: 500;
      }
      
      .separator {
        margin: 0 12rpx;
      }
    }
  }
  
  .toolbar-right {
    display: flex;
    gap: 24rpx;
    
    .view-toggle, .more-btn {
      padding: 12rpx;
    }
  }
}

.search-bar {
  padding: 20rpx 30rpx;
  background: #fff;
  margin: 0 30rpx;
  border-bottom: 2rpx solid #f5f5f5;
}

.file-content {
  height: calc(100vh - 600rpx);
  padding: 0 30rpx 30rpx;
  
  .file-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 20rpx;
    padding-top: 20rpx;
    
    .file-card {
      background: #fff;
      border-radius: 16rpx;
      padding: 20rpx;
      text-align: center;
      position: relative;
      box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.04);
      
      &.selected {
        background: #f0f5ff;
        border: 2rpx solid #667eea;
      }
      
      .file-checkbox {
        position: absolute;
        top: 12rpx;
        left: 12rpx;
        z-index: 10;
        
        .checkbox {
          width: 40rpx;
          height: 40rpx;
          border: 2rpx solid #ddd;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          
          &.checked {
            background: #667eea;
            border-color: #667eea;
          }
        }
      }
      
      .file-icon {
        width: 100rpx;
        height: 100rpx;
        margin: 0 auto 16rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        
        .thumb-img {
          width: 100%;
          height: 100%;
          border-radius: 12rpx;
          object-fit: cover;
        }
      }
      
      .file-info {
        .file-name {
          display: block;
          font-size: 26rpx;
          color: #333;
          margin-bottom: 8rpx;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
          
          &.folder {
            font-weight: 500;
          }
        }
        
        .file-meta {
          display: flex;
          justify-content: center;
          gap: 16rpx;
          font-size: 22rpx;
          color: #999;
          
          .file-date {
            color: #ccc;
          }
        }
      }
    }
  }
  
  .file-list {
    padding-top: 20rpx;
    
    .file-row {
      display: flex;
      align-items: center;
      padding: 24rpx 30rpx;
      background: #fff;
      margin-bottom: 12rpx;
      border-radius: 16rpx;
      box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.04);
      
      &.selected {
        background: #f0f5ff;
      }
      
      .row-checkbox {
        margin-right: 20rpx;
        
        .checkbox {
          width: 40rpx;
          height: 40rpx;
          border: 2rpx solid #ddd;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          
          &.checked {
            background: #667eea;
            border-color: #667eea;
          }
        }
      }
      
      .row-icon {
        width: 80rpx;
        height: 80rpx;
        margin-right: 20rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        
        image {
          width: 100%;
          height: 100%;
          border-radius: 12rpx;
          object-fit: cover;
        }
      }
      
      .row-info {
        flex: 1;
        
        .row-name {
          display: block;
          font-size: 30rpx;
          color: #333;
          margin-bottom: 8rpx;
        }
        
        .row-meta {
          font-size: 24rpx;
          color: #999;
        }
      }
      
      .row-action {
        padding: 12rpx;
      }
    }
  }
  
  .empty-state {
    text-align: center;
    padding: 100rpx 0;
    
    .empty-text {
      display: block;
      font-size: 32rpx;
      color: #999;
      margin-top: 30rpx;
    }
    
    .empty-hint {
      display: block;
      font-size: 26rpx;
      color: #ccc;
      margin-top: 16rpx;
    }
  }
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  padding: 20rpx 30rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  box-shadow: 0 -2rpx 20rpx rgba(0,0,0,0.08);
  
  .select-info {
    display: flex;
    justify-content: space-between;
    margin-bottom: 20rpx;
    
    text {
      font-size: 28rpx;
      color: #666;
    }
    
    .select-all {
      color: #667eea;
    }
  }
  
  .action-btns {
    display: flex;
    justify-content: space-around;
    
    .action-btn {
      display: flex;
      flex-direction: column;
      align-items: center;
      
      text {
        font-size: 24rpx;
        color: #666;
        margin-top: 8rpx;
      }
    }
  }
}

.fab-upload {
  position: fixed;
  right: 40rpx;
  bottom: 100rpx;
  width: 120rpx;
  height: 120rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 30rpx rgba(102, 126, 234, 0.4);
  z-index: 100;
}

.folder-popup {
  width: 560rpx;
  padding: 40rpx;
  
  .popup-title {
    display: block;
    font-size: 32rpx;
    font-weight: 600;
    text-align: center;
    margin-bottom: 30rpx;
    color: #333;
  }
  
  .folder-input {
    width: 100%;
    height: 100rpx;
    background: #f5f6fa;
    border-radius: 16rpx;
    padding: 0 30rpx;
    font-size: 30rpx;
    margin-bottom: 30rpx;
  }
  
  .popup-actions {
    display: flex;
    gap: 20rpx;
    
    .dialog-btn {
      flex: 1;
      height: 80rpx;
      border-radius: 40rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 30rpx;
      font-weight: 500;
      border: none;
      
      &.cancel {
        background: #f5f5f5;
        color: #666;
      }
      
      &.primary {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: #fff;
      }
      
      &:active {
        opacity: 0.9;
        transform: translateY(2rpx);
      }
    }
  }
}
</style>