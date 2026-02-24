# 饮水目标设置功能 - 开发完成报告

## 📋 功能概述

已完成首页健康概览饮水目标设置功能的全部开发工作，包括前端弹窗UI、后端API和数据库表结构。

## ✅ 已实现功能

### 1. 前端功能 (pages/home/index.vue)

#### 饮水目标设置弹窗
- **快速选择**: 1500ml / 2000ml / 2500ml / 3000ml 四个预设选项
- **手动输入**: 支持自定义输入目标饮水量（500-5000ml范围验证）
- **当前目标显示**: 弹窗顶部显示当前设置的目标值
- **友好提示**: 显示建议每日饮水量范围（1500-2500ml）

#### 实时显示区域
- **统计卡片**: 显示当前饮水量（ml）
- **进度条**: 显示饮水进度百分比，支持动画效果
- **点击交互**: 点击饮水统计卡片或进度条可打开设置弹窗
- **编辑提示**: 饮水卡片右上角显示编辑图标提示可点击

#### 触发点
1. 健康概览区域 - 饮水统计卡片
2. 健康概览区域 - 饮水进度条

### 2. 后端API (/api/health/water)

| 接口 | 方法 | 描述 |
|------|------|------|
| `/api/health/water/target` | POST | 设置饮水目标 |
| `/api/health/water/target` | GET | 获取饮水目标 |
| `/api/health/water/today` | GET | 获取今日饮水数据（包含目标值） |

#### 主要代码文件
- `WaterController.java` - REST API 控制器
- `UserWaterGoalService.java` - 服务接口
- `UserWaterGoalServiceImpl.java` - 服务实现
- `UserWaterGoalMapper.java` - 数据访问层
- `UserWaterGoal.java` - 实体类
- `WaterTodayVO.java` - 返回数据VO

### 3. 数据库表

#### user_water_goal 表结构
```sql
CREATE TABLE IF NOT EXISTS user_water_goal (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    target_amount INT NOT NULL DEFAULT 2000 COMMENT '每日目标饮水量（毫升）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_id (user_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户饮水目标设置表';
```

## 📁 修改/涉及文件

### 前端文件
1. `/frontend/src/pages/home/index.vue` - 首页（已包含弹窗UI和逻辑）
2. `/frontend/src/api/water.js` - API调用（已包含setTarget/getTarget）

### 后端文件
1. `/backend/family-service/health-service/src/main/resources/schema.sql` - 数据库表结构（已更新）
2. `WaterController.java` - 控制器（已存在）
3. `UserWaterGoalService.java` - 服务接口（已存在）
4. `UserWaterGoalServiceImpl.java` - 服务实现（已存在）
5. `UserWaterGoalMapper.java` - Mapper（已存在）
6. `UserWaterGoal.java` - 实体类（已存在）

### 构建产物
- `/backend/family-service/health-service/target/health-service-1.0.0.jar`

## 🎨 UI预览

```
┌─────────────────────────────────────┐
│  💧 设置饮水目标                 [X] │
├─────────────────────────────────────┤
│  当前目标                    2000ml │
│                                     │
│  每日目标饮水量 (ml)                │
│  ┌─────────────────────────────┐    │
│  │         2000                │    │
│  └─────────────────────────────┘    │
│                                     │
│  快速选择                           │
│  ┌────────┬────────┬────────┬─────┐│
│  │ 1500ml │ 2000ml │ 2500ml │3000ml│  <- 可点击选择
│  └────────┴────────┴────────┴─────┘│
│                                     │
│  [i] 建议每日饮水 1500-2500ml...    │
│                                     │
│  ┌──────────┐  ┌────────────────┐   │
│  │   取消   │  │      保存      │   │
│  └──────────┘  └────────────────┘   │
└─────────────────────────────────────┘
```

## 🔌 API 调用示例

### 设置饮水目标
```javascript
// POST /api/health/water/target
const res = await waterApi.setTarget(2500)
// 返回: { userId: 1, targetAmount: 2500, success: true }
```

### 获取今日饮水数据
```javascript
// GET /api/health/water/today?userId=1
const res = await waterApi.getToday(userId)
// 返回: { 
//   userId: 1, 
//   todayAmount: 1200, 
//   targetAmount: 2000, 
//   percent: 60,
//   status: "half",
//   message: "已完成一半，继续加油！"
// }
```

## 🚀 部署步骤

1. **执行数据库脚本**
   ```sql
   -- 在MySQL中执行 schema.sql 中的 user_water_goal 表创建语句
   ```

2. **部署后端服务**
   ```bash
   # 启动 health-service
   java -jar backend/family-service/health-service/target/health-service-1.0.0.jar
   ```

3. **部署前端**
   ```bash
   # 在HBuilderX中运行或打包
   cd frontend
   # 开发调试
   npm run dev:%PLATFORM%
   # 打包发布
   npm run build:%PLATFORM%
   ```

## 📝 注意事项

1. **默认目标值**: 新用户默认饮水目标为 2000ml
2. **输入验证**: 目标值必须在 500-5000ml 范围内
3. **数据同步**: 设置目标后会立即更新首页显示
4. **错误处理**: 设置失败会显示详细错误信息

## ✨ 功能特点

- ✅ 快速选择常用目标值
- ✅ 自定义输入目标值
- ✅ 实时进度显示
- ✅ 动画效果流畅
- ✅ 移动端适配良好
- ✅ 输入验证完整
- ✅ 错误处理完善
