-- 京采市场平台首页依赖 sys_city_market 主数据。
-- 若 platform/customer/* 返回「市场不存在: marketId=1」，在目标库执行本脚本。
--
-- 说明：
--   - marketId=1 与前端默认 platformMarketId、nx_distributer_sys_market_id 对齐
--   - 请把 sys_cm_market_name 改成你库里的真实市场名

INSERT INTO sys_city_market (
    sys_city_market_id,
    sys_cm_city_id,
    sys_cm_market_name,
    sys_cm_register_gift_points,
    sys_cm_points_per_yuan,
    sys_cm_self_print_enabled
)
SELECT
    1,
    1,
    '京贸物联批发市场',
    0,
    1,
    0
FROM (SELECT 1) AS _seed
WHERE NOT EXISTS (
    SELECT 1 FROM sys_city_market WHERE sys_city_market_id = 1
);

-- 可选：把已有配送商挂到 marketId=1（仅当尚未设置时）
-- UPDATE nx_distributer
-- SET nx_distributer_sys_market_id = 1
-- WHERE nx_distributer_sys_market_id IS NULL OR nx_distributer_sys_market_id = 0;
