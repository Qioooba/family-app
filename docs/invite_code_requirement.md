# 家庭小程序 - 注册强制验证邀请码功能

## 任务概述
家庭内部小程序，必须填写正确邀请码才能注册，不允许外人使用。

---

## 一、修改的文件列表

### 前端文件
1. `/Volumes/document/Projects/family-app/frontend/src/pages/register/index.vue`

### 后端文件
1. `/Volumes/document/Projects/family-app/backend/family-service/src/main/java/com/family/family/controller/UserController.java`

### 新增数据库文件
1. `/Volumes/document/Projects/family-app/database/init_default_invite_code.sql`

---

## 二、前后端修改的代码

### 2.1 前端修改 (register/index.vue)

#### 2.1.1 邀请码输入框 - 改为必填
```vue
<!-- 邀请码输入（必填） -->
<view class="form-item">
  <text class="input-label">邀请码 <text class="required">*</text></text>
  <view class="input-wrapper">
    <text class="input-icon">🔑</text>
    <input
      v-model="form.inviteCode"
      class="register-input"
      placeholder="请输入家庭邀请码"
      placeholder-class="input-placeholder"
      maxlength="20"
      type="text"
    />
  </view>
  <view class="input-hint">家庭内部小程序，必须填写正确邀请码才能注册</view>
</view>
```

#### 2.1.2 表单验证 - 添加邀请码必填验证
```javascript
// 表单验证
const validateForm = () => {
  // 邀请码必填验证
  if (!form.inviteCode.trim()) {
    uni.showToast({ title: '请输入邀请码', icon: 'none' })
    return false
  }
  // ... 其他验证
  return true
}
```

#### 2.1.3 注册处理函数 - 先验证邀请码再注册
```javascript
const handleRegister = async () => {
  if (!validateForm()) return

  loading.value = true
  try {
    // 第一步：先验证邀请码是否有效
    const verifyResult = await familyApi.verifyCode(form.inviteCode.trim())
    if (verifyResult.code !== 200) {
      uni.showToast({ title: '邀请码无效或已过期', icon: 'none' })
      loading.value = false
      return
    }

    // 第二步：注册用户（携带邀请码）
    const registerData = {
      nickname: form.nickname,
      username: form.username,
      password: form.password,
      inviteCode: form.inviteCode.trim()
    }

    const registerRes = await userStore.register(registerData)

    // 第三步：使用邀请码加入家庭
    if (registerRes && registerRes.id) {
      try {
        const joinResult = await familyApi.joinByCode(form.inviteCode.trim(), registerRes.id)
        uni.showToast({ title: `注册成功，已加入家庭`, icon: 'success' })
      } catch (joinError) {
        uni.showToast({ title: '注册成功', icon: 'success' })
      }
    }
    // ... 自动登录跳转
  } catch (e) {
    uni.showToast({ title: e.message || '注册失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}
```

---

### 2.2 后端修改 (UserController.java)

#### 2.2.1 注入InviteCodeService
```java
@Autowired
private FamilyMemberMapper familyMemberMapper;

@Autowired
private InviteCodeService inviteCodeService;
```

#### 2.2.2 注册接口修改 - 添加邀请码验证
```java
@PostMapping("/register")
@SaIgnore
@RateLimit(qps = 3.0, message = "注册请求过于频繁，请稍后再试")
public Map<String, Object> register(@RequestBody Map<String, Object> params) {
    Map<String, Object> result = new HashMap<>();
    try {
        String phone = (String) params.get("phone");
        String username = (String) params.get("username");
        String password = (String) params.get("password");
        String nickname = (String) params.get("nickname");
        String inviteCode = (String) params.get("inviteCode");

        // 校验邀请码（必填）
        if (inviteCode == null || inviteCode.trim().isEmpty()) {
            result.put("code", 400);
            result.put("message", "请输入邀请码");
            return result;
        }

        // 验证邀请码是否有效
        com.family.family.entity.InviteCode verifiedCode = inviteCodeService.verifyInviteCode(inviteCode.trim());
        if (verifiedCode == null) {
            result.put("code", 400);
            result.put("message", "邀请码无效或已过期");
            return result;
        }

        // ... 用户创建逻辑 ...

        // 创建新用户，设置邀请码对应的家庭
        user.setCurrentFamilyId(verifiedCode.getFamilyId());

        // 自动将用户加入邀请码对应的家庭
        FamilyMember familyMember = new FamilyMember();
        familyMember.setFamilyId(verifiedCode.getFamilyId());
        familyMember.setUserId(user.getId());
        familyMember.setRole("member");
        familyMember.setNickname(nickname);
        familyMember.setJoinTime(LocalDateTime.now());
        familyMemberMapper.insert(familyMember);

        // 更新邀请码使用次数
        verifiedCode.setUsedCount(verifiedCode.getUsedCount() + 1);
        inviteCodeService.updateById(verifiedCode);

        // ... 返回结果 ...
    }
}
```

---

## 三、注册流程说明（含邀请码验证）

### 3.1 完整注册流程

```
┌─────────────────────────────────────────────────────────────┐
│                      用户注册流程                             │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  1. 填写表单                                                │
│     ├── 用户名（必填）                                       │
│     ├── 昵称（必填）                                         │
│     ├── 密码（必填）                                         │
│     ├── 确认密码（必填）                                     │
│     └── 邀请码（必填）★★★                                   │
│                                                             │
│  2. 前端验证                                                │
│     ├── 检查邀请码是否填写 → 未填写提示"请输入邀请码"         │
│     ├── 检查用户名、昵称、密码格式                           │
│     └── 同意用户协议                                        │
│                                                             │
│  3. 后端验证邀请码                                          │
│     ├── 检查邀请码是否填写                                   │
│     ├── 查询邀请码是否存在且启用                             │
│     ├── 检查邀请码是否过期                                   │
│     └── 检查邀请码使用次数是否已达上限                       │
│                                                             │
│  4. 创建用户                                                │
│     ├── 验证手机号/用户名是否已注册                          │
│     ├── 创建用户记录                                         │
│     └── 设置用户为邀请码对应的家庭                           │
│                                                             │
│  5. 加入家庭                                                │
│     ├── 在family_member表添加成员记录                       │
│     └── 更新邀请码used_count                                │
│                                                             │
│  6. 返回结果                                                │
│     └── 注册成功，自动登录                                   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 3.2 邀请码验证逻辑

**InviteCodeService.verifyInviteCode() 验证步骤：**

1. **查询邀请码** - 根据code查询status=1的记录
2. **检查过期时间** - expires_at > 当前时间
3. **检查使用次数** - used_count < max_uses
4. **返回验证结果** - 通过返回InviteCode对象，否则返回null

### 3.3 错误提示说明

| 场景 | 前端提示 | 后端返回 |
|------|----------|----------|
| 邀请码未填写 | 请输入邀请码 | 请输入邀请码 |
| 邀请码不存在/已禁用 | 邀请码无效或已过期 | 邀请码无效或已过期 |
| 邀请码已过期 | 邀请码无效或已过期 | 邀请码无效或已过期 |
| 邀请码已达使用上限 | 邀请码无效或已过期 | 邀请码无效或已过期 |

---

## 四、数据库检查

### 4.1 邀请码表 (family_invite_code)

```sql
CREATE TABLE IF NOT EXISTS family_invite_code (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    family_id BIGINT NOT NULL COMMENT '家庭ID',
    code VARCHAR(20) NOT NULL COMMENT '邀请码',
    creator_id BIGINT NOT NULL COMMENT '创建者ID',
    max_uses INT DEFAULT 1 COMMENT '最大使用次数',
    used_count INT DEFAULT 0 COMMENT '已使用次数',
    expires_at DATETIME COMMENT '过期时间',
    status TINYINT DEFAULT 1 COMMENT '状态 0禁用 1启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_code (code),
    INDEX idx_family_id (family_id),
    INDEX idx_creator_id (creator_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家庭邀请码表';
```

### 4.2 初始化默认邀请码

```sql
-- 默认邀请码：FAMILY01
-- 家庭ID：1（默认家庭）
-- 有效期：10年
-- 最大使用次数：10000次
INSERT INTO family_invite_code 
  (code, family_id, creator_id, max_uses, used_count, status, expires_at, create_time)
VALUES 
  ('FAMILY01', 1, 1, 10000, 0, 1, DATE_ADD(NOW(), INTERVAL 10 YEAR), NOW());
```

---

## 五、测试结果

### 5.1 测试用例

| # | 测试场景 | 预期结果 | 状态 |
|---|----------|----------|------|
| 1 | 不填写邀请码直接注册 | 提示"请输入邀请码" | ✅ |
| 2 | 填写无效邀请码 | 提示"邀请码无效或已过期" | ✅ |
| 3 | 填写已过期的邀请码 | 提示"邀请码无效或已过期" | ✅ |
| 4 | 填写已达到使用上限的邀请码 | 提示"邀请码无效或已过期" | ✅ |
| 5 | 填写正确的邀请码FAMILY01 | 注册成功并加入对应家庭 | ✅ |
| 6 | 邀请码使用次数正确递增 | used_count +1 | ✅ |
| 7 | 用户自动加入对应家庭 | family_member表新增记录 | ✅ |

### 5.2 测试数据

```sql
-- 测试用邀请码
-- 邀请码: FAMILY01
-- 家庭ID: 1
-- 有效期: 2026-03-07 ~ 2036-03-07
-- 已使用: 0 次
-- 剩余: 10000 次
```

---

## 六、部署注意事项

1. **数据库**：执行 `init_default_invite_code.sql` 初始化默认邀请码
2. **后端**：重新编译部署 `family-service`
3. **前端**：重新编译部署 H5/小程序
4. **现有用户**：已注册用户不受影响，新用户必须使用邀请码注册
