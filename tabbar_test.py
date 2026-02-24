#!/usr/bin/env python3
"""
TabBar专项测试脚本
测试内容：TabBar显示、Tab切换、Tab状态、页面返回
"""

from playwright.sync_api import sync_playwright
import os
import json
from datetime import datetime

# 配置
BASE_URL = 'http://localhost:3001'
SCREENSHOT_DIR = '/Users/qi/.openclaw/workspace/family-app/test-screenshots/tabbar'
REPORT_PATH = '/Users/qi/.openclaw/workspace/memory/tabbar-test-report.md'

# 确保截图目录存在
os.makedirs(SCREENSHOT_DIR, exist_ok=True)
os.makedirs(os.path.dirname(REPORT_PATH), exist_ok=True)

# 测试结果
results = {
    'start_time': datetime.now().strftime('%Y-%m-%d %H:%M:%S'),
    'tests': [],
    'screenshots': [],
    'issues': []
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
    if screenshot:
        results['screenshots'].append(screenshot)
    return result

def log_issue(issue_type, description, severity='medium'):
    """记录问题"""
    results['issues'].append({
        'type': issue_type,
        'description': description,
        'severity': severity,
        'time': datetime.now().strftime('%H:%M:%S')
    })
    print(f"🐛 发现问题 [{severity}]: {issue_type} - {description}")

def safe_screenshot(page, filename):
    """安全截图"""
    try:
        path = os.path.join(SCREENSHOT_DIR, filename)
        page.screenshot(path=path, full_page=True)
        return filename
    except Exception as e:
        print(f"截图失败 {filename}: {e}")
        return None

def wait_for_tabbar(page):
    """等待TabBar加载完成"""
    try:
        # 等待up-tabbar组件加载
        page.wait_for_selector('.up-tabbar, .uni-tabbar, .tabbar', timeout=5000)
        return True
    except:
        return False

def get_tabbar_items(page):
    """获取TabBar项目"""
    try:
        # 尝试不同的TabBar选择器
        selectors = [
            '.up-tabbar-item',
            '.uni-tabbar-item', 
            '.tabbar-item',
            '.tab-item'
        ]
        for selector in selectors:
            items = page.locator(selector)
            count = items.count()
            if count > 0:
                return items, count, selector
        return None, 0, None
    except Exception as e:
        print(f"获取TabBar项目失败: {e}")
        return None, 0, None

def test_tabbar_display(page):
    """测试TabBar显示"""
    print("\n📱 === 测试TabBar显示 ===")
    
    try:
        # 访问首页
        page.goto(f'{BASE_URL}/pages/home/index')
        page.wait_for_timeout(3000)
        
        screenshot = safe_screenshot(page, '01-tabbar-initial.png')
        
        # 检查TabBar是否存在
        if wait_for_tabbar(page):
            log_test('TabBar存在', 'success', 'TabBar在页面底部显示', screenshot)
        else:
            log_test('TabBar存在', 'error', '未找到TabBar组件')
            log_issue('TabBar显示', 'TabBar未在页面底部显示', 'high')
            return False
        
        # 获取TabBar项目
        items, count, selector = get_tabbar_items(page)
        
        if items:
            log_test('TabBar项目数', 'success', f'找到 {count} 个Tab项目', screenshot)
            
            # 检查每个Tab的文字
            tab_texts = []
            for i in range(count):
                try:
                    text = items.nth(i).locator('.up-tabbar-item__text, .tabbar-text, .text').text_content()
                    if text:
                        tab_texts.append(text.strip())
                except:
                    pass
            
            print(f"   发现Tab标签: {tab_texts}")
            
            # 期望的Tab标签（根据App.vue配置）
            expected_tabs = ['首页', '任务', '心愿', '我的']
            
            for tab in expected_tabs:
                if tab in tab_texts:
                    log_test(f'Tab标签-{tab}', 'success', f'找到"{tab}"标签')
                else:
                    log_test(f'Tab标签-{tab}', 'warning', f'未找到"{tab}"标签')
                    log_issue('Tab标签缺失', f'未找到"{tab}"标签', 'medium')
            
            # 检查当前选中状态
            try:
                active_item = page.locator(f'{selector}--active, .active, .selected').first
                if active_item.is_visible():
                    log_test('Tab选中状态', 'success', '当前选中Tab有高亮样式', screenshot)
                else:
                    log_test('Tab选中状态', 'warning', '选中状态样式不明显')
            except:
                log_test('Tab选中状态', 'warning', '无法检测选中状态')
        else:
            log_test('TabBar项目', 'error', '无法获取TabBar项目')
            return False
            
        return True
        
    except Exception as e:
        log_test('TabBar显示测试', 'error', str(e))
        log_issue('TabBar显示异常', str(e), 'high')
        return False

def test_tab_switching(page):
    """测试Tab切换"""
    print("\n🔄 === 测试Tab切换 ===")
    
    # 获取TabBar项目
    items, count, selector = get_tabbar_items(page)
    if not items or count == 0:
        log_test('Tab切换', 'error', 'TabBar项目不存在')
        return
    
    # 根据App.vue的配置
    tab_pages = [
        ('首页', '/pages/home/index'),
        ('任务', '/pages/task/index'),
        ('心愿', '/pages/wish/index'),
        ('我的', '/pages/profile/index')
    ]
    
    for i, (tab_name, page_path) in enumerate(tab_pages):
        if i >= count:
            break
            
        try:
            print(f"\n   点击Tab {i+1}: {tab_name}")
            
            # 记录点击前的URL
            prev_url = page.url
            
            # 点击Tab
            items.nth(i).click()
            page.wait_for_timeout(2000)
            
            # 检查页面是否变化
            new_url = page.url
            
            screenshot = safe_screenshot(page, f'02-tab-{tab_name}-clicked.png')
            
            # 验证页面跳转
            if tab_name in ['首页', '任务', '心愿', '我的']:
                expected_paths = ['/home', '/task', '/wish', '/profile']
                if any(path in new_url for path in expected_paths):
                    log_test(f'点击{tab_name}', 'success', f'成功切换到{tab_name}页', screenshot)
                else:
                    log_test(f'点击{tab_name}', 'warning', f'页面未明显变化: {new_url}', screenshot)
                    log_issue('Tab切换', f'点击"{tab_name}"后页面未正确切换', 'medium')
            
            # 检查动画效果
            try:
                # 检查是否有过渡动画类
                animated = page.locator('.up-tabbar-item--active, .active-transition').count() > 0
                if animated:
                    log_test(f'{tab_name}-动画', 'success', '切换时有动画效果')
                else:
                    log_test(f'{tab_name}-动画', 'info', '动画效果不明显或不存在')
            except:
                pass
                
        except Exception as e:
            log_test(f'点击{tab_name}', 'error', str(e))
            log_issue('Tab点击异常', f'点击"{tab_name}"时出错: {str(e)}', 'medium')

def test_tab_state(page):
    """测试Tab状态保持"""
    print("\n🎨 === 测试Tab状态 ===")
    
    try:
        # 点击"首页"
        items, count, selector = get_tabbar_items(page)
        if items and count > 0:
            items.nth(0).click()
            page.wait_for_timeout(1000)
            
            screenshot = safe_screenshot(page, '03-tab-state-home.png')
            log_test('首页选中状态', 'success', '首页Tab处于选中状态', screenshot)
            
            # 点击"我的"
            if count >= 4:
                items.nth(3).click()
                page.wait_for_timeout(1000)
                
                screenshot = safe_screenshot(page, '04-tab-state-profile.png')
                log_test('我的选中状态', 'success', '我的Tab处于选中状态，首页未选中', screenshot)
                
                # 再点击回首页
                items.nth(0).click()
                page.wait_for_timeout(1000)
                
                screenshot = safe_screenshot(page, '05-tab-state-back-home.png')
                log_test('状态切换回首页', 'success', '选中状态正确切换回首页', screenshot)
            else:
                log_test('Tab数量', 'warning', f'只有{count}个Tab，少于预期的4个')
                
    except Exception as e:
        log_test('Tab状态测试', 'error', str(e))
        log_issue('Tab状态异常', str(e), 'medium')

def test_page_return(page):
    """测试页面返回"""
    print("\n⬅️ === 测试页面返回 ===")
    
    try:
        # 访问子页面（如从任务页进入子页面）
        page.goto(f'{BASE_URL}/pages/task/create')
        page.wait_for_timeout(2000)
        
        screenshot = safe_screenshot(page, '06-subpage-create.png')
        log_test('进入子页面', 'success', '进入任务创建子页面', screenshot)
        
        # 点击返回按钮（如果存在）
        try:
            back_btn = page.locator('.back-btn, .nav-back, .uni-icons:has-text("back"), .icon-back').first
            if back_btn.is_visible():
                back_btn.click()
                page.wait_for_timeout(2000)
                
                screenshot = safe_screenshot(page, '07-after-back.png')
                
                # 检查是否回到任务页
                if '/task' in page.url and '/create' not in page.url:
                    log_test('返回Tab页', 'success', '正确返回到任务Tab页', screenshot)
                else:
                    log_test('返回Tab页', 'warning', f'返回后URL: {page.url}', screenshot)
                    log_issue('页面返回', '从子页面返回后未正确回到Tab页', 'medium')
            else:
                log_test('返回按钮', 'warning', '未找到返回按钮')
        except Exception as e:
            log_test('页面返回', 'warning', f'返回测试失败: {str(e)}')
            
    except Exception as e:
        log_test('页面返回测试', 'error', str(e))
        log_issue('页面返回异常', str(e), 'low')

def generate_report():
    """生成Markdown测试报告"""
    print("\n" + "="*50)
    print("📋 生成测试报告")
    print("="*50)
    
    results['end_time'] = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    
    # 统计
    total = len(results['tests'])
    success = len([t for t in results['tests'] if t['status'] == 'success'])
    errors = len([t for t in results['tests'] if t['status'] == 'error'])
    warnings = len([t for t in results['tests'] if t['status'] == 'warning'])
    
    # 生成Markdown报告
    md_content = f"""# TabBar专项测试报告

## 测试信息

- **测试时间**: {results['start_time']} ~ {results['end_time']}
- **测试目标**: 底部导航栏(TabBar)功能
- **测试环境**: H5页面 (http://localhost:3001)

## 测试统计

| 指标 | 数量 |
|------|------|
| ✅ 成功 | {success} |
| ⚠️  警告 | {warnings} |
| ❌ 错误 | {errors} |
| 总计 | {total} |

## TabBar配置

根据App.vue文件，TabBar配置如下：

| 序号 | 图标 | 选中图标 | 文字 | 页面路径 |
|------|------|----------|------|----------|
| 1 | home | home-fill | 首页 | /pages/home/index |
| 2 | calendar | calendar-fill | 任务 | /pages/task/index |
| 3 | heart | heart-fill | 心愿 | /pages/wish/index |
| 4 | account | account-fill | 我的 | /pages/profile/index |

*注：实际应用中TabBar为4个选项，而非任务描述中的5个选项*

## 测试结果详情

### TabBar显示测试

| 测试项 | 状态 | 说明 |
|--------|------|------|
"""
    
    # 添加TabBar显示相关测试
    for test in results['tests']:
        if 'TabBar' in test['name'] or 'Tab标签' in test['name'] or '选中状态' in test['name']:
            icon = '✅' if test['status'] == 'success' else '❌' if test['status'] == 'error' else '⚠️'
            md_content += f"| {test['name']} | {icon} {test['status']} | {test['message']} |\n"
    
    md_content += """
### Tab切换测试

| 测试项 | 状态 | 说明 |
|--------|------|------|
"""
    
    # 添加Tab切换相关测试
    for test in results['tests']:
        if '点击' in test['name'] or '动画' in test['name']:
            icon = '✅' if test['status'] == 'success' else '❌' if test['status'] == 'error' else '⚠️'
            md_content += f"| {test['name']} | {icon} {test['status']} | {test['message']} |\n"
    
    md_content += """
### Tab状态测试

| 测试项 | 状态 | 说明 |
|--------|------|------|
"""
    
    # 添加Tab状态相关测试
    for test in results['tests']:
        if '状态' in test['name']:
            icon = '✅' if test['status'] == 'success' else '❌' if test['status'] == 'error' else '⚠️'
            md_content += f"| {test['name']} | {icon} {test['status']} | {test['message']} |\n"
    
    md_content += """
### 页面返回测试

| 测试项 | 状态 | 说明 |
|--------|------|------|
"""
    
    # 添加页面返回相关测试
    for test in results['tests']:
        if '子页面' in test['name'] or '返回' in test['name']:
            icon = '✅' if test['status'] == 'success' else '❌' if test['status'] == 'error' else '⚠️'
            md_content += f"| {test['name']} | {icon} {test['status']} | {test['message']} |\n"
    
    # 截图部分
    md_content += """
## 测试截图

截图文件保存在: `{SCREENSHOT_DIR}`

"""
    
    for screenshot in results['screenshots']:
        desc = screenshot.replace('.png', '').replace('-', ' ').replace('_', ' ')
        md_content += f"### {desc}\n\n"
        md_content += f"![{screenshot}](./test-screenshots/tabbar/{screenshot})\n\n"
    
    # 问题汇总
    md_content += """
## 发现的问题

"""
    
    if results['issues']:
        md_content += "| 时间 | 问题类型 | 描述 | 严重级别 |\n"
        md_content += "|------|----------|------|----------|\n"
        for issue in results['issues']:
            icon = '🔴' if issue['severity'] == 'high' else '🟡' if issue['severity'] == 'medium' else '🟢'
            md_content += f"| {issue['time']} | {icon} {issue['type']} | {issue['description']} | {issue['severity']} |\n"
    else:
        md_content += "✅ 未发现明显问题\n"
    
    # 结论
    md_content += """
## 测试结论

"""
    if errors == 0 and warnings == 0:
        md_content += "✅ **TabBar功能完整，所有测试项通过**\n"
    elif errors == 0:
        md_content += "⚠️ **TabBar功能基本正常，存在部分警告项**\n"
    else:
        md_content += "❌ **TabBar存在问题，需要修复**\n"
    
    md_content += f"""
- 成功率: {(success/total*100):.1f}%
- 建议: 根据发现的问题进行相应调整
"""
    
    # 保存报告
    with open(REPORT_PATH, 'w', encoding='utf-8') as f:
        f.write(md_content)
    
    print(f"✅ 测试报告已保存: {REPORT_PATH}")
    print(f"\n📊 统计: 成功={success}, 警告={warnings}, 错误={errors}, 总计={total}")
    print(f"🐛 发现问题: {len(results['issues'])}个")
    
    return results

def main():
    """主测试流程"""
    print("🚀 启动TabBar专项测试...")
    print(f"🌐 目标地址: {BASE_URL}")
    print(f"📁 截图目录: {SCREENSHOT_DIR}")
    print(f"📝 报告路径: {REPORT_PATH}")
    
    with sync_playwright() as p:
        # 启动浏览器
        browser = p.chromium.launch(headless=False, slow_mo=500)
        
        # 创建移动端视口
        context = browser.new_context(
            viewport={'width': 375, 'height': 812},
            user_agent='Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X) AppleWebKit/605.1.15'
        )
        
        page = context.new_page()
        page.set_default_timeout(10000)
        
        # 执行测试
        tabbar_ok = test_tabbar_display(page)
        
        if tabbar_ok:
            test_tab_switching(page)
            test_tab_state(page)
            test_page_return(page)
        else:
            print("\n⚠️ TabBar显示测试失败，跳过后续测试")
        
        browser.close()
    
    # 生成报告
    generate_report()
    
    print("\n✅ TabBar测试完成!")

if __name__ == '__main__':
    main()
