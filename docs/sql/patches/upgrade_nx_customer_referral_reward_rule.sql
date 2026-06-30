-- =============================================================================
-- 补丁：nx_customer_referral_reward_rule 奖励规则表结构补齐
--
-- 【何时执行】
--   报错 Unknown column 'rule_status' / 'trigger_type' / 'beneficiary_type' 等
--   注册带 promotionCode 时在 queryActiveRuleMatches 失败
--
-- 【重要】请执行本文件全部 ALTER，不要只加单列；已存在的列报 Duplicate 则跳过该句。
--
-- 【执行前】
--   SHOW COLUMNS FROM nx_customer_referral_reward_rule;
-- 【执行后对照 init.sql 第 5 节，至少应有】
--   rule_code, trigger_type, reward_target, beneficiary_type, reward_kind,
--   coupon_template_id, points_amount, enabled, rule_status,
--   effective_start_at, effective_end_at, priority, rule_version, created_at, updated_at
-- =============================================================================

ALTER TABLE nx_customer_referral_reward_rule
    ADD COLUMN trigger_type VARCHAR(32) NOT NULL DEFAULT 'REGISTER'
        COMMENT 'REGISTER 等' AFTER rule_code;

ALTER TABLE nx_customer_referral_reward_rule
    ADD COLUMN beneficiary_type VARCHAR(32) NOT NULL DEFAULT 'CUSTOMER_USER'
        COMMENT 'CUSTOMER_USER/COMMUNITY_USER/EXTERNAL_PROMOTER' AFTER reward_target;

ALTER TABLE nx_customer_referral_reward_rule
    ADD COLUMN reward_kind VARCHAR(16) NOT NULL DEFAULT 'COUPON'
        COMMENT 'COUPON/POINTS/...' AFTER beneficiary_type;

ALTER TABLE nx_customer_referral_reward_rule
    ADD COLUMN rule_status TINYINT NOT NULL DEFAULT 1
        COMMENT '1=有效 0=停用' AFTER enabled;

ALTER TABLE nx_customer_referral_reward_rule
    ADD COLUMN effective_start_at DATETIME DEFAULT NULL AFTER rule_status;

ALTER TABLE nx_customer_referral_reward_rule
    ADD COLUMN effective_end_at DATETIME DEFAULT NULL AFTER effective_start_at;

ALTER TABLE nx_customer_referral_reward_rule
    ADD COLUMN priority INT NOT NULL DEFAULT 0 AFTER effective_end_at;

ALTER TABLE nx_customer_referral_reward_rule
    ADD COLUMN rule_version INT NOT NULL DEFAULT 1 AFTER priority;

ALTER TABLE nx_customer_referral_reward_rule
    ADD COLUMN created_at DATETIME DEFAULT CURRENT_TIMESTAMP AFTER rule_version;

ALTER TABLE nx_customer_referral_reward_rule
    ADD COLUMN updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER created_at;

-- -----------------------------------------------------------------------------
-- 若前面列已加过，只需执行下面「剩余段」（从 effective_start_at 到 updated_at）：
-- -----------------------------------------------------------------------------
-- ALTER TABLE nx_customer_referral_reward_rule ADD COLUMN effective_start_at DATETIME DEFAULT NULL AFTER rule_status;
-- ALTER TABLE nx_customer_referral_reward_rule ADD COLUMN effective_end_at DATETIME DEFAULT NULL AFTER effective_start_at;
-- ALTER TABLE nx_customer_referral_reward_rule ADD COLUMN priority INT NOT NULL DEFAULT 0 AFTER effective_end_at;
-- ALTER TABLE nx_customer_referral_reward_rule ADD COLUMN rule_version INT NOT NULL DEFAULT 1 AFTER priority;
-- ALTER TABLE nx_customer_referral_reward_rule ADD COLUMN created_at DATETIME DEFAULT CURRENT_TIMESTAMP AFTER rule_version;
-- ALTER TABLE nx_customer_referral_reward_rule ADD COLUMN updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER created_at;

-- SHOW COLUMNS FROM nx_customer_referral_reward_rule;
