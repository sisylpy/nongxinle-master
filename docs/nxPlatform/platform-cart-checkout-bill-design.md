# 京采市场 · 购物车 / Checkout / Bill / 平台订单 — 业务主权与重构设计

> **文档性质**：目标业务模型与重构规格（**不是**当前代码实现说明）。  
> **维护位置**：农新乐 Java 后台 `docs/nxPlatform/`（**业务主权文档，以后端为准**）。  
> **读者**：产品、后端、小程序前端、Electron 平台后台。  
> **目的**：统一「购物车临时行 / checkout / bill / 付款 / assign / fulfillment」边界，避免继续把 `submitBySupplier`、bill 生成、配送商履约混为一条链。  
> **关联文档**（实现细节 / 联调参数见各文档，**业务主权以本文件为准**）：  
> - 饭店端接口联调（前端小程序）：`jingcaiMarket/docs/平台订货购物车接口说明.md`（2026-06-21 第一轮联调版）  
> - 配送商端：`jcJieDan/docs/平台客户订单接口说明.md`  
> - 平台分配 Phase 2：`docs/nxPlatform/Phase2a-Backend-Confirmation.md` 等  

**当前阶段明确不做**：真实微信支付、微信回调、司机路线、配送商/司机结算、跨批发商复杂结算、Electric 对购物车 `-1` 行的主调度。

---

## 1. 业务主权说明

### 1.1 核心原则

| 概念 | 主权归属 | 一句话 |
|------|----------|--------|
| **购物车** | 饭店端、`-1` 临时 order | 用户意向，未承诺履约 |
| **Checkout / 付款确认** | 饭店端一次批次操作 | 确认本批购物车 + **支付已知价合计** + **生成一张正式 bill** |
| **Bill** | `gb_department_bill` | 饭店视角的一张采购单；checkout 后才存在 |
| **正式 order** | `status ≥ 0` 且已挂 bill | 配送商/Electric 可见性的起点 |
| **价格确认** | `priceConfirmStatus`（行级） | 价格是否已知、能否算 `subtotal`；**与配送商无关** |
| **配送商分配** | `assignStatus`（`nx_platform_order_assign`） | 配送商是否已确定；**Checkout 后**才创建 |
| **Fulfillment** | `nx_platform_order_fulfillment` | **Checkout 后**且仅 `assignStatus = ASSIGNED` 行创建 |
| **补款** | Bill 生命周期后续 | 同一张 bill 内 checkout 时已存在的 PENDING 行称重/改价后，`supplementDue > 0` 的差额支付（**不是**往旧 bill 追加新商品） |

**「付款」在本模型中的含义**：

- **不是**所有商品最终金额都已结清；
- **是**饭店确认本批购物车，**支付其中已知价格商品的合计金额**，并**新生成一张**正式 `gb_department_bill`；
- 未知价格商品**不计入本次支付**，但**必须进入同一张 bill**，后续称重/改价可能产生**补款**（见 §1.5）。

### 1.2 购物车临时阶段（Checkout 之前）

饭店用户通过 `addCartLine*` 写入购物车时，**只产生临时 order 行**（`status = -1`），**不是正式订单**。

无论是否已选择配送商，只要**未** checkout / 付款确认：

| 规则 | 要求 |
|------|------|
| Order 性质 | 购物车临时行，**不是**正式订单 |
| `nx_do_status` / GB 等价状态 | **`-1`**（`NX_ORDER_STATUS_GOUWU`） |
| `gb_do_bill_id` | **`null`** |
| 正式 bill | **不生成** |
| 配送商端 | **不可见** |
| 司机端 | **不参与** |
| 配送商结算 | **不参与** |
| Electric 主调度 | **不关注** |
| Assign / Fulfillment | **不创建**（或创建了也**不得**用于可见性/履约） |

`status = -1` 的 order 只是用户购物车意向，**在 checkout 之前不得视为正式订单**。

### 1.3 三种购物车写入来源（addCartLine）

#### 来源 A：用户不知道配送商

| 项 | 说明 |
|----|------|
| 字段 | 无 `nxDistributerId`、无 `nxDistributerGoodsId`（仅有 `nxGoodsId` 等平台商品） |
| 购物车临时阶段 | 仅写入 `status = -1` 临时行；**不挂 bill** |
| Checkout 后 | 挂**新** bill；`assignStatus = PENDING`；由 Electric / 平台后台分配配送商 |

#### 来源 B：用户自己选择了配送商

| 项 | 说明 |
|----|------|
| 字段 | 有 `nxDistributerId`、`nxDistributerGoodsId` |
| 本质 | **仍是**购物车临时行，不是正式履约单 |
| 付款前 | 配送商**不可见** |
| Checkout 后 | 挂**新** bill；`assignStatus = ASSIGNED`；`assignSource = CUSTOMER_SELECTED_SUPPLIER` |

#### 来源 C：商品有默认配送商

| 项 | 说明 |
|----|------|
| 触发 | 搜索/选品时发现 `nx_department_nx_goods_default` 等默认配送商 |
| UX | **必须提示**：「该商品原来指定由【XX 配送商】供货，是否继续使用？」 |
| 用户选继续默认 | 写入购物车时带配送商字段；Checkout 后 `assignStatus = ASSIGNED` |
| 用户选平台重新指派 | 写入购物车时不带配送商；Checkout 后 `assignStatus = PENDING` |
| 原则 | 默认配送商只是**建议**，**不强制** |

### 1.4 行级两条独立维度（禁止混用「PENDING」）

文档与代码中 **「PENDING」必须拆成两条正交维度**，不得用单一 PENDING 同时表达「价未确认」与「配送商未分配」：

| 维度 | 字段（建议） | 存储位置 | 含义 |
|------|--------------|----------|------|
| **价格是否已知** | `priceConfirmStatus` | `CONFIRMED` / `PENDING`；`gb_do_price_confirm_status` | 能否计算 `lineSubtotal`、是否计入 checkout 的 `knownTotal` / 本次支付 |
| **配送商是否已确定** | `assignStatus` | `ASSIGNED` / `PENDING`；`nx_platform_order_assign.assign_status` | 行是否已有履约配送商；决定配送商可见性与 Electric **分配台**是否介入 |

**Checkout 后，是否进入 Electric 分配，不由 `priceConfirmStatus` 决定，而由购物车写入时是否已有配送商（即 checkout 后 `assignStatus`）决定：**

| 购物车写入时是否有配送商 | Checkout 后 `assignStatus` | Electric 分配台 | 配送商端 |
|--------------------|----------------------------|-----------------|----------|
| **有**（`nxDistributerId` 等） | **ASSIGNED** | **不进入**待分配队列 | **可见**；即使 `priceConfirmStatus = PENDING`，也由**该配送商**称重/改价 |
| **无** | **PENDING** | **进入**待分配队列 | **不可见**，直至平台 assign → ASSIGNED |

**四种合法组合（Checkout 后）：**

| `priceConfirmStatus` | `assignStatus` | 典型场景 | 后续主路径 |
|----------------------|----------------|----------|------------|
| CONFIRMED | ASSIGNED | 已知价 + 用户选了配送商 | 配送商直接出库 |
| CONFIRMED | PENDING | 已知价 + 未选配送商 | Electric 分配配送商 |
| **PENDING** | **ASSIGNED** | 需称重 + 用户选了配送商 | **该配送商称重/改价**（不进 Electric 分配） |
| **PENDING** | **PENDING** | 需称重 + 未选配送商 | Electric 分配配送商 → 再称重/改价 |

> **禁止**：用「价格 PENDING 的行都要进 Electric」或「assign PENDING 等于价格未确认」等混语。Bill 上的 `pendingItemCount` **仅统计** `priceConfirmStatus = PENDING` 的行数，与 `assignStatus` 无关。

### 1.5 Bill 固定性：一张 bill 一批货；补款 ≠ 追加商品

**核心规则（文档与代码均不得违反）：**

1. **付款前**：`status = -1` 行只是**购物车临时行**，不是正式订单；**不存在**正式 `gb_department_bill`。
2. **checkout / 付款确认**：对本批选中的购物车临时行，**新生成一张** `gb_department_bill`，并将本批行挂到该 bill。
3. **bill 一旦生成，商品集合固定**：本 bill 只包含 checkout 时选中的那些行；**不允许**后续再把新买的商品追加进这张 bill。
4. **付款后又买新东西**：新商品进入**新的**购物车临时行 → 必须**重新 checkout** → **重新付款** → **生成新的 bill**。
5. **允许的是「补款」，不是「往旧 bill 加新行」**：文档与 API **禁止使用「补单」** 指代追加商品；`paySupplement` 仅支付**同一张 bill 内**、checkout 时已存在且曾为 `priceConfirmStatus = PENDING` 的行，在称重/改价后产生的 `supplementDue` 差额。
6. **`addCartLine*` 语义**：只写 `status = -1`、`gb_do_bill_id = null` 的临时行；**不得**接受已有 `billId` 或表现得像「追加到已生成 bill」。

| 概念 | 正确含义 | 错误含义（禁止） |
|------|----------|------------------|
| **补款** | 同 bill 内已知行称重后 `supplementDue` 的差额支付 | 把新买商品挂到旧 bill |
| **新 checkout** | 新购物车批次 → 新 bill | 在旧 bill 上继续加行 |
| **购物车临时行** | `status = -1`，未承诺履约 | 正式订单 |

---

## 2. Checkout / 付款确认语义

### 2.1 行级价格分类（`priceConfirmStatus`）

沿用现有 **`CONFIRMED` / `PENDING`**（`PlatformLineAmountConfirmService` / `gb_do_price_confirm_status`），**不新增枚举**。本节 **仅描述价格维度**，不涉及配送商分配。

#### 已知价格 — `priceConfirmStatus = CONFIRMED`

- 有有效价格（空、0、0.1 等视为无效）；
- 订货数量与计价单位可可靠换算；
- 可计算 `lineSubtotal`；
- 计入 **`knownTotal`**；
- 计入 **本次支付金额**。

#### 未知价格 — `priceConfirmStatus = PENDING`

- 价格无效，或单位无法换算，或需配送商称重；
- **不计入**本次支付金额；
- Checkout **成功后仍进入同一张 bill**；
- 在 bill 内标记为待确认价 / 后续补款来源；
- **不继续留在购物车**；
- **与 `assignStatus` 无关**：价未确认 **不意味着** 必须进 Electric 分配（见 §1.4）。

### 2.2 Checkout 成功后必须发生的事

对**本批选中**的全部购物车 order（`priceConfirmStatus` 为 CONFIRMED 或 PENDING 均可）：

1. 创建 **一张** `gb_department_bill`（`bill_source = PLATFORM_CASH`）；
2. **全部**选中行写入 `gb_do_bill_id`；
3. 全部 order **`status: -1 → 正式`**（建议 NX `0`，GB 同步）；
4. 从购物车列表移除（查询不再包含 `-1`）；
5. 逐行写入 `priceConfirmStatus`（CONFIRMED / PENDING）；
6. **有配送商**（购物车写入时已有 `nxDistributerId` + `nxDistributerGoodsId`）→ `assignStatus = ASSIGNED` + `CUSTOMER_SELECTED_SUPPLIER` + fulfillment（**即使** `priceConfirmStatus = PENDING`）；
7. **无配送商** → `assignStatus = PENDING`（不进配送商端；**进入 Electric 分配台**）；
8. 设置 bill 金额字段（见 §3）并 **recalc**；
9. **`paidTotal` 初始 = `knownTotal`**（本次实际收到的已知价合计；mock 阶段可等同）。

### 2.3 重要边界（必须遵守）

1. `status = -1` 的 order **不给配送商看**；
2. 一旦 checkout 成功，**整批**选中 order 正式化（含 `priceConfirmStatus = PENDING` 行）；
3. 未知价格商品 **不留在购物车**，而是 **同 bill 内待确认价**；
4. 未知价行的 `priceConfirmStatus = PENDING` = 待确认 / 补款来源（**不是**购物车项，**也不是** assign 待分配的同义词）；
5. **本次支付金额 = 已知价合计**，≠ `finalTotal`；
6. **`submitBySupplier` / `addCartLine*` 不得生成 bill**；生成 bill 的唯一入口是 **checkoutConfirm**（每次 checkout **新建一张** bill）；
7. `priceConfirmStatus` 判断 **保留**，用于购物车预览与 checkout 分组（§7.3）；
8. **`priceConfirmStatus` 与 `assignStatus` 必须分字段表达**；API / SQL / 文档不得混称「PENDING 行」而不注明维度（§1.4）；
9. **禁止向已生成 bill 追加新商品**；新购商品只能进入新的购物车临时行并重新 checkout（§1.5）。

---

## 3. Bill 金额字段语义

Checkout 生成 bill 后（`gb_department_bill` 平台现金字段）：

| 字段 | 语义 |
|------|------|
| **`knownTotal`** | 已知价格（CONFIRMED）商品 `subtotal` 合计；checkout 时写入，recalc **不重算** |
| **`paidTotal`** | 累计已支付金额；**checkout 成功初始 = `knownTotal`** |
| **`pendingItemCount`** | `priceConfirmStatus = PENDING` 的**行数**（与 `assignStatus` 无关） |
| **`finalTotal`**（`gb_db_total`） | 当前所有**已确认价格**行的 `subtotal` 合计；checkout 初值通常 = `knownTotal`；称重后增大 |
| **`supplementDue`** | `max(finalTotal - paidTotal, 0)`；checkout 初值 **0**；称重/改价后 recalc |
| **`payStatus`** | 由 `paidTotal`、`pendingItemCount`、`finalTotal`、`supplementDue` 推导 |

### 3.1 Checkout 成功后的 `payStatus` 建议

| 场景 | 建议状态 |
|------|----------|
| 全部为 CONFIRMED，且 `paidTotal = knownTotal = finalTotal` | **`PAID`** |
| `priceConfirmStatus` 混合（CONFIRMED + PENDING），且 `paidTotal = knownTotal` | **`PARTIAL_PAID`**（已付已知部分，仍有待确认价行） |
| 全部 `priceConfirmStatus = PENDING`，`knownTotal = 0` | **`NONE`**（bill 已建但无已知首付；是否允许纯 unknown checkout 由产品确认） |

**`AWAIT_FIRST_PAY`**：仅适用于 **bill 已创建但 known 部分尚未支付** 的中间态；**不应**成为 checkout 成功后的长期状态。

### 3.2 称重 / 改价之后（补款，不是追加商品）

1. 仅针对 **checkout 时已挂在本 bill 上** 的行：`priceConfirmStatus: PENDING → CONFIRMED`，更新行价与小计；
2. `recalcBillPaymentState`：`finalTotal` 上升；
3. 若 `finalTotal > paidTotal` → `supplementDue > 0` → **`AWAIT_SUPPLEMENT`**（待**补款**）；
4. 用户通过 `paySupplement` **补款**后 `paidTotal` 增加，recalc → **`PAID`**。

> **禁止**：把称重后新增的商品行挂到旧 bill，或把 `paySupplement` 描述成「补单 / 追加采购」。

### 3.3 阻断下一批 Checkout

沿用 **`AWAIT_FIRST_PAY`** / **`AWAIT_SUPPLEMENT`** 且有余额时阻断（`PlatformOutstandingBillService`），阻断点放在 **checkoutConfirm 前**（阻止在未结清旧 bill 时再 **checkout 生成新 bill**），**不是**写入购物车临时行之前。

### 3.4 Bill 商品集合不可变

| 时机 | 规则 |
|------|------|
| checkoutConfirm 成功 | 本批选中购物车临时行 **全部**挂到**这一张新 bill**；bill 行集合 **封口** |
| checkout 之后 | **不得**再向该 `gb_department_bill_id` 插入新的 order 行 |
| 用户再次采购 | 新 `status = -1` 临时行 → 新 checkout → **新 bill** |
| 价未确认行后续称重 | 仍在**同 bill** 内改价 → 可能 **补款**（`supplementDue`） |

---

## 4. 目标状态机

```text
                    ┌─────────────────────────────────────┐
                    │  addCartLine*（购物车临时阶段）       │
                    │  status=-1, bill_id=null             │
                    │  无 assign / fulfillment             │
                    └──────────────┬──────────────────────┘
                                   │
                    ┌──────────────▼──────────────────────┐
                    │  购物车列表 listCartLines              │
                    │  仅 status=-1                        │
                    └──────────────┬──────────────────────┘
                                   │
                    ┌──────────────▼──────────────────────┐
                    │  checkoutPreview                     │
                    │  按 priceConfirmStatus 分组          │
                    │  payAmount = knownTotal              │
                    └──────────────┬──────────────────────┘
                                   │
                    ┌──────────────▼──────────────────────┐
                    │  checkoutConfirm                     │
                    │  **新建** bill, 挂全批行, -1→正式    │
                    │  paidTotal=knownTotal                │
                    │  有配送商→assign ASSIGNED            │
                    │  无配送商→assign PENDING             │
                    └──────────────┬──────────────────────┘
                                   │
              ┌────────────────────┼────────────────────┐
              │                    │                    │
    ┌─────────▼─────────┐ ┌────────▼────────┐ ┌─────────▼─────────┐
    │ 配送商出库/称重    │ │ Electric assign  │ │ 补款 paySupplement │
    │ 同 bill 内 PENDING │ │ assign PENDING→  │ │ supplementDue    │
    │ 行改价（非加新行） │ │ ASSIGNED         │ │ （非新 bill）      │
    └─────────┬─────────┘ └──────────────────┘ └─────────┬─────────┘
              │                                          │
              └──────────────────┬───────────────────────┘
                                 │
                    ┌────────────▼────────────┐
                    │  recalc → AWAIT_SUPPLEMENT / PAID │
                    └─────────────────────────┘
```

---

## 5. 当前代码偏差（实现 vs 目标）

> 以下描述 **截至重构前** 的 Java 实现，用于指导改造，**不是**目标行为。

| 区域 | 当前行为 | 目标行为 |
|------|----------|----------|
| **`submitBySupplier`** | 一次调用：建 bill + 建行 + assign ASSIGNED + fulfillment + recalc + 返回支付态 | **仅写购物车临时行**（来源 B），`status=-1` |
| **`submitLine`** | 建 NX 行 `status=0`，**立即** `assignStatus = PENDING` | **仅写购物车临时行**（来源 A），`status=-1`，checkout 前无 assign |
| **Bill 创建时机** | `PlatformCartSubmitServiceImpl.createPlatformCashBill` 在 submit 内 | 仅在 **checkoutConfirm** |
| **幂等 submitToken** | 绑在 bill 上，submit 时生效 | 绑 **checkout** 批次 |
| **4001 阻断** | submit 前 | checkoutConfirm 前 |
| **配送商可见** | ASSIGNED 行 submit 后可见 | checkout 后且 **`assignStatus = ASSIGNED`** 才可见 |
| **列表 API** | `listTodayLines` 混查 assign 与购物车语义 | 拆 **listCartLines** vs **listFormalOrders/Bills** |
| **price / assign 混语** | 文档与接口常混称「PENDING 行」 | 分 `priceConfirmStatus` / `assignStatus`（§1.4） |
| **默认配送商** | SQL 只读展示 `defaultDistributerId` | 加 **提示 + 用户选择** 流程（来源 C） |
| **checkoutPreview** | **不存在** | 必须新增 |

---

## 6. `submitBySupplier` / `submitLine` 新定位

### 6.1 命名与职责（目标）

| 现名 | 目标名（建议） | 新定位 |
|------|----------------|--------|
| `POST .../cart/submitBySupplier` | `POST .../cart/lines/withSupplier` 或保留路径改语义 | **`addCartLineWithSupplier`** |
| `POST .../orders/submitLine` | `POST .../cart/lines/withoutSupplier` | **`addCartLineWithoutSupplier`** |

### 6.2 `addCartLineWithSupplier`（原 submitBySupplier）必须做

- 创建 GB + NX（或产品定的 B 方案）购物车行，`status = -1`；
- 保存 `nxDistributerId`、`nxDistributerGoodsId`；
- 调用 `PlatformLineAmountConfirmService` 得到行级 `priceConfirmStatus`（**预览用**）；
- 返回行 id、价格分类、参考价等。

### 6.3 `addCartLineWithSupplier` 禁止做

- ❌ 创建 `gb_department_bill`  
- ❌ 写 `gb_do_bill_id`  
- ❌ `knownTotal` / `paidTotal` / recalc  
- ❌ `GbPlatformOrderBridgeService` / assign / fulfillment  
- ❌ 让配送商查询可见  
- ❌ `saveDepDisGoodsAndPurchase` 等**正式履约侧**副作用（延后到 checkoutConfirm 正式化，**不在购物车临时阶段**）
- ❌ 向已有 bill 追加商品（§1.5）

### 6.4 `addCartLineWithoutSupplier`（原 submitLine）必须做

- 创建购物车**临时**行，`status = -1`；
- 仅有 `nxGoodsId` 等平台商品信息，无配送商 SKU。

### 6.5 `addCartLineWithoutSupplier` 禁止做

- ❌ 立即写 `assignStatus = PENDING`  
- ❌ `status = 0`  
- ❌ fulfillment  
- ❌ bill  
- ❌ 挂 `gb_do_bill_id` 或关联已有 bill（§1.5）

---

## 7. 目标接口 / 服务四类

### 7.1 写入购物车（购物车临时阶段）

| 接口 | 职责 |
|------|------|
| `addCartLineWithSupplier` | 来源 B；**仅** `-1` 临时行 |
| `addCartLineWithoutSupplier` | 来源 A；**仅** `-1` 临时行 |
| （可选）`resolveDefaultSupplierPrompt` | 来源 C：返回默认配送商信息，**不写库** |

> `addCartLine*` **不是** checkout，**不是**付款，**不会**创建或追加 bill。

### 7.2 购物车列表 — `listCartLines`

- 查询条件：`gbDepartmentId` + `nx_do_status = -1`（及平台/market 过滤）；
- 返回分组或字段：`priceConfirmStatus`（CONFIRMED/PENDING）；
- **不与**正式订单 / bill 列表混用。

### 7.3 Checkout 预览 — `checkoutPreview`

**入参**：`gbDepartmentId`、`marketId`、`orderIds[]`（均为 `-1`）

**出参**：

| 字段 | 说明 |
|------|------|
| `confirmedLines[]` | `priceConfirmStatus = CONFIRMED` 的行 |
| `pendingPriceLines[]` | `priceConfirmStatus = PENDING` 的行（**不是** assign 待分配） |
| `confirmedItemCount` | 已知价行数 |
| `pendingPriceItemCount` | 未知价行数（对应 bill.`pendingItemCount`） |
| `knownTotal` | 已知金额合计 |
| `payAmount` | **本次应付 = knownTotal** |
| `notice` | 「部分商品需称重确认，后续可能产生补款」 |

### 7.4 Checkout 确认 — `checkoutConfirm`

**入参**：`gbDepartmentId`、`marketId`、`orderIds[]`、`checkoutToken`（幂等）

**职责**：

1. 校验：全部 `status=-1`、同一饭店、无 blocking bill（4001）；
2. 重算每行 `priceConfirmStatus`；
3. 创建 **一张新的** `PLATFORM_CASH` bill（§1.5；**不是**向旧 bill 追加）；
4. 全部选中行挂 `gb_do_bill_id`；
5. `status -1 → 正式`；
6. `paidTotal = knownTotal`（mock 支付可同事务完成）；
7. 按 §1.4：有配送商 → `assignStatus = ASSIGNED` + bridge + fulfillment；无配送商 → `assignStatus = PENDING`（**与 price 是否 PENDING 无关**）；
8. `GbBillPaymentRecalcService.recalcBillPaymentState`；
9. 返回 bill 摘要 + 行列表（含 `priceConfirmStatus`、`assignStatus`）+ `payStatus`。

**路径建议**：`POST api/platform/customer/cart/checkout/preview`、`POST api/platform/customer/cart/checkout/confirm`

---

## 8. 配送商可见性规则

**可见性只看 `assignStatus`，不看 `priceConfirmStatus`。**

| 阶段 | `assignStatus` | `priceConfirmStatus` | 配送商是否可见 |
|------|----------------|----------------------|----------------|
| 购物车 | （未创建 assign） | 任意 | **否** |
| Checkout 后 | **ASSIGNED** | CONFIRMED | **是** |
| Checkout 后 | **ASSIGNED** | **PENDING** | **是**（该配送商负责称重/改价） |
| Checkout 后 | **PENDING** | 任意 | **否**（等 Electric assign 后） |
| 平台 assign 后 | ASSIGNED | 任意 | **是** |

配送商查询必须过滤：

- `nx_do_status >= 0`（或非 -1）；
- `assignStatus = ASSIGNED`（`nx_platform_order_assign.assign_status`）；
- 已挂 `gb_do_bill_id`（可选加强）。

---

## 9. Electric 关注边界

**Electric 分配台只看 `assignStatus = PENDING`，不看 `priceConfirmStatus`。**

| 对象 | Electric 是否主关注 | 说明 |
|------|---------------------|------|
| 购物车 `-1` 行 | **否** | 尚未 checkout |
| Checkout 后 `assignStatus = PENDING` | **是** | 分配台 pending 列表（**无论**价格是否已 CONFIRMED） |
| Checkout 后 `assignStatus = ASSIGNED` | **否**（分配台） | 不进待分配队列；Phase 2b 可只读履约/出库状态 |
| `priceConfirmStatus = PENDING` 且 `assignStatus = ASSIGNED` | **否**（分配台） | 价未确认由已指定配送商处理，**不**因价 PENDING 进入 Electric |
| 未 checkout 的任何 assign 记录 | **不应存在** | 若历史脏数据，查询应排除 |

---

## 10. 现有类：保留 / 迁移 / 删除职责

### 10.1 建议保留（调整调用时机）

| 类 | 重构后用途 |
|----|------------|
| `PlatformLineAmountConfirmService` | 购物车临时行预览 + checkoutPreview + checkoutConfirm |
| `PlatformGbNxOrderLineServiceImpl` | 拆成「购物车临时建行」与「checkoutConfirm 挂 bill / 转正」两段 |
| `GbBillPaymentRecalcServiceImpl` | checkoutConfirm 后 + 称重/改价后 |
| `GbBillPlatformConstants` | pay_status / price_confirm 枚举 |
| `PlatformOutstandingBillService` | checkoutConfirm 前阻断 |
| `GbBillCreationGuardService` | checkout 后 legacy 合单防重复 |
| `GbPlatformOrderBridgeServiceImpl` | **仅 checkoutConfirm**（ASSIGNED 行） |
| `PlatformOrderFulfillmentServiceImpl` | **仅 checkoutConfirm 后 ASSIGNED** |
| `PlatformBillPaymentServiceImpl` | **补款** `paySupplement`（同 bill 内 supplementDue；**非**追加商品） |
| `PlatformDistributerOrderServiceImpl` | 仅正式 ASSIGNED 单 |

### 10.2 逻辑迁移清单

| 从 | 到 |
|----|-----|
| `PlatformCartSubmitServiceImpl.createPlatformCashBill` | `PlatformCartCheckoutService.confirm` |
| `PlatformCartSubmitServiceImpl` 汇总 knownTotal / recalc | `checkoutConfirm` |
| `PlatformCartSubmitServiceImpl` submitToken 幂等 | `checkoutConfirm` |
| `PlatformCartSubmitServiceImpl.assertNotBlocked` | `checkoutConfirm` |
| `PlatformCustomerSubmitLineServiceImpl` 内 assign save | `checkoutConfirm`（`assignStatus = PENDING` 分支） |
| `PlatformGbNxOrderLineServiceImpl` bridge 调用 | `checkoutConfirm` |
| `PlatformGbNxOrderLineServiceImpl` `gb_do_bill_id` | `checkoutConfirm` |
| `saveDepDisGoodsAndPurchase` | checkoutConfirm 正式化（**不在购物车临时阶段**） |

### 10.3 应删除或降级的职责

| 位置 | 删除/降级内容 |
|------|----------------|
| `PlatformCartSubmitServiceImpl.submitBySupplier` | bill / recalc / 支付响应 / 幂等 / 阻断 |
| `PlatformCustomerSubmitLineServiceImpl.submitLine` | 即时 `assignStatus = PENDING`、`status=0` |
| `PlatformCartSubmitResponse` 作为 addCartLine 响应 | 去掉 billPayState、knownPayAmount 等；checkout 专用新 DTO |
| 文档中「submitBySupplier = 下单即 ASSIGNED」 | 全部改为 checkout 后 ASSIGNED |

---

## 11. 后续代码改造顺序

1. ~~**定稿本设计**（本文）~~ **已定稿**（§1.4 双维度 + §12 A/B）；与前端对齐后进入实现  
2. **DTO + 接口契约**：`listCartLines`、`checkoutPreview`、`checkoutConfirm` 请求/响应  
3. **实现 `listCartLines`**（`status=-1` 查询）  
4. **降级 `submitLine`**：`status=-1`，移除即时 assign  
5. **降级 `submitBySupplier`**：仅建行，移除 bill/bridge/recalc  
6. **实现 `checkoutPreview`**（只读分组 + payAmount）  
7. **实现 `checkoutConfirm`**（迁移 bill 创建 + 挂行 + 转正 + assign/fulfillment + paidTotal）  
8. **调整阻断 / submitToken** 到 checkout  
9. **配送商 / listTodayLines SQL** 排除 `-1` 与未 checkout 数据  
10. **来源 C** 默认配送商提示 API + 前端流程  
11. **更新** `jingcaiMarket/docs/平台订货购物车接口说明.md` 联调章节  
12. **Runner 验收**：购物车临时行 + checkout 混合价 bill（`PlatformCartCheckoutRound1Runner`）  
13. **（后续阶段）** 真实微信支付、补款回调、称重后 recalc 触发点、bill 列表 API  

---

## 12. 混合单示例（验收参照）

共同前提：购物车 2 行，绿甘蓝与大土豆；checkout 前均为 `status = -1`。

| 商品 | 数量/规格 | `priceConfirmStatus` | subtotal |
|------|-----------|----------------------|----------|
| 绿甘蓝 | 3 斤 @ 1.30/斤 | **CONFIRMED** | 3.90 |
| 大土豆 | 1 个（按斤计价，需称重） | **PENDING** | — |

**checkoutPreview**（A/B 相同）：

- `confirmedItemCount=1`, `pendingPriceItemCount=1`  
- `knownTotal=payAmount=3.90`  
- `notice` = 部分商品需称重确认，后续可能产生补款  

**checkoutConfirm 后 bill**（A/B 相同）：

- 2 行均同一 `gb_do_bill_id`，`status` 正式  
- `knownTotal=3.90`, `paidTotal=3.90`, `pendingItemCount=1`, `supplementDue=0`, `payStatus=PARTIAL_PAID`  

差异仅在 **`assignStatus`**（及由此产生的 Electric / 配送商路径）：

---

### 12.A 大土豆未选配送商 — `price PENDING` + `assign PENDING`

写入购物车：绿甘蓝、大土豆均**未选**配送商（来源 A）。

| 商品 | Checkout 后 `priceConfirmStatus` | Checkout 后 `assignStatus` | 后续 |
|------|----------------------------------|----------------------------|------|
| 绿甘蓝 | CONFIRMED | **PENDING** | Electric 分配配送商 |
| 大土豆 | PENDING | **PENDING** | Electric 分配配送商 → 分配后再称重 |

- 两行均 **不进配送商端**，均 **进入 Electric 分配台**（因 `assignStatus = PENDING`，**不是因为**大土豆价格 PENDING）。  
- Electric 为绿甘蓝 assign 后 → `assignStatus = ASSIGNED` → 配送商可见、可出库。  
- Electric 为大土豆 assign 后 → 同上 → 配送商称重 → `priceConfirmStatus` 变 CONFIRMED → recalc。

---

### 12.B 大土豆已选配送商 — `price PENDING` + `assign ASSIGNED`

写入购物车：用户为**两行均选了同一配送商**（来源 B；绿甘蓝价已知，大土豆价待称）。

| 商品 | Checkout 后 `priceConfirmStatus` | Checkout 后 `assignStatus` | 后续 |
|------|----------------------------------|----------------------------|------|
| 绿甘蓝 | CONFIRMED | **ASSIGNED** | 配送商直接出库 |
| 大土豆 | PENDING | **ASSIGNED** | **同一配送商**称重/改价（**不进 Electric 分配台**） |

- Checkout 后两行对**该配送商均可见**（jcJieDan）。  
- 大土豆 **不因** `priceConfirmStatus = PENDING` 进入 Electric；`assignStatus` 已在 checkout 时确定为 ASSIGNED。  
- 配送商对大土豆称重（如 2.50）→ `priceConfirmStatus: PENDING → CONFIRMED` → recalc：

| 字段 | 值 |
|------|-----|
| `finalTotal` | 6.40 |
| `supplementDue` | 2.50 |
| `payStatus` | `AWAIT_SUPPLEMENT` |

> **验收要点**：A 与 B 的 bill 金额字段在 checkout 后相同；差异是 B 的大土豆走**已 ASSIGNED 配送商称重**，A 的大土豆先走 **Electric assign**，再称重。不得用单一「PENDING 行」描述这两种路径。

---

## 13. 修订记录

| 日期 | 说明 |
|------|------|
| 2026-06-20 | 初版：购物车 / checkout / bill 业务主权；纠正 submitBySupplier 与「付款」语义 |
| 2026-06-20 | 文档归属调整：移至农新乐 Java 后台 `docs/nxPlatform/` |
| 2026-06-20 | 定稿补充：`priceConfirmStatus` 与 `assignStatus` 双维度；混合单示例 A/B |
| 2026-06-21 | 术语统一：「购物车临时阶段」；§1.5 bill 固定性 / 补款 vs 禁止追加商品 |
