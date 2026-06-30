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
public class NxDisRouteStopEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer nxDrsId;
    private Integer nxDrsDriverRouteId;
    /** Phase 1.5a：stop 挂 shipment task，不直接挂 order/bill */
    private Integer nxDrsShipmentTaskId;
    private Integer nxDrsStopSeq;
    private Integer nxDrsDepartmentId;
    private String nxDrsDepartmentName;
    private String nxDrsLat;
    private String nxDrsLng;
    private String nxDrsAddress;
    private Integer nxDrsOrderCount;
    /** Phase 2b-5：客户调度快照 */
    private String nxDrsCustomerTier;
    private Integer nxDrsPriorityWeight;
    private Integer nxDrsItemCount;
    private String nxDrsTotalQuantity;
    private Long nxDrsLegDistanceM;
    private Long nxDrsLegDurationS;
    /** 读模型：本段距离来源与类型 */
    private String legDistanceProvider;
    private String legDistanceType;
    private String nxDrsStopStatus;

    /** Phase 2a：排程快照与 ETA */
    private Integer nxDrsEarliestDeliveryTimeS;
    private Integer nxDrsLatestDeliveryTimeS;
    private Integer nxDrsServiceMinutes;
    private Date nxDrsPlannedArrivalAt;
    private Date nxDrsPlannedServiceStartAt;
    private Date nxDrsPlannedDepartureAt;
    private Integer nxDrsWaitMinutes;
    private Integer nxDrsLateMinutes;
    private String nxDrsTimeWindowStatus;

    /** Phase 2b-5：当日窗口 override */
    private Integer nxDrsTimeWindowOverrideFlag;
    private String nxDrsTimeWindowAdjustReason;

    /** PR-2c：统一解析后的时间窗（读模型，不落库）。 */
    private Integer resolvedEarliestDeliveryTimeS;
    private Integer resolvedLatestDeliveryTimeS;
    private String resolvedWindowSource;

    /** Phase 1.5a：嵌套 task（只读接口加载） */
    private NxDisShipmentTaskEntity shipmentTask;

    /** @deprecated Phase 1.5c 起请走 stop.shipmentTask.items，新读模型恒为空 */
    private List<Integer> orderIds;

    /** Phase 2b-2：只读操作态（与嵌套 shipmentTask 镜像） */
    private Boolean canAssign;
    private String assignBlockedReason;
    private Boolean canConfirmLoad;
    private String confirmLoadBlockedReason;
    private Boolean canMove;
    private String moveBlockedReason;
    private Boolean canUnlock;
    private String unlockBlockedReason;
    private String operationStatusLabel;
    private String nxDrsTimeWindowStatusLabel;

    /** Phase 2b-5：只读 priority preview + label（与 task 镜像） */
    private String customerTierLabel;
    private Integer priorityScorePreview;
    private String priorityReason;
    /** Phase 2b-6：只读时间语义 */
    private String plannedArrivalLabel;
    private String stopTemporalStatus;
    private String stopTemporalStatusLabel;

    /** Phase 3a 读模型 */
    private String sandboxStopKey;
    private String stopSource;
    private Boolean confirmViaSandbox;

    /** Phase 3a：排程模式与展示文案（GET enrichment，不写库） */
    private String scheduleMode;
    private String scheduleModeLabel;
    private Date timeAnchorAt;
    private String timeAnchorLabel;
    private String plannedDepartLabel;
    /** Phase 3a：站点预计离开时间展示（与 plannedArrivalLabel 对称） */
    private String plannedDepartureLabel;
    private String fastestArrivalLabel;
    private String customerWindowLabel;
    /** Phase 3a：服务时长来源（DEFAULT / DEPARTMENT / TASK） */
    private String serviceMinutesSource;
    private String serviceDurationLabel;
    private Boolean isAfterCustomerWindow;
    private String timeBasis;
    private String timeBasisLabel;

    /** Phase 3a 读模型：客户名主权在 nx_department，GET 实时解析，非 DB 快照 */
    private String liveDepartmentName;

    /** Phase 3a.1b：沙盘建议站点 — 确认出货完成（非旧 assign） */
    private Boolean canConfirmCustomer;
    private String confirmCustomerActionLabel;
    private String confirmCustomerBlockedReason;
    /** Phase 3c：已确认店返回沙盘 */
    private Boolean canReturnToSandbox;
    private String returnToSandboxActionLabel;
    private String returnToSandboxBlockedReason;
    private String returnToSandboxWarning;
    private String returnToSandboxConfirmMessage;
    private Integer suggestedDriverUserId;
    private String suggestedDriverName;

    /** Phase 3D：读模型分层 SANDBOX | EXECUTION */
    private String stopScope;
}
