# 喝水打卡功能修复说明

## 问题原因

1. **health-service 服务未启动** - 端口 8089 被占用，服务启动失败
2. **时间格式不匹配** - 前端发送 ISO 格式时间 (2024-01-01T12:00:00.000Z)，后端期望 HH:mm:ss 格式
3. **数据库表可能不存在** - water_record 表需要创建

## 修复内容

### 1. 修改服务端口 (已完成)
- health-service: 8089 → 8096
- gateway 路由同步更新

### 2. 修复前端时间格式 (已完成)
修改文件: `/Volumes/document/Projects/family-app/frontend/src/pages/water/index.vue`
- 将 `recordTime: new Date().toISOString()` 改为 `recordTime: "HH:mm:ss"` 格式

### 3. 添加 Jackson 配置 (已完成)
新增文件: `/Volumes/document/Projects/family-app/backend/family-service/health-service/src/main/java/com/family/health/config/JacksonConfig.java`
- 确保 LocalTime 类型正确序列化和反序列化

### 4. 创建数据库表 SQL (已完成)
文件: `/Volumes/document/Projects/family-app/backend/family-service/health-service/src/main/resources/schema.sql`

```sql
CREATE TABLE IF NOT EXISTS water_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    amount INT NOT NULL COMMENT '喝水量(ml)',
    record_date DATE NOT NULL COMMENT '记录日期',
    record_time TIME COMMENT '记录时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_date (user_id, record_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='喝水记录表';
```

## 需要执行的操作

### 步骤 1: 停止占用 8089 端口的进程
```bash
lsof -i :8089
kill -9 <PID>
```

### 步骤 2: 创建数据库表
在 MySQL 中执行：
```sql
USE family_app;

CREATE TABLE IF NOT EXISTS water_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    amount INT NOT NULL COMMENT '喝水量(ml)',
    record_date DATE NOT NULL COMMENT '记录日期',
    record_time TIME COMMENT '记录时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_date (user_id, record_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='喝水记录表';
```

### 步骤 3: 重新编译并启动服务
```bash
cd /Volumes/document/Projects/family-app/backend

# 编译 health-service
cd family-service/health-service
mvn clean compile

# 启动 health-service (端口 8096)
mvn spring-boot:run
```

### 步骤 4: 重启 Gateway (更新路由配置)
```bash
cd /Volumes/document/Projects/family-app/backend/family-gateway
mvn spring-boot:run
```

### 步骤 5: 重新构建前端
```bash
cd /Volumes/document/Projects/family-app/frontend
npm run build
# 或启动开发服务器
npm run dev
```

## 验证

1. 检查 health-service 是否运行在 8096 端口
2. 检查 gateway 日志是否正常转发 `/api/health/**` 请求到 8096
3. 测试喝水打卡功能

## 注意事项

- 确保用户已登录（有有效的 token）
- 确保数据库连接正常
- 确保 Redis 服务运行正常（用于 Sa-Token 会话管理）
