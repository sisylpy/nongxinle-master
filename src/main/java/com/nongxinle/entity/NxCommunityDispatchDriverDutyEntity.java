package com.nongxinle.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Setter
@Getter
@ToString
public class NxCommunityDispatchDriverDutyEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer nxCommunityDispatchDriverDutyId;
    private Integer nxCddCommunityId;
    private Integer nxCddDriverUserId;
    private String nxCddRouteDate;
    private String nxCddStatus;
    private Date nxCddCheckedInAt;
    private Date nxCddCheckedOutAt;
}
