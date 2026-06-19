# Phase 2b SQL Patch 草案

> **文件：** [`docs/sql/patches/upgrade_nx_platform_phase2b_fulfillment.sql`](../sql/patches/upgrade_nx_platform_phase2b_fulfillment.sql)  
> **状态：** 草案，待确认后进入 Java 实现。  
> **前置：** `upgrade_nx_platform_phase2a.sql`

---

## 1. 执行顺序

```
1. upgrade_nx_platform_phase2a.sql     （若已执行可跳过）
2. upgrade_nx_platform_phase2b_fulfillment.sql
3. （可选）同文件 §3 BACKFILL — 历史 ASSIGNED 补 fulfillment
4. （可选）同文件 §4 — 已出库历史单 → READY_FOR_PICKUP
5. 部署 Phase 2b Java
```

**注意：** `upgrade_nx_platform_snapshot_phase2b.sql` 是**供货快照**预埋，与本次履约表**无关**，勿混跑。

---

## 2. `nx_platform_order_fulfillment` 字段说明

| Java/文档名 | 数据库列 | 类型 | 说明 |
|-------------|----------|------|------|
| pofId | `nx_pof_id` | INT PK AI | 履约行主键 |
| marketId | `nx_pof_market_id` | INT NOT NULL | 市场 ID |
| orderId | `nx_pof_order_id` | INT NOT NULL | 订单 ID，**UK**，与 assign 1:1 |
| platformAssignId | `nx_pof_platform_assign_id` | INT NOT NULL | `nx_platform_order_assign.nx_poa_id` |
| departmentId | `nx_pof_department_id` | INT NOT NULL | 客户部门 |
| nxGoodsId | `nx_pof_nx_goods_id` | INT NULL | 标准商品 ID（冗余） |
| distributerId | `nx_pof_distributer_id` | INT NOT NULL | 履约配送商 |
| disGoodsId | `nx_pof_dis_goods_id` | INT NOT NULL | 配送商商品 |
| fulfillmentStatus | `nx_pof_fulfillment_status` | VARCHAR(32) | 见 §3 |
| costMissing | `nx_pof_cost_missing` | TINYINT | 0/1，**不阻断**出库与 READY |
| costMissingReason | `nx_pof_cost_missing_reason` | VARCHAR(128) | 如 `NO_VALID_BUYING_PRICE` |
| readyForPickupAt | `nx_pof_ready_for_pickup_at` | DATETIME | 出库完成、待司机取货 |
| pickedUpAt | `nx_pof_picked_up_at` | DATETIME | Phase 2c 预留 |
| deliveredAt | `nx_pof_delivered_at` | DATETIME | Phase 2c 预留 |
| createdAt | `nx_pof_created_at` | DATETIME | 创建时间 |
| updatedAt | `nx_pof_updated_at` | DATETIME | 更新时间 |
| updatedBy | `nx_pof_updated_by` | INT NULL | 操作员 |

### 与 `nx_platform_order_assign` 分工

| 表 | 职责 |
|----|------|
| `nx_platform_order_assign` | **分配主权**：PENDING / ASSIGNED |
| `nx_platform_order_fulfillment` | **履约进度**：出库完成、司机取送（2c） |

`assign_status` 在分配完成后保持 **ASSIGNED**，不因出库而改。

---

## 3. `fulfillment_status` 枚举

| 状态 | Phase | 含义 |
|------|-------|------|
| `ASSIGNED` | **2b v1** | assign / backfill 创建；等待配送商现有出库 |
| `READY_FOR_PICKUP` | **2b v1** | 配送商出库完成，货已备好，待平台司机 |
| `PICKED_UP` | 2c 预留 | 司机已从配送商取货 |
| `DELIVERING` | 2c 预留 | 配送中 |
| `DELIVERED` | 2c 预留 | 已送达客户 |
| `SUPPLIER_EXCEPTION` | 后续 | 配送商异常（2b v1 无 API） |

---

## 4. 索引

| 索引名 | 列 | 用途 |
|--------|-----|------|
| `uk_pof_order` | `nx_pof_order_id` | 一订单一行履约 |
| `idx_pof_platform_assign` | `nx_pof_platform_assign_id` | 按 assign 反查 |
| `idx_pof_market_status` | market + status | Electric 按市场看待取货池 |
| `idx_pof_distributer_status` | distributer + status | 配送商小程序履约只读 |
| `idx_pof_market_dis_status` | market + distributer + status | 市场内某配送商履约列表 |
| `idx_pof_department` | department_id | 客户维度 |
| `idx_pof_dis_goods` | dis_goods_id | 商品维度统计 |

---

## 5. 成本字段语义（`cost_missing`）

| 规则 | 说明 |
|------|------|
| 存储位置 | fulfillment 表，**不**用 cost_missing 阻断履约 |
| `cost_missing=0` | assign/出库时已解析有效 buying 成本（Java 写 `nxDoCostPrice`） |
| `cost_missing=1` | 无有效 buying 价；出库与 `READY_FOR_PICKUP` **仍可进行** |
| `cost_missing_reason` | 机器可读原因码 + 日志 |

**有效 buying 成本（Java 解析，非 SQL）：**

- 来源：`nx_dg_buying_price` / `_one` / `_two` / `_three`
- 有效：严格 **> 0.1**
- **禁止：** 销售价 `nxDoPrice` → `nxDoCostPrice`；禁止 0.1 兜底；禁止假成本

---

## 6. BACKFILL 说明

§3 INSERT：

- 源：`nx_platform_order_assign` WHERE `PLATFORM` + `ASSIGNED`
- 排除：已有 pof 的 orderId；**不含 PENDING**
- 初始：`fulfillment_status=ASSIGNED`，`cost_missing=0`

§4 UPDATE（注释态，按需打开）：

- 已将 `nx_DO_purchase_status >= 4` 的历史单升为 `READY_FOR_PICKUP`

---

## 7. PlatformOrderGuard 边界（Java 后续，非 SQL）

**阻止：** 改销售价、换商品、重新分配、协作链、删单/重置（平台 ASSIGNED 单）

**不阻止：** 确认重量/数量、出库完成、`purchaseStatus`/`status` 推进、出库聚合流程

> 平台订单 = **锁定配送商与商品**；期望价由 assign 写入；**实际价可在出库流程中修改**。

---

## 8. Phase 2b v1 状态链

```
assign 成功 → INSERT pof (ASSIGNED)
配送商现有出库完成 → UPDATE pof (READY_FOR_PICKUP, ready_for_pickup_at)
Electric → 只读 fulfillmentStatus
```

**不做：** 接单/备货中/拒单按钮、司机代码、快照、支付、客户确认。

---

## 9. 后续 Java 改动点清单（确认 SQL 后实施）

| # | 模块 | 改动 |
|---|------|------|
| 1 | SQL patch | 执行本文件 + 可选 backfill |
| 2 | Entity/Dao/XML | `NxPlatformOrderFulfillmentEntity`, Dao, `NxPlatformOrderFulfillmentDao.xml` |
| 3 | Service | `PlatformFulfillmentService`：`createOnAssign`, `markReadyForPickup`, `isPlatformAssigned` |
| 4 | assign | `PlatformOrderAssignServiceImpl.assign` → create pof + `PlatformDisGoodsCostResolver` |
| 5 | 成本 | `PlatformDisGoodsCostResolver`（buying price 档位解析，>0.1） |
| 6 | 出库 | `PlatformOutboundFinishSupport`；改 `disOutOrdersFinish`, `giveOrderWeightForStockPrintAndFinish` 等 |
| 7 | Guard | `PlatformOrderGuard`；`updateOrderPrice` 等写入口 |
| 8 | 读 SQL | JOIN poa/pof：`queryOrderDepartmentList`, `queryPureOrderNxDepartmentSimple`, `queryOutGoodsWithOrdersUltraSimple`, `disGetNxGoodsApplyUltraSimple` |
| 9 | DTO | `OutOrderSimpleDTO` + 平台字段；Service 平台行/客户置顶排序 |
| 10 | Controller | `disGetTodayOrderCustomer` 返回 platform 标记/分区 |
| 11 | Electric | 履约状态只读列 |
| 12 | 文档 | 配送商小程序对接说明 |

**不做（2b v1）：** 司机批次、快照、支付、确认、异常 API、独立平台出库。

---

## 10. Phase 2b 验收 SQL

### 10.1 表与索引

```sql
SHOW CREATE TABLE nx_platform_order_fulfillment;

SHOW INDEX FROM nx_platform_order_fulfillment;
-- 预期：uk_pof_order, idx_pof_platform_assign, idx_pof_market_status,
--       idx_pof_distributer_status, idx_pof_market_dis_status
```

### 10.2 BACKFILL 后数量一致

```sql
-- ASSIGNED 平台 assign 行数（不含 PENDING）
SELECT COUNT(*) AS assigned_platform_assigns
FROM nx_platform_order_assign
WHERE nx_poa_assign_mode = 'PLATFORM'
  AND nx_poa_assign_status = 'ASSIGNED';

-- fulfillment 行数应 <= assigned（新 assign 在 Java 前可能少 1）
SELECT COUNT(*) AS fulfillment_rows
FROM nx_platform_order_fulfillment;

-- 不应有 PENDING 订单被 backfill
SELECT COUNT(*) AS pending_leaked_into_pof
FROM nx_platform_order_fulfillment pof
INNER JOIN nx_platform_order_assign poa ON poa.nx_poa_order_id = pof.nx_pof_order_id
WHERE poa.nx_poa_assign_status = 'PENDING';
-- 预期：0
```

### 10.3 一订单一行

```sql
SELECT nx_pof_order_id, COUNT(*) AS cnt
FROM nx_platform_order_fulfillment
GROUP BY nx_pof_order_id
HAVING cnt > 1;
-- 预期：空
```

### 10.4 历史 ASSIGNED 样例（如 orderId=200171）

```sql
SELECT
    poa.nx_poa_order_id,
    poa.nx_poa_assign_status,
    poa.nx_poa_assigned_distributer_id,
    poa.nx_poa_assigned_dis_goods_id,
    pof.nx_pof_fulfillment_status,
    pof.nx_pof_cost_missing,
    pof.nx_pof_ready_for_pickup_at
FROM nx_platform_order_assign poa
LEFT JOIN nx_platform_order_fulfillment pof ON pof.nx_pof_order_id = poa.nx_poa_order_id
WHERE poa.nx_poa_assign_mode = 'PLATFORM'
  AND poa.nx_poa_order_id = 200171;
-- backfill 后应有 pof，status=ASSIGNED（若未出库）
```

### 10.5 PENDING 不应有 fulfillment

```sql
SELECT poa.nx_poa_order_id, poa.nx_poa_assign_status, pof.nx_pof_id
FROM nx_platform_order_assign poa
LEFT JOIN nx_platform_order_fulfillment pof ON pof.nx_pof_order_id = poa.nx_poa_order_id
WHERE poa.nx_poa_assign_mode = 'PLATFORM'
  AND poa.nx_poa_assign_status = 'PENDING'
  AND pof.nx_pof_id IS NOT NULL;
-- 预期：空
```

### 10.6 出库完成后（Java 上线后跑）

```sql
SELECT
    dor.nx_department_orders_id,
    dor.nx_DO_purchase_status,
    poa.nx_poa_assign_status,
    pof.nx_pof_fulfillment_status,
    pof.nx_pof_ready_for_pickup_at,
    pof.nx_pof_cost_missing
FROM nx_department_orders dor
INNER JOIN nx_platform_order_assign poa ON poa.nx_poa_order_id = dor.nx_department_orders_id
LEFT JOIN nx_platform_order_fulfillment pof ON pof.nx_pof_order_id = dor.nx_department_orders_id
WHERE poa.nx_poa_assign_mode = 'PLATFORM'
  AND dor.nx_DO_purchase_status >= 4
ORDER BY dor.nx_department_orders_id DESC
LIMIT 20;
-- 预期：purchase_status>=4 的平台单 → fulfillment_status=READY_FOR_PICKUP
```

### 10.7 成本与销售价分离（Java 上线后）

```sql
SELECT
    dor.nx_department_orders_id,
    dor.nx_DO_price AS sales_price,
    dor.nx_DO_cost_price AS cost_price,
    pof.nx_pof_cost_missing
FROM nx_department_orders dor
INNER JOIN nx_platform_order_assign poa ON poa.nx_poa_order_id = dor.nx_department_orders_id
LEFT JOIN nx_platform_order_fulfillment pof ON pof.nx_pof_order_id = dor.nx_department_orders_id
WHERE poa.nx_poa_assign_mode = 'PLATFORM'
  AND poa.nx_poa_assign_status = 'ASSIGNED'
  AND dor.nx_DO_price IS NOT NULL
ORDER BY dor.nx_department_orders_id DESC
LIMIT 20;
-- 预期：有 cost 时 sales_price <> cost_price（除非碰巧相等）；无 cost 时 cost_missing=1
```

---

## 11. 相关文档

- [Phase2b-Implementation-Plan.md](./Phase2b-Implementation-Plan.md)
- [Phase2b-Outbound-Integration-Design.md](./Phase2b-Outbound-Integration-Design.md)
- [Phase2a-Backend-Confirmation.md](./Phase2a-Backend-Confirmation.md)
