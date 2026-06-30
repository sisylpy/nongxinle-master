-- 优果团助手 Phase 1A
-- 独立 ygt_ 表，不写入 nx_community_orders / nx_community_orders_sub。

CREATE TABLE IF NOT EXISTS ygt_wecom_group (
  ygt_wecom_group_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  ygt_wg_corp_id VARCHAR(64) NOT NULL COMMENT '企业微信corpId',
  ygt_wg_chat_id VARCHAR(128) NOT NULL COMMENT '客户群chat_id/roomid',
  ygt_wg_chat_name VARCHAR(255) DEFAULT NULL COMMENT '客户群名称',
  ygt_wg_owner_user_id VARCHAR(128) DEFAULT NULL COMMENT '群主企微userId',
  ygt_wg_member_count INT DEFAULT NULL COMMENT '群成员数',
  ygt_wg_status TINYINT NOT NULL DEFAULT 0 COMMENT '1启用 0停用',
  ygt_wg_nx_community_id INT DEFAULT NULL COMMENT '绑定nxCommunity门店ID，只读引用',
  ygt_wg_leader_user_id INT DEFAULT NULL COMMENT '团长/负责人用户ID，只读引用',
  ygt_wg_admin_user_id INT DEFAULT NULL COMMENT '后台管理员用户ID，只读引用',
  ygt_wg_source VARCHAR(32) NOT NULL DEFAULT 'MANUAL' COMMENT '来源 API/ARCHIVE/MANUAL',
  ygt_wg_create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ygt_wg_update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_ygt_wg_corp_chat (ygt_wg_corp_id, ygt_wg_chat_id),
  KEY idx_ygt_wg_status (ygt_wg_status),
  KEY idx_ygt_wg_community (ygt_wg_nx_community_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优果团企微客户群范围';

CREATE TABLE IF NOT EXISTS ygt_archive_cursor (
  ygt_archive_cursor_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  ygt_ac_corp_id VARCHAR(64) NOT NULL COMMENT '企业微信corpId',
  ygt_ac_chat_id VARCHAR(128) NOT NULL DEFAULT '*' COMMENT '客户群chat_id，*表示corp全局拉取游标',
  ygt_ac_last_seq BIGINT NOT NULL DEFAULT 0 COMMENT '最后处理的会话存档seq',
  ygt_ac_last_pull_status VARCHAR(32) NOT NULL DEFAULT 'INIT' COMMENT 'INIT/SUCCESS/PARTIAL/FAILED',
  ygt_ac_last_error VARCHAR(512) DEFAULT NULL COMMENT '最近失败原因，不含密钥/明文',
  ygt_ac_last_pull_time DATETIME DEFAULT NULL,
  ygt_ac_last_success_time DATETIME DEFAULT NULL,
  ygt_ac_create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ygt_ac_update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_ygt_ac_corp_chat (ygt_ac_corp_id, ygt_ac_chat_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优果团会话存档拉取游标';

CREATE TABLE IF NOT EXISTS ygt_group_buy_campaign (
  ygt_group_buy_campaign_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  ygt_gbc_corp_id VARCHAR(64) NOT NULL,
  ygt_gbc_group_id BIGINT NOT NULL COMMENT 'ygt_wecom_group_id',
  ygt_gbc_nx_community_id INT DEFAULT NULL COMMENT '绑定门店，只读引用',
  ygt_gbc_title VARCHAR(255) NOT NULL,
  ygt_gbc_status VARCHAR(32) NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT/OPEN/CLOSED/CANCELLED',
  ygt_gbc_open_time DATETIME DEFAULT NULL,
  ygt_gbc_close_time DATETIME DEFAULT NULL,
  ygt_gbc_pickup_time DATETIME DEFAULT NULL COMMENT '预计提货时间',
  ygt_gbc_remark VARCHAR(512) DEFAULT NULL,
  ygt_gbc_create_user_id INT DEFAULT NULL,
  ygt_gbc_create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ygt_gbc_update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_ygt_gbc_group_status (ygt_gbc_group_id, ygt_gbc_status),
  KEY idx_ygt_gbc_community (ygt_gbc_nx_community_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优果团团期';

CREATE TABLE IF NOT EXISTS ygt_campaign_goods (
  ygt_campaign_goods_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  ygt_cg_campaign_id BIGINT NOT NULL,
  ygt_cg_nx_community_goods_id INT DEFAULT NULL COMMENT 'nx_community_goods_id，只读引用',
  ygt_cg_goods_name_snapshot VARCHAR(255) NOT NULL,
  ygt_cg_standard_snapshot VARCHAR(128) DEFAULT NULL,
  ygt_cg_price_snapshot DECIMAL(12,2) DEFAULT NULL,
  ygt_cg_sort INT NOT NULL DEFAULT 0,
  ygt_cg_status TINYINT NOT NULL DEFAULT 1,
  ygt_cg_create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ygt_cg_update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_ygt_cg_campaign (ygt_cg_campaign_id),
  KEY idx_ygt_cg_nx_goods (ygt_cg_nx_community_goods_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优果团团期商品';

CREATE TABLE IF NOT EXISTS ygt_chat_message (
  ygt_chat_message_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  ygt_cm_corp_id VARCHAR(64) NOT NULL,
  ygt_cm_group_id BIGINT NOT NULL COMMENT 'ygt_wecom_group_id',
  ygt_cm_chat_id VARCHAR(128) NOT NULL,
  ygt_cm_msg_id VARCHAR(128) DEFAULT NULL,
  ygt_cm_seq BIGINT NOT NULL,
  ygt_cm_public_key_ver INT DEFAULT NULL,
  ygt_cm_action VARCHAR(32) DEFAULT NULL,
  ygt_cm_from_user VARCHAR(128) DEFAULT NULL,
  ygt_cm_msg_time BIGINT DEFAULT NULL,
  ygt_cm_msg_type VARCHAR(32) DEFAULT NULL,
  ygt_cm_content TEXT COMMENT '解密文本内容；接口列表应避免大段返回',
  ygt_cm_raw_json MEDIUMTEXT COMMENT '解密后的原始JSON',
  ygt_cm_parse_status VARCHAR(32) NOT NULL DEFAULT 'NEW' COMMENT 'NEW/CANDIDATE_CREATED/IGNORED/PARSE_FAILED',
  ygt_cm_parse_error VARCHAR(512) DEFAULT NULL,
  ygt_cm_create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ygt_cm_update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_ygt_cm_corp_msg (ygt_cm_corp_id, ygt_cm_msg_id),
  UNIQUE KEY uk_ygt_cm_corp_seq (ygt_cm_corp_id, ygt_cm_seq),
  KEY idx_ygt_cm_chat_time (ygt_cm_chat_id, ygt_cm_msg_time),
  KEY idx_ygt_cm_parse_status (ygt_cm_parse_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优果团企微原始消息';

CREATE TABLE IF NOT EXISTS ygt_order_candidate (
  ygt_order_candidate_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  ygt_oc_corp_id VARCHAR(64) NOT NULL,
  ygt_oc_group_id BIGINT NOT NULL,
  ygt_oc_chat_id VARCHAR(128) NOT NULL,
  ygt_oc_message_id BIGINT NOT NULL,
  ygt_oc_campaign_id BIGINT DEFAULT NULL,
  ygt_oc_from_user VARCHAR(128) DEFAULT NULL,
  ygt_oc_external_user_id VARCHAR(128) DEFAULT NULL,
  ygt_oc_customer_name_snapshot VARCHAR(255) DEFAULT NULL,
  ygt_oc_member_identifier VARCHAR(128) DEFAULT NULL,
  ygt_oc_msg_time BIGINT DEFAULT NULL,
  ygt_oc_original_text TEXT,
  ygt_oc_parse_status VARCHAR(32) NOT NULL DEFAULT 'PLACEHOLDER',
  ygt_oc_status VARCHAR(32) NOT NULL DEFAULT 'PENDING_REVIEW',
  ygt_oc_review_remark VARCHAR(512) DEFAULT NULL,
  ygt_oc_reviewer_user_id INT DEFAULT NULL,
  ygt_oc_reviewed_time DATETIME DEFAULT NULL,
  ygt_oc_create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ygt_oc_update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_ygt_oc_message (ygt_oc_message_id),
  KEY idx_ygt_oc_status (ygt_oc_status),
  KEY idx_ygt_oc_group (ygt_oc_group_id),
  KEY idx_ygt_oc_campaign (ygt_oc_campaign_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优果团订单候选';

CREATE TABLE IF NOT EXISTS ygt_order_candidate_item (
  ygt_order_candidate_item_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  ygt_oci_candidate_id BIGINT NOT NULL,
  ygt_oci_campaign_goods_id BIGINT DEFAULT NULL,
  ygt_oci_nx_community_goods_id INT DEFAULT NULL,
  ygt_oci_goods_name_snapshot VARCHAR(255) DEFAULT NULL,
  ygt_oci_quantity DECIMAL(12,3) DEFAULT NULL,
  ygt_oci_unit VARCHAR(128) DEFAULT NULL,
  ygt_oci_price_snapshot DECIMAL(12,2) DEFAULT NULL,
  ygt_oci_amount DECIMAL(12,2) DEFAULT NULL,
  ygt_oci_remark VARCHAR(512) DEFAULT NULL,
  ygt_oci_confidence DECIMAL(5,2) DEFAULT NULL,
  ygt_oci_manual_adjusted TINYINT NOT NULL DEFAULT 0,
  ygt_oci_create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ygt_oci_update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_ygt_oci_candidate (ygt_oci_candidate_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优果团订单候选明细';

CREATE TABLE IF NOT EXISTS ygt_group_order (
  ygt_group_order_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  ygt_go_candidate_id BIGINT DEFAULT NULL,
  ygt_go_campaign_id BIGINT DEFAULT NULL,
  ygt_go_corp_id VARCHAR(64) DEFAULT NULL,
  ygt_go_group_id BIGINT NOT NULL,
  ygt_go_nx_community_id INT DEFAULT NULL,
  ygt_go_chat_id VARCHAR(128) NOT NULL,
  ygt_go_source_chat_message_id BIGINT DEFAULT NULL,
  ygt_go_from_user VARCHAR(128) DEFAULT NULL,
  ygt_go_member_identifier VARCHAR(128) DEFAULT NULL,
  ygt_go_external_user_id VARCHAR(128) DEFAULT NULL,
  ygt_go_customer_name_snapshot VARCHAR(255) DEFAULT NULL,
  ygt_go_status VARCHAR(32) NOT NULL DEFAULT 'CONFIRMED' COMMENT 'CONFIRMED/CANCELLED',
  ygt_go_pickup_code VARCHAR(64) DEFAULT NULL,
  ygt_go_total_amount DECIMAL(12,2) DEFAULT NULL,
  ygt_go_confirm_user_id INT DEFAULT NULL,
  ygt_go_confirm_time DATETIME DEFAULT NULL,
  ygt_go_remark VARCHAR(512) DEFAULT NULL,
  ygt_go_create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ygt_go_update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_ygt_go_candidate (ygt_go_candidate_id),
  KEY idx_ygt_go_campaign (ygt_go_campaign_id),
  KEY idx_ygt_go_pickup_code (ygt_go_pickup_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优果团正式团购订单';

CREATE TABLE IF NOT EXISTS ygt_group_order_item (
  ygt_group_order_item_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  ygt_goi_order_id BIGINT NOT NULL,
  ygt_goi_campaign_id BIGINT DEFAULT NULL,
  ygt_goi_candidate_item_id BIGINT DEFAULT NULL,
  ygt_goi_campaign_goods_id BIGINT DEFAULT NULL,
  ygt_goi_nx_community_goods_id INT DEFAULT NULL,
  ygt_goi_goods_name_snapshot VARCHAR(255) NOT NULL,
  ygt_goi_spec_snapshot VARCHAR(128) DEFAULT NULL,
  ygt_goi_quantity DECIMAL(12,3) DEFAULT NULL,
  ygt_goi_unit VARCHAR(128) DEFAULT NULL,
  ygt_goi_unit_snapshot VARCHAR(128) DEFAULT NULL,
  ygt_goi_price_snapshot DECIMAL(12,2) DEFAULT NULL,
  ygt_goi_amount DECIMAL(12,2) DEFAULT NULL,
  ygt_goi_remark VARCHAR(512) DEFAULT NULL,
  ygt_goi_create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_ygt_goi_order (ygt_goi_order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优果团正式订单明细';

CREATE TABLE IF NOT EXISTS ygt_verify_log (
  ygt_verify_log_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  ygt_vl_order_id BIGINT NOT NULL,
  ygt_vl_action VARCHAR(32) NOT NULL COMMENT 'VERIFY/REVOKE',
  ygt_vl_operator_user_id INT DEFAULT NULL,
  ygt_vl_remark VARCHAR(512) DEFAULT NULL,
  ygt_vl_create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_ygt_vl_order (ygt_vl_order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优果团核销日志';

CREATE TABLE IF NOT EXISTS ygt_customer_link (
  ygt_customer_link_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  ygt_cl_corp_id VARCHAR(64) NOT NULL,
  ygt_cl_chat_id VARCHAR(128) DEFAULT NULL,
  ygt_cl_external_userid VARCHAR(128) DEFAULT NULL,
  ygt_cl_from_user VARCHAR(128) DEFAULT NULL,
  ygt_cl_display_name VARCHAR(255) DEFAULT NULL,
  ygt_cl_nx_customer_user_id INT DEFAULT NULL COMMENT 'nx_customer_user_id，只读引用',
  ygt_cl_status TINYINT NOT NULL DEFAULT 1,
  ygt_cl_create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ygt_cl_update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_ygt_cl_corp_chat (ygt_cl_corp_id, ygt_cl_chat_id),
  KEY idx_ygt_cl_nx_user (ygt_cl_nx_customer_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优果团企微客户与NxCustomer弱映射';
