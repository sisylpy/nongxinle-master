package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class SandboxManualDispatchIncomingStopPreviewDto {
    /** 插入后在本路线中的送序（1-based）。 */
    private Integer seq;
    private Integer departmentId;
    private String customerName;
    private String plannedArrivalLabel;
    private String plannedArrivalAt;
}
