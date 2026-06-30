-- nxCommunity 商城派单 M1
-- 执行前: SHOW TABLES LIKE 'nx_community_dispatch%';

CREATE TABLE IF NOT EXISTS nx_community_dispatch_plan (
    nx_community_dispatch_plan_id   INT AUTO_INCREMENT PRIMARY KEY,
    nx_cdp_community_id             INT          NOT NULL COMMENT 'nx_community_id',
    nx_cdp_route_date               DATE         NOT NULL COMMENT '配送路线日',
    nx_cdp_status                   VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE' COMMENT 'DRAFT|ACTIVE|COMPLETED|CANCELLED',
    nx_cdp_depot_lat                VARCHAR(32)  NULL,
    nx_cdp_depot_lng                VARCHAR(32)  NULL,
    nx_cdp_created_at               DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    nx_cdp_updated_at               DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_cdp_comm_date (nx_cdp_community_id, nx_cdp_route_date),
    KEY idx_cdp_status (nx_cdp_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='nxCommunity 派单计划';

CREATE TABLE IF NOT EXISTS nx_community_dispatch_driver_route (
    nx_community_dispatch_driver_route_id INT AUTO_INCREMENT PRIMARY KEY,
    nx_cddr_plan_id                   INT          NOT NULL,
    nx_cddr_community_id              INT          NOT NULL,
    nx_cddr_driver_user_id            INT          NOT NULL COMMENT 'nx_community_user_id, roleId=5',
    nx_cddr_route_status              VARCHAR(16)  NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT|LOADING|IN_DELIVERY|COMPLETED|IDLE',
    nx_cddr_loading_entered_at        DATETIME     NULL,
    nx_cddr_actual_depart_at          DATETIME     NULL,
    nx_cddr_stop_count                INT          NOT NULL DEFAULT 0,
    nx_cddr_created_at                DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    nx_cddr_updated_at                DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_cddr_plan_driver (nx_cddr_plan_id, nx_cddr_driver_user_id),
    KEY idx_cddr_driver (nx_cddr_driver_user_id),
    KEY idx_cddr_status (nx_cddr_route_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='nxCommunity 司机路线';

CREATE TABLE IF NOT EXISTS nx_community_dispatch_stop (
    nx_community_dispatch_stop_id     INT AUTO_INCREMENT PRIMARY KEY,
    nx_cds_plan_id                    INT          NOT NULL,
    nx_cds_driver_route_id            INT          NULL,
    nx_cds_community_id               INT          NOT NULL,
    nx_cds_address_id                 INT          NULL COMMENT 'nx_customer_user_address_id',
    nx_cds_customer_name              VARCHAR(64)  NULL,
    nx_cds_customer_phone             VARCHAR(32)  NULL,
    nx_cds_lat                        VARCHAR(32)  NULL,
    nx_cds_lng                        VARCHAR(32)  NULL,
    nx_cds_address_text               VARCHAR(512) NULL,
    nx_cds_stop_status                VARCHAR(16)  NOT NULL DEFAULT 'ASSIGNED',
    nx_cds_route_seq                  INT          NOT NULL DEFAULT 1,
    nx_cds_service_date               VARCHAR(16)  NULL,
    nx_cds_service_time               VARCHAR(32)  NULL,
    nx_cds_assigned_driver_user_id    INT          NULL,
    nx_cds_order_count                INT          NOT NULL DEFAULT 0,
    nx_cds_confirmed_at               DATETIME     NULL,
    nx_cds_delivered_at               DATETIME     NULL,
    nx_cds_created_at                 DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    nx_cds_updated_at                 DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_cds_plan (nx_cds_plan_id),
    KEY idx_cds_route (nx_cds_driver_route_id),
    KEY idx_cds_status (nx_cds_stop_status),
    KEY idx_cds_driver (nx_cds_assigned_driver_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='nxCommunity 配送站点';

CREATE TABLE IF NOT EXISTS nx_community_dispatch_stop_item (
    nx_community_dispatch_stop_item_id INT AUTO_INCREMENT PRIMARY KEY,
    nx_cdsi_stop_id                   INT          NOT NULL,
    nx_cdsi_community_order_id        INT          NOT NULL,
    nx_cdsi_goods_summary             VARCHAR(256) NULL,
    nx_cdsi_order_total               VARCHAR(32)  NULL,
    nx_cdsi_created_at                DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_cdsi_stop_order (nx_cdsi_stop_id, nx_cdsi_community_order_id),
    KEY idx_cdsi_order (nx_cdsi_community_order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='nxCommunity 站点订单';

CREATE TABLE IF NOT EXISTS nx_community_order_dispatch (
    nx_community_order_dispatch_id    INT AUTO_INCREMENT PRIMARY KEY,
    nx_cod_community_order_id         INT          NOT NULL,
    nx_cod_community_id               INT          NOT NULL,
    nx_cod_dispatch_status            VARCHAR(16)  NOT NULL DEFAULT 'ASSIGNED',
    nx_cod_dispatch_stop_id           INT          NULL,
    nx_cod_assigned_driver_user_id    INT          NULL,
    nx_cod_route_date                 DATE         NULL,
    nx_cod_created_at                 DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    nx_cod_updated_at                 DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_cod_order (nx_cod_community_order_id),
    KEY idx_cod_stop (nx_cod_dispatch_stop_id),
    KEY idx_cod_driver (nx_cod_assigned_driver_user_id),
    KEY idx_cod_status (nx_cod_dispatch_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='nxCommunity 订单派单扩展';

CREATE TABLE IF NOT EXISTS nx_community_dispatch_driver_duty (
    nx_community_dispatch_driver_duty_id INT AUTO_INCREMENT PRIMARY KEY,
    nx_cdd_community_id               INT          NOT NULL,
    nx_cdd_driver_user_id             INT          NOT NULL,
    nx_cdd_route_date                 DATE         NOT NULL,
    nx_cdd_status                     VARCHAR(16)  NOT NULL DEFAULT 'ON_DUTY' COMMENT 'ON_DUTY|OFF_DUTY',
    nx_cdd_checked_in_at              DATETIME     NULL,
    nx_cdd_checked_out_at             DATETIME     NULL,
    UNIQUE KEY uk_cdd_driver_date (nx_cdd_community_id, nx_cdd_driver_user_id, nx_cdd_route_date),
    KEY idx_cdd_status (nx_cdd_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='nxCommunity 司机值班';
