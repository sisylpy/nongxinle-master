package com.nongxinle.route.dispatch.strategy;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FallbackStopAssignment {
    private Integer depFatherId;
    private String sandboxStopKey;
    private DispatchPlanningReason planningReason;
    /** P1 {@code DeliveryHistoryPreferenceDto.reason} 或补充说明。 */
    private String historyReason;
}
