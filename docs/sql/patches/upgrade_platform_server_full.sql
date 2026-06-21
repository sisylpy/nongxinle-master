-- =============================================================================
-- 京采平台 · 服务器全量结构升级（幂等，可重复执行）
--
-- 适用：远程 nongxinle 库缺 platform_checkout_payment / assign / bill 扩展等
-- 用法：Navicat 选中 nongxinle 库 → 打开本文件 → 运行全部
--
-- 包含：
--   · sys_city_market 种子 + 微信支付配置类
--   · gb_department_bill 平台现金字段 + gb_department_bill_payment
--   · Phase 2a/2b 平台表 + assign GB 来源 + checkout 支付表
--
-- 不含：测试种子、清理脚本、历史 backfill（按需另跑）
-- =============================================================================

-- =============================================================================
-- §0 市场主数据 marketId=1
-- =============================================================================
INSERT INTO sys_city_market (
    sys_city_market_id,
    sys_cm_city_id,
    sys_cm_market_name,
    sys_cm_register_gift_points,
    sys_cm_points_per_yuan,
    sys_cm_self_print_enabled
)
SELECT 1, 1, '京贸物联批发市场', 0, 1, 0
FROM (SELECT 1) AS _seed
WHERE NOT EXISTS (SELECT 1 FROM sys_city_market WHERE sys_city_market_id = 1);

-- 京采小程序 wx58ba279bc3d04c4a 对应支付配置（列存在时才更新）
SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_city_market'
       AND COLUMN_NAME = 'sys_cm_pay_config_class') > 0,
    'UPDATE sys_city_market SET sys_cm_pay_config_class = ''MyWxJJCGPayConfig''
     WHERE sys_city_market_id = 1
       AND (sys_cm_pay_config_class IS NULL OR sys_cm_pay_config_class = ''''
            OR sys_cm_pay_config_class = ''MyWxJjdhPayConfig'')',
    'SELECT ''skip sys_cm_pay_config_class (column missing)'' AS msg'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;


-- =============================================================================
-- §1 gb_department_bill 平台现金 + gb_department_bill_payment
--     （来自 upgrade_gb_bill_platform_cash_payment_v1.sql）
-- =============================================================================

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'gb_department_bill' AND COLUMN_NAME = 'gb_db_known_total') = 0,
    'ALTER TABLE gb_department_bill ADD COLUMN gb_db_known_total DECIMAL(12,2) NULL COMMENT ''提交时已知价合计'' AFTER gb_db_total',
    'SELECT ''skip gb_db_known_total'' AS msg'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'gb_department_bill' AND COLUMN_NAME = 'gb_db_paid_total') = 0,
    'ALTER TABLE gb_department_bill ADD COLUMN gb_db_paid_total DECIMAL(12,2) NULL DEFAULT 0.00 COMMENT ''累计已支付'' AFTER gb_db_known_total',
    'SELECT ''skip gb_db_paid_total'' AS msg'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'gb_department_bill' AND COLUMN_NAME = 'gb_db_supplement_due') = 0,
    'ALTER TABLE gb_department_bill ADD COLUMN gb_db_supplement_due DECIMAL(12,2) NULL DEFAULT 0.00 COMMENT ''待补款'' AFTER gb_db_paid_total',
    'SELECT ''skip gb_db_supplement_due'' AS msg'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'gb_department_bill' AND COLUMN_NAME = 'gb_db_pending_item_count') = 0,
    'ALTER TABLE gb_department_bill ADD COLUMN gb_db_pending_item_count INT NULL COMMENT ''待确认价行数'' AFTER gb_db_supplement_due',
    'SELECT ''skip gb_db_pending_item_count'' AS msg'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'gb_department_bill' AND COLUMN_NAME = 'gb_db_pay_status') = 0,
    'ALTER TABLE gb_department_bill ADD COLUMN gb_db_pay_status VARCHAR(32) NULL COMMENT ''支付状态'' AFTER gb_db_pending_item_count',
    'SELECT ''skip gb_db_pay_status'' AS msg'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'gb_department_bill' AND COLUMN_NAME = 'gb_db_bill_source') = 0,
    'ALTER TABLE gb_department_bill ADD COLUMN gb_db_bill_source VARCHAR(32) NULL COMMENT ''LEGACY|PLATFORM_CASH'' AFTER gb_db_pay_status',
    'SELECT ''skip gb_db_bill_source'' AS msg'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

UPDATE gb_department_bill SET gb_db_bill_source = 'LEGACY' WHERE gb_db_bill_source IS NULL;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'gb_department_bill' AND COLUMN_NAME = 'gb_db_platform_submit_token') = 0,
    'ALTER TABLE gb_department_bill ADD COLUMN gb_db_platform_submit_token VARCHAR(64) NULL COMMENT ''checkout 幂等 token'' AFTER gb_db_bill_source',
    'SELECT ''skip gb_db_platform_submit_token'' AS msg'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.STATISTICS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'gb_department_bill' AND INDEX_NAME = 'idx_gbdb_dep_pay') = 0,
    'CREATE INDEX idx_gbdb_dep_pay ON gb_department_bill (gb_DB_dep_id, gb_db_bill_source, gb_db_pay_status)',
    'SELECT ''skip idx_gbdb_dep_pay'' AS msg'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.STATISTICS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'gb_department_bill' AND INDEX_NAME = 'uk_gb_bill_platform_submit_token') = 0,
    'CREATE UNIQUE INDEX uk_gb_bill_platform_submit_token ON gb_department_bill (gb_db_platform_submit_token)',
    'SELECT ''skip uk_gb_bill_platform_submit_token'' AS msg'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS gb_department_bill_payment (
    gb_bp_id              INT            NOT NULL AUTO_INCREMENT,
    gb_bp_bill_id         INT            NOT NULL,
    gb_bp_pay_phase       VARCHAR(16)    NOT NULL COMMENT 'FIRST|SUPPLEMENT',
    gb_bp_amount          DECIMAL(12,2)  NOT NULL,
    gb_bp_out_trade_no    VARCHAR(64)    NOT NULL,
    gb_bp_transaction_id  VARCHAR(64)    NULL,
    gb_bp_status          VARCHAR(16)    NOT NULL DEFAULT 'PENDING',
    gb_bp_paid_at         DATETIME       NULL,
    gb_bp_notify_raw      TEXT           NULL,
    gb_bp_created_at      DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    gb_bp_updated_at      DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (gb_bp_id),
    UNIQUE KEY uk_gb_bp_out_trade_no (gb_bp_out_trade_no),
    KEY idx_gb_bp_bill_id (gb_bp_bill_id),
    KEY idx_gb_bp_bill_phase (gb_bp_bill_id, gb_bp_pay_phase, gb_bp_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='GB采购单支付流水';

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'gb_department_orders' AND COLUMN_NAME = 'gb_do_price_confirm_status') = 0,
    'ALTER TABLE gb_department_orders ADD COLUMN gb_do_price_confirm_status VARCHAR(16) NULL COMMENT ''PENDING|CONFIRMED'' AFTER gb_do_price',
    'SELECT ''skip gb_do_price_confirm_status'' AS msg'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;


-- =============================================================================
-- §2 Phase 2a 平台表
-- =============================================================================
CREATE TABLE IF NOT EXISTS nx_market_department (
    nx_market_department_id   INT          NOT NULL AUTO_INCREMENT,
    nx_md_market_id           INT          NOT NULL,
    nx_md_department_id       INT          NOT NULL,
    nx_md_status              VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE',
    nx_md_source              VARCHAR(32)  NOT NULL DEFAULT 'IMPORT',
    nx_md_created_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    nx_md_created_by          INT          NULL,
    PRIMARY KEY (nx_market_department_id),
    UNIQUE KEY uk_market_dep (nx_md_market_id, nx_md_department_id),
    KEY idx_md_department (nx_md_department_id),
    KEY idx_md_market_status (nx_md_market_id, nx_md_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS nx_department_nx_goods_default (
    nx_dngd_id                     INT          NOT NULL AUTO_INCREMENT,
    nx_dngd_market_id              INT          NOT NULL,
    nx_dngd_department_id          INT          NOT NULL,
    nx_dngd_nx_goods_id            INT          NOT NULL,
    nx_dngd_default_distributer_id INT          NOT NULL,
    nx_dngd_default_dis_goods_id   INT          NOT NULL,
    nx_dngd_source                 VARCHAR(32)  NOT NULL DEFAULT 'MANUAL',
    nx_dngd_status                 VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE',
    nx_dngd_last_order_id          INT          NULL,
    nx_dngd_last_switch_log_id     INT          NULL,
    nx_dngd_active_snapshot_id     INT          NULL,
    nx_dngd_created_at             DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    nx_dngd_created_by             INT          NULL,
    nx_dngd_updated_at             DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    nx_dngd_updated_by             INT          NULL,
    PRIMARY KEY (nx_dngd_id),
    UNIQUE KEY uk_default (nx_dngd_market_id, nx_dngd_department_id, nx_dngd_nx_goods_id),
    KEY idx_default_dis (nx_dngd_default_distributer_id),
    KEY idx_default_dis_goods (nx_dngd_default_dis_goods_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS nx_supplier_switch_log (
    nx_ssl_id                    INT          NOT NULL AUTO_INCREMENT,
    nx_ssl_market_id             INT          NOT NULL,
    nx_ssl_department_id         INT          NOT NULL,
    nx_ssl_nx_goods_id           INT          NOT NULL,
    nx_ssl_order_id              INT          NULL,
    nx_ssl_from_distributer_id   INT          NULL,
    nx_ssl_from_dis_goods_id     INT          NULL,
    nx_ssl_to_distributer_id     INT          NOT NULL,
    nx_ssl_to_dis_goods_id       INT          NOT NULL,
    nx_ssl_switch_scope          VARCHAR(32)  NOT NULL,
    nx_ssl_reason_code           VARCHAR(32)  NOT NULL,
    nx_ssl_reason_note           VARCHAR(512) NULL,
    nx_ssl_snapshot_id           INT          NULL,
    nx_ssl_snapshot_action       VARCHAR(32)  NOT NULL DEFAULT 'NONE',
    nx_ssl_operator_id           INT          NOT NULL,
    nx_ssl_created_at            DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (nx_ssl_id),
    KEY idx_ssl_dep_goods (nx_ssl_market_id, nx_ssl_department_id, nx_ssl_nx_goods_id),
    KEY idx_ssl_order (nx_ssl_order_id),
    KEY idx_ssl_created (nx_ssl_created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS nx_platform_order_assign (
    nx_poa_id                      INT          NOT NULL AUTO_INCREMENT,
    nx_poa_market_id               INT          NOT NULL,
    nx_poa_order_id                INT          NOT NULL,
    nx_poa_department_id           INT          NOT NULL,
    nx_poa_nx_goods_id             INT          NULL,
    nx_poa_assign_status           VARCHAR(16)  NOT NULL DEFAULT 'PENDING',
    nx_poa_assign_mode             VARCHAR(16)  NOT NULL DEFAULT 'PLATFORM',
    nx_poa_assigned_distributer_id INT          NULL,
    nx_poa_assigned_dis_goods_id   INT          NULL,
    nx_poa_assigned_price          DECIMAL(10, 2) NULL,
    nx_poa_assigned_at             DATETIME     NULL,
    nx_poa_assigned_by             INT          NULL,
    nx_poa_default_id              INT          NULL,
    nx_poa_switch_log_id           INT          NULL,
    nx_poa_snapshot_id             INT          NULL,
    nx_poa_deviation_flags         VARCHAR(256) NULL,
    nx_poa_created_at              DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    nx_poa_updated_at              DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (nx_poa_id),
    UNIQUE KEY uk_poa_order (nx_poa_order_id),
    KEY idx_poa_market_status (nx_poa_market_id, nx_poa_assign_status),
    KEY idx_poa_dep_date (nx_poa_department_id, nx_poa_assign_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- =============================================================================
-- §3 Phase 2b 快照 + 履约
-- =============================================================================
CREATE TABLE IF NOT EXISTS nx_department_nx_goods_snapshot (
    nx_dngs_id                       INT            NOT NULL AUTO_INCREMENT,
    nx_dngs_market_id                INT            NOT NULL,
    nx_dngs_department_id            INT            NOT NULL,
    nx_dngs_nx_goods_id              INT            NOT NULL,
    nx_dngs_snapshot_version         INT            NOT NULL,
    nx_dngs_snapshot_name            VARCHAR(128)   NULL,
    nx_dngs_status                   VARCHAR(16)    NOT NULL DEFAULT 'DRAFT',
    nx_dngs_default_distributer_id   INT            NOT NULL,
    nx_dngs_default_dis_goods_id     INT            NOT NULL,
    nx_dngs_snapshot_price           DECIMAL(10, 2) NOT NULL,
    nx_dngs_snapshot_unit            VARCHAR(32)    NULL,
    nx_dngs_snapshot_standard        VARCHAR(64)    NULL,
    nx_dngs_snapshot_goods_name      VARCHAR(128)   NULL,
    nx_dngs_snapshot_goods_brand     VARCHAR(64)    NULL,
    nx_dngs_snapshot_goods_place     VARCHAR(64)    NULL,
    nx_dngs_snapshot_quality_label   VARCHAR(64)    NULL,
    nx_dngs_snapshot_quality_note    VARCHAR(512)   NULL,
    nx_dngs_sample_start_date        DATE           NULL,
    nx_dngs_sample_end_date          DATE           NULL,
    nx_dngs_source_order_count       INT            NOT NULL DEFAULT 0,
    nx_dngs_price_tolerance_type     VARCHAR(16)    NOT NULL DEFAULT 'PERCENT',
    nx_dngs_price_tolerance_value    DECIMAL(10, 2) NOT NULL DEFAULT 10.00,
    nx_dngs_allow_supplier_switch    TINYINT        NOT NULL DEFAULT 1,
    nx_dngs_allow_quality_substitution TINYINT      NOT NULL DEFAULT 0,
    nx_dngs_superseded_by_id         INT            NULL,
    nx_dngs_created_by               INT            NOT NULL,
    nx_dngs_created_at               DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    nx_dngs_updated_by               INT            NULL,
    nx_dngs_updated_at               DATETIME       NULL,
    nx_dngs_accepted_by              INT            NULL,
    nx_dngs_accepted_at              DATETIME       NULL,
    nx_dngs_last_compared_order_id   INT            NULL,
    nx_dngs_last_compared_at         DATETIME       NULL,
    PRIMARY KEY (nx_dngs_id),
    UNIQUE KEY uk_snapshot_version (nx_dngs_market_id, nx_dngs_department_id, nx_dngs_nx_goods_id, nx_dngs_snapshot_version),
    KEY idx_snapshot_group_status (nx_dngs_market_id, nx_dngs_department_id, nx_dngs_nx_goods_id, nx_dngs_status),
    KEY idx_snapshot_superseded (nx_dngs_superseded_by_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS nx_department_nx_goods_snapshot_order (
    nx_dngso_id           INT         NOT NULL AUTO_INCREMENT,
    nx_dngso_snapshot_id  INT         NOT NULL,
    nx_dngso_order_id     INT         NOT NULL,
    nx_dngso_order_source VARCHAR(16) NOT NULL DEFAULT 'LIVE',
    PRIMARY KEY (nx_dngso_id),
    UNIQUE KEY uk_snapshot_order (nx_dngso_snapshot_id, nx_dngso_order_id, nx_dngso_order_source),
    KEY idx_dngso_order (nx_dngso_order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS nx_platform_order_fulfillment (
    nx_pof_id                      INT          NOT NULL AUTO_INCREMENT,
    nx_pof_market_id               INT          NOT NULL,
    nx_pof_order_id                INT          NOT NULL,
    nx_pof_platform_assign_id      INT          NOT NULL,
    nx_pof_department_id           INT          NOT NULL,
    nx_pof_nx_goods_id             INT          NULL,
    nx_pof_distributer_id          INT          NOT NULL,
    nx_pof_dis_goods_id            INT          NOT NULL,
    nx_pof_fulfillment_status      VARCHAR(32)  NOT NULL DEFAULT 'ASSIGNED',
    nx_pof_cost_missing            TINYINT      NOT NULL DEFAULT 0,
    nx_pof_cost_missing_reason     VARCHAR(128) NULL,
    nx_pof_ready_for_pickup_at     DATETIME     NULL,
    nx_pof_picked_up_at            DATETIME     NULL,
    nx_pof_delivered_at            DATETIME     NULL,
    nx_pof_created_at              DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    nx_pof_updated_at              DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    nx_pof_updated_by              INT          NULL,
    PRIMARY KEY (nx_pof_id),
    UNIQUE KEY uk_pof_order (nx_pof_order_id),
    KEY idx_pof_platform_assign (nx_pof_platform_assign_id),
    KEY idx_pof_market_status (nx_pof_market_id, nx_pof_fulfillment_status),
    KEY idx_pof_distributer_status (nx_pof_distributer_id, nx_pof_fulfillment_status),
    KEY idx_pof_market_dis_status (nx_pof_market_id, nx_pof_distributer_id, nx_pof_fulfillment_status),
    KEY idx_pof_department (nx_pof_department_id),
    KEY idx_pof_dis_goods (nx_pof_dis_goods_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- =============================================================================
-- §4 assign GB 来源扩展（幂等加列）
-- =============================================================================
SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'nx_platform_order_assign'
       AND COLUMN_NAME = 'nx_poa_assign_source') = 0,
    'ALTER TABLE nx_platform_order_assign ADD COLUMN nx_poa_assign_source VARCHAR(32) NULL AFTER nx_poa_assign_mode',
    'SELECT ''skip nx_poa_assign_source'' AS msg'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'nx_platform_order_assign'
       AND COLUMN_NAME = 'nx_poa_source_type') = 0,
    'ALTER TABLE nx_platform_order_assign ADD COLUMN nx_poa_source_type VARCHAR(8) NULL AFTER nx_poa_assign_source',
    'SELECT ''skip nx_poa_source_type'' AS msg'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'nx_platform_order_assign'
       AND COLUMN_NAME = 'nx_poa_gb_department_id') = 0,
    'ALTER TABLE nx_platform_order_assign ADD COLUMN nx_poa_gb_department_id INT NULL AFTER nx_poa_source_type',
    'SELECT ''skip nx_poa_gb_department_id'' AS msg'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'nx_platform_order_assign'
       AND COLUMN_NAME = 'nx_poa_gb_department_father_id') = 0,
    'ALTER TABLE nx_platform_order_assign ADD COLUMN nx_poa_gb_department_father_id INT NULL AFTER nx_poa_gb_department_id',
    'SELECT ''skip nx_poa_gb_department_father_id'' AS msg'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'nx_platform_order_assign'
       AND COLUMN_NAME = 'nx_poa_gb_department_order_id') = 0,
    'ALTER TABLE nx_platform_order_assign ADD COLUMN nx_poa_gb_department_order_id INT NULL AFTER nx_poa_gb_department_father_id',
    'SELECT ''skip nx_poa_gb_department_order_id'' AS msg'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.STATISTICS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'nx_platform_order_assign'
       AND INDEX_NAME = 'uk_poa_gb_department_order_id') = 0,
    'CREATE UNIQUE INDEX uk_poa_gb_department_order_id ON nx_platform_order_assign (nx_poa_gb_department_order_id)',
    'SELECT ''skip uk_poa_gb_department_order_id'' AS msg'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.STATISTICS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'nx_platform_order_assign'
       AND INDEX_NAME = 'idx_poa_source_gb_dep') = 0,
    'CREATE INDEX idx_poa_source_gb_dep ON nx_platform_order_assign (nx_poa_source_type, nx_poa_gb_department_id)',
    'SELECT ''skip idx_poa_source_gb_dep'' AS msg'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

UPDATE nx_platform_order_assign SET nx_poa_source_type = 'NX' WHERE nx_poa_source_type IS NULL;
UPDATE nx_platform_order_assign
SET nx_poa_assign_source = 'PLATFORM_MANUAL'
WHERE nx_poa_assign_source IS NULL
  AND nx_poa_assign_status = 'ASSIGNED'
  AND nx_poa_assign_mode = 'PLATFORM';


-- =============================================================================
-- §5 checkout 微信支付意图表 platform_checkout_payment
-- =============================================================================
CREATE TABLE IF NOT EXISTS platform_checkout_payment (
    pcp_id                       INT AUTO_INCREMENT PRIMARY KEY,
    pcp_checkout_token           VARCHAR(64)  NOT NULL COMMENT 'checkout 批次幂等 token',
    pcp_market_id                INT          NOT NULL,
    pcp_gb_department_id         INT          NOT NULL,
    pcp_gb_department_father_id  INT          NULL,
    pcp_gb_distributer_id        INT          NULL,
    pcp_gb_order_user_id         INT          NULL,
    pcp_delivery_date            VARCHAR(32)  NULL,
    pcp_remark                   VARCHAR(512) NULL,
    pcp_order_ids_json           TEXT         NOT NULL COMMENT 'JSON nxOrderId 快照',
    pcp_known_total              DECIMAL(12,2) NOT NULL,
    pcp_pending_price_item_count INT          NOT NULL DEFAULT 0,
    pcp_out_trade_no             VARCHAR(64)  NOT NULL,
    pcp_wx_prepay_id             VARCHAR(128) NULL,
    pcp_open_id                  VARCHAR(128) NOT NULL,
    pcp_status                   VARCHAR(16)  NOT NULL DEFAULT 'PENDING',
    pcp_bill_id                  INT          NULL,
    pcp_transaction_id           VARCHAR(64)  NULL,
    pcp_notify_raw               TEXT         NULL,
    pcp_paid_at                  VARCHAR(32)  NULL,
    pcp_expire_at                VARCHAR(32)  NULL COMMENT 'PENDING 过期时间',
    pcp_closed_at                VARCHAR(32)  NULL COMMENT '取消/超时关闭时间',
    pcp_created_at               VARCHAR(32)  NULL,
    pcp_updated_at               VARCHAR(32)  NULL,
    UNIQUE KEY uk_pcp_checkout_token (pcp_checkout_token),
    UNIQUE KEY uk_pcp_out_trade_no (pcp_out_trade_no),
    KEY idx_pcp_dep_status (pcp_gb_department_id, pcp_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台购物车 checkout 微信支付';

-- 旧表缺列时补齐
SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'platform_checkout_payment'
       AND COLUMN_NAME = 'pcp_pending_price_item_count') = 0,
    'ALTER TABLE platform_checkout_payment ADD COLUMN pcp_pending_price_item_count INT NOT NULL DEFAULT 0 AFTER pcp_known_total',
    'SELECT ''skip pcp_pending_price_item_count'' AS msg'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'platform_checkout_payment'
       AND COLUMN_NAME = 'pcp_expire_at') = 0,
    'ALTER TABLE platform_checkout_payment ADD COLUMN pcp_expire_at VARCHAR(32) NULL AFTER pcp_paid_at',
    'SELECT ''skip pcp_expire_at'' AS msg'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'platform_checkout_payment'
       AND COLUMN_NAME = 'pcp_closed_at') = 0,
    'ALTER TABLE platform_checkout_payment ADD COLUMN pcp_closed_at VARCHAR(32) NULL AFTER pcp_expire_at',
    'SELECT ''skip pcp_closed_at'' AS msg'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;


-- =============================================================================
-- §5 市场后台用户 Phase 1a-0（platform_market_user）
--     详见 upgrade_platform_market_user_v1.sql
-- =============================================================================

CREATE TABLE IF NOT EXISTS platform_market_user (
    pmu_id           INT          NOT NULL AUTO_INCREMENT,
    market_id        INT          NOT NULL,
    login_account    VARCHAR(64)  NOT NULL,
    phone            VARCHAR(32)  NULL,
    password_hash    VARCHAR(128) NOT NULL,
    real_name        VARCHAR(64)  NULL,
    role_type        VARCHAR(32)  NOT NULL DEFAULT 'OPERATOR',
    status           VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE',
    last_login_at    DATETIME     NULL,
    created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (pmu_id),
    UNIQUE KEY uk_pmu_market_login (market_id, login_account),
    KEY idx_pmu_market (market_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS platform_market_user_session (
    pms_id     BIGINT       NOT NULL AUTO_INCREMENT,
    pmu_id     INT          NOT NULL,
    token      VARCHAR(64)  NOT NULL,
    expire_at  DATETIME     NOT NULL,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (pms_id),
    UNIQUE KEY uk_pms_token (token),
    KEY idx_pms_pmu (pmu_id),
    KEY idx_pms_expire (expire_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- =============================================================================
-- §6 京采平台优惠券 Phase 1a（详见 upgrade_platform_coupon_phase1a.sql）
-- =============================================================================

CREATE TABLE IF NOT EXISTS platform_coupon_template (
    pct_id                      INT            NOT NULL AUTO_INCREMENT,
    market_id                   INT            NOT NULL,
    template_name               VARCHAR(128)   NOT NULL,
    coupon_type                 VARCHAR(32)    NOT NULL,
    discount_amount             DECIMAL(12,2)  NOT NULL,
    threshold_amount            DECIMAL(12,2)  NOT NULL DEFAULT 0,
    scope_type                  VARCHAR(32)    NOT NULL DEFAULT 'ALL',
    scope_ref_ids               TEXT           NULL,
    use_channel                 VARCHAR(32)    NOT NULL DEFAULT 'ALL',
    biz_purpose                 VARCHAR(32)    NOT NULL DEFAULT 'marketing',
    claim_strategy              VARCHAR(32)    NOT NULL DEFAULT 'public_active',
    validity_type               VARCHAR(32)    NOT NULL,
    validity_days               INT            NULL,
    start_date                  VARCHAR(32)    NULL,
    stop_date                   VARCHAR(32)    NULL,
    status                      VARCHAR(16)    NOT NULL DEFAULT 'ACTIVE',
    issue_count                 INT            NOT NULL DEFAULT 0,
    use_count                   INT            NOT NULL DEFAULT 0,
    created_by_market_user_id   INT            NULL,
    updated_by_market_user_id   INT            NULL,
    created_at                  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (pct_id),
    KEY idx_pct_market (market_id),
    KEY idx_pct_market_status (market_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS platform_store_coupon (
    psc_id                      INT            NOT NULL AUTO_INCREMENT,
    template_id                 INT            NOT NULL,
    market_id                   INT            NOT NULL,
    store_gb_department_id      INT            NOT NULL,
    status                      VARCHAR(16)    NOT NULL DEFAULT 'AVAILABLE',
    source_type                 VARCHAR(32)    NOT NULL DEFAULT 'manual',
    source_biz_id               VARCHAR(64)    NULL,
    start_date                  VARCHAR(32)    NULL,
    stop_date                   VARCHAR(32)    NULL,
    locked_checkout_token       VARCHAR(64)    NULL,
    locked_payment_id           INT            NULL,
    issued_by_market_user_id    INT            NULL,
    claimed_by_user_id          INT            NULL,
    used_by_user_id             INT            NULL,
    used_bill_id                INT            NULL,
    used_at                     DATETIME       NULL,
    created_at                  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (psc_id),
    KEY idx_psc_market_store (market_id, store_gb_department_id),
    KEY idx_psc_template (template_id),
    KEY idx_psc_store_status (store_gb_department_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS platform_coupon_usage_log (
    pcul_id                     BIGINT         NOT NULL AUTO_INCREMENT,
    store_coupon_id             INT            NOT NULL,
    template_id                 INT            NOT NULL,
    market_id                   INT            NOT NULL,
    store_gb_department_id      INT            NOT NULL,
    verify_type                 VARCHAR(32)    NOT NULL,
    checkout_token              VARCHAR(64)    NULL,
    payment_id                  INT            NULL,
    bill_id                     INT            NULL,
    discount_amount             DECIMAL(12,2)  NULL,
    before_status               VARCHAR(16)    NULL,
    after_status                VARCHAR(16)    NULL,
    operator_type               VARCHAR(32)    NOT NULL,
    operator_id                 INT            NULL,
    created_at                  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (pcul_id),
    KEY idx_pcul_store_coupon (store_coupon_id),
    KEY idx_pcul_market (market_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- =============================================================================
-- §7 验收（执行后应都有结果）
-- =============================================================================
SELECT 'platform_checkout_payment' AS check_item,
       COUNT(*) AS table_exists
FROM information_schema.tables
WHERE table_schema = DATABASE() AND table_name = 'platform_checkout_payment';

SELECT 'gb_department_bill_payment' AS check_item,
       COUNT(*) AS table_exists
FROM information_schema.tables
WHERE table_schema = DATABASE() AND table_name = 'gb_department_bill_payment';

SELECT 'nx_platform_order_assign' AS check_item,
       COUNT(*) AS table_exists
FROM information_schema.tables
WHERE table_schema = DATABASE() AND table_name = 'nx_platform_order_assign';

SELECT 'nx_platform_order_fulfillment' AS check_item,
       COUNT(*) AS table_exists
FROM information_schema.tables
WHERE table_schema = DATABASE() AND table_name = 'nx_platform_order_fulfillment';

SELECT 'nx_market_department' AS check_item,
       COUNT(*) AS table_exists
FROM information_schema.tables
WHERE table_schema = DATABASE() AND table_name = 'nx_market_department';

SELECT 'platform_market_user' AS check_item,
       COUNT(*) AS table_exists
FROM information_schema.tables
WHERE table_schema = DATABASE() AND table_name = 'platform_market_user';

SELECT 'platform_coupon_template' AS check_item,
       COUNT(*) AS table_exists
FROM information_schema.tables
WHERE table_schema = DATABASE() AND table_name = 'platform_coupon_template';

SELECT 'platform_store_coupon' AS check_item,
       COUNT(*) AS table_exists
FROM information_schema.tables
WHERE table_schema = DATABASE() AND table_name = 'platform_store_coupon';

SELECT sys_city_market_id, sys_cm_market_name
FROM sys_city_market WHERE sys_city_market_id = 1;
