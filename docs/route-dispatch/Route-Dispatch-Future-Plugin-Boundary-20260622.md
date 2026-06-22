# 派单插件化边界审计（2026-06-22）

> **范围**：只读审计 + 文档沉淀。不新建 Maven module，不大规模改包名。  
> **原则**：宁可少留，不要多留。通用 Dispatch Core 与生鲜平台 Adapter 分层清晰后，再考虑物理抽离。

**关联文档**：[Route-Dispatch-Backend-Handoff-20260622.md](./Route-Dispatch-Backend-Handoff-20260622.md)

---

## 1. 两层概念

```text
┌─────────────────────────────────────────────────────────────┐
│  Dispatch Core（未来可复用）                                  │
│  Driver / Stop / Route / Plan / Simulation / Timeline / ETA   │
│  TimeWindow / ManualConstraint / RiskHint / Duty / Cards    │
└───────────────────────────┬─────────────────────────────────┘
                            │ 端口 + 读模型组装
┌───────────────────────────▼─────────────────────────────────┐
│  Fresh-Market Adapter（当前 nongxinle 生鲜/京采）              │
│  NxDepartment / NxDepartmentOrder / Bill / disId / purchase   │
│  装车 gate / 市场打单 / 分拣称重 / ShipmentTask 落库           │
└─────────────────────────────────────────────────────────────┘
```

**目标项目映射（未来，非本次实现）**

| 项目 | Adapter 差异 |
|------|-------------|
| 京采生鲜 `NxDepartment` + `NxDepartmentOrder` + `Bill` | **当前实现** |
| 平台饭馆 `GbDepartment` | 客户/订单实体不同，Core 接口相同 |
| 居民小程序 `NxCommunity` | 门店/订单模型不同，无 bill/装车链 |

---

## 2. Dispatch Core / Adapter 边界表

| 当前类/方法 | 当前职责 | Core / Adapter | 原因 | 未来可抽离 | 抽离风险 |
|-------------|----------|----------------|------|------------|----------|
| `DisRouteSandboxTodayTimelineBuilder` | timeline[] 节点（start/leg/stop/end） | **Core** | 纯 UI 节点，无订单实体 | ✅ 高 | 低 |
| `DisRouteManualDispatchEditPageTimelineBuilder` | 人工编辑 routeTimeline[] + insert 槽 | **Core** | 通用「插入模拟」展示 | ✅ 高 | 低 |
| `DisRouteSandboxManualDispatchSimulator` | 固定顺序插入、leg 重算、时间窗影响 | **Core** | 算法与领域无关 | ✅ 高 | 中（依赖 Stop 快照结构） |
| `DisRouteSandboxManualDispatchEditPageBuilder` | edit-page pageViewModel 组装 | **Core**（展示）+ **Adapter**（数据来源） | 组装逻辑通用；stop 来自 Nx 实体 | ⚠️ 部分 | 中 |
| `DisRouteDispatchCardTemplateBuilder` | `DispatchStoreCardDto` / `DispatchDriverCardDto` | **Core** | 统一卡片模板 | ✅ 高 | 低 |
| `DispatchDriverCardDto` / `DispatchStoreCardDto` | 司机卡/分店卡契约 | **Core** | 跨项目 UI 合同 | ✅ 高 | 低 |
| `ManualDispatchConfirmMode` | DIRECT / RISK_ACK / FORBIDDEN 等 | **Core** | 通用确认策略枚举 | ✅ 高 | 低 |
| `DisRouteManualTimeConstraintHelper` | 人工时间约束 summaryLabel | **Core** | 与订单来源无关 | ✅ 高 | 低 |
| `DisRouteTemporalHelper` / `DisRouteSandboxDisplayFormatHelper` | 时间/距离/时长格式化 | **Core** | 纯工具 | ✅ 高 | 低 |
| `DisRouteSandboxTodayPageViewModelBuilder` | pageViewModel sections 组装 | **Core**（结构） | section/timeline 模式通用 | ⚠️ 部分 | 中 |
| `DisRouteSandboxTodayViewModelMaps` | ViewModel → 显式 Map 契约 | **Core** | 序列化边界 | ✅ 高 | 低 |
| `DisRouteDriverDutyToggleHelper` | 可派开关业务规则 | **Core** | 司机 duty 通用 | ✅ 高 | 低 |
| `DisRouteDispatchDriverNameHelper` | 司机名解析兜底 | **Core** | 通用「资料缺失」策略 | ✅ 高 | 低 |
| `DisRouteFeasibilityService` | 路线可执行性评估 | **Core** | 时间窗/顺序可行性 | ✅ 中 | 中 |
| `DisRouteScheduleService` | 固定顺序排程 | **Core** | 通用 VRP 后处理 | ✅ 中 | 中 |
| `RouteCostProvider` / `RouteOptimizer` | 路径优化插件 | **Core 插件口** | 已抽象 | ✅ 已有 | 低 |
| `DisRouteSandboxComputeServiceImpl` | 沙盘合并、分区、eligible 过滤 | **Adapter** | 读 `NxDepartmentOrder`、disId | ❌ 暂留 Adapter | 高 |
| `DisRouteSandboxTodayServiceImpl` | today 读模型 | **Adapter** | 绑定 Nx plan/task/stop | ❌ 暂留 Adapter | 高 |
| `DisRouteSandboxConfirmServiceImpl` | 沙盘站点确认落库 | **Adapter** | 写 ShipmentTask + RouteStop | ❌ 暂留 Adapter | 高 |
| `DisRouteSandboxConfirmLoadingServiceImpl` | 装车确认 | **Adapter** | purchase_status 同步 | ❌ 暂留 Adapter | 高 |
| `DisRouteSandboxRouteLoadingGateServiceImpl` | 进入装车/退回派单 | **Adapter** | 京采装车流程 | ❌ 暂留 Adapter | 高 |
| `DisRouteSandboxDeliveryExecutionServiceImpl` | 送达/异常 | **Adapter** | 绑定 nx_dst_status | ❌ 暂留 Adapter | 中 |
| `DisRouteEligibleOrderPolicy` | 哪些 live order 进沙盘 | **Adapter** | `do_status` 规则 | ❌ 暂留 Adapter | 高 |
| `DisRouteBillPrintStatusHelper` | 打单状态 | **Adapter** | bill 业务 | ❌ 不抽 | — |
| `DisRoutePurchaseStatusLabels` | 采购/装车状态文案 | **Adapter** | purchase_status | ❌ 不抽 | — |
| `DisRouteSandboxUnassignedStopHelper` | sandboxStopKey / departmentId | **Adapter** | Nx 部门 ID 语义 | ❌ 暂留 Adapter | 中 |
| `DisRouteShipmentTaskItemOrderResolver` | task_item ↔ order 解析 | **Adapter** | Nx 订单主权 | ❌ 不抽 | 高 |
| `DisRouteDispatchReadModelAssembler` | 旧 Map  enrichment | **Adapter** | Nx 实体直出 | ❌ 调试链暂留 | 中 |
| `DisRouteDispatchServiceImpl.simulate()` | 全量持久化 simulate | **Adapter 遗留** | 写 plan/task，非 Core 语义 | ⚠️ 归档 | 中 |
| `NxDisRouteDispatchController` | HTTP 入口 | **Adapter** | Spring + Nx 租户 disId | ❌ 不抽 | — |
| `NxDisRoutePlanEntity` / `NxDisShipmentTaskEntity` | 持久化模型 | **Adapter** | 表结构绑定生鲜 | ❌ 不抽（需新 schema） | 高 |

---

## 3. 当前派单后端类职责分类（摘要）

完整列表见 §2。按包归纳：

| 包/层 | 倾向 | 代表 |
|-------|------|------|
| `com.nongxinle.route.DisRouteSandbox*Timeline*` | Core | TimelineBuilder |
| `com.nongxinle.route.DisRouteDispatchCardTemplateBuilder` | Core | Store/Driver 卡 |
| `com.nongxinle.route.DisRoute*Helper`（时间/格式/duty） | Core | Temporal, DisplayFormat |
| `com.nongxinle.route.DisRouteEligibleOrderPolicy` | Adapter | do_status |
| `com.nongxinle.route.DisRoute*Bill*` / `*Purchase*` | Adapter | 打单/采购 |
| `com.nongxinle.service.impl.DisRouteSandbox*ServiceImpl` | Adapter | 读写 Nx 表 |
| `com.nongxinle.entity.NxDis*` | Adapter | ORM 实体 |
| `com.nongxinle.dto.route.Dispatch*CardDto` | Core 合同 | 跨页模板 |
| `com.nongxinle.dto.route.SandboxToday*` | Core 结构 + Adapter 字段混用 | section 卡仍带 Nx 状态名 |
| `com.nongxinle.dto.route.DriverDispatchCandidateDto` | Adapter 遗留 | 旧 drivers[] |

---

## 4. 命名污染审计

| 命名模式 | 未来 Core 是否保留 | 未来应归 Adapter | 不应出现在 Core |
|----------|-------------------|------------------|-----------------|
| `Nx` 前缀（`NxDisRoutePlan`） | ❌ | ✅ 全部实体/Dao | 通用 DTO/接口 |
| `Dis` 前缀（`DisRoute*`） | ⚠️ 可改为 `Dispatch*` | 与 Nx 绑定的 Service | — |
| `Department` / `depFatherId` / `departmentId` | ❌ | ✅ 客户/门店 ID | 应用 `Stop.siteId` / `CustomerRef` |
| `Bill` / `Print` | ❌ | ✅ | Core |
| `Order` / `NxDepartmentOrder` | ❌ | ✅ | Core 用 `DeliveryDemand` / `CargoLine` |
| `Purchase` / `purchase_status` | ❌ | ✅ | Core |
| `ShipmentTask` | ⚠️ 概念可泛化为 `DispatchTask` | ✅ 当前表名 | — |
| `Sandbox` | ✅ Core 语义（内存模拟） | — | — |
| `ManualDispatch` | ✅ Core | — | — |
| `Community` / `Gb` | ❌ | ✅ 其他项目 Adapter | Core |
| `storeCard` / `driverCard` / `pageViewModel` | ✅ Core 合同名 | — | — |
| `confirmMode` / `primaryAction` | ✅ Core | — | — |
| `executionDriverRoutes` / `loadingWorkbench` | ❌ 废弃 | 调试 Adapter | Core |
| `simulateAction` | ❌ 已删 | — | Core（用 `primaryAction`） |
| `eligible-drivers`（API 名） | ❌ 从未存在 | 内部变量 `sandboxIneligibleDrivers` | — |

---

## 5. 正式接口最小合同表

| 接口 | 正式页面 | 数据根 | 旧字段已清？ | 仍返回的旧/并存字段 | 保留原因 | 删除条件 |
|------|----------|--------|-------------|---------------------|----------|----------|
| `GET /dispatch/sandbox/today` | today.js | `data.pageViewModel` | ✅ | 无 | — | — |
| `GET /dispatch/loading/today` | loading.js | `data.pageViewModel` | ✅ | 无 | — | — |
| `GET /loading/today` | driverLoading.js（带 driverUserId） | `data.pageViewModel` | ✅ 旧 Map 已删 | 元数据：`routeDate*`、`warnings` | 页面上下文 | 司机专用 contract 稳定后可只返 pageViewModel |
| `GET /delivery/today` | delivery.js、driverDelivery.js | `data.pageViewModel` | ✅ | 元数据同上 | 同上 | 同上 |
| `GET /drivers/available` | duty.js | `data.driverCards[]` + `data.summary` | ✅ | 无 | — | — |
| `POST /sandbox/manual-dispatch/driver-panorama` | manualDispatchDrivers.js | `storeCard` + `drivers[]` + `summary` | ✅ | 无 | — | — |
| `POST /sandbox/manual-dispatch/edit-page` | manualRouteEdit.js | `data.pageViewModel` | ✅ | `baselineRoute`/`insertPositions[]`（非展示主链） | simulate/confirm payload | 前端完全不读 stops 后收缩 |

**调试/非正式（勿当合同）**

| 接口 | 说明 |
|------|------|
| `GET /sandbox/today` | debug-only 全量；today.js 仍调但只读 pageViewModel |
| `GET /plan/today` | 旧 plan 读模型 |

---

## 6. 旧概念全仓搜索结果

| 关键词 | 命中位置（代表） | 分类 | 处理 |
|--------|------------------|------|------|
| `eligible-drivers` | 无 API | 已删除/从未存在 | 文档注明即可 |
| `eligibleDrivers` / `sandboxIneligibleDrivers` | `DisRouteSandboxComputeServiceImpl`、EligibilityHelper | 3 调试/内部 | 保留内部逻辑 |
| `simulateAction` | 已删 DTO；`DispatchDriverCardDto` 注释 | 1 已删除 | 注释可低 token 改 `primaryAction` |
| `executionDriverRoutes` | `buildToday` 全量、`RouteDispatchReadModelAssembler` | 3 调试接口 | 随 `GET /sandbox/today` 归档 |
| `executionSummary` / `deliveryWorkbench` | 同上 | 1 已从正式 delivery 删 | — |
| `loadingWorkbench` / `loadingDriverRoutes` | 同上 | 1 已从正式 loading 删 | — |
| `buildExecution*` / `buildLoading*`（前端） | jcJieDan 已删 | 1 已删除 | — |
| `buildExecutionRouteStatsLine` | `DisRouteSandboxTodayDriverRouteCardBuilder` | 4 正式主链（pageViewModel 内） | 保留，非旧链 |
| `POST /preview` | Controller 已删 | 1 已删除 | 归档文档已标注 |
| `POST /confirm`（顶层） | Controller 已删 | 1 已删除 | — |
| `simulationOnly()` | `ManualDispatchPanoramaCapabilities` | 1 已删除 | 工厂方法已移除 |
| `simulationOnlyDriverCount` | panorama summary | 1 已删除 | 字段已从 DTO 移除 |
| `FORBIDDEN` / `仅模拟` | `ManualDispatchConfirmMode` | Core 枚举 | 保留；OFF_DUTY 不进 drivers[] |
| `司机资料缺失` | `DisRouteDispatchDriverNameHelper` | Core 兜底 | 保留 |
| `insertOptions[]` | simulate 响应 / edit-page 内部 | simulate 链保留；**非页面主展示** | edit-page 用 routeTimeline |
| 前端组装 ViewModel | jcJieDan handoff：已禁止 | 1 已迁移 | — |

---

## 7. 司机端旧链 Known Gap

**旧 HTTP 已删除（2026-06-23）** — `NxDisRouteDispatchController` 无以下路径；勿再文档/封装为正式接口：

| 路径 | 状态 |
|------|------|
| `GET /driver/loading/today` | **HTTP 已删** |
| `GET /driver/delivery/today` | **HTTP 已删** |
| `GET /driver/route/today` | **HTTP 已删** |

| 现状 | 说明 |
|------|------|
| jcJieDan 司机页 | 实际调 `GET /loading|delivery/today?driverUserId=`（老板读模型） |
| 期望 | 司机扁平原子 `pageViewModel`（`stopList[]` 在根） |
| 阻塞 | 后端仍返老板 `sections[]` → 合同报错 |
| 迁移目标 | **新建**司机专用 pageViewModel 读接口；**不得**恢复 `driver/*/today` 实体直出 |
| 司机站点详情 | 无 `pageViewModel.stopDetail` 专用 API（另 gap） |

---

## 8. 文档清理结果

| 动作 | 路径 |
|------|------|
| **新建正式** | `docs/route-dispatch/Route-Dispatch-Future-Plugin-Boundary-20260622.md`（本文） |
| **更新正式** | `docs/route-dispatch/Route-Dispatch-Backend-Handoff-20260622.md` |
| **归档** | `docs/route-dispatch/archive/old-contracts/Phase1.5a-Route-Dispatch-API-ARCHIVED.md` |
| **归档** | `docs/route-dispatch/archive/old-contracts/Route-Dispatch-Sandbox-Today-Phase3a1c-Frontend-ARCHIVED.md` |
| **保留+废弃头** | `docs/nxPlatform/Phase1.5a-Route-Dispatch-API.md` |
| **保留+废弃头** | `docs/nxPlatform/Route-Dispatch-Sandbox-Today-Phase3a1c-Frontend.md` |
| **保留（补充合同）** | `docs/nxPlatform/Route-Dispatch-Manual-Dispatch-Phase2B-Contract.md` |
| **保留（设计）** | `docs/nxPlatform/Route-Dispatch-Sandbox-Design.md` |

---

## 9. 后续低 token 小任务清单

| 字段/接口 | 优先级 | 预计范围 | 说明 |
|-----------|--------|----------|------|
| `summary.summaryLine`（duty） | P2 | 1 DTO + 1 builder | 若仍有缺失仅补一行 |
| `cardKey` on timeline nodes | P3 | TimelineBuilder | 前端可选 |
| `deltaLine` / `simulationSummary` 边角 | P3 | edit-page builder | 已有主路径 |
| `drawerSubtitle` / `bottomHint` | P3 | 已补，验收入即可 | — |
| `toneClass` / `toggleButtonDisabled` | P3 | CardTemplateBuilder | UI 细项 |
| `manualConstraintSummary` | P2 | 已补 stop 卡 | 验收入 |
| `navPendingLabel` | P2 | 司机 delivery pageViewModel | 需司机 contract 先做 |
| 删 `drivers[]` 并存 | P1 | DriverDispatchListResponse | ✅ HTTP 已不序列化 |
| 司机 pageViewModel 专用 builder | P1 | 新 builder 或 slice | `loading/today?driverUserId` 目标态 |

---

## 10. 已决议（原不确定项，2026-06-23）

1. **`POST /simulate` HTTP**：全仓无正式调用 → **已删**；`DisRouteDispatchService.simulate()` 保留。
2. **`GET /sandbox/today`**：today.js 仍用（只读 pageViewModel）→ **保留 debug-only**，响应双层标记。
3. **`drivers/available.drivers[]`**：duty 只用 `driverCards[]` → **HTTP 不再序列化**。
4. **司机旧 HTTP**（`GET /driver/loading/today`、`/driver/delivery/today`、`/driver/route/today`）：Controller **已删**；未来新建 pageViewModel，**勿恢复**上述路径。
5. **`SIMULATION_ONLY`** / **`simulationOnlyDriverCount`**：无业务路径 → **已删**。

---

*审计完成：2026-06-23。不确定项已归零（除司机扁平原子 contract 待专迭代）。*
