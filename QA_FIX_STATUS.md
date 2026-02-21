# QA修复状态追踪

## 当前状态概览
- **待修复**: 17 项
- **已修复**: 0 项
- **验证通过**: 0 项
- **阻塞中**: 5 项 (P0级别)

---

## P0 - 阻塞级别（影响编译/启动/安全）

### Q007 - 编译错误
**状态**: 🔴 待修复
**负责**: 后端
**问题**: Lombok 1.18.32 与 Java 17 兼容性问题导致user-service编译失败
**建议方案**:
1. 升级Lombok到1.18.30+
2. 或升级maven-compiler-plugin
3. 检查annotationProcessorPaths配置

### Q008 - 架构问题 (TaskController重复)
**状态**: 🔴 待修复
**负责**: 后端
**问题**: task-service和family-service各有一个TaskController
**建议方案**: 删除family-service中的TaskController

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

---

## P1 - 高优先级（影响功能）

### Q001~Q003 - 接口路径不一致
**状态**: 🟡 待修复
**负责**: 后端/前端协调
- Q001: Wish路径不匹配 `/wish` vs `/api/wish`
- Q002: Task路径不匹配 `/task` vs `/api/task`
- Q003: Schedule路径不匹配 `/schedule` vs `/api/schedule`

### Q012 - logout功能不完整
**状态**: 🟡 待修复
**负责**: 后端
**问题**: logout方法未调用StpUtil.logout()

### Q015 - 硬编码userId
**状态**: 🟡 待修复
**负责**: 后端
**问题**: switchFamily中硬编码`Long userId = 1L`

### Q016~Q017 - 字段命名不一致
**状态**: 🟡 待修复
**负责**: 前端
- deadline vs dueTime
- category vs categoryId

---

## P2 - 中优先级

### Q004, Q005, Q010, Q013
**状态**: 🟢 待规划

---

## P3 - 低优先级（代码规范）

### Q006, Q009
**状态**: 🔵 长期规划

---

## 修复历史记录

### 2026-02-22 01:05
- 初始审查完成，发现问题17项
- 创建QA追踪文档
- P0级别问题5项，需优先处理

