# 代码修改不生效排查指南

## 🔍 常见原因及解决方案

### 1. **缓存问题（最常见）**

**现象：** 修改了代码，编译后还是旧的

**解决方案：**
```bash
# 清除所有缓存后重新编译
cd /Volumes/document/Projects/family-app/frontend
rm -rf dist/build/mp-weixin
rm -rf node_modules/.cache
npm run build:mp-weixin
```

### 2. **编辑了错误的文件**

**现象：** 修改了 A 文件，实际运行的是 B 文件

**检查方法：**
```bash
# 检查修改时间
ls -la src/pages/reminder/index.vue

# 确认修改已保存
grep "修改的内容" src/pages/reminder/index.vue
```

### 3. **构建未成功**

**现象：** npm run build 报错了但没注意

**检查方法：**
```bash
# 查看构建输出是否有错误
npm run build:mp-weixin 2>&1 | grep -E "error|Error|失败"
```

### 4. **上传了错误的目录**

**现象：** 编译成功了，但上传的是旧代码

**检查方法：**
```bash
# 检查编译后的文件时间
ls -la dist/build/mp-weixin/pages/reminder/index.js

# 确认文件内容已更新
grep "新添加的代码" dist/build/mp-weixin/pages/reminder/index.js
```

### 5. **微信小程序缓存**

**现象：** 代码已更新，但手机上还是旧的

**解决方案：**
1. 微信开发者工具 → 清除缓存 → 全部清除
2. 手机上删除小程序
3. 重新扫码进入

---

## 🛠️ 标准操作流程（建议每次遵循）

```bash
# 1. 修改代码前，先备份或确认当前状态
git status

# 2. 修改代码后，验证修改已保存
grep "修改的关键字" 文件路径

# 3. 清除缓存并重新编译
rm -rf dist/build/mp-weixin
npm run build:mp-weixin

# 4. 验证编译结果
grep "修改的关键字" dist/build/mp-weixin/对应文件

# 5. 复制静态资源（如图标）
cp static/tabbar/reminder*.png dist/build/mp-weixin/static/tabbar/

# 6. 上传
cli upload --project dist/build/mp-weixin --version x.x.x

# 7. 设置体验版后，清除手机缓存测试
```

---

## 🔧 自动化脚本

可以创建一个自动化的构建上传脚本：

```bash
#!/bin/bash
# build-and-upload.sh

echo "=== 开始构建 ==="
cd /Volumes/document/Projects/family-app/frontend

# 清除缓存
echo "清除缓存..."
rm -rf dist/build/mp-weixin

# 编译
echo "编译中..."
npm run build:mp-weixin

# 复制图标
echo "复制静态资源..."
cp static/tabbar/reminder*.png dist/build/mp-weixin/static/tabbar/ 2>/dev/null || true

# 验证关键文件
echo "验证编译结果..."
if grep -q "关键字" dist/build/mp-weixin/pages/xxx/index.js; then
    echo "✅ 代码已正确编译"
else
    echo "❌ 代码未正确编译，请检查"
    exit 1
fi

# 上传
echo "上传中..."
/Applications/wechatwebdevtools.app/Contents/MacOS/cli upload \
    --project dist/build/mp-weixin \
    --version 1.1.1 \
    --desc "自动构建"

echo "=== 完成 ==="
```

---

## ❓ 你遇到的具体情况

请告诉我：
1. **修改了哪个文件？**（完整路径）
2. **修改了什么内容？**（可以贴关键代码）
3. **执行了什么命令？**
4. **期望什么效果？实际什么效果？**

这样我可以帮你精确定位问题！
