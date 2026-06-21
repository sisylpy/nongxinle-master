-- =============================================================================
-- 配送商路线派单 Phase 2b-1：配送批次 + 可执行性状态
--
-- 前置：upgrade_nx_dis_route_schedule_phase2.sql
-- =============================================================================

ALTER TABLE nx_dis_route_plan
    ADD COLUMN nx_drp_dispatch_batch VARCHAR(16) NULL
        COMMENT 'MORNING|AFTERNOON|ADHOC' AFTER nx_drp_schedule_status;

ALTER TABLE nx_dis_route_plan
    ADD COLUMN nx_drp_batch_start_at DATETIME NULL
        COMMENT '批次开始' AFTER nx_drp_dispatch_batch;

ALTER TABLE nx_dis_route_plan
    ADD COLUMN nx_drp_batch_end_at DATETIME NULL
        COMMENT '批次结束' AFTER nx_drp_batch_start_at;

ALTER TABLE nx_dis_route_plan
    ADD COLUMN nx_drp_default_depart_at DATETIME NULL
        COMMENT '批次默认出发' AFTER nx_drp_batch_end_at;

ALTER TABLE nx_dis_route_plan
    ADD COLUMN nx_drp_feasibility_status VARCHAR(32) NULL
        COMMENT 'FEASIBLE|HAS_WAIT|HAS_LATE|INFEASIBLE|DRIVER_TOO_LATE|NO_AVAILABLE_DRIVER'
        AFTER nx_drp_default_depart_at;

ALTER TABLE nx_dis_driver_route
    ADD COLUMN nx_ddr_dispatch_eligible TINYINT NOT NULL DEFAULT 1
        COMMENT '1=适合本批次 0=不适合' AFTER nx_ddr_schedule_status;

ALTER TABLE nx_dis_driver_route
    ADD COLUMN nx_ddr_ineligible_reason VARCHAR(32) NULL
        COMMENT 'DRIVER_CHECKIN_TOO_LATE 等' AFTER nx_ddr_dispatch_eligible;

ALTER TABLE nx_dis_driver_route
    ADD COLUMN nx_ddr_feasibility_status VARCHAR(32) NULL
        COMMENT 'FEASIBLE|HAS_WAIT|HAS_LATE|INFEASIBLE|DRIVER_TOO_LATE|IDLE'
        AFTER nx_ddr_ineligible_reason;
