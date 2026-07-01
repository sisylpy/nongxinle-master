package com.nongxinle.community.dispatch.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class CommunityDriverDutyRequest {
    private Integer communityId;
    private Integer driverUserId;
    /** 路线日，不传默认今天 */
    private String dutyDate;
    private Integer operatorUserId;
}
