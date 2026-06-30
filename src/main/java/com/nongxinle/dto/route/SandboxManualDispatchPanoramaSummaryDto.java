package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SandboxManualDispatchPanoramaSummaryDto {
    private Integer onDutyDriverCount;
    private Integer simulatableDriverCount;
    private Integer directConfirmDriverCount;
    private Integer riskAckDriverCount;
    private Integer forbiddenDriverCount;
    private String summaryLine;
}
