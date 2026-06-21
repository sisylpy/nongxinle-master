-- Phase 1 P0：路线站点订单快照关联 + routeDate/dispatchDate 主权字段
-- 前置：upgrade_nx_dis_route_dispatch_phase1.sql
-- 执行前：SHOW TABLES LIKE 'nx_dis_route_stop_order';

-- routeDate / dispatchDate（若 plan 表已存在则补齐列；Duplicate column 可跳过）
ALTER TABLE nx_dis_route_plan
    ADD COLUMN nx_drp_route_date DATE NULL COMMENT '配送路线日(主权)' AFTER nx_drp_plan_date;
ALTER TABLE nx_dis_route_plan
    ADD COLUMN nx_drp_dispatch_date DATE NULL COMMENT '确认派单日' AFTER nx_drp_route_date;

UPDATE nx_dis_route_plan SET nx_drp_route_date = nx_drp_plan_date WHERE nx_drp_route_date IS NULL;

CREATE TABLE IF NOT EXISTS nx_dis_route_stop_order (
    nx_drso_id              INT AUTO_INCREMENT PRIMARY KEY,
    nx_drso_stop_id         INT          NOT NULL COMMENT 'nx_dis_route_stop.nx_drs_id',
    nx_drso_order_id        INT          NOT NULL COMMENT 'nx_department_orders_id 快照',
    nx_drso_department_id   INT          NULL COMMENT '客户父部门ID冗余',
    nx_drso_goods_name      VARCHAR(128) NULL COMMENT '商品名快照',
    nx_drso_quantity        VARCHAR(32)  NULL COMMENT '数量快照',
    nx_drso_standard        VARCHAR(32)  NULL COMMENT '规格快照',
    nx_drso_remark          VARCHAR(256) NULL COMMENT '备注快照',
    UNIQUE KEY uk_stop_order (nx_drso_stop_id, nx_drso_order_id),
    KEY idx_drso_order (nx_drso_order_id),
    KEY idx_drso_stop (nx_drso_stop_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='路线站点订单快照';

CREATE TABLE IF NOT EXISTS nx_dis_route_unassigned_stop_order (
    nx_druo_id                  INT AUTO_INCREMENT PRIMARY KEY,
    nx_druo_unassigned_stop_id  INT          NOT NULL COMMENT 'nx_dis_route_unassigned_stop.nx_drus_id',
    nx_druo_order_id            INT          NOT NULL COMMENT 'nx_department_orders_id 快照',
    nx_druo_department_id       INT          NULL,
    nx_druo_goods_name          VARCHAR(128) NULL,
    nx_druo_quantity            VARCHAR(32)  NULL,
    nx_druo_standard            VARCHAR(32)  NULL,
    nx_druo_remark              VARCHAR(256) NULL,
    UNIQUE KEY uk_unassigned_stop_order (nx_druo_unassigned_stop_id, nx_druo_order_id),
    KEY idx_druo_order (nx_druo_order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='未分配站点订单快照';
