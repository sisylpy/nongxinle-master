-- =============================================================================
-- 京采平台测试数据清理 · GB 饭店 57(父)/58(子) · 市场 1
--
-- 清除内容：
--   1. PLATFORM_CASH 采购单及支付流水（含 AWAIT_FIRST_PAY / AWAIT_SUPPLEMENT 阻断单）
--   2. 关联 gb_department_orders（含 gb_do_price_confirm_status 平台行）/ nx_department_orders
--   3. nx_platform_order_assign / nx_platform_order_fulfillment
--
-- 保留：nx_market_department 绑定、nx_department_nx_goods_default 默认配送商
--
-- 用法：
--   1) 先执行「第 1 步：预览」确认范围
--   2) 再执行「第 2 步：删除」；确认无误后 COMMIT，有问题则 ROLLBACK
-- =============================================================================

-- USE nongxinle;   -- 按实际库名取消注释

SET @market_id   = 1;
SET @dep_father  = 57;
SET @dep_child   = 58;

-- =============================================================================
-- 第 1 步：预览（只读，可单独执行）
-- =============================================================================

SELECT '① 阻断中的 PLATFORM_CASH 账单' AS section,
       gb_department_bill_id,
       gb_DB_dep_id,
       gb_DB_dep_father_id,
       gb_db_pay_status,
       gb_db_known_total,
       gb_db_paid_total,
       gb_db_supplement_due,
       gb_db_platform_submit_token
FROM gb_department_bill
WHERE gb_db_bill_source = 'PLATFORM_CASH'
  AND gb_db_pay_status IN ('AWAIT_FIRST_PAY', 'AWAIT_SUPPLEMENT')
  AND (gb_DB_dep_id IN (@dep_father, @dep_child)
    OR gb_DB_dep_father_id IN (@dep_father, @dep_child));

SELECT '② 全部 PLATFORM_CASH 账单（将删除）' AS section,
       gb_department_bill_id,
       gb_DB_dep_id,
       gb_db_pay_status,
       gb_db_total,
       gb_db_known_total
FROM gb_department_bill
WHERE gb_db_bill_source = 'PLATFORM_CASH'
  AND (gb_DB_dep_id IN (@dep_father, @dep_child)
    OR gb_DB_dep_father_id IN (@dep_father, @dep_child));

SELECT '③ 平台 assign 行数' AS section,
       COUNT(*) AS cnt
FROM nx_platform_order_assign poa
WHERE poa.nx_poa_market_id = @market_id
  AND (
        poa.nx_poa_department_id IN (@dep_father, @dep_child)
     OR poa.nx_poa_gb_department_id IN (@dep_father, @dep_child)
     OR poa.nx_poa_gb_department_father_id = @dep_father
  );

SELECT '④ 京采 GB 平台 nx 订单行数（nx_DO_gb_department*）' AS section,
       COUNT(*) AS cnt
FROM nx_department_orders nx
WHERE nx.nx_DO_gb_department_id IN (@dep_father, @dep_child)
   OR nx.nx_DO_gb_department_father_id = @dep_father;

SELECT '⑤ gb_department_orders（平台测试行，将删除）' AS section,
       gb.gb_department_orders_id,
       gb.gb_DO_department_id,
       gb.gb_DO_bill_id,
       gb.gb_DO_nx_department_order_id,
       gb.gb_do_price_confirm_status,
       gb.gb_DO_goods_name
FROM gb_department_orders gb
WHERE (gb.gb_DO_department_id IN (@dep_father, @dep_child)
    OR gb.gb_DO_department_father_id IN (@dep_father, @dep_child))
  AND (
        gb.gb_DO_bill_id IN (
            SELECT gb_department_bill_id FROM gb_department_bill
            WHERE gb_db_bill_source = 'PLATFORM_CASH'
              AND (gb_DB_dep_id IN (@dep_father, @dep_child)
                OR gb_DB_dep_father_id IN (@dep_father, @dep_child))
        )
     OR gb.gb_do_price_confirm_status IS NOT NULL
     OR EXISTS (
            SELECT 1 FROM nx_platform_order_assign poa
            WHERE poa.nx_poa_gb_department_order_id = gb.gb_department_orders_id
              AND poa.nx_poa_market_id = @market_id
        )
     OR gb.gb_DO_nx_department_order_id IN (
            SELECT nx.nx_department_orders_id FROM nx_department_orders nx
            WHERE nx.nx_DO_gb_department_id IN (@dep_father, @dep_child)
               OR nx.nx_DO_gb_department_father_id = @dep_father
        )
  );


-- =============================================================================
-- 第 2 步：删除（事务）
-- =============================================================================

START TRANSACTION;

DROP TEMPORARY TABLE IF EXISTS tmp_plat_bills;
CREATE TEMPORARY TABLE tmp_plat_bills (
    bill_id INT PRIMARY KEY
) ENGINE=MEMORY;

INSERT INTO tmp_plat_bills (bill_id)
SELECT gb_department_bill_id
FROM gb_department_bill
WHERE gb_db_bill_source = 'PLATFORM_CASH'
  AND (gb_DB_dep_id IN (@dep_father, @dep_child)
    OR gb_DB_dep_father_id IN (@dep_father, @dep_child));

DROP TEMPORARY TABLE IF EXISTS tmp_plat_gb_orders;
CREATE TEMPORARY TABLE tmp_plat_gb_orders (
    gb_order_id INT PRIMARY KEY,
    nx_order_id INT NOT NULL DEFAULT 0
) ENGINE=MEMORY;

INSERT INTO tmp_plat_gb_orders (gb_order_id, nx_order_id)
SELECT DISTINCT
    gb.gb_department_orders_id,
    IFNULL(gb.gb_DO_nx_department_order_id, 0)
FROM gb_department_orders gb
WHERE gb.gb_DO_bill_id IN (SELECT bill_id FROM tmp_plat_bills)
   OR gb.gb_department_orders_id IN (
        SELECT poa.nx_poa_gb_department_order_id
        FROM nx_platform_order_assign poa
        WHERE poa.nx_poa_market_id = @market_id
          AND poa.nx_poa_gb_department_order_id IS NOT NULL
          AND (
                poa.nx_poa_gb_department_id IN (@dep_father, @dep_child)
             OR poa.nx_poa_gb_department_father_id = @dep_father
             OR poa.nx_poa_department_id IN (@dep_father, @dep_child)
          )
   )
   OR (
        (gb.gb_DO_department_id IN (@dep_father, @dep_child)
      OR gb.gb_DO_department_father_id IN (@dep_father, @dep_child))
    AND (
            gb.gb_do_price_confirm_status IS NOT NULL
         OR gb.gb_DO_nx_department_order_id IN (
                SELECT nx.nx_department_orders_id
                FROM nx_department_orders nx
                WHERE nx.nx_DO_gb_department_id IN (@dep_father, @dep_child)
                   OR nx.nx_DO_gb_department_father_id = @dep_father
            )
        )
   );

DROP TEMPORARY TABLE IF EXISTS tmp_plat_nx_orders;
CREATE TEMPORARY TABLE tmp_plat_nx_orders (
    nx_order_id INT PRIMARY KEY
) ENGINE=MEMORY;

INSERT INTO tmp_plat_nx_orders (nx_order_id)
SELECT nx_order_id
FROM tmp_plat_gb_orders
WHERE nx_order_id > 0;

INSERT IGNORE INTO tmp_plat_nx_orders (nx_order_id)
SELECT poa.nx_poa_order_id
FROM nx_platform_order_assign poa
WHERE poa.nx_poa_market_id = @market_id
  AND (
        poa.nx_poa_department_id IN (@dep_father, @dep_child)
     OR poa.nx_poa_gb_department_id IN (@dep_father, @dep_child)
     OR poa.nx_poa_gb_department_father_id = @dep_father
  );

INSERT IGNORE INTO tmp_plat_nx_orders (nx_order_id)
SELECT nx.nx_department_orders_id
FROM nx_department_orders nx
WHERE nx.nx_DO_gb_department_id IN (@dep_father, @dep_child)
   OR nx.nx_DO_gb_department_father_id = @dep_father;

-- 删除前快照
SELECT '待删 bill' AS item, COUNT(*) AS cnt FROM tmp_plat_bills
UNION ALL SELECT '待删 gb_order', COUNT(*) FROM tmp_plat_gb_orders
UNION ALL SELECT '待删 nx_order', COUNT(*) FROM tmp_plat_nx_orders;

-- 1) 履约
DELETE pof
FROM nx_platform_order_fulfillment pof
INNER JOIN tmp_plat_nx_orders t ON t.nx_order_id = pof.nx_pof_order_id;

-- 2) 平台分配
DELETE poa
FROM nx_platform_order_assign poa
WHERE poa.nx_poa_order_id IN (SELECT nx_order_id FROM tmp_plat_nx_orders)
   OR poa.nx_poa_gb_department_order_id IN (SELECT gb_order_id FROM tmp_plat_gb_orders)
   OR (
        poa.nx_poa_market_id = @market_id
    AND (
            poa.nx_poa_department_id IN (@dep_father, @dep_child)
         OR poa.nx_poa_gb_department_id IN (@dep_father, @dep_child)
         OR poa.nx_poa_gb_department_father_id = @dep_father
        )
   );

-- 3) 支付流水
DELETE bp
FROM gb_department_bill_payment bp
INNER JOIN tmp_plat_bills t ON t.bill_id = bp.gb_bp_bill_id;

-- 4) GB 订单行
DELETE gb
FROM gb_department_orders gb
WHERE gb.gb_department_orders_id IN (SELECT gb_order_id FROM tmp_plat_gb_orders)
   OR gb.gb_DO_bill_id IN (SELECT bill_id FROM tmp_plat_bills)
   OR gb.gb_DO_nx_department_order_id IN (SELECT nx_order_id FROM tmp_plat_nx_orders);

-- 5) NX 订单行
DELETE nx
FROM nx_department_orders nx
INNER JOIN tmp_plat_nx_orders t ON t.nx_order_id = nx.nx_department_orders_id;

-- 6) PLATFORM_CASH 账单
DELETE b
FROM gb_department_bill b
INNER JOIN tmp_plat_bills t ON t.bill_id = b.gb_department_bill_id;

-- 删除后验证（应为 0）
SELECT '删除后-阻断账单' AS check_item,
       COUNT(*) AS cnt
FROM gb_department_bill
WHERE gb_db_bill_source = 'PLATFORM_CASH'
  AND gb_db_pay_status IN ('AWAIT_FIRST_PAY', 'AWAIT_SUPPLEMENT')
  AND (gb_DB_dep_id IN (@dep_father, @dep_child)
    OR gb_DB_dep_father_id IN (@dep_father, @dep_child))
UNION ALL
SELECT '删除后-平台assign', COUNT(*)
FROM nx_platform_order_assign poa
WHERE poa.nx_poa_market_id = @market_id
  AND (
        poa.nx_poa_department_id IN (@dep_father, @dep_child)
     OR poa.nx_poa_gb_department_id IN (@dep_father, @dep_child)
     OR poa.nx_poa_gb_department_father_id = @dep_father
  )
UNION ALL
SELECT '删除后-GB平台nx订单', COUNT(*)
FROM nx_department_orders nx
WHERE nx.nx_DO_gb_department_id IN (@dep_father, @dep_child)
   OR nx.nx_DO_gb_department_father_id = @dep_father
UNION ALL
SELECT '删除后-gb_department_orders(平台行)', COUNT(*)
FROM gb_department_orders gb
WHERE (gb.gb_DO_department_id IN (@dep_father, @dep_child)
    OR gb.gb_DO_department_father_id IN (@dep_father, @dep_child))
  AND (
        gb.gb_do_price_confirm_status IS NOT NULL
     OR gb.gb_DO_bill_id IN (
            SELECT gb_department_bill_id FROM gb_department_bill
            WHERE gb_db_bill_source = 'PLATFORM_CASH'
              AND (gb_DB_dep_id IN (@dep_father, @dep_child)
                OR gb_DB_dep_father_id IN (@dep_father, @dep_child))
        )
     OR EXISTS (
            SELECT 1 FROM nx_platform_order_assign poa
            WHERE poa.nx_poa_gb_department_order_id = gb.gb_department_orders_id
              AND poa.nx_poa_market_id = @market_id
        )
  );

-- 确认无误后执行下一行；有问题则 ROLLBACK;
COMMIT;
-- ROLLBACK;

DROP TEMPORARY TABLE IF EXISTS tmp_plat_bills;
DROP TEMPORARY TABLE IF EXISTS tmp_plat_gb_orders;
DROP TEMPORARY TABLE IF EXISTS tmp_plat_nx_orders;
