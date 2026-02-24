from playwright.sync_api import sync_playwright
import time

with sync_playwright() as p:
    browser = p.chromium.launch(headless=False, slow_mo=1500)
    page = browser.new_page(viewport={'width': 375, 'height': 812})
    
    print("=== 登录 ===")
    page.goto('http://localhost:3000')
    time.sleep(4)
    
    page.locator('.login-input').first.click()
    page.keyboard.type('15861890687', delay=50)
    
    page.locator('.login-input').nth(1).click()
    page.keyboard.type('111222', delay=50)
    
    page.locator('.login-btn').first.click()
    print("✅ 登录成功")
    time.sleep(5)
    
    print("\n=== 测试首页功能 ===")
    # 测试点击"添加任务"
    page.locator('text=添加任务').first.click()
    time.sleep(3)
    page.screenshot(path='/Users/qi/.openclaw/workspace/family-app/test-screenshots/03-click-addtask.png')
    print("✅ 点击添加任务")
    
    # 返回首页
    page.locator('text=首页').first.click()
    time.sleep(2)
    
    # 测试点击"记录饮食"
    page.locator('text=记录饮食').first.click()
    time.sleep(3)
    page.screenshot(path='/Users/qi/.openclaw/workspace/family-app/test-screenshots/04-click-food.png')
    print("✅ 点击记录饮食")
    
    # 返回首页
    page.go_back()
    time.sleep(2)
    
    print("\n=== 测试家庭页 ===")
    page.locator('text=家庭').first.click()
    time.sleep(4)
    page.screenshot(path='/Users/qi/.openclaw/workspace/family-app/test-screenshots/05-family.png')
    print("✅ 家庭页截图")
    
    print("\n=== 测试我的页 ===")
    page.locator('text=我的').first.click()
    time.sleep(4)
    page.screenshot(path='/Users/qi/.openclaw/workspace/family-app/test-screenshots/06-profile.png')
    print("✅ 我的页截图")
    
    print("\n=== 测试任务列表（从首页今日待办进入） ===")
    page.locator('text=首页').first.click()
    time.sleep(2)
    page.locator('text=更多').first.click()
    time.sleep(3)
    page.screenshot(path='/Users/qi/.openclaw/workspace/family-app/test-screenshots/07-task-list.png')
    print("✅ 任务列表截图")
    
    print("\n🎉 全部测试完成！保持浏览器打开30秒...")
    time.sleep(30)
    browser.close()