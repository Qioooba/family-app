#!/bin/bash
# 启动所有家庭助手App服务的脚本

echo "🚀 启动家庭助手App服务..."

# 颜色定义
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 检查MySQL
echo "📋 检查MySQL服务..."
if ! mysqladmin ping -h localhost --silent 2>/dev/null; then
    echo -e "${RED}❌ MySQL未启动，请先启动MySQL${NC}"
    echo "   提示: brew services start mysql"
    exit 1
fi
echo -e "${GREEN}✅ MySQL运行正常${NC}"

# 检查Redis
echo "📋 检查Redis服务..."
if ! redis-cli ping 2>/dev/null | grep -q "PONG"; then
    echo -e "${YELLOW}⚠️ Redis未启动，正在尝试启动...${NC}"
    redis-server --daemonize yes
    sleep 2
fi
echo -e "${GREEN}✅ Redis运行正常${NC}"

# 服务列表（按依赖顺序）
declare -a services=(
    "user-service:8081"
    "family-service:8082"
    "task-service:8083"
    "wish-service:8092"
    "vote-service:8091"
    "anniversary-service:8087"
    "calendar-service:8088"
    "health-service:8089"
    "food-service:8095"
    "ai-service:8090"
    "message-service:8094"
    "notify-service:8093"
    "file-service:8084"
    "recipe-service:8086"
)

echo ""
echo "🔄 启动微服务..."
cd /Users/qi/.openclaw/workspace/family-app/backend/family-service

for service in "${services[@]}"; do
    IFS=':' read -r name port <<< "$service"
    
    # 检查端口是否已被占用
    if lsof -i :$port >/dev/null 2>&1; then
        echo -e "${YELLOW}⚠️ $name (端口 $port) 已在运行${NC}"
        continue
    fi
    
    echo "📝 启动 $name (端口 $port)..."
    cd "$name" 2>/dev/null || continue
    
    # 后台启动服务
    nohup mvn spring-boot:run -q > "../../logs/$name.log" 2>&1 &
    
    cd ..
    sleep 3
done

# 启动网关
echo ""
echo "🌐 启动API网关..."
cd /Users/qi/.openclaw/workspace/family-app/backend/family-gateway

if ! lsof -i :8099 >/dev/null 2>&1; then
    nohup mvn spring-boot:run -q > "../family-service/logs/gateway.log" 2>&1 &
    echo -e "${GREEN}✅ 网关已启动 (端口 8099)${NC}"
else
    echo -e "${YELLOW}⚠️ 网关 (端口 8099) 已在运行${NC}"
fi

echo ""
echo -e "${GREEN}🎉 所有服务启动完成！${NC}"
echo ""
echo "📊 服务状态检查:"
sleep 5

# 检查各服务端口
for service in "${services[@]}"; do
    IFS=':' read -r name port <<< "$service"
    if lsof -i :$port >/dev/null 2>&1; then
        echo -e "${GREEN}✅ $name (端口 $port)${NC}"
    else
        echo -e "${RED}❌ $name (端口 $port) 未启动${NC}"
    fi
done

# 检查网关
if lsof -i :8099 >/dev/null 2>&1; then
    echo -e "${GREEN}✅ API网关 (端口 8099)${NC}"
else
    echo -e "${RED}❌ API网关 (端口 8099) 未启动${NC}"
fi

echo ""
echo "📖 日志位置: /Users/qi/.openclaw/workspace/family-app/backend/family-service/logs/"
echo "🧪 测试网关: curl http://localhost:8099/api/user/info"
echo ""
