package com.nongxinle.entity;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class GbDepartmentBillPaymentEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer gbBpId;
    private Integer gbBpBillId;
    private String gbBpPayPhase;
    private BigDecimal gbBpAmount;
    private String gbBpOutTradeNo;
    private String gbBpTransactionId;
    private String gbBpStatus;
    private String gbBpPaidAt;
    private String gbBpNotifyRaw;
    private String gbBpCreatedAt;
    private String gbBpUpdatedAt;
}
