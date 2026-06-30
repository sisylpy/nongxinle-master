-- =============================================================================
-- 京采平台 · 市场后台用户（Phase 1a-0）
-- platform_market_user + platform_market_user_session
-- 幂等：CREATE TABLE IF NOT EXISTS
-- =============================================================================

CREATE TABLE IF NOT EXISTS platform_market_user (
    pmu_id           INT          NOT NULL AUTO_INCREMENT COMMENT 'PK',
    market_id        INT          NOT NULL COMMENT 'sys_city_market.sys_city_market_id',
    login_account    VARCHAR(64)  NOT NULL COMMENT '登录账号',
    phone            VARCHAR(32)  NULL COMMENT '手机号',
    password_hash    VARCHAR(128) NOT NULL COMMENT 'SHA-256 hex',
    real_name        VARCHAR(64)  NULL COMMENT '姓名',
    role_type        VARCHAR(32)  NOT NULL DEFAULT 'OPERATOR' COMMENT 'ADMIN/OPERATOR/FINANCE/CUSTOMER_SERVICE',
    status           VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/DISABLED',
    last_login_at    DATETIME     NULL,
    created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (pmu_id),
    UNIQUE KEY uk_pmu_market_login (market_id, login_account),
    KEY idx_pmu_market (market_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='京采市场 Electron 后台用户';

CREATE TABLE IF NOT EXISTS platform_market_user_session (
    pms_id     BIGINT       NOT NULL AUTO_INCREMENT,
    pmu_id     INT          NOT NULL,
    token      VARCHAR(64)  NOT NULL COMMENT 'opaque session token',
    expire_at  DATETIME     NOT NULL,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (pms_id),
    UNIQUE KEY uk_pms_token (token),
    KEY idx_pms_pmu (pmu_id),
    KEY idx_pms_expire (expire_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='市场后台登录会话';

SELECT 'platform_market_user' AS check_item,
       COUNT(*) AS table_exists
FROM information_schema.tables
WHERE table_schema = DATABASE() AND table_name = 'platform_market_user';
