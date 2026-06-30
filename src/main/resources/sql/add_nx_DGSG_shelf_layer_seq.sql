-- 货架商品：同一层内序号（与代码中 nxDgsgShelfLayerSeq / nx_DGSG_shelf_layer_seq 对应）
-- 未执行此脚本时，包含该列的查询会报 Unknown column 并导致接口 500
ALTER TABLE nx_distributer_goods_shelf_goods
    ADD COLUMN nx_DGSG_shelf_layer_seq INT NULL COMMENT '同一层内序号，从1递增'
    AFTER nx_DGSG_shelf_layer;
