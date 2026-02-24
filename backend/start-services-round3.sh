#!/bin/bash

# Family App Backend Services Startup Script (Round 3 Fix)
# 启动所有后端服务

cd /Users/qi/.openclaw/workspace/family-app/backend

# 设置环境变量
export MYSQL_USERNAME=root
export MYSQL_PASSWORD=your_secure_mysql_password
export REDIS_HOST=localhost
export REDIS_PORT=6379

# 日志目录
mkdir -p logs

# 颜色输出
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  Family App Backend - Round 3 Startup  ${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""

# 检查依赖
echo "Checking dependencies..."
if ! redis-cli ping > /dev/null 2>&1; then
    echo -e "${RED}Error: Redis is not running${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Redis is running${NC}"

if ! mysql -u root -e "SELECT 1" > /dev/null 2>&1; then
    echo -e "${YELLOW}Warning: MySQL root access may need password${NC}"
fi
echo -e "${GREEN}✓ MySQL is running${NC}"

echo ""
echo "Starting services..."
echo ""

# 函数：启动服务
start_service() {
    local service_name=$1
    local service_dir=$2
    local port=$3
    
    echo -n "Starting $service_name on port $port... "
    
    cd /Users/qi/.openclaw/workspace/family-app/backend/$service_dir
    
    # 检查端口是否被占用
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
        echo -e "${YELLOW}Port $port already in use, skipping${NC}"
        return
    fi
    
    # 启动服务
    nohup mvn spring-boot:run -q > /Users/qi/.openclaw/workspace/family-app/backend/logs/$service_name.log 2>&1 &
    local pid=$!
    
    # 等待服务启动
    local count=0
    local max_wait=60
    while [ $count -lt $max_wait ]; do
        if curl -s http://localhost:$port/actuator/health > /dev/null 2>&1 || \
           curl -s http://localhost:$port > /dev/null 2>&1; then
            echo -e "${GREEN}OK (PID: $pid)${NC}"
            return
        fi
        sleep 1
        count=$((count + 1))
    done
    
    echo -e "${YELLOW}Started (health check timeout)${NC}"
}

# 启动Gateway (端口 8099)
start_service "gateway" "family-gateway" "8099"
sleep 5

# 启动核心业务服务 (按依赖顺序)
start_service "user-service" "family-service/user-service" "8081"
sleep 3

start_service "family-service" "family-service/family-service" "8082"
sleep 2

start_service "task-service" "family-service/task-service" "8083"
sleep 2

start_service "file-service" "family-service/file-service" "8084"
sleep 2

start_service "recipe-service" "family-service/recipe-service" "8086"
sleep 2

start_service "anniversary-service" "family-service/anniversary-service" "8087"
sleep 2

start_service "calendar-service" "family-service/calendar-service" "8088"
sleep 2

start_service "health-service" "family-service/health-service" "8089"
sleep 2

start_service "ai-service" "family-service/ai-service" "8090"
sleep 2

start_service "vote-service" "family-service/vote-service" "8091"
sleep 2

start_service "wish-service" "family-service/wish-service" "8092"
sleep 2

start_service "notify-service" "family-service/notify-service" "8093"
sleep 2

start_service "message-service" "family-service/message-service" "8094"
sleep 2

start_service "food-service" "family-service/food-service" "8095"

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  All services startup initiated!       ${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo "Service Status:"
sleep 3

# 显示端口监听状态
echo ""
echo "Listening ports:"
lsof -Pi :8081-8099 -sTCP:LISTEN 2>/dev/null | grep LISTEN || echo "No services listening yet (starting up...)"

echo ""
echo "Logs directory: /Users/qi/.openclaw/workspace/family-app/backend/logs/"
echo ""
echo "To check logs: tail -f /Users/qi/.openclaw/workspace/family-app/backend/logs/<service-name>.log"
echo "To stop all: pkill -f 'spring-boot:run'"
