package com.nongxinle.dto.platform.customer;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Map;

@Getter
@Setter
@ToString
public class PlatformCheckoutPayResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer paymentId;
    private String checkoutToken;
    private String knownTotal;
    private String payAmount;
    private String outTradeNo;
    /** 支付成功后才有 */
    private Integer billId;
    private String payStatus;
    private Boolean mockPay;
    private Boolean idempotent;
    private String message;
    /** 小程序 wx.requestPayment 参数 */
    private Map<String, String> wxPayParams;
}
