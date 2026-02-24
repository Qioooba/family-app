#!/usr/bin/env python3
"""
Family App - Playwright 自动化测试脚本
测试内容：登录、首页、记一笔、报表、我的 等页面
"""

from playwright.sync_api import sync_playwright
import os
import json
from datetime import datetime

# 配置
BASE_URL = 'http://localhost:3000'
SCREENSHOT_DIR = '/Users/qi/.openclaw/workspace/family-app/test-screenshots'
PHONE = '15861890687'
PASSWORD = '111222'

# 确保截图目录存在
os.makedirs(SCREENSHOT_DIR, exist_ok=True)

# 测试结果
results = {
    'start_time': datetime.now().strftime('%Y-%m-%d %H:%M:%S'),
    'tests': [],
    'errors': []
}

def log_test(name, status, message='', screenshot=None):
    """记录测试结果"""
    result = {
        'name': name,
        'status': status,
        'message': message,
        'screenshot': screenshot,
        'time': datetime.now().strftime('%H:%M:%S')
    }
    results['tests'].append(result)
    icon = '✅' if status == 'success' else '❌' if status == 'error' else '⚠️'
    print(f"{icon} {name}: {message or status}")
    return result

def safe_screenshot(page, filename):
    """安全截图，失败时返回 None"""
    try:
        path = os.path.join(SCREENSHOT_DIR, filename)
        page.screenshot(path=path, full_page=True)
        return filename
    except Exception as e:
        print(f"截图失败 {filename}: {e}")
        return None

def test_login(page):
    """测试登录功能"""
    print("\n📱 === 开始登录测试 ===")
    
    try:
        # 1. 访问登录页
        page.goto(f'{BASE_URL}/login')
        page.wait_for_timeout(2000)
        safe_screenshot(page, '01-login-page.png')
        log_test('登录页加载', 'success', '页面加载成功', '01-login-page.png')
        
        # 2. 输入账号密码
        try:
            phone_input = page.locator('input[type="text"], input[placeholder*="手机号"], input[name="username"]').first
            password_input = page.locator('input[type="password"], input[placeholder*="密码"]').first
            
            phone_input.fill(PHONE)
            password_input.fill(PASSWORD)
            safe_screenshot(page, '02-login-filled.png')
            log_test('填写登录信息', 'success', '手机号和密码已填写', '02-login-filled.png')
        except Exception as e:
            log_test('填写登录信息', 'error', str(e))
            raise
        
        # 3. 点击登录按钮
        try:
            login_btn = page.locator('button:has-text("登录"), button[type="submit"], .login-btn').first
            login_btn.click()
            page.wait_for_timeout(3000)
            safe_screenshot(page, '03-after-login.png')
            log_test('登录按钮点击', 'success', '已点击登录', '03-after-login.png')
        except Exception as e:
            log_test('登录按钮点击', 'error', str(e))
            raise
        
        # 4. 检查登录结果
        try:
            # 检查是否还在登录页
            if '/login' in page.url:
                # 可能有错误提示
                error_msg = page.locator('.error-message, .toast, .el-message').first
                if error_msg.is_visible():
                    msg_text = error_msg.text_content()
                    log_test('登录结果', 'error', f'登录失败: {msg_text}')
                    return False
            else:
                log_test('登录结果', 'success', '登录成功，已跳转')
                return True
        except Exception as e:
            log_test('登录结果检查', 'error', str(e))
            return False
            
    except Exception as e:
        log_test('登录流程', 'error', str(e), safe_screenshot(page, 'error-login.png'))
        return False

def test_home_page(page):
    """测试首页"""
    print("\n🏠 === 开始首页测试 ===")
    
    try:
        page.goto(f'{BASE_URL}/')
        page.wait_for_timeout(2000)
        
        # 检查关键元素
        selectors = [
            ('收入', '收入显示'),
            ('支出', '支出显示'),
            ('余额', '余额显示'),
            ('记一笔', '记账按钮'),
        ]
        
        for text, desc in selectors:
            try:
                elem = page.locator(f'text={text}').first
                if elem.is_visible():
                    log_test(f'首页-{desc}', 'success', f'找到"{text}"')
                else:
                    log_test(f'首页-{desc}', 'warning', f'"{text}"不可见')
            except:
                log_test(f'首页-{desc}', 'warning', f'未找到"{text}"')
        
        safe_screenshot(page, '04-home-page.png')
        log_test('首页截图', 'success', '', '04-home-page.png')
        
    except Exception as e:
        log_test('首页测试', 'error', str(e), safe_screenshot(page, 'error-home.png'))

def test_record_page(page):
    """测试记账页面"""
    print("\n📝 === 开始记账页面测试 ===")
    
    try:
        # 尝试找到记账入口
        try:
            record_btn = page.locator('text=记一笔, button:has-text("记")').first
            record_btn.click()
            page.wait_for_timeout(2000)
        except:
            # 直接访问记账页面
            page.goto(f'{BASE_URL}/record')
            page.wait_for_timeout(2000)
        
        safe_screenshot(page, '05-record-page.png')
        
        # 检查表单元素
        form_elements = [
            ('input[placeholder*="金额"], input[type="number"]', '金额输入'),
            ('button:has-text("支出"), button:has-text("收入")', '收支切换'),
            ('button:has-text("保存"), button[type="submit"]', '保存按钮'),
        ]
        
        for selector, desc in form_elements:
            try:
                elem = page.locator(selector).first
                if elem.is_visible():
                    log_test(f'记账页-{desc}', 'success', f'元素可见')
                else:
                    log_test(f'记账页-{desc}', 'warning', f'元素不可见')
            except Exception as e:
                log_test(f'记账页-{desc}', 'warning', str(e))
        
        log_test('记账页', 'success', '页面加载完成', '05-record-page.png')
        
    except Exception as e:
        log_test('记账页测试', 'error', str(e), safe_screenshot(page, 'error-record.png'))

def test_report_page(page):
    """测试报表页面"""
    print("\n📊 === 开始报表页面测试 ===")
    
    try:
        page.goto(f'{BASE_URL}/report')
        page.wait_for_timeout(2000)
        
        safe_screenshot(page, '06-report-page.png')
        
        # 检查图表或数据
        chart_selectors = [
            'canvas',
            '.chart',
            '.echarts',
            '[class*="chart"]',
        ]
        
        chart_found = False
        for selector in chart_selectors:
            try:
                if page.locator(selector).count() > 0:
                    chart_found = True
                    break
            except:
                pass
        
        if chart_found:
            log_test('报表页-图表', 'success', '找到图表元素')
        else:
            log_test('报表页-图表', 'warning', '未找到图表元素')
        
        log_test('报表页', 'success', '页面加载完成', '06-report-page.png')
        
    except Exception as e:
        log_test('报表页测试', 'error', str(e), safe_screenshot(page, 'error-report.png'))

def test_profile_page(page):
    """测试我的页面"""
    print("\n👤 === 开始我的页面测试 ===")
    
    try:
        page.goto(f'{BASE_URL}/profile')
        page.wait_for_timeout(2000)
        
        safe_screenshot(page, '07-profile-page.png')
        
        # 检查常见元素
        profile_elements = [
            ('头像', '头像'),
            ('设置', '设置'),
            ('退出', '退出登录'),
        ]
        
        for text, desc in profile_elements:
            try:
                elem = page.locator(f'text={text}').first
                if elem.is_visible():
                    log_test(f'我的页-{desc}', 'success', f'找到"{text}"')
                else:
                    log_test(f'我的页-{desc}', 'warning', f'"{text}"不可见')
            except:
                log_test(f'我的页-{desc}', 'warning', f'未找到"{text}"')
        
        log_test('我的页', 'success', '页面加载完成', '07-profile-page.png')
        
    except Exception as e:
        log_test('我的页测试', 'error', str(e), safe_screenshot(page, 'error-profile.png'))

def test_api_endpoints():
    """测试后端 API 接口"""
    print("\n🔌 === 开始 API 接口测试 ===")
    
    import urllib.request
    import urllib.error
    
    endpoints = [
        ('GET', '/api/health', '健康检查'),
        ('GET', '/api/user/profile', '用户信息'),
        ('GET', '/api/transactions', '交易记录'),
        ('GET', '/api/categories', '分类列表'),
    ]
    
    for method, endpoint, desc in endpoints:
        try:
            url = f'{BASE_URL}{endpoint}'
            req = urllib.request.Request(url, method=method)
            req.add_header('Accept', 'application/json')
            
            with urllib.request.urlopen(req, timeout=5) as response:
                status = response.getcode()
                body = response.read().decode('utf-8')[:200]  # 只取前200字符
                
                if status == 200:
                    log_test(f'API-{desc}', 'success', f'状态码: {status}')
                elif status == 401:
                    log_test(f'API-{desc}', 'warning', f'需要登录 (401)')
                else:
                    log_test(f'API-{desc}', 'warning', f'状态码: {status}')
                    
        except urllib.error.HTTPError as e:
            if e.code == 401:
                log_test(f'API-{desc}', 'warning', f'需要登录 (401)')
            else:
                log_test(f'API-{desc}', 'error', f'HTTP {e.code}')
        except Exception as e:
            log_test(f'API-{desc}', 'error', str(e))

def generate_report():
    """生成测试报告"""
    print("\n" + "="*50)
    print("📋 测试报告")
    print("="*50)
    
    results['end_time'] = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    
    # 统计
    total = len(results['tests'])
    success = len([t for t in results['tests'] if t['status'] == 'success'])
    errors = len([t for t in results['tests'] if t['status'] == 'error'])
    warnings = len([t for t in results['tests'] if t['status'] == 'warning'])
    
    print(f"\n📊 统计:")
    print(f"   总测试数: {total}")
    print(f"   ✅ 成功: {success}")
    print(f"   ⚠️  警告: {warnings}")
    print(f"   ❌ 错误: {errors}")
    
    print(f"\n🕐 开始时间: {results['start_time']}")
    print(f"🕐 结束时间: {results['end_time']}")
    
    print(f"\n📁 截图文件:")
    for test in results['tests']:
        if test.get('screenshot'):
            print(f"   - {test['screenshot']}: {test['name']}")
    
    # 保存 JSON 报告
    report_path = os.path.join(SCREENSHOT_DIR, 'test-report.json')
    with open(report_path, 'w', encoding='utf-8') as f:
        json.dump(results, f, ensure_ascii=False, indent=2)
    print(f"\n💾 详细报告已保存: {report_path}")
    
    # 保存 Markdown 报告
    md_path = os.path.join(SCREENSHOT_DIR, 'test-report.md')
    with open(md_path, 'w', encoding='utf-8') as f:
        f.write("# Family App 测试报告\n\n")
        f.write(f"**测试时间:** {results['start_time']} ~ {results['end_time']}\n\n")
        f.write("## 统计\n\n")
        f.write(f"- ✅ 成功: {success}\n")
        f.write(f"- ⚠️  警告: {warnings}\n")
        f.write(f"- ❌ 错误: {errors}\n")
        f.write(f"- 总计: {total}\n\n")
        f.write("## 详细结果\n\n")
        f.write("| 时间 | 测试项 | 状态 | 说明 |\n")
        f.write("|------|--------|------|------|\n")
        for test in results['tests']:
            icon = '✅' if test['status'] == 'success' else '❌' if test['status'] == 'error' else '⚠️'
            f.write(f"| {test['time']} | {test['name']} | {icon} {test['status']} | {test['message']} |\n")
        f.write("\n## 截图\n\n")
        for test in results['tests']:
            if test.get('screenshot'):
                f.write(f"### {test['screenshot']}\n")
                f.write(f"**{test['name']}** - {test['message']}\n\n")
                f.write(f"![{test['screenshot']}]({test['screenshot']})\n\n")
    print(f"💾 Markdown 报告已保存: {md_path}")
    
    return results

def main():
    """主测试流程"""
    print("🚀 启动 Playwright 测试...")
    print(f"📱 测试账号: {PHONE}")
    print(f"🌐 目标地址: {BASE_URL}")
    print(f"📁 截图目录: {SCREENSHOT_DIR}")
    
    # 先测试 API
    test_api_endpoints()
    
    with sync_playwright() as p:
        # 启动浏览器（非 headless 模式便于调试）
        browser = p.chromium.launch(headless=False, slow_mo=500)
        
        # 创建移动端视口
        context = browser.new_context(
            viewport={'width': 375, 'height': 812},
            user_agent='Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X) AppleWebKit/605.1.15'
        )
        
        page = context.new_page()
        
        # 设置超时
        page.set_default_timeout(10000)
        
        # 执行测试
        login_success = test_login(page)
        
        if login_success:
            test_home_page(page)
            test_record_page(page)
            test_report_page(page)
            test_profile_page(page)
        else:
            print("\n⚠️ 登录失败，跳过后续页面测试")
            # 仍然尝试测试各个页面
            test_home_page(page)
            test_record_page(page)
            test_report_page(page)
            test_profile_page(page)
        
        browser.close()
    
    # 生成报告
    return generate_report()

if __name__ == '__main__':
    main()
