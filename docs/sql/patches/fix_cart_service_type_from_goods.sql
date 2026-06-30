-- 修正购物车车道：与商品 nx_cg_service_type 对齐（未提交订单 status=-1）
UPDATE nx_community_orders_sub s
INNER JOIN nx_community_goods g ON s.nx_COS_community_goods_id = g.nx_community_goods_id
SET s.nx_COS_service_type = g.nx_cg_service_type
WHERE s.nx_COS_status = -1
  AND g.nx_cg_service_type IS NOT NULL
  AND (s.nx_COS_service_type IS NULL OR s.nx_COS_service_type != g.nx_cg_service_type);
