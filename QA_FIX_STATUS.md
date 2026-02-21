# QA修复状态追踪

## 当前状态概览
- **待修复**: 19 项
- **已修复**: 3 项 (Q001/Q003/Q020/Q021)
- **验证通过**: 0 项
- **阻塞中**: 6 项 (P0级别)

---

## P0 - 阻塞级别（影响编译/启动/安全）

### Q023 - 编译错误 (CacheAspect Lombok)
**状态**: 🔴 新发现
**负责**: 后端
**问题**: `CacheAspect.java` 已有`@Slf4j`注解但编译失败，找不到`log`变量
**错误信息**: `[ERROR] 找不到符号: 变量 log`
**建议方案**:
1. 检查Lombok annotationProcessorPaths配置
2. 或手动添加`private static final Logger log = LoggerFactory.getLogger(...)`

### Q007 - 编译错误 (Lombok)
**状态**: 🟡 需重新验证
**负责**: 后端
**问题**: Lombok 1.18.32 与 Java 17 兼容性问题导致user-service编译失败

### Q022 - 编译错误 (依赖)
**状态**: 🟡 需重新验证
**负责**: 后端
**问题**: bucket4j-core 8.7.0 依赖无法从Maven Central下载

### Q008 - 架构问题 (TaskController重复)
**状态**: ✅ 已修复 (git f501c2c)
**验证时间**: 2026-02-22 01:30
**验证结果**: 只存在一个TaskController在task-service中

### Q011 - 安全问题 (ExportController无权限)
**状态**: ✅ 已修复 (git f501c2c)
**验证时间**: 2026-02-22 01:33
**验证结果**: 已有`@SaCheckLogin`和用户家庭权限校验
**建议方案**: 添加@SaCheckLogin和用户家庭权限校验

### Q014 - 架构问题 (UserController重复)
**状态**: 🔴 待修复
**负责**: 后端
**问题**: user-service和family-service各有一个UserController
**建议方案**: 删除family-service中的UserController

### Q018 - Controller缺失 (Moments)
**状态**: 🔴 待修复
**负责**: 后端
**问题**: 前端调用 `/api/moments/xxx` 接口，后端不存在对应Controller
**建议方案**: 创建MomentsController实现6个接口

### Q019 - Controller缺失 (Schedule)
**状态**: 🔴 待修复
**负责**: 后端
**问题**: 前端调用 `/schedule/xxx` 接口，后端不存在对应Controller
**建议方案**: 创建ScheduleController实现排班功能

---

## P1 - 高优先级（影响功能）

### Q001/Q003/Q020 - 接口路径不一致
**状态**: ✅ 已修复
**负责**: 前端
**修复时间**: 2026-02-22 01:24
**修复内容**:
- Q001: Wish API路径统一为 `/api/wish/xxx`
- Q003: Schedule API路径统一为 `/api/schedule/xxx`
- Q020: User API路径统一为 `/api/user/xxx`

**修改文件**: `frontend/src/api/wish.js`, `schedule.js`, `index.js`

### Q012 - logout功能不完整
**状态**: 🟡 待修复
**负责**: 后端
**问题**: logout方法未调用StpUtil.logout()
**建议**: 添加 StpUtil.logout() 调用

### Q015 - 硬编码userId
**状态**: 🟡 待修复
**负责**: 后端
**问题**: switchFamily中硬编码`Long userId = 1L`
**建议**: 使用 StpUtil.getLoginIdAsLong()

### Q016~Q017 - 字段命名不一致
**状态**: 🟡 待修复
**负责**: 前端/后端协调
- deadline vs dueTime
- category vs categoryId

### Q021 - Coupon路径不匹配
**状态**: ✅ 已修复
**负责**: 前端
**修复时间**: 2026-02-22 01:24
**修复内容**:
- 前端API路径改为 `/api/game/coupon/xxx`，与后端一致
- 删除独立的 `couponApi`，集成到 `gameApi` 中
- 更新 `pages/coupon/index.vue` 使用 `gameApi`

**修改文件**: `frontend/src/api/game.js`, `pages/coupon/index.vue`, `api/index.js`

---

## P2 - 中优先级

### Q004/Q005 - 路径检查 ✅
经过验证，Vote和Family模块路径已匹配：
- Vote: `/vote/xxx` ✅
- Family: `/family/xxx` ✅

### Q010/Q013
**状态**: 🟢 待规划

---

## P3 - 低优先级（代码规范）

### Q006 - 导入方式不统一
**状态**: 🔵 长期规划
**建议**: 统一使用 `@/` 绝对路径导入

### Q009 - 权限检查不一致
**状态**: 🔵 长期规划
**建议**: 统一使用Sa-Token进行权限检查

---

## 修复历史记录

### 2026-02-22 01:15
- 完成首次全面审查
- 发现P0问题6项，P1问题6项，P2问题4项，P3问题3项
- 创建详细问题报告 QA_REPORT_2026-02-22.md
- 启动持续监控模式

---

## 监控状态

- **开始时间**: 2026-02-22 01:14:29
- **预计结束**: 2026-02-22 07:14:29
- **当前状态**: 🟢 运行中
- **下次检查**: 每10分钟
- **下次报告**: 每15分钟

---

## 持续审查新发现问题

### 2026-02-22 01:26-01:35 第1轮审查

#### ✅ 已验证修复
| 编号 | 问题 | 验证时间 |
|------|------|----------|
| Q008 | 重复TaskController已删除 | 01:30 |
| Q011 | ExportController权限校验已添加 | 01:33 |
| Q014 | 重复UserController已删除 | 01:32 |
| Q018 | MomentController已创建且接口完整 | 01:28 |

#### 🔴 新发现问题
| 编号 | 类型 | 优先级 | 描述 |
|------|------|--------|------|
| Q023 | 编译错误 | P0 | CacheAspect Lombok编译失败 |
| Q024 | 接口不一致 | P1 | ScheduleController与前端不匹配 |
| Q025 | 接口缺失 | P1 | Task子任务/重复规则/提醒接口缺失 |
| Q026 | 接口不一致 | P1 | FamilyController路径缺少/api前缀 |
| Q027 | 接口不一致 | P1 | VoteController路径缺少/api前缀 |
| Q028 | 接口不一致 | P1 | UserController路径缺少/api前缀 |
| Q029 | 接口不一致 | P1 | RecipeController路径缺少/api前缀 |
| Q030 | 接口不一致 | P1 | PointsCouponController与game.js不匹配 |
| Q031 | 接口缺失 | P2 | Shopping价格相关接口缺失(5个) |

#### ⚠️ Q023可能已修复
新commit `a8945a5` 更新了pom.xml，配置Lombok annotationProcessorPaths v1.18.30

### 2026-02-22 01:33 第2轮审查 - 新commit a8945a5

#### 🆕 新代码: 扫码录入功能
- 前端shopping/index.vue: 扫码UI完成 ✅
- 后端ShoppingController.scan: 接口已存在 ✅
- 缓存系统代码: 新增CacheAspect等

#### 🔴 新发现问题
- Q031: Shopping价格历史/追踪/比价接口缺失
