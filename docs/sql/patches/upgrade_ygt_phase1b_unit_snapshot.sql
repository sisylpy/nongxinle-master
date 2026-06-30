-- 优果团 ygt_campaign_goods 补加 ygt_cg_unit_snapshot 字段
-- 幂等：先检查字段是否存在，不存在才 ADD
DELIMITER $$
DROP PROCEDURE IF EXISTS add_ygt_cg_unit_snapshot$$
CREATE PROCEDURE add_ygt_cg_unit_snapshot()
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'nongxinle'
      AND TABLE_NAME = 'ygt_campaign_goods'
      AND COLUMN_NAME = 'ygt_cg_unit_snapshot'
  ) THEN
    ALTER TABLE ygt_campaign_goods
      ADD COLUMN ygt_cg_unit_snapshot VARCHAR(32) DEFAULT NULL COMMENT '单位快照' AFTER ygt_cg_standard_snapshot;
  END IF;
END$$
DELIMITER ;
CALL add_ygt_cg_unit_snapshot();
DROP PROCEDURE IF EXISTS add_ygt_cg_unit_snapshot;
