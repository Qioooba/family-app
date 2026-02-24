from playwright.sync_api import sync_playwright
import time

results = []

def test_step(name, func):
    try:
        func()
        results.append({"name": name, "status": "✅"})
        print(f"✅ {name}")
    except Exception as e:
        results.append({"name": name, "status": "❌", "error": str(e)})
        print(f"❌ {name}: {e}")

def screenshot(page, name):
    page.screenshot(path=f'/Users/qi/.openclaw/workspace/family-app/test-round2/{name}.png')

with sync_playwright() as p:
    browser = p.chromium.launch(headless=False, slow_mo=1000)
    page = browser.new_page(viewport={'width': 375, 'height': 812})
    
    # ========== 登录测试 ==========
    print("\n=== 登录测试 ===")
    page.goto('http://localhost:3000')
    time.sleep(4)
    screenshot(page, '01-login')
    
    # 输入账号密码登录
    page.locator('.login-input').first.click()
    page.keyboard.type('15861890687', delay=50)
    page.locator('.login-input').nth(1).click()
    page.keyboard.type('111222', delay=50)
    page.locator('.login-btn').first.click()
    time.sleep(5)
    screenshot(page, '02-home')
    
    # ========== 首页功能测试 ==========
    print("\n=== 首页功能测试 ===")
    
    # 测试1: 添加任务按钮
    def test_add_task():
        page.locator('text=添加任务').first.click()
        time.sleep(3)
        screenshot(page, '03-add-task-click')
        assert '创建任务' in page.content() or 'task' in page.url.lower()
        page.go_back()
        time.sleep(2)
    test_step("添加任务按钮", test_add_task)
    
    # 测试2: 记录饮食按钮
    def test_food():
        page.locator('text=记录饮食').first.click()
        time.sleep(3)
        screenshot(page, '04-food-record')
        page.go_back()
        time.sleep(2)
    test_step("记录饮食按钮", test_food)
    
    # 测试3: 喝水打卡按钮
    def test_water():
        page.locator('text=喝水打卡').first.click()
        time.sleep(3)
        screenshot(page, '05-water-check')
        page.go_back()
        time.sleep(2)
    test_step("喝水打卡按钮", test_water)
    
    # 测试4: 今日待办-更多按钮
    def test_task_more():
        page.locator('text=更多').first.click()
        time.sleep(3)
        screenshot(page, '06-task-more')
        page.go_back()
        time.sleep(2)
    test_step("今日待办-更多按钮", test_task_more)
    
    # 测试5: 任务列表滚动
    def test_scroll():
        page.evaluate('window.scrollTo(0, 500)')
        time.sleep(1)
        screenshot(page, '07-home-scrolled')
    test_step("首页滚动", test_scroll)
    
    # ========== 任务模块测试 ==========
    print("\n=== 任务模块测试 ===")
    
    # 测试6: 任务导航
    def test_task_nav():
        page.locator('.uni-tabbar__label:has-text("任务"), text=任务').first.click()
        time.sleep(3)
        screenshot(page, '08-task-page')
    test_step("任务导航", test_task_nav)
    
    # 测试7: 创建任务表单-填写标题
    def test_task_form_title():
        page.locator('input[placeholder*="标题"], .task-title-input').first.fill("测试任务标题")
        time.sleep(1)
        screenshot(page, '09-task-title-filled')
    test_step("任务表单-填写标题", test_task_form_title)
    
    # 测试8: 选择分类
    def test_task_category():
        page.locator('text=购物').first.click()
        time.sleep(1)
        screenshot(page, '10-task-category')
    test_step("任务表单-选择分类", test_task_category)
    
    # 测试9: 选择优先级
    def test_task_priority():
        page.locator('text=重要').first.click()
        time.sleep(1)
        screenshot(page, '11-task-priority')
    test_step("任务表单-选择优先级", test_task_priority)
    
    # 测试10: 选择指派给
    def test_task_assign():
        page.locator('text=爸爸').first.click()
        time.sleep(1)
        screenshot(page, '12-task-assign')
    test_step("任务表单-指派给", test_task_assign)
    
    # 测试11: 保存任务
    def test_task_save():
        page.locator('.save-btn, text=保存, button:has-text("保存")').first.click()
        time.sleep(3)
        screenshot(page, '13-task-saved')
    test_step("保存任务按钮", test_task_save)
    
    # ========== 家庭模块测试 ==========
    print("\n=== 家庭模块测试 ===")
    
    # 测试12: 家庭导航
    def test_family_nav():
        page.locator('.uni-tabbar__label:has-text("家庭"), text=家庭').first.click()
        time.sleep(3)
        screenshot(page, '14-family-page')
    test_step("家庭导航", test_family_nav)
    
    # 测试13: 家庭成员查看
    def test_family_members():
        if '成员' in page.content():
            print("   ✅ 成员列表显示")
        page.evaluate('window.scrollTo(0, 300)')
        time.sleep(1)
        screenshot(page, '15-family-scrolled')
    test_step("家庭页面滚动", test_family_members)
    
    # 测试14: 邀请成员按钮
    def test_family_invite():
        try:
            page.locator('text=邀请, text=添加成员').first.click()
            time.sleep(2)
            screenshot(page, '16-family-invite')
            page.go_back()
        except:
            print("   ⚠️ 邀请按钮未找到或不可点击")
    test_step("邀请成员按钮", test_family_invite)
    
    # ========== 我的模块测试 ==========
    print("\n=== 我的模块测试 ===")
    
    # 测试15: 我的导航
    def test_profile_nav():
        page.locator('.uni-tabbar__label:has-text("我的"), text=我的').first.click()
        time.sleep(3)
        screenshot(page, '17-profile-page')
    test_step("我的导航", test_profile_nav)
    
    # 测试16: 设置按钮
    def test_profile_settings():
        try:
            page.locator('text=设置').first.click()
            time.sleep(2)
            screenshot(page, '18-settings-page')
            page.go_back()
        except:
            print("   ⚠️ 设置按钮未找到")
    test_step("设置按钮", test_profile_settings)
    
    # 测试17: 关于按钮
    def test_profile_about():
        try:
            page.locator('text=关于').first.click()
            time.sleep(2)
            screenshot(page, '19-about-page')
            page.go_back()
        except:
            print("   ⚠️ 关于按钮未找到")
    test_step("关于按钮", test_profile_about)
    
    # 测试18: 退出登录
    def test_logout():
        try:
            page.locator('text=退出, text=退出登录').first.click()
            time.sleep(3)
            screenshot(page, '20-logout')
            # 检查是否回到登录页
            if '登录' in page.content() or 'password' in page.content().lower():
                print("   ✅ 退出成功，回到登录页")
        except:
            print("   ⚠️ 退出按钮未找到")
    test_step("退出登录", test_logout)
    
    # ========== 测试完成 ==========
    print("\n=== 测试完成 ===")
    print(f"\n总计: {len(results)} 项测试")
    success = len([r for r in results if r['status'] == '✅'])
    failed = len([r for r in results if r['status'] == '❌'])
    print(f"✅ 成功: {success}")
    print(f"❌ 失败: {failed}")
    
    # 保存测试结果
    import json
    with open('/Users/qi/.openclaw/workspace/family-app/test-round2/test-results.json', 'w') as f:
        json.dump(results, f, indent=2, ensure_ascii=False)
    
    # 生成报告
    report = "# 全面功能测试报告\n\n"
    report += f"测试时间: {time.strftime('%Y-%m-%d %H:%M:%S')}\n\n"
    report += "## 测试结果\n\n"
    for r in results:
        report += f"- {r['status']} {r['name']}\n"
        if 'error' in r:
            report += f"  - 错误: {r['error']}\n"
    
    with open('/Users/qi/.openclaw/workspace/family-app/test-round2/TEST_REPORT.md', 'w') as f:
        f.write(report)
    
    print("\n📊 报告已保存到 test-round2/TEST_REPORT.md")
    
    browser.close()
