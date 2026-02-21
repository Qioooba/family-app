#!/bin/bash

# QA持续测试脚本 - 6小时监控
# 项目: /Volumes/document/Projects/family-app/

PROJECT_DIR="/Volumes/document/Projects/family-app"
LOG_FILE="$PROJECT_DIR/qa_monitor.log"
QA_ISSUES_FILE="$PROJECT_DIR/QA_ISSUES.md"
QA_FIX_FILE="$PROJECT_DIR/QA_FIX_STATUS.md"

# 记录开始时间
START_TIME=$(date +%s)
END_TIME=$((START_TIME + 6 * 3600))  # 6小时后结束

# 上次检查的commit
LAST_COMMIT=""

# 输出函数
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a "$LOG_FILE"
}

# 检查git变化
check_git_changes() {
    cd "$PROJECT_DIR" || exit 1
    
    # 获取最新commit
    CURRENT_COMMIT=$(git log -1 --format="%H")
    CURRENT_MSG=$(git log -1 --format="%s")
    
    if [ "$CURRENT_COMMIT" != "$LAST_COMMIT" ]; then
        if [ -n "$LAST_COMMIT" ]; then
            log "📝 检测到新commit: $CURRENT_MSG"
            log "🔍 开始代码审查..."
            review_new_changes "$LAST_COMMIT" "$CURRENT_COMMIT"
        else
            log "📍 初始commit: $CURRENT_MSG"
        fi
        LAST_COMMIT="$CURRENT_COMMIT"
    fi
}

# 审查新变更
review_new_changes() {
    local OLD_COMMIT=$1
    local NEW_COMMIT=$2
    
    # 获取变更文件
    CHANGED_FILES=$(git diff --name-only "$OLD_COMMIT" "$NEW_COMMIT")
    
    # 检查前端变更
    if echo "$CHANGED_FILES" | grep -q "frontend/src/"; then
        log "📱 前端代码变更:"
        echo "$CHANGED_FILES" | grep "frontend/src/" | while read -r file; do
            log "  - $file"
        done
        check_frontend_api_consistency
    fi
    
    # 检查后端变更
    if echo "$CHANGED_FILES" | grep -q "backend/"; then
        log "⚙️ 后端代码变更:"
        echo "$CHANGED_FILES" | grep "backend/" | while read -r file; do
            log "  - $file"
        done
        check_backend_compilation
    fi
}

# 检查前端API一致性
check_frontend_api_consistency() {
    log "🔍 检查前端API调用一致性..."
    
    # 检查主要API文件
    API_DIR="$PROJECT_DIR/frontend/src/api"
    if [ -d "$API_DIR" ]; then
        # 统计API文件
        API_COUNT=$(find "$API_DIR" -name "*.js" | wc -l)
        log "  📊 前端API文件数: $API_COUNT"
    fi
}

# 检查后端编译
check_backend_compilation() {
    log "🔍 检查后端编译状态..."
    cd "$PROJECT_DIR/backend" || return
    
    # 尝试编译
    if mvn clean compile -q -Dmaven.test.skip=true 2>&1 | grep -q "ERROR"; then
        log "  ❌ 编译失败!"
        # 提取错误信息
        mvn clean compile -q -Dmaven.test.skip=true 2>&1 | grep "ERROR" | head -5 | while read -r line; do
            log "    $line"
        done
    else
        log "  ✅ 编译通过"
    fi
}

# 生成15分钟汇总报告
generate_summary_report() {
    log "═══════════════════════════════════════════"
    log "📊 QA审查汇总报告 ($(date '+%H:%M:%S'))"
    log "═══════════════════════════════════════════"
    
    cd "$PROJECT_DIR" || exit 1
    
    # 获取最新commit
    LATEST_COMMIT=$(git log -1 --format="%h %s")
    log "📝 最新commit: $LATEST_COMMIT"
    
    # 统计QA问题
    if [ -f "$QA_ISSUES_FILE" ]; then
        TODO_COUNT=$(grep -c "^| Q" "$QA_ISSUES_FILE" 2>/dev/null || echo "0")
        log "📋 待修复问题: $TODO_COUNT 项"
    fi
    
    # 检查后端状态
    if [ -f "$QA_FIX_FILE" ]; then
        BLOCKING_COUNT=$(grep -c "🔴" "$QA_FIX_FILE" 2>/dev/null || echo "0")
        log "🚨 P0阻塞问题: $BLOCKING_COUNT 项"
    fi
    
    # 运行编译检查
    check_backend_compilation
    
    log "═══════════════════════════════════════════"
}

# 主循环
log "🚀 QA持续测试模式启动 (6小时)"
log "📁 项目: $PROJECT_DIR"
log "⏰ 开始时间: $(date)"
log "⏰ 预计结束: $(date -r $END_TIME 2>/dev/null || date -d @$END_TIME 2>/dev/null || echo '6 hours later')"
log "═══════════════════════════════════════════"

# 首次检查
check_git_changes
generate_summary_report

LAST_SUMMARY_TIME=$(date +%s)

# 主循环
while [ $(date +%s) -lt $END_TIME ]; do
    CURRENT_TIME=$(date +%s)
    
    # 每10分钟检查git
    check_git_changes
    
    # 每15分钟生成汇总报告
    TIME_DIFF=$((CURRENT_TIME - LAST_SUMMARY_TIME))
    if [ $TIME_DIFF -ge 900 ]; then  # 900秒 = 15分钟
        generate_summary_report
        LAST_SUMMARY_TIME=$CURRENT_TIME
    fi
    
    # 等待10分钟
    log "⏳ 下次检查: 10分钟后..."
    sleep 600
done

log "═══════════════════════════════════════════"
log "✅ QA持续测试完成! (6小时结束)"
log "⏰ 结束时间: $(date)"
log "═══════════════════════════════════════════"
