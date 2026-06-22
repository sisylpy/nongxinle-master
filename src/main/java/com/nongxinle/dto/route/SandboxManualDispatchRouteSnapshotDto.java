package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class SandboxManualDispatchRouteSnapshotDto {
    private Integer stopCount;
    private Long totalDistanceM;
    private Long totalDurationS;
    private Long returnToDepotDurationS;
    private String routeSummary;
}
