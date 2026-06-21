-- =============================================================================
-- 京采平台 · 优惠券 Phase 1a（市场模板 + 门店实例 + 审计日志）
-- 禁止复用 nx_community_coupon / nx_customer_user_coupon
-- 幂等：CREATE TABLE IF NOT EXISTS
-- =============================================================================

CREATE TABLE IF NOT EXISTS platform_coupon_template (
    pct_id                      INT            NOT NULL AUTO_INCREMENT COMMENT 'PK',
    market_id                   INT            NOT NULL COMMENT 'sys_city_market.sys_city_market_id',
    template_name               VARCHAR(128)   NOT NULL COMMENT '展示名称',
    coupon_type                 VARCHAR(32)    NOT NULL COMMENT 'CASH / FULL_REDUCTION',
    discount_amount             DECIMAL(12,2)  NOT NULL COMMENT '优惠额',
    threshold_amount            DECIMAL(12,2)  NOT NULL DEFAULT 0 COMMENT '满减门槛',
    scope_type                  VARCHAR(32)    NOT NULL DEFAULT 'ALL' COMMENT 'ALL/CATEGORY/GOODS',
    scope_ref_ids               TEXT           NULL COMMENT 'JSON 数组：平台商品/分类 id',
    use_channel                 VARCHAR(32)    NOT NULL DEFAULT 'ALL' COMMENT 'ALL/PLATFORM_MINIAPP/POS',
    biz_purpose                 VARCHAR(32)    NOT NULL DEFAULT 'marketing' COMMENT 'marketing/manual/referral_reward/promotion_reward',
    claim_strategy              VARCHAR(32)    NOT NULL DEFAULT 'public_active' COMMENT 'public_active/reward_only/auto_grant',
    validity_type               VARCHAR(32)    NOT NULL COMMENT 'FIXED_DATE / DAYS_AFTER_CLAIM',
    validity_days               INT            NULL COMMENT '领取后有效天数',
    start_date                  VARCHAR(32)    NULL COMMENT '固定有效期开始 yyyy-MM-dd',
    stop_date                   VARCHAR(32)    NULL COMMENT '固定有效期结束 yyyy-MM-dd',
    status                      VARCHAR(16)    NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/DISABLED',
    issue_count                 INT            NOT NULL DEFAULT 0 COMMENT '已发放实例数',
    use_count                   INT            NOT NULL DEFAULT 0 COMMENT '已使用数（后续阶段）',
    created_by_market_user_id   INT            NULL COMMENT '创建人 pmu_id 审计',
    updated_by_market_user_id   INT            NULL COMMENT '更新人 pmu_id 审计',
    created_at                  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (pct_id),
    KEY idx_pct_market (market_id),
    KEY idx_pct_market_status (market_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='京采市场券模板';

CREATE TABLE IF NOT EXISTS platform_store_coupon (
    psc_id                      INT            NOT NULL AUTO_INCREMENT COMMENT 'PK',
    template_id                 INT            NOT NULL COMMENT 'platform_coupon_template.pct_id',
    market_id                   INT            NOT NULL COMMENT '市场维度',
    store_gb_department_id      INT            NOT NULL COMMENT '门店级 gb_department_id',
    status                      VARCHAR(16)    NOT NULL DEFAULT 'AVAILABLE' COMMENT 'AVAILABLE/LOCKED/USED/EXPIRED/VOID',
    source_type                 VARCHAR(32)    NOT NULL DEFAULT 'manual' COMMENT 'manual/active_claim/referral_reward/promotion_reward',
    source_biz_id               VARCHAR(64)    NULL,
    start_date                  VARCHAR(32)    NULL,
    stop_date                   VARCHAR(32)    NULL,
    locked_checkout_token       VARCHAR(64)    NULL COMMENT '锁券 checkout token（预留）',
    locked_payment_id           INT            NULL COMMENT 'platform_checkout_payment.pcp_id（预留）',
    issued_by_market_user_id    INT            NULL COMMENT '市场后台发券人 pmu_id',
    claimed_by_user_id          INT            NULL COMMENT '饭店端领券人 gb_department_user_id 审计',
    used_by_user_id             INT            NULL COMMENT '用券人 gb_department_user_id 审计',
    used_bill_id                INT            NULL COMMENT '核销 bill（预留）',
    used_at                     DATETIME       NULL,
    created_at                  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (psc_id),
    KEY idx_psc_market_store (market_id, store_gb_department_id),
    KEY idx_psc_template (template_id),
    KEY idx_psc_store_status (store_gb_department_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='京采门店持券实例';

CREATE TABLE IF NOT EXISTS platform_coupon_usage_log (
    pcul_id                     BIGINT         NOT NULL AUTO_INCREMENT,
    store_coupon_id             INT            NOT NULL,
    template_id                 INT            NOT NULL,
    market_id                   INT            NOT NULL,
    store_gb_department_id      INT            NOT NULL,
    verify_type                 VARCHAR(32)    NOT NULL COMMENT 'ISSUE/LOCK/VERIFY/UNLOCK/EXPIRE/VOID',
    checkout_token              VARCHAR(64)    NULL,
    payment_id                  INT            NULL,
    bill_id                     INT            NULL,
    discount_amount             DECIMAL(12,2)  NULL,
    before_status               VARCHAR(16)    NULL,
    after_status                VARCHAR(16)    NULL,
    operator_type               VARCHAR(32)    NOT NULL COMMENT 'MARKET_USER/STORE_USER/SYSTEM',
    operator_id                 INT            NULL,
    created_at                  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (pcul_id),
    KEY idx_pcul_store_coupon (store_coupon_id),
    KEY idx_pcul_market (market_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='京采平台券状态/核销审计';

SELECT 'platform_coupon_template' AS check_item,
       COUNT(*) AS table_exists
FROM information_schema.tables
WHERE table_schema = DATABASE() AND table_name = 'platform_coupon_template';

SELECT 'platform_store_coupon' AS check_item,
       COUNT(*) AS table_exists
FROM information_schema.tables
WHERE table_schema = DATABASE() AND table_name = 'platform_store_coupon';
