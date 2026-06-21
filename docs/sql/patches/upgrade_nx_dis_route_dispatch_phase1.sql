-- 配送商多司机路线派单引擎 Phase 1
-- 执行前: SHOW TABLES LIKE 'nx_dis_route%';

CREATE TABLE IF NOT EXISTS nx_dis_route_plan (
    nx_drp_id                  INT AUTO_INCREMENT PRIMARY KEY,
    nx_drp_distributer_id      INT          NOT NULL COMMENT '配送商ID',
    nx_drp_plan_date           DATE         NOT NULL COMMENT '计划配送日(兼容)',
    nx_drp_route_date          DATE         NOT NULL COMMENT '配送路线日(主权)',
    nx_drp_dispatch_date       DATE         NULL COMMENT '确认派单日',
    nx_drp_status              VARCHAR(16)  NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT|CONFIRMED|CANCELLED',
    nx_drp_depot_lat           VARCHAR(32)  NULL COMMENT '出发点纬度',
    nx_drp_depot_lng           VARCHAR(32)  NULL COMMENT '出发点经度',
    nx_drp_optimizer_type      VARCHAR(32)  NOT NULL DEFAULT 'BALANCED_INSERTION_2OPT',
    nx_drp_cost_provider_type  VARCHAR(32)  NOT NULL DEFAULT 'TENCENT_MATRIX',
    nx_drp_driver_count        INT          NOT NULL DEFAULT 0,
    nx_drp_total_distance_m    BIGINT       NULL,
    nx_drp_total_duration_s    BIGINT       NULL,
    nx_drp_created_by          INT          NULL,
    nx_drp_confirmed_by        INT          NULL,
    nx_drp_created_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    nx_drp_confirmed_at        DATETIME     NULL,
    KEY idx_drp_dis_date (nx_drp_distributer_id, nx_drp_plan_date),
    KEY idx_drp_status (nx_drp_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='配送商每日路线计划';

CREATE TABLE IF NOT EXISTS nx_dis_driver_route (
    nx_ddr_id                  INT AUTO_INCREMENT PRIMARY KEY,
    nx_ddr_plan_id             INT          NOT NULL,
    nx_ddr_driver_user_id      INT          NOT NULL COMMENT 'nx_distributer_user_id',
    nx_ddr_route_seq           INT          NOT NULL DEFAULT 1 COMMENT '司机序号',
    nx_ddr_total_distance_m    BIGINT       NULL,
    nx_ddr_total_duration_s    BIGINT       NULL,
    nx_ddr_stop_count          INT          NOT NULL DEFAULT 0,
    KEY idx_ddr_plan (nx_ddr_plan_id),
    KEY idx_ddr_driver (nx_ddr_driver_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='计划内司机路线';

CREATE TABLE IF NOT EXISTS nx_dis_route_stop (
    nx_drs_id                  INT AUTO_INCREMENT PRIMARY KEY,
    nx_drs_driver_route_id     INT          NOT NULL,
    nx_drs_stop_seq            INT          NOT NULL COMMENT '路线内站点顺序，从1开始',
    nx_drs_department_id       INT          NOT NULL COMMENT '饭店客户部门ID(父部门)',
    nx_drs_department_name     VARCHAR(128) NULL,
    nx_drs_lat                 VARCHAR(32)  NULL,
    nx_drs_lng                 VARCHAR(32)  NULL,
    nx_drs_address             VARCHAR(256) NULL,
    nx_drs_order_count         INT          NOT NULL DEFAULT 0,
    nx_drs_leg_distance_m      BIGINT       NULL COMMENT '上一站到本站的距离(米)',
    nx_drs_leg_duration_s      BIGINT       NULL COMMENT '上一站到本站的时长(秒)',
    nx_drs_stop_status         VARCHAR(16)  NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING|ARRIVED|COMPLETED',
    KEY idx_drs_route (nx_drs_driver_route_id),
    KEY idx_drs_dep (nx_drs_department_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='司机路线站点';

CREATE TABLE IF NOT EXISTS nx_dis_route_unassigned_stop (
    nx_drus_id                 INT AUTO_INCREMENT PRIMARY KEY,
    nx_drus_plan_id            INT          NOT NULL,
    nx_drus_department_id      INT          NOT NULL,
    nx_drus_department_name    VARCHAR(128) NULL,
    nx_drus_order_count        INT          NOT NULL DEFAULT 0,
    nx_drus_reason             VARCHAR(32)  NOT NULL DEFAULT 'NO_COORDINATE',
    KEY idx_drus_plan (nx_drus_plan_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='未分配站点(如无坐标)';

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
