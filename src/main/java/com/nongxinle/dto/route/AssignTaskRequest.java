package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class AssignTaskRequest {
    private Integer taskId;
    private Integer assignedDriverUserId;
    private Integer operatorUserId;
    private String assignReason;
    private Integer manualStopSeq;
}
