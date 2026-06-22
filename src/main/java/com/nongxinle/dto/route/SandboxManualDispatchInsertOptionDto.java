package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@ToString
public class SandboxManualDispatchInsertOptionDto {
    private String insertPositionKey;
    private Integer insertStopSeq;
    private String label;
    private Boolean recommended;
    private Boolean manualSeqLocked;
    private SandboxManualDispatchRouteImpactDto routeImpact;
    private SandboxManualDispatchIncomingStopPreviewDto incomingStop;
    private SandboxManualDispatchRequiredArrivalDto requiredArrival;
    private List<SandboxManualDispatchStopImpactDto> stopImpacts = new ArrayList<SandboxManualDispatchStopImpactDto>();
    private String confirmMode;
    private List<String> riskHints = new ArrayList<String>();
}
