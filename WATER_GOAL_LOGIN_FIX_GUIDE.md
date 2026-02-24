# 饮水目标保存失败修复指南

## 问题描述
用户点击保存饮水目标时提示"请先登录"（错误码4010），即使用户已经登录。

## 根本原因
**health-service 与 user-service 的 Sa-Token 配置不一致**，导致：
- user-service 生成的 token 无法在 health-service 中正确验证
- 两个服务的 Sa-Token 配置参数不匹配

### 配置差异对比
| 配置项 | user-service | health-service（修复前） |
|--------|--------------|--------------------------|
| timeout | 2592000 | 86400 |
| activity-timeout | -1 | 未配置 |
| is-concurrent | true | 未配置 |
| is-share | false | 未配置 |
| token-style | uuid | 未配置 |
| is-log | false | 未配置 |

## 修复内容

### 1. 后端配置修复 ✅
**文件:** `backend/family-service/health-service/src/main/resources/application.yml`

将 Sa-Token 配置与 user-service 统一：
```yaml
sa-token:
  token-name: Authorization
  timeout: 2592000
  activity-timeout: -1
  is-concurrent: true
  is-share: false
  token-style: uuid
  is-log: true  # 开启调试日志便于排查问题
```

### 2. 后端调试日志增强 ✅
**文件:** `backend/family-service/health-service/src/main/java/com/family/health/controller/WaterController.java`

在 `setTarget` 方法中添加了详细的请求头和 token 调试信息：
- 打印 Authorization Header
- 打印所有请求头
- 打印 Sa-Token 当前状态

### 3. 前端调试日志增强 ✅
**文件:** 
- `frontend/src/utils/request.js`
- `frontend/src/pages/home/index.vue`

添加了详细的 token 传递调试日志，包括：
- token 原始值、类型、长度
- 请求头详情
- 错误详情

## 测试步骤

### 步骤1: 重启 health-service
```bash
cd /Users/qi/.openclaw/workspace/family-app/backend/family-service/health-service
# 停止现有服务
# 重新打包并启动
mvn clean package -DskipTests
java -jar target/health-service-*.jar
```

### 步骤2: 测试登录
1. 打开小程序/前端页面
2. 使用账号密码登录
3. 检查控制台是否有 token 输出

### 步骤3: 测试保存饮水目标
1. 在首页点击饮水进度区域
2. 设置新的饮水目标（如 2500ml）
3. 点击保存
4. 观察控制台日志

## 预期结果
### 前端控制台应输出：
```
[Home] ========== saveWaterGoal 开始 ==========
[Home] 输入值: 2500 转换后: 2500
[Home] 保存饮水目标前检查 ==========
[Home] token原始值: f692752c-51c6-4c9a-bded-654bc3f2015b
[Home] token类型: string
[Home] token长度: 36
[Home] token存在: true
[Request Debug] POST /api/health/water/target
[Request Debug] token原始值: f692752c-51c6-4c9a-bded-654bc3f2015b
[Request Debug] 请求头: {
  "Content-Type": "application/json",
  "Authorization": "f692752c-51c6-4c9a-bded-654bc3f2015b",
  "X-Request-Id": "..."
}
```

### 后端控制台应输出：
```
[设置饮水目标] ========== 请求开始 ==========
[设置饮水目标] 请求参数: {targetAmount=2500}
[设置饮水目标] Authorization Header: f692752c-51c6-4c9a-bded-654bc3f2015b
[设置饮水目标] Sa-Token 当前token: f692752c-51c6-4c9a-bded-654bc3f2015b
[设置饮水目标] Sa-Token 是否登录: true
[设置饮水目标] 从token获取用户ID: 123
[设置饮水目标] 用户ID: 123, 目标: 2500
[设置饮水目标] 设置成功: {...}
```

## 如果问题仍然存在

### 检查清单
1. ✅ health-service 是否已重启？
2. ✅ 两个服务是否连接到同一个 Redis？
3. ✅ 前端是否正确存储了 token？
4. ✅ 网络请求中是否携带了 Authorization header？

### 进一步排查
1. 检查 Redis 中是否存在 token：
   ```bash
   redis-cli
   keys "Authorization:*"
   ```

2. 检查 user-service 返回的 token 格式：
   - 应该是 UUID 格式（36字符，如：f692752c-51c6-4c9a-bded-654bc3f2015b）
   - 不应该包含 "Bearer " 前缀

3. 检查请求是否跨域：
   - 如果是 H5 开发模式，确保代理配置正确
   - 检查是否有 CORS 拦截

## 注意事项

1. **必须重启 health-service** 才能使配置生效
2. 修复后首次登录时会生成新的 token，旧 token 会失效
3. 建议用户重新登录一次以确保使用新配置生成的 token

## 相关文件变更

### 后端
- `backend/family-service/health-service/src/main/resources/application.yml`
- `backend/family-service/health-service/src/main/java/com/family/health/controller/WaterController.java`

### 前端
- `frontend/src/utils/request.js`
- `frontend/src/pages/home/index.vue`
