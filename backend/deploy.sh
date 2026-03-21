#!/bin/bash

# Family App 一键部署脚本
# 用法: bash deploy.sh

set -e

# 配置
MYSQL_ROOT_PASSWORD="FamilyApp2024!"
APP_DIR="/opt/family-app"
MYSQL_CONTAINER="family-mysql"
BACKEND_JAR="family-service-1.0.0.jar"

echo "=========================================="
echo "   Family App 部署脚本"
echo "=========================================="

# 检查是否为 root
if [ "$EUID" -ne 0 ]; then
  echo "请使用 root 用户运行此脚本"
  exit 1
fi

echo ""
echo "========== 1. 安装必要软件 =========="

# 安装 Docker
if ! command -v docker &> /dev/null; then
    echo "安装 Docker..."
    curl -fsSL https://get.docker.com | sh
    systemctl enable docker
    systemctl start docker
else
    echo "Docker 已安装"
fi

# 安装 Java 17
if ! command -v java &> /dev/null; then
    echo "安装 Java 17..."
    apt update
    apt install -y openjdk-17-jdk
else
    echo "Java 已安装"
fi

echo ""
echo "========== 2. 创建应用目录 =========="
mkdir -p $APP_DIR
echo "应用目录: $APP_DIR"

echo ""
echo "========== 3. 启动 MySQL =========="

# 检查 MySQL 是否已运行
if docker ps | grep -q $MYSQL_CONTAINER; then
    echo "MySQL 容器已在运行"
else
    # 检查是否已有停止的容器
    if docker ps -a | grep -q $MYSQL_CONTAINER; then
        echo "启动已有 MySQL 容器..."
        docker start $MYSQL_CONTAINER
    else
        echo "创建并启动 MySQL 容器..."
        docker run -d \
            --name $MYSQL_CONTAINER \
            -e MYSQL_ROOT_PASSWORD=$MYSQL_ROOT_PASSWORD \
            -e MYSQL_DATABASE=family_app \
            -v mysql-data:/var/lib/mysql \
            -p 3306:3306 \
            --restart=always \
            mysql:8.0 --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci

        # 等待 MySQL 启动
        echo "等待 MySQL 启动..."
        sleep 15
    fi
fi

echo ""
echo "========== 4. 导入数据库 =========="

# 检查数据库是否已有数据
DB_EXISTS=$(docker exec $MYSQL_CONTAINER mysql -uroot -p$MYSQL_ROOT_PASSWORD -e "SHOW DATABASES LIKE 'family_app';" 2>/dev/null | grep family_app || echo "")

if [ -z "$DB_EXISTS" ]; then
    if [ -f "/tmp/family_app.sql" ]; then
        echo "导入数据库..."
        docker exec -i $MYSQL_CONTAINER mysql -uroot -p$MYSQL_ROOT_PASSWORD family_app < /tmp/family_app.sql
        echo "数据库导入完成"
    else
        echo "警告: 未找到 family_app.sql 文件，跳过数据库导入"
    fi
else
    echo "数据库已存在，跳过导入"
fi

echo ""
echo "========== 5. 部署后端 =========="

if [ -f "$APP_DIR/$BACKEND_JAR" ]; then
    # 检查后端是否在运行
    if pgrep -f "family-service" > /dev/null; then
        echo "停止旧版本后端..."
        pkill -f "family-service"
        sleep 3
    fi

    echo "启动后端服务..."
    cd $APP_DIR
    nohup java -Xmx512m -Xms256m \
        -jar $BACKEND_JAR \
        --server.port=8443 \
        --spring.datasource.url=jdbc:mysql://localhost:3306/family_app \
        --spring.datasource.username=root \
        --spring.datasource.password=$MYSQL_ROOT_PASSWORD \
        > /var/log/family-app.log 2>&1 &

    echo "后端服务已启动"
    sleep 10
else
    echo "警告: 未找到 JAR 文件: $APP_DIR/$BACKEND_JAR"
    echo "请手动上传 JAR 文件后运行: java -jar $APP_DIR/$BACKEND_JAR"
fi

echo ""
echo "========== 6. 检查状态 =========="

# 检查 MySQL
if docker ps | grep -q $MYSQL_CONTAINER; then
    echo "✅ MySQL: 运行中"
else
    echo "❌ MySQL: 未运行"
fi

# 检查后端
if curl -sk https://localhost:8443/api/user/login -X POST -H "Content-Type: application/json" -d '{"phone":"test","password":"test"}' 2>/dev/null | grep -q "code"; then
    echo "✅ 后端服务: 运行中"
else
    echo "⚠️ 后端服务: 启动中或未响应"
fi

echo ""
echo "========== 部署完成 =========="
echo ""
echo "服务地址: https://36.151.150.188:8443"
echo "MySQL: localhost:3306"
echo ""
echo "常用命令:"
echo "  查看日志: tail -f /var/log/family-app.log"
echo "  重启后端: cd $APP_DIR && java -jar $BACKEND_JAR &"
echo "  重启MySQL: docker restart $MYSQL_CONTAINER"
echo ""
