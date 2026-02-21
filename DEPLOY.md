# 🚀 家庭小程序部署指南

## 📋 前置要求

- Docker 20.10+
- Docker Compose 2.0+
- 服务器内存 4GB+
- 域名（可选，用于HTTPS）

---

## 🚀 一键部署

### 1. 克隆/上传项目到服务器

```bash
# 假设项目上传到 /opt/family-app
cd /opt/family-app
```

### 2. 启动基础设施

```bash
cd docker

# 启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f
```

### 3. 初始化数据库

```bash
# 等待MySQL启动完成（约30秒）
sleep 30

# 执行初始化脚本
docker exec -i family-mysql mysql -uroot -pFamily@2025 < ../database/schema.sql
```

### 4. 构建并启动后端服务

```bash
# 构建用户服务
cd ../backend/family-service/user-service
mvn clean package -DskipTests
docker build -t family/user-service:latest .

# 启动服务（使用docker-compose已配置）
docker-compose up -d user-service
```

### 5. 配置Nginx

```bash
# 编辑nginx配置
cd docker/nginx
# 修改nginx.conf中的域名和SSL证书路径

# 复制前端构建文件到nginx目录
mkdir -p html
cp -r ../../frontend/dist/* html/
```

### 6. 访问系统

- **H5页面**: http://your-server-ip
- **API接口**: http://your-server-ip/api
- **MinIO控制台**: http://your-server-ip:9001

---

## 📁 目录结构说明

```
/opt/family-app/
├── backend/              # 后端代码
├── frontend/             # 前端代码
├── database/             # 数据库脚本
├── docker/               # Docker配置
│   ├── docker-compose.yml
│   ├── nginx/
│   │   ├── nginx.conf
│   │   └── ssl/         # SSL证书
│   └── html/            # 前端静态文件
└── logs/                # 日志文件
```

---

## ⚙️ 配置修改

### 1. 数据库密码

编辑 `docker/docker-compose.yml`:
```yaml
mysql:
  environment:
    MYSQL_ROOT_PASSWORD: 你的密码
```

### 2. 后端数据库连接

编辑 `backend/family-service/user-service/src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/family_app
    username: root
    password: 你的密码
```

### 3. Nginx域名配置

编辑 `docker/nginx/nginx.conf`:
```nginx
server {
    listen 80;
    server_name your-domain.com;  # 修改为你的域名
    
    # ...
}
```

---

## 🔒 HTTPS配置（推荐）

### 方式1：使用Certbot自动获取证书

```bash
# 安装Certbot
docker run -it --rm \
  -v "/opt/family-app/docker/nginx/ssl:/etc/letsencrypt" \
  -v "/opt/family-app/docker/nginx/html:/data/letsencrypt" \
  certbot/certbot certonly \
  --webroot --webroot-path=/data/letsencrypt \
  -d your-domain.com
```

### 方式2：使用自有证书

将证书文件放入 `docker/nginx/ssl/`:
- cert.pem (证书)
- key.pem (私钥)

---

## 📊 常用命令

```bash
# 查看所有服务状态
docker-compose ps

# 查看服务日志
docker-compose logs -f [service-name]

# 重启服务
docker-compose restart [service-name]

# 停止所有服务
docker-compose down

# 停止并删除数据（慎用）
docker-compose down -v

# 进入MySQL容器
docker exec -it family-mysql mysql -uroot -p

# 进入Redis容器
docker exec -it family-redis redis-cli
```

---

## 🔍 故障排查

### 1. MySQL连接失败

```bash
# 检查MySQL日志
docker-compose logs mysql

# 检查端口是否被占用
netstat -tlnp | grep 3306

# 重置MySQL数据
docker-compose down -v
rm -rf /var/lib/docker/volumes/family-app_mysql_data
docker-compose up -d mysql
```

### 2. 后端服务启动失败

```bash
# 查看日志
docker-compose logs user-service

# 检查数据库连接
docker exec -it family-mysql mysql -uroot -p -e "SHOW DATABASES;"
```

### 3. 前端访问不了

```bash
# 检查nginx配置
docker exec -it family-nginx nginx -t

# 重启nginx
docker-compose restart nginx
```

---

## 📈 性能优化

### 1. JVM参数优化

编辑 `docker/docker-compose.yml`:
```yaml
user-service:
  environment:
    - JAVA_OPTS=-Xms512m -Xmx1024m -XX:+UseG1GC
```

### 2. MySQL优化

编辑 `docker/docker-compose.yml`:
```yaml
mysql:
  command: >
    --default-authentication-plugin=mysql_native_password
    --innodb-buffer-pool-size=1G
    --max-connections=200
```

### 3. Nginx优化

已在 `nginx.conf` 中配置Gzip压缩和缓存。

---

## 🔄 更新部署

```bash
# 拉取最新代码
git pull

# 重新构建镜像
docker-compose build

# 重启服务
docker-compose up -d

# 执行数据库迁移（如有）
docker exec -i family-mysql mysql -uroot -pFamily@2025 family_app < database/migration.sql
```

---

## 📞 联系方式

如有问题，请查看日志或联系技术支持。

---

## 📝 版本记录

- v1.0.0 (2025-02-21) - 初始版本发布
  - 用户管理
  - 任务清单
  - 心愿墙
  - 智能菜谱
  - 食材识别
  - 卡路里追踪
  - 日期记录
  - 家庭投票
  - 家庭空间
