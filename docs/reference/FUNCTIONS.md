# 项目功能清单

本文档列出家庭小程序当前保留的功能模块。

---

## ✅ 完整实现的功能模块

### 1. 任务管理 (Task)
**Controller**: `TaskController.java`, `SubtaskController.java`, `TaskStatsController.java`, `ScheduleController.java`

| 功能 | 端点 | 状态 |
|------|------|------|
| 任务列表 | GET `/api/task/list` | ✅ |
| 今日任务 | GET `/api/task/today/{familyId}` | ✅ |
| 创建任务 | POST `/api/task/create` | ✅ |
| 任务详情 | GET `/api/task/{id}` | ✅ |
| 更新任务 | PUT `/api/task/update` | ✅ |
| 删除任务 | DELETE `/api/task/{id}` | ✅ |
| 完成任务 | POST `/api/task/complete/{id}` | ✅ |
| 子任务管理 | `/api/task/subtask/*` | ✅ |
| 排班管理 | `/api/schedule/*` | ✅ |
| 任务统计 | `/api/task/stats/*` | ✅ |

**前端页面**: `pages/task/`
- index.vue, create.vue, detail.vue, board.vue, calendar.vue
- gantt.vue, timeline.vue, heatmap.vue, statistics.vue
- schedule.vue, drag-sort.vue, habit.vue, reminder.vue

---

### 2. 家庭管理 (Family)
**Controller**: `FamilyController.java`, `InviteCodeController.java`

| 功能 | 端点 | 状态 |
|------|------|------|
| 家庭列表 | GET `/api/family/list` | ✅ |
| 家庭信息 | GET `/api/family/info` | ✅ |
| 创建家庭 | POST `/api/family/create` | ✅ |
| 更新家庭 | PUT `/api/family/update` | ✅ |
| 删除家庭 | DELETE `/api/family/{id}` | ✅ |
| 成员列表 | GET `/api/family/{familyId}/members` | ✅ |
| 移除成员 | DELETE `/api/family/{familyId}/members/{userId}` | ✅ |
| 切换家庭 | POST `/api/family/switch/{familyId}` | ✅ |
| 邀请码 | `/api/family/invite-code*` | ✅ |
| 加入家庭 | POST `/api/family/join-by-code` | ✅ |

**前端页面**: `pages/family/`
- index.vue, switch.vue, account.vue

---

### 3. 纪念日管理 (Anniversary)
**Controller**: `AnniversaryController.java`, `ReminderController.java`, `CardController.java`

| 功能 | 端点 | 状态 |
|------|------|------|
| 纪念日列表 | GET `/api/calendar/anniversary/list/{familyId}` | ✅ |
| 即将到期 | GET `/api/calendar/anniversary/upcoming/{familyId}` | ✅ |
| 创建纪念日 | POST `/api/calendar/anniversary/create` | ✅ |
| 今日纪念日 | GET `/api/calendar/anniversary/today/{familyId}` | ✅ |
| 删除纪念日 | DELETE `/api/calendar/anniversary/{id}` | ✅ |
| 农历转公历 | GET `/api/calendar/anniversary/lunar-to-solar` | ✅ |
| 公历转农历 | GET `/api/calendar/anniversary/solar-to-lunar` | ✅ |

**前端页面**: `pages/anniversary/`, `pages/calendar/`, `pages/card/`

---

### 4. 天气查询 (Weather)
**Controller**: `WeatherController.java`

| 功能 | 端点 | 状态 |
|------|------|------|
| 当前天气 | GET `/api/weather/current` | ✅ |
| 天气预报 | GET `/api/weather/forecast` | ✅ |
| 天气预警 | GET `/api/weather/warning` | ✅ |
| 生活指数 | GET `/api/weather/life-index` | ✅ |

**前端页面**: `pages/weather/`

---

### 5. 用户系统 (User)
**Controller**: `UserController.java`

| 功能 | 端点 | 状态 |
|------|------|------|
| 用户名密码登录 | POST `/api/user/login` | ✅ |
| 用户注册 | POST `/api/user/register` | ✅ |
| 微信一键登录 | POST `/api/user/wechat-login` | ✅ |
| 用户信息 | GET `/api/user/info` | ✅ |
| 用户退出 | POST `/api/user/logout` | ✅ |

**登录方式**:
- 用户名+密码登录
- 微信一键登录（仅 openid，不获取手机号）
- 注册需邀请码

**前端页面**: `pages/login/`, `pages/register/`, `pages/profile/`

---

## ❌ 已删除的功能模块

| 功能模块 | 删除原因 |
|---------|---------|
| 🤖 **AI 助手** | 深度合成技术不符合审核要求 |
| 📷 **家庭相册** | 用户不需要 |
| 💰 **家庭账本** | 用户不需要 |
| 💝 **心愿墙** | 用户不需要 |
| 🍳 **智能菜谱** | 用户不需要 |
| 🗳️ **家庭投票** | 用户不需要 |
| 🛒 **智能购物** | 用户不需要 |
| 🎟️ **优惠券** | 用户不需要 |
| 🎮 **家庭游戏** | 用户不需要 |
| 💧 **健康记录** | 用户不需要 |
| 📊 **数据看板** | 用户不需要 |
| 💬 **家庭圈** | 用户不需要 |

---

## 📊 数据库实体清单

### 核心实体
| 实体 | 说明 |
|------|------|
| User | 用户 |
| Family | 家庭 |
| FamilyMember | 家庭成员 |
| Task | 任务 |
| TaskSubtask | 子任务 |
| Schedule | 排班 |
| TaskTag | 任务标签 |
| InviteCode | 邀请码 |

### 日期实体
| 实体 | 说明 |
|------|------|
| Anniversary | 纪念日 |
| AnniversaryReminder | 纪念日提醒 |

### 天气实体
| 实体 | 说明 |
|------|------|
| Weather | 天气数据 |

---

## 📈 功能完成度统计

| 类别 | 数量 |
|------|------|
| 完整功能模块 | 5 |
| Controller | 10+ |
| API接口 | 50+ |
| 前端页面 | 30+ |
| 数据库表 | 10+ |

---

## 📝 备注

1. 所有保留功能均已实现完整的CRUD和业务逻辑
2. 已删除功能的代码仍然存在于项目中，但不在文档中展示
3. 登录系统支持用户名密码和微信一键登录（仅openid）
4. 新用户注册需要邀请码
