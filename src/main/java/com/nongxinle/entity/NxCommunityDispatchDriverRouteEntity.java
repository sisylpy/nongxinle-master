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
public class NxCommunityDispatchDriverRouteEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer nxCommunityDispatchDriverRouteId;
    private Integer nxCddrPlanId;
    private Integer nxCddrCommunityId;
    private Integer nxCddrDriverUserId;
    private String nxCddrRouteStatus;
    private Date nxCddrLoadingEnteredAt;
    private Date nxCddrActualDepartAt;
    private Integer nxCddrStopCount;
    private Date nxCddrCreatedAt;
    private Date nxCddrUpdatedAt;

    private NxCommunityUserEntity driverUser;
    private List<NxCommunityDispatchStopEntity> stops;
}
