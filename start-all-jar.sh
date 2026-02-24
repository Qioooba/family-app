#!/bin/bash
# 批量启动所有家庭助手服务

echo "🚀 批量启动服务..."

cd /Users/qi/.openclaw/workspace/family-app/backend/family-service

# 启动所有服务
nohup java -jar user-service/target/user-service-1.0.0.jar > /Users/qi/.openclaw/workspace/family-app/logs/user-service.log 2>&1 &
echo "✅ user-service (8081)"
sleep 2

nohup java -jar family-service/target/family-service-1.0.0.jar > /Users/qi/.openclaw/workspace/family-app/logs/family-service.log 2>&1 &
echo "✅ family-service (8082)"
sleep 2

nohup java -jar task-service/target/task-service-1.0.0.jar > /Users/qi/.openclaw/workspace/family-app/logs/task-service.log 2>&1 &
echo "✅ task-service (8083)"
sleep 2

nohup java -jar file-service/target/file-service-1.0.0.jar > /Users/qi/.openclaw/workspace/family-app/logs/file-service.log 2>&1 &
echo "✅ file-service (8084)"
sleep 2

nohup java -jar recipe-service/target/recipe-service-1.0.0.jar > /Users/qi/.openclaw/workspace/family-app/logs/recipe-service.log 2>&1 &
echo "✅ recipe-service (8086)"
sleep 2

nohup java -jar anniversary-service/target/anniversary-service-1.0.0.jar > /Users/qi/.openclaw/workspace/family-app/logs/anniversary-service.log 2>&1 &
echo "✅ anniversary-service (8087)"
sleep 2

nohup java -jar calendar-service/target/calendar-service-1.0.0.jar > /Users/qi/.openclaw/workspace/family-app/logs/calendar-service.log 2>&1 &
echo "✅ calendar-service (8088)"
sleep 2

nohup java -jar health-service/target/health-service-1.0.0.jar > /Users/qi/.openclaw/workspace/family-app/logs/health-service.log 2>&1 &
echo "✅ health-service (8089)"
sleep 2

nohup java -jar ai-service/target/ai-service-1.0.0.jar > /Users/qi/.openclaw/workspace/family-app/logs/ai-service.log 2>&1 &
echo "✅ ai-service (8090)"
sleep 2

nohup java -jar vote-service/target/vote-service-1.0.0.jar > /Users/qi/.openclaw/workspace/family-app/logs/vote-service.log 2>&1 &
echo "✅ vote-service (8091)"
sleep 2

nohup java -jar wish-service/target/wish-service-1.0.0.jar > /Users/qi/.openclaw/workspace/family-app/logs/wish-service.log 2>&1 &
echo "✅ wish-service (8092)"
sleep 2

nohup java -jar notify-service/target/notify-service-1.0.0.jar > /Users/qi/.openclaw/workspace/family-app/logs/notify-service.log 2>&1 &
echo "✅ notify-service (8093)"
sleep 2

nohup java -jar message-service/target/message-service-1.0.0.jar > /Users/qi/.openclaw/workspace/family-app/logs/message-service.log 2>&1 &
echo "✅ message-service (8094)"
sleep 2

nohup java -jar food-service/target/food-service-1.0.0.jar > /Users/qi/.openclaw/workspace/family-app/logs/food-service.log 2>&1 &
echo "✅ food-service (8095)"
sleep 2

# 启动网关
cd /Users/qi/.openclaw/workspace/family-app/backend/family-gateway
nohup java -jar target/family-gateway-1.0.0.jar > /Users/qi/.openclaw/workspace/family-app/logs/gateway.log 2>&1 &
echo "✅ gateway (8099)"

echo ""
echo "🎉 所有服务启动完成！"
echo "等待10秒服务初始化..."
sleep 10
echo ""
echo "📊 检查服务端口状态..."

# 检查所有端口
for port in 8081 8082 8083 8084 8086 8087 8088 8089 8090 8091 8092 8093 8094 8095 8099; do
    if lsof -i :$port > /dev/null 2>&1; then
        echo "✅ 端口 $port 正常"
    else
        echo "❌ 端口 $port 未启动"
    fi
done
