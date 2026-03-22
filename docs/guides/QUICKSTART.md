# 🚀 快速启动指南

## 📦 项目已完成

- ✅ **88个文件** - 完整项目代码
- ✅ **55个Java文件** - 8个微服务后端
- ✅ **11个Vue页面** - 完整前端界面
- ✅ **11个Dockerfile** - 容器化配置
- ✅ **17张数据库表** - 完整数据设计

---

## 🚀 部署方式

### 方式一：Docker部署（推荐）

```bash
# 进入项目目录
cd /Users/qi/.openclaw/workspace/family-app

# 运行一键部署脚本
./deploy.sh
```

部署完成后访问：
- **H5页面**: http://localhost
- **API文档**: http://localhost/api
- **MinIO**: http://localhost:9001

### 方式二：手动部署

#### 1. 启动基础设施
```bash
cd docker
docker-compose up -d mysql redis minio
```

#### 2. 初始化数据库
```bash
sleep 30
docker exec -i family-mysql mysql -uroot -pFamily@2025 < ../database/schema.sql
```

#### 3. 启动后端（需要Maven和JDK17）
```bash
cd backend/family-service/user-service
mvn clean package -DskipTests
java -jar target/user-service-1.0.0.jar
```

#### 4. 启动前端（需要Node.js）
```bash
cd frontend
npm install
npm run dev:h5
```

---

## 📱 功能预览

| 功能 | 访问路径 |
|------|---------|
| 登录页面 | /pages/login/index |
| 首页概览 | /pages/home/index |
| 任务清单 | /pages/task/index |
| 心愿墙 | /pages/wish/index |
| 智能菜谱 | /pages/recipe/index |
| 食材识别 | /pages/food/index |
| 日期记录 | /pages/calendar/index |
| 家庭投票 | /pages/vote/index |
| 家庭空间 | /pages/family/index |
| 个人中心 | /pages/profile/index |

---

## 🔧 配置说明

### 数据库配置
- **Host**: localhost:3306
- **Database**: family_app
- **User**: root
- **Password**: Family@2025

### Redis配置
- **Host**: localhost:6379
- **No Password**

### MinIO配置
- **Endpoint**: localhost:9000
- **Access Key**: minioadmin
- **Secret Key**: minioadmin123

---

## 📊 服务列表

| 服务名 | 端口 | 说明 |
|--------|------|------|
| nginx | 80/443 | 前端和反向代理 |
| gateway | 8080 | API网关 |
| user-service | - | 用户服务 |
| family-service | - | 家庭服务 |
| task-service | - | 任务服务 |
| wish-service | - | 心愿服务 |
| recipe-service | - | 菜谱服务 |
| food-service | - | 食材服务 |
| health-service | - | 健康服务 |
| calendar-service | - | 日历服务 |
| vote-service | - | 投票服务 |
| mysql | 3306 | 数据库 |
| redis | 6379 | 缓存 |
| minio | 9000/9001 | 对象存储 |
| rabbitmq | 5672/15672 | 消息队列 |
| elasticsearch | 9200 | 搜索引擎 |

---

## 🛠️ 常用命令

```bash
# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f [service-name]

# 重启服务
docker-compose restart

# 停止所有服务
docker-compose down

# 完全重置（删除数据）
docker-compose down -v
```

---

## 📞 技术支持

如遇到问题，请查看 `DEPLOY.md` 详细部署文档。
