from playwright.sync_api import sync_playwright
import time
import os

# 截图保存路径
SCREENSHOT_DIR = "/Users/qi/.openclaw/workspace/family-app/test-screenshots"

# 测试报告
test_report = []

def log(message):
    """记录测试日志"""
    print(f"[测试] {message}")
    test_report.append(message)

def screenshot(page, filename):
    """保存截图"""
    path = os.path.join(SCREENSHOT_DIR, filename)
    page.screenshot(path=path, full_page=True)
    log(f"截图已保存: {filename}")
    return path

def test_login(page):
    """测试登录功能"""
    log("=" * 50)
    log("开始登录测试")
    log("=" * 50)
    
    # 访问登录页面
    page.goto('http://localhost:3000')
    page.wait_for_load_state('networkidle')
    time.sleep(1)
    
    # 截图：登录页面
    screenshot(page, 'login-page.png')
    log("✓ 已访问登录页面")
    
    try:
        # 输入手机号
        phone_input = page.locator('input[type="tel"], input[placeholder*="手机"], input[placeholder*="phone"]').first
        if phone_input.count() > 0:
            phone_input.fill('15861890687')
            log("✓ 已输入手机号")
        else:
            # 尝试其他选择器
            inputs = page.locator('input').all()
            for inp in inputs:
                placeholder = inp.get_attribute('placeholder') or ''
                if '手机' in placeholder or 'phone' in placeholder.lower() or '账号' in placeholder:
                    inp.fill('15861890687')
                    log("✓ 已输入手机号")
                    break
        
        # 输入密码
        password_input = page.locator('input[type="password"]').first
        if password_input.count() > 0:
            password_input.fill('111222')
            log("✓ 已输入密码")
        else:
            inputs = page.locator('input').all()
            for inp in inputs:
                placeholder = inp.get_attribute('placeholder') or ''
                if '密码' in placeholder or 'password' in placeholder.lower():
                    inp.fill('111222')
                    log("✓ 已输入密码")
                    break
        
        # 点击登录按钮
        login_btn = page.locator('button:has-text("登录"), button:has-text("Login"), .login-btn, [type="submit"]').first
        if login_btn.count() > 0:
            login_btn.click()
            log("✓ 已点击登录按钮")
        else:
            # 尝试找包含登录文本的元素
            buttons = page.locator('button').all()
            for btn in buttons:
                text = btn.inner_text()
                if '登录' in text or 'Login' in text:
                    btn.click()
                    log("✓ 已点击登录按钮")
                    break
        
        # 等待跳转
        time.sleep(3)
        
        # 截图：登录后
        screenshot(page, 'login-success.png')
        
        # 检查是否登录成功（URL变化或页面元素）
        current_url = page.url
        log(f"当前URL: {current_url}")
        
        if '/login' not in current_url and current_url != 'http://localhost:3000':
            log("✓ 登录成功，已跳转到首页")
            return True
        else:
            # 检查是否有错误提示
            error_msg = page.locator('.error, .toast, .message').first
            if error_msg.count() > 0 and error_msg.is_visible():
                log(f"✗ 登录失败: {error_msg.inner_text()}")
            else:
                log("? 登录状态不确定，请检查截图")
            return False
            
    except Exception as e:
        log(f"✗ 登录测试出错: {str(e)}")
        screenshot(page, 'login-error.png')
        return False

def test_home_page(page):
    """测试首页"""
    log("=" * 50)
    log("开始首页测试")
    log("=" * 50)
    
    try:
        # 截图首页
        screenshot(page, 'home-page.png')
        
        # 检查页面元素
        checks = {
            '天气组件': False,
            '任务列表': False,
            '底部导航': False
        }
        
        # 检查天气组件
        weather_selectors = ['.weather', '[class*="weather"]', '.forecast', '.temperature']
        for selector in weather_selectors:
            if page.locator(selector).count() > 0:
                checks['天气组件'] = True
                break
        
        # 检查任务列表
        task_selectors = ['.task-list', '[class*="task"]', '.todo-list', '.mission-list']
        for selector in task_selectors:
            if page.locator(selector).count() > 0:
                checks['任务列表'] = True
                break
        
        # 检查底部导航
        nav_selectors = ['.tab-bar', '.bottom-nav', '[class*="nav"]', 'nav', '.footer']
        for selector in nav_selectors:
            if page.locator(selector).count() > 0:
                checks['底部导航'] = True
                break
        
        for item, found in checks.items():
            status = "✓" if found else "✗"
            log(f"{status} {item}: {'找到' if found else '未找到'}")
        
        # 尝试点击快捷入口
        shortcuts = page.locator('.shortcut, .quick-entry, [class*="shortcut"], [class*="quick"]').all()
        log(f"找到 {len(shortcuts)} 个快捷入口")
        
        for i, shortcut in enumerate(shortcuts[:5]):  # 最多测试5个
            try:
                text = shortcut.inner_text()[:20] if shortcut.is_visible() else '未知'
                shortcut.click()
                time.sleep(1)
                log(f"✓ 快捷入口 {i+1} ({text}): 可点击")
                page.go_back()
                time.sleep(1)
            except Exception as e:
                log(f"✗ 快捷入口 {i+1}: 点击失败 - {str(e)}")
        
        return True
        
    except Exception as e:
        log(f"✗ 首页测试出错: {str(e)}")
        screenshot(page, 'home-error.png')
        return False

def test_task_page(page):
    """测试任务页面"""
    log("=" * 50)
    log("开始任务页面测试")
    log("=" * 50)
    
    try:
        # 点击任务导航
        task_nav = page.locator('.tab-bar >> text=任务, nav >> text=任务, [class*="nav"] >> text=任务, button:has-text("任务")').first
        if task_nav.count() == 0:
            # 尝试其他选择器
            links = page.locator('a, button').all()
            for link in links:
                text = link.inner_text()
                if '任务' in text or 'Task' in text:
                    link.click()
                    log("✓ 已点击任务导航")
                    break
        else:
            task_nav.click()
            log("✓ 已点击任务导航")
        
        time.sleep(2)
        screenshot(page, 'task-page.png')
        
        # 检查任务列表
        task_items = page.locator('.task-item, [class*="task-item"], .list-item').count()
        log(f"✓ 任务列表显示，找到 {task_items} 个任务项")
        
        # 尝试点击新增任务按钮
        add_btn = page.locator('button:has-text("新增"), button:has-text("添加"), button:has-text("+"), .add-btn, [class*="add"]').first
        if add_btn.count() > 0:
            add_btn.click()
            log("✓ 已点击新增任务按钮")
            time.sleep(1)
            screenshot(page, 'task-add-form.png')
            
            # 尝试填写表单
            inputs = page.locator('input').all()
            if len(inputs) > 0:
                inputs[0].fill('测试任务')
                log("✓ 已填写任务标题")
            
            # 查找提交按钮
            submit_btn = page.locator('button[type="submit"], button:has-text("提交"), button:has-text("保存"), button:has-text("确定")').first
            if submit_btn.count() > 0:
                submit_btn.click()
                log("✓ 已点击提交按钮")
                time.sleep(1)
                screenshot(page, 'task-add-result.png')
        else:
            log("✗ 未找到新增任务按钮")
        
        return True
        
    except Exception as e:
        log(f"✗ 任务页面测试出错: {str(e)}")
        screenshot(page, 'task-error.png')
        return False

def test_user_page(page):
    """测试用户页面"""
    log("=" * 50)
    log("开始用户页面测试")
    log("=" * 50)
    
    try:
        # 点击我的导航
        user_nav = page.locator('.tab-bar >> text=我的, nav >> text=我的, [class*="nav"] >> text=我的, button:has-text("我的")').first
        if user_nav.count() == 0:
            links = page.locator('a, button').all()
            for link in links:
                text = link.inner_text()
                if '我的' in text or 'Me' in text or 'User' in text or '我' in text:
                    link.click()
                    log("✓ 已点击我的导航")
                    break
        else:
            user_nav.click()
            log("✓ 已点击我的导航")
        
        time.sleep(2)
        screenshot(page, 'user-page.png')
        
        # 检查用户信息
        user_info = page.locator('.user-info, [class*="user"], .profile, .avatar').count()
        log(f"✓ 找到 {user_info} 个用户信息相关元素")
        
        # 点击设置菜单
        settings = page.locator('text=设置, text=Settings, .settings, [class*="setting"]').first
        if settings.count() > 0:
            settings.click()
            log("✓ 已点击设置菜单")
            time.sleep(1)
            screenshot(page, 'settings-page.png')
        else:
            log("✗ 未找到设置菜单")
        
        # 测试退出登录
        logout = page.locator('text=退出, text=登出, text=Logout, .logout, [class*="logout"]').first
        if logout.count() > 0:
            logout.click()
            log("✓ 已点击退出登录")
            time.sleep(2)
            screenshot(page, 'logout-result.png')
        else:
            log("✗ 未找到退出登录按钮")
        
        return True
        
    except Exception as e:
        log(f"✗ 用户页面测试出错: {str(e)}")
        screenshot(page, 'user-error.png')
        return False

def test_family_page(page):
    """测试家庭页面"""
    log("=" * 50)
    log("开始家庭页面测试")
    log("=" * 50)
    
    try:
        # 点击家庭导航
        family_nav = page.locator('.tab-bar >> text=家庭, nav >> text=家庭, [class*="nav"] >> text=家庭, button:has-text("家庭")').first
        if family_nav.count() == 0:
            links = page.locator('a, button').all()
            for link in links:
                text = link.inner_text()
                if '家庭' in text or 'Family' in text or '家' in text:
                    link.click()
                    log("✓ 已点击家庭导航")
                    break
            else:
                log("✗ 未找到家庭导航，可能该功能不存在")
                return False
        else:
            family_nav.click()
            log("✓ 已点击家庭导航")
        
        time.sleep(2)
        screenshot(page, 'family-page.png')
        
        # 检查家庭页面功能
        family_elements = page.locator('.family, [class*="family"], .member, [class*="member"]').count()
        log(f"✓ 找到 {family_elements} 个家庭相关元素")
        
        return True
        
    except Exception as e:
        log(f"✗ 家庭页面测试出错: {str(e)}")
        screenshot(page, 'family-error.png')
        return False

def main():
    """主测试函数"""
    log("开始家庭应用自动化测试")
    log(f"截图保存目录: {SCREENSHOT_DIR}")
    
    with sync_playwright() as p:
        # 启动浏览器（非无头模式以便观察）
        browser = p.chromium.launch(headless=False, slow_mo=100)
        page = browser.new_page(viewport={'width': 1280, 'height': 800})
        
        try:
            # 1. 登录测试
            login_success = test_login(page)
            
            if login_success:
                # 2. 首页测试
                test_home_page(page)
                
                # 3. 任务页面测试
                test_task_page(page)
                
                # 4. 用户页面测试
                test_user_page(page)
                
                # 5. 家庭页面测试
                test_family_page(page)
            else:
                log("登录失败，跳过后续测试")
            
        except Exception as e:
            log(f"测试过程中发生错误: {str(e)}")
        
        finally:
            # 保存测试报告
            report_path = os.path.join(SCREENSHOT_DIR, 'test-report.txt')
            with open(report_path, 'w', encoding='utf-8') as f:
                f.write('\n'.join(test_report))
            log(f"测试报告已保存: {report_path}")
            
            # 关闭浏览器
            browser.close()
            log("测试完成，浏览器已关闭")

if __name__ == '__main__':
    main()
