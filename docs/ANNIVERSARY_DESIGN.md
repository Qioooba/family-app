# 纪念日/倒计时功能设计文档

## 功能概述
在首页添加纪念日/倒计时功能模块，展示重要的家庭纪念日。

## 功能设计

### 1. 首页展示区域
- **位置**：首页"今日待办"下方，"今日菜谱"上方
- **样式**：温馨卡片式，圆角设计，与其他模块风格统一
- **显示**：
  - 最近即将到来的纪念日（1-3个）
  - 倒计时天数（如：还有5天、今天、已过去3天）
  - 纪念日名称和日期
  - 点击展开查看更多/添加

### 2. 功能特性
- **添加纪念日**：
  - 名称（如：结婚纪念日、生日、宝宝出生日）
  - 日期（公历/农历选择）
  - 是否重复（每年/仅一次）
  - 提醒设置（提前1天/3天/7天）
  - 备注（可选）

- **显示逻辑**：
  - 按倒计时排序（最近的在前）
  - 已过期的显示"已过去X天"
  - 当天的显示"就是今天！🎉"
  - 每年重复的节日自动计算下一年的倒计时

- **快捷操作**：
  - 首页显示最近1-3个
  - 点击"更多"查看全部
  - 支持编辑/删除

### 3. 页面设计
```
┌─────────────────────────┐
│ 💕 纪念日                 │
├─────────────────────────┤
│ 🎂 宝宝生日    还有5天   │
│ 2024年3月15日            │
├─────────────────────────┤
│ 💑 结婚纪念日  还有12天  │
│ 2020年6月1日             │
├─────────────────────────┤
│ 👵 奶奶生日    就是今天🎉│
│ 1950年1月1日             │
├─────────────────────────┤
│     [查看更多] [+ 添加]  │
└─────────────────────────┘
```

### 4. 技术实现
- **前端**：
  - 首页添加纪念日组件
  - 弹窗添加/编辑纪念日
  - 列表页展示全部纪念日
  
- **后端**：
  - 数据库表：anniversary
  - API接口：
    - GET /api/anniversary/list - 获取列表
    - POST /api/anniversary/create - 创建
    - PUT /api/anniversary/update - 更新
    - DELETE /api/anniversary/delete - 删除

- **数据库表结构**：
```sql
CREATE TABLE anniversary (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    family_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL COMMENT '纪念日名称',
    date DATE NOT NULL COMMENT '日期',
    is_lunar TINYINT DEFAULT 0 COMMENT '是否农历 0-公历 1-农历',
    is_repeat TINYINT DEFAULT 1 COMMENT '是否每年重复 0-仅一次 1-每年',
    reminder_days INT DEFAULT 3 COMMENT '提前提醒天数',
    note VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_family_date (family_id, date)
);
```

### 5. 开发计划
- 前端：首页组件 + 添加/编辑弹窗 + 列表页
- 后端：实体类 + Mapper + Service + Controller
- 预计工期：半天

---
**创建时间**：2026-02-23  
**优先级**：高
