package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class DisRouteReoptimizeRequest {
    private Integer planId;
    private Integer operatorUserId;
    private String depotLat;
    private String depotLng;
    private String costProviderType;
    private String optimizerType;
}
