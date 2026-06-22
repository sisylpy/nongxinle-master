package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class SandboxManualDispatchRouteImpactDto {
    private Long totalDistanceM;
    private Long totalDurationS;
    private Long totalDistanceDeltaM;
    private Long totalDurationDeltaS;
    private Long returnToDepotDurationS;
    private Long returnToDepotDurationDeltaS;
    private String routeSummary;
    private String returnToDepotLabel;
}
