#!/bin/bash

# Family App 服务启动脚本
BACKEND_DIR="/Volumes/document/Projects/family-app/backend"
LOG_DIR="$BACKEND_DIR/logs"

mkdir -p $LOG_DIR

# 启动服务函数
start_service() {
    local service_name=$1
    local jar_path=$2
    local log_file="$LOG_DIR/${service_name}.log"
    
    echo "启动 $service_name..."
    nohup java -jar "$jar_path" > "$log_file" 2>&1 &
    echo $!
}

# 启动所有服务
echo "正在启动所有服务..."

# 按依赖顺序启动
start_service "family-service" "$BACKEND_DIR/family-service/family-service/target/family-service-1.0.0.jar"
sleep 2

start_service "user-service" "$BACKEND_DIR/family-service/user-service/target/user-service-1.0.0.jar"
start_service "calendar-service" "$BACKEND_DIR/family-service/calendar-service/target/calendar-service-1.0.0.jar"
start_service "health-service" "$BACKEND_DIR/family-service/health-service/target/health-service-1.0.0.jar"
start_service "file-service" "$BACKEND_DIR/family-service/file-service/target/file-service-1.0.0.jar"
start_service "notify-service" "$BACKEND_DIR/family-service/notify-service/target/notify-service-1.0.0.jar"
start_service "recipe-service" "$BACKEND_DIR/family-service/recipe-service/target/recipe-service-1.0.0.jar"
start_service "task-service" "$BACKEND_DIR/family-service/task-service/target/task-service-1.0.0.jar"
start_service "message-service" "$BACKEND_DIR/family-service/message-service/target/message-service-1.0.0.jar"
start_service "anniversary-service" "$BACKEND_DIR/family-service/anniversary-service/target/anniversary-service-1.0.0.jar"
start_service "ai-service" "$BACKEND_DIR/family-service/ai-service/target/ai-service-1.0.0.jar"
start_service "vote-service" "$BACKEND_DIR/family-service/vote-service/target/vote-service-1.0.0.jar"
start_service "wish-service" "$BACKEND_DIR/family-service/wish-service/target/wish-service-1.0.0.jar"
start_service "food-service" "$BACKEND_DIR/family-service/food-service/target/food-service-1.0.0.jar"

echo "所有服务已启动！"
echo "等待服务启动完成..."
sleep 10

# 检查端口
echo "检查服务端口..."
for port in 8080 8081 8082 8083 8084 8085 8086 8087 8088 8089 8090 8091 8092 8093; do
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
        echo "  ✓ 端口 $port 已监听"
    else
        echo "  ✗ 端口 $port 未监听"
    fi
done
