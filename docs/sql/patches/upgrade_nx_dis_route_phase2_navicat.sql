-- =============================================================================
-- Navicat 一键执行：Phase 2a 排程 + Phase 2b-1 批次/可执行性
--
-- 用法：
--   1. Navicat 左侧双击选中 Tomcat 实际连接的库（nongxinle 或 chain_order）
--   2. 新建查询，整段粘贴，点「运行」
--   3. 若某条报 Duplicate column name，说明该列已有，跳过继续跑后面
-- =============================================================================

-- ---------- Phase 2a：排程字段 ----------

ALTER TABLE nx_dis_route_stop
    ADD COLUMN nx_drs_earliest_delivery_time_s INT NULL COMMENT '排程快照：最早送达(秒，距0点)' AFTER nx_drs_leg_duration_s;

ALTER TABLE nx_dis_route_stop
    ADD COLUMN nx_drs_latest_delivery_time_s INT NULL COMMENT '排程快照：最晚送达(秒，距0点)' AFTER nx_drs_earliest_delivery_time_s;

ALTER TABLE nx_dis_route_stop
    ADD COLUMN nx_drs_service_minutes INT NULL COMMENT '排程快照：卸货分钟' AFTER nx_drs_latest_delivery_time_s;

ALTER TABLE nx_dis_route_stop
    ADD COLUMN nx_drs_planned_arrival_at DATETIME NULL COMMENT '预计到达' AFTER nx_drs_service_minutes;

ALTER TABLE nx_dis_route_stop
    ADD COLUMN nx_drs_planned_service_start_at DATETIME NULL COMMENT '预计开始卸货' AFTER nx_drs_planned_arrival_at;

ALTER TABLE nx_dis_route_stop
    ADD COLUMN nx_drs_planned_departure_at DATETIME NULL COMMENT '预计离开' AFTER nx_drs_planned_service_start_at;

ALTER TABLE nx_dis_route_stop
    ADD COLUMN nx_drs_wait_minutes INT NOT NULL DEFAULT 0 COMMENT '早到等待分钟' AFTER nx_drs_planned_departure_at;

ALTER TABLE nx_dis_route_stop
    ADD COLUMN nx_drs_late_minutes INT NOT NULL DEFAULT 0 COMMENT '迟到分钟' AFTER nx_drs_wait_minutes;

ALTER TABLE nx_dis_route_stop
    ADD COLUMN nx_drs_time_window_status VARCHAR(16) NULL COMMENT 'OK|EARLY_WAIT|LATE|NO_WINDOW' AFTER nx_drs_late_minutes;

ALTER TABLE nx_dis_driver_route
    ADD COLUMN nx_ddr_planned_depart_at DATETIME NULL COMMENT '建议出发' AFTER nx_ddr_stop_count;

ALTER TABLE nx_dis_driver_route
    ADD COLUMN nx_ddr_planned_finish_at DATETIME NULL COMMENT '预计完成(末站离开)' AFTER nx_ddr_planned_depart_at;

ALTER TABLE nx_dis_driver_route
    ADD COLUMN nx_ddr_total_service_minutes INT NOT NULL DEFAULT 0 COMMENT '卸货总分钟' AFTER nx_ddr_planned_finish_at;

ALTER TABLE nx_dis_driver_route
    ADD COLUMN nx_ddr_total_wait_minutes INT NOT NULL DEFAULT 0 COMMENT '等待总分钟' AFTER nx_ddr_total_service_minutes;

ALTER TABLE nx_dis_driver_route
    ADD COLUMN nx_ddr_total_late_minutes INT NOT NULL DEFAULT 0 COMMENT '迟到总分钟' AFTER nx_ddr_total_wait_minutes;

ALTER TABLE nx_dis_driver_route
    ADD COLUMN nx_ddr_schedule_status VARCHAR(16) NULL COMMENT 'OK|HAS_LATE|NO_WINDOW' AFTER nx_ddr_total_late_minutes;

ALTER TABLE nx_dis_route_plan
    ADD COLUMN nx_drp_planned_start_at DATETIME NULL COMMENT '全队最早出发' AFTER nx_drp_total_duration_s;

ALTER TABLE nx_dis_route_plan
    ADD COLUMN nx_drp_planned_end_at DATETIME NULL COMMENT '全队最晚完成' AFTER nx_drp_planned_start_at;

ALTER TABLE nx_dis_route_plan
    ADD COLUMN nx_drp_total_wait_minutes INT NOT NULL DEFAULT 0 COMMENT '全队等待总分钟' AFTER nx_drp_planned_end_at;

ALTER TABLE nx_dis_route_plan
    ADD COLUMN nx_drp_total_late_minutes INT NOT NULL DEFAULT 0 COMMENT '全队迟到总分钟' AFTER nx_drp_total_wait_minutes;

ALTER TABLE nx_dis_route_plan
    ADD COLUMN nx_drp_schedule_status VARCHAR(16) NULL COMMENT 'OK|HAS_LATE|NO_WINDOW' AFTER nx_drp_total_late_minutes;

-- ---------- Phase 2b-1：批次 + 可执行性 ----------

ALTER TABLE nx_dis_route_plan
    ADD COLUMN nx_drp_dispatch_batch VARCHAR(16) NULL COMMENT 'MORNING|AFTERNOON|ADHOC' AFTER nx_drp_schedule_status;

ALTER TABLE nx_dis_route_plan
    ADD COLUMN nx_drp_batch_start_at DATETIME NULL COMMENT '批次开始' AFTER nx_drp_dispatch_batch;

ALTER TABLE nx_dis_route_plan
    ADD COLUMN nx_drp_batch_end_at DATETIME NULL COMMENT '批次结束' AFTER nx_drp_batch_start_at;

ALTER TABLE nx_dis_route_plan
    ADD COLUMN nx_drp_default_depart_at DATETIME NULL COMMENT '批次默认出发' AFTER nx_drp_batch_end_at;

ALTER TABLE nx_dis_route_plan
    ADD COLUMN nx_drp_feasibility_status VARCHAR(32) NULL COMMENT 'FEASIBLE|HAS_LATE|INFEASIBLE 等' AFTER nx_drp_default_depart_at;

ALTER TABLE nx_dis_driver_route
    ADD COLUMN nx_ddr_dispatch_eligible TINYINT NOT NULL DEFAULT 1 COMMENT '1=适合本批次 0=不适合' AFTER nx_ddr_schedule_status;

ALTER TABLE nx_dis_driver_route
    ADD COLUMN nx_ddr_ineligible_reason VARCHAR(32) NULL COMMENT 'DRIVER_CHECKIN_TOO_LATE 等' AFTER nx_ddr_dispatch_eligible;

ALTER TABLE nx_dis_driver_route
    ADD COLUMN nx_ddr_feasibility_status VARCHAR(32) NULL COMMENT 'FEASIBLE|IDLE|DRIVER_TOO_LATE 等' AFTER nx_ddr_ineligible_reason;

-- ---------- 验证 ----------

SHOW COLUMNS FROM nx_dis_driver_route LIKE 'nx_ddr_dispatch%';
SHOW COLUMNS FROM nx_dis_driver_route LIKE 'nx_ddr_feasibility%';
SHOW COLUMNS FROM nx_dis_route_plan LIKE 'nx_drp_dispatch%';
SHOW COLUMNS FROM nx_dis_route_plan LIKE 'nx_drp_feasibility%';
