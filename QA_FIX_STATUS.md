# QA修复状态追踪

## 当前状态概览
- **待修复**: 23 项
- **已修复**: 3 项 (Q001/Q003/Q020/Q021)
- **验证通过**: 1 项 (Q011)
- **验证失败**: 4 项 (Q008/Q014/Q019/Q023)
- **阻塞中**: 6 项 (P0级别)

---

## P0 - 阻塞级别（影响编译/启动/安全）

### Q023 - 编译错误 (CacheAspect Lombok)
**状态**: 🔴 **验证失败 - 仍需修复**
**验证时间**: 2026-02-22 09:42
**验证结果**: 
- ⚠️ common-core编译通过（有警告）
- ❌ family-service编译失败 - LogAspect.java第107行找不到`log`变量
- ❌ 根pom.xml的maven-compiler-plugin缺少`annotationProcessorPaths`配置
**问题根因**: @Slf4j注解未生效，Lombok处理器未正确配置
**修复内容**: 需要在根pom.xml中添加:
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <annotationProcessorPaths>
            <path>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
                <version>${lombok.version}</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```
```xml
<annotationProcessorPaths>
    <path>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>1.18.30</version>
    </path>
</annotationProcessorPaths>
```

### Q007 - 编译错误 (Lombok)
**状态**: 🟡 需重新验证
**负责**: 后端
**问题**: Lombok 1.18.32 与 Java 17 兼容性问题导致user-service编译失败

### Q022 - 编译错误 (依赖)
**状态**: 🟡 需重新验证
**负责**: 后端
**问题**: bucket4j-core 8.7.0 依赖无法从Maven Central下载

### Q008 - 架构问题 (TaskController重复)
**状态**: 🔴 **验证失败 - 仍需修复**
**验证时间**: 2026-02-22 09:43
**验证结果**: ❌ 仍然存在两个TaskController：
1. task-service/src/main/java/com/family/task/controller/TaskController.java
2. family-service/src/main/java/com/family/family/controller/TaskController.java
**问题**: family-service中的TaskController仍然存在，应删除

## P0 - 阻塞级别（影响编译/启动/安全）

### Q011 - 安全问题 (ExportController无权限)
**状态**: ✅ **已验证通过**
**验证时间**: 2026-02-22 09:43
**验证结果**: ExportController已有`@SaCheckLogin`注解，权限校验正确

### Q014 - 架构问题 (UserController重复)
**状态**: 🔴 **验证失败 - 仍需修复**
**验证时间**: 2026-02-22 09:43
**验证结果**: ❌ 仍然存在两个UserController：
1. user-service/src/main/java/com/family/user/controller/UserController.java ✅
2. family-service/src/main/java/com/family/family/controller/UserController.java ❌ 应删除
**建议方案**: 删除family-service中的UserController，前端应调用user-service的接口

### Q018 - Controller缺失 (Moments)
**状态**: 🔴 待修复
**负责**: 后端
**问题**: 前端调用 `/api/moments/xxx` 接口，后端不存在对应Controller
**建议方案**: 创建MomentsController实现6个接口

### Q019 - Controller缺失 (Schedule)
**状态**: 🔴 **验证失败 - 仍需修复**
**验证时间**: 2026-02-22 09:42
**验证结果**: 
- ✅ ScheduleController已创建
- ❌ 缺少Schedule实体类（entity.Schedule不存在，只有TaskSchedule）
- ❌ 缺少ScheduleService接口和实现类
- ❌ 导致family-service编译失败（11个编译错误）
**修复内容**: 需要创建:
1. com.family.family.entity.Schedule 实体类
2. com.family.family.service.ScheduleService 接口
3. com.family.family.service.impl.ScheduleServiceImpl 实现类

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

### 2026-02-22 01:45 第3轮审查 - 编译检查

#### 🔴 严重: family-service编译失败
编译错误统计:
- ScheduleController.java: 11个错误 - 缺少Schedule实体类
- TaskRepeatService.java: 2个错误 - 错误引用TaskController
- TaskRepeatServiceImpl.java: 3个错误 - 错误引用TaskController  
- LogAspect.java: 1个错误 - Lombok @Slf4j未生效
- ExportController.java: 2个错误 - 方法引用无效
- MomentsController.java: 4个错误 - 类型不匹配

#### 🔴 新增编译问题编号
| 编号 | 类型 | 描述 |
|------|------|------|
| Q032 | 编译错误 | Schedule实体类缺失 |
| Q033 | 编译错误 | TaskRepeatService错误引用TaskController |
| Q034 | 编译错误 | LogAspect Lombok问题 |
| Q035 | 编译错误 | ExportController方法引用错误 |
| Q036 | 编译错误 | MomentsController类型不匹配 |

**状态**: family-service模块编译失败，阻塞整个项目构建！

---

## QA验证报告 - 2026-02-22 09:40

### 验证人: QA验证子代理
### 验证范围: P0级别问题修复状态

### 验证结果汇总

| 编号 | 问题 | 原状态 | 验证结果 | 备注 |
|------|------|--------|----------|------|
| Q008 | TaskController重复 | ✅已修复 | 🔴验证失败 | 两个TaskController仍然存在 |
| Q011 | ExportController权限 | ✅已修复 | ✅验证通过 | @SaCheckLogin已添加 |
| Q014 | UserController重复 | 🔴待修复 | 🔴验证失败 | 两个UserController仍然存在 |
| Q019 | ScheduleController缺失 | ⚠️部分修复 | 🔴验证失败 | 缺少Schedule实体和Service |
| Q023 | Lombok编译错误 | ✅已修复 | 🔴验证失败 | 根pom.xml缺少annotationProcessorPaths配置 |

### 新增发现问题

| 编号 | 类型 | 优先级 | 描述 |
|------|------|--------|------|
| Q037 | 接口不匹配 | P1 | MomentService接口与MomentsController调用不匹配 |
| Q038 | Lombok问题 | P0 | 所有@Data注解实体类getter/setter未生成，导致TaskAttachment/TaskComment等Controller编译失败 |

### 编译错误统计 (family-service)

```
总错误数: 40+ 个
- ScheduleController.java: 11个错误
- LogAspect.java: 1个错误 (Lombok @Slf4j)
- ExportController.java: 2个错误
- MomentsController.java: 4个错误
- TaskAttachmentController.java: 11个错误 (实体类getter/setter)
- TaskCommentController.java: 11个错误 (实体类getter/setter)
- TaskFilterController.java: 1个错误
```

### 阻塞问题

1. **P0: Q023 Lombok配置问题** - 导致所有实体类无法编译
2. **P0: Q019 Schedule实体缺失** - 导致ScheduleController无法编译
3. **P1: Q037 MomentService接口不匹配** - 导致MomentsController无法编译

### 建议优先修复顺序

1. 🔴 修复根pom.xml的Lombok配置 (Q023)
2. 🔴 删除重复的Controller (Q008, Q014)
3. 🔴 创建Schedule实体和Service (Q019)
4. 🔴 修复MomentService接口 (Q037)
5. 🟡 修复ExportController方法引用 (Q035)
