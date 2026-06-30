# Phase 2 派单策略分层设计

> **状态**：PR-2a 框架已落地；`OWNER_FIXED_ROUTE` 默认委托旧 optimizer，**行为零变化**。  
> **范围**：沙盘 `compute()` 建议链路；不碰 confirm / loading / depart / complete / 前台。  
> **关联**：[Route-Dispatch-Future-Plugin-Boundary-20260622.md](./Route-Dispatch-Future-Plugin-Boundary-20260622.md)、[Route-Dispatch-Session-Notes-History-P1-20260622.md](./Route-Dispatch-Session-Notes-History-P1-20260622.md)

---

## 1. 背景

当前私人配送商老板自用派单（非平台撮合）。客户—司机关系通常固定，历史司机、历史顺序、送达时间应在 **OWNER** 模式下优先于纯距离；未来平台/商城派单司机与客户不固定，不能套用强历史绑定。

**Phase 1** 已完成：今日临时送达时间保存与展示。  
**Phase 2** 目标：策略分层 + `OWNER_FIXED_ROUTE` 下时窗与历史优先（分 PR 落地）。  
**Phase 2a（本阶段）**：只搭框架，**不改变派单结果**。

---

## 2. 策略分层总架构

```text
DisRouteSandboxComputeServiceImpl.compute()
  buildVirtualTasks + deliveryHistoryPreferences (P1)
  DispatchStrategyOrchestrator                    ← NEW L1
    resolveStrategyMode(disId) → OWNER_FIXED_ROUTE（默认）
    strategy.plan(context) → DispatchStrategyOutcome
  if outcome.delegateLegacyOptimizer:
    runOptimization + applyOptimizationInMemory     ← 旧路径（PR-2a）
  else:
    applyAssignmentPlanInMemory                     ← PR-2b/2c 起
  legMetrics + schedulePreview + pageViewModel      ← 不变
```

| 层 | 职责 |
|---|---|
| **L0 冻结** | confirmed / manualLocked / 执行中 |
| **L1 策略** | 选模式、分司机、定站序、预警（PR-2b 起生效） |
| **L2 几何** | 矩阵、leg、可选 2-opt；**不在 optimizer 内写业务权重** |

---

## 3. 策略模式

| 模式 | 适用 | Phase 2 |
|---|---|---|
| `OWNER_FIXED_ROUTE` | 私人老板 disId（**默认**） | PR-2a 委托旧 optimizer；2b/2c 实现逻辑 |
| `PLATFORM_DYNAMIC` | 未来平台派单 | **stub**，不进入老板正式链路 |
| `MALL_DYNAMIC` | 未来商城派单 | **stub**，不进入老板正式链路 |

模式解析（当前）：

```properties
dis.route.dispatch.strategy-mode=OWNER_FIXED_ROUTE
```

预留：`nx_distributer.nx_dis_dispatch_strategy_mode`（未来按配送商；老板系统默认永不为 PLATFORM/MALL）。

---

## 4. 核心产物：`DispatchAssignmentPlan`

```text
DispatchAssignmentPlan
├── strategyMode
├── planningPhase          // LEGACY_OPTIMIZER_DELEGATION | STRATEGY_PLANNED
├── warnings[]
└── driverRoutes[]
      ├── driverUserId
      └── stops[] (StopAssignment)
            ├── depFatherId, stopSeq, stopClass, feasibility
            ├── timeWindow, historySeed, planningReason
```

debug `GET /sandbox/today` 可挂载 `dispatchAssignmentPlan`（与 `deliveryHistoryPreferences` 同级）；正式 `pageViewModel` 结构不变。

---

## 5. `OWNER_FIXED_ROUTE` 算法（PR-2b/2c 实现，本文档为设计目标）

### 5.1 决策优先级

```text
P0  人工锁定 / confirmed / 执行中
P1  送达时间（可行窗口内）
P2  历史司机（eligible 内）
P3  历史顺序 avgStopSeq
P4  距离 / 顺路（无窗无历史新客户）
P5  已过时窗 / 不可达 → 预警，不抢 P1
```

### 5.2 历史司机 HARD 绑定 — 非无条件死绑

配置项 `history-driver-bind=HARD` 表示 **在可行条件下优先绑定**，须同时满足保护规则：

| 保护 | 行为 |
|---|---|
| **eligible** | 历史司机今日不可派（off duty / sandbox ineligible）→ 不绑定，fallback + warning |
| **人工锁定** | 已 confirm / `manualLocked` 的司机—客户对 → L0 冻结，策略不覆盖 |
| **容量 / 负载** | 单司机已超载（可配置阈值）→ 允许拆给第二司机，warning 非错误 |
| **物理可达** | 绑定后全路线 INFEASIBLE → 降级为 SOFT 或 unassigned，不硬塞 |

HARD ≠ 「无论什么情况都给历史司机」；是 **默认归属**，可被 L0 与可达性否决。

### 5.3 `INFEASIBLE_LATE` — 不抢时窗优先级，也不必永远排最后

- **不抢 P1**：已过窗口的站 **不得** 为「满足 06:00 窗」被插到路线第 1 位。
- **late bucket**：归类为 `INFEASIBLE_LATE` 后进入 **补单/迟到段**，与「正常 timed 段」分开编排。
- **段内排序**：late bucket 内仍按 **顺路程度 → 历史顺序 → 距离** 排序，**不一定永远全局最后**（例如顺路可夹在 two late stops 之间，或 late 段整体置后但内部优化）。

```text
路线结构（示意）：
  [ timed 可行段：按 earliest 排序 ]
  [ late bucket：不抢 earliest，段内置后 + 顺路/历史/距离 ]
  [ distance-only 新客户：必要时插入或 append ]
```

### 5.4 几何抛光（PR-2d 可选）

从 `BalancedInsertion2OptRouteOptimizer` **抽出** `RouteGeometryPolisher`（无业务权重）；仅在 L1 骨架约束内 2-opt。**PR-2a 不改动 optimizer 本体。**

---

## 6. PLATFORM / MALL stub 边界

- 注册于 `DispatchStrategyRegistry`，**当前 `resolveMode(disId)` 永不返回**。
- 若被误调：`UnsupportedOperationException` + 日志。
- 不接入 `GET /dispatch/sandbox/today` 正式链。

---

## 7. 与现有组件边界

| 组件 | Phase 2 关系 |
|---|---|
| `DisRouteSandboxComputeServiceImpl` | 唯一插入 orchestrator |
| `BalancedInsertion2OptRouteOptimizer` | PR-2a **不改**；PR-2d 可选抽 polish |
| `DisRouteDeliveryHistoryPreferenceService` | L1 输入（P1） |
| confirm / loading / depart / complete | **不碰** |
| 前台 | **不碰** |

---

## 8. PR 落地拆分

| PR | 内容 | 派单结果 |
|---|---|---|
| **2a** | 框架 + Orchestrator + Plan 结构；OWNER 委托旧 optimizer | **零变化** |
| **2b** | 历史司机预绑定（含 HARD 保护规则） | 变化 |
| **2c** | 时窗站序 + late bucket + 预警 | 变化 |
| **2d** | 历史顺序种子 + 可选 2-opt 抽出 | 变化 |
| **2e** | 读模型 planningReason（可选） | 展示 |

**PR-2a 验收**：disId=160 建议路线与接入框架前 bitwise 一致（司机、站序、里程）。

---

## 9. 配置（PR-2a 起）

```properties
dis.route.dispatch.strategy-mode=OWNER_FIXED_ROUTE
# 以下 PR-2b 起生效
# dis.route.dispatch.owner.history-driver-bind=HARD
# dis.route.dispatch.owner.timed-stop-order=EARLIEST_FIRST
# dis.route.dispatch.owner.infeasible-placement=LATE_BUCKET
# dis.route.dispatch.owner.enable-2opt-polish=true
# dis.route.dispatch.owner.fallback-to-distance=true
```

---

## 10. 类路径（PR-2a）

```text
com.nongxinle.route.dispatch.strategy
  DispatchStrategyMode
  DispatchPlanningPhase
  DispatchStopClass / DispatchFeasibility / DispatchPlanningReason
  DispatchStrategyContext
  DispatchAssignmentPlan / DriverRoutePlan / StopAssignment
  DispatchStrategyOutcome
  DispatchStrategy
  OwnerFixedRouteDispatchStrategy
  PlatformDynamicDispatchStrategy      (stub)
  MallDynamicDispatchStrategy          (stub)
  DispatchStrategyRegistry
  DispatchStrategyOrchestrator
  DispatchAssignmentPlanReadModelAssembler
```

---

*文档版本：2026-06-27 · PR-2a*
