-- =============================================================================
-- disId=160 派单运行数据清空（手动在 Native/Navicat 测试库执行）
--
-- 范围：仅 nx_dis_* 派单表，不动 nx_department_orders（订单你自己删）
-- 不动：nx_department 客户、nx_distributer_user 司机、商品/供货商等基础资料
--
-- 当前测试订单（你自己删）：200244–200250
-- =============================================================================

SELECT DATABASE() AS current_db;
SET @dis_id := 160;

-- ---------------------------------------------------------------------------
-- 1. 删除前预览
-- ---------------------------------------------------------------------------
SELECT 'nx_dis_route_plan' AS tbl, COUNT(*) AS cnt
FROM nx_dis_route_plan WHERE nx_drp_distributer_id = @dis_id
UNION ALL
SELECT 'nx_dis_driver_route', COUNT(*)
FROM nx_dis_driver_route dr
         JOIN nx_dis_route_plan p ON p.nx_drp_id = dr.nx_ddr_plan_id
WHERE p.nx_drp_distributer_id = @dis_id
UNION ALL
SELECT 'nx_dis_shipment_task', COUNT(*)
FROM nx_dis_shipment_task WHERE nx_dst_distributer_id = @dis_id
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
FROM nx_dis_driver_duty WHERE nx_ddd_distributer_id = @dis_id;

-- ---------------------------------------------------------------------------
-- 2. 删除（先子表后主表；全程限定 disId=160）
-- ---------------------------------------------------------------------------
START TRANSACTION;

-- 2.1 站点-订单快照（legacy，Phase 1.5c 后少用）
DELETE so
FROM nx_dis_route_stop_order so
         JOIN nx_dis_route_stop s ON s.nx_drs_id = so.nx_drso_stop_id
         LEFT JOIN nx_dis_driver_route dr ON dr.nx_ddr_id = s.nx_drs_driver_route_id
         LEFT JOIN nx_dis_route_plan p ON p.nx_drp_id = dr.nx_ddr_plan_id AND p.nx_drp_distributer_id = @dis_id
         LEFT JOIN nx_dis_shipment_task t ON t.nx_dst_id = s.nx_drs_shipment_task_id AND t.nx_dst_distributer_id = @dis_id
WHERE p.nx_drp_id IS NOT NULL OR t.nx_dst_id IS NOT NULL;

-- 2.2 未分派站点-订单关联
DELETE uo
FROM nx_dis_route_unassigned_stop_order uo
         JOIN nx_dis_route_unassigned_stop us ON us.nx_drus_id = uo.nx_druo_unassigned_stop_id
         JOIN nx_dis_route_plan p ON p.nx_drp_id = us.nx_drus_plan_id
WHERE p.nx_drp_distributer_id = @dis_id;

-- 2.3 装车任务明细（关联 live/history order id，随 task 清）
DELETE ti
FROM nx_dis_shipment_task_item ti
         JOIN nx_dis_shipment_task t ON t.nx_dst_id = ti.nx_dsti_task_id
WHERE t.nx_dst_distributer_id = @dis_id;

-- 2.4 路线站点
DELETE s
FROM nx_dis_route_stop s
         LEFT JOIN nx_dis_driver_route dr ON dr.nx_ddr_id = s.nx_drs_driver_route_id
         LEFT JOIN nx_dis_route_plan p ON p.nx_drp_id = dr.nx_ddr_plan_id AND p.nx_drp_distributer_id = @dis_id
         LEFT JOIN nx_dis_shipment_task t ON t.nx_dst_id = s.nx_drs_shipment_task_id AND t.nx_dst_distributer_id = @dis_id
WHERE p.nx_drp_id IS NOT NULL OR t.nx_dst_id IS NOT NULL;

-- 2.5 装车/配送任务
DELETE FROM nx_dis_shipment_task
WHERE nx_dst_distributer_id = @dis_id;

-- 2.6 司机路线
DELETE dr
FROM nx_dis_driver_route dr
         JOIN nx_dis_route_plan p ON p.nx_drp_id = dr.nx_ddr_plan_id
WHERE p.nx_drp_distributer_id = @dis_id;

-- 2.7 未分派站点
DELETE us
FROM nx_dis_route_unassigned_stop us
         JOIN nx_dis_route_plan p ON p.nx_drp_id = us.nx_drus_plan_id
WHERE p.nx_drp_distributer_id = @dis_id;

-- 2.8 路线计划（主表）
DELETE FROM nx_dis_route_plan
WHERE nx_drp_distributer_id = @dis_id;

-- 2.9 司机当日可派状态（运行态，非司机账号）
DELETE FROM nx_dis_driver_duty
WHERE nx_ddd_distributer_id = @dis_id;

-- ---------------------------------------------------------------------------
-- 3. 删除后校验（应全 0）
-- ---------------------------------------------------------------------------
SELECT 'nx_dis_route_plan' AS tbl, COUNT(*) AS cnt
FROM nx_dis_route_plan WHERE nx_drp_distributer_id = @dis_id
UNION ALL
SELECT 'nx_dis_shipment_task', COUNT(*)
FROM nx_dis_shipment_task WHERE nx_dst_distributer_id = @dis_id
UNION ALL
SELECT 'nx_dis_driver_duty', COUNT(*)
FROM nx_dis_driver_duty WHERE nx_ddd_distributer_id = @dis_id;

-- 确认无误后：
COMMIT;
-- 不对则：
-- ROLLBACK;
