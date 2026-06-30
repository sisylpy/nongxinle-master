-- Phase 3E：司机端配送执行（单店送达 / 配送异常）
-- 前置：upgrade_nx_dis_route_dispatch_phase3d.sql

ALTER TABLE nx_dis_shipment_task
    ADD COLUMN nx_dst_delivered_at DATETIME NULL
        COMMENT '单店送达时间' AFTER nx_dst_stop_status,
    ADD COLUMN nx_dst_delivery_remark VARCHAR(256) NULL
        COMMENT '单店送达备注' AFTER nx_dst_delivered_at,
    ADD COLUMN nx_dst_delivery_operator_user_id INT NULL
        COMMENT '单店送达操作人' AFTER nx_dst_delivery_remark,
    ADD COLUMN nx_dst_exception_type VARCHAR(32) NULL
        COMMENT '配送异常类型' AFTER nx_dst_delivery_operator_user_id,
    ADD COLUMN nx_dst_exception_remark VARCHAR(512) NULL
        COMMENT '配送异常备注' AFTER nx_dst_exception_type,
    ADD COLUMN nx_dst_exception_at DATETIME NULL
        COMMENT '配送异常记录时间' AFTER nx_dst_exception_remark;

CREATE INDEX idx_dst_status_route ON nx_dis_shipment_task (nx_dst_status, nx_dst_driver_route_id);
