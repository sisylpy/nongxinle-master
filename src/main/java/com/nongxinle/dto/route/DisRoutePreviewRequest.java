package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Setter
@Getter
@ToString
public class DisRoutePreviewRequest {
    private Integer disId;
    /** 配送路线日，不传默认今天 */
    private String routeDate;
    /** @deprecated 请用 routeDate */
    private String planDate;
    private String depotLat;
    private String depotLng;
    private List<Integer> driverUserIds;
    private Integer operatorUserId;
    private String optimizerType;
    private String costProviderType;
    /** Phase 2b-1：MORNING / AFTERNOON / ADHOC，默认 MORNING */
    private String batchCode;
    /** ADHOC 必填，格式 yyyy-MM-dd HH:mm:ss */
    private String batchStartAt;
    private String defaultDepartAt;
    private String batchEndAt;
}
