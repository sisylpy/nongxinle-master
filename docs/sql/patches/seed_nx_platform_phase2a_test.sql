-- =============================================================================
-- Phase 2a Round 1 测试种子（请按真实库 ID 修改后执行）
--
-- 前置（严格按序，不可跳步）：
--   1. check_nx_department_orders_distributer_id.sql
--   2. upgrade_nx_platform_phase2a.sql
--   3. 本文件（改 ID 后执行）
--
-- ⚠️  必须先完成 1、2，再启动含 Phase 2a 代码的应用（见 docs/nxPlatform/Phase2a-Round1-验收指南.md）
-- =============================================================================

-- 必改：marketId、departmentId（须为真实 nx_department，且业务上属于该市场）
-- submitLine 请求中的 nxGoodsId 另在 API 里填真实 nx_goods.nx_goods_id

-- 示例：市场 1 管辖客户 departmentId=100（请替换）
INSERT INTO nx_market_department
    (nx_md_market_id, nx_md_department_id, nx_md_status, nx_md_source)
SELECT 1, 100, 'ACTIVE', 'IMPORT'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM nx_market_department
    WHERE nx_md_market_id = 1 AND nx_md_department_id = 100
);

-- 验收 SQL（submitLine 成功后替换 orderId）：
-- SELECT * FROM nx_department_orders WHERE nx_department_orders_id = ?;
-- SELECT * FROM nx_platform_order_assign WHERE nx_poa_order_id = ?;
--
-- 旧配送商 PENDING 排除验收（有限覆盖，仅 3 个高频查询 + 须带 disId）：
--   disGetUnPlanPurchaseApplys / queryDisOrdersByParams / queryDepOrdersAcount
-- 详见 docs/nxPlatform/Phase2a-Round1-验收指南.md 第四节
