-- =============================================================================
-- 京采市场 P0：nx_platform_order_assign GB 来源扩展
--
-- 目标：记录「饭店从批发商店铺下单」等 GB 来源的平台分配主权，
--       不改动 PENDING / ASSIGNED 语义，不改动 nx_platform_order_fulfillment。
--
-- 前置：必须先存在 nx_platform_order_assign（Phase 2a：
--       upgrade_nx_platform_phase2a.sql）
--
-- 执行顺序（本轮 P0 + 后续 P1 Java）：
--   1. upgrade_nx_platform_phase2a.sql              （若未执行）
--   2. upgrade_nx_platform_phase2b_fulfillment.sql （若未执行；P1 会写 fulfillment）
--   3. 本文件 upgrade_nx_platform_assign_gb_source.sql
--   4. （可选）下方 §3 BACKFILL — 旧平台单 source_type / assign_source 补标
--   5. 部署 P1 Java（GbPlatformOrderBridgeService）
--
-- 幂等：
--   - 若某列已存在，对应 ALTER 报 Duplicate column name → 跳过该句即可。
--   - §3 BACKFILL 可重复执行（仅更新仍为 NULL 的行）。
--
-- 回滚（慎用，仅在新列无业务依赖前）：
--   ALTER TABLE nx_platform_order_assign
--     DROP INDEX uk_poa_gb_department_order_id,
--     DROP INDEX idx_poa_source_gb_dep,
--     DROP COLUMN nx_poa_gb_department_order_id,
--     DROP COLUMN nx_poa_gb_department_father_id,
--     DROP COLUMN nx_poa_gb_department_id,
--     DROP COLUMN nx_poa_source_type,
--     DROP COLUMN nx_poa_assign_source;
--
-- 字段说明 / 验收 SQL：见本文件 §4、§5 及对话中的 P0 说明文档。
-- =============================================================================


-- -----------------------------------------------------------------------------
-- 1. 扩展 nx_platform_order_assign（均可 NULL，兼容旧数据）
-- -----------------------------------------------------------------------------

ALTER TABLE nx_platform_order_assign
    ADD COLUMN nx_poa_assign_source VARCHAR(32) NULL
        COMMENT '分配来源：CUSTOMER_SELECTED_SUPPLIER|PLATFORM_MANUAL|PLATFORM_DEFAULT；PENDING 可为 NULL'
        AFTER nx_poa_assign_mode;

ALTER TABLE nx_platform_order_assign
    ADD COLUMN nx_poa_source_type VARCHAR(8) NULL
        COMMENT '客户/订单来源类型：GB|NX'
        AFTER nx_poa_assign_source;

ALTER TABLE nx_platform_order_assign
    ADD COLUMN nx_poa_gb_department_id INT NULL
        COMMENT 'GB 饭店部门 ID → gb_department.gb_department_id（GB 来源时填写）'
        AFTER nx_poa_source_type;

ALTER TABLE nx_platform_order_assign
    ADD COLUMN nx_poa_gb_department_father_id INT NULL
        COMMENT 'GB 父饭店部门 ID → gb_department.gb_department_id'
        AFTER nx_poa_gb_department_id;

ALTER TABLE nx_platform_order_assign
    ADD COLUMN nx_poa_gb_department_order_id INT NULL
        COMMENT 'GB 行订单 ID → gb_department_orders.gb_department_orders_id；P1 幂等键之一'
        AFTER nx_poa_gb_department_father_id;


-- -----------------------------------------------------------------------------
-- 2. 索引
-- -----------------------------------------------------------------------------

-- 按 GB 行单反查 platform assign；数据库级幂等：一条 GB 行单最多一条 assign
-- MySQL UNIQUE 允许多行 NULL，不影响非 GB / 未填 gb_department_order_id 的旧单
CREATE UNIQUE INDEX uk_poa_gb_department_order_id
    ON nx_platform_order_assign (nx_poa_gb_department_order_id);

-- 若曾执行过旧版 patch（普通索引 idx_poa_gb_department_order_id），请先：
--   ALTER TABLE nx_platform_order_assign DROP INDEX idx_poa_gb_department_order_id;
-- 再执行上方 CREATE UNIQUE INDEX（或整段替换为 DROP + CREATE UNIQUE）。

-- 按 GB 饭店 + 来源类型列表（后续 Electric「饭店已指定配送商」等，非本轮必做）
CREATE INDEX idx_poa_source_gb_dep
    ON nx_platform_order_assign (nx_poa_source_type, nx_poa_gb_department_id);


-- =============================================================================
-- 3. BACKFILL（可选，可重复执行）
--
-- 原则：
--   - 仅补 metadata，不新建 assign 行、不改 assign_status / fulfillment。
--   - Phase 2a/2b 已有 PENDING / ASSIGNED 语义保持不变。
--   - CUSTOMER_SELECTED_SUPPLIER 仅由 P1 起新写入，不在此 backfill。
-- =============================================================================

-- 3.1 凡已有 platform assign、尚未标 source_type 的，视为 NX 直连客户路径（submitLine / assign）
UPDATE nx_platform_order_assign poa
SET poa.nx_poa_source_type = 'NX'
WHERE poa.nx_poa_source_type IS NULL;


-- 3.2 已 ASSIGNED、assign_source 仍为空 → 标为 PLATFORM_MANUAL（Electric assign 或历史 assign API）
--     PENDING 保持 assign_source = NULL（尚未分配，不属于任何一种 assignSource）
UPDATE nx_platform_order_assign poa
SET poa.nx_poa_assign_source = 'PLATFORM_MANUAL'
WHERE poa.nx_poa_assign_source IS NULL
  AND poa.nx_poa_assign_status = 'ASSIGNED'
  AND poa.nx_poa_assign_mode = 'PLATFORM';


-- 3.3 （可选，默认不执行）若希望区分「无法判定来源」的 ASSIGNED 旧单，改用 UNKNOWN：
-- UPDATE nx_platform_order_assign poa
-- SET poa.nx_poa_assign_source = 'UNKNOWN'
-- WHERE poa.nx_poa_assign_source IS NULL
--   AND poa.nx_poa_assign_status = 'ASSIGNED'
--   AND poa.nx_poa_assign_mode = 'PLATFORM';


-- 3.4 （可选，默认不执行）为历史 GB→NX 已有关联、但尚无 platform assign 的行补主权记录。
--     ⚠️ 不在 P0 默认范围；数据量大、需确认 marketId/departmentId 映射后再单独脚本处理。
--     P1 起新单由 Java 同事务写入即可。


-- =============================================================================
-- 4. 建列 / 索引后检查（手动执行）
-- =============================================================================
-- SHOW COLUMNS FROM nx_platform_order_assign LIKE 'nx_poa_assign_source';
-- SHOW COLUMNS FROM nx_platform_order_assign LIKE 'nx_poa_source_type';
-- SHOW COLUMNS FROM nx_platform_order_assign LIKE 'nx_poa_gb_%';
-- SHOW INDEX FROM nx_platform_order_assign WHERE Key_name IN (
--     'uk_poa_gb_department_order_id',
--     'idx_poa_source_gb_dep'
-- );


-- =============================================================================
-- 5. 验收 SQL（P0 执行后）
-- =============================================================================

-- 5.1 新列存在且可空
-- SELECT COLUMN_NAME, IS_NULLABLE, COLUMN_TYPE, COLUMN_COMMENT
-- FROM information_schema.COLUMNS
-- WHERE TABLE_SCHEMA = DATABASE()
--   AND TABLE_NAME = 'nx_platform_order_assign'
--   AND COLUMN_NAME IN (
--       'nx_poa_assign_source',
--       'nx_poa_source_type',
--       'nx_poa_gb_department_id',
--       'nx_poa_gb_department_father_id',
--       'nx_poa_gb_department_order_id'
--   )
-- ORDER BY ORDINAL_POSITION;

-- 5.2 backfill 后：不应再有 source_type 为空的行（若跑过 §3.1）
-- SELECT COUNT(*) AS missing_source_type
-- FROM nx_platform_order_assign
-- WHERE nx_poa_source_type IS NULL;

-- 5.3 PENDING 单 assign_source 必须仍为 NULL（不得被 §3.2 误标）
-- SELECT nx_poa_id, nx_poa_order_id, nx_poa_assign_status, nx_poa_assign_source, nx_poa_source_type
-- FROM nx_platform_order_assign
-- WHERE nx_poa_assign_status = 'PENDING'
--   AND nx_poa_assign_source IS NOT NULL;
-- 期望：0 行

-- 5.4 ASSIGNED 旧单：source_type=NX，assign_source=PLATFORM_MANUAL（若跑过 §3.1+3.2）
-- SELECT nx_poa_assign_status,
--        nx_poa_source_type,
--        nx_poa_assign_source,
--        COUNT(*) AS cnt
-- FROM nx_platform_order_assign
-- GROUP BY nx_poa_assign_status, nx_poa_source_type, nx_poa_assign_source
-- ORDER BY 1, 2, 3;

-- 5.5 Phase 2a pending 列表逻辑不受影响（仍仅 PENDING + PLATFORM）
-- SELECT COUNT(*) AS pending_count
-- FROM nx_platform_order_assign
-- WHERE nx_poa_assign_mode = 'PLATFORM'
--   AND nx_poa_assign_status = 'PENDING';

-- 5.6 尚无 CUSTOMER_SELECTED_SUPPLIER / GB source（P1 前预期为 0）
-- SELECT COUNT(*) AS gb_customer_selected
-- FROM nx_platform_order_assign
-- WHERE nx_poa_source_type = 'GB'
--    OR nx_poa_assign_source = 'CUSTOMER_SELECTED_SUPPLIER';

-- 5.7 fulfillment 表未被本 patch 修改
-- SELECT COUNT(*) FROM information_schema.COLUMNS
-- WHERE TABLE_SCHEMA = DATABASE()
--   AND TABLE_NAME = 'nx_platform_order_fulfillment'
--   AND COLUMN_NAME LIKE 'nx_poa_%';
-- 期望：0（fulfillment 列名均为 nx_pof_*）
