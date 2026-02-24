from playwright.sync_api import sync_playwright
import time

with sync_playwright() as p:
    browser = p.chromium.launch(headless=False, slow_mo=1500)
    page = browser.new_page(viewport={'width': 375, 'height': 812})
    
    print("=== 测试登录 ===")
    page.goto('http://localhost:3000')
    time.sleep(4)
    
    # 输入账号密码
    page.locator('.login-input').first.click()
    page.keyboard.type('15861890687')
    page.locator('.login-input').nth(1).click()
    page.keyboard.type('111222')
    
    page.screenshot(path='/Users/qi/.openclaw/workspace/family-app/test-login/01-filled.png')
    print("✅ 已填写")
    
    # 点击登录
    page.locator('.login-btn').click()
    time.sleep(5)
    
    url = page.url
    print(f"URL: {url}")
    page.screenshot(path='/Users/qi/.openclaw/workspace/family-app/test-login/02-result.png')
    
    if 'home' in url:
        print("✅ 登录成功，进入首页！")
    else:
        print(f"❌ 未进入首页: {url}")
    
    time.sleep(5)
    browser.close()