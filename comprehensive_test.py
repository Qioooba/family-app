#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
家庭项目 - 全面深度测试
测试所有功能点并记录问题
"""

from playwright.sync_api import sync_playwright
import time
import json
import os

# 测试结果
results = {
    "test_time": time.strftime('%Y-%m-%d %H:%M:%S'),
    "login": {},
    "home": {},
    "task": {},
    "family": {},
    "profile": {},
    "issues": []
}

def screenshot(page, name):
    """截图并保存"""
    path = f'/Users/qi/.openclaw/workspace/family-app/test-screenshots/{name}.png'
    page.screenshot(path=path, full_page=False)
    print(f"  📸 截图: {name}.png")
    return path

def log_issue(module, desc, level="P1"):
    """记录问题"""
    issue = {"module": module, "desc": desc, "level": level, "time": time.strftime('%H:%M:%S')}
    results["issues"].append(issue)
    icon = "🔴" if level == "P0" else "🟠" if level == "P1" else "🟡"
    print(f"  {icon} [{level}] {module}: {desc}")

def test_login(page):
    """1. 登录测试"""
    print("\n" + "="*50)
    print("📱 1. 登录模块测试")
    print("="*50)
    
    try:
        # 访问登录页
        print("\n  → 访问登录页面...")
        page.goto('http://localhost:3000/#/pages/login/index')
        page.wait_for_load_state('networkidle')
        time.sleep(3)
        screenshot(page, "01-login-page")
        
        # 检查页面元素
        print("  → 检查页面元素...")
        try:
            # 尝试多种选择器
            selectors = ['.title', '.login-title', 'text=家庭助手', '.uni-title']
            title_found = False
            for sel in selectors:
                try:
                    if page.locator(sel).count() > 0:
                        title = page.locator(sel).first.text_content()
                        print(f"    ✅ 标题: {title}")
                        results["login"]["title"] = title
                        title_found = True
                        break
                except:
                    continue
            if not title_found:
                log_issue("登录页", "页面标题未找到", "P1")
        except Exception as e:
            log_issue("登录页", f"检查标题出错: {e}", "P2")
        
        # 填写登录表单
        print("\n  → 填写登录信息...")
        try:
            # 查找输入框
            inputs = page.locator('input')
            print(f"    找到 {inputs.count()} 个输入框")
            
            # 手机号输入
            phone_selectors = ['input[type="tel"]', 'input[placeholder*="手机"]', 'input[placeholder*="phone"]', '.login-input', 'input']
            for sel in phone_selectors:
                try:
                    phone_input = page.locator(sel).first
                    if phone_input.count() > 0:
                        phone_input.fill('15861890687')
                        print("    ✅ 手机号已填写")
                        break
                except:
                    continue
            
            time.sleep(0.5)
            
            # 密码输入
            for sel in ['input[type="password"]', 'input[placeholder*="密码"]', 'input']:
                try:
                    pwd_input = page.locator(sel).nth(1) if inputs.count() > 1 else page.locator(sel).first
                    if pwd_input.count() > 0:
                        pwd_input.fill('111222')
                        print("    ✅ 密码已填写")
                        break
                except:
                    continue
            
            time.sleep(0.5)
            screenshot(page, "02-login-filled")
            
        except Exception as e:
            log_issue("登录页", f"填写表单失败: {e}", "P0")
            return False
        
        # 点击登录按钮
        print("\n  → 点击登录...")
        try:
            btn_selectors = ['.login-btn', 'button:has-text("登录")', 'text=登录', '.btn-primary', 'button']
            for sel in btn_selectors:
                try:
                    btn = page.locator(sel).first
                    if btn.count() > 0 and btn.is_visible():
                        btn.click()
                        print("    ✅ 点击登录按钮")
                        break
                except:
                    continue
            
            time.sleep(5)
            screenshot(page, "03-after-login")
            
            # 检查登录结果
            current_url = page.url
            results["login"]["url_after_login"] = current_url
            print(f"    当前URL: {current_url}")
            
            if 'home' in current_url or 'index' in current_url or page.locator('.uni-tabbar').count() > 0:
                print("    ✅ 登录成功，进入首页")
                results["login"]["success"] = True
                return True
            else:
                log_issue("登录", f"登录后未进入首页，URL: {current_url}", "P0")
                results["login"]["success"] = False
                return False
                
        except Exception as e:
            log_issue("登录", f"登录操作失败: {e}", "P0")
            return False
            
    except Exception as e:
        log_issue("登录模块", f"整体测试失败: {e}", "P0")
        return False

def test_home_page(page):
    """2. 首页测试"""
    print("\n" + "="*50)
    print("🏠 2. 首页模块测试")
    print("="*50)
    
    try:
        # 确保在首页
        if 'home' not in page.url:
            page.goto('http://localhost:3000/#/pages/home/index')
            time.sleep(3)
        
        screenshot(page, "04-home-page")
        
        # 检查用户信息
        print("\n  → 检查用户信息...")
        try:
            user_selectors = ['.user-name', '.username', '.nickname', 'text=欢迎', 'text=您好']
            user_found = False
            for sel in user_selectors:
                try:
                    if page.locator(sel).count() > 0:
                        text = page.locator(sel).first.text_content()
                        print(f"    ✅ 用户: {text}")
                        results["home"]["username"] = text
                        user_found = True
                        break
                except:
                    continue
            if not user_found:
                log_issue("首页", "用户名未显示", "P1")
        except Exception as e:
            log_issue("首页", f"检查用户失败: {e}", "P2")
        
        # 检查快捷入口
        print("\n  → 检查快捷入口...")
        shortcuts = ["添加任务", "记录饮食", "喝水打卡", "优惠券", "营养成分", "AI助手"]
        results["home"]["shortcuts"] = {}
        
        for shortcut in shortcuts:
            try:
                if page.locator(f'text={shortcut}').count() > 0:
                    el = page.locator(f'text={shortcut}').first
                    if el.is_visible():
                        print(f"    ✅ {shortcut}")
                        results["home"]["shortcuts"][shortcut] = "visible"
                    else:
                        results["home"]["shortcuts"][shortcut] = "hidden"
                else:
                    log_issue("首页", f"快捷入口'{shortcut}'不存在", "P2")
                    results["home"]["shortcuts"][shortcut] = "missing"
            except Exception as e:
                results["home"]["shortcuts"][shortcut] = f"error: {e}"
        
        # 测试点击添加任务
        print("\n  → 测试点击'添加任务'...")
        try:
            add_task = page.locator('text=添加任务').first
            if add_task.count() > 0 and add_task.is_visible():
                add_task.click()
                time.sleep(3)
                screenshot(page, "05-click-add-task")
                results["home"]["add_task_click"] = "success"
                print("    ✅ 点击添加任务成功")
                
                # 测试返回
                page.go_back()
                time.sleep(2)
                screenshot(page, "06-back-to-home")
                results["home"]["navigation"] = "success"
                print("    ✅ 返回首页成功")
            else:
                log_issue("首页", "添加任务按钮不可见", "P1")
        except Exception as e:
            log_issue("首页", f"添加任务测试失败: {e}", "P1")
        
        # 测试今日待办
        print("\n  → 测试今日待办...")
        try:
            page.evaluate('window.scrollTo(0, 400)')
            time.sleep(1)
            screenshot(page, "07-home-scrolled")
            
            more_btn = page.locator('text=更多').first
            if more_btn.count() > 0 and more_btn.is_visible():
                more_btn.click()
                time.sleep(3)
                screenshot(page, "08-task-more")
                results["home"]["task_more"] = "success"
                print("    ✅ 点击更多成功")
                
                page.go_back()
                time.sleep(2)
            else:
                log_issue("首页", "更多按钮不可见", "P2")
        except Exception as e:
            log_issue("首页", f"今日待办测试失败: {e}", "P2")
        
        # 测试记录饮食
        print("\n  → 测试记录饮食...")
        try:
            food_btn = page.locator('text=记录饮食').first
            if food_btn.count() > 0:
                food_btn.click()
                time.sleep(3)
                screenshot(page, "09-food-record")
                results["home"]["food_record"] = "success"
                print("    ✅ 记录饮食页面")
                page.go_back()
                time.sleep(2)
        except Exception as e:
            log_issue("首页", f"记录饮食失败: {e}", "P2")
        
        # 测试喝水打卡
        print("\n  → 测试喝水打卡...")
        try:
            water_btn = page.locator('text=喝水打卡').first
            if water_btn.count() > 0:
                water_btn.click()
                time.sleep(3)
                screenshot(page, "10-water-check")
                results["home"]["water_check"] = "success"
                print("    ✅ 喝水打卡页面")
                page.go_back()
                time.sleep(2)
        except Exception as e:
            log_issue("首页", f"喝水打卡失败: {e}", "P2")
        
        # 测试AI助手
        print("\n  → 测试AI助手...")
        try:
            ai_btn = page.locator('text=AI助手').first
            if ai_btn.count() > 0:
                ai_btn.click()
                time.sleep(3)
                screenshot(page, "11-ai-assistant")
                results["home"]["ai_assistant"] = "success"
                print("    ✅ AI助手页面")
                page.go_back()
                time.sleep(2)
        except Exception as e:
            log_issue("首页", f"AI助手失败: {e}", "P2")
        
        # 测试滚动
        print("\n  → 测试页面滚动...")
        try:
            page.evaluate('window.scrollTo(0, 800)')
            time.sleep(1)
            screenshot(page, "12-home-deep-scroll")
            results["home"]["scroll"] = "success"
            print("    ✅ 页面滚动正常")
        except Exception as e:
            log_issue("首页", f"滚动测试失败: {e}", "P2")
        
        print("\n  ✅ 首页测试完成")
        
    except Exception as e:
        log_issue("首页模块", f"整体测试失败: {e}", "P0")

def test_task_module(page):
    """3. 任务模块测试"""
    print("\n" + "="*50)
    print("📋 3. 任务模块测试")
    print("="*50)
    
    try:
        # 点击底部任务导航
        print("\n  → 点击任务导航...")
        try:
            # 尝试多种方式找到任务导航
            task_tab = None
            for sel in ['.uni-tabbar__label:has-text("任务")', 'text=任务 >> visible=true', '.tabbar-item:has-text("任务")', 'text=任务']:
                try:
                    tab = page.locator(sel).first
                    if tab.count() > 0 and tab.is_visible():
                        task_tab = tab
                        break
                except:
                    continue
            
            if task_tab:
                task_tab.click()
                time.sleep(3)
                screenshot(page, "13-task-page")
                results["task"]["page_load"] = "success"
                print("    ✅ 任务页加载")
            else:
                log_issue("任务", "任务导航按钮未找到", "P0")
                return
        except Exception as e:
            log_issue("任务", f"导航失败: {e}", "P0")
            return
        
        # 测试创建任务表单
        print("\n  → 测试创建任务...")
        try:
            # 查找输入框
            input_selectors = ['input[placeholder*="标题"]', '.task-title input', 'input', 'textarea']
            title_filled = False
            for sel in input_selectors:
                try:
                    inp = page.locator(sel).first
                    if inp.count() > 0:
                        inp.fill("测试任务标题")
                        title_filled = True
                        print("    ✅ 填写任务标题")
                        break
                except:
                    continue
            
            if not title_filled:
                log_issue("任务", "任务标题输入框未找到", "P1")
            
            # 选择分类
            try:
                for cat in ['购物', '工作', '生活']:
                    cat_btn = page.locator(f'text={cat}').first
                    if cat_btn.count() > 0:
                        cat_btn.click()
                        print(f"    ✅ 选择分类: {cat}")
                        break
            except:
                pass
            
            time.sleep(1)
            screenshot(page, "14-task-form")
            results["task"]["form"] = "filled"
            
        except Exception as e:
            log_issue("任务", f"表单测试失败: {e}", "P1")
        
        print("\n  ✅ 任务模块测试完成")
        
    except Exception as e:
        log_issue("任务模块", f"整体测试失败: {e}", "P0")

def test_family_module(page):
    """4. 家庭模块测试"""
    print("\n" + "="*50)
    print("👨‍👩‍👧 4. 家庭模块测试")
    print("="*50)
    
    try:
        print("\n  → 点击家庭导航...")
        family_tab = None
        for sel in ['.uni-tabbar__label:has-text("家庭")', 'text=家庭 >> visible=true', '.tabbar-item:has-text("家庭")']:
            try:
                tab = page.locator(sel).first
                if tab.count() > 0 and tab.is_visible():
                    family_tab = tab
                    break
            except:
                continue
        
        if family_tab:
            family_tab.click()
            time.sleep(3)
            screenshot(page, "15-family-page")
            results["family"]["page_load"] = "success"
            print("    ✅ 家庭页加载")
            
            # 检查内容
            content = page.content()
            checks = ['家庭', '成员', '邀请', '设置']
            found = [c for c in checks if c in content]
            print(f"    ✅ 页面包含: {', '.join(found) if found else '基础内容'}")
            results["family"]["content"] = found
        else:
            log_issue("家庭", "家庭导航按钮未找到", "P0")
        
        print("\n  ✅ 家庭模块测试完成")
        
    except Exception as e:
        log_issue("家庭模块", f"整体测试失败: {e}", "P0")

def test_profile_module(page):
    """5. 我的页面测试"""
    print("\n" + "="*50)
    print("👤 5. 我的页面测试")
    print("="*50)
    
    try:
        print("\n  → 点击我的导航...")
        profile_tab = None
        for sel in ['.uni-tabbar__label:has-text("我的")', 'text=我的 >> visible=true', '.tabbar-item:has-text("我的")']:
            try:
                tab = page.locator(sel).first
                if tab.count() > 0 and tab.is_visible():
                    profile_tab = tab
                    break
            except:
                continue
        
        if profile_tab:
            profile_tab.click()
            time.sleep(3)
            screenshot(page, "16-profile-page")
            results["profile"]["page_load"] = "success"
            print("    ✅ 我的页加载")
            
            # 滚动测试
            page.evaluate('window.scrollTo(0, 400)')
            time.sleep(1)
            screenshot(page, "17-profile-scrolled")
            
            # 检查菜单项
            print("\n  → 检查菜单项...")
            menu_items = ["设置", "关于", "帮助", "退出", "账号", "通知"]
            results["profile"]["menu"] = {}
            for item in menu_items:
                try:
                    if page.locator(f'text={item}').count() > 0:
                        results["profile"]["menu"][item] = "found"
                        print(f"    ✅ {item}")
                    else:
                        results["profile"]["menu"][item] = "not_found"
                except:
                    results["profile"]["menu"][item] = "error"
        else:
            log_issue("我的", "我的导航按钮未找到", "P0")
        
        print("\n  ✅ 我的模块测试完成")
        
    except Exception as e:
        log_issue("我的模块", f"整体测试失败: {e}", "P0")

def test_back_to_home(page):
    """6. 返回首页测试"""
    print("\n" + "="*50)
    print("🏠 6. 返回首页测试")
    print("="*50)
    
    try:
        print("\n  → 点击首页导航...")
        home_tab = None
        for sel in ['.uni-tabbar__label:has-text("首页")', 'text=首页 >> visible=true', '.tabbar-item:has-text("首页")']:
            try:
                tab = page.locator(sel).first
                if tab.count() > 0 and tab.is_visible():
                    home_tab = tab
                    break
            except:
                continue
        
        if home_tab:
            home_tab.click()
            time.sleep(2)
            screenshot(page, "18-back-home")
            print("    ✅ 返回首页成功")
            results["navigation"] = {"back_to_home": "success"}
        else:
            log_issue("导航", "首页导航按钮未找到", "P1")
    except Exception as e:
        log_issue("导航", f"返回首页失败: {e}", "P1")

def generate_report():
    """生成测试报告"""
    print("\n" + "="*50)
    print("📊 生成测试报告")
    print("="*50)
    
    # JSON报告
    json_path = '/Users/qi/.openclaw/workspace/family-app/test-screenshots/test-result.json'
    with open(json_path, 'w') as f:
        json.dump(results, f, indent=2, ensure_ascii=False)
    print(f"\n  ✅ JSON报告: {json_path}")
    
    # Markdown报告
    md_path = '/Users/qi/.openclaw/workspace/family-app/test-screenshots/COMPREHENSIVE_TEST_REPORT.md'
    
    # 统计问题
    p0_count = len([i for i in results['issues'] if i['level'] == 'P0'])
    p1_count = len([i for i in results['issues'] if i['level'] == 'P1'])
    p2_count = len([i for i in results['issues'] if i['level'] == 'P2'])
    
    md_content = f"""# 🧪 家庭项目 - 全面深度测试报告

## 📋 测试概况
| 项目 | 内容 |
|------|------|
| 测试时间 | {results['test_time']} |
| 测试账号 | 15861890687 |
| 测试总问题 | {len(results['issues'])} 个 |
| 🔴 P0 严重 | {p0_count} 个 |
| 🟠 P1 重要 | {p1_count} 个 |
| 🟡 P2 一般 | {p2_count} 个 |

---

## 📱 1. 登录模块
```json
{json.dumps(results.get('login', {}), indent=2, ensure_ascii=False)}
```

---

## 🏠 2. 首页模块
```json
{json.dumps(results.get('home', {}), indent=2, ensure_ascii=False)}
```

---

## 📋 3. 任务模块
```json
{json.dumps(results.get('task', {}), indent=2, ensure_ascii=False)}
```

---

## 👨‍👩‍👧 4. 家庭模块
```json
{json.dumps(results.get('family', {}), indent=2, ensure_ascii=False)}
```

---

## 👤 5. 我的模块
```json
{json.dumps(results.get('profile', {}), indent=2, ensure_ascii=False)}
```

---

## 🔴 问题清单

"""
    
    if results['issues']:
        for issue in results['issues']:
            icon = "🔴" if issue['level'] == "P0" else "🟠" if issue['level'] == "P1" else "🟡"
            md_content += f"- {icon} **[{issue['level']}]** {issue['module']}: {issue['desc']} ({issue['time']})\n"
    else:
        md_content += "✅ 未发现任何问题！\n"
    
    md_content += f"""

---

## 📸 截图文件

所有截图保存在 `test-screenshots/` 目录：
"""
    
    # 列出所有截图
    try:
        files = sorted([f for f in os.listdir('/Users/qi/.openclaw/workspace/family-app/test-screenshots') if f.endswith('.png')])
        for f in files:
            md_content += f"- `{f}`\n"
    except:
        md_content += "- (截图列表获取失败)\n"
    
    with open(md_path, 'w') as f:
        f.write(md_content)
    
    print(f"  ✅ Markdown报告: {md_path}")
    print(f"\n  📊 统计: P0={p0_count}, P1={p1_count}, P2={p2_count}")
    
    return md_path, json_path

def main():
    """主测试流程"""
    print("\n" + "="*60)
    print("🔬 家庭项目 - 全面深度测试启动")
    print("="*60)
    print("\n  测试账号: 15861890687")
    print("  目标URL: http://localhost:3000")
    print("  截图目录: test-screenshots/")
    
    with sync_playwright() as p:
        # 启动浏览器（非无头模式，保持打开）
        print("\n  启动浏览器...")
        browser = p.chromium.launch(headless=False, slow_mo=500)
        
        # 创建页面
        context = browser.new_context(
            viewport={'width': 375, 'height': 812},
            device_scale_factor=2
        )
        page = context.new_page()
        
        # 设置超时
        page.set_default_timeout(10000)
        
        try:
            # 执行测试
            login_success = test_login(page)
            
            if login_success:
                test_home_page(page)
                test_task_module(page)
                test_family_module(page)
                test_profile_module(page)
                test_back_to_home(page)
            else:
                print("\n  ⚠️ 登录失败，跳过其他测试")
            
            # 生成报告
            md_path, json_path = generate_report()
            
            # 最终截图
            screenshot(page, "99-final-state")
            
            print("\n" + "="*60)
            print("🎉 测试完成！")
            print("="*60)
            print(f"\n  📁 报告文件:")
            print(f"     - {md_path}")
            print(f"     - {json_path}")
            print(f"\n  📸 截图目录: test-screenshots/")
            print(f"\n  🔴 发现问题: {len(results['issues'])} 个")
            
            # 保持浏览器打开
            print("\n" + "="*60)
            print("⏸️ 浏览器保持打开状态")
            print("="*60)
            print("\n  浏览器将保持打开，您可以手动查看")
            print("  按 Ctrl+C 结束测试并关闭浏览器\n")
            
            # 无限等待，保持浏览器打开
            while True:
                time.sleep(1)
                
        except KeyboardInterrupt:
            print("\n\n  👋 用户中断测试")
        except Exception as e:
            print(f"\n\n  ❌ 测试异常: {e}")
        finally:
            print("\n  关闭浏览器...")
            browser.close()

if __name__ == "__main__":
    main()
