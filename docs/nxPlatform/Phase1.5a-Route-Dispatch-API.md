# Phase 1.5a 路线派单 API 清单（OpenAPI 级）

> ⚠️ **已废弃 — 不可作为当前合同（2026-06-22）**  
> 归档副本：[`docs/route-dispatch/archive/old-contracts/Phase1.5a-Route-Dispatch-API-ARCHIVED.md`](../route-dispatch/archive/old-contracts/Phase1.5a-Route-Dispatch-API-ARCHIVED.md)  
> 当前正式口径：[`Route-Dispatch-Backend-Handoff-20260622.md`](../route-dispatch/Route-Dispatch-Backend-Handoff-20260622.md)

---

> Base path: `/api/nxdisroutedispatch`  
> 前置 SQL: `docs/sql/patches/upgrade_nx_dis_route_dispatch_phase1_5a.sql`  
> 保留骨架: `RouteCostProvider` / `RouteOptimizer` / `route_plan` / `driver_route` / `route_stop`  
> 不改: jcJieDan、旧 `disGetDriversOptimalRoute`、order/bill 原状态机

---

## ⚠️ Phase 3 动态沙盘（设计基准，优先阅读）

**当前实现仍是「simulate 写库 + GET 只读」模式，与目标业务不符。**

改造设计见：**[`Route-Dispatch-Sandbox-Design.md`](./Route-Dispatch-Sandbox-Design.md)**

核心变更方向：

| 概念 | 现状 | 目标 |
|------|------|------|
| 沙盘 | simulate 生成后像固定方案存 DB | **实时计算**建议分派，打开页面即最新 |
| 确认 | assign 锁单 task（已部分正确） | **局部锁定**，其他客户继续流动 |
| 读接口 | `GET /plan/today` 只读库 | `GET /sandbox/today` 读 + 自动 compute |
| simulate | 老板日常按钮 | **deprecated** → 内部 `SandboxComputeService` |

**订单主权**：未确认前 `nx_department_orders`；bill 后 `nx_department_order_history`；task_item 仅 ID 关联。

**前端**：onShow 自动拉沙盘；**不要**把「重新生成派车方案」当主流程。

---

## 状态与边界（全局）

| 概念 | 值 | 含义 |
|------|-----|------|
| **plan.status** | `SIMULATED` | 沙盘建议路线 |
| | `ASSIGNED` | 至少一个 task 已人工确认，可装车 |
| | `READY` | 全部 ACTIVE task 均为 `READY_TO_GO`，可全队出发 |
| | `CANCELLED` | 计划作废 |
| **task.status** | `SIMULATED` | 系统建议，未确认 |
| | `ASSIGNED` | 人工确认司机，`manualLocked=1`，可装车 |
| | `READY_TO_GO` | 全部 ACTIVE item 已 bill，可出发 |
| | `UNASSIGNED` | 无坐标 |
| | `CANCELLED` | 作废 |
| | `IN_DELIVERY` / `DELIVERED` | Phase 1 预留，不实现 |
| **item.status** | `ACTIVE` / `REMOVED` / `CANCELLED` | plan READY 仅统计 `ACTIVE` |

**硬边界**

- `do_status < 3`：仅 live order **进入沙盘候选**，≠ 可装车 ≠ 可出发
- 系统 simulate/reoptimize **只写** `suggestedDriverUserId`
- assign/move **只写** `assignedDriverUserId` + lock；bill **不得改** assigned
- `historyOrderId` / `billId` **权威在 item**；task 主表不存 historyOrderId
- 补单：已 `READY_TO_GO` / `IN_DELIVERY` 的 task 关闭（`open_key=NULL`）后，**新建 task**，不污染旧 task

---

## 1. POST `/simulate`

> **⚠️ Phase 3 起 deprecated（设计）**  
> 对外不应再作为老板日常「重新生成方案」主流程。  
> 语义将改为内部 **沙盘计算**（见 `Route-Dispatch-Sandbox-Design.md` §6）。  
> 目标接口：`GET /sandbox/today`（autoCompute）替代手动调用。

自动沙盘分派：从 eligible live order 生成/更新 task，运行优化器，产出/更新 `SIMULATED` plan。

### Request

```yaml
SimulateRequest:
  type: object
  required: [disId]
  properties:
    disId:
      type: integer
      description: 配送商 ID
    routeDate:
      type: string
      format: date
      description: 路线日，默认今天
    depotLat:
      type: string
    depotLng:
      type: string
    driverUserIds:
      type: array
      items: { type: integer }
      description: 参与分派的司机；空则取全部司机
    costProviderType:
      type: string
      enum: [TENCENT_MATRIX]
      default: TENCENT_MATRIX
    optimizerType:
      type: string
      enum: [BALANCED_INSERTION_2OPT]
      default: BALANCED_INSERTION_2OPT
    operatorUserId:
      type: integer
      description: 触发人（系统自动时可空）
```

### 内部行为（非对外）

1. 查询 eligible live orders（`do_status < 3`，路线日匹配，未取消）
2. 按 `(disId, routeDate, depFatherId)` upsert **open task**（`open_key` 非空）
3. 新 order 行 append **ACTIVE item**（`live_order_id` 唯一）
4. **不**覆盖已有 `manualLocked=1` task 的 assigned / seq
5. 优化器输出 → 仅更新 **unlocked** task 的 `suggestedDriverUserId`
6. 写入/更新 `SIMULATED` plan、`driver_route`、`route_stop`（stop 挂 `shipment_task_id`）
7. **不写** `assignedDriverUserId`

### Response `200`

```yaml
SimulateResponse:
  type: object
  properties:
    plan:
      $ref: '#/components/schemas/RoutePlanDetail'
```

### Errors

| HTTP | 场景 |
|------|------|
| 400 | 无 eligible order / 坐标无效 / 无司机 |
| 409 | 并发 simulate 冲突（可选） |

---

## 2. POST `/tasks/{taskId}/assign`

人工确认：客户/任务分给指定司机，进入 **ASSIGNED**，锁定。

### Request

```yaml
AssignTaskRequest:
  type: object
  required: [assignedDriverUserId, operatorUserId]
  properties:
    assignedDriverUserId:
      type: integer
    operatorUserId:
      type: integer
    assignReason:
      type: string
    manualStopSeq:
      type: integer
      description: 可选，固定在该司机路线中的序号
```

### 行为

- `task.status` → `ASSIGNED`
- 写 `assignedDriverUserId`, `manualLocked=1`, `assignConfirmedAt`, `operatorUserId`, `assignReason`
- 更新 stop 所属 `driver_route` / `stop_seq`
- `reconcilePlanStatus` → plan 至少 `ASSIGNED`
- **不改** item；**不改** suggested（可保留供对比）

### Response `200`

```yaml
  properties:
    task:
      $ref: '#/components/schemas/ShipmentTaskDetail'
    plan:
      $ref: '#/components/schemas/RoutePlanDetail'
```

---

## 3. POST `/tasks/{taskId}/move`

人工调线（含强制修改已锁定 task 的唯一算法入口）。

### Request

```yaml
MoveTaskRequest:
  type: object
  required: [assignedDriverUserId, operatorUserId, adjustReason]
  properties:
    assignedDriverUserId:
      type: integer
    manualStopSeq:
      type: integer
    operatorUserId:
      type: integer
    adjustReason:
      type: string
      description: 必填，审计
```

### 行为

- 更新 assigned driver / stop seq
- 保持或设置 `manualLocked=1`
- 写 `adjustReason`, `operatorUserId`
- reoptimize **不得**替代此操作

---

## 4. POST `/tasks/{taskId}/unlock`

解除锁定，允许后续 reoptimize 调整（ rarely used）。

### Request

```yaml
UnlockTaskRequest:
  type: object
  required: [operatorUserId]
  properties:
    operatorUserId:
      type: integer
    adjustReason:
      type: string
```

### 行为

- `manualLocked` → `0`
- task 保持 `ASSIGNED`（推荐）或降级 `SIMULATED`（产品配置）
- **不**自动清除 `assignedDriverUserId`（避免装车清单闪烁）

---

## 5. POST `/plan/{planId}/reoptimize`

在现有 plan 上重算路线，**尊重 manualLocked**。

### Request

```yaml
ReoptimizeRequest:
  type: object
  properties:
    operatorUserId:
      type: integer
    depotLat:
      type: string
    depotLng:
      type: string
    costProviderType:
      type: string
    optimizerType:
      type: string
```

### 行为

1. 加载 plan 下所有 **ACTIVE** task + stops
2. **Locked**（`manualLocked=1`）：固定 `assignedDriverUserId` + `manualStopSeq`（若有）
3. **Unlocked**：参与 balanced insertion + 2-opt；更新 `suggestedDriverUserId` 与 stop 顺序
4. 刷新 leg distance/duration；**不**改 locked task 的 assigned
5. plan 状态不变（仍为 SIMULATED 或 ASSIGNED）

### Errors

| 400 | plan 已 CANCELLED |
| 409 | plan 已 READY（可选禁止重算） |

---

## 6. GET `/driver/loading/today`

司机装车页：人工确认后、打单完成前。

### Query

| 参数 | 必填 | 说明 |
|------|------|------|
| `driverUserId` | 是 | 司机用户 ID |

### 筛选

```text
task.status = ASSIGNED
AND task.assignedDriverUserId = driverUserId
AND task.routeDate = today
AND item.item_status = ACTIVE
```

### Response `200`

```yaml
DriverLoadingResponse:
  properties:
    routeDate:
      type: string
    plan:
      $ref: '#/components/schemas/RoutePlanSummary'
    tasks:
      type: array
      items:
        $ref: '#/components/schemas/ShipmentTaskDetail'
```

---

## 7. GET `/driver/delivery/today`

司机正式配送页：打单完成后可出发。

### Query

| 参数 | 必填 |
|------|------|
| `driverUserId` | 是 |

### 筛选（二选一组合，实现取并集展示）

```text
A) task.status = READY_TO_GO AND assignedDriverUserId = me AND routeDate = today
B) plan.status = READY AND stop/task assignedDriverUserId = me
```

### Response `200`

同 `DriverLoadingResponse` 结构；items 含 `billId`, `historyOrderId`。

---

## 8. GET `/plan`

调度查沙盘 / 装车 / 出发全览。

### Query

| 参数 | 必填 | 默认 |
|------|------|------|
| `disId` | 是 | |
| `routeDate` | 是 | |
| `status` | 否 | `SIMULATED` |

`status` 枚举: `SIMULATED` | `ASSIGNED` | `READY` | `CANCELLED`

### Response `200`

```yaml
PlanQueryResponse:
  properties:
    routeDate:
      type: string
    plan:
      $ref: '#/components/schemas/RoutePlanDetail'
```

### `RoutePlanDetail`（摘要 schema）

```yaml
RoutePlanDetail:
  properties:
    planId: { type: integer }
    disId: { type: integer }
    routeDate: { type: string }
    dispatchDate: { type: string, nullable: true }
    status: { type: string }
    depotLat: { type: string }
    depotLng: { type: string }
    totalDistanceM: { type: integer }
    totalDurationS: { type: integer }
    driverRoutes:
      type: array
      items:
        type: object
        properties:
          driverRouteId: { type: integer }
          driverUserId: { type: integer }
          driverName: { type: string }
          stopCount: { type: integer }
          stops:
            type: array
            items:
              $ref: '#/components/schemas/RouteStopDetail'

RouteStopDetail:
  properties:
    stopId: { type: integer }
    stopSeq: { type: integer }
    shipmentTaskId: { type: integer }
    stopStatus: { type: string }
    legDistanceM: { type: integer }
    legDurationS: { type: integer }
    task:
      $ref: '#/components/schemas/ShipmentTaskDetail'

ShipmentTaskDetail:
  properties:
    taskId: { type: integer }
    status: { type: string }
    depFatherId: { type: integer }
    depName: { type: string }
    lat: { type: string }
    lng: { type: string }
    address: { type: string }
    suggestedDriverUserId: { type: integer, nullable: true }
    assignedDriverUserId: { type: integer, nullable: true }
    manualLocked: { type: boolean }
    manualStopSeq: { type: integer, nullable: true }
    priorityLevel: { type: integer }
    assignConfirmedAt: { type: string, format: date-time, nullable: true }
    operatorUserId: { type: integer, nullable: true }
    assignReason: { type: string, nullable: true }
    adjustReason: { type: string, nullable: true }
    items:
      type: array
      items:
        $ref: '#/components/schemas/ShipmentTaskItemDetail'

ShipmentTaskItemDetail:
  properties:
    itemId: { type: integer }
    liveOrderId: { type: integer }
    historyOrderId: { type: integer, nullable: true }
    billId: { type: integer, nullable: true }
    goodsName: { type: string }
    quantity: { type: string }
    standard: { type: string }
    remark: { type: string }
    itemStatus: { type: string }
```

---

## 9. 内部服务：`DisShipmentTaskService.onBillPrinted`

**非 HTTP 对外接口**；由 `NxDepartmentBillController` 打印成功后一行调用。

### Java 签名（草案）

```java
void onBillPrinted(Integer billId, List<BillPrintedOrderRef> refs);

class BillPrintedOrderRef {
    Integer liveOrderId;
    Integer historyOrderId;
    Integer depFatherId;
    Integer disId;
}
```

### 行为

```
FOR each ref:
  FIND item BY live_order_id AND item_status = ACTIVE
  UPDATE item.history_order_id, item.bill_id

FOR each affected task:
  IF all ACTIVE items have bill_id:
    task.status = READY_TO_GO
    CLEAR task.open_key          -- 关闭 task，允许同日补单新建 task
  ELSE:
    remain ASSIGNED              -- 部分打单

reconcilePlanStatus(planId):
  IF all ACTIVE tasks on plan are READY_TO_GO:
    plan.status = READY
    plan.ready_at / dispatch_date = now
  ELSE IF any task ASSIGNED:
    plan.status = ASSIGNED

NEVER update task.assigned_driver_user_id / manual_locked / assign_* fields
NEVER update route_stop driver assignment
```

### Hook 落点（只读设计）

| 入口 | 时机 |
|------|------|
| `NxDepartmentBillController.saveAccountBillPrinter` | moveOrderToHistory 成功后 |
| `saveAccountBillPrinterSelf` / `Phone*` | 同上 |
| GB 协同 bill 分支 | 同上（若产生 history） |

---

## 10. 内部服务：`DisShipmentTaskService.onBillReverted`（预留）

**非 HTTP**；`deleteBillAgain*` 成功后调用。

### 行为（设计预留，Phase 1.5c 实现）

```
FOR each item with bill_id = revertedBillId:
  CLEAR item.bill_id, item.history_order_id

FOR each affected task:
  IF was READY_TO_GO:
    task.status = ASSIGNED           -- 保留 assignedDriverUserId
    RESTORE task.open_key            -- 重新占用「未关闭」槽位（若尚无其他 open task）

reconcilePlanStatus:
  plan READY → ASSIGNED if any task not READY_TO_GO
```

---

## 11. 现有 Phase 1 接口处置

| 原接口 | Phase 1.5a |
|--------|------------|
| `POST /preview` | 替换为 **`POST /simulate`**（语义见 §1） |
| `POST /confirm` | **废弃**；由 assign + reconcile 替代 |
| `GET /driver/route/today` | 拆为 **`/driver/loading/today`** + **`/driver/delivery/today`** |
| `GET /drivers` | 保留 |
| `GET /plan/{planId}` | 保留；响应增加 task 嵌套 |
| `GET /plan`, `/plan/today` | 保留；`status` 默认改为 `SIMULATED` |

---

## 12. `reconcilePlanStatus` 规则（内部）

仅统计 **ACTIVE** task / item：

| 条件 | plan.status |
|------|-------------|
| 无 active task 或全部 CANCELLED | 不升 READY |
| 存在 ASSIGNED 且非全部 READY_TO_GO | `ASSIGNED` |
| 全部 active task = READY_TO_GO | `READY` |
| 无任何 ASSIGNED/READY task，仅 SIMULATED | `SIMULATED` |

---

## 13. Phase 1.5a 实现范围（不含）

- jcJieDan / 前端
- 旧 `disGetDriversOptimalRoute`
- order/bill `do_status` / `purchase_status` 写入变更
- `IN_DELIVERY` / `DELIVERED` 司机执行 API
- Runner 自动化测试
- stop_order 表 DROP（仅停止写入）

---

## 14. Phase 3 规划接口

### GET `/sandbox/today` ✅ Phase 3a

今日派车主读接口：**打开即内存 sandbox compute**，不写未确认客户到 DB。

| 参数 | 默认 | 说明 |
|------|------|------|
| `disId` | 必填 | |
| `routeDate` | 今天 | |
| `batchCode` | `MORNING` | |

响应关键字段：`confirmedStops[]`、`sandboxSuggestedStops[]`、`unassignedStops[]`、`invalidStops[]`、`sandboxVersion`、`orderVersion`、`actionPermissions`。

### POST `/sandbox/stops/confirm` ✅ Phase 3a

确认沙盘客户（**无需 taskId**）。响应为完整 `sandbox/today` 读模型。

### POST `/tasks/{taskId}/return-to-sandbox`（Phase 3c，未实现）

保留调试；日常由 GET sandbox 触发内部 compute。
