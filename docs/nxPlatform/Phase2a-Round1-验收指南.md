# Phase 2a 第一轮验收指南（submitLine 最小闭环）

> **当前状态：暂停在 submitLine，等待真实库验收。**  
> 第一轮通过后，再进入第二轮：`pending` / `detail` / `suppliers` / `assign`。  
> 本轮 **不做**：Electric 页面、快照、小程序接入、assign 全套接口。

---

## 一、SQL patch 执行顺序（必须严格按序）

在目标库（开发 / 测试）**按以下顺序**执行，**不可跳步、不可颠倒**：

| 步骤 | 文件 | 说明 |
|------|------|------|
| **1** | `docs/sql/patches/check_nx_department_orders_distributer_id.sql` | 阶段 0：确认 `nx_DO_distributer_id` 是否允许 NULL |
| **2** | `docs/sql/patches/upgrade_nx_platform_phase2a.sql` | 创建 4 张平台表（含 `nx_platform_order_assign`） |
| **3** | `docs/sql/patches/seed_nx_platform_phase2a_test.sql` | **先改真实 ID 再执行**（见下文） |

### 步骤 1 结果 → 应用配置

查询 `IS_NULLABLE` 后：

| 结果 | submitLine 行为 | 额外配置 |
|------|-----------------|----------|
| `YES`（允许 NULL） | PENDING 阶段 `nxDoDistributerId = NULL` | 无 |
| `NO`（NOT NULL） | PENDING 阶段写「平台池配送商」ID | 在 `application.properties` 配置 `platform.pool-distributer-id.{marketId}=<池配送商ID>` |

可选覆盖（一般不需要）：`platform.distributer-id-nullable=true/false`

### 步骤 3 种子数据（必改真实 ID）

编辑 `seed_nx_platform_phase2a_test.sql` 中的占位值：

- `nx_md_market_id` → 真实 `sys_city_market_id`
- `nx_md_department_id` → 真实 `nx_department.nx_department_id`（且该客户在业务上属于该市场）
- submitLine 请求中的 `nxGoodsId` → 真实 `nx_goods.nx_goods_id`（种子 SQL 不含商品行，在 API 请求里填）

---

## 二、⚠️ 先跑 SQL，再启动含新代码的应用

本轮 Java 已在 **部分旧配送商查询** 中加入：

```sql
AND NOT EXISTS (
  SELECT 1 FROM nx_platform_order_assign poa
  WHERE poa.nx_poa_order_id = dor.nx_department_orders_id
    AND poa.nx_poa_assign_mode = 'PLATFORM'
    AND poa.nx_poa_assign_status = 'PENDING'
)
```

**若数据库尚未执行步骤 2（`nx_platform_order_assign` 表不存在），上述旧接口会直接报错（表不存在）。**

**正确部署顺序：**

1. 执行步骤 1 → 2 → 3（SQL patch + 种子）
2. 确认 4 张平台表存在：`SHOW TABLES LIKE 'nx_%platform%';` / `nx_market_department` 等
3. **再** 编译 / 部署 / 启动含 Phase 2a 代码的应用

回滚注意：若已部署新 Java 但未建表，旧配送商端会异常；优先补跑步骤 2，而非先回滚 Java。

---

## 三、第一轮 API 验收清单

应用启动后，按序验收（Postman / curl 均可）。

### 3.1 阶段 0 — 列 NULL 探测

**GET** `/api/platform/orders/schema/distributerIdNullable`

期望成功响应（`code: 0`）：

```json
{
  "code": 0,
  "data": {
    "nullable": true,
    "columnName": "nx_DO_distributer_id",
    "tableExists": true,
    "catalog": "nongxinle",
    "probeSource": "information_schema",
    "note": "..."
  }
}
```

探测逻辑优先 `information_schema.COLUMNS`（MySQL 混合大小写列名下 `DatabaseMetaData.getColumns` 常取不到列）。

若仍失败，响应会附带 `catalog`、`tableExists`、`errorCause`，并对照后端日志 `[probeSchemaFromDatabase]` 堆栈。

**手动 SQL 确认（不依赖接口）：**

```sql
-- 在目标库执行
SELECT COLUMN_NAME, IS_NULLABLE, COLUMN_TYPE
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'nx_department_orders'
  AND COLUMN_NAME LIKE '%distributer_id%';
```

或执行 `docs/sql/patches/check_nx_department_orders_distributer_id.sql`。

**当前仓库 `application.properties` 指向的数据库（供对照）：** `jdbc:mysql://101.42.222.149:3306/nongxinle` — 该库已确认存在 `nx_department_orders`，列名 `nx_DO_distributer_id`，`IS_NULLABLE = YES`。

### 3.2 提交一行平台订单

**POST** `/api/platform/orders/submitLine`

```json
{
  "marketId": 1,
  "departmentId": 100,
  "nxGoodsId": 500,
  "goodsName": "西红柿",
  "quantity": "10",
  "standard": "斤",
  "remark": "",
  "orderUserId": 1
}
```

（`marketId` / `departmentId` / `nxGoodsId` 换为真实值；`departmentId` 须在 `nx_market_department` 且 `ACTIVE`）

期望 HTTP 200，`R.ok`，`data` 含：

| 字段 | 期望 |
|------|------|
| `orderId` | 新生成的 `nx_department_orders_id` |
| `platformAssignId` | 新生成的 `nx_poa_id` |
| `assignStatus` | `PENDING` |
| `assignMode` | `PLATFORM` |
| `nxDoDisGoodsId` | `null` |
| `nxDoCollaborativeNxDisId` | `-1` |
| `nxDoDistributerId` | `null`（列允许 NULL）或配置的池配送商 ID（列 NOT NULL） |

### 3.3 数据库行验收

```sql
SELECT nx_department_orders_id, nx_DO_nx_goods_id, nx_DO_dis_goods_id,
       nx_DO_distributer_id, nx_DO_collaborative_nx_dis_id, nx_DO_status
FROM nx_department_orders
WHERE nx_department_orders_id = ?;

SELECT nx_poa_id, nx_poa_order_id, nx_poa_assign_status, nx_poa_assign_mode,
       nx_poa_assigned_distributer_id, nx_poa_assigned_dis_goods_id
FROM nx_platform_order_assign
WHERE nx_poa_order_id = ?;
```

核对：

1. `nx_department_orders` 写入成功  
2. `nx_platform_order_assign` 写入且 `assign_status = PENDING`  
3. `nx_DO_dis_goods_id` 为空  
4. `nx_DO_collaborative_nx_dis_id = -1`  
5. `nx_DO_distributer_id` 符合 NULL / 池配送商策略  

### 3.4 旧配送商端 — PENDING 单不可见（有限覆盖）

通过旧配送商端或对应 API，用 **带 `disId` 的高频查询** 验证 **看不到** 上一步创建的 PENDING 平台单。

---

## 四、旧配送商 PENDING 排除：**有限覆盖**（非全量）

当前 Java **仅** 在以下 3 个 MyBatis 查询（`NxDepartmentOrdersDao.xml`）中加入了 `excludePlatformPendingOrders`，且 **仅在传入 `disId` 或 `orderDisId` 时生效**：

| 查询 ID | 典型场景 |
|---------|----------|
| `disGetUnPlanPurchaseApplys` | 配送商未计划采购申请 |
| `queryDisOrdersByParams` | 配送商订单列表 / 参数查询 |
| `queryDepOrdersAcount` | 配送商视角订单计数 |

**未覆盖**（第一轮验收时可能仍能看到 PENDING 单，属已知限制）：

- 其它分拣 / 打印 / 结算 / 统计类 SQL
- 不带 `disId` 的查询路径
- 客户侧（部门）订单列表
- 直接按 `nx_department_orders_id` 查详情

因此：

- 验收项「旧配送商高频查询看不到 PENDING 单」**仅针对上述 3 个入口**，不能推断为「所有配送商端入口均已隔离」。
- 第二轮实现 `assign` 前后，需逐步扩大排除范围或改为更统一的过滤策略。

补充：`nx_DO_distributer_id = NULL` 时，按 `disId` 过滤的旧查询通常本就不会命中；排除片段主要防范 **NOT NULL + 平台池配送商占位 ID** 时误进配送商视图。

---

## 五、第一轮不验收 / 不开发

- `pending` / `detail` / `suppliers` / `assign` 接口  
- Electric 桌面端（`electron-platform`）  
- 快照（Phase 2b）  
- 小程序接入  
- 协作链 / `saveCollaborativeOrderWhenNeeded` 改动  

---

## 六、验收通过后

进入 **Phase 2a 第二轮**：`pending` → `detail` → `suppliers` → `assign`（仍不做 Electric / 快照 / 小程序）。

请将以下信息反馈以便确认可进入第二轮：

1. 步骤 1 的 `IS_NULLABLE` 结果  
2. `distributerIdNullable` 接口返回值  
3. 一次 `submitLine` 的请求体与响应  
4. 上述两条 SQL 的查询结果  
5. 旧配送商 3 个高频入口是否均看不到该 PENDING 单  
