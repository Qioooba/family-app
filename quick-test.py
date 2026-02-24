from playwright.sync_api import sync_playwright
import time

with sync_playwright() as p:
    browser = p.chromium.launch(headless=False, slow_mo=1000)
    page = browser.new_page(viewport={'width': 375, 'height': 812})
    
    print("=== 快速测试当前状态 ===")
    
    # 访问登录页
    page.goto('http://localhost:3000')
    time.sleep(4)
    page.screenshot(path='/Users/qi/.openclaw/workspace/family-app/test-final/status-01-login.png')
    print("✅ 登录页截图完成")
    
    # 检查登录页元素
    html = page.content()
    if '登录' in html and 'password' in html.lower():
        print("✅ 登录页正常显示")
    else:
        print("❌ 登录页可能有问题")
    
    # 尝试登录
    try:
        page.locator('.login-input').first.click()
        page.keyboard.type('15861890687', delay=50)
        page.locator('.login-input').nth(1).click()
        page.keyboard.type('111222', delay=50)
        page.screenshot(path='/Users/qi/.openclaw/workspace/family-app/test-final/status-02-filled.png')
        
        page.locator('.login-btn').first.click()
        time.sleep(5)
        page.screenshot(path='/Users/qi/.openclaw/workspace/family-app/test-final/status-03-after-login.png')
        
        url = page.url
        print(f"登录后URL: {url}")
        
        if 'home' in url:
            print("✅ 登录成功，进入首页")
            
            # 测试首页按钮
            buttons = ["添加任务", "记录饮食", "喝水打卡"]
            for btn in buttons:
                try:
                    page.goto('http://localhost:3000/#/pages/home/index')
                    time.sleep(2)
                    page.locator(f'text={btn}').first.click()
                    time.sleep(3)
                    page.screenshot(path=f'/Users/qi/.openclaw/workspace/family-app/test-final/status-btn-{btn}.png')
                    print(f"✅ 按钮 '{btn}' 可点击")
                    page.go_back()
                    time.sleep(2)
                except Exception as e:
                    print(f"❌ 按钮 '{btn}': {e}")
        else:
            print(f"❌ 登录后未进入首页，当前: {url}")
    except Exception as e:
        print(f"❌ 登录失败: {e}")
    
    print("\n=== 测试完成 ===")
    print("截图保存在 test-final/ 目录")
    
    time.sleep(5)
    browser.close()