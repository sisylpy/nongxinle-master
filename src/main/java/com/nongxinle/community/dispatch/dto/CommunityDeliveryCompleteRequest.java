package com.nongxinle.community.dispatch.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class CommunityDeliveryCompleteRequest {
    private Integer communityId;
    private String routeDate;
    private Integer driverUserId;
    private String remark;
}
