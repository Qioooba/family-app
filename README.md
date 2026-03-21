# 家庭小程序 (Family App)

家庭内部使用的智能家庭管理平台

![Status](https://img.shields.io/badge/状态-开发中-blue)
![Version](https://img.shields.io/badge/版本-1.0.0-blue)
![License](https://img.shields.io/badge/协议-MIT-yellow)

## 📱 功能模块

### ✅ 保留的功能模块

| 序号 | 功能模块 | 说明 | 状态 |
|------|---------|------|------|
| 1 | 📋 **任务管理** | 任务CRUD、子任务、排班表、任务统计、任务标签 | ✅ 完整 |
| 2 | 📅 **纪念日** | 纪念日管理、农历转换、倒计时 | ✅ 完整 |
| 3 | 🌤️ **天气** | 天气预报、气温、湿度 | ✅ 完整 |
| 4 | 🏠 **家庭管理** | 家庭创建、成员管理、邀请码 | ✅ 完整 |
| 5 | 👤 **用户系统** | 登录/注册、Sa-Token鉴权 | ✅ 完整 |

### 🧪 仓库内保留但非默认启用的模块

| 序号 | 功能模块 | 删除原因 |
|------|---------|---------|
| 1 | 🤖 **AI 助手** | 已不作为当前主流程功能 |
| 2 | 📷 **家庭相册** | 代码仍在仓库中，默认不作为主功能维护 |
| 3 | 💰 **家庭账本** | 代码仍在仓库中，默认不作为主功能维护 |
| 4 | 📱 **微信手机号登录** | 当前主流程以 openid 登录为主 |

> **说明**: ✅ 完整 - 功能已实现并可用

## 🔐 登录方式

- **用户名+密码登录** - 传统账号密码方式
- **微信一键登录** - 仅获取 openid，不获取手机号
- **注册需邀请码** - 新用户需要邀请码才能注册

## 🏗️ 技术架构

### 后端
| 技术 | 版本 | 说明 |
|------|------|------|
| **Java** | 17 | 编程语言 |
| **Spring Boot** | 3.1.5 | 基础框架 |
| **Spring Cloud** | 2022.0.4 | 微服务框架 |
| **MyBatis Plus** | 3.5.5 | ORM框架 |
| **MySQL** | 8.0 | 关系型数据库 |
| **Redis** | 7.0 | 缓存数据库 |
| **Sa-Token** | 1.37.0 | 权限认证框架 |
| **SpringDoc** | 2.2.0 | API文档 |
| **WebSocket** | - | 实时通信 |
| **Hutool** | 5.8.23 | 工具库 |
| **Lombok** | 1.18.40 | 代码简化 |

### 前端
| 技术 | 版本 | 说明 |
|------|------|------|
| **Vue** | 3.4.21 | 前端框架 |
| **TypeScript** | 5.4.5 | 类型支持 |
| **UniApp** | 3.0.0-alpha | 跨平台框架 |
| **Pinia** | 2.0.36 | 状态管理 |
| **uView Plus** | 3.2.24 | UI组件库 |
| **ECharts** | 5.5.0 | 图表库 |
| **Dayjs** | 1.11.10 | 日期处理 |

### 部署
- **Docker** 20.10+
- **Docker Compose** 2.0+

## 🚀 快速开始

### 环境要求
- Docker 20.10+
- Docker Compose 2.0+

### 部署步骤

```bash
# 1. 克隆项目
git clone https://github.com/Qioooba/family-app.git
cd family-app

# 2. 配置环境变量
cp .env.example .env

# 3. 启动后端
cd backend
mvn spring-boot:run

# 4. 启动前端
cd ../frontend
npm install
npm run dev:h5

# 5. 构建产物
npm run build:h5
```

### 访问服务

| 服务 | 地址 |
|------|------|
| 前端页面 | http://localhost:3000 |
| 后端API | http://localhost:8443 |
| API文档 | http://localhost:8443/swagger-ui.html |

### 远程部署

远程环境建议把基础设施参数放到 `/etc/family-app/family-app.env`，业务侧第三方参数继续放 `sys_config`。

```bash
# 1. 本地构建后端
mvn -pl backend/family-service -am clean package

# 2. 准备环境变量文件
cp ops/family-app.env.example /path/to/family-app.env

# 3. 执行远程同步
REMOTE_PASSWORD='你的SSH密码' \
REMOTE_MYSQL_PASSWORD='你的MySQL密码' \
LOCAL_ENV_FILE='/path/to/family-app.env' \
bash scripts/deploy_remote_backend.sh
```

脚本会自动完成：
- 上传最新 JAR
- 执行 [database/remote_sync_schema.sql](/Volumes/document/Projects/family-app/database/remote_sync_schema.sql)
- 安装 [ops/family-app.service](/Volumes/document/Projects/family-app/ops/family-app.service)
- 重启 `family-app.service`

## 📂 项目结构

```
family-app/
├── backend/                      # 后端代码
│   ├── family-common/            # 公共模块
│   │   └── common-core/          # 核心工具类
│   ├── family-service/           # 业务服务模块
│   │   └── src/main/java/com/family/
│   │       ├── family/           # 家庭模块
│   │       ├── user/             # 用户模块
│   │       ├── task/             # 任务模块
│   │       ├── calendar/         # 日历/纪念日模块
│   │       └── weather/          # 天气模块
│   └── pom.xml
├── frontend/                     # 前端代码
│   ├── src/
│   │   ├── pages/                # 页面目录
│   │   │   ├── task/             # 任务相关页面
│   │   │   ├── family/           # 家庭相关页面
│   │   │   ├── anniversary/      # 纪念日页面
│   │   │   ├── weather/          # 天气页面
│   │   │   └── ...               # 其他模块
│   │   ├── components/           # 公共组件
│   │   └── utils/                # 工具函数
│   └── package.json
├── docker/                       # Docker配置
│   ├── docker-compose.yml
│   ├── Dockerfile.backend
│   └── Dockerfile.frontend
└── README.md
```

## 🔌 API 模块列表

### 核心模块
| 模块 | 基础路径 | 主要功能 |
|------|---------|---------|
| 用户模块 | `/api/user` | 登录、注册、用户信息 |
| 家庭模块 | `/api/family` | 家庭CRUD、成员管理、邀请码、切换家庭 |
| 任务模块 | `/api/task` | 任务CRUD、完成、统计、归档、提醒 |
| 子任务 | `/api/task/subtask` | 子任务增删改查、状态切换 |
| 排班模块 | `/api/schedule` | 排班CRUD、交换排班 |
| 天气模块 | `/api/weather` | 天气查询、预报 |

### 日期模块
| 模块 | 基础路径 | 主要功能 |
|------|---------|---------|
| 纪念日 | `/api/calendar/anniversary` | 纪念日CRUD、农历转换 |

## 📝 相关文档

- [Docker部署指南](./docker/DEPLOY.md)
- [PROJECT.md](./PROJECT.md) - 项目详细说明
- [FUNCTIONS.md](./FUNCTIONS.md) - 功能清单

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！

## 📄 开源协议

本项目基于 [MIT](./LICENSE) 协议开源。

---

**🌟 如果这个项目对你有帮助，请给它一个 Star！**

*Made with ❤️ by Qioooba*
