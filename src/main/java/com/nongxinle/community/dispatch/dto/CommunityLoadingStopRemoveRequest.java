package com.nongxinle.community.dispatch.dto;

import lombok.Getter;
import lombok.Setter;

/** 装车中移除单店：取消站点、订单回到待分派池。 */
@Getter
@Setter
public class CommunityLoadingStopRemoveRequest {
    private Integer communityId;
    private String routeDate;
    private Integer operatorUserId;
    private String reason;
    /** 路线编辑页移除：仅返回 exitedLoading，不附带整页 pageViewModel。 */
    private Boolean suppressTodayResponse;
}
