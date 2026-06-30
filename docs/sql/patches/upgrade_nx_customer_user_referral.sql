-- =============================================================================
-- 补丁：将已存在的 nx_customer_user_referral 升级到与当前 Java 一致
--
-- 【何时执行】
--   报错 Unknown column 'campaign_id' / 'promotion_code_id' / 'source_owner_type' 等。
--
-- 【执行前】
--   SHOW COLUMNS FROM nx_customer_user_referral;
--   备份：CREATE TABLE nx_customer_user_referral_bak AS SELECT * FROM nx_customer_user_referral;
--
-- 【说明】若某列已存在，对应 ALTER 会失败，跳过该句继续即可。
-- =============================================================================

-- 1. 推广码与来源主体（若从 v1 inviter_user_id 迁移，请先完成 Step 3 数据迁移再删 inviter 列）
ALTER TABLE nx_customer_user_referral
    ADD COLUMN promotion_code_id INT DEFAULT NULL COMMENT '使用的推广码id' AFTER invitee_user_id;

ALTER TABLE nx_customer_user_referral
    ADD COLUMN promotion_code_snapshot VARCHAR(16) DEFAULT NULL COMMENT '注册时推广码快照' AFTER promotion_code_id;

ALTER TABLE nx_customer_user_referral
    ADD COLUMN source_owner_type VARCHAR(32) DEFAULT NULL COMMENT 'CUSTOMER_USER/COMMUNITY_USER/EXTERNAL_PROMOTER' AFTER promotion_code_snapshot;

ALTER TABLE nx_customer_user_referral
    ADD COLUMN source_owner_id INT DEFAULT NULL COMMENT '来源主体id' AFTER source_owner_type;

ALTER TABLE nx_customer_user_referral
    ADD COLUMN promoter_id INT DEFAULT NULL COMMENT 'EXTERNAL_PROMOTER快照' AFTER source_owner_id;

-- 2. 资格与奖励快照
ALTER TABLE nx_customer_user_referral
    ADD COLUMN qualification_status VARCHAR(16) NOT NULL DEFAULT 'QUALIFIED' COMMENT 'QUALIFIED/UNQUALIFIED' AFTER share_entry;

ALTER TABLE nx_customer_user_referral
    ADD COLUMN invalid_reason VARCHAR(64) DEFAULT NULL AFTER qualification_status;

ALTER TABLE nx_customer_user_referral
    ADD COLUMN reward_qualified TINYINT NOT NULL DEFAULT 0 COMMENT '注册时是否具备发券资格' AFTER invalid_reason;

ALTER TABLE nx_customer_user_referral
    ADD COLUMN reward_rule_id INT DEFAULT NULL COMMENT '注册时命中奖励规则id快照' AFTER reward_qualified;

-- 3. 活动快照（本次报错缺失列；不依赖前置列名，直接追加）
ALTER TABLE nx_customer_user_referral
    ADD COLUMN campaign_id INT DEFAULT NULL COMMENT '注册时命中推广活动id快照';

-- 4. 若仍为旧数据且存在 inviter_user_id，可一次性回填 CUSTOMER_USER 来源（按需执行）
-- UPDATE nx_customer_user_referral
-- SET source_owner_type = 'CUSTOMER_USER',
--     source_owner_id = inviter_user_id,
--     qualification_status = 'QUALIFIED',
--     reward_qualified = 1
-- WHERE inviter_user_id IS NOT NULL
--   AND (source_owner_id IS NULL OR source_owner_type IS NULL);

-- 5. 索引（已存在则跳过）
ALTER TABLE nx_customer_user_referral
    ADD KEY idx_referral_source (source_owner_type, source_owner_id);

ALTER TABLE nx_customer_user_referral
    ADD KEY idx_referral_promoter (promoter_id);

ALTER TABLE nx_customer_user_referral
    ADD KEY idx_referral_qualified (qualification_status, bind_time);

-- 6. 旧版 inviter_user_id 收尾（本次报错：Field 'inviter_user_id' doesn't have a default value）
-- 【说明】v1 仅 C 用户邀请 C 用户，用 inviter_user_id；最终版用 source_owner_type + source_owner_id
--         支持 CUSTOMER_USER / COMMUNITY_USER / EXTERNAL_PROMOTER 三类主体，不再写 inviter_user_id。
-- 【执行前确认】SHOW COLUMNS FROM nx_customer_user_referral LIKE 'inviter_user_id';
-- 6a. 若有历史数据，先回填新字段（按需取消注释）
-- UPDATE nx_customer_user_referral
-- SET source_owner_type = 'CUSTOMER_USER',
--     source_owner_id = inviter_user_id,
--     qualification_status = IFNULL(qualification_status, 'QUALIFIED'),
--     reward_qualified = IF(reward_qualified IS NULL OR reward_qualified = 0, 1, reward_qualified)
-- WHERE inviter_user_id IS NOT NULL
--   AND (source_owner_id IS NULL OR source_owner_type IS NULL);

-- 6b. 删除旧列（推荐；Java insert 不再写入 inviter_user_id）
ALTER TABLE nx_customer_user_referral DROP COLUMN inviter_user_id;

-- 若暂时不能删列，可改为允许 NULL（不推荐长期保留）：
-- ALTER TABLE nx_customer_user_referral MODIFY COLUMN inviter_user_id INT NULL DEFAULT NULL;

-- 7. 验证
-- SHOW COLUMNS FROM nx_customer_user_referral;
-- SELECT nx_customer_user_referral_id, invitee_user_id, campaign_id, qualification_status FROM nx_customer_user_referral LIMIT 5;
