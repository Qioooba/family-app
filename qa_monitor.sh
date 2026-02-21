#!/bin/bash
# QA持续监控脚本 - 6小时循环
# 每10分钟检查git变化，每15分钟生成报告

PROJECT_DIR="/Volumes/document/Projects/family-app"
LOG_FILE="$PROJECT_DIR/qa_monitor.log"
LAST_COMMIT_FILE="$PROJECT_DIR/.qa_last_commit"
REPORT_COUNT=0

# 计算结束时间 (6小时后)
END_TIME=$(($(date +%s) + 6 * 3600))

log() {
    echo "[$(date '+%m-%d %H:%M:%S')] $1" | tee -a "$LOG_FILE"
}

check_git() {
    cd "$PROJECT_DIR" || return 1
    CURRENT_COMMIT=$(git log -1 --format="%H")
    CURRENT_MSG=$(git log -1 --format="%s")
    
    if [ -f "$LAST_COMMIT_FILE" ]; then
        LAST_COMMIT=$(cat "$LAST_COMMIT_FILE")
    else
        LAST_COMMIT=""
    fi
    
    if [ "$CURRENT_COMMIT" != "$LAST_COMMIT" ]; then
        if [ -n "$LAST_COMMIT" ]; then
            log "🆕 新commit: $CURRENT_MSG"
            # 获取变更文件
            CHANGED_FILES=$(git diff --name-only "$LAST_COMMIT" "$CURRENT_COMMIT" 2>/dev/null | head -20)
            log "📁 变更文件:"
            echo "$CHANGED_FILES" | while read -r f; do
                [ -n "$f" ] && log "   - $f"
            done
        fi
        echo "$CURRENT_COMMIT" > "$LAST_COMMIT_FILE"
        return 0  # 有变化
    fi
    return 1  # 无变化
}

generate_report() {
    REPORT_COUNT=$((REPORT_COUNT + 1))
    log ""
    log "═══════════════════════════════════════════"
    log "📊 QA审查报告 #$REPORT_COUNT - $(date '+%H:%M:%S')"
    log "═══════════════════════════════════════════"
    
    cd "$PROJECT_DIR" || return
    LATEST=$(git log -1 --format="%h %s")
    log "📝 最新: $LATEST"
    
    # 统计问题
    TODO=$(grep -c "^| Q" QA_ISSUES.md 2>/dev/null || echo "0")
    P0=$(grep -c "P0" QA_ISSUES.md 2>/dev/null || echo "0")
    log "📋 待修复: $TODO | 🚨 P0: $P0"
    
    # 编译检查
    log "🔨 编译检查..."
    cd backend && mvn compile -q -Dmaven.test.skip=true -o 2>&1 | grep -q "ERROR" && log "   ❌ 编译失败" || log "   ✅ 编译通过"
    cd "$PROJECT_DIR"
    
    # 计算剩余时间
    NOW=$(date +%s)
    REMAIN=$(( (END_TIME - NOW) / 60 ))
    log "⏱️  监控剩余: ${REMAIN}分钟"
    log "═══════════════════════════════════════════"
    log ""
}

# 初始化
echo "" > "$LOG_FILE"
echo "$CURRENT_COMMIT" > "$LAST_COMMIT_FILE" 2>/dev/null || true
log "🚀 QA持续测试模式启动"
log "⏰ 开始: $(date '+%H:%M:%S') | 结束: $(date -r $END_TIME '+%H:%M:%S' 2>/dev/null || date -d @$END_TIME '+%H:%M:%S' 2>/dev/null || echo '6h later')"
log "📁 项目: $PROJECT_DIR"
log "═══════════════════════════════════════════"

# 首次报告
generate_report
LAST_REPORT=$(date +%s)

# 主循环
while [ $(date +%s) -lt $END_TIME ]; do
    NOW=$(date +%s)
    
    # 每10分钟检查git
    check_git
    
    # 每15分钟生成报告
    if [ $((NOW - LAST_REPORT)) -ge 900 ]; then
        generate_report
        LAST_REPORT=$NOW
    fi
    
    # 睡眠1分钟后继续检查
    sleep 60
done

log "✅ QA持续测试完成！总报告数: $REPORT_COUNT"
