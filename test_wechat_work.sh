#!/bin/bash
# 企业微信推送测试脚本

echo "==================== 企业微信推送测试 ===================="

# 测试获取access_token
CORPID="ww6c1c7590db91ef85"
SECRET="Ne0oN5Y8mNmRA_wkIP7I4PMn_sr2GFPkbBABqUaEEE4"

echo ""
echo "1. 测试获取企业微信access_token..."
TOKEN_URL="https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=${CORPID}&corpsecret=${SECRET}"

curl -s "${TOKEN_URL}" | python3 -m json.tool 2>/dev/null || curl -s "${TOKEN_URL}"

echo ""
echo ""
echo "2. 如果上面返回了access_token，说明配置正确！"
echo ""
echo "==================== 测试说明 ===================="
echo ""
echo "请让陶陶在小程序中："
echo "1. 打开小程序 → 待办 → 添加任务"
echo "2. 指派给齐军"
echo "3. 观察齐军微信是否收到通知"
echo ""
echo "如果收不到，检查："
echo "- 齐军是否关注了'我的企业'微信插件"
echo "- 企业微信中齐军的账号是否是'qi_jun'"
echo ""
