package com.nongxinle.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Setter
@Getter
@ToString
public class NxDisSandboxDayTimeWindowEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer nxDsdtwId;
    private Integer nxDsdtwDistributerId;
    private String nxDsdtwRouteDate;
    private Integer nxDsdtwDepFatherId;
    private Integer nxDsdtwEarliestDeliveryTimeS;
    private Integer nxDsdtwLatestDeliveryTimeS;
    private Integer nxDsdtwServiceMinutes;
    private String nxDsdtwAdjustReason;
    private Integer nxDsdtwOperatorUserId;
    private Date nxDsdtwUpdatedAt;
}
