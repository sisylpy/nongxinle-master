package com.nongxinle.route.dispatch.strategy;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FrozenStopAssignment {
    private Integer depFatherId;
    private String sandboxStopKey;
    private DispatchPlanningReason planningReason;
    /** 如 confirmed / executing / manualLocked — 仅 debug，不进 optimizer。 */
    private String frozenReason;
}
