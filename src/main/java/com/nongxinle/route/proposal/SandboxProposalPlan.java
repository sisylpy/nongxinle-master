package com.nongxinle.route.proposal;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/** 分派中页面唯一 Proposal 主权（与 mixed mergedPlan / ActiveRoute 分离）。 */
@Getter
@Setter
public class SandboxProposalPlan {

    private List<ProposalDriverRoute> proposalRoutes = new ArrayList<ProposalDriverRoute>();
    private List<ProposalStop> unassignedStops = new ArrayList<ProposalStop>();
    private SandboxProposalPlanSummary summary = new SandboxProposalPlanSummary();
}
