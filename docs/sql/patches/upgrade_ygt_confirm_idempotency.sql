-- 优果团候选单确认幂等与正式订单字段补强
-- 仅升级 ygt_ 表；不触碰 nx_community_orders / nx_community_orders_sub / 购物车 / 优惠券 / POS / checkout。
-- 已有库执行前请确认对应列/索引不存在；重复执行会因列或索引已存在而失败。

ALTER TABLE ygt_group_order
  ADD COLUMN ygt_go_corp_id VARCHAR(64) DEFAULT NULL AFTER ygt_go_campaign_id,
  ADD COLUMN ygt_go_member_identifier VARCHAR(128) DEFAULT NULL AFTER ygt_go_from_user,
  MODIFY COLUMN ygt_go_status VARCHAR(32) NOT NULL DEFAULT 'CONFIRMED' COMMENT 'CONFIRMED/CANCELLED';

ALTER TABLE ygt_group_order
  ADD UNIQUE KEY uk_ygt_go_candidate (ygt_go_candidate_id);

ALTER TABLE ygt_group_order_item
  ADD COLUMN ygt_goi_campaign_id BIGINT DEFAULT NULL AFTER ygt_goi_order_id,
  ADD COLUMN ygt_goi_candidate_item_id BIGINT DEFAULT NULL AFTER ygt_goi_campaign_id,
  ADD COLUMN ygt_goi_spec_snapshot VARCHAR(128) DEFAULT NULL AFTER ygt_goi_goods_name_snapshot,
  ADD COLUMN ygt_goi_unit_snapshot VARCHAR(128) DEFAULT NULL AFTER ygt_goi_unit;
