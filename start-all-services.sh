#!/bin/bash

echo "🚀 启动所有后端服务..."
cd /Volumes/document/Projects/family-app/backend

# 启动所有服务
services=(
    "wish-service:8084"
    "recipe-service:8085"
    "vote-service:8086"
    "anniversary-service:8087"
    "calendar-service:8088"
    "health-service:8089"
    "food-service:8090"
    "ai-service:8091"
    "message-service:8092"
    "notify-service:8093"
    "file-service:8094"
)

for service in "${services[@]}"; do
    IFS=':' read -r name port <<< "$service"
    echo "🔄 启动 $name (端口 $port)..."
    
    nohup java -jar -Xmx256m family-service/$name/target/$name-1.0.0.jar \
        --server.port=$port \
        --spring.datasource.url=jdbc:mysql://localhost:3306/family_app \
        --spring.datasource.username=root \
        --spring.datasource.password=root123 \
        --spring.redis.host=localhost \
        --spring.redis.port=6379 \
        > ../logs/$name.log 2>&1 &
    
    echo "✅ $name 启动在 http://localhost:$port"
done

echo ""
echo "🎉 所有后端服务已启动！"
echo ""
echo "📊 完整服务列表："
echo "  - 用户服务:    http://localhost:8081"
echo "  - 家庭服务:    http://localhost:8082"
echo "  - 任务服务:    http://localhost:8083"
echo "  - 心愿服务:    http://localhost:8084"
echo "  - 菜谱服务:    http://localhost:8085"
echo "  - 投票服务:    http://localhost:8086"
echo "  - 纪念日服务:  http://localhost:8087"
echo "  - 日历服务:    http://localhost:8088"
echo "  - 健康服务:    http://localhost:8089"
echo "  - 食材服务:    http://localhost:8090"
echo "  - AI服务:      http://localhost:8091"
echo "  - 消息服务:    http://localhost:8092"
echo "  - 通知服务:    http://localhost:8093"
echo "  - 文件服务:    http://localhost:8094"
echo ""
echo "🌐 前端地址: http://localhost:3000"
echo ""
echo "📝 日志位置: logs/"
