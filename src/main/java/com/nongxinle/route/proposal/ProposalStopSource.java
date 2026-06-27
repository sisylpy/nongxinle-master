package com.nongxinle.route.proposal;

/** 分派中 ProposalStop 来源（策略层，非司机状态）。 */
public enum ProposalStopSource {
    HISTORY,
    FALLBACK,
    OPTIMIZER,
    UNASSIGNED
}
