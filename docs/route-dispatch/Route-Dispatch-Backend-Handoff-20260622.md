# 配送派单后端交接文档（2026-06-22）

> 范围：Phase 2B 沙盘派单 + 人工调度 + 装车/配送执行 + 司机可派状态。  
> 原则：**正式页面只消费 `pageViewModel` / 统一模板**；旧字段短期保留兼容，前端不得再兜底。  
> **HTTP 变更（2026-06-23）**：顶层 `POST /simulate` **已删**；`GET /driver/loading/today`、`GET /driver/delivery/today`、`GET /driver/route/today` **已删**（Controller 无注册；未来司机端须新建 pageViewModel 接口，不得复用上述路径）。

---

## 1. 后端总览

派单后端主链：

```text
NxDisRouteDispatchController
  → DisRouteSandboxComputeService（compute / mergedPlan 分区）
  → DisRouteSandboxTodayServiceImpl（today 读模型）
  → DisRouteSandboxManualDispatchServiceImpl（人工调度）
  → DisRouteDriverDispatchListServiceImpl（司机可派列表）
  → RouteDispatchReadModelAssembler / DisRouteSandboxTodayViewModelMaps（显式 Map 契约）
  → DisRouteDispatchCardTemplateBuilder（DispatchStoreCardDto / DispatchDriverCardDto）
```

写操作（confirm / depart / loading gate / delivery complete）分散在各自 Service，**confirm 人工调度落库尚未开放**（`confirmEnabled=false`）。

---

## 2. 页面 → 接口 → Service → 契约

| 页面 | 正式接口 | Service / Builder | 正式契约 | 旧/调试字段 |
|------|----------|-------------------|----------|-------------|
| 今日派单（老板） | `GET /dispatch/sandbox/today` | `buildDispatchSandboxToday` | `data.pageViewModel` | **正式** |
| 今日派单（jcJieDan 暂用） | `GET /sandbox/today` | `buildToday` | 全量 + `pageViewModel`；**debug-only**，应迁正式路径 | deprecated |
| 装车页（老板） | `GET /dispatch/loading/today` | `buildLoadingSandboxToday` → `extractLoadingPageContract` | `data.pageViewModel` | — |
| 装车页 | `GET /loading/today` | `buildLoadingToday` → `extractLoadingSlice` | `data.pageViewModel` + 元数据 | ~~loadingDriverRoutes/plan~~ **已删（2026-06-22 收口）** |
| 配送任务页 | `GET /delivery/today` | `buildDeliveryToday` → `extractDeliverySlice` | `data.pageViewModel`（`EXECUTION_DRIVER_ROUTES`） | ~~executionDriverRoutes/plan~~ **已删（2026-06-22 收口）** |
| 司机可派状态 | `GET /drivers/available` | `DisRouteDriverDispatchListServiceImpl.listDriversForBatch` | `driverCards[]` + `summary.summaryLine` | `drivers[]`（DriverDispatchCandidateDto） |
| 人工调度选司机 | `POST /sandbox/manual-dispatch/driver-panorama` | `DisRouteSandboxManualDispatchServiceImpl.listDriverPanorama` | `storeCard` + `drivers[]`（DispatchDriverCardDto）+ `summary.summaryLine` | — |
| 人工路线编辑 | `POST /sandbox/manual-dispatch/edit-page` | `DisRouteSandboxManualDispatchEditPageBuilder.build` | `data.pageViewModel` | — |
| 人工模拟 | `POST /sandbox/manual-dispatch/simulate` | `DisRouteSandboxManualDispatchSimulator` | simulate 响应体 | — |
| 人工确认 | `POST /sandbox/manual-dispatch/confirm` | 未完全开放 | — | — |
| 司机端装车 | ~~`GET /driver/loading/today`~~ | — | **已删 HTTP**（2026-06-23） | 未来新建 pageViewModel；**勿再调用** |
| 司机端配送 | ~~`GET /driver/delivery/today`~~ | — | **已删 HTTP**（2026-06-23） | 同上 |
| 司机端路线（旧） | ~~`GET /driver/route/today`~~ | — | **已删 HTTP**（2026-06-23） | 已拆为 loading/delivery，两路径均已删 |
| 司机站点详情 | 无独立接口 | — | **缺口**：无 `pageViewModel.stopDetail` | 前端 KNOWN GAP |
| 确认沙盘站点 | `POST /sandbox/stops/confirm` | `DisRouteSandboxConfirmService` | 动作响应 | — |
| 进入装车 | `POST /driver-routes/{id}/enter-loading` | `DisRouteSandboxRouteLoadingGateService` | 动作 + 可刷新 loading slice | — |
| 配送完成 | `POST /delivery/stops/{id}/complete` | `DisRouteSandboxDeliveryExecutionService` | 动作响应 | — |

---

## 3. 核心类职责

| 类 | 作用 | 使用接口 | 主链/兼容 | 后续 |
|----|------|----------|-----------|------|
| `DispatchStoreCardDto` | 统一分店卡 | driver-panorama `storeCard` | **正式** | 扩展 manualTimeConstraint |
| `DispatchDriverCardDto` | 统一司机卡 | driver-panorama、duty `driverCards`、delivery `driverCard` | **正式** | 各页逐步统一 |
| `DisRouteDispatchCardTemplateBuilder` | 组装 Store/Driver 卡 | 上述 + delivery execution | **正式** | 合并重复格式化 |
| `DisRouteSandboxTodayPageViewModelBuilder` | 今日/装车/配送 pageViewModel | sandbox/today、loading、delivery | **正式** | — |
| `DisRouteSandboxTodayDriverRouteCardBuilder` | DRIVER_ROUTE section 卡 + timeline | pageViewModel sections | **正式** | delivery 复用 EXECUTION enrichment |
| `DisRouteSandboxTodayTimelineBuilder` | timeline[] 节点 | 所有 DRIVER_ROUTE 卡 | **正式** | — |
| `DisRouteSandboxTodayViewModelMaps` | ViewModel → 显式 Map | dispatch/loading/delivery 正式契约 | **正式** | — |
| `RouteDispatchReadModelAssembler` | 旧 Map 读模型 enrichment | sandbox/today 全量、executionDriverRoutes | **过渡** | delivery 正式已迁 pageViewModel |
| `DisRouteDispatchDriverNameHelper` | 司机名稳定解析 | delivery executionDriverRoutes、driverCard | **正式** | — |
| `DisRouteManualTimeConstraintHelper` | manualTimeConstraint.summaryLabel | edit-page、storeCard | **正式** | — |
| `DisRouteManualDispatchEditPageTimelineBuilder` | 人工编辑 routeTimeline[] | edit-page | **正式** | — |
| `DisRouteSandboxManualDispatchEditPageBuilder` | 人工编辑 pageViewModel | edit-page | **正式** | — |
| `DriverDispatchCandidateDto` | 旧司机列表项 | drivers/available `drivers[]` | **兼容** | duty 页已迁 driverCards；**删除条件见 §7** |

---

## 4. 统一模板接入情况

### DispatchStoreCardDto

| 字段 | driver-panorama | edit-page customer | today UNASSIGNED |
|------|-----------------|-------------------|------------------|
| customerName / goodsSummary | ✅ | ✅（customer DTO） | ✅ section 卡 |
| distanceText / durationText | ✅ | 部分 | ✅ |
| plannedArrival/Departure | ✅ | 部分 | ✅ |
| customerWindowLabel | ✅ | ✅ | ✅ |
| serviceDurationLabel | ✅ | 部分 | ✅ |
| dispatchStatusLabel | ✅ | — | ✅ |
| manualTimeConstraint | ✅（empty 对象） | ✅ incomingManualTimeConstraint | — |
| manualTimeConstraint.summaryLabel | ✅（2026-06-22 补） | ✅ | — |

### DispatchDriverCardDto

| 字段 | driver-panorama | duty driverCards | delivery driverCard |
|------|-----------------|------------------|---------------------|
| driverName | ✅ | ✅ | ✅（含缺失兜底） |
| dutyStatus/Label | ✅ | ✅ | ✅ |
| dispatchStage/Label | ✅ | ✅ | ✅ |
| plannedDeparture/estimatedReturn | ✅ | ✅ | ✅ |
| totalDistance/Duration Text | ✅ | ✅ | ✅ |
| customer/completed/pending Count | ✅ | ✅ | ✅ |
| canSimulate/canConfirm/confirmMode | ✅ | —（duty 无） | — |
| confirmModeLabel | ✅（2026-06-22 补） | — | — |
| canToggleDuty/toggleDisabledReason | — | ✅ | — |
| primaryAction | ✅ simulate | ✅ toggle duty | — |
| riskHints/operationHint | ✅ | ✅ | ✅ |

**未接入统一模板（仍用 SandboxTodaySectionCardDto / 实体）：**

- 今日派单 pageViewModel sections（DRIVER_ROUTE 扁平行，非 DispatchDriverCardDto）
- 装车 pageViewModel sections（同上）
- 司机端 loading/delivery（`NxDisDriverRouteEntity` 直出）

---

## 5. 业务边界检查

### 5.1 旧订单状态 vs 派单状态

| 位置 | 说明 | 风险 |
|------|------|------|
| `nx_dst_status`（ShipmentTask） | 派单/配送主状态（ASSIGNED/READY_TO_GO/IN_DELIVERY/DELIVERED） | **正确主链** |
| `nx_do_purchase_status` | 装车确认时**可选**写入（ConfirmLoadingRequest） | 仅采购侧同步，非派单 UI 主状态 |
| `RouteDispatchReadModelAssembler` loading stops | 附带 `purchaseStatus`/`purchaseStatusLabel` 给装车页 orders[] | **展示兼容**，非派单阶段 |
| `do_status` | 未在派单读模型中作为 dispatchStage | ✅ |

长期目标 `DRAFT→CONFIRMED→IN_DELIVERY→DELIVERED→SIGNED`：**ShipmentTask 已部分覆盖，SIGN 未独立**。

### 5.2 人工调度司机准入

`DisRouteSandboxManualDispatchPanoramaHelper.resolveCapabilities`：

- OFF_DUTY：driver-panorama **跳过**（不进入 drivers[]）
- ON_DUTY 全阶段：`canSimulate=true`, `canConfirm=true`
- LOADING/EXECUTION/COMPLETED → `confirmMode=RISK_ACK` + riskHints
- IDLE/SANDBOX/CONFIRMED → `confirmMode=DIRECT`

### 5.3 司机可派开关

`DisRouteDriverDutyToggleHelper.canCloseDuty`：

- 可关：IDLE / SANDBOX / COMPLETED / 空
- 不可关：CONFIRMED / LOADING / EXECUTION

duty 页 `driverCards[]` 返回 `canToggleDuty`、`toggleDisabledReason`、`primaryAction.label`（开启/关闭可派）。

---

## 6. 已知缺口确认（2026-06-22）

| # | 项 | 状态 |
|---|-----|------|
| 1 | driver-panorama `summary.summaryLine` | ✅ 已补 |
| 2 | driver-panorama `drivers[].confirmModeLabel` | ✅ 本次补 |
| 3 | duty `summary.summaryLine` | ✅ 已补 |
| 4 | duty `driverCards[].primaryAction.label` | ✅ 已有（开启/关闭可派） |
| 5 | edit-page `simulationSummary.deltaLine` | ✅ 已补（manualStopSeq 时） |
| 6 | edit-page `incomingManualTimeConstraint.summaryLabel` | ✅ 本次补 |
| 7 | edit-page `stops[].manualConstraintSummary` | ✅ 本次补 |
| 8 | edit-page `pageViewModel.routeTimeline[]` | ✅ 已补（`DisRouteManualDispatchEditPageTimelineBuilder`） |
| 9 | delivery/today pageViewModel DRIVER_ROUTE | ✅ 已补 |
| 10 | delivery 第二司机 driverName + driverCard | ✅ DisRouteDispatchDriverNameHelper 兜底 |
| 11 | driverStopDetail 独立 stopDetail pageViewModel | ❌ **仍缺**；无专用接口 |

---

## 7. 旧代码 / 兼容清单（2026-06-22 收口）

### 本次已删除

| 项 | 原因 |
|----|------|
| `SandboxManualDispatchDriverPanoramaDriverDto` | 零引用，已被 `DispatchDriverCardDto` 替代 |
| `POST /preview` | 仅返回错误；jcJieDan / 后端无调用方 |
| `POST /confirm`（顶层旧 confirm） | 仅返回错误；正式链为 `POST /tasks/{id}/assign` 与 `POST /sandbox/stops/confirm` |
| `GET /loading/today`、`GET /delivery/today` 响应中的旧 Map 字段 | 正式页面只读 `pageViewModel` |
| `POST /simulate`（顶层 HTTP） | 全仓无正式调用方 |
| `GET /driver/loading/today`、`GET /driver/delivery/today`、`GET /driver/route/today` | jcJieDan 无页面调用 |
| `DriverRouteTasksResponse` + 旧实体直出 Service 方法 | 随上述 HTTP 删除 |
| `SIMULATION_ONLY` confirmMode 枚举值 | 无业务路径 |
| `simulationOnlyDriverCount` | 恒 0 占位字段 |
| `drivers/available` HTTP 响应中的 `drivers[]` | duty 页只用 `driverCards[]`；`@JSONField(serialize=false)` |

### 顶层 POST /simulate、POST /confirm 引用审计

| 接口 | 调用方 | 与 manual-dispatch 链关系 | 结论 |
|------|--------|---------------------------|------|
| `POST /confirm` | **无**（jcJieDan 未封装、无页面调用） | 与 `POST /sandbox/manual-dispatch/confirm` **无关**（后者为人工调度确认，尚未开放） | **已删 HTTP 入口** |
| `POST /preview` | **无** | — | **已删 HTTP 入口** |
| `POST /simulate`（顶层） | **无**（jcJieDan `simulateRoute` 无页面 import；无脚本；无 Java HTTP 自调） | 与 `POST /sandbox/manual-dispatch/simulate` **无关** | **已删 HTTP 入口**（2026-06-23）；`DisRouteDispatchService.simulate()` 保留内部 |

### 旧字段审计（正式前端是否消费）

| 字段 / 概念 | 正式前端 | 结论 |
|-------------|----------|------|
| `executionDriverRoutes[]` / `executionSummary` / `deliveryWorkbench` | delivery.js → 仅 `pageViewModel` | 已从 `GET /delivery/today` 删除；全量仍在 `GET /sandbox/today` |
| `loadingDriverRoutes[]` / `plan` / `loadingWorkbench` | loading.js → 仅 `pageViewModel` | 已从 `GET /loading/today` 删除 |
| `simulateAction`（Map） | 无 | 随 `SandboxManualDispatchDriverPanoramaDriverDto` 删除；`DispatchDriverCardDto.primaryAction` 为正式字段 |
| `eligible-drivers` 独立 API | 无 | 不存在；仅 compute 内部 `sandboxIneligibleDrivers` |
| `drivers/available` 内部 `drivers[]` | Java 构建用；HTTP 不序列化 | — |
| `GET /sandbox/today` | jcJieDan today.js 仍调（只读 pageViewModel）；**debug-only** 全量 | today.js 迁 `getDispatchSandboxToday` 后可删路径 |
| `RouteDispatchReadModelAssembler` 全量 Map | `GET /sandbox/today` debug | 同上 |
| loading stops 上 `purchaseStatus` | 装车 pageViewModel 内 orders | 采购/派单解耦后删 |
| `baselineRoute` / `insertPositions[]` on edit-page | simulate payload | confirm 开放 + 前端不读后收缩 |
| `DisRouteDispatchService.simulate()` | 无 HTTP 入口；潜在内部/运维 | 确认零内部调用后可删 Service 方法 |

---

## 8. 正式合同字段（前端不得兜底）

| 页面 | 必须字段 |
|------|----------|
| 今日派单 | `pageViewModel.sections[]`、`DRIVER_ROUTE.timeline[]`、`UNASSIGNED_CUSTOMER.primaryAction` |
| 装车 | `pageViewModel.sections[]`、`LOADING_DRIVER_ROUTES` |
| 配送 | `pageViewModel.sections[]`、`EXECUTION_DRIVER_ROUTES`、`DRIVER_ROUTE.driverName`、`driverCard` |
| 司机可派 | `driverCards[]`、`summary.summaryLine`、`primaryAction.label`、`canToggleDuty` |
| 人工调度 | `storeCard`、`drivers[]`、`summary.summaryLine`、`confirmModeLabel`、`primaryAction` |
| 人工编辑（已选位） | `routeTimeline[]`、`hasSelectedPosition`、`selectedPositionHint`、`simulationSummary`、`drawerSubtitle` |
| 人工编辑（未选位） | `routeTimeline[]`（含 insert 节点）、`insertPositions[]`、`bottomHint`、`drawerSubtitle`（无 simulationSummary） |

**禁止前端兜底：** `driverName = "司机 " + id`、`buildExecutionPageView`、从 executionDriverRoutes 组装配送页。

---

## 9. manualRouteEdit 一条路线刷新合同

请求带 `manualStopSeq` 时，edit-page 内部单次 compute + simulate，返回：

```json
{
  "pageViewModel": {
    "customer": {},
    "incomingManualTimeConstraint": { "summaryLabel": "..." },
    "driver": { "confirmModeLabel": "..." },
    "hasSelectedPosition": true,
    "selectedPositionHint": "已选：插到二号店前面",
    "routeTimeline": [
      { "type": "start", "name": "市场", "timeRight": "现在可送" },
      { "type": "leg", "legText": "1.2 公里 · 5 分钟" },
      { "type": "stop", "seq": 1, "customerName": "...", "insertedStop": false },
      { "type": "end", "name": "返回市场", "timeRight": "预计返回" }
    ],
    "insertPositions": [{ "manualStopSeq": 3, "simulatedRoute": { "stops": [] } }],
    "selectedManualStopSeq": 3,
    "simulationSummary": {
      "routeSummary": "模拟后：共 N 站 · ...",
      "deltaLine": "较原路线 +X 公里 · +Y 分钟",
      "riskHints": []
    },
    "drawerSubtitle": "为「XX店」编辑送达时间约束",
    "bottomHint": "已选插入位置，可重新模拟或调整约束",
    "actions": { "confirmEnabled": false }
  }
}
```

未选位时 `routeTimeline[]` 在相邻 stop 之间含 `type: "insert"` 节点（`manualStopSeq` + `buttonLabel`），无 `simulationSummary`。

每次切换插入位置应重新 POST edit-page（带新 manualStopSeq）。

---

## 10. 后续迁移顺序建议

1. 司机端 loading/delivery → pageViewModel（最高风险，独立迭代）
2. 司机站点详情 → `stopDetail` pageViewModel 专用接口
3. 今日派单/装车 section 卡 → 内嵌 `DispatchDriverCardDto`（可选，非必须）
4. ~~删除 `executionDriverRoutes` 兼容字段（前端全部迁完后）~~ **正式 GET /delivery/today 已删（2026-06-22）**；`GET /sandbox/today` 仍保留
5. ~~删除 `SandboxManualDispatchDriverPanoramaDriverDto`~~ **已完成（2026-06-22）**
6. 独立 SIGN 状态与旧 purchase_status 解耦

---

## 11. 相关文档

| 文档 | 用途 |
|------|------|
| [`Route-Dispatch-Future-Plugin-Boundary-20260622.md`](./Route-Dispatch-Future-Plugin-Boundary-20260622.md) | **Dispatch Core / Adapter 边界、命名审计、旧概念搜索、正式最小合同** |
| `docs/nxPlatform/Route-Dispatch-Manual-Dispatch-Phase2B-Contract.md` | 人工调度补充合同 |
| `docs/nxPlatform/Route-Dispatch-Sandbox-Design.md` | 沙盘设计（主链） |
| `docs/route-dispatch/archive/old-contracts/` | 已废弃旧合同归档 |

---

## 12. Dispatch Core / Adapter 边界（摘要）

详见插件边界文档 §1–§4。核心结论：

- **Core**：Timeline、Store/Driver 卡、Manual Insert 模拟、Duty/confirmMode、时间窗/ETA 格式化、Feasibility/Schedule。
- **Adapter**：`NxDepartmentOrder` 准入、`ShipmentTask` 落库、装车 gate、`purchase_status`、bill 打单、`disId` 租户。
- **命名**：`Nx*`/`Department*`/`Bill*`/`Purchase*` 不应进入未来 Core；`pageViewModel`/`storeCard`/`driverCard`/`primaryAction` 应保留。

---

## 13. 正式接口最小合同（2026-06-22）

| 接口 | 数据根 | 旧字段 |
|------|--------|--------|
| `GET /dispatch/sandbox/today` | `pageViewModel` | ✅ 已清 |
| `GET /dispatch/loading/today` | `pageViewModel` | ✅ 已清 |
| `GET /loading/today` | `pageViewModel` + 元数据 | ✅ 已清 |
| `GET /delivery/today` | `pageViewModel` + 元数据 | ✅ 已清 |
| `GET /drivers/available` | `driverCards[]` + `summary` | ✅ `drivers[]` 已从 HTTP 响应移除 |
| `POST .../driver-panorama` | `storeCard` + `drivers[]` + `summary` | ✅ 已清 |
| `POST .../edit-page` | `pageViewModel.routeTimeline[]` 等 | ⚠️ `baselineRoute`/`insertPositions` 非展示主链 |

---

## 14. 司机端旧链 Known Gap

**旧 HTTP 已删除（2026-06-23）**：`NxDisRouteDispatchController` **不再注册**以下路径；`DriverRouteTasksResponse` 与 `getDriverLoadingToday()` / `getDriverDeliveryToday()` 已移除：

- `GET /driver/loading/today`
- `GET /driver/delivery/today`
- `GET /driver/route/today`

jcJieDan `apiRouteDispatch.js` 中 `getDriverLoadingToday` / `getDriverDeliveryToday` 导出为**死代码**（指向已删 HTTP），本轮不改前台。

**当前 gap**：司机页实际调用 `GET /loading|delivery/today?driverUserId=`，期望司机扁平原子 pageViewModel，后端仍返老板 `sections[]` → 合同报错。未来须**新建**司机专用 pageViewModel 读接口，**不得**恢复 `driver/*/today` 实体直出。

## 15. 不确定项归零（2026-06-23）

| 项 | 结论 |
|----|------|
| `POST /simulate` | **已删 HTTP**；Service 层 `simulate()` 暂留 |
| `GET /sandbox/today` | **保留 debug-only**；响应含 `deprecated`/`debugOnly`/`_debugOnly` 标记 |
| `drivers[]` on `/drivers/available` | **已从 HTTP 移除**（内部字段保留） |
| `GET /driver/loading/today`、`/driver/delivery/today`、`/driver/route/today` | **已删 HTTP**（见 §14） |

## 16. 低 token 小字段清单（本轮不补）

`summaryLine`、`cardKey`、`deltaLine`、`toneClass`、`toggleButtonDisabled`、`navPendingLabel` 等 — 见插件边界文档 §9。

---

*最后更新：2026-06-23，不确定项归零 + 旧 HTTP 删除。*
