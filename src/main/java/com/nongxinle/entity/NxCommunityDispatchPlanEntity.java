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
public class NxCommunityDispatchPlanEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer nxCommunityDispatchPlanId;
    private Integer nxCdpCommunityId;
    private String nxCdpRouteDate;
    private String nxCdpStatus;
    private String nxCdpDepotLat;
    private String nxCdpDepotLng;
    private Date nxCdpCreatedAt;
    private Date nxCdpUpdatedAt;

    private List<NxCommunityDispatchDriverRouteEntity> driverRoutes;
    private List<NxCommunityDispatchStopEntity> stops;
}
