#!/bin/bash
# 测试企业微信直接推送

echo "==================== 企业微信推送测试 ===================="

CORPID="${WECHAT_WORK_CORPID:-}"
SECRET="${WECHAT_WORK_SECRET:-}"
AGENTID="${WECHAT_WORK_AGENTID:-}"

if [ -z "${CORPID}" ] || [ -z "${SECRET}" ] || [ -z "${AGENTID}" ]; then
    echo "❌ 请先导出 WECHAT_WORK_CORPID / WECHAT_WORK_SECRET / WECHAT_WORK_AGENTID"
    exit 1
fi

echo ""
echo "1. 获取access_token..."
TOKEN_RESULT=$(curl -s "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=${CORPID}&corpsecret=${SECRET}")
echo "Token结果: ${TOKEN_RESULT}"

ACCESS_TOKEN=$(echo ${TOKEN_RESULT} | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('access_token',''))" 2>/dev/null)

if [ -z "${ACCESS_TOKEN}" ]; then
    echo "❌ 获取token失败"
    exit 1
fi

echo "✅ 获取token成功"

echo ""
echo "2. 发送测试消息给齐军(QiJun)..."
SEND_RESULT=$(curl -s -X POST "https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=${ACCESS_TOKEN}" \
  -H "Content-Type: application/json" \
  -d "{
    \"touser\": \"QiJun\",
    \"msgtype\": \"textcard\",
    \"agentid\": ${AGENTID},
    \"textcard\": {
      \"title\": \"🏠 新任务指派\",
      \"description\": \"<div class='gray'>$(date '+%Y-%m-%d %H:%M')</div><div class='highlight'>陶陶给你指派了任务：测试企业微信推送</div>\",
      \"url\": \"${APP_BASE_URL:-http://localhost:8443}\",
      \"btntxt\": \"查看详情\"
    }
  }")

echo "发送结果: ${SEND_RESULT}"

echo ""
echo "3. 发送测试消息给陶陶(TaoTao)..."
SEND_RESULT2=$(curl -s -X POST "https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=${ACCESS_TOKEN}" \
  -H "Content-Type: application/json" \
  -d "{
    \"touser\": \"TaoTao\",
    \"msgtype\": \"textcard\",
    \"agentid\": ${AGENTID},
    \"textcard\": {
      \"title\": \"✅ 任务已指派\",
      \"description\": \"<div class='gray'>$(date '+%Y-%m-%d %H:%M')</div><div class='highlight'>你已指派任务给齐军：测试企业微信推送</div>\",
      \"url\": \"${APP_BASE_URL:-http://localhost:8443}\",
      \"btntxt\": \"查看详情\"
    }
  }")

echo "发送结果: ${SEND_RESULT2}"

echo ""
echo "==================== 测试完成 ===================="
echo ""
echo "请检查齐军和陶陶的微信是否收到通知！"
