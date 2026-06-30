-- =============================================================================
-- 路线派单 Phase 3a：清理旧沙盘 SIMULATED 残留（simulate 时代提前落库的数据）
--
-- 背景：Phase 3a 起 GET /sandbox/today 纯内存计算，不再写 plan/task/stop。
--       旧 POST /simulate 产生的 SIMULATED / UNASSIGNED task 会干扰 open_key，
--       并在 invalidStops 里报 STALE_SANDBOX_CACHE。
--
-- 本脚本删除：
--   · 未确认（manual_locked=0）的 SIMULATED / UNASSIGNED shipment_task
--   · 其 task_item（仅无 bill_id / history_order_id 的行）
--   · 关联 route_stop、空 driver_route、无 task 挂靠的 SIMULATED plan
--   · 同 plan 下 legacy unassigned_stop / stop_order 快照（若表存在）
--
-- 不删除（执行记录）：
--   · manual_locked = 1
--   · status ∈ ASSIGNED / READY_TO_GO / IN_DELIVERY / DELIVERED
--   · task_item 已挂 bill_id 或 history_order_id
--
-- ⚠️  不可恢复。执行前请确认库名 / 备份。
--
-- 用法：
--   1. 设置 @cleanup_dis_id（NULL = 全部配送商）
--   2. 运行 §1 预览，核对行数
--   3. 确认后运行 §2（事务内 DELETE）
--
-- 相关文档：docs/nxPlatform/Route-Dispatch-Sandbox-Design.md §0.1
-- =============================================================================

-- §0 范围：指定配送商；NULL = 全部
SET @cleanup_dis_id = NULL;   -- 演示环境示例：SET @cleanup_dis_id = 160;
SET @cleanup_dep_name = NULL; -- 查某店：SET @cleanup_dep_name = '演示一店';

-- =============================================================================
-- §1 清理前预览（旧 simulate 落库残留）
-- =============================================================================

SELECT 'preview_stale_task' AS step, COUNT(*) AS cnt
FROM nx_dis_shipment_task t
WHERE (@cleanup_dis_id IS NULL OR t.nx_dst_distributer_id = @cleanup_dis_id)
  AND t.nx_dst_status IN ('SIMULATED', 'UNASSIGNED')
  AND IFNULL(t.nx_dst_manual_locked, 0) = 0
  AND NOT EXISTS (
      SELECT 1 FROM nx_dis_shipment_task_item i
      WHERE i.nx_dsti_task_id = t.nx_dst_id
        AND (i.nx_dsti_bill_id IS NOT NULL OR i.nx_dsti_history_order_id IS NOT NULL)
  );

SELECT 'preview_stale_item' AS step, COUNT(*) AS cnt
FROM nx_dis_shipment_task_item i
INNER JOIN nx_dis_shipment_task t ON t.nx_dst_id = i.nx_dsti_task_id
WHERE (@cleanup_dis_id IS NULL OR t.nx_dst_distributer_id = @cleanup_dis_id)
  AND t.nx_dst_status IN ('SIMULATED', 'UNASSIGNED')
  AND IFNULL(t.nx_dst_manual_locked, 0) = 0
  AND i.nx_dsti_bill_id IS NULL
  AND i.nx_dsti_history_order_id IS NULL;

SELECT 'preview_stale_stop' AS step, COUNT(*) AS cnt
FROM nx_dis_route_stop s
INNER JOIN nx_dis_shipment_task t ON t.nx_dst_id = s.nx_drs_shipment_task_id
WHERE (@cleanup_dis_id IS NULL OR t.nx_dst_distributer_id = @cleanup_dis_id)
  AND t.nx_dst_status IN ('SIMULATED', 'UNASSIGNED')
  AND IFNULL(t.nx_dst_manual_locked, 0) = 0;

SELECT 'preview_simulated_plan' AS step, COUNT(*) AS cnt
FROM nx_dis_route_plan p
WHERE p.nx_drp_status = 'SIMULATED'
  AND (@cleanup_dis_id IS NULL OR p.nx_drp_distributer_id = @cleanup_dis_id);

-- -----------------------------------------------------------------------------
-- §1b 沙盘仍显示某店？Phase 3a 主链读 live order，不是读 SIMULATED task。
--     preview_stale_task=0 但页面仍有站点 → 通常是 nx_department_orders 仍有 do_status<3
-- -----------------------------------------------------------------------------

SELECT 'preview_eligible_live_order' AS step, COUNT(*) AS cnt
FROM nx_department_orders dor
JOIN nx_department ds ON ds.nx_department_id = dor.nx_DO_department_father_id
WHERE (@cleanup_dis_id IS NULL OR dor.nx_DO_distributer_id = @cleanup_dis_id)
  AND (@cleanup_dis_id IS NULL OR ds.nx_department_dis_id = @cleanup_dis_id)
  AND dor.nx_DO_collaborative_nx_dis_id = -1
  AND dor.nx_DO_status < 3
  AND dor.nx_DO_gb_department_id = -1
  AND dor.nx_DO_nx_comm_restraunt_id = -1
  AND (@cleanup_dep_name IS NULL OR ds.nx_department_name = @cleanup_dep_name);

SELECT 'preview_eligible_by_dep' AS step,
       ds.nx_department_id   AS dep_father_id,
       ds.nx_department_name AS dep_name,
       COUNT(*)              AS order_cnt
FROM nx_department_orders dor
JOIN nx_department ds ON ds.nx_department_id = dor.nx_DO_department_father_id
WHERE (@cleanup_dis_id IS NULL OR dor.nx_DO_distributer_id = @cleanup_dis_id)
  AND (@cleanup_dis_id IS NULL OR ds.nx_department_dis_id = @cleanup_dis_id)
  AND dor.nx_DO_collaborative_nx_dis_id = -1
  AND dor.nx_DO_status < 3
  AND dor.nx_DO_gb_department_id = -1
  AND dor.nx_DO_nx_comm_restraunt_id = -1
  AND (@cleanup_dep_name IS NULL OR ds.nx_department_name = @cleanup_dep_name)
GROUP BY ds.nx_department_id, ds.nx_department_name
ORDER BY ds.nx_department_name;

SELECT 'preview_confirmed_task' AS step, t.nx_dst_id, t.nx_dst_status,
       t.nx_dst_manual_locked, t.nx_dst_dep_name
FROM nx_dis_shipment_task t
WHERE (@cleanup_dis_id IS NULL OR t.nx_dst_distributer_id = @cleanup_dis_id)
  AND (@cleanup_dep_name IS NULL OR t.nx_dst_dep_name = @cleanup_dep_name)
  AND t.nx_dst_status NOT IN ('CANCELLED', 'CLOSED')
ORDER BY t.nx_dst_id DESC
LIMIT 20;

-- =============================================================================
-- §2 执行清理（事务）— 仅删旧 simulate 落库，不碰 live order
-- =============================================================================
START TRANSACTION;

DROP TEMPORARY TABLE IF EXISTS tmp_stale_task_ids;
CREATE TEMPORARY TABLE tmp_stale_task_ids (
    task_id INT NOT NULL PRIMARY KEY
);

INSERT INTO tmp_stale_task_ids (task_id)
SELECT t.nx_dst_id
FROM nx_dis_shipment_task t
WHERE (@cleanup_dis_id IS NULL OR t.nx_dst_distributer_id = @cleanup_dis_id)
  AND t.nx_dst_status IN ('SIMULATED', 'UNASSIGNED')
  AND IFNULL(t.nx_dst_manual_locked, 0) = 0
  AND NOT EXISTS (
      SELECT 1 FROM nx_dis_shipment_task_item i
      WHERE i.nx_dsti_task_id = t.nx_dst_id
        AND (i.nx_dsti_bill_id IS NOT NULL OR i.nx_dsti_history_order_id IS NOT NULL)
  );

DROP TEMPORARY TABLE IF EXISTS tmp_stale_plan_ids;
CREATE TEMPORARY TABLE tmp_stale_plan_ids (
    plan_id INT NOT NULL PRIMARY KEY
);

INSERT INTO tmp_stale_plan_ids (plan_id)
SELECT DISTINCT p.nx_drp_id
FROM nx_dis_route_plan p
WHERE p.nx_drp_status = 'SIMULATED'
  AND (@cleanup_dis_id IS NULL OR p.nx_drp_distributer_id = @cleanup_dis_id);

-- 2.1 legacy stop_order（表存在时）
DELETE drso
FROM nx_dis_route_stop_order drso
INNER JOIN nx_dis_route_stop s ON s.nx_drs_id = drso.nx_drso_stop_id
INNER JOIN tmp_stale_task_ids st ON st.task_id = s.nx_drs_shipment_task_id;

-- 2.2 route_stop
DELETE s
FROM nx_dis_route_stop s
INNER JOIN tmp_stale_task_ids st ON st.task_id = s.nx_drs_shipment_task_id;

-- 2.3 无 task 挂靠、但属于 SIMULATED plan 的 orphan stop（极旧 simulate 残留）
DELETE s
FROM nx_dis_route_stop s
INNER JOIN nx_dis_driver_route r ON r.nx_ddr_id = s.nx_drs_driver_route_id
INNER JOIN tmp_stale_plan_ids sp ON sp.plan_id = r.nx_ddr_plan_id
WHERE s.nx_drs_shipment_task_id IS NULL;

-- 2.4 task_item（无 bill / history）
DELETE i
FROM nx_dis_shipment_task_item i
INNER JOIN tmp_stale_task_ids st ON st.task_id = i.nx_dsti_task_id
WHERE i.nx_dsti_bill_id IS NULL
  AND i.nx_dsti_history_order_id IS NULL;

-- 2.5 shipment_task
DELETE t
FROM nx_dis_shipment_task t
INNER JOIN tmp_stale_task_ids st ON st.task_id = t.nx_dst_id;

-- 2.6 legacy unassigned_stop_order + unassigned_stop
DELETE druo
FROM nx_dis_route_unassigned_stop_order druo
INNER JOIN nx_dis_route_unassigned_stop u ON u.nx_drus_id = druo.nx_druo_unassigned_stop_id
INNER JOIN tmp_stale_plan_ids sp ON sp.plan_id = u.nx_drus_plan_id;

DELETE u
FROM nx_dis_route_unassigned_stop u
INNER JOIN tmp_stale_plan_ids sp ON sp.plan_id = u.nx_drus_plan_id;

-- 2.7 空 driver_route（该 plan 下已无 stop）
DELETE r
FROM nx_dis_driver_route r
INNER JOIN tmp_stale_plan_ids sp ON sp.plan_id = r.nx_ddr_plan_id
WHERE NOT EXISTS (
    SELECT 1 FROM nx_dis_route_stop s WHERE s.nx_drs_driver_route_id = r.nx_ddr_id
);

-- 2.8 SIMULATED plan：已无 task 挂靠、且无 ASSIGNED/READY 兄弟 plan 冲突时可删
DELETE p
FROM nx_dis_route_plan p
INNER JOIN tmp_stale_plan_ids sp ON sp.plan_id = p.nx_drp_id
WHERE NOT EXISTS (
    SELECT 1 FROM nx_dis_shipment_task t WHERE t.nx_dst_plan_id = p.nx_drp_id
)
AND NOT EXISTS (
    SELECT 1 FROM nx_dis_driver_route r
    INNER JOIN nx_dis_route_stop s ON s.nx_drs_driver_route_id = r.nx_ddr_id
    WHERE r.nx_ddr_plan_id = p.nx_drp_id
);

COMMIT;

-- =============================================================================
-- §3 清理后验收
-- =============================================================================
SELECT 'after_stale_task' AS step, COUNT(*) AS cnt
FROM nx_dis_shipment_task t
WHERE (@cleanup_dis_id IS NULL OR t.nx_dst_distributer_id = @cleanup_dis_id)
  AND t.nx_dst_status IN ('SIMULATED', 'UNASSIGNED')
  AND IFNULL(t.nx_dst_manual_locked, 0) = 0;

SELECT 'after_simulated_plan' AS step, COUNT(*) AS cnt
FROM nx_dis_route_plan p
WHERE p.nx_drp_status = 'SIMULATED'
  AND (@cleanup_dis_id IS NULL OR p.nx_drp_distributer_id = @cleanup_dis_id);

SELECT 'after_open_key_dup' AS step, nx_dst_open_key, COUNT(*) AS c
FROM nx_dis_shipment_task
WHERE nx_dst_open_key IS NOT NULL
  AND (@cleanup_dis_id IS NULL OR nx_dst_distributer_id = @cleanup_dis_id)
GROUP BY nx_dst_open_key
HAVING c > 1;

-- =============================================================================
-- §4 可选：让指定客户从沙盘消失（测试/演示店）
--
-- Phase 3a 沙盘 = 实时读 nx_department_orders（do_status < 3）。
-- 要页面上不再出现「演示一店」，必须让该店不再有 eligible live order，
-- 或删掉/完成其未确认的派车执行记录。
--
-- 方式 A（推荐测试）：把该店未出单订单标为已完成 do_status=3（不进沙盘，订单仍在库）
-- 方式 B：删除未确认的 ASSIGNED/SIMULATED 执行记录（仍可能有 live order 重新出现）
--
-- ⚠️  仅用于联调/演示；生产勿随意改订单状态。
-- =============================================================================

-- §4 预览：将被移出沙盘的 live order
SELECT 'preview_orders_to_complete' AS step,
       dor.nx_department_orders_id AS order_id,
       ds.nx_department_name     AS dep_name,
       dor.nx_DO_goods_name      AS goods_name,
       dor.nx_DO_status          AS order_status,
       dor.nx_DO_arrive_date     AS arrive_date
FROM nx_department_orders dor
JOIN nx_department ds ON ds.nx_department_id = dor.nx_DO_department_father_id
WHERE dor.nx_DO_distributer_id = @cleanup_dis_id
  AND ds.nx_department_dis_id = @cleanup_dis_id
  AND dor.nx_DO_collaborative_nx_dis_id = -1
  AND dor.nx_DO_status < 3
  AND dor.nx_DO_gb_department_id = -1
  AND dor.nx_DO_nx_comm_restraunt_id = -1
  AND (@cleanup_dep_name IS NULL OR ds.nx_department_name = @cleanup_dep_name);

-- §4 执行（确认 §4 预览无误后，取消下面注释再跑）

-- START TRANSACTION;
--
-- UPDATE nx_department_orders dor
-- INNER JOIN nx_department ds ON ds.nx_department_id = dor.nx_DO_department_father_id
-- SET dor.nx_DO_status = 3
-- WHERE dor.nx_DO_distributer_id = @cleanup_dis_id
--   AND ds.nx_department_dis_id = @cleanup_dis_id
--   AND dor.nx_DO_collaborative_nx_dis_id = -1
--   AND dor.nx_DO_status < 3
--   AND dor.nx_DO_gb_department_id = -1
--   AND dor.nx_DO_nx_comm_restraunt_id = -1
--   AND (@cleanup_dep_name IS NULL OR ds.nx_department_name = @cleanup_dep_name);
--
-- -- 未确认执行记录一并清掉（manual_locked=0 的 ASSIGNED/SIMULATED）
-- DELETE drso FROM nx_dis_route_stop_order drso
-- INNER JOIN nx_dis_route_stop s ON s.nx_drs_id = drso.nx_drso_stop_id
-- INNER JOIN nx_dis_shipment_task t ON t.nx_dst_id = s.nx_drs_shipment_task_id
-- WHERE t.nx_dst_distributer_id = @cleanup_dis_id
--   AND IFNULL(t.nx_dst_manual_locked, 0) = 0
--   AND (@cleanup_dep_name IS NULL OR t.nx_dst_dep_name = @cleanup_dep_name)
--   AND NOT EXISTS (
--       SELECT 1 FROM nx_dis_shipment_task_item i
--       WHERE i.nx_dsti_task_id = t.nx_dst_id
--         AND (i.nx_dsti_bill_id IS NOT NULL OR i.nx_dsti_history_order_id IS NOT NULL)
--   );
--
-- DELETE s FROM nx_dis_route_stop s
-- INNER JOIN nx_dis_shipment_task t ON t.nx_dst_id = s.nx_drs_shipment_task_id
-- WHERE t.nx_dst_distributer_id = @cleanup_dis_id
--   AND IFNULL(t.nx_dst_manual_locked, 0) = 0
--   AND (@cleanup_dep_name IS NULL OR t.nx_dst_dep_name = @cleanup_dep_name);
--
-- DELETE i FROM nx_dis_shipment_task_item i
-- INNER JOIN nx_dis_shipment_task t ON t.nx_dst_id = i.nx_dsti_task_id
-- WHERE t.nx_dst_distributer_id = @cleanup_dis_id
--   AND IFNULL(t.nx_dst_manual_locked, 0) = 0
--   AND (@cleanup_dep_name IS NULL OR t.nx_dst_dep_name = @cleanup_dep_name)
--   AND i.nx_dsti_bill_id IS NULL AND i.nx_dsti_history_order_id IS NULL;
--
-- DELETE t FROM nx_dis_shipment_task t
-- WHERE t.nx_dst_distributer_id = @cleanup_dis_id
--   AND IFNULL(t.nx_dst_manual_locked, 0) = 0
--   AND t.nx_dst_status IN ('SIMULATED', 'UNASSIGNED', 'ASSIGNED')
--   AND (@cleanup_dep_name IS NULL OR t.nx_dst_dep_name = @cleanup_dep_name)
--   AND NOT EXISTS (
--       SELECT 1 FROM nx_dis_shipment_task_item i
--       WHERE i.nx_dsti_task_id = t.nx_dst_id
--         AND (i.nx_dsti_bill_id IS NOT NULL OR i.nx_dsti_history_order_id IS NOT NULL)
--   );
--
-- COMMIT;
--
-- SELECT 'after_eligible_live_order' AS step, COUNT(*) AS cnt
-- FROM nx_department_orders dor
-- JOIN nx_department ds ON ds.nx_department_id = dor.nx_DO_department_father_id
-- WHERE dor.nx_DO_distributer_id = @cleanup_dis_id
--   AND dor.nx_DO_status < 3
--   AND (@cleanup_dep_name IS NULL OR ds.nx_department_name = @cleanup_dep_name);
