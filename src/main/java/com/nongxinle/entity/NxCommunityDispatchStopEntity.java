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
public class NxCommunityDispatchStopEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer nxCommunityDispatchStopId;
    private Integer nxCdsPlanId;
    private Integer nxCdsDriverRouteId;
    private Integer nxCdsCommunityId;
    private Integer nxCdsAddressId;
    private String nxCdsCustomerName;
    private String nxCdsCustomerPhone;
    private String nxCdsLat;
    private String nxCdsLng;
    private String nxCdsAddressText;
    private String nxCdsStopStatus;
    private Integer nxCdsRouteSeq;
    private String nxCdsServiceDate;
    private String nxCdsServiceTime;
    private Integer nxCdsAssignedDriverUserId;
    private Integer nxCdsOrderCount;
    private Date nxCdsConfirmedAt;
    private Date nxCdsDeliveredAt;
    private Date nxCdsCreatedAt;
    private Date nxCdsUpdatedAt;

    private List<NxCommunityDispatchStopItemEntity> items;
}
