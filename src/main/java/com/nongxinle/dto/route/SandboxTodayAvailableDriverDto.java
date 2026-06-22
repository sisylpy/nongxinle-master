package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SandboxTodayAvailableDriverDto {
    private Integer driverUserId;
    private String driverName;
    private String driverAvatarUrl;
    private String statusLabel;
    private String badgeLabel;
    private String routeHint;
    private String operationHint;
}
