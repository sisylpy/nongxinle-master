package com.nongxinle.dto.platform.customer;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
public class PlatformCheckoutPaymentCancelRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer paymentId;
    private String outTradeNo;
    private Integer gbDepartmentId;
}
