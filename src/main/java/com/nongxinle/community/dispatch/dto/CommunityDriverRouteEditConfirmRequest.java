package com.nongxinle.community.dispatch.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CommunityDriverRouteEditConfirmRequest {

    private Integer communityId;
    private String routeDate;
    private Integer driverUserId;
    private Integer operatorUserId;
    private String sourcePage;
    private List<String> stopKeys;
}
