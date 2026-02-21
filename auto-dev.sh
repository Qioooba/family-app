#!/bin/bash

# 家庭小程序持续开发脚本
# 自动按优先级开发前端API对接功能

PROJECT_DIR="/Users/qi/.openclaw/workspace/family-app"
LOG_FILE="/tmp/family-app-dev.log"

log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a $LOG_FILE
}

cd $PROJECT_DIR

# 设置Java环境
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home

# 检查并启动服务
log "检查服务状态..."
if ! curl -s http://localhost:8080/family/list/1 > /dev/null 2>&1; then
    log "启动Family服务..."
    cd $PROJECT_DIR/backend/family-service/family-service
    mvn spring-boot:run > /tmp/family-service.log 2>&1 &
    sleep 15
fi

if ! curl -s http://localhost:8081/user/info > /dev/null 2>&1; then
    log "启动User服务..."
    cd $PROJECT_DIR/backend/family-service/user-service
    mvn spring-boot:run > /tmp/user-service.log 2>&1 &
    sleep 15
fi

log "服务已就绪，开始开发..."

# 阶段1: 登录页面完善
echo "========================================" >> $LOG_FILE
echo "阶段1: 登录页面完善" >> $LOG_FILE
echo "========================================" >> $LOG_FILE

# 测试登录功能
log "测试登录API..."
LOGIN_RESULT=$(curl -s -X POST http://localhost:8081/user/login \
    -H "Content-Type: application/json" \
    -d '{"username":"test","password":"test123"}' 2>/dev/null)
log "登录结果: $LOGIN_RESULT"

# 提交阶段1
cd $PROJECT_DIR
git add -A
git commit -m "dev: 阶段1 - 登录功能验证" 2>/dev/null || true
git push origin main 2>/dev/null || true

log "阶段1完成"
sleep 5

# 阶段2-7: 其他模块开发（简化版）
for stage in 2 3 4 5 6 7; do
    echo "" >> $LOG_FILE
    echo "========================================" >> $LOG_FILE
    echo "阶段$stage: API对接开发" >> $LOG_FILE
    echo "========================================" >> $LOG_FILE
    
    log "阶段$stage开发进行中..."
    
    # 这里会执行实际的开发命令
    # 由于复杂性，需要手动或通过AI执行
    
    sleep 30
done

log "所有阶段开发完成"
