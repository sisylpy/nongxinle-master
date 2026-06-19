package com.nongxinle.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Setter
@Getter
@ToString
public class NxPlatformOrderFulfillmentEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer nxPofId;
    private Integer nxPofMarketId;
    private Integer nxPofOrderId;
    private Integer nxPofPlatformAssignId;
    private Integer nxPofDepartmentId;
    private Integer nxPofNxGoodsId;
    private Integer nxPofDistributerId;
    private Integer nxPofDisGoodsId;
    private String nxPofFulfillmentStatus;
    private Integer nxPofCostMissing;
    private String nxPofCostMissingReason;
    private Date nxPofReadyForPickupAt;
    private Date nxPofPickedUpAt;
    private Date nxPofDeliveredAt;
    private Date nxPofCreatedAt;
    private Date nxPofUpdatedAt;
    private Integer nxPofUpdatedBy;
}
