#!/bin/bash

echo "🚀 开始完整部署..."

# 1. 确保数据库在运行
echo "📦 检查数据库..."
cd docker
docker-compose ps | grep -q "mysql.*Up" || docker-compose up -d mysql redis
cd ..

# 2. 等待数据库
echo "⏳ 等待数据库启动..."
sleep 5

# 3. 编译后端（如果还没编译）
echo "🔨 编译后端..."
cd backend
if [ ! -f "family-service/user-service/target/user-service-1.0.0.jar" ]; then
    mvn clean package -DskipTests -q
fi

# 4. 启动用户服务
echo "👤 启动用户服务..."
nohup java -jar -Xmx256m family-service/user-service/target/user-service-1.0.0.jar \
    --server.port=8081 \
    --spring.datasource.url=jdbc:mysql://localhost:3306/family_app \
    --spring.datasource.username=root \
    --spring.datasource.password=root123 \
    --spring.redis.host=localhost \
    --spring.redis.port=6379 \
    > ../logs/user-service.log 2>&1 &

echo "✅ 用户服务启动在 http://localhost:8081"

# 5. 启动家庭服务
echo "🏠 启动家庭服务..."
nohup java -jar -Xmx256m family-service/family-service/target/family-service-1.0.0.jar \
    --server.port=8082 \
    --spring.datasource.url=jdbc:mysql://localhost:3306/family_app \
    --spring.datasource.username=root \
    --spring.datasource.password=root123 \
    --spring.redis.host=localhost \
    --spring.redis.port=6379 \
    > ../logs/family-service.log 2>&1 &

echo "✅ 家庭服务启动在 http://localhost:8082"

# 6. 启动任务服务
echo "📋 启动任务服务..."
nohup java -jar -Xmx256m family-service/task-service/target/task-service-1.0.0.jar \
    --server.port=8083 \
    --spring.datasource.url=jdbc:mysql://localhost:3306/family_app \
    --spring.datasource.username=root \
    --spring.datasource.password=root123 \
    --spring.redis.host=localhost \
    --spring.redis.port=6379 \
    > ../logs/task-service.log 2>&1 &

echo "✅ 任务服务启动在 http://localhost:8083"

echo ""
echo "🎉 后端服务已启动！"
echo "📊 服务地址:"
echo "  - 用户服务: http://localhost:8081"
echo "  - 家庭服务: http://localhost:8082"
echo "  - 任务服务: http://localhost:8083"
echo ""
echo "📝 日志位置: logs/"
echo ""

# 7. 启动前端
echo "🎨 启动前端..."
cd ../frontend
npm install --legacy-peer-deps 2>/dev/null
npm run dev:h5 &

echo "🌐 前端地址: http://localhost:5173"
echo ""
echo "✅ 全部部署完成！"
