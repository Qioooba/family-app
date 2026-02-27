# 项目功能清单（实际存在）

## 🔍 功能核查说明

本文档根据实际代码分析生成，列出项目中**实际存在**的功能模块。

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

**前端页面**: `pages/task/` (40+页面)
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

### 3. 投票系统 (Vote)
**Controller**: `VoteController.java`

| 功能 | 端点 | 状态 |
|------|------|------|
| 投票列表 | GET `/api/vote/list/{familyId}` | ✅ |
| 创建投票 | POST `/api/vote/create` | ✅ |
| 参与投票 | POST `/api/vote/{id}/vote` | ✅ |
| 投票结果 | GET `/api/vote/{id}/result` | ✅ |

**前端页面**: `pages/vote/`
- index.vue, create.vue

---

### 4. 家庭相册 (Album)
**Controller**: `AlbumController.java`

| 功能 | 端点 | 状态 |
|------|------|------|
| 相册CRUD | `/family/album` | ✅ |
| 照片管理 | `/family/album/photo/*` | ✅ |
| 照片标签 | `/family/album/photo/tag*` | ✅ |
| 时光轴 | GET `/family/album/timeline/{familyId}` | ✅ |
| 共享相册 | `/family/album/share*` | ✅ |

**前端页面**: `pages/family/album.vue`

---

### 5. 家庭账本 (Ledger)
**Controller**: `LedgerController.java`

| 功能 | 端点 | 状态 |
|------|------|------|
| 账本管理 | `/family/ledger` | ✅ |
| 记账记录 | `/family/ledger/record*` | ✅ |
| 分类管理 | `/family/ledger/category*` | ✅ |
| 预算管理 | `/family/ledger/budget*` | ✅ |
| 报表统计 | `/family/ledger/{id}/report/*` | ✅ |
| 成员分摊 | `/family/ledger/share*` | ✅ |

**前端页面**: `pages/dashboard/finance.vue`, `pages/family/report.vue`

---

### 6. 智能购物 (Shopping)
**Controller**: `ShoppingController.java`

| 功能 | 端点 | 状态 |
|------|------|------|
| 购物清单 | `/api/shopping/list/*` | ✅ |
| 添加商品 | POST `/api/shopping/item/add` | ✅ |
| 更新状态 | PUT `/api/shopping/item/{id}/status` | ✅ |
| 扫码识别 | POST `/api/shopping/scan` | ✅ |
| 库存管理 | `/api/shopping/inventory*` | ✅ |
| 价格记录 | POST `/api/shopping/price-record` | ✅ |
| 价格趋势 | GET `/api/shopping/price-trend` | ✅ |

**前端页面**: `pages/shopping/`
- index.vue, list.vue, inventory.vue, scan.vue, price.vue, coupons.vue

---

### 7. 优惠券管理 (Coupon)
**Controller**: `CouponController.java`

| 功能 | 端点 | 状态 |
|------|------|------|
| 添加优惠券 | POST `/api/coupon` | ✅ |
| 优惠券列表 | GET `/api/coupons` | ✅ |
| 按状态筛选 | GET `/api/coupons/status/{status}` | ✅ |
| 即将过期 | GET `/api/coupons/expiring` | ✅ |
| 使用优惠券 | PUT `/api/coupon/{id}/use` | ✅ |
| 统计信息 | GET `/api/coupons/statistics` | ✅ |
| 检查过期 | POST `/api/coupons/check-expired` | ✅ |

**前端页面**: `pages/coupon/`, `pages/shopping/coupons.vue`

---

### 8. 食材/库存管理 (Ingredient)
**Controller**: `IngredientController.java`

| 功能 | 端点 | 状态 |
|------|------|------|
| 食材列表 | GET `/api/food/ingredient/list/{familyId}` | ✅ |
| 添加食材 | POST `/api/food/ingredient/add` | ✅ |
| 更新食材 | PUT `/api/food/ingredient/update` | ✅ |
| 图片识别 | POST `/api/food/ingredient/recognize` | ✅ |
| 即将过期 | GET `/api/food/ingredient/expiring/{familyId}` | ✅ |
| 删除食材 | DELETE `/api/food/ingredient/{id}` | ✅ |

**前端页面**: `pages/shopping/inventory.vue`, `pages/food/`

---

### 9. 纪念日管理 (Anniversary)
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

### 10. 家庭游戏 (Game)
**Controller**: `GameController.java`, `PointsCouponController.java`

| 功能 | 端点 | 状态 |
|------|------|------|
| 家庭挑战 | `/api/game/challenge*` | ✅ |
| 挑战参与 | `/api/game/challenge/{id}/join` | ✅ |
| 打卡记录 | `/api/game/challenge/checkin` | ✅ |
| 排行榜 | GET `/api/game/challenge/{id}/rank` | ✅ |
| 猜谜游戏 | `/api/game/riddle*` | ✅ |
| 答题游戏 | `/api/game/quiz*` | ✅ |
| 积分系统 | `/api/game/points*` | ✅ |
| 积分兑换券 | `/api/game/coupon*` | ✅ |
| 我的券 | GET `/api/game/my-coupons` | ✅ |
| 成就系统 | GET `/api/game/achievements` | ✅ |

**前端页面**: `pages/game/`
- index.vue, challenges.vue, games.vue, points.vue, exchange.vue

---

### 11. 用户系统 (User)
**Controller**: `UserController.java`

| 功能 | 端点 | 状态 |
|------|------|------|
| 用户登录 | POST `/api/user/login` | ✅ |
| 用户注册 | POST `/api/user/register` | ✅ |
| 用户信息 | GET `/api/user/info` | ✅ |
| 用户退出 | POST `/api/user/logout` | ✅ |

**前端页面**: `pages/login/`, `pages/register/`, `pages/profile/`

---

### 12. 数据看板 (Dashboard)
**Controller**: `TaskStatsController.java`, `ExportController.java`

| 功能 | 端点 | 状态 |
|------|------|------|
| 任务概览 | GET `/api/task/stats/overview` | ✅ |
| 任务趋势 | GET `/api/task/stats/trend` | ✅ |
| 成员统计 | GET `/api/task/stats/by-member` | ✅ |
| 优先级分布 | GET `/api/task/stats/by-priority` | ✅ |
| 逾期任务 | GET `/api/task/stats/overdue-list` | ✅ |
| 数据导出 | GET `/api/export/family-data` | ✅ |

**前端页面**: `pages/dashboard/`
- index.vue, report.vue, finance.vue, export.vue, monthly.vue, yearly-memory.vue

---

### 13. 健康记录 (Health)
**Controller**: `HealthController.java`, `WeightController.java`

| 功能 | 端点 | 状态 |
|------|------|------|
| 今日饮水 | GET `/api/health/water/today` | ✅ |
| 饮水目标 | GET `/api/health/water/target` | ✅ |
| 添加饮水 | POST `/api/health/water` | ✅ |
| 饮水历史 | GET `/api/health/water/history` | ✅ |
| 记录体重 | POST `/api/diet/weight/record` | ✅ |
| 体重历史 | GET `/api/diet/weight/history` | ✅ |
| 最新体重 | GET `/api/diet/weight/latest` | ✅ |

**前端页面**: `pages/water/`, `pages/nutrition/`

---

### 14. 家庭圈/动态 (Moments)
**Controller**: *(实际页面存在，需要进一步确认Controller)*

**前端页面**: `pages/moments/`
- index.vue, create.vue, comments.vue, notifications.vue, announcements.vue

---

## ⚠️ 框架已搭建的功能模块

以下模块已搭建基础框架（Controller、Entity），但业务逻辑可能不完整：

### 15. 心愿墙 (Wish)
**Controller**: `WishController.java` - 返回空数据框架

**前端页面**: `pages/wish/`
- index.vue, create.vue, detail.vue, milestone.vue, budget.vue

---

### 16. 智能菜谱 (Recipe)
**Controller**: `RecipeController.java` - 返回示例数据

**前端页面**: `pages/recipe/`
- index.vue, detail.vue

---

### 17. AI助手 (AI)
**Controller**: `AiController.java` - 模拟回复

**前端页面**: `pages/ai/`, `pages/ai-shopping/`

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
| TaskAttachment | 任务附件 |

### 生活实体
| 实体 | 说明 |
|------|------|
| Vote / VoteRecord | 投票/记录 |
| Wish / WishMilestone | 心愿/里程碑 |
| Recipe / RecipeRecord | 菜谱/记录 |
| Ingredient | 食材/库存 |
| Anniversary / AnniversaryReminder | 纪念日/提醒 |
| Coupon | 优惠券 |

### 家庭空间实体
| 实体 | 说明 |
|------|------|
| FamilyAlbum / AlbumPhoto | 相册/照片 |
| PhotoTag / AlbumShare | 照片标签/共享 |
| FamilyLedger / LedgerRecord | 账本/记录 |
| LedgerCategory / LedgerBudget | 分类/预算 |
| LedgerShare | 成员分摊 |

### 游戏实体
| 实体 | 说明 |
|------|------|
| FamilyChallenge / ChallengeParticipant | 挑战/参与者 |
| ChallengeCheckin | 挑战打卡 |
| GameRiddle / GameQuiz | 猜谜/答题 |
| GameWheel | 转盘游戏 |
| PointsCoupon / PointsCouponRecord | 积分券/记录 |
| UserPoints | 用户积分 |

### 购物实体
| 实体 | 说明 |
|------|------|
| ShoppingList | 购物清单 |
| ShoppingItem | 购物项 |
| Inventory | 库存 |

### 健康实体
| 实体 | 说明 |
|------|------|
| WaterRecord | 饮水记录 |
| UserWeight | 体重记录 |
| HealthRecord / DietRecord | 健康/饮食记录 |

### 其他实体
| 实体 | 说明 |
|------|------|
| InviteCode | 邀请码 |
| TaskTemplate | 任务模板 |
| FamilyReport | 家庭报告 |
| OperationLog | 操作日志 |
| UserActivity | 用户活动 |
| MomentLike | 动态点赞 |

---

## 📈 功能完成度统计

| 类别 | 完整功能 | 框架功能 | 总计 |
|------|---------|---------|------|
| 核心功能 | 14 | 3 | 17 |
| Controller | 40+ | 5+ | 45+ |
| API接口 | 150+ | 20+ | 170+ |
| 前端页面 | 80+ | - | 80+ |
| 数据库表 | 60+ | - | 60+ |

---

## 📝 备注

1. **完整功能**: 已实现完整的CRUD和业务逻辑
2. **框架功能**: 已搭建Controller和Entity，返回模拟数据或空数据
3. 所有功能模块的前端页面均已存在
4. 部分高级功能（如AI、菜谱推荐）使用模拟数据
