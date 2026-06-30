-- =============================================================================
-- 补丁：nx_community_coupon 券模板元数据列（业务用途 / 领取策略 / 有效期）
--
-- 【何时执行】
--   报错 Unknown column 'nx_cp_validity_type' / 'nx_cp_biz_purpose' / 'nx_cp_claim_strategy'
--   或 saveRuleCoupon / 推广奖励发券读取模板失败
--
-- 【说明】
--   与 docs/sql/nx_customer_promotion_init.sql 第 8 节等价；适用于只执行了
--   upgrade_coupon_rule_v1.sql 而未完整执行推广 init 的环境。
--   不使用 AFTER nx_cp_type（该列可能已被 rule_v1 删除）。
--
-- 【执行前】
--   SHOW COLUMNS FROM nx_community_coupon;
-- 某条 ALTER 报 Duplicate column name 时跳过该句即可。
--
-- 【常见情况】biz_purpose / claim_strategy 已有，只缺 validity 两列时，
--   直接执行下方「剩余段」即可。
-- =============================================================================

ALTER TABLE nx_community_coupon
    ADD COLUMN nx_cp_biz_purpose VARCHAR(32) DEFAULT 'marketing'
        COMMENT 'marketing/referral_register/new_user_gift/compensation';

ALTER TABLE nx_community_coupon
    ADD COLUMN nx_cp_claim_strategy VARCHAR(32) DEFAULT 'public_active'
        COMMENT 'public_active/reward_only/auto_grant';

ALTER TABLE nx_community_coupon
    ADD COLUMN nx_cp_validity_type VARCHAR(16) DEFAULT 'FIXED_DATE'
        COMMENT 'FIXED_DATE/DAYS_AFTER_CLAIM';

ALTER TABLE nx_community_coupon
    ADD COLUMN nx_cp_validity_days INT DEFAULT NULL
        COMMENT '领取后有效天数（DAYS_AFTER_CLAIM 时使用）';

-- -----------------------------------------------------------------------------
-- 剩余段（biz_purpose / claim_strategy 已存在时，只执行本段 + UPDATE）
-- -----------------------------------------------------------------------------
-- ALTER TABLE nx_community_coupon
--     ADD COLUMN nx_cp_validity_type VARCHAR(16) DEFAULT 'FIXED_DATE'
--         COMMENT 'FIXED_DATE/DAYS_AFTER_CLAIM';
--
-- ALTER TABLE nx_community_coupon
--     ADD COLUMN nx_cp_validity_days INT DEFAULT NULL
--         COMMENT '领取后有效天数（DAYS_AFTER_CLAIM 时使用）';

-- 旧券：已有起止日期但未写 validity_type 的，补为固定日期
UPDATE nx_community_coupon
SET nx_cp_validity_type = 'FIXED_DATE'
WHERE (nx_cp_validity_type IS NULL OR nx_cp_validity_type = '')
  AND nx_cp_start_date IS NOT NULL
  AND nx_cp_stop_date IS NOT NULL;

-- SHOW COLUMNS FROM nx_community_coupon;
