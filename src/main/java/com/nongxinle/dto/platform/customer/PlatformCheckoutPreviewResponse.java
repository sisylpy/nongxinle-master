package com.nongxinle.dto.platform.customer;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@ToString
public class PlatformCheckoutPreviewResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<PlatformCheckoutLineItem> confirmedLines;
    private List<PlatformCheckoutLineItem> pendingPriceLines;
    private Integer confirmedItemCount;
    /** 仅统计 priceConfirmStatus=PENDING 的行，不代表 assign 待分配 */
    private Integer pendingPriceItemCount;
    private String knownTotal;
    /** 本次应付 = knownTotal */
    private String payAmount;
    private String notice;
}
