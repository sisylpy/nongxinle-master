package com.nongxinle.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Setter
@Getter
@ToString
public class NxDisRoutePlanEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer nxDrpId;
    private Integer nxDrpDistributerId;
    private String nxDrpPlanDate;
    /** 配送路线日（主权字段，订单快照范围） */
    private String nxDrpRouteDate;
    /** 确认派单日（confirm 时写入） */
    private String nxDrpDispatchDate;
    private String nxDrpStatus;
    private String nxDrpDepotLat;
    private String nxDrpDepotLng;
    private String nxDrpOptimizerType;
    private String nxDrpCostProviderType;
    private Integer nxDrpDriverCount;
    private Long nxDrpTotalDistanceM;
    private Long nxDrpTotalDurationS;

    /** Phase 2a：全队排程汇总 */
    private Date nxDrpPlannedStartAt;
    private Date nxDrpPlannedEndAt;
    private Integer nxDrpTotalWaitMinutes;
    private Integer nxDrpTotalLateMinutes;
    private String nxDrpScheduleStatus;

    /** Phase 2b-1：配送批次与可执行性 */
    private String nxDrpDispatchBatch;
    private Date nxDrpBatchStartAt;
    private Date nxDrpBatchEndAt;
    private Date nxDrpDefaultDepartAt;
    private String nxDrpFeasibilityStatus;

    private Integer nxDrpCreatedBy;
    private Integer nxDrpConfirmedBy;
    private Date nxDrpCreatedAt;
    private Date nxDrpConfirmedAt;
    /** plan → READY 操作人（仅 reconcilePlanStatus 写入） */
    private Integer nxDrpReadyBy;
    /** plan → READY 时间 */
    private Date nxDrpReadyAt;

    private List<NxDisDriverRouteEntity> driverRoutes;
    /** @deprecated Phase 1.5c 起新主链不再加载 unassigned_stop */
    private List<NxDisRouteUnassignedStopEntity> unassignedStops;
    /** simulate / plan 查询：plan 下全部 shipment_task（含 items） */
    private List<NxDisShipmentTaskEntity> shipmentTasks;

    /** Phase 2b-2：只读操作态（GET  enrichment，不写库） */
    private Boolean canStartLoading;
    private String loadingBlockedReason;
    private String operationHint;
    private String nxDrpDispatchBatchLabel;
    private String nxDrpFeasibilityStatusLabel;
    private String nxDrpScheduleStatusLabel;
}
