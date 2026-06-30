package com.nongxinle.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Setter
@Getter
@ToString
public class NxCommunityOrderDispatchEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer nxCommunityOrderDispatchId;
    private Integer nxCodCommunityOrderId;
    private Integer nxCodCommunityId;
    private String nxCodDispatchStatus;
    private Integer nxCodDispatchStopId;
    private Integer nxCodAssignedDriverUserId;
    private String nxCodRouteDate;
    private Date nxCodCreatedAt;
    private Date nxCodUpdatedAt;
}
