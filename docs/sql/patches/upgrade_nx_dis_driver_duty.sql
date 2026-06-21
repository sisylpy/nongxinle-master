-- =============================================================================
-- 配送商司机每日上岗状态（Phase 1.5d）
--
-- 业务：账号存在 ≠ 今日可派；仅 ON_DUTY 司机可进入 simulate 沙盘。
-- 账号长期状态仍用 nx_distributer_user.nx_DIU_admin=5 标识司机角色。
--
-- 前置：upgrade_nx_dis_route_dispatch_phase1_5a.sql（或 Phase 1 路线表）
-- =============================================================================

CREATE TABLE IF NOT EXISTS nx_dis_driver_duty (
    nx_ddd_id                INT          NOT NULL AUTO_INCREMENT
        COMMENT '主键',
    nx_ddd_distributer_id    INT          NOT NULL
        COMMENT '配送商 disId',
    nx_ddd_driver_user_id    INT          NOT NULL
        COMMENT '司机用户 → nx_distributer_user',
    nx_ddd_duty_date         VARCHAR(10)  NOT NULL
        COMMENT '上岗日 yyyy-MM-dd',
    nx_ddd_duty_status       VARCHAR(16)  NOT NULL DEFAULT 'OFF_DUTY'
        COMMENT 'OFF_DUTY|ON_DUTY（后续可扩展 BREAK/IN_DELIVERY/OFFLINE）',
    nx_ddd_check_in_at       DATETIME     NULL
        COMMENT '上岗时间',
    nx_ddd_check_out_at      DATETIME     NULL
        COMMENT '下岗时间',
    nx_ddd_operator_user_id  INT          NULL
        COMMENT '最近一次操作人',
    nx_ddd_updated_at        DATETIME     NULL,
    PRIMARY KEY (nx_ddd_id),
    UNIQUE KEY uk_dis_driver_duty_date (nx_ddd_distributer_id, nx_ddd_driver_user_id, nx_ddd_duty_date),
    KEY idx_dis_date_status (nx_ddd_distributer_id, nx_ddd_duty_date, nx_ddd_duty_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
  COMMENT='配送商司机每日上岗状态';
