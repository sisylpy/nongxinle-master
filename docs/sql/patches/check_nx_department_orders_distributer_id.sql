-- =============================================================================
-- Phase 2a 阶段 0：确认 nx_department_orders.nx_DO_distributer_id 是否允许 NULL
--
-- 执行顺序：本文件为第 1 步 → 然后 upgrade_nx_platform_phase2a.sql → seed → 再启 Java
-- 详见 docs/nxPlatform/Phase2a-Round1-验收指南.md
-- =============================================================================

SELECT COLUMN_NAME, IS_NULLABLE, COLUMN_TYPE, COLUMN_DEFAULT
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'nx_department_orders'
  AND COLUMN_NAME IN ('nx_DO_distributer_id', 'nx_do_distributer_id');

-- 或：
-- SHOW CREATE TABLE nx_department_orders;
