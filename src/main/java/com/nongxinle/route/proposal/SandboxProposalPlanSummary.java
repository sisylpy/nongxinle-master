package com.nongxinle.route.proposal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SandboxProposalPlanSummary {

    private int proposalRouteCount;
    private int assignedStopCount;
    private int unassignedStopCount;
    private int customerStopCount;
}
