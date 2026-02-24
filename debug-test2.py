from playwright.sync_api import sync_playwright
import time

with sync_playwright() as p:
    browser = p.chromium.launch(headless=False, slow_mo=2000)
    page = browser.new_page(viewport={'width': 375, 'height': 812})
    
    print("1. 访问登录页...")
    page.goto('http://localhost:3000')
    time.sleep(4)
    page.screenshot(path='/Users/qi/.openclaw/workspace/family-app/test-screenshots/debug-01.png')
    print("   ✅ 登录页截图完成")
    
    print("2. 查找输入框（用class）...")
    # 用class名查找
    inputs = page.locator('.login-input').all()
    print(f"   找到 {len(inputs)} 个 .login-input")
    
    if len(inputs) >= 2:
        print("3. 填写登录信息...")
        inputs[0].fill('15861890687')
        time.sleep(0.5)
        inputs[1].fill('111222')
        print("   ✅ 填写完成")
        page.screenshot(path='/Users/qi/.openclaw/workspace/family-app/test-screenshots/debug-02-filled.png')
        
        print("4. 点击登录按钮...")
        # 用button的class找
        login_btn = page.locator('button.login-btn').first
        print(f"   按钮可见: {login_btn.is_visible()}")
        login_btn.click()
        print("   ✅ 点击登录")
        
        time.sleep(5)
        page.screenshot(path='/Users/qi/.openclaw/workspace/family-app/test-screenshots/debug-03-after-login.png')
        
        url = page.url
        print(f"   当前URL: {url}")
        
        if 'home' in url:
            print("   ✅ 登录成功！进入首页")
            
            # 测试首页
            time.sleep(2)
            page.screenshot(path='/Users/qi/.openclaw/workspace/family-app/test-screenshots/debug-04-home.png')
            
            # 点击底部导航 - 任务
            try:
                task_nav = page.locator('.tab-item:has-text("任务"), text="任务"').first
                if task_nav.is_visible():
                    task_nav.click()
                    time.sleep(3)
                    page.screenshot(path='/Users/qi/.openclaw/workspace/family-app/test-screenshots/debug-05-task.png')
                    print("   ✅ 任务页截图")
            except Exception as e:
                print(f"   ⚠️ 任务页: {e}")
            
            # 点击底部导航 - 我的
            try:
                profile_nav = page.locator('.tab-item:has-text("我的"), text="我的"').first
                if profile_nav.is_visible():
                    profile_nav.click()
                    time.sleep(3)
                    page.screenshot(path='/Users/qi/.openclaw/workspace/family-app/test-screenshots/debug-06-profile.png')
                    print("   ✅ 我的页截图")
            except Exception as e:
                print(f"   ⚠️ 我的页: {e}")
        else:
            print("   ❌ 可能登录失败，仍在登录页")
    else:
        print(f"   ❌ 输入框数量不对: {len(inputs)}")
        # 尝试用xpath
        print("   尝试用xpath...")
        phone = page.locator('//input[1]').first
        password = page.locator('//input[2]').first
        if phone.is_visible():
            phone.fill('15861890687')
            password.fill('111222')
            print("   ✅ 用xpath填写完成")
    
    print("\n5. 保持5秒查看结果...")
    time.sleep(5)
    
    browser.close()
    print("\n🎉 测试完成！截图在 test-screenshots/")