# 微信登录和邀请码问题修复报告

## 修复时间
2026-03-07

## 问题1: 微信登录 401 错误

### 问题原因
1. **数据库字段名不匹配**: `UserMapper.selectByWxOpenid` 使用 `wx_openid`，但数据库表字段是 `open_id`
2. **缺少详细日志**: 无法快速定位问题
3. **微信配置可能为空**: 没有检查 `appId` 和 `appSecret`

### 修复内容

#### 1. 修复 UserMapper.java
**文件**: `backend/family-service/src/main/java/com/family/family/mapper/UserMapper.java`

```java
// 修改前
@Select("SELECT * FROM sys_user WHERE wx_openid = #{wxOpenid} LIMIT 1")

// 修改后
@Select("SELECT * FROM sys_user WHERE open_id = #{wxOpenid} LIMIT 1")
```

#### 2. 增强 WxDecryptUtil.java
**文件**: `backend/family-service/src/main/java/com/family/family/utils/WxDecryptUtil.java`

- 添加参数校验（appId, appSecret, code）
- 添加详细日志输出
- 添加 HTTP 超时设置
- 改进错误处理

#### 3. 增强 WxLoginController.java
**文件**: `backend/family-service/src/main/java/com/family/family/controller/WxLoginController.java`

- 添加微信配置检查
- 添加详细日志（每个步骤）
- 改进错误信息和状态码

## 问题2: 邀请码 404 错误

### 问题原因
1. **数据库没有默认邀请码**: `family_invite_code` 表中没有数据
2. **缺少日志**: 无法判断是找不到还是已过期

### 修复内容

#### 1. 创建默认邀请码 SQL
**文件**: `database/fix_invite_code_default.sql`

```sql
INSERT INTO family_invite_code (family_id, code, creator_id, max_uses, used_count, expires_at, status, create_time) 
VALUES (1, '111222', 1, 100, 0, '2026-12-31 23:59:59', 1, NOW())
ON DUPLICATE KEY UPDATE 
    expires_at = '2026-12-31 23:59:59',
    status = 1,
    max_uses = 100;
```

#### 2. 增强 InviteCodeController.java
**文件**: `backend/family-service/src/main/java/com/family/family/controller/InviteCodeController.java`

- 添加 `@Slf4j` 日志支持
- 添加详细日志输出

#### 3. 增强 InviteCodeServiceImpl.java
**文件**: `backend/family-service/src/main/java/com/family/family/service/impl/InviteCodeServiceImpl.java`

- 添加 `@Slf4j` 日志支持
- 在 `verifyInviteCode` 方法中添加详细日志

## 执行步骤

### 1. 执行 SQL 插入默认邀请码
```bash
mysql -u root -p family_app < database/fix_invite_code_default.sql
```

### 2. 检查微信配置
确保环境变量已设置：
```bash
export WEIXIN_APPID=your_appid
export WEIXIN_APPSECRET=your_appsecret
```

或在 `application.yml` 中直接配置：
```yaml
weixin:
  appId: your_actual_appid
  appSecret: your_actual_appsecret
```

### 3. 重新编译部署
```bash
cd backend/family-service
mvn clean package -DskipTests
docker restart family-service  # 或手动启动
```

## 测试建议

### 微信登录测试
1. 使用微信小程序调用 `/api/user/wx-login`
2. 检查日志输出，确认每个步骤正常
3. 验证新用户是否自动创建
4. 验证老用户是否正常登录

### 邀请码测试
1. 调用 `/api/family/verify-code` 验证邀请码 `111222`
2. 检查日志输出，确认验证流程
3. 测试使用邀请码加入家庭 `/api/family/join-by-code`

## 日志查看
```bash
# 查看实时日志
tail -f /path/to/your/app.log | grep -E "(微信|邀请码)"

# 关键日志关键字
- "收到微信登录请求"
- "微信登录获取 openid 成功"
- "新用户创建成功"
- "收到验证邀请码请求"
- "邀请码验证通过"
```

## 备注
- 默认邀请码 `111222` 有效期至 2026-12-31
- 默认邀请码最多可使用 100 次
- 所有修复代码已添加详细日志，便于排查问题
