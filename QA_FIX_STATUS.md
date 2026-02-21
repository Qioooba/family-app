# QA修复状态追踪

## 当前状态概览
- **待修复**: 22 项
- **已修复**: 0 项
- **验证通过**: 0 项
- **阻塞中**: 6 项 (P0级别)

---

## P0 - 阻塞级别（影响编译/启动/安全）

### Q007 - 编译错误 (Lombok)
**状态**: 🔴 待修复
**负责**: 后端
**问题**: Lombok 1.18.32 与 Java 17 兼容性问题导致user-service编译失败
**建议方案**:
1. 升级Lombok到1.18.30+
2. 或升级maven-compiler-plugin
3. 检查annotationProcessorPaths配置

### Q022 - 编译错误 (依赖)
**状态**: 🔴 待修复
**负责**: 后端
**问题**: bucket4j-core 8.7.0 依赖无法从Maven Central下载
**建议方案**:
1. 更换bucket4j版本
2. 或添加jcenter/other仓库源
3. 或使用替代库

### Q008 - 架构问题 (TaskController重复)
**状态**: 🔴 待修复
**负责**: 后端
**问题**: task-service和family-service各有一个TaskController
**建议方案**: 删除family-service中的TaskController，保留task-service版本

### Q011 - 安全问题 (ExportController无权限)
**状态**: 🔴 待修复
**负责**: 后端
**问题**: `exportFamilyData` 接口无权限校验，任何人可导出任意家庭数据
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
**状态**: 🟡 待修复
**负责**: 后端/前端协调
- Q001: Wish路径不匹配 `/wish` vs `/api/wish`
- Q003: Schedule路径不匹配 `/schedule` vs `/api/schedule`
- Q020: User路径不匹配 `/api/user` vs `/user`

**建议**: 统一规范为 `/api/{module}` 前缀

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
**状态**: 🟡 待修复
**负责**: 前后端协调
**问题**: 前端 `/api/coupon/xxx` vs 后端 `/api/game/coupon/xxx`
**建议**: 统一路径

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
