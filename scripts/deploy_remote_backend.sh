#!/bin/bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
REMOTE_HOST="${REMOTE_HOST:-36.151.150.188}"
REMOTE_USER="${REMOTE_USER:-root}"
REMOTE_APP_DIR="${REMOTE_APP_DIR:-/opt/family-app}"
REMOTE_ENV_DIR="${REMOTE_ENV_DIR:-/etc/family-app}"
REMOTE_SERVICE_NAME="${REMOTE_SERVICE_NAME:-family-app.service}"
REMOTE_DB_NAME="${REMOTE_DB_NAME:-family_app}"
JAR_PATH="${JAR_PATH:-$ROOT_DIR/backend/family-service/target/family-service-1.0.0.jar}"
REMOTE_JAR_PATH="$REMOTE_APP_DIR/family-service-1.0.0.jar"
DB_PATCH_FILE="${DB_PATCH_FILE:-$ROOT_DIR/database/remote_sync_schema.sql}"
SERVICE_TEMPLATE="${SERVICE_TEMPLATE:-$ROOT_DIR/ops/family-app.service}"

if ! command -v ssh >/dev/null 2>&1; then
  echo "ssh 未安装" >&2
  exit 1
fi

if ! command -v scp >/dev/null 2>&1; then
  echo "scp 未安装" >&2
  exit 1
fi

if [ ! -f "$JAR_PATH" ]; then
  echo "未找到后端 JAR: $JAR_PATH" >&2
  echo "先执行: mvn -pl backend/family-service -am clean package" >&2
  exit 1
fi

if [ ! -f "$DB_PATCH_FILE" ]; then
  echo "未找到数据库同步脚本: $DB_PATCH_FILE" >&2
  exit 1
fi

if [ ! -f "$SERVICE_TEMPLATE" ]; then
  echo "未找到 systemd 模板: $SERVICE_TEMPLATE" >&2
  exit 1
fi

if [ -z "${REMOTE_PASSWORD:-}" ]; then
  echo "请设置 REMOTE_PASSWORD 环境变量后再执行" >&2
  exit 1
fi

if [ -z "${REMOTE_MYSQL_PASSWORD:-}" ]; then
  echo "请设置 REMOTE_MYSQL_PASSWORD 环境变量后再执行" >&2
  exit 1
fi

if [ -z "${LOCAL_ENV_FILE:-}" ]; then
  echo "请设置 LOCAL_ENV_FILE 指向远程服务器使用的 family-app.env 文件" >&2
  exit 1
fi

if [ ! -f "$LOCAL_ENV_FILE" ]; then
  echo "未找到环境变量文件: $LOCAL_ENV_FILE" >&2
  exit 1
fi

SSH_BASE=(sshpass -p "$REMOTE_PASSWORD" ssh -o StrictHostKeyChecking=no "${REMOTE_USER}@${REMOTE_HOST}")
SCP_BASE=(sshpass -p "$REMOTE_PASSWORD" scp -o StrictHostKeyChecking=no)

echo "上传数据库补丁和后端 JAR 到 ${REMOTE_HOST}"
"${SSH_BASE[@]}" "mkdir -p '$REMOTE_APP_DIR' '$REMOTE_ENV_DIR'"
"${SCP_BASE[@]}" "$DB_PATCH_FILE" "${REMOTE_USER}@${REMOTE_HOST}:${REMOTE_APP_DIR}/remote_sync_schema.sql"
"${SCP_BASE[@]}" "$JAR_PATH" "${REMOTE_USER}@${REMOTE_HOST}:${REMOTE_JAR_PATH}"
"${SCP_BASE[@]}" "$LOCAL_ENV_FILE" "${REMOTE_USER}@${REMOTE_HOST}:${REMOTE_ENV_DIR}/family-app.env"
"${SCP_BASE[@]}" "$SERVICE_TEMPLATE" "${REMOTE_USER}@${REMOTE_HOST}:/etc/systemd/system/${REMOTE_SERVICE_NAME}"

echo "执行远程数据库同步"
"${SSH_BASE[@]}" "mysql -uroot -p'${REMOTE_MYSQL_PASSWORD}' '${REMOTE_DB_NAME}' < '${REMOTE_APP_DIR}/remote_sync_schema.sql'"

echo "重载 systemd 并重启服务"
"${SSH_BASE[@]}" "systemctl daemon-reload && systemctl enable '${REMOTE_SERVICE_NAME}' && systemctl restart '${REMOTE_SERVICE_NAME}' && systemctl --no-pager --full status '${REMOTE_SERVICE_NAME}' | sed -n '1,40p'"

echo "部署完成"
