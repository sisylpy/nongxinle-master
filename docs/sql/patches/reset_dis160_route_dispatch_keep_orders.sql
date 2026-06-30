-- =============================================================================
-- disId=160：清空「分派 / 装车 / 配送 / 司机上岗」运行数据，只保留 nx_department_orders 订单
--
-- 用途：Today 沙盘从第一步重新模拟（建议派车 → 编辑路线 → 装车）
-- 不动：客户 nx_department、司机 nx_distributer_user、商品/供货商、订单 nx_department_orders
--
-- Navicat 用法（重要）：
--   1) 可选：先单独跑「A. 删除前预览」
--   2) 必须：选中「B. 删除 + C. 校验」整段执行
--   3) 本脚本 **不使用 START TRANSACTION**，每条 DELETE 自动提交，避免未 COMMIT 锁死 duty 表
--   ※ 只跑 A 或只跑 C 的 SELECT，不会删任何数据
--   ※ 小程序连的是哪个库，就在哪个库跑（grainservice.club → 远程 101.42.222.149，不是 localhost）
-- =============================================================================

SELECT DATABASE() AS current_db, NOW() AS run_at;

SET @dis_id := 160;

-- =============================================================================
-- A. 删除前预览（可选，仅 SELECT）
-- =============================================================================
SELECT 'nx_dis_route_plan' AS tbl, COUNT(*) AS cnt
FROM nx_dis_route_plan
WHERE nx_drp_distributer_id = @dis_id
UNION ALL
SELECT 'nx_dis_driver_route', COUNT(*)
FROM nx_dis_driver_route dr
         JOIN nx_dis_route_plan p ON p.nx_drp_id = dr.nx_ddr_plan_id
WHERE p.nx_drp_distributer_id = @dis_id
UNION ALL
SELECT 'nx_dis_shipment_task', COUNT(*)
FROM nx_dis_shipment_task
WHERE nx_dst_distributer_id = @dis_id
UNION ALL
SELECT 'nx_dis_shipment_task_item', COUNT(*)
FROM nx_dis_shipment_task_item ti
         JOIN nx_dis_shipment_task t ON t.nx_dst_id = ti.nx_dsti_task_id
WHERE t.nx_dst_distributer_id = @dis_id
UNION ALL
SELECT 'nx_dis_route_stop', COUNT(*)
FROM nx_dis_route_stop s
         LEFT JOIN nx_dis_driver_route dr ON dr.nx_ddr_id = s.nx_drs_driver_route_id
         LEFT JOIN nx_dis_route_plan p ON p.nx_drp_id = dr.nx_ddr_plan_id AND p.nx_drp_distributer_id = @dis_id
         LEFT JOIN nx_dis_shipment_task t ON t.nx_dst_id = s.nx_drs_shipment_task_id AND t.nx_dst_distributer_id = @dis_id
WHERE p.nx_drp_id IS NOT NULL OR t.nx_dst_id IS NOT NULL
UNION ALL
SELECT 'nx_dis_route_stop_order', COUNT(*)
FROM nx_dis_route_stop_order so
         JOIN nx_dis_route_stop s ON s.nx_drs_id = so.nx_drso_stop_id
         LEFT JOIN nx_dis_driver_route dr ON dr.nx_ddr_id = s.nx_drs_driver_route_id
         LEFT JOIN nx_dis_route_plan p ON p.nx_drp_id = dr.nx_ddr_plan_id AND p.nx_drp_distributer_id = @dis_id
         LEFT JOIN nx_dis_shipment_task t ON t.nx_dst_id = s.nx_drs_shipment_task_id AND t.nx_dst_distributer_id = @dis_id
WHERE p.nx_drp_id IS NOT NULL OR t.nx_dst_id IS NOT NULL
UNION ALL
SELECT 'nx_dis_route_unassigned_stop', COUNT(*)
FROM nx_dis_route_unassigned_stop us
         JOIN nx_dis_route_plan p ON p.nx_drp_id = us.nx_drus_plan_id
WHERE p.nx_drp_distributer_id = @dis_id
UNION ALL
SELECT 'nx_dis_route_unassigned_stop_order', COUNT(*)
FROM nx_dis_route_unassigned_stop_order uo
         JOIN nx_dis_route_unassigned_stop us ON us.nx_drus_id = uo.nx_druo_unassigned_stop_id
         JOIN nx_dis_route_plan p ON p.nx_drp_id = us.nx_drus_plan_id
WHERE p.nx_drp_distributer_id = @dis_id
UNION ALL
SELECT 'nx_dis_driver_duty', COUNT(*)
FROM nx_dis_driver_duty
WHERE nx_ddd_distributer_id = @dis_id
UNION ALL
SELECT 'nx_department_orders (保留)', COUNT(*)
FROM nx_department_orders dor
WHERE dor.nx_DO_distributer_id = @dis_id
  AND (dor.nx_DO_status IS NULL OR dor.nx_DO_status < 3)
  AND (dor.nx_DO_collaborative_nx_dis_id = -1 OR dor.nx_DO_collaborative_nx_dis_id IS NULL);

-- 司机上岗明细（你截图里的 nx_ddd_id / nx_ddd_driver_user_id 等）
SELECT nx_ddd_id,
       nx_ddd_distributer_id,
       nx_ddd_driver_user_id,
       nx_ddd_duty_date,
       nx_ddd_duty_status
FROM nx_dis_driver_duty
WHERE nx_ddd_distributer_id = @dis_id
ORDER BY nx_ddd_duty_date, nx_ddd_driver_user_id;

-- =============================================================================
-- B + C. 删除派单运行数据 + 校验（Navicat 从 B.0 选到 C 段最后一个 SELECT）
-- 不用事务：避免 Navicat 忘记 COMMIT 导致 nx_dis_driver_duty 锁等待 ~50s，duty/on 超时
-- =============================================================================

-- B.0 司机当日可派/上岗状态（独立表，不依赖 plan/route；必须清，否则 Today 仍显示旧上岗）
DELETE FROM nx_dis_driver_duty
WHERE nx_ddd_distributer_id = @dis_id;

SELECT ROW_COUNT() AS deleted_nx_dis_driver_duty_rows;

-- B.1 legacy 站点-订单快照
DELETE so
FROM nx_dis_route_stop_order so
         JOIN nx_dis_route_stop s ON s.nx_drs_id = so.nx_drso_stop_id
         LEFT JOIN nx_dis_driver_route dr ON dr.nx_ddr_id = s.nx_drs_driver_route_id
         LEFT JOIN nx_dis_route_plan p ON p.nx_drp_id = dr.nx_ddr_plan_id AND p.nx_drp_distributer_id = @dis_id
         LEFT JOIN nx_dis_shipment_task t ON t.nx_dst_id = s.nx_drs_shipment_task_id AND t.nx_dst_distributer_id = @dis_id
WHERE p.nx_drp_id IS NOT NULL OR t.nx_dst_id IS NOT NULL;

-- B.2 未分派站点-订单
DELETE uo
FROM nx_dis_route_unassigned_stop_order uo
         JOIN nx_dis_route_unassigned_stop us ON us.nx_drus_id = uo.nx_druo_unassigned_stop_id
         JOIN nx_dis_route_plan p ON p.nx_drp_id = us.nx_drus_plan_id
WHERE p.nx_drp_distributer_id = @dis_id;

-- B.3 任务明细（只删 dis160 的 task，不碰 nx_department_orders）
DELETE ti
FROM nx_dis_shipment_task_item ti
         JOIN nx_dis_shipment_task t ON t.nx_dst_id = ti.nx_dsti_task_id
WHERE t.nx_dst_distributer_id = @dis_id;

-- B.4 路线站点
DELETE s
FROM nx_dis_route_stop s
         LEFT JOIN nx_dis_driver_route dr ON dr.nx_ddr_id = s.nx_drs_driver_route_id
         LEFT JOIN nx_dis_route_plan p ON p.nx_drp_id = dr.nx_ddr_plan_id AND p.nx_drp_distributer_id = @dis_id
         LEFT JOIN nx_dis_shipment_task t ON t.nx_dst_id = s.nx_drs_shipment_task_id AND t.nx_dst_distributer_id = @dis_id
WHERE p.nx_drp_id IS NOT NULL OR t.nx_dst_id IS NOT NULL;

-- B.5 装车/配送任务（含 ASSIGNED / LOADING / IN_DELIVERY 等全部清掉）
DELETE FROM nx_dis_shipment_task
WHERE nx_dst_distributer_id = @dis_id;

-- B.6 司机路线（含 nx_ddr_loading_entered_at 装车标记）
DELETE dr
FROM nx_dis_driver_route dr
         JOIN nx_dis_route_plan p ON p.nx_drp_id = dr.nx_ddr_plan_id
WHERE p.nx_drp_distributer_id = @dis_id;

-- B.7 未分派站点
DELETE us
FROM nx_dis_route_unassigned_stop us
         JOIN nx_dis_route_plan p ON p.nx_drp_id = us.nx_drus_plan_id
WHERE p.nx_drp_distributer_id = @dis_id;

-- B.8 路线计划
DELETE FROM nx_dis_route_plan
WHERE nx_drp_distributer_id = @dis_id;

-- ---------------------------------------------------------------------------
-- C. 删除后校验（派单表应全 0；订单仍 >0）
-- ---------------------------------------------------------------------------
SELECT 'nx_dis_route_plan' AS tbl, COUNT(*) AS cnt
FROM nx_dis_route_plan
WHERE nx_drp_distributer_id = @dis_id
UNION ALL
SELECT 'nx_dis_shipment_task', COUNT(*)
FROM nx_dis_shipment_task
WHERE nx_dst_distributer_id = @dis_id
UNION ALL
SELECT 'nx_dis_driver_route', COUNT(*)
FROM nx_dis_driver_route dr
         JOIN nx_dis_route_plan p ON p.nx_drp_id = dr.nx_ddr_plan_id
WHERE p.nx_drp_distributer_id = @dis_id
UNION ALL
SELECT 'nx_dis_driver_duty (应为 0)', COUNT(*)
FROM nx_dis_driver_duty
WHERE nx_ddd_distributer_id = @dis_id
UNION ALL
SELECT 'nx_department_orders (应保留)', COUNT(*)
FROM nx_department_orders dor
WHERE dor.nx_DO_distributer_id = @dis_id
  AND (dor.nx_DO_status IS NULL OR dor.nx_DO_status < 3)
  AND (dor.nx_DO_collaborative_nx_dis_id = -1 OR dor.nx_DO_collaborative_nx_dis_id IS NULL);

-- 校验 nx_dis_driver_duty 明细应为空
SELECT nx_ddd_id, nx_ddd_driver_user_id, nx_ddd_duty_date
FROM nx_dis_driver_duty
WHERE nx_ddd_distributer_id = @dis_id;

SELECT 'reset_dis160 done (autocommit)' AS status, NOW() AS finished_at;

-- =============================================================================
-- E. 若 duty/on 仍卡住：先解除 Navicat 未提交事务（在**同一连接**执行 ROLLBACK 或 KILL 长 Sleep 连接）
-- =============================================================================
-- SHOW PROCESSLIST;
-- SELECT trx_id, trx_state, trx_started, trx_mysql_thread_id FROM information_schema.innodb_trx;
-- ROLLBACK;
-- KILL <blocking_conn_id>;

-- =============================================================================
-- D. 清完后小程序侧
-- =============================================================================
-- 1. 重新打开 Today 派车 → 应只剩「建议派车 / 待分配」，无已确认/装车
-- 2. 给测试司机重新设「上岗/可派」（B.0 已删 nx_dis_driver_duty）
-- 3. 从「编辑路线 → 提交」重新走一遍
