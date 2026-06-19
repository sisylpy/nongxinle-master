#!/usr/bin/env bash
#
# 从 Navicat 导出的 SQL 恢复 nongxinle 到本地 MySQL，
# 跳过数据量过大的两张历史表（仅保留表结构，不导入数据）：
#   - nx_department_order_history
#   - nx_department_orders_history
#
# 用法:
#   ./bfData/import-nongxinle-local.sh
#   ./bfData/import-nongxinle-local.sh /path/to/dump.sql
#   SAVE_FILTERED=1 ./bfData/import-nongxinle-local.sh   # 同时保存过滤后的 SQL
#

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SOURCE_SQL="${1:-${SCRIPT_DIR}/nongxinle0620.sql}"

MYSQL_BIN="${MYSQL_BIN:-/usr/local/mysql/bin/mysql}"
MYSQL_HOST="${MYSQL_HOST:-127.0.0.1}"
MYSQL_PORT="${MYSQL_PORT:-3306}"
MYSQL_USER="${MYSQL_USER:-root}"
MYSQL_PASSWORD="${MYSQL_PASSWORD:-Lpy87176693}"
MYSQL_DATABASE="${MYSQL_DATABASE:-nongxinle}"

# 与 NxDepartmentOrderHistoryEntity / NxDepartmentOrdersHistoryEntity 对应
EXCLUDED_TABLES=(
  "nx_department_order_history"
  "nx_department_orders_history"
)

FILTERED_SQL="${SCRIPT_DIR}/nongxinle0620_no_history_data.sql"
SAVE_FILTERED="${SAVE_FILTERED:-0}"

log() { printf '[%s] %s\n' "$(date '+%H:%M:%S')" "$*"; }
die() { log "ERROR: $*"; exit 1; }

[[ -f "$SOURCE_SQL" ]] || die "找不到 SQL 文件: $SOURCE_SQL"
[[ -x "$MYSQL_BIN" || -n "$(command -v "$MYSQL_BIN" 2>/dev/null || true)" ]] || die "找不到 mysql 客户端: $MYSQL_BIN"

export MYSQL_PWD="$MYSQL_PASSWORD"

log "源文件: $SOURCE_SQL ($(du -h "$SOURCE_SQL" | awk '{print $1}'))"
log "目标库: ${MYSQL_USER}@${MYSQL_HOST}:${MYSQL_PORT}/${MYSQL_DATABASE}"
log "排除数据表: ${EXCLUDED_TABLES[*]}（保留 CREATE TABLE）"

log "检查 MySQL 连接..."
"$MYSQL_BIN" -h"$MYSQL_HOST" -P"$MYSQL_PORT" -u"$MYSQL_USER" -e "SELECT 1" >/dev/null

log "确保数据库存在..."
"$MYSQL_BIN" -h"$MYSQL_HOST" -P"$MYSQL_PORT" -u"$MYSQL_USER" \
  -e "CREATE DATABASE IF NOT EXISTS \`${MYSQL_DATABASE}\` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"

filter_sql() {
  awk '
    function is_excluded_records(line,   t) {
      for (t in excluded) {
        if (line ~ ("^-- Records of " t "$")) return 1
      }
      return 0
    }
    function is_excluded_insert(line,   t) {
      for (t in excluded) {
        if (line ~ ("^INSERT INTO `" t "`")) return 1
      }
      return 0
    }
    BEGIN {
      excluded["nx_department_order_history"] = 1
      excluded["nx_department_orders_history"] = 1
      skip_data = 0
    }
    is_excluded_records($0) {
      skip_data = 1
      print
      print "BEGIN;"
      print "COMMIT;"
      next
    }
    skip_data {
      if (is_excluded_insert($0)) next
      if ($0 ~ /^BEGIN;/) next
      if ($0 ~ /^COMMIT;/) { skip_data = 0; next }
    }
    { print }
  '
}

START_TS=$(date +%s)

if [[ "$SAVE_FILTERED" == "1" ]]; then
  log "生成过滤后的 SQL: $FILTERED_SQL"
  filter_sql < "$SOURCE_SQL" > "$FILTERED_SQL"
  log "过滤完成: $(du -h "$FILTERED_SQL" | awk '{print $1}')"
  log "开始导入..."
  "$MYSQL_BIN" -h"$MYSQL_HOST" -P"$MYSQL_PORT" -u"$MYSQL_USER" "$MYSQL_DATABASE" < "$FILTERED_SQL"
else
  log "流式过滤并导入（不落地中间文件）..."
  filter_sql < "$SOURCE_SQL" | "$MYSQL_BIN" -h"$MYSQL_HOST" -P"$MYSQL_PORT" -u"$MYSQL_USER" "$MYSQL_DATABASE"
fi

END_TS=$(date +%s)
ELAPSED=$((END_TS - START_TS))

log "导入完成，耗时 ${ELAPSED}s"
log "校验被排除表（应存在但 row=0）..."

for tbl in "${EXCLUDED_TABLES[@]}"; do
  COUNT=$("$MYSQL_BIN" -h"$MYSQL_HOST" -P"$MYSQL_PORT" -u"$MYSQL_USER" -N -e \
    "SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA='${MYSQL_DATABASE}' AND TABLE_NAME='${tbl}';")
  ROWS=$("$MYSQL_BIN" -h"$MYSQL_HOST" -P"$MYSQL_PORT" -u"$MYSQL_USER" -N -e \
    "SELECT COUNT(*) FROM \`${MYSQL_DATABASE}\`.\`${tbl}\`;")
  log "  ${tbl}: table_exists=${COUNT}, rows=${ROWS}"
done

TOTAL_TABLES=$("$MYSQL_BIN" -h"$MYSQL_HOST" -P"$MYSQL_PORT" -u"$MYSQL_USER" -N -e \
  "SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA='${MYSQL_DATABASE}';")
log "库 ${MYSQL_DATABASE} 共 ${TOTAL_TABLES} 张表"

unset MYSQL_PWD
log "全部完成。"
