-- 货架商品：层尾标记（1=该层最后一行，0=非层尾）。执行一次即可。
ALTER TABLE nx_distributer_goods_shelf_goods
    ADD COLUMN `nx_DGSG_shelf_layer_last` INT NULL DEFAULT NULL COMMENT '是否层尾：1=层尾，0=非层尾' AFTER `nx_DGSG_shelf_layer`;
