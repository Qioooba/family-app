#!/bin/bash
# 微信小程序上传脚本 v1.1

APPID="${WEIXIN_APPID:-}"
VERSION="${MINIPROGRAM_VERSION:-1.0.0}"
PROJECT_PATH="${MINIPROGRAM_PROJECT_PATH:-/Volumes/document/Projects/family-app/frontend/dist/build/mp-weixin}"
DESC="${MINIPROGRAM_DESC:-自动构建上传体验版}"

if [ -z "$APPID" ]; then
    echo "❌ 请先导出 WEIXIN_APPID"
    exit 1
fi

# 检查private key
KEY_FILE="${MINIPROGRAM_PRIVATE_KEY_PATH:-/Volumes/document/Projects/family-app/frontend/private.key}"

if [ ! -f "$KEY_FILE" ]; then
    echo "❌ 错误：找不到 private.key 文件"
    echo ""
    echo "请按以下步骤获取并放置："
    echo "1. 登录微信小程序后台：https://mp.weixin.qq.com"
    echo "2. 进入 开发管理 -> 开发设置 -> 小程序代码上传"
    echo "3. 生成并下载上传密钥(private.key)"
    echo "4. 将文件放置到：$KEY_FILE"
    echo ""
    echo "或者使用微信开发者工具手动上传："
    echo "- 项目路径：$PROJECT_PATH"
    exit 1
fi

echo "🚀 开始上传微信小程序体验版..."
echo "AppID: $APPID"
echo "版本: $VERSION"
echo "描述: $DESC"
echo ""

miniprogram-ci upload \
    --appid "$APPID" \
    --pkp "$KEY_FILE" \
    --project-path "$PROJECT_PATH" \
    --version "$VERSION" \
    --desc "$DESC"

if [ $? -eq 0 ]; then
    echo "✅ 上传成功！"
    echo ""
    echo "请在微信小程序后台设置体验版："
    echo "1. 进入 版本管理"
    echo "2. 找到版本 $VERSION"
    echo "3. 点击'选为体验版'"
else
    echo "❌ 上传失败"
fi
