package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class SandboxManualDispatchConstraintsDto {
    private Boolean manualDriverLocked;
    private Boolean manualSeqLocked;
    private Boolean requiredArrivalLocked;
    private Integer manualStopSeq;
    private String requiredLatestArrivalAt;
}
