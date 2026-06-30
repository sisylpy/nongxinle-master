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
public class NxDisShipmentTaskEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer nxDstId;
    private Integer nxDstDistributerId;
    private String nxDstRouteDate;
    private Integer nxDstDepFatherId;
    private String nxDstDepName;
    private String nxDstLat;
    private String nxDstLng;
    private String nxDstAddress;
    private String nxDstStatus;
    private Integer nxDstSuggestedDriverUserId;
    private Integer nxDstAssignedDriverUserId;
    private Integer nxDstManualLocked;
    private Integer nxDstManualStopSeq;
    private Integer nxDstPriorityLevel;
    /** Phase 2b-5：客户调度快照 */
    private String nxDstCustomerTier;
    private Integer nxDstPriorityWeight;
    private Integer nxDstOrderCount;
    private Integer nxDstItemCount;
    private String nxDstTotalQuantity;
    private Integer nxDstEarliestDeliveryTimeS;
    private Integer nxDstLatestDeliveryTimeS;
    private Integer nxDstServiceMinutes;
    private Integer nxDstTimeWindowOverrideFlag;
    private String nxDstTimeWindowAdjustReason;
    private Date nxDstAssignConfirmedAt;
    private Integer nxDstOperatorUserId;
    private String nxDstAssignReason;
    private String nxDstAdjustReason;
    private Integer nxDstPlanId;
    /** Phase 3a.1：所属司机趟次 */
    private Integer nxDstDriverRouteId;
    /** Phase 3a.1：趟内停靠顺序 */
    private Integer nxDstRouteSeq;
    private Long nxDstLegDistanceM;
    private Long nxDstLegDurationS;
    /** 读模型：本段距离来源与类型 */
    private String legDistanceProvider;
    private String legDistanceType;
    private Date nxDstPlannedArrivalAt;
    private Date nxDstPlannedServiceStartAt;
    private Date nxDstPlannedDepartureAt;
    private Integer nxDstWaitMinutes;
    private Integer nxDstLateMinutes;
    private String nxDstTimeWindowStatus;
    private String nxDstStopStatus;
    private String nxDstOpenKey;
    /** Phase 3E：单店送达 */
    private Date nxDstDeliveredAt;
    private String nxDstDeliveryRemark;
    private Integer nxDstDeliveryOperatorUserId;
    /** Phase 3E：配送异常 */
    private String nxDstExceptionType;
    private String nxDstExceptionRemark;
    private Date nxDstExceptionAt;
    private Date nxDstCreatedAt;
    private Date nxDstUpdatedAt;

    /** MyBatis 动态更新：置 true 时清空 open_key */
    private Boolean clearOpenKey;
    /** MyBatis 动态更新：置 true 时清空送达/异常字段（重新出发或重新分派） */
    private Boolean clearDeliveryCompletion;

    private List<NxDisShipmentTaskItemEntity> items;

    /** Phase 2b-2：只读操作态 */
    private Boolean canAssign;
    private String assignBlockedReason;
    private Boolean canConfirmLoad;
    private String confirmLoadBlockedReason;
    private Boolean canMove;
    private String moveBlockedReason;
    private Boolean canUnlock;
    private String unlockBlockedReason;
    private String operationStatusLabel;
    private String nxDstStatusLabel;

    /** Phase 2b-5：只读 priority preview + label */
    private String customerTierLabel;
    private Integer priorityScorePreview;
    private String priorityReason;

    /** 读模型：是否含有效 eligible 订单 item，可展示为今日站点 */
    private Boolean dispatchValid;
    /** 读模型：无效原因 */
    private String dispatchInvalidReason;
    /** 读模型：已锁定/已分派但无有效订单，需人工清理 */
    private Boolean needsManualCleanup;

    /** Phase 3a 读模型：沙盘站点键 dep:{depFatherId} */
    private String sandboxStopKey;
    /** Phase 3a 读模型：CONFIRMED | SANDBOX_SUGGESTED | UNASSIGNED */
    private String stopSource;
    /** Phase 3a：true 表示应走 POST /sandbox/stops/confirm 而非 taskId assign */
    private Boolean confirmViaSandbox;

    /** Phase 3a 读模型：客户名主权在 nx_department，GET 实时解析，非 DB 快照 */
    private String liveDepartmentName;

    /** Phase 3a.1 读模型：配送单打印弱参考 */
    private String billPrintStatus;
    private Integer unprintedBillCount;
    private String billPrintWarning;

    /** Phase 3a.1b：沙盘建议 — 确认出货完成（非旧 assign） */
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
}
