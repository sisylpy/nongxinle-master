package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SandboxTodayMapMissingStopDto {
    private String customerName;
    private Integer departmentId;
    private Integer depFatherId;
    private String driverName;
    private Integer driverUserId;
    private String assignmentLabel;
    private String reason = "缺少有效坐标";
}
