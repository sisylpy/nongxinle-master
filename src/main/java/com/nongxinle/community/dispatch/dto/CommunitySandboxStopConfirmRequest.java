package com.nongxinle.community.dispatch.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Setter
@Getter
@ToString
public class CommunitySandboxStopConfirmRequest {
    private Integer communityId;
    private String routeDate;
    private String sandboxStopKey;
    private Integer addressId;
    private Integer driverUserId;
    private Integer operatorUserId;
    private List<Integer> orderIds;
}
