-- Phase 3a.1：shipment_task 承担 delivery stop 执行字段（route_stop 退出新主链）
-- 前置：upgrade_nx_dis_route_dispatch_phase2b5.sql

ALTER TABLE nx_dis_shipment_task
    ADD COLUMN nx_dst_driver_route_id INT NULL
        COMMENT '所属司机趟次 nx_dis_driver_route.nx_ddr_id' AFTER nx_dst_plan_id,
    ADD COLUMN nx_dst_route_seq INT NULL
        COMMENT '司机趟内停靠顺序（展示/排程）' AFTER nx_dst_manual_stop_seq,
    ADD COLUMN nx_dst_leg_distance_m BIGINT NULL DEFAULT 0
        COMMENT '上一站到本站的 leg 距离米' AFTER nx_dst_route_seq,
    ADD COLUMN nx_dst_leg_duration_s BIGINT NULL DEFAULT 0
        COMMENT '上一站到本站的 leg 秒数' AFTER nx_dst_leg_distance_m,
    ADD COLUMN nx_dst_planned_arrival_at DATETIME NULL
        COMMENT '排程预计到达' AFTER nx_dst_leg_duration_s,
    ADD COLUMN nx_dst_planned_service_start_at DATETIME NULL
        COMMENT '排程预计开始卸货' AFTER nx_dst_planned_arrival_at,
    ADD COLUMN nx_dst_planned_departure_at DATETIME NULL
        COMMENT '排程预计离站' AFTER nx_dst_planned_service_start_at,
    ADD COLUMN nx_dst_wait_minutes INT NULL DEFAULT 0
        COMMENT '早到等待分钟' AFTER nx_dst_planned_departure_at,
    ADD COLUMN nx_dst_late_minutes INT NULL DEFAULT 0
        COMMENT '迟到分钟' AFTER nx_dst_wait_minutes,
    ADD COLUMN nx_dst_time_window_status VARCHAR(32) NULL
        COMMENT 'OK|EARLY_WAIT|LATE|NO_WINDOW' AFTER nx_dst_late_minutes,
    ADD COLUMN nx_dst_stop_status VARCHAR(32) NULL
        COMMENT '执行停靠状态；CANCELLED 不计入' AFTER nx_dst_time_window_status;

CREATE INDEX idx_nx_dst_driver_route ON nx_dis_shipment_task (nx_dst_driver_route_id);
