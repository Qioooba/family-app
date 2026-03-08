# 环境配置说明

## 本地开发配置

项目使用 `.env` 文件存储敏感信息，该文件已添加到 `.gitignore`，不会提交到 Git。

### 1. 创建本地配置文件

在项目根目录创建 `.env` 文件：

```bash
cp .env.example .env
```

然后编辑 `.env` 文件，填入真实的密钥：

```bash
# 微信小程序配置
WEIXIN_APPID=你的小程序AppID
WEIXIN_APPSECRET=你的小程序AppSecret

# 腾讯地图 API Key
TENCENT_MAP_KEY=你的腾讯地图Key

# SSL 证书密码
SSL_KEY_STORE_PASSWORD=你的SSL密码

# MySQL 数据库
MYSQL_ROOT_PASSWORD=你的MySQL密码
```

### 2. 启动后端服务

使用环境变量启动：

```bash
cd backend/family-service
export WEIXIN_APPID=$(grep WEIXIN_APPID ../../.env | cut -d= -f2)
export WEIXIN_APPSECRET=$(grep WEIXIN_APPSECRET ../../.env | cut -d= -f2)
export MYSQL_PASSWORD=$(grep MYSQL_PASSWORD ../../.env | cut -d= -f2)
export SSL_KEY_STORE_PASSWORD=$(grep SSL_KEY_STORE_PASSWORD ../../.env | cut -d= -f2)
mvn spring-boot:run
```

### 3. 构建前端

前端需要在构建时注入环境变量：

```bash
cd frontend
# 在 .env 文件中添加 VITE_TENCENT_MAP_KEY=你的Key
npm run build:mp-weixin
```

## 生产环境配置

生产环境应使用：
1. 服务器环境变量
2. Kubernetes Secrets
3. 阿里云/腾讯云密钥管理服务

不要提交任何真实密钥到代码仓库！

## 已清理的敏感信息

以下信息已从代码中移除，改为从环境变量读取：

- ✅ 微信 AppSecret
- ✅ 腾讯地图 API Key
- ✅ SSL 证书密码
- ✅ MySQL 密码

## 注意事项

1. `.env` 文件绝对不要提交到 Git
2. 定期更换生产环境密钥
3. 为每个环境（开发/测试/生产）使用不同的密钥
