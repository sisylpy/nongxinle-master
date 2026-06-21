package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/** Phase 2b-1：调度可执行性警告（不落库） */
@Setter
@Getter
@ToString
public class RouteDispatchWarning {
    private String code;
    private String severity;
    private Integer driverUserId;
    private String driverName;
    private Integer routeId;
    private Integer stopId;
    private Integer taskId;
    private String departmentName;
    private Integer lateMinutes;
    private String message;
    private String suggestion;
}
