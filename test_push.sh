#!/bin/bash
# 测试企业微信推送

echo "==================== 测试企业微信推送 ===================="

CORPID="ww6c1c7590db91ef85"
SECRET="Ne0oN5Y8mNmRA_wkIP7I4PMn_sr2GFPkbBABqUaEEE4"
AGENTID="1000002"

echo ""
echo "1. 获取access_token..."
TOKEN_URL="https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=${CORPID}&corpsecret=${SECRET}"
TOKEN_RESULT=$(curl -s "${TOKEN_URL}")
echo "Token结果: ${TOKEN_RESULT}"

ACCESS_TOKEN=$(echo ${TOKEN_RESULT} | python3 -c "import sys,json; print(json.load(sys.stdin).get('access_token',''))" 2>/dev/null)

if [ -z "${ACCESS_TOKEN}" ]; then
    echo "❌ 获取token失败"
    exit 1
fi

echo "✅ 获取token成功: ${ACCESS_TOKEN:0:20}..."

echo ""
echo "2. 发送测试消息给齐军(qi_jun)..."
SEND_URL="https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=${ACCESS_TOKEN}"

curl -s -X POST "${SEND_URL}" \
  -H "Content-Type: application/json" \
  -d '{
    "touser": "qi_jun",
    "msgtype": "textcard",
    "agentid": "'${AGENTID}'",
    "textcard": {
      "title": "🏠 测试消息",
      "description": "<div class=gray>'$(date "+%Y-%m-%d %H:%M")'</div><div class=highlight>这是一条测试消息，企业微信推送已配置成功！</div>",
      "url": "https://qioba.cn",
      "btntxt": "查看详情"
    }
  }' | python3 -m json.tool 2>/dev/null || echo "发送完成"

echo ""
echo "==================== 测试完成 ===================="
