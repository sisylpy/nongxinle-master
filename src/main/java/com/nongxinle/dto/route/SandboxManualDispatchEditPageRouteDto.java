package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/** 人工路线编辑页 — 路线（当前路线或模拟路线）。 */
@Setter
@Getter
@ToString
public class SandboxManualDispatchEditPageRouteDto {
    private String routeSummary;
    private Long totalDistanceM;
    private Long totalDurationS;
    private Long totalDistanceDeltaM;
    private Long totalDurationDeltaS;
    private String returnToDepotLabel;
    private Long returnToDepotDurationDeltaS;
    private List<SandboxManualDispatchEditPageStopCardDto> stops = new ArrayList<SandboxManualDispatchEditPageStopCardDto>();
}
