-- =============================================================================
-- 补丁：nx_customer_user_coupon 推广领奖实例有效期列
--
-- 【何时执行】
--   报错 Unknown column 'nx_cuc_start_date' / 'nx_cuc_stop_date'
--   或推广奖励领取（referral_reward）需要写入用户券有效期快照时
--
-- 【执行前】
--   SHOW COLUMNS FROM nx_customer_user_coupon;
-- =============================================================================

ALTER TABLE nx_customer_user_coupon
    ADD COLUMN nx_cuc_start_date VARCHAR(32) DEFAULT NULL
        COMMENT '实例有效开始日 yyyy-MM-dd' AFTER nx_cuc_share_time;

ALTER TABLE nx_customer_user_coupon
    ADD COLUMN nx_cuc_stop_date VARCHAR(32) DEFAULT NULL
        COMMENT '实例有效截止日 yyyy-MM-dd（含当日）' AFTER nx_cuc_start_date;

-- 若尚未执行来源扩展（init.sql 步骤 9 前半段），按需执行：
-- ALTER TABLE nx_customer_user_coupon ADD COLUMN nx_cuc_source_type VARCHAR(32) DEFAULT 'active_claim' AFTER nx_cuc_type;
-- ALTER TABLE nx_customer_user_coupon ADD COLUMN nx_cuc_source_biz_id INT DEFAULT NULL AFTER nx_cuc_source_type;
-- ALTER TABLE nx_customer_user_coupon ADD UNIQUE KEY uk_cuc_referral_reward_once (nx_cuc_source_type, nx_cuc_source_biz_id);

-- SHOW COLUMNS FROM nx_customer_user_coupon;
