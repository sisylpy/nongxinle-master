# Phase 2a 第二轮验收指南（pending / detail / suppliers / assign）

> **范围：** 仅后端 4 个接口，不做 Electric / 快照 / 小程序。  
> **前置：** 第一轮 submitLine 已通过（如 orderId=200169）。

---

## 接口一览

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/platform/orders/pending` | 按 market 聚合 PENDING 客户列表 |
| POST | `/api/platform/orders/detail` | 客户某日平台订单明细 |
| POST | `/api/platform/goods/suppliers` | 市场内某标准商品可选配送商 |
| POST | `/api/platform/orders/assign` | PENDING → ASSIGNED |

---

## 1. pending

**POST** `/api/platform/orders/pending`

```json
{
  "marketId": 1,
  "applyDate": "2025-06-19"
}
```

`applyDate` 可省略，默认当天。

期望：`customers` 中含 `departmentId=1436`，`orderIds` 含 `200169`；仅 `PENDING + PLATFORM`，不混入旧配送商订单。

---

## 2. detail

**POST** `/api/platform/orders/detail`

```json
{
  "marketId": 1,
  "departmentId": 1436,
  "applyDate": "2025-06-19",
  "orderIds": [200169]
}
```

期望：`lines[0]` 含圆白菜、`assignStatus=PENDING`（assign 前）；`defaultRecommend` 无记录时为 null；`distributerSummary` 为空数组。

---

## 3. suppliers

**POST** `/api/platform/goods/suppliers`

```json
{
  "marketId": 1,
  "departmentId": 1436,
  "nxGoodsId": 10101
}
```

期望：返回 marketId=1 下卖 `nxGoodsId=10101` 的配送商商品列表。

- `currentQuotePrice`：销售报价（`nx_dg_will_price_one` / `nx_dg_will_price`）
- `customerHistoryPrice`：客户历史价（`nx_department_dis_goods.nx_DDG_order_price`），无则 null
- 已过滤：非本市场、下架、隐藏、临时（无 nxGoodsId / 无父分类）、无有效销售报价
- **有效销售价**：仅 `nx_dg_will_price_one` / `nx_dg_will_price`，须 **严格 &gt; 0.1**（空、0、0.1 均为无效占位价，不使用进价）

---

## 4. assign

**POST** `/api/platform/orders/assign`

```json
{
  "marketId": 1,
  "orderId": 200169,
  "disGoodsId": 12345,
  "switchScope": "ORDER_AND_DEFAULT",
  "reasonCode": "OTHER",
  "reasonNote": "平台手动分配",
  "operatorId": 1
}
```

`switchScope`：`ORDER_ONLY`（仅本单 + 写切换日志）或 `ORDER_AND_DEFAULT`（ additionally upsert default）。

期望：

| 检查项 | assign 后 |
|--------|-----------|
| `nx_platform_order_assign.assign_status` | `ASSIGNED` |
| `nx_department_orders.nx_DO_distributer_id` | 目标配送商 |
| `nx_department_orders.nx_DO_dis_goods_id` | 目标 disGoods |
| `nx_DO_collaborative_nx_dis_id` | `-1` |
| `ORDER_AND_DEFAULT` | `nx_department_nx_goods_default` 写入 1+1436+10101 → 目标配送商 |
| 协作链 | **未**调用 `saveCollaborativeOrderWhenNeeded` |

定价：事务内 `processOrderPrice` + `applyDepartmentGoodsPriceIfFound`（与旧链相同逻辑，不触发协作保存）。

---

## 第二轮验收 SQL

```sql
-- assign 后
SELECT * FROM nx_platform_order_assign WHERE nx_poa_order_id = 200169;
SELECT nx_DO_distributer_id, nx_DO_dis_goods_id, nx_DO_collaborative_nx_dis_id, nx_DO_price
FROM nx_department_orders WHERE nx_department_orders_id = 200169;

-- ORDER_AND_DEFAULT 后
SELECT * FROM nx_department_nx_goods_default
WHERE nx_dngd_market_id = 1 AND nx_dngd_department_id = 1436 AND nx_dngd_nx_goods_id = 10101;

SELECT * FROM nx_supplier_switch_log WHERE nx_ssl_order_id = 200169 ORDER BY nx_ssl_id DESC LIMIT 1;
```

---

## 已知限制（延续第一轮）

旧配送商端 PENDING 排除仍为 **有限覆盖**（3 个高频查询 + 须带 disId），非全量入口隔离。
