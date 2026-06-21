package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class UnlockTaskRequest {
    private Integer taskId;
    private Integer operatorUserId;
    private String adjustReason;
}
