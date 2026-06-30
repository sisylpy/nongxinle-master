-- ============================================================
-- upgrade_coupon_rule_v1.sql
-- 规则券第一版：新增规则字段 → 测试数据迁移 → 删除商品券旧字段
-- ============================================================

-- 1) nx_community_coupon 新增规则字段
ALTER TABLE nx_community_coupon
    ADD COLUMN coupon_type       VARCHAR(32)   NOT NULL DEFAULT 'CASH' COMMENT 'CASH|FULL_REDUCTION',
    ADD COLUMN discount_amount   DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '减免金额',
    ADD COLUMN threshold_amount  DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '满减门槛',
    ADD COLUMN scope_type        VARCHAR(32)   NOT NULL DEFAULT 'ALL' COMMENT 'ALL|CATEGORY|GOODS',
    ADD COLUMN scope_ref_ids     TEXT          NULL COMMENT 'JSON数组，分类或商品ID',
    ADD COLUMN use_channel       VARCHAR(32)   NOT NULL DEFAULT 'ALL' COMMENT 'ALL|POS|MINIAPP';

-- 2) 旧券金额与默认规则（DROP nx_cp_price 之前执行）
UPDATE nx_community_coupon
SET discount_amount = CAST(nx_cp_price AS DECIMAL(10,2))
WHERE (discount_amount IS NULL OR discount_amount = 0)
  AND nx_cp_price IS NOT NULL
  AND nx_cp_price <> '';

UPDATE nx_community_coupon
SET coupon_type = 'CASH',
    threshold_amount = 0,
    scope_type = 'ALL',
    scope_ref_ids = NULL,
    use_channel = 'ALL'
WHERE coupon_type IS NULL OR coupon_type = '';

-- 3) nx_customer_user_coupon 新增订单绑定
ALTER TABLE nx_customer_user_coupon
    ADD COLUMN nx_cuc_order_id INT NULL COMMENT '锁定/核销时绑定的订单ID';

-- 4) nx_community_orders 新增用户券绑定
ALTER TABLE nx_community_orders
    ADD COLUMN nx_co_user_coupon_id INT NULL COMMENT '当前订单使用的用户券ID';

-- 5) 清理测试库脏数据
DELETE FROM nx_community_goods WHERE nx_cg_sell_type = 3;
DELETE FROM nx_community_orders_sub WHERE nx_cos_cuc_id IS NOT NULL;

-- 6) 删除 nx_community_coupon 废弃列
ALTER TABLE nx_community_coupon
    DROP COLUMN nx_cp_cg_goods_id,
    DROP COLUMN nx_cp_price,
    DROP COLUMN nx_cp_original_price,
    DROP COLUMN nx_cp_standard,
    DROP COLUMN nx_cp_quantity,
    DROP COLUMN nx_promote_cg_father_id,
    DROP COLUMN nx_cp_recommand_goods,
    DROP COLUMN nx_cp_type;

-- 7) 删除 nx_customer_user_coupon 废弃列
ALTER TABLE nx_customer_user_coupon
    DROP COLUMN nx_cuc_sub_order_id,
    DROP COLUMN nx_cuc_type;

-- 8) 删除订单子单券行标识
ALTER TABLE nx_community_orders_sub
    DROP COLUMN nx_cos_cuc_id;

-- 9) 索引
CREATE INDEX idx_cuc_order_id ON nx_customer_user_coupon (nx_cuc_order_id);
CREATE INDEX idx_co_user_coupon_id ON nx_community_orders (nx_co_user_coupon_id);
