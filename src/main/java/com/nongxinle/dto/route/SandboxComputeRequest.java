package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class SandboxComputeRequest {
    private Integer disId;
    private String routeDate;
    private String batchCode;
    private String depotLat;
    private String depotLng;
}
