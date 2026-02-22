#!/bin/bash

# 家庭小程序本地部署脚本
# 使用本地 Maven 启动，不使用 Docker

echo "🚀 开始本地部署..."

# 1. 启动 MySQL 和 Redis（Docker）
echo "📦 启动数据库服务..."
cd docker
docker-compose up -d mysql redis
cd ..

# 2. 等待数据库启动
echo "⏳ 等待数据库启动（10秒）..."
sleep 10

# 3. 初始化数据库
echo "🗄️  初始化数据库..."
mysql -h127.0.0.1 -P3306 -uroot -proot123 < database/schema.sql 2>/dev/null || echo "数据库已存在或密码错误"

# 4. 编译并启动后端
echo "🔨 编译后端服务..."
cd backend
mvn clean package -DskipTests -q

# 5. 启动 Gateway
echo "🌐 启动 API 网关..."
nohup java -jar -Xmx512m -Dspring.profiles.active=local family-service/gateway/target/gateway-1.0.0.jar > ../logs/gateway.log 2>&1 &

echo "✅ 后端服务已启动！"
echo "📊 API 地址: http://localhost:8080"
echo "📖 API 文档: http://localhost:8080/swagger-ui.html"

# 6. 启动前端（开发模式）
echo "🎨 启动前端..."
cd ../frontend
npm run dev:h5 &

echo "🎉 部署完成！"
echo "🌐 前端地址: http://localhost:5173"
