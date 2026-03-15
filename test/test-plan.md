# 提醒功能测试计划

## 测试发现问题

### 1. 接口参数类型检查

前端发送数据格式 (add.vue):
```javascript
{
  reminderName: String,
  reminderType: String,
  priority: Number,
  frequencyType: String,
  frequencyConfig: JSON.stringify(Object), // String
  pushScope: String,
  targetUserIds: JSON.stringify(Array) || null, // String
  quietHoursStart: String || null,
  quietHoursEnd: String || null,
  titleTemplate: String,
  contentTemplate: String,
  remindTime: String,
  status: Number
}
```

后端接收 (Reminder.java):
```java
private String reminderName; ✓
private String reminderType; ✓
private Integer priority; ✓ (Number->Integer)
private String frequencyType; ✓
private String frequencyConfig; ✓ (JSON字符串)
private String pushScope; ✓
private String targetUserIds; ✓ (JSON字符串)
private String quietHoursStart; ✓
private String quietHoursEnd; ✓
private String titleTemplate; ✓
private String contentTemplate; ✓
```

### 2. 需要测试的接口

| 接口 | 方法 | 参数 | 预期 |
|------|------|------|------|
| /api/reminder/list | GET | - | 返回提醒列表 |
| /api/reminder/detail/{id} | GET | id | 返回详情 |
| /api/reminder/add | POST | Reminder对象 | 创建成功 |
| /api/reminder/update | POST | Reminder对象 | 更新成功 |
| /api/reminder/toggle | POST | id | 切换状态 |
| /api/reminder/delete | POST | id | 删除成功 |
| /api/reminder/users | GET | - | 返回用户列表 |

### 3. 测试步骤

1. 先测试公开接口（无需登录）
2. 使用 mock 登录态测试需要登录的接口
3. 检查响应数据格式
4. 检查字段类型一致性

