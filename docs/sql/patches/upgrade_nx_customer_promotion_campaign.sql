-- =============================================================================
-- 补丁：将已存在的 nx_customer_promotion_campaign 升级到与当前 Java 一致
--
-- 【何时执行】
--   报错 Unknown column 'commerce_id' / 'campaign_scene' 等，且表已存在但为旧结构。
--   常见于：曾执行过旧版 init 片段，或 CREATE TABLE IF NOT EXISTS 跳过了重建。
--
-- 【执行前】
--   SHOW COLUMNS FROM nx_customer_promotion_campaign;
--   备份表：CREATE TABLE nx_customer_promotion_campaign_bak AS SELECT * FROM nx_customer_promotion_campaign;
--
-- 【说明】若某列已存在，对应 ALTER 会失败，跳过该句继续执行后续语句即可。
-- =============================================================================

-- 1. commerce_id
ALTER TABLE nx_customer_promotion_campaign
    ADD COLUMN commerce_id INT NULL COMMENT '与 community 对应商圈' AFTER community_id;

UPDATE nx_customer_promotion_campaign c
INNER JOIN nx_community com ON com.nx_community_id = c.community_id
SET c.commerce_id = com.nx_community_commerce_id
WHERE c.commerce_id IS NULL;

UPDATE nx_customer_promotion_campaign
SET commerce_id = 0
WHERE commerce_id IS NULL;

ALTER TABLE nx_customer_promotion_campaign
    MODIFY commerce_id INT NOT NULL COMMENT '与 community 对应商圈，注册命中维度';

-- 2. campaign_scene（稳定业务场景，注册匹配用）
ALTER TABLE nx_customer_promotion_campaign
    ADD COLUMN campaign_scene VARCHAR(32) NOT NULL DEFAULT 'REGISTER_ACQUISITION'
        COMMENT '稳定业务场景：注册拉新' AFTER campaign_code;

UPDATE nx_customer_promotion_campaign
SET campaign_scene = 'REGISTER_ACQUISITION'
WHERE campaign_scene IS NULL OR campaign_scene = '';

-- 3. 活动实例编号：旧数据若仍为 register_promotion，按 id 生成唯一编号
UPDATE nx_customer_promotion_campaign
SET campaign_code = CONCAT('CPN-', community_id, '-', nx_customer_promotion_campaign_id)
WHERE campaign_code IS NULL
   OR campaign_code = ''
   OR campaign_code = 'register_promotion';

-- 4. 其余列
ALTER TABLE nx_customer_promotion_campaign
    ADD COLUMN campaign_name VARCHAR(128) DEFAULT NULL AFTER campaign_scene;

ALTER TABLE nx_customer_promotion_campaign
    ADD COLUMN terminated_at DATETIME DEFAULT NULL AFTER effective_end_at;

ALTER TABLE nx_customer_promotion_campaign
    ADD COLUMN status_reason VARCHAR(256) DEFAULT NULL AFTER terminated_at;

ALTER TABLE nx_customer_promotion_campaign
    ADD COLUMN campaign_version INT NOT NULL DEFAULT 1 AFTER status_reason;

-- 5. campaign_status：TINYINT(1/0) → VARCHAR(ACTIVE/SUSPENDED/TERMINATED)
UPDATE nx_customer_promotion_campaign
SET campaign_status = 'ACTIVE'
WHERE campaign_status IN ('1', 1);

UPDATE nx_customer_promotion_campaign
SET campaign_status = 'SUSPENDED'
WHERE campaign_status IN ('0', 0);

UPDATE nx_customer_promotion_campaign
SET campaign_status = 'ACTIVE'
WHERE campaign_status IS NULL OR campaign_status = '';

ALTER TABLE nx_customer_promotion_campaign
    MODIFY campaign_status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE'
        COMMENT 'ACTIVE/SUSPENDED/TERMINATED';

-- 6. 索引（若已存在会报错，可忽略）
ALTER TABLE nx_customer_promotion_campaign
    ADD UNIQUE KEY uk_campaign_code (campaign_code);

ALTER TABLE nx_customer_promotion_campaign
    ADD KEY idx_campaign_scope (community_id, commerce_id, campaign_scene, owner_scope, campaign_status);

ALTER TABLE nx_customer_promotion_campaign
    ADD KEY idx_campaign_time (effective_start_at, effective_end_at);

-- 7. 验证
-- SHOW COLUMNS FROM nx_customer_promotion_campaign;
-- SELECT nx_customer_promotion_campaign_id, community_id, commerce_id, campaign_code, campaign_scene, campaign_status FROM nx_customer_promotion_campaign;
