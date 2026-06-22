package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class SandboxManualDispatchCustomerContextDto {
    private Integer departmentId;
    private String sandboxStopKey;
    private String customerName;
    private String goodsSummary;
}
