package com.nongxinle.dispatch.core.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DispatchDriver {

    private Integer driverUserId;
    private String driverName;
    private String driverPhone;
    private String dutyStatus;
    private String dispatchPhaseLabel;
}
