-- =============================================================================
-- 批发市场平台化 Phase 2b：平台订单履约扩展表
--
-- 前置：必须先执行 upgrade_nx_platform_phase2a.sql（含 nx_platform_order_assign）
--
-- 执行顺序（Phase 2b）：
--   1. upgrade_nx_platform_phase2a.sql          （若未执行）
--   2. 本文件 upgrade_nx_platform_phase2b_fulfillment.sql
--   3. （可选）下方 §3 BACKFILL 段 — 为历史 ASSIGNED 平台单补 fulfillment
--   4. 再部署 Phase 2b Java
--
-- 业务原则（拍板）：
--   - 平台单进入现有出库聚合，不做单独出库流程
--   - assign 主权仍在 nx_platform_order_assign.assign_status = ASSIGNED
--   - 履约进度在 nx_platform_order_fulfillment.fulfillment_status
--   - Phase 2b v1 仅使用：ASSIGNED → READY_FOR_PICKUP
--   - cost_missing=1 不阻断出库与 READY_FOR_PICKUP
--
-- 成本价说明（Java 实现，非本 SQL）：
--   - nxDoPrice = 销售价（assign 锁定）；nxDoCostPrice = buying price 解析
--   - 禁止把销售价复制为成本价；禁止 0.1 假成本
--   - 有效成本：nx_dg_buying_price / _one / _two / _three，须严格 > 0.1
--
-- 回滚（慎用）：
--   DROP TABLE IF EXISTS nx_platform_order_fulfillment;
--
-- 字段说明 / 验收 SQL：docs/nxPlatform/Phase2b-SQL-Patch-Draft.md
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. nx_platform_order_fulfillment
-- -----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS nx_platform_order_fulfillment (
    nx_pof_id                      INT          NOT NULL AUTO_INCREMENT
        COMMENT 'pofId 履约行主键',
    nx_pof_market_id               INT          NOT NULL
        COMMENT 'marketId → sys_city_market',
    nx_pof_order_id                INT          NOT NULL
        COMMENT 'orderId → nx_department_orders.nx_department_orders_id，1:1',
    nx_pof_platform_assign_id      INT          NOT NULL
        COMMENT 'platformAssignId → nx_platform_order_assign.nx_poa_id',
    nx_pof_department_id           INT          NOT NULL
        COMMENT 'departmentId 客户部门',
    nx_pof_nx_goods_id             INT          NULL
        COMMENT 'nxGoodsId 标准商品（冗余，便于查询）',
    nx_pof_distributer_id          INT          NOT NULL
        COMMENT 'distributerId 实际履约配送商',
    nx_pof_dis_goods_id            INT          NOT NULL
        COMMENT 'disGoodsId 配送商商品 SKU',
    nx_pof_fulfillment_status      VARCHAR(32)  NOT NULL DEFAULT 'ASSIGNED'
        COMMENT 'ASSIGNED|READY_FOR_PICKUP|PICKED_UP|DELIVERING|DELIVERED|SUPPLIER_EXCEPTION',
    nx_pof_cost_missing            TINYINT      NOT NULL DEFAULT 0
        COMMENT '1=未能解析有效 buying 成本价；不阻断出库/READY_FOR_PICKUP',
    nx_pof_cost_missing_reason     VARCHAR(128) NULL
        COMMENT '成本缺失原因码，如 NO_VALID_BUYING_PRICE / NOT_RESOLVED_AT_ASSIGN',
    nx_pof_ready_for_pickup_at     DATETIME     NULL
        COMMENT '配送商现有出库完成、待平台司机取货',
    nx_pof_picked_up_at            DATETIME     NULL
        COMMENT 'Phase 2c 司机已取货',
    nx_pof_delivered_at            DATETIME     NULL
        COMMENT 'Phase 2c 已送达客户',
    nx_pof_created_at              DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    nx_pof_updated_at              DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    nx_pof_updated_by              INT          NULL
        COMMENT 'updatedBy 操作员/系统',
    PRIMARY KEY (nx_pof_id),
    UNIQUE KEY uk_pof_order (nx_pof_order_id),
    KEY idx_pof_platform_assign (nx_pof_platform_assign_id),
    KEY idx_pof_market_status (nx_pof_market_id, nx_pof_fulfillment_status),
    KEY idx_pof_distributer_status (nx_pof_distributer_id, nx_pof_fulfillment_status),
    KEY idx_pof_market_dis_status (nx_pof_market_id, nx_pof_distributer_id, nx_pof_fulfillment_status),
    KEY idx_pof_department (nx_pof_department_id),
    KEY idx_pof_dis_goods (nx_pof_dis_goods_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台订单履约扩展（不替代 nx_DO_status / 出库聚合）';


-- =============================================================================
-- 2. 建表后检查（手动执行）
-- =============================================================================
-- SHOW CREATE TABLE nx_platform_order_fulfillment;
-- SHOW INDEX FROM nx_platform_order_fulfillment;


-- =============================================================================
-- 3. BACKFILL（可选）— 历史 ASSIGNED 平台单补 fulfillment
--
-- 条件：
--   assign_mode = PLATFORM
--   assign_status = ASSIGNED
--   且 nx_platform_order_fulfillment 尚无对应 order_id
--
-- 不影响 PENDING 单（PENDING 不在本 INSERT 范围）
--
-- 说明：
--   - 初始 fulfillment_status = ASSIGNED
--   - cost_missing 默认 0；若已知历史单无成本，可事后 UPDATE 或等 Java 出库时再标
--   - 已出库（purchase_status=4）的历史单：backfill 仍为 ASSIGNED；
--     Phase 2b Java 上线后可跑 §4 一次性修正脚本，或人工 UPDATE → READY_FOR_PICKUP
-- =============================================================================

INSERT INTO nx_platform_order_fulfillment (
    nx_pof_market_id,
    nx_pof_order_id,
    nx_pof_platform_assign_id,
    nx_pof_department_id,
    nx_pof_nx_goods_id,
    nx_pof_distributer_id,
    nx_pof_dis_goods_id,
    nx_pof_fulfillment_status,
    nx_pof_cost_missing,
    nx_pof_cost_missing_reason,
    nx_pof_created_at,
    nx_pof_updated_at
)
SELECT
    poa.nx_poa_market_id,
    poa.nx_poa_order_id,
    poa.nx_poa_id,
    poa.nx_poa_department_id,
    COALESCE(poa.nx_poa_nx_goods_id, dor.nx_DO_nx_goods_id),
    poa.nx_poa_assigned_distributer_id,
    poa.nx_poa_assigned_dis_goods_id,
    'ASSIGNED',
    0,
    NULL,
    COALESCE(poa.nx_poa_assigned_at, poa.nx_poa_created_at, NOW()),
    NOW()
FROM nx_platform_order_assign poa
INNER JOIN nx_department_orders dor
    ON dor.nx_department_orders_id = poa.nx_poa_order_id
WHERE poa.nx_poa_assign_mode = 'PLATFORM'
  AND poa.nx_poa_assign_status = 'ASSIGNED'
  AND poa.nx_poa_assigned_distributer_id IS NOT NULL
  AND poa.nx_poa_assigned_dis_goods_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM nx_platform_order_fulfillment pof
      WHERE pof.nx_pof_order_id = poa.nx_poa_order_id
  );


-- =============================================================================
-- 4. BACKFILL 补充（可选）— 已出库的历史平台单 → READY_FOR_PICKUP
--
-- 仅当订单已在配送商侧完成出库（nx_DO_purchase_status >= 4）时执行。
-- 新环境若无此类数据，可跳过。
-- =============================================================================

-- UPDATE nx_platform_order_fulfillment pof
-- INNER JOIN nx_department_orders dor ON dor.nx_department_orders_id = pof.nx_pof_order_id
-- INNER JOIN nx_platform_order_assign poa ON poa.nx_poa_order_id = pof.nx_pof_order_id
-- SET pof.nx_pof_fulfillment_status = 'READY_FOR_PICKUP',
--     pof.nx_pof_ready_for_pickup_at = COALESCE(pof.nx_pof_ready_for_pickup_at, NOW()),
--     pof.nx_pof_updated_at = NOW()
-- WHERE poa.nx_poa_assign_mode = 'PLATFORM'
--   AND poa.nx_poa_assign_status = 'ASSIGNED'
--   AND dor.nx_DO_purchase_status >= 4
--   AND pof.nx_pof_fulfillment_status = 'ASSIGNED';
