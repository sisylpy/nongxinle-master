package com.nongxinle.dto.platform.customer;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
public class PlatformBillPaymentIntentResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer billId;
    private Integer paymentId;
    private String payPhase;
    private String payAmount;
    private String outTradeNo;
    private Boolean mockPay;
    private String message;
}
