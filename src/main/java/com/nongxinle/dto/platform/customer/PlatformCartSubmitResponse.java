package com.nongxinle.dto.platform.customer;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@ToString
public class PlatformCartSubmitResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer billId;
    private String billTradeNo;
    private String supplierName;
    private Integer nxDistributerId;
    private String knownPayAmount;
    private Integer pendingItemCount;
    private Integer totalLineCount;
    private String billPayState;
    private String paidAmount;
    private String supplementDue;
    private String finalTotalEstimate;
    private Boolean payableNow;
    private String payPhase;
    private Boolean idempotent;
    private String message;
    private List<PlatformCartSubmitLineResponse> items;
}
