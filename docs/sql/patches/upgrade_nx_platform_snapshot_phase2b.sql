-- =============================================================================
-- 批发市场平台化 Phase 2b 预埋：客户商品供货快照（2a 不实现业务）
--
-- 索引说明：
--   - uk_snapshot_version：同一客户+标准商品下版本号唯一（允许多条 SUPERSEDED）
--   - 同一 (market, department, nxGoods) 仅一条 ACTIVE：由 Service 事务保证，
--     新建 ACTIVE 前将旧 ACTIVE 改为 SUPERSEDED；不在 DB 层用含 status 的唯一索引
-- =============================================================================

CREATE TABLE IF NOT EXISTS nx_department_nx_goods_snapshot (
    nx_dngs_id                       INT            NOT NULL AUTO_INCREMENT,
    nx_dngs_market_id                INT            NOT NULL,
    nx_dngs_department_id            INT            NOT NULL,
    nx_dngs_nx_goods_id              INT            NOT NULL,
    nx_dngs_snapshot_version         INT            NOT NULL COMMENT '同组递增，从 1 开始',
    nx_dngs_snapshot_name            VARCHAR(128)   NULL,
    nx_dngs_status                   VARCHAR(16)    NOT NULL DEFAULT 'DRAFT'
        COMMENT 'DRAFT|ACTIVE|SUPERSEDED|EXPIRED|DISABLED',
    nx_dngs_default_distributer_id   INT            NOT NULL,
    nx_dngs_default_dis_goods_id     INT            NOT NULL,
    nx_dngs_snapshot_price           DECIMAL(10, 2) NOT NULL COMMENT '参考价，非锁死价',
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
    nx_dngs_price_tolerance_type     VARCHAR(16)    NOT NULL DEFAULT 'PERCENT' COMMENT 'PERCENT|FIXED',
    nx_dngs_price_tolerance_value    DECIMAL(10, 2) NOT NULL DEFAULT 10.00,
    nx_dngs_allow_supplier_switch    TINYINT        NOT NULL DEFAULT 1,
    nx_dngs_allow_quality_substitution TINYINT      NOT NULL DEFAULT 0,
    nx_dngs_superseded_by_id         INT            NULL COMMENT '被哪个新版本取代',
    nx_dngs_created_by               INT            NOT NULL,
    nx_dngs_created_at               DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    nx_dngs_updated_by               INT            NULL,
    nx_dngs_updated_at               DATETIME       NULL,
    nx_dngs_accepted_by              INT            NULL,
    nx_dngs_accepted_at               DATETIME       NULL,
    nx_dngs_last_compared_order_id   INT            NULL,
    nx_dngs_last_compared_at         DATETIME       NULL,
    PRIMARY KEY (nx_dngs_id),
    UNIQUE KEY uk_snapshot_version (
        nx_dngs_market_id,
        nx_dngs_department_id,
        nx_dngs_nx_goods_id,
        nx_dngs_snapshot_version
    ),
    KEY idx_snapshot_group_status (
        nx_dngs_market_id,
        nx_dngs_department_id,
        nx_dngs_nx_goods_id,
        nx_dngs_status
    ),
    KEY idx_snapshot_superseded (nx_dngs_superseded_by_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户认可供货标准快照（含版本）';

-- 来源订单必须用子表追溯，不用 JSON 作主存储
CREATE TABLE IF NOT EXISTS nx_department_nx_goods_snapshot_order (
    nx_dngso_id           INT         NOT NULL AUTO_INCREMENT,
    nx_dngso_snapshot_id  INT         NOT NULL,
    nx_dngso_order_id     INT         NOT NULL,
    nx_dngso_order_source VARCHAR(16) NOT NULL DEFAULT 'LIVE' COMMENT 'LIVE|HISTORY',
    PRIMARY KEY (nx_dngso_id),
    UNIQUE KEY uk_snapshot_order (nx_dngso_snapshot_id, nx_dngso_order_id, nx_dngso_order_source),
    KEY idx_dngso_order (nx_dngso_order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='快照来源订单';
