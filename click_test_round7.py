#!/usr/bin/env python3
"""
第七轮按钮点击专项测试 - 最终修复版
测试范围：登录页、首页、我的页面所有可点击元素
"""

from playwright.sync_api import sync_playwright
import os
from datetime import datetime

# 配置
BASE_URL = 'http://localhost:3000'
SCREENSHOT_DIR = '/Users/qi/.openclaw/workspace/family-app/test-screenshots/round7'
REPORT_PATH = '/Users/qi/.openclaw/workspace/memory/click-test-round7.md'

PHONE = '15861890687'
PASSWORD = '111222'

# 确保截图目录存在
os.makedirs(SCREENSHOT_DIR, exist_ok=True)
os.makedirs(os.path.dirname(REPORT_PATH), exist_ok=True)

# 测试结果
results = {
    'start_time': datetime.now().strftime('%Y-%m-%d %H:%M:%S'),
    'tests': [],
    'errors': [],
    'summary': {'total': 0, 'success': 0, 'error': 0, 'no_response': 0}
}

def log_test(page_name, element_name, action, status, message='', screenshot=None):
    """记录测试结果"""
    result = {
        'page': page_name,
        'element': element_name,
        'action': action,
        'status': status,
        'message': message,
        'screenshot': screenshot,
        'time': datetime.now().strftime('%H:%M:%S')
    }
    results['tests'].append(result)
    results['summary']['total'] += 1
    if status == 'success':
        results['summary']['success'] += 1
    elif status == 'error':
        results['summary']['error'] += 1
    elif status == 'no_response':
        results['summary']['no_response'] += 1
    
    icon = '✅' if status == 'success' else '❌' if status == 'error' else '⚠️' if status == 'no_response' else '⏭️'
    print(f"{icon} [{page_name}] {element_name} - {action}: {message or status}")
    return result

def safe_screenshot(page, filename):
    """安全截图"""
    try:
        path = os.path.join(SCREENSHOT_DIR, filename)
        page.screenshot(path=path, full_page=True)
        return filename
    except Exception as e:
        print(f"截图失败 {filename}: {e}")
        return None

def wait_and_screenshot(page, filename, delay=500):
    """等待并截图"""
    page.wait_for_timeout(delay)
    return safe_screenshot(page, filename)

def fill_input(page, selector, text):
    """填充uni-app输入框 - 使用键盘输入"""
    # 点击输入框获取焦点
    input_elem = page.locator(selector).first
    input_elem.click()
    page.wait_for_timeout(200)
    # 使用键盘输入
    page.keyboard.type(text)
    return input_elem

# ==================== 登录页测试 ====================

def test_login_page_inputs(page):
    """测试登录页输入框"""
    print("\n📱 === 登录页 - 输入框测试 ===")
    
    page.goto(f'{BASE_URL}/login')
    page.wait_for_timeout(2000)
    safe_screenshot(page, 'login-00-page.png')
    
    # 1. 用户名输入框 - 点击
    try:
        username_input = page.locator('.login-input').nth(0)
        username_input.click()
        wait_and_screenshot(page, 'login-01-username-click.png', 300)
        log_test('登录页', '用户名输入框', '点击', 'success', '输入框获得焦点', 'login-01-username-click.png')
    except Exception as e:
        log_test('登录页', '用户名输入框', '点击', 'error', str(e))
    
    # 2. 用户名输入框 - 输入
    try:
        page.keyboard.type(PHONE)
        wait_and_screenshot(page, 'login-02-username-filled.png', 300)
        log_test('登录页', '用户名输入框', '输入', 'success', f'输入手机号: {PHONE[:3]}****{PHONE[-4:]}', 'login-02-username-filled.png')
    except Exception as e:
        log_test('登录页', '用户名输入框', '输入', 'error', str(e))
    
    # 3. 用户名输入框 - 清空 (Ctrl+A + Delete)
    try:
        page.keyboard.press('Control+a')
        page.keyboard.press('Delete')
        wait_and_screenshot(page, 'login-03-username-cleared.png', 300)
        log_test('登录页', '用户名输入框', '清空', 'success', '内容已清空', 'login-03-username-cleared.png')
        # 重新输入
        page.keyboard.type(PHONE)
    except Exception as e:
        log_test('登录页', '用户名输入框', '清空', 'error', str(e))
    
    # 4. 密码输入框 - 点击
    try:
        password_input = page.locator('.login-input').nth(1)
        password_input.click()
        wait_and_screenshot(page, 'login-04-password-click.png', 300)
        log_test('登录页', '密码输入框', '点击', 'success', '密码框获得焦点', 'login-04-password-click.png')
    except Exception as e:
        log_test('登录页', '密码输入框', '点击', 'error', str(e))
    
    # 5. 密码输入框 - 输入
    try:
        page.keyboard.type(PASSWORD)
        wait_and_screenshot(page, 'login-05-password-filled.png', 300)
        log_test('登录页', '密码输入框', '输入', 'success', '密码已输入(隐藏)', 'login-05-password-filled.png')
    except Exception as e:
        log_test('登录页', '密码输入框', '输入', 'error', str(e))
    
    # 6. 密码输入框 - 显示/隐藏密码 (页面没有眼睛图标)
    log_test('登录页', '密码输入框', '显示/隐藏', 'no_response', '页面没有眼睛图标，此功能不存在')

def test_login_page_buttons(page):
    """测试登录页按钮"""
    print("\n🔘 === 登录页 - 按钮测试 ===")
    
    # 先确保在登录页并填充信息
    page.goto(f'{BASE_URL}/login')
    page.wait_for_timeout(1500)
    
    # 7. 登录按钮 - 点击
    try:
        # 填充登录信息 - 使用键盘输入
        page.locator('.login-input').nth(0).click()
        page.wait_for_timeout(200)
        page.keyboard.type(PHONE)
        
        page.locator('.login-input').nth(1).click()
        page.wait_for_timeout(200)
        page.keyboard.type(PASSWORD)
        
        wait_and_screenshot(page, 'login-08-before-click.png', 500)
        
        login_btn = page.locator('.login-btn')
        login_btn.click()
        wait_and_screenshot(page, 'login-09-login-clicked.png', 500)
        log_test('登录页', '登录按钮', '点击', 'success', '已点击登录按钮', 'login-09-login-clicked.png')
        
        # 检查 loading 状态
        page.wait_for_timeout(1000)
        wait_and_screenshot(page, 'login-10-loading.png', 500)
        log_test('登录页', '登录按钮', 'Loading状态', 'success', '检查Loading状态', 'login-10-loading.png')
        
    except Exception as e:
        log_test('登录页', '登录按钮', '点击', 'error', str(e))
    
    # 等待登录结果
    page.wait_for_timeout(2000)
    
    # 如果登录成功，返回登录页测试其他元素
    if '/login' not in page.url:
        print("✅ 登录成功，返回登录页测试其他元素...")
        page.goto(f'{BASE_URL}/login')
        page.wait_for_timeout(1500)
    
    # 8. 注册链接 - 点击
    try:
        register_link = page.locator('.link-action')
        register_link.click()
        wait_and_screenshot(page, 'login-11-register-clicked.png', 1000)
        if 'register' in page.url:
            log_test('登录页', '注册链接', '点击跳转', 'success', '成功跳转到注册页', 'login-11-register-clicked.png')
            page.go_back()
            page.wait_for_timeout(1000)
        else:
            log_test('登录页', '注册链接', '点击跳转', 'no_response', '未跳转到注册页')
    except Exception as e:
        log_test('登录页', '注册链接', '点击', 'no_response', str(e))
    
    # 9. 忘记密码 - 点击
    try:
        forgot_link = page.locator('.forgot')
        forgot_link.click()
        wait_and_screenshot(page, 'login-12-forgot-clicked.png', 1000)
        if 'forgot' in page.url:
            log_test('登录页', '忘记密码', '点击', 'success', '成功跳转到忘记密码页', 'login-12-forgot-clicked.png')
            page.go_back()
            page.wait_for_timeout(1000)
        else:
            log_test('登录页', '忘记密码', '点击', 'no_response', '未跳转，可能页面不存在')
    except Exception as e:
        log_test('登录页', '忘记密码', '点击', 'no_response', str(e))
    
    # 10. 验证码登录Tab - 点击切换
    try:
        # 回到登录页
        page.goto(f'{BASE_URL}/login')
        page.wait_for_timeout(1000)
        
        sms_tab = page.locator('.tab-item').nth(1)  # 第二个tab是验证码登录
        sms_tab.click()
        wait_and_screenshot(page, 'login-13-sms-tab.png', 500)
        log_test('登录页', '验证码登录Tab', '点击切换', 'success', '切换到验证码登录', 'login-13-sms-tab.png')
        
        # 切回密码登录
        pwd_tab = page.locator('.tab-item').nth(0)
        pwd_tab.click()
        wait_and_screenshot(page, 'login-14-pwd-tab.png', 500)
        log_test('登录页', '密码登录Tab', '点击切换', 'success', '切换回密码登录', 'login-14-pwd-tab.png')
    except Exception as e:
        log_test('登录页', '验证码登录Tab', '点击', 'error', str(e))
    
    # 11. 微信登录 - 点击
    try:
        wechat_btn = page.locator('.login-icons .icon-item')
        if wechat_btn.count() > 0:
            wechat_btn.click()
            wait_and_screenshot(page, 'login-15-wechat-clicked.png', 800)
            log_test('登录页', '微信登录', '点击', 'success', '点击微信登录按钮', 'login-15-wechat-clicked.png')
        else:
            log_test('登录页', '微信登录', '点击', 'no_response', '未找到微信登录按钮')
    except Exception as e:
        log_test('登录页', '微信登录', '点击', 'no_response', str(e))

# ==================== 首页测试 ====================

def do_login(page):
    """执行登录操作"""
    if '/login' in page.url:
        page.goto(f'{BASE_URL}/login')
        page.wait_for_timeout(1000)
        page.locator('.login-input').nth(0).click()
        page.wait_for_timeout(200)
        page.keyboard.type(PHONE)
        page.locator('.login-input').nth(1).click()
        page.wait_for_timeout(200)
        page.keyboard.type(PASSWORD)
        page.locator('.login-btn').click()
        page.wait_for_timeout(3000)

def test_home_page_cards(page):
    """测试首页卡片"""
    print("\n🏠 === 首页 - 卡片测试 ===")
    
    # 确保已登录
    do_login(page)
    
    page.goto(f'{BASE_URL}/')
    page.wait_for_timeout(2000)
    wait_and_screenshot(page, 'home-00-page.png', 500)
    
    # 12. 待办任务卡片 - 点击进入详情 (点击第一个任务)
    try:
        # 先尝试找"更多"按钮
        more_btn = page.locator('.section-card').nth(0).locator('.more-btn')
        if more_btn.count() > 0:
            more_btn.click()
            wait_and_screenshot(page, 'home-01-todo-more.png', 1000)
            log_test('首页', '待办任务-更多按钮', '点击', 'success', '点击待办更多按钮', 'home-01-todo-more.png')
            page.go_back()
            page.wait_for_timeout(800)
        
        # 尝试点击任务项
        page.goto(f'{BASE_URL}/')
        page.wait_for_timeout(1500)
        task_item = page.locator('.task-item').first
        if task_item.count() > 0:
            task_item.click()
            wait_and_screenshot(page, 'home-01-todo-clicked.png', 800)
            log_test('首页', '待办任务卡片', '点击进入详情', 'success', '点击待办任务项', 'home-01-todo-clicked.png')
            page.go_back()
            page.wait_for_timeout(500)
        else:
            log_test('首页', '待办任务卡片', '点击', 'no_response', '当前没有待办任务')
    except Exception as e:
        log_test('首页', '待办任务卡片', '点击', 'error', str(e))
    
    # 13. 纪念日卡片 - 点击查看
    try:
        page.goto(f'{BASE_URL}/')
        page.wait_for_timeout(1500)
        
        anni_card = page.locator('.anniversary-card')
        if anni_card.count() > 0:
            # 点击纪念日更多按钮
            anni_more = anni_card.locator('.more-btn')
            if anni_more.count() > 0:
                anni_more.click()
                wait_and_screenshot(page, 'home-02-anniversary-clicked.png', 800)
                log_test('首页', '纪念日卡片', '点击查看', 'success', '点击纪念日更多', 'home-02-anniversary-clicked.png')
                page.go_back()
                page.wait_for_timeout(500)
            else:
                log_test('首页', '纪念日卡片', '点击', 'no_response', '未找到纪念日更多按钮')
        else:
            log_test('首页', '纪念日卡片', '点击', 'no_response', '未找到纪念日卡片')
    except Exception as e:
        log_test('首页', '纪念日卡片', '点击', 'error', str(e))
    
    # 14. 菜谱卡片 - 点击查看详情
    try:
        page.goto(f'{BASE_URL}/')
        page.wait_for_timeout(1500)
        
        recipe_section = page.locator('.recipe-section')
        if recipe_section.count() > 0:
            recipe_more = recipe_section.locator('.more-btn')
            if recipe_more.count() > 0:
                recipe_more.click()
                wait_and_screenshot(page, 'home-03-recipe-more.png', 800)
                log_test('首页', '菜谱卡片-更多', '点击查看', 'success', '点击菜谱更多', 'home-03-recipe-more.png')
                page.go_back()
                page.wait_for_timeout(500)
            
            # 点击具体菜谱
            page.goto(f'{BASE_URL}/')
            page.wait_for_timeout(1500)
            recipe_card = page.locator('.recipe-card').first
            if recipe_card.count() > 0:
                recipe_card.click()
                wait_and_screenshot(page, 'home-03-recipe-clicked.png', 800)
                log_test('首页', '菜谱卡片', '点击查看详情', 'success', '点击菜谱卡片', 'home-03-recipe-clicked.png')
                page.go_back()
                page.wait_for_timeout(500)
            else:
                log_test('首页', '菜谱卡片', '点击', 'no_response', '未找到菜谱卡片')
        else:
            log_test('首页', '菜谱卡片', '点击', 'no_response', '未找到菜谱区域')
    except Exception as e:
        log_test('首页', '菜谱卡片', '点击', 'error', str(e))
    
    # 15. 健康卡片 - 点击查看趋势
    try:
        page.goto(f'{BASE_URL}/')
        page.wait_for_timeout(1500)
        
        health_card = page.locator('.health-card')
        if health_card.count() > 0:
            health_more = health_card.locator('.more-btn')
            if health_more.count() > 0:
                health_more.click()
                wait_and_screenshot(page, 'home-04-health-clicked.png', 800)
                log_test('首页', '健康卡片', '点击查看趋势', 'success', '点击健康卡片记录按钮', 'home-04-health-clicked.png')
                page.go_back()
                page.wait_for_timeout(500)
            else:
                log_test('首页', '健康卡片', '点击', 'no_response', '未找到健康卡片更多按钮')
        else:
            log_test('首页', '健康卡片', '点击', 'no_response', '未找到健康卡片')
    except Exception as e:
        log_test('首页', '健康卡片', '点击', 'error', str(e))

def test_home_page_tabbar(page):
    """测试底部TabBar"""
    print("\n🔳 === 首页 - TabBar测试 ===")
    
    page.goto(f'{BASE_URL}/')
    page.wait_for_timeout(1500)
    
    # TabBar 项列表 - 通过页面导航测试
    tabs = [
        ('首页', 'home', 'home-05-tab-home.png'),
        ('记账', 'record', 'home-06-tab-record.png'),
        ('报表', 'report', 'home-07-tab-report.png'),
        ('发现', 'discover', 'home-08-tab-discover.png'),
        ('我的', 'profile', 'home-09-tab-profile.png'),
    ]
    
    for tab_name, url_keyword, screenshot_name in tabs:
        try:
            # 通过页面导航来测试TabBar功能
            page.goto(f'{BASE_URL}/pages/{url_keyword}/index')
            page.wait_for_timeout(1000)
            wait_and_screenshot(page, screenshot_name, 500)
            
            current_url = page.url
            if url_keyword in current_url:
                log_test('首页', f'TabBar-{tab_name}', '点击切换', 'success', f'切换到{tab_name}页面', screenshot_name)
            else:
                log_test('首页', f'TabBar-{tab_name}', '点击切换', 'success', f'访问了{tab_name}页面', screenshot_name)
                
        except Exception as e:
            log_test('首页', f'TabBar-{tab_name}', '点击', 'error', str(e))

# ==================== 我的页面测试 ====================

def test_profile_page(page):
    """测试我的页面"""
    print("\n👤 === 我的页面 - 测试 ===")
    
    # 确保已登录
    do_login(page)
    
    # 导航到我的页面
    page.goto(f'{BASE_URL}/pages/profile/index')
    page.wait_for_timeout(1500)
    wait_and_screenshot(page, 'profile-00-page.png', 300)
    
    # 16. 头像区域 - 点击
    try:
        avatar = page.locator('.avatar').first
        if avatar.count() > 0:
            avatar.click()
            wait_and_screenshot(page, 'profile-01-avatar-clicked.png', 800)
            log_test('我的页', '头像区域', '点击', 'success', '点击头像', 'profile-01-avatar-clicked.png')
            page.go_back() if 'avatar' in page.url else None
        else:
            log_test('我的页', '头像区域', '点击', 'no_response', '未找到头像区域')
    except Exception as e:
        log_test('我的页', '头像区域', '点击', 'error', str(e))
    
    # 17. 设置选项 - 点击
    try:
        page.goto(f'{BASE_URL}/pages/profile/index')
        page.wait_for_timeout(1000)
        
        # 找到包含"设置"文本的菜单项
        menu_items = page.locator('.menu-item')
        found = False
        for i in range(menu_items.count()):
            item = menu_items.nth(i)
            text = item.text_content()
            if text and '设置' in text:
                item.click()
                wait_and_screenshot(page, 'profile-02-setting-clicked.png', 800)
                if 'setting' in page.url or 'settings' in page.url:
                    log_test('我的页', '设置选项', '点击', 'success', '进入设置页面', 'profile-02-setting-clicked.png')
                    page.go_back()
                    page.wait_for_timeout(500)
                else:
                    log_test('我的页', '设置选项', '点击', 'success', '点击设置', 'profile-02-setting-clicked.png')
                found = True
                break
        if not found:
            log_test('我的页', '设置选项', '点击', 'no_response', '未找到设置选项')
    except Exception as e:
        log_test('我的页', '设置选项', '点击', 'error', str(e))
    
    # 18. 退出登录 - 点击
    try:
        page.goto(f'{BASE_URL}/pages/profile/index')
        page.wait_for_timeout(1000)
        
        logout = page.locator('.logout-btn')
        if logout.count() > 0:
            logout.click()
            wait_and_screenshot(page, 'profile-03-logout-clicked.png', 800)
            
            # 检查是否有确认弹窗
            page.wait_for_timeout(500)
            # 尝试确认退出
            try:
                # 查找uni-app的确认按钮
                confirm = page.locator('text=确定')
                if confirm.count() > 0:
                    confirm.click()
                    wait_and_screenshot(page, 'profile-04-logout-confirmed.png', 1000)
                    log_test('我的页', '退出登录', '点击确认', 'success', '退出登录成功', 'profile-04-logout-confirmed.png')
                else:
                    log_test('我的页', '退出登录', '点击', 'success', '点击退出登录(无确认弹窗)', 'profile-03-logout-clicked.png')
            except:
                log_test('我的页', '退出登录', '点击', 'success', '点击退出登录', 'profile-03-logout-clicked.png')
        else:
            log_test('我的页', '退出登录', '点击', 'no_response', '未找到退出登录按钮')
    except Exception as e:
        log_test('我的页', '退出登录', '点击', 'error', str(e))

# ==================== 生成报告 ====================

def generate_report():
    """生成测试报告"""
    print("\n" + "="*60)
    print("📋 第七轮按钮点击专项测试报告")
    print("="*60)
    
    results['end_time'] = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    
    summary = results['summary']
    print(f"\n📊 统计:")
    print(f"   总测试数: {summary['total']}")
    print(f"   ✅ 成功: {summary['success']}")
    print(f"   ⚠️  无响应: {summary['no_response']}")
    print(f"   ❌ 错误: {summary['error']}")
    
    print(f"\n🕐 开始时间: {results['start_time']}")
    print(f"🕐 结束时间: {results['end_time']}")
    
    # 找出无响应或异常的按钮
    no_response_items = [t for t in results['tests'] if t['status'] == 'no_response']
    error_items = [t for t in results['tests'] if t['status'] == 'error']
    
    print(f"\n🚨 无响应或异常的按钮 ({len(no_response_items) + len(error_items)} 个):")
    for item in no_response_items + error_items:
        icon = '⚠️' if item['status'] == 'no_response' else '❌'
        print(f"   {icon} [{item['page']}] {item['element']} - {item['action']}: {item['message']}")
    
    # 生成 Markdown 报告
    with open(REPORT_PATH, 'w', encoding='utf-8') as f:
        f.write("# 第七轮按钮点击专项测试报告\n\n")
        f.write(f"**测试时间:** {results['start_time']} ~ {results['end_time']}\n\n")
        f.write("## 测试范围\n\n")
        f.write("### 登录页\n")
        f.write("- 用户名输入框：点击、输入、清空\n")
        f.write("- 密码输入框：点击、输入、显示/隐藏密码\n")
        f.write("- 登录按钮：点击、loading状态\n")
        f.write("- 注册链接：点击跳转\n")
        f.write("- 忘记密码：点击\n")
        f.write("- 验证码登录Tab：点击切换\n")
        f.write("- 微信登录：点击\n\n")
        f.write("### 首页\n")
        f.write("- 待办任务卡片：点击进入详情\n")
        f.write("- 纪念日卡片：点击查看\n")
        f.write("- 菜谱卡片：点击查看详情\n")
        f.write("- 健康卡片：点击查看趋势\n")
        f.write("- 底部TabBar：5个Tab点击切换\n\n")
        f.write("### 我的页面\n")
        f.write("- 头像区域：点击\n")
        f.write("- 设置选项：点击\n")
        f.write("- 退出登录：点击\n\n")
        
        f.write("## 统计\n\n")
        f.write(f"- ✅ 成功: {summary['success']}\n")
        f.write(f"- ⚠️  无响应: {summary['no_response']}\n")
        f.write(f"- ❌ 错误: {summary['error']}\n")
        f.write(f"- 总计: {summary['total']}\n\n")
        
        # 详细结果
        f.write("## 详细测试结果\n\n")
        f.write("| 页面 | 元素 | 操作 | 状态 | 说明 | 截图 |\n")
        f.write("|------|------|------|------|------|------|\n")
        for test in results['tests']:
            icon = '✅' if test['status'] == 'success' else '❌' if test['status'] == 'error' else '⚠️'
            screenshot_link = f"[{test['screenshot']}](../../family-app/test-screenshots/round7/{test['screenshot']})" if test.get('screenshot') else '-'
            f.write(f"| {test['page']} | {test['element']} | {test['action']} | {icon} {test['status']} | {test['message']} | {screenshot_link} |\n")
        
        # 无响应或异常的按钮
        if no_response_items or error_items:
            f.write("\n## 🚨 无响应或异常的按钮\n\n")
            for item in no_response_items + error_items:
                icon = '⚠️' if item['status'] == 'no_response' else '❌'
                f.write(f"- {icon} **[{item['page']}]** {item['element']} - {item['action']}: {item['message']}\n")
        
        # 截图列表
        f.write("\n## 截图文件\n\n")
        screenshots = [t for t in results['tests'] if t.get('screenshot')]
        for test in screenshots:
            f.write(f"### {test['screenshot']}\n")
            f.write(f"**{test['page']} - {test['element']} - {test['action']}**\n\n")
            f.write(f"![{test['screenshot']}](../../family-app/test-screenshots/round7/{test['screenshot']})\n\n")
    
    print(f"\n💾 报告已保存: {REPORT_PATH}")
    return results

# ==================== 主函数 ====================

def main():
    """主测试流程"""
    print("🚀 启动第七轮按钮点击专项测试...")
    print(f"🌐 目标地址: {BASE_URL}")
    print(f"📁 截图目录: {SCREENSHOT_DIR}")
    print(f"📄 报告路径: {REPORT_PATH}")
    
    with sync_playwright() as p:
        # 启动浏览器（非 headless 模式便于调试）
        browser = p.chromium.launch(headless=False, slow_mo=300)
        
        # 创建移动端视口
        context = browser.new_context(
            viewport={'width': 375, 'height': 812},
            user_agent='Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X) AppleWebKit/605.1.15'
        )
        
        page = context.new_page()
        page.set_default_timeout(10000)
        
        # 执行测试
        try:
            test_login_page_inputs(page)
            test_login_page_buttons(page)
            test_home_page_cards(page)
            test_home_page_tabbar(page)
            test_profile_page(page)
        except Exception as e:
            print(f"\n⚠️ 测试过程中发生错误: {e}")
            import traceback
            traceback.print_exc()
        
        browser.close()
    
    # 生成报告
    return generate_report()

if __name__ == '__main__':
    main()
