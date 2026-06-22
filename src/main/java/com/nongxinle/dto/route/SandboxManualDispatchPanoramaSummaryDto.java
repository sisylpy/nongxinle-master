package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class SandboxManualDispatchPanoramaSummaryDto {
    private Integer onDutyDriverCount;
    private Integer simulatableDriverCount;
    private Integer directConfirmDriverCount;
    private Integer riskAckDriverCount;
    private Integer forbiddenDriverCount;
    /** 人工调度页顶部摘要一行文案 */
    private String summaryLine;
}
