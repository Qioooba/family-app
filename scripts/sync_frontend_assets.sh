#!/bin/bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
FRONTEND_DIR="$ROOT_DIR/frontend"
BACKEND_STATIC_DIR="$ROOT_DIR/backend/family-service/src/main/resources/static"
H5_BUILD_DIR="$FRONTEND_DIR/dist/build/h5"
MP_BUILD_DIR="$FRONTEND_DIR/dist/build/mp-weixin"

if [ ! -d "$H5_BUILD_DIR" ]; then
  echo "❌ 缺少 H5 构建产物: $H5_BUILD_DIR"
  echo "请先执行: cd frontend && npm run build:h5"
  exit 1
fi

if [ ! -d "$MP_BUILD_DIR" ]; then
  echo "❌ 缺少微信小程序构建产物: $MP_BUILD_DIR"
  echo "请先执行: cd frontend && npm run build:mp-weixin"
  exit 1
fi

echo "🧹 清理旧的前端静态产物..."
rm -rf \
  "$BACKEND_STATIC_DIR/assets" \
  "$BACKEND_STATIC_DIR/h5" \
  "$BACKEND_STATIC_DIR/mp-weixin" \
  "$BACKEND_STATIC_DIR/static" \
  "$BACKEND_STATIC_DIR/index.html"

echo "📦 同步 H5 构建产物..."
cp -R "$H5_BUILD_DIR" "$BACKEND_STATIC_DIR/h5"
cp "$H5_BUILD_DIR/index.html" "$BACKEND_STATIC_DIR/index.html"
cp -R "$H5_BUILD_DIR/assets" "$BACKEND_STATIC_DIR/assets"
cp -R "$H5_BUILD_DIR/static" "$BACKEND_STATIC_DIR/static"

echo "📦 同步微信小程序构建产物..."
cp -R "$MP_BUILD_DIR" "$BACKEND_STATIC_DIR/mp-weixin"
rm -rf "$BACKEND_STATIC_DIR/mp-weixin/node-modules" "$BACKEND_STATIC_DIR/mp-weixin/project.private.config.json"

echo "✅ 前端构建产物已同步到后端静态目录"
