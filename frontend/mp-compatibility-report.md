# 小程序兼容性审查报告

## 发现的问题汇总

### 1. 样式单位问题 (HIGH PRIORITY)
多个 Vue 文件使用了 `px` 单位，这在小程序中可能导致显示不一致。
**影响文件：**
- components/calendar/CalendarModal.vue
- components/anniversary/AnniversaryCard.vue
- components/anniversary/AnniversaryModal.vue
- 以及其他多处...

**修复方案：** 将所有 px 单位转换为 rpx，或使用计算函数处理

### 2. v-for 缺少 key (MEDIUM PRIORITY)
多个 v-for 指令缺少 `:key` 属性，这会导致 Vue 警告和性能问题。
**影响文件：**
- components/calendar/CalendarModal.vue (categories v-for)
- components/calendar/CalendarTask.vue
- components/calendar/CalendarGrid.vue
- components/anniversary/AnniversaryCard.vue
- components/anniversary/AnniversaryModal.vue (多处)
- 以及其他多处...

### 3. 图片路径问题 (MEDIUM PRIORITY)
部分图片使用相对路径 `../../static/`，在小程序中应使用绝对路径 `/static/`
**影响文件：**
- components/task/TaskCreateModal.vue
- components/task/TodoCreateModal.vue
- pages/family-sub/switch.vue

### 4. window/document API 使用 (已条件编译保护 - OK)
以下文件正确使用了条件编译或 typeof 检查：
- utils/request.js ✅
- utils/weather.js ✅ (使用 #ifdef H5)
- utils/theme.js ✅ (使用 typeof window !== 'undefined')
- utils/lazyLoad.js ✅
- stores/user.js ✅

### 5. uni.getSystemInfoSync 弃用警告
pages/task-sub/drag-sort.vue 使用了 uni.getSystemInfoSync()，但代码已使用 uni.getSystemInfo 异步方法，当前实现是正确的。

### 6. pages.json 配置检查 (OK)
- 页面路径配置正确
- tabBar 配置正确
- subPackages 配置正确

## 修复清单

### 必须修复的
1. [ ] 修复所有 v-for 缺少 key 的问题
2. [ ] 修复图片相对路径问题
3. [ ] 检查并修复 px 单位问题（可选，因为 uni-app 会自动转换部分 px）

### 可选优化
4. [ ] 添加更多条件编译保护
5. [ ] 优化事件绑定（@click 在小程序中已兼容）
