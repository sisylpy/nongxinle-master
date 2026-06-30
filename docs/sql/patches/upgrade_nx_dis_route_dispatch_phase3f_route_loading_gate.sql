-- Phase 3f：路线级「进入装车 / 撤销回今日派单」门禁
-- 前置：upgrade_nx_dis_route_dispatch_phase3d.sql

ALTER TABLE nx_dis_driver_route
    ADD COLUMN nx_ddr_loading_entered_at DATETIME NULL
        COMMENT '老板确认让司机进入装车流程的时间；NULL=仍在今日派单页' AFTER nx_ddr_depart_remark,
    ADD COLUMN nx_ddr_loading_entered_operator_user_id INT NULL
        COMMENT '进入装车操作人' AFTER nx_ddr_loading_entered_at;

-- 已有装车中但未出发的路线：视为已进入装车，避免升级后退回今日派单页
UPDATE nx_dis_driver_route
SET nx_ddr_loading_entered_at = COALESCE(nx_ddr_loading_entered_at, NOW())
WHERE nx_ddr_loading_entered_at IS NULL
  AND nx_ddr_actual_depart_at IS NULL
  AND nx_ddr_route_status IN ('LOADING', 'READY_TO_DEPART');
