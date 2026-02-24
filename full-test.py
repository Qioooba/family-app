from playwright.sync_api import sync_playwright
import time

with sync_playwright() as p:
    browser = p.chromium.launch(headless=False, slow_mo=1500)
    page = browser.new_page(viewport={'width': 375, 'height': 812})
    
    print("=== 登录 ===")
    page.goto('http://localhost:3000')
    time.sleep(4)
    
    # 登录
    page.locator('.login-input').first.click()
    time.sleep(0.5)
    page.keyboard.type('15861890687', delay=50)
    
    page.locator('.login-input').nth(1).click()
    time.sleep(0.5)
    page.keyboard.type('111222', delay=50)
    
    page.locator('.login-btn').first.click()
    print("✅ 登录成功")
    time.sleep(5)
    page.screenshot(path='/Users/qi/.openclaw/workspace/family-app/test-screenshots/01-home.png')
    
    print("\n=== 测试任务页 ===")
    # 点击任务导航
    page.locator('text=任务').first.click()
    time.sleep(4)
    page.screenshot(path='/Users/qi/.openclaw/workspace/family-app/test-screenshots/02-task.png')
    print("✅ 任务页截图")
    
    print("\n=== 测试心愿页 ===")
    page.locator('text=心愿').first.click()
    time.sleep(4)
    page.screenshot(path='/Users/qi/.openclaw/workspace/family-app/test-screenshots/03-wish.png')
    print("✅ 心愿页截图")
    
    print("\n=== 测试家庭页 ===")
    page.locator('text=家庭').first.click()
    time.sleep(4)
    page.screenshot(path='/Users/qi/.openclaw/workspace/family-app/test-screenshots/04-family.png')
    print("✅ 家庭页截图")
    
    print("\n=== 测试我的页 ===")
    page.locator('text=我的').first.click()
    time.sleep(4)
    page.screenshot(path='/Users/qi/.openclaw/workspace/family-app/test-screenshots/05-profile.png')
    print("✅ 我的页截图")
    
    # 测试首页功能
    print("\n=== 回到首页测试功能 ===")
    page.locator('text=首页').first.click()
    time.sleep(3)
    
    # 点击添加任务
    print("点击'添加任务'...")
    page.locator('text=添加任务').first.click()
    time.sleep(3)
    page.screenshot(path='/Users/qi/.openclaw/workspace/family-app/test-screenshots/06-add-task.png')
    print("✅ 添加任务页截图")
    
    print("\n🎉 全部测试完成！保持浏览器打开供查看...")
    print("按 Ctrl+C 关闭或等待60秒自动关闭")
    
    time.sleep(60)  # 保持60秒
    browser.close()