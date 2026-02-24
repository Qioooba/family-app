from playwright.sync_api import sync_playwright
import time

with sync_playwright() as p:
    # 启动浏览器 - 非headless，保持窗口打开
    browser = p.chromium.launch(
        headless=False,
        slow_mo=2000,
        args=['--window-size=400,900']
    )
    
    # 创建页面
    page = browser.new_page(viewport={'width': 375, 'height': 812})
    
    print("=== 开始测试 ===")
    
    # 访问登录页
    print("1. 访问登录页...")
    page.goto('http://localhost:3000')
    time.sleep(4)
    page.screenshot(path='/Users/qi/.openclaw/workspace/family-app/test-run/login.png')
    print("   截图已保存")
    
    # 检查是否能找到输入框
    inputs = page.locator('.login-input').all()
    print(f"   找到 {len(inputs)} 个输入框")
    
    if len(inputs) >= 2:
        # 输入账号
        print("2. 输入手机号...")
        inputs[0].click()
        time.sleep(0.5)
        page.keyboard.type('15861890687')
        time.sleep(0.5)
        
        # 输入密码
        print("3. 输入密码...")
        inputs[1].click()
        time.sleep(0.5)
        page.keyboard.type('111222')
        time.sleep(0.5)
        
        page.screenshot(path='/Users/qi/.openclaw/workspace/family-app/test-run/login-filled.png')
        print("   已填写")
        
        # 点击登录
        print("4. 点击登录按钮...")
        page.locator('.login-btn').click()
        time.sleep(5)
        
        url = page.url
        print(f"   当前URL: {url}")
        page.screenshot(path='/Users/qi/.openclaw/workspace/family-app/test-run/after-login.png')
        
        if 'home' in url:
            print("   ✅ 登录成功！")
            
            # 测试首页按钮
            print("\n5. 测试首页按钮...")
            buttons = ["添加任务", "记录饮食", "喝水打卡"]
            for i, btn in enumerate(buttons):
                try:
                    page.goto('http://localhost:3000/#/pages/home/index')
                    time.sleep(2)
                    page.locator(f'text={btn}').first.click()
                    time.sleep(3)
                    page.screenshot(path=f'/Users/qi/.openclaw/workspace/family-app/test-run/btn-{i}.png')
                    print(f"   ✅ {btn}")
                    page.go_back()
                    time.sleep(2)
                except Exception as e:
                    print(f"   ❌ {btn}: {e}")
            
            # 测试导航
            print("\n6. 测试底部导航...")
            navs = ["任务", "家庭", "我的"]
            for i, nav in enumerate(navs):
                try:
                    page.locator(f'text={nav}').first.click()
                    time.sleep(3)
                    page.screenshot(path=f'/Users/qi/.openclaw/workspace/family-app/test-run/nav-{i}.png')
                    print(f"   ✅ {nav}")
                except Exception as e:
                    print(f"   ❌ {nav}: {e}")
        else:
            print("   ❌ 登录后未进入首页")
    else:
        print("   ❌ 输入框数量不对")
    
    print("\n=== 测试完成 ===")
    print("截图保存在 test-run/ 目录")
    
    # 保持10秒查看
    time.sleep(10)
    browser.close()