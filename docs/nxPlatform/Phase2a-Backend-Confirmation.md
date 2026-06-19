# Phase 2a 平台订单分配 — 收口确认

> **状态：主链已跑通，Phase 2a 收口完成。**  
> 基准验收：orderId=200171 ASSIGNED；default 1436+100470→160/31235。

## 主链

```
submitLine → PENDING → pending / detail / suppliers → assign → ASSIGNED
                                                              ↓
                                    switch_log（必写）+ default（ORDER_AND_DEFAULT 可选）+ price
```

**入口：** 小程序/其他渠道 `submitLine` 或测试 SQL  
**操作端：** `electron-platform` 三栏桌面（见 [Phase2a-Electric-Confirmation.md](./Phase2a-Electric-Confirmation.md)）

---

## 表主权

| 表 | 职责 |
|----|------|
| `nx_department_orders` | 订单主表；PENDING 时 `nx_DO_distributer_id=NULL`；ASSIGNED 后写入配送商/商品/价格 |
| `nx_platform_order_assign` | 平台分配状态（PENDING / ASSIGNED）、assign_mode=PLATFORM |
| `nx_department_nx_goods_default` | 客户+标准商品默认配送商（仅 ORDER_AND_DEFAULT upsert） |
| `nx_supplier_switch_log` | 每次 assign 写切换日志（ORDER_ONLY / ORDER_AND_DEFAULT 均写） |

**SQL 补丁（严格顺序）：**

1. `docs/sql/patches/check_nx_department_orders_distributer_id.sql`
2. `docs/sql/patches/upgrade_nx_platform_phase2a.sql`
3. `docs/sql/patches/seed_nx_platform_phase2a_test.sql`（可选测试数据）

---

## 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/platform/orders/submitLine` | 提交一行 → PENDING |
| POST | `/api/platform/orders/pending` | 按客户聚合待分配 |
| POST | `/api/platform/orders/detail` | 客户订单明细 + defaultRecommend |
| POST | `/api/platform/goods/suppliers` | 市场内候选配送商（过滤无效价） |
| POST | `/api/platform/orders/assign` | PENDING → ASSIGNED |
| GET | `/api/platform/orders/schema/distributerIdNullable` | 探测 distributer_id 是否可 NULL |

### assign 参数

- `switchScope`：`ORDER_ONLY` | `ORDER_AND_DEFAULT`
- `disGoodsId`：目标配送商商品
- `operatorId`：操作员

---

## 价格规则

- **有效销售价**：`nx_dg_will_price_one` / `nx_dg_will_price`，须 **严格 > 0.1**
- 0、空、0.1 均为无效占位价；**不使用进价**
- 工具类：`SalesPriceUtils`；校验：`PlatformDisGoodsValidator`
- assign 定价：`applyPlatformAssignPricing`（历史有效价 > 当前报价兜底）

---

## 后端验收清单

### 1. pending 不含已分配单

`WHERE nx_poa_assign_status = 'PENDING'` — orderId=200171 **不出现**。

### 2. 新 PENDING detail 带 defaultRecommend

LEFT JOIN `nx_department_nx_goods_default`（ACTIVE）→ `defaultDistributerId` / `defaultDisGoodsId` / `source`。

### 3. suppliers 默认标记

`isDefaultRecommend=1` 当且仅当 `defaultDisGoodsId == disGoodsId`。

### 4. ORDER_ONLY 不更新 default ✅

**代码：** `PlatformOrderAssignServiceImpl.upsertDefaultAndSwitchLog`  
仅当 `switchScope=ORDER_AND_DEFAULT` 时调用 `upsertDefaultRelation`。

**验收 SQL（临时分配本单前后对比）：**

```sql
SELECT nx_dngd_id, nx_dngd_department_id, nx_dngd_nx_goods_id,
       nx_dngd_default_distributer_id, nx_dngd_default_dis_goods_id,
       nx_dngd_last_order_id, nx_dngd_last_switch_log_id
FROM nx_department_nx_goods_default
WHERE nx_dngd_market_id = 1 AND nx_dngd_department_id = 1436;
-- ORDER_ONLY assign 后上述行应无变化（last_order_id / last_switch_log_id 不变）
```

### 5. ORDER_AND_DEFAULT 更新 default ✅

upsert default，`source=AUTO_SWITCH`，更新 `lastOrderId` / `lastSwitchLogId`（200171 已验）。

### 6. 0.1 无效价不进 suppliers ✅

SQL `> 0.1` + Java `SalesPriceUtils.isValidSalesPrice`。

### 7. assign 拒绝无效价 ✅

分配前 `hasValidSalesQuote`；分配后 `nxDoPrice > 0.1`；失败事务回滚。

---

## 与旧链隔离（禁止项）

| 禁止 | 原因 |
|------|------|
| 平台 assign 调用 `saveCollaborativeOrderWhenNeeded` | 不进入旧协作链 |
| 平台 assign 调用 `savePurGoodsAuto` | 不自动采购 |
| 快照（Phase 2b） | 本阶段未做 |
| 小程序平台分配 UI | 本阶段未做 |
| 司机调度 | 本阶段未做 |
| 供应商评分 / 自动推荐 | 本阶段未做 |
| 运营大屏 | 本阶段未做 |
| 已分配订单改派 | 前后端均不支持 |

**PENDING 排除（有限覆盖）：** 旧配送商查询 3 处加 `NOT EXISTS nx_platform_order_assign PENDING`，非全库改造。

---

## 已验收数据样例（200171）

```
orderId=200171, assignStatus=ASSIGNED
nxDoDistributerId=160, nxDoDisGoodsId=31235
nxDoPrice=1.5, nxDoSubtotal=15.0, nxDoCollaborativeNxDisId=-1
switchScope=ORDER_AND_DEFAULT → defaultId=1, switchLogId=2
```

---

## 文档索引

- [Phase2a-Round1-验收指南.md](./Phase2a-Round1-验收指南.md) — submitLine、表结构
- [Phase2a-Round2-验收指南.md](./Phase2a-Round2-验收指南.md) — pending/detail/suppliers/assign
- [Phase2a-Electric-Confirmation.md](./Phase2a-Electric-Confirmation.md) — Electron 三栏与交互
