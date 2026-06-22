#!/usr/bin/env bash
# Phase 3a.1b GET /sandbox/today 读模型验收脚本
# 用法: ./scripts/check-route-dispatch-phase3a1b-read.sh [json_file]
# 若不传 json_file，需设置 BASE_URL / DIS_ID / ROUTE_DATE 并 curl 拉取。

set -euo pipefail

JSON_FILE="${1:-}"

if [[ -z "$JSON_FILE" ]]; then
  : "${BASE_URL:?请设置 BASE_URL 或传入 JSON 文件路径}"
  : "${DIS_ID:?请设置 DIS_ID}"
  ROUTE_DATE="${ROUTE_DATE:-$(date +%Y-%m-%d)}"
  BATCH_CODE="${BATCH_CODE:-MORNING}"
  JSON_FILE="$(mktemp /tmp/route_dispatch_read_XXXXXX.json)"
  curl -sS "${BASE_URL}/api/dis/route/sandbox/today?disId=${DIS_ID}&routeDate=${ROUTE_DATE}&batchCode=${BATCH_CODE}" \
    | python3 -c 'import json,sys; d=json.load(sys.stdin); print(json.dumps(d.get("data") or d, ensure_ascii=False, indent=2))' \
    > "$JSON_FILE"
  echo "已拉取 JSON -> $JSON_FILE"
fi

python3 - "$JSON_FILE" <<'PY'
import json, sys

path = sys.argv[1]
with open(path, encoding="utf-8") as f:
    raw = json.load(f)
data = raw.get("data", raw)

errors = []
warnings = []

# 一、drivers 结构：对象 { drivers[], summary, routeDate }
drivers_obj = data.get("drivers")
if not isinstance(drivers_obj, dict):
    errors.append("drivers 应为对象 { drivers, summary, routeDate }，当前类型: %s" % type(drivers_obj).__name__)
else:
    driver_list = drivers_obj.get("drivers")
    if not isinstance(driver_list, list):
        errors.append("drivers.drivers 应为数组")
    else:
        eligible = [d for d in driver_list if d.get("batchEligible") or d.get("dutyStatus") == "ON_DUTY"]
        print("drivers.drivers[] 共 %d 人，batchEligible/ON_DUTY 约 %d 人" % (len(driver_list), len(eligible)))

# 二、sandboxSuggestedStops
suggested = data.get("sandboxSuggestedStops") or []
print("sandboxSuggestedStops: %d 条" % len(suggested))

PRIMARY_KEYS = {
    "sandboxStopKey", "departmentId", "departmentName", "liveOrderIds", "items",
    "suggestedDriverUserId", "suggestedDriverName", "scheduleMode",
    "fastestArrivalLabel", "timeBasisLabel", "customerWindowLabel",
    "canConfirmCustomer", "confirmCustomerActionLabel", "confirmCustomerBlockedReason",
    "stopSource", "confirmViaSandbox",
}
LEGACY_PREFIXES = ("nxDrs", "nxDst", "shipmentTask")

for i, stop in enumerate(suggested):
    for bad in LEGACY_PREFIXES:
        hits = [k for k in stop.keys() if k.startswith(bad) or k == bad]
        if hits:
            errors.append("sandboxSuggestedStops[%d] 含旧字段 %s" % (i, hits[:3]))

    if stop.get("deliveryStopId") or stop.get("routeStopId") or stop.get("nxDrsId"):
        errors.append("sandboxSuggestedStops[%d] 不应含持久化 ID" % i)

    mode = stop.get("scheduleMode")
    fastest = stop.get("fastestArrivalLabel") or ""
    planned = stop.get("plannedArrivalLabel") or fastest
    if mode == "ADHOC_NOW":
        for label, name in [(fastest, "fastestArrivalLabel"), (planned, "plannedArrivalLabel")]:
            if label and "明天" in label:
                errors.append("ADHOC_NOW stop[%d] %s 仍含「明天」: %s" % (i, name, label))

    if stop.get("canConfirmCustomer") is True:
        if stop.get("confirmCustomerActionLabel") != "确认该店出货完成":
            warnings.append("stop[%d] confirmCustomerActionLabel 非预期" % i)
        if stop.get("canAssign") is True or stop.get("canConfirmLoad") is True:
            warnings.append("stop[%d] 旧 canAssign/canConfirmLoad 仍为 true（主按钮应看 canConfirmCustomer）" % i)
        blocked = stop.get("assignBlockedReason") or ""
        if "不能分派" in blocked:
            errors.append("stop[%d] 仍含旧 assign 文案: %s" % (i, blocked))
    elif stop.get("suggestedDriverUserId"):
        reason = stop.get("confirmCustomerBlockedReason") or ""
        if not reason:
            warnings.append("stop[%d] 有建议司机但 canConfirmCustomer=false 且无 confirmCustomerBlockedReason" % i)

# 三、confirmedStops 才有 deliveryStopId
confirmed = data.get("confirmedStops") or []
for i, stop in enumerate(confirmed):
    if not stop.get("deliveryStopId"):
        warnings.append("confirmedStops[%d] 缺少 deliveryStopId" % i)

perms = data.get("actionPermissions") or {}
print("actionPermissions.canConfirmCustomer =", perms.get("canConfirmCustomer"))

print("\n=== 结果 ===")
if warnings:
    print("WARN (%d):" % len(warnings))
    for w in warnings:
        print("  -", w)
if errors:
    print("FAIL (%d):" % len(errors))
    for e in errors:
        print("  -", e)
    sys.exit(1)
print("PASS: Phase 3a.1b 读模型检查通过")
PY
