-- =============================================================================
-- 推广模块测试数据清空（TRUNCATE）
--
-- 用途：本地/测试环境重新开始推广人员、推广码、推荐关系、奖励等联调前执行。
--
-- 【会清空】
--   - 外部推广人员 nx_customer_promoter
--   - 统一推广码 / 锁 / 推荐关系 / 无效码尝试 / 奖励记录 / 已读水位
--   - 社区员工推广资格
--   - 推广活动、奖励规则（配置也一并清空，见下方「可选」注释）
--   - 用户券表中来源为 referral_reward 的记录
--
-- 【不会动】
--   - nx_customer_user（C 端用户）
--   - nx_community_coupon（券模板）
--   - 用户主动领取的券（nx_cuc_source_type = active_claim 等）
--
-- 【注意】
--   1. TRUNCATE 不可回滚，执行前请确认库名与环境。
--   2. 若某张表尚未创建，对应语句会报错，可注释掉该行。
--   3. 清空后需重新配置：推广活动、奖励规则，再新建推广人员。
-- =============================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 先删推广奖励发放到用户券里的实例（整表 TRUNCATE 会误删用户主动领的券）
DELETE FROM nx_customer_user_coupon
WHERE nx_cuc_source_type = 'referral_reward';

-- 子表 → 父表
TRUNCATE TABLE nx_customer_referral_reward;
TRUNCATE TABLE nx_customer_user_referral;
TRUNCATE TABLE nx_customer_promotion_code_attempt;
TRUNCATE TABLE nx_customer_promotion_code;
TRUNCATE TABLE nx_customer_promotion_code_owner_lock;
TRUNCATE TABLE nx_customer_promoter;
TRUNCATE TABLE nx_community_user_promotion_eligible;
TRUNCATE TABLE nx_customer_referral_read_state;

-- 配置表：完整联调一般也要清；若只想清人员/业务数据、保留活动与规则，注释下面两行
TRUNCATE TABLE nx_customer_referral_reward_rule;
TRUNCATE TABLE nx_customer_promotion_campaign;

SET FOREIGN_KEY_CHECKS = 1;

-- 执行后可抽查
-- SELECT 'promoter' AS t, COUNT(*) AS c FROM nx_customer_promoter
-- UNION ALL SELECT 'code', COUNT(*) FROM nx_customer_promotion_code
-- UNION ALL SELECT 'referral', COUNT(*) FROM nx_customer_user_referral
-- UNION ALL SELECT 'reward', COUNT(*) FROM nx_customer_referral_reward
-- UNION ALL SELECT 'campaign', COUNT(*) FROM nx_customer_promotion_campaign
-- UNION ALL SELECT 'rule', COUNT(*) FROM nx_customer_referral_reward_rule;
