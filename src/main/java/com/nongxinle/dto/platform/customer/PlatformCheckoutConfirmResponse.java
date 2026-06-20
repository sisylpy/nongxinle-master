package com.nongxinle.dto.platform.customer;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@ToString
public class PlatformCheckoutConfirmResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer billId;
    private String billTradeNo;
    private String knownTotal;
    private String paidTotal;
    /** bill.pendingItemCount：仅 priceConfirmStatus=PENDING 行数 */
    private Integer pendingPriceItemCount;
    private String supplementDue;
    private String payStatus;
    private Boolean idempotent;
    private String message;
    private List<PlatformCheckoutLineItem> lines;
}
