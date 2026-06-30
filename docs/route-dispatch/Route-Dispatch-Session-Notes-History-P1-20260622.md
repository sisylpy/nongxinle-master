# 派单算法 & 历史配送偏好 P1 — 会话思路归档

> **用途**：本窗口聊天记录的浓缩交接稿，便于换窗口/换人后继续。  
> **测试环境**：`disId = 160`（配送商 ID，不是 marketId=1）  
> **日期**：2026-06-22 前后  
> **状态**：P1 已实现（代码在本地，**未 commit**，需 selective stage）

---

## 1. 我们在讨论什么

独立配送商老板的 **今日派车沙箱**，不是平台/market 多配送商合并项目。

主链 API（正式）：

```http
GET /api/nxdisroutedispatch/dispatch/sandbox/today?disId=160
→ 只返回 data.pageViewModel
```

Debug（deprecated，可看内部字段）：

```http
GET /api/nxdisroutedispatch/sandbox/today?disId=160
→ 含 sandboxSuggestedStops、deliveryHistoryPreferences 等
```

---

## 2. 当前测试背景（disId=160）

| 项 | 值 |
|---|---|
| 派单运行表 | 曾清空（见 `docs/sql/patches/cleanup_dis160_route_dispatch_data.sql`） |
| Live 订单 | 约 7 条 → 4 个客户（depFatherId 去重） |
| 在线司机 | 2 人 ON_DUTY：张司机 userId=**1**，小曹 userId=**294** |
| 沙盘现象 | **4 店全部分给张司机**，小曹在 `availableDrivers` 显示空闲 |

**结论**：这是现有 optimizer 的 **预期行为**，不是 bug（见 §3）。

---

## 3. 当前自动派单算法（未改）

### 3.1 流水线（compute）

```
eligible 订单 (status<3)
  → 按 depFatherId 聚合成 virtual stop
  → ON_DUTY 且未被 block 的司机 → dispatchEligibleDrivers
  → BalancedInsertion2OptRouteOptimizer（腾讯驾车矩阵）
  → suggestedStops / unassignedStops
  → pageViewModel sections
```

核心类：

- `DisRouteSandboxComputeServiceImpl.compute()`
- `BalancedInsertion2OptRouteOptimizer`（`BALANCE_WEIGHT = 0.35`）

### 3.2 为什么 4 店给 1 个司机

- 目标：**路线成本优先** + 软均衡（0.35），不是均分
- 4 店地理上可串成一条顺路 → 继续往已有路线追加比第二司机从仓库新开便宜
- 同分时 **列表第一个司机**（userId 升序 → 张司机=1）先拿到第一站，易滚雪球
- 无「每车至少 N 店」硬约束

### 3.3 真实 optimizer 决策（dis160 实测重放）

插入顺序（离仓库远→近）：二号店 → 一号店 → 四点 → 三号店  
最终路线（2-opt 后）：**二号店 → 三号店 → 一号店 → 四点**，全部分给张司机。

逐步原因摘要：每一步都是「顺路追加 insertionCost」压过「均衡 penalty + 小曹空车从仓库出发」。

---

## 4. 业务方向（已写入文档，H2 才实现）

**派单不是单纯求最短路线。**

优先级（目标态）：

1. **尊重历史配送事实** — 谁常给这个店送
2. **尊重老板手动调过的顺序** — `manual_stop_seq` / `manual_locked`
3. **无历史 / 历史司机不可派 / 冲突时** — 再用地图 optimizer

当前 **P1 只读历史，不改变分派结果**。4 店给 1 司机仍会复现。

---

## 5. 历史事实从哪读（表审计结论）

### 5.1 主历史源

**`nx_dis_shipment_task`**（不要用 `nx_dis_route_stop` 作新主链唯一来源；Phase 3a.1 后 confirm 不写 route_stop）

| 用途 | 字段 |
|---|---|
| 客户 | `nx_dst_dep_father_id` |
| 实际司机 | `nx_dst_assigned_driver_user_id` |
| 顺序 | `COALESCE(nx_dst_manual_stop_seq, nx_dst_route_seq)` |
| 人工锁定 | `nx_dst_manual_locked = 1`（权重 ×2） |
| 真正完成 | `nx_dst_status = 'DELIVERED'` 且 `nx_dst_delivered_at IS NOT NULL` |
| 订单明细 | `nx_dis_shipment_task_item.nx_dsti_live_order_id` |

辅助：`nx_dis_driver_route`（`nx_ddr_driver_user_id`）、`nx_distributer_user`（司机名）

### 5.2 不算强历史的状态

`SIMULATED` / `UNASSIGNED` / `ASSIGNED` / `READY_TO_GO` / `IN_DELIVERY` / `EXCEPTION` / `CANCELLED` — P1 一律 **不统计**。

### 5.3 阶段区分（task.status）

| 阶段 | status |
|---|---|
| 系统建议（内存） | SIMULATED |
| 老板 confirm | ASSIGNED |
| 装车确认 | READY_TO_GO |
| 出发 | IN_DELIVERY |
| 送达 | DELIVERED |

---

## 6. 历史配送偏好 P1 — 已实现内容

### 6.1 服务

```text
DisRouteDeliveryHistoryPreferenceService.resolve(request)
```

**插入点**：`DisRouteSandboxComputeServiceImpl.compute()`  
`buildVirtualTasks()` **之后**、`runOptimization()` **之前**  
→ 挂载到 `SandboxComputeResult.deliveryHistoryPreferences`  
→ **optimizer 未改**

### 6.2 输入

- `disId`
- `depFatherIds` — 来自 **virtualTasks**（仅待分派、未 confirm 的客户）
- `eligibleDriverUserIds` — 来自 `dispatchEligibleDrivers`
- `lookbackDays` 默认 180
- `minDeliveredTimes` 默认 1

配置（可选 properties）：

```properties
dis.route.dispatch.history.lookback-days=180
dis.route.dispatch.history.manual-locked-weight=2.0
dis.route.dispatch.history.min-delivered-times=1
```

### 6.3 输出（每 depFatherId）

| 字段 | 说明 |
|---|---|
| preferredDriverUserId / Name | 仅在 **今日 eligible** 司机中选 |
| deliveredTimes / recentDeliveredAt / avgStopSeq / manualLockedTimes | 偏好司机统计 |
| confidence | 0~1，**解释用**，不参与派单 |
| reason | 见 `DisRouteDeliveryHistoryReason` |
| historicalTop* | debug：全历史主导（即使今日不可派） |
| candidateDrivers[] | debug：eligible 候选列表 |

### 6.4 reason 码

- `HISTORY_DOMINANT_DRIVER`
- `HISTORY_TIE_BROKEN_BY_RECENCY`
- `INSUFFICIENT_HISTORY` — 仅当 `deliveredTimes < minDeliveredTimes`（默认 min=1 **不会**因 1 次送达触发）
- `NO_HISTORY`
- `PREFERRED_DRIVER_NOT_ELIGIBLE`
- `NO_ELIGIBLE_DRIVER`
- `MULTIPLE_EQUAL_CANDIDATES`

### 6.5 NO_HISTORY 输出口径（已修正）

```text
deliveredTimes = 0
manualLockedTimes = 0
totalDeliveredTimesAllDrivers = 0
confidence = 0
recentDeliveredAt / avgStopSeq = null
```

### 6.6 minDeliveredTimes=1

**1 次 DELIVERED = 有效历史**，`reason` 可为 `HISTORY_DOMINANT_DRIVER`，confidence 自然偏低（freq 项约 0.33），**不会**标成无效。

### 6.7 HTTP 暴露

| 接口 | deliveryHistoryPreferences |
|---|---|
| `GET /dispatch/sandbox/today` | **不暴露**（仅 pageViewModel） |
| `GET /sandbox/today`（debug） | **有** `deliveryHistoryPreferences` |

### 6.8 重要：偏好只算「当前待分派」客户

已 confirm / 已送达且无新 live order 的客户 **不在** `virtualTasks` → **不会出现在** `preferencesByDepFatherId`。

要看到某店的历史偏好：**complete 后需再下一单**，该店才会重新进入待分派池。

---

## 7. 新增/修改文件清单（P1）

**新增：**

- `com.nongxinle.service.DisRouteDeliveryHistoryPreferenceService`
- `com.nongxinle.service.impl.DisRouteDeliveryHistoryPreferenceServiceImpl`
- `com.nongxinle.route.DisRouteDeliveryHistoryReason`
- `com.nongxinle.route.DisRouteDeliveryHistoryPreferenceReadModelAssembler`
- `com.nongxinle.dto.route.DeliveryHistoryPreference*`（Request/BatchResult/Dto/AggRow/CandidateDto）

**修改：**

- `NxDisShipmentTaskDao` + `.xml`（`queryDeliveryHistoryAggByDepAndDriver`）
- `DisRouteDispatchSettings`（history 配置项）
- `SandboxComputeResult`（`deliveryHistoryPreferences`）
- `DisRouteSandboxComputeServiceImpl`（挂载，不改 optimizer）
- `DisRouteSandboxTodayServiceImpl`（debug 输出）
- `docs/route-dispatch/Route-Dispatch-Backend-Handoff-20260622.md` §18
- `docs/route-dispatch/Route-Dispatch-Future-Plugin-Boundary-20260622.md`

**明确未改：**

- `BalancedInsertion2OptRouteOptimizer`
- 正式 `pageViewModel` 结构
- 小程序/前台

---

## 8. 验收步骤（待你本机跑）

### 8.1 清数据后（无历史）

```bash
curl -s "http://localhost:8080/nongxinle_war_exploded/api/nxdisroutedispatch/sandbox/today?disId=160"
```

预期：4 个 dep 均为 `NO_HISTORY`，统计字段为 **0**。

### 8.2 完整链路造历史

```text
POST /sandbox/stops/confirm        （例：三号店 1515 → 张司机 1）
POST /tasks/{taskId}/confirm-loading
POST /driver-routes/{id}/depart
POST /delivery/stops/{taskId}/complete
```

DB 验证：

```sql
SELECT nx_dst_dep_father_id, nx_dst_status, nx_dst_assigned_driver_user_id,
       nx_dst_delivered_at,
       COALESCE(nx_dst_manual_stop_seq, nx_dst_route_seq) AS stop_seq
FROM nx_dis_shipment_task
WHERE nx_dst_distributer_id = 160 AND nx_dst_status = 'DELIVERED';
```

### 8.3 再下单后查偏好

给 **同一客户** 再下一单 → 再调 debug `/sandbox/today`  
预期该 dep 出现：`preferredDriverUserId=1`，`deliveredTimes>=1`，`reason=HISTORY_DOMINANT_DRIVER`。

### 8.4 正式接口

```bash
curl -s ".../dispatch/sandbox/today?disId=160"
```

预期：`data` 只有 `{ "pageViewModel": ... }`。

---

## 9. 后续 H2（**本轮不做**）

1. 按 `preferredDriverUserId` 做 dep → driver **预绑定**
2. 按 `avgStopSeq` / manual 记录做路线内 **顺序种子**
3. 无历史 stop 再进 optimizer
4. 历史司机不可派 → fallback optimizer
5. 可选：optimizer 内 preference score（单靠 score 很难压过路程差，建议预绑定优先）

插入点仍在 `runOptimization` 之前，从 `SandboxComputeResult.deliveryHistoryPreferences` 读。

---

## 10. 相关 SQL / 文档路径

| 文件 | 用途 |
|---|---|
| `docs/sql/patches/cleanup_dis160_route_dispatch_data.sql` | 清空 nx_dis_* 派单表 |
| `docs/sql/patches/query_dis160_eligible_live_orders.sql` | eligible 订单查询 |
| `docs/route-dispatch/Route-Dispatch-Backend-Handoff-20260622.md` | 后端交接 §17 清数据、§18 历史 P1 |
| `docs/route-dispatch/Route-Dispatch-Future-Plugin-Boundary-20260622.md` | Core/Adapter 边界 |
| `docs/nxPlatform/Route-Dispatch-Manual-Dispatch-Phase2B-Contract.md` | 人工派车契约 |

---

## 11. 已知 gap（讨论过，未修）

- `compute()` 查 eligible 订单时 **未把 page routeDate 传入 SQL**（部分订单可能因 arrive 日期不进沙盘）
- 司机端曾误传 `disId=1`（市场 ID）；当前模块 **只认 nxDistributerId=160**
- `today.js` 仍走 deprecated `GET /sandbox/today`（读 pageViewModel 没问题）

---

## 12. Git 状态提醒

P1 改动与大量无关文件（community/coupon/electron 等）混在工作区。  
**不要 `git add .`**；commit 时需 selective stage 仅 route-dispatch + 历史 P1 相关文件。  
**尚未 commit**（除非用户后续明确要求）。

---

*归档自 Cursor 会话：派单 optimizer 复盘 → 表审计 → 历史偏好 P1 设计与实现 → 验收口径补充。*
