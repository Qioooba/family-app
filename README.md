# 家庭小程序 (Family App)

一站式智能家庭管理平台

![Status](https://img.shields.io/badge/状态-已完成-brightgreen)
![Version](https://img.shields.io/badge/版本-1.0.0-blue)
![License](https://img.shields.io/badge/协议-MIT-yellow)

## 🎉 项目状态

- ✅ **全部14个功能模块已完成**
- ✅ **Docker部署配置已完成**
- ✅ **全部服务部署成功**
- ✅ **2026-02-22 开发完成**

## 📱 功能模块

| 序号 | 功能模块 | 说明 |
|------|---------|------|
| 1 | 📋 备忘清单 | 任务管理、子任务、排班表 |
| 2 | 💝 心愿墙 | 心愿管理、预算、里程碑 |
| 3 | 🍳 智能菜谱 | AI推荐、投票点菜 |
| 4 | 📸 食材识别 | 拍照识别、库存管理 |
| 5 | 📅 日期记录 | 纪念日、农历、贺卡 |
| 6 | 🗳️ 家庭投票 | 投票创建、统计 |
| 7 | 🏠 家庭空间 | 相册、账本、文件共享 |
| 8 | 🤖 AI助手 | 对话、营养师、日报 |
| 9 | 🛒 智能购物 | 扫码、价格追踪 |
| 10 | 📊 数据看板 | 报表、可视化 |
| 11 | 💬 家庭圈 | 动态、评论 |
| 12 | 🎮 家庭游戏 | 转盘、积分 |
| 13 | 🌙 深色模式 | 主题切换 |
| 14 | 📤 数据导出 | Excel/PDF导出 |

## 🏗️ 技术架构

### 后端
- **框架**: Spring Boot 3.2
- **微服务**: Spring Cloud Alibaba
- **数据库**: MySQL 8.0
- **缓存**: Redis 7.0
- **消息队列**: RabbitMQ
- **部署**: Docker + Docker Compose

### 前端
- **框架**: Vue 3 + TypeScript
- **跨平台**: UniApp 3.0
- **状态管理**: Pinia
- **图表**: ECharts
- **组件库**: uView Plus

## 🚀 快速开始

### 环境要求
- Docker 20.10+
- Docker Compose 2.0+

### 部署步骤

```bash
# 1. 克隆项目
git clone https://github.com/Qioooba/family-app.git
cd family-app

# 2. 启动服务
cd docker
cp .env.example .env
docker-compose up -d --build

# 3. 查看状态
docker-compose ps
```

### 访问服务

| 服务 | 地址 |
|------|------|
| 前端页面 | http://localhost |
| 后端API | http://localhost:8080 |
| API文档 | http://localhost:8080/swagger-ui.html |

## 📂 项目结构

```
family-app/
├── backend/              # 后端代码
│   ├── family-service/   # 微服务模块
│   └── pom.xml
├── frontend/             # 前端代码
│   ├── src/
│   └── package.json
├── docker/               # Docker配置
│   ├── docker-compose.yml
│   ├── Dockerfile.backend
│   └── Dockerfile.frontend
├── todo-features/        # 功能设计文档
├── DEVELOPMENT_PLAN.md   # 开发计划
└── README.md
```

## 📊 项目统计

| 统计项 | 数值 |
|--------|------|
| 开发时长 | 约2天 |
| Git提交 | 100+次 |
| 代码行数 | 前端~68K + 后端~80K |
| 数据库表 | 60+张 |
| API接口 | 150+个 |
| 前端页面 | 50+个 |

## 📝 相关文档

- [开发计划](./DEVELOPMENT_PLAN.md)
- [详细进度](./DETAILED_PROGRESS.md)
- [优先级清单](./PRIORITY_TODO.md)
- [Docker部署](./docker/DEPLOY.md)
- [更新日志](./CHANGELOG.md)

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！

## 📄 开源协议

本项目基于 [MIT](./LICENSE) 协议开源。

---

**🌟 如果这个项目对你有帮助，请给它一个 Star！**

*Made with ❤️ by Qioooba*
