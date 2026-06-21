package com.nongxinle.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Setter
@Getter
@ToString
public class NxDisDriverDutyEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer nxDddId;
    private Integer nxDddDistributerId;
    private Integer nxDddDriverUserId;
    private String nxDddDutyDate;
    private String nxDddDutyStatus;
    private Date nxDddCheckInAt;
    private Date nxDddCheckOutAt;
    private Integer nxDddOperatorUserId;
    private Date nxDddUpdatedAt;
}
