package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class DriverAvailableDto {
    private Integer driverUserId;
    private String driverName;
    private String driverPhone;
    private String dutyStatus;
    private String dutyDate;
}
