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

    /** Phase 1.5a：嵌套 task（只读接口加载） */
    private NxDisShipmentTaskEntity shipmentTask;

    /** @deprecated Phase 1.5c 起请走 stop.shipmentTask.items，新读模型恒为空 */
    private List<Integer> orderIds;
    /** @deprecated Phase 1.5c 起不再加载 stop_order */
    private List<NxDisRouteStopOrderEntity> orders;

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
}
