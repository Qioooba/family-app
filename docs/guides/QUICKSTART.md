# 🚀 快速启动指南

## 当前项目形态

- 后端：`backend/family-service`
- 前端：`frontend`
- 数据库脚本：`database`
- 运维模板：`ops`
- 部署脚本：`scripts`

本仓库当前不是多微服务 + Docker 编排结构，以下步骤以当前主链路为准。

## 本地开发

### 1. 准备环境变量

```bash
cp .env.example .env
```

如需上传体验版，还需要准备：

- `frontend/private.key`

### 2. 启动数据库

可使用本地 MySQL，或使用你自己的 Docker MySQL。

数据库名默认：

- `family_app`

### 3. 启动后端

```bash
cd backend/family-service
mvn clean package -DskipTests
java -jar target/family-service-1.0.0.jar
```

默认接口地址：

- `http://localhost:8443`

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev:h5
```

### 5. 构建微信小程序

```bash
cd frontend
npm run build:mp-weixin
```

构建产物目录：

- `frontend/dist/build/mp-weixin`

## 远程部署后端

```bash
mvn -pl backend/family-service -am clean package
REMOTE_PASSWORD='你的SSH密码' \
REMOTE_MYSQL_PASSWORD='你的MySQL密码' \
LOCAL_ENV_FILE='/path/to/family-app.env' \
bash scripts/deploy_remote_backend.sh
```

相关文件：

- `ops/family-app.env.example`
- `ops/family-app.service`
- `database/remote_sync_schema.sql`

## 上传体验版

```bash
cd frontend
npm run build:mp-weixin
WEIXIN_APPID=你的小程序AppID \
MINIPROGRAM_VERSION=版本号 \
MINIPROGRAM_DESC='更新说明' \
npm run upload:mp-weixin
```

默认读取私钥位置：

- `frontend/private.key`
