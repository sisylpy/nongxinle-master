package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class MoveTaskRequest {
    private Integer taskId;
    private Integer assignedDriverUserId;
    private Integer manualStopSeq;
    private Integer operatorUserId;
    private String adjustReason;
}
