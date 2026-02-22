# 家庭小程序 - Docker 部署方案

## 部署架构

```
┌─────────────────────────────────────────────────────────┐
│                      Docker Network                      │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────┐  │
│  │   Nginx     │  │   MySQL     │  │     Redis       │  │
│  │   (前端)    │  │   (数据库)   │  │    (缓存)       │  │
│  └──────┬──────┘  └─────────────┘  └─────────────────┘  │
│         │                                                │
│  ┌──────┴──────────────────────────────────────────┐   │
│  │              后端微服务 (多个容器)                │   │
│  │  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌────────┐ │   │
│  │  │  Gateway│ │  User   │ │ Family  │ │ Task   │ │   │
│  │  │  Service│ │ Service │ │ Service │ │Service │ │   │
│  │  └─────────┘ └─────────┘ └─────────┘ └────────┘ │   │
│  └──────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
```

## 快速开始

### 1. 环境准备

确保已安装：
- Docker 20.10+
- Docker Compose 2.0+

### 2. 克隆代码

```bash
git clone https://github.com/Qioooba/family-app.git
cd family-app
```

### 3. 启动服务

```bash
# 构建并启动所有服务
docker-compose up -d --build

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down
```

### 4. 访问服务

- 前端页面: http://localhost
- 后端API: http://localhost:8080
- API文档: http://localhost:8080/swagger-ui.html

## 服务说明

| 服务 | 端口 | 说明 |
|------|------|------|
| nginx | 80 | 前端静态页面 |
| gateway | 8080 | API网关 |
| user-service | 8081 | 用户服务 |
| family-service | 8082 | 家庭服务 |
| task-service | 8083 | 任务服务 |
| mysql | 3306 | 数据库 |
| redis | 6379 | 缓存 |

## 环境变量配置

复制 `.env.example` 为 `.env` 并修改：

```bash
cp .env.example .env
# 编辑 .env 文件
```

### 关键配置项

```env
# 数据库
MYSQL_ROOT_PASSWORD=your_mysql_password
MYSQL_DATABASE=family_app

# Redis
REDIS_PASSWORD=your_redis_password

# JWT密钥
JWT_SECRET=your_jwt_secret_key

# 文件存储
MINIO_ACCESS_KEY=your_minio_access_key
MINIO_SECRET_KEY=your_minio_secret_key
```

## 生产环境部署

### 1. 修改配置

编辑 `docker-compose.prod.yml`：
- 修改端口映射
- 配置域名SSL证书
- 设置资源限制

### 2. 构建生产镜像

```bash
docker-compose -f docker-compose.prod.yml build
docker-compose -f docker-compose.prod.yml up -d
```

### 3. 数据库初始化

首次部署需要初始化数据库：

```bash
# 进入MySQL容器
docker exec -it family-app-mysql mysql -uroot -p

# 执行初始化脚本（已自动执行）
source /docker-entrypoint-initdb.d/init.sql
```

## 监控与日志

### 查看服务状态

```bash
# 所有容器状态
docker-compose ps

# 资源使用情况
docker stats
```

### 查看日志

```bash
# 所有服务日志
docker-compose logs

# 特定服务日志
docker-compose logs -f gateway-service

# 最近100行日志
docker-compose logs --tail=100
```

## 备份与恢复

### 数据库备份

```bash
# 备份MySQL
docker exec family-app-mysql mysqldump -uroot -p family_app > backup.sql

# 备份Redis
docker exec family-app-redis redis-cli SAVE
docker cp family-app-redis:/data/dump.rdb ./redis-backup.rdb
```

### 数据恢复

```bash
# 恢复MySQL
docker exec -i family-app-mysql mysql -uroot -p family_app < backup.sql

# 恢复Redis
docker cp redis-backup.rdb family-app-redis:/data/dump.rdb
docker restart family-app-redis
```

## 常见问题

### Q1: 端口被占用

修改 `docker-compose.yml` 中的端口映射：
```yaml
ports:
  - "8080:8080"  # 改为 "8081:8080" 或其他可用端口
```

### Q2: 内存不足

在 `docker-compose.yml` 中添加资源限制：
```yaml
deploy:
  resources:
    limits:
      memory: 512M
    reservations:
      memory: 256M
```

### Q3: 容器启动失败

检查日志：
```bash
docker-compose logs service-name
```

### Q4: 数据持久化

数据默认挂载到 Docker volumes：
- MySQL: `family-app-mysql-data`
- Redis: `family-app-redis-data`
- MinIO: `family-app-minio-data`

## 更新部署

### 更新代码后重新部署

```bash
# 拉取最新代码
git pull origin main

# 重新构建并启动
docker-compose down
docker-compose up -d --build
```

### 滚动更新（不停机）

```bash
# 逐个更新服务
docker-compose up -d --no-deps --build gateway-service
docker-compose up -d --no-deps --build user-service
# ... 其他服务
```

## 安全建议

1. **修改默认密码** - 所有服务的默认密码必须修改
2. **使用HTTPS** - 生产环境必须配置SSL证书
3. **限制端口暴露** - 仅暴露必要的端口
4. **定期更新镜像** - 及时更新基础镜像修复安全漏洞
5. **启用防火墙** - 配置服务器防火墙规则

## 技术支持

如有问题，请查看：
- 项目文档: `/docs`
- 问题反馈: GitHub Issues
- 更新日志: CHANGELOG.md
