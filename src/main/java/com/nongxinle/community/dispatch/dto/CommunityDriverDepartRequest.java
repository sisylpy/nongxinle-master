package com.nongxinle.community.dispatch.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class CommunityDriverDepartRequest {
    private Integer communityId;
    private String routeDate;
    private Integer driverUserId;
}
