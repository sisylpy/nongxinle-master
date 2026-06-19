# Phase 2b / 2c 设计报告（修订版）

> **核心原则（必读）：**  
> 平台订单**不是**配送商小程序里的「单独出库流程」。  
> 平台订单必须**进入现有出库聚合链路**，与配送商自有订单**按同一商品（如油菜）合并出货**；  
> 仅在**显示层**平台客户优先、平台行打标签；  
> **出库完成后**同步 `READY_FOR_PICKUP`，供 Electric 安排平台司机取货。  
>
> **不做：** 快照、客户确认、支付、单独平台出库、破坏聚合逻辑、TSP/导航/运营大屏。

---

## 0. 业务边界速查

| 平台订单跳过 | 平台订单保留（复用现有出库端） |
|-------------|-------------------------------|
| 重新选品 | 订单客户列表 |
| 重新分配配送商 | 按客户查看订单 |
| 旧协作链（collaborative） | **出货中按商品聚合** |
| 清空/篡改平台已定价 | 商品下客户明细 |
| 待平台分配（PENDING） | 称重 / 确认数量 |
| | 出库完成（沿用现有按钮） |

**平台订单判定（后端）：**

```sql
EXISTS (
  SELECT 1 FROM nx_platform_order_assign poa
  WHERE poa.nx_poa_order_id = dor.nx_department_orders_id
    AND poa.nx_poa_assign_mode = 'PLATFORM'
    AND poa.nx_poa_assign_status = 'ASSIGNED'
)
AND dor.nx_DO_distributer_id = #{当前配送商 disId}
```

---

## 1. 当前配送商小程序 — 订单客户列表

### 1.1 主接口

| 接口 | 路径 | 用途 |
|------|------|------|
| `disGetTodayOrderCustomer` | `GET/POST api/nxdepartmentorders/disGetTodayOrderCustomer/{disId}` | 今日订货 **客户列表**（小程序首页常见） |
| `disGetTypePrepareOutDepCata` | `POST .../disGetTypePrepareOutDepCata` | **出库端**按部门列表（出货流程内客户 Tab） |

### 1.2 查询链路

**`disGetTodayOrderCustomer`**

```
Controller: NxDepartmentOrdersController ~8299
  → queryOrderDepartmentList(map)     // disId, status<3, isSelfOrder=-1
  → batchQueryDepartmentOrderStats(depFatherIds)
```

- SQL：`NxDepartmentOrdersDao.xml` → `queryOrderDepartmentList`
- 条件：`nx_DO_distributer_id = disId`，`nx_DO_status < 3`，`collaborative_nx_dis_id = -1`
- **无** `excludePlatformPendingOrders`（PENDING 因 distributerId=NULL 通常不可见）
- **ASSIGNED 平台单：可见**（assign 后 disId 已写入）

**`disGetTypePrepareOutDepCata`**

```
  → queryPureOrderNxDepartmentSimple(map)  // disId, status<3, purStatus=4, orderGoodsType
```

- 出库场景下「有订单的客户」列表
- 另有协作商列表 `queryOfferOrderNxDistributer`（平台单 `collaborative=-1`，**不走协作分支**）

### 1.3 Phase 2b 改造要点（客户列表）

| 项 | 方案 |
|----|------|
| JOIN | `queryOrderDepartmentList` / `queryPureOrderNxDepartmentSimple` LEFT JOIN `nx_platform_order_assign` + LEFT JOIN `nx_platform_order_fulfillment` |
| 返回字段 | 见 §6 |
| 排序 | Controller 或 Service **分组排序**：`platformCustomers[]` 在上，`ownCustomers[]` 在下；或单列表加 `isPlatformOrder` 后 sort |
| 前端结构 | 「平台客户」区块 + 「我的客户」区块（同接口两段数组，或单数组+排序键） |

**示例（前端）：**

```
平台客户
  三店    [平台订单]  待处理 3

我的客户
  米线李  待处理 5
  配测    待处理 2
```

---

## 2. 当前「出货中」— 商品聚合接口

### 2.1 主接口

| 接口 | 条件摘要 | Service / SQL |
|------|----------|---------------|
| `disGetTypePrepareOutCata` | `purStatus=1`, `dayuOrderStatus=-2` | `queryGreatGrandOrderFatherGoodsUltraSimple` — **商品大类/catalog** |
| `disGetTypePrepareOutPage` | `purStatus=4`, `purType=0`, 分页 | `queryOutGoodsWithOrdersUltraSimple` — **出货中商品+订单** |
| `disGetTypePrepareOrder` | `purStatus=4`, `purType=0` | `disGetOutStockGoodsApplyForStock` — 旧版树形（部分门店仍用） |

**「出货中」核心（新版简化）：** `disGetTypePrepareOutPage`

```
Controller ~3877
  map: disId, status<3, dayuOrderStatus>-2, purStatus=4, purType=0
  → queryOutGoodsWithOrdersUltraSimple
  → 返回 PageUtils<List<OutGoodsSimpleDTO>>
```

### 2.2 聚合 SQL 逻辑（`queryOutGoodsWithOrdersUltraSimple`）

文件：`NxDepartmentOrdersDao.xml` ~9021

1. **内层子查询**：从 `nx_department_orders` + `nx_distributer_goods` 取 **DISTINCT dis_goods_id**（按商品去重分页）
2. **外层 JOIN**：同一 `dis_goods_id` 下挂 **所有客户订单行** → `OutGoodsSimpleDTO.nxDepartmentOrdersEntities`
3. 过滤：`nx_DO_distributer_id = disId`，`nx_DO_purchase_status < purStatus`，`nx_DO_purchase_goods_id = -1`（purType=0 出库商品），`collaborative = -1`

**关键：聚合键 = `nx_distributer_goods_id`（配送商 SKU），不是 orderId。**  
平台订单与自有订单若 **同一 disGoodsId**，天然在同一 `OutGoodsSimpleDTO` 下。

### 2.3 Phase 2b — 聚合总量**不拆分**

| 规则 | 说明 |
|------|------|
| totalQuantity | 仍 = 该 disGoodsId 下**所有**订单 quantity 之和（平台+自有） |
| 可选统计 | DTO 增加 `platformLineCount` / `ownLineCount`；或前端对 `nxDepartmentOrdersEntities` 按 `isPlatformOrder` 汇总 `platformQuantity`、`ownQuantity` |
| **禁止** | 按 platform/own 拆成两个 `OutGoodsSimpleDTO` 或两条出库记录 |

**油菜示例（正确）：**

```
油菜  合计 15 斤
  [平台] 三店   10 斤
  米线李         3 斤
  配测           2 斤
```

---

## 3. 商品下客户明细接口

### 3.1 数据来源

**不是独立接口**，而是 `OutGoodsSimpleDTO` 内嵌：

```
OutGoodsSimpleDTO
  ├── nxDistributerGoodsId / nxDgGoodsName / ...
  └── nxDepartmentOrdersEntities: List<OutOrderSimpleDTO>
```

相关查询：

- `queryOutGoodsWithOrdersUltraSimple`（分页出货）
- `disGetNxGoodsApplyUltraSimple`（`disGetTypePrepareOutByDep` 按客户查商品）

DTO 定义：

- `OutGoodsSimpleDTO.java`
- `OutOrderSimpleDTO.java`（含 department 名称、orderCode、quantity 等）

### 3.2 Phase 2b 改造

| 项 | 方案 |
|----|------|
| JOIN | 明细 SQL 对 `ndo` LEFT JOIN `poa` + `pof` |
| `OutOrderSimpleDTO` 增字段 | `isPlatformOrder`, `orderSource`, `platformAssignId`, `platformFulfillmentStatus`, `platformLabel` |
| 排序 | Service 层对 `nxDepartmentOrdersEntities`：**platform 行优先**，再保留原 `df.nx_department_id` 顺序 |
| 标签 | 前端：`platformLabel = "平台"` / `"平台订单"` |

---

## 4. 出库完成接口（READY_FOR_PICKUP 挂载点）

### 4.1 现有「出库完成」端点

| 端点 | 作用 | 设置 |
|------|------|------|
| **`disOutOrdersFinish/{ids}`** | 批量出库完成（主路径） | `purchaseStatus=4`(FINISH_OUT)，有价则 `status=2`，weight=quantity |
| `disOutOrdersWithWeightFinish` | 带重量批量完成 | 同上 |
| **`giveOrderWeightForStockPrintAndFinish`** | 称重+出库一步 | `purchaseStatus=4`, `status=2` |
| 其它 | `disOutOrdersFinish` 变体、库存扣减相关 ~4500–5700 行 | 均可能设 `purchaseStatus=4` |

文件：`NxDepartmentOrdersController.java`

**业务含义对齐：** 用户定义的「配送商出库/备货完成」≈ `nxDoPurchaseStatus = 4`（`NX_DEP_ORDER_FINISH_OUT`），**不是**单独新按钮。

### 4.2 Phase 2b — 挂接 fulfillment（推荐单点 + 兜底）

**主挂载点（推荐）：** `NxDepartmentOrdersServiceImpl.update` 包装方法或 AOP，当：

```
purchaseStatus 变为 4
AND EXISTS platform assign (ASSIGNED)
AND fulfillment_status NOT IN (READY_FOR_PICKUP, PICKED_UP, ...)
→ UPDATE pof SET fulfillment_status = 'READY_FOR_PICKUP', ready_at = NOW()
```

**兜底：** 在 `disOutOrdersFinish`、`giveOrderWeightForStockPrintAndFinish` 显式调用 `PlatformFulfillmentService.markReadyForPickup(orderId)`。

**不在此阶段做：** 新建「平台出库完成」独立 API。

### 4.3 平台单出库前需注意

| 风险 | 说明 |
|------|------|
| `nxDoCostPrice` | `disOutOrdersFinish` 强依赖 costPrice 算 costSubtotal；平台 assign **未写** costPrice → Phase 2b 需 assign 时写默认成本或出库完成处判空 |
| 价格保护 | 平台单已有 `nxDoPrice`；出库时 **禁止** 清空或改为 ≤0.1 |
| 编辑守卫 | `exchangeDepApplyGoods`、`editDepApplyGoods`、`disInitOrderStatus` 等对 platform 单应拒绝或受限 |

---

## 5. 哪些接口需要 LEFT JOIN `nx_platform_order_assign`

### 5.1 必须 JOIN（读 + 展示标记）

| SQL ID | 触发接口 |
|--------|----------|
| `queryOrderDepartmentList` | `disGetTodayOrderCustomer` |
| `queryPureOrderNxDepartmentSimple` | `disGetTypePrepareOutDepCata` |
| `batchQueryDepartmentOrderStats` | 客户列表统计（可选：platformPendingCount） |
| `queryOutGoodsWithOrdersUltraSimple` | `disGetTypePrepareOutPage` |
| `disGetNxGoodsApplyUltraSimple` | `disGetTypePrepareOutByDep` |
| `queryGreatGrandOrderFatherGoodsUltraSimple` | `disGetTypePrepareOutCata`（若大类需 platform 角标） |
| `queryNotWeightDisOrdersSimpleByParams` | `phoneGetToFillDepOrders`（标记+守卫，非单独流程） |

### 5.2 建议 JOIN `nx_platform_order_fulfillment`

同上读接口 + Electric `detail` 类接口；写接口仅 fulfillment 服务内部。

### 5.3 已有 PENDING 排除（保持 + 扩展）

`excludePlatformPendingOrders` 片段目前在 3 处；Phase 2b 建议 **所有 disId 过滤的配送商 SQL 统一 include**，防止池化 disId 泄漏。

### 5.4 共享 SQL 片段（实施时）

```xml
<sql id="platformOrderJoinSelect">
  poa.nx_poa_id AS platform_assign_id,
  poa.nx_poa_assign_status AS platform_assign_status,
  poa.nx_poa_assign_mode AS platform_assign_mode,
  pof.nx_pof_fulfillment_status AS platform_fulfillment_status,
  CASE WHEN poa.nx_poa_id IS NOT NULL
            AND poa.nx_poa_assign_mode = 'PLATFORM'
            AND poa.nx_poa_assign_status = 'ASSIGNED'
       THEN 1 ELSE 0 END AS is_platform_order,
  CASE WHEN ... THEN 'PLATFORM' ELSE 'OWN' END AS order_source
</sql>

LEFT JOIN nx_platform_order_assign poa ON poa.nx_poa_order_id = ndo.nx_department_orders_id
LEFT JOIN nx_platform_order_fulfillment pof ON pof.nx_pof_order_id = ndo.nx_department_orders_id
```

---

## 6. 返回字段规范

### 6.1 订单行 / 客户行

| 字段 | 类型 | 说明 |
|------|------|------|
| `orderSource` | `PLATFORM` / `OWN` | 订单来源 |
| `isPlatformOrder` | `1` / `0` | 是否平台已分配单 |
| `platformAssignId` | int? | `nx_poa_id` |
| `platformFulfillmentStatus` | string? | pof 状态 |
| `platformLabel` | string? | `"平台"` / `"平台订单"`（前端展示） |

### 6.2 商品聚合层（可选）

| 字段 | 说明 |
|------|------|
| `totalQuantity` | 不变，全量 |
| `platformQuantity` | 可选，后端或前端汇总 |
| `ownQuantity` | 可选 |
| `hasPlatformLines` | 可选，方便 UI 角标 |

### 6.3 客户列表层

| 字段 | 说明 |
|------|------|
| `isPlatformCustomer` | 该 dep 下存在 platform ASSIGNED 待处理单 |
| `platformPendingCount` | 平台待处理行数 |
| 分组 | `platformDepartments[]` + `ownDepartments[]` 或 sortKey |

---

## 7. 手机端排序与展示

### 7.1 客户列表

1. 后端返回 `isPlatformCustomer` 或分区数组  
2. 前端：**平台客户区块置顶**  
3. 行内 badge：`platformLabel`  
4. 同组内：原 `pinyin` / `settle_type` 排序不变  

### 7.2 出货中商品明细

1. **不拆商品卡片**  
2. `OutOrderSimpleDTO` 列表：`isPlatformOrder=1` 排前  
3. 平台行显示「平台」标签  
4. 商品标题下可展示：`平台 10 + 自有 5 = 15`（可选）  

---

## 8. `nx_platform_order_fulfillment` 表设计

### 8.1 职责

- **不取代**出库端；仅表达 **平台履约进度**（Electric + 司机可见）
- `assign_status` = 分配主权；`fulfillment_status` = 履约进度

### 8.2 推荐 DDL 草案

```sql
CREATE TABLE nx_platform_order_fulfillment (
    nx_pof_id                  INT NOT NULL AUTO_INCREMENT,
    nx_pof_market_id           INT NOT NULL,
    nx_pof_order_id            INT NOT NULL,
    nx_pof_platform_assign_id  INT NOT NULL,
    nx_pof_department_id       INT NOT NULL,
    nx_pof_distributer_id      INT NOT NULL,
    nx_pof_dis_goods_id        INT NOT NULL,
    nx_pof_fulfillment_status  VARCHAR(32) NOT NULL DEFAULT 'ASSIGNED',
    nx_pof_exception_code      VARCHAR(32) NULL,
    nx_pof_exception_note      VARCHAR(512) NULL,
    nx_pof_ready_for_pickup_at DATETIME NULL,
    nx_pof_picked_up_at        DATETIME NULL,
    nx_pof_delivered_at        DATETIME NULL,
    nx_pof_created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    nx_pof_updated_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (nx_pof_id),
    UNIQUE KEY uk_pof_order (nx_pof_order_id),
    KEY idx_pof_dis_status (nx_pof_distributer_id, nx_pof_fulfillment_status),
    KEY idx_pof_market_ready (nx_pof_market_id, nx_pof_fulfillment_status)
);
```

### 8.3 状态机（修订）

```
ASSIGNED                    ← assign 成功创建
  → SUPPLIER_PROCESSING     ← 可选：进入现有出库备货（purchaseStatus≥1）时自动
  → READY_FOR_PICKUP        ← 【关键】现有出库完成 purchaseStatus=4 时同步
  → PICKED_UP               ← 司机取货
  → DELIVERING              ← 配送中
  → DELIVERED               ← 送达
  → SUPPLIER_EXCEPTION      ← 配送商异常（Phase 2b 可后置 API）
```

**CONFIRMED / PAID / 快照：** 后续阶段，不在 2b/2c。

---

## 9. Electric 如何展示平台履约状态

### 9.1 Phase 2a 订单分配页（增强）

- 中栏订单行增加 `platformFulfillmentStatus` 只读列  
- ASSIGNED 且 `SUPPLIER_PROCESSING`：「配送商出库中」  
- `READY_FOR_PICKUP`：「待司机取货」（2c 调度入口）  

### 9.2 Phase 2c 司机调度 Tab

- 左：仅 `READY_FOR_PICKUP` 订单/客户池  
- 中：批次站点预览（取货 + 送货）  
- 右：平台司机 + 派单  

---

## 10. 司机批次 / 站点表（Phase 2c 草案）

### 10.1 司机主权

- 司机归属 **`marketId` 平台**，不挂靠单一配送商  
- 复用 `disDriverUserLogin` 登录能力时，增加 **平台司机身份表**：

```sql
nx_platform_driver (
  nx_pd_id, nx_pd_market_id, nx_pd_user_id,  -- 关联 nx_distributer_user 或独立账号
  nx_pd_name, nx_pd_phone, nx_pd_status, ...
)
```

### 10.2 批次与站点

| 表 | 说明 |
|----|------|
| `nx_platform_driver_batch` | 司机 + 日期 + batch_status |
| `nx_platform_driver_batch_stop` | stop_seq, stop_type=`PICKUP_SUPPLIER`\|`DROP_CUSTOMER`, stop_phase, lat/lng, stop_status |
| `nx_platform_driver_batch_order` | batch ↔ order_id ↔ delivery_stop |
| `nx_platform_driver_batch_pickup` | order_id ↔ distributer_id ↔ pickup_stop（一单多配送商取货点） |

### 10.3 两段路线

1. **MARKET_PICKUP**：配送商 A→B→C（`nxDistributerLan/Lun`）  
2. **CUSTOMER_DELIVERY**：客户 1→2→3（`nxDepartmentLat/Lng`）  

第一版：简单距离排序 + **Electric 手动拖拽**；无 TSP、无导航。

### 10.4 司机确认

| 动作 | stop | fulfillment |
|------|------|-------------|
| 到达配送商 | ARRIVED | — |
| 已取货 | COMPLETED | PICKED_UP |
| 到达客户 | ARRIVED | DELIVERING |
| 已送达 | COMPLETED | DELIVERED |
| 异常 | EXCEPTION | 挂起 |

---

## 11. 坐标字段可用性

| 实体 | 字段 | 类型 | 现状 |
|------|------|------|------|
| `NxDistributerEntity` | `nxDistributerLan` / `nxDistributerLun` | String 可空 | 经/纬（命名历史原因 Lan≈lng） |
| | `nxDistributerAddress` | String | 文本地址 |
| `NxDepartmentEntity` | `nxDepartmentLat` / `nxDepartmentLng` | String 可空 | 客户坐标 |
| | `nxDepartmentAddress` | String | 文本地址 |

**已有校验：** `NxDepartmentOrdersPartController.isValidCoordinate()`（客户坐标，中国范围）。

**配送商坐标：** 字段存在，使用较少，**缺失率可能更高** — 2c 前建议 SQL 抽样。

**Electric 无坐标：** 红点提示 + 允许手动排序 + 不参与距离排序。

---

## 12. 配送商异常（Phase 2b 最小 / 可后置）

接口草案：`POST api/platform/supplier/reportException`

| code | 含义 |
|------|------|
| OUT_OF_STOCK | 缺货 |
| INSUFFICIENT_QTY | 数量不足 |
| QUALITY_ISSUE | 质量不合适 |
| DELAY | 无法按时备货 |
| REJECT | 拒单/无法供货 |
| OTHER | 其它 |

→ `fulfillment_status = SUPPLIER_EXCEPTION`；Electric 告警。**2b 第一版可只做表字段 + 文档，API 后置。**

---

## 13. Phase 2b 最小开发范围

**主题：平台订单融入现有出库端**

| # | 交付项 |
|---|--------|
| 1 | DDL `nx_platform_order_fulfillment` |
| 2 | assign 成功 → insert pof（ASSIGNED） |
| 3 | 读接口 JOIN poa/pof + 返回 §6 字段 |
| 4 | 客户列表：平台客户置顶 + 标签 |
| 5 | `OutOrderSimpleDTO`：平台行优先排序 + 标签 |
| 6 | **不拆**商品聚合；可选 platform/own 数量统计 |
| 7 | 出库完成钩子 → `READY_FOR_PICKUP` |
| 8 | 平台单编辑守卫（禁换品/改价/协作/重置） |
| 9 | costPrice 出库兼容 |
| 10 | PENDING SQL 排除补齐 |
| 11 | Electric 履约状态只读展示 |
| 12 | 文档 + 配送商小程序对接说明 |

**不做：** 快照、支付、确认、单独平台出库 UI、司机调度、异常完整流转。

---

## 14. Phase 2c 最小开发范围

**主题：平台司机调度**

| # | 交付项 |
|---|--------|
| 1 | `nx_platform_driver` + batch/stop/order/pickup 四表 |
| 2 | Electric 调度 Tab（READY 池 → 手动排序 → 派单） |
| 3 | 司机任务 API + 小程序任务页 |
| 4 | 取货/送货确认 → pof 状态联动 |
| 5 | 坐标红绿点 + 缺失提示 |
| 6 | 验收：多配送商取货 + 多客户送货 |

**不做：** TSP、导航、CONFIRMED/PAID、快照、运营大屏。

---

## 15. 实施顺序建议

```
Phase 2a ✅ 分配主链
    ↓
Phase 2b  融入出库端 + fulfillment + READY_FOR_PICKUP
    ↓
Phase 2c  平台司机批次调度
    ↓
（更后）  客户 CONFIRMED/PAID → 供货快照
```

---

## 16. 禁止项（当前阶段）

1. 客户供货快照  
2. 客户确认 / 支付  
3. 自动路线优化 / 地图导航  
4. 供应商评分 / 运营大屏  
5. 小程序**客户**下单端改造  
6. **平台订单单独出库流程**  
7. 破坏现有按商品聚合出库逻辑  
8. 平台 assign 走协作链  

---

## 附录：关键代码索引

| 模块 | 路径 |
|------|------|
| 客户列表 | `NxDepartmentOrdersController.disGetTodayOrderCustomer` |
| 出库客户 | `disGetTypePrepareOutDepCata` |
| 出货聚合 | `disGetTypePrepareOutPage` → `queryOutGoodsWithOrdersUltraSimple` |
| 出货 catalog | `disGetTypePrepareOutCata` |
| 出库完成 | `disOutOrdersFinish`, `giveOrderWeightForStockPrintAndFinish` |
| 聚合 DTO | `OutGoodsSimpleDTO`, `OutOrderSimpleDTO` |
| 平台 assign | `PlatformOrderAssignServiceImpl` |
| PENDING 排除 | `NxDepartmentOrdersDao.xml` `excludePlatformPendingOrders` |
| 客户坐标校验 | `NxDepartmentOrdersPartController.isValidCoordinate` |

---

*只读梳理，未修改代码。与 [Phase2a-Backend-Confirmation.md](./Phase2a-Backend-Confirmation.md) 配套。*
