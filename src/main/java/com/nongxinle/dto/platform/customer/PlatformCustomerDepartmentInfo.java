package com.nongxinle.dto.platform.customer;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Setter
@Getter
@ToString
public class PlatformCustomerDepartmentInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer departmentId;
    private String departmentName;
    private String address;
}
