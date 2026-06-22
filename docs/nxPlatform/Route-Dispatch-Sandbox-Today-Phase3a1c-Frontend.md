# GET /sandbox/today 前端对接说明（Phase 3a.1c 收口）

> ⚠️ **已废弃 — 不可作为当前前端合同（2026-06-22）**  
> 正式老板页请用 `GET /dispatch/sandbox/today` → `data.pageViewModel` only。  
> 归档副本：[`docs/route-dispatch/archive/old-contracts/Route-Dispatch-Sandbox-Today-Phase3a1c-Frontend-ARCHIVED.md`](../route-dispatch/archive/old-contracts/Route-Dispatch-Sandbox-Today-Phase3a1c-Frontend-ARCHIVED.md)

---

> 接口：`GET /api/nxDisRouteDispatch/sandbox/today?disId=&routeDate=&batchCode=MORNING`  
> 本文仅描述 **2026-06 收口** 相对上一版的响应变化；前端按此改 `jcJieDan/subPackage/pages/routeDispatch/` 即可。

---

## 一、工作台动作：未确认阶段不出现「去装车」

### 规则

| 条件 | `dispatchWorkbench.nextActions` |
|------|--------------------------------|
| `confirmedStops.length === 0` 且存在沙盘建议站 | **首项** `CONFIRM_CUSTOMER` / 「确认该店出货完成」`enabled=true`；`GO_LOADING` **disabled** |
| `confirmedStops.length > 0` 且 plan 允许装车 | `GO_LOADING` 才可能 `enabled=true` |

沙盘建议站 **不算** 可装车站点。只有老板在站点卡片点「确认该店出货完成」入库后，`confirmedStops` 才有值。

### 响应示例（未确认）

```json
{
  "confirmedStops": [],
  "sandboxSuggestedStops": [{ "sandboxStopKey": "...", "canConfirmCustomer": true }],
  "plan": {
    "canStartLoading": false,
    "loadingBlockedReason": "请先在站点卡片确认出货完成"
  },
  "dispatchWorkbench": {
    "nextActions": [
      {
        "action": "CONFIRM_CUSTOMER",
        "label": "确认该店出货完成",
        "enabled": true,
        "reason": "请在下方站点卡片逐店确认出货完成"
      },
      { "action": "CHECK_IN_DRIVER", "label": "开启可派", "enabled": false, "reason": "当前已有可派司机" },
      { "action": "UNLOCK_LOCKED_STOP", "label": "处理锁定站点", "enabled": false, "reason": "..." },
      {
        "action": "GO_LOADING",
        "label": "去装车",
        "enabled": false,
        "reason": "请先在站点卡片确认出货完成"
      }
    ]
  }
}
```

### 前端建议

- 新增动作码 `CONFIRM_CUSTOMER`：点击后滚动到司机路线时间轴 / `sandboxSuggestedStops` 区域即可（实际确认仍在站点卡片）。
- **不要** 在 `confirmedStops` 为空时展示可点的「去装车」。

---

## 二、司机站点数口径：建议 vs 已确认

### 新增 / 明确字段（`data.drivers.drivers[]`）

| 字段 | 含义 | 数据来源 |
|------|------|----------|
| `sandboxSuggestedStopCount` | 沙盘建议站点数（未入库） | 后端从 `plan.driverRoutes[].suggestedStopCount`  overlay |
| `confirmedStopCount` | 已确认入库站点数 | DB 已保护 task / `confirmedStops` 按司机聚合 |
| `currentStopCount` | **兼容字段**，与 `confirmedStopCount` 同值 | 勿再当「当前路线总站点」 |

### 对比

```json
{
  "plan": {
    "driverRoutes": [{
      "driverUserId": 101,
      "suggestedStopCount": 1,
      "stops": [{ "sandboxStopKey": "..." }]
    }]
  },
  "drivers": {
    "drivers": [{
      "driverUserId": 101,
      "sandboxSuggestedStopCount": 1,
      "confirmedStopCount": 0,
      "currentStopCount": 0,
      "operationHint": "可参与早班派车，沙盘建议 1 站"
    }]
  }
}
```

### 前端建议

- **司机路线卡片**（有 stops 时间轴）：站点数、时间轴读 **`plan.driverRoutes`**（含沙盘建议 stops）。
- **司机可派列表**若展示站点数：用 `confirmedStopCount`，文案标明 **「已确认站点」**；沙盘建议用 `sandboxSuggestedStopCount`，文案 **「沙盘建议」**。
- **不要** 用 `drivers[].currentStopCount` 驱动路线卡片是否展示 stops。

---

## 三、司机卡片：展示全程里程与时长

### 字段位置：`plan.driverRoutes[]`

| 字段 | 示例 | 说明 |
|------|------|------|
| `nxDdrTotalDistanceM` | `79199` | 全程路线距离（含返程），米 |
| `nxDdrTotalDurationS` | `5250` | 全程预计时长，秒 |
| `routeDistanceType` | `ROUTE_DISTANCE` | `ROUTE_DISTANCE` / `ESTIMATED_STRAIGHT_DISTANCE` |
| `returnLegDistanceM` / `returnLegDurationS` | 返程段 | 时间轴返程 leg 可用 |
| `routeScheduleSummaryLabel` | `现在可送 · 约 42 分钟后到` | 首站到达摘要，**不是**全程里程 |
| `stops[].plannedArrivalLabel` | `今天 08:00` | 预计到达 |
| `stops[].plannedDepartureLabel` | `今天 08:30` | **预计离开**（时间轴必读，禁止从 task 兜底） |
| `stops[].serviceMinutes` / `serviceMinutesSource` / `serviceDurationLabel` | `30` / `DEFAULT` / `服务 30 分钟` | 服务时长及来源 |
| `stops[].customerWindowLabel` | 可选 | 客户时窗（confirmed 有时窗数据时透出） |
| `driverRoutes[].totalDistanceM` / `totalDurationS` | canonical | 司机卡片全程里程/时长（含返程；时长含服务+等待） |
| `driverRoutes[].plannedDepartAt` / `plannedReturnAt` / `plannedFinishAt` | datetime | 市场出发 / 返回市场完成（二者相同） |
| `driverRoutes[].lastStopDepartureAt` | datetime | 最后一店离店，不等于整趟完成 |
| `driverRoutes[].returnLegLabel` | `返回市场 51.4 km · 59 分钟` | 返程段展示 |
| `stops[].plannedArrivalAt` / `plannedDepartureAt` | datetime | 逻辑判断用；label 仅展示 |
| `summary.totalRouteDistanceM` / `summary.totalRouteDurationS` | 汇总 | 顶部 Hero 总公里/总时长 |

### 展示建议（司机卡片顶部两行）

```
全程约 79.2 公里 · 预计约 88 分钟          ← nxDdrTotalDistanceM / nxDdrTotalDurationS
约 42 分钟到一号店                          ← stops[0].plannedArrivalLabel + 客户名
```

格式化参考：

```javascript
// 距离：米 → 「79.2 公里」
Math.round(m / 100) / 10 + ' 公里'

// 时长：秒 → 「88 分钟」
Math.max(1, Math.round(s / 60)) + ' 分钟'
```

**不要** 只展示 `routeScheduleSummaryLabel`（例如「现在可送 · 约 42 分钟后到」），否则老板看不到整趟远近。

`routeDistanceType === 'ESTIMATED_STRAIGHT_DISTANCE'` 时可加后缀「（含直线估算）」。

---

## 四、批次 Label：ADHOC_NOW 统一口径

### 问题（已修复）

同一响应里 `dispatchBatch=MORNING` 但根级 `dispatchBatchLabel=晚班`、drivers 块 `dispatchBatchLabel=早班` —— 临时补单场景不应出现早/晚班混用。

### 规则

当 `sandboxScheduleMode === 'ADHOC_NOW'` 时，以下字段 **统一** 为：

```
dispatchBatchLabel = displayShiftLabel = "临时补单｜现在可送"
```

同时：

- `dispatchWorkbench.dispatchBatchLabel` 同上
- `drivers.dispatchBatchLabel` 同上
- `plan.nxDrpDispatchBatchLabel`（序列化键 `dispatchBatchLabel`）同上
- 详细到达文案仍读 `scheduleBannerLine`（如 `临时补单｜现在可送｜约 42 分钟后到`）

### 非 ADHOC 场景

今日路线日仍按服务器时刻 overlay 早/中/晚班（`displayShiftCode` / `displayShiftLabel`），各块 `dispatchBatchLabel` 保持一致。

### 前端建议

- 页头批次文案：ADHOC 下 **不要** 再读 `DisRouteDispatchLabels` 本地映射的「早班/晚班」。
- 优先读根级 `dispatchBatchLabel` 或 `displayShiftLabel`；ADHOC 时二者相同。

---

| confirm 防重复 | 仅 **ACTIVE** item + protected task 阻断；bill **不**阻断 confirm/return | ✅ |

---

## 七、Phase 3c：返回沙盘（前端对接）

### 接口

```
POST /api/nxdisroutedispatch/sandbox/stops/{deliveryStopId}/return-to-sandbox
Content-Type: application/json

{
  "operatorUserId": 1,
  "reason": "客户临时改单，取消本次分派，返回沙盘",
  "disId": 1,
  "routeDate": "2026-06-21",
  "batchCode": "MORNING"
}
```

响应：`data` = 完整 `GET /sandbox/today` 读模型（与 confirm 一致）。

### confirmedStops 新增字段

| 字段 | 说明 |
|------|------|
| `canReturnToSandbox` | 是否显示按钮 |
| `returnToSandboxActionLabel` | 「返回沙盘」（可显示为「取消确认」） |
| `returnToSandboxBlockedReason` | 不可返回原因 |
| `returnToSandboxWarning` | 已打印配送单弱提醒 |
| `returnToSandboxConfirmMessage` | 弹窗全文 |

### 按钮规则

- **仅** `canReturnToSandbox === true` 时显示
- 文案：**「返回沙盘」** 或 **「取消确认」**
- **禁止**：「删除订单」「撤销订单」「删除配送单」
- 弹窗优先用 `returnToSandboxConfirmMessage`：
  - 基础：「取消后，该店会回到动态沙盘…订单和配送单不会删除。」
  - 已打印追加：「该店配送单已打印，返回沙盘后请注意是否需要补打或重打。」
- 成功后整页用响应刷新；**禁止**再用旧 `deliveryStopId` 操作该店
- 返回后该店主操作 ID 恢复为 `sandboxStopKey`

### 页面状态（与 3a.1c 一致）

| 区块 | 规则 |
|------|------|
| 沙盘建议店 | `sandboxStopKey`；「确认该店出货完成」「改派司机」；无 `deliveryStopId` |
| 已确认店 | `deliveryStopId`；可「返回沙盘」；无「确认该店出货完成」 |
| 空闲司机 | 无「去分派」 |
| GO_LOADING | 仅 `confirmedStops.length > 0` |
| 距离 | `ROUTE_DISTANCE` → 「约 xx 公里」；`ESTIMATED_STRAIGHT_DISTANCE` → 「约 xx 公里（直线估算）」 |

---

## 八、Phase 3D：确认司机出发（前端对接）

### 字段位置：`plan.driverRoutes[]` 顶层

| 字段 | 说明 |
|------|------|
| `driverRouteId` | **确认出发唯一主键**；有 confirmed stop 时必有值 |
| `driverUserId` / `driverName` | 司机身份 |
| `routeStatus` / `routeStatusLabel` | `LOADING`=装车中；全 confirmed 待出发仍为装车中直至 depart；`IDLE`=空闲（无路线无站点） |
| `confirmedStopCount` / `sandboxSuggestedStopCount` / `totalStopCount` | 站点计数 |
| `canDepart` / `departBlockedReason` / `departConfirmMessage` / `departWarning` | 出发按钮与提示 |

### 严格契约：禁止兜底 driverRouteId

确认司机出发 **只读** `plan.driverRoutes[i].driverRouteId`。

**禁止**从以下字段兜底：

- `stops[0].driverRouteId`
- `confirmedStops[].driverRouteId`
- `shipmentTask.nxDstDriverRouteId`
- `nxDdrId`

若 `driverRoutes[].driverRouteId` 缺失，前端应显示开发态错误：

> 接口缺少 driverRoutes[].driverRouteId，无法确认司机出发。

### 接口

```
POST /api/nxdisroutedispatch/driver-routes/{driverRouteId}/depart
```

### 验收示例

| 场景 | 期望 |
|------|------|
| 张司机：1 confirmed + 1 suggested | `driverRouteId=8`，`canDepart=false`，`departBlockedReason=请先确认全部门店出货完成` |
| 二号店 confirm 后 | `confirmedStopCount=2`，`sandboxSuggestedStopCount=0`，`canDepart=true` |
| 小曹：无站点 | `driverRouteId=null`，`routeStatus=IDLE`，`stops=[]`，`departBlockedReason=司机路线不存在` |

---

## 九、验收清单（含 3c / 3D）

1. return 后店不在 `confirmedStops`，重新出现在 `sandboxSuggestedStops`。
2. 订单、配送单未删除；bill 不阻断 return。
3. `IN_DELIVERY` / 整车已出发不可 return。
4. §五 3a.1c 各项仍成立。
5. 3D：`driverRoutes[].driverRouteId` 顶层透出；空闲司机 `routeStatus=IDLE`；出发不从 stop 兜底。

---

## 十、相关后端文件

- `DisRouteSandboxReturnServiceImpl` — return-to-sandbox 写路径
- `DisRouteReturnToSandboxPolicy` — 允许/禁止判定
- `DisRouteSandboxTodayServiceImpl` — 3a.1c 读模型收口
- `DisRouteDispatchWorkbenchServiceImpl` — workbench 动作
