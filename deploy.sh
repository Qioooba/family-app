#!/bin/bash

# 家庭小程序一键部署脚本
# 使用方法: ./deploy.sh

set -e

echo "🚀 开始部署家庭小程序..."

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 检查Docker是否安装
if ! command -v docker &> /dev/null; then
    echo "${RED}❌ Docker未安装，请先安装Docker${NC}"
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    echo "${RED}❌ Docker Compose未安装，请先安装Docker Compose${NC}"
    exit 1
fi

# 获取脚本所在目录
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR"

echo "📁 工作目录: $SCRIPT_DIR"

# 步骤1: 创建必要目录
echo "📂 创建必要目录..."
mkdir -p docker/nginx/ssl
mkdir -p docker/nginx/html
mkdir -p logs

# 步骤2: 启动基础设施
echo "🐳 启动基础设施（MySQL/Redis/MinIO/RabbitMQ/ES）..."
cd docker
docker-compose pull
docker-compose up -d mysql redis minio rabbitmq elasticsearch

# 步骤3: 等待MySQL启动
echo "⏳ 等待MySQL启动（约30秒）..."
sleep 30

# 检查MySQL是否启动成功
until docker exec family-mysql mysql -uroot -pFamily@2025 -e "SELECT 1" > /dev/null 2>&1; do
    echo "⏳ MySQL还未就绪，继续等待..."
    sleep 5
done

echo "${GREEN}✅ MySQL已就绪${NC}"

# 步骤4: 初始化数据库
echo "💾 初始化数据库..."
if [ -f "../database/schema.sql" ]; then
    docker exec -i family-mysql mysql -uroot -pFamily@2025 < ../database/schema.sql
    echo "${GREEN}✅ 数据库初始化完成${NC}"
else
    echo "${YELLOW}⚠️ 数据库脚本不存在，跳过初始化${NC}"
fi

# 步骤5: 构建后端服务
echo "🔨 构建后端服务..."
cd ../backend

# 构建common模块
echo "📦 构建公共模块..."
cd family-common/common-core
mvn clean install -DskipTests > /dev/null 2>&1 || echo "${YELLOW}⚠️ common-core构建可能已存在${NC}"
cd ../common-security
mvn clean install -DskipTests > /dev/null 2>&1 || echo "${YELLOW}⚠️ common-security构建可能已存在${NC}"
cd ../../..

# 构建用户服务
echo "👤 构建用户服务..."
cd family-service/user-service
mvn clean package -DskipTests
docker build -t family/user-service:latest .
cd ../../..

echo "${GREEN}✅ 后端服务构建完成${NC}"

# 步骤6: 启动后端服务
echo "🚀 启动后端服务..."
cd docker
docker-compose up -d gateway user-service

# 步骤7: 检查服务状态
echo "🔍 检查服务状态..."
sleep 10

services=("family-mysql" "family-redis" "family-minio" "family-gateway")
for service in "${services[@]}"; do
    if docker ps | grep -q "$service"; then
        echo "${GREEN}✅ $service 运行中${NC}"
    else
        echo "${RED}❌ $service 未启动${NC}"
    fi
done

# 步骤8: 配置完成提示
echo ""
echo "${GREEN}========================================${NC}"
echo "${GREEN}🎉 家庭小程序部署完成！${NC}"
echo "${GREEN}========================================${NC}"
echo ""
echo "📱 访问地址:"
echo "  • H5页面: http://localhost"
echo "  • API接口: http://localhost/api"
echo "  • MinIO控制台: http://localhost:9001"
echo ""
echo "🔧 管理命令:"
echo "  • 查看日志: cd docker && docker-compose logs -f"
echo "  • 停止服务: cd docker && docker-compose down"
echo "  • 重启服务: cd docker && docker-compose restart"
echo ""
echo "📖 详细文档: 查看 DEPLOY.md"
echo ""
