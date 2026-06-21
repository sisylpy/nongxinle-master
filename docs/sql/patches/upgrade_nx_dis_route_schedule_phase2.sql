-- =============================================================================
-- 配送商路线派单 Phase 2a：固定顺序配送时间窗排程结果字段
--
-- 前置：upgrade_nx_dis_route_dispatch_phase1.sql / phase1_5a.sql
-- 原则：客户长期配置仍在 nx_department；本 patch 仅存当次 plan 计算快照与 ETA
-- =============================================================================

ALTER TABLE nx_dis_route_stop
    ADD COLUMN nx_drs_earliest_delivery_time_s INT NULL
        COMMENT '排程快照：最早送达(秒，距0点)' AFTER nx_drs_leg_duration_s;

ALTER TABLE nx_dis_route_stop
    ADD COLUMN nx_drs_latest_delivery_time_s INT NULL
        COMMENT '排程快照：最晚送达(秒，距0点)' AFTER nx_drs_earliest_delivery_time_s;

ALTER TABLE nx_dis_route_stop
    ADD COLUMN nx_drs_service_minutes INT NULL
        COMMENT '排程快照：卸货分钟' AFTER nx_drs_latest_delivery_time_s;

ALTER TABLE nx_dis_route_stop
    ADD COLUMN nx_drs_planned_arrival_at DATETIME NULL
        COMMENT '预计到达' AFTER nx_drs_service_minutes;

ALTER TABLE nx_dis_route_stop
    ADD COLUMN nx_drs_planned_service_start_at DATETIME NULL
        COMMENT '预计开始卸货' AFTER nx_drs_planned_arrival_at;

ALTER TABLE nx_dis_route_stop
    ADD COLUMN nx_drs_planned_departure_at DATETIME NULL
        COMMENT '预计离开' AFTER nx_drs_planned_service_start_at;

ALTER TABLE nx_dis_route_stop
    ADD COLUMN nx_drs_wait_minutes INT NOT NULL DEFAULT 0
        COMMENT '早到等待分钟' AFTER nx_drs_planned_departure_at;

ALTER TABLE nx_dis_route_stop
    ADD COLUMN nx_drs_late_minutes INT NOT NULL DEFAULT 0
        COMMENT '迟到分钟' AFTER nx_drs_wait_minutes;

ALTER TABLE nx_dis_route_stop
    ADD COLUMN nx_drs_time_window_status VARCHAR(16) NULL
        COMMENT 'OK|EARLY_WAIT|LATE|NO_WINDOW' AFTER nx_drs_late_minutes;

ALTER TABLE nx_dis_driver_route
    ADD COLUMN nx_ddr_planned_depart_at DATETIME NULL
        COMMENT '建议出发' AFTER nx_ddr_stop_count;

ALTER TABLE nx_dis_driver_route
    ADD COLUMN nx_ddr_planned_finish_at DATETIME NULL
        COMMENT '预计完成(末站离开)' AFTER nx_ddr_planned_depart_at;

ALTER TABLE nx_dis_driver_route
    ADD COLUMN nx_ddr_total_service_minutes INT NOT NULL DEFAULT 0
        COMMENT '卸货总分钟' AFTER nx_ddr_planned_finish_at;

ALTER TABLE nx_dis_driver_route
    ADD COLUMN nx_ddr_total_wait_minutes INT NOT NULL DEFAULT 0
        COMMENT '等待总分钟' AFTER nx_ddr_total_service_minutes;

ALTER TABLE nx_dis_driver_route
    ADD COLUMN nx_ddr_total_late_minutes INT NOT NULL DEFAULT 0
        COMMENT '迟到总分钟' AFTER nx_ddr_total_wait_minutes;

ALTER TABLE nx_dis_driver_route
    ADD COLUMN nx_ddr_schedule_status VARCHAR(16) NULL
        COMMENT 'OK|HAS_LATE|NO_WINDOW' AFTER nx_ddr_total_late_minutes;

ALTER TABLE nx_dis_route_plan
    ADD COLUMN nx_drp_planned_start_at DATETIME NULL
        COMMENT '全队最早出发' AFTER nx_drp_total_duration_s;

ALTER TABLE nx_dis_route_plan
    ADD COLUMN nx_drp_planned_end_at DATETIME NULL
        COMMENT '全队最晚完成' AFTER nx_drp_planned_start_at;

ALTER TABLE nx_dis_route_plan
    ADD COLUMN nx_drp_total_wait_minutes INT NOT NULL DEFAULT 0
        COMMENT '全队等待总分钟' AFTER nx_drp_planned_end_at;

ALTER TABLE nx_dis_route_plan
    ADD COLUMN nx_drp_total_late_minutes INT NOT NULL DEFAULT 0
        COMMENT '全队迟到总分钟' AFTER nx_drp_total_wait_minutes;

ALTER TABLE nx_dis_route_plan
    ADD COLUMN nx_drp_schedule_status VARCHAR(16) NULL
        COMMENT 'OK|HAS_LATE|NO_WINDOW' AFTER nx_drp_total_late_minutes;
