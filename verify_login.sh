#!/bin/bash
# 登录功能验证脚本

BASE_URL="http://localhost:3000"
PHONE="15861890687"
PASSWORD="111222"

echo "=================================================="
echo "🔧 登录功能验证"
echo "=================================================="

# 1. 测试登录 API
echo ""
echo "1️⃣ 测试登录 API..."
LOGIN_RESPONSE=$(curl -s -X POST "${BASE_URL}/user/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"${PHONE}\",\"password\":\"${PASSWORD}\",\"loginType\":\"password\"}")

echo "响应: $LOGIN_RESPONSE"

# 提取 token
TOKEN=$(echo "$LOGIN_RESPONSE" | python3 -c "import sys,json; print(json.load(sys.stdin).get('data',''))" 2>/dev/null)

if [ -n "$TOKEN" ] && [ "$TOKEN" != "None" ]; then
    echo "   ✅ 登录成功，获取到 Token"
    
    # 2. 测试获取用户信息
    echo ""
    echo "2️⃣ 测试获取用户信息..."
    USER_RESPONSE=$(curl -s -X GET "${BASE_URL}/user/info" \
      -H "Authorization: ${TOKEN}")
    
    echo "响应: $USER_RESPONSE"
    
    # 检查响应码
    CODE=$(echo "$USER_RESPONSE" | python3 -c "import sys,json; print(json.load(sys.stdin).get('code',''))" 2>/dev/null)
    
    if [ "$CODE" = "200" ]; then
        USERNAME=$(echo "$USER_RESPONSE" | python3 -c "import sys,json; print(json.load(sys.stdin).get('data',{}).get('username',''))" 2>/dev/null)
        NICKNAME=$(echo "$USER_RESPONSE" | python3 -c "import sys,json; print(json.load(sys.stdin).get('data',{}).get('nickname',''))" 2>/dev/null)
        
        echo "   ✅ 获取用户信息成功"
        echo "   📱 用户名: $USERNAME"
        echo "   👤 昵称: $NICKNAME"
        
        echo ""
        echo "=================================================="
        echo "✅ 登录功能验证通过！"
        echo "=================================================="
        echo ""
        echo "📝 修复内容总结:"
        echo "   1. 修复 uview-plus 组件名称: u-icon → up-icon"
        echo "   2. 修复 uview-plus 组件名称: u-tabbar → up-tabbar"
        echo "   3. 更新相关样式类名"
        echo ""
        echo "💡 登录信息:"
        echo "   手机号: $PHONE"
        echo "   密码: $PASSWORD"
        echo "=================================================="
        exit 0
    else
        echo "   ❌ 获取用户信息失败"
        exit 1
    fi
else
    echo "   ❌ 登录失败"
    exit 1
fi
