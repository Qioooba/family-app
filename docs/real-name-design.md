# 家庭成员真实姓名功能设计方案

## 1. 需求分析

### 当前问题
- 用户注册时只收集手机号，没有真实姓名
- 待办指派时显示昵称，不知道是谁
- 家庭成员可能使用微信昵称/自定义昵称，识别困难

### 目标
- 每个用户有独立的"真实姓名"和"昵称"
- 注册时必填真实姓名
- 待办、家庭成员列表优先显示真实姓名
- 支持修改真实姓名

## 2. 数据库设计

### 2.1 sys_user 表（用户基础信息）
```sql
-- 新增字段
ALTER TABLE sys_user 
ADD COLUMN real_name VARCHAR(50) DEFAULT NULL COMMENT '真实姓名' AFTER phone,
ADD COLUMN nickname VARCHAR(50) DEFAULT NULL COMMENT '昵称/别名' AFTER real_name;

-- 更新现有数据：将nickname复制到real_name
UPDATE sys_user SET real_name = nickname WHERE real_name IS NULL AND nickname IS NOT NULL;
```

### 2.2 family_member 表（家庭关系）
```sql
-- 新增字段
ALTER TABLE family_member 
ADD COLUMN real_name VARCHAR(50) DEFAULT NULL COMMENT '在这个家庭的真实姓名' AFTER nickname;

-- 说明：允许用户在不同家庭使用不同真实姓名（如工作群 vs 家庭群）
```

## 3. 后端API设计

### 3.1 修改注册接口
**POST /api/user/register**
```json
// 请求参数新增
{
  "phone": "13800138000",
  "password": "123456",
  "realName": "张三",      // 新增：必填
  "nickname": "小明"       // 新增：可选，默认同realName
}
```

### 3.2 修改用户信息接口
**PUT /api/user/info**
```json
// 支持修改realName
{
  "realName": "张三",      // 真实姓名
  "nickname": "小明",      // 昵称
  "avatar": "xxx.jpg"
}
```

### 3.3 修改家庭成员列表接口
**GET /api/family/{familyId}/members**
```json
// 返回数据新增realName
{
  "code": 200,
  "data": [
    {
      "userId": 1,
      "realName": "张三",      // 新增
      "nickname": "小明",
      "avatar": "xxx.jpg",
      "role": "owner"
    }
  ]
}
```

### 3.4 修改任务列表接口
**GET /api/task/list**
```json
// 返回assigneeRealName
{
  "code": 200,
  "data": {
    "list": [
      {
        "id": 1,
        "title": "买菜",
        "assigneeId": 1,
        "assigneeRealName": "张三",   // 新增
        "assigneeNickname": "小明"    // 保留
      }
    ]
  }
}
```

## 4. 前端设计

### 4.1 注册页面
```
┌─────────────────────────────┐
│        注册账号              │
├─────────────────────────────┤
│  手机号*                     │
│  [________________]         │
├─────────────────────────────┤
│  真实姓名*                   │
│  [________________]         │
│  建议填写真实姓名，方便家人识别 │
├─────────────────────────────┤
│  昵称（选填）                │
│  [________________]         │
│  如：爸爸、妈妈、小明        │
├─────────────────────────────┤
│  密码*                       │
│  [________________]         │
├─────────────────────────────┤
│       [注册按钮]            │
└─────────────────────────────┘
```

### 4.2 个人资料页面
```
┌─────────────────────────────┐
│  头像        [更换头像]      │
├─────────────────────────────┤
│  真实姓名    张三      [编辑]│
├─────────────────────────────┤
│  昵称        小明      [编辑]│
├─────────────────────────────┤
│  手机号      138****8000     │
└─────────────────────────────┘
```

### 4.3 显示规则

| 场景 | 显示格式 | 示例 |
|------|---------|------|
| 待办列表 | 真实姓名 | 张三 |
| 家庭成员列表 | 真实姓名(昵称) | 张三(爸爸) |
| 任务详情 | 指派人：真实姓名 | 指派人：张三 |
| 首页今日待办 | 真实姓名 | 张三 |

### 4.4 编辑真实姓名弹窗
```
┌─────────────────────────────┐
│        修改真实姓名          │
├─────────────────────────────┤
│                             │
│  真实姓名                    │
│  [________________]         │
│                             │
│  修改后所有家庭成员将看到    │
│  你的新名字                  │
│                             │
├─────────────────────────────┤
│  [取消]        [保存]       │
└─────────────────────────────┘
```

## 5. 数据迁移方案

### 5.1 现有数据处理
```sql
-- 1. 将现有nickname作为real_name
UPDATE sys_user SET real_name = nickname WHERE real_name IS NULL;

-- 2. 如果nickname为空，使用username（手机号）
UPDATE sys_user SET real_name = username WHERE real_name IS NULL;

-- 3. 设置nickname默认等于real_name
UPDATE sys_user SET nickname = real_name WHERE nickname IS NULL;

-- 4. 同步到family_member表
UPDATE family_member fm
JOIN sys_user u ON fm.user_id = u.id
SET fm.real_name = u.real_name
WHERE fm.real_name IS NULL;
```

### 5.2 默认值逻辑
- 新用户注册：realName必填，nickname选填（默认为realName）
- 已有用户：realName = nickname，nickname保持不变

## 6. 接口兼容性

### 6.1 向后兼容
- 旧版APP只使用nickname，不受影响
- 新版APP优先使用realName

### 6.2 前端适配
- 所有显示用户名称的地方统一使用工具函数
```javascript
// utils/user.js
export const getDisplayName = (user) => {
  // 优先级：真实姓名 > 昵称 > 用户名 > '未命名'
  return user.realName || user.nickname || user.username || '未命名';
}

export const getFullDisplayName = (user) => {
  // 有昵称时显示：真实姓名(昵称)
  if (user.realName && user.nickname && user.realName !== user.nickname) {
    return `${user.realName}(${user.nickname})`;
  }
  return user.realName || user.nickname || '未命名';
}
```

## 7. 实现步骤

### Phase 1: 后端基础
1. 数据库添加字段
2. 修改User实体类
3. 修改注册接口
4. 修改用户信息接口

### Phase 2: 后端关联
5. 修改家庭成员查询接口（返回realName）
6. 修改任务接口（返回assigneeRealName）
7. 修改其他涉及用户信息的接口

### Phase 3: 前端页面
8. 修改注册页面（添加真实姓名输入）
9. 修改个人资料页面（添加真实姓名编辑）
10. 修改家庭成员选择器（显示真实姓名）

### Phase 4: 前端显示
11. 统一修改所有显示用户名称的地方
12. 待办列表显示真实姓名
13. 家庭成员列表显示真实姓名(昵称)

## 8. 注意事项

1. **数据一致性**：修改sys_user.realName后，需要同步更新family_member.realName
2. **隐私保护**：真实姓名只在家庭内部可见
3. **字符限制**：真实姓名限制2-20个字符，支持中文
4. **唯一性**：真实姓名不需要全局唯一，同家庭内可区分即可

## 9. 测试 checklist

- [ ] 新用户注册填写真实姓名
- [ ] 注册后个人资料显示正确
- [ ] 修改真实姓名后同步到家庭成员
- [ ] 待办指派显示真实姓名
- [ ] 家庭成员列表显示真实姓名(昵称)
- [ ] 旧用户数据迁移正确
- [ ] 为空时显示fallback逻辑正确
