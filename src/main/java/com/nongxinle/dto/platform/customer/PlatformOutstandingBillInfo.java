package com.nongxinle.dto.platform.customer;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
public class PlatformOutstandingBillInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private Boolean blocked;
    private Integer billId;
    private String billPayState;
    private String payPhase;
    private String payAmount;
    private String supplierName;
    private String message;
}
