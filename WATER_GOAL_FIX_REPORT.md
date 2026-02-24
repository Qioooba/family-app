# 饮水目标保存按钮报错 - 修复报告

## 问题描述
用户在设置饮水目标弹窗中点击保存按钮时报错。

## 根本原因分析

### 1. 后端问题（主要原因）
**文件**: `backend/family-service/health-service/src/main/java/com/family/health/controller/WaterController.java`

**问题**: `setTarget` 方法直接使用 `StpUtil.getLoginIdAsLong()` 获取用户ID，当用户未登录或token过期时，会抛出异常导致500错误。

```java
// 修复前
Long userId = StpUtil.getLoginIdAsLong();  // 未登录时会抛出异常
```

**修复**: 添加try-catch处理未登录情况
```java
// 修复后
Long userId;
try {
    userId = StpUtil.getLoginIdAsLong();
} catch (Exception e) {
    // 未登录
    return Result.error(4010, "请先登录");
}
```

### 2. 前端问题（增强处理）
**文件**: `frontend/src/pages/home/index.vue`

**问题**: 
- 参数验证不够严格（使用 `!target` 判断空值，无法检测NaN）
- 缺少登录状态预检查
- 错误处理不够详细

**修复**:
1. 使用 `isNaN(target)` 替代 `!target` 进行验证
2. 添加详细的日志输出便于调试
3. 添加登录状态预检查，未登录时引导用户登录
4. 根据错误码显示不同的错误提示

### 3. API层增强
**文件**: `frontend/src/api/water.js`

**增强**: 添加调试日志和参数预处理
```javascript
setTarget: (targetAmount) => {
    console.log('[WaterAPI] setTarget 被调用，参数:', targetAmount)
    const data = { targetAmount: parseInt(targetAmount) || 2000 }
    return request.post('/api/health/water/target', data)
}
```

## 修改的文件列表

1. `backend/family-service/health-service/src/main/java/com/family/health/controller/WaterController.java`
2. `frontend/src/pages/home/index.vue`
3. `frontend/src/api/water.js`

## 测试建议

1. **已登录用户测试**: 正常设置饮水目标，应保存成功
2. **未登录用户测试**: 点击保存应提示"请先登录"并跳转到登录页
3. **边界值测试**: 
   - 输入499（应提示输入500-5000之间的数值）
   - 输入5001（应提示输入500-5000之间的数值）
   - 输入1500、2000、2500等正常值（应保存成功）
4. **异常输入测试**:
   - 输入空值（应有友好提示）
   - 输入非数字（应有友好提示）

## 日志输出

修复后会在控制台输出详细的调试信息：
- `[Home] saveWaterGoal 被调用，输入值: xxx 转换后: xxx`
- `[Home] 开始调用 waterApi.setTarget，参数: xxx`
- `[WaterAPI] setTarget 被调用，参数: xxx`
- `[Home] 保存饮水目标成功，响应: xxx`

便于后续问题排查。
