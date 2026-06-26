package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SandboxDriverRouteEditAvailableCustomerDto {
    private String stopKey;
    private Integer departmentId;
    private String customerName;
    private String goodsSummary;
    private String constraintLabel;
}
