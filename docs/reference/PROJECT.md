# 家庭小程序 (Family App)

> 家庭内部使用的智能家庭管理平台

---

## 项目概述

家庭小程序是一款专为家庭设计的综合管理应用，提供任务管理、纪念日提醒、天气查询、家庭管理等核心功能，让家庭成员之间的沟通和协作更加便捷高效。

### 核心功能

| 功能模块 | 说明 |
|---------|------|
| 📋 **任务管理** | 任务管理、子任务、排班表、任务统计 |
| 📅 **纪念日** | 纪念日管理、农历转换、电子贺卡 |
| 🌤️ **天气** | 天气预报、气温、湿度、生活指数 |
| 🏠 **家庭管理** | 家庭创建、成员管理、邀请码 |
| 👤 **用户系统** | 登录/注册、微信一键登录 |

---

## 技术栈

### 后端

| 技术 | 版本 | 用途 |
|-----|------|------|
| **Spring Boot** | 3.1.5 | 核心框架 |
| **Spring Cloud** | 2022.0.4 | 微服务框架 |
| **MyBatis-Plus** | 3.5.5 | ORM框架 |
| **MySQL** | 8.0 | 关系型数据库 |
| **Redis** | 7.0 | 缓存和会话存储 |
| **Sa-Token** | 1.37.0 | 身份认证 |

### 前端

| 技术 | 版本 | 用途 |
|-----|------|------|
| **UniApp** | 3.0+ | 跨端开发框架 |
| **Vue 3** | 3.4.21 | 前端框架 |
| **TypeScript** | 5.4.5 | 类型安全 |
| **Pinia** | 2.0.36 | 状态管理 |
| **uView Plus** | 3.2.24 | UI组件库 |
| **ECharts** | 5.5.0 | 数据可视化 |

### 基础设施

| 技术 | 版本 | 用途 |
|-----|------|------|
| **Docker** | 20.10+ | 容器化 |
| **Docker Compose** | 2.0+ | 容器编排 |
| **Nginx** | 1.24+ | 反向代理 |

---

## 项目结构

```
family-app/
├── backend/                          # 后端代码
│   └── family-service/              # 家庭服务模块
│       ├── src/main/java/com/family/
│       │   ├── family/              # 家庭管理
│       │   ├── user/                # 用户服务
│       │   ├── task/                # 任务清单
│       │   ├── calendar/            # 纪念日
│       │   └── weather/             # 天气
│       ├── src/main/resources/
│       │   ├── db/                  # 数据库脚本
│       │   └── application.yml
│       └── pom.xml
├── frontend/                         # 前端代码
│   ├── src/
│   │   ├── api/                     # API接口定义
│   │   ├── components/              # 公共组件
│   │   ├── pages/                   # 页面文件
│   │   ├── static/                  # 静态资源
│   │   ├── stores/                  # Pinia状态管理
│   │   └── utils/                   # 工具函数
│   ├── package.json
│   └── vite.config.js
├── docker/                           # Docker配置
│   ├── docker-compose.yml
│   ├── Dockerfile.backend
│   └── Dockerfile.frontend
└── README.md
```

---

## 启动指南

### 方式一：Docker 一键部署（推荐）

```bash
# 1. 克隆项目
git clone https://github.com/Qioooba/family-app.git
cd family-app

# 2. 运行部署脚本
cd docker
cp .env.example .env
docker-compose up -d

# 3. 等待服务启动
docker-compose ps
```

**访问地址：**
- 前端页面：http://localhost
- 后端API：http://localhost:8080
- API文档：http://localhost:8080/swagger-ui.html

### 方式二：本地开发环境

#### 启动后端

```bash
# 1. 启动基础设施
docker-compose up -d mysql redis

# 2. 启动后端服务（需要JDK 17+ 和 Maven）
cd backend/family-service
mvn clean package -DskipTests
java -jar target/family-service-1.0.0.jar
```

#### 启动前端

```bash
# 1. 进入前端目录
cd frontend

# 2. 安装依赖
npm install

# 3. 启动H5开发服务器
npm run dev:h5
```

---

## 接口文档概述

### API 设计规范

- **基础路径**: `/api/{module}/{resource}`
- **响应格式**: JSON (code, message, data)
- **认证方式**: Sa-Token Token (Header: `satoken: {token}`)
- **分页参数**: `pageNum`, `pageSize`

### 主要接口模块

| 模块 | 基础路径 | 说明 |
|-----|---------|------|
| 用户模块 | `/api/user` | 注册、登录、用户信息 |
| 家庭模块 | `/api/family` | 家庭创建、成员管理 |
| 任务模块 | `/api/task` | 任务CRUD、子任务、排班 |
| 纪念日模块 | `/api/calendar/anniversary` | 纪念日管理、农历 |
| 天气模块 | `/api/weather` | 天气查询 |

### 在线文档

启动服务后访问：http://localhost:8080/swagger-ui.html

---

## 核心数据表

| 表名 | 说明 |
|-----|------|
| `sys_user` | 用户表 |
| `sys_family` | 家庭表 |
| `sys_family_member` | 家庭成员关联表 |
| `task` | 任务表 |
| `task_sub` | 子任务表 |
| `anniversary` | 纪念日表 |

---

## 许可证

本项目基于 [MIT](./LICENSE) 协议开源。

---

**🌟 如果这个项目对你有帮助，请给它一个 Star！**

*Made with ❤️ by Qioooba*
