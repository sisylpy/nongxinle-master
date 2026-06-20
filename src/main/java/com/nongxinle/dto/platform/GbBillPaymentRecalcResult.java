package com.nongxinle.dto.platform;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
public class GbBillPaymentRecalcResult {

    private Integer billId;
    private String billSource;
    private BigDecimal finalTotal;
    private BigDecimal knownTotal;
    private BigDecimal paidTotal;
    private BigDecimal supplementDue;
    private Integer pendingItemCount;
    private String payStatus;
    private boolean blocksNewOrder;
    private boolean noOp;
}
