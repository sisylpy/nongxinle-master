# Phase 2b / 2c 设计报告（只读）

> **前提：** Phase 2a 平台订单分配主链已跑通（submitLine → PENDING → assign → ASSIGNED）。  
> **快照：** 不做。快照必须在客户订单完成、收货、支付或确认认可之后（Phase 2b 预埋 SQL 仅保留，不实现业务）。  
> **本文档：** 只读梳理 + 设计建议，不含实现代码。

---

## 1. 配送商小程序订单处理现状

### 1.1 主 API 入口

配送商端（微信小程序）核心 Controller：

| 模块 | 路径前缀 | 文件 |
|------|----------|------|
| 订单主链 | `api/nxdepartmentorders` | `NxDepartmentOrdersController.java` |
| 路线/利润 | `api/nxdepartmentorderspart` | `NxDepartmentOrdersPartController.java` |
| 称重 | `api/nxdistributerweight` | `NxDistributerWeightController.java` |
| 采购备货 | `api/nxdistributerpurchasegoods` | `NxDistributerPurchaseGoodsController.java` |
| 司机用户 | `api/nxdistributeruser` | `NxDistributerUserController.java`（`disDriverUserLogin`） |

### 1.2 配送商「待处理」常用查询接口

| 接口 | 用途 | Service / SQL |
|------|------|---------------|
| `disGetTodayOrderCustomer/{disId}` | 今日订货客户列表 | `queryOrderDepartmentList` + `batchQueryDepartmentOrderStats` |
| `phoneGetToFillDepOrders` | 小程序填单价/重量 | `queryNotWeightDisOrdersSimpleByParams` |
| `disGetToWeightOrders` | 待称重（status=0） | `queryDepOrdersOrderFatherGoods` |
| `disGetToPlanPurchaseGoods` | 未计划采购 | `disGetUnPlanPurchaseApplysNew` ✅ 有 PENDING 排除 |
| `disGetStockGoods` / `disGetTypePrepareOrder` | 备货/出库 | `queryGrandGoodsOrder` / `disGetOutStockGoodsApplyForStock` |
| `queryDisOrdersByParams`（多处） | 通用配送商订单 | ✅ 有 PENDING 排除 |

过滤条件共性：`dor.nx_DO_distributer_id = #{disId}`，`nx_DO_status < 3`（未完成送货单前）。

### 1.3 现有 `nxDoStatus` / `nxDoPurchaseStatus` 语义

**`nxDoStatus`（`NxDistributerTypeUtils`）：**

| 值 | 常量 | 业务含义 |
|----|------|----------|
| -1 | GOUWU | 购物车/临时单 |
| -2 | （OCR） | 待修正 |
| 0 | NEW | 新订单 |
| 1 | PROCUREMENT | 有重量 |
| 2 | HAS_FINISHED | 价/重填完 |
| 3 | HAS_BILL | 已生成送货单 |
| 4 | RECEIVED | 客户已收货 |

**`nxDoPurchaseStatus`：**

| 值 | 含义 |
|----|------|
| 0 | 未采购 |
| 1 | 进入备货 |
| 2 | 已采购 |
| 3 | 采购完成 |
| 4 | 分拣/出库完成 |
| 5 | 配送完成（`deliveryOrder`） |

### 1.4 配送商端现有「操作」——无平台履约语义

旧链是 **填价 → 称重 → 采购 → 出库 → 配送 → 收货**，没有独立的：

- 接单 / 拒单
- 备货中 / 备货完成
- 等待司机取货

相关端点示例：`updateOrderWeight`、`saveToFillWeightAndPrice`、`disOutOrdersFinish`、`deliveryOrder`、`delete`/`disInitOrderStatus`（相当于取消/重置）。

**结论：** 现有按钮与状态**不能**直接表达平台履约所需的 7 种动作；强行复用 `nxDoStatus`/`nxDoPurchaseStatus` 会与打印、分拣、结算、历史单语义冲突。

---

## 2. 平台订单是否能进入配送商端

### 2.1 ASSIGNED 平台单

Phase 2a assign 后订单字段（已验收 orderId=200171）：

```
nxDoDistributerId = 160          ← 配送商可见的关键
nxDoDisGoodsId = 31235
nxDoStatus = 0                    ← 仍为「新订单」
nxDoPurchaseStatus = 0            ← 未采购
nxDoPrice / nxDoSubtotal 已写入   ← 平台已定价
nxDoCollaborativeNxDisId = -1     ← 非协作链
poa.assignStatus = ASSIGNED
```

**能否进入配送商列表：** **能**，凡 `WHERE nx_DO_distributer_id = disId` 且 `status < 3` 的查询均会命中。

典型入口：

- `phoneGetToFillDepOrders` — **会显示**（status<3，orderDisId=160）
- `disGetToWeightOrders` — **会显示**（equalStatus=0）
- `disGetTodayOrderCustomer` — **会计入**客户订单统计（queryOrderDepartmentList 按 disId）

**问题：** 平台单价格已在 assign 时写好，但 status 仍为 0，配送商小程序会把它当作「待填价/待称重新单」，**语义错位**——这是 Phase 2b 必须解决的集成缺口。

### 2.2 PENDING 平台单

PENDING 阶段（Phase 2a 实测 DB 允许 NULL）：

```
nxDoDistributerId = NULL
poa.assignStatus = PENDING
```

**能否进入配送商列表：** **不能**（`distributer_id = disId` 无法匹配 NULL）。

**例外风险：** 若 market 配置「平台池配送商」且 PENDING 写入 pool disId，则**未加 exclude 片段的查询**可能泄漏。当前仅 **3 个 SQL** 有 `excludePlatformPendingOrders`：

- `disGetUnPlanPurchaseApplys`
- `queryDisOrdersByParams`
- `queryDepOrdersAcount`

`phoneGetToFillDepOrders`、`queryOrderDepartmentList`、`queryDepOrdersOrderFatherGoods` 等**均无**排除 → Phase 2b 应统一补齐或改查询策略。

### 2.3 汇总

| 问题 | 结论 |
|------|------|
| ASSIGNED + disId 匹配能否看到？ | ✅ 能 |
| PENDING 是否已排除？ | ⚠️ 依赖 NULL distributerId；池化场景下部分接口未排除 |
| ASSIGNED 能否进待备货？ | ⚠️ 物理上能进旧「新单/填价/称重」列表，但无「备货完成/待取货」状态 |
| 平台 assign 是否走协作链？ | ✅ 否（`collaborativeNxDisId=-1`，不调 `saveCollaborativeOrderWhenNeeded`） |

---

## 3. 推荐的平台履约状态表

### 3.1 设计原则

1. **不污染 `nxDoStatus`** — 已被填价/称重/送货单/收货全链使用。
2. **不污染 `nxDoPurchaseStatus`** — 已被采购/分拣/出库/配送使用。
3. **扩展 `nx_platform_order_assign.assign_status` 不够** — 它只表达「分配主权」，无法承载备货时间线、司机事件、异常。
4. **新增独立履约表**，Electric + 配送商小程序 + 司机小程序统一读写的 **`fulfillment_status`**。

### 3.2 推荐表：`nx_platform_order_fulfillment`

```sql
-- 设计草案（Phase 2b 实施时再出正式 patch）
CREATE TABLE nx_platform_order_fulfillment (
    nx_pof_id                INT NOT NULL AUTO_INCREMENT,
    nx_pof_market_id         INT NOT NULL,
    nx_pof_order_id          INT NOT NULL,          -- uk，1:1 nx_department_orders
    nx_pof_platform_assign_id INT NOT NULL,
    nx_pof_department_id     INT NOT NULL,
    nx_pof_distributer_id    INT NOT NULL,          -- 冗余，便于配送商端索引
    nx_pof_dis_goods_id      INT NOT NULL,
    nx_pof_fulfillment_status VARCHAR(32) NOT NULL, -- 见下表
    nx_pof_exception_code    VARCHAR(32) NULL,      -- OUT_OF_STOCK|REJECTED|DELAY|OTHER
    nx_pof_exception_note    VARCHAR(512) NULL,
    -- 配送商侧时间戳
    nx_pof_supplier_accepted_at    DATETIME NULL,
    nx_pof_preparing_at            DATETIME NULL,
    nx_pof_ready_for_pickup_at     DATETIME NULL,
    nx_pof_supplier_operator_id    INT NULL,
    -- 司机侧时间戳（也可冗余到 batch_stop，此处留摘要）
    nx_pof_picked_up_at            DATETIME NULL,
    nx_pof_delivered_at            DATETIME NULL,
    nx_pof_confirmed_at            DATETIME NULL,   -- 客户确认/支付后
    nx_pof_created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    nx_pof_updated_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (nx_pof_id),
    UNIQUE KEY uk_pof_order (nx_pof_order_id),
    KEY idx_pof_dis_status (nx_pof_distributer_id, nx_pof_fulfillment_status),
    KEY idx_pof_market_status (nx_pof_market_id, nx_pof_fulfillment_status)
);
```

### 3.3 状态链

```
ASSIGNED                  ← assign 完成时创建 pof，初始状态
  → SUPPLIER_ACCEPTED     ← 配送商接单
  → PREPARING             ← 备货中（可选：接单后自动进入）
  → READY_FOR_PICKUP      ← 备货完成，等待平台司机取货
  → PICKED_UP             ← 司机已从配送商取货
  → DELIVERING            ← 司机配送中
  → DELIVERED             ← 司机送达客户
  → CONFIRMED             ← 客户确认收货（或 PAID 支付完成，二选一终态）
```

**异常分支（任意配送商阶段）：**

```
→ SUPPLIER_EXCEPTION      ← 缺货/拒单/无法备货（需平台运营介入）
```

**与 Phase 2a 关系：**

- `nx_platform_order_assign.assign_status` 保持 `ASSIGNED`（分配主权不变）
- 履约进度看 `nx_platform_order_fulfillment.fulfillment_status`
- **快照触发点：** `CONFIRMED` / `PAID` 之后（不在 2b/2c 实现）

### 3.4 配送商小程序应对应的 API（Phase 2b 新建，建议前缀 `api/platform/supplier/`）

| 动作 | 状态迁移 | 说明 |
|------|----------|------|
| 接单 | → SUPPLIER_ACCEPTED | 配送商确认接受平台单 |
| 开始备货 | → PREPARING | 可选，可与接单合并 |
| 标记可取货 | → READY_FOR_PICKUP | **司机调度前置条件** |
| 缺货/拒单 | → SUPPLIER_EXCEPTION | 写 exception_code/note |

旧 `phoneGetToFillDepOrders` 对平台单应 **降级为只读展示** 或 **过滤掉**（通过 JOIN pof 或 assign_mode=PLATFORM 标记），避免配送商误操作填价/称重。

---

## 4. Electric 司机调度页面第一版设计

### 4.1 页面定位

在 `electron-platform` 新增 **第二 Tab「司机调度」**（或顶栏切换），与 Phase 2a「订单分配」并列。

### 4.2 三栏布局（第一版）

| 左栏 | 中栏 | 右栏 |
|------|------|------|
| 待调度批次 / 今日 READY 订单 | 批次内站点顺序 | 司机列表 + 分派操作 |
| 按 marketId + 订货日筛选 | 取货点 + 送货点明细 | 选择司机 → 生成批次 |

**左栏数据源：** `fulfillment_status = READY_FOR_PICKUP` 且尚未绑定活跃 driver_batch 的订单；可按客户分组。

**中栏：** 选中待建批次的订单集合 → 预览路线站点（见 §5）。

**右栏：**

- 司机下拉（market 下 `NxDistributerUserEntity`，type=DRIVER，或平台自有司机表）
- 按钮：**「生成取货路线」** → **「生成送货路线」** → **「确认派单」**
- 第一版：**手动拖拽排序** 站点，不做自动优化

### 4.3 交互约束

- 仅 `READY_FOR_PICKUP` 可进入调度池
- 同一订单行不可重复进入未关闭批次
- 坐标缺失的站点标红，允许加入批次但提示「需补坐标或手动排序」
- 批次创建后司机小程序可读

---

## 5. 司机调度数据模型

### 5.1 表结构建议

#### A. `nx_platform_driver_batch`（司机批次）

| 字段 | 说明 |
|------|------|
| batch_id | PK |
| market_id | 市场 |
| driver_user_id | 司机（nx_distributer_user 或平台司机） |
| batch_date | 调度日 |
| batch_status | DRAFT / DISPATCHED / IN_PROGRESS / COMPLETED / CANCELLED |
| pickup_route_summary | 可选文字摘要 |
| delivery_route_summary | 可选 |
| operator_id | Electric 操作员 |
| dispatched_at | 派单时间 |

#### B. `nx_platform_driver_batch_stop`（路线站点 — 有序）

| 字段 | 说明 |
|------|------|
| stop_id | PK |
| batch_id | FK |
| stop_seq | 顺序号（1..n） |
| stop_type | `PICKUP_SUPPLIER` / `DROP_CUSTOMER` |
| stop_phase | `MARKET_PICKUP` / `CUSTOMER_DELIVERY` — 区分两段路线 |
| distributer_id | 取货点时填 |
| department_id | 送货点时填 |
| lat, lng | 冗余坐标快照 |
| stop_status | PENDING / ARRIVED / COMPLETED / SKIPPED / EXCEPTION |
| arrived_at, completed_at | 司机确认时间 |

#### C. `nx_platform_driver_batch_order`（批次 ↔ 客户订单）

| 字段 | 说明 |
|------|------|
| batch_id + order_id | 一个客户订单在一个批次 |
| department_id | 冗余 |
| delivery_stop_id | 关联 DROP_CUSTOMER 站点 |

#### D. `nx_platform_driver_batch_pickup`（订单 ↔ 配送商取货点）

解决「一个客户订单涉及多个配送商取货点」：

| 字段 | 说明 |
|------|------|
| batch_id | FK |
| order_id | 客户订单行 |
| distributer_id | 取货配送商 |
| dis_goods_id | 配送商商品 |
| pickup_stop_id | 关联 PICKUP_SUPPLIER 站点 |
| fulfillment_id | 关联 nx_platform_order_fulfillment |

**示例：** 客户 A 有 3 行订单，分别分配给配送商 X、Y、X → 批次含 2 个 PICKUP（X 合并）、1 个 PICKUP（Y）、1 个 DROP（A）。

### 5.2 路线生成逻辑（第一版 — 简单）

**市场内部取货路线（MARKET_PICKUP）：**

1. 收集批次内所有 `PICKUP_SUPPLIER` 站点（按 distributer_id 去重）
2. 有坐标：按与 market 中心或首站的 **欧氏/哈弗辛距离贪心** 排序（非 TSP 优化）
3. 无坐标：排末尾，Electric 手动拖拽 `stop_seq`

**客户送货路线（CUSTOMER_DELIVERY）：**

1. 按 `department_id` 去重得到 DROP 站点
2. 同样简单坐标排序 + 手动调整
3. 取货全部 `COMPLETED` 后才允许开始送货阶段（状态机约束）

### 5.3 司机小程序任务流程

**登录：** 复用 `disDriverUserLogin`（`LiziDriverAppID`）或新建平台司机 AppID（Phase 2c 决策）。

**任务列表 API（新建 `api/platform/driver/tasks`）：**

```
GET 我的批次 → batch_status IN (DISPATCHED, IN_PROGRESS)
GET 批次详情 → stops 有序列表 + 每站关联订单摘要
POST 确认到达 → stop_status: PENDING → ARRIVED
POST 确认取货完成 → PICKUP stop → COMPLETED；联动 pof → PICKED_UP
POST 确认送达 → DROP stop → COMPLETED；联动 pof → DELIVERED
POST 异常反馈 → stop + pof exception
```

**司机确认矩阵：**

| 事件 | stop 变化 | fulfillment 变化 |
|------|-----------|-------------------|
| 到达配送商 | ARRIVED | — |
| 已取货 | COMPLETED | PICKED_UP |
| 到达客户 | ARRIVED | DELIVERING |
| 已送达 | COMPLETED | DELIVERED |
| 异常 | EXCEPTION | 可挂起 |

---

## 6. 配送商坐标 / 客户坐标字段确认

### 6.1 字段存在性

| 实体 | 字段 | 类型 | 文件 |
|------|------|------|------|
| `NxDistributerEntity` | `nxDistributerLan` | String | 经度（命名 Lan≈longitude） |
| | `nxDistributerLun` | String | 纬度 |
| | `nxDistributerAddress` | String | 文本地址 |
| `NxDepartmentEntity` | `nxDepartmentLat` | String | 纬度 |
| | `nxDepartmentLng` | String | 经度 |
| | `nxDepartmentAddress` | String | 文本地址 |
| | `nxDepartmentDriverId` | Integer | 旧链绑定司机 |
| | `nxDepartmentDisRouteId` | Integer | 旧链线路 |

另有 GB 侧 `GbRouteEntity` / `GbRouteDepEntity`、`NxDistributerRouteEntity`（仅 name+disId，**无坐标**）— 属旧配送商自有线路，平台调度**不应复用**。

### 6.2 现有使用情况

- `NxDepartmentOrdersPartController.disGetTodayOrderCustomerRoute` 已用 `nxDepartmentLat/Lng`，含 `isValidCoordinate()`（非空、可 parse、中国范围 18–54°N, 73–135°E）
- 配送商坐标 **`nxDistributerLan/Lun` 在路线代码中未见同等校验**，使用度低于客户坐标

### 6.3 数据质量（推断 + 需 Phase 2b 前 SQL 抽样）

| 维度 | 评估 |
|------|------|
| 字段类型 | 均为 **String 可 NULL**，无 DB 级约束 |
| 客户坐标 | 路线功能已依赖 → 部分客户有值，但**一定存在缺失/脏数据** |
| 配送商坐标 | 实体有字段，业务使用少 → **缺失率可能更高** |
| 建议抽样 SQL | `SELECT COUNT(*), SUM(lat IS NULL OR lat='') FROM nx_department`；同理 `nx_distributer` |

### 6.4 Electric 坐标缺失提示（第一版）

- 调度预览站点点位：**绿点=有效坐标，红点=缺失**
- Tooltip：`配送商 {name} 未设置坐标，请在基础资料中补充 nx_distributer_lan/lun`
- 允许继续派单，但默认 **stop_seq 排末尾**，并 banner：`N 个站点无坐标，已禁用到距离排序`
- 第一版不做地图导航、不做路径算法

---

## 7. Phase 2b 最小开发范围

**目标：** 平台单进入配送商端后有正确履约语义；Electric 可看到「待取货」池；**不做快照、不做司机派单**。

| # | 项 | 说明 |
|---|-----|------|
| 1 | DDL | `nx_platform_order_fulfillment` 表 |
| 2 | assign 钩子 | assign 成功时 insert pof，status=ASSIGNED |
| 3 | 配送商 API | `accept` / `startPrepare` / `markReady` / `reportException` |
| 4 | 配送商列表 | 新接口「平台待履约单」或改造现有列表 JOIN pof；旧填价接口对 PLATFORM 单隔离 |
| 5 | PENDING 排除补齐 | 所有 `disId` 过滤的配送商 SQL 统一加 `excludePlatformPendingOrders` |
| 6 | Electric | 订单分配页增加 fulfillment 状态只读列；可选「履约看板」Tab 仅列表 |
| 7 | 文档 | 配送商小程序对接说明、状态机图 |
| 8 | 验收 | assign → 配送商接单 → READY_FOR_PICKUP 全链 |

**不做：** 快照、司机批次、路线优化、运营大屏、客户端改造。

---

## 8. Phase 2c 最小开发范围

**目标：** Electric 手动派司机 + 司机小程序确认取货/送达。

| # | 项 | 说明 |
|---|-----|------|
| 1 | DDL | `nx_platform_driver_batch` + `_stop` + `_order` + `_pickup` |
| 2 | Electric | 司机调度 Tab：READY 池 → 选手动排序 → 派单 |
| 3 | 司机 API | 批次列表、站点确认、异常上报 |
| 4 | 状态联动 | stop 完成 → pof 迁移 PICKED_UP / DELIVERING / DELIVERED |
| 5 | 坐标 | 调度页红绿点 + 缺失提示；简单距离排序 |
| 6 | 验收 | 多配送商取货 + 单客户送货完整演练 |

**不做：** 自动路线优化、复杂地图导航、供应商评分、运营大屏、快照、小程序客户端 UI 大改。

---

## 9. 风险与决策项（实施前需确认）

1. **平台单是否还要走旧称重/填价？** 建议否；价格已在 assign 写入，履约走 pof 新链。
2. **司机身份：** 用 market 级平台司机还是配送商下属司机（`NX_DIS_USER_DRIVER=5`）？
3. **PENDING 池化 disId：** 若生产仍用 NULL，泄漏风险低；若用 pool disId，必须先补齐 SQL 排除。
4. **终态 CONFIRMED vs PAID：** 与客户支付链对接时机 — 建议 Phase 2c 只做 DELIVERED，CONFIRMED/PAID 随结算 Phase 再接。
5. **快照：** 明确在 CONFIRMED/PAID 后触发，与 `upgrade_nx_platform_snapshot_phase2b.sql` 预埋表对齐。

---

## 10. 文档索引

| 文档 | 内容 |
|------|------|
| `Phase2a-Backend-Confirmation.md` | 2a 主链收口 |
| `Phase2a-Electric-Confirmation.md` | 2a 三栏桌面 |
| `upgrade_nx_platform_phase2a.sql` | 2a 四表 |
| `upgrade_nx_platform_snapshot_phase2b.sql` | 快照预埋（**暂不实现**） |
| 本文档 | 2b/2c 设计报告 |

---

*生成方式：只读代码梳理（2026-06-19），未修改 Java/SQL/前端。*
