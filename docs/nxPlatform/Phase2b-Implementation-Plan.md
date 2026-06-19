# Phase 2b 正式实施方案

> **状态：设计拍板，待开发。** 本文档不含 Java 实现，仅实施规格。  
> **前置：** Phase 2a 已跑通（assign 主链 + electron-platform 分配页）。  
> **原则：** 平台订单进入**现有出库聚合**；**锁定配送商/商品，实际价可改**；出库完成 → `READY_FOR_PICKUP`；**不阻断于成本缺失**。

---

## 0. 业务决策（已拍板）

### 0.1 销售价 vs 成本价

| 字段 | 含义 | 来源 |
|------|------|------|
| `nxDoPrice` | 客户成交**销售价** | Electric assign 确定，配送商端**只读** |
| `nxDoCostPrice` | 配送商**采购成本** | `NxDistributerGoodsEntity` **buying price** 解析 |

**禁止：**

- 把 `nxDoPrice` 复制为 `nxDoCostPrice`
- 用销售价（will price）当成本价
- 用 `0.1` 作有效成本兜底
- 制造假成本

**有效成本价：** 与销售价相同规则，**严格 > 0.1**（复用 `SalesPriceUtils.isValidSalesPrice` 或抽 `CostPriceUtils` 别名）。

### 0.2 平台单出库语义

**不是**「仅填价不称重」，而是：

> **平台订单：锁定配送商与商品；期望价 `nxDoExpectPrice` 由 assign 写入；配送商可改实际价 `nxDoPrice`。**

- 固定单位：可快捷确认数量（weight = quantity）
- 按斤/按重：仍走称重或数量确认
- 出库完成后 → `fulfillment_status = READY_FOR_PICKUP`

### 0.3 成本缺失与履约

- `READY_FOR_PICKUP` **不依赖**成本价完整
- 成本缺失 **不阻断**出库与平台履约
- 可记录 `costMissing` / warning / 日志，供后续成本核算

---

## 1. 现有出库完成接口 — costPrice 依赖（只读结论）

### 1.1 强依赖点

以下方法在计算 `nxDoCostSubtotal` 时 **无 null/有效性判断**，直接：

```java
BigDecimal nxDoCostPriceB = new BigDecimal(ordersEntity.getNxDoCostPrice());
```

| 方法 | 路径 | 风险 |
|------|------|------|
| `disOutOrdersFinish` | `/{ids}` ~2055 | `nxDoCostPrice` 为 null/空/非数字 → **NumberFormatException**，出库中断 |
| `giveOrderWeightForStockPrintAndFinish` | POST ~4488 | 同上 |
| `disOutOrdersWithWeightFinish` | POST ~2026 | 使用**请求体**传入的 costPrice，仍可能 NFE |
| `giveOrderWeightListForStockAndFinish` | POST ~4782 | 同上模式 |
| 其它 ~4919、5174、5528、5709 | 库存扣减相关出库 | 同上 |

**销售价分支：** `disOutOrdersFinish` 在有 `nxDoPrice` 时正常算 `nxDoSubtotal`；**成本分支无 guard**。

**Phase 2a 现状：** `applyDisGoodsToPlatformOrder` 只设 `nxDoCostPriceLevel="1"`，**未写** `nxDoCostPrice` → 平台单出库极易在现有代码上 **抛异常**。

### 1.2 最小兼容方案（Phase 2b 必做）

新增统一出口（规格名）：`PlatformOutboundFinishSupport`（或扩展现有 Service），所有 `purchaseStatus → 4` 的路径**收敛调用**：

```
finishStockOut(orderId, confirmedWeight):
  1. 加载订单 + 判断是否平台 ASSIGNED 单
  2. 销售价：平台单禁止改价；用锁定 nxDoPrice × weight → nxDoSubtotal
  3. 成本价：
     a. 若 nxDoCostPrice 已有效 → costSubtotal = costPrice × weight
     b. 若空/无效 → 从 disGoods buying price 再解析一次（PlatformDisGoodsCostResolver）
     c. 仍无效 → 跳过 costSubtotal，设 costMissing=true，打 warn 日志
     d. 绝不写假成本、绝不阻断
  4. nxDoPurchaseStatus = 4；有价则 nxDoStatus = 2
  5. 若平台单 → PlatformFulfillmentService.markReadyForPickup(orderId)
  6. update(order)
```

**第一版改造范围（最小）：**

- 必改：`disOutOrdersFinish`、`giveOrderWeightForStockPrintAndFinish`（小程序出货主路径）
- 建议同批：`disOutOrdersWithWeightFinish`、`giveOrderWeightListForStockAndFinish`
- 深库存路径（~5k 行）可 Phase 2b.1 跟进，但规格上同一 helper

---

## 2. DDL — `nx_platform_order_fulfillment`

**补丁文件（建议）：** `docs/sql/patches/upgrade_nx_platform_phase2b_fulfillment.sql`  
**执行顺序：** Phase 2a 四表已存在 → 本 patch → 再部署 Java。

```sql
CREATE TABLE IF NOT EXISTS nx_platform_order_fulfillment (
    nx_pof_id                   INT          NOT NULL AUTO_INCREMENT,
    nx_pof_market_id            INT          NOT NULL,
    nx_pof_order_id             INT          NOT NULL COMMENT 'nx_department_orders PK',
    nx_pof_platform_assign_id   INT          NOT NULL,
    nx_pof_department_id        INT          NOT NULL,
    nx_pof_distributer_id       INT          NOT NULL,
    nx_pof_dis_goods_id         INT          NOT NULL,
    nx_pof_fulfillment_status   VARCHAR(32)  NOT NULL DEFAULT 'ASSIGNED'
        COMMENT 'Phase2b-v1: ASSIGNED|READY_FOR_PICKUP|SUPPLIER_EXCEPTION(预留)',
    nx_pof_cost_missing         TINYINT      NOT NULL DEFAULT 0
        COMMENT '1=出库时未能解析有效成本价，不阻断履约',
    nx_pof_cost_missing_at      DATETIME     NULL,
    nx_pof_ready_for_pickup_at  DATETIME     NULL,
    nx_pof_exception_code       VARCHAR(32)  NULL COMMENT 'Phase2b 后续',
    nx_pof_exception_note       VARCHAR(512) NULL,
    nx_pof_created_at           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    nx_pof_updated_at           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (nx_pof_id),
    UNIQUE KEY uk_pof_order (nx_pof_order_id),
    KEY idx_pof_dis_status (nx_pof_distributer_id, nx_pof_fulfillment_status),
    KEY idx_pof_market_ready (nx_pof_market_id, nx_pof_fulfillment_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台订单履约扩展（不替代 nxDoStatus/出库聚合）';
```

**Phase 2b v1 状态仅用：**

- `ASSIGNED` — assign / fulfillment 创建
- `READY_FOR_PICKUP` — 出库完成
- （表字段预留 `SUPPLIER_EXCEPTION`，**v1 不做 API**）

---

## 3. assign 成功时创建 fulfillment + 写成本价

### 3.1 钩子位置

`PlatformOrderAssignServiceImpl.assign()` 在：

```
applyDisGoodsToPlatformOrder → applyPlatformAssignPricing → update(order)
→ upsertDefaultAndSwitchLog → poa ASSIGNED
```

**新增（顺序）：**

1. `applyPlatformAssignCost(order, disGoods)` — 解析 buying price
2. `platformFulfillmentService.createOnAssign(poa, order)` — insert pof，`ASSIGNED`

### 3.2 `PlatformDisGoodsCostResolver`（新组件）

路径建议：`com.nongxinle.service.platform.PlatformDisGoodsCostResolver`

**输入：** `NxDistributerGoodsEntity disGoods`, `String orderStandard`

**解析顺序（与旧链 `NxDistributerGoodsController` ~721 档位逻辑对齐）：**

| 优先级 | 条件 | 成本字段 | level |
|--------|------|----------|-------|
| 1 | `orderStandard` = `nxDgGoodsStandardname` | `nxDgBuyingPriceOne` + `OneUpdate` | `"1"` |
| 2 | `orderStandard` = `nxDgWillPriceTwoStandard` | `nxDgBuyingPriceTwo` + `TwoUpdate` | `"2"` |
| 3 | `orderStandard` = `nxDgWillPriceThreeStandard` | `nxDgBuyingPriceThree` + `ThreeUpdate` | `"3"` |
| 4 | 兜底 | `nxDgBuyingPrice` + `nxDgBuyingPriceUpdate` | `"1"` 或 `"0"` |

**有效：** 值 `> 0.1`（`SalesPriceUtils.isValidSalesPrice`）

**输出 `CostResolveResult`：**

```java
String costPrice;          // null if missing
String costPriceUpdate;
String costPriceLevel;
boolean costMissing;
```

**写入订单（assign 时）：**

```
if (valid costPrice):
  order.nxDoCostPrice = costPrice
  order.nxDoCostPriceUpdate = update
  order.nxDoCostPriceLevel = level
  order.nxDoCostSubtotal = costPrice × quantity  // 按 quantity 预填，出库时可按 weight 重算
else:
  不写 nxDoCostPrice
  log [platformAssignCost] costMissing disGoodsId=...
  // 不阻断 assign
```

---

## 4. 配送商客户列表 — SQL / 接口改造

### 4.1 涉及接口

| 接口 | SQL ID |
|------|--------|
| `disGetTodayOrderCustomer/{disId}` | `queryOrderDepartmentList` + `batchQueryDepartmentOrderStats` |
| `disGetTypePrepareOutDepCata` | `queryPureOrderNxDepartmentSimple` |

### 4.2 共享 JOIN 片段（新增 XML）

```xml
<sql id="platformOrderJoinFromDor">
  LEFT JOIN nx_platform_order_assign poa
    ON poa.nx_poa_order_id = dor.nx_department_orders_id
   AND poa.nx_poa_assign_mode = 'PLATFORM'
  LEFT JOIN nx_platform_order_fulfillment pof
    ON pof.nx_pof_order_id = dor.nx_department_orders_id
</sql>

<sql id="platformOrderFlagsSelect">
  poa.nx_poa_id AS platform_assign_id,
  poa.nx_poa_assign_status AS platform_assign_status,
  pof.nx_pof_fulfillment_status AS platform_fulfillment_status,
  pof.nx_pof_cost_missing AS platform_cost_missing,
  CASE WHEN poa.nx_poa_assign_status = 'ASSIGNED' THEN 1 ELSE 0 END AS is_platform_order,
  CASE WHEN poa.nx_poa_assign_status = 'ASSIGNED' THEN 'PLATFORM' ELSE 'OWN' END AS order_source
</sql>
```

### 4.3 客户级聚合（Service 层）

SQL 仍按 **department 去重**；Service 对结果集：

1. 查询每个 `depFatherId` 是否存在 `is_platform_order=1` 且未完成出库的订单（`purchase_status < 4` 或 fulfillment `ASSIGNED`）
2. 设置 `isPlatformCustomer = 1/0`
3. **排序：** `isPlatformCustomer DESC`，再原 `pinyin` / `settle_type`

**Controller 返回结构（兼容旧前端）：**

```json
{
  "platformDep": [ { "dep": {...}, "isPlatformCustomer": 1, "platformLabel": "平台客户", ... } ],
  "ownDep": [ ... ],
  "nxDep": [ ... merged sorted ... ]   // 可选：仍提供合并列表
}
```

v1 可只增强 `nxDep` 每项加字段 + 后端 sort，小程序自行分区展示。

### 4.4 batchQueryDepartmentOrderStats

可选增加：`platformPendingCount`（该平台客户下 ASSIGNED 且未 READY 行数）。**非 v1 必须。**

---

## 5. 出货中商品聚合 — SQL 改造

### 5.1 涉及接口

| 接口 | SQL ID | 说明 |
|------|--------|------|
| `disGetTypePrepareOutPage` | `queryOutGoodsWithOrdersUltraSimple` | **出货中主列表** |
| `disGetTypePrepareOutCata` | `queryGreatGrandOrderFatherGoodsUltraSimple` | 大类 catalog（可选角标） |
| `disGetTypePrepareOutByDep` | `disGetNxGoodsApplyUltraSimple` | 按客户查商品 |

### 5.2 改造原则

- **不改** DISTINCT `dis_goods_id` 聚合键
- **不改** `purStatus` / `purType` 过滤语义
- 在外层 `ndo` JOIN 增加 `platformOrderJoinFromDor` + flags
- **不增加** WHERE 把平台/自有拆开

### 5.3 可选汇总字段（Service）

对每个 `OutGoodsSimpleDTO`：

```java
platformLineCount, ownLineCount
platformQuantitySum, ownQuantitySum  // 展示用
totalQuantitySum                       // = 全量，与现网一致
```

---

## 6. 商品下客户明细 — 字段与排序

### 6.1 DTO 扩展

**`OutOrderSimpleDTO` 新增：**

| 字段 | 说明 |
|------|------|
| `isPlatformOrder` | 1/0 |
| `orderSource` | PLATFORM / OWN |
| `platformAssignId` | int |
| `platformFulfillmentStatus` | string |
| `platformLabel` | `"平台"` |
| `priceEditable` | 1（平台 ASSIGNED 单可改实际价） |
| `expectPrice` / `nxDoExpectPrice` | 平台分配期望价 |
| `actualPrice` / `nxDoPrice` | 配送商实际价 |
| `priceDifferent` / `nxDoPriceDifferent` | 实际价 − 期望价 |
| `platformPriceLabel` | `"期望价/实际价"` |

### 6.2 SQL

在 `queryOutGoodsWithOrdersUltraSimple` / `disGetNxGoodsApplyUltraSimple` 的 `ndo`  SELECT 中 `<include platformOrderFlagsSelect/>`。

### 6.3 排序（Service，不改 SQL ORDER BY 破坏商品序）

每个 `OutGoodsSimpleDTO.getNxDepartmentOrdersEntities()`：

```java
sort: isPlatformOrder DESC, 原 department 顺序
```

---

## 7. 平台客户 / 平台行置顶

| 层级 | 实现 |
|------|------|
| 客户列表 | `isPlatformCustomer DESC` + 前端「平台客户」分组 |
| 商品下明细 | `isPlatformOrder DESC` |
| 商品卡片 | **不拆分**；标题可选展示「平台 x + 自有 y = 合计 z」 |

---

## 8. 平台订单价格展示（Round 2-B.1）

### 8.1 读接口

返回 `priceEditable=1`、`expectPrice`、`actualPrice`、`priceDifferent`；**不返回 `priceLocked`**。

小程序可展示：

```
平台期望价：¥{expectPrice}
实际出库价：¥{actualPrice}
差价：{priceDifferent}
```

### 8.2 写接口边界（平台 ASSIGNED 单）

**允许：** 改实际价 `nxDoPrice`、确认重量/数量、出库完成。

**禁止（后续 Guard，非 Round 2-B.1）：** 改配送商、改商品、改 `nxDoExpectPrice`、换 SKU、删单/重置。

**改价后系统重算：** `nxDoSubtotal`、`nxDoPriceDifferent`；**不覆盖** `nxDoExpectPrice`。

---

## 9. 出库完成 → READY_FOR_PICKUP

### 9.1 触发条件

```
EXISTS poa (PLATFORM, ASSIGNED)
AND 出库完成：nxDoPurchaseStatus 变为 4
```

### 9.2 更新

```sql
UPDATE nx_platform_order_fulfillment
SET nx_pof_fulfillment_status = 'READY_FOR_PICKUP',
    nx_pof_ready_for_pickup_at = NOW(),
    nx_pof_cost_missing = :costMissingFlag,
    nx_pof_cost_missing_at = CASE WHEN :costMissingFlag=1 THEN NOW() ELSE nx_pof_cost_missing_at END
WHERE nx_pof_order_id = :orderId;
```

### 9.3 与 assign 状态关系

- `nx_platform_order_assign.assign_status` **保持 ASSIGNED**（分配主权不变）
- 履约进度只看 `pof.fulfillment_status`

---

## 10. costPrice 缺失留痕

| 时机 | 行为 |
|------|------|
| assign | 解析失败 → 不写 cost，`[platformAssignCost] costMissing` |
| 出库完成 | 再解析一次；仍失败 → `pof.cost_missing=1`，warn 日志 |
| READY_FOR_PICKUP | **照常写入** |
| Electric | 可选只读展示「成本缺失」角标（v1 简单文本即可） |

**日志示例：**

```
[platformOutboundCost] orderId=200173 costMissing=1 disGoodsId=31235 reason=no_valid_buying_price
[platformFulfillment] orderId=200173 → READY_FOR_PICKUP costMissing=1
```

---

## 11. Electric 只读展示（Phase 2b v1）

在 `electron-platform` 订单分配/详情中（或轻量「履约」列）：

| fulfillment_status | 展示 |
|--------------------|------|
| ASSIGNED | 已分配 / 配送商出库中 |
| READY_FOR_PICKUP | 待司机取货 |
| cost_missing=1 | 成本待补（不阻断） |

**不做：** 司机调度 UI、批次创建（Phase 2c 设计保留于 `Phase2b-Outbound-Integration-Design.md`）。

---

## 12. Phase 2b v1 最小交付清单

| # | 交付项 | 类型 |
|---|--------|------|
| 1 | `upgrade_nx_platform_phase2b_fulfillment.sql` | SQL |
| 2 | Entity/Dao/Service：`NxPlatformOrderFulfillment*` | Java |
| 3 | `PlatformDisGoodsCostResolver` + assign 写成本 | Java |
| 4 | assign 创建 pof `ASSIGNED` | Java |
| 5 | `PlatformOutboundFinishSupport` + 改 `disOutOrdersFinish` 等 | Java |
| 6 | `PlatformOrderGuard` + 改价入口拒绝 | Java |
| 7 | XML：`queryOrderDepartmentList`、`queryPureOrderNxDepartmentSimple`、`queryOutGoodsWithOrdersUltraSimple`、`disGetNxGoodsApplyUltraSimple` JOIN flags | XML |
| 8 | DTO：`OutOrderSimpleDTO` 等扩展 + Service 排序 | Java |
| 9 | `disGetTodayOrderCustomer` 返回 platform 字段 / 分区 | Controller |
| 10 | Electric 履约状态只读列 | 前端小改 |
| 11 | 文档：配送商小程序对接说明 | MD |

**明确不做（v1）：** 快照、确认、支付、司机代码、路线、导航、评分、大屏、独立平台出库、拆聚合、接单/备货中/异常 API。

---

## 13. Phase 2c 范围（仅设计，不开发）

保留：`nx_platform_driver`、`driver_batch`、取货/送货站点、Electric 调度 Tab、司机小程序任务。  
详见 `Phase2b-Outbound-Integration-Design.md` §10。

---

## 14. Phase 2b 验收用例

### 14.1 成本与 assign

| # | 步骤 | 预期 |
|---|------|------|
| A1 | assign 到 disGoods 有有效 `nxDgBuyingPriceOne` | `nxDoCostPrice` 写入 buying 价；**≠** `nxDoPrice` |
| A2 | assign 到 buying 价均为 0.1/空 | assign **成功**；`nxDoCostPrice` 空；pof 创建；日志 costMissing |
| A3 | 同商品 will price=1.5, buying price=1.2 | 销售 1.5，成本 1.2，互不混淆 |

### 14.2 出库聚合（油菜场景）

| # | 步骤 | 预期 |
|---|------|------|
| B1 | 平台客户三店 10 斤 + 自有客户 5 斤，同一 disGoodsId | **一条**油菜卡片，合计 15 斤 |
| B2 | 商品下明细 | 三店（平台标签）在上，自有在下 |
| B3 | 客户列表 | 「平台客户」区块在上 |

### 14.3 期望价 / 实际价 / 称重

| # | 步骤 | 预期 |
|---|------|------|
| C1 | 平台单调用 `updateOrderPrice` | **拒绝** |
| C2 | 平台单 `giveOrderWeightForStockPrintAndFinish` 确认重量 | **成功**；subtotal = 锁定价 × 重量 |
| C3 | 固定单位 `disOutOrdersFinish`（weight=quantity） | **成功** |

### 14.4 出库完成与履约

| # | 步骤 | 预期 |
|---|------|------|
| D1 | 平台单出库完成，有成本 | `purchaseStatus=4`；pof=`READY_FOR_PICKUP`；`cost_missing=0` |
| D2 | 平台单出库完成，无成本 | **仍成功**；pof=`READY_FOR_PICKUP`；`cost_missing=1` |
| D3 | 自有单出库 | pof 无记录；行为与现网一致 |
| D4 | Electric 查看 | 显示 READY_FOR_PICKUP / 成本缺失标记 |

### 14.5 回归

| # | 步骤 | 预期 |
|---|------|------|
| E1 | 纯自有订单出库 | 与 Phase 2a 前行为一致 |
| E2 | PENDING 平台单 | 仍不出现在配送商列表 |
| E3 | 协作单 | 不受影响（platform 单 `collaborative=-1`） |

---

## 15. 文件索引（实施时）

| 模块 | 路径 |
|------|------|
| assign | `PlatformOrderAssignServiceImpl.java` |
| 出库完成 | `NxDepartmentOrdersController.disOutOrdersFinish`, `giveOrderWeightForStockPrintAndFinish` |
| 销售价工具 | `SalesPriceUtils.java` |
| 销售价解析参考 | `PlatformDisGoodsValidator.java` |
| 成本档位参考 | `NxDistributerGoodsController.java` ~721 |
| 出货聚合 SQL | `NxDepartmentOrdersDao.xml` → `queryOutGoodsWithOrdersUltraSimple` |
| 客户列表 SQL | `queryOrderDepartmentList`, `queryPureOrderNxDepartmentSimple` |
| 出货 DTO | `OutGoodsSimpleDTO.java`, `OutOrderSimpleDTO.java` |
| 融入设计 | `Phase2b-Outbound-Integration-Design.md` |
| Phase 2a | `Phase2a-Backend-Confirmation.md` |

---

*文档版本：Phase 2b v1 实施规格（含成本价 buying price 拍板）。*
