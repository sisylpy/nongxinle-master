-- =============================================================================
-- 服务器执行：nx_community_coupon 元数据列补齐
--
-- 适用：saveRuleCoupon 报 Unknown column 'nx_cp_validity_type' 等
--
-- 用法：
--   1. 先连上服务器 MySQL，选中业务库（如 nongxinle）
--   2. 整段执行；某条 ALTER 报 Duplicate column name → 跳过该句，继续下一条
--   3. 最后 SHOW COLUMNS 确认 nx_cp_validity_type / nx_cp_validity_days 存在
-- =============================================================================

-- 执行前确认当前库
SELECT DATABASE();

SHOW COLUMNS FROM nx_community_coupon LIKE 'nx_cp_%';

-- ---------------------------------------------------------------------------
-- 1) 业务用途（已有则跳过）
-- ---------------------------------------------------------------------------
ALTER TABLE nx_community_coupon
    ADD COLUMN nx_cp_biz_purpose VARCHAR(32) DEFAULT 'marketing'
        COMMENT 'marketing/referral_register/new_user_gift/compensation';

-- ---------------------------------------------------------------------------
-- 2) 领取策略（已有则跳过）
-- ---------------------------------------------------------------------------
ALTER TABLE nx_community_coupon
    ADD COLUMN nx_cp_claim_strategy VARCHAR(32) DEFAULT 'public_active'
        COMMENT 'public_active/reward_only/auto_grant';

-- ---------------------------------------------------------------------------
-- 3) 有效期类型（本次报错缺失的核心列）
-- ---------------------------------------------------------------------------
ALTER TABLE nx_community_coupon
    ADD COLUMN nx_cp_validity_type VARCHAR(16) DEFAULT 'FIXED_DATE'
        COMMENT 'FIXED_DATE/DAYS_AFTER_CLAIM';

-- ---------------------------------------------------------------------------
-- 4) 领取后有效天数
-- ---------------------------------------------------------------------------
ALTER TABLE nx_community_coupon
    ADD COLUMN nx_cp_validity_days INT DEFAULT NULL
        COMMENT '领取后有效天数（DAYS_AFTER_CLAIM 时使用）';

-- ---------------------------------------------------------------------------
-- 5) 旧券数据回填：有起止日期但未写 validity_type 的，补为固定日期
-- ---------------------------------------------------------------------------
UPDATE nx_community_coupon
SET nx_cp_validity_type = 'FIXED_DATE'
WHERE (nx_cp_validity_type IS NULL OR nx_cp_validity_type = '')
  AND nx_cp_start_date IS NOT NULL
  AND nx_cp_stop_date IS NOT NULL;

-- ---------------------------------------------------------------------------
-- 6) 执行后验证
-- ---------------------------------------------------------------------------
SHOW COLUMNS FROM nx_community_coupon
WHERE Field IN (
    'nx_cp_biz_purpose',
    'nx_cp_claim_strategy',
    'nx_cp_validity_type',
    'nx_cp_validity_days'
);
