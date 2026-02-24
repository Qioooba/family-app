from playwright.sync_api import sync_playwright
import time

with sync_playwright() as p:
    browser = p.chromium.launch(headless=False, slow_mo=2000)
    page = browser.new_page(viewport={'width': 375, 'height': 812})
    
    print("1. 访问登录页...")
    page.goto('http://localhost:3000')
    time.sleep(3)
    page.screenshot(path='/Users/qi/.openclaw/workspace/family-app/test-screenshots/debug-01.png')
    print("   ✅ 截图保存: debug-01.png")
    
    print("2. 查找输入框...")
    # 打印页面HTML结构
    html = page.content()
    print(f"   页面HTML长度: {len(html)}")
    
    # 查找所有input
    inputs = page.locator('input').all()
    print(f"   找到 {len(inputs)} 个input")
    
    for i, inp in enumerate(inputs):
        placeholder = inp.get_attribute('placeholder') or ''
        input_type = inp.get_attribute('type') or ''
        print(f"   Input {i}: placeholder='{placeholder}', type='{input_type}'")
    
    # 尝试用placeholder查找
    phone_input = page.locator('input[placeholder*="手机号"]').first
    password_input = page.locator('input[placeholder*="密码"]').first
    
    print(f"   手机号输入框可见: {phone_input.is_visible()}")
    print(f"   密码输入框可见: {password_input.is_visible()}")
    
    if phone_input.is_visible() and password_input.is_visible():
        print("3. 填写登录信息...")
        phone_input.fill('15861890687')
        password_input.fill('111222')
        print("   ✅ 填写完成")
        page.screenshot(path='/Users/qi/.openclaw/workspace/family-app/test-screenshots/debug-02.png')
        
        print("4. 点击登录按钮...")
        login_btn = page.locator('.login-btn, button:has-text("登录")').first
        print(f"   登录按钮可见: {login_btn.is_visible()}")
        login_btn.click()
        print("   ✅ 点击完成")
        
        time.sleep(5)
        page.screenshot(path='/Users/qi/.openclaw/workspace/family-app/test-screenshots/debug-03.png')
        print(f"   当前URL: {page.url}")
    else:
        print("   ❌ 输入框不可见")
        page.screenshot(path='/Users/qi/.openclaw/workspace/family-app/test-screenshots/debug-error.png')
    
    print("5. 保持浏览器打开10秒...")
    time.sleep(10)
    
    browser.close()
    print("\n测试完成！")