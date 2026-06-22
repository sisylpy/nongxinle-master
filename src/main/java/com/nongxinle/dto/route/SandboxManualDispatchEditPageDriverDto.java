package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/** 人工路线编辑页 — 所选司机。 */
@Setter
@Getter
@ToString
public class SandboxManualDispatchEditPageDriverDto {
    private Integer driverUserId;
    private String driverName;
    private String dispatchStage;
    private String dispatchStageLabel;
    private String dutyStatus;
    private Boolean canSimulate;
    private Boolean canConfirm;
    private String confirmMode;
    private String confirmModeLabel;
    private List<String> riskHints = new ArrayList<String>();
    private String blockedReason;
    private String routeSummary;
    private String operationHint;
}
