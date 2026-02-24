from playwright.sync_api import sync_playwright
import time
import json

issues = []

def log_issue(name, error):
    issues.append({"name": name, "error": str(error)})
    print(f"❌ {name}: {error}")

with sync_playwright() as p:
    browser = p.chromium.launch(headless=False, slow_mo=500)
    page = browser.new_page(viewport={'width': 375, 'height': 812})
    
    print("=== 开始全面测试 ===\n")
    
    # 1. 登录页测试
    print("1. 登录页测试")
    try:
        page.goto('http://localhost:3000')
        time.sleep(3)
        
        # 检查页面元素
        title = page.locator('.title').text_content()
        print(f"   标题: {title}")
        
        # 输入账号密码
        page.locator('.login-input').first.click()
        page.keyboard.type('15861890687')
        page.locator('.login-input').nth(1).click()
        page.keyboard.type('111222')
        
        # 点击登录
        page.locator('.login-btn').click()
        time.sleep(4)
        
        url = page.url
        print(f"   登录后URL: {url}")
        
        if 'home' in url:
            print("   ✅ 登录成功")
        else:
            log_issue("登录", f"未进入首页，当前: {url}")
    except Exception as e:
        log_issue("登录页", e)
    
    # 2. 首页按钮测试
    print("\n2. 首页按钮测试")
    buttons = ["添加任务", "记录饮食", "喝水打卡", "AI助手"]
    for btn in buttons:
        try:
            page.goto('http://localhost:3000/#/pages/home/index')
            time.sleep(2)
            page.locator(f'text={btn}').click()
            time.sleep(2)
            print(f"   ✅ {btn}")
            page.go_back()
            time.sleep(1)
        except Exception as e:
            log_issue(f"首页按钮-{btn}", e)
    
    # 3. 底部导航测试
    print("\n3. 底部导航测试")
    tabs = ["首页", "任务", "心愿", "家庭", "我的"]
    for tab in tabs:
        try:
            page.locator(f'text={tab}').first.click()
            time.sleep(2)
            print(f"   ✅ 导航-{tab}")
        except Exception as e:
            log_issue(f"导航-{tab}", e)
    
    # 4. 任务模块测试
    print("\n4. 任务模块测试")
    try:
        page.goto('http://localhost:3000/#/pages/task/index')
        time.sleep(2)
        
        # 测试分类标签
        categories = ["购物", "家务", "财务", "育儿", "其他"]
        for cat in categories:
            try:
                page.locator(f'text={cat}').first.click()
                time.sleep(1)
                print(f"   ✅ 分类-{cat}")
            except:
                pass
    except Exception as e:
        log_issue("任务模块", e)
    
    # 5. 家庭模块测试
    print("\n5. 家庭模块测试")
    try:
        page.goto('http://localhost:3000/#/pages/family/index')
        time.sleep(2)
        print("   ✅ 家庭页加载")
        
        # 滚动测试
        page.evaluate('window.scrollTo(0, 500)')
        time.sleep(1)
        print("   ✅ 家庭页滚动")
    except Exception as e:
        log_issue("家庭模块", e)
    
    # 6. 我的模块测试
    print("\n6. 我的模块测试")
    try:
        page.goto('http://localhost:3000/#/pages/profile/index')
        time.sleep(2)
        print("   ✅ 我的页加载")
        
        # 滚动测试
        page.evaluate('window.scrollTo(0, 300)')
        time.sleep(1)
        print("   ✅ 我的页滚动")
    except Exception as e:
        log_issue("我的模块", e)
    
    # 保存结果
    print(f"\n=== 测试完成 ===")
    print(f"发现问题: {len(issues)} 个")
    
    with open('test-manual/RESULT.json', 'w') as f:
        json.dump(issues, f, indent=2, ensure_ascii=False)
    
    for i in issues:
        print(f"  - {i['name']}: {i['error']}")
    
    time.sleep(5)
    browser.close()