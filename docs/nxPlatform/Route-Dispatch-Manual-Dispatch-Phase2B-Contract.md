# Phase 2B 人工调度主权合同

> **补充合同**。正式页面口径以 [`Route-Dispatch-Backend-Handoff-20260622.md`](../route-dispatch/Route-Dispatch-Backend-Handoff-20260622.md) 为准；插件边界见 [`Route-Dispatch-Future-Plugin-Boundary-20260622.md`](../route-dispatch/Route-Dispatch-Future-Plugin-Boundary-20260622.md)。

> **状态**：Phase 2B 新开发主权文档。前后台均按本合同实现，**不保留旧入口、旧字段、旧语义**。  
> **原则**：自动派单严格；人工调度宽松（ON_DUTY 可见）；人工约束三分法；系统不得覆盖老板约束。

---

## 0. 共享枚举

### 0.1 人工约束三分法（禁止混用 `manualLocked`）

| 字段 | 含义 |
|------|------|
| `manualDriverLocked` | 老板指定司机 |
| `manualSeqLocked` | 老板指定 `manualStopSeq`（第 N 个送） |
| `requiredArrivalLocked` | 老板指定 `requiredLatestArrivalAt` |

组合规则：

- 仅司机 → 系统可推荐多个插入顺序方案
- 司机 + `manualStopSeq` → **单方案**，顺序不可改
- 司机 + `requiredLatestArrivalAt` → 评估可行性，**不自动修改**老板时间约束

### 0.2 `dispatchStage`

| 值 | 含义 |
|----|------|
| `IDLE` | 上岗、今日无待送站 |
| `SANDBOX` | 沙盘中（含建议/未全确认） |
| `CONFIRMED` | 已确认待装车 |
| `LOADING` | 装车中 |
| `EXECUTION` | 已出发/配送中 |
| `COMPLETED` | 今日路线已完成（仍在岗，**可再派**） |

### 0.3 `confirmMode`

| 值 | 含义 | 人工调度 |
|----|------|----------|
| `DIRECT` | 可直接确认 | IDLE / SANDBOX / CONFIRMED |
| `RISK_ACK` | 有风险，老板二次确认后可提交 | LOADING / EXECUTION / COMPLETED 及时间窗违约等 |
| `FORBIDDEN` | 不可模拟也不可确认 | **仅 OFF_DUTY**（不进 `drivers[]`） |

**核心原则**：

```text
人工调度中，只要 ON_DUTY，均可选择、可模拟、可确认。
不同 dispatchStage 只影响风险提示和影响评估，不决定是否禁止分派。
```

`LOADING` / `EXECUTION` / `COMPLETED` 示例：

```text
canSimulate = true
canConfirm = true
confirmMode = RISK_ACK
blockedReason = null
riskHints = [...]
```

### 0.4 `manualStopSeq` 顺序范围

| dispatchStage | `manualStopSeq` 含义 | Phase 2B 实现 |
|---------------|---------------------|---------------|
| `SANDBOX` / `CONFIRMED` | 该司机**待送路线**中的第 N 个送达 | 2B-2 |
| `LOADING` | 装车任务中**未出发路线**的第 N 个送达 | 2B-2 |
| `EXECUTION` | 剩余未完成站中的第 N 个（非历史全线第 N 个） | 2B-2 |

---

## 1. 入口 `START_MANUAL_DISPATCH`

正式 `pageViewModel` UNASSIGNED 卡在 **存在至少 1 名 ON_DUTY 司机** 时 enabled。

**enabled 时 `payload` 必填且完整**（禁止 null 关键字段、禁止空 payload）：

| 字段 | 说明 |
|------|------|
| `disId` | |
| `routeDate` | |
| `batchCode` | |
| `operatorUserId` | |
| `departmentId` | 与 `depFatherId` 同值 |
| `depFatherId` | |
| `sandboxStopKey` | 如 `dep:1516` |
| `liveOrderIds` | 允许 `[]`，字段必须存在 |
| `driverPanoramaPath` | 正式全景接口路径 |
| `simulatePath` | |
| `confirmPath` | |

```json
{
  "actionType": "START_MANUAL_DISPATCH",
  "label": "人工调度",
  "enabled": true,
  "disabledReason": "",
  "payload": {
    "disId": 160,
    "routeDate": "2026-06-22",
    "batchCode": "MORNING",
    "operatorUserId": 1001,
    "departmentId": 1516,
    "depFatherId": 1516,
    "sandboxStopKey": "dep:1516",
    "liveOrderIds": [88001, 88002],
    "driverPanoramaPath": "/api/nxdisroutedispatch/sandbox/manual-dispatch/driver-panorama",
    "simulatePath": "/api/nxdisroutedispatch/sandbox/manual-dispatch/simulate",
    "confirmPath": "/api/nxdisroutedispatch/sandbox/manual-dispatch/confirm"
  }
}
```

无在岗司机时 **不带 payload**：

```json
{
  "actionType": "START_MANUAL_DISPATCH",
  "enabled": false,
  "disabledReason": "当前没有上岗司机"
}
```

---

## 2. Phase 2B-1：`driver-panorama`

### 2.1 正式接口（唯一入口）

```
POST /api/nxdisroutedispatch/sandbox/manual-dispatch/driver-panorama
```

### 2.2 列表原则

```text
drivers[] = 今日全部 ON_DUTY 司机
OFF_DUTY 司机不进入 drivers[]
```

### 2.3 请求

`disId`, `routeDate`, `batchCode`, `operatorUserId`, `departmentId` / `depFatherId`, `sandboxStopKey`, `liveOrderIds`。

### 2.4 统一模板（`driver-panorama`）

响应使用派单系统统一模板，前端只渲染模板字段：

**`storeCard`（`DispatchStoreCardDto`）** — 待分派客户分店卡

| 字段 | 说明 |
|------|------|
| `customerName` / `goodsSummary` | 客户名、商品汇总 |
| `distanceText` / `durationText` | 距离、预计耗时 |
| `plannedArrivalLabel` / `plannedDepartureLabel` | 预计到达 / 离开 |
| `customerWindowLabel` | 送达窗口 |
| `serviceDurationLabel` | 服务时长 |
| `dispatchStatusLabel` | 当前派单状态（未分配客户为「未分配」） |
| `manualTimeConstraint` | 人工时间约束（见 §3.5 命名） |

**`drivers[]`（`DispatchDriverCardDto`）** — 司机卡

| 字段 | 说明 |
|------|------|
| `driverUserId` / `driverName` / `driverAvatarUrl` | 基本信息 |
| `dutyStatus` / `dutyStatusLabel` | 工作状态（列表内恒为 `ON_DUTY`） |
| `dispatchStage` / `dispatchStageLabel` | 派单阶段 §0.2 |
| `plannedDepartAt` / `plannedDepartLabel` | 准备出发时间 |
| `plannedReturnAt` / `plannedReturnLabel` | 预计返回时间 |
| `totalDistanceM` / `totalDurationS` / `totalDistanceText` / `totalDurationText` | 当前路线总距离/耗时 |
| `routeSummary` | 路线摘要（客户数 + 距离 + 耗时） |
| `currentStopCount` / `completedStopCount` / `pendingStopCount` | 客户数 / 已完成 / 待送 |
| `canSimulate` / `canConfirm` / `confirmMode` | 能力 |
| `blockedReason` / `riskHints` / `operationHint` | 阻断原因 / 风险提示 / 操作提示 |
| `primaryAction` | 主操作按钮（`SIMULATE_MANUAL_DISPATCH`） |

### 2.5 响应示例

```json
{
  "disId": 160,
  "routeDate": "2026-06-22",
  "batchCode": "MORNING",
  "storeCard": {
    "departmentId": 1516,
    "sandboxStopKey": "dep:1516",
    "customerName": "四点",
    "goodsSummary": "油菜 66斤 等3品",
    "distanceText": "12.3 公里",
    "durationText": "28 分钟",
    "plannedArrivalLabel": "约 07:50 到",
    "customerWindowLabel": "常规窗口 07:20–08:20",
    "dispatchStatusLabel": "未分配",
    "manualTimeConstraint": {
      "manualArrivalSpecified": false
    }
  },
  "drivers": [
    {
      "driverUserId": 301,
      "driverName": "张司机",
      "dutyStatus": "ON_DUTY",
      "dutyStatusLabel": "可派",
      "dispatchStage": "CONFIRMED",
      "dispatchStageLabel": "已确认待装车",
      "plannedDepartLabel": "07:10 出发",
      "plannedReturnLabel": "09:30 返回",
      "totalDistanceText": "28 公里",
      "totalDurationText": "1 小时",
      "canSimulate": true,
      "canConfirm": true,
      "confirmMode": "DIRECT",
      "riskHints": [],
      "routeSummary": "3 个客户 · 28 公里 · 约 1 小时",
      "currentStopCount": 3,
      "completedStopCount": 0,
      "pendingStopCount": 3,
      "operationHint": "可插入已确认路线，需评估对后续客户影响",
      "primaryAction": {
        "actionType": "SIMULATE_MANUAL_DISPATCH",
        "label": "模拟加入该司机路线",
        "enabled": true,
        "payload": {
          "disId": 160,
          "routeDate": "2026-06-22",
          "batchCode": "MORNING",
          "operatorUserId": 1001,
          "departmentId": 1516,
          "driverUserId": 301
        }
      }
    },
    {
      "driverUserId": 302,
      "driverName": "小曹",
      "dispatchStage": "LOADING",
      "dispatchStageLabel": "装车中",
      "dutyStatus": "ON_DUTY",
      "canSimulate": true,
      "canConfirm": true,
      "confirmMode": "RISK_ACK",
      "blockedReason": null,
      "riskHints": [
        "司机正在装车，追加可能影响装车顺序"
      ],
      "routeSummary": "2 个客户 · 装车中",
      "currentStopCount": 2,
      "completedStopCount": 0,
      "pendingStopCount": 2,
      "operationHint": "可插入装车路线，请关注装车顺序影响",
      "primaryAction": {
        "actionType": "SIMULATE_MANUAL_DISPATCH",
        "label": "模拟加入该司机路线",
        "enabled": true,
        "payload": { "driverUserId": 302 }
      }
    }
  ],
  "summary": {
    "onDutyDriverCount": 4,
    "simulatableDriverCount": 4,
    "directConfirmDriverCount": 2,
    "riskAckDriverCount": 2,
    "forbiddenDriverCount": 0,
    "summaryLine": "上岗司机 4 人，4 人可试算加入路线"
  }
}
```

### 2.6 `confirmMode` 缺省

| dispatchStage | canSimulate | canConfirm | confirmMode | dispatchStageLabel |
|---------------|-------------|------------|-------------|-------------------|
| IDLE | true | true | DIRECT | 空闲 |
| SANDBOX | true | true | DIRECT | 沙盘中 |
| CONFIRMED | true | true | DIRECT | 已确认待装车 |
| LOADING | true | true | RISK_ACK | 装车中 |
| EXECUTION | true | true | RISK_ACK | 配送中 |
| COMPLETED | true | true | RISK_ACK | 已完成 / 可再派 |

**唯一禁止**：`OFF_DUTY` 不进入 `drivers[]`。

---

## 3. Phase 2B-2：`simulate`

### 3.1 接口

```
POST /api/nxdisroutedispatch/sandbox/manual-dispatch/simulate
```

### 3.2 请求

`disId`, `routeDate`, `batchCode`, `departmentId`, `sandboxStopKey`, `liveOrderIds`, `driverUserId`, 可选 `manualStopSeq`, 可选 `requiredLatestArrivalAt`。

### 3.3 规则

1. 仅司机 → `simulationMode = MULTI_INSERT_OPTIONS`
2. 有 `manualStopSeq` → `SINGLE_LOCKED_SEQ`，`insertOptions.length === 1`
3. 有 `requiredLatestArrivalAt` → `requiredArrival.feasible` / `slackMinutes` / `violationMinutes`
4. **所有 ON_DUTY 司机**均可 simulate；`dispatchStage` 只影响 `riskHints` / `confirmMode`，不拒绝请求
5. simulate **不写 DB**

### 3.4 响应字段

| 字段 | 说明 |
|------|------|
| `simulationId` | 本次模拟 ID（供后续 confirm 引用） |
| `simulationMode` | `MULTI_INSERT_OPTIONS` / `SINGLE_LOCKED_SEQ` |
| `manualConstraints` | 三锁语义 |
| `customer` / `driver` | 客户与司机摘要（含 `dispatchStage`） |
| `baseline` | 原路线里程/耗时/站点数 |
| `insertOptions[]` | 插入方案（含 `routeImpact`、`incomingStop`、`stopImpacts`、`requiredArrival`、`confirmMode`、`riskHints`） |
| `insertOptions[].incomingStop.seq` | 插入后送序（1-based），锁定顺序时用于验收 |
| `insertOptions[].stopImpacts[].seq` / `insertedStop` | 每站送序；插入客户 `insertedStop=true` |
| `insertOptions[].requiredArrival` | 有 `requiredLatestArrivalAt` 时必填：`deadlineLabel`、`plannedArrivalLabel`、`feasible`、`slackMinutes` 或 `violationMinutes` |
| `requiredArrival` | 推荐/锁定方案上的必须送达评估（与对应 `insertOptions[]` 项一致） |
| `recommendedOptionKey` | 系统推荐方案 key |
| `confirmMode` / `riskHints` | 默认取推荐/锁定方案的风险级别 |

---

## 3.5 Phase 2B：`edit-page` 人工路线编辑页 ViewModel

### 3.5.1 接口

```
POST /api/nxdisroutedispatch/sandbox/manual-dispatch/edit-page
```

本阶段 **仅返回页面 ViewModel**，不做 confirm 落库。内部复用 simulate 计算（**单次** `compute` + **单次** simulate），不写 DB；**禁止**重复 `compute` 或额外腾讯矩阵。

### 3.5.2 请求

与 `simulate` 相同：`disId`, `routeDate`, `batchCode`, `departmentId`, `sandboxStopKey`, `liveOrderIds`, `driverUserId`；可选 `manualStopSeq`, `requiredLatestArrivalAt`。

### 3.5.3 响应

```json
{
  "pageViewModel": { ... }
}
```

| 字段 | 说明 |
|------|------|
| `customer` | 待插入客户摘要 |
| `incomingManualTimeConstraint` | 待插入客户人工时间约束模板 |
| `driver` | 所选司机（含 `dispatchStage`、`canSimulate`、`canConfirm`、`confirmMode`、`riskHints`） |
| `baselineRoute` | 司机当前路线；`stops[]` 含 `customerTimeWindow`、`systemEta`、`manualTimeConstraint` |
| `insertPositions[]` | 可插入位置：`label`（如「插到第 1 个送」）、`anchorLabel`（如「插到二号店前面」）、`manualStopSeq` |
| `insertPositions[].simulatedRoute` | 该位置插入后的模拟路线 + 每店 `impact` |
| `insertPositions[].requiredArrival` | 必须送达评估 |
| `insertPositions[].confirmMode` / `riskHints` | 该方案风险 |
| `actions` | `editPagePath` / `simulatePath` / `confirmPath`；`confirmEnabled=false` |
| `simulationSummary` | **manualStopSeq 已选时必填**：`routeSummary` 或 `totalDistanceText`+`totalDurationText`；可选 `deltaLine`、`riskHints[]` |

### 3.5.5 `simulationSummary`（选中插入位置后）

| 字段 | 必填 | 说明 |
|------|------|------|
| `routeSummary` | 二选一 | 整行摘要，如 `模拟后：共 4 站 · 18.6 公里 · 52 分钟` |
| `totalDistanceText` | 二选一 | 无 `routeSummary` 时与 `totalDurationText` 拼成「模拟后：xx · xx」 |
| `totalDurationText` | 二选一 | 同上 |
| `deltaLine` | 否 | 相对基线，如 `较原路线 +1.2 公里 · +6 分钟` |
| `riskHints` | 否 | 风险提示字符串数组 |

未传 `manualStopSeq`（首次进页）时不返回 `simulationSummary`。

### 3.5.6 `manualTimeConstraint`（每店卡预留）

| 字段 | 说明 |
|------|------|
| `manualArrivalSpecified` | 是否人工指定送达时间 |
| `requiredLatestArrivalAt` / `requiredLatestArrivalLabel` | 必须几点前到 |
| `preferredArrivalAt` / `preferredArrivalLabel` | 最好几点到 |
| `allowLate` | 是否允许晚 |
| `remarkReason` | 备注原因 |

区分三层：`customerTimeWindow`（原时间窗） / `manualTimeConstraint`（老板约束） / `systemEta`（系统模拟 ETA）。

`systemEta.plannedArrivalAt` 与 `plannedArrivalLabel` 必须同源（同一模拟时刻）；模拟路线店卡统一从 `stop.systemEta` 读取。

`baselineRoute` 总里程/耗时取自 merged plan 司机路线；`simulatedRoute` 的 `totalDistanceDeltaM` / `totalDurationDeltaS` 由 edit-page 按同一口径重算。

无客户时间窗时，`impact.timeWindowImpactLabel` 为 `未设置时间窗`，不返回 `符合时间窗`。

---

## 4. `confirm`：必须引用模拟结果

```
POST /api/nxdisroutedispatch/sandbox/manual-dispatch/confirm
```

确认 **不得** 仅信任前端拼装的字段。必须引用一次有效 simulate：

| 字段 | 说明 |
|------|------|
| `simulationId` | simulate 返回的唯一 ID |
| `selectedOptionKey` | 多方案时选中的 `insertPositionKey` |

### 4.1 确认快照双轨

```json
{
  "systemRecommendation": {
    "suggestedDriverUserId": 302,
    "suggestedStopSeq": 3
  },
  "manualDecision": {
    "manualDriverLocked": true,
    "assignedDriverUserId": 301,
    "manualSeqLocked": true,
    "manualStopSeq": 1,
    "requiredArrivalLocked": false,
    "confirmMode": "DIRECT",
    "dispatchStageAtConfirm": "CONFIRMED"
  },
  "simulationRef": {
    "simulationId": "md-sim-20260622-abc123",
    "selectedOptionKey": "LOCKED"
  }
}
```

---

## 5. 自动派单 vs 人工调度

| 维度 | 自动派单 | 人工调度 |
|------|----------|----------|
| 司机范围 | `batchEligible` + 排除装车中/配送中/已完成等 | **全部 ON_DUTY**（仅排除 OFF_DUTY） |
| 顺序 | 优化器 | 老板可锁 `manualStopSeq` |
| 必须送达 | 客户时间窗 | 老板 `requiredLatestArrivalAt` |
| 阶段限制 | 系统替老板做主 | **不禁止**；仅 `riskHints` + `confirmMode` |
| 覆盖人工 | N/A | **禁止** |

---

## 6. 实施分期

| 阶段 | 内容 |
|------|------|
| **2B-1** ✅ | `driver-panorama` ON_DUTY 全景 |
| **2B-2** | simulate + edit-page ViewModel | ✅ 本文 + 实现 |
| **2B-3** | confirm + `simulationId` 校验 |
