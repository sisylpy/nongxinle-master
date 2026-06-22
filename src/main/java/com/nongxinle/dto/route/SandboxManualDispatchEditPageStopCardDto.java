package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/** 人工路线编辑页 — 单个店卡。 */
@Setter
@Getter
@ToString
public class SandboxManualDispatchEditPageStopCardDto {
    private Integer seq;
    private Integer departmentId;
    private String sandboxStopKey;
    private String customerName;
    private Boolean insertedStop;
    private SandboxManualDispatchEditPageCustomerTimeWindowDto customerTimeWindow;
    private SandboxManualDispatchEditPageSystemEtaDto systemEta;
    private SandboxManualDispatchManualTimeConstraintDto manualTimeConstraint;
    private SandboxManualDispatchEditPageStopImpactDto impact;
    /** 人工约束一行摘要 */
    private String manualConstraintSummary;
}
