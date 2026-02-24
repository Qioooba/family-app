#!/usr/bin/env python3
"""
登录功能验证脚本
"""
import requests
import json

BASE_URL = "http://localhost:3000"
PHONE = "15861890687"
PASSWORD = "111222"

def test_login():
    """测试登录功能"""
    print("=" * 50)
    print("🔧 登录功能验证")
    print("=" * 50)
    
    # 1. 测试登录 API
    print("\n1️⃣ 测试登录 API...")
    login_url = f"{BASE_URL}/user/login"
    login_data = {
        "username": PHONE,
        "password": PASSWORD,
        "loginType": "password"
    }
    
    try:
        response = requests.post(login_url, json=login_data, timeout=10)
        result = response.json()
        
        if result.get("code") == 200:
            token = result.get("data")
            print(f"   ✅ 登录成功，获取到 Token: {token[:20]}...")
            
            # 2. 测试获取用户信息
            print("\n2️⃣ 测试获取用户信息...")
            user_url = f"{BASE_URL}/user/info"
            headers = {"Authorization": token}
            
            user_response = requests.get(user_url, headers=headers, timeout=10)
            user_result = user_response.json()
            
            if user_result.get("code") == 200:
                user_info = user_result.get("data")
                print(f"   ✅ 获取用户信息成功")
                print(f"   📱 用户名: {user_info.get('username')}")
                print(f"   👤 昵称: {user_info.get('nickname')}")
                return True
            else:
                print(f"   ❌ 获取用户信息失败: {user_result.get('message')}")
                return False
        else:
            print(f"   ❌ 登录失败: {result.get('message')}")
            return False
            
    except Exception as e:
        print(f"   ❌ 请求异常: {e}")
        return False

def main():
    print("\n🚀 开始验证登录功能...\n")
    
    success = test_login()
    
    print("\n" + "=" * 50)
    if success:
        print("✅ 登录功能验证通过！")
        print("=" * 50)
        print("\n📝 修复内容总结:")
        print("   1. 修复 uview-plus 组件名称: u-icon → up-icon")
        print("   2. 修复 uview-plus 组件名称: u-tabbar → up-tabbar")
        print("   3. 更新相关样式类名")
        print("\n💡 登录信息:")
        print(f"   手机号: {PHONE}")
        print(f"   密码: {PASSWORD}")
    else:
        print("❌ 登录功能验证失败！")
    print("=" * 50)
    
    return success

if __name__ == "__main__":
    import sys
    success = main()
    sys.exit(0 if success else 1)
