# QA问题追踪

## 待修复

| 编号 | 类型 | 位置 | 描述 | 优先级 | 发现时间 |
|------|------|------|------|--------|----------|
| Q001 | 接口不一致 | api/wish.js vs WishController.java | 前端路径`/wish`，后端路径`/api/wish` | P1 | 2026-02-22 |
| Q002 | 接口不一致 | api/task.js vs TaskController.java | 前端路径`/task`，后端路径`/api/task` | P1 | 2026-02-22 |
| Q003 | 接口不一致 | api/schedule.js vs ScheduleController.java | 前端路径`/schedule`，后端路径`/api/schedule` | P1 | 2026-02-22 |
| Q004 | 接口不一致 | api/vote.js vs VoteController.java | 路径前缀不匹配 | P2 | 2026-02-22 |
| Q005 | 接口不一致 | api/family.js vs FamilyController.java | 路径前缀`/family`与其他服务不一致 | P2 | 2026-02-22 |
| Q006 | 代码规范 | 前端所有api/*.js | 导入方式不统一：`@/` vs `../` | P3 | 2026-02-22 |
| Q007 | 编译错误 | user-service/pom.xml | Lombok与Java 17兼容性问题 | P0 | 2026-02-22 |
| Q008 | 架构问题 | task-service + family-service | 存在两个TaskController，可能冲突 | P0 | 2026-02-22 |
| Q009 | 代码规范 | 多个Controller.java | 权限检查方式不统一 | P3 | 2026-02-22 |
| Q010 | 接口缺失验证 | api/task.js | getById接口需确认后端实现 | P2 | 2026-02-22 |
| Q011 | 安全问题 | ExportController.java | 导出数据无权限校验 | P0 | 2026-02-22 |
| Q012 | 功能缺陷 | UserController.java (user-service) | logout未调用StpUtil.logout() | P1 | 2026-02-22 |
| Q013 | 安全问题 | UserController.java | getUserById无权限校验 | P2 | 2026-02-22 |
| Q014 | 架构问题 | user-service + family-service | 两个UserController重复 | P0 | 2026-02-22 |
| Q015 | 代码缺陷 | family-service/UserController.java | switchFamily硬编码userId=1L | P1 | 2026-02-22 |
| Q016 | 字段不一致 | task/create.vue vs Task.java | deadline vs dueTime | P1 | 2026-02-22 |
| Q017 | 字段不一致 | task/create.vue vs Task.java | category vs categoryId | P1 | 2026-02-22 |
| Q018 | Controller缺失 | api/moments.js | 后端不存在MomentsController | P0 | 2026-02-22 |
| Q019 | Controller缺失 | api/schedule.js | 后端不存在ScheduleController | P0 | 2026-02-22 |
| Q020 | 路径不匹配 | api/user.js vs UserController.java | 前端`/api/user` vs 后端`/user` | P1 | 2026-02-22 |
| Q021 | 路径不匹配 | api/coupon.js vs PointsCouponController.java | 路径完全不匹配 | P1 | 2026-02-22 |
| Q022 | 依赖问题 | backend/pom.xml | bucket4j-core依赖无法下载 | P0 | 2026-02-22 |

## 已修复

| 编号 | 修复时间 | 验证结果 |
|------|----------|----------|
| - | - | - |

## 接口一致性检查清单

### 已检查模块
- [x] user.js - 用户模块
- [x] task.js - 任务模块
- [x] wish.js - 心愿模块
- [x] water.js - 饮水模块
- [x] family.js - 家庭模块
- [x] schedule.js - 排班模块
- [x] moments.js - 家庭圈模块
- [x] ai.js - AI模块
- [x] vote.js - 投票模块
- [x] recipe.js - 菜谱模块
- [x] stats.js - 统计模块
- [x] coupon.js - 优惠券模块
- [x] shopping.js - 购物模块
- [x] game.js - 游戏模块
- [x] anniversary.js - 纪念日模块

### 发现的模式问题
1. **路径前缀不统一**: 部分后端使用`/api`前缀，部分不使用
2. **权限检查不一致**: 部分Controller使用Sa-Token，部分不使用
3. **代码风格不一致**: 前端导入方式有两种风格
4. **服务拆分问题**: task-service和family-service有重复代码

## 后端编译状态
- [ ] 整体编译通过
- [x] 发现编译错误（Lombok兼容性问题）

## 待验证修复
| 问题编号 | 等待修复通知 | 验证状态 |
|----------|--------------|----------|
| Q001 | ⏳ | - |
| Q002 | ⏳ | - |
| Q003 | ⏳ | - |
| Q007 | ⏳ | - |
| Q008 | ⏳ | - |
