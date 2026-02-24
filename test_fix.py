from playwright.sync_api import sync_playwright
import time

with sync_playwright() as p:
    browser = p.chromium.launch(headless=False, slow_mo=1500)
    page = browser.new_page(viewport={'width': 375, 'height': 812})
    
    print("=== 测试登录跳转修复 ===")
    page.goto('http://localhost:3000')
    time.sleep(4)
    
    # 输入账号
    page.locator('.login-input').first.click()
    page.keyboard.type('15861890687')
    
    # 输入密码
    page.locator('.login-input').nth(1).click()
    page.keyboard.type('111222')
    
    print("已填写账号密码")
    
    # 点击登录
    page.locator('.login-btn').click()
    print("已点击登录，等待跳转...")
    time.sleep(6)
    
    url = page.url
    print(f"当前URL: {url}")
    
    page.screenshot(path='/Users/qi/.openclaw/workspace/family-app/test-fix/result.png')
    
    if 'home' in url:
        print("✅ 登录跳转修复成功！已进入首页")
    else:
        print(f"❌ 仍未进入首页: {url}")
    
    time.sleep(5)
    browser.close()