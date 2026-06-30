-- Phase 3D：司机路线确认出发（route 级执行态）
-- 前置：upgrade_nx_dis_route_dispatch_phase3a1.sql

ALTER TABLE nx_dis_driver_route
    ADD COLUMN nx_ddr_route_status VARCHAR(32) NULL DEFAULT 'LOADING'
        COMMENT 'LOADING|READY_TO_DEPART|IN_DELIVERY|DELIVERED|CLOSED' AFTER nx_ddr_stop_count,
    ADD COLUMN nx_ddr_actual_depart_at DATETIME NULL
        COMMENT '实际确认出发时间' AFTER nx_ddr_route_status,
    ADD COLUMN nx_ddr_depart_operator_user_id INT NULL
        COMMENT '确认出发操作人' AFTER nx_ddr_actual_depart_at,
    ADD COLUMN nx_ddr_depart_remark VARCHAR(256) NULL
        COMMENT '确认出发备注' AFTER nx_ddr_depart_operator_user_id;

CREATE INDEX idx_ddr_route_status ON nx_dis_driver_route (nx_ddr_route_status);
