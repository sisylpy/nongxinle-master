package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@ToString
public class SandboxManualDispatchSimulateResponse {
    private String simulationId;
    private String simulationMode;
    private SandboxManualDispatchConstraintsDto manualConstraints;
    private SandboxManualDispatchCustomerContextDto customer;
    private SandboxManualDispatchDriverSummaryDto driver;
    private SandboxManualDispatchRouteSnapshotDto baseline;
    private List<SandboxManualDispatchInsertOptionDto> insertOptions = new ArrayList<SandboxManualDispatchInsertOptionDto>();
    private String recommendedOptionKey;
    /** 推荐/锁定方案上的必须送达可行性（与 insertOptions[].requiredArrival 一致）。 */
    private SandboxManualDispatchRequiredArrivalDto requiredArrival;
    private String confirmMode;
    private List<String> riskHints = new ArrayList<String>();
}
