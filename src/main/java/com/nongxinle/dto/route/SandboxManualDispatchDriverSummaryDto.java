package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class SandboxManualDispatchDriverSummaryDto {
    private Integer driverUserId;
    private String driverName;
    private String dispatchStage;
    private String dispatchStageLabel;
}
