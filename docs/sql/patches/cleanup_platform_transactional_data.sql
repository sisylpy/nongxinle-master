-- =============================================================================
-- 京采平台 ·  transactional 数据清理（保留主数据 / LEGACY 业务）
--
-- 用途：本地与远程联调数据混乱时，清空平台购物车 / checkout / bill / assign /
--       fulfillment / 平台 NX·GB 订单行，便于从真实饭馆重新走一遍下单。
--
-- ⚠️  不可恢复。执行前请确认当前库（Navicat 左上角库名 / jdbc.url）。
-- ⚠️  不删除：nx_department、gb_department、配送商商品、市场配置、LEGACY bill。
--
-- 用法：
--   1. 先运行到「§1 清理前预览」为止，核对各表行数
--   2. 确认无误后，继续运行 §2（或整文件一次执行）
--
-- 识别范围（平台订单 NX id）：
--   · nx_platform_order_assign 中已有 assign 的行
--   · 京采购物车临时行：nx_DO_status = -1 且 nx_DO_gb_department_id > 0
-- =============================================================================

-- =============================================================================
-- §0 可选：仅清理指定市场（NULL = 全部市场）
-- =============================================================================
SET @cleanup_market_id = NULL;  -- 例如只清 marketId=1：SET @cleanup_market_id = 1;

-- =============================================================================
-- §1 清理前预览
-- =============================================================================
SELECT 'preview_assign' AS step, COUNT(*) AS cnt FROM nx_platform_order_assign
WHERE (@cleanup_market_id IS NULL OR nx_poa_market_id = @cleanup_market_id);

SELECT 'preview_fulfillment' AS step, COUNT(*) AS cnt FROM nx_platform_order_fulfillment pof
WHERE (@cleanup_market_id IS NULL OR pof.nx_pof_market_id = @cleanup_market_id);

SELECT 'preview_checkout_payment' AS step, COUNT(*) AS cnt FROM platform_checkout_payment
WHERE (@cleanup_market_id IS NULL OR pcp_market_id = @cleanup_market_id);

SELECT 'preview_platform_cash_bill' AS step, COUNT(*) AS cnt FROM gb_department_bill
WHERE gb_db_bill_source = 'PLATFORM_CASH';

SELECT 'preview_platform_nx_orders' AS step, COUNT(*) AS cnt
FROM nx_department_orders dor
WHERE dor.nx_department_orders_id IN (
    SELECT nx_poa_order_id FROM nx_platform_order_assign poa
    WHERE (@cleanup_market_id IS NULL OR poa.nx_poa_market_id = @cleanup_market_id)
    UNION
    SELECT dor2.nx_department_orders_id FROM nx_department_orders dor2
    WHERE dor2.nx_DO_status = -1
      AND dor2.nx_DO_gb_department_id IS NOT NULL
      AND dor2.nx_DO_gb_department_id > 0
);

-- =============================================================================
-- §2 执行清理（事务）
-- =============================================================================
START TRANSACTION;

DROP TEMPORARY TABLE IF EXISTS tmp_platform_nx_order_ids;
CREATE TEMPORARY TABLE tmp_platform_nx_order_ids (
    nx_order_id INT NOT NULL PRIMARY KEY
);

INSERT IGNORE INTO tmp_platform_nx_order_ids (nx_order_id)
SELECT poa.nx_poa_order_id
FROM nx_platform_order_assign poa
WHERE (@cleanup_market_id IS NULL OR poa.nx_poa_market_id = @cleanup_market_id);

INSERT IGNORE INTO tmp_platform_nx_order_ids (nx_order_id)
SELECT dor.nx_department_orders_id
FROM nx_department_orders dor
WHERE dor.nx_DO_status = -1
  AND dor.nx_DO_gb_department_id IS NOT NULL
  AND dor.nx_DO_gb_department_id > 0;

DROP TEMPORARY TABLE IF EXISTS tmp_platform_gb_order_ids;
CREATE TEMPORARY TABLE tmp_platform_gb_order_ids (
    gb_order_id INT NOT NULL PRIMARY KEY
);

INSERT IGNORE INTO tmp_platform_gb_order_ids (gb_order_id)
SELECT poa.nx_poa_gb_department_order_id
FROM nx_platform_order_assign poa
WHERE poa.nx_poa_gb_department_order_id IS NOT NULL
  AND poa.nx_poa_gb_department_order_id > 0
  AND (@cleanup_market_id IS NULL OR poa.nx_poa_market_id = @cleanup_market_id);

INSERT IGNORE INTO tmp_platform_gb_order_ids (gb_order_id)
SELECT gbo.gb_department_orders_id
FROM gb_department_orders gbo
INNER JOIN tmp_platform_nx_order_ids t ON t.nx_order_id = gbo.gb_do_nx_department_order_id
WHERE gbo.gb_do_nx_department_order_id IS NOT NULL
  AND gbo.gb_do_nx_department_order_id > 0;

INSERT IGNORE INTO tmp_platform_gb_order_ids (gb_order_id)
SELECT gbo.gb_department_orders_id
FROM gb_department_orders gbo
INNER JOIN gb_department_bill bill ON bill.gb_department_bill_id = gbo.gb_do_bill_id
WHERE bill.gb_db_bill_source = 'PLATFORM_CASH';

DROP TEMPORARY TABLE IF EXISTS tmp_platform_bill_ids;
CREATE TEMPORARY TABLE tmp_platform_bill_ids (
    bill_id INT NOT NULL PRIMARY KEY
);

INSERT IGNORE INTO tmp_platform_bill_ids (bill_id)
SELECT bill.gb_department_bill_id
FROM gb_department_bill bill
WHERE bill.gb_db_bill_source = 'PLATFORM_CASH';

INSERT IGNORE INTO tmp_platform_bill_ids (bill_id)
SELECT DISTINCT gbo.gb_do_bill_id
FROM gb_department_orders gbo
INNER JOIN tmp_platform_gb_order_ids tg ON tg.gb_order_id = gbo.gb_department_orders_id
WHERE gbo.gb_do_bill_id IS NOT NULL
  AND gbo.gb_do_bill_id > 0;

-- 采购商品（平台单关联）
DELETE gdpg FROM gb_distributer_purchase_goods gdpg
INNER JOIN gb_department_orders gbo ON gbo.gb_do_purchase_goods_id = gdpg.gb_distributer_purchase_goods_id
INNER JOIN tmp_platform_gb_order_ids tg ON tg.gb_order_id = gbo.gb_department_orders_id
WHERE gbo.gb_do_purchase_goods_id IS NOT NULL AND gbo.gb_do_purchase_goods_id > 0;

DELETE ndpg FROM nx_distributer_purchase_goods ndpg
INNER JOIN nx_department_orders dor ON dor.nx_DO_purchase_goods_id = ndpg.nx_distributer_purchase_goods_id
INNER JOIN tmp_platform_nx_order_ids t ON t.nx_order_id = dor.nx_department_orders_id
WHERE dor.nx_DO_purchase_goods_id IS NOT NULL AND dor.nx_DO_purchase_goods_id > 0;

-- 快照-订单关联
DELETE dngso FROM nx_department_nx_goods_snapshot_order dngso
INNER JOIN tmp_platform_nx_order_ids t ON t.nx_order_id = dngso.nx_dngso_order_id;

-- 履约
DELETE pof FROM nx_platform_order_fulfillment pof
INNER JOIN tmp_platform_nx_order_ids t ON t.nx_order_id = pof.nx_pof_order_id;

-- 换供日志
DELETE swlog FROM nx_supplier_switch_log swlog
INNER JOIN tmp_platform_nx_order_ids t ON t.nx_order_id = swlog.nx_ssl_order_id;

-- 分配
DELETE poa FROM nx_platform_order_assign poa
INNER JOIN tmp_platform_nx_order_ids t ON t.nx_order_id = poa.nx_poa_order_id;

-- bill 支付流水
DELETE bp FROM gb_department_bill_payment bp
INNER JOIN tmp_platform_bill_ids tb ON tb.bill_id = bp.gb_bp_bill_id;

-- checkout 微信支付意图
DELETE FROM platform_checkout_payment
WHERE (@cleanup_market_id IS NULL OR pcp_market_id = @cleanup_market_id);

-- GB 订单行
DELETE gbo FROM gb_department_orders gbo
INNER JOIN tmp_platform_gb_order_ids tg ON tg.gb_order_id = gbo.gb_department_orders_id;

-- NX 订单行
DELETE dor FROM nx_department_orders dor
INNER JOIN tmp_platform_nx_order_ids t ON t.nx_order_id = dor.nx_department_orders_id;

-- 平台 bill
DELETE bill FROM gb_department_bill bill
INNER JOIN tmp_platform_bill_ids tb ON tb.bill_id = bill.gb_department_bill_id;

-- 平台快照（可选：历史比价数据，一并清空便于干净联调）
DELETE dngs FROM nx_department_nx_goods_snapshot dngs
WHERE (@cleanup_market_id IS NULL OR dngs.nx_dngs_market_id = @cleanup_market_id);

-- 默认配送商表上的 last_order 引用
UPDATE nx_department_nx_goods_default dngd
SET dngd.nx_dngd_last_order_id = NULL,
    dngd.nx_dngd_last_switch_log_id = NULL
WHERE (@cleanup_market_id IS NULL OR dngd.nx_dngd_market_id = @cleanup_market_id);

COMMIT;

-- =============================================================================
-- §3 清理后验证（应均为 0 或仅剩 LEGACY）
-- =============================================================================
SELECT 'post_assign' AS step, COUNT(*) AS cnt FROM nx_platform_order_assign
WHERE (@cleanup_market_id IS NULL OR nx_poa_market_id = @cleanup_market_id);

SELECT 'post_fulfillment' AS step, COUNT(*) AS cnt FROM nx_platform_order_fulfillment
WHERE (@cleanup_market_id IS NULL OR nx_pof_market_id = @cleanup_market_id);

SELECT 'post_checkout_payment' AS step, COUNT(*) AS cnt FROM platform_checkout_payment
WHERE (@cleanup_market_id IS NULL OR pcp_market_id = @cleanup_market_id);

SELECT 'post_platform_cash_bill' AS step, COUNT(*) AS cnt FROM gb_department_bill
WHERE gb_db_bill_source = 'PLATFORM_CASH';

SELECT 'post_platform_cart_nx' AS step, COUNT(*) AS cnt FROM nx_department_orders
WHERE nx_DO_status = -1 AND nx_DO_gb_department_id > 0;

SELECT 'post_assign_open_nx' AS step, COUNT(*) AS cnt
FROM nx_department_orders dor
WHERE EXISTS (
    SELECT 1 FROM nx_platform_order_assign poa WHERE poa.nx_poa_order_id = dor.nx_department_orders_id
);
