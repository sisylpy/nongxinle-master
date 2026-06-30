-- 优果团助手 Phase 1B
-- 仅升级 ygt_ 表；不写入 nx_community_orders / nx_community_orders_sub / 购物车 / 优惠券 / POS / checkout。
-- 如果已经执行过 Phase 1A 建表脚本，再执行本文件；如果是新库，可直接执行更新后的 upgrade_ygt_phase1a.sql。

ALTER TABLE ygt_group_buy_campaign
  ADD COLUMN ygt_gbc_pickup_time DATETIME DEFAULT NULL COMMENT '预计提货时间' AFTER ygt_gbc_close_time,
  ADD COLUMN ygt_gbc_remark VARCHAR(512) DEFAULT NULL AFTER ygt_gbc_pickup_time;

ALTER TABLE ygt_campaign_goods
  ADD COLUMN ygt_cg_unit_snapshot VARCHAR(32) DEFAULT NULL COMMENT '单位快照' AFTER ygt_cg_standard_snapshot,
  ADD COLUMN ygt_cg_sort INT NOT NULL DEFAULT 0 AFTER ygt_cg_price_snapshot;

ALTER TABLE ygt_order_candidate
  ADD COLUMN ygt_oc_external_user_id VARCHAR(128) DEFAULT NULL AFTER ygt_oc_from_user,
  ADD COLUMN ygt_oc_customer_name_snapshot VARCHAR(255) DEFAULT NULL AFTER ygt_oc_external_user_id,
  ADD COLUMN ygt_oc_member_identifier VARCHAR(128) DEFAULT NULL AFTER ygt_oc_customer_name_snapshot;

ALTER TABLE ygt_order_candidate_item
  CHANGE COLUMN ygt_oci_goods_name_text ygt_oci_goods_name_snapshot VARCHAR(255) DEFAULT NULL,
  CHANGE COLUMN ygt_oci_quantity_text ygt_oci_quantity DECIMAL(12,3) DEFAULT NULL,
  CHANGE COLUMN ygt_oci_standard_text ygt_oci_unit VARCHAR(128) DEFAULT NULL,
  CHANGE COLUMN ygt_oci_parse_confidence ygt_oci_confidence DECIMAL(5,2) DEFAULT NULL,
  ADD COLUMN ygt_oci_price_snapshot DECIMAL(12,2) DEFAULT NULL AFTER ygt_oci_unit,
  ADD COLUMN ygt_oci_amount DECIMAL(12,2) DEFAULT NULL AFTER ygt_oci_price_snapshot,
  ADD COLUMN ygt_oci_remark VARCHAR(512) DEFAULT NULL AFTER ygt_oci_amount,
  ADD COLUMN ygt_oci_manual_adjusted TINYINT NOT NULL DEFAULT 0 AFTER ygt_oci_confidence,
  ADD COLUMN ygt_oci_update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER ygt_oci_create_time;

ALTER TABLE ygt_group_order
  ADD COLUMN ygt_go_nx_community_id INT DEFAULT NULL AFTER ygt_go_group_id,
  ADD COLUMN ygt_go_corp_id VARCHAR(64) DEFAULT NULL AFTER ygt_go_campaign_id,
  ADD COLUMN ygt_go_source_chat_message_id BIGINT DEFAULT NULL AFTER ygt_go_chat_id,
  ADD COLUMN ygt_go_member_identifier VARCHAR(128) DEFAULT NULL AFTER ygt_go_from_user,
  ADD COLUMN ygt_go_external_user_id VARCHAR(128) DEFAULT NULL AFTER ygt_go_from_user,
  ADD COLUMN ygt_go_customer_name_snapshot VARCHAR(255) DEFAULT NULL AFTER ygt_go_external_user_id,
  MODIFY COLUMN ygt_go_status VARCHAR(32) NOT NULL DEFAULT 'CONFIRMED' COMMENT 'CONFIRMED/CANCELLED',
  ADD COLUMN ygt_go_remark VARCHAR(512) DEFAULT NULL AFTER ygt_go_confirm_time,
  ADD UNIQUE KEY uk_ygt_go_candidate (ygt_go_candidate_id);

ALTER TABLE ygt_group_order_item
  ADD COLUMN ygt_goi_campaign_id BIGINT DEFAULT NULL AFTER ygt_goi_order_id,
  ADD COLUMN ygt_goi_candidate_item_id BIGINT DEFAULT NULL AFTER ygt_goi_campaign_id,
  ADD COLUMN ygt_goi_spec_snapshot VARCHAR(128) DEFAULT NULL AFTER ygt_goi_goods_name_snapshot,
  CHANGE COLUMN ygt_goi_standard ygt_goi_unit VARCHAR(128) DEFAULT NULL,
  ADD COLUMN ygt_goi_unit_snapshot VARCHAR(128) DEFAULT NULL AFTER ygt_goi_unit,
  CHANGE COLUMN ygt_goi_price ygt_goi_price_snapshot DECIMAL(12,2) DEFAULT NULL,
  CHANGE COLUMN ygt_goi_subtotal ygt_goi_amount DECIMAL(12,2) DEFAULT NULL,
  ADD COLUMN ygt_goi_remark VARCHAR(512) DEFAULT NULL AFTER ygt_goi_amount;
