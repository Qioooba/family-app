# 🏠 家庭小程序 - 详细开发进度表

> 更新日期: 2026-02-21
> 目的: 避免重复开发，清晰规划剩余工作

---

## 📊 总体进度概览

| 阶段 | 功能模块 | 状态 | 进度 |
|------|---------|------|------|
| 核心功能 | 14个模块 | ✅ 已完成 | 100% |
| 数据库设计 | 39张表 | ✅ 已完成 | 100% |
| 后端API | 基础+拓展 | 🔄 部分完成 | 70% |
| 前端页面 | 15个页面 | ✅ 已完成 | 100% |
| 前端API对接 | 前端调用后端 | ⏳ 待开发 | 0% |

---

## ✅ 已完成（不要再动这部分）

### 一、数据库设计（39张表）

#### 核心表（25张）- 已完成
- ✅ user - 用户表
- ✅ family - 家庭表
- ✅ family_member - 家庭成员表
- ✅ task - 任务表
- ✅ wish - 心愿表
- ✅ recipe - 菜谱表
- ✅ recipe_ingredient - 菜谱食材表
- ✅ recipe_step - 菜谱步骤表
- ✅ food_record - 饮食记录表
- ✅ food_inventory - 食材库存表
- ✅ anniversary - 纪念日表
- ✅ calendar_event - 日程事件表
- ✅ vote - 投票表
- ✅ vote_option - 投票选项表
- ✅ vote_record - 投票记录表
- ✅ moment - 家庭圈动态表
- ✅ moment_comment - 动态评论表
- ✅ shopping_item - 购物清单表
- ✅ points_record - 积分记录表
- ✅ game_record - 游戏记录表
- ✅ ai_conversation - AI对话表
- ✅ dashboard_stat - 看板统计表
- ✅ badge - 徽章表
- ✅ user_badge - 用户徽章表
- ✅ notification - 通知表

#### 拓展表（14张）- 已完成
- ✅ task_reminder - 任务提醒表
- ✅ task_subtask - 子任务表
- ✅ task_schedule - 家务排班表
- ✅ wish_budget - 心愿预算表
- ✅ wish_milestone - 心愿里程碑表
- ✅ recipe_inventory_match - 菜谱库存匹配表
- ✅ diet_nutrition_detail - 饮食营养详情表
- ✅ user_weight - 用户体重记录表
- ✅ anniversary_reminder - 纪念日提醒设置表
- ✅ price_history - 商品价格历史表
- ✅ coupon - 优惠券表
- ✅ points_coupon - 积分兑换券表
- ✅ points_coupon_record - 积分券兑换记录表
- ✅ family_report - 家庭报告表

### 二、后端API（已完成部分）

| 模块 | Controller | 状态 | 说明 |
|------|-----------|------|------|
| 用户服务 | UserController | ✅ | 注册/登录/用户信息 |
| 家庭服务 | FamilyController | ✅ | 家庭CRUD/成员管理 |
| 任务服务 | TaskController | ✅ | 任务基础CRUD |
| | SubtaskController | ✅ | 子任务增删改查/切换状态 |
| | ScheduleController | ✅ | 排班表增删改查/今日排班 |
| 心愿服务 | WishController | ✅ | 心愿基础CRUD |
| 菜谱服务 | RecipeController | ✅ | 菜谱基础CRUD |
| 饮食服务 | FoodController | ✅ | 饮食记录基础功能 |
| | WeightController | ✅ | 体重记录/历史/最新 |
| 日程服务 | CalendarController | ✅ | 日程基础CRUD |
| 投票服务 | VoteController | ✅ | 投票基础CRUD |
| AI服务 | AIController | ✅ | 对话/日报/推荐 |
| 积分商城 | CouponController | 🔄 | 券列表✅，兑换❌ |

### 三、前端页面（15个页面）- 全部完成

| 页面 | 状态 | 功能覆盖 |
|------|------|---------|
| 首页/仪表盘 | ✅ | 统计卡片、快捷入口 |
| 任务清单 | ✅ | 任务列表、分类筛选 |
| 心愿墙 | ✅ | 心愿列表、认领功能 |
| 智能菜谱 | ✅ | 菜谱列表、收藏 |
| 饮食记录 | ✅ | 记录列表、拍照入口 |
| 纪念日 | ✅ | 日历视图、倒计时 |
| 家庭投票 | ✅ | 投票列表、参与投票 |
| 家庭圈 | ✅ | 动态流、点赞评论 |
| 智能购物 | ✅ | 购物清单、库存管理 |
| 家庭游戏 | ✅ | 转盘、积分、排行榜 |
| AI助手 | ✅ | 对话界面、推荐卡片 |
| 个人中心 | ✅ | 信息展示、设置入口 |

### 四、前端UI设计 - 已完成
- ✅ 清新绿色主题（preview-fresh.html）
- ✅ 所有页面UI框架
- ✅ 按钮交互效果

---

## 🔄 进行中/待开发（这是接下来的工作）

### 第一阶段：前端API对接（最优先）
**状态**: ⏳ 待开发 | **预计时间**: 2-3天

目前前端页面和后端API是断开的，需要把前端按钮和功能连到真实的后端接口。

#### 1.1 用户模块对接
- [ ] 注册页面 → POST /api/user/register
- [ ] 登录页面 → POST /api/user/login
- [ ] 用户信息展示 → GET /api/user/info
- [ ] 用户信息编辑 → PUT /api/user/update

#### 1.2 任务模块对接
- [ ] 任务列表 → GET /api/task/list
- [ ] 创建任务 → POST /api/task/create
- [ ] 完成任务 → PUT /api/task/{id}/complete
- [ ] 子任务展示 → GET /api/task/subtask/list/{taskId}
- [ ] 添加子任务 → POST /api/task/subtask/add
- [ ] 切换子任务 → PUT /api/task/subtask/{id}/toggle
- [ ] 排班表展示 → GET /api/schedule/list/{familyId}
- [ ] 今日排班 → GET /api/schedule/today/{familyId}

#### 1.3 心愿模块对接
- [ ] 心愿列表 → GET /api/wish/list
- [ ] 创建心愿 → POST /api/wish/create
- [ ] 认领心愿 → POST /api/wish/{id}/claim
- [ ] 更新进度 → POST /api/wish/{id}/progress

#### 1.4 饮食模块对接
- [ ] 体重记录 → POST /api/diet/weight/record
- [ ] 体重历史 → GET /api/diet/weight/history
- [ ] 饮食记录 → GET /api/food/records

#### 1.5 其他模块对接（类似上面）
- [ ] 菜谱模块API对接
- [ ] 纪念日模块API对接
- [ ] 投票模块API对接
- [ ] 家庭圈模块API对接
- [ ] 购物模块API对接
- [ ] 游戏模块API对接

---

### 第二阶段：任务系统增强
**状态**: 🔄 部分完成 | **预计时间**: 1天

#### 2.1 重复任务
**数据库**: ✅ 已有repeat_type, repeat_rule字段
**后端API**: ⏳ 待开发
- [ ] POST /api/task/{id}/repeat - 设置重复规则
- [ ] 定时任务生成器（每天凌晨生成当日任务实例）

**前端页面**: ⏳ 待开发
- [ ] 任务创建/编辑页添加重复设置选项
- [ ] 重复规则选择器（每天/每周/每月/自定义）

#### 2.2 任务提醒
**数据库**: ✅ task_reminder表已存在
**后端API**: ⏳ 待开发
- [ ] POST /api/task/{id}/reminder - 设置提醒
- [ ] 定时推送服务（时间提醒）
- [ ] 地理围栏服务（位置提醒）

**前端页面**: ⏳ 待开发
- [ ] 提醒设置弹窗
- [ ] 时间选择器
- [ ] 地图选点（位置提醒）

---

### 第三阶段：心愿系统增强
**状态**: ⏳ 待开始 | **预计时间**: 0.5天

#### 3.1 心愿预算
**数据库**: ✅ wish_budget表已存在
**后端API**: ⏳ 待开发
- [ ] POST /api/wish/{id}/budget - 设置预算
- [ ] GET /api/wish/budget-stats - 预算统计

**前端页面**: ⏳ 待开发
- [ ] 心愿卡片显示预算进度条
- [ ] 预算设置弹窗

#### 3.2 心愿里程碑
**数据库**: ✅ wish_milestone表已存在
**后端API**: ⏳ 待开发
- [ ] POST /api/wish/{id}/milestone - 添加里程碑
- [ ] PUT /api/milestone/{id}/complete - 完成里程碑
- [ ] GET /api/wish/{id}/milestones - 里程碑列表

**前端页面**: ⏳ 待开发
- [ ] 里程碑时间线展示

---

### 第四阶段：购物系统增强
**状态**: ⏳ 待开始 | **预计时间**: 1天

#### 4.1 扫码录入
**后端API**: ⏳ 待开发
- [ ] POST /api/shopping/scan - 扫码识别
- [ ] 对接商品条码库（或自建条码库）

**前端页面**: ⏳ 待开发
- [ ] 扫码界面（调用相机）
- [ ] 扫码结果确认弹窗

#### 4.2 价格追踪
**数据库**: ✅ price_history表已存在
**后端API**: ⏳ 待开发
- [ ] POST /api/shopping/price-record - 记录价格
- [ ] GET /api/shopping/price-trend - 价格趋势

**前端页面**: ⏳ 待开发
- [ ] 价格曲线图
- [ ] 最低价提醒设置

#### 4.3 优惠券管理
**数据库**: ✅ coupon表已存在
**后端API**: ⏳ 待开发
- [ ] POST /api/coupon - 添加优惠券
- [ ] GET /api/coupons - 优惠券列表
- [ ] PUT /api/coupon/{id}/use - 标记使用

**前端页面**: ⏳ 待开发
- [ ] 优惠券列表页面
- [ ] 到期提醒

---

### 第五阶段：健康系统增强
**状态**: 🔄 部分完成 | **预计时间**: 0.5天

#### 5.1 体重记录（已完成后端）
**后端API**: ✅ WeightController已完成
**前端页面**: ⏳ 待开发
- [ ] 体重记录弹窗
- [ ] 趋势曲线图

#### 5.2 营养成分分析
**数据库**: ✅ diet_nutrition_detail表已存在
**后端API**: ⏳ 待开发
- [ ] GET /api/diet/nutrition/daily - 每日营养分析
- [ ] GET /api/diet/nutrition/weekly - 周营养报告

**前端页面**: ⏳ 待开发
- [ ] 营养成分饼图
- [ ] 摄入建议

#### 5.3 喝水打卡
**数据库**: ⏳ 待创建 water_record表
**后端API**: ⏳ 待开发
- [ ] POST /api/diet/water - 记录喝水
- [ ] GET /api/diet/water/today - 今日喝水量

**前端页面**: ⏳ 待开发
- [ ] 喝水打卡按钮
- [ ] 8杯水进度展示

---

### 第六阶段：纪念日增强
**状态**: ⏳ 待开始 | **预计时间**: 0.5天

#### 6.1 农历支持
**数据库**: ⏳ anniversary表增加lunar_date字段
**后端API**: ⏳ 待开发
- [ ] 集成农历转换库
- [ ] 农历日期存储和查询

**前端页面**: ⏳ 待开发
- [ ] 农历/公历切换开关
- [ ] 传统节日自动显示

#### 6.2 多级提醒
**数据库**: ✅ anniversary_reminder表已存在
**后端API**: ⏳ 待开发
- [ ] POST /api/anniversary/reminder - 设置提醒

**前端页面**: ⏳ 待开发
- [ ] 提醒设置（3天/7天/30天）

#### 6.3 贺卡生成
**后端API**: ⏳ 待开发
- [ ] POST /api/anniversary/card - 生成贺卡图片

**前端页面**: ⏳ 待开发
- [ ] 贺卡模板选择
- [ ] 分享功能

---

### 第七阶段：积分商城完成
**状态**: 🔄 部分完成 | **预计时间**: 0.5天

#### 7.1 积分兑换
**后端API**: 🔄 CouponController需完善
- [x] GET /api/game/coupon/list - 券列表 ✅
- [ ] POST /api/game/coupon/exchange - 兑换券

**前端页面**: ⏳ 待开发
- [ ] 券详情页面
- [ ] 兑换确认弹窗

#### 7.2 我的券包
**数据库**: ✅ points_coupon_record表已存在
**后端API**: ⏳ 待开发
- [ ] GET /api/game/my-coupons - 我的券列表
- [ ] POST /api/game/coupon/use - 使用券

**前端页面**: ⏳ 待开发
- [ ] 我的券包页面
- [ ] 券详情/核销码展示

---

### 第八阶段：家庭报告
**状态**: ⏳ 待开始 | **预计时间**: 1天

#### 8.1 报告生成
**数据库**: ✅ family_report表已存在
**后端API**: ⏳ 待开发
- [ ] POST /api/report/generate - 生成报告（定时或手动）
- [ ] GET /api/report/{id} - 获取报告详情
- [ ] GET /api/reports - 报告列表

**前端页面**: ⏳ 待开发
- [ ] 报告列表页面
- [ ] 报告详情页

#### 8.2 数据可视化
**前端页面**: ⏳ 待开发
- [ ] 周/月/年切换
- [ ] 多种图表类型（柱状图、饼图、折线图）

---

### 第九阶段：其他功能
**状态**: ⏳ 待开始 | **预计时间**: 1天

#### 9.1 多家庭切换
**后端API**: ⏳ 待开发
- [ ] user表增加当前家庭字段
- [ ] POST /api/user/switch-family - 切换家庭接口

**前端页面**: ⏳ 待开发
- [ ] 家庭切换下拉框
- [ ] 家庭选择弹窗

#### 9.2 深色模式
**前端页面**: ⏳ 待开发
- [ ] 主题切换开关
- [ ] 深色样式适配（所有页面）

#### 9.3 数据导出
**后端API**: ⏳ 待开发
- [ ] GET /api/export/family-data - 导出家庭数据（Excel/PDF）

**前端页面**: ⏳ 待开发
- [ ] 导出按钮
- [ ] 导出选项弹窗

---

## 📈 工作量统计

| 阶段 | 待开发项 | 预计时间 | 优先级 |
|------|---------|---------|--------|
| 前端API对接 | 约30个接口 | 2-3天 | 🔴 P0 - 最紧急 |
| 任务系统增强 | 2个功能 | 1天 | 🟡 P1 |
| 心愿系统增强 | 2个功能 | 0.5天 | 🟡 P1 |
| 购物系统增强 | 3个功能 | 1天 | 🟡 P1 |
| 健康系统增强 | 2个功能 | 0.5天 | 🟡 P1 |
| 纪念日增强 | 3个功能 | 0.5天 | 🟢 P2 |
| 积分商城完成 | 2个功能 | 0.5天 | 🟢 P2 |
| 家庭报告 | 2个功能 | 1天 | 🟢 P2 |
| 其他功能 | 3个功能 | 1天 | 🔵 P3 |

**总计**: 约8-10天完成所有功能

---

## 🎯 下一步建议

### 方案A：先完成核心闭环（推荐）
1. **前端API对接**（2-3天）- 让现有功能真正可用
2. **重复任务**（0.5天）- 高频使用功能
3. **扫码录入**（0.5天）- 购物场景刚需

### 方案B：按模块推进
1. **任务模块全部完成**（2天）
2. **心愿模块全部完成**（1天）
3. **购物模块全部完成**（1天）
4. ...以此类推

### 方案C：优先级排序
按照用户价值从高到低逐个开发

---

## ⚠️ 注意事项

1. **不要再创建数据库表** - 39张表已经全部设计完成
2. **优先做前端API对接** - 这是让项目真正跑起来的关键
3. **已完成后端的优先做前端** - 如体重记录、子任务等
4. **避免同时开发多个大功能** - 完成一个上线一个

---

**你想按哪个方案继续？或者指定某个功能先开发？**
