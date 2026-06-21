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
    private String nxDstOpenKey;
    private Date nxDstCreatedAt;
    private Date nxDstUpdatedAt;

    /** MyBatis 动态更新：置 true 时清空 open_key */
    private Boolean clearOpenKey;

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
}
