from playwright.sync_api import sync_playwright
import time

with sync_playwright() as p:
    browser = p.chromium.launch(headless=False, slow_mo=2000)
    page = browser.new_page(viewport={'width': 375, 'height': 812})
    
    print("1. 访问登录页...")
    page.goto('http://localhost:3000')
    time.sleep(5)
    page.screenshot(path='/Users/qi/.openclaw/workspace/family-app/test-screenshots/debug-01.png')
    print("   ✅ 登录页截图完成")
    
    print("2. 点击并输入手机号...")
    # 点击第一个输入框
    phone_input = page.locator('.login-input').first
    phone_input.click()
    time.sleep(1)
    # 输入文字
    page.keyboard.type('15861890687', delay=100)
    print("   ✅ 手机号输入完成")
    time.sleep(1)
    
    print("3. 点击并输入密码...")
    # 点击第二个输入框
    password_input = page.locator('.login-input').nth(1)
    password_input.click()
    time.sleep(1)
    # 输入密码
    page.keyboard.type('111222', delay=100)
    print("   ✅ 密码输入完成")
    time.sleep(1)
    
    page.screenshot(path='/Users/qi/.openclaw/workspace/family-app/test-screenshots/debug-02-filled.png')
    
    print("4. 点击登录按钮...")
    login_btn = page.locator('.login-btn').first
    print(f"   按钮可见: {login_btn.is_visible()}")
    login_btn.click()
    print("   ✅ 点击登录")
    
    time.sleep(6)
    page.screenshot(path='/Users/qi/.openclaw/workspace/family-app/test-screenshots/debug-03-after-login.png')
    
    url = page.url
    print(f"   当前URL: {url}")
    
    if 'home' in url or url != 'http://localhost:3000/':
        print("   ✅ 登录成功！")
        
        # 保持查看首页
        time.sleep(3)
        page.screenshot(path='/Users/qi/.openclaw/workspace/family-app/test-screenshots/debug-04-home.png')
        print("   ✅ 首页截图完成")
        
        # 尝试找导航点击
        print("5. 测试导航...")
        # 通过文本找导航项
        tabs = page.locator('text=/任务|首页|我的|家庭/').all()
        print(f"   找到 {len(tabs)} 个导航项")
        for i, tab in enumerate(tabs[:4]):
            try:
                text = tab.text_content() or ''
                print(f"   Tab {i}: {text}")
            except:
                pass
        
    else:
        print("   ❌ 仍在登录页，可能登录失败")
    
    print("\n6. 保持10秒查看...")
    time.sleep(10)
    
    browser.close()
    print("\n🎉 测试完成！")