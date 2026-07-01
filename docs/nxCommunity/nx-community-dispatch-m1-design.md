# nxCommunity 商城派单 M1 设计

> Checkpoint: `checkpoint-before-nxcommunity-dispatch-20260630`  
> API 前缀: `api/nxcommunitydispatch/*`  
> 独立表前缀: `nx_community_dispatch_*` / `nx_community_order_dispatch`

## 1. 边界

- **独立**于 `todaydispatch`、`route/dispatch`、`nx_dis_*`、`NxDepartmentOrders*`。
- **不改** `getIsDeliveryOrders`、`getDeliverRoute`、`deliverySavePindin` 等旧接口。
- 派单状态与 `nxCoStatus`（支付/打印/核销）**分离**，扩展表 `nx_community_order_dispatch`。
- 司机身份: `NxCommunityUserEntity`，`nxCouRoleId = 5`。
- 沙箱仅展示/模拟；**编辑路线确认**后写入正式派单表。
- **无站点操作按钮**：分派中/装车中列表 timeline 不展示 per-stop 操作按钮；调整路线请进「编辑路线」。

## 2. 表结构

| 表 | 说明 |
|----|------|
| `nx_community_dispatch_plan` | 门店 + 路线日计划头 |
| `nx_community_dispatch_driver_route` | 司机路线 |
| `nx_community_dispatch_stop` | 配送站点 |
| `nx_community_dispatch_stop_item` | 站点 ↔ 商城订单 |
| `nx_community_dispatch_driver_duty` | 司机值班（M1 可选） |
| `nx_community_order_dispatch` | 订单派单扩展（1:1 订单） |

DDL: `docs/sql/patches/upgrade_nx_community_dispatch_m1.sql`

## 3. 状态枚举

### 3.1 订单派单 (`nx_cod_dispatch_status`)

`UNASSIGNED` / `CANCELLED` → confirm upsert 同一条记录 → `LOADING` → `IN_DELIVERY` → `DELIVERED` / `EXCEPTION`

**当前态表（非历史）**：`nx_community_order_dispatch` 每订单最多一行（`uk_cod_order`）；confirm 时 insert 或 update。路线编辑确认时移除的站点，dispatch 行置 `UNASSIGNED` 并解绑 stop/driver，订单回到 eligible 池。

沙箱模拟 **不落库** `SIMULATED`；pageViewModel 用 `simulated: true` 标记。

### 3.2 站点 (`nx_cds_stop_status`)

`ASSIGNED` → `LOADING` → `IN_DELIVERY` → `DELIVERED` / `EXCEPTION` / `CANCELLED`

**M1 语义说明（避免混淆）**

| 状态 | 含义 |
|------|------|
| `LOADING`（confirm 后） | **已确认分配司机**，订单进入司机装车页；**不等于**司机已开始物理装车 |
| `IN_DELIVERY`（depart-now 后） | 司机已发车，配送途中 |
| `DELIVERED`（complete-now 后） | 本站点送达完成 |

主链：`confirm → LOADING → depart-now → IN_DELIVERY → complete-now → DELIVERED`

### 3.3 司机路线 (`nx_cddr_route_status`)

`DRAFT` → `LOADING` → `IN_DELIVERY` → `COMPLETED` / `IDLE`

路线级 `LOADING` 与站点级相同：**confirm 后进入装车页**，不是“装车动作已完成”。

### 3.4 与 `nxCoStatus`

| nxCoStatus | 派单关系 |
|----------|----------|
| ≥2 已支付 + serviceType=1 | 可进 eligible 池 |
| 5 / 99 | 不进池；取消时派单同步 CANCELLED |
| 送达 complete | 仅对本 stop 下绑定的订单 sync `nxCoStatus=5`（经 DAO，不改旧 Controller） |

**complete-now 更新 nxCoStatus 边界**

- 只遍历 `nx_community_dispatch_stop_item` 中 **本 stopId** 绑定的订单 ID。
- 这些订单必须先经 **confirm** 写入派单链（`nx_community_order_dispatch` + stop_item），不会扫全表。
- eligible 池已排除 `serviceType≠1`（自提）、`status=99`（取消）、`status=5`（已完成）；旧配送接口订单若无 dispatch 行，不会进入 stop_item。
- M1 未在 complete 时二次校验订单状态；若 confirm 后订单被取消，存在误置 `nxCoStatus=5` 风险（见 §8 风险点）。

**complete-now 二次校验（M1 已修）**

- 拒绝非 `IN_DELIVERY` 的 stop / route；
- 校验 driverUserId、communityId；
- 逐单校验 serviceType=1、非取消/非已完成、dispatch 绑定本 stop 且状态 `IN_DELIVERY`；
- 任一订单不通过则整单拒绝 complete，不更新 `nxCoStatus`。

## 4. M1 接口清单

| 方法 | 路径 | 阶段 |
|------|------|------|
| GET | `/dispatch/sandbox/today` | P2 |
| GET | `/drivers/available` | P2 |
| POST | `/sandbox/stops/confirm` | P3 |
| POST | `/sandbox/driver-route-edit/{page,preview,confirm}` | P3 |
| GET | `/dispatch/loading/today` | P4 |
| GET | `/driver-terminal/loading/today` | P4 |
| POST | `/drivers/{driverUserId}/depart-now` | P4 |
| GET | `/driver-terminal/delivery/today` | P4 |
| POST | `/delivery/stops/{stopId}/complete-now` | P4 |

## 5. 主链（M1 必须完整）

```
eligible 订单池
  → 沙箱模拟路线（内存）
  → confirm → plan / driver_route / stop / stop_item / order_dispatch
  → 装车 loading
  → depart → 配送中
  → complete → 送达
```

## 6. 包结构

```
com.nongxinle.community.dispatch.*
com.nongxinle.entity.NxCommunityDispatch*Entity
com.nongxinle.dao.NxCommunityDispatch*Dao
```

## 7. 参考 todaydispatch（只读）

分层: Facade → Compute → Confirm → Assembler → Controller  
不可复用: `DisRouteSandboxComputeService`、`nx_dis_*` DAO。

## 8. M1 已知风险点

1. ~~**complete 无二次校验**~~：已加 stop/route/dispatch/订单全量校验，失败整单拒绝。
2. **driver_duty 未接入**：`nx_community_dispatch_driver_duty` 已建表，沙箱/派单未过滤值班状态。
3. **沙箱算法极简**：按地址分组 + 司机轮询，无路线优化与地图 polyline。
4. **无集成测试 / curl 自动化**：M1 仅编译 + 建表自检，接口需手工验收。
