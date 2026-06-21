-- Phase 2b-5：客户调度参数 + 当日送达窗口快照 / override
-- 前置：upgrade_nx_dis_route_dispatch_phase1_5a.sql、upgrade_nx_dis_route_schedule_phase2.sql

-- 1. 客户长期调度参数（复用 earliest/latest/unload）
ALTER TABLE nx_department
    ADD COLUMN nx_department_dispatch_customer_tier VARCHAR(16) NULL DEFAULT 'NORMAL'
        COMMENT 'VIP|NORMAL|SMALL|NEW' AFTER nx_department_unload_duration,
    ADD COLUMN nx_department_dispatch_priority_weight INT NOT NULL DEFAULT 0
        COMMENT '调度优先级权重，越大越优先' AFTER nx_department_dispatch_customer_tier,
    ADD COLUMN nx_department_dispatch_remark VARCHAR(256) NULL
        COMMENT '调度备注' AFTER nx_department_dispatch_priority_weight;

-- 2. shipment_task 当日快照
ALTER TABLE nx_dis_shipment_task
    ADD COLUMN nx_dst_customer_tier VARCHAR(16) NULL
        COMMENT '客户分类快照 VIP|NORMAL|SMALL|NEW' AFTER nx_dst_priority_level,
    ADD COLUMN nx_dst_priority_weight INT NULL DEFAULT 0
        COMMENT 'priorityWeight 快照' AFTER nx_dst_customer_tier,
    ADD COLUMN nx_dst_order_count INT NULL DEFAULT 0
        COMMENT '有效订单行数快照' AFTER nx_dst_priority_weight,
    ADD COLUMN nx_dst_item_count INT NULL DEFAULT 0
        COMMENT '有效 item 数快照' AFTER nx_dst_order_count,
    ADD COLUMN nx_dst_total_quantity VARCHAR(32) NULL
        COMMENT '商品数量合计快照（展示用）' AFTER nx_dst_item_count,
    ADD COLUMN nx_dst_earliest_delivery_time_s INT NULL
        COMMENT '当日最早送达秒（距 00:00）' AFTER nx_dst_total_quantity,
    ADD COLUMN nx_dst_latest_delivery_time_s INT NULL
        COMMENT '当日最晚送达秒（距 00:00）' AFTER nx_dst_earliest_delivery_time_s,
    ADD COLUMN nx_dst_service_minutes INT NULL
        COMMENT '卸货/服务分钟快照' AFTER nx_dst_latest_delivery_time_s,
    ADD COLUMN nx_dst_time_window_override_flag TINYINT NOT NULL DEFAULT 0
        COMMENT '1=当日窗口已人工 override' AFTER nx_dst_service_minutes,
    ADD COLUMN nx_dst_time_window_adjust_reason VARCHAR(256) NULL
        COMMENT '当日窗口调整原因' AFTER nx_dst_time_window_override_flag;

-- 3. route_stop 当日快照（与 task 对齐；schedule 优先读 stop）
ALTER TABLE nx_dis_route_stop
    ADD COLUMN nx_drs_customer_tier VARCHAR(16) NULL
        COMMENT '客户分类快照' AFTER nx_drs_order_count,
    ADD COLUMN nx_drs_priority_weight INT NULL DEFAULT 0
        COMMENT 'priorityWeight 快照' AFTER nx_drs_customer_tier,
    ADD COLUMN nx_drs_item_count INT NULL DEFAULT 0
        COMMENT 'item 数快照' AFTER nx_drs_priority_weight,
    ADD COLUMN nx_drs_total_quantity VARCHAR(32) NULL
        COMMENT '数量合计快照' AFTER nx_drs_item_count,
    ADD COLUMN nx_drs_time_window_override_flag TINYINT NOT NULL DEFAULT 0
        COMMENT '1=当日窗口已 override' AFTER nx_drs_time_window_status,
    ADD COLUMN nx_drs_time_window_adjust_reason VARCHAR(256) NULL
        COMMENT '当日窗口调整原因' AFTER nx_drs_time_window_override_flag;
