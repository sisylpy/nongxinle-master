-- =============================================================================
-- 批发市场平台化 Phase 2a：订单分配核心闭环
--
-- 执行顺序（Round 1 验收，不可跳步）：
--   1. check_nx_department_orders_distributer_id.sql
--   2. 本文件（upgrade_nx_platform_phase2a.sql）
--   3. seed_nx_platform_phase2a_test.sql（改真实 ID）
--   4. 再启动含 Phase 2a Java 的应用
--
-- 详见 docs/nxPlatform/Phase2a-Round1-验收指南.md
--
-- 执行前必做（步骤 1）：
--   SHOW CREATE TABLE nx_department_orders;
-- 确认 nx_DO_distributer_id 是否允许 NULL：
--   - 允许 NULL → submitLine 待分配阶段写 NULL
--   - NOT NULL   → 每个 market 注册「平台池配送商」，submitLine 写该 ID，
--                  PENDING 期间靠 nx_platform_order_assign 排除，不进配送商视图
--                  （当前 Java 为有限覆盖，仅 3 个高频配送商查询，见验收指南第四节）
--
-- 回滚：按需 DROP TABLE（注意顺序，先删有外键逻辑的从表）
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. 市场-客户归属（平台客户主权边界，不修改 nx_department.nx_department_dis_id）
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS nx_market_department (
    nx_market_department_id   INT          NOT NULL AUTO_INCREMENT,
    nx_md_market_id           INT          NOT NULL COMMENT 'sys_city_market.sys_city_market_id',
    nx_md_department_id       INT          NOT NULL COMMENT 'nx_department.nx_department_id',
    nx_md_status              VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE|DISABLED',
    nx_md_source              VARCHAR(32)  NOT NULL DEFAULT 'IMPORT' COMMENT 'REGISTER|IMPORT|LEGACY_BIND',
    nx_md_created_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    nx_md_created_by          INT          NULL,
    PRIMARY KEY (nx_market_department_id),
    UNIQUE KEY uk_market_dep (nx_md_market_id, nx_md_department_id),
    KEY idx_md_department (nx_md_department_id),
    KEY idx_md_market_status (nx_md_market_id, nx_md_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台市场管辖客户';

-- -----------------------------------------------------------------------------
-- 2. 客户 + 标准商品 → 当前默认配送商（不含价格/品质标准）
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS nx_department_nx_goods_default (
    nx_dngd_id                     INT          NOT NULL AUTO_INCREMENT,
    nx_dngd_market_id              INT          NOT NULL,
    nx_dngd_department_id          INT          NOT NULL,
    nx_dngd_nx_goods_id            INT          NOT NULL,
    nx_dngd_default_distributer_id INT          NOT NULL,
    nx_dngd_default_dis_goods_id     INT          NOT NULL,
    nx_dngd_source                 VARCHAR(32)  NOT NULL DEFAULT 'MANUAL'
        COMMENT 'MANUAL|AUTO_FIRST|AUTO_SWITCH|FROM_SNAPSHOT',
    nx_dngd_status                 VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE|DISABLED',
    nx_dngd_last_order_id          INT          NULL,
    nx_dngd_last_switch_log_id     INT          NULL,
    nx_dngd_active_snapshot_id     INT          NULL COMMENT 'Phase 2b 快照 ID，2a 不使用',
    nx_dngd_created_at             DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    nx_dngd_created_by             INT          NULL,
    nx_dngd_updated_at             DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    nx_dngd_updated_by             INT          NULL,
    PRIMARY KEY (nx_dngd_id),
    UNIQUE KEY uk_default (nx_dngd_market_id, nx_dngd_department_id, nx_dngd_nx_goods_id),
    KEY idx_default_dis (nx_dngd_default_distributer_id),
    KEY idx_default_dis_goods (nx_dngd_default_dis_goods_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户-标准商品默认供货关系';

-- -----------------------------------------------------------------------------
-- 3. 切换供应商日志（纯历史，不承载当前状态）
-- switch_scope: ORDER_ONLY | ORDER_AND_DEFAULT | ORDER_AND_SNAPSHOT(2b)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS nx_supplier_switch_log (
    nx_ssl_id                    INT          NOT NULL AUTO_INCREMENT,
    nx_ssl_market_id             INT          NOT NULL,
    nx_ssl_department_id         INT          NOT NULL,
    nx_ssl_nx_goods_id           INT          NOT NULL,
    nx_ssl_order_id              INT          NULL COMMENT '触发订单，纯改默认时可空',
    nx_ssl_from_distributer_id   INT          NULL,
    nx_ssl_from_dis_goods_id     INT          NULL,
    nx_ssl_to_distributer_id     INT          NOT NULL,
    nx_ssl_to_dis_goods_id       INT          NOT NULL,
    nx_ssl_switch_scope          VARCHAR(32)  NOT NULL
        COMMENT 'ORDER_ONLY|ORDER_AND_DEFAULT|ORDER_AND_SNAPSHOT',
    nx_ssl_reason_code           VARCHAR(32)  NOT NULL
        COMMENT 'PRICE_LOWER|QUALITY_BETTER|OUT_OF_STOCK|CUSTOMER_COMPLAINT|DELIVERY_DELAY|TEMP_ADJUST|OTHER',
    nx_ssl_reason_note           VARCHAR(512) NULL,
    nx_ssl_snapshot_id           INT          NULL COMMENT 'Phase 2b',
    nx_ssl_snapshot_action       VARCHAR(32)  NOT NULL DEFAULT 'NONE'
        COMMENT 'NONE|COMPARED|UPDATED|SUPERSEDED',
    nx_ssl_operator_id           INT          NOT NULL,
    nx_ssl_created_at            DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (nx_ssl_id),
    KEY idx_ssl_dep_goods (nx_ssl_market_id, nx_ssl_department_id, nx_ssl_nx_goods_id),
    KEY idx_ssl_order (nx_ssl_order_id),
    KEY idx_ssl_created (nx_ssl_created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='配送商切换历史日志';

-- -----------------------------------------------------------------------------
-- 4. 平台订单分配扩展（待分配主权，不污染 nx_do_status 语义）
-- assign_status: PENDING | ASSIGNED | DEVIATION_FLAGGED(2b) | EXCEPTION
-- assign_mode:   PLATFORM | LEGACY_DIS
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS nx_platform_order_assign (
    nx_poa_id                      INT          NOT NULL AUTO_INCREMENT,
    nx_poa_market_id               INT          NOT NULL,
    nx_poa_order_id                INT          NOT NULL COMMENT 'nx_department_orders.nx_department_orders_id',
    nx_poa_department_id           INT          NOT NULL,
    nx_poa_nx_goods_id             INT          NULL,
    nx_poa_assign_status           VARCHAR(16)  NOT NULL DEFAULT 'PENDING',
    nx_poa_assign_mode             VARCHAR(16)  NOT NULL DEFAULT 'PLATFORM',
    nx_poa_assigned_distributer_id  INT          NULL,
    nx_poa_assigned_dis_goods_id   INT          NULL,
    nx_poa_assigned_price          DECIMAL(10, 2) NULL COMMENT '分配时成交价快照，便于审计',
    nx_poa_assigned_at             DATETIME     NULL,
    nx_poa_assigned_by             INT          NULL,
    nx_poa_default_id              INT          NULL COMMENT '分配时引用的 default 行',
    nx_poa_switch_log_id           INT          NULL,
    nx_poa_snapshot_id             INT          NULL COMMENT 'Phase 2b',
    nx_poa_deviation_flags         VARCHAR(256) NULL COMMENT 'Phase 2b 偏离摘要',
    nx_poa_created_at              DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    nx_poa_updated_at              DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (nx_poa_id),
    UNIQUE KEY uk_poa_order (nx_poa_order_id),
    KEY idx_poa_market_status (nx_poa_market_id, nx_poa_assign_status),
    KEY idx_poa_dep_date (nx_poa_department_id, nx_poa_assign_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台订单分配扩展';
