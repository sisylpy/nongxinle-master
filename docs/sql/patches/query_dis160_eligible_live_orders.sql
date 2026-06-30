-- =============================================================================
-- disId=160 派单 eligible 订单查询（手动在 Native/Navicat 执行，勿在生产库跑）
--
-- 口径与后端一致：
--   DisRouteEligibleOrderPolicy / NxDisRoutePlanDao.queryEligibleLiveOrderSnapshots
--   nx_DO_status < 3  （未出单/未进入 history 主链前的 live order）
--
-- 预期：测试环境约 7 条；以 COUNT 结果为准。
-- =============================================================================

-- 0. 确认当前库
SELECT DATABASE() AS current_db;

SET @dis_id := 160;
SET @route_date := DATE_FORMAT(CURDATE(), '%Y-%m-%d');  -- 今日路线日；可按需改

-- 1. 最简：disId=160 且 status < 3
SELECT COUNT(*) AS cnt_status_lt_3
FROM nx_department_orders
WHERE nx_DO_distributer_id = @dis_id
  AND nx_DO_status < 3;

SELECT
    dor.nx_department_orders_id AS order_id,
    dor.nx_DO_status            AS order_status,
    dor.nx_DO_department_father_id AS dep_father_id,
    ds.nx_department_name       AS dep_name,
    dor.nx_DO_goods_name,
    dor.nx_DO_quantity,
    dor.nx_DO_standard,
    dor.nx_DO_arrive_date,
    dor.nx_DO_arrive_only_date,
    dor.nx_DO_apply_date,
    dor.nx_DO_bill_id
FROM nx_department_orders dor
LEFT JOIN nx_department ds
       ON ds.nx_department_id = dor.nx_DO_department_father_id
WHERE dor.nx_DO_distributer_id = @dis_id
  AND dor.nx_DO_status < 3
ORDER BY dor.nx_DO_department_father_id, dor.nx_department_orders_id;

-- 2. 与沙盘 compute 完全一致的 eligible 口径（推荐对照用）
SELECT COUNT(*) AS cnt_sandbox_eligible
FROM nx_department_orders dor
         JOIN nx_department ds ON ds.nx_department_id = dor.nx_DO_department_father_id
WHERE dor.nx_DO_distributer_id = @dis_id
  AND ds.nx_department_dis_id = @dis_id
  AND dor.nx_DO_collaborative_nx_dis_id = -1
  AND dor.nx_DO_status < 3
  AND dor.nx_DO_gb_department_id = -1
  AND dor.nx_DO_nx_comm_restraunt_id = -1
  AND (
      dor.nx_DO_arrive_date = @route_date
      OR dor.nx_DO_arrive_only_date = @route_date
  );

SELECT
    dor.nx_department_orders_id AS order_id,
    dor.nx_DO_status            AS order_status,
    ds.nx_department_id         AS department_id,
    ds.nx_department_name       AS department_name,
    dor.nx_DO_goods_name,
    dor.nx_DO_quantity,
    dor.nx_DO_standard,
    dor.nx_DO_arrive_date,
    dor.nx_DO_arrive_only_date
FROM nx_department_orders dor
         JOIN nx_department ds ON ds.nx_department_id = dor.nx_DO_department_father_id
WHERE dor.nx_DO_distributer_id = @dis_id
  AND ds.nx_department_dis_id = @dis_id
  AND dor.nx_DO_collaborative_nx_dis_id = -1
  AND dor.nx_DO_status < 3
  AND dor.nx_DO_gb_department_id = -1
  AND dor.nx_DO_nx_comm_restraunt_id = -1
  AND (
      dor.nx_DO_arrive_date = @route_date
      OR dor.nx_DO_arrive_only_date = @route_date
  )
ORDER BY ds.nx_department_pinyin, dor.nx_department_orders_id;

-- 3. 若第 1 段约 7 条、第 2 段更少：说明部分订单 routeDate/GB/协作 口径未进沙盘
--    测试派单前优先看第 2 段；要放宽路线日可把 @route_date 改成订单上的 arrive 日期。
