package com.nongxinle.dto.platform.customer;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@ToString
public class PlatformCheckoutPaymentStatusResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer paymentId;
    private String checkoutToken;
    private String outTradeNo;
    private String status;
    /** 是否仍锁定购物车行（仅 PENDING 且未超时） */
    private Boolean locked;
    /** 是否已超时关闭（EXPIRED）或查询时刚被自动关闭 */
    private Boolean expired;
    private String knownTotal;
    private Integer pendingPriceItemCount;
    private Integer billId;
    private String billPayStatus;
    private String transactionId;
    private String paidAt;
    private List<Integer> orderIds;
    private String message;
}
