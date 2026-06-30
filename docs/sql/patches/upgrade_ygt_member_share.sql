-- 优果团分享注册链路
-- 仅新增 ygt_ 表；不写 nx_community_orders / nx_community_orders_sub / 购物车 / POS / checkout / 老商城优惠券核销。

CREATE TABLE IF NOT EXISTS ygt_member_share_invite (
  ygt_member_share_invite_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  ygt_msi_share_code VARCHAR(32) NOT NULL COMMENT '不可猜分享码',
  ygt_msi_source_type VARCHAR(32) NOT NULL DEFAULT 'GROUP_OWNER' COMMENT 'GROUP_OWNER/MEMBER/SYSTEM',
  ygt_msi_inviter_customer_user_id INT DEFAULT NULL COMMENT '分享人 nx_customer_user_id，只读引用',
  ygt_msi_wecom_group_id BIGINT NOT NULL COMMENT 'ygt_wecom_group_id',
  ygt_msi_campaign_id BIGINT NOT NULL COMMENT 'ygt_group_buy_campaign_id',
  ygt_msi_corp_id VARCHAR(64) DEFAULT NULL,
  ygt_msi_chat_id VARCHAR(128) DEFAULT NULL,
  ygt_msi_community_id INT DEFAULT NULL COMMENT 'nx_community_id，只读引用',
  ygt_msi_status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/DISABLED/EXPIRED',
  ygt_msi_expire_time DATETIME DEFAULT NULL,
  ygt_msi_create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ygt_msi_update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_ygt_msi_share_code (ygt_msi_share_code),
  KEY idx_ygt_msi_campaign_group (ygt_msi_campaign_id, ygt_msi_wecom_group_id),
  KEY idx_ygt_msi_inviter (ygt_msi_inviter_customer_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优果团分享注册链接';

CREATE TABLE IF NOT EXISTS ygt_member_join_source (
  ygt_member_join_source_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  ygt_mjs_customer_user_id INT NOT NULL COMMENT 'nx_customer_user_id，只读引用',
  ygt_mjs_open_id VARCHAR(128) DEFAULT NULL,
  ygt_mjs_union_id VARCHAR(128) DEFAULT NULL,
  ygt_mjs_phone_snapshot VARCHAR(64) DEFAULT NULL,
  ygt_mjs_nickname_snapshot VARCHAR(255) DEFAULT NULL,
  ygt_mjs_source_campaign_id BIGINT NOT NULL,
  ygt_mjs_source_wecom_group_id BIGINT NOT NULL,
  ygt_mjs_source_share_code VARCHAR(32) NOT NULL,
  ygt_mjs_inviter_customer_user_id INT DEFAULT NULL,
  ygt_mjs_bind_status VARCHAR(32) NOT NULL DEFAULT 'SOURCE_REGISTERED' COMMENT 'SOURCE_REGISTERED/BOUND/CONFLICT/UNBOUND',
  ygt_mjs_bind_source VARCHAR(32) NOT NULL DEFAULT 'SHARE_REGISTER' COMMENT 'SHARE_REGISTER/MANUAL_BIND/SELF_BIND/AUTO_MATCH',
  ygt_mjs_create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ygt_mjs_update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_ygt_mjs_customer_share (ygt_mjs_customer_user_id, ygt_mjs_source_share_code),
  KEY idx_ygt_mjs_campaign_group (ygt_mjs_source_campaign_id, ygt_mjs_source_wecom_group_id),
  KEY idx_ygt_mjs_status (ygt_mjs_bind_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优果团会员分享注册来源';

CREATE TABLE IF NOT EXISTS ygt_member_benefit_log (
  ygt_member_benefit_log_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  ygt_mbl_join_source_id BIGINT NOT NULL,
  ygt_mbl_customer_user_id INT NOT NULL,
  ygt_mbl_benefit_code VARCHAR(64) NOT NULL,
  ygt_mbl_benefit_name VARCHAR(128) NOT NULL,
  ygt_mbl_discount_text VARCHAR(128) DEFAULT NULL,
  ygt_mbl_use_channel VARCHAR(32) NOT NULL DEFAULT 'YOUGUOTUAN',
  ygt_mbl_status VARCHAR(32) NOT NULL DEFAULT 'PLACEHOLDER' COMMENT 'PLACEHOLDER/ISSUED/CANCELLED',
  ygt_mbl_create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_ygt_mbl_source_code (ygt_mbl_join_source_id, ygt_mbl_benefit_code),
  KEY idx_ygt_mbl_customer (ygt_mbl_customer_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优果团新人福利占位记录';
