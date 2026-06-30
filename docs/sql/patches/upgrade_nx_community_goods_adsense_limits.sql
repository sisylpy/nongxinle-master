-- Adsense 尾货限购字段（起订量 / 购买倍数 / 每人限购）
ALTER TABLE nx_community_goods
    ADD COLUMN nx_cg_adsense_min_order_qty INT NULL COMMENT 'Adsense起订量' AFTER nx_cg_adsense_rest_quantity,
    ADD COLUMN nx_cg_adsense_order_multiple INT NULL DEFAULT 1 COMMENT 'Adsense购买倍数' AFTER nx_cg_adsense_min_order_qty,
    ADD COLUMN nx_cg_adsense_limit_per_customer INT NULL COMMENT 'Adsense每人限购' AFTER nx_cg_adsense_order_multiple;
