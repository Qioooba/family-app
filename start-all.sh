#!/bin/bash

echo "🚀 快速启动所有服务..."

cd /Volumes/document/Projects/family-app/backend/family-service

# 启动所有服务
services=(
  "user-service:8081"
  "family-service:8082"
  "task-service:8083"
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

for svc in "${services[@]}"; do
  IFS=':' read -r name port <<< "$svc"
  echo "🔄 启动 $name (端口 $port)..."
  
  nohup java -jar -Xmx256m "$name/target/$name-1.0.0.jar" \
    --server.port=$port \
    --spring.datasource.url=jdbc:mysql://localhost:3306/family_app \
    --spring.datasource.username=root \
    --spring.datasource.password=root123 \
    --spring.redis.host=localhost \
    --spring.redis.port=6379 \
    --spring.redis.password= \
    > "/tmp/$name.log" 2>&1 &
  sleep 2
done

echo ""
echo "⏳ 等待服务启动（30秒）..."
sleep 30

echo ""
echo "🧪 测试所有服务..."
for svc in "${services[@]}"; do
  IFS=':' read -r name port <<< "$svc"
  status=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:$port/actuator/health 2>/dev/null)
  if [ "$status" = "200" ]; then
    echo "✅ $name (端口 $port) - 正常"
  else
    echo "❌ $name (端口 $port) - 异常 (HTTP $status)"
  fi
done

echo ""
echo "📝 日志位置: /tmp/*-service.log"
