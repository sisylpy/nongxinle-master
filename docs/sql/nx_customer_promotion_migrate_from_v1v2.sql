-- =============================================================================
-- NxCustomerUser 推广体系：从 v1/v2 旧结构迁移到最终版（正式唯一迁移入口）
--
-- 【适用条件】
--   本库已执行过 nx_customer_referral.sql 和/或 nx_customer_referral_v2.sql，
--   且尚未完整迁移到 nx_customer_promotion_init.sql 的最终结构。
--
-- 【不适用】
--   全新环境请直接执行 nx_customer_promotion_init.sql，不要执行本文件。
--
-- 【执行方式】
--   1. 全库备份
--   2. 逐步解开注释执行（不要整文件一次性执行）
--   3. 每步执行前先跑对应「前置检查」SQL，确认结构符合预期
-- =============================================================================

-- Step 1. 新建推广员、推广码表
-- 【前置检查】应不存在最终版表：
--   SELECT COUNT(*) FROM information_schema.tables
--   WHERE table_schema = DATABASE() AND table_name = 'nx_customer_promoter';
--   -- 若已为 1 且结构完整，跳过或对照 init.sql 补列
-- 【操作】复制 nx_customer_promotion_init.sql 中 nx_customer_promoter、
--   nx_community_user_promotion_eligible、nx_customer_promotion_code（含 owner_lock）的 CREATE

-- Step 1b. 社区工作人员推广资格表
-- 【前置检查】同上，表名 nx_community_user_promotion_eligible

-- Step 2. 删除用户表旧 shareCode 主权（若存在）
-- 【前置检查】
--   SHOW COLUMNS FROM nx_customer_user LIKE 'nx_cu_referral_code';
--   SHOW INDEX FROM nx_customer_user WHERE Key_name = 'uk_cu_referral_code';
-- 【操作】
-- ALTER TABLE nx_customer_user DROP INDEX uk_cu_referral_code;
-- ALTER TABLE nx_customer_user DROP COLUMN nx_cu_referral_code;

-- Step 3. 改造 referral 表
-- 【前置检查】
--   SHOW COLUMNS FROM nx_customer_user_referral LIKE 'inviter_user_id';
--   SHOW COLUMNS FROM nx_customer_user_referral LIKE 'source_owner_type';
--   -- 若已有 source_owner_type 且无 inviter_user_id，说明 Step 3 已完成
-- 【操作】
-- ALTER TABLE nx_customer_user_referral
--     ADD COLUMN promotion_code_id INT NULL AFTER invitee_user_id,
--     ...
-- UPDATE ... SET source_owner_type = 'CUSTOMER_USER', source_owner_id = inviter_user_id ...
-- ALTER TABLE nx_customer_user_referral DROP COLUMN inviter_user_id;

-- Step 4. 将旧用户 shareCode 迁移到推广码表
-- 【前置检查】
--   SELECT COUNT(*) FROM nx_customer_user WHERE nx_cu_referral_code IS NOT NULL;
--   SELECT COUNT(*) FROM nx_customer_promotion_code;
-- 【操作】INSERT INTO nx_customer_promotion_code ... SELECT ...

-- Step 5. 奖励规则扩展
-- 【前置检查】
--   SHOW COLUMNS FROM nx_customer_referral_reward_rule LIKE 'rule_status';
-- 【若 rule_status 不存在】执行整文件：
--   docs/sql/patches/upgrade_nx_customer_referral_reward_rule.sql

-- Step 6. 奖励记录扩展
-- 【前置检查】
--   SHOW COLUMNS FROM nx_customer_referral_reward LIKE 'coupon_validity_type_snapshot';
-- 【操作】ALTER TABLE nx_customer_referral_reward ...

-- Step 7. 新增推广活动表、无效尝试表、referral.campaign_id
-- 【前置检查】
--   SHOW TABLES LIKE 'nx_customer_promotion_campaign';
--   SHOW COLUMNS FROM nx_customer_user_referral LIKE 'campaign_id';
-- 【若 campaign_id 不存在】执行：
--   docs/sql/patches/upgrade_nx_customer_user_referral.sql

-- Step 7b. 推广码并发控制
-- 【前置检查】
--   SHOW TABLES LIKE 'nx_customer_promotion_code_owner_lock';
--   SHOW COLUMNS FROM nx_customer_promotion_code LIKE 'active_owner_slot';

-- Step 7c. 推广活动表升级（campaign_scene、commerce_id 等）
-- 【前置检查】
--   SHOW COLUMNS FROM nx_customer_promotion_campaign LIKE 'commerce_id';
--   SHOW COLUMNS FROM nx_customer_promotion_campaign LIKE 'campaign_scene';
-- 【若 commerce_id 不存在】直接执行整文件：
--   docs/sql/patches/upgrade_nx_customer_promotion_campaign.sql
-- 【或手工】见该 patch 文件内分步 ALTER + 回填 commerce_id（来自 nx_community.nx_community_commerce_id）

-- Step 7d. 用户券实例有效期列（推广领奖 DAYS_AFTER_CLAIM）
-- 【前置检查】
--   SHOW COLUMNS FROM nx_customer_user_coupon LIKE 'nx_cuc_start_date';
-- 【操作】
-- ALTER TABLE nx_customer_user_coupon ADD COLUMN nx_cuc_start_date VARCHAR(32) NULL ...;
-- ALTER TABLE nx_customer_user_coupon ADD COLUMN nx_cuc_stop_date VARCHAR(32) NULL ...;

-- Step 8. COMPANY_PROMOTER → EXTERNAL_PROMOTER
-- 【前置检查】
--   SELECT COUNT(*) FROM nx_customer_promotion_code WHERE owner_type='COMPANY_PROMOTER';
-- 【操作】
-- UPDATE nx_customer_promotion_code SET owner_type='EXTERNAL_PROMOTER' WHERE owner_type='COMPANY_PROMOTER';
-- UPDATE nx_customer_user_referral SET source_owner_type='EXTERNAL_PROMOTER' WHERE source_owner_type='COMPANY_PROMOTER';
