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
public class NxDisDriverRouteEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer nxDdrId;
    private Integer nxDdrPlanId;
    private Integer nxDdrDriverUserId;
    private Integer nxDdrRouteSeq;
    private Long nxDdrTotalDistanceM;
    private Long nxDdrTotalDurationS;
    private Integer nxDdrStopCount;

    /** Phase 3D：路线执行态 */
    private String nxDdrRouteStatus;
    private Date nxDdrActualDepartAt;
    private Integer nxDdrDepartOperatorUserId;
    private String nxDdrDepartRemark;

    /** Phase 3f：老板确认进入装车流程 */
    private Date nxDdrLoadingEnteredAt;
    private Integer nxDdrLoadingEnteredOperatorUserId;

    /** Phase 2a：排程汇总 */
    private Date nxDdrPlannedDepartAt;
    private Date nxDdrPlannedFinishAt;
    private Integer nxDdrTotalServiceMinutes;
    private Integer nxDdrTotalWaitMinutes;
    private Integer nxDdrTotalLateMinutes;
    private String nxDdrScheduleStatus;

    /** Phase 2b-1：批次可派性与可执行性 */
    private Integer nxDdrDispatchEligible;
    private String nxDdrIneligibleReason;
    private String nxDdrFeasibilityStatus;

    private String driverName;
    private String driverPhone;
    /** 路线日（来自 plan） */
    private String routeDate;
    /** 派单日（来自 plan） */
    private String dispatchDate;
    private List<NxDisRouteStopEntity> stops;

    /** Phase 2b-2：只读操作态 */
    private Boolean canLoad;
    private String loadBlockedReason;
    private Boolean canAssignMore;
    private String assignBlockedReason;
    private String nxDdrFeasibilityStatusLabel;
    private String nxDdrIneligibleReasonLabel;
    /** Phase 2b-6：路线操作语义 */
    private String routeOperationStatusLabel;
    private Integer assignedStopCount;
    private Integer suggestedStopCount;
    private Integer lockedStopCount;

    /** Phase 3a：沙盘排程展示（GET enrichment） */
    private String scheduleMode;
    private String scheduleModeLabel;
    private String routeScheduleSummaryLabel;
    private String plannedDepartLabel;
    private String plannedFinishLabel;
    /** Phase 3a：整趟返回市场完成时刻（= plannedFinishAt canonical） */
    private Date plannedReturnAt;
    private String plannedReturnLabel;
    /** Phase 3a：末站到达/离店（与整趟返回区分） */
    private Date lastStopArrivalAt;
    private Date lastStopDepartureAt;
    /** Phase 3a：返程段展示文案 */
    private String returnLegLabel;
    private String timeBasisLabel;

    /** 读模型：返程段（末站→市场） */
    private Long returnLegDistanceM;
    private Long returnLegDurationS;
    private String returnLegDistanceType;
    private String routeDistanceType;
    private String distanceProvider;

    /** Phase 3D：读模型 — 确认司机出发 */
    private Integer totalStopCount;
    private Integer confirmedStopCount;
    private String routeStatus;
    private String routeStatusLabel;
    private Boolean canDepart;
    private String departActionLabel;
    private String departBlockedReason;
    private String departConfirmMessage;
    private String departWarning;
    private Integer unprintedBillCount;
    private Date departedAt;

    /** Phase 3D：读模型分层 SANDBOX | EXECUTION */
    private String routeScope;
    private Boolean sandboxEligible;

    /** Phase 3E：读模型 — 全店已送达 / 可完成路线 */
    private Boolean allStopsDelivered;
    private Boolean canCompleteRoute;
}
