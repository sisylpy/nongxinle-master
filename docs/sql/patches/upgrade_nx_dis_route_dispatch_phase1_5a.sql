-- =============================================================================
-- 配送商路线派单 Phase 1.5a：shipment_task 中间层 + plan 三阶段状态
--
-- 前置（须已执行）：
--   upgrade_nx_dis_route_dispatch_phase1.sql
--   upgrade_nx_dis_route_stop_order_snapshot.sql   （若曾部署 Phase 1 P0）
--
-- 业务原则（拍板）：
--   - do_status < 3 仅表示「可进入沙盘候选 live order」，不等于可装车
--   - ASSIGNED = 人工确认司机，可装车；READY_TO_GO = 打单完成，可出发
--   - historyOrderId / billId 权威映射在 nx_dis_shipment_task_item，不在 task 主表
--   - plan READY 只统计 ACTIVE task / ACTIVE item
--   - 一个客户一天仅允许一个「未关闭」open task；补单不得污染已 READY / IN_DELIVERY 的 task
--   - 不改 order / bill 原状态机；bill 删除回退预留 onBillReverted（Java）
--
-- 执行顺序：
--   1. 本文件
--   2. 部署 Phase 1.5a Java（DisShipmentTaskService + simulate 等）
--   3. （可选）§5 数据迁移：stop_order → task_item（仅历史 plan 只读兼容）
--
-- 回滚（慎用，仅开发环境）：
--   ALTER TABLE nx_dis_route_stop DROP COLUMN nx_drs_shipment_task_id;
--   DROP TABLE IF EXISTS nx_dis_shipment_task_item;
--   DROP TABLE IF EXISTS nx_dis_shipment_task;
--   -- plan status 需手工改回 DRAFT/CONFIRMED
--
-- 接口说明：docs/nxPlatform/Phase1.5a-Route-Dispatch-API.md
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. nx_dis_shipment_task — 客户/站点级配送任务（路线派单主权对象）
-- -----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS nx_dis_shipment_task (
    nx_dst_id                       INT          NOT NULL AUTO_INCREMENT
        COMMENT 'taskId 配送任务主键',
    nx_dst_distributer_id           INT          NOT NULL
        COMMENT 'disId 配送商',
    nx_dst_route_date               VARCHAR(10)  NOT NULL
        COMMENT '路线日 yyyy-MM-dd（与 plan.nx_drp_route_date 对齐）',
    nx_dst_dep_father_id            INT          NOT NULL
        COMMENT '客户部门 fatherId → nx_department',
    nx_dst_dep_name                 VARCHAR(128) NULL
        COMMENT '客户名称快照',
    nx_dst_lat                      VARCHAR(32)  NULL,
    nx_dst_lng                      VARCHAR(32)  NULL,
    nx_dst_address                  VARCHAR(256) NULL
        COMMENT '送货地址快照',

    nx_dst_status                   VARCHAR(32)  NOT NULL DEFAULT 'SIMULATED'
        COMMENT 'SIMULATED|ASSIGNED|READY_TO_GO|UNASSIGNED|CANCELLED|CLOSED|IN_DELIVERY|DELIVERED',

    nx_dst_suggested_driver_user_id INT          NULL
        COMMENT '系统建议司机 → nx_distributer_user；仅 simulate/reoptimize 写入',
    nx_dst_assigned_driver_user_id  INT          NULL
        COMMENT '人工确认司机；assign/move 写入；bill 回填不得修改',

    nx_dst_manual_locked            TINYINT      NOT NULL DEFAULT 0
        COMMENT '1=人工确认后锁定；reoptimize 不得改司机/顺序',
    nx_dst_manual_stop_seq          INT          NULL
        COMMENT '人工固定 stop 序号（同 driver_route 内）；可空',
    nx_dst_priority_level           INT          NOT NULL DEFAULT 0
        COMMENT '客户/任务优先级；越大越优先插入',

    nx_dst_assign_confirmed_at      DATETIME     NULL,
    nx_dst_operator_user_id         INT          NULL
        COMMENT '最近一次 assign/move/unlock 操作人',
    nx_dst_assign_reason            VARCHAR(256) NULL
        COMMENT '人工确认分车原因',
    nx_dst_adjust_reason            VARCHAR(256) NULL
        COMMENT '人工调线原因',

    nx_dst_plan_id                  INT          NULL
        COMMENT '当前所属 route plan → nx_dis_route_plan.nx_drp_id；可空',

    -- MySQL 无 partial unique：用 open_key 保证「同一客户同一天仅一个未关闭 task」
    -- 未关闭(open) = SIMULATED | ASSIGNED | UNASSIGNED（UNASSIGNED 仍占 open_key，补坐标后复用同一 task）
    -- 关闭(closed) 释放 open_key：READY_TO_GO | CANCELLED | CLOSED | IN_DELIVERY | DELIVERED
    -- Java: DisShipmentTaskOpenKeyUtils.isClosedStatus()
    nx_dst_open_key                 VARCHAR(64)  NULL
        COMMENT '未关闭任务唯一键：disId-routeDate-depFatherId；关闭时 NULL',

    nx_dst_created_at               DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    nx_dst_updated_at               DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (nx_dst_id),

    UNIQUE KEY uk_dst_open_key (nx_dst_open_key),

    KEY idx_dst_dis_route_date (nx_dst_distributer_id, nx_dst_route_date),
    KEY idx_dst_dis_route_status (nx_dst_distributer_id, nx_dst_route_date, nx_dst_status),
    KEY idx_dst_plan (nx_dst_plan_id),
    KEY idx_dst_suggested_driver (nx_dst_suggested_driver_user_id),
    KEY idx_dst_assigned_driver (nx_dst_assigned_driver_user_id),
    KEY idx_dst_dep (nx_dst_dep_father_id, nx_dst_route_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
  COMMENT='配送任务：沙盘预分派→人工确认→打单可出发；承接 live→history 生命周期';

-- -----------------------------------------------------------------------------
-- 2. nx_dis_shipment_task_item — 订单行映射（bill / history 权威在此）
-- -----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS nx_dis_shipment_task_item (
    nx_dsti_id                      INT          NOT NULL AUTO_INCREMENT
        COMMENT 'itemId',
    nx_dsti_task_id                 INT          NOT NULL
        COMMENT 'taskId → nx_dis_shipment_task',
    nx_dsti_live_order_id           INT          NOT NULL
        COMMENT 'live 订单行 → nx_department_orders；simulate 时写入',
    nx_dsti_history_order_id        INT          NULL
        COMMENT 'history 订单行 → nx_department_order_history；onBillPrinted 回填',
    nx_dsti_bill_id                 INT          NULL
        COMMENT 'billId → nx_department_bill；onBillPrinted 回填',

    nx_dsti_goods_name              VARCHAR(128) NULL,
    nx_dsti_quantity                VARCHAR(32)  NULL,
    nx_dsti_standard                VARCHAR(32)  NULL,
    nx_dsti_remark                  VARCHAR(256) NULL,

    nx_dsti_item_status             VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE'
        COMMENT 'ACTIVE|REMOVED|CANCELLED；plan READY 统计仅 ACTIVE',

    nx_dsti_created_at              DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    nx_dsti_updated_at              DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (nx_dsti_id),

    UNIQUE KEY uk_dsti_live_order (nx_dsti_live_order_id),

    KEY idx_dsti_task (nx_dsti_task_id),
    KEY idx_dsti_task_status (nx_dsti_task_id, nx_dsti_item_status),
    KEY idx_dsti_bill (nx_dsti_bill_id),
    KEY idx_dsti_history_order (nx_dsti_history_order_id),

    CONSTRAINT fk_dsti_task FOREIGN KEY (nx_dsti_task_id)
        REFERENCES nx_dis_shipment_task (nx_dst_id)
        ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
  COMMENT='配送任务订单行：live/history/bill 权威映射';

-- -----------------------------------------------------------------------------
-- 3. nx_dis_route_stop — 增加 shipment_task_id
-- -----------------------------------------------------------------------------

ALTER TABLE nx_dis_route_stop
    ADD COLUMN nx_drs_shipment_task_id INT NULL
        COMMENT 'shipment taskId → nx_dis_shipment_task；stop 不直接挂 order/bill'
        AFTER nx_drs_driver_route_id;

ALTER TABLE nx_dis_route_stop
    ADD KEY idx_drs_shipment_task (nx_drs_shipment_task_id);

-- 新 stop 写入后应保证 task_id 非空；历史 stop 在迁移前可为 NULL

-- -----------------------------------------------------------------------------
-- 4. nx_dis_route_plan — 三阶段状态（DRAFT/CONFIRMED → SIMULATED/ASSIGNED/READY）
-- -----------------------------------------------------------------------------

-- 4.1 数据迁移（已部署 Phase 1 的环境）
UPDATE nx_dis_route_plan
SET nx_drp_status = 'SIMULATED'
WHERE nx_drp_status = 'DRAFT';

UPDATE nx_dis_route_plan
SET nx_drp_status = 'ASSIGNED'
WHERE nx_drp_status = 'CONFIRMED';

-- 注：原 CONFIRMED 是否应升为 READY 需按 task 是否已全部 bill 人工核对；
--     Phase 1.5a 默认映射为 ASSIGNED，由 reconcilePlanStatus 后续修正。

-- 4.2 可选：扩展 plan 字段（正式出发审计；与 confirmed_by 语义分离）
ALTER TABLE nx_dis_route_plan
    ADD COLUMN nx_drp_ready_by INT NULL
        COMMENT 'plan → READY 操作人'
        AFTER nx_drp_confirmed_at;

ALTER TABLE nx_dis_route_plan
    ADD COLUMN nx_drp_ready_at DATETIME NULL
        COMMENT 'plan → READY 时间（全队可出发）'
        AFTER nx_drp_ready_by;

-- nx_drp_status 合法值（应用层 DisRoutePlanStatus 同步）：
--   SIMULATED | ASSIGNED | READY | CANCELLED

-- -----------------------------------------------------------------------------
-- 5. 旧 stop_order 表处理策略（本 patch 不 DROP）
-- -----------------------------------------------------------------------------

-- nx_dis_route_stop_order / nx_dis_route_unassigned_stop_order：
--   · Phase 1.5a 起 Java 停止写入（DEPRECATED）
--   · 读接口改走 task → task_item
--   · 表保留供历史 plan 只读兼容与可选回填
--
-- 可选回填（手工执行，按需）：
--
-- INSERT INTO nx_dis_shipment_task (...)
-- SELECT ... FROM nx_dis_route_stop_order drso
-- JOIN nx_dis_route_stop drs ON ...
-- WHERE drs.nx_drs_shipment_task_id IS NULL;
--
-- INSERT INTO nx_dis_shipment_task_item (nx_dsti_task_id, nx_dsti_live_order_id, ...)
-- SELECT task_id, nx_drso_order_id, ... FROM nx_dis_route_stop_order ...
-- ON DUPLICATE KEY UPDATE nx_dsti_task_id = VALUES(nx_dsti_task_id);
--
-- UPDATE nx_dis_route_stop drs
-- JOIN nx_dis_shipment_task t ON ...
-- SET drs.nx_drs_shipment_task_id = t.nx_dst_id
-- WHERE drs.nx_drs_shipment_task_id IS NULL;

-- -----------------------------------------------------------------------------
-- 6. 验收 SQL
-- -----------------------------------------------------------------------------

-- SHOW CREATE TABLE nx_dis_shipment_task;
-- SHOW CREATE TABLE nx_dis_shipment_task_item;
-- SHOW COLUMNS FROM nx_dis_route_stop LIKE 'nx_drs_shipment_task_id';
-- SHOW COLUMNS FROM nx_dis_route_plan LIKE 'nx_drp_ready%';
--
-- SELECT nx_drp_status, COUNT(*) FROM nx_dis_route_plan GROUP BY nx_drp_status;
--
-- -- open task 唯一性：同一 dep 同一天只能有一条 open_key 非空
-- SELECT nx_dst_open_key, COUNT(*) c FROM nx_dis_shipment_task
-- WHERE nx_dst_open_key IS NOT NULL GROUP BY nx_dst_open_key HAVING c > 1;
