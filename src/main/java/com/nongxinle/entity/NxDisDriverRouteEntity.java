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
}
