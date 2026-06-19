-- =============================================================================
-- Phase 2b Round 2-B.1：历史 ASSIGNED 平台单期望价回填（可选，执行前请先跑 §1 预览）
--
-- 目标：
--   nxDoExpectPrice  为空的历史 ASSIGNED 平台单 → 用当前有效 nxDoPrice 回填
--   nxDoPriceDifferent → 0
--
-- 不处理：
--   PENDING、已有 nxDoExpectPrice、nxDoPrice 无效（空 / 0 / 0.1 / ≤0.1）
--
-- 依赖：upgrade_nx_platform_phase2a.sql 已执行
-- =============================================================================

-- §1 预览（只读）
SELECT
    dor.nx_department_orders_id AS order_id,
    poa.nx_poa_assign_status,
    dor.nx_DO_price AS current_price,
    dor.nx_DO_expect_price AS current_expect_price,
    dor.nx_DO_price_different AS current_price_different
FROM nx_department_orders dor
INNER JOIN nx_platform_order_assign poa
    ON poa.nx_poa_order_id = dor.nx_department_orders_id
   AND poa.nx_poa_assign_mode = 'PLATFORM'
   AND poa.nx_poa_assign_status = 'ASSIGNED'
WHERE (dor.nx_DO_expect_price IS NULL OR TRIM(dor.nx_DO_expect_price) = '')
  AND dor.nx_DO_price IS NOT NULL
  AND TRIM(dor.nx_DO_price) <> ''
  AND dor.nx_DO_price REGEXP '^-?[0-9]+(\\.[0-9]+)?$'
  AND CAST(dor.nx_DO_price AS DECIMAL(18,4)) > 0.1
ORDER BY dor.nx_department_orders_id;

-- §2 回填（确认 §1 结果后再执行）
-- UPDATE nx_department_orders dor
-- INNER JOIN nx_platform_order_assign poa
--     ON poa.nx_poa_order_id = dor.nx_department_orders_id
--    AND poa.nx_poa_assign_mode = 'PLATFORM'
--    AND poa.nx_poa_assign_status = 'ASSIGNED'
-- SET dor.nx_DO_expect_price = dor.nx_DO_price,
--     dor.nx_DO_price_different = '0'
-- WHERE (dor.nx_DO_expect_price IS NULL OR TRIM(dor.nx_DO_expect_price) = '')
--   AND dor.nx_DO_price IS NOT NULL
--   AND TRIM(dor.nx_DO_price) <> ''
--   AND dor.nx_DO_price REGEXP '^-?[0-9]+(\\.[0-9]+)?$'
--   AND CAST(dor.nx_DO_price AS DECIMAL(18,4)) > 0.1;

-- §3 验证（只读）
-- SELECT dor.nx_department_orders_id, dor.nx_DO_expect_price, dor.nx_DO_price, dor.nx_DO_price_different
-- FROM nx_department_orders dor
-- INNER JOIN nx_platform_order_assign poa ON poa.nx_poa_order_id = dor.nx_department_orders_id
-- WHERE poa.nx_poa_assign_mode = 'PLATFORM' AND poa.nx_poa_assign_status = 'ASSIGNED'
-- ORDER BY dor.nx_department_orders_id DESC LIMIT 20;
