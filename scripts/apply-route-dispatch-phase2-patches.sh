#!/usr/bin/env bash
#
# 配送路线派单 Phase 2a + Phase 2b-1 SQL patch 一键执行
#
# 用法:
#   ./scripts/apply-route-dispatch-phase2-patches.sh
#   MYSQL_DATABASE=chain_order ./scripts/apply-route-dispatch-phase2-patches.sh
#
# 连接优先级:
#   1. 环境变量 MYSQL_HOST / MYSQL_USER / MYSQL_PASSWORD / MYSQL_DATABASE
#   2. src/main/resources/jdbc.properties（若存在）
#   3. generatorConfig.xml 默认: chain_order / root / swolo123
#

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
PATCH_DIR="$REPO_ROOT/docs/sql/patches"
JDBC_PROPS="$REPO_ROOT/src/main/resources/jdbc.properties"

MYSQL_BIN="${MYSQL_BIN:-mysql}"
if ! command -v "$MYSQL_BIN" >/dev/null 2>&1 && [[ -x /usr/local/mysql/bin/mysql ]]; then
  MYSQL_BIN="/usr/local/mysql/bin/mysql"
fi

MYSQL_HOST="${MYSQL_HOST:-127.0.0.1}"
MYSQL_PORT="${MYSQL_PORT:-3306}"
MYSQL_USER="${MYSQL_USER:-root}"
MYSQL_PASSWORD="${MYSQL_PASSWORD:-}"
MYSQL_DATABASE="${MYSQL_DATABASE:-}"

log() { printf '[%s] %s\n' "$(date '+%H:%M:%S')" "$*"; }
die() { log "ERROR: $*"; exit 1; }

load_jdbc_properties() {
  [[ -f "$JDBC_PROPS" ]] || return 0

  log "读取 $JDBC_PROPS"
  while IFS='=' read -r key value; do
    [[ -z "${key// }" ]] && continue
    [[ "$key" =~ ^[[:space:]]*# ]] && continue
    key="$(echo "$key" | tr -d '[:space:]')"
    value="$(echo "$value" | sed 's/^[[:space:]]*//;s/[[:space:]]*$//')"
    case "$key" in
      jdbc.url)
        if [[ "$value" =~ jdbc:mysql://([^:/]+)(:([0-9]+))?/([^?]+) ]]; then
          MYSQL_HOST="${BASH_REMATCH[1]}"
          [[ -n "${BASH_REMATCH[3]:-}" ]] && MYSQL_PORT="${BASH_REMATCH[3]}"
          MYSQL_DATABASE="${BASH_REMATCH[4]}"
        fi
        ;;
      jdbc.username) MYSQL_USER="$value" ;;
      jdbc.password) MYSQL_PASSWORD="$value" ;;
    esac
  done < "$JDBC_PROPS"
}

apply_defaults() {
  if [[ -z "$MYSQL_DATABASE" ]]; then
    MYSQL_DATABASE="chain_order"
    log "未配置库名，使用默认: $MYSQL_DATABASE"
  fi
  if [[ -z "$MYSQL_PASSWORD" ]]; then
    MYSQL_PASSWORD="swolo123"
    log "未配置密码，使用 generatorConfig 默认（chain_order 本地开发）"
  fi
}

mysql_cmd() {
  "$MYSQL_BIN" -h"$MYSQL_HOST" -P"$MYSQL_PORT" -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$@"
}

run_patch() {
  local label="$1"
  local file="$2"
  [[ -f "$file" ]] || die "找不到 patch: $file"

  log "执行 $label -> $(basename "$file")"
  set +e
  local output
  output="$(mysql_cmd "$MYSQL_DATABASE" < "$file" 2>&1)"
  local code=$?
  set -e

  if [[ $code -eq 0 ]]; then
    log "$label 成功"
    return 0
  fi

  if echo "$output" | grep -qi "Duplicate column"; then
    log "$label 已存在（Duplicate column），跳过"
    return 0
  fi

  echo "$output" >&2
  die "$label 失败 (exit=$code)"
}

verify_columns() {
  log "验证新列..."
  mysql_cmd "$MYSQL_DATABASE" -e "
SHOW COLUMNS FROM nx_dis_driver_route LIKE 'nx_ddr_dispatch%';
SHOW COLUMNS FROM nx_dis_driver_route LIKE 'nx_ddr_feasibility%';
SHOW COLUMNS FROM nx_dis_route_plan LIKE 'nx_drp_dispatch%';
SHOW COLUMNS FROM nx_dis_route_plan LIKE 'nx_drp_feasibility%';
"
}

main() {
  command -v "$MYSQL_BIN" >/dev/null 2>&1 || die "找不到 mysql 客户端，可设置 MYSQL_BIN=/usr/local/mysql/bin/mysql"

  load_jdbc_properties
  apply_defaults

  log "目标库: $MYSQL_USER@$MYSQL_HOST:$MYSQL_PORT/$MYSQL_DATABASE"

  run_patch "Phase 2a schedule" "$PATCH_DIR/upgrade_nx_dis_route_schedule_phase2.sql"
  run_patch "Phase 2b-1 batch/feasibility" "$PATCH_DIR/upgrade_nx_dis_route_dispatch_phase2b1.sql"
  verify_columns

  log "全部完成。可重跑 assess / schedule 验收。"
}

main "$@"
