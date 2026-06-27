# 今日派单沙盘：司机可派状态统一口径

主判断类：`com.nongxinle.route.DisRouteSandboxDriverDispatchStateHelper`

分派中页面主权链：`SandboxProposalPlan → VisibleDriverRouteSnapshot → pageViewModel`（不读 mergedPlan / partition）。

## 当前运行时阶段（仅此四种）

| 阶段 | 含义 | 是否可派 |
|------|------|----------|
| **OFF_DUTY** | 未上岗 / 已关闭可派 | 否 |
| **IDLE** | ON_DUTY，且无 LOADING / IN_DELIVERY active route | 是 |
| **LOADING** | 已进入装车，仍有未完成 active task | 否 |
| **IN_DELIVERY**（route phase: `ACTIVE_EXECUTION`） | 仍有 IN_DELIVERY / EXCEPTION 等待送 task | 否 |

**司机送完上一趟后直接回到 IDLE**，不再挂 `COMPLETED` / `COMPLETED_REASSIGNABLE` 作为当前状态。

`DELIVERED` / `COMPLETED` / `nx_ddr_route_status` 仅表示 **DB 历史或执行记录**，不参与今日分派当前运行时判断。

## 1. 正在配送 / 装车中，不可再派

| 信号 | 阶段 |
|------|------|
| route 下仍有 `IN_DELIVERY` / 未关闭 `EXCEPTION` task | IN_DELIVERY |
| route 已进入装车且仍有待送 active task | LOADING |
| `ASSIGNED` / `READY_TO_GO` 等未送达 task 占 active dispatch | LOADING / CONFIRMED（legacy plan 切片） |

此时：

- 不得进入 `dispatchEligibleDrivers`
- 历史偏好视为 `PREFERRED_DRIVER_NOT_ELIGIBLE`
- 不得出现在分派中 `availableDrivers` / suggested 路线卡

## 2. 空闲可派（IDLE）

**ON_DUTY 且没有 LOADING / IN_DELIVERY active route** 即为 IDLE / AVAILABLE：

- 进入 `dispatchEligibleDrivers`
- `batchEligible=true`（duty overlay）
- 可接受 history bind 与 optimizer suggested 站
- 上一趟 route task 全 `DELIVERED` 时 **仍视为 IDLE**，不用 completed 头状态

判定以 **active task 列表** 为准；route 头 `IN_DELIVERY` / `DELIVERED`  alone 不决定当前可派性。

legacy merge 如需判断「仅 terminal task」使用 `hasOnlyTerminalActiveTasks(route, …)`，**不作为司机/route 当前 phase 暴露**。

## 3. 已有 suggested 路线 ≠ 不可派

司机已有本次沙盘 suggested stops 时：

- **不一定**出现在 `availableDrivers`（已被建议占用）
- **必须**出现在 `pageViewModel.sections` 与 `mapOverview`（经 ProposalPlan → Snapshot）

## 4. availableDrivers 含义

**当前可人工选择、且未被本次 suggested/confirmed/loading 路线占用的 ON_DUTY 司机**。

不是「参与派车的全部司机列表」；已有路线卡的司机在 `sections` 展示。

## 5. duty card / listDrivers 输出

`listDriversForBatch` 的 duty card `dispatchStage` 只输出：

- `IDLE`（含上一轮已送完、本轮有 sandbox 建议的司机，如程军）
- `LOADING`
- `EXECUTION`（配送中）

不再输出 `COMPLETED` / `SANDBOX` / `CONFIRMED` 作为当前运行时阶段。

## 消费方

| 场景 | 方法 |
|------|------|
| compute 不可派集合 | `blocksSandboxComputeDispatch(route, …)` |
| legacy merge ephemeral 追加 | `acceptsSandboxEphemeralStops(route, …)` |
| legacy merge terminal 保护 | `hasOnlyTerminalActiveTasks(route, …)` |
| 司机列表 batchEligible | `applyDriverListOverlay(dto, route, …)` |
| 分派中路线卡 | `shouldRenderSuggestedDriverRouteCard(…)` + ProposalPlan |
| mapOverview | `shouldRenderDriverOnSandboxMap(…)` |
| availableDrivers | `blocksAvailableIdleSlot(route/stop, …)` + routed 集合 |
| duty card 阶段归一 | `ManualDispatchDispatchStage.toTodayDutyCardStage(…)` |
