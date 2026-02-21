#!/bin/sh
# 启动脚本
if [ -f "family-service.jar" ]; then
    echo "Starting family-service..."
    java -jar family-service.jar &
fi

if [ -f "ai-service.jar" ]; then
    echo "Starting ai-service..."
    java -jar ai-service.jar &
fi

# 保持容器运行
tail -f /dev/null
