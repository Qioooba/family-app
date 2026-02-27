# 家庭小程序 (family-app)

> 一站式智能家庭管理平台，帮助家庭成员高效协作、共同管理家庭事务。

---

## 项目概述

家庭小程序是一款专为家庭设计的综合管理应用，提供任务管理、智能菜谱、纪念日提醒、家庭投票等14大功能模块，让家庭成员之间的沟通和协作更加便捷高效。

### 核心功能

| 功能模块 | 说明 |
|---------|------|
| 📋 **备忘清单** | 任务管理、子任务、排班表 |
| 💝 **心愿墙** | 心愿管理、预算规划、里程碑追踪 |
| 🍳 **智能菜谱** | AI推荐菜谱、投票点菜 |
| 📸 **食材识别** | 拍照识别食材、库存管理 |
| 📅 **日期记录** | 纪念日管理、农历转换、电子贺卡 |
| 🗳️ **家庭投票** | 投票创建、实时统计 |
| 🏠 **家庭空间** | 家庭相册、账本、文件共享 |
| 🤖 **AI助手** | 智能对话、营养师、日报生成 |
| 🛒 **智能购物** | 扫码购物、价格追踪 |
| 📊 **数据看板** | 数据报表、可视化图表 |
| 💬 **家庭圈** | 家庭动态、评论互动 |
| 🎮 **家庭游戏** | 积分转盘、猜谜语、知识问答 |
| 🌙 **深色模式** | 主题切换、护眼模式 |
| 📤 **数据导出** | Excel/PDF导出 |

---

## 技术栈

### 后端

| 技术 | 版本 | 用途 |
|-----|------|------|
| **Spring Boot** | 3.2+ | 核心框架 |
| **Spring Cloud Alibaba** | 最新版 | 微服务架构 |
| **MyBatis-Plus** | 3.5+ | ORM框架 |
| **MySQL** | 8.0 | 关系型数据库 |
| **Redis** | 7.0 | 缓存和会话存储 |
| **RabbitMQ** | 3.x | 消息队列 |
| **MinIO** | 最新版 | 对象存储 |
| **JWT** | 0.11+ | 身份认证 |

### 前端

| 技术 | 版本 | 用途 |
|-----|------|------|
| **UniApp** | 3.0+ | 跨端开发框架 |
| **Vue 3** | 3.3+ | 前端框架 |
| **TypeScript** | 5.x | 类型安全 |
| **Pinia** | 2.x | 状态管理 |
| **uView Plus** | 3.x | UI组件库 |
| **ECharts** | 5.x | 数据可视化 |

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
│       │   ├── wish/                # 心愿墙
│       │   ├── recipe/              # 智能菜谱
│       │   ├── food/                # 食材管理
│       │   ├── vote/                # 家庭投票
│       │   ├── calendar/            # 日期记录
│       │   └── game/                # 家庭游戏
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
├── database/                         # 数据库脚本
│   ├── schema.sql                   # 完整数据库结构
│   └── init/                        # 初始化数据
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
./deploy.sh

# 3. 等待服务启动（约30秒）
docker-compose ps
```

**访问地址：**
- 前端页面：http://localhost
- 后端API：http://localhost:8080
- API文档：http://localhost:8080/swagger-ui.html
- MinIO控制台：http://localhost:9001

### 方式二：本地开发环境

#### 启动后端

```bash
# 1. 启动基础设施
docker-compose up -d mysql redis minio rabbitmq

# 2. 初始化数据库（等待MySQL完全启动）
sleep 30
docker exec -i family-mysql mysql -uroot -pFamily@2025 < database/schema.sql

# 3. 启动后端服务（需要JDK 17+ 和 Maven 3.8+）
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

# 4. 或构建微信小程序
npm run build:mp-weixin
```

### 环境配置

创建 `.env` 文件（参考 `.env.example`）：

```env
# 数据库配置
MYSQL_HOST=localhost
MYSQL_PORT=3306
MYSQL_DB=family_app
MYSQL_USER=root
MYSQL_PASSWORD=Family@2025

# Redis配置
REDIS_HOST=localhost
REDIS_PORT=6379

# MinIO配置
MINIO_ENDPOINT=http://localhost:9000
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin123

# JWT配置
JWT_SECRET=your-jwt-secret-key-here
JWT_EXPIRATION=86400000
```

---

## 接口文档概述

### API 设计规范

- **基础路径**: `/api/{module}/{resource}`
- **响应格式**: JSON (code, message, data)
- **认证方式**: JWT Token (Header: `Authorization: Bearer {token}`)
- **分页参数**: `pageNum`, `pageSize`

### 主要接口模块

| 模块 | 基础路径 | 说明 |
|-----|---------|------|
| 用户模块 | `/api/user` | 注册、登录、用户信息 |
| 家庭模块 | `/api/family` | 家庭创建、成员管理 |
| 任务模块 | `/api/task` | 任务CRUD、子任务、排班 |
| 心愿模块 | `/api/wish` | 心愿墙、里程碑 |
| 菜谱模块 | `/api/recipe` | 菜谱管理、AI推荐 |
| 食材模块 | `/api/food/ingredient` | 食材识别、库存管理 |
| 投票模块 | `/api/vote` | 投票创建、投票记录 |
| 日历模块 | `/api/calendar` | 纪念日、农历 |
| 游戏模块 | `/api/game` | 猜谜、问答、挑战 |

### 在线文档

启动服务后访问：http://localhost:8080/swagger-ui.html

---

## 数据库说明

### 数据库配置

- **数据库**: MySQL 8.0
- **默认库名**: `family_app`
- **字符集**: `utf8mb4_unicode_ci`

### 核心数据表

| 表名 | 说明 |
|-----|------|
| `sys_user` | 用户表 |
| `sys_family` | 家庭表 |
| `sys_family_member` | 家庭成员关联表 |
| `task` | 任务表 |
| `task_sub` | 子任务表 |
| `wish` | 心愿表 |
| `wish_milestone` | 心愿里程碑表 |
| `recipe` | 菜谱表 |
| `recipe_ingredient` | 菜谱食材关联表 |
| `ingredient` | 食材表 |
| `vote` | 投票表 |
| `vote_record` | 投票记录表 |
| `anniversary` | 纪念日表 |
| `game_riddle` | 谜语表 |
| `game_quiz` | 问答表 |
| `game_challenge` | 挑战表 |

### 数据库脚本位置

```
database/
├── schema.sql              # 完整数据库结构
├── init/                   # 初始化数据脚本
└── migration/              # 数据库迁移脚本
    └── V2__add_base_entity_columns.sql
```

### 修复脚本

```
backend/family-service/src/main/resources/db/
├── fix_columns.sql         # 列修复脚本
├── fix_database.sql        # 数据库修复脚本
└── migration/              # Flyway迁移脚本
```

---

## 最近修复记录

### 2026-02-28

#### 修复内容

1. **食材API路径修复**
   - 修复路径: `/api/ingredient` → `/api/food/ingredient`
   - 统一食材模块API命名空间

2. **游戏API修复**
   - 修复猜谜语接口 (riddle)
   - 修复知识问答接口 (quiz)
   - 修复挑战任务接口 (challenge)

3. **数据库修复脚本**
   - 添加 `fix_columns.sql` - 修复缺失字段
   - 添加 `fix_database.sql` - 数据库完整性检查
   - 添加 Flyway 迁移脚本 `V2__add_base_entity_columns.sql`

4. **安全漏洞修复**
   - 修复 FamilyController 权限检查
   - 修复 TaskController 权限验证
   - 添加统一的权限校验拦截器

5. **实体字段修复**
   - 修复 Ingredient 实体缺失字段
   - 添加 BaseEntity 通用字段 (createTime, updateTime, createBy, updateBy)

#### 提交记录

```
commit af988c5
fix: 修复数据库表结构和接口问题

- 修复食材 API 路径 (/api/food/ingredient)
- 修复游戏 API (riddle/quiz/challenge)
- 添加数据库修复脚本
- 修复安全漏洞 (权限验证)
- 修复 FamilyController 权限检查
- 修复 TaskController 权限验证
- 修复 Ingredient 实体字段
```

---

## 项目统计

| 统计项 | 数值 |
|--------|------|
| 开发时长 | 约2天 |
| Git提交 | 100+次 |
| 代码行数 | 前端~68K + 后端~80K |
| 数据库表 | 17+张 |
| API接口 | 150+个 |
| 前端页面 | 50+个 |

---

## 相关文档

- [README.md](./README.md) - 项目简介
- [QUICKSTART.md](./QUICKSTART.md) - 快速启动指南
- [CONTRIBUTING.md](./CONTRIBUTING.md) - 贡献指南
- [LICENSE](./LICENSE) - 开源协议

---

## 许可证

本项目基于 [MIT](./LICENSE) 协议开源。

---

**🌟 如果这个项目对你有帮助，请给它一个 Star！**

*Made with ❤️ by Qioooba*
