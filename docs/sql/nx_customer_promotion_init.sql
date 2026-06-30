-- =============================================================================
-- NxCustomerUser 推广体系：全新环境初始化 SQL（正式唯一入口之一）
--
-- 【执行条件】
--   1. 本库从未执行过任何推广模块 SQL（含 nx_customer_referral.sql / v2 / 旧版片段）
--   2. 基础表已存在：nx_community_coupon、nx_customer_user_coupon、nx_customer_user、
--      nx_community、nx_community_user（应用运行所需）
--
-- 【不可重复执行】
--   - 本文件含 CREATE TABLE IF NOT EXISTS，但步骤 8–9 的 ALTER 无 IF NOT EXISTS，
--     重复执行会因列/索引已存在而失败。
--   - 若推广表已存在但结构为旧版（例如 campaign 无 commerce_id），CREATE 不会自动升级；
--     请执行 docs/sql/patches/upgrade_nx_customer_promotion_campaign.sql。
--   - 若推广表已存在且结构完整，请改用 nx_customer_promotion_migrate_from_v1v2.sql。
--
-- 【执行方式】整文件在事务外按顺序执行一次；执行前请备份。
-- =============================================================================

-- 1. 外部临时推广人员（兼职/地推/合作方）
CREATE TABLE IF NOT EXISTS nx_customer_promoter (
    nx_customer_promoter_id   INT NOT NULL AUTO_INCREMENT,
    promoter_name             VARCHAR(64) NOT NULL COMMENT '推广员姓名',
    promoter_phone            VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    promoter_type             VARCHAR(32) NOT NULL COMMENT 'FULL_TIME/PART_TIME/PARTNER',
    commerce_id               INT NOT NULL,
    community_id              INT NOT NULL,
    promoter_status           VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/SUSPENDED/TERMINATED',
    cooperation_start_at      DATETIME DEFAULT NULL,
    cooperation_end_at        DATETIME DEFAULT NULL,
    disabled_at               DATETIME DEFAULT NULL,
    disabled_reason           VARCHAR(256) DEFAULT NULL,
    created_at                DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at                DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (nx_customer_promoter_id),
    KEY idx_promoter_community (community_id),
    KEY idx_promoter_commerce (commerce_id),
    KEY idx_promoter_status (promoter_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='外部临时推广员';

-- 1b. 社区正式工作人员推广资格（轻量关联，不复制人员资料）
CREATE TABLE IF NOT EXISTS nx_community_user_promotion_eligible (
    community_user_id   INT NOT NULL COMMENT 'nx_community_user_id',
    enabled             TINYINT NOT NULL DEFAULT 1 COMMENT '1=具备推广资格 0=停用',
    valid_start_at      DATETIME DEFAULT NULL,
    valid_end_at        DATETIME DEFAULT NULL,
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (community_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社区工作人员推广资格';

-- 2. 统一推广码（主权表，不再使用用户表 shareCode 字段）
CREATE TABLE IF NOT EXISTS nx_customer_promotion_code (
    nx_customer_promotion_code_id INT NOT NULL AUTO_INCREMENT,
    promotion_code                VARCHAR(16) NOT NULL COMMENT '推广码字符串',
    owner_type                    VARCHAR(32) NOT NULL COMMENT 'CUSTOMER_USER/COMMUNITY_USER/EXTERNAL_PROMOTER',
    owner_id                      INT NOT NULL COMMENT '用户id、社区员工id或外部推广员id',
    commerce_id                   INT NOT NULL,
    community_id                  INT NOT NULL,
    code_status                   VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/SUSPENDED/DISABLED',
    valid_start_at                DATETIME DEFAULT NULL,
    valid_end_at                  DATETIME DEFAULT NULL,
    disabled_at                   DATETIME DEFAULT NULL,
    disabled_reason               VARCHAR(256) DEFAULT NULL,
    use_count                     INT NOT NULL DEFAULT 0 COMMENT '被尝试使用次数',
    valid_register_count          INT NOT NULL DEFAULT 0 COMMENT '有效注册数',
    invalid_register_count        INT NOT NULL DEFAULT 0 COMMENT '无效注册数',
    reward_rule_id                INT DEFAULT NULL COMMENT '可选关联规则',
    active_owner_slot             VARCHAR(64) GENERATED ALWAYS AS (
        IF(code_status = 'ACTIVE', CONCAT(owner_type, ':', owner_id), NULL)
    ) STORED COMMENT 'ACTIVE 时唯一槽位，防并发双 ACTIVE',
    created_at                    DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at                    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (nx_customer_promotion_code_id),
    UNIQUE KEY uk_promotion_code (promotion_code),
    UNIQUE KEY uk_code_active_owner_slot (active_owner_slot),
    KEY idx_code_owner (owner_type, owner_id),
    KEY idx_code_status (code_status),
    KEY idx_code_community (community_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='统一推广码';

-- 2b. 推广码主体行锁（串行化 create/regenerate/activate 等同主体写操作）
CREATE TABLE IF NOT EXISTS nx_customer_promotion_code_owner_lock (
    owner_type   VARCHAR(32) NOT NULL,
    owner_id     INT NOT NULL,
    created_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (owner_type, owner_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='推广码主体互斥锁行';

-- 3. 推广来源关系（唯一事实主表）
CREATE TABLE IF NOT EXISTS nx_customer_user_referral (
    nx_customer_user_referral_id INT NOT NULL AUTO_INCREMENT,
    invitee_user_id              INT NOT NULL COMMENT '新注册用户',
    promotion_code_id            INT DEFAULT NULL COMMENT '使用的推广码id',
    promotion_code_snapshot      VARCHAR(16) DEFAULT NULL COMMENT '注册时推广码快照',
    source_owner_type            VARCHAR(32) DEFAULT NULL COMMENT 'CUSTOMER_USER/COMMUNITY_USER/EXTERNAL_PROMOTER',
    source_owner_id              INT DEFAULT NULL COMMENT '来源主体id',
    promoter_id                  INT DEFAULT NULL COMMENT 'EXTERNAL_PROMOTER快照，便于报表',
    commerce_id                  INT NOT NULL,
    community_id                 INT NOT NULL,
    share_entry                  VARCHAR(64) DEFAULT NULL,
    qualification_status         VARCHAR(16) NOT NULL DEFAULT 'UNQUALIFIED' COMMENT 'QUALIFIED/UNQUALIFIED',
    invalid_reason               VARCHAR(64) DEFAULT NULL,
    reward_qualified             TINYINT NOT NULL DEFAULT 0 COMMENT '注册时是否具备发券资格',
    reward_rule_id               INT DEFAULT NULL COMMENT '注册时命中奖励规则id快照',
    campaign_id                  INT DEFAULT NULL COMMENT '注册时命中推广活动id快照',
    bind_time                    DATETIME NOT NULL,
    created_at                   DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (nx_customer_user_referral_id),
    UNIQUE KEY uk_referral_invitee (invitee_user_id),
    KEY idx_referral_source (source_owner_type, source_owner_id),
    KEY idx_referral_promoter (promoter_id),
    KEY idx_referral_qualified (qualification_status, bind_time),
    KEY idx_referral_community (community_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='推广来源关系（仅QUALIFIED记录）';

-- 3b. 无效推广码尝试（审计主权，不占用 referral invitee 唯一键）
CREATE TABLE IF NOT EXISTS nx_customer_promotion_code_attempt (
    nx_customer_promotion_code_attempt_id INT NOT NULL AUTO_INCREMENT,
    invitee_user_id              INT NOT NULL,
    promotion_code_id            INT DEFAULT NULL,
    promotion_code_snapshot      VARCHAR(16) DEFAULT NULL,
    source_owner_type            VARCHAR(32) DEFAULT NULL,
    source_owner_id              INT DEFAULT NULL,
    invalid_reason               VARCHAR(64) DEFAULT NULL,
    commerce_id                  INT DEFAULT NULL,
    community_id                 INT DEFAULT NULL,
    share_entry                  VARCHAR(64) DEFAULT NULL,
    attempted_at                 DATETIME NOT NULL,
    created_at                   DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (nx_customer_promotion_code_attempt_id),
    KEY idx_attempt_invitee (invitee_user_id),
    KEY idx_attempt_code (promotion_code_id),
    KEY idx_attempt_time (attempted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='推广码无效尝试审计';

-- 4. 推广活动（决定推广业绩是否计入，与奖励规则分离）
CREATE TABLE IF NOT EXISTS nx_customer_promotion_campaign (
    nx_customer_promotion_campaign_id INT NOT NULL AUTO_INCREMENT,
    community_id                      INT NOT NULL,
    commerce_id                       INT NOT NULL COMMENT '与 community 对应商圈，注册命中维度',
    campaign_code                     VARCHAR(32) NOT NULL COMMENT '活动实例唯一编号（后台创建或系统生成）',
    campaign_scene                    VARCHAR(32) NOT NULL DEFAULT 'REGISTER_ACQUISITION'
        COMMENT '稳定业务场景：注册拉新等活动类型，注册匹配用此字段',
    campaign_name                     VARCHAR(128) DEFAULT NULL,
    owner_scope                       VARCHAR(32) NOT NULL COMMENT 'ALL/CUSTOMER_USER/COMMUNITY_USER/EXTERNAL_PROMOTER',
    campaign_status                   VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/SUSPENDED/TERMINATED',
    effective_start_at                DATETIME DEFAULT NULL,
    effective_end_at                  DATETIME DEFAULT NULL,
    terminated_at                     DATETIME DEFAULT NULL,
    status_reason                     VARCHAR(256) DEFAULT NULL,
    campaign_version                  INT NOT NULL DEFAULT 1,
    created_at                        DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at                        DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (nx_customer_promotion_campaign_id),
    UNIQUE KEY uk_campaign_code (campaign_code),
    KEY idx_campaign_scope (community_id, commerce_id, campaign_scene, owner_scope, campaign_status),
    KEY idx_campaign_time (effective_start_at, effective_end_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='推广活动（业绩资格）';

-- 5. 推广奖励规则（仅负责奖励发放，不决定员工/地推业绩资格）
CREATE TABLE IF NOT EXISTS nx_customer_referral_reward_rule (
    nx_customer_referral_reward_rule_id INT NOT NULL AUTO_INCREMENT,
    community_id                        INT NOT NULL,
    rule_code                           VARCHAR(32) NOT NULL,
    trigger_type                        VARCHAR(32) NOT NULL DEFAULT 'REGISTER',
    reward_target                       VARCHAR(16) NOT NULL COMMENT 'direct_inviter',
    beneficiary_type                    VARCHAR(32) NOT NULL DEFAULT 'CUSTOMER_USER',
    reward_kind                         VARCHAR(16) NOT NULL DEFAULT 'COUPON',
    coupon_template_id                  INT DEFAULT NULL,
    points_amount                       INT DEFAULT NULL,
    enabled                             TINYINT NOT NULL DEFAULT 1,
    rule_status                         TINYINT NOT NULL DEFAULT 1 COMMENT '1=有效 0=停用',
    effective_start_at                  DATETIME DEFAULT NULL,
    effective_end_at                    DATETIME DEFAULT NULL,
    priority                            INT NOT NULL DEFAULT 0,
    rule_version                        INT NOT NULL DEFAULT 1,
    created_at                          DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at                          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (nx_customer_referral_reward_rule_id),
    KEY idx_rule_active (community_id, rule_code, reward_target, rule_status, enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='推广奖励规则（奖励发放）';

-- 奖励规则时间重叠：同一 community + trigger_type + rule_code + reward_target + beneficiary_type
-- 在 enabled=1 且 rule_status=1 时，effective 时间窗不得重叠（闭区间，首尾相接视为重叠）。
-- 应用层解析：queryActiveRuleMatches 返回 0/1/多条；多条时注册成功但不发券，写入 FAILED 奖励记录。

-- 6. 推广奖励记录
CREATE TABLE IF NOT EXISTS nx_customer_referral_reward (
    nx_customer_referral_reward_id INT NOT NULL AUTO_INCREMENT,
    beneficiary_type               VARCHAR(32) NOT NULL DEFAULT 'CUSTOMER_USER',
    beneficiary_user_id            INT NOT NULL COMMENT '普通用户受益人',
    trigger_user_id                INT NOT NULL,
    referral_id                    INT NOT NULL,
    community_id                   INT DEFAULT NULL,
    reward_level                   TINYINT NOT NULL DEFAULT 1,
    reward_kind                    VARCHAR(16) NOT NULL DEFAULT 'COUPON',
    rule_id                        INT DEFAULT NULL,
    coupon_template_id             INT DEFAULT NULL,
    coupon_name_snapshot           VARCHAR(128) DEFAULT NULL,
    coupon_price_snapshot          VARCHAR(32) DEFAULT NULL,
    coupon_original_price_snapshot VARCHAR(32) DEFAULT NULL,
    coupon_words_snapshot          VARCHAR(512) DEFAULT NULL,
    coupon_start_date_snapshot     VARCHAR(32) DEFAULT NULL,
    coupon_stop_date_snapshot      VARCHAR(32) DEFAULT NULL,
    coupon_validity_type_snapshot  VARCHAR(16) DEFAULT NULL COMMENT 'FIXED_DATE/DAYS_AFTER_CLAIM',
    coupon_validity_days_snapshot  INT DEFAULT NULL,
    points_amount                  INT DEFAULT NULL,
    status                         TINYINT NOT NULL DEFAULT 0,
    user_coupon_id                 INT DEFAULT NULL,
    claimed_at                     DATETIME DEFAULT NULL,
    created_at                     DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (nx_customer_referral_reward_id),
    UNIQUE KEY uk_reward_beneficiary_trigger_rule (beneficiary_user_id, trigger_user_id, rule_id),
    KEY idx_reward_beneficiary_status (beneficiary_user_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='推广奖励记录';

-- 7. 推广动态已读水位
CREATE TABLE IF NOT EXISTS nx_customer_referral_read_state (
    user_id               INT NOT NULL,
    last_read_referral_id INT NOT NULL DEFAULT 0,
    last_read_at          DATETIME DEFAULT NULL,
    updated_at            DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='推广注册动态已读水位';

-- 8. 优惠券模板扩展
ALTER TABLE nx_community_coupon
    ADD COLUMN nx_cp_biz_purpose VARCHAR(32) DEFAULT 'marketing'
        COMMENT 'marketing/referral_register/new_user_gift/compensation' AFTER nx_cp_type;

ALTER TABLE nx_community_coupon
    ADD COLUMN nx_cp_claim_strategy VARCHAR(32) DEFAULT 'public_active'
        COMMENT 'public_active/reward_only/auto_grant' AFTER nx_cp_biz_purpose;

ALTER TABLE nx_community_coupon
    ADD COLUMN nx_cp_validity_type VARCHAR(16) DEFAULT 'FIXED_DATE'
        COMMENT 'FIXED_DATE/DAYS_AFTER_CLAIM' AFTER nx_cp_claim_strategy;

ALTER TABLE nx_community_coupon
    ADD COLUMN nx_cp_validity_days INT DEFAULT NULL
        COMMENT '领取后有效天数' AFTER nx_cp_validity_type;

-- 9. 用户优惠券来源扩展
ALTER TABLE nx_customer_user_coupon
    ADD COLUMN nx_cuc_source_type VARCHAR(32) DEFAULT 'active_claim' AFTER nx_cuc_type;

ALTER TABLE nx_customer_user_coupon
    ADD COLUMN nx_cuc_source_biz_id INT DEFAULT NULL AFTER nx_cuc_source_type;

ALTER TABLE nx_customer_user_coupon
    ADD COLUMN nx_cuc_start_date VARCHAR(32) DEFAULT NULL COMMENT '实例有效开始日 yyyy-MM-dd' AFTER nx_cuc_share_time;

ALTER TABLE nx_customer_user_coupon
    ADD COLUMN nx_cuc_stop_date VARCHAR(32) DEFAULT NULL COMMENT '实例有效截止日 yyyy-MM-dd（含当日）' AFTER nx_cuc_start_date;

ALTER TABLE nx_customer_user_coupon
    ADD UNIQUE KEY uk_cuc_referral_reward_once (nx_cuc_source_type, nx_cuc_source_biz_id);
